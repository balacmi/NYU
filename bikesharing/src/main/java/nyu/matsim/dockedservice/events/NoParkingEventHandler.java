package nyu.matsim.dockedservice.events;

import org.matsim.core.events.handler.EventHandler;

public interface NoParkingEventHandler extends EventHandler{

	public void handleEvent(final NoParkingEvent event);
}
