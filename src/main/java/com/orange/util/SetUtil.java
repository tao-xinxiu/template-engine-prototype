package com.orange.util;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.orange.model.architecture.Microservice;
import com.orange.model.architecture.MicroserviceState;

public class SetUtil {
    public static Set<Microservice> deepCopy(Set<Microservice> microservices) {
	return microservices.stream().map(Microservice::new).collect(Collectors.toSet());
    }

    public static Set<Microservice> searchByName(Set<Microservice> microservices, String name) {
	return search(microservices, ms -> ms.getName().equals(name));
    }

    public static Set<Microservice> searchByState(Set<Microservice> microservices,
	    MicroserviceState state) {
	return search(microservices, ms -> ms.getState() == state);
    }

    public static Set<Microservice> search(Set<Microservice> microservices,
	    Predicate<Microservice> predicate) {
	return microservices.stream().filter(predicate).collect(Collectors.toSet());
    }

    public static boolean noneMatch(Set<Microservice> microservices,
	    Predicate<Microservice> predicate) {
	return microservices.stream().noneMatch(predicate);
    }

    public static Microservice getUniqueMicroservice(Set<Microservice> microservices,
	    Predicate<Microservice> predicate) {
	Set<Microservice> result = search(microservices, predicate);
	switch (result.size()) {
	case 0:
	    return null;
	case 1:
	    return result.iterator().next();
	default:
	    throw new IllegalStateException(String.format(
		    "Illegal state: found more than one microservice [%s] which satisfy [%s]", result, predicate));
	}
    }

    public static Microservice getUniqueMicroservice(Set<Microservice> microservices,
	    String name, String version) {
	return getUniqueMicroservice(microservices, ms -> ms.getName().equals(name) && ms.getVersion().equals(version));
    }

    public static Microservice getOneMicroservice(Set<Microservice> microservices,
	    Predicate<Microservice> predicate) {
	Set<Microservice> result = search(microservices, predicate);
	switch (result.size()) {
	case 0:
	    return null;
	default:
	    return result.iterator().next();
	}
    }

    public static Set<String> collectVersions(Set<Microservice> microservices) {
	return microservices.stream().map(ms -> ms.getVersion()).collect(Collectors.toSet());
    }

    public static Set<String> collectNames(Set<Microservice> microservices) {
	return microservices.stream().map(ms -> ms.getName()).collect(Collectors.toSet());
    }

    public static boolean uniqueByName(Set<Microservice> microservices) {
	Set<String> names = new HashSet<>();
	for (Microservice ms : microservices) {
	    if (!names.add(ms.getName())) {
		return false;
	    }
	}
	return true;
    }

    // verify whether microservices have unique path and env
    public static boolean uniqueByPathEnv(Set<Microservice> microservices) {
	Set<PathEnv> appeared = new HashSet<>();
	for (Microservice ms : microservices) {
	    if (!appeared.add(new PathEnv(ms.getPath(), ms.getEnv()))) {
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
