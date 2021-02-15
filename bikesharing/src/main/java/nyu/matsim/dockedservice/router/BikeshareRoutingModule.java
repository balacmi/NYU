package nyu.matsim.dockedservice.router;

import java.util.ArrayList;
import java.util.List;

import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.Route;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.population.routes.RouteUtils;
import org.matsim.core.router.RoutingModule;
import org.matsim.facilities.Facility;

public class BikeshareRoutingModule implements RoutingModule {

	private final static String BIKE_INTERACTION = "bike_interaction";

	public List<? extends PlanElement> calcRoute(Facility fromFacility, Facility toFacility, double departureTime,
			Person person) {
		final List<PlanElement> trip = new ArrayList<PlanElement>();
		final Leg accessLeg = PopulationUtils.createLeg("access_walk_bike");
		Route accessRoute = RouteUtils.createGenericRouteImpl(fromFacility.getLinkId(), toFacility.getLinkId());
		accessLeg.setRoute(accessRoute);
		final Activity accessActivity = PopulationUtils.createActivityFromLinkId(BIKE_INTERACTION,
				fromFacility.getLinkId());
		final Leg bikeshareLeg = PopulationUtils.createLeg("bikeshare");
		Route bikeRoute = RouteUtils.createGenericRouteImpl(fromFacility.getLinkId(), toFacility.getLinkId());
		bikeshareLeg.setRoute(bikeRoute);
		final Activity egressActivity = PopulationUtils.createActivityFromLinkId(BIKE_INTERACTION,
				toFacility.getLinkId());
		final Leg egressLeg = PopulationUtils.createLeg("egress_walk_bike");
		Route egressRoute = RouteUtils.createGenericRouteImpl(fromFacility.getLinkId(), toFacility.getLinkId());
		egressLeg.setRoute(egressRoute);
		trip.add(accessLeg);
		// trip.add(accessActivity);
		trip.add(bikeshareLeg);
		// trip.add(egressActivity);
		trip.add(egressLeg);
		return trip;
	}

}
