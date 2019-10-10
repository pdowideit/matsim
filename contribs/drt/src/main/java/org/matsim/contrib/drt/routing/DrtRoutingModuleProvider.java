package org.matsim.contrib.drt.routing;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.drt.run.DrtConfigGroup;
import org.matsim.contrib.dvrp.run.ModalProviders;
import org.matsim.contrib.dvrp.trafficmonitoring.DvrpTravelTimeModule;
import org.matsim.core.router.FastAStarEuclideanFactory;
import org.matsim.core.router.RoutingModule;
import org.matsim.core.router.costcalculators.TravelDisutilityFactory;
import org.matsim.core.router.util.LeastCostPathCalculatorFactory;
import org.matsim.core.router.util.TravelTime;

public class DrtRoutingModuleProvider extends ModalProviders.AbstractProvider<DrtRoutingModule> {
	private final LeastCostPathCalculatorFactory leastCostPathCalculatorFactory = new FastAStarEuclideanFactory();
	private final DrtConfigGroup drtCfg;

	@Inject
	@Named(DvrpTravelTimeModule.DVRP_ESTIMATED)
	private TravelTime travelTime;

	@Inject
	private Scenario scenario;

	@Inject
	@Named(TransportMode.walk)
	private RoutingModule walkRouter;

	public DrtRoutingModuleProvider( DrtConfigGroup drtCfg ) {
		super(drtCfg.getMode());
		this.drtCfg = drtCfg;
	}

	@Override
	public DrtRoutingModule get() {
		Network network = getModalInstance(Network.class);
		return new DrtRoutingModule(drtCfg, network, leastCostPathCalculatorFactory, travelTime,
				getModalInstance( TravelDisutilityFactory.class), walkRouter, scenario);
	}
}
