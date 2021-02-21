package nyu.matsim.dockedservice.io;

import java.util.Optional;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;

import nyu.matsim.dockedservice.service.SharingStation;
import nyu.matsim.dockedservice.service.SharingVehicle;

public interface SharingVehicleSpecification {
	Id<SharingVehicle> getId();

	Optional<Id<Link>> getStartLinkId();

	Optional<Id<SharingStation>> getStartStationId();
}
