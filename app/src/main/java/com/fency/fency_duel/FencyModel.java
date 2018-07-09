package com.fency.fency_duel;

public abstract class FencyModel {

    protected FencyModeActivity activity;
    protected int state;

    public FencyModel(FencyModeActivity activity){
        this.activity = activity;
        state = 0;
    }
}
