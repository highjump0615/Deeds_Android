package com.iliayugai.deeds.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.iliayugai.deeds.FullImageActivity;
import com.iliayugai.deeds.R;
import com.iliayugai.deeds.utils.CommonUtils;
import com.squareup.picasso.Picasso;

import java.util.List;

import uk.co.senab.photoview.PhotoView;

/**
 * Created by highjump on 15-5-28.
 */
public class FullImageAdapter extends PagerAdapter {

    private List<String> mAryUrl;
    private Context mContext;

    public FullImageAdapter(Context ctx, List<String> aryUrl) {
        mContext = ctx;
        mAryUrl = aryUrl;
    }

    @Override
    public int getCount() {
        int nCount = 0;

        if (mAryUrl != null) {
            nCount = mAryUrl.size();
        }
        else {
            nCount = CommonUtils.maryBitmap.size();
        }

        return nCount;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        PhotoView photoView = new PhotoView(container.getContext());

//        int nColor = mContext.getResources().getColor(R.color.color_img_bg);
//        photoView.setBackgroundColor(nColor);

        if (mAryUrl != null) {
            String strUrl = mAryUrl.get(position);

            Picasso.with(mContext)
                    .load(strUrl)
                    .placeholder(R.mipmap.home_item_default)
                    .error(R.mipmap.home_item_default)
                    .into(photoView);
        }
        else {
            Bitmap bm = CommonUtils.maryBitmap.get(position);
            photoView.setImageBitmap(bm);
        }

        // Now just add PhotoView to ViewPager and return it
        container.addView(photoView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        return photoView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }
}
