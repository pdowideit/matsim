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
 * Checks whether if the plan consists only of a sequence of activities and legs,
 * starting and ending with an activity and with no two activities or two legs directly
 * one after another.
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
		PlanElementType lastPlanElementType = null;
		for (PlanElement pe: plan.getPlanElements()) {
			if (pe instanceof Activity) {
				if (lastPlanElementType == null || lastPlanElementType.equals(PlanElementType.Leg)) {
					// ok
					lastPlanElementType = PlanElementType.Activity;
				} else {
					log.error("Invalid plan: Found Activity which is neither the first PlanElement of the plan nor located after a Leg!\n"
					 + "PlanChecker description: " + description + "\n"
					 + "Invalid plan: " + plan.toString());
					throw new RuntimeException("Invalid Plan");
				}
			} else if (pe instanceof Leg) {
				if (lastPlanElementType.equals(PlanElementType.Leg)) {
					// ok
					lastPlanElementType = PlanElementType.Leg;
				} else {
					log.error("Invalid plan: Found Leg after a PlanElement which is not an Activity!\n"
					 + "PlanChecker description: " + description + "\n"
					 + "Invalid plan: " + plan.toString());
					throw new RuntimeException("Invalid Plan");
				}
			} else {
				log.error("Unknown PlanElement type: " + pe.getClass().getName() + "\n"
				 + "PlanChecker description: " + description + "\n"
				 + "Invalid plan: " + plan.toString());
				throw new RuntimeException("Unknown PlanElement type");
			}
		}
	}
}
