/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008 by the members listed in the COPYING,        *
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

package org.matsim.core.replanning.strategies;

import org.matsim.core.config.groups.ChangeModeConfigGroup;
import org.matsim.core.config.groups.ControlerConfigGroup;
import org.matsim.core.config.groups.GlobalConfigGroup;
import org.matsim.core.config.groups.ControlerConfigGroup.PlanCheckerLevel;
import org.matsim.core.replanning.PlanStrategy;
import org.matsim.core.replanning.PlanStrategyImpl;
import org.matsim.core.replanning.modules.ChangeSingleLegMode;
import org.matsim.core.replanning.modules.PlanChecker;
import org.matsim.core.replanning.modules.ReRoute;
import org.matsim.core.replanning.modules.TripsToLegsModule;
import org.matsim.core.replanning.selectors.RandomPlanSelector;
import org.matsim.core.router.TripRouter;
import org.matsim.facilities.ActivityFacilities;

import javax.inject.Inject;
import javax.inject.Provider;

public class ChangeSingleTripMode implements Provider<PlanStrategy> {

	private final ControlerConfigGroup controlerConfigGroup;
	private final GlobalConfigGroup globalConfigGroup;
	private final ChangeModeConfigGroup changeLegModeConfigGroup;
	private Provider<TripRouter> tripRouterProvider;
	private ActivityFacilities activityFacilities;

	@Inject
	ChangeSingleTripMode(ControlerConfigGroup controlerConfigGroup, GlobalConfigGroup globalConfigGroup,
			ChangeModeConfigGroup changeLegModeConfigGroup, ActivityFacilities activityFacilities,
			Provider<TripRouter> tripRouterProvider) {
		this.controlerConfigGroup = controlerConfigGroup;
		this.globalConfigGroup = globalConfigGroup;
		this.changeLegModeConfigGroup = changeLegModeConfigGroup;
		this.activityFacilities = activityFacilities;
		this.tripRouterProvider = tripRouterProvider;
	}

    @Override
	public PlanStrategy get() {
		PlanStrategyImpl.Builder builder = new PlanStrategyImpl.Builder(new RandomPlanSelector<>());
		if (controlerConfigGroup.getPlanCheckerLevel().equals(PlanCheckerLevel.AFTER_EACH_MODIFICATION)) {
			builder.addStrategyModule(new PlanChecker(globalConfigGroup, "Strategy ChangeSingleTripMode after Step PlanSelector"));
		}
		builder.addStrategyModule(new TripsToLegsModule(tripRouterProvider, globalConfigGroup));
		if (controlerConfigGroup.getPlanCheckerLevel().equals(PlanCheckerLevel.AFTER_EACH_MODIFICATION)) {
			builder.addStrategyModule(new PlanChecker(globalConfigGroup, "Strategy ChangeSingleTripMode after Step TripsToLegsModule"));
		}
		builder.addStrategyModule(new ChangeSingleLegMode(globalConfigGroup, changeLegModeConfigGroup));
		if (controlerConfigGroup.getPlanCheckerLevel().equals(PlanCheckerLevel.AFTER_EACH_MODIFICATION)) {
			builder.addStrategyModule(new PlanChecker(globalConfigGroup, "Strategy ChangeSingleTripMode after Step ChangeSingleLegMode"));
		}
		builder.addStrategyModule(new ReRoute(activityFacilities, tripRouterProvider, globalConfigGroup));
		if (controlerConfigGroup.getPlanCheckerLevel().equals(PlanCheckerLevel.AFTER_EACH_MODIFICATION) ||
				controlerConfigGroup.getPlanCheckerLevel().equals(PlanCheckerLevel.MEDIUM)) {
			builder.addStrategyModule(new PlanChecker(globalConfigGroup, "Strategy ChangeSingleTripMode after Step ReRoute"));
		}
		return builder.build();
	}

}
