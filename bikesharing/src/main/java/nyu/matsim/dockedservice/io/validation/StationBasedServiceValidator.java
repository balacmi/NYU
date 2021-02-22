package nyu.matsim.dockedservice.io.validation;

import java.util.HashSet;
import java.util.Set;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;

import com.google.common.base.Verify;

import nyu.matsim.dockedservice.io.SharingServiceSpecification;
import nyu.matsim.dockedservice.io.SharingStationSpecification;
import nyu.matsim.dockedservice.io.SharingVehicleSpecification;
import nyu.matsim.dockedservice.service.SharingService;
import nyu.matsim.dockedservice.service.SharingStation;
import nyu.matsim.dockedservice.service.SharingVehicle;

public class StationBasedServiceValidator implements SharingServiceValidator {
	private final Id<SharingService> serviceId;

	public StationBasedServiceValidator(Id<SharingService> serviceId) {
		this.serviceId = serviceId;
	}

	@Override
	public void validate(SharingServiceSpecification specification) {
		Set<Id<Link>> stationLinkIds = new HashSet<>();
		Set<Id<SharingStation>> stationIds = new HashSet<>();

		for (SharingStationSpecification station : specification.getStations()) {
			Verify.verify(!stationIds.contains(station.getId()), "Service %s has duplicate station %s",
					serviceId.toString(), station.getId().toString());

			Verify.verify(!stationLinkIds.contains(station.getLinkId()), "Service %s has multiple stations on link %s",
					serviceId.toString(), station.getLinkId().toString());

			stationLinkIds.add(station.getLinkId());
			stationIds.add(station.getId());
		}

		Set<Id<SharingVehicle>> vehicleIds = new HashSet<>();

		for (SharingVehicleSpecification vehicle : specification.getVehicles()) {
			Verify.verify(!vehicleIds.contains(vehicle.getId()), "Service %s has duplicate vehicle %s",
					serviceId.toString(), vehicle.getId().toString());

			Verify.verify(vehicle.getStartStationId().isPresent(), "Vehicle %s of service %s needs start link",
					vehicle.getId().toString(), serviceId.toString());

			Verify.verify(stationIds.contains(vehicle.getStartStationId().get()),
					"Station %s for vehicle %s does not exist in service %s",
					vehicle.getStartStationId().get().toString(), vehicle.getId().toString(), serviceId.toString());

			vehicleIds.add(vehicle.getId());
		}
	}
}
