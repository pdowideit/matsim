/* *********************************************************************** *
 * project: org.matsim.*
 * TripStructureUtilsSubtoursTest.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2013 by the members listed in the COPYING,        *
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
package org.matsim.core.router;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.*;
import org.junit.Test;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.PopulationFactoryImpl;
import org.matsim.core.router.TripStructureUtils.Trip;
import org.matsim.core.router.TripStructureUtils.Subtour;
import org.matsim.core.scenario.ScenarioUtils;

/**
 * @author thibautd
 */
public class TripStructureUtilsSubtoursTest {
	private static final String STAGE = "stage_activity";
	private static final StageActivityTypes CHECKER = new StageActivityTypesImpl( Arrays.asList( STAGE ) );

	// /////////////////////////////////////////////////////////////////////////
	// fixtures
	// /////////////////////////////////////////////////////////////////////////
	private static class Fixture {
		private final Plan plan;
		private final List<Subtour> subtoursIfLink;
		// TODO: facilities

		public Fixture(
				final Plan plan,
				final List<Subtour> subtoursIfLink) {
			this.plan = plan;
			this.subtoursIfLink = subtoursIfLink;
		}
	}

	private static Fixture createMonoSubtourFixture() {
		final PopulationFactory fact = createPopulationFactory();

		final Id id1 = new IdImpl( 1 );
		final Id id2 = new IdImpl( 2 );

		final Plan plan = fact.createPlan();
		final Activity act1 = fact.createActivityFromLinkId( "h" , id1 );
		plan.addActivity( act1 );

		final List<PlanElement> trip1 = new ArrayList<PlanElement>();
		final Leg leg1 = fact.createLeg( "velo" );
		plan.addLeg( leg1 );
		trip1.add( leg1 );

		final Activity act2 = fact.createActivityFromLinkId( "w" , id2 );
		plan.addActivity( act2 );

		final Activity act2b = fact.createActivityFromLinkId( "w" , id2 );
		plan.addActivity( act2b );

		final List<PlanElement> trip2 = new ArrayList<PlanElement>();
		final Leg leg2 = fact.createLeg( "walk" );
		plan.addLeg( leg2 );
		trip2.add( leg2 );
		final Activity stage = fact.createActivityFromLinkId( STAGE , id2 );
		plan.addActivity( stage );
		trip2.add( stage );
		final Leg leg3 = fact.createLeg( "swim" );
		plan.addLeg( leg3 );
		trip2.add( leg3 );
		final Leg leg4 = fact.createLeg( "walk" );
		plan.addLeg( leg4 );
		trip2.add( leg4 );

		final Activity act3 = fact.createActivityFromLinkId( "h" , id1 );
		plan.addActivity( act3 );

		return new Fixture(
				plan,
				Arrays.asList(
					new Subtour(
						Arrays.asList(
							new Trip( act1 , trip1 , act2 ),
							new Trip( act2b , trip2 , act3 ) ),
						true) ) );
	}

	private static Fixture createTwoNestedSubtours() {
		final PopulationFactory fact = createPopulationFactory();

		final Id id1 = new IdImpl( 1 );
		final Id id2 = new IdImpl( 2 );

		final Plan plan = fact.createPlan();
		final Activity act1 = fact.createActivityFromLinkId( "h" , id1 );
		plan.addActivity( act1 );

		final List<PlanElement> trip1 = new ArrayList<PlanElement>();
		final Leg leg1 = fact.createLeg( "velo" );
		plan.addLeg( leg1 );
		trip1.add( leg1 );

		final Activity act2 = fact.createActivityFromLinkId( "w" , id2 );
		plan.addActivity( act2 );

		final List<PlanElement> trip2 = new ArrayList<PlanElement>();
		final Leg leg2 = fact.createLeg( "walk" );
		plan.addLeg( leg2 );
		trip2.add( leg2 );
		final Activity stage = fact.createActivityFromLinkId( STAGE , id2 );
		plan.addActivity( stage );
		trip2.add( stage );
		final Leg leg3 = fact.createLeg( "swim" );
		plan.addLeg( leg3 );
		trip2.add( leg3 );
		final Leg leg4 = fact.createLeg( "walk" );
		plan.addLeg( leg4 );
		trip2.add( leg4 );

		final Activity act3 = fact.createActivityFromLinkId( "h" , id2 );
		plan.addActivity( act3 );

		final List<PlanElement> trip3 = new ArrayList<PlanElement>();
		final Leg leg5 = fact.createLeg( "velo" );
		plan.addLeg( leg5 );
		trip3.add( leg5 );

		final Activity act4 = fact.createActivityFromLinkId( "h" , id1 );
		plan.addActivity( act4 );

		final Subtour rootSubtour =
			new Subtour(
						Arrays.asList(
							new Trip( act1 , trip1 , act2 ),
							new Trip( act3 , trip3 , act4 ) ),
						true);
		final Subtour childSubtour =
			new Subtour(
						Arrays.asList(
							new Trip( act2 , trip2 , act3 ) ),
						true);
		childSubtour.parent = rootSubtour;

		return new Fixture(
				plan,
				Arrays.asList(
					rootSubtour,
					childSubtour));
	}

	private static Fixture createComplexSubtours() {
		final PopulationFactory fact = createPopulationFactory();

		final Id id1 = new IdImpl( 1 );
		final Id id2 = new IdImpl( 2 );
		final Id id3 = new IdImpl( 3 );

		final Plan plan = fact.createPlan();
		final Activity act1 = fact.createActivityFromLinkId( "h" , id1 );
		plan.addActivity( act1 );

		final List<PlanElement> trip1 = new ArrayList<PlanElement>();
		final Leg leg1 = fact.createLeg( "velo" );
		plan.addLeg( leg1 );
		trip1.add( leg1 );

		final Activity act2 = fact.createActivityFromLinkId( "w" , id2 );
		plan.addActivity( act2 );

		final List<PlanElement> trip2 = new ArrayList<PlanElement>();
		final Leg leg2 = fact.createLeg( "walk" );
		plan.addLeg( leg2 );
		trip2.add( leg2 );
		final Activity stage = fact.createActivityFromLinkId( STAGE , id2 );
		plan.addActivity( stage );
		trip2.add( stage );
		final Leg leg3 = fact.createLeg( "swim" );
		plan.addLeg( leg3 );
		trip2.add( leg3 );
		final Leg leg4 = fact.createLeg( "walk" );
		plan.addLeg( leg4 );
		trip2.add( leg4 );

		final Activity act3 = fact.createActivityFromLinkId( "s" , id3 );
		plan.addActivity( act3 );

		final List<PlanElement> trip3 = new ArrayList<PlanElement>();
		final Leg leg5 = fact.createLeg( "velo" );
		plan.addLeg( leg5 );
		trip3.add( leg5 );

		final Activity act4 = fact.createActivityFromLinkId( "t" , id1 );
		plan.addActivity( act4 );

		final List<PlanElement> trip4 = new ArrayList<PlanElement>();
		final Leg leg6 = fact.createLeg( "skateboard" );
		plan.addLeg( leg6 );
		trip4.add( leg6 );

		final Activity act5 = fact.createActivityFromLinkId( "aa" , id3 );
		plan.addActivity( act5 );
	
		final List<PlanElement> trip5 = new ArrayList<PlanElement>();
		final Leg leg7 = fact.createLeg( "skateboard" );
		plan.addLeg( leg7 );
		trip5.add( leg7 );

		final Activity act6 = fact.createActivityFromLinkId( "l" , id2 );
		plan.addActivity( act6 );

		final List<PlanElement> trip6 = new ArrayList<PlanElement>();
		final Leg leg8 = fact.createLeg( "skateboard" );
		plan.addLeg( leg8 );
		trip6.add( leg8 );

		final Activity act7 = fact.createActivityFromLinkId( "s" , id3 );
		plan.addActivity( act7 );

		final List<PlanElement> trip7 = new ArrayList<PlanElement>();
		final Leg leg9 = fact.createLeg( "velo" );
		plan.addLeg( leg9 );
		trip7.add( leg9 );

		final Activity act8 = fact.createActivityFromLinkId( "h" , id1 );
		plan.addActivity( act8 );


		final Subtour rootSubtour1 =
			new Subtour(
						Arrays.asList(
							new Trip( act1 , trip1 , act2 ),
							new Trip( act2 , trip2 , act3 ),
							new Trip( act3 , trip3 , act4 ) ),
						true);
		final Subtour rootSubtour2 =
			new Subtour(
						Arrays.asList(
							new Trip( act4 , trip4 , act5 ),
							new Trip( act7 , trip7 , act8 )),
						true);

		final Subtour childSubtour =
			new Subtour(
						Arrays.asList(
							new Trip( act5 , trip5 , act6 ),
							new Trip( act6 , trip6 , act7 )),
						true);
		childSubtour.parent = rootSubtour2;

		return new Fixture(
				plan,
				Arrays.asList(
					rootSubtour1,
					rootSubtour2,
					childSubtour));
	}

	private static Fixture createOpenPlan() {
		final PopulationFactory fact = createPopulationFactory();

		final Id id1 = new IdImpl( 1 );
		final Id id2 = new IdImpl( 2 );
		final Id id3 = new IdImpl( 3 );

		final Plan plan = fact.createPlan();
		final Activity act1 = fact.createActivityFromLinkId( "h" , id1 );
		plan.addActivity( act1 );

		final List<PlanElement> trip1 = new ArrayList<PlanElement>();
		final Leg leg1 = fact.createLeg( "velo" );
		plan.addLeg( leg1 );
		trip1.add( leg1 );

		final Activity act2 = fact.createActivityFromLinkId( "w" , id2 );
		plan.addActivity( act2 );

		final List<PlanElement> trip2 = new ArrayList<PlanElement>();
		final Leg leg2 = fact.createLeg( "walk" );
		plan.addLeg( leg2 );
		trip2.add( leg2 );
		final Activity stage = fact.createActivityFromLinkId( STAGE , id2 );
		plan.addActivity( stage );
		trip2.add( stage );
		final Leg leg3 = fact.createLeg( "swim" );
		plan.addLeg( leg3 );
		trip2.add( leg3 );
		final Leg leg4 = fact.createLeg( "walk" );
		plan.addLeg( leg4 );
		trip2.add( leg4 );

		final Activity act3 = fact.createActivityFromLinkId( "h" , id2 );
		plan.addActivity( act3 );

		final List<PlanElement> trip3 = new ArrayList<PlanElement>();
		final Leg leg5 = fact.createLeg( "velo" );
		plan.addLeg( leg5 );
		trip3.add( leg5 );

		final Activity act4 = fact.createActivityFromLinkId( "camping" , id3 );
		plan.addActivity( act4 );

		final Subtour rootSubtour =
			new Subtour(
						Arrays.asList(
							new Trip( act1 , trip1 , act2 ),
							new Trip( act3 , trip3 , act4 ) ),
						false);
		final Subtour childSubtour =
			new Subtour(
						Arrays.asList(
							new Trip( act2 , trip2 , act3 ) ),
						true);
		childSubtour.parent = rootSubtour;

		return new Fixture(
				plan,
				Arrays.asList(
					rootSubtour,
					childSubtour));
	}

	// /////////////////////////////////////////////////////////////////////////
	// tests
	// /////////////////////////////////////////////////////////////////////////
	@Test
	public void testOneSubtour() {
		performTest( createMonoSubtourFixture() );
	}

	@Test
	public void testTwoNestedSubtours() {
		performTest( createTwoNestedSubtours() );
	}

	@Test
	public void testComplexSubtours() {
		performTest( createComplexSubtours() );
	}

	@Test
	public void testOpenPlan() throws Exception {
		performTest( createOpenPlan() );
	}

	private static void performTest(final Fixture fixture) {
		final Collection<Subtour> subtours =
			TripStructureUtils.getSubtours(
					fixture.plan,
					CHECKER );

		assertEquals(
				"unexpected number of subtours in "+subtours,
				fixture.subtoursIfLink.size(),
				subtours.size() );

		assertEquals(
				"uncompatible subtours",
				// do not bother about iteration order,
				// but ensure you get some information on failure
				new HashSet<Subtour>( fixture.subtoursIfLink ),
				new HashSet<Subtour>( subtours ) );
	}

	private static PopulationFactory createPopulationFactory() {
		return new PopulationFactoryImpl( ScenarioUtils.createScenario( ConfigUtils.createConfig() ) );
	}
}

