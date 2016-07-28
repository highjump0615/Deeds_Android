package com.iliayugai.deeds.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by highjump on 15-6-3.
 */

@ParseClassName("General")
public class GeneralData extends ParseObject {

    // get & set
    public int getItemcount() {
        return getInt("itemcount");
    }

    public void setItemcount(int value) {
        put("itemcount", value);
    }

    public int getItemdone() {
        return getInt("itemdone");
    }

    public void setItemdone(int value) {
        put("itemdone", value);
    }
}
