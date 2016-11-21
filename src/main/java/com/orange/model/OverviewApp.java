package com.orange.model;

import java.util.ArrayList;
import java.util.List;

public class OverviewApp {
	private String guid;
	private String name;
	private List<String> routes;
	private List<OverviewDroplet> droplets;

	public OverviewApp(String guid, String name, List<String> routes) {
		this.guid = guid;
		this.name = name;
		this.routes = routes;
		this.droplets = new ArrayList<>();
	}
	
	public OverviewApp(String guid, String name, List<String> routes, List<OverviewDroplet> droplets) {
		this.guid = guid;
		this.name = name;
		this.routes = routes;
		this.droplets = droplets;
	}

	public String getGuid() {
		return guid;
	}

	public String getName() {
		return name;
	}

	public List<String> getRoutes() {
		return routes;
	}

	public List<OverviewDroplet> getDroplets() {
		return droplets;
	}
	
	public void addOverviewDroplet(OverviewDroplet overviewDroplet) {
		this.droplets.add(overviewDroplet);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((droplets == null) ? 0 : droplets.hashCode());
		result = prime * result + ((guid == null) ? 0 : guid.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((routes == null) ? 0 : routes.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OverviewApp other = (OverviewApp) obj;
		if (droplets == null) {
			if (other.droplets != null)
				return false;
		} else if (!droplets.equals(other.droplets))
			return false;
		if (guid == null) {
			if (other.guid != null)
				return false;
		} else if (!guid.equals(other.guid))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (routes == null) {
			if (other.routes != null)
				return false;
		} else if (!routes.equals(other.routes))
			return false;
		return true;
	}
	
	
}