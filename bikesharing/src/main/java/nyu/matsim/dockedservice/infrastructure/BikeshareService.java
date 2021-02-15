package nyu.matsim.dockedservice.infrastructure;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.utils.collections.QuadTree;

import com.google.inject.Inject;

public class BikeshareService implements BikeshareServiceInterface {

	@Inject
	private Network network;

	private QuadTree<BikesharingStation> quadTreeAvailBikesStations;
	private QuadTree<BikesharingStation> quadTreeAvailParkStations;
	private Map<Id<BikesharingVehicle>, Coord> mapAvailableBikes;
	private Map<Id<BikesharingStation>, BikesharingStation> mapBikesharingStations;
	private Map<Id<BikesharingVehicle>, BikesharingVehicle> mapBikesharingVehicles;
	private Map<Id<Person>, Id<BikesharingVehicle>> mapPersonBike;
	private Map<Id<Person>, Id<BikesharingStation>> mapPersonParkingStation;

	@Override
	public Id<BikesharingVehicle> getAndRemoveClosestBike(Coord coord, Id<Person> id) {

		synchronized (this.quadTreeAvailBikesStations) {
			BikesharingStation station = this.quadTreeAvailBikesStations.getClosest(coord.getX(), coord.getY());
			if (station == null)
				return null;
			else {
				BikesharingVehicle vehicle = station.getAvailableBikes().remove(0);
				if (station.getAvailableBikes().size() == 0)
					this.quadTreeAvailBikesStations.remove(station.getCoord().getX(), station.getCoord().getY(),
							station);
				this.mapPersonBike.put(id, vehicle.getBikeId());
				return vehicle.getBikeId();
			}
		}

	}

	@Override
	public Map<Id<BikesharingVehicle>, Coord> getBikeCoordMap() {
		return this.mapAvailableBikes;
	}

	@Override
	public Map<Id<BikesharingVehicle>, BikesharingVehicle> getMapBikesharingVehicles() {
		return mapBikesharingVehicles;
	}

	@Override
	public void addBikeToStation(Id<BikesharingStation> stationId, BikesharingVehicle bike) {
		synchronized (this.quadTreeAvailBikesStations) {

			BikesharingStation station = this.mapBikesharingStations.get(stationId);
			station.addBike(bike);
			if (station.getAvailableBikes().size() == 1) {
				this.quadTreeAvailBikesStations.put(station.getCoord().getX(), station.getCoord().getY(), station);
			}
		}
	}

	@Override
	public Coord reserveClosestParkingSpot(Coord coord, Id<Person> personId) {

		synchronized (this.quadTreeAvailParkStations) {
			BikesharingStation station = this.quadTreeAvailParkStations.getClosest(coord.getX(), coord.getY());

			if (station == null)
				return null;

			if (station.getAvailableBikes().size() == station.getParkingSlots() + 1)
				this.quadTreeAvailParkStations.remove(station.getCoord().getX(), station.getCoord().getY(), station);
			this.mapPersonParkingStation.put(personId, station.getStationId());
			return station.getCoord();
		}
	}

	@Override
	public Id<BikesharingVehicle> getRentedBike(Id<Person> personId) {
		return this.mapPersonBike.get(personId);
	}

	@Override
	public Id<BikesharingStation> getParkingStation(Id<Person> personId) {
		return this.mapPersonParkingStation.get(personId);
	}

	@Override
	public void setUp(List<BikesharingStation> stations) {
		double minx = (1.0D / 0.0D);
		double miny = (1.0D / 0.0D);
		double maxx = (-1.0D / 0.0D);
		double maxy = (-1.0D / 0.0D);
		for (Link l : network.getLinks().values()) {
			if (l.getCoord().getX() < minx)
				minx = l.getCoord().getX();
			if (l.getCoord().getY() < miny)
				miny = l.getCoord().getY();
			if (l.getCoord().getX() > maxx)
				maxx = l.getCoord().getX();
			if (l.getCoord().getY() <= maxy)
				continue;
			maxy = l.getCoord().getY();
		}
		minx -= 1.0D;
		miny -= 1.0D;
		maxx += 1.0D;
		maxy += 1.0D;
		this.quadTreeAvailBikesStations = new QuadTree<>(minx, miny, maxx, maxy);
		this.quadTreeAvailParkStations = new QuadTree<>(minx, miny, maxx, maxy);
		this.mapAvailableBikes = new ConcurrentHashMap<>();
		this.mapBikesharingStations = new ConcurrentHashMap<>();
		this.mapBikesharingVehicles = new ConcurrentHashMap<>();
		this.mapPersonBike = new ConcurrentHashMap<>();
		this.mapPersonParkingStation = new ConcurrentHashMap<>();
		for (BikesharingStation station : stations) {
			List<BikesharingVehicle> bikes = new ArrayList<>(station.getAvailableBikes());
			BikesharingStation newStation = new BikesharingStation(station.getStationId(), 
					station.getParkingSlots(), bikes, station.getCoord());
			if (station.getAvailableBikes().size() > 0)
				this.quadTreeAvailBikesStations.put(newStation.getCoord().getX(), newStation.getCoord().getY(), newStation);
			
			if (station.getAvailableBikes().size() < station.getParkingSlots())
				this.quadTreeAvailParkStations.put(newStation.getCoord().getX(), newStation.getCoord().getY(), newStation);
			
			this.mapBikesharingStations.put(newStation.getStationId(), newStation);
			
			for (BikesharingVehicle bike : newStation.getAvailableBikes()) {
				this.mapAvailableBikes.put(bike.getBikeId(), newStation.getCoord());
				this.mapBikesharingVehicles.put(bike.getBikeId(), bike);	
			}
		}
	}

	
}
