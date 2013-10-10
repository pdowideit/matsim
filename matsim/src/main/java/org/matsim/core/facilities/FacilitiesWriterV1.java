/* *********************************************************************** *
 * project: org.matsim.*
 * FacilitiesWriterHandlerImplV1.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2007 by the members listed in the COPYING,        *
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

package org.matsim.core.facilities;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.SortedSet;

import org.matsim.core.api.experimental.facilities.ActivityFacilities;
import org.matsim.core.api.experimental.facilities.ActivityFacility;
import org.matsim.core.api.internal.MatsimWriter;
import org.matsim.core.utils.io.MatsimXmlWriter;
import org.matsim.core.utils.misc.FacilitiesUtils;
import org.matsim.core.utils.misc.Time;

/**
 * @author mrieser / Senozon AG
 */
/*package*/ class FacilitiesWriterV1 extends MatsimXmlWriter implements MatsimWriter {

	private final String DTD = "http://www.matsim.org/files/dtd/facilities_v1.dtd";
	
	private final ActivityFacilities facilities;
	
	public FacilitiesWriterV1(final ActivityFacilities facilities) {
		this.facilities = facilities;
	}
	
	@Override
	public void write(String filename) {
		this.writeOpenAndInit(filename);
		for (ActivityFacility f : FacilitiesUtils.getSortedFacilities(this.facilities).values()) {
			this.writeFacility((ActivityFacilityImpl) f);
		}
		this.writeFinish();
	}
	
	private final void writeOpenAndInit(final String filename) {
		try {
			openFile(filename);
			this.writeXmlHead();
			this.writeDoctype("facilities", DTD);
			this.startFacilities(this.facilities, this.writer);
			this.writeSeparator(this.writer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private final void writeFacility(final ActivityFacilityImpl f) {
		try {
			this.startFacility(f, this.writer);
			Iterator<ActivityOption> a_it = f.getActivityOptions().values().iterator();
			while (a_it.hasNext()) {
				ActivityOption a = a_it.next();
				this.startActivity((ActivityOptionImpl) a, this.writer);
				this.startCapacity((ActivityOptionImpl) a, this.writer);
				this.endCapacity(this.writer);
				SortedSet<OpeningTime> o_set = ((ActivityOptionImpl) a).getOpeningTimes();
				Iterator<OpeningTime> o_it = o_set.iterator();
				while (o_it.hasNext()) {
					OpeningTime o = o_it.next();
					this.startOpentime(o, this.writer);
					this.endOpentime(this.writer);
				}
				this.endActivity(this.writer);
			}
			this.endFacility(this.writer);
			this.writeSeparator(this.writer);
			this.writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private final void writeFinish() {
		try {
			this.endFacilities(this.writer);
			this.writer.flush();
			this.writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//////////////////////////////////////////////////////////////////////
	// <facilities ... > ... </facilities>
	//////////////////////////////////////////////////////////////////////

	public void startFacilities(final ActivityFacilities facilities, final BufferedWriter out) throws IOException {
		out.write("<facilities");
		if (((ActivityFacilitiesImpl) facilities).getName() != null) {
			out.write(" name=\"" + ((ActivityFacilitiesImpl) facilities).getName() + "\"");
		}
		out.write(">\n\n");
	}


	public void endFacilities(final BufferedWriter out) throws IOException {
		out.write("</facilities>\n");
	}

	//////////////////////////////////////////////////////////////////////
	// <facility ... > ... </facility>
	//////////////////////////////////////////////////////////////////////

	public void startFacility(final ActivityFacilityImpl facility, final BufferedWriter out) throws IOException {
		out.write("\t<facility");
		out.write(" id=\"" + facility.getId() + "\"");
		out.write(" x=\"" + facility.getCoord().getX() + "\"");
		out.write(" y=\"" + facility.getCoord().getY() + "\"");
		if (facility.getDesc() != null) { out.write(" desc=\"" + facility.getDesc() + "\""); }
		out.write(">\n");
	}

	public void endFacility(final BufferedWriter out) throws IOException {
		out.write("\t</facility>\n\n");
	}

	//////////////////////////////////////////////////////////////////////
	// <activity ... > ... </activity>
	//////////////////////////////////////////////////////////////////////

	public void startActivity(final ActivityOptionImpl activity, final BufferedWriter out) throws IOException {
		out.write("\t\t<activity");
		out.write(" type=\"" + activity.getType() + "\"");
		out.write(">\n");
	}

	public void endActivity(final BufferedWriter out) throws IOException {
		out.write("\t\t</activity>\n\n");
	}

	//////////////////////////////////////////////////////////////////////
	// <capacity ... />
	//////////////////////////////////////////////////////////////////////

	public void startCapacity(final ActivityOptionImpl activity, final BufferedWriter out) throws IOException {
		if (activity.getCapacity() != Integer.MAX_VALUE) {
			out.write("\t\t\t<capacity");
			out.write(" value=\"" + activity.getCapacity() + "\"");
			out.write(" />\n");
		}
	}

	public void endCapacity(final BufferedWriter out) throws IOException {
	}

	//////////////////////////////////////////////////////////////////////
	// <opentime ... />
	//////////////////////////////////////////////////////////////////////

	public void startOpentime(final OpeningTime opentime, final BufferedWriter out) throws IOException {
		out.write("\t\t\t<opentime");
		out.write(" day=\"wkday\"");
		out.write(" start_time=\"" + Time.writeTime(opentime.getStartTime()) + "\"");
		out.write(" end_time=\"" + Time.writeTime(opentime.getEndTime()) + "\"");
		out.write(" />\n");
	}

	public void endOpentime(final BufferedWriter out) throws IOException {
	}
	//////////////////////////////////////////////////////////////////////
	// <!-- ============ ... ========== -->
	//////////////////////////////////////////////////////////////////////

	public void writeSeparator(final BufferedWriter out) throws IOException {
		out.write("<!-- =================================================" +
							"===================== -->\n\n");
	}
}
