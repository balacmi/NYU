package nyu.matsim.dockedservice.service.events;

import org.matsim.core.events.handler.EventHandler;

public interface SharingVehicleEventHandler extends EventHandler {
	void handleEvent(SharingVehicleEvent event);
}
