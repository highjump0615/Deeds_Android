package com.iliayugai.deeds.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by highjump on 15-6-3.
 */

@ParseClassName("Category")
public class CategoryData extends ParseObject {

    // get & set
    public String getName() {
        return getString("name");
    }

    public void setName(String value) {
        put("name", value);
    }
}
