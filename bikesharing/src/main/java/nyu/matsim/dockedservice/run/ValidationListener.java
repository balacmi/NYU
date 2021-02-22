package nyu.matsim.dockedservice.run;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.core.controler.events.StartupEvent;
import org.matsim.core.controler.listener.StartupListener;

import nyu.matsim.dockedservice.io.SharingServiceSpecification;
import nyu.matsim.dockedservice.io.validation.SharingServiceValidator;
import nyu.matsim.dockedservice.service.SharingService;

class ValidationListener implements StartupListener {
	private final Logger logger = Logger.getLogger(ValidationListener.class);

	private final Id<SharingService> serviceId;
	private final SharingServiceValidator validator;
	private final SharingServiceSpecification specification;

	ValidationListener(Id<SharingService> serviceId, SharingServiceValidator validator,
			SharingServiceSpecification specification) {
		this.serviceId = serviceId;
		this.validator = validator;
		this.specification = specification;
	}

	@Override
	public void notifyStartup(StartupEvent event) {
		logger.info("Validating sharing service " + serviceId.toString() + "...");
		validator.validate(specification);
	}
}
