package nyu.matsim.bikesharing.infrastructure;

import java.util.List;
import java.util.Map;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Person;

public interface BikeshareServiceInterface {

	Id<BikesharingVehicle> getAndRemoveClosestBike(Coord coord, Id<Person> id);

	Map<Id<BikesharingVehicle>, Coord> getBikeCoordMap();

	Map<Id<BikesharingVehicle>, BikesharingVehicle> getMapBikesharingVehicles();

	void addBikeToStation(Id<BikesharingStation> stationId, BikesharingVehicle bike);

	Coord reserveClosestParkingSpot(Coord coord, Id<Person> personId);

	Id<BikesharingVehicle> getRentedBike(Id<Person> personId);

	Id<BikesharingStation> getParkingStation(Id<Person> personId);

	void setUp(List<BikesharingStation> stations);

}