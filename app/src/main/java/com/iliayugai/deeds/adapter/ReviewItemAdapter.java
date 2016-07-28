package com.iliayugai.deeds.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.iliayugai.deeds.FollowingActivity;
import com.iliayugai.deeds.ProfileActivity;
import com.iliayugai.deeds.R;
import com.iliayugai.deeds.model.ItemData;
import com.iliayugai.deeds.model.NotificationData;
import com.iliayugai.deeds.model.UserData;
import com.iliayugai.deeds.utils.CommonUtils;
import com.iliayugai.deeds.view.ViewHolderBase;
import com.iliayugai.deeds.view.ViewHolderFavouriteItem;
import com.iliayugai.deeds.view.ViewHolderFollow;
import com.iliayugai.deeds.view.ViewHolderLoading;
import com.iliayugai.deeds.view.ViewHolderReviewInfo;
import com.makeramen.roundedimageview.RoundedImageView;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.squareup.picasso.Picasso;
import com.viewpagerindicator.CirclePageIndicator;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by highjump on 15-6-4.
 */
public class ReviewItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = ReviewItemAdapter.class.getSimpleName();

    private UserData mUserData;

    private int ITEM_VIEW_TYPE_INFO = 0;
    private int ITEM_VIEW_TYPE_ITEM = 1;

    private Context mContext;

    public ReviewItemAdapter(Context ctx, UserData user) {
        mContext = ctx;

        mUserData = user;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder vhRes = null;

        if (viewType == ITEM_VIEW_TYPE_INFO) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_review_user, parent, false);

            ViewHolderReviewInfo vh = new ViewHolderReviewInfo(mContext, v);
            vhRes = vh;
        }
        else {
            // create a new view
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_review_item, parent, false);

            ViewHolderReviewItem vh = new ViewHolderReviewItem(v);
            vhRes = vh;
        }

        return vhRes;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof ViewHolderReviewInfo) {
            ViewHolderReviewInfo viewHolder = (ViewHolderReviewInfo)holder;
            viewHolder.fillContent(mUserData);
        }
        else if (holder instanceof ViewHolderReviewItem) {
            ViewHolderReviewItem viewHolder = (ViewHolderReviewItem)holder;

            NotificationData nData = mUserData.maryReview.get(position - 1);
            viewHolder.fillContent(nData);
        }
    }

    @Override
    public int getItemCount() {
        int nCount = mUserData.maryReview.size();

        nCount += 1;

        return nCount;
    }

    @Override
    public int getItemViewType(int position) {

        int nViewType = ITEM_VIEW_TYPE_ITEM;

        if (position == 0) {
            return ITEM_VIEW_TYPE_INFO;
        }

        return nViewType;
    }

    public class ViewHolderReviewItem extends ViewHolderBase {

        private RoundedImageView mImgViewPhoto;
        private Button mButUser;
        private ImageView mImgViewIcon;
        private TextView mTextRate;
        private TextView mTextReview;
        private TextView mTextTime;

        public ViewHolderReviewItem(View itemView) {
            super(itemView);

            mImgViewPhoto = (RoundedImageView)itemView.findViewById(R.id.imgview_user);
            mButUser = (Button)itemView.findViewById(R.id.but_user);
            mImgViewIcon = (ImageView)itemView.findViewById(R.id.imgview_icon);
            mTextRate = (TextView)itemView.findViewById(R.id.text_rate);
            mTextReview = (TextView)itemView.findViewById(R.id.text_review);
            mTextTime = (TextView)itemView.findViewById(R.id.text_time);
        }

        public void fillContent(final NotificationData data) {
            //
            // user info
            //
            final UserData user = data.getUser();

            user.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject parseObject, ParseException e) {
                    user.showPhoto(mContext, mImgViewPhoto);
                }
            });

            mButUser.setText(data.getUsername());

            // content
            mTextReview.setText(data.getComment());

            // date
            mTextTime.setText(CommonUtils.getTimeString(data.getCreatedAt()));

            // rate
            if (data.getRate() == NotificationData.RATE_NORMAL) {
                mImgViewIcon.setImageResource(R.mipmap.review_bad);
                mTextRate.setText("Normal");
            }
            else if (data.getRate() == NotificationData.RATE_GOOD) {
                mImgViewIcon.setImageResource(R.mipmap.review_normal);
                mTextRate.setText("Good");
            }
            else {
                mImgViewIcon.setImageResource(R.mipmap.review_good);
                mTextRate.setText("Amazing");
            }
        }
    }
}
