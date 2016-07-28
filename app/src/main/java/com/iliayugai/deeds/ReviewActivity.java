package com.iliayugai.deeds;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.iliayugai.deeds.adapter.ReviewItemAdapter;
import com.iliayugai.deeds.model.NotificationData;
import com.iliayugai.deeds.model.UserData;
import com.iliayugai.deeds.utils.CommonUtils;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;

import java.util.List;

/**
 * Created by highjump on 15-6-11.
 */
public class ReviewActivity extends Activity implements View.OnClickListener {

    private UserData mUser;

    private RecyclerView mRecyclerView;
    private ReviewItemAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;

    public static String SELECTED_USERNAME = "selected_username";
    private String mstrUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_review);

        mUser = CommonUtils.mUserSelected;
        // get username
        Intent intent = getIntent();
        if (intent.hasExtra(SELECTED_USERNAME)) {
            mstrUsername = intent.getStringExtra(SELECTED_USERNAME);
        }

        UserData currentUser = (UserData) UserData.getCurrentUser();

        // init view
        Button butBack = (Button)findViewById(R.id.but_back);
        butBack.setOnClickListener(this);

        ImageView butRate = (ImageView)findViewById(R.id.imgview_rate);
        butRate.setOnClickListener(this);

        if (mUser.getObjectId().equals(currentUser.getObjectId())) {
            butRate.setVisibility(View.GONE);
        }

        // init list
        mRecyclerView = (RecyclerView)findViewById(R.id.list);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new ReviewItemAdapter(this, mUser);
        mRecyclerView.setAdapter(mAdapter);

        // get review info
        ParseRelation<NotificationData> relation = mUser.getRelation("review");
        ParseQuery<NotificationData> reviewQuery = relation.getQuery();

        reviewQuery.orderByDescending("createdAt");

        reviewQuery.findInBackground(new FindCallback<NotificationData>() {
            @Override
            public void done(List<NotificationData> list, ParseException e) {
                if (e == null) {
                    mUser.maryReview.clear();

                    for (NotificationData nData : list) {
                        mUser.maryReview.add(nData);
                    }

                    mAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        switch (id) {
            case R.id.imgview_rate:
                CommonUtils.mUserSelected = mUser;
                CommonUtils.moveNextActivity(this, RateActivity.class, false);
                break;

            case R.id.but_back:
                onBackPressed();
        }
    }
}
