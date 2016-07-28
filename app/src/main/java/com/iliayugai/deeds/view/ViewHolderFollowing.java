package com.iliayugai.deeds.view;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.iliayugai.deeds.R;
import com.iliayugai.deeds.model.UserData;
import com.iliayugai.deeds.utils.CommonUtils;
import com.makeramen.roundedimageview.RoundedImageView;
import com.parse.ParseFile;
import com.squareup.picasso.Picasso;

public class ViewHolderFollowing extends ViewHolderFollow {

    private RoundedImageView mImgPhoto;
    private TextView mTextUsername;
    private TextView mTextDist;

    public ViewHolderFollowing(Context ctx, View itemView) {
        super(ctx, itemView);

        mImgPhoto = (RoundedImageView)itemView.findViewById(R.id.imgview_photo);
        mTextUsername = (TextView)itemView.findViewById(R.id.text_username);
        mTextDist = (TextView)itemView.findViewById(R.id.text_distance);
    }

    public void fillContent(UserData data, boolean showFollow) {
        data.showPhoto(mContext, mImgPhoto);

        mTextUsername.setText(data.getUsernameToShow());

        String strInfo;
        double dDist = CommonUtils.getDistanceFromPoint(data.getLocation());
        if (dDist < 0) {
            strInfo = "Unknown";
        }
        else {
            strInfo = String.format("%.0fKM Away", dDist);
        }

        mTextDist.setText(strInfo);

        updateFollowButton(data);

        if (showFollow) {
            mButFollow.setVisibility(View.VISIBLE);
        }
        else {
            mButFollow.setVisibility(View.GONE);
        }
    }
}