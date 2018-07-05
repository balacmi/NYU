package nyu.matsim.bikesharing.qsim;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.core.config.groups.PlansCalcRouteConfigGroup;
import org.matsim.core.mobsim.framework.MobsimAgent;
import org.matsim.core.mobsim.framework.PlanAgent;
import org.matsim.core.mobsim.qsim.agents.WithinDayAgentUtils;
import org.matsim.core.mobsim.qsim.interfaces.DepartureHandler;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.utils.geometry.CoordUtils;

import com.google.inject.Inject;

import nyu.matsim.bikesharing.infrastructure.BikeshareService;
import nyu.matsim.bikesharing.infrastructure.BikesharingVehicle;

public class BikeshareDepartureHandler implements DepartureHandler {

	@Inject
	private BikeshareService bikeshareService;

	@Inject
	private Network network;

	@Inject
	private PlansCalcRouteConfigGroup pcrcg;
	@Override
	public boolean handleDeparture(double now, MobsimAgent agent, Id<Link> linkId) {
		if (agent instanceof PlanAgent) {
			if (agent.getMode().startsWith("access_walk_bike")) {
				// Plan plan = WithinDayAgentUtils.getModifiablePlan( agent ) ;
				Link link = network.getLinks().get(linkId);
				Plan plan = WithinDayAgentUtils.getModifiablePlan(agent);
				final Integer planElementsIndex = WithinDayAgentUtils.getCurrentPlanElementIndex(agent);
				final Leg accessLeg = (Leg) plan.getPlanElements().get(planElementsIndex);
				final Leg leg = (Leg) plan.getPlanElements().get(planElementsIndex + 1);
				final Leg egressLeg = (Leg) plan.getPlanElements().get(planElementsIndex + 2);
				Id<BikesharingVehicle> bikeId = bikeshareService.getAndRemoveClosestBike(link.getCoord(), agent.getId());
				if (bikeId == null) {
					//or we can replace this leg with a walk leg
					agent.setStateToAbort(now);
					return true;
				}
				Coord bikeCoord = this.bikeshareService.getBikeCoordMap().get(bikeId);

				/*if (CoordUtils.calcEuclideanDistance(link.getCoord(), bikeCoord) > 500) {
					accessLeg.setMode("walk");
					accessLeg.setTravelTime(CoordUtils.calcEuclideanDistance(link.getCoord(),
							network.getLinks().get(leg.getRoute().getEndLinkId()).getCoord()) * 1.05 / 1.38);
					plan.getPlanElements().remove(planElementsIndex + 1);
					plan.getPlanElements().remove(planElementsIndex + 2);
					return true;
				}*/
				double beelineDistanceFactorWalk = pcrcg.getBeelineDistanceFactors().get("walk");
				double speedWalk = pcrcg.getTeleportedModeSpeeds().get("walk");
				double accessTime = CoordUtils.calcEuclideanDistance(link.getCoord(), bikeCoord) * beelineDistanceFactorWalk / speedWalk;

				accessLeg.setTravelTime(accessTime);
				accessLeg.getRoute().setTravelTime(accessTime);
				accessLeg.getRoute().setEndLinkId(NetworkUtils.getNearestLinkExactly(network, bikeCoord).getId());
				
				double beelineDistanceFactorBike = pcrcg.getBeelineDistanceFactors().get("bike");
				double speedBike = pcrcg.getTeleportedModeSpeeds().get("bike");
				double travelTime = CoordUtils.calcEuclideanDistance(bikeCoord,
						network.getLinks().get(leg.getRoute().getEndLinkId()).getCoord()) * beelineDistanceFactorBike / speedBike;
				leg.setTravelTime(travelTime);
				leg.getRoute().setTravelTime(travelTime);
				leg.getRoute().setStartLinkId(NetworkUtils.getNearestLinkExactly(network, bikeCoord).getId());
				egressLeg.setTravelTime(0.0);
				egressLeg.getRoute().setTravelTime(0.0);

			}

		}

		return false;
	}

}
