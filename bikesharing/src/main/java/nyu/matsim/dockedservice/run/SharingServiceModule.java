
package nyu.matsim.dockedservice.run;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.run.AbstractDvrpModeModule;
import org.matsim.core.config.ConfigGroup;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.router.RoutingModule;

import com.google.inject.Singleton;

import nyu.matsim.dockedservice.io.DefaultSharingServiceSpecification;
import nyu.matsim.dockedservice.io.SharingServiceReader;
import nyu.matsim.dockedservice.io.SharingServiceSpecification;
import nyu.matsim.dockedservice.io.validation.FreefloatingServiceValidator;
import nyu.matsim.dockedservice.io.validation.SharingServiceValidator;
import nyu.matsim.dockedservice.io.validation.StationBasedServiceValidator;
import nyu.matsim.dockedservice.routing.FreefloatingInteractionFinder;
import nyu.matsim.dockedservice.routing.InteractionFinder;
import nyu.matsim.dockedservice.routing.SharingRoutingModule;
import nyu.matsim.dockedservice.routing.StationBasedInteractionFinder;
import nyu.matsim.dockedservice.service.SharingService;
import nyu.matsim.dockedservice.service.SharingUtils;

public class SharingServiceModule extends AbstractDvrpModeModule {
	private final SharingServiceConfigGroup serviceConfig;

	public SharingServiceModule(SharingServiceConfigGroup serviceConfig) {
		super(SharingUtils.getServiceMode(serviceConfig));
		this.serviceConfig = serviceConfig;
	}

	@Override
	public void install() {
		installQSimModule(new SharingQSimServiceModule(serviceConfig));

		bindModal(SharingServiceSpecification.class).toProvider(modalProvider(getter -> {
			SharingServiceSpecification specification = new DefaultSharingServiceSpecification();
			new SharingServiceReader(specification).readURL(
					ConfigGroup.getInputFileURL(getConfig().getContext(), serviceConfig.getServiceInputFile()));
			return specification;
		})).in(Singleton.class);

		bindModal(SharingRoutingModule.class).toProvider(modalProvider(getter -> {
			Scenario scenario = getter.get(Scenario.class);
			RoutingModule accessEgressRoutingModule = getter.getNamed(RoutingModule.class, TransportMode.walk);
			RoutingModule mainModeRoutingModule = getter.getNamed(RoutingModule.class, serviceConfig.getMode());

			InteractionFinder interactionFinder = getter.getModal(InteractionFinder.class);

			return new SharingRoutingModule(scenario, accessEgressRoutingModule, mainModeRoutingModule,
					interactionFinder, Id.create(serviceConfig.getId(), SharingService.class));
		}));

		addRoutingModuleBinding(getMode()).to(modalKey(SharingRoutingModule.class));

		bindModal(FreefloatingInteractionFinder.class).toProvider(modalProvider(getter -> {
			Network network = getter.get(Network.class);
			return new FreefloatingInteractionFinder(network);
		})).in(Singleton.class);

		bindModal(StationBasedInteractionFinder.class).toProvider(modalProvider(getter -> {
			Network network = getter.get(Network.class);
			SharingServiceSpecification specification = getter.getModal(SharingServiceSpecification.class);

			return new StationBasedInteractionFinder(network, specification,
					serviceConfig.getMaximumAccessEgressDistance());
		}));

		bindModal(OutputWriter.class).toProvider(modalProvider(getter -> {
			SharingServiceSpecification specification = getter.getModal(SharingServiceSpecification.class);
			OutputDirectoryHierarchy outputHierarchy = getter.get(OutputDirectoryHierarchy.class);

			return new OutputWriter(Id.create(serviceConfig.getId(), SharingService.class), specification,
					outputHierarchy);
		})).in(Singleton.class);

		addControlerListenerBinding().to(modalKey(OutputWriter.class));

		bindModal(FreefloatingServiceValidator.class).toProvider(modalProvider(getter -> {
			return new FreefloatingServiceValidator(Id.create(serviceConfig.getId(), SharingService.class));
		})).in(Singleton.class);

		bindModal(StationBasedServiceValidator.class).toProvider(modalProvider(getter -> {
			return new StationBasedServiceValidator(Id.create(serviceConfig.getId(), SharingService.class));
		})).in(Singleton.class);

		bindModal(ValidationListener.class).toProvider(modalProvider(getter -> {
			SharingServiceSpecification specification = getter.getModal(SharingServiceSpecification.class);
			SharingServiceValidator validator = getter.getModal(SharingServiceValidator.class);

			return new ValidationListener(Id.create(serviceConfig.getId(), SharingService.class), validator,
					specification);
		}));

		addControlerListenerBinding().to(modalKey(ValidationListener.class));

		switch (serviceConfig.getServiceScheme()) {
		case Freefloating:
			bindModal(InteractionFinder.class).to(modalKey(FreefloatingInteractionFinder.class));
			bindModal(SharingServiceValidator.class).to(modalKey(FreefloatingServiceValidator.class));
			break;
		case StationBased:
			bindModal(InteractionFinder.class).to(modalKey(StationBasedInteractionFinder.class));
			bindModal(SharingServiceValidator.class).to(modalKey(StationBasedServiceValidator.class));
			break;
		default:
			throw new IllegalStateException();
		}
	}
}
