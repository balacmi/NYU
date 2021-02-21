package nyu.matsim.dockedservice.run;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.run.AbstractDvrpModeQSimModule;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.router.RoutingModule;

import com.google.inject.Singleton;

import nyu.matsim.dockedservice.io.SharingServiceSpecification;
import nyu.matsim.dockedservice.service.FreefloatingService;
import nyu.matsim.dockedservice.service.SharingService;
import nyu.matsim.dockedservice.service.SharingUtils;
import nyu.matsim.dockedservice.user.UserEngine;
import nyu.matsim.dockedservice.user.UserLogic;

public class SharingQSimServiceModule extends AbstractDvrpModeQSimModule {
	private final SharingServiceConfigGroup serviceConfig;

	protected SharingQSimServiceModule(SharingServiceConfigGroup serviceConfig) {
		super(SharingUtils.getServiceMode(serviceConfig));
		this.serviceConfig = serviceConfig;
	}

	@Override
	protected void configureQSim() {
		addModalComponent(UserEngine.class);

		bindModal(UserEngine.class).toProvider(modalProvider(getter -> {
			EventsManager eventsManager = getter.get(EventsManager.class);
			UserLogic logic = getter.getModal(UserLogic.class);

			return new UserEngine(Id.create(serviceConfig.getId(), SharingService.class), logic, eventsManager);
		})).in(Singleton.class);

		bindModal(UserLogic.class).toProvider(modalProvider(getter -> {
			EventsManager eventsManager = getter.get(EventsManager.class);
			Scenario scenario = getter.get(Scenario.class);

			SharingService service = getter.getModal(SharingService.class);

			RoutingModule accessEgressRoutingModule = getter.getNamed(RoutingModule.class, TransportMode.walk);
			RoutingModule mainModeRoutingModule = getter.getNamed(RoutingModule.class, serviceConfig.getMode());

			return new UserLogic(service, accessEgressRoutingModule, mainModeRoutingModule, scenario, eventsManager);
		})).in(Singleton.class);

		bindModal(FreefloatingService.class).toProvider(modalProvider(getter -> {
			Network network = getter.get(Network.class);
			EventsManager eventsManager = getter.get(EventsManager.class);
			SharingServiceSpecification specification = getter.getModal(SharingServiceSpecification.class);

			return new FreefloatingService(Id.create(serviceConfig.getId(), SharingService.class),
					specification.getVehicles(), network, serviceConfig.getMaximumAccessEgressDistance(),
					eventsManager);
		}));

		switch (serviceConfig.getServiceScheme()) {
		case Freefloating:
			bindModal(SharingService.class).to(modalKey(FreefloatingService.class));
			break;
		case StationBased:
			throw new IllegalStateException("TODO");
		default:
			throw new IllegalStateException();
		}
	}
}
