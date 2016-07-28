package com.iliayugai.deeds;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.iliayugai.deeds.model.CategoryData;
import com.iliayugai.deeds.model.GeneralData;
import com.iliayugai.deeds.model.ItemData;
import com.iliayugai.deeds.model.UserData;
import com.iliayugai.deeds.utils.CommonUtils;
import com.iliayugai.deeds.view.ImageItemView;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Created by highjump on 15-6-1.
 */
public class AddActivity extends Activity implements View.OnClickListener {

    private static final String TAG = AddActivity.class.getSimpleName();

    private static final int IMAGE_ITEM_COUNT = 3;
    private int mnSavedImgCount = 0;

    public static String ADD_TYPE = "add_type";
    public static int ADD_POST = 0;
    public static int EDIT_POST = 1;

    private int mnType = ADD_POST;
    private int mnItemType = ItemData.ITEMTYPE_DEED;

    private EditText mEditTitle;
    private EditText mEditDesc;

    private String mstrLocation;
    private TextView mTextLocation;

    private ItemData mItem;

    private ArrayList<ImageItemView> maryItemView = new ArrayList<ImageItemView>();
    private ArrayList<TargetWithIndex> maryTarget = new ArrayList<TargetWithIndex>();
    private ArrayList<ParseFile> maryFile = new ArrayList<ParseFile>();

    private int mnSelectedIndex = -1;   // for category
    private TextView mTextCategory;

    private Button mButDeed;
    private Button mButNeed;
    private ImageView mImgviewDeedMark;

    // variables to capture image
    private Uri mFileUri;
    private Bitmap mBitmapCropped = null;

    private ProgressDialog mProgressDialog;

    private boolean mbImageModified = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add);

        // init view
        Button butBack = (Button)findViewById(R.id.but_back);
        butBack.setOnClickListener(this);

        TextView textTitle = (TextView)findViewById(R.id.text_title);

        Button butPublish = (Button)findViewById(R.id.but_savepublish);
        LinearLayout layoutSave = (LinearLayout)findViewById(R.id.layout_save);

        mButDeed = (Button)findViewById(R.id.but_deed);
        mButDeed.setOnClickListener(this);

        mButNeed = (Button)findViewById(R.id.but_need);
        mButNeed.setOnClickListener(this);

        mImgviewDeedMark = (ImageView)findViewById(R.id.imgview_deed_mark);

        mEditTitle = (EditText)findViewById(R.id.edit_title);
        mEditDesc = (EditText)findViewById(R.id.edit_desc);

        // take photo buttons
        ImageView imgview = (ImageView)findViewById(R.id.imgview_camera);
        imgview.setOnClickListener(this);

        imgview = (ImageView)findViewById(R.id.imgview_plus);
        imgview.setOnClickListener(this);

        ImageItemView itemView = (ImageItemView)findViewById(R.id.item1);
        maryItemView.add(itemView);

        itemView = (ImageItemView)findViewById(R.id.item2);
        maryItemView.add(itemView);

        itemView = (ImageItemView)findViewById(R.id.item3);
        maryItemView.add(itemView);

        int i = 0;

        for (ImageItemView iView : maryItemView) {

            final int finalI = i;

            iView.mImgviewContent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onImageItem(finalI);
                }
            });

            iView.mImgviewClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onImageItemClose(finalI);
                }
            });

            i++;
        }

        // location & category
        mTextLocation = (TextView)findViewById(R.id.text_location);
        Button button = (Button)findViewById(R.id.but_location);
        button.setOnClickListener(this);

        mTextCategory = (TextView)findViewById(R.id.text_category);
        button = (Button)findViewById(R.id.but_category);
        button.setOnClickListener(this);

        // save buttons
        button = (Button)findViewById(R.id.but_savepublish);
        button.setOnClickListener(this);

        button = (Button)findViewById(R.id.but_delete);
        button.setOnClickListener(this);

        button = (Button)findViewById(R.id.but_save);
        button.setOnClickListener(this);

        button = (Button)findViewById(R.id.but_done);
        button.setOnClickListener(this);

        // get type
        Intent intent = getIntent();
        if (intent.hasExtra(ADD_TYPE)) {
            mnType = intent.getIntExtra(ADD_TYPE, ADD_POST);
        }

        // image
        CommonUtils.maryBitmap.clear();

        if (mnType == ADD_POST) {
            butBack.setVisibility(View.GONE);
            textTitle.setText("Add Post");
            butPublish.setVisibility(View.VISIBLE);
            layoutSave.setVisibility(View.GONE);
        }
        else {
            butBack.setVisibility(View.VISIBLE);
            textTitle.setText("Edit Post");
            butPublish.setVisibility(View.GONE);
            layoutSave.setVisibility(View.VISIBLE);

            mItem = CommonUtils.mItemSelected;

            mnItemType = mItem.getType();

            mEditTitle.setText(mItem.getTitle());
            mEditDesc.setText(mItem.getDesc());

            // add empty bitmap to array
            for (i = 0; i < IMAGE_ITEM_COUNT; i++) {
                Bitmap bm = Bitmap.createBitmap(1, 1, Bitmap.Config.ALPHA_8);
                CommonUtils.maryBitmap.add(bm);
            }

            for (ImageItemView iView : maryItemView) {
                iView.showCloseButton(true);
                iView.enableCloseButton(false);
            }

            // load images
            for (i = 0; i < mItem.getImages().size(); i++) {
                TargetWithIndex target = new TargetWithIndex(i);
                maryTarget.add(target);
            }

            for (i = 0; i < mItem.getImages().size(); i++) {
                ParseFile fileImage = mItem.getImages().get(i);
                String strUrl = fileImage.getUrl();
                TargetWithIndex target = maryTarget.get(i);

                Picasso.with(this).load(strUrl).into(target);
            }

            // set category index
            for (i = 0; i < CommonUtils.maryCategory.size(); i++) {
                CategoryData cData = CommonUtils.maryCategory.get(i);

                if (cData.getObjectId().equals(mItem.getCategory().getObjectId())) {
                    showCategory(i);
                    break;
                }
            }

            // location
            showLocation(mItem.getAddress());
        }

        updateSegment(mnItemType);
        updateImageViews();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        CommonUtils.maryBitmap.clear();
    }

    private class TargetWithIndex implements Target {
        private int mnIndex;

        public TargetWithIndex(int index) {
            this.mnIndex = index;
        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            CommonUtils.maryBitmap.set(mnIndex, bitmap);
            updateImageViews();

            ImageItemView iView = maryItemView.get(mnIndex);
            iView.enableCloseButton(true);

//            Log.d(TAG, "loaded bitmap: " + mnIndex);
        }

        @Override
        public void onBitmapFailed(Drawable drawable) {

        }

        @Override
        public void onPrepareLoad(Drawable drawable) {
        }
    };

    private void updateSegment(int type) {

        mnItemType = type;

        int nColorGray = getResources().getColor(R.color.color_gray);

        if (mnItemType == ItemData.ITEMTYPE_DEED) {
            mButDeed.setBackgroundResource(R.mipmap.add_deed_active_but_bg);
            mButNeed.setBackgroundResource(R.mipmap.add_need_deactive_but_bg);

            mButDeed.setTextColor(Color.WHITE);
            mButNeed.setTextColor(nColorGray);

            mImgviewDeedMark.setImageResource(R.mipmap.add_deed_mark);
        }
        else {
            mButDeed.setBackgroundResource(R.mipmap.add_deed_deactive_but_bg);
            mButNeed.setBackgroundResource(R.mipmap.add_need_active_but_bg);

            mButNeed.setTextColor(Color.WHITE);
            mButDeed.setTextColor(nColorGray);

            mImgviewDeedMark.setImageResource(R.mipmap.add_need_mark);
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        switch (id) {
            case R.id.but_back:
                onBackPressed();
                break;

            case R.id.but_deed:
                updateSegment(ItemData.ITEMTYPE_DEED);
                break;

            case R.id.but_need:
                updateSegment(ItemData.ITEMTYPE_INNEED);
                break;

            case R.id.imgview_camera:
                onTakePhoto();
                break;

            case R.id.imgview_plus:
                onChoosePhoto();
                break;

            case R.id.but_location:

                // Get the layout inflater
                LayoutInflater inflater = getLayoutInflater();
                View editView = inflater.inflate(R.layout.layout_add_text_dialog, null);
                final EditText editText = (EditText)editView.findViewById(R.id.edit_location);

                AlertDialog dlgLocation = new AlertDialog.Builder(this)
                        .setTitle("Input your address")
                        .setView(editView)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                showLocation(editText.getText().toString());
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .create();

                dlgLocation.show();

                break;

            case R.id.but_category:

                String[] aryCategory = new String[CommonUtils.maryCategory.size()];
                int i = 0;

                for (CategoryData cData : CommonUtils.maryCategory) {
                    aryCategory[i] = cData.getName();
                    i++;
                }

                AlertDialog dlgCategory = new AlertDialog.Builder(this)
                        .setTitle("Select Category")
                        .setItems(aryCategory, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                showCategory(i);
                            }
                        })
                        .create();
                dlgCategory.show();

                break;

            case R.id.but_savepublish:
            case R.id.but_save:
                saveItem();
                break;

            case R.id.but_delete:
                AlertDialog dlgDelete = new AlertDialog.Builder(this)
                        .setTitle("Delete item?")
                        .setIconAttribute(android.R.attr.alertDialogIcon)
                        .setMessage("This will remove item permanently!")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                mProgressDialog = ProgressDialog.show(AddActivity.this, "", "Deleting...");

                                // decrease the count value
                                ParseQuery<GeneralData> query = ParseQuery.getQuery(GeneralData.class);
                                query.getFirstInBackground(new GetCallback<GeneralData>() {
                                    @Override
                                    public void done(GeneralData generalData, ParseException e) {
                                        mProgressDialog.dismiss();

                                        if (mItem.getDone()) {
                                            generalData.increment("itemdone", -1);
                                        }

                                        generalData.increment("itemcount", -1);
                                        generalData.saveInBackground();

                                        mItem.deleteInBackground();
                                        onBackPressed();
                                    }
                                });
                            }
                        })
                        .setNeutralButton(android.R.string.no, null)
                        .create();

                dlgDelete.show();
                break;

            case R.id.but_done:
                if (mItem.getDone()) {
                    return;
                }

                AlertDialog dlgDone = new AlertDialog.Builder(this)
                        .setTitle("Make it done?")
                        .setIconAttribute(android.R.attr.alertDialogIcon)
                        .setMessage("This will mark this item as solved!")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                mItem.setDone(true);
                                mItem.saveInBackground();

                                // increase the count value
                                ParseQuery<GeneralData> query = ParseQuery.getQuery(GeneralData.class);
                                query.getFirstInBackground(new GetCallback<GeneralData>() {
                                    @Override
                                    public void done(GeneralData generalData, ParseException e) {
                                        generalData.increment("itemdone");
                                        generalData.saveInBackground();
                                    }
                                });
                            }
                        })
                        .setNeutralButton(android.R.string.no, null)
                        .create();
                dlgDone.show();

                break;
        }
    }

    private void showLocation(String value) {
        mstrLocation = value;

        if (TextUtils.isEmpty(value)) {
            mTextLocation.setText("City or Area");
        }
        else {
            mTextLocation.setText(value);
        }
    }

    private void showCategory(int index) {
        mnSelectedIndex = index;

        if (mnSelectedIndex >= 0) {
            CategoryData cData = CommonUtils.maryCategory.get(mnSelectedIndex);
            mTextCategory.setText(cData.getName());
        }
        else {
            mTextCategory.setText("Select your category");
        }
    }

    private void saveItem() {
        String strTitle = mEditTitle.getText().toString();
        String strDesc = mEditDesc.getText().toString();

        if (TextUtils.isEmpty(strTitle)) {
            CommonUtils.createErrorAlertDialog(this, "", "Input the title").show();
            return;
        }

        if (TextUtils.isEmpty(strDesc)) {
            CommonUtils.createErrorAlertDialog(this, "", "Input the description").show();
            return;
        }

//        if (CommonUtils.maryBitmap.size() < IMAGE_ITEM_COUNT) {
//            CommonUtils.createErrorAlertDialog(this, "", "You must have 3 pictures").show();
//            return;
//        }

        if (mnSelectedIndex < 0) {
            CommonUtils.createErrorAlertDialog(this, "", "You must select a category").show();
            return;
        }

        final ItemData itemData;

        if (mnType == ADD_POST) {
            itemData = new ItemData();
        }
        else {
            itemData = CommonUtils.mItemSelected;
        }

        mProgressDialog = ProgressDialog.show(this, "", "Uploading...");

        UserData currentUser = (UserData) UserData.getCurrentUser();

        itemData.setUser(currentUser);
        itemData.setUsername(currentUser.getUsernameToShow());
        itemData.setTitle(strTitle);
        itemData.setDesc(strDesc);
        itemData.setType(mnItemType);

        if (!TextUtils.isEmpty(mstrLocation)) {
            itemData.setAddress(mstrLocation);
        }

        if (CommonUtils.mCurrentLocation != null) {
            itemData.setLocation(new ParseGeoPoint(CommonUtils.mCurrentLocation.getLatitude(), CommonUtils.mCurrentLocation.getLongitude()));
        }

        // category
        CategoryData cData = CommonUtils.maryCategory.get(mnSelectedIndex);
        itemData.setCategory(cData);

        mnSavedImgCount = 0;

        if (mbImageModified) {
            itemData.remove("images");
            maryFile.clear();

            for (Bitmap bm : CommonUtils.maryBitmap) {
                final ParseFile fileImage = new ParseFile("image.jpg", CommonUtils.getCompressedBitmap(bm, 320));
                fileImage.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        saveItemToDB(itemData);
                    }
                });

                maryFile.add(fileImage);
            }
        }
        else {
            mnSavedImgCount = IMAGE_ITEM_COUNT;
            saveItemToDB(itemData);
        }
    }

    private void saveItemToDB(ItemData item) {
        mnSavedImgCount++;

        if (mnSavedImgCount < IMAGE_ITEM_COUNT) {
            return;
        }

        for (ParseFile pFile : maryFile) {
            item.add("images", pFile);
        }

        item.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                mProgressDialog.dismiss();

                if (e == null) {
                    if (mnType == ADD_POST) {
                        // clear data & UI
                        mEditTitle.setText("");
                        mEditDesc.setText("");

                        showLocation("");
                        showCategory(-1);

                        CommonUtils.maryBitmap.clear();
                        updateImageViews();

                        CommonUtils.createErrorAlertDialog(AddActivity.this, "", "Your item has been successfully posted").show();

                        // increase the count value
                        ParseQuery<GeneralData> query = ParseQuery.getQuery(GeneralData.class);
                        query.getFirstInBackground(new GetCallback<GeneralData>() {
                            @Override
                            public void done(GeneralData generalData, ParseException e) {
                                generalData.increment("itemcount");
                                generalData.saveInBackground();
                            }
                        });
                    }
                    else {
                        onBackPressed();
                    }

                    if (!mbImageModified) {
                        return;
                    }

                    // add to cache manually
                }
            }
        });
    }

    private void onTakePhoto() {
        if (CommonUtils.maryBitmap.size() >= IMAGE_ITEM_COUNT) {
            return;
        }

        try {
            mFileUri = getOutputMediaFileUri(CommonUtils.MEDIA_TYPE_IMAGE);
            Log.e(TAG, "mFileUri = " + mFileUri.getPath());

            //create new Intent
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mFileUri);

            if (mnType == ADD_POST) {
                CommonUtils.mMainActivity.startActivityForResult(intent, CommonUtils.CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
            }
            else {
                startActivityForResult(intent, CommonUtils.CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
            }
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

    private void onChoosePhoto() {
        if (CommonUtils.maryBitmap.size() >= IMAGE_ITEM_COUNT) {
            return;
        }

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

        if (mnType == ADD_POST) {
            CommonUtils.mMainActivity.startActivityForResult(intent, CommonUtils.GALLERY_OPEN_IMAGE_REQUEST_CODE);
        }
        else {
            startActivityForResult(intent, CommonUtils.GALLERY_OPEN_IMAGE_REQUEST_CODE);
        }
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
                    if (mnType == ADD_POST) {
                        CommonUtils.mMainActivity.startActivityForResult(cropIntent, CommonUtils.CAMERA_CROP_REQUEST_CODE);
                    }
                    else {
                        startActivityForResult(cropIntent, CommonUtils.CAMERA_CROP_REQUEST_CODE);
                    }
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

                    CommonUtils.maryBitmap.add(mBitmapCropped);
                    updateImageViews();

                    mbImageModified = true;
                }
                break;
            }
        }
    }

    private void updateImageViews() {
        // clear imageviews
        for (ImageItemView itemView : maryItemView) {
            itemView.setImage(null);
        }

        int i = 0;
        for (Bitmap bm : CommonUtils.maryBitmap) {
            ImageItemView iView = maryItemView.get(i);
            iView.setImage(bm);

            i++;
        }
    }

    public void onImageItem(int index) {
        if (CommonUtils.maryBitmap.size() <= index) {
            return;
        }

        Intent intent = new Intent(this, FullImageActivity.class);
        intent.putExtra(FullImageActivity.IMAGE_INDEX, index);
        intent.putExtra(FullImageActivity.IMAGE_SOURCE, FullImageActivity.IMAGE_FROM_BITMAP);

        startActivity(intent);
    }

    public void onImageItemClose(int index) {
        CommonUtils.maryBitmap.remove(index);

        mbImageModified = true;

        updateImageViews();
    }
}
