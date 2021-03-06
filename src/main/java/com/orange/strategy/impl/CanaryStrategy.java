package com.orange.strategy.impl;

import java.util.Arrays;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orange.model.StrategyConfig;
import com.orange.model.architecture.Architecture;
import com.orange.model.architecture.Microservice;
import com.orange.strategy.Strategy;
import com.orange.strategy.Transition;
import com.orange.util.SetUtil;

// Strategy assume route not updated between Ainit and Af
public class CanaryStrategy extends Strategy {
    private static final Logger logger = LoggerFactory.getLogger(CanaryStrategy.class);

    public CanaryStrategy(StrategyConfig config) {
	super(config);
	transitions = Arrays.asList(rolloutTransit, addCanaryTransit, updateExceptInstancesRoutesTransit,
		library.updateRouteTransit(Arrays.asList("guid", "nbProcesses")), scaleupTransit,
		library.removeUndesiredTransit);
    }

    @Override
    public boolean valid(Architecture currentArchitecture, Architecture finalArchitecture) {
	for (String site : finalArchitecture.listSitesName()) {
	    for (Microservice desiredMicroservice : finalArchitecture.getSiteMicroservices(site)) {
		Set<Microservice> currentMicroservices = SetUtil.searchByName(
			currentArchitecture.getSiteMicroservices(site), (String) desiredMicroservice.get("name"));
		if (!SetUtil.uniqueByNameVersion(currentMicroservices)) {
		    return false;
		}
	    }
	}
	return true;
    }

    /**
     * next architecture: add canary microservice with new pkg and env
     */
    protected Transition addCanaryTransit = new Transition() {
	@Override
	public Architecture next(Architecture currentArchitecture, Architecture finalArchitecture) {
	    Architecture nextArchitecture = new Architecture(currentArchitecture);
	    for (String site : finalArchitecture.listSitesName()) {
		Set<Microservice> currentMicroservices = nextArchitecture.getSiteMicroservices(site);
		for (Microservice desiredMs : finalArchitecture.getSiteMicroservices(site)) {
		    Microservice updatingMs = SetUtil.getUniqueMicroservice(currentMicroservices,
			    (String) desiredMs.get("name"), (String) desiredMs.get("version"));
		    if (updatingMs == null && SetUtil.noneMatch(currentMicroservices,
			    ms -> ms.eqAttr(Arrays.asList("name", "path", "env"), desiredMs))) {
			Microservice newMs = desiredMs.deepCopy();
			newMs.set("guid", null);
			newMs.set("routes", library.tmpRoute(site, desiredMs));
			newMs.set("nbProcesses", config.getCanaryNbr());
			nextArchitecture.getSite(site).addMicroservice(newMs);
			logger.info("Added a new microservice: {} ", newMs);
		    }
		}
	    }
	    return nextArchitecture;
	}
    };

    /**
     * next architecture: update desired microservice properties except nbProcesses
     * and routes
     */
    protected Transition updateExceptInstancesRoutesTransit = new Transition() {
	// assume that it doesn't exist two microservices with same pkg and name
	@Override
	public Architecture next(Architecture currentArchitecture, Architecture finalArchitecture) {
	    Architecture nextArchitecture = new Architecture(currentArchitecture);
	    for (String site : finalArchitecture.listSitesName()) {
		for (Microservice desiredMs : finalArchitecture.getSiteMicroservices(site)) {
		    Microservice nextMs = SetUtil.getUniqueMicroservice(nextArchitecture.getSiteMicroservices(site),
			    (String) desiredMs.get("name"), (String) desiredMs.get("version"));
		    if (nextMs != null
			    && !nextMs.eqAttrExcept(Arrays.asList("guid", "routes", "nbProcesses"), desiredMs)) {
			nextMs.copyAttrExcept(Arrays.asList("guid", "routes", "nbProcesses"), desiredMs);
			nextMs.set("routes", library.tmpRoute(site, desiredMs));
			logger.info("Updated microservice [{}_{}] to {} ", nextMs.get("name"), nextMs.get("version"),
				nextMs);
		    }
		}
	    }
	    return nextArchitecture;
	}
    };

    /**
     * next architecture: scale down non-desired microservice when the microservice
     * routed running instances equals to desired instances
     */
    protected Transition rolloutTransit = new Transition() {
	@Override
	public Architecture next(Architecture currentArchitecture, Architecture finalArchitecture) {
	    Architecture nextArchitecture = new Architecture(currentArchitecture);
	    for (String site : finalArchitecture.listSitesName()) {
		for (Microservice desiredMs : finalArchitecture.getSiteMicroservices(site)) {
		    Set<Microservice> nextMicroservices = nextArchitecture.getSiteMicroservices(site);
		    Set<Microservice> relatedMicroservices = SetUtil.search(nextMicroservices,
			    ms -> ms.eqAttr(Arrays.asList("name", "state", "routes"), desiredMs));
		    if (relatedMicroservices.stream().mapToInt(ms -> (int) ms.get("nbProcesses"))
			    .sum() == (int) desiredMs.get("nbProcesses")) {
			Microservice oldVersionMs = SetUtil.getOneMicroservice(relatedMicroservices,
				ms -> !ms.eqAttr("version", desiredMs));
			if (oldVersionMs == null) { // this ms not have oldVersion instances
			    continue;
			}
			boolean canaryNotCreated = SetUtil.noneMatch(relatedMicroservices,
				ms -> ms.eqAttr("version", desiredMs));
			int scaleDownNb = canaryNotCreated ? config.getCanaryNbr() : config.getCanaryIncrease();
			int nextNbr = (int) oldVersionMs.get("nbProcesses") - scaleDownNb;
			if (nextNbr >= 1) {
			    oldVersionMs.set("nbProcesses", nextNbr);
			    logger.info("rolled out microservice {}", oldVersionMs);
			} else {
			    nextMicroservices.remove(oldVersionMs);
			    logger.info("removed microservice {}", oldVersionMs);
			}
		    }
		}
	    }
	    return nextArchitecture;
	}
    };

    /**
     * get next architecture: scale up desired microservices
     */
    protected Transition scaleupTransit = new Transition() {
	@Override
	public Architecture next(Architecture currentArchitecture, Architecture finalArchitecture) {
	    Architecture nextArchitecture = new Architecture(currentArchitecture);
	    for (String site : finalArchitecture.listSitesName()) {
		for (Microservice desiredMicroservice : finalArchitecture.getSiteMicroservices(site)) {
		    Set<Microservice> nextMicroservices = nextArchitecture.getSiteMicroservices(site);
		    if (SetUtil.noneMatch(nextMicroservices, ms -> ms.isInstantiation(desiredMicroservice))) {
			Microservice nextMs = SetUtil.getUniqueMicroservice(nextMicroservices,
				ms -> ms.eqAttr(Arrays.asList("name", "version"), desiredMicroservice));
			int nextNbr = (int) nextMs.get("nbProcesses") + config.getCanaryIncrease();
			if (nextNbr > (int) desiredMicroservice.get("nbProcesses")) {
			    nextNbr = (int) desiredMicroservice.get("nbProcesses");
			}
			nextMs.set("nbProcesses", nextNbr);
			logger.info("scaled up microservice {}", nextMs);
		    }
		}
	    }
	    return nextArchitecture;
	}

    };
}
