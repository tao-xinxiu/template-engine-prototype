package com.orange.util;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.orange.model.state.AppState;
import com.orange.model.state.OverviewApp;

public class SetUtil {
    public static Set<OverviewApp> deepCopy(Set<OverviewApp> apps) {
	return apps.stream().map(OverviewApp::new).collect(Collectors.toSet());
    }

    public static Set<OverviewApp> searchByName(Set<OverviewApp> apps, String appName) {
	return search(apps, app -> app.getName().equals(appName));
    }

    public static Set<OverviewApp> searchByState(Set<OverviewApp> apps, AppState appState) {
	return search(apps, app -> app.getState() == appState);
    }

    public static Set<OverviewApp> search(Set<OverviewApp> apps, Predicate<OverviewApp> predicate) {
	return apps.stream().filter(predicate).collect(Collectors.toSet());
    }

    public static boolean noneMatch(Set<OverviewApp> apps, Predicate<OverviewApp> predicate) {
	return apps.stream().noneMatch(predicate);
    }

    public static OverviewApp getUniqueApp(Set<OverviewApp> apps, Predicate<OverviewApp> predicate) {
	Set<OverviewApp> result = search(apps, predicate);
	switch (result.size()) {
	case 0:
	    return null;
	case 1:
	    return result.iterator().next();
	default:
	    throw new IllegalStateException(String
		    .format("Illegal state: found more than one apps [%s] which satisfy [%s]", result, predicate));
	}
    }

    public static OverviewApp getOneApp(Set<OverviewApp> apps, Predicate<OverviewApp> predicate) {
	Set<OverviewApp> result = search(apps, predicate);
	switch (result.size()) {
	case 0:
	    return null;
	default:
	    return result.iterator().next();
	}
    }

    public static Set<OverviewApp> exludedApps(Set<OverviewApp> apps, OverviewApp inclusion) {
	return apps.stream().filter(app -> app != inclusion).collect(Collectors.toSet());
    }

    public static Set<OverviewApp> exludedApps(Set<OverviewApp> apps, Set<OverviewApp> inclusion) {
	return apps.stream().filter(app -> !inclusion.contains(app)).collect(Collectors.toSet());
    }

    public static boolean uniqueByName(Set<OverviewApp> apps) {
	Set<String> names = new HashSet<>();
	for (OverviewApp app : apps) {
	    if (!names.add(app.getName())) {
		return false;
	    }
	}
	return true;
    }

    // verify whether apps have unique path and env
    public static boolean uniqueByPathEnv(Set<OverviewApp> apps) {
	Set<PathEnv> appeared = new HashSet<>();
	for (OverviewApp app : apps) {
	    if (!appeared.add(new PathEnv(app.getPath(), app.getEnv()))) {
		return false;
	    }
	}
	return true;
    }

    static class PathEnv {
	String path;
	Map<String, String> env;

	public PathEnv(String path, Map<String, String> env) {
	    this.path = path;
	    this.env = env;
	}

	@Override
	public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + ((env == null) ? 0 : env.hashCode());
	    result = prime * result + ((path == null) ? 0 : path.hashCode());
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
	    PathEnv other = (PathEnv) obj;
	    if (env == null) {
		if (other.env != null)
		    return false;
	    } else if (!env.equals(other.env))
		return false;
	    if (path == null) {
		if (other.path != null)
		    return false;
	    } else if (!path.equals(other.path))
		return false;
	    return true;
	}
    }
}
