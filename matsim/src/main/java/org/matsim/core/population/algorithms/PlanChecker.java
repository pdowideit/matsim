/* *********************************************************************** *
 * project: org.matsim.*
 * PlanChecker.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2019 by the members listed in the COPYING,        *
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

package org.matsim.core.population.algorithms;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;

/**
 * Checks whether if the plan consists only of a sequence of activities and
 * legs, starting and ending with an activity and with no two activities or two
 * legs directly one after another.
 * 
 * @author gleich
 *
 */
public class PlanChecker implements PlanAlgorithm {

	private static final Logger log = Logger.getLogger(PlanChecker.class);

	private enum PlanElementType {
		Activity, Leg
	}

	private final String description;

	public PlanChecker(final String description) {
		this.description = description;
	}

	@Override
	public void run(Plan plan) {
		PlanElementType lastPlanElementType;
		
		if (plan.getPlanElements().size() < 1) {
			// TODO: Is a plan with 0 or 1 plan elements valid?
			log.error(
					"Invalid plan: 0 PlanElements!\n"
							+ createDetailedErrorMessage(plan));
			throw new RuntimeException("Invalid Plan");
		}
		
		// Check first PlanElement is an Activity
		if (plan.getPlanElements().get(0) instanceof Activity) {
			// ok
			lastPlanElementType = PlanElementType.Activity;
		} else {
			log.error(
					"Invalid plan: Found first PlanElement which is not an Activity!\n"
							+ createDetailedErrorMessage(plan));
			throw new RuntimeException("Invalid Plan");
		}
		PlanElement pe;

		if (plan.getPlanElements().size() == 1) {
			// there is nothing else we could check
			return;
		}
		
		// Check that after a Leg only an Activity can follow and after an Activity only a Leg can follow 
		for (int i = 1; i < plan.getPlanElements().size(); i++) {
			pe = plan.getPlanElements().get(i);
			if (pe instanceof Activity) {
				if (lastPlanElementType.equals(PlanElementType.Leg)) {
					// ok
					lastPlanElementType = PlanElementType.Activity;
				} else {
					log.error(
							"Invalid plan: Found Activity after a PlanElement which is not a Leg!\n"
									+ createDetailedErrorMessage(plan));
					throw new RuntimeException("Invalid Plan");
				}
			} else if (pe instanceof Leg) {
				if (lastPlanElementType.equals(PlanElementType.Activity)) {
					// ok
					lastPlanElementType = PlanElementType.Leg;
				} else {
					log.error("Invalid plan: Found Leg after a PlanElement which is not an Activity!\n"
							+ createDetailedErrorMessage(plan));
					throw new RuntimeException("Invalid Plan");
				}
			} else {
				log.error("Unknown PlanElement type: " + pe.getClass().getName() + "\n" + createDetailedErrorMessage(plan));
				throw new RuntimeException("Unknown PlanElement type");
			}
		}
		// Check last PlanElement is an Activity
		if (!lastPlanElementType.equals(PlanElementType.Activity)) {
			log.error("Invalid plan: Last PlanElement is not an Activity!\n"
					+ createDetailedErrorMessage(plan));
			throw new RuntimeException("Invalid Plan");
		}
	}

	private String createDetailedErrorMessage(Plan plan) {
		StringBuilder strBuild = new StringBuilder();
		strBuild.append("PlanChecker description: ");
		strBuild.append(description);
		strBuild.append("\nInvalid plan: ");
		strBuild.append(plan.toString());
		strBuild.append("\nPlanElements: \n\t");
		for (PlanElement pe : plan.getPlanElements()) {
			strBuild.append(pe.toString());
			strBuild.append("\n\t");
		}
		return strBuild.toString();
	}
}
