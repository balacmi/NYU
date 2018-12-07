package nyu.matsim.ptrouter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.config.groups.PlansCalcRouteConfigGroup;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.facilities.ActivityFacilities;
import org.matsim.facilities.ActivityFacilitiesFactory;
import org.matsim.facilities.ActivityFacility;

import ch.ethz.matsim.baseline_scenario.transit.connection.DefaultTransitConnectionFinder;
import ch.ethz.matsim.baseline_scenario.transit.routing.DefaultEnrichedTransitRouter;
import ch.ethz.matsim.baseline_scenario.transit.routing.EnrichedTransitRoute;
import ch.ethz.matsim.baseline_scenario.zurich.cutter.utils.DefaultDepartureFinder;
import ch.sbb.matsim.routing.pt.raptor.SwissRailRaptorFactory;

/**
 * Hello world!
 *
 */
public class Router extends Thread {
	private SwissRailRaptorFactory factory;
	private Set<Data> result;
	private String s;
	private Scenario scenario;

	public Router(SwissRailRaptorFactory factory, Set<Data> r, Scenario scenario) {
		this.factory = factory;
		this.result = r;
		this.scenario = scenario;
	}

	private synchronized void addData(Data d) {
		this.result.add(d);
	}

	public Set<Data> getData() {
		return this.result;
	}

	public void run() {

		DefaultTransitConnectionFinder connectionFinder = new DefaultTransitConnectionFinder(
				new DefaultDepartureFinder());
		DefaultEnrichedTransitRouter router = new DefaultEnrichedTransitRouter(factory.get(),
				scenario.getTransitSchedule(), connectionFinder, scenario.getNetwork(), 1.3, 0.0);

		String[] arr = s.split(",");
		Coord coordStart = new Coord(Double.parseDouble(arr[6]), Double.parseDouble(arr[7]));
		Link lStart = NetworkUtils.getNearestLink(scenario.getNetwork(), coordStart);

		Coord coordEnd = new Coord(Double.parseDouble(arr[10]), Double.parseDouble(arr[11]));
		Link lEnd = NetworkUtils.getNearestLink(scenario.getNetwork(), coordEnd);

		final ActivityFacilities facilities = scenario.getActivityFacilities();
		final ActivityFacilitiesFactory ff = facilities.getFactory();
		// FakeFacility facStart = new FakeFacility(coordStart);
		// FakeFacility facEnd = new FakeFacility(coordEnd);
		ActivityFacility facStart = ff.createActivityFacility(Id.create(0, ActivityFacility.class), coordStart,
				lStart.getId());
		ActivityFacility facEnd = ff.createActivityFacility(Id.create(1, ActivityFacility.class), coordEnd,
				lEnd.getId());
		List<Leg> legs = router.calculateRoute(facStart, facEnd, Double.parseDouble(arr[12]), null);

		Data d = new Data();
		d.id = arr[1];
		d.travelTime = legs.get(legs.size() - 1).getDepartureTime() + legs.get(legs.size() - 1).getTravelTime()
				- Double.parseDouble(arr[12]);

		if (legs.size() > 1) {
			d.transit = true;
			d.accessTime = legs.get(0).getTravelTime();
			d.egressTime = legs.get(legs.size() - 1).getTravelTime();
			for (Leg leg : legs) {
				if (leg.getMode().equals("pt")) {
					d.inVehicleTime += ((EnrichedTransitRoute) leg.getRoute()).getInVehicleTime();
					d.transferTime += ((EnrichedTransitRoute) leg.getRoute()).getWaitingTime();
					d.distance += ((EnrichedTransitRoute) leg.getRoute()).getDistance();
					d.transfers++;
				} else if (leg.getMode().equals("transit_walk")) {
					d.transferTime += leg.getTravelTime();
				}
			}
		}
		this.addData(d);
	}

	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
		BufferedReader readLink = IOUtils.getBufferedReader(args[0]);
		readLink.readLine();
		String s = readLink.readLine();
		Config config = ConfigUtils.createConfig();
		config.network().setInputFile(args[1]);

		final double travelingWalk = -6.0D;
		((PlanCalcScoreConfigGroup) config.getModule("planCalcScore")).getModes().get(TransportMode.walk)
				.setMarginalUtilityOfTraveling(travelingWalk);
		PlansCalcRouteConfigGroup routeConfigGroup = config.plansCalcRoute();
		routeConfigGroup.getModeRoutingParams().get("walk").setBeelineDistanceFactor(1.25);
		routeConfigGroup.getModeRoutingParams().get("walk").setTeleportedModeSpeed(1.11 / 0.3048);
		((PlanCalcScoreConfigGroup) config.getModule("planCalcScore")).setUtilityOfLineSwitch(-0.0D);

		config.transit().setTransitScheduleFile(args[2]);

		// config.transit().setVehiclesFile("C:\\Users\\balacm\\Downloads\\vehicle.xml");
		// config.transit().setInputScheduleCRS("W");
		config.transit().setUseTransit(true);
		config.transitRouter().setSearchRadius(4000.0);
		config.transitRouter().setMaxBeelineWalkConnectionDistance(300.0);

		// config.transitRouter().setAdditionalTransferTime(60.0);
		final Scenario scenario = ScenarioUtils.loadScenario(config);

		SwissRailRaptorFactory factory = new SwissRailRaptorFactory(scenario.getTransitSchedule(), config);

		// DefaultTransitConnectionFinder connectionFinder = new
		// DefaultTransitConnectionFinder(new DefaultDepartureFinder());
		// DefaultEnrichedTransitRouter router = new
		// DefaultEnrichedTransitRouter(factory.get(), scenario.getTransitSchedule(),
		// connectionFinder, scenario.getNetwork(), 1.3, 0.0);

		List<Thread> threads = new LinkedList<>();
		Set<Data> res = Collections.synchronizedSet(new HashSet<Data>());

		for (int i = 0; i < Integer.parseInt(args[3]); i++) {
			threads.add(new Thread(() -> {
				Router router = new Router(factory, res, scenario);

				while (true) {
					String s2 = null;

					synchronized (readLink) {
						try {
							s2 = readLink.readLine();
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}

					if (s2 != null) {
						router.setString(s2);
						router.run();
					} else {
						return;
					}
				}
			}));
		}

		threads.forEach(Thread::start);

		for (Thread thread : threads) {
			thread.join();
		}

		BufferedWriter writer = IOUtils.getBufferedWriter(args[4]);
		writer.write(
				"id,travelTime,accessTime,egressTime,inVehicleTime,numTransfers,transferTime,distance[feet],transit");
		writer.newLine();
		for (Data d : res) {
			writer.write(d.id + "," + d.travelTime + "," + d.accessTime + "," + d.egressTime + "," + d.inVehicleTime
					+ "," + (d.transfers - 1) + "," + d.transferTime + "," + d.distance + "," + d.transit + "\n");
		}

		writer.flush();
		writer.close();

	}

	private void setString(String s) {
		this.s = s;
	}

	class Data {
		String id;
		double travelTime;
		double accessTime;
		double egressTime;
		double transferTime;
		double inVehicleTime;
		int transfers = 0;
		double distance;
		boolean transit = false;
	}
}
