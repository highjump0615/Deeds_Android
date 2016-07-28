package com.iliayugai.deeds.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.iliayugai.deeds.MainActivity;
import com.iliayugai.deeds.ProfileActivity;
import com.iliayugai.deeds.R;
import com.iliayugai.deeds.model.UserData;
import com.iliayugai.deeds.utils.CommonUtils;
import com.iliayugai.deeds.utils.DeedItemClickListener;
import com.iliayugai.deeds.view.ViewHolderFollowing;
import com.iliayugai.deeds.view.ViewHolderLoading;

import java.util.ArrayList;

/**
 * Created by highjump on 15-6-12.
 */
public class UserItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements DeedItemClickListener {

    private static final String TAG = UserItemAdapter.class.getSimpleName();

    private ArrayList<UserData> maryUser;
    private Context mContext;

    public boolean mbNeedMore = false;

    private int ITEM_VIEW_TYPE_ITEM = 0;
    private int ITEM_VIEW_TYPE_FOOTER = 1;

    public UserItemAdapter(ArrayList<UserData> aryUser, Context ctx) {
        this.maryUser = aryUser;
        this.mContext = ctx;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder vhRes = null;

        if (viewType == ITEM_VIEW_TYPE_ITEM) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_follower_item, parent, false);

            ViewHolderFollowing vh = new ViewHolderFollowing(mContext, v);
            vh.setOnItemClickListener(this);
            vhRes = vh;
        }
        else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_list_loading, parent, false);

            ViewHolderLoading vh = new ViewHolderLoading(v);
            vhRes = vh;
        }

        return vhRes;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof ViewHolderFollowing) {
            ViewHolderFollowing viewHolder = (ViewHolderFollowing)holder;

            UserData uData = maryUser.get(position);
            viewHolder.fillContent(uData, false);
        }
        else {
//            Log.d(TAG, "Footer is shown: ");

            MainActivity activity = (MainActivity)mContext;
            activity.onButSearch(false, false);
        }
    }

    @Override
    public int getItemCount() {
        int nCount = maryUser.size();

        if (mbNeedMore) {
            nCount++;
        }

        return nCount;
    }

    @Override
    public int getItemViewType(int position) {

        int nViewType = ITEM_VIEW_TYPE_FOOTER;

        if (position < maryUser.size()) {
            return ITEM_VIEW_TYPE_ITEM;
        }

        return nViewType;
    }

    @Override
    public void onItemClick(View view, int position) {
        MainActivity activity = (MainActivity)mContext;

        UserData uData = maryUser.get(position);
        UserData currentUser = (UserData) UserData.getCurrentUser();

        if (uData.getObjectId().equals(currentUser.getObjectId())) {
            return;
        }

        CommonUtils.mUserSelected = uData;
        CommonUtils.moveNextActivity(activity, ProfileActivity.class, false);
    }
}
