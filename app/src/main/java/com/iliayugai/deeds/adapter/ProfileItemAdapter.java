package com.iliayugai.deeds.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;
import com.iliayugai.deeds.AddActivity;
import com.iliayugai.deeds.DetailActivity;
import com.iliayugai.deeds.FollowingActivity;
import com.iliayugai.deeds.ProfileActivity;
import com.iliayugai.deeds.R;
import com.iliayugai.deeds.ReviewActivity;
import com.iliayugai.deeds.model.ItemData;
import com.iliayugai.deeds.model.UserData;
import com.iliayugai.deeds.utils.CommonUtils;
import com.iliayugai.deeds.utils.DeedItemClickListener;
import com.iliayugai.deeds.view.ViewHolderFavouriteItem;
import com.iliayugai.deeds.view.ViewHolderFollow;
import com.iliayugai.deeds.view.ViewHolderLoading;
import com.makeramen.roundedimageview.RoundedImageView;
import com.parse.ParseFile;
import com.squareup.picasso.Picasso;
import com.viewpagerindicator.CirclePageIndicator;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by highjump on 15-6-4.
 */
public class ProfileItemAdapter extends RecyclerSwipeAdapter<RecyclerView.ViewHolder> implements DeedItemClickListener {

    private static final String TAG = ProfileItemAdapter.class.getSimpleName();

    private ArrayList<ItemData> maryBlog;

    public boolean mbNeedMore = false;
    private UserData mUserData;
    private String mStrUsername;

    private int ITEM_VIEW_TYPE_INFO = 0;
    private int ITEM_VIEW_TYPE_ITEM = 1;
    private int ITEM_VIEW_TYPE_FOOTER = 2;

    private Context mContext;

    public ProfileItemAdapter(Context ctx, ArrayList<ItemData> aryBlog, UserData user, String username) {
        mContext = ctx;
        this.maryBlog = aryBlog;

        mUserData = user;
        mStrUsername = username;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder vhRes = null;

        if (viewType == ITEM_VIEW_TYPE_INFO) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_profile_info_item, parent, false);

            ViewHolderProfileInfo vh = new ViewHolderProfileInfo(mContext, v);
            vhRes = vh;
        }
        else if (viewType == ITEM_VIEW_TYPE_ITEM) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_favourite_item, parent, false);

            UserData currentUser = (UserData) UserData.getCurrentUser();
            ViewHolderFavouriteItem vh = null;

            if (mUserData.getObjectId().equals(currentUser.getObjectId())) {
                vh = new ViewHolderFavouriteItem(v, mContext, ViewHolderFavouriteItem.SWIPE_ITEM_EDIT);
            }
            else {
                vh = new ViewHolderFavouriteItem(v, mContext, ViewHolderFavouriteItem.SWIPE_ITEM_NONE);
            }

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

        if (holder instanceof ViewHolderProfileInfo) {
            ViewHolderProfileInfo viewHolder = (ViewHolderProfileInfo)holder;
            viewHolder.fillContent(mUserData, mStrUsername);
        }
        else if (holder instanceof ViewHolderFavouriteItem) {
            ViewHolderFavouriteItem viewHolder = (ViewHolderFavouriteItem)holder;

            ItemData itemData = maryBlog.get(position - 1);
            viewHolder.fillContent(itemData, position);

            mItemManger.bind(viewHolder.itemView, position);
        }
        else if (holder instanceof ViewHolderLoading) {
//            Log.d(TAG, "Footer is shown: ");

            ProfileActivity activity = (ProfileActivity)mContext;
            activity.getBlog(false);
        }
    }

    @Override
    public int getItemCount() {
        int nCount = maryBlog.size();

        nCount += 1;

        if (mbNeedMore) {
            nCount++;
        }

        return nCount;
    }

    @Override
    public int getItemViewType(int position) {

        int nViewType = ITEM_VIEW_TYPE_FOOTER;

        if (position == 0) {
            return ITEM_VIEW_TYPE_INFO;
        }
        else if (position < maryBlog.size() + 1) {
            return ITEM_VIEW_TYPE_ITEM;
        }

        return nViewType;
    }

    @Override
    public void onItemClick(View view, final int position) {
        int id = view.getId();

        ItemData itemData = maryBlog.get(position - 1);

        ProfileActivity activity = (ProfileActivity) mContext;
        CommonUtils.mItemSelected = itemData;

        switch (id) {
            case R.id.imgview_edit:
                Intent intent = new Intent(activity, AddActivity.class);
                intent.putExtra(AddActivity.ADD_TYPE, AddActivity.EDIT_POST);

                activity.startActivity(intent);
                activity.overridePendingTransition(R.anim.anim_in, R.anim.anim_out);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        closeItem(position);
                    }
                }, 500);

                break;

            default:
                CommonUtils.moveNextActivity(activity, DetailActivity.class, false);
                break;
        }
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.swipe;
    }


    public class ViewHolderProfileInfo extends ViewHolderFollow implements View.OnClickListener {

        private ViewPager mViewPager;
        private RoundedImageView mImgViewPhoto;

        private Button mButFollowing;
        private Button mButFollower;

        // main
        private TextView mTextUsername;
        private Button mButReviewNum;
        private TextView mTextInfo;

        // about
        private TextView mTextAbout;

        public ViewHolderProfileInfo(Context ctx, View itemView) {
            super(ctx, itemView);

            LayoutInflater layoutInflater = LayoutInflater.from(ctx);

            View viewMain = layoutInflater.inflate(R.layout.layout_profile_info_main, null);
            View viewAbout = layoutInflater.inflate(R.layout.layout_profile_info_about, null);

            ArrayList<View> aryView = new ArrayList<View>();
            aryView.add(viewMain);
            aryView.add(viewAbout);

            mViewPager = (ViewPager)itemView.findViewById(R.id.viewpager);

            mViewPager.setAdapter(new LandingPagerAdapter(aryView));
            mViewPager.setCurrentItem(0);

            CirclePageIndicator circlePageIndicator = (CirclePageIndicator)itemView.findViewById(R.id.indicator);
            circlePageIndicator.setViewPager(mViewPager);

            int nColorTheme = ctx.getResources().getColor(R.color.color_theme);
            int nColorThemeTrans = Color.argb(0x7f, Color.red(nColorTheme), Color.green(nColorTheme), Color.blue(nColorTheme));
            circlePageIndicator.setFillColor(nColorTheme);
            circlePageIndicator.setPageColor(nColorThemeTrans);

            mImgViewPhoto = (RoundedImageView)itemView.findViewById(R.id.imgview_photo);

            mButFollowing = (Button)itemView.findViewById(R.id.but_following);
            mButFollowing.setOnClickListener(this);

            mButFollower = (Button)itemView.findViewById(R.id.but_follower);
            mButFollower.setOnClickListener(this);

            // main
            mTextUsername = (TextView)viewMain.findViewById(R.id.text_username);
            mButReviewNum = (Button)viewMain.findViewById(R.id.but_review_num);
            mButReviewNum.setOnClickListener(this);

            mTextInfo = (TextView)viewMain.findViewById(R.id.text_info);

            // about
            mTextAbout = (TextView)viewAbout.findViewById(R.id.text_about);
        }

        public void fillContent(UserData data, String username) {
            if (data.getCreatedAt() != null) {
                data.showPhoto(mContext, mImgViewPhoto);

                mTextUsername.setText(data.getUsernameToShow());

                // member since
                int nYear = Calendar.getInstance().get(Calendar.YEAR);
                String strDesc = "Member Since: " + nYear;

                if (!TextUtils.isEmpty(data.getAddress())) {
                    strDesc += " Location: " + data.getAddress();
                }

                mTextInfo.setText(strDesc);

                // about
                mTextAbout.setText(data.getAbout());
            }
            else {
                mTextUsername.setText(username);
                mTextInfo.setText("");
                mTextAbout.setText("");
            }

            // review num
            mButReviewNum.setText("" + data.mnReviewCount);

            // follow
            UserData currentUser = (UserData) UserData.getCurrentUser();
            if (data.getObjectId().equals(currentUser.getObjectId())) {
                mButFollow.setVisibility(View.GONE);
            }
            else {
                mButFollow.setVisibility(View.VISIBLE);
                updateFollowButton(data);
            }

            mButFollowing.setText(data.maryFollowingUser.size() + " Following");
            mButFollower.setText(data.maryFollowerUser.size() + " Followers");
        }

        @Override
        public void onClick(View view) {
            int id = view.getId();

            ProfileActivity profileActivity = (ProfileActivity)mContext;

            switch (id) {
                case R.id.but_following:
                    if (mUserData.maryFollowingUser.size() == 0) {
                        return;
                    }

                case R.id.but_follower:
                    if (mUserData.maryFollowerUser.size() == 0) {
                        return;
                    }

                    Intent intent = new Intent(profileActivity, FollowingActivity.class);

                    if (id == R.id.but_following) {
                        intent.putExtra(FollowingActivity.FOLLOWING_TYPE, FollowingActivity.FOLLOW_FOLLOWING);
                    }
                    else {
                        intent.putExtra(FollowingActivity.FOLLOWING_TYPE, FollowingActivity.FOLLOW_FOLLOWER);
                    }

                    CommonUtils.mUserSelected = mUserData;

                    profileActivity.startActivity(intent);
                    profileActivity.overridePendingTransition(R.anim.anim_in, R.anim.anim_out);

                    break;

                case R.id.but_review_num:
                    CommonUtils.mUserSelected = mUserData;

                    CommonUtils.moveNextActivity(profileActivity, ReviewActivity.class, false);

                    break;
            }
        }
    }
}
