package com.iliayugai.deeds;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.iliayugai.deeds.adapter.FullImageAdapter;
import com.iliayugai.deeds.utils.CommonUtils;
import com.parse.ParseFile;

import java.util.ArrayList;

/**
 * Created by highjump on 15-6-13.
 */
public class FullImageActivity extends Activity {

    public static String IMAGE_INDEX = "image_index";
    public static String IMAGE_SOURCE = "image_source";

    public static int IMAGE_FROM_ITEM = 0;
    public static int IMAGE_FROM_BITMAP = 1;
    private int mnSource = IMAGE_FROM_ITEM;

    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullimage);

        // get index
        Intent intent = getIntent();
        int nIndex = 0;
        if (intent.hasExtra(IMAGE_INDEX)) {
            nIndex = intent.getIntExtra(IMAGE_INDEX, 0);
        }

        if (intent.hasExtra(IMAGE_SOURCE)) {
            mnSource = intent.getIntExtra(IMAGE_SOURCE, IMAGE_FROM_ITEM);
        }

        mViewPager = (ViewPager) findViewById(R.id.viewpager);

        if (mnSource == IMAGE_FROM_ITEM) {
            ArrayList<String> aryUrl = new ArrayList<String>();

            for (ParseFile pFile : CommonUtils.mItemSelected.getImages()) {
                String strUrl = pFile.getUrl();
                aryUrl.add(strUrl);
            }

            mViewPager.setAdapter(new FullImageAdapter(this, aryUrl));
        }
        else {
            mViewPager.setAdapter(new FullImageAdapter(this, null));
        }

        mViewPager.setCurrentItem(nIndex);
    }
}
