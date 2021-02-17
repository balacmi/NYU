package nyu.matsim.dockedservice.qsim;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.groups.PlansCalcRouteConfigGroup;
import org.matsim.core.mobsim.framework.MobsimAgent;
import org.matsim.core.mobsim.framework.PlanAgent;
import org.matsim.core.mobsim.qsim.agents.WithinDayAgentUtils;
import org.matsim.core.mobsim.qsim.interfaces.DepartureHandler;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.utils.geometry.CoordUtils;

import com.google.inject.Inject;

import nyu.matsim.dockedservice.events.NoBikeEvent;
import nyu.matsim.dockedservice.events.NoParkingEvent;
import nyu.matsim.dockedservice.infrastructure.BikeshareServiceInterface;
import nyu.matsim.dockedservice.infrastructure.BikesharingVehicle;

public class BikeshareDepartureHandler implements DepartureHandler {

	private BikeshareServiceInterface bikeshareService;

	private Network network;

	private PlansCalcRouteConfigGroup pcrcg;

	private EventsManager eventsManager;

	@Inject
	public BikeshareDepartureHandler(Scenario scenario, EventsManager eventsManager, Network network,
			BikeshareServiceInterface bikeshareService, PlansCalcRouteConfigGroup pcrcg) {
		this.network = network;
		this.pcrcg = pcrcg;
		this.bikeshareService = bikeshareService;
		this.eventsManager = eventsManager;
	}

	@Override
	public boolean handleDeparture(double now, MobsimAgent agent, Id<Link> linkId) {
		if (agent instanceof PlanAgent) {
			if (agent.getMode().startsWith("access_walk_bike")) {
				Link link = network.getLinks().get(linkId);
				Plan plan = WithinDayAgentUtils.getModifiablePlan(agent);
				final Integer planElementsIndex = WithinDayAgentUtils.getCurrentPlanElementIndex(agent);
				final Leg accessLeg = (Leg) plan.getPlanElements().get(planElementsIndex);
				final Leg leg = (Leg) plan.getPlanElements().get(planElementsIndex + 1);
				final Leg egressLeg = (Leg) plan.getPlanElements().get(planElementsIndex + 2);
				Id<BikesharingVehicle> bikeId = bikeshareService.getAndRemoveClosestBike(link.getCoord(),
						agent.getId());
				if (bikeId == null) {
					agent.setStateToAbort(now);
					this.eventsManager.processEvent(new NoBikeEvent(now, link.getCoord(), agent.getId()));
					// or we can replace this leg with a walk leg
					// TODO: currently the agent just never arrives with the access leg
					// the scoring does not have information that this agent is aborted
					// probably it would be better to have a fallback mode
					// finally we need to generate events that the rental did not occur
					// similar needs to happen for the parking mechanics
					// agent.setStateToAbort(now);
					// Leg legToModify = (Leg)WithinDayAgentUtils.getCurrentPlanElement(agent);
					// legToModify.setMode("fallback_bikeshare");
					// legToModify.setTravelTime(CoordUtils.calcEuclideanDistance(link.getCoord(),
					// network.getLinks().get(leg.getRoute().getEndLinkId()).getCoord()) * 1.05 /
					// 1.38);
					// plan.getPlanElements().remove(planElementsIndex + 1);
					// plan.getPlanElements().remove(planElementsIndex + 1);

					// teleportationEngine.handleDeparture(now, agent, linkId);

					return true;
				}
				Coord bikeCoord = this.bikeshareService.getBikeCoordMap().get(bikeId);

				double beelineDistanceFactorWalk = pcrcg.getBeelineDistanceFactors().get("walk");
				double speedWalk = pcrcg.getTeleportedModeSpeeds().get("walk");
				double accessTime = CoordUtils.calcEuclideanDistance(link.getCoord(), bikeCoord)
						* beelineDistanceFactorWalk / speedWalk;

				accessLeg.setTravelTime(accessTime);
				accessLeg.getRoute().setTravelTime(accessTime);
				accessLeg.getRoute().setEndLinkId(NetworkUtils.getNearestLinkExactly(network, bikeCoord).getId());

				double beelineDistanceFactorBike = pcrcg.getBeelineDistanceFactors().get("bike");
				double speedBike = pcrcg.getTeleportedModeSpeeds().get("bike");
				Link destinationLink = network.getLinks().get(leg.getRoute().getEndLinkId());
				Coord parkingCoord = this.bikeshareService.reserveClosestParkingSpot(destinationLink.getCoord(),
						agent.getId());

				if (parkingCoord == null) {
					// or we can replace this leg with a walk leg
					agent.setStateToAbort(now);
					this.eventsManager.processEvent(new NoParkingEvent(now, destinationLink.getCoord(), agent.getId()));
					// accessLeg.setMode("fallback_bikeshare");
					// accessLeg.setTravelTime(CoordUtils.calcEuclideanDistance(link.getCoord(),
					// network.getLinks().get(leg.getRoute().getEndLinkId()).getCoord()) * 1.05 /
					// 1.38);
					// plan.getPlanElements().remove(planElementsIndex + 1);
					// plan.getPlanElements().remove(planElementsIndex + 1);
					// teleportationEngine.handleDeparture(now, agent, linkId);

					return true;
				}

				double travelTime = CoordUtils.calcEuclideanDistance(bikeCoord, parkingCoord)
						* beelineDistanceFactorBike / speedBike;
				leg.setTravelTime(travelTime);
				leg.getRoute().setTravelTime(travelTime);
				leg.getRoute().setStartLinkId(NetworkUtils.getNearestLinkExactly(network, bikeCoord).getId());
				Link parkingLink = NetworkUtils.getNearestLinkExactly(network, parkingCoord);
				leg.getRoute().setEndLinkId(parkingLink.getId());

				double egressTime = CoordUtils.calcEuclideanDistance(parkingCoord, destinationLink.getCoord())
						* beelineDistanceFactorWalk / speedWalk;

				egressLeg.getRoute().setStartLinkId(parkingLink.getId());
				egressLeg.setTravelTime(egressTime);
				egressLeg.getRoute().setTravelTime(egressTime);

			}

		}

		return false;
	}

}
