package com.iliayugai.deeds.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import com.iliayugai.deeds.R;
import com.iliayugai.deeds.utils.DeedCallbackInterface;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.ParseClassName;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Created by highjump on 15-6-2.
 */

@ParseClassName("_User")
public class UserData extends ParseUser {

    private static final String TAG = UserData.class.getSimpleName();

    public ArrayList<ItemData> maryFavouriteItem = new ArrayList<ItemData>();
    public ArrayList<UserData> maryFollowingUser = new ArrayList<UserData>();
    public ArrayList<UserData> maryFollowerUser = new ArrayList<UserData>();
    public ArrayList<NotificationData> maryReview = new ArrayList<NotificationData>();

    public int mnReviewCount = 0;

    // get & set
    public String getFirstname() {
        return getString("firstname");
    }

    public void setFirstname(String value) {
        put("firstname", value);
    }

    public String getLastname() {
        return getString("lastname");
    }

    public void setLastname(String value) {
        put("lastname", value);
    }

    public void setFullname() {
        String strFullname = getUsernameToShow();
        put("fullname", strFullname);
    }

    public String getPhone() {
        return getString("phone");
    }

    public void setPhone(String value) {
        put("phone", value);
    }

    public String getAddress() {
        return getString("address");
    }

    public void setAddress(String value) {
        put("address", value);
    }

    public String getAbout() {
        return getString("about");
    }

    public void setAbout(String value) {
        put("about", value);
    }

    public ParseFile getPhoto() {
        return getParseFile("photo");
    }

    public void setPhoto(ParseFile value) {
        put("photo", value);
    }

    public ParseGeoPoint getLocation() {
        return getParseGeoPoint("location");
    }

    public void setLocation(ParseGeoPoint value) {
        put("location", value);
    }

    public ParseRelation getFavourite() {
        return getRelation("favourite");
    }

    public ParseRelation getFollowing() {
        return getRelation("following");
    }

    public String getUsernameToShow() {
        String strFirstname = getFirstname();
        String strLastname = getLastname();
        String strName = "";

        if (!TextUtils.isEmpty(strFirstname) || !TextUtils.isEmpty(strLastname)) {
            strName = strFirstname + " " + strLastname;
        }

        return strName;
    }

    public void getFavouriteItem(final DeedCallbackInterface callback) {
        ParseRelation<ItemData> relation = getRelation("favourite");
        ParseQuery<ItemData> query = relation.getQuery();

        query.findInBackground(new FindCallback<ItemData>() {
            @Override
            public void done(List<ItemData> list, ParseException e) {
                if (e == null) {
                    maryFavouriteItem.clear();

                    for (ItemData itemData : list) {
                        maryFavouriteItem.add(itemData);
                    }
                }

                if (callback != null) {
                    callback.onSuccess();
                }
            }
        });

        getFollowingUser(callback);
    }

    public boolean isItemFavourite(ItemData data) {
        boolean bExisting = false;

        for (ItemData iData : maryFavouriteItem) {
            if (iData.getObjectId().equals(data.getObjectId())) {
                bExisting = true;
                break;
            }
        }

        return bExisting;
    }

    public void getFollowingUser(final DeedCallbackInterface callback) {
        ParseRelation<UserData> relation = getRelation("following");
        ParseQuery<UserData> query = relation.getQuery();

        query.findInBackground(new FindCallback<UserData>() {
            @Override
            public void done(List<UserData> list, ParseException e) {
                if (e == null) {
                    maryFollowingUser.clear();

                    for (UserData uData : list) {
                        maryFollowingUser.add(uData);
                    }
                }

                if (callback != null) {
                    callback.onSuccess();
                }
            }
        });
    }

    public void getFollowerUser(final DeedCallbackInterface callback) {
        ParseRelation<UserData> relation = getRelation("follower");
        ParseQuery<UserData> query = relation.getQuery();

        query.findInBackground(new FindCallback<UserData>() {
            @Override
            public void done(List<UserData> list, ParseException e) {
                if (e == null) {
                    maryFollowerUser.clear();

                    for (UserData uData : list) {
                        maryFollowerUser.add(uData);
                    }
                }

                if (callback != null) {
                    callback.onSuccess();
                }
            }
        });
    }

    public boolean isUserFollowing(UserData data) {
        boolean bExist = false;

        for (UserData uData : maryFollowingUser) {
            if (uData.getObjectId().equals(data.getObjectId())) {
                bExist = true;
                break;
            }
        }

        return bExist;
    }

    public void showPhoto(Context ctx, ImageView imgView) {
        ParseFile filePhoto = getPhoto();

        if (filePhoto != null) {
            Picasso.with(ctx)
                    .load(filePhoto.getUrl())
                    .placeholder(R.mipmap.user_default)
                    .error(R.mipmap.user_default)
                    .into(imgView);
        }
        else {
            imgView.setImageResource(R.mipmap.user_default);
        }
    }

    public void doFollow(UserData user) {
        if (getObjectId().equals(user.getObjectId())) {
            return;
        }

        if (isUserFollowing(user)) {
            return;
        }

        ParseRelation<UserData> relation = getFollowing();
        relation.add(user);
        saveInBackground();

        Map mapParam = new HashMap();
        mapParam.put("userId", user.getObjectId());

        ParseCloud.callFunctionInBackground("addMeAsFollower", mapParam, new FunctionCallback<Object>() {
            @Override
            public void done(Object o, ParseException e) {
                if (e != null) {
//                    Log.d(TAG, e.getLocalizedMessage());
                }
            }
        });

        // save to notification data
        NotificationData notifyObj = new NotificationData();
        notifyObj.setUser(this);
        notifyObj.setUsername(getUsernameToShow());
        notifyObj.setTargetUser(user);
        notifyObj.setType(NotificationData.NOTIFICATION_FOLLOW);
        notifyObj.saveInBackground();

        maryFollowingUser.add(user);

        // push notification
        ParseQuery query = ParseInstallation.getQuery();
        query.whereEqualTo("user", user);

        try {
            ParsePush push = new ParsePush();
            push.setQuery(query);
            JSONObject data = new JSONObject("{\"alert\": \"" + getUsernameToShow() + " thinks you are a great person\"," +
                    "\"notifyType\": \"follow\"," +
                    "\"notifyItem\":" + user.getObjectId() + "," +
                    "\"badge\": \"Increment\"," +
                    "\"sound\": \"cheering.caf\"}");
            push.setData(data);
            push.sendInBackground();
        } catch (JSONException e1) {
            e1.printStackTrace();
        }
    }

    public void doUnFollow(UserData user) {
        if (getObjectId().equals(user.getObjectId())) {
            return;
        }

        if (!isUserFollowing(user)) {
            return;
        }

        ParseRelation<UserData> relation = getFollowing();
        relation.remove(user);
        saveInBackground();

        Map mapParam = new HashMap();
        mapParam.put("userId", user.getObjectId());

        ParseCloud.callFunctionInBackground("removeMeAsFollower", mapParam, new FunctionCallback<Object>() {
            @Override
            public void done(Object o, ParseException e) {
                if (e != null) {
//                    Log.d(TAG, e.getLocalizedMessage());
                }
            }
        });

        for (UserData uData : maryFollowingUser) {
            if (uData.getObjectId().equals(user.getObjectId())) {
                maryFollowingUser.remove(uData);
                break;
            }
        }
    }
}
