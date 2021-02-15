package nyu.matsim.dockedservice.router;

import java.util.List;

import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.core.router.MainModeIdentifier;
import org.matsim.core.router.MainModeIdentifierImpl;

import com.google.inject.Inject;

public class BikesharingMainModeIdentifier implements MainModeIdentifier {

	private final MainModeIdentifier delegate = new MainModeIdentifierImpl();

	@Inject
	public BikesharingMainModeIdentifier() {

	}

	@Override
	public String identifyMainMode(List<? extends PlanElement> tripElements) {
		for (PlanElement pe : tripElements) {
			if (pe instanceof Leg) {
				if (((Leg) pe).getMode().contains("access_walk_bike"))
					return "bikeshare";
			}
		}
		return delegate.identifyMainMode(tripElements);
	}
}
