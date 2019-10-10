/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2017 by the members listed in the COPYING,        *
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

/**
 *
 */
package org.matsim.contrib.drt.routing;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.google.inject.Inject;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.contrib.drt.run.DrtConfigGroup;
import org.matsim.contrib.dvrp.run.ModalProviders;
import org.matsim.core.router.RoutingModule;
import org.matsim.core.router.TripRouter;
import org.matsim.facilities.Facility;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;

import com.google.inject.name.Named;

/**
 * @author jbischoff
 * @author michalm (Michal Maciejewski)
 */
public class StopBasedDrtRoutingModule implements RoutingModule {
	private final Scenario scenario;

	private static final Logger logger = Logger.getLogger(StopBasedDrtRoutingModule.class);
	private static final String walkRouterMode = TransportMode.walk;

	public interface AccessEgressStopFinder {
		Pair<TransitStopFacility, TransitStopFacility> findStops(Facility fromFacility, Facility toFacility);
	}

	private final DrtStageActivityType drtStageActivityType;
	private final Network modalNetwork;
	private final RoutingModule walkRouter;
	private final AccessEgressStopFinder stopFinder;
	private final DrtConfigGroup drtCfg;
	private final DrtRoutingModule drtRoutingModule;

	/* package */ StopBasedDrtRoutingModule( DrtRoutingModule drtRoutingModule,
						    @Named(walkRouterMode) RoutingModule walkRouter, AccessEgressStopFinder stopFinder,
						    DrtConfigGroup drtCfg, Scenario scenario, Network modalNetwork ) {
		// please leave constructors with long argument lists package private.  kai, oct'19
		this.drtRoutingModule = drtRoutingModule;
		this.walkRouter = walkRouter;
		this.stopFinder = stopFinder;
		this.drtCfg = drtCfg;
		this.scenario = scenario ;
		this.drtStageActivityType = new DrtStageActivityType(drtCfg.getMode());
		this.modalNetwork = modalNetwork;
	}

	@Override
	public List<? extends PlanElement> calcRoute(Facility fromFacility, Facility toFacility, double departureTime,
			Person person) {
		Pair<TransitStopFacility, TransitStopFacility> stops = stopFinder.findStops(fromFacility, toFacility);

		TransitStopFacility accessFacility = stops.getLeft();
		if (accessFacility == null) {
			printWarning(() -> "No access stop found, agent will walk, using mode "
					+ drtStageActivityType.drtWalk
					+ ". Agent Id:\t"
					+ person.getId());
			return createWalkTrip(fromFacility, toFacility, departureTime, person, drtStageActivityType.drtWalk);
		}

		TransitStopFacility egressFacility = stops.getRight();
		if (egressFacility == null) {
			printWarning(() -> "No egress stop found, agent will walk, using mode "
					+ drtStageActivityType.drtWalk
					+ ". Agent Id:\t"
					+ person.getId());
			return createWalkTrip(fromFacility, toFacility, departureTime, person, drtStageActivityType.drtWalk);
		}

		if (accessFacility.getLinkId() == egressFacility.getLinkId()) {
			if( drtCfg.isPrintDetailedWarnings() ){
				printWarning(() -> "Start and end stop are the same, agent will walk, using mode "
						+ drtStageActivityType.drtWalk
						+ ". Agent Id:\t"
						+ person.getId());
			}
			return createWalkTrip(fromFacility, toFacility, departureTime, person, drtStageActivityType.drtWalk);
		}

		List<PlanElement> trip = new ArrayList<>();

		double now = departureTime;

		// access leg:
		List<? extends PlanElement> accessWalk = createWalkTrip(fromFacility, accessFacility, now, person, null );
		trip.addAll(accessWalk);
		for( PlanElement planElement : accessWalk ){
			now = TripRouter.calcEndOfPlanElement( now, planElement, scenario.getConfig() ) ;
		}

		// interaction activity:
		trip.add(createDrtStageActivity(accessFacility));

		// drt proper:
//		double drtLegStartTime = departureTime + walkToAccessStopLeg.getTravelTime() + 1;
		now++ ;
//		Leg drtLeg = (Leg)drtRoutingModule.calcRoute(accessFacility, egressFacility, now, person).get(0);
//		List<? extends PlanElement> drtLeg = drtRoutingModule.calcRoute( accessFacility, egressFacility, now, person );
		Link accessActLink = modalNetwork.getLinks().get(  accessFacility.getLinkId() ) ; // we want that this crashes if not found.  kai/gl, oct'19
		Link egressActLink = modalNetwork.getLinks().get(  egressFacility.getLinkId() ) ; // we want that this crashes if not found.  kai/gl, oct'19
		List<? extends PlanElement> drtLeg = drtRoutingModule.addDirectDrtRoute( departureTime, accessActLink, egressActLink ) ;
		trip.addAll(drtLeg);
		for( PlanElement planElement : drtLeg ){
			now = TripRouter.calcEndOfPlanElement( now, planElement, scenario.getConfig() ) ;
		}

		// interaction activity:
		trip.add(createDrtStageActivity(egressFacility));

		// egress leg:
//		double walkFromStopStartTime = drtLeg.getDepartureTime() + drtLeg.getTravelTime() + 1;
		now++ ;
		trip.addAll(createWalkTrip(egressFacility, toFacility, now, person, null));

		return trip;
	}

	private List<? extends PlanElement> createWalkTrip(Facility fromFacility, Facility toFacility, double departureTime, Person person, String mode) {
		List<? extends PlanElement> result = walkRouter.calcRoute( fromFacility, toFacility, departureTime, person );
		if (mode != null) {
			// Overwrite real walk mode legs with fallback mode for respective drt mode
			for (PlanElement pe: result) {
				if (pe instanceof Leg) {
					Leg leg = (Leg) pe;
					if (leg.getMode().equals(walkRouterMode)) {
						// keep non_network_walk bushwhacking to network, but replace real network walk leg
						leg.setMode(mode);
					}
				}
			}
		}

		return result ;
	}

	private Activity createDrtStageActivity(Facility stopFacility) {
		Activity activity = scenario.getPopulation().getFactory().createActivityFromCoord(drtStageActivityType.drtStageActivity,
				stopFacility.getCoord());
		activity.setMaximumDuration(1);
		activity.setLinkId(stopFacility.getLinkId());
		return activity;
	}

	private void printWarning(Supplier<String> supplier) {
		if (drtCfg.isPrintDetailedWarnings()) {
			logger.warn(supplier.get());
		}
	}

	static Coord getFacilityCoord(Facility facility, Network network) {
		Coord coord = facility.getCoord();
		if (coord == null) {
			coord = network.getLinks().get(facility.getLinkId()).getCoord();
			if (coord == null)
				throw new RuntimeException("From facility has neither coordinates nor link Id. Should not happen.");
		}
		return coord;
	}

}
