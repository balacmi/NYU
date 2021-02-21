package nyu.matsim.dockedservice.service;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;

import com.google.common.base.Verify;
import com.google.common.collect.ImmutableList;

import nyu.matsim.dockedservice.io.SharingStationSpecification;

public class SharingStation {
	private final SharingStationSpecification specification;
	private final Link link;

	private List<SharingVehicle> vehicles = new LinkedList<>();

	public SharingStation(SharingStationSpecification specification, Link link) {
		this.specification = specification;
		this.link = link;
	}

	public Id<SharingStation> getId() {
		return specification.getId();
	}

	public void addVehicle(SharingVehicle vehicle) {
		Verify.verify(vehicles.size() + 1 <= specification.getCapacity());
		Verify.verify(!vehicles.contains(vehicle));
		vehicles.add(vehicle);
	}

	public void removeVehicle(SharingVehicle vehicle) {
		Verify.verify(vehicles.contains(vehicle));
		vehicles.remove(vehicle);
	}

	public ImmutableList<SharingVehicle> getVehicles() {
		return ImmutableList.copyOf(vehicles);
	}

	public int getFreeCapacity() {
		return specification.getCapacity() - vehicles.size();
	}

	public Id<Link> getLinkId() {
		return link.getId();
	}

	public Coord getCoord() {
		return link.getCoord();
	}

	public Map<String, Object> getCustomAttributes() {
		return Collections.emptyMap();
	}
}
