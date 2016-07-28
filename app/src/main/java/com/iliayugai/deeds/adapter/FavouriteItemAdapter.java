package com.iliayugai.deeds.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.daimajia.swipe.SimpleSwipeListener;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;
import com.iliayugai.deeds.DetailActivity;
import com.iliayugai.deeds.FavouriteActivity;
import com.iliayugai.deeds.HomeActivity;
import com.iliayugai.deeds.ProfileActivity;
import com.iliayugai.deeds.R;
import com.iliayugai.deeds.model.ItemData;
import com.iliayugai.deeds.model.UserData;
import com.iliayugai.deeds.utils.CommonUtils;
import com.iliayugai.deeds.utils.DeedItemClickListener;
import com.iliayugai.deeds.view.ViewHolderFavouriteItem;
import com.parse.ParseRelation;

import java.util.ArrayList;

/**
 * Created by highjump on 15-6-4.
 */
public class FavouriteItemAdapter extends RecyclerSwipeAdapter<RecyclerView.ViewHolder> implements DeedItemClickListener {

    private static final String TAG = FavouriteItemAdapter.class.getSimpleName();

    private ArrayList<ItemData> maryBlog;
    private Context mContext;

    public FavouriteItemAdapter(Context ctx, ArrayList<ItemData> maryBlog) {
        mContext = ctx;
        this.maryBlog = maryBlog;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_favourite_item, parent, false);
        // set the view's size, margins, paddings and layout parameters

        ViewHolderFavouriteItem vh = new ViewHolderFavouriteItem(v, mContext, ViewHolderFavouriteItem.SWIPE_ITEM_DELETE);
        vh.setOnItemClickListener(this);

        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        ViewHolderFavouriteItem viewHolder = (ViewHolderFavouriteItem)holder;

        ItemData itemData = maryBlog.get(position);
        viewHolder.fillContent(itemData, position);

        mItemManger.bind(viewHolder.itemView, position);
    }

    @Override
    public int getItemCount() {
        return maryBlog.size();
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.swipe;
    }

    @Override
    public void onItemClick(View view, int position) {
        int id = view.getId();

        ItemData itemData = maryBlog.get(position);
        FavouriteActivity activity = (FavouriteActivity) mContext;

        UserData currentUser = (UserData)UserData.getCurrentUser();

        switch (id) {
            case R.id.imgview_photo:
            case R.id.but_user:
                UserData user = itemData.getUser();
                if (currentUser.getObjectId().equals(user.getObjectId())) {
                    return;
                }

                CommonUtils.mUserSelected = user;

                Intent intent = new Intent(activity, ProfileActivity.class);
                intent.putExtra(ProfileActivity.SELECTED_USERNAME, itemData.getUsername());

                activity.startActivity(intent);
                activity.overridePendingTransition(R.anim.anim_in, R.anim.anim_out);

                break;

            case R.id.but_delete:
                notifyItemRemoved(position);
                maryBlog.remove(position);
                closeAllItems(true);

                ParseRelation<ItemData> relation = currentUser.getFavourite();
                relation.remove(itemData);
                currentUser.saveInBackground();

                currentUser.maryFavouriteItem.remove(itemData);

                break;

            default:
                CommonUtils.mItemSelected = itemData;
                CommonUtils.moveNextActivity(activity, DetailActivity.class, false);

                break;
        }
    }
}
