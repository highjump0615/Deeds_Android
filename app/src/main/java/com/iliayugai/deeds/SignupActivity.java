package com.iliayugai.deeds;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.iliayugai.deeds.adapter.LandingPagerAdapter;
import com.iliayugai.deeds.model.ItemData;
import com.iliayugai.deeds.model.NotificationData;
import com.iliayugai.deeds.model.UserData;
import com.iliayugai.deeds.utils.CommonUtils;
import com.iliayugai.deeds.widget.CustomViewPager;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;
import com.viewpagerindicator.CirclePageIndicator;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by highjump on 15-5-29.
 */
public class SignupActivity extends Activity implements View.OnClickListener {

    private static final String TAG = SignupActivity.class.getSimpleName();

    private LayoutInflater mLayoutInflater;
    private CustomViewPager mViewPager;

    private static int PAGE_FORM = 0;
    private static int PAGE_PHOTO = 1;
    private int mnCurrentPage = PAGE_FORM;

    private EditText mEditEmail;
    private EditText mEditFirstname;
    private EditText mEditLastname;
    private EditText mEditPhone;
    private EditText mEditLocation;
    private EditText mEditPwd;
    private EditText mEditCPwd;
    private Switch mSwitch;

    private Button mButLogout;
    private Button mButNext;
    private ImageView mImgviewPhoto;
    private EditText mEditAbout;

    // variables to capture image
    private Uri mFileUri;
    private Bitmap mBitmapCropped = null;

    private ProgressDialog mProgressDialog;

    private String mstrNameOld;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_signup);

        // init viewpager
        mLayoutInflater = LayoutInflater.from(this);

        View viewForm = mLayoutInflater.inflate(R.layout.layout_signup_form, null);
        View viewPhoto = mLayoutInflater.inflate(R.layout.layout_signup_photo, null);

        ArrayList<View> aryView = new ArrayList<View>();
        aryView.add(viewForm);
        aryView.add(viewPhoto);

        mViewPager = (CustomViewPager)findViewById(R.id.viewpager);

        mViewPager.setAdapter(new LandingPagerAdapter(aryView));
        mViewPager.setCurrentItem(mnCurrentPage);
        mViewPager.setPagingEnabled(false);

        CirclePageIndicator circlePageIndicator = (CirclePageIndicator)findViewById(R.id.indicator);
        circlePageIndicator.setViewPager(mViewPager);
//        circlePageIndicator.setRadius(12);

        int nColorTheme = getResources().getColor(R.color.color_theme);
        int nColorThemeTrans = Color.argb(0x7f, Color.red(nColorTheme), Color.green(nColorTheme), Color.blue(nColorTheme));
        circlePageIndicator.setFillColor(nColorTheme);
        circlePageIndicator.setPageColor(nColorThemeTrans);

        // init edit text
        TextView textTitle = (TextView)viewForm.findViewById(R.id.text_title);
        mEditEmail = (EditText)viewForm.findViewById(R.id.edit_email);
        mEditFirstname = (EditText)viewForm.findViewById(R.id.edit_firstname);
        mEditLastname = (EditText)viewForm.findViewById(R.id.edit_lastname);
        mEditPhone = (EditText)viewForm.findViewById(R.id.edit_phone);
        mEditLocation = (EditText)viewForm.findViewById(R.id.edit_location);
        mEditPwd = (EditText)viewForm.findViewById(R.id.edit_password);
        mEditCPwd = (EditText)viewForm.findViewById(R.id.edit_cpassword);
        mSwitch = (Switch)viewForm.findViewById(R.id.switch_term);

        mImgviewPhoto = (ImageView)viewPhoto.findViewById(R.id.imgview_photo);
        mEditAbout = (EditText)viewPhoto.findViewById(R.id.edit_about);

        // Button
        Button button = (Button)findViewById(R.id.but_back);
        button.setOnClickListener(this);

        mButLogout = (Button)findViewById(R.id.but_logout);
        mButLogout.setOnClickListener(this);

        LinearLayout layoutSwitch = (LinearLayout)viewForm.findViewById(R.id.layout_switch);

        UserData currentUser = (UserData)UserData.getCurrentUser();
        if (currentUser == null) {
            mEditPwd.setVisibility(View.VISIBLE);
            mEditCPwd.setVisibility(View.VISIBLE);
            layoutSwitch.setVisibility(View.VISIBLE);
        }
        else {
            mEditPwd.setVisibility(View.GONE);
            mEditCPwd.setVisibility(View.GONE);
            layoutSwitch.setVisibility(View.GONE);

            mEditEmail.setText(currentUser.getEmail());
            mEditFirstname.setText(currentUser.getFirstname());
            mEditLastname.setText(currentUser.getLastname());
            mEditPhone.setText(currentUser.getPhone());
            mEditLocation.setText(currentUser.getAddress());
            mEditAbout.setText(currentUser.getAbout());

            // show photo
            currentUser.showPhoto(this, mImgviewPhoto);

            // fill contents
            textTitle.setText("Please hit next step after making changes");
        }

        mButNext = (Button)findViewById(R.id.but_next);
        mButNext.setOnClickListener(this);

        button = (Button)viewPhoto.findViewById(R.id.but_take_photo);
        button.setOnClickListener(this);

        button = (Button)viewPhoto.findViewById(R.id.but_choose_photo);
        button.setOnClickListener(this);

        updateButton();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        switch (id) {
            case R.id.but_back:
                if (mnCurrentPage == PAGE_FORM) {
                    onBackPressed();
                }
                else {
                    mnCurrentPage = PAGE_FORM;
                    mViewPager.setCurrentItem(mnCurrentPage, true);

                    updateButton();
                }
                break;

            case R.id.but_next:
                onButNext();

                break;

            case R.id.but_take_photo:
                onTakePhoto();
                break;

            case R.id.but_choose_photo:
                onChoosePhoto();
                break;

            case R.id.but_logout:

                // remove user from installation
                ParseInstallation installation = ParseInstallation.getCurrentInstallation();
                if (installation != null) {
                    installation.remove("user");
                    installation.saveInBackground();
                }

                ParseUser.logOutInBackground();
                AccessToken.setCurrentAccessToken(null);

                Intent intent = new Intent(this, LandingActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                overridePendingTransition(R.anim.pop_in, R.anim.pop_out);

                break;
        }
    }

    private void onChoosePhoto() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
        intent.setType("image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 256);
        intent.putExtra("outputY", 256);
        intent.putExtra("scale", 1);
        intent.putExtra("return-data", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mFileUri);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        //intent.putExtra("noFaceDetection",!faceDetection); // lol, negative boolean noFaceDetection

        startActivityForResult(intent, CommonUtils.GALLERY_OPEN_IMAGE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.e(TAG, "onActivityResult : requestCode : " + requestCode);

        switch (requestCode) {
            case CommonUtils.CAMERA_CAPTURE_IMAGE_REQUEST_CODE:
                if (resultCode == RESULT_OK) {

//                    Bundle extras = data.getExtras();
//                    if (extras != null) {
//                        mBitmapCropped = extras.getParcelable("data");
//                        refreshImageViews();
//                    }

                    //call the standard crop action intent (the user device may not support it)
                    Intent cropIntent = new Intent("com.android.camera.action.CROP");
                    //indicate image type and Uri
                    cropIntent.setDataAndType(mFileUri, "image/*");
                    //set crop properties
                    cropIntent.putExtra("crop", "true");
                    //indicate aspect of desired crop
                    cropIntent.putExtra("aspectX", 1);
                    cropIntent.putExtra("aspectY", 1);
                    //indicate output X and Y
                    cropIntent.putExtra("outputX", 256);
                    cropIntent.putExtra("outputY", 256);
                    //retrieve data on return
                    cropIntent.putExtra("return-data", true);
                    //start the activity - we handle returning in onActivityResult
                    startActivityForResult(cropIntent, CommonUtils.CAMERA_CROP_REQUEST_CODE);

                }
                else if (resultCode == RESULT_CANCELED) {
                    // User cancelled the photoImage capture
                    mFileUri = null;
                }
                else {
                    // Image capture failed, advise user
                    mFileUri = null;
                }
                break;

            case CommonUtils.CAMERA_CROP_REQUEST_CODE:
            case CommonUtils.GALLERY_OPEN_IMAGE_REQUEST_CODE: {

                if (resultCode == RESULT_OK) {
                    //get the returned data
                    Bundle extras = data.getExtras();
                    //get the cropped bitmap
                    mBitmapCropped = extras.getParcelable("data");

                    refreshImageViews();
                }
                break;
            }
        }
    }

    /**
     * Display room photo in ImageViews
     */
    private void refreshImageViews() {

        mImgviewPhoto.setImageBitmap(mBitmapCropped);

        if (mFileUri != null) {
//            mImgviewPhoto.setImageBitmap(CommonUtils.getBitmapFromUri(Uri.parse(mFileUri.getPath()), 1, 500));
        }
    }

    private void onButNext() {
        String strEmail = mEditEmail.getText().toString();
        String strFirstname = mEditFirstname.getText().toString();
        String strLastname = mEditLastname.getText().toString();
        String strPhone = mEditPhone.getText().toString();
        String strLocation = mEditLocation.getText().toString();
        String strPwd = mEditPwd.getText().toString();
        String strCPwd = mEditCPwd.getText().toString();

        UserData currentUser = (UserData)UserData.getCurrentUser();

        if (mnCurrentPage == PAGE_FORM) {

            if (TextUtils.isEmpty(strEmail)) {
                CommonUtils.createErrorAlertDialog(this, "", "Input your email address").show();
                return;
            }

            if (TextUtils.isEmpty(strFirstname)) {
                CommonUtils.createErrorAlertDialog(this, "", "Input your first name").show();
                return;
            }

            if (TextUtils.isEmpty(strLastname)) {
                CommonUtils.createErrorAlertDialog(this, "", "Input your last name").show();
                return;
            }

            if (currentUser == null) {
                if (TextUtils.isEmpty(strPwd)) {
                    CommonUtils.createErrorAlertDialog(this, "", "Input your password").show();
                    return;
                }

                if (!strPwd.equals(strCPwd)) {
                    CommonUtils.createErrorAlertDialog(this, "", "Password does not match").show();
                    return;
                }

                if (!mSwitch.isChecked()) {
                    CommonUtils.createErrorAlertDialog(this, "", "You must agree to our terms and conditions").show();
                    return;
                }
            }

            mnCurrentPage = PAGE_PHOTO;
            mViewPager.setCurrentItem(mnCurrentPage, true);

            updateButton();
        }
        else {
            UserData user = currentUser;

            if (user == null) {
                user = new UserData();
            }

            mstrNameOld = user.getUsernameToShow();

            user.setUsername(strEmail);
            user.setEmail(strEmail);
            user.setFirstname(strFirstname);
            user.setLastname(strLastname);
            user.setFullname();
            user.setPhone(strPhone);
            user.setAddress(strLocation);
            user.setAbout(mEditAbout.getText().toString());

            user.setPassword(strPwd);

            if (mBitmapCropped != null) {
                mProgressDialog = ProgressDialog.show(this, "", "Saving photo image ...");

                final ParseFile fileImage = new ParseFile("photo.jpg", CommonUtils.getCompressedBitmap(mBitmapCropped, CommonUtils.mnszProfilePhoto));
                final UserData finalUser = user;

                fileImage.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        mProgressDialog.dismiss();

                        if (e == null) {
                            finalUser.setPhoto(fileImage);
                            saveUserInfo(finalUser);
                        } else {
                            CommonUtils.createErrorAlertDialog(SignupActivity.this, "", e.getLocalizedMessage()).show();
                        }
                    }
                });
            }
            else {
                saveUserInfo(user);
            }
        }
    }

    private void saveUserInfo(UserData user) {
        UserData currentUser = (UserData)UserData.getCurrentUser();

        if (currentUser != null) {
            mProgressDialog = ProgressDialog.show(this, "", "Saving ...");

            user.saveInBackground();

            final String strNameNew = user.getUsernameToShow();

            if (!strNameNew.equals(mstrNameOld)) {
                // change username when changed
                ParseQuery<ItemData> itemQuery = ParseQuery.getQuery(ItemData.class);
                itemQuery.whereEqualTo("user", user);

                itemQuery.findInBackground(new FindCallback<ItemData>() {
                    @Override
                    public void done(List<ItemData> list, ParseException e) {
                        if (e == null) {
                            for (ItemData iData : list) {
                                iData.setUsername(strNameNew);
                                iData.saveInBackground();
                            }
                        }
                        else {
//                            Log.d(TAG, e.getLocalizedMessage());
                        }
                    }
                });

                ParseQuery<NotificationData> notifyQuery = ParseQuery.getQuery(NotificationData.class);
                notifyQuery.whereEqualTo("user", user);

                notifyQuery.findInBackground(new FindCallback<NotificationData>() {
                    @Override
                    public void done(List<NotificationData> list, ParseException e) {
                        if (e == null) {
                            for (NotificationData nData : list) {
                                nData.setUsername(strNameNew);
                                nData.saveInBackground();
                            }
                        }
                        else {
//                            Log.d(TAG, e.getLocalizedMessage());
                        }
                    }
                });
            }

            // back
            onBackPressed();
        }
        else {
            mProgressDialog = ProgressDialog.show(this, "", "Signing up ...");

            user.signUpInBackground(new SignUpCallback() {
                @Override
                public void done(ParseException e) {
                    mProgressDialog.dismiss();

                    if (e == null) {
                        CommonUtils.moveNextActivity(SignupActivity.this, MainActivity.class, true);
                    } else {
                        CommonUtils.createErrorAlertDialog(SignupActivity.this, "", e.getLocalizedMessage()).show();
                    }
                }
            });
        }
    }

    private void onTakePhoto() {
        try {
            mFileUri = getOutputMediaFileUri(CommonUtils.MEDIA_TYPE_IMAGE);
            Log.e(TAG, "mFileUri = " + mFileUri.getPath());

            //create new Intent
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mFileUri);

            startActivityForResult(intent, CommonUtils.CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
        }
        catch (ActivityNotFoundException anfe) {
            CommonUtils.createErrorAlertDialog(this, "Alert", "Your device doesn't support capturing images!").show();
        }
    }

    /**
     * Creating file uri to store photoImage/video
     */
    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(CommonUtils.getOutputMediaFile(this, type == CommonUtils.MEDIA_TYPE_IMAGE));
    }

    private void updateButton() {

        UserData currentUser = (UserData)UserData.getCurrentUser();

        if (mnCurrentPage == PAGE_FORM) {
            mButNext.setText("NEXT STEP");

            if (currentUser != null) {
                mButLogout.setVisibility(View.VISIBLE);
            }
        }
        else {
            mButNext.setText("FINISH");

            if (currentUser != null) {
                mButLogout.setVisibility(View.GONE);
            }
        }
    }
}
