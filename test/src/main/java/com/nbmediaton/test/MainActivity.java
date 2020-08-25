package com.nbmediaton.test;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private Button button;

    private final static String adUnitId = "1597376394065";

    private final static String appKey = "d1493247164b4be0aaebbc5901ea9543";

    private static boolean isLoad = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        InMobiHelper.getInstance().initRewardedVideo(this, appKey);
        button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLoad) {
                    isLoad = false;
                    if (InMobiHelper.getInstance().isRewardedVideoAvailable(adUnitId)) {
                        InMobiHelper.getInstance().showRewardedVideo(adUnitId);
                        button.setEnabled(true);
                        button.setText("加载激励视频");
                    } else {
                        Toast.makeText(MainActivity.this, "广告还没准备好", Toast.LENGTH_SHORT).show();
                    }
                    return;
                }
                button.setEnabled(false);
                button.setText("加载中...");
                InMobiHelper.getInstance().loadRewardedVideo(MainActivity.this, adUnitId, new InMobiHelper.RewardedVideoCallback() {
                    @Override
                    public void onAdLoadFailed() {
                        isLoad = true;
                        button.setEnabled(true);
                        button.setText("加载激励视频");
                    }

                    @Override
                    public void onAdLoadSucceeded() {
                        isLoad = true;
                        button.setEnabled(true);
                        button.setText("展示激励视频");
                    }
                });

            }

        });

    }

}
