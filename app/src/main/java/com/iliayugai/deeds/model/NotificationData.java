package com.iliayugai.deeds.model;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseRelation;

import java.util.ArrayList;

/**
 * Created by highjump on 15-6-3.
 */

@ParseClassName("Notification")
public class NotificationData extends ParseObject {

    // notification type
    public static int NOTIFICATION_COMMENT = 0;
    public static int NOTIFICATION_FAVOURITE = 1;
    public static int NOTIFICATION_FOLLOW = 2;
    public static int NOTIFICATION_RATE = 3;
    public static int NOTIFICATION_MENTION = 4;
    public static int NOTIFICATION_POST = 5;

    // rate type
    public static int RATE_NORMAL = 0;
    public static int RATE_GOOD = 1;
    public static int RATE_AMAZING = 2;

    public boolean mbExpanded = false;

    // get & set
    public int getType() {
        return getInt("type");
    }

    public void setType(int value) {
        put("type", value);
    }

    public ItemData getItem() {
        return (ItemData) get("item");
    }

    public void setItem(ItemData value) {
        put("item", value);
    }

    public UserData getUser() {
        return (UserData) get("user");
    }

    public void setUser(UserData value) {
        put("user", value);
    }

    public String getUsername() {
        return getString("username");
    }

    public void setUsername(String value) {
        put("username", value);
    }

    public UserData getTargetUser() {
        return (UserData) get("targetuser");
    }

    public void setTargetUser(UserData value) {
        put("targetuser", value);
    }

    public String getComment() {
        return getString("comment");
    }

    public void setComment(String value) {
        put("comment", value);
    }

    public int getRate() {
        return getInt("rate");
    }

    public void setRate(int value) {
        put("rate", value);
    }
}
