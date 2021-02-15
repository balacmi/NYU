package nyu.matsim.dockedservice.config;

import org.matsim.core.config.ReflectiveConfigGroup;

public class BikesharingConfigGroup extends ReflectiveConfigGroup {

	private static final String GROUP_NAME = "bikesharing";
	private int fleetSize = 100;

	public BikesharingConfigGroup() {
		super(GROUP_NAME);
	}

	@StringSetter("fleetSize")
	public void setFleetSize(final String fleetSize) {
		this.fleetSize = Integer.parseInt(fleetSize);
	}

	@StringGetter("fleetSize")
	public int getFleetSize() {
		return this.fleetSize;
	}

}
