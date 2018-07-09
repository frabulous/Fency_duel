package com.fency.fency_duel;

public abstract class FencyHandler {

    protected Player player;
    protected FencyModeActivity context;

    public FencyHandler(FencyModeActivity context, Player player){
        this.context = context;
        this.player = player;
    }
}
