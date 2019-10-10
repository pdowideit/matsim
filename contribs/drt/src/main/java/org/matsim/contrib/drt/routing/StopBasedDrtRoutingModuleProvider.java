package org.matsim.contrib.drt.routing;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.drt.run.DrtConfigGroup;
import org.matsim.contrib.dvrp.run.ModalProviders;
import org.matsim.core.router.RoutingModule;

public class StopBasedDrtRoutingModuleProvider extends ModalProviders.AbstractProvider<StopBasedDrtRoutingModule> {
	private final DrtConfigGroup drtCfg ;
	@Inject private Scenario scenario ;
	@Inject @Named(TransportMode.walk) private RoutingModule walkRouter ;

	public StopBasedDrtRoutingModuleProvider( final DrtConfigGroup drtCfg ) {
		super( drtCfg.getMode() );
		this.drtCfg = drtCfg ;
	}

	@Override
	public StopBasedDrtRoutingModule get() {
		return  new StopBasedDrtRoutingModule(
				getModalInstance( DrtRoutingModule.class ),
				walkRouter,
				getModalInstance( StopBasedDrtRoutingModule.AccessEgressStopFinder.class ),
				drtCfg,
				scenario,
				getModalInstance( Network.class )
		) ;
	}
}
