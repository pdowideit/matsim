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
package org.matsim.core.replanning.modules;

import org.matsim.core.config.groups.GlobalConfigGroup;
import org.matsim.core.population.algorithms.PlanAlgorithm;

/**
 * @author gleich
 *
 */
public class PlanChecker extends AbstractMultithreadedModule {
	
	private final String description;

	public PlanChecker(GlobalConfigGroup globalConfigGroup, String description) {
		super(globalConfigGroup);
		this.description = description;
	}

	@Override
	public PlanAlgorithm getPlanAlgoInstance() {
		return new org.matsim.core.population.algorithms.PlanChecker(description);
	}

}
