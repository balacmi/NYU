package nyu.matsim.dockedservice.service;

import java.util.Collection;
import java.util.Optional;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.mobsim.framework.MobsimAgent;

public interface SharingService {
	public Id<SharingService> getId();

	public Collection<SharingVehicle> getVehicles();

	public void pickupVehicle(SharingVehicle vehicle, MobsimAgent agent);

	public void dropoffVehicle(SharingVehicle vehicle, MobsimAgent agent);

	public Optional<SharingVehicle> findClosestVehicle(MobsimAgent agent);

	public Id<Link> findClosestDropoffLocation(SharingVehicle vehicle, MobsimAgent agent);

	public Optional<Id<SharingStation>> getStationId(Id<Link> linkId);
}
