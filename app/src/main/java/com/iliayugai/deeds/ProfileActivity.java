package com.iliayugai.deeds;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.iliayugai.deeds.adapter.ProfileItemAdapter;
import com.iliayugai.deeds.model.ItemData;
import com.iliayugai.deeds.model.NotificationData;
import com.iliayugai.deeds.model.UserData;
import com.iliayugai.deeds.utils.CommonUtils;
import com.iliayugai.deeds.utils.DeedCallbackInterface;
import com.parse.CountCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by highjump on 15-6-1.
 */
public class ProfileActivity extends Activity implements SwipeRefreshLayout.OnRefreshListener, View.OnClickListener {

    private static final String TAG = ProfileActivity.class.getSimpleName();

    public static String SELECTED_USERNAME = "selected_username";
    private String mstrUsername;

    private SwipeRefreshLayout mRefreshLayout;

    private int mnCountOnce = 9;
    private int mnCurrentCount = 0;
    private boolean mbNeedMore = false;

    ArrayList<ItemData> maryItem = new ArrayList<ItemData>();

    private RecyclerView mRecyclerView;
    private ProfileItemAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;

    private UserData mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile);

        mUser = CommonUtils.mUserSelected;
        // get username
        Intent intent = getIntent();
        if (intent.hasExtra(SELECTED_USERNAME)) {
            mstrUsername = intent.getStringExtra(SELECTED_USERNAME);
        }

        // init view
        Button butBack = (Button)findViewById(R.id.but_back);
        butBack.setOnClickListener(this);

        ImageView imgEdit = (ImageView)findViewById(R.id.imgview_edit);
        imgEdit.setOnClickListener(this);

        if (mUser != null) {
            butBack.setVisibility(View.VISIBLE);
            imgEdit.setVisibility(View.GONE);
        }
        else {
            butBack.setVisibility(View.GONE);
            imgEdit.setVisibility(View.VISIBLE);

            mUser = (UserData) UserData.getCurrentUser();
        }

        mRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swiperefresh);
        mRefreshLayout.setOnRefreshListener(this);

        // init list
        mRecyclerView = (RecyclerView)findViewById(R.id.list);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mAdapter = new ProfileItemAdapter(this, maryItem, mUser, mstrUsername);
        mRecyclerView.setAdapter(mAdapter);

        mUser.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                mAdapter.notifyDataSetChanged();
            }
        });

        getBlog(false);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mAdapter.notifyDataSetChanged();

        if (CommonUtils.mUserSelected != null) {
            mUser.getFollowingUser(new DeedCallbackInterface() {
                @Override
                public void onSuccess() {
                    mAdapter.notifyDataSetChanged();
                }
            });
        }

        // get follower
        mUser.getFollowerUser(new DeedCallbackInterface() {
            @Override
            public void onSuccess() {
                mAdapter.notifyDataSetChanged();
            }
        });

        // get review count
        ParseRelation<NotificationData> relation = mUser.getRelation("review");
        ParseQuery<NotificationData> relationQuery = relation.getQuery();

        relationQuery.countInBackground(new CountCallback() {
            @Override
            public void done(int i, ParseException e) {
                if (e == null) {
                    mUser.mnReviewCount = i;
                    mAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();

        CommonUtils.mUserSelected = null;
    }

    public void getBlog(final boolean bRefresh) {
        if (bRefresh) { // refreshing
            mnCurrentCount = 0;
        }

        // get blog data
        ParseQuery<ItemData> query = ParseQuery.getQuery(ItemData.class);
        query.whereEqualTo("user", mUser);

        query.orderByDescending("createdAt");
        query.setSkip(mnCurrentCount);
        query.setLimit(mnCountOnce);

        query.findInBackground(new FindCallback<ItemData>() {
            @Override
            public void done(List<ItemData> list, ParseException e) {
                stopRefresh();

                if (e == null) {
                    if (bRefresh) {
                        mAdapter.notifyItemRangeRemoved(1, mAdapter.getItemCount() - 1);
                        maryItem.clear();
                    }

                    if (list.size() > 0) {
                        mbNeedMore = (list.size() == mnCountOnce);

                        for (ItemData iData : list) {
                            maryItem.add(iData);
                        }
                    } else {
                        mbNeedMore = false;
                    }

                    mAdapter.mbNeedMore = mbNeedMore;

                    if (mnCurrentCount > 0) {
                        mAdapter.notifyItemRemoved(mnCurrentCount + 1);
                    }

                    int nAdded = list.size();
                    if (mbNeedMore) {
                        nAdded++;
                    }
                    mAdapter.notifyItemRangeInserted(mnCurrentCount + 1, nAdded);

                    mnCurrentCount = maryItem.size();
                }
                else {
//                    Log.d(TAG, e.getLocalizedMessage());
                }
            }
        });
    }

    private void stopRefresh() {
        mRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onRefresh() {
        getBlog(true);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        switch (id) {
            case R.id.but_back:
                onBackPressed();
                break;

            case R.id.imgview_edit:
                CommonUtils.moveNextActivity(this, SignupActivity.class, false);
                break;
        }
    }

    public void reloadTable() {
        mAdapter.notifyDataSetChanged();
    }
}
