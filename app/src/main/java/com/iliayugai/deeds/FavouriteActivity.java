package com.iliayugai.deeds;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.iliayugai.deeds.adapter.FavouriteItemAdapter;
import com.iliayugai.deeds.model.ItemData;
import com.iliayugai.deeds.model.UserData;

import java.util.ArrayList;

/**
 * Created by highjump on 15-6-1.
 */
public class FavouriteActivity extends Activity {

    private static final String TAG = FavouriteActivity.class.getSimpleName();

    private ItemData mItemSelected;
    private ArrayList<ItemData> maryItem = new ArrayList<ItemData>();

    private RecyclerView mRecyclerView;
    private FavouriteItemAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;

    private EditText mEditSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_favourite);

        mEditSearch = (EditText)findViewById(R.id.edit_search);

        // init list
        mRecyclerView = (RecyclerView)findViewById(R.id.list);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mAdapter = new FavouriteItemAdapter(this, maryItem);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        onButSearch();
    }

    public void onButSearch() {
        UserData currentUser = (UserData) UserData.getCurrentUser();
        maryItem.clear();

        String strSearch = mEditSearch.getText().toString();

        // get items with search keyword
        for (ItemData iData : currentUser.maryFavouriteItem) {
            if (TextUtils.isEmpty(strSearch)) {
                maryItem.add(iData);
                continue;
            }

            if (iData.getTitle().contains(strSearch)) {
                maryItem.add(iData);
            }
        }

        mAdapter.notifyDataSetChanged();
    }
}
