package com.fency.fency_duel;

public class Player extends FencyModel{

    public Player(FencyModeActivity activity){
        super(activity);
        state = R.integer.HIGH_STAND;
    }

    public void changeState(int to){
        boolean same = (to==state);

        state = to;
        activity.updatePlayerView(this);
        //TODO: temporary action
        if(activity.getClass().equals(DuelModeActivity.class)) {
            if(this.equals(activity.user) && !same)
                ((DuelModeActivity) activity).sendPlayerState(to);
        }

        if (state==R.integer.HIGH_ATTACK || state==R.integer.LOW_ATTACK) {
            activity.game.changeState();
        }

    }

    public String toString(){
        String str = "Stato = "+ state;
        return str;
    }

    public int getState() {
        return state;
    }
}
