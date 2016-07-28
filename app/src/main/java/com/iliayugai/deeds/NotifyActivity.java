package com.iliayugai.deeds;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.iliayugai.deeds.adapter.NotifyItemAdapter;
import com.iliayugai.deeds.model.ItemData;
import com.iliayugai.deeds.model.NotificationData;
import com.iliayugai.deeds.model.UserData;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by highjump on 15-6-1.
 */
public class NotifyActivity extends Activity implements SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = NotifyActivity.class.getSimpleName();

    private SwipeRefreshLayout mRefreshLayout;

    private int mnCountOnce = 13;
    private int mnCurrentCount = 0;
    private boolean mbNeedMore = false;

    ArrayList<NotificationData> maryNotify = new ArrayList<NotificationData>();

    private RecyclerView mRecyclerView;
    private NotifyItemAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_notification);

        // init view
        mRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swiperefresh);
        mRefreshLayout.setOnRefreshListener(this);

        // init list
        mRecyclerView = (RecyclerView)findViewById(R.id.list);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new NotifyItemAdapter(this, maryNotify);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getNotification(false, true);
            }
        }, 500);
    }

    public void getNotification(final boolean bRefresh, boolean bAnimation) {
        UserData currentUser = (UserData) UserData.getCurrentUser();

        if (bRefresh) {
            mnCurrentCount = 0;
        }

        if (bAnimation) {
            if (!mRefreshLayout.isRefreshing()) {
                mRefreshLayout.setRefreshing(true);
            }
        }

        ParseQuery<NotificationData> query = ParseQuery.getQuery(NotificationData.class);
        query.whereEqualTo("targetuser", currentUser);
        query.whereLessThanOrEqualTo("type", NotificationData.NOTIFICATION_MENTION);

        query.include("user");
        query.include("item");

        query.orderByDescending("createdAt");

        query.setSkip(mnCurrentCount);
        query.setLimit(mnCountOnce);

        query.findInBackground(new FindCallback<NotificationData>() {
            @Override
            public void done(List<NotificationData> list, ParseException e) {
                stopRefresh();

                if (e == null) {
                    if (bRefresh) {
                        mAdapter.notifyItemRangeRemoved(0, mAdapter.getItemCount());
                        maryNotify.clear();
                    }

                    if (list.size() > 0) {
                        mbNeedMore = (list.size() == mnCountOnce);

                        for (NotificationData nData : list) {
                            maryNotify.add(nData);
                        }
                    } else {
                        mbNeedMore = false;
                    }

                    mAdapter.mbNeedMore = mbNeedMore;

                    if (mnCurrentCount > 0) {
                        mAdapter.notifyItemRemoved(mnCurrentCount);
                    }

                    int nAdded = list.size();
                    if (mbNeedMore) {
                        nAdded++;
                    }
                    mAdapter.notifyItemRangeInserted(mnCurrentCount, nAdded);

                    mnCurrentCount = maryNotify.size();
                }
            }
        });
    }

    private void stopRefresh() {
        mRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onRefresh() {
        getNotification(true, false);
    }
}
