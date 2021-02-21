package nyu.matsim.dockedservice.routing;

import java.util.Optional;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.facilities.FacilitiesUtils;
import org.matsim.facilities.Facility;

public class FreefloatingInteractionFinder implements InteractionFinder {
	private final Network network;

	public FreefloatingInteractionFinder(Network network) {
		this.network = network;
	}

	@Override
	public Optional<Id<Link>> findPickup(Facility originFacility) {
		return Optional.ofNullable(FacilitiesUtils.decideOnLink(originFacility, network)).map(Link::getId);
	}

	@Override
	public Optional<Id<Link>> findDropoff(Facility destinationFacility) {
		return Optional.ofNullable(FacilitiesUtils.decideOnLink(destinationFacility, network)).map(Link::getId);
	}
}
