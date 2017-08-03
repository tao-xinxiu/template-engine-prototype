package com.orange.midstate.strategy;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orange.model.StrategySiteConfig;
import com.orange.model.StrategyConfig;
import com.orange.model.state.Overview;
import com.orange.model.state.OverviewApp;
import com.orange.util.SetUtil;
import com.orange.util.VersionGenerator;

public class BlueGreenStrategy extends BlueGreenPkgUpdateStrategy {
    private static final Logger logger = LoggerFactory.getLogger(BlueGreenStrategy.class);

    public BlueGreenStrategy(StrategyConfig config) {
	super(config);
    }

    @Override
    public List<TransitPoint> transitPoints() {
	return Arrays.asList(newPkgEnvTransit, updateExceptPkgEnvRouteTransit, updateRouteTransit,
		library.removeUndesiredTransit);
    }

    protected TransitPoint newPkgEnvTransit = new TransitPoint() {
	@Override
	public boolean condition(Overview currentState, Overview finalState) {
	    for (String site : finalState.listSitesName()) {
		for (OverviewApp desiredApp : finalState.getOverviewSite(site).getOverviewApps()) {
		    if (SetUtil.noneMatch(currentState.getOverviewSite(site).getOverviewApps(),
			    app -> app.getName().equals(desiredApp.getName())
				    && app.getPath().equals(desiredApp.getPath())
				    && app.getEnv().equals(desiredApp.getEnv()))) {
			logger.info("newPkgEnvTransit detected for microservice {}", desiredApp);
			return true;
		    }
		}
	    }
	    return false;
	}

	@Override
	public Overview next(Overview currentState, Overview finalState) {
	    logger.info("start getting next architecture by adding microservice with new pkg and env");
	    Overview nextState = new Overview(currentState);
	    for (String site : finalState.listSitesName()) {
		Set<OverviewApp> currentApps = nextState.getOverviewSite(site).getOverviewApps();
		Set<String> usedVersions = currentApps.stream().map(app -> app.getInstanceVersion())
			.collect(Collectors.toSet());
		StrategySiteConfig siteConfig = (StrategySiteConfig) (config.getSiteConfig(site));
		for (OverviewApp desiredApp : finalState.getOverviewSite(site).getOverviewApps()) {
		    if (SetUtil.noneMatch(currentApps,
			    app -> app.getName().equals(desiredApp.getName())
				    && app.getPath().equals(desiredApp.getPath())
				    && app.getEnv().equals(desiredApp.getEnv()))) {
			OverviewApp newApp = new OverviewApp(desiredApp);
			newApp.setGuid(null);
			newApp.setRoutes(Collections.singleton(siteConfig.getTmpRoute(desiredApp.getName())));
			newApp.setInstanceVersion(VersionGenerator.random(usedVersions));
			usedVersions.add(newApp.getInstanceVersion());
			nextState.getOverviewSite(site).addOverviewApp(newApp);
			logger.info("Added a new microservice: {} ", newApp);
			continue;
		    }
		}
	    }
	    return nextState;
	}
    };

    protected TransitPoint updateExceptPkgEnvRouteTransit = new TransitPoint() {
	@Override
	public boolean condition(Overview currentState, Overview finalState) {
	    for (String site : finalState.listSitesName()) {
		for (OverviewApp desiredApp : finalState.getOverviewSite(site).getOverviewApps()) {
		    if (SetUtil.noneMatch(currentState.getOverviewSite(site).getOverviewApps(),
			    app -> app.getName().equals(desiredApp.getName())
				    && app.getPath().equals(desiredApp.getPath())
				    && app.getEnv().equals(desiredApp.getEnv())
				    && app.getNbProcesses() == desiredApp.getNbProcesses()
				    && app.getServices().equals(desiredApp.getServices())
				    && app.getState().equals(desiredApp.getState()))) {
			logger.info("updateExceptPkgEnvRouteTransit detected");
			return true;
		    }
		}
	    }
	    return false;
	}

	// assume that it doesn't exist two apps with same pkg and name
	@Override
	public Overview next(Overview currentState, Overview finalState) {
	    logger.info("start getting next architecture by updating desired microservice properties (except route)");
	    Overview nextState = new Overview(currentState);
	    for (String site : finalState.listSitesName()) {
		for (OverviewApp desiredApp : finalState.getOverviewSite(site).getOverviewApps()) {
		    if (SetUtil.noneMatch(nextState.getOverviewSite(site).getOverviewApps(),
			    app -> app.isInstantiation(desiredApp))) {
			OverviewApp nextApp = SetUtil.getOneApp(nextState.getOverviewSite(site).getOverviewApps(),
				app -> app.getName().equals(desiredApp.getName())
					&& app.getPath().equals(desiredApp.getPath())
					&& app.getEnv().equals(desiredApp.getEnv()));
			nextApp.setNbProcesses(desiredApp.getNbProcesses());
			nextApp.setServices(desiredApp.getServices());
			nextApp.setState(desiredApp.getState());
			logger.info("Updated microservice [{}_{}] to {} ", nextApp.getName(),
				nextApp.getInstanceVersion(), nextApp);
		    }
		}
	    }
	    return nextState;
	}
    };

    protected TransitPoint updateRouteTransit = new TransitPoint() {
	@Override
	public boolean condition(Overview currentState, Overview finalState) {
	    for (String site : finalState.listSitesName()) {
		for (OverviewApp desiredApp : finalState.getOverviewSite(site).getOverviewApps()) {
		    if (SetUtil.noneMatch(currentState.getOverviewSite(site).getOverviewApps(),
			    app -> app.isInstantiation(desiredApp))) {
			logger.info("updateRouteTransit detected");
			return true;
		    }
		}
	    }
	    return false;
	}

	// assume that it doesn't exist two apps with same pkg and name
	@Override
	public Overview next(Overview currentState, Overview finalState) {
	    logger.info("start getting next architecture by updating desired microservice route");
	    Overview nextState = new Overview(currentState);
	    for (String site : finalState.listSitesName()) {
		for (OverviewApp desiredApp : finalState.getOverviewSite(site).getOverviewApps()) {
		    if (SetUtil.noneMatch(nextState.getOverviewSite(site).getOverviewApps(),
			    app -> app.isInstantiation(desiredApp))) {
			OverviewApp nextApp = SetUtil.getOneApp(nextState.getOverviewSite(site).getOverviewApps(),
				app -> app.getName().equals(desiredApp.getName())
					&& app.getPath().equals(desiredApp.getPath())
					&& app.getEnv().equals(desiredApp.getEnv())
					&& app.getNbProcesses() == desiredApp.getNbProcesses()
					&& app.getServices().equals(desiredApp.getServices())
					&& app.getState().equals(desiredApp.getState()));
			nextApp.setRoutes(desiredApp.getRoutes());
			logger.info("Updated microservice [{}_{}] route to {} ", nextApp.getName(),
				nextApp.getInstanceVersion(), nextApp.getRoutes());
		    }
		}
	    }
	    return nextState;
	}
    };
}
