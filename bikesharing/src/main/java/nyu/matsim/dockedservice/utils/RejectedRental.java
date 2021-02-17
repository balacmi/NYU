package nyu.matsim.dockedservice.utils;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Person;

public class RejectedRental {
	
	private Id<Person> personId;
	private String rejectionType; //can be either nobike or noparking
	private Coord coord;
	private double timeStamp;
	
	public RejectedRental(Id<Person> personId, String rejectionType, Coord coord, double timeStamp) {
		this.personId = personId;
		this.rejectionType = rejectionType;
		this.coord = coord;
		this.timeStamp = timeStamp;
	}

	public Id<Person> getPersonId() {
		return personId;
	}

	public String getRejectionType() {
		return rejectionType;
	}

	public Coord getCoord() {
		return coord;
	}

	public double getTimeStamp() {
		return timeStamp;
	}

}
