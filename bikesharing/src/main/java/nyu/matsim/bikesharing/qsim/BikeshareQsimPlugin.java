package nyu.matsim.bikesharing.qsim;

import java.util.Arrays;
import java.util.Collection;

import org.matsim.core.config.Config;
import org.matsim.core.mobsim.qsim.AbstractQSimPlugin;
import org.matsim.core.mobsim.qsim.interfaces.DepartureHandler;

public class BikeshareQsimPlugin extends AbstractQSimPlugin {
	public BikeshareQsimPlugin(Config config) {
		super(config);

	}

	public Collection<Class<? extends DepartureHandler>> departureHandlers() {
		return Arrays.asList(BikeshareDepartureHandler.class);
	}

}
