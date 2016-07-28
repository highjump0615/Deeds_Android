package com.iliayugai.deeds.model;

import android.os.Parcelable;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseRelation;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by highjump on 15-6-3.
 */

@ParseClassName("Item")
public class ItemData extends ParseObject {

    public static int ITEMTYPE_DEED = 0;
    public static int ITEMTYPE_INNEED = 1;

    // get & set
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

    public String getTitle() {
        return getString("title");
    }

    public void setTitle(String value) {
        put("title", value);
    }

    public String getDesc() {
        return getString("desc");
    }

    public void setDesc(String value) {
        put("desc", value);
    }

    public String getAddress() {
        return getString("address");
    }

    public void setAddress(String value) {
        put("address", value);
    }

    public CategoryData getCategory() {
        return (CategoryData) get("category");
    }

    public void setCategory(CategoryData value) {
        put("category", value);
    }

    public int getType() {
        return getInt("type");
    }

    public void setType(int value) {
        put("type", value);
    }

    public ParseGeoPoint getLocation() {
        return getParseGeoPoint("location");
    }

    public void setLocation(ParseGeoPoint value) {
        put("location", value);
    }

    public ParseRelation getCommentobject() {
        return getRelation("commentobject");
    }

    public boolean getDone() {
        return getBoolean("done");
    }

    public void setDone(boolean value) {
        put("done", value);
    }

    public ArrayList<ParseFile> getImages() {
        return (ArrayList<ParseFile>) get("images");
    }
}
