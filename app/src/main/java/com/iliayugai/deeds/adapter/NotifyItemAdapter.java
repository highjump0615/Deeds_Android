package com.iliayugai.deeds.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.iliayugai.deeds.DetailActivity;
import com.iliayugai.deeds.NotifyActivity;
import com.iliayugai.deeds.ProfileActivity;
import com.iliayugai.deeds.R;
import com.iliayugai.deeds.ReviewActivity;
import com.iliayugai.deeds.model.ItemData;
import com.iliayugai.deeds.model.NotificationData;
import com.iliayugai.deeds.model.UserData;
import com.iliayugai.deeds.utils.CommonUtils;
import com.iliayugai.deeds.utils.DeedItemClickListener;
import com.iliayugai.deeds.view.ViewHolderBase;
import com.iliayugai.deeds.view.ViewHolderLoading;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by highjump on 15-6-4.
 */
public class NotifyItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements DeedItemClickListener {

    private static final String TAG = NotifyItemAdapter.class.getSimpleName();

    private ArrayList<NotificationData> maryNotify;

    public boolean mbNeedMore = false;

    private int ITEM_VIEW_TYPE_NOTIFY = 0;
    private int ITEM_VIEW_TYPE_COMMENT = 1;
    private int ITEM_VIEW_TYPE_FOOTER = 2;

    private Context mContext;

    private NotificationData mPrevNotify;

    public NotifyItemAdapter(Context ctx, ArrayList<NotificationData> aryNotify) {
        mContext = ctx;
        this.maryNotify = aryNotify;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder vhRes = null;

        if (viewType == ITEM_VIEW_TYPE_NOTIFY) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_notify_item, parent, false);

            ViewHolderNotify vh = new ViewHolderNotify(v);
            vh.setOnItemClickListener(this);

            vhRes = vh;
        }
        else if (viewType == ITEM_VIEW_TYPE_COMMENT) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_notify_comment, parent, false);

            ViewHolderNotifyComment vh = new ViewHolderNotifyComment(v);
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

        if (holder instanceof ViewHolderNotify) {
            NotificationData nData = getNotificationAtIndex(position);

            ViewHolderNotify viewHolder = (ViewHolderNotify)holder;
            viewHolder.fillContent(nData);
        }
        else if (holder instanceof ViewHolderNotifyComment) {
            getNotificationAtIndex(position);

            ViewHolderNotifyComment viewHolder = (ViewHolderNotifyComment)holder;
            viewHolder.fillContent(mPrevNotify);
        }
        else if (holder instanceof ViewHolderLoading) {
//            Log.d(TAG, "Footer is shown: ");

            NotifyActivity activity = (NotifyActivity)mContext;
            activity.getNotification(false, false);
        }
    }

    private int getNotificationCount() {
        int nCount = 0;

        for (NotificationData nData : maryNotify) {
            if (nData.mbExpanded) {
                nCount++;
            }

            nCount++;
        }

        return nCount;
    }

    private NotificationData getNotificationAtIndex(int index) {
        NotificationData nData = null;
        int nIndex = 0;

        for (NotificationData ntData : maryNotify) {
            if (nIndex == index) {
                nData = ntData;
                break;
            }

            if (ntData.mbExpanded) {
                nIndex++;
                if (nIndex == index) {
                    mPrevNotify = ntData;
                    nData = null;
                    break;
                }
            }

            nIndex++;
        }

        return nData;
    }

    @Override
    public int getItemCount() {
        int nCount = getNotificationCount();

        if (mbNeedMore) {
            nCount++;
        }

        return nCount;
    }

    @Override
    public int getItemViewType(int position) {

        int nViewType = ITEM_VIEW_TYPE_FOOTER;

        if (position < getNotificationCount()) {

            NotificationData nData = getNotificationAtIndex(position);

            if (nData != null) {
                nViewType = ITEM_VIEW_TYPE_NOTIFY;
            }
            else {
                nViewType = ITEM_VIEW_TYPE_COMMENT;
            }
        }

        return nViewType;
    }

    private void alertForDelete() {
        CommonUtils.createErrorAlertDialog(mContext, "", "Cannot find this item").show();
        return;
    }

    @Override
    public void onItemClick(View view, int position) {
        int id = view.getId();

        NotifyActivity activity = (NotifyActivity)mContext;

        switch (id) {
            case R.id.layout_notify_root:
            case R.id.layout_notify_comment_root:
                NotificationData nData = getNotificationAtIndex(position);

                if (nData == null) {
                    CommonUtils.mItemSelected = mPrevNotify.getItem();
                    if (CommonUtils.mItemSelected == null) {
                        alertForDelete();
                        return;
                    }

                    CommonUtils.moveNextActivity(activity, DetailActivity.class, false);

                    return;
                }

                if (nData.getType() == NotificationData.NOTIFICATION_COMMENT) {
                    if (nData.mbExpanded) {
                        nData.mbExpanded = false;
                        view.setSelected(false);
                        notifyItemRemoved(position + 1);
                    }
                    else {
                        view.setSelected(true);
                        nData.mbExpanded = true;
                        notifyItemInserted(position + 1);
                    }
                }
                else {
                    if (nData.getType() == NotificationData.NOTIFICATION_FOLLOW) {
                        CommonUtils.mUserSelected = nData.getUser();
                        gotoProfile(nData.getUsername());
                    }
                    else if (nData.getType() == NotificationData.NOTIFICATION_RATE) {
                        UserData currentUser = (UserData) UserData.getCurrentUser();

                        CommonUtils.mUserSelected = currentUser;

                        Intent intent = new Intent(activity, ReviewActivity.class);
                        intent.putExtra(ReviewActivity.SELECTED_USERNAME, nData.getUsername());

                        activity.startActivity(intent);
                        activity.overridePendingTransition(R.anim.anim_in, R.anim.anim_out);
                    }
                    else {
                        CommonUtils.mItemSelected = nData.getItem();
                        if (CommonUtils.mItemSelected == null) {
                            alertForDelete();
                            return;
                        }

                        CommonUtils.moveNextActivity(activity, DetailActivity.class, false);
                    }
                }

                break;
        }
    }

    private void gotoProfile(String username) {

        UserData currentUser = (UserData) UserData.getCurrentUser();

        if (CommonUtils.mUserSelected.getObjectId().equals(currentUser.getObjectId())) {
            return;
        }

        NotifyActivity activity = (NotifyActivity)mContext;
        Intent intent = new Intent(activity, ProfileActivity.class);
        intent.putExtra(ProfileActivity.SELECTED_USERNAME, username);

        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.anim_in, R.anim.anim_out);
    }

    public class ViewHolderNotify extends ViewHolderBase {

        private View mViewParent;

        private ImageView mImgViewIcon;
        private TextView mTextDesc;
        private TextView mTextTime;

        public ViewHolderNotify(View itemView) {
            super(itemView);

            mImgViewIcon = (ImageView)itemView.findViewById(R.id.imgview_icon);
            mTextDesc = (TextView)itemView.findViewById(R.id.text_desc);
            mTextTime = (TextView)itemView.findViewById(R.id.text_time);

            mViewParent = itemView;
        }

        public void fillContent(NotificationData data) {
            int nType = data.getType();

            if (nType == NotificationData.NOTIFICATION_COMMENT) {
                mImgViewIcon.setImageResource(R.drawable.notify_comment_icon);
                mTextDesc.setText("You have got new comment");
            }
            else if (nType == NotificationData.NOTIFICATION_FOLLOW) {
                mImgViewIcon.setImageResource(R.drawable.notify_follow_icon);
                mTextDesc.setText(data.getUsername() + " now follows you");
            }
            else if (nType == NotificationData.NOTIFICATION_FAVOURITE) {
                mImgViewIcon.setImageResource(R.drawable.notify_favourite_icon);
                mTextDesc.setText(data.getUsername() + " favorited your post");
            }
            else if (nType == NotificationData.NOTIFICATION_RATE) {
                mImgViewIcon.setImageResource(R.drawable.notify_review_icon);
                mTextDesc.setText("You have been rated");
            }
            else if (nType == NotificationData.NOTIFICATION_MENTION) {
                mImgViewIcon.setImageResource(R.drawable.notify_mention_icon);
                mTextDesc.setText("You have been mentioned");
            }

            // date
            DateFormat dateFormat = new SimpleDateFormat("MMMM dd yyyy, HH:mm");
            String strDate = dateFormat.format(data.getCreatedAt());
            mTextTime.setText(strDate);

            // selected
            mViewParent.setSelected(data.mbExpanded);
        }
    }

    public class ViewHolderNotifyComment extends ViewHolderBase {

        private TextView mTextComment;

        public ViewHolderNotifyComment(View itemView) {
            super(itemView);

            mTextComment = (TextView)itemView.findViewById(R.id.text_comment);
        }

        public void fillContent(NotificationData data) {
            mTextComment.setText(data.getComment());
        }

    }
}
