package nyu.matsim.dockedservice.listeners;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import javax.inject.Inject;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.controler.MatsimServices;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.listener.IterationEndsListener;
import org.matsim.core.utils.io.IOUtils;

import nyu.matsim.dockedservice.events.BikeshareDemand;
import nyu.matsim.dockedservice.utils.RentalInfo;


public class BikeshareDemandWriter implements IterationEndsListener {
	@Inject
	private MatsimServices controler;
	@Inject
	private BikeshareDemand demandHandler;

	@Override
	public void notifyIterationEnds(IterationEndsEvent event) {


		Map<Id<Person>, ArrayList<RentalInfo>> agentRentalsMap = demandHandler.getAgentRentalsMap();
		final BufferedWriter outLink = IOUtils.getBufferedWriter(
				this.controler.getControlerIO().getIterationFilename(event.getIteration(), "BS.txt"));
		try {
			outLink.write(
					"personID,startTime,endTIme,startLink,pickupLink,dropoffLink,endLink,startCoordX,startCoordY,"
							+ "pickupCoordX,pickupCoordY,dropoffCoordX,dropoffCoordY,endCoordX,endCoordY,distance,"
							+ "inVehicleTime,accessTime,egressTime,vehicleID");
			outLink.newLine();

			for (Id<Person> personId : agentRentalsMap.keySet()) {

				for (RentalInfo i : agentRentalsMap.get(personId)) {

					outLink.write(personId + "," + i.toString());
					outLink.newLine();
				}

			}

			outLink.flush();
			outLink.close();

		} catch (IOException e) {
			e.printStackTrace();
		}		

	}

}
