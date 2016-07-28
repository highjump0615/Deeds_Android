package com.iliayugai.deeds.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.iliayugai.deeds.DetailActivity;
import com.iliayugai.deeds.FullImageActivity;
import com.iliayugai.deeds.ProfileActivity;
import com.iliayugai.deeds.R;
import com.iliayugai.deeds.model.ItemData;
import com.iliayugai.deeds.model.NotificationData;
import com.iliayugai.deeds.model.UserData;
import com.iliayugai.deeds.utils.CommonUtils;
import com.iliayugai.deeds.utils.DeedEditorActionListener;
import com.iliayugai.deeds.utils.DeedItemClickListener;
import com.iliayugai.deeds.view.ViewHolderBase;
import com.makeramen.roundedimageview.RoundedImageView;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by highjump on 15-6-4.
 */
public class DetailItemAdapter extends RecyclerSwipeAdapter<RecyclerView.ViewHolder> implements DeedItemClickListener, DeedEditorActionListener, AdapterView.OnItemClickListener {

    private static final String TAG = DetailItemAdapter.class.getSimpleName();

    private ItemData mItem;
    private ArrayList<NotificationData> maryComment;

    private int ITEM_VIEW_TYPE_PHOTO = 0;
    private int ITEM_VIEW_TYPE_ITEM = 1;
    private int ITEM_VIEW_TYPE_USER = 2;
    private int ITEM_VIEW_TYPE_COMMENT = 3;

    private Context mContext;
    private int mnDiff = 2;
    public int mnAtPos = 0;
    public String mstrAtText = "";

    private EditText mEditComment;
    public float mfMaxMentionHeight;

    public DetailItemAdapter(Context ctx, ItemData item, ArrayList<NotificationData> aryComment) {
        mContext = ctx;

        mItem = item;
        maryComment = aryComment;

        if (item.getImages().size() > 0) {
            mnDiff = 3;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder vhRes = null;

        if (viewType == ITEM_VIEW_TYPE_PHOTO) {
            View v = null;

            if (mItem.getImages().size() == 1) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_detail_photo1, parent, false);
            }
            else if (mItem.getImages().size() == 2) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_detail_photo2, parent, false);
            }
            else {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_detail_photo3, parent, false);
            }

            ViewHolderPhoto vh = new ViewHolderPhoto(v);
            vh.setOnItemClickListener(this);

            vhRes = vh;
        }
        if (viewType == ITEM_VIEW_TYPE_ITEM) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_detail_item, parent, false);

            ViewHolderDetail vh = new ViewHolderDetail(v);
            vh.setOnItemClickListener(this);

            vhRes = vh;
        }
        else if (viewType == ITEM_VIEW_TYPE_USER) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_detail_user, parent, false);

            ViewHolderDetailUser vh = new ViewHolderDetailUser(v);
            vh.setOnItemClickListener(this);
            vh.setDeedEditorActionListener(this);

            final DetailActivity activity = (DetailActivity)mContext;

            mEditComment = vh.mEditComment;
            mEditComment.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean b) {
                    if (!b) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                activity.scrollToEditText();
                                mEditComment.requestFocus();
                            }
                        }, 200);
                    }
                }
            });

            vhRes = vh;
        }
        else if (viewType == ITEM_VIEW_TYPE_COMMENT) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_detail_comment, parent, false);

            ViewHolderDetailComment vh = new ViewHolderDetailComment(v);
            vh.setOnItemClickListener(this);

            vhRes = vh;
        }

        return vhRes;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof ViewHolderPhoto) {
            ViewHolderPhoto viewHolder = (ViewHolderPhoto)holder;
            viewHolder.fillContent(mItem);
        }
        if (holder instanceof ViewHolderDetail) {
            ViewHolderDetail viewHolder = (ViewHolderDetail)holder;
            viewHolder.fillContent(mItem);
        }
        else if (holder instanceof ViewHolderDetailUser) {
            ViewHolderDetailUser viewHolder = (ViewHolderDetailUser) holder;
            viewHolder.fillContent(mItem);
        }
        else if (holder instanceof ViewHolderDetailComment) {
            NotificationData nData = maryComment.get(position - mnDiff);

            ViewHolderDetailComment viewHolder = (ViewHolderDetailComment)holder;
            viewHolder.fillContent(nData);

            mItemManger.bind(viewHolder.itemView, position);
        }
    }

    @Override
    public int getItemCount() {
        int nCount = maryComment.size();

        nCount += mnDiff;

        return nCount;
    }

    @Override
    public int getItemViewType(int position) {

        int nViewType;
        int nPos = position;

        if (mItem.getImages().size() > 0) {
            nPos--;
        }

        if (nPos < 0) {
            nViewType = ITEM_VIEW_TYPE_PHOTO;
        }
        else if (nPos == 0) {
            nViewType = ITEM_VIEW_TYPE_ITEM;
        }
        else if (nPos == 1) {
            nViewType = ITEM_VIEW_TYPE_USER;
        }
        else {
            nViewType = ITEM_VIEW_TYPE_COMMENT;
        }

        return nViewType;
    }

    @Override
    public void onItemClick(View view, int position) {
        int id = view.getId();

        int nPos = position;

        DetailActivity activity = (DetailActivity)mContext;

        if (mItem.getImages().size() > 0) {
            nPos--;
        }

        if (nPos < 0) {
            switch (id) {
                case R.id.imgview1:
                    onImageView(0);
                    break;

                case R.id.imgview2:
                    onImageView(1);
                    break;

                case R.id.imgview3:
                    onImageView(2);
                    break;
            }
        }
        else if (nPos == 0) {
            switch (id) {
                case R.id.layout_report:
                    AlertDialog dialog = new AlertDialog.Builder(mContext)
                            .setTitle("")
                            .setMessage("Do you want to report this item?")
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    mItem.increment("reportcount");
                                    mItem.saveInBackground();
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, null)
                            .create();

                    dialog.show();

                    break;

                case R.id.layout_favourite:
                    onButFavourite();
                    break;

                case R.id.layout_share:
                    onButShare();
                    break;
            }
        }
        else if (nPos == 1) {
            switch (id) {
                case R.id.imgview_user_item:
                    CommonUtils.mUserSelected = mItem.getUser();
                    gotoProfile(mItem.getUsername());

                    break;

                case R.id.layout_follow:
                    onButFollow();

                    break;
            }
        }
        else {
            NotificationData nData = maryComment.get(position - mnDiff);

            switch (id) {
                case R.id.imgview_user:
                case R.id.but_user:
                    CommonUtils.mUserSelected = nData.getUser();
                    gotoProfile(nData.getUsername());

                    break;

                case R.id.but_delete:
                    notifyItemRemoved(position);

                    nData.deleteInBackground();
                    maryComment.remove(nData);

                    break;
            }
        }
    }

    private void onButFollow() {
        UserData currentUser = (UserData) UserData.getCurrentUser();
        currentUser.doFollow(mItem.getUser());

        notifyItemChanged(1);
    }

    private void onButShare() {
        DetailActivity activity = (DetailActivity)mContext;

        ShareDialog shareDialog = new ShareDialog(activity);

        ShareLinkContent.Builder linkContentBuilder = new ShareLinkContent.Builder()
                .setContentTitle(mItem.getTitle())
                .setContentDescription(mItem.getDesc());

        if (mItem.getImages().size() > 0) {
            ParseFile filePhoto = mItem.getImages().get(0);
            linkContentBuilder.setImageUrl(Uri.parse(filePhoto.getUrl()))
                    .setContentUrl(Uri.parse(filePhoto.getUrl()));
        }

        shareDialog.show(linkContentBuilder.build());
    }

    private void onImageView(int nIndex) {
        DetailActivity activity = (DetailActivity)mContext;
        Intent intent = new Intent(activity, FullImageActivity.class);
        intent.putExtra(FullImageActivity.IMAGE_INDEX, nIndex);

        activity.startActivity(intent);
    }

    private void onButFavourite() {
        UserData currentUser = (UserData) UserData.getCurrentUser();
        if (currentUser.isItemFavourite(mItem)) {
            return;
        }

        ParseRelation<ItemData> relation = currentUser.getFavourite();
        relation.add(mItem);
        currentUser.saveInBackground();

        currentUser.maryFavouriteItem.add(mItem);

        notifyItemChanged(0);

        // if it is me, return
        if (mItem.getUser().getObjectId().equals(currentUser.getObjectId())) {
            return;
        }

        // save to notification data
        NotificationData notifyObj = new NotificationData();
        notifyObj.setItem(mItem);
        notifyObj.setUser(currentUser);
        notifyObj.setUsername(currentUser.getUsernameToShow());
        notifyObj.setTargetUser(mItem.getUser());
        notifyObj.setType(NotificationData.NOTIFICATION_FAVOURITE);
        notifyObj.saveInBackground();

        // animation

        // push notification
        ParseQuery query = ParseInstallation.getQuery();
        query.whereEqualTo("user", mItem.getUser());

        try {
            ParsePush push = new ParsePush();
            push.setQuery(query);
            JSONObject data = new JSONObject("{\"alert\": \"" + currentUser.getUsernameToShow() + " loves your post\"," +
                    "\"notifyType\": \"favourite\"," +
                    "\"notifyItem\":" + mItem.getObjectId() + "," +
                    "\"badge\": \"Increment\"," +
                    "\"sound\": \"cheering.caf\"}");
            push.setData(data);
            push.sendInBackground();
        } catch (JSONException e1) {
            e1.printStackTrace();
        }
    }

    private void gotoProfile(String username) {

        UserData currentUser = (UserData) UserData.getCurrentUser();

        if (CommonUtils.mUserSelected.getObjectId().equals(currentUser.getObjectId())) {
            return;
        }

        DetailActivity activity = (DetailActivity)mContext;
        Intent intent = new Intent(activity, ProfileActivity.class);
        intent.putExtra(ProfileActivity.SELECTED_USERNAME, username);

        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.anim_in, R.anim.anim_out);
    }

    @Override
    public void onEditorAction(TextView textView, int actionId) {
        if (actionId == EditorInfo.IME_ACTION_SEND) {
            String strComment = textView.getText().toString();

            // save comment data
            if (TextUtils.isEmpty(strComment)) {
                return;
            }

            //
            // save to notification database
            //
            UserData currentUser = (UserData) UserData.getCurrentUser();

            final NotificationData notifyObj = new NotificationData();
            notifyObj.setItem(mItem);
            notifyObj.setUser(currentUser);
            notifyObj.setUsername(currentUser.getUsernameToShow());
            notifyObj.setTargetUser(mItem.getUser());
            notifyObj.setType(NotificationData.NOTIFICATION_COMMENT);
            notifyObj.setComment(strComment);

            notifyObj.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {
                        CommonUtils.createErrorAlertDialog(mContext, "", e.getLocalizedMessage()).show();
                        return;
                    } else {
                        //
                        // add comment object
                        //
                        ParseRelation<NotificationData> relation = mItem.getCommentobject();
                        relation.add(notifyObj);

                        mItem.saveInBackground();
                    }
                }
            });

            maryComment.add(0, notifyObj);
            notifyItemInserted(mnDiff);

            // if it is me, return
            if (!mItem.getUser().getObjectId().equals(currentUser.getObjectId())) {
                // push notification
                ParseQuery query = ParseInstallation.getQuery();
                query.whereEqualTo("user", mItem.getUser());

                try {
                    ParsePush push = new ParsePush();
                    push.setQuery(query);
                    JSONObject data = new JSONObject("{\"alert\": \"" + currentUser.getUsernameToShow() + " left you a little something\"," +
                            "\"notifyType\": \"comment\"," +
                            "\"notifyItem\":" + mItem.getObjectId() + "," +
                            "\"badge\": \"Increment\"," +
                            "\"sound\": \"cheering.caf\"}");
                    push.setData(data);
                    push.sendInBackground();
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }

            //
            // check mentioning and send notification
            //
            DetailActivity activity = (DetailActivity)mContext;
            while (true) {
                int nIndex = strComment.indexOf("@");

                if (nIndex < 0) {
                    break;
                }
                else {
                    String strToCompare = strComment.substring(nIndex + 1);
                    for (UserData uData : activity.maryMention) {
                        if (strToCompare.indexOf(uData.getUsernameToShow()) == 0) {
                            NotificationData mentionObj = new NotificationData();
                            mentionObj.setItem(mItem);
                            mentionObj.setUser(currentUser);
                            mentionObj.setUsername(currentUser.getUsernameToShow());
                            mentionObj.setTargetUser(uData);
                            mentionObj.setType(NotificationData.NOTIFICATION_MENTION);
                            mentionObj.setComment(textView.getText().toString());
                            mentionObj.saveInBackground();

                            // send notification to commented user
                            ParseQuery query = ParseInstallation.getQuery();
                            query.whereEqualTo("user", uData);

                            try {
                                ParsePush push = new ParsePush();
                                push.setQuery(query);
                                JSONObject data = new JSONObject("{\"alert\": \"Your name has been mentioned\"," +
                                        "\"notifyType\": \"mention\"," +
                                        "\"notifyItem\":" + mItem.getObjectId() + "," +
                                        "\"badge\": \"Increment\"," +
                                        "\"sound\": \"cheering.caf\"}");
                                push.setData(data);
                                push.sendInBackground();
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }

                    strComment = strToCompare;
                }
            }

            textView.setText("");
        }
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.swipe;
    }

    private TextWatcher mTextWatcher = new TextWatcher() {
        private CharSequence temp = "";

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
//            Log.d(TAG, "on s: " + s + ", start: " + start + ", before: " + before + ", count: " + count);
        }

        @Override
        public void afterTextChanged(Editable s) {
//            Log.d(TAG, "temp: " + temp + ", s: " + s);

            DetailActivity activity = (DetailActivity)mContext;

            if (s.length() > temp.length()) {
                String strText = s.toString().substring(temp.length());

                if (strText.equals("@")) {
//                    Log.d(TAG, "at pos: " + temp.length());

                    if (activity.mLayoutMention.getVisibility() == View.VISIBLE) {
                        return;
                    }

                    if (activity.maryMention.size() == 0) {
                        return;
                    }

                    mnAtPos = s.length();

                    showMentionView(true);
                    activity.mAdMention.notifyDataSetChanged();
                }
            }

            if (s.length() < mnAtPos) {
                showMentionView(false);
                mnAtPos = 0;
            }

            // extract the at text
            if (mnAtPos > 0) {
                mstrAtText = s.toString().substring(mnAtPos);
                showMentionView(true);
                activity.mAdMention.notifyDataSetChanged();
            }

            temp = s.toString();
        }
    };

    public void showMentionView(boolean bShow) {
        DetailActivity activity = (DetailActivity)mContext;

        if (bShow) {
            //
            // calculate frame size
            //
            int nNavbarHeight = (int) mContext.getResources().getDimension(R.dimen.nav_bar_height);
            int nMargin = CommonUtils.dipToPixels(mContext, 10);

            int[] editPos = new int[2];
            Rect r = new Rect();

            mEditComment.getLocationInWindow(editPos);
            mEditComment.getLocalVisibleRect(r);
            Log.d(TAG, "wind pos: " + editPos[0] + ", " + editPos[1]);
            Log.d(TAG, "local rt: " + r.left + ", " + r.height());

            DisplayMetrics displaymetrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            int height = activity.getWindow().getDecorView().getHeight();
            int width = activity.getWindow().getDecorView().getWidth();

            Log.d(TAG, "window width: " + width + ", height: " + height);

            View rootview = activity.getWindow().getDecorView(); // this = activity
            rootview.getWindowVisibleDisplayFrame(r);

            Log.d(TAG, "w: " + r.width() + ", h: " + r.height() + ", l: " + r.left + ", t: " + r.top);

            float fUpSpace = editPos[1] - nNavbarHeight - nMargin - r.top;
            float fDownSpace = r.height() - editPos[1] - mEditComment.getHeight() - nMargin + r.top;

            Log.d(TAG, "up: " + fUpSpace + ", down: " + fDownSpace);

            if (Math.max(fUpSpace, fDownSpace) < 0) {
                return;
            }

            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(mEditComment.getWidth(), 0);
            layoutParams.leftMargin = editPos[0];

            if (fUpSpace > fDownSpace) {
                mfMaxMentionHeight = fUpSpace;
                layoutParams.bottomMargin = r.height() + r.top - editPos[1];
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                activity.mLayoutMention.setBackgroundResource(R.mipmap.mention_frame_up);
            }
            else {
                mfMaxMentionHeight = fDownSpace;
                layoutParams.topMargin = editPos[1] + mEditComment.getHeight() - r.top;
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
                activity.mLayoutMention.setBackgroundResource(R.mipmap.mention_frame_down);
            }

            layoutParams.height = (int) mfMaxMentionHeight;

            activity.mLayoutMention.setLayoutParams(layoutParams);
            activity.mLayoutMention.setVisibility(View.VISIBLE);
        }
        else {
            activity.mLayoutMention.setVisibility(View.GONE);
        }
    }

    public void setFocusToEdit() {
        mEditComment.requestFocus();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        DetailActivity activity = (DetailActivity) mContext;

        UserData uData = activity.mAdMention.getAtUser(i);
        String strText = String.format("%s%s", mEditComment.getText().toString().substring(0, mnAtPos), uData.getUsernameToShow());
        mEditComment.setText(strText);
        mEditComment.setSelection(strText.length());

        showMentionView(false);
        mnAtPos = 0;
    }

    public class ViewHolderPhoto extends ViewHolderBase {

        private ImageView mImgView1;
        private ImageView mImgView2;
        private ImageView mImgView3;

        public ViewHolderPhoto(View itemView) {
            super(itemView);

            mImgView1 = (ImageView)itemView.findViewById(R.id.imgview1);
            if (mImgView1 != null) {
                mImgView1.setOnClickListener(this);
            }

            mImgView2 = (ImageView)itemView.findViewById(R.id.imgview2);
            if (mImgView2 != null) {
                mImgView2.setOnClickListener(this);
            }

            mImgView3 = (ImageView)itemView.findViewById(R.id.imgview3);
            if (mImgView3 != null) {
                mImgView3.setOnClickListener(this);
            }
        }

        public void fillContent(ItemData data) {
            // images
            ParseFile filePhoto;

            if (data.getImages().size() > 0) {
                filePhoto = data.getImages().get(0);
                Picasso.with(mContext)
                        .load(filePhoto.getUrl())
                        .placeholder(R.mipmap.home_item_default)
                        .error(R.mipmap.home_item_default)
                        .into(mImgView1);
            }

            if (data.getImages().size() > 1) {
                filePhoto = data.getImages().get(1);
                Picasso.with(mContext)
                        .load(filePhoto.getUrl())
                        .placeholder(R.mipmap.home_item_default)
                        .error(R.mipmap.home_item_default)
                        .into(mImgView2);
            }

            if (data.getImages().size() > 2) {
                filePhoto = data.getImages().get(2);
                Picasso.with(mContext)
                        .load(filePhoto.getUrl())
                        .placeholder(R.mipmap.home_item_default)
                        .error(R.mipmap.home_item_default)
                        .into(mImgView3);
            }
        }
    }

    public class ViewHolderDetail extends ViewHolderBase {

        private TextView mTextTitle;
        private TextView mTextAddress;
        private TextView mTextInfo;
        private TextView mTextDesc;

        private LinearLayout mLayoutShare;
        private LinearLayout mLayoutFavourite;
        private ImageView mImgViewFavourite;
        private LinearLayout mLayoutReport;

        public ViewHolderDetail(View itemView) {
            super(itemView);

            mTextTitle = (TextView)itemView.findViewById(R.id.text_title);
            mTextAddress = (TextView)itemView.findViewById(R.id.text_address);
            mTextInfo = (TextView)itemView.findViewById(R.id.text_info);
            mTextDesc = (TextView)itemView.findViewById(R.id.text_desc);

            mLayoutShare = (LinearLayout)itemView.findViewById(R.id.layout_share);
            mLayoutShare.setOnClickListener(this);

            mLayoutFavourite = (LinearLayout)itemView.findViewById(R.id.layout_favourite);
            mLayoutFavourite.setOnClickListener(this);

            mImgViewFavourite = (ImageView)itemView.findViewById(R.id.imgview_favourite);

            mLayoutReport = (LinearLayout)itemView.findViewById(R.id.layout_report);
            mLayoutReport.setOnClickListener(this);
        }

        public void fillContent(ItemData data) {

            // title
            mTextTitle.setText(data.getTitle());

            // address
            String strAddress = data.getAddress();
            if (TextUtils.isEmpty(strAddress)) {
                strAddress = "Unknown Location";
            }
            mTextAddress.setText(strAddress);

            // info
            String strInfo;
            double dDist = CommonUtils.getDistanceFromPoint(data.getLocation());
            if (dDist < 0) {
                strInfo = "Unknown";
            }
            else {
                strInfo = String.format("%.0fKM Away", dDist);
            }

            DateFormat dateFormat = new SimpleDateFormat("MMMM dd yyyy");
            String strDate = dateFormat.format(data.getCreatedAt());
            strInfo += "   " + strDate;
            mTextInfo.setText(strInfo);

            // desc
            mTextDesc.setText(data.getDesc());

            // favourite
            UserData currentUser = (UserData) UserData.getCurrentUser();

            if (currentUser.isItemFavourite(data)) {
                mImgViewFavourite.setImageResource(R.mipmap.detail_favourited);
            }
            else {
                mImgViewFavourite.setImageResource(R.mipmap.detail_favourite);
            }
        }

        @Override
        public void onClick(View view) {
            super.onClick(view);
        }
    }

    public class ViewHolderDetailUser extends ViewHolderBase {

        private RoundedImageView mImgviewUserItem;
        private TextView mTextUsername;
        private LinearLayout mLayoutFollow;
        private ImageView mImgviewFollow;

        private RoundedImageView mImgviewUser;
        private EditText mEditComment;

        public ViewHolderDetailUser(View itemView) {
            super(itemView);

            mImgviewUserItem = (RoundedImageView)itemView.findViewById(R.id.imgview_user_item);
            mImgviewUserItem.setOnClickListener(this);

            mTextUsername = (TextView)itemView.findViewById(R.id.text_username);

            mLayoutFollow = (LinearLayout)itemView.findViewById(R.id.layout_follow);
            mLayoutFollow.setOnClickListener(this);

            mImgviewFollow = (ImageView)itemView.findViewById(R.id.imgview_follow);

            mImgviewUser = (RoundedImageView)itemView.findViewById(R.id.imgview_user);

            mEditComment = (EditText)itemView.findViewById(R.id.edit_comment);
            mEditComment.setOnEditorActionListener(this);
            mEditComment.addTextChangedListener(mTextWatcher);
        }

        public void fillContent(ItemData data) {
            // user info
            UserData user = data.getUser();

            if (user.getCreatedAt() != null) {  // fetched
                user.showPhoto(mContext, mImgviewUserItem);

                mTextUsername.setText(user.getUsernameToShow());
            }
            else {
                mTextUsername.setText(data.getUsername());
            }

            UserData currentUser = (UserData) UserData.getCurrentUser();
            currentUser.showPhoto(mContext, mImgviewUser);

            if (currentUser.isUserFollowing(data.getUser())) {
                mImgviewFollow.setImageResource(R.mipmap.detail_follow);
            }
            else {
                mImgviewFollow.setImageResource(R.mipmap.detail_follow_plus);
            }
        }
    }

    public class ViewHolderDetailComment extends ViewHolderBase {

        private RoundedImageView mImgviewUser;
        private Button mButUser;
        private TextView mTextComment;
        private TextView mTextTime;

        public SwipeLayout mSwipeLayout;

        public ViewHolderDetailComment(View itemView) {
            super(itemView);

            mSwipeLayout = (SwipeLayout) itemView.findViewById(R.id.swipe);

            mImgviewUser = (RoundedImageView)itemView.findViewById(R.id.imgview_user);
            mImgviewUser.setOnClickListener(this);

            mButUser = (Button)itemView.findViewById(R.id.but_user);
            mButUser.setOnClickListener(this);

            mTextComment = (TextView)itemView.findViewById(R.id.text_comment);
            mTextTime = (TextView)itemView.findViewById(R.id.text_time);

            Button button = (Button)itemView.findViewById(R.id.but_delete);
            button.setOnClickListener(this);
        }

        public void fillContent(NotificationData data) {

            UserData user = data.getUser();

            user.showPhoto(mContext, mImgviewUser);

            mButUser.setText(user.getUsernameToShow());

            mTextComment.setText(data.getComment());
            mTextTime.setText(CommonUtils.getTimeString(data.getCreatedAt()));

            // set swipe
            UserData currentUser = (UserData)UserData.getCurrentUser();

            if (user.getObjectId().equals(currentUser.getObjectId()) ||
                mItem.getUser().getObjectId().equals(currentUser.getObjectId())) {
                mSwipeLayout.setSwipeEnabled(true);
            }
            else {
                mSwipeLayout.setSwipeEnabled(false);
            }
        }
    }
}
