package com.fency.fency_duel;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;

public abstract class FencyModeActivity extends FencyActivity {
    private final int vibrationLength = 100;

    protected Player user;
    protected Player opponent;
    protected SensorHandler sensorHandler;
    protected Game game;
    protected Vibrator vibrator;
    protected Handler handler;
    protected Runnable runnable;
    protected boolean userAttacking = false;
    protected boolean opponentAttacking = false;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        audioPlayer01 = MediaPlayer.create(this, R.raw.sword_clash);
        user = new Player(this);
        opponent = new Player(this);
        sensorHandler = new SensorHandler(this, user);
        sensorHandler.registerListeners();
        game = new Game(this,user,opponent);
        vibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
        handler = new Handler();
    }

    @Override
    protected void onPause(){
        super.onPause();
        sensorHandler.unregisterListeners();
        if (audioPlayer01!=null)
            audioPlayer01.release();
        if (audioPlayer02!=null)
            audioPlayer02.release();

    }
    @Override
    protected void onResume(){
        super.onResume();
        sensorHandler.registerListeners();
        audioPlayer01 = MediaPlayer.create(this, R.raw.sword_clash);
        audioPlayer02 = null;
    }

    @Override
    protected void onStop(){
        audioPlayer01 = null;
        audioPlayer02 = null;
        finish();
        super.onStop();
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        onStop();
    }


    public abstract void updatePlayerView(Player caller);

    public void updateGameView(int gameState){
        if (gameState == R.integer.GAME_DRAW) {
            vibrator.vibrate(vibrationLength);
            //play sword_clash audio
            if(audioPlayer01 !=null)
                audioPlayer01.start();
        }
    }
}
