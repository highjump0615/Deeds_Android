package com.iliayugai.deeds;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.LoggingBehavior;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.internal.ImageDownloader;
import com.facebook.internal.ImageRequest;
import com.facebook.internal.ImageResponse;
import com.facebook.internal.Logger;
import com.iliayugai.deeds.adapter.LandingPagerAdapter;
import com.iliayugai.deeds.model.UserData;
import com.iliayugai.deeds.utils.CommonUtils;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.viewpagerindicator.CirclePageIndicator;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

/**
 * Created by highjump on 15-5-26.
 */
public class LandingActivity extends Activity implements View.OnClickListener {

    private static final String TAG = LandingActivity.class.getSimpleName();

    private LayoutInflater mLayoutInflater;
    private ViewPager mViewPager;

    private View mViewBg;
    private int mnColorCurrent;

    private ProfileTracker mProfileTracker;
    private AccessTokenTracker mTokenTracker;

    private Button mButFacebook;

    private ProgressDialog mProgressDialog;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_landing);

        mViewBg = findViewById(R.id.view_bg);

        // init viewpager
        mLayoutInflater = LayoutInflater.from(this);

        View viewDeed = mLayoutInflater.inflate(R.layout.layout_landing_deed, null);
        View viewKind = mLayoutInflater.inflate(R.layout.layout_landing_kind, null);
        View viewFree = mLayoutInflater.inflate(R.layout.layout_landing_free, null);

        ArrayList<View> aryView = new ArrayList<View>();
        aryView.add(viewDeed);
        aryView.add(viewKind);
        aryView.add(viewFree);

        mViewPager = (ViewPager)findViewById(R.id.viewpager);

        mViewPager.setAdapter(new LandingPagerAdapter(aryView));
        mViewPager.setCurrentItem(0);

        CirclePageIndicator circlePageIndicator = (CirclePageIndicator)findViewById(R.id.indicator);
        circlePageIndicator.setViewPager(mViewPager);
//        circlePageIndicator.setRadius(7);
        circlePageIndicator.setOnPageChangeListener(new LandingOnPageChangeList());

        mnColorCurrent = getResources().getColor(R.color.color_theme);

        Button button = (Button)findViewById(R.id.but_signup);
        button.setOnClickListener(this);

        button = (Button)findViewById(R.id.but_signin);
        button.setOnClickListener(this);

        mButFacebook = (Button)findViewById(R.id.but_facebook);
        mButFacebook.setOnClickListener(this);


        mProfileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                if (currentProfile != null) {
                    CommonUtils.mFBCurProfile = currentProfile;
                }
            }
        };

        mTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken accessToken, AccessToken accessToken1) {
                if (accessToken1 != null) {
                    CommonUtils.mFBCurToken = accessToken1;
                }
            }
        };
    }

    @Override
    public void onClick(View view) {

        int id = view.getId();

        switch (id) {
            case R.id.but_signup:
                CommonUtils.moveNextActivity(this, SignupActivity.class, false);
                break;

            case R.id.but_signin:
                CommonUtils.moveNextActivity(this, SigninActivity.class, false);
                break;

            case R.id.but_facebook:
                onButFacebook();
//                setFBProfile();

                break;
        }
    }

    private void gotoMain() {
        mButFacebook.setEnabled(true);

        CommonUtils.moveNextActivity(this, MainActivity.class, false);
    }

    private void onButFacebook() {

        mButFacebook.setEnabled(false);

        ArrayList<String> aryPermission = new ArrayList<>();
        aryPermission.add("user_about_me");
        aryPermission.add("email");
        aryPermission.add("user_location");

        ParseFacebookUtils.logInWithReadPermissionsInBackground(this, aryPermission, new LogInCallback() {
            @Override
            public void done(ParseUser parseUser, ParseException e) {
                if (parseUser == null) {
//                    Log.d(TAG, "Uh oh. The user cancelled the Facebook login");

                    mProgressDialog.dismiss();
                    mButFacebook.setEnabled(true);
                } else if (parseUser.isNew()) {
//                    Log.d(TAG, "User signed up and logged in through Facebook!");

                    setFBProfile();
                } else {
//                    Log.d(TAG, "User logged in through Facebook!");

                    setFBProfile();
                }
            }
        });

        mProgressDialog = ProgressDialog.show(this, "", "Signing in with Facebook...");
    }

    private void setFBProfile() {
        if (CommonUtils.mFBCurProfile == null) {
            mProgressDialog.dismiss();
            gotoMain();
        }

        final UserData currentUser = (UserData) UserData.getCurrentUser();
        currentUser.setFirstname(CommonUtils.mFBCurProfile.getFirstName());
        currentUser.setLastname(CommonUtils.mFBCurProfile.getLastName());
        currentUser.setFullname();

        //
        // photo info
        //
        ImageRequest.Builder requestBuilder = new ImageRequest.Builder(
                this,
                CommonUtils.mFBCurProfile.getProfilePictureUri(CommonUtils.mnszProfilePhoto, CommonUtils.mnszProfilePhoto));

        ImageRequest request = requestBuilder.setAllowCachedRedirects(true)
                .setCallerTag(this)
                .setCallback(
                        new ImageRequest.Callback() {
                            @Override
                            public void onCompleted(ImageResponse response) {
                                Bitmap responseImage = response.getBitmap();

                                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                responseImage.compress(Bitmap.CompressFormat.JPEG, 100, stream);

                                ParseFile filePhoto = new ParseFile("photo.jpg", stream.toByteArray());
                                currentUser.setPhoto(filePhoto);

                                // location and about
                                setLocationAndBio();
                            }
                        })
                .build();

        ImageDownloader.downloadAsync(request);

//        String strPhotoUrl = CommonUtils.mFBCurProfile.getProfilePictureUri(CommonUtils.mnszProfilePhoto, CommonUtils.mnszProfilePhoto).toString();
//        String strPhotoUrl = "https://graph.facebook.com/370875256433886/picture?height=116&width=116";
//        String strPhotoUrl = "http://graph.facebook.com/370875256433886/picture";
//
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        InputStream is = null;
//        URL urlPhoto = null;
//
//        try {
//            urlPhoto = new URL(strPhotoUrl);
//            URLConnection ucon = urlPhoto.openConnection();
//
//            is = ucon.getInputStream();
//            byte[] byteChunk = new byte[4096]; // Or whatever size you want to read in at a time.
//            int n;
//
//            while ( (n = is.read(byteChunk)) > 0 ) {
//                baos.write(byteChunk, 0, n);
//            }
//
//            ParseFile filePhoto = new ParseFile("photo.jpg", baos.toByteArray());
////            currentUser.setPhoto(filePhoto);
//        }
//        catch (MalformedURLException e) {
//            e.printStackTrace();
//        }
//        catch (IOException e) {
//            System.err.printf ("Failed while reading bytes from %s: %s", urlPhoto.toExternalForm(), e.getMessage());
//            e.printStackTrace ();
//            // Perform any other exception handling that's appropriate.
//        }
//        finally {
//            if (is != null) {
//                try {
//                    is.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
    }

    private void setLocationAndBio() {
        // location & about
        GraphRequest request = GraphRequest.newMeRequest(
                CommonUtils.mFBCurToken,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject jsonObject, GraphResponse graphResponse) {

                        try {

                            UserData currentUser = (UserData) UserData.getCurrentUser();

                            if (jsonObject.has("email")) {
                                currentUser.setEmail(jsonObject.getString("email"));
                                currentUser.setUsername(jsonObject.getString("email"));
                            }
                            else {
                                currentUser.setUsername(CommonUtils.mFBCurProfile.getId());
                            }

                            // location and about
                            if (jsonObject.has("bio")) {
                                currentUser.setAbout(jsonObject.getString("bio"));
                            }

                            if (jsonObject.has("location")) {
                                currentUser.setAddress(jsonObject.getJSONObject("location").getString("name"));
                            }

                            currentUser.saveInBackground();
                            mProgressDialog.dismiss();

                            gotoMain();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                });

        request.executeAsync();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
    }

    public class LandingOnPageChangeList implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {

            ColorDrawable aryColor[] = new ColorDrawable[2];
            aryColor[0] = new ColorDrawable(mnColorCurrent);

            switch (position) {
                case 0:
                    aryColor[1] = new ColorDrawable(getResources().getColor(R.color.color_theme));
                    mnColorCurrent = getResources().getColor(R.color.color_theme);
                    break;

                case 1:
                    aryColor[1] = new ColorDrawable(getResources().getColor(R.color.color_green));
                    mnColorCurrent = getResources().getColor(R.color.color_green);
                    break;

                case 2:
                    aryColor[1] = new ColorDrawable(getResources().getColor(R.color.color_red));
                    mnColorCurrent = getResources().getColor(R.color.color_red);
                    break;
            }

            //Let's change background's color from blue to red.
            TransitionDrawable trans = new TransitionDrawable(aryColor);
            trans.startTransition(200);

            //This will work also on old devices. The latest API says you have to use setBackground instead.
            mViewBg.setBackgroundDrawable(trans);

        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    }

}
