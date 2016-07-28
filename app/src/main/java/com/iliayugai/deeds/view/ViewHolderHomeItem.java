package com.iliayugai.deeds.view;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.iliayugai.deeds.R;
import com.iliayugai.deeds.model.ItemData;
import com.iliayugai.deeds.model.UserData;

/**
 * Created by highjump on 15-6-5.
 */
public class ViewHolderHomeItem extends ViewHolderItem {

    private TextView mTextLocation;
    private ImageView mImgViewFavourite;

    public ViewHolderHomeItem(View itemView, Context ctx) {
        super(itemView, ctx);

        mTextLocation = (TextView) itemView.findViewById(R.id.text_location);
        mImgViewFavourite = (ImageView) itemView.findViewById(R.id.imgview_fav);
    }

    public void fillContent(ItemData data, int index) {

        super.fillContent(data, index);

        // address
        String strAddress = data.getAddress();
        if (TextUtils.isEmpty(strAddress)) {
            strAddress = "Unknown Location";
        }
        mTextLocation.setText(strAddress);

        // favourite
        UserData currentUser = (UserData) UserData.getCurrentUser();
        if (currentUser.isItemFavourite(data)) {
            mImgViewFavourite.setVisibility(View.VISIBLE);
        }
        else {
            mImgViewFavourite.setVisibility(View.GONE);
        }

    }

}
