package nyu.matsim.dockedservice.service.events;

import org.matsim.core.events.handler.EventHandler;

public interface SharingPickupEventHandler extends EventHandler {
	void handleEvent(SharingPickupEvent event);
}
