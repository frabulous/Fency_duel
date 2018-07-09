package com.fency.fency_duel;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class PracticeModeActivity extends FencyModeActivity {

    private static final long ATTACK_ANIMATION_DELAY = 500; //milliseconds
    private static final long SERVICE_TIME= 1500;

    private DummyHandler arbiter;
    private ImageView discipuli, magistri;
    private TextView imperium, approbatio, sequentia;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practice_mode);
        cntFullScreen = findViewById(R.id.container_practice);
        runnable = new Runnable() {
            @Override
            public void run() {
                approbatio.setText("");
            }
        };
        audioPlayer02 = MediaPlayer.create(this, R.raw.approve_01);
        discipuli = (ImageView)findViewById(R.id.ivPlayerState);
        magistri = (ImageView)findViewById(R.id.ivOpponentState);
        imperium = (TextView)findViewById(R.id.tvCommand);
        approbatio = (TextView)findViewById(R.id.tvCheck);
        sequentia = (TextView)findViewById(R.id.tvCombo);
        arbiter = new DummyHandler(this, opponent);
        imperium.setText("");
        approbatio.setText("");

        //start tutorial
        arbiter.step(false);
    }

    @Override
    public void updatePlayerView(Player caller) {

        int state = caller.getState();
        ImageView icon = null;
        boolean attackOn = true;

        if(caller.equals(user)){
            icon = discipuli;
            attackOn = userAttacking;
        }
        else if(caller.equals(opponent)){
            icon = magistri;
            attackOn = opponentAttacking;
        }

        icon.setImageAlpha(255);

        if(!attackOn) {
            //change player img
            switch (state) {
                case R.integer.HIGH_STAND:
                    icon.setImageResource(R.mipmap.fency_high_stand);
                    break;
                case R.integer.LOW_STAND:
                    icon.setImageResource(R.mipmap.fency_low_stand);
                    break;
                case R.integer.HIGH_ATTACK:
                    icon.setImageResource(R.mipmap.fency_high_attack);
                    break;
                case R.integer.LOW_ATTACK:
                    icon.setImageResource(R.mipmap.fency_low_attack);
                    break;
                case R.integer.INVALID:
                    icon.setImageAlpha(80);
                    break;
            }
            if (state==R.integer.HIGH_ATTACK || state==R.integer.LOW_ATTACK){

                if(caller.equals(user)){
                    userAttacking = true;
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // Allow user image change only after delay
                            userAttacking = false;
                        }
                    }, ATTACK_ANIMATION_DELAY);
                }
                else if(caller.equals(opponent)){
                    opponentAttacking = true;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // Allow opponent image and state change only after delay
                            opponentAttacking = false;
                            opponent.changeState(R.integer.HIGH_STAND);
                        }
                    }, ATTACK_ANIMATION_DELAY);
                }
            }
        }
    }


    @Override
    public void updateGameView(int gameState){
        super.updateGameView(gameState);

        if(user.getState()==arbiter.toImperium()) {
            approbatio.setText(R.string.success);
            if(audioPlayer02!=null)
                audioPlayer02.start();
            arbiter.step(true);
        }else {
            approbatio.setText(R.string.failure);
            arbiter.step(false);
        }
        //update combo TextView
        sequentia.setText(""+ arbiter.getCombo());
        sequentia.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));

        //clear the approbatio label with a delay
        handler.postDelayed(runnable, SERVICE_TIME);
    }

    public void impera(int actum){

        switch(actum) {
            case R.integer.HIGH_ATTACK:
                imperium.setText(R.string.attackUp);
                break;
            case R.integer.HIGH_STAND:
                imperium.setText(R.string.parryUp);
                break;
            case R.integer.LOW_ATTACK:
                imperium.setText(R.string.attackDown);
                break;
            case R.integer.LOW_STAND:
                imperium.setText(R.string.parryDown);
                break;
        }
        imperium.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left));

    }

    @Override
    protected void onStop(){
        handler.removeCallbacks(runnable);
        arbiter.onExit();
        super.onStop();
    }

}
