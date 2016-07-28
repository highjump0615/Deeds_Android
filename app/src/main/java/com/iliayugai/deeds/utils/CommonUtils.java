package com.iliayugai.deeds.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;

import com.facebook.AccessToken;
import com.facebook.Profile;
import com.iliayugai.deeds.LandingActivity;
import com.iliayugai.deeds.MainActivity;
import com.iliayugai.deeds.R;
import com.iliayugai.deeds.model.CategoryData;
import com.iliayugai.deeds.model.ItemData;
import com.iliayugai.deeds.model.UserData;
import com.parse.ParseGeoPoint;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by highjump on 15-5-26.
 */
public class CommonUtils {

    private static final String TAG = CommonUtils.class.getSimpleName();

    public static MainActivity mMainActivity;

    public static final int MEDIA_TYPE_IMAGE = 0;
    public static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    public static final int CAMERA_CROP_REQUEST_CODE = 101;
    public static final int GALLERY_OPEN_IMAGE_REQUEST_CODE = 102;

    public static Location mCurrentLocation = null;

    public static Profile mFBCurProfile = null;
    public static AccessToken mFBCurToken = null;

    public static int mnszProfilePhoto = 116;

    // param for filter
    public static ArrayList<CategoryData> maryCategory = new ArrayList<CategoryData>();
    public static CategoryData mCategorySelected = null;
    public static boolean mbFilterDeed = true;
    public static boolean mbFilterNeed = true;
    public static int mnFilterDistance = 500;

    public static int mnItemcount = 0;
    public static int mnItemdone = 0;

    // selected objects
    public static ItemData mItemSelected;
    public static UserData mUserSelected;

    public static ArrayList<Bitmap> maryBitmap = new ArrayList<Bitmap>();

    /**********************************************************************************************/
    /*                                  Related User Interface                                    */
    /**********************************************************************************************/

    /**
     * Move to destination activity class with animate transition.
     */
    public static void moveNextActivity(Activity source, Class<?> destinationClass, boolean removeSource) {
        Intent intent = new Intent(source, destinationClass);

        if (removeSource) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        source.startActivity(intent);

        if (removeSource) {
            source.finish();
        }

        source.overridePendingTransition(R.anim.anim_in, R.anim.anim_out);
    }

    /**
     * Create error AlertDialog.
     */
    public static Dialog createErrorAlertDialog(final Context context, String title, String message) {
        return new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null).create();
    }

    /*
     * returning photoImage / video
     */
    public static File getOutputMediaFile(Context context, boolean isImageType) {
        File mediaStorageDir = getMyApplicationDirectory(context, Config.IMAGE_DIRECTORY_NAME);

        if (mediaStorageDir == null) return null;

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date());
        File mediaFile;

        if (isImageType) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator /*+ "IMG_"*/ + timeStamp + ".jpg");
        } else {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator /*+ "VID_"*/ + timeStamp + ".3gp");
        }

        return mediaFile;
    }

    /**
     * Get path of images to be saved
     */
    public static File getMyApplicationDirectory(Context context, String directoryName) {
        String appName = context.getString(R.string.app_name);

        // External sdcard location
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), appName);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
//                Log.d(TAG, "Oops! Failed create " + appName + " directory");
                return null;
            }
        }

        mediaStorageDir = new File(Environment.getExternalStorageDirectory() + File.separator + appName, directoryName);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
//                Log.d(TAG, "Oops! Failed create " + directoryName + " directory");
                return null;
            }
        }

        return mediaStorageDir;
    }

    /**
     * Get bitmap from internal image file.
     */
    public static Bitmap getBitmapFromUri(Uri fileUri, int sampleSize, float imgSize) {
//        // bitmap factory
//        BitmapFactory.Options options = new BitmapFactory.Options();
//
//        // downsizing photoImage as it throws OutOfMemory Exception for larger
//        // images
//        options.inSampleSize = sampleSize;
//        options.inMutable = true;
//
//        return BitmapFactory.decodeFile(fileUri.getPath(), options);

        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        // 开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(fileUri.getPath(), newOpts);// 此时返回bm为空

        if (bitmap != null)
            bitmap.recycle();

        newOpts.inJustDecodeBounds = false;
        newOpts.inPurgeable = true;
        newOpts.inInputShareable = true;

        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        // 现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
        float hh = imgSize;// 这里设置高度为800f
        float ww = imgSize;// 这里设置宽度为480f
        // 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;// be=1表示不缩放
        if (w >= h && w > ww)
        {// 如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        }
        else if (w < h && h > hh)
        {// 如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;// 设置缩放比例
        // 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        bitmap = BitmapFactory.decodeFile(fileUri.getPath(), newOpts);

        return bitmap;
    }

    public static byte[] getCompressedBitmap(Bitmap bm, int imgSize) {

        int nWidth = bm.getWidth();
        int nHeight = bm.getHeight();

        float fScaleWidth = (float)imgSize / nWidth;
        float fScaleHeight = (float)imgSize / nHeight;

        Matrix matrix = new Matrix();
        matrix.postScale(fScaleWidth, fScaleHeight);

        Bitmap bmNew = Bitmap.createBitmap(bm, 0, 0, nWidth, nHeight, matrix, true);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmNew.compress(Bitmap.CompressFormat.JPEG, 100, stream);

        return stream.toByteArray();
    }

    public static Bitmap getCroppedBitmap(Bitmap bmp) {

        Bitmap output = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xffa19774;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bmp.getWidth(), bmp.getHeight());

        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(Color.parseColor("#BAB399"));
        canvas.drawCircle(bmp.getWidth() / 2+0.7f, bmp.getHeight() / 2+0.7f, bmp.getWidth() / 2+0.1f, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bmp, rect, rect, paint);

        return output;
    }

    public static double getDistanceFromPoint(ParseGeoPoint point) {
        ParseGeoPoint ptUser;
        UserData currentUser = (UserData) UserData.getCurrentUser();

        if (mCurrentLocation != null) {
            ptUser = new ParseGeoPoint(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        }
        else {
            ptUser = currentUser.getLocation();
        }

        if (ptUser == null || ptUser.getLatitude() == 0 || ptUser.getLongitude() == 0) {
            return -1;
        }

        if (point == null || point.getLatitude() == 0 || point.getLongitude() == 0) {
            return -1;
        }

        double dDistance = ptUser.distanceInKilometersTo(point);

        return dDistance;
    }

    public static String getTimeString(Date date) {

        Date dateShow = date;
        if (dateShow == null) {
            dateShow = new Date();
        }

        Calendar thatDay = Calendar.getInstance();
        thatDay.setTime(dateShow);

        Calendar today = Calendar.getInstance();

        long diff = today.getTimeInMillis() - thatDay.getTimeInMillis();

        long second = diff / 1000;
        int min = (int)second / 60;
        int hour = min / 60;
        int day = hour / 24;
        int month = day / 30;
        int year = month / 23;

        if (min < 0) {
            return "";
        }
        else if (min <= 1) {
            return String.format("%d Minute Ago", min);
        }
        else if (min < 60) {
            return String.format("%d Minutes Ago", min);
        }
        else if (min >= 60 && min < 60 * 24) {
            if (hour == 1) {
                return String.format("%d Hour Ago", hour);
            }
            else {
                return String.format("%d Hours Ago", hour);
            }
        }
        else if (day < 31) {
            if (day == 1) {
                return String.format("%d Day Ago", day);
            }
            else {
                return String.format("%d Days Ago", day);
            }
        }
        else if (month < 12) {
            if (month == 1) {
                return String.format("%d Month Ago", month);
            }
            else {
                return String.format("%d Months Ago", month);
            }
        }
        else {
            if (year == 1) {
                return String.format("%d Year Ago", year);
            }
            else {
                return String.format("%d Years Ago", year);
            }
        }
    }

    /**
     * Convert dip to pixels
     */
    public static int dipToPixels(Context context, int dip) {
        /*DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;*/
        Resources r = context.getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, r.getDisplayMetrics());
        return (int) px;
    }

    public static int dp2px(Context context, float dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    public static int pixelToDip(Context context, int px) {
        /*Resources r = context.getResources();
        float dip = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, px, r.getDisplayMetrics());
        return (int) dip;*/
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }
}
