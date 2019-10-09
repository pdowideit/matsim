/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2016 by the members listed in the COPYING,        *
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
 * *********************************************************************** */

package org.matsim.contrib.drt.routing;

import static org.matsim.core.router.NetworkRoutingInclAccessEgressModule.addBushwhackingLegFromLinkToFacilityIfNecessary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.inject.Inject;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.contrib.drt.run.DrtConfigGroup;
import org.matsim.contrib.dvrp.path.VrpPathWithTravelData;
import org.matsim.contrib.dvrp.path.VrpPaths;
import org.matsim.contrib.dvrp.run.ModalProviders;
import org.matsim.contrib.dvrp.trafficmonitoring.DvrpTravelTimeModule;
import org.matsim.core.config.Config;
import org.matsim.core.config.groups.PlansCalcRouteConfigGroup;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.router.*;
import org.matsim.core.router.costcalculators.TravelDisutilityFactory;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.LeastCostPathCalculatorFactory;
import org.matsim.core.router.util.TravelTime;
import org.matsim.facilities.FacilitiesUtils;
import org.matsim.facilities.Facility;

import com.google.inject.name.Named;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;

/**
 * @author jbischoff
 * @author michalm (Michal Maciejewski)
 * @author Kai Nagel
 */
public final class DrtRoutingModule implements RoutingModule {
	private static final Logger LOGGER = Logger.getLogger(DrtRoutingModule.class);

	private final DrtConfigGroup drtCfg;
	private final Config config;
	private final Network network;
	private final TravelTime travelTime;
	private final LeastCostPathCalculator router;
	private final PopulationFactory populationFactory;
	private final RoutingModule walkRouter;
	private final DrtStageActivityType drtStageActivityType;
	private final PlansCalcRouteConfigGroup plansCalcRouteConfig;

	DrtRoutingModule(DrtConfigGroup drtCfg, Network network,
			LeastCostPathCalculatorFactory leastCostPathCalculatorFactory,
			@Named(DvrpTravelTimeModule.DVRP_ESTIMATED) TravelTime travelTime,
			TravelDisutilityFactory travelDisutilityFactory, @Named(TransportMode.walk) RoutingModule walkRouter,
			Scenario scenario) {
		// please leave constructors with long argument lists package private.  kai, oct'19

		this.drtCfg = drtCfg;
		this.config = scenario.getConfig();
		this.network = network;
		this.travelTime = travelTime;
		this.populationFactory = scenario.getPopulation().getFactory();
		this.walkRouter = walkRouter;
		this.drtStageActivityType = new DrtStageActivityType(drtCfg.getMode());
		this.plansCalcRouteConfig = scenario.getConfig().plansCalcRoute();

		// Euclidean with overdoFactor > 1.0 could lead to 'experiencedTT < unsharedRideTT',
		// while the benefit would be a marginal reduction of computation time ==> so stick to 1.0
		router = leastCostPathCalculatorFactory.createPathCalculator(network,
				travelDisutilityFactory.createTravelDisutility(travelTime), travelTime);
	}

	@Override
	public List<? extends PlanElement> calcRoute(Facility fromFacility, Facility toFacility, double departureTime,
			Person person) {
		LOGGER.debug("entering calcRoute ...");
		LOGGER.debug("fromFacility=" + fromFacility.toString());
		LOGGER.debug("toFacility=" + toFacility.toString());

		Gbl.assertNotNull(fromFacility);
		Gbl.assertNotNull(toFacility);

		Link accessActLink = FacilitiesUtils.decideOnLink(fromFacility, network);
		Link egressActLink = FacilitiesUtils.decideOnLink(toFacility, network);

		double now = departureTime;

		if (accessActLink == egressActLink) {
			if (drtCfg.isPrintDetailedWarnings()) {
				LOGGER.error("Start and end stop are the same, agent will walk using mode "
						+ drtStageActivityType.drtWalk
						+ ". Agent Id:\t"
						+ person.getId());
			}
			Leg leg = (Leg)walkRouter.calcRoute(fromFacility, toFacility, departureTime, person).get(0);
			leg.setDepartureTime(now);
			leg.setMode(drtStageActivityType.drtWalk);
			LOGGER.debug("travel time on walk leg=" + leg.getTravelTime());
			return Collections.singletonList(leg);
		}
		// yyyy I think that our life will become easier if we don't do direct walk.  kai, jul'19

		List<PlanElement> result = new ArrayList<>();

		// === access:
		if (plansCalcRouteConfig.isInsertingAccessEgressWalk() || !(fromFacility instanceof TransitStopFacility ))  {
//			now = addBushwhackingLegFromFacilityToLinkIfNecessary(fromFacility, person, accessActLink, now, result,
//					populationFactory, drtStageActivityType.drtStageActivity);
			List<? extends PlanElement> result2 = walkRouter.calcRoute( fromFacility, new LinkWrapperFacility( accessActLink ), now, person );
			result.addAll( result2 ) ;
			for( PlanElement planElement : result2 ){
				now = TripRouter.calcEndOfPlanElement( now, planElement,  config ) ;
			}
		}

		// === drt proper:
		final List<PlanElement> newResult = addDirectDrtRoute( departureTime, accessActLink, egressActLink );
		result.addAll( newResult ) ;
		for ( final PlanElement planElement : newResult ) {
			now = TripRouter.calcEndOfPlanElement( now, planElement, config ) ;
		}

		// === egress:
		if (plansCalcRouteConfig.isInsertingAccessEgressWalk()) {
			addBushwhackingLegFromLinkToFacilityIfNecessary(toFacility, person, egressActLink, now, result,
					populationFactory, drtStageActivityType.drtStageActivity);
		}

		return result;
	}

	/* package */ List<PlanElement> addDirectDrtRoute( final double departureTime, final Link accessActLink, final Link egressActLink ) {
		VrpPathWithTravelData unsharedPath = VrpPaths.calcAndCreatePath(accessActLink, egressActLink, departureTime,
				router, travelTime);
		double unsharedRideTime = unsharedPath.getTravelTime();//includes first & last link
		double maxTravelTime = getMaxTravelTime(drtCfg, unsharedRideTime);
		double unsharedDistance = VrpPaths.calcDistance(unsharedPath);//includes last link

		DrtRoute route = populationFactory.getRouteFactories()
				.createRoute(DrtRoute.class, accessActLink.getId(), egressActLink.getId());
		route.setDistance(unsharedDistance);
		route.setTravelTime(maxTravelTime);
		route.setUnsharedRideTime(unsharedRideTime);
		route.setMaxWaitTime(drtCfg.getMaxWaitTime());

		Leg leg = populationFactory.createLeg(drtCfg.getMode());
		leg.setDepartureTime(departureTime);
		leg.setTravelTime(maxTravelTime);
		leg.setRoute(route);
		List<PlanElement> result = new ArrayList<>() ;
		result.add(leg);
		return result;
	}

	/**
	 * Calculates the maximum travel time defined as: drtCfg.getMaxTravelTimeAlpha() * unsharedRideTime + drtCfg.getMaxTravelTimeBeta()
	 *
	 * @param drtCfg
	 * @param unsharedRideTime ride time of the direct (shortest-time) route
	 * @return maximum travel time
	 */
	static double getMaxTravelTime( DrtConfigGroup drtCfg, double unsharedRideTime ) {
		return drtCfg.getMaxTravelTimeAlpha() * unsharedRideTime + drtCfg.getMaxTravelTimeBeta();
	}
	
	public static class Provider extends ModalProviders.AbstractProvider<DrtRoutingModule> {
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
	
		public Provider( DrtConfigGroup drtCfg ) {
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
}
