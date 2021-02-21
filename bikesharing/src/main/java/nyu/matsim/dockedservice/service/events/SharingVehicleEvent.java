package nyu.matsim.dockedservice.service.events;

import java.util.Map;
import java.util.Optional;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.events.HasLinkId;
import org.matsim.api.core.v01.network.Link;

import nyu.matsim.dockedservice.service.SharingService;
import nyu.matsim.dockedservice.service.SharingStation;
import nyu.matsim.dockedservice.service.SharingVehicle;

public class SharingVehicleEvent extends Event implements HasLinkId {
	static public final String TYPE = "sharing vehicle placed";

	private final Id<SharingService> serviceId;
	private final Id<Link> linkId;
	private final Id<SharingVehicle> vehicleId;
	private final Optional<Id<SharingStation>> stationId;

	public SharingVehicleEvent(double time, Id<SharingService> serviceId, Id<Link> linkId, Id<SharingVehicle> vehicleId,
			Optional<Id<SharingStation>> stationId) {
		super(time);

		this.serviceId = serviceId;
		this.linkId = linkId;
		this.vehicleId = vehicleId;
		this.stationId = stationId;
	}

	public Map<String, String> getAttributes() {
		Map<String, String> attributes = super.getAttributes();
		attributes.put("service", serviceId.toString());
		attributes.put("link", linkId.toString());
		attributes.put("vehicle", vehicleId.toString());

		if (stationId.isPresent()) {
			attributes.put("station", stationId.get().toString());
		}

		return attributes;
	}

	@Override
	public Id<Link> getLinkId() {
		return linkId;
	}

	@Override
	public String getEventType() {
		return TYPE;
	}
}
