package nyu.matsim.dockedservice.events;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.PersonArrivalEvent;
import org.matsim.api.core.v01.events.handler.PersonArrivalEventHandler;
import org.matsim.api.core.v01.population.Person;

import com.google.inject.Inject;

import nyu.matsim.dockedservice.infrastructure.BikeshareServiceInterface;
import nyu.matsim.dockedservice.infrastructure.BikesharingStation;
import nyu.matsim.dockedservice.infrastructure.BikesharingVehicle;

public class BikeEventsHandler implements PersonArrivalEventHandler {

	@Inject
	private BikeshareServiceInterface bikeService;

	@Override
	public void handleEvent(PersonArrivalEvent event) {

		if (event.getLegMode().equals("bikeshare")) {
			Id<Person> personId = event.getPersonId();
			Id<BikesharingVehicle> bikeId = this.bikeService.getRentedBike(personId);
			BikesharingVehicle bike = this.bikeService.getMapBikesharingVehicles().get(bikeId);
			Id<BikesharingStation> stationId = this.bikeService.getParkingStation(personId);
			this.bikeService.addBikeToStation(stationId, bike);
		}

	}

}
