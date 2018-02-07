package com.orange.reconfig;

import java.util.Map.Entry;

import com.orange.model.OperationConfig;
import com.orange.model.PaaSSiteAccess;
import com.orange.model.architecture.Architecture;
import com.orange.model.architecture.Microservice;
import com.orange.model.workflow.ParallelWorkflow;
import com.orange.model.workflow.SerialWorkflow;
import com.orange.model.workflow.Workflow;
import com.orange.paas.PaaSAPI;
import com.orange.paas.cf.CloudFoundryAPIv2;

public class WorkflowCalculator {
    private Architecture currentArchitecture;
    private Architecture desiredArchitecture;
    private OperationConfig config;

    public WorkflowCalculator(Architecture currentArchitecture, Architecture desiredArchitecture, OperationConfig config) {
	this.currentArchitecture = currentArchitecture;
	this.desiredArchitecture = desiredArchitecture;
	this.config = config;
    }

    public Workflow getReconfigureWorkflow() {
	Workflow reconfigure = new ParallelWorkflow("parallel update sites");
	for (PaaSSiteAccess site : desiredArchitecture.listPaaSSites()) {
	    Workflow reconfigSite = config.isParallelUpdateMicroservices()
		    ? new ParallelWorkflow(String.format("parallel update site %s entities", site.getName()))
		    : new SerialWorkflow(String.format("serial update site %s entities", site.getName()));
	    SiteComparator comparator = new SiteComparator(currentArchitecture.getArchitectureSite(site.getName()),
		    desiredArchitecture.getArchitectureSite(site.getName()));
	    PaaSAPI directory = new CloudFoundryAPIv2(site, config);
	    for (Microservice addedMicroservice : comparator.getAddedMicroservices()) {
		reconfigSite.addStep(directory.add(addedMicroservice));
	    }
	    for (Microservice removedMicroservice : comparator.getRemovedMicroservices()) {
		reconfigSite.addStep(directory.remove(removedMicroservice));
	    }
	    for (Entry<Microservice, Microservice> modifiedMicroservice : comparator
		    .getModifiedMicroservice().entrySet()) {
		reconfigSite.addStep(directory.modify(modifiedMicroservice.getKey(), modifiedMicroservice.getValue()));
	    }
	    reconfigure.addStep(reconfigSite);
	}
	return reconfigure;
    }
}
