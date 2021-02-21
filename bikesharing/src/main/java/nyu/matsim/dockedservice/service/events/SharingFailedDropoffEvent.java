package nyu.matsim.dockedservice.service.events;

import java.util.Optional;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;

import nyu.matsim.dockedservice.service.SharingService;
import nyu.matsim.dockedservice.service.SharingStation;
import nyu.matsim.dockedservice.service.SharingVehicle;

public class SharingFailedDropoffEvent extends AbstractSharingEvent {
	static public final String TYPE = "sharing vehicle dropoff failed";

	public SharingFailedDropoffEvent(double time, Id<SharingService> serviceId, Id<Person> personId, Id<Link> linkId,
			Id<SharingVehicle> vehicleId, Optional<Id<SharingStation>> stationId) {
		super(time, serviceId, personId, linkId, Optional.of(vehicleId), stationId);
	}

	@Override
	public String getEventType() {
		return TYPE;
	}
}