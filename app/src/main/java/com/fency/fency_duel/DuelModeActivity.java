package com.fency.fency_duel;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
//https://github.com/OmarAflak/Bluetooth-Library
import me.aflak.bluetooth.Bluetooth;

public class DuelModeActivity extends FencyModeActivity {

    private static final int INIT_REQUEST = 2;
    private static final int BT_SEND_DELAY = 1; //millisec

    private boolean isReady, isDuelStarted, isSendReady, isBtServer;
    private String myMacAddress, opponentMacAddress;
    private Bluetooth bluetooth;
    private TextView tvUserScore, tvOpponentScore, tvResult;
    private int userScore, opponentScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_duel_mode);
        cntFullScreen = findViewById(R.id.container_duel);

        isReady = false;
        isBtServer = false;
        isDuelStarted = false;
        isSendReady = false;
        Toast.makeText(this,"Duel ON", Toast.LENGTH_SHORT).show();

        Intent initDeviceIntent = new Intent(this, DeviceConnectionActivity.class);
        startActivityForResult(initDeviceIntent, INIT_REQUEST);
    }


    @Override
    public void updatePlayerView(Player caller) {
        if (isReady) {
            // TODO:
            int state = caller.getState();
            ImageView icon = null;
            boolean attackOn = true;

            if(caller.equals(user)){
                icon = (ImageView)findViewById(R.id.ivPlayerState);
                attackOn = userAttacking;
                // Send updates to opponent via BT
                //sendPlayerState(state);
            }
            else if(caller.equals(opponent)){
                icon = (ImageView)findViewById(R.id.ivOpponentState);
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
                        }, 500);
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
                        }, 500);
                    }
                }
            }


        }
    }

    @Override
    public void updateGameView(int gameState){
        super.updateGameView(gameState);
        String result = "Draw";
        int color = (R.color.card_white);

        switch (gameState){
            case R.integer.GAME_DRAW:
                break;
            case R.integer.GAME_P1:
                result = "You WIN! :)";
                userScore++;
                tvUserScore.setText("" + userScore);
                color = R.color.colorAccent;
                break;
            case R.integer.GAME_P2:
                result = "You LOSE :(";
                opponentScore++;
                tvOpponentScore.setText("" + opponentScore);
                color = R.color.opponent_filter;
                break;
        }
        //update result TextView
        //DRAW, WIN or LOSE
        tvResult.setTextColor(getResources().getColor(color));
        tvResult.setText(result);
        tvResult.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == INIT_REQUEST){
            if(resultCode == RESULT_OK) {
                isReady = true;
                opponentMacAddress = data.getStringExtra("key_opponentMacAddress");
                if( (data.getStringExtra("key_connectionRole")).equals("S")){
                    isBtServer = true;
                }
                initialize();
            }
            else {
                Toast.makeText(getApplicationContext(),"Non connesso",Toast.LENGTH_SHORT).show();
                //onStop();
            }
        }
    }

    private void initialize(){
        bluetooth = new Bluetooth(this);
        //TODO

    }

    public void sendPlayerState(int state) {
        //if (user.getState()!= state){
        if (bluetooth!=null && isDuelStarted && isSendReady) {
            isSendReady = false;
            String state_str = "INV";

            switch (state) {
                case R.integer.HIGH_ATTACK:
                    state_str = "H_A";
                    break;
                case R.integer.LOW_ATTACK:
                    state_str = "L_A";
                    break;
                case R.integer.HIGH_STAND:
                    state_str = "H_S";
                    break;
                case R.integer.LOW_STAND:
                    state_str = "L_S";
                    break;
            }
            final String str = state_str;

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    //TODO
                    //simpleBluetooth.sendData(str);
                    isSendReady = true;
                }
            }, BT_SEND_DELAY);

        }
        //}
    }

    private void receivedOpponentState(String state_str) {
        int state_int = R.integer.INVALID;

        //update opponent state
        switch (state_str){
            case "H_A":
                state_int = R.integer.HIGH_ATTACK;
                break;
            case "L_A":
                state_int = R.integer.LOW_ATTACK;
                break;
            case "H_S":
                state_int = R.integer.HIGH_STAND;
                break;
            case "L_S":
                state_int = R.integer.LOW_STAND;
                break;
        }
        //if(opponent.getState()!= state_int)
        opponent.changeState(state_int);


    }

    private void start(){
        //init sensors
        sensorHandler.registerListeners();
        isDuelStarted = true;
        isSendReady = true;
    }
}
