package nyu.matsim.dockedservice.service;

import java.util.Optional;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.IdMap;
import org.matsim.core.mobsim.framework.MobsimAgent;

import nyu.matsim.dockedservice.routing.InteractionPoint;

public interface SharingService {
	public Id<SharingService> getId();

	public IdMap<SharingVehicle, SharingVehicle> getVehicles();

	public IdMap<SharingStation, SharingStation> getStations();

	public void pickupVehicle(SharingVehicle vehicle, MobsimAgent agent);

	public void dropoffVehicle(SharingVehicle vehicle, MobsimAgent agent);

	public Optional<VehicleInteractionPoint> findClosestVehicle(MobsimAgent agent);

	public InteractionPoint findClosestDropoffLocation(SharingVehicle vehicle, MobsimAgent agent);
}
