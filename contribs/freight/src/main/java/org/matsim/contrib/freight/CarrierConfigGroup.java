/*
 *  *********************************************************************** *
 *  * project: org.matsim.*
 *  * CarrierConfig.java
 *  *                                                                         *
 *  * *********************************************************************** *
 *  *                                                                         *
 *  * copyright       : (C) 2015 by the members listed in the COPYING, *
 *  *                   LICENSE and WARRANTY file.                            *
 *  * email           : info at matsim dot org                                *
 *  *                                                                         *
 *  * *********************************************************************** *
 *  *                                                                         *
 *  *   This program is free software; you can redistribute it and/or modify  *
 *  *   it under the terms of the GNU General Public License as published by  *
 *  *   the Free Software Foundation; either version 2 of the License, or     *
 *  *   (at your option) any later version.                                   *
 *  *   See also COPYING, LICENSE and WARRANTY file                           *
 *  *                                                                         *
 *  * ***********************************************************************
 */

package org.matsim.contrib.freight;

import org.matsim.core.config.ReflectiveConfigGroup;

public class CarrierConfigGroup extends ReflectiveConfigGroup {

    public static final String GROUPNAME="carrier" ;

    private boolean physicallyEnforceTimeWindowBeginnings = true ;

    @Deprecated
//   please use FreightConfigGroup
    public CarrierConfigGroup(  ){
        super( GROUPNAME );
    }

    public boolean getPhysicallyEnforceTimeWindowBeginnings() {
        return physicallyEnforceTimeWindowBeginnings;
    }

    /**
     * please use FreightConfigGroup
     *
     * Physically enforces beginnings of time windows for freight activities, i.e. freight agents
     * wait before closed doors until they can deliver / pick up their goods, and then take their required duration.
     *
     * <p>The default value is false. Time windows will be ignored by the physical simulation, leaving treatment
     * of early arrival to the Scoring.
     *
     * @see org.matsim.contrib.freight.mobsim.WithinDayActivityReScheduling
     */
    @Deprecated
    public void setPhysicallyEnforceTimeWindowBeginnings(boolean physicallyEnforceTimeWindowBeginnings) {
        this.physicallyEnforceTimeWindowBeginnings = physicallyEnforceTimeWindowBeginnings;
    }

}
