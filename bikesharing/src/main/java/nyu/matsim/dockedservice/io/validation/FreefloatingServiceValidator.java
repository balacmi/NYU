package nyu.matsim.dockedservice.io.validation;

import java.util.HashSet;
import java.util.Set;

import org.matsim.api.core.v01.Id;

import com.google.common.base.Verify;

import nyu.matsim.dockedservice.io.SharingServiceSpecification;
import nyu.matsim.dockedservice.io.SharingVehicleSpecification;
import nyu.matsim.dockedservice.service.SharingService;
import nyu.matsim.dockedservice.service.SharingVehicle;

public class FreefloatingServiceValidator implements SharingServiceValidator {
	private final Id<SharingService> serviceId;

	public FreefloatingServiceValidator(Id<SharingService> serviceId) {
		this.serviceId = serviceId;
	}

	@Override
	public void validate(SharingServiceSpecification specification) {
		Set<Id<SharingVehicle>> vehicleIds = new HashSet<>();

		for (SharingVehicleSpecification vehicle : specification.getVehicles()) {
			Verify.verify(!vehicleIds.contains(vehicle.getId()), "Service %s has duplicate vehicle %s",
					serviceId.toString(), vehicle.getId().toString());

			Verify.verify(vehicle.getStartLinkId().isPresent(), "Vehicle %s of service %s needs start link",
					vehicle.getId().toString(), serviceId.toString());
		}
	}
}
