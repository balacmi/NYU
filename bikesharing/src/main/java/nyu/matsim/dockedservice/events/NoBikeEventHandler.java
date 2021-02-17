package nyu.matsim.dockedservice.events;

import org.matsim.core.events.handler.EventHandler;

public interface NoBikeEventHandler extends EventHandler{
	public void handleEvent(final NoBikeEvent event);

}
