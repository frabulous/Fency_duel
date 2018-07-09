package com.fency.fency_duel;

import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;

public class HomeActivity extends FencyActivity implements View.OnClickListener{

    private View btnAudio01;
    private View toPracticeMode, toDuelMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        cntFullScreen = findViewById(R.id.container_home);
        goFullScreen();

        toPracticeMode = findViewById(R.id.btnPractice);
        toPracticeMode.setOnClickListener(this);

        toDuelMode = findViewById(R.id.btnDuel);
        toDuelMode.setOnClickListener(this);

        btnAudio01 = findViewById(R.id.audio01_box);
        btnAudio01.setOnClickListener(this);

        audioPlayer01 = MediaPlayer.create(this, R.raw.menu_theme);
        audioPlayer01.setLooping(true);

        audioPlayer02 = MediaPlayer.create(this, R.raw.turn_page);
    }


    @Override
    public void onClick(View view) {
        int id = view.getId();

        switch(id){
            case R.id.btnPractice:
                if (audioPlayer02!=null)
                    audioPlayer02.start();
                switchActivity(PracticeModeActivity.class);
                break;
            case R.id.btnDuel:
                if (audioPlayer02!=null)
                    audioPlayer02.start();
                switchActivity(DuelModeActivity.class);
                break;
            case R.id.audio01_box:
                CheckBox s1 = (CheckBox) view;
                if (s1.isChecked()){
                    audioPlayer01.start();
                    s1.setTextColor(0xDC110901);
                }
                else {
                    audioPlayer01.pause();
                    s1.setTextColor(Color.WHITE);
                }
                break;
        }
    }

    @Override
    protected void onPause(){
        audioPlayer01.pause();
        super.onPause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        audioPlayer01.start();
    }
}
