package com.orange.state.diff;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.orange.model.OverviewApp;
import com.orange.model.Route;

public class AppComparator {
	private OverviewApp currentApp;
	private OverviewApp desiredApp;
	private boolean nameUpdated;
	private boolean routesUpdated;
	private List<Route> addedRoutes = new ArrayList<>();
	private List<Route> removedRoutes = new ArrayList<>();
	private boolean stateUpdated;
	private boolean instancesUpdated;
	private boolean envUpdated; 

	public AppComparator(OverviewApp currentApp, OverviewApp desiredApp) {
		if (!currentApp.getGuid().equals(desiredApp.getGuid())) {
			throw new IllegalStateException(
					String.format("Illegal AppComparator with different guid in currentApp %s and desiredApp %s",
							currentApp, desiredApp));
		}
		this.currentApp = currentApp;
		this.desiredApp = desiredApp;
		if (!currentApp.getRoutes().equals(desiredApp.getRoutes())) {
			routesUpdated = true;
			addedRoutes = desiredApp.listRoutes().stream().filter(route -> !currentApp.listRoutes().contains(route))
					.collect(Collectors.toList());
			removedRoutes = currentApp.listRoutes().stream().filter(route -> !desiredApp.listRoutes().contains(route))
					.collect(Collectors.toList());
		}
		nameUpdated = !currentApp.getName().equals(desiredApp.getName());
		stateUpdated = !(currentApp.getState() == desiredApp.getState());
		instancesUpdated = !(currentApp.getInstances() == desiredApp.getInstances());
		envUpdated = !(currentApp.getEnv().equals(desiredApp.getEnv()));
	}

	public OverviewApp getCurrentApp() {
		return currentApp;
	}

	public OverviewApp getDesiredApp() {
		return desiredApp;
	}

	public boolean isNameUpdated() {
		return nameUpdated;
	}

	public boolean isRoutesUpdated() {
		return routesUpdated;
	}

	public List<Route> getAddedRoutes() {
		return addedRoutes;
	}

	public List<Route> getRemovedRoutes() {
		return removedRoutes;
	}

	public boolean isStateUpdated() {
		return stateUpdated;
	}

	public boolean isInstancesUpdated() {
		return instancesUpdated;
	}

	public boolean isEnvUpdated() {
		return envUpdated;
	}
}