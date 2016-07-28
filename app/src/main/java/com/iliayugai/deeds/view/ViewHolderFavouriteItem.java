package com.iliayugai.deeds.view;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.daimajia.swipe.SwipeLayout;
import com.iliayugai.deeds.R;
import com.iliayugai.deeds.model.ItemData;
import com.iliayugai.deeds.model.UserData;

/**
 * Created by highjump on 15-6-5.
 */
public class ViewHolderFavouriteItem extends ViewHolderItem {

    public static int SWIPE_ITEM_NONE = 0;
    public static int SWIPE_ITEM_DELETE = 1;
    public static int SWIPE_ITEM_EDIT = 2;

    public SwipeLayout mSwipeLayout;

    private Button mButDelete;
    private ImageView mImgviewEdit;

    public ViewHolderFavouriteItem(View itemView, Context ctx, int swipeType) {
        super(itemView, ctx);

        mSwipeLayout = (SwipeLayout) itemView.findViewById(R.id.swipe);

        LinearLayout layoutSurface = (LinearLayout)itemView.findViewById(R.id.layout_surface);
        layoutSurface.setOnClickListener(this);

        mButDelete = (Button)itemView.findViewById(R.id.but_delete);
        mButDelete.setOnClickListener(this);

        mImgviewEdit = (ImageView)itemView.findViewById(R.id.imgview_edit);
        mImgviewEdit.setOnClickListener(this);

        if (swipeType == SWIPE_ITEM_NONE) {
            mSwipeLayout.setSwipeEnabled(false);
        }
        else if (swipeType == SWIPE_ITEM_DELETE) {
            mImgviewEdit.setVisibility(View.GONE);
        }
        else if (swipeType == SWIPE_ITEM_EDIT) {
            mButDelete.setVisibility(View.GONE);
        }
    }
}
