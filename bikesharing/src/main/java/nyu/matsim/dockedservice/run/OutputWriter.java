package nyu.matsim.dockedservice.run;

import org.matsim.api.core.v01.Id;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.controler.events.ShutdownEvent;
import org.matsim.core.controler.listener.ShutdownListener;

import nyu.matsim.dockedservice.io.SharingServiceSpecification;
import nyu.matsim.dockedservice.io.SharingServiceWriter;
import nyu.matsim.dockedservice.service.SharingService;

class OutputWriter implements ShutdownListener {
	private final Id<SharingService> serviceId;
	private final SharingServiceSpecification specification;
	private final OutputDirectoryHierarchy outputHierarchy;

	public OutputWriter(Id<SharingService> serviceId, SharingServiceSpecification specification,
			OutputDirectoryHierarchy outputHierarchy) {
		this.serviceId = serviceId;
		this.specification = specification;
		this.outputHierarchy = outputHierarchy;
	}

	@Override
	public void notifyShutdown(ShutdownEvent event) {
		String path = outputHierarchy.getOutputFilename("output_sharing_" + serviceId + ".xml");
		new SharingServiceWriter(specification).write(path);
	}
}
