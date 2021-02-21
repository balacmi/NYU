package nyu.matsim.dockedservice.examples;

import java.util.Arrays;
import java.util.List;

import org.matsim.core.config.CommandLine;
import org.matsim.core.config.CommandLine.ConfigurationException;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;

import nyu.matsim.dockedservice.run.SharingConfigGroup;
import nyu.matsim.dockedservice.run.SharingModule;
import nyu.matsim.dockedservice.run.SharingServiceConfigGroup;
import nyu.matsim.dockedservice.run.SharingServiceConfigGroup.ServiceScheme;
import nyu.matsim.dockedservice.service.SharingUtils;

public class RunTeleportationFreefloatingBikesharing {
	static public void main(String[] args) throws ConfigurationException {
		CommandLine cmd = new CommandLine.Builder(args) //
				.requireOptions("config-path") //
				.build();

		Config config = ConfigUtils.loadConfig(cmd.getOptionStrict("config-path"));

		// By default, "bike" is configured to use Euclidean distance with
		// distance/speed factor.
		// By default, "bike" is configured to be simulated using teleportation.

		// We need to add the sharing config group
		SharingConfigGroup sharingConfig = new SharingConfigGroup();
		config.addModule(sharingConfig);

		// We need to define a service ...
		SharingServiceConfigGroup serviceConfig = new SharingServiceConfigGroup();
		sharingConfig.addService(serviceConfig);

		// ... with a service id. The respective mode will be "sharing:velib".
		serviceConfig.setId("velib");

		// ... with freefloating characteristics
		serviceConfig.setMaximumAccessEgressDistance(1000);
		serviceConfig.setServiceScheme(ServiceScheme.Freefloating);
		serviceConfig.setServiceAreaShapeFile("velib_area.shp");

		// ... with a number of available vehicles and their initial locations
		serviceConfig.setServiceInputFile("velib.xml");

		// ... and, we need to define the underlying mode, here "bike".
		serviceConfig.setMode("bike");

		// Finally, we need to make sure that the service mode (sharing:velib) is
		// considered in mode choice.
		List<String> modes = Arrays.asList(config.subtourModeChoice().getModes());
		modes.add(SharingUtils.getServiceMode(serviceConfig));
		config.subtourModeChoice().setModes(modes.toArray(new String[modes.size()]));

		// Set up controller (no specific settings needed for scenario)
		Controler controller = new Controler(config);

		// Does not really "override" anything
		controller.addOverridingModule(new SharingModule());

		controller.run();
	}
}
