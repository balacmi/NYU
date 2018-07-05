package nyu.matsim.bikesharing;

import java.util.ArrayList;
import java.util.List;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.gbl.MatsimRandom;

import com.google.inject.Provides;

import nyu.matsim.bikesharing.events.BikeshareDemand;
import nyu.matsim.bikesharing.infrastructure.BikeshareService;
import nyu.matsim.bikesharing.infrastructure.BikesharingStation;
import nyu.matsim.bikesharing.infrastructure.BikesharingVehicle;
import nyu.matsim.bikesharing.listeners.BikeshareDemandWriter;
import nyu.matsim.bikesharing.qsim.BikeshareDepartureHandler;

public class BikeshareModule extends AbstractModule {

	@Override
	public void install() {
		addControlerListenerBinding().to(BikeshareLoader.class);
		addControlerListenerBinding().to(BikeshareDemandWriter.class);
		addEventHandlerBinding().to(BikeshareDemand.class);
		bind(BikeshareService.class).asEagerSingleton();
		bind(BikeshareDepartureHandler.class).asEagerSingleton();
		bind(BikeshareDemand.class).asEagerSingleton();
	}

	@Provides
	public List<BikesharingStation> provideBikehsareStations(Network network) {

		Link[] links = network.getLinks().values().toArray(new Link[network.getLinks().values().size()]);
		List<BikesharingStation> stations = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			Coord coord = links[MatsimRandom.getRandom().nextInt(links.length)].getCoord();

			List<BikesharingVehicle> bikes = new ArrayList<>();
			for (int j = 0; j < 5; j++) {
				BikesharingVehicle vehicle = new BikesharingVehicle(
						Id.create(Integer.toString(i) + Integer.toString(j), BikesharingVehicle.class));
				bikes.add(vehicle);
			}

			BikesharingStation station = new BikesharingStation(
					Id.create(Integer.toString(i), BikesharingStation.class), 10, bikes, coord);
			stations.add(station);

		}
		return stations;

	}

}
