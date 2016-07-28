package com.iliayugai.deeds;

import android.app.Activity;
import android.os.Bundle;

import com.iliayugai.deeds.model.UserData;
import com.iliayugai.deeds.utils.CommonUtils;
import com.parse.Parse;
import com.parse.ParseCrashReporting;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by highjump on 15-5-26.
 */
public class SplashActivity extends Activity {

    public Timer mTransitionTimer;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        // init view
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        UserData currentUser = (UserData) UserData.getCurrentUser();

                        if (currentUser != null) {
                            CommonUtils.moveNextActivity(SplashActivity.this, MainActivity.class, true);
                        }
                        else {
                            CommonUtils.moveNextActivity(SplashActivity.this, LandingActivity.class, true);
                        }
                    }
                });
            }
        };
        mTransitionTimer = new Timer();
        mTransitionTimer.schedule(task, 3000);
    }
}
