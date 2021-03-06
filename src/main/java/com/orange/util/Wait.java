package com.orange.util;

import java.util.function.Consumer;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.heroku.api.Build;

public class Wait {
    private static Logger logger = LoggerFactory.getLogger(RetryFunction.class);
    private int timeoutSec;
    private int backoffSec = 1;

    public Wait(int timeoutSec) {
	this.timeoutSec = timeoutSec;
    }

    public void waitUntil(Predicate<String> predicate, String predicateText, String parameter) {
	logger.info("start " + predicateText);
	// int waitedSec = 0;
	long endTime = System.currentTimeMillis() + timeoutSec * 1000;
	while (!predicate.test(parameter)) {
	    try {
		Thread.sleep(1000 * backoffSec);
	    } catch (InterruptedException e) {
		throw new IllegalStateException(e);
	    }
	    // waitedSec += backoffSec;
	    // if (waitedSec >= timeoutSec) {
	    if (System.currentTimeMillis() >= endTime) {
		throw new IllegalStateException("After " + timeoutSec + "s, Timeout during waiting " + predicateText);
	    }
	}
	logger.info(predicateText + " finished.");
    }

    public void waitUntil(Predicate<Build> predicate, String predicateText, Build parameter) {
	logger.info("start " + predicateText);
	long endTime = System.currentTimeMillis() + timeoutSec * 1000;
	while (!predicate.test(parameter)) {
	    try {
		Thread.sleep(1000 * backoffSec);
	    } catch (InterruptedException e) {
		throw new IllegalStateException(e);
	    }
	    if (System.currentTimeMillis() >= endTime) {
		throw new IllegalStateException("After " + timeoutSec + "s, Timeout during waiting " + predicateText);
	    }
	}
	logger.info(predicateText + " finished.");
    }

    public void waitUntil(Predicate<String> predicate, String predicateText, String parameter,
	    Consumer<? super String> action, String actionParam) {
	logger.info("start " + predicateText);
	long endTime = System.currentTimeMillis() + timeoutSec * 1000;
	while (!predicate.test(parameter)) {
	    try {
		action.accept(actionParam);
		Thread.sleep(1000 * backoffSec);
	    } catch (InterruptedException e) {
		throw new IllegalStateException(e);
	    }
	    if (System.currentTimeMillis() >= endTime) {
		throw new IllegalStateException("After " + timeoutSec + "s, Timeout during waiting " + predicateText);
	    }
	}
	logger.info(predicateText + " finished.");
    }

}
