package com.iliayugai.deeds;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.iliayugai.deeds.model.NotificationData;
import com.iliayugai.deeds.model.UserData;
import com.iliayugai.deeds.utils.CommonUtils;
import com.iliayugai.deeds.view.ViewHolderReviewInfo;
import com.makeramen.roundedimageview.RoundedImageView;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by highjump on 15-6-11.
 */
public class RateActivity extends Activity implements View.OnClickListener {

    private static final String TAG = RateActivity.class.getSimpleName();

    private UserData mUser;

    private SeekBar mSBRate;
    private EditText mEditReview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_rate);

        mUser = CommonUtils.mUserSelected;
        UserData currentUser = (UserData) UserData.getCurrentUser();

        // init view
        Button button = (Button)findViewById(R.id.but_back);
        button.setOnClickListener(this);

        ViewHolderReviewInfo vh = new ViewHolderReviewInfo(this, findViewById(R.id.layout_root));
        vh.fillContent(mUser);

        TextView textView = (TextView)findViewById(R.id.text_exp);
        textView.setText("Your Experience About " + mUser.getUsernameToShow());

        mSBRate = (SeekBar)findViewById(R.id.seekbar_rate);
        mSBRate.incrementProgressBy(1);
        mSBRate.setProgress(1);

        RoundedImageView imgviewUser = (RoundedImageView)findViewById(R.id.imgview_user);
        currentUser.showPhoto(this, imgviewUser);

        mEditReview = (EditText)findViewById(R.id.edit_review);

        button = (Button)findViewById(R.id.but_submit);
        button.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        switch (id) {
            case R.id.but_back:
                onBackPressed();
                break;

            case R.id.but_submit:
                onButSubmit();
                break;
        }
    }

    private void onButSubmit() {
        String strReview = mEditReview.getText().toString();

        if (TextUtils.isEmpty(strReview)) {
            return;
        }

        //
        // save to notification database
        //
        final UserData currentUser = (UserData) UserData.getCurrentUser();

        final NotificationData notifyObj = new NotificationData();
        notifyObj.setUser(currentUser);
        notifyObj.setUsername(currentUser.getUsernameToShow());
        notifyObj.setTargetUser(mUser);
        notifyObj.setType(NotificationData.NOTIFICATION_RATE);
        notifyObj.setComment(strReview);
        notifyObj.setRate(mSBRate.getProgress());

        notifyObj.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    CommonUtils.createErrorAlertDialog(RateActivity.this, "", e.getLocalizedMessage()).show();
                    return;
                }
                else {
                    // push notification
                    ParseQuery query = ParseInstallation.getQuery();
                    query.whereEqualTo("user", mUser);

                    try {
                        ParsePush push = new ParsePush();
                        push.setQuery(query);
                        JSONObject data = new JSONObject("{\"alert\": \"You have been given a rating\"," +
                                                        "\"notifyType\": \"rate\"," +
                                                        "\"notifyItem\":" + currentUser.getObjectId() + "," +
                                                        "\"badge\": \"Increment\"," +
                                                        "\"sound\": \"cheering.caf\"}");
                        push.setData(data);
                        push.sendInBackground();
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }

                    // add review object
                    Map mapParam = new HashMap();
                    mapParam.put("userId", mUser.getObjectId());
                    mapParam.put("notifyId", notifyObj.getObjectId());

                    ParseCloud.callFunctionInBackground("addReviewToUser", mapParam, new FunctionCallback<Object>() {
                        @Override
                        public void done(Object o, ParseException e) {
                            if (e != null) {
//                                Log.d(TAG, e.getLocalizedMessage());
                            }
                        }
                    });

                    mUser.maryReview.add(0, notifyObj);
                    mUser.mnReviewCount++;

                    onBackPressed();
                }
            }
        });
    }
}
