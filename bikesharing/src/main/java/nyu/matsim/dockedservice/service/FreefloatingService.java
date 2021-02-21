package nyu.matsim.dockedservice.service;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.IdSet;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.mobsim.framework.MobsimAgent;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.utils.collections.QuadTree;
import org.matsim.core.utils.geometry.CoordUtils;

import com.google.common.base.Verify;

import nyu.matsim.dockedservice.io.SharingVehicleSpecification;

public class FreefloatingService implements SharingService {
	private final Id<SharingService> serviceId;
	private final double maximumDistance;

	private final Network network;

	private final IdSet<SharingVehicle> lockedIds = new IdSet<>(SharingVehicle.class);
	private final QuadTree<SharingVehicle> spatialIndex;
	private final Collection<SharingVehicle> vehicles = new LinkedList<>();

	public FreefloatingService(Id<SharingService> serviceId, Collection<SharingVehicleSpecification> fleet,
			Network network, double maximumDistance) {
		this.network = network;

		this.maximumDistance = maximumDistance;
		this.serviceId = serviceId;

		double[] bounds = NetworkUtils.getBoundingBox(network.getNodes().values());
		this.spatialIndex = new QuadTree<>(bounds[0], bounds[1], bounds[2], bounds[3]);

		for (SharingVehicleSpecification specfication : fleet) {
			if (specfication.getStartLinkId().isEmpty()) {
				throw new IllegalStateException("Vehicle needs start link: " + specfication.getId().toString());
			}

			Link link = network.getLinks().get(specfication.getStartLinkId().get());
			SharingVehicle vehicle = new SharingVehicle(specfication, link);
			vehicles.add(vehicle);

			Coord coord = link.getCoord();
			spatialIndex.put(coord.getX(), coord.getY(), vehicle);
		}
	}

	@Override
	public void pickupVehicle(SharingVehicle vehicle, MobsimAgent agent) {
		Verify.verify(!lockedIds.contains(vehicle.getId()));

		Coord coord = vehicle.getLink().getCoord();
		spatialIndex.remove(coord.getX(), coord.getY(), vehicle);

		lockedIds.add(vehicle.getId());
	}

	@Override
	public void dropoffVehicle(SharingVehicle vehicle, MobsimAgent agent) {
		Verify.verify(lockedIds.contains(vehicle.getId()));

		Link link = network.getLinks().get(agent.getCurrentLinkId());
		vehicle.setLink(link);

		Coord coord = vehicle.getLink().getCoord();
		spatialIndex.put(coord.getX(), coord.getY(), vehicle);

		lockedIds.remove(vehicle.getId());
	}

	@Override
	public Optional<SharingVehicle> findClosestVehicle(MobsimAgent agent) {
		Link currentLink = network.getLinks().get(agent.getCurrentLinkId());

		if (spatialIndex.size() == 0) {
			return Optional.empty();
		}

		SharingVehicle vehicle = spatialIndex.getClosest(currentLink.getCoord().getX(), currentLink.getCoord().getY());

		if (CoordUtils.calcEuclideanDistance(vehicle.getLink().getCoord(), currentLink.getCoord()) > maximumDistance) {
			return Optional.empty();
		}

		return Optional.of(vehicle);
	}

	@Override
	public Id<Link> findClosestDropoffLocation(SharingVehicle vehicle, MobsimAgent agent) {
		return agent.getCurrentLinkId();
	}

	@Override
	public Optional<Id<SharingStation>> getStationId(Id<Link> linkId) {
		return Optional.empty();
	}

	@Override
	public Id<SharingService> getId() {
		return serviceId;
	}

	@Override
	public Collection<SharingVehicle> getVehicles() {
		return vehicles;
	}
}
