package com.fency.fency_duel;

import android.os.Handler;
import android.widget.Toast;

public class DummyHandler extends FencyHandler{

    private long WAITING_TIME = 2500; //millisec

    private int state;
    private Handler handler;
    private Runnable runnable;
    private int combo;

    public DummyHandler(PracticeModeActivity context, Player player) {
        super(context, player);
        handler = new Handler();
        state = 0;
        combo = 0;
    }

    public void step (boolean success){

        if(success) {
            combo++;
            state = (state+1)%4;

            if(combo%4 == 0 && WAITING_TIME>=700){
                WAITING_TIME -= 200;
                Toast.makeText(context,"speed up!",Toast.LENGTH_SHORT).show();
            }
        }
        else {
            combo = 0;
            WAITING_TIME = 2500;
        }

        ((PracticeModeActivity)context).impera(toImperium());

        handler.postDelayed(runnable= new Runnable() {
            @Override
            public void run() {
                // Allow opponent state change only after delay
                player.changeState(toAction());
            }
        }, WAITING_TIME);

    }

    public int toAction(){
        int action = -1;
        switch (this.state){
            case 0:
                action = R.integer.HIGH_ATTACK;
                break;
            case 1:
                action = R.integer.LOW_ATTACK;
                break;
            case 2:
                action = R.integer.HIGH_STAND;
                break;
            case 3:
                action = R.integer.LOW_STAND;
                break;
        }
        return action;
    }

    public int toImperium(){
        int action = -1;
        switch (this.state){
            case 0:
                action = R.integer.HIGH_STAND;
                break;
            case 1:
                action = R.integer.LOW_STAND;
                break;
            case 2:
                action = R.integer.LOW_ATTACK;
                break;
            case 3:
                action = R.integer.HIGH_ATTACK;
                break;
        }
        return action;
    }

    public int getCombo(){
        return this.combo;
    }

    public void onExit(){
        handler.removeCallbacks(runnable);
    }

}
