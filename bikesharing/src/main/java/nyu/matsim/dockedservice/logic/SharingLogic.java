package nyu.matsim.dockedservice.logic;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.IdMap;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.mobsim.framework.MobsimAgent;
import org.matsim.core.mobsim.framework.PlanAgent;
import org.matsim.core.mobsim.qsim.agents.WithinDayAgentUtils;
import org.matsim.core.router.LinkWrapperFacility;
import org.matsim.core.router.RoutingModule;
import org.matsim.core.router.TripRouter;
import org.matsim.core.router.TripStructureUtils;
import org.matsim.facilities.Facility;

import com.google.common.base.Verify;

import nyu.matsim.dockedservice.service.SharingService;
import nyu.matsim.dockedservice.service.SharingUtils;
import nyu.matsim.dockedservice.service.SharingVehicle;
import nyu.matsim.dockedservice.service.events.SharingDropoffEvent;
import nyu.matsim.dockedservice.service.events.SharingFailedDropoffEvent;
import nyu.matsim.dockedservice.service.events.SharingFailedPickupEvent;
import nyu.matsim.dockedservice.service.events.SharingPickupEvent;

public class SharingLogic {
	private final IdMap<Person, SharingVehicle> activeVehicles = new IdMap<>(Person.class);

	private final RoutingModule accessEgressRoutingModule;
	private final RoutingModule mainModeRoutingModule;

	private final Network network;
	private final PopulationFactory populationFactory;
	private final Config config;
	private final EventsManager eventsManager;

	private final SharingService service;

	public SharingLogic(SharingService service, RoutingModule accessEgressRoutingModule,
			RoutingModule mainModeRoutingModule, Scenario scenario, EventsManager eventsManager) {
		this.service = service;
		this.eventsManager = eventsManager;

		this.accessEgressRoutingModule = accessEgressRoutingModule;
		this.mainModeRoutingModule = mainModeRoutingModule;

		this.config = scenario.getConfig();
		this.network = scenario.getNetwork();
		this.populationFactory = scenario.getPopulation().getFactory();
	}

	/**
	 * Agent tries to pick up a vehicle.
	 * 
	 * If it returns false, agent needs to abort!
	 * 
	 * @param agent
	 */
	public boolean tryPickupVehicle(double now, MobsimAgent agent) {
		Plan plan = WithinDayAgentUtils.getModifiablePlan(agent);
		int pickupActivityIndex = WithinDayAgentUtils.getCurrentPlanElementIndex(agent);

		Verify.verify(plan.getPlanElements().get(pickupActivityIndex) instanceof Activity);
		Activity pickupActivity = (Activity) plan.getPlanElements().get(pickupActivityIndex);
		Verify.verify(pickupActivity.getType().equals(SharingUtils.PICKUP_ACTIVITY));

		// Find closest vehicle and hope it is at the current station / link
		Optional<SharingVehicle> selectedVehicle = service.findClosestVehicle(agent);

		if (selectedVehicle.isPresent()) {
			Id<Link> vehicleLinkId = selectedVehicle.get().getLink().getId();

			if (vehicleLinkId.equals(pickupActivity.getLinkId())) {
				// We are at the current location. We can pick up the vehicle.
				service.pickupVehicle(selectedVehicle.get(), agent);
				activeVehicles.put(agent.getId(), selectedVehicle.get());

				eventsManager.processEvent(new SharingPickupEvent(now, service.getId(), agent.getId(), vehicleLinkId,
						selectedVehicle.get().getId(), service.getStationId(vehicleLinkId)));
			} else {
				// The closest vehicle is not here, we need to get there after this activity ...

				eventsManager.processEvent(new SharingFailedPickupEvent(now, service.getId(), agent.getId(),
						vehicleLinkId, service.getStationId(vehicleLinkId)));

				// Remove everything until the dropoff activity
				int dropoffActivityIndex = findDropoffActivityIndex(pickupActivityIndex, plan);
				Activity dropoffActivity = (Activity) plan.getPlanElements().get(dropoffActivityIndex);

				plan.getPlanElements().subList(pickupActivityIndex + 1, dropoffActivityIndex).clear();

				// Create new plan elements
				List<PlanElement> updatedElements = new LinkedList<>();

				// 1) Leg to pickup activity
				List<? extends PlanElement> accessElements = routeAccessEgressStage(pickupActivity.getLinkId(),
						vehicleLinkId, now, agent);
				updatedElements.addAll(accessElements);

				for (PlanElement planElement : accessElements) {
					now = TripRouter.calcEndOfPlanElement(now, planElement, config);
				}

				// 2) Pickup activity
				Activity updatedPickupActivity = createPickupActivity(now, vehicleLinkId);
				updatedElements.add(updatedPickupActivity);
				now = TripRouter.calcEndOfPlanElement(now, updatedPickupActivity, config);

				// 3) Leg to dropoff activity
				List<? extends PlanElement> mainElements = routeMainStage(vehicleLinkId, dropoffActivity.getLinkId(),
						now, agent);
				updatedElements.addAll(mainElements);

				// Insert new plan elements
				plan.getPlanElements().addAll(pickupActivityIndex + 1, updatedElements);
			}
			
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Agent tries to drop off a vehicle.
	 * 
	 * If it returns false, agent needs to abort!
	 * 
	 * @param agent
	 */
	public void tryDropoffVehicle(double now, MobsimAgent agent) {
		Plan plan = WithinDayAgentUtils.getModifiablePlan(agent);
		int dropoffActivityIndex = WithinDayAgentUtils.getCurrentPlanElementIndex(agent);

		Verify.verify(plan.getPlanElements().get(dropoffActivityIndex) instanceof Activity);
		Activity dropoffActivity = (Activity) plan.getPlanElements().get(dropoffActivityIndex);
		Verify.verify(dropoffActivity.getType().equals(SharingUtils.DROPOFF_ACTIVITY));

		SharingVehicle vehicle = activeVehicles.get(agent.getId());
		Verify.verifyNotNull(vehicle);

		// Find closest place to drop off the vehicle and hope we're already there ...
		Id<Link> closestDropoffLinkId = service.findClosestDropoffLocation(vehicle, agent);

		if (closestDropoffLinkId.equals(dropoffActivity.getLinkId())) {
			// We're at the right spot. Drop the vehicle here.
			service.dropoffVehicle(vehicle, agent);
			activeVehicles.remove(agent.getId());

			eventsManager.processEvent(new SharingDropoffEvent(now, service.getId(), agent.getId(),
					closestDropoffLinkId, vehicle.getId(), service.getStationId(closestDropoffLinkId)));
		} else {
			// We cannot drop the vehicle here, so let's try the proposed place

			eventsManager.processEvent(new SharingFailedDropoffEvent(now, service.getId(), agent.getId(),
					closestDropoffLinkId, vehicle.getId(), service.getStationId(closestDropoffLinkId)));

			// Remove everything until the end of the trip
			int destinationActivityIndex = findNextOrdinaryActivityIndex(dropoffActivityIndex, plan);
			Activity destinationActivity = (Activity) plan.getPlanElements().get(destinationActivityIndex);

			plan.getPlanElements().subList(dropoffActivityIndex + 1, destinationActivityIndex).clear();

			// Create new plan elements
			List<PlanElement> updatedElements = new LinkedList<>();

			// 1) Leg to new dropoff activity
			List<? extends PlanElement> mainElements = routeMainStage(dropoffActivity.getLinkId(), closestDropoffLinkId,
					now, agent);
			updatedElements.addAll(mainElements);

			for (PlanElement planElement : mainElements) {
				now = TripRouter.calcEndOfPlanElement(now, planElement, config);
			}

			// 2) Dropoff activity
			Activity updatedPickupActivity = createDropoffActivity(now, closestDropoffLinkId);
			updatedElements.add(updatedPickupActivity);
			now = TripRouter.calcEndOfPlanElement(now, updatedPickupActivity, config);

			// 3) Leg to destination
			List<? extends PlanElement> accessElements = routeAccessEgressStage(closestDropoffLinkId,
					destinationActivity.getLinkId(), now, agent);
			updatedElements.addAll(accessElements);

			// Insert new plan elements
			plan.getPlanElements().addAll(dropoffActivityIndex + 1, updatedElements);
		}
	}

	private Activity createPickupActivity(double now, Id<Link> linkId) {
		Activity activity = populationFactory.createActivityFromLinkId(SharingUtils.PICKUP_ACTIVITY, linkId);
		activity.setStartTime(now);
		activity.setMaximumDuration(SharingUtils.INTERACTION_DURATION);
		SharingUtils.setServiceId(activity, service.getId());
		return activity;
	}

	private Activity createDropoffActivity(double now, Id<Link> linkId) {
		Activity activity = populationFactory.createActivityFromLinkId(SharingUtils.DROPOFF_ACTIVITY, linkId);
		activity.setStartTime(now);
		activity.setMaximumDuration(SharingUtils.INTERACTION_DURATION);
		SharingUtils.setServiceId(activity, service.getId());
		return activity;
	}

	private List<? extends PlanElement> routeAccessEgressStage(Id<Link> originId, Id<Link> destinationId,
			double departureTime, MobsimAgent agent) {
		Facility originFacility = new LinkWrapperFacility(network.getLinks().get(originId));
		Facility destinationFacility = new LinkWrapperFacility(network.getLinks().get(destinationId));

		return accessEgressRoutingModule.calcRoute(originFacility, destinationFacility, departureTime,
				((PlanAgent) agent).getCurrentPlan().getPerson());
	}

	private List<? extends PlanElement> routeMainStage(Id<Link> originId, Id<Link> destinationId, double departureTime,
			MobsimAgent agent) {
		Facility originFacility = new LinkWrapperFacility(network.getLinks().get(originId));
		Facility destinationFacility = new LinkWrapperFacility(network.getLinks().get(destinationId));

		return mainModeRoutingModule.calcRoute(originFacility, destinationFacility, departureTime,
				((PlanAgent) agent).getCurrentPlan().getPerson());
	}

	private int findNextOrdinaryActivityIndex(int currentIndex, Plan plan) {
		List<PlanElement> elements = plan.getPlanElements();

		for (int i = currentIndex + 1; i < elements.size(); i++) {
			PlanElement element = elements.get(i);

			if (element instanceof Activity) {
				Activity activity = (Activity) element;

				if (!TripStructureUtils.isStageActivityType(activity.getType())) {
					return i;
				}
			}
		}

		throw new IllegalStateException("No ordinary activity found");
	}

	private int findDropoffActivityIndex(int pickupActivityIndex, Plan plan) {
		List<PlanElement> elements = plan.getPlanElements();

		for (int i = pickupActivityIndex + 1; i < elements.size(); i++) {
			PlanElement element = elements.get(i);

			if (element instanceof Activity) {
				Activity activity = (Activity) element;

				if (activity.getType().equals(SharingUtils.DROPOFF_ACTIVITY)) {
					return i;
				}
			}
		}

		throw new IllegalStateException("No dropoff activity found");
	}
}
