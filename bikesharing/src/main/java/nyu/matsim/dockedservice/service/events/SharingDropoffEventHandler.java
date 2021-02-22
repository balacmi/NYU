package nyu.matsim.dockedservice.service.events;

import org.matsim.core.events.handler.EventHandler;

public interface SharingDropoffEventHandler extends EventHandler {
	void handleEvent(SharingDropoffEvent event);
}
