package nyu.matsim.dockedservice.routing;

import java.util.Optional;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.utils.collections.QuadTree;
import org.matsim.core.utils.collections.QuadTrees;
import org.matsim.core.utils.geometry.CoordUtils;
import org.matsim.facilities.FacilitiesUtils;
import org.matsim.facilities.Facility;

import nyu.matsim.dockedservice.io.SharingServiceSpecification;
import nyu.matsim.dockedservice.io.SharingStationSpecification;

public class StationBasedInteractionFinder implements InteractionFinder {
	private final Network network;
	private final QuadTree<Link> stations;
	private final double maxmimumDistance;

	public StationBasedInteractionFinder(Network network, SharingServiceSpecification specification,
			double maximumDistance) {
		this.network = network;
		this.maxmimumDistance = maximumDistance;

		this.stations = QuadTrees.createQuadTree(specification.getStations().stream()
				.map(SharingStationSpecification::getLinkId).map(network.getLinks()::get).collect(Collectors.toSet()));
	}

	@Override
	public Optional<Id<Link>> findPickup(Facility originFacility) {
		return findStation(originFacility);
	}

	@Override
	public Optional<Id<Link>> findDropoff(Facility destinationFacility) {
		return findStation(destinationFacility);
	}

	private Optional<Id<Link>> findStation(Facility nearbyFacility) {
		Link nearbyLink = FacilitiesUtils.decideOnLink(nearbyFacility, network);

		Link link = stations.getClosest(nearbyLink.getCoord().getX(), nearbyLink.getCoord().getY());

		if (link != null) {
			if (CoordUtils.calcEuclideanDistance(link.getCoord(), nearbyLink.getCoord()) <= maxmimumDistance) {
				return Optional.of(link.getId());
			}
		}

		return Optional.empty();
	}
}
