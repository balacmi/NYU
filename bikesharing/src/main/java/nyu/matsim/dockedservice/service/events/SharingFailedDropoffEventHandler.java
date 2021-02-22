package nyu.matsim.dockedservice.service.events;

import org.matsim.core.events.handler.EventHandler;

public interface SharingFailedDropoffEventHandler extends EventHandler {
	void handleEvent(SharingFailedDropoffEvent event);
}
