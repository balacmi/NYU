package nyu.matsim.bikesharing;

import java.util.List;

import org.matsim.core.controler.events.BeforeMobsimEvent;
import org.matsim.core.controler.listener.BeforeMobsimListener;

import com.google.inject.Inject;

import nyu.matsim.bikesharing.infrastructure.BikeshareServiceInterface;
import nyu.matsim.bikesharing.infrastructure.BikesharingStation;

public class BikeshareLoader implements BeforeMobsimListener {

	@Inject
	private BikeshareServiceInterface service;

	@Inject
	private List<BikesharingStation> stations;

	@Override
	public void notifyBeforeMobsim(BeforeMobsimEvent event) {

		service.setUp(stations);

	}

}
