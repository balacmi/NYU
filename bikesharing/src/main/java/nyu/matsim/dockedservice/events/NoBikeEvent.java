package nyu.matsim.dockedservice.events;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.population.Person;

public class NoBikeEvent extends Event {
	public static String REJECTION_TYPE = "nobike";
	private Coord coord;
	private Id<Person> personId;

	public NoBikeEvent(double time, Coord coord, Id<Person> personId) {
		super(time);
		this.coord = coord;
		this.personId = personId;
	}

	@Override
	public String getEventType() {
		return NoBikeEvent.REJECTION_TYPE;
	}

	public Coord getCoord() {
		return coord;
	}

	public Id<Person> getPersonId() {
		return personId;
	}

}
