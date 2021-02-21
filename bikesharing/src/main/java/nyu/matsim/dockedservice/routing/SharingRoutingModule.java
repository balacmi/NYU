package nyu.matsim.dockedservice.routing;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.core.config.Config;
import org.matsim.core.router.LinkWrapperFacility;
import org.matsim.core.router.RoutingModule;
import org.matsim.core.router.TripRouter;
import org.matsim.facilities.Facility;

import nyu.matsim.dockedservice.service.SharingService;
import nyu.matsim.dockedservice.service.SharingUtils;

public class SharingRoutingModule implements RoutingModule {
	private final RoutingModule accessEgressRoutingModule;
	private final RoutingModule mainModeRoutingModule;

	private final InteractionFinder interactionFinder;
	private final Config config;
	private final Network network;
	private final PopulationFactory populationFactory;

	private final Id<SharingService> serviceId;

	public SharingRoutingModule(Scenario scenario, RoutingModule accessEgressRoutingModule,
			RoutingModule mainModeRoutingModule, InteractionFinder interactionFinder, Id<SharingService> serviceId) {
		this.interactionFinder = interactionFinder;
		this.accessEgressRoutingModule = accessEgressRoutingModule;
		this.mainModeRoutingModule = mainModeRoutingModule;
		this.config = scenario.getConfig();
		this.network = scenario.getNetwork();
		this.serviceId = serviceId;
		this.populationFactory = scenario.getPopulation().getFactory();
	}

	@Override
	public List<? extends PlanElement> calcRoute(Facility fromFacility, Facility toFacility, double departureTime,
			Person person) {
		List<PlanElement> allElements = new LinkedList<>();

		Optional<Id<Link>> pickupLinkId = interactionFinder.findPickup(fromFacility);
		Optional<Id<Link>> dropoffLinkId = interactionFinder.findDropoff(toFacility);

		if (pickupLinkId.isEmpty() || dropoffLinkId.isEmpty()) {
			return null;
		}

		if (pickupLinkId.get().equals(dropoffLinkId.get())) {
			return null;
		}

		// Route pickup stage

		List<? extends PlanElement> pickupElements = routeAccessEgressStage(fromFacility.getLinkId(),
				pickupLinkId.get(), departureTime, person);
		allElements.addAll(pickupElements);

		for (PlanElement planElement : pickupElements) {
			departureTime = TripRouter.calcEndOfPlanElement(departureTime, planElement, config);
		}

		// Pickup activity
		Activity pickupActivity = createPickupActivity(departureTime, pickupLinkId.get());
		pickupActivity.setStartTime(departureTime);
		allElements.add(pickupActivity);

		departureTime = TripRouter.calcEndOfPlanElement(departureTime, pickupActivity, config);

		// Route main stage

		List<? extends PlanElement> mainElements = routeMainStage(pickupActivity.getLinkId(), dropoffLinkId.get(),
				departureTime, person);
		allElements.addAll(mainElements);

		for (PlanElement planElement : pickupElements) {
			departureTime = TripRouter.calcEndOfPlanElement(departureTime, planElement, config);
		}

		// Dropoff activity
		Activity dropoffActivity = createDropoffActivity(departureTime, dropoffLinkId.get());
		dropoffActivity.setStartTime(departureTime);
		allElements.add(dropoffActivity);

		departureTime = TripRouter.calcEndOfPlanElement(departureTime, dropoffActivity, config);

		// Route dropoff stage

		List<? extends PlanElement> dropoffElements = routeAccessEgressStage(dropoffActivity.getLinkId(),
				toFacility.getLinkId(), departureTime, person);
		allElements.addAll(dropoffElements);

		return allElements;
	}

	// TODO: The following two functions are almost an exact replicate of the
	// functions in UserLogic. Try to conslidate.

	private List<? extends PlanElement> routeAccessEgressStage(Id<Link> originId, Id<Link> destinationId,
			double departureTime, Person person) {
		Facility originFacility = new LinkWrapperFacility(network.getLinks().get(originId));
		Facility destinationFacility = new LinkWrapperFacility(network.getLinks().get(destinationId));

		return accessEgressRoutingModule.calcRoute(originFacility, destinationFacility, departureTime, person);
	}

	private List<? extends PlanElement> routeMainStage(Id<Link> originId, Id<Link> destinationId, double departureTime,
			Person person) {
		Facility originFacility = new LinkWrapperFacility(network.getLinks().get(originId));
		Facility destinationFacility = new LinkWrapperFacility(network.getLinks().get(destinationId));

		return mainModeRoutingModule.calcRoute(originFacility, destinationFacility, departureTime, person);
	}

	private Activity createPickupActivity(double now, Id<Link> linkId) {
		Activity activity = populationFactory.createActivityFromLinkId(SharingUtils.PICKUP_ACTIVITY, linkId);
		activity.setStartTime(now);
		activity.setMaximumDuration(SharingUtils.INTERACTION_DURATION);
		SharingUtils.setServiceId(activity, serviceId);
		return activity;
	}

	private Activity createDropoffActivity(double now, Id<Link> linkId) {
		Activity activity = populationFactory.createActivityFromLinkId(SharingUtils.DROPOFF_ACTIVITY, linkId);
		activity.setStartTime(now);
		activity.setMaximumDuration(SharingUtils.INTERACTION_DURATION);
		SharingUtils.setServiceId(activity, serviceId);
		return activity;
	}
}
