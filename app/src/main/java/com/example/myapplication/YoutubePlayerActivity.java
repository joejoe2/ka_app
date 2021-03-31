package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

public class YoutubePlayerActivity extends YouTubeBaseActivity{
    public static final String YOUTUBE_API_KEY ="AIzaSyCOaT31V6GC9WhLpjCe0f5zD7PUuyzDWWg";
    String youtubeVideoLink;
    Button goBackButton;
    ImageView manImage;
    ImageView womanImage;
    YouTubePlayerView youtubePlayerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUI();
        setListener();
        setYoutubePlayer();
    }

    private void initUI(){
        setContentView(R.layout.activity_youtube_play);
        goBackButton =findViewById(R.id.back_to_2);
        manImage =findViewById(R.id.imageView5);
        womanImage =findViewById(R.id.imageView6);
        manImage.bringToFront();
        womanImage.bringToFront();
        youtubePlayerView = findViewById(R.id.youtube_player);
    }

    private void setListener(){
        goBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                YoutubePlayerActivity.this.finish();
            }
        });
    }

    private void setYoutubePlayer(){
        Bundle bundle=this.getIntent().getExtras();
        youtubeVideoLink = bundle.getString("link");
        youtubePlayerView.initialize(YOUTUBE_API_KEY, new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                youTubePlayer.loadVideo(youtubeVideoLink);
            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
                Toast.makeText(getApplicationContext(),"Video Loading Failed",Toast.LENGTH_SHORT).show();
            }
        });
    }
}
