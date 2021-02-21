package nyu.matsim.dockedservice.io;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;

import nyu.matsim.dockedservice.service.SharingStation;

public interface SharingStationSpecification {
	Id<SharingStation> getId();

	Id<Link> getLinkId();

	int getCapacity();
}
