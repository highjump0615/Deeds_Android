package com.iliayugai.deeds;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.iliayugai.deeds.adapter.DetailItemAdapter;
import com.iliayugai.deeds.adapter.MentionAdapter;
import com.iliayugai.deeds.model.ItemData;
import com.iliayugai.deeds.model.NotificationData;
import com.iliayugai.deeds.model.UserData;
import com.iliayugai.deeds.utils.CommonUtils;
import com.makeramen.roundedimageview.RoundedImageView;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by highjump on 15-6-8.
 */
public class DetailActivity extends Activity implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = DetailActivity.class.getSimpleName();

    private SwipeRefreshLayout mRefreshLayout;

    private ItemData mItem;
    private ArrayList<NotificationData> maryComment = new ArrayList<NotificationData>();
    public ArrayList<UserData> maryMention;

    private RecyclerView mRecyclerView;
    public DetailItemAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;

    private RelativeLayout mLayoutRoot;
    private RelativeLayout mLayoutInfo;

    private RoundedImageView mImgViewUser;
    private TextView mTextUsername;
    private Button mButPhone;
    private Button mButEmail;

    public RelativeLayout mLayoutMention;
    private ListView mLVMention;
    public MentionAdapter mAdMention;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_detail);

        mItem = CommonUtils.mItemSelected;

        // init view
        mRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swiperefresh);
        mRefreshLayout.setOnRefreshListener(this);

        mImgViewUser = (RoundedImageView)findViewById(R.id.imgview_user);
        mTextUsername = (TextView)findViewById(R.id.text_username);
        mButPhone = (Button)findViewById(R.id.but_phone);
        mButPhone.setOnClickListener(this);

        mButEmail = (Button)findViewById(R.id.but_email);
        mButEmail.setOnClickListener(this);

        mLayoutRoot = (RelativeLayout)findViewById(R.id.layout_root);
        mLayoutRoot.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = mLayoutRoot.getRootView().getHeight() - mLayoutRoot.getHeight();
                if (heightDiff > 100) {
//                    Log.d(TAG, "Show Keyboard: " + heightDiff);
//                    mAdapter.setFocusToEdit();
//
                }
                else {
//                    Log.d(TAG, "Hide Keyboard: " + heightDiff);
                    showMentionView(false);
                }
            }
        });

        mLayoutInfo = (RelativeLayout)findViewById(R.id.layout_info);
        mLayoutInfo.setVisibility(View.GONE);
        mLayoutInfo.setOnClickListener(this);

        Button button = (Button)findViewById(R.id.but_back);
        button.setOnClickListener(this);

        button = (Button)findViewById(R.id.but_want);
        button.setOnClickListener(this);

        // init list
        mRecyclerView = (RecyclerView)findViewById(R.id.list);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new DetailItemAdapter(this, mItem, maryComment);
        mRecyclerView.setAdapter(mAdapter);

        // get comment info
        getCommentData();

        //
        // user info
        //
        mItem.getUser().fetchIfNeededInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                mAdapter.notifyDataSetChanged();

                // set user info
                UserData user = (UserData)parseObject;

                user.showPhoto(DetailActivity.this, mImgViewUser);

                mTextUsername.setText(user.getUsernameToShow());
                mButPhone.setText(user.getPhone());
                mButEmail.setText(user.getEmail());
            }
        });

        mTextUsername.setText(mItem.getUsername());

        //
        // mention list
        //
        mLayoutMention = (RelativeLayout)findViewById(R.id.layout_mention);
        mLVMention = (ListView)findViewById(R.id.list_mention);
        mAdMention = new MentionAdapter(this, maryMention);
        mLVMention.setAdapter(mAdMention);
        mLVMention.setOnItemClickListener(mAdapter);
    }

    private void getCommentData() {

        UserData currentUser = (UserData) UserData.getCurrentUser();

        maryMention = new ArrayList<UserData>(currentUser.maryFollowingUser);
        if (!isExistinginMentionList(mItem.getUser())) {
            maryMention.add(mItem.getUser());
        }

        ParseRelation relation = mItem.getCommentobject();
        ParseQuery<NotificationData> commentQuery = relation.getQuery();

        commentQuery.include("user");
        commentQuery.orderByDescending("createdAt");

        commentQuery.findInBackground(new FindCallback<NotificationData>() {
            @Override
            public void done(List<NotificationData> list, ParseException e) {
                stopRefresh();

                if (e == null) {
                    maryComment.clear();

                    for (NotificationData nData : list) {
                        if (nData.getType() == NotificationData.NOTIFICATION_COMMENT) {
                            maryComment.add(nData);
                        }

                        if (isExistinginMentionList(nData.getUser())) {
                            continue;
                        }

                        maryMention.add(nData.getUser());
                    }

                    mAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    private boolean isExistinginMentionList(UserData user) {
        boolean bExist = false;
        UserData currentUser = (UserData) UserData.getCurrentUser();

        if (user.getObjectId().equals(currentUser.getObjectId())) {
            return true;
        }

        for (UserData uData : maryMention) {
            if (uData.getObjectId().equals(user.getObjectId())) {
                bExist = true;
                break;
            }
        }

        return bExist;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        switch (id) {
            case R.id.but_back:
                onBackPressed();
                break;

            case R.id.but_want:
                showContact(true);
                break;

            case R.id.layout_info:
                showContact(false);
                break;

            case R.id.but_phone:
                onButPhone();
                break;

            case R.id.but_email:
                onButEmail();
                break;
        }
    }

    private void onButEmail() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");

        String[] strTo = {mButEmail.getText().toString()};
        intent.putExtra(Intent.EXTRA_EMAIL, strTo);
        intent.putExtra(Intent.EXTRA_SUBJECT, mItem.getTitle());

        startActivity(Intent.createChooser(intent, "Send Email"));
    }

    private void onButPhone() {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + mButPhone.getText().toString()));
        startActivity(intent);
    }

    private void showContact(boolean show) {

        AlphaAnimation animAlpha;

        if (show) {
            animAlpha = new AlphaAnimation(0f, 1f);
            mLayoutInfo.setVisibility(View.VISIBLE);
        }
        else {
            animAlpha = new AlphaAnimation(1f, 0f);
            mLayoutInfo.setVisibility(View.GONE);
        }

        animAlpha.setDuration(300);
        mLayoutInfo.startAnimation(animAlpha);
    }

    @Override
    public void onRefresh() {
        getCommentData();
    }

    private void stopRefresh() {
        mRefreshLayout.setRefreshing(false);
    }

    public void showMentionView(boolean bShow) {
        mAdapter.showMentionView(bShow);
    }

    public void scrollToEditText() {
        int nDiff = 1;
        if (mItem.getImages().size() > 0) {
            nDiff++;
        }
        mRecyclerView.scrollToPosition(nDiff);
    }
}
