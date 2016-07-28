package com.iliayugai.deeds.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.iliayugai.deeds.R;

/**
 * Created by highjump on 15-6-13.
 */
public class ImageItemView extends RelativeLayout {

    public ImageView mImgviewClose;
    public ImageView mImgviewContent;

    public ImageItemView(Context context) {
        super(context);
        init();
    }

    public ImageItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ImageItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ImageItemView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.layout_image_item, this);

        mImgviewClose = (ImageView)findViewById(R.id.imgview_close);
        mImgviewContent = (ImageView)findViewById(R.id.imgview_content);
    }

    public void setImage(Bitmap bitmap) {
        if (bitmap != null) {
            mImgviewContent.setImageBitmap(bitmap);
            mImgviewClose.setVisibility(VISIBLE);
            setVisibility(VISIBLE);
        }
        else {
            mImgviewContent.setImageResource(R.mipmap.home_item_default);
            mImgviewClose.setVisibility(INVISIBLE);
            setVisibility(INVISIBLE);
        }
    }

    public void showCloseButton(boolean show) {
        if (show) {
            mImgviewClose.setVisibility(VISIBLE);
        }
        else {
            mImgviewClose.setVisibility(INVISIBLE);
        }
    }

    public void enableCloseButton(boolean enable) {
        mImgviewClose.setEnabled(enable);
    }
}
