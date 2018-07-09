package com.fency.fency_duel;

public class Game extends FencyModel {

    private Player playerOne;
    private Player playerTwo;

    public Game(FencyModeActivity activity, Player one, Player two){
        super(activity);
        playerOne = one;
        playerTwo = two;
        state = R.integer.GAME_DRAW;
    }

    public void changeState(){
        int s1 = playerOne.getState();
        int s2 = playerTwo.getState();

        if((s1==R.integer.HIGH_ATTACK && (s2==R.integer.LOW_STAND || s2==R.integer.INVALID)) ||
                (s1==R.integer.LOW_ATTACK && (s2==R.integer.HIGH_STAND || s2==R.integer.INVALID))) {
            state = R.integer.GAME_P1;
        }
        else if((s2==R.integer.HIGH_ATTACK && (s1==R.integer.LOW_STAND || s1==R.integer.INVALID)) ||
                (s2==R.integer.LOW_ATTACK && (s1==R.integer.HIGH_STAND || s1==R.integer.INVALID))) {
            state = R.integer.GAME_P2;
        }
        else {
            state = R.integer.GAME_DRAW;
        }

        activity.updateGameView(state);

    }
}
