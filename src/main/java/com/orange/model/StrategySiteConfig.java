package com.orange.model;

public class StrategySiteConfig {
    private static final String defaultTmpRouteHostSuffix = "-tmp";
    private String tmpRouteHostSuffix = defaultTmpRouteHostSuffix;
    private String tmpRouteDomain;
    // TODO add timeout, retry times of deployment operations

    public StrategySiteConfig() {
    }

    public StrategySiteConfig(String tmpRouteDomain) {
	this.tmpRouteDomain = tmpRouteDomain;
    }

    public String getTmpRouteHostSuffix() {
	return tmpRouteHostSuffix;
    }

    public void setTmpRouteHostSuffix(String tmpRouteHostSuffix) {
	this.tmpRouteHostSuffix = tmpRouteHostSuffix;
    }

    public String getTmpRouteDomain() {
	return tmpRouteDomain;
    }

    public void setTmpRouteDomain(String tmpRouteDomain) {
	this.tmpRouteDomain = tmpRouteDomain;
    }

    public static String getDefaulttmproutehostsuffix() {
	return defaultTmpRouteHostSuffix;
    }

    public String getTmpRoute(String appName) {
	return appName + tmpRouteHostSuffix + "." + tmpRouteDomain;
    }
}