package nyu.matsim.dockedservice;

import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy.OverwriteFileSetting;
import org.matsim.core.mobsim.qsim.components.QSimComponentsConfig;
import org.matsim.core.mobsim.qsim.components.QSimComponentsConfigurator;

import nyu.matsim.dockedservice.config.BikesharingConfigGroup;
import nyu.matsim.dockedservice.qsim.BikeshareQsimModule;

public class RunBikesharing {
	public static void main(String[] args) {

		Config config = ConfigUtils.loadConfig(args[0], new BikesharingConfigGroup());

		Controler controler = new Controler(config);
		controler.getConfig().controler().setOverwriteFileSetting(OverwriteFileSetting.deleteDirectoryIfExists);

		controler.configureQSimComponents(new QSimComponentsConfigurator() {
			@Override
			public void configure(QSimComponentsConfig components) {
				components.addNamedComponent("bikesharing");

			}
		});
		controler.addOverridingQSimModule(new BikeshareQsimModule());
		controler.addOverridingModule(new BikeshareModule());
		controler.run();

		System.out.println("Bikesharing runner!");
	}
}
