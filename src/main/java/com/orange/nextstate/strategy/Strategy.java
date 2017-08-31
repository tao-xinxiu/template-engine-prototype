package com.orange.nextstate.strategy;

import java.util.List;

import com.orange.model.StrategyConfig;
import com.orange.model.state.Architecture;

public abstract class Strategy {
    protected StrategyConfig config;
    protected StrategyLibrary library;

    public Strategy(StrategyConfig config) {
	this.config = config;
	this.library = new StrategyLibrary(config);
    }

    public abstract boolean valid(Architecture currentState, Architecture finalState);

    public abstract List<Transit> transits();

}