package org.matsim.contrib.drt.routing;

import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.drt.run.DrtConfigGroup;
import org.matsim.contrib.dvrp.run.ModalProviders;
import org.matsim.core.config.groups.PlansCalcRouteConfigGroup;
import org.matsim.pt.transitSchedule.api.TransitSchedule;

import javax.inject.Inject;

public class ClosestAccessEgressTopFinderProvider extends ModalProviders.AbstractProvider<StopBasedDrtRoutingModule.AccessEgressStopFinder> {
	@Inject PlansCalcRouteConfigGroup plansCalcRouteConfig ;
	@Inject Network network ;
	private DrtConfigGroup drtCfg;

	public ClosestAccessEgressTopFinderProvider( final DrtConfigGroup drtCfg ) {
		super( drtCfg.getMode() );
		this.drtCfg = drtCfg ;
	}

	@Override
	public ClosestAccessEgressStopFinder get() {
		return new ClosestAccessEgressStopFinder(  getModalInstance( TransitSchedule.class ), drtCfg, plansCalcRouteConfig, network ) ;
	}
}
