/*
 * *********************************************************************** *
 * project: org.matsim.*
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2018 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** *
 */

package org.matsim.contrib.drt.run;

import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.drt.optimizer.rebalancing.NoRebalancingStrategy;
import org.matsim.contrib.drt.optimizer.rebalancing.RebalancingStrategy;
import org.matsim.contrib.drt.optimizer.rebalancing.mincostflow.DrtModeMinCostFlowRebalancingModule;
import org.matsim.contrib.drt.routing.ClosestAccessEgressStopFinder;
import org.matsim.contrib.drt.routing.DefaultDrtRouteUpdater;
import org.matsim.contrib.drt.routing.DrtRouteUpdater;
import org.matsim.contrib.drt.routing.DrtRoutingModule;
import org.matsim.contrib.drt.routing.StopBasedDrtRoutingModule;
import org.matsim.contrib.drt.routing.StopBasedDrtRoutingModule.AccessEgressStopFinder;
import org.matsim.contrib.dvrp.fleet.FleetModule;
import org.matsim.contrib.dvrp.router.DvrpRoutingNetworkProvider;
import org.matsim.contrib.dvrp.router.TimeAsTravelDisutility;
import org.matsim.contrib.dvrp.run.AbstractDvrpModeModule;
import org.matsim.contrib.dvrp.run.DvrpModes;
import org.matsim.contrib.dvrp.run.ModalProviders;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.router.costcalculators.TravelDisutilityFactory;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.pt.transitSchedule.api.TransitSchedule;
import org.matsim.pt.transitSchedule.api.TransitScheduleReader;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;
import org.matsim.utils.gis.shp2matsim.ShpGeometryUtils;

/**
 * @author michalm (Michal Maciejewski)
 */
public final class DrtModeModule extends AbstractDvrpModeModule {
	private final DrtConfigGroup drtCfg;

	public DrtModeModule(DrtConfigGroup drtCfg) {
		super(drtCfg.getMode());
		this.drtCfg = drtCfg;
	}

	@Override
	public void install() {
		DvrpModes.registerDvrpMode(binder(), getMode());
		install(DvrpRoutingNetworkProvider.createDvrpModeRoutingNetworkModule(getMode(),
				drtCfg.isUseModeFilteredSubnetwork()));
		bindModal(TravelDisutilityFactory.class).toInstance(TimeAsTravelDisutility::new);

		install(new FleetModule(getMode(), drtCfg.getVehiclesFileUrl(getConfig().getContext()),
				drtCfg.isChangeStartLinkToLastLinkInSchedule()));

		if (drtCfg.getMinCostFlowRebalancing().isPresent()) {
			install(new DrtModeMinCostFlowRebalancingModule(drtCfg));
		} else {
			bindModal(RebalancingStrategy.class).to(NoRebalancingStrategy.class).asEagerSingleton();
		}

		switch (drtCfg.getOperationalScheme()) {
			case door2door:
				addRoutingModuleBinding(getMode()).toProvider(new DrtRoutingModule.Provider(drtCfg));//not singleton
				break;

			case serviceAreaBased:
			case stopbased:
				if (drtCfg.getOperationalScheme() == DrtConfigGroup.OperationalScheme.serviceAreaBased) {
					bindModal(TransitSchedule.class).toProvider(new ShapeFileStopProvider(getConfig(), drtCfg))
							.asEagerSingleton();
				} else {
					bindModal(TransitSchedule.class).toInstance(readTransitSchedule());
				}
				bindModal(DrtRoutingModule.class).toProvider(new DrtRoutingModule.Provider(drtCfg));//not singleton
				
				addRoutingModuleBinding(getMode()).toProvider( new StopBasedDrtRoutingModule.Provider( drtCfg) );//not singleton

				bindModal(AccessEgressStopFinder.class).toProvider( new ClosestAccessEgressStopFinder.Provider( drtCfg ) ).asEagerSingleton();

				break;

			default:
				throw new IllegalStateException();
		}

		bindModal(DrtRouteUpdater.class).toProvider( new DefaultDrtRouteUpdater.DefaultDrtRouteUpdaterProvider( drtCfg ) ).asEagerSingleton();

		addControlerListenerBinding().to(modalKey(DrtRouteUpdater.class));
	}

	private static class ShapeFileStopProvider extends ModalProviders.AbstractProvider<TransitSchedule> {

		private final DrtConfigGroup drtCfg;
		private final URL context;

		protected ShapeFileStopProvider(Config config, DrtConfigGroup drtCfg) {
			super(drtCfg.getMode());
			this.drtCfg = drtCfg;
			this.context = config.getContext();
		}

		@Override
		public TransitSchedule get() {
			final List<PreparedGeometry> preparedGeometries = ShpGeometryUtils.loadPreparedGeometries(
					drtCfg.getDrtServiceAreaShapeFileURL(context));
			Network network = getModalInstance(Network.class);
			Set<Link> relevantLinks = network.getLinks()
					.values()
					.stream()
					.filter(link -> ShpGeometryUtils.isCoordInPreparedGeometries(link.getToNode().getCoord(),
							preparedGeometries))
					.collect(Collectors.toSet());
			final TransitSchedule schedule = ScenarioUtils.createScenario(ConfigUtils.createConfig())
					.getTransitSchedule();
			relevantLinks.stream().forEach(link -> {
				TransitStopFacility f = schedule.getFactory()
						.createTransitStopFacility(Id.create(link.getId(), TransitStopFacility.class),
								link.getToNode().getCoord(), false);
				f.setLinkId(link.getId());
				schedule.addStopFacility(f);
			});
			return schedule;
		}
	}

	private TransitSchedule readTransitSchedule() {
		URL url = drtCfg.getTransitStopsFileUrl(getConfig().getContext());
		Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		new TransitScheduleReader(scenario).readURL(url);
		return scenario.getTransitSchedule();
	}

}
