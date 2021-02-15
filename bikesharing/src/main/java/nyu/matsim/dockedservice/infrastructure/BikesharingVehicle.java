package nyu.matsim.dockedservice.infrastructure;

import org.matsim.api.core.v01.Id;

public class BikesharingVehicle {
	
	private Id<BikesharingVehicle> bikeId;
	
	public BikesharingVehicle(Id<BikesharingVehicle> bikeId) {
		this.bikeId = bikeId;
	}

	public Id<BikesharingVehicle> getBikeId() {
		return bikeId;
	}

}
