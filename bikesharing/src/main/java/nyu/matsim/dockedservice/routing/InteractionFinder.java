package nyu.matsim.dockedservice.routing;

import java.util.Optional;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.facilities.Facility;

public interface InteractionFinder {
	Optional<Id<Link>> findPickup(Facility originFacility);

	Optional<Id<Link>> findDropoff(Facility destinationFacility);
}
