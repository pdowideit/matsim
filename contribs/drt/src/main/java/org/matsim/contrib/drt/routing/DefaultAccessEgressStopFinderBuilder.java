package org.matsim.contrib.drt.routing;

import com.google.inject.Inject;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.drt.run.DrtConfigGroup;
import org.matsim.contrib.dvrp.run.ModalProviders;
import org.matsim.core.config.groups.PlansCalcRouteConfigGroup;
import org.matsim.pt.transitSchedule.api.TransitSchedule;

public class DefaultAccessEgressStopFinderBuilder extends ModalProviders.AbstractProvider {
	@Inject private TransitSchedule transitSchedule;
	private DrtConfigGroup drtConfig;
	@Inject private PlansCalcRouteConfigGroup plansCalcRouteCfg;
	@Inject private Network network;

	public DefaultAccessEgressStopFinderBuilder( final DrtConfigGroup drtConfig ) {
		super( drtConfig.getMode() );
		this.drtConfig = drtConfig ;
	}

	@Override public DefaultAccessEgressStopFinder get() {
		return new DefaultAccessEgressStopFinder( transitSchedule, drtConfig, plansCalcRouteCfg, network );
	}

	public DefaultAccessEgressStopFinderBuilder setTransitSchedule( TransitSchedule transitSchedule) {
		this.transitSchedule = transitSchedule;
		return this;
	}

	public DefaultAccessEgressStopFinderBuilder setPlansCalcRouteConfigGroup( PlansCalcRouteConfigGroup plansCalcRouteCfg) {
		this.plansCalcRouteCfg = plansCalcRouteCfg;
		return this;
	}

	public DefaultAccessEgressStopFinderBuilder setNetwork( Network network) {
		this.network = network;
		return this;
	}
}
