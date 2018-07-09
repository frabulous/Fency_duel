package com.fency.fency_duel;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public abstract class FencyActivity extends AppCompatActivity {

    protected Intent startingIntent;
    protected View cntFullScreen;
    protected MediaPlayer audioPlayer01;
    protected MediaPlayer audioPlayer02;

    protected void goFullScreen(){
        cntFullScreen.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    protected void switchActivity(Class<? extends FencyActivity> to){
        Intent intent = new Intent(this, to);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //get Intent from Home
        startingIntent = getIntent();

        audioPlayer01=null;
        audioPlayer02=null;
    }

    @Override
    protected void onPause(){
        super.onPause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        goFullScreen();
    }

    @Override
    protected void onStop(){
        super.onStop();
    }
}
