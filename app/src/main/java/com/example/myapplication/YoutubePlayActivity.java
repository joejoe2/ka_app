package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

public class YoutubePlayActivity extends YouTubeBaseActivity{
    public static final String API_KEY ="AIzaSyCOaT31V6GC9WhLpjCe0f5zD7PUuyzDWWg";
    String ytblink="";
    Button go_back_two;
    ImageView man2;
    ImageView woman2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube_play);
        go_back_two=findViewById(R.id.back_to_2);
        man2=findViewById(R.id.imageView5);
        woman2=findViewById(R.id.imageView6);
        man2.bringToFront();
        woman2.bringToFront();
        go_back_two.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                YoutubePlayActivity.this.finish();
            }
        });

        Bundle bundle=this.getIntent().getExtras();
        ytblink=bundle.getString("link");
        YouTubePlayerView youtubePlayerView = findViewById(R.id.youtube_player);
        youtubePlayerView.initialize(API_KEY, new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                youTubePlayer.loadVideo(ytblink);
            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
                Toast.makeText(getApplicationContext(),"Video Loading Failed",Toast.LENGTH_SHORT).show();
            }
        });
    }
}
