package nyu.matsim.bikesharing.events;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.PersonArrivalEvent;
import org.matsim.api.core.v01.events.PersonDepartureEvent;
import org.matsim.api.core.v01.events.handler.PersonArrivalEventHandler;
import org.matsim.api.core.v01.events.handler.PersonDepartureEventHandler;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Person;

import com.google.inject.Inject;

import nyu.matsim.bikesharing.infrastructure.BikeshareService;
import nyu.matsim.bikesharing.utils.RentalInfo;

public class BikeshareDemand implements PersonDepartureEventHandler, PersonArrivalEventHandler {

	@Inject
	private Network network;

	@Inject
	private BikeshareService bikeshareFleet;
	Map<Id<Person>, ArrayList<RentalInfo>> bikeRentals = new ConcurrentHashMap<>();
	Map<Id<Person>, RentalInfo> currentRental = new ConcurrentHashMap<>();

	@Override
	public void reset(int iteration) {

		bikeRentals.clear();
		currentRental.clear();
	}

	@Override
	public void handleEvent(PersonArrivalEvent event) {

		if (event.getLegMode().equals("egress_walk_bike")) {

			RentalInfo rentalInfo = this.currentRental.get(event.getPersonId());
			rentalInfo.setEgressEndTime(event.getTime());
			if (this.bikeRentals.containsKey(event.getPersonId())) {
				this.bikeRentals.get(event.getPersonId()).add(rentalInfo);
			} else {
				ArrayList<RentalInfo> newArrayList = new ArrayList<>();
				newArrayList.add(rentalInfo);
				this.bikeRentals.put(event.getPersonId(), newArrayList);
			}

		}
	}

	@Override
	public void handleEvent(PersonDepartureEvent event) {

		if (event.getLegMode().equals("access_walk_bike")) {
			Coord coord = this.network.getLinks().get(event.getLinkId()).getCoord();

			RentalInfo newRental = new RentalInfo();
			newRental.setAccessStartTime(event.getTime());

			newRental.setOriginCoord(coord);
			newRental.setOriginLinkId(event.getLinkId());

			this.currentRental.put(event.getPersonId(), newRental);

		} else if (event.getLegMode().equals("bikeshare")) {
			Coord coord = this.network.getLinks().get(event.getLinkId()).getCoord();

			RentalInfo info = this.currentRental.get(event.getPersonId());
			info.setAccessEndTime(event.getTime());
			info.setStartTime(event.getTime());
			info.setPickupCoord(coord);
			info.setPickupLinkId(event.getLinkId());
			info.setVehId(this.bikeshareFleet.getRentedBike(event.getPersonId()));
		} else if (event.getLegMode().equals("egress_walk_bike")) {
			Coord coord = this.network.getLinks().get(event.getLinkId()).getCoord();

			RentalInfo info = this.currentRental.get(event.getPersonId());
			info.setEgressStartTime(event.getTime());
			info.setEndTime(event.getTime());
			info.setInVehicleTime(event.getTime() - info.getAccessEndTime());
			info.setDropoffCoord(coord);
			info.setDropoffLinkId(event.getLinkId());
			info.setEndCoord(coord);
			info.setEndLinkId(event.getLinkId());

		}
	}

	public Map<Id<Person>, ArrayList<RentalInfo>> getAgentRentalsMap() {
		return this.bikeRentals;
	}

}
