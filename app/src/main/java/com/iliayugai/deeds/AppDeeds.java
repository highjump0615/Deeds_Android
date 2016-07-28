package com.iliayugai.deeds;

import android.app.Application;
import android.util.Log;

import com.facebook.FacebookSdk;
import com.iliayugai.deeds.model.CategoryData;
import com.iliayugai.deeds.model.GeneralData;
import com.iliayugai.deeds.model.ItemData;
import com.iliayugai.deeds.model.NotificationData;
import com.iliayugai.deeds.model.UserData;
import com.iliayugai.deeds.utils.CommonUtils;
import com.parse.Parse;
import com.parse.ParseCrashReporting;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseUser;
import com.parse.SaveCallback;

/**
 * Created by highjump on 15-6-2.
 */
public class AppDeeds extends Application {

    private static final String TAG = AppDeeds.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();

        ParseUser.registerSubclass(UserData.class);
        ParseObject.registerSubclass(ItemData.class);
        ParseObject.registerSubclass(CategoryData.class);
        ParseObject.registerSubclass(NotificationData.class);
        ParseObject.registerSubclass(GeneralData.class);

        ParseCrashReporting.enable(this);
        Parse.initialize(this, "UsUIH0KJ5nWdLhCnUztnbl2zJ3mdEsIWR7kEW93C", "qTHfMsjlFUOmkBgQjZDlrkZJWlluwjk2PhJlcC7h");

        FacebookSdk.sdkInitialize(getApplicationContext());
        ParseFacebookUtils.initialize(getApplicationContext());

        ParsePush.subscribeInBackground("", new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
//                    Log.d(TAG, "successfully subscribed to the broadcast channel.");
                }
                else {
//                    Log.d(TAG, "failed to subscribe for push " + e);
                }
            }
        });
    }
}
