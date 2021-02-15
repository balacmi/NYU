package nyu.matsim.dockedservice.qsim;

import org.matsim.core.mobsim.qsim.AbstractQSimModule;

public class BikeshareQsimModule extends AbstractQSimModule  {

	@Override
	protected void configureQSim() {

		this.addQSimComponentBinding( "bikesharing" ).to( BikeshareDepartureHandler.class ) ;
	}
	

}
