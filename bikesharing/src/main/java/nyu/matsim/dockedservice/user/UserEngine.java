package nyu.matsim.dockedservice.user;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.ActivityStartEvent;
import org.matsim.api.core.v01.events.handler.ActivityStartEventHandler;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.mobsim.framework.MobsimAgent;
import org.matsim.core.mobsim.qsim.InternalInterface;
import org.matsim.core.mobsim.qsim.agents.WithinDayAgentUtils;
import org.matsim.core.mobsim.qsim.interfaces.MobsimEngine;

import nyu.matsim.dockedservice.service.SharingService;
import nyu.matsim.dockedservice.service.SharingUtils;

public class UserEngine implements ActivityStartEventHandler, MobsimEngine {
	private final Id<SharingService> serviceId;
	private final UserLogic logic;
	private final EventsManager eventsManager;

	private InternalInterface internalInterface;

	public UserEngine(Id<SharingService> serviceId, UserLogic logic, EventsManager eventsManager) {
		this.serviceId = serviceId;
		this.logic = logic;
		this.eventsManager = eventsManager;
	}

	@Override
	public void handleEvent(ActivityStartEvent event) {
		if (event.getActType().equals(SharingUtils.PICKUP_ACTIVITY)) {
			MobsimAgent agent = internalInterface.getMobsim().getAgents().get(event.getPersonId());
			Activity activity = (Activity) WithinDayAgentUtils.getCurrentPlanElement(agent);

			if (serviceId.equals(SharingUtils.getServiceId(activity))) {
				logic.tryPickupVehicle(event.getTime(), agent);
			}
		} else if (event.getActType().equals(SharingUtils.DROPOFF_ACTIVITY)) {
			MobsimAgent agent = internalInterface.getMobsim().getAgents().get(event.getPersonId());
			Activity activity = (Activity) WithinDayAgentUtils.getCurrentPlanElement(agent);

			if (serviceId.equals(SharingUtils.getServiceId(activity))) {
				logic.tryDropoffVehicle(event.getTime(), agent);
			}
		}
	}

	@Override
	public void doSimStep(double time) {
	}

	@Override
	public void onPrepareSim() {
		eventsManager.addHandler(this);
	}

	@Override
	public void afterSim() {
		eventsManager.removeHandler(this);
	}

	@Override
	public void setInternalInterface(InternalInterface internalInterface) {
		this.internalInterface = internalInterface;
	}
}
