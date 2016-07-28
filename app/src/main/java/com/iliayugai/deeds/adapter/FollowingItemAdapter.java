package com.iliayugai.deeds.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;
import com.iliayugai.deeds.FollowingActivity;
import com.iliayugai.deeds.R;
import com.iliayugai.deeds.model.UserData;
import com.iliayugai.deeds.utils.CommonUtils;
import com.iliayugai.deeds.view.ViewHolderBase;
import com.iliayugai.deeds.view.ViewHolderFollow;
import com.iliayugai.deeds.view.ViewHolderFollowing;
import com.makeramen.roundedimageview.RoundedImageView;
import com.parse.ParseFile;
import com.squareup.picasso.Picasso;

/**
 * Created by highjump on 15-6-10.
 */
public class FollowingItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;

    private UserData mUser;
    private int mnType;

    public FollowingItemAdapter(Context ctx, UserData user, int type) {
        mContext = ctx;

        this.mUser = user;
        this.mnType = type;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_follower_item, parent, false);

        ViewHolderFollowing vh = new ViewHolderFollowing(mContext, v);
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ViewHolderFollowing viewHolder = (ViewHolderFollowing)holder;

        UserData uData;

        if (mnType == FollowingActivity.FOLLOW_FOLLOWER) {
            uData = mUser.maryFollowerUser.get(position);
        }
        else {
            uData = mUser.maryFollowingUser.get(position);
        }

        viewHolder.fillContent(uData, true);
    }

    @Override
    public int getItemCount() {
        int nCount = 0;

        if (mnType == FollowingActivity.FOLLOW_FOLLOWER) {
            nCount = mUser.maryFollowerUser.size();
        }
        else {
            nCount = mUser.maryFollowingUser.size();
        }

        return nCount;
    }


}
