package nyu.matsim.dockedservice.io;

import java.util.Stack;

import org.matsim.api.core.v01.Id;
import org.matsim.core.utils.io.MatsimXmlParser;
import org.xml.sax.Attributes;

import nyu.matsim.dockedservice.service.SharingStation;
import nyu.matsim.dockedservice.service.SharingVehicle;

public class SharingServiceReader extends MatsimXmlParser {
	private final SharingServiceSpecification service;

	public SharingServiceReader(SharingServiceSpecification service) {
		this.service = service;
	}

	@Override
	public void startTag(String name, Attributes attributes, Stack<String> context) {
		if (name.equals("vehicle")) {
			service.addVehicle(ImmutableSharingVehicleSpecification.newBuilder() //
					.id(Id.create(attributes.getValue("id"), SharingVehicle.class)) //
					.startLinkId(Id.createLinkId(attributes.getValue("link"))) //
					.startStationId(Id.create(attributes.getValue("station"), SharingStation.class)) //
					.build());
		} else if (name.equals("station")) {
			service.addStation(ImmutableSharingStationSpecification.newBuilder() //
					.id(Id.create(attributes.getValue("id"), SharingStation.class)) //
					.linkId(Id.createLinkId(attributes.getValue("link"))) //
					.capacity(Integer.parseInt(attributes.getValue("capacity"))) //
					.build());
		}
	}

	@Override
	public void endTag(String name, String content, Stack<String> context) {
	}
}
