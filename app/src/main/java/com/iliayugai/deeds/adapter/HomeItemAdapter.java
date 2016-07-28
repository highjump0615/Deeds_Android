package com.iliayugai.deeds.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.iliayugai.deeds.DetailActivity;
import com.iliayugai.deeds.HomeActivity;
import com.iliayugai.deeds.ProfileActivity;
import com.iliayugai.deeds.R;
import com.iliayugai.deeds.model.ItemData;
import com.iliayugai.deeds.model.UserData;
import com.iliayugai.deeds.utils.CommonUtils;
import com.iliayugai.deeds.utils.DeedItemClickListener;
import com.iliayugai.deeds.view.ViewHolderHomeItem;
import com.iliayugai.deeds.view.ViewHolderLoading;
import java.util.ArrayList;

/**
 * Created by highjump on 15-6-4.
 */
public class HomeItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements DeedItemClickListener {

    private static final String TAG = HomeItemAdapter.class.getSimpleName();

    private ArrayList<ItemData> maryBlog;
    public boolean mbNeedMore = false;

    private int ITEM_VIEW_TYPE_ITEM = 0;
    private int ITEM_VIEW_TYPE_FOOTER = 1;

    private Context mContext;

    public HomeItemAdapter(Context ctx, ArrayList<ItemData> maryBlog) {
        mContext = ctx;
        this.maryBlog = maryBlog;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder vhRes = null;

        if (viewType == ITEM_VIEW_TYPE_ITEM) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_home_item, parent, false);
            // set the view's size, margins, paddings and layout parameters

            ViewHolderHomeItem vh = new ViewHolderHomeItem(v, mContext);
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

        if (holder instanceof ViewHolderHomeItem) {

            ViewHolderHomeItem viewHolder = (ViewHolderHomeItem)holder;

            ItemData itemData = maryBlog.get(position);
            viewHolder.fillContent(itemData, position);
        }
        else {
//            Log.d(TAG, "Footer is shown: ");

            HomeActivity activity = (HomeActivity)mContext;
            activity.getBlog(false);
        }
    }

    @Override
    public int getItemCount() {
        int nCount = maryBlog.size();

        if (mbNeedMore) {
            nCount++;
        }

        return nCount;
    }

    @Override
    public int getItemViewType(int position) {

        int nViewType = ITEM_VIEW_TYPE_FOOTER;

        if (position < maryBlog.size()) {
            return ITEM_VIEW_TYPE_ITEM;
        }

        return nViewType;
    }

    @Override
    public void onItemClick(View view, int position) {
        int id = view.getId();

        ItemData itemData = maryBlog.get(position);
        HomeActivity homeActivity = (HomeActivity) mContext;

        switch (id) {
            case R.id.imgview_photo:
            case R.id.but_user:
                UserData currentUser = (UserData)UserData.getCurrentUser();
                if (currentUser.getObjectId().equals(itemData.getUser().getObjectId())) {
                    return;
                }

                CommonUtils.mUserSelected = itemData.getUser();

                Intent intent = new Intent(homeActivity, ProfileActivity.class);
                intent.putExtra(ProfileActivity.SELECTED_USERNAME, itemData.getUsername());

                homeActivity.startActivity(intent);
                homeActivity.overridePendingTransition(R.anim.anim_in, R.anim.anim_out);

                break;

            default:
                CommonUtils.mItemSelected = itemData;
                CommonUtils.moveNextActivity(homeActivity, DetailActivity.class, false);

                break;
        }
    }
}
