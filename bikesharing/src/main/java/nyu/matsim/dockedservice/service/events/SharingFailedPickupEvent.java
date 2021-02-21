package nyu.matsim.dockedservice.service.events;

import java.util.Optional;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;

import nyu.matsim.dockedservice.service.SharingService;
import nyu.matsim.dockedservice.service.SharingStation;

public class SharingFailedPickupEvent extends AbstractSharingEvent {
	static public final String TYPE = "sharing vehicle pickup failed";

	public SharingFailedPickupEvent(double time, Id<SharingService> serviceId, Id<Person> personId, Id<Link> linkId,
			Optional<Id<SharingStation>> stationId) {
		super(time, serviceId, personId, linkId, Optional.empty(), stationId);
	}

	@Override
	public String getEventType() {
		return TYPE;
	}
}
