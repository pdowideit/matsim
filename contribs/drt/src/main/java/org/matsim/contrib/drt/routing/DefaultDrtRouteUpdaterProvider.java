package org.matsim.contrib.drt.routing;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Population;
import org.matsim.contrib.drt.run.DrtConfigGroup;
import org.matsim.contrib.dvrp.run.ModalProviders;
import org.matsim.contrib.dvrp.trafficmonitoring.DvrpTravelTimeModule;
import org.matsim.core.config.Config;
import org.matsim.core.router.costcalculators.TravelDisutilityFactory;
import org.matsim.core.router.util.TravelTime;

public class DefaultDrtRouteUpdaterProvider extends ModalProviders.AbstractProvider<DrtRouteUpdater> {
	@Inject
	@Named(DvrpTravelTimeModule.DVRP_ESTIMATED)
	private TravelTime travelTime;

	@Inject
	private Population population;

	@Inject
	private Config config;
	private DrtConfigGroup drtCfg;

	public DefaultDrtRouteUpdaterProvider( final DrtConfigGroup drtCfg ) {
		super( drtCfg.getMode() );
		this.drtCfg = drtCfg;
	}

	@Override
	public DefaultDrtRouteUpdater get() {
		Network network = getModalInstance(Network.class);
		return new DefaultDrtRouteUpdater( drtCfg, network, travelTime,
				getModalInstance( TravelDisutilityFactory.class), population, config);
	}
}
