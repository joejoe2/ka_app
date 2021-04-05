package com.example.myapplication.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

/**
 * use received bundle's data of 'link' to build a customized youtube player activity
 */
public class YoutubePlayerActivity extends YouTubeBaseActivity{
    private static final String YOUTUBE_API_KEY ="AIzaSyCOaT31V6GC9WhLpjCe0f5zD7PUuyzDWWg";
    private String youtubeVideoLink;
    private Button goBackButton;
    private ImageView manImage;
    private ImageView womanImage;
    private YouTubePlayerView youtubePlayerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getBundle();
        initUI();
        setListener();
        setYoutubePlayer();
    }

    private void getBundle(){
        Bundle bundle=this.getIntent().getExtras();
        youtubeVideoLink = bundle.getString("link");
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
