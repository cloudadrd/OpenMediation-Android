package com.fuguishj.mobi;

import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.VideoView;

import com.bytedance.applog.AppLog;
import com.bytedance.applog.InitConfig;
import com.bytedance.applog.util.UriConfig;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        VideoView videoView = findViewById(R.id.my_video);

        videoView.setVideoURI(Uri.parse("android.resource://com.fuguishj.mobi/" + R.raw.video));
        videoView.seekTo(0);
        videoView.requestFocus();

        final InitConfig config = new InitConfig("194117", "NbJrtt");

        config.setUriConfig(UriConfig.DEFAULT);
        config.setEnablePlay(true);
        AppLog.setEnableLog(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                AppLog.init(MainActivity.this, config);
            }
        }).start();


        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
                mp.setLooping(true);
            }
        });

    }
}
