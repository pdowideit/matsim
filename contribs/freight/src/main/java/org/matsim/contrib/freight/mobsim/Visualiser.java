package org.matsim.contrib.freight.mobsim;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.contrib.freight.CarrierConfigGroup;
import org.matsim.contrib.freight.FreightConfigGroup;
import org.matsim.contrib.freight.carrier.Carrier;
import org.matsim.contrib.freight.carrier.Carriers;
import org.matsim.contrib.freight.mobsim.CarrierAgentTracker;
import org.matsim.contrib.freight.mobsim.FreightQSimFactory;
import org.matsim.contrib.freight.scoring.CarrierScoringFunctionFactory;
import org.matsim.contrib.otfvis.OTFVis;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.config.groups.QSimConfigGroup;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.algorithms.SnapshotGenerator;
import org.matsim.core.mobsim.framework.Mobsim;
import org.matsim.core.mobsim.qsim.QSim;
import org.matsim.core.scoring.ScoringFunction;
import org.matsim.vis.otfvis.OTFClientLive;
import org.matsim.vis.otfvis.OTFFileWriter;
import org.matsim.vis.otfvis.OnTheFlyServer;

public class Visualiser {
	
	private Config config;

	private Scenario scenario;

	public Visualiser(Config config, Scenario scenario){
		this.config = config;
		this.scenario = scenario;
	}
	
	public void visualizeLive(Carriers carriers){
		sim(carriers);
	}
	
	public void makeMVI(Carriers carriers, String outfile, double snapshotInterval){
		OTFFileWriter otfFileWriter = new OTFFileWriter(scenario, outfile);
		
		EventsManager events = EventsUtils.createEventsManager();

		CarrierAgentTracker carrierAgentTracker = new CarrierAgentTracker(carriers, scenario.getNetwork(), new CarrierScoringFunctionFactory() {
			
			@Override
			public ScoringFunction createScoringFunction(Carrier carrier) {
				return getNoScoring();
			}
			
		});

		FreightConfigGroup freightConfig = new FreightConfigGroup();
		freightConfig.setPhysicallyEnforceTimeWindowBeginnings(true);

		FreightQSimFactory mobsimFactory = new FreightQSimFactory(scenario, events, carrierAgentTracker);

		Mobsim mobsim = mobsimFactory.get();
		
		SnapshotGenerator visualizer = new SnapshotGenerator(scenario.getNetwork(), snapshotInterval, scenario.getConfig().qsim());
		visualizer.addSnapshotWriter(otfFileWriter);
		events.addHandler(visualizer);
		
		mobsim.run();
		
		visualizer.finish();
		otfFileWriter.finish();
	}
	
	private void sim(Carriers carriers) {
		EventsManager events = EventsUtils.createEventsManager();

		CarrierAgentTracker carrierAgentTracker = new CarrierAgentTracker(carriers, scenario.getNetwork(), new CarrierScoringFunctionFactory() {
			
			@Override
			public ScoringFunction createScoringFunction(Carrier carrier) {
				return getNoScoring();
			}
			
		});

		FreightConfigGroup freightConfig = new FreightConfigGroup();
		freightConfig.setPhysicallyEnforceTimeWindowBeginnings(true);

		FreightQSimFactory mobsimFactory = new FreightQSimFactory(scenario, events, carrierAgentTracker);

		config.qsim().setSnapshotStyle(QSimConfigGroup.SnapshotStyle.queue);
		Mobsim mobsim = mobsimFactory.get();

		OnTheFlyServer server = OTFVis.startServerAndRegisterWithQSim(config, scenario, events, (QSim) mobsim);
		OTFClientLive.run(config, server);

		mobsim.run();
	}
	
	private static ScoringFunction getNoScoring() {
		
		return new ScoringFunction(){

			@Override
			public void handleActivity(Activity activity) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void handleLeg(Leg leg) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void agentStuck(double time) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void addMoney(double amount) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void finish() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public double getScore() {
				// TODO Auto-generated method stub
				return 0;
			}

		

			@Override
			public void handleEvent(Event event) {
				// TODO Auto-generated method stub
				
			}
			
		};
	}

}
