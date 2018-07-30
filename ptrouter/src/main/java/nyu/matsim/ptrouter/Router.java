package nyu.matsim.ptrouter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.config.groups.PlansCalcRouteConfigGroup;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.pt.router.FakeFacility;
import org.matsim.pt.router.TransitRouter;

import ch.sbb.matsim.routing.pt.raptor.SwissRailRaptorFactory;

/**
 * Hello world!
 *
 */
public class Router extends Thread {
	private SwissRailRaptorFactory factory;
	private String s;
	private Set<Data> result;
	public Router(SwissRailRaptorFactory factory, String s, Set<Data> r) {
		this.factory = factory;
		this.s = s;
		this.result = r;
	}
	
	private synchronized void addData(Data d) {
		this.result.add(d);
	}
	
	public Set<Data> getData() {
		return this.result;
	}

	public void run() {

		TransitRouter router = factory.get();
		
		String[] arr = s.split(",");
		Coord coordStart = new Coord(Double.parseDouble(arr[6]), Double.parseDouble(arr[7]));

		Coord coordEnd = new Coord(Double.parseDouble(arr[10]), Double.parseDouble(arr[11]));

		FakeFacility facStart = new FakeFacility(coordStart);
		FakeFacility facEnd = new FakeFacility(coordEnd);

		List<Leg> legs = router.calcRoute(facStart, facEnd, Double.parseDouble(arr[12]), null);

		Data d = new Data();
		d.id = arr[1];
		d.travelTime = legs.get(legs.size() - 1).getDepartureTime() + legs.get(legs.size() - 1).getTravelTime() - Double.parseDouble(arr[12]);
		for (Leg l : legs) {
			d.distance += l.getRoute().getDistance();
		}
		if (legs.size() > 1)
			d.transit = true;
		this.addData(d);
	}

	public static void main(String[] args) throws IOException {
		BufferedReader readLink = IOUtils.getBufferedReader(args[0]);
		readLink.readLine();
		String s = readLink.readLine();
		Config config = ConfigUtils.createConfig();

		final double travelingWalk = -15.0D;
		((PlanCalcScoreConfigGroup)config.getModule("planCalcScore")).
		getModes().get(TransportMode.walk).setMarginalUtilityOfTraveling(travelingWalk);	    
	    PlansCalcRouteConfigGroup routeConfigGroup = config.plansCalcRoute();
	    routeConfigGroup.getModeRoutingParams().get("walk").setBeelineDistanceFactor(1.25);
	    routeConfigGroup.getModeRoutingParams().get("walk").setTeleportedModeSpeed(1.11 / 0.3048);
	    ((PlanCalcScoreConfigGroup)config.getModule("planCalcScore")).setUtilityOfLineSwitch(-0.0D);
		
		config.transit().setTransitScheduleFile(args[1]);
		//config.transit().setVehiclesFile("C:\\Users\\balacm\\Downloads\\vehicle.xml");
		//config.transit().setInputScheduleCRS("W");
		config.transit().setUseTransit(true);
		config.transitRouter().setSearchRadius(4000.0);
		config.transitRouter().setMaxBeelineWalkConnectionDistance(300.0);

		//config.transitRouter().setAdditionalTransferTime(60.0);
		final Scenario scenario = ScenarioUtils.loadScenario(config);
		SwissRailRaptorFactory factory = new SwissRailRaptorFactory(scenario.getTransitSchedule(), config);
		
		ExecutorService executor = Executors.newFixedThreadPool(Integer.parseInt(args[2]));
		Set<Data> res = new HashSet<Data>();
		while (s != null) {

			Router worker = new Router(factory, s, res);
			executor.execute(worker);
			s = readLink.readLine();
		}

		executor.shutdown();
		while (!executor.isTerminated()) {

		}
		
		BufferedWriter writer = IOUtils.getBufferedWriter(args[3]);
		for (Data d : res) {
			writer.write(d.id + "," + d.travelTime + "," + d.transit +"\n");
		}
		
		writer.flush();
		writer.close();
		System.out.println("bla");

	}
	
	class Data {
		String id;
		double travelTime;
		double distance;
		boolean transit = false;
	}
}
