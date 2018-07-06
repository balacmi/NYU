package nyu.matsim.bikesharing;

import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy.OverwriteFileSetting;

import nyu.matsim.bikesharing.config.BikesharingConfigGroup;
import nyu.matsim.bikesharing.qsim.BikeshareQsimModule;

/**
 * Hello world!
 *
 */
public class RunBikesharing {
	public static void main(String[] args) {

		Config config = ConfigUtils.loadConfig(args[0], new BikesharingConfigGroup());

		Controler controler = new Controler(config);
		controler.getConfig().controler().setOverwriteFileSetting(OverwriteFileSetting.deleteDirectoryIfExists);

		controler.addOverridingModule(new BikeshareQsimModule());
		controler.addOverridingModule(new BikeshareModule());
		controler.run();

		System.out.println("Bikesharing runner!");
	}
}
