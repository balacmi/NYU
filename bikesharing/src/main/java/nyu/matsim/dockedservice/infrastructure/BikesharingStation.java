package nyu.matsim.dockedservice.infrastructure;

import java.util.List;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;

public class BikesharingStation {

	private Id<BikesharingStation> stationId;

	private int parkingSlots;

	private List<BikesharingVehicle> availableBikes;

	private Coord coord;

	public Coord getCoord() {
		return coord;
	}

	public BikesharingStation(Id<BikesharingStation> id, int parkingSlots, List<BikesharingVehicle> availableBikes,
			Coord coord) {
		this.stationId = id;
		this.parkingSlots = parkingSlots;
		this.availableBikes = availableBikes;
		this.coord = coord;
	}

	public Id<BikesharingStation> getStationId() {
		return stationId;
	}

	public int getParkingSlots() {
		return parkingSlots;
	}

	public List<BikesharingVehicle> getAvailableBikes() {
		return availableBikes;
	}

	public void addBike(BikesharingVehicle bike) {

		this.availableBikes.add(bike);
		
	}

}
