package com.iliayugai.deeds;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.iliayugai.deeds.adapter.FollowingItemAdapter;
import com.iliayugai.deeds.model.UserData;
import com.iliayugai.deeds.utils.CommonUtils;

/**
 * Created by highjump on 15-6-10.
 */
public class FollowingActivity extends Activity implements View.OnClickListener {

    public static String FOLLOWING_TYPE = "following_type";
    public static int FOLLOW_FOLLOWING = 0;
    public static int FOLLOW_FOLLOWER = 1;

    private int mnType = FOLLOW_FOLLOWING;

    private RecyclerView mRecyclerView;
    private FollowingItemAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;

    private UserData mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_following);

        mUser = CommonUtils.mUserSelected;

        // get type
        Intent intent = getIntent();
        if (intent.hasExtra(FOLLOWING_TYPE)) {
            mnType = intent.getIntExtra(FOLLOWING_TYPE, FOLLOW_FOLLOWING);
        }

        // init view
        Button button = (Button)findViewById(R.id.but_back);
        button.setOnClickListener(this);

        // init list
        mRecyclerView = (RecyclerView)findViewById(R.id.list);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new FollowingItemAdapter(this, mUser, mnType);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        switch (id) {
            case R.id.but_back:
                onBackPressed();
                break;
        }
    }
}
