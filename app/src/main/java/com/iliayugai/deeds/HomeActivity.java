package com.iliayugai.deeds;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.iliayugai.deeds.adapter.HomeItemAdapter;
import com.iliayugai.deeds.model.ItemData;
import com.iliayugai.deeds.model.UserData;
import com.iliayugai.deeds.utils.CommonUtils;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by highjump on 15-6-1.
 */
public class HomeActivity extends Activity implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener, TextView.OnEditorActionListener {

    private static final String TAG = HomeActivity.class.getSimpleName();

    private static int QUERY_LAST = 0;
    private static int QUERY_NEAR = 1;

    private int mnQueryType = QUERY_LAST;

    private EditText mEditSearch;
    private View mViewUnderline;

    private Button mButLast;
    private Button mButNear;

    private SwipeRefreshLayout mRefreshLayout;

    private int mnCountOnce = 6;
    private int mnCurrentCount = 0;
    private boolean mbNeedMore = false;

    ArrayList<ItemData> maryBlog = new ArrayList<ItemData>();

    private RecyclerView mRecyclerView;
    private HomeItemAdapter mAdapter;
    private GridLayoutManager mLayoutManager;

    private ParseQuery mQueryOld = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);

        // init
        ImageView imgView = (ImageView)findViewById(R.id.imgview_menu);
        imgView.setOnClickListener(this);

        imgView = (ImageView)findViewById(R.id.imgview_filter);
        imgView.setOnClickListener(this);

        mRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swiperefresh);
        mRefreshLayout.setOnRefreshListener(this);

        mEditSearch = (EditText)findViewById(R.id.edit_search);
        mEditSearch.setOnEditorActionListener(this);

        mViewUnderline = findViewById(R.id.view_underline);

        imgView = (ImageView)findViewById(R.id.imgview_search);
        imgView.setOnClickListener(this);

        mButLast = (Button)findViewById(R.id.but_last);
        mButLast.setOnClickListener(this);

        mButNear = (Button)findViewById(R.id.but_near);
        mButNear.setOnClickListener(this);

        // init list
        mRecyclerView = (RecyclerView)findViewById(R.id.list);

        mLayoutManager = new GridLayoutManager(this, 2);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mRecyclerView.setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        // set footer
        mLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                int nSize = 1;

                if (mbNeedMore) {
                    if (position == maryBlog.size()) { // footer
                        nSize = 2;
                    }
                }
//                            Log.d(TAG, "getSpanSize: " + position + ", " + nSize);

                return nSize;
            }
        });

        mAdapter = new HomeItemAdapter(this, maryBlog);
        mRecyclerView.setAdapter(mAdapter);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getBlogWithProgress(true, false, false);
            }
        }, 500);
    }

    @Override
    protected void onResume() {
        super.onResume();

        reloadTable();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        switch (id) {
            case R.id.imgview_menu:
                CommonUtils.mMainActivity.onButLeftMenu();
                break;

            case R.id.imgview_filter:
                CommonUtils.mMainActivity.onButRightMenu();
                break;

            case R.id.imgview_search:
                getBlogWithProgress(true, true, true);
                break;

            case R.id.but_last:
                setQueryType(QUERY_LAST);
                break;

            case R.id.but_near:
                setQueryType(QUERY_NEAR);
                break;
        }
    }

    public void setQueryType(final int type) {
        dismissKeyboard();

        if (type == mnQueryType) {
            return;
        }

        mnQueryType = type;

        int nColorTheme = getResources().getColor(R.color.color_theme);
        int nColorGray = getResources().getColor(R.color.color_gray);

        final TranslateAnimation transAlpha;
        final int nWidth = mViewUnderline.getWidth();

        if (type == QUERY_NEAR) {
            transAlpha = new TranslateAnimation(0, nWidth, 0, 0);

            mButLast.setTextColor(nColorGray);
            mButNear.setTextColor(nColorTheme);
        }
        else {
            transAlpha = new TranslateAnimation(0, -nWidth, 0, 0);

            mButLast.setTextColor(nColorTheme);
            mButNear.setTextColor(nColorGray);
        }

        transAlpha.setDuration(200);
        transAlpha.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                TranslateAnimation anim = new TranslateAnimation(0, 0, 0, 0);
                mViewUnderline.setAnimation(anim);

                LinearLayout.LayoutParams underlp = (LinearLayout.LayoutParams) mViewUnderline.getLayoutParams();

                if (type == QUERY_NEAR) {
                    underlp.leftMargin = nWidth;
                    underlp.rightMargin = -nWidth;
                } else {
                    underlp.leftMargin = 0;
                    underlp.rightMargin = 0;
                }

                mViewUnderline.setLayoutParams(underlp);

                getBlogWithProgress(true, true, true);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        mViewUnderline.startAnimation(transAlpha);
    }

    public void dismissKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public void getBlogWithProgress(boolean bAnimation, boolean bRefresh, boolean bCancelOld) {

        if (bCancelOld) {
            if (mQueryOld != null) {
                mQueryOld.cancel();
                stopRefresh();
            }
        }
        else {
            if (mQueryOld != null) {
                return;
            }
        }

        if (bAnimation) {
            if (!mRefreshLayout.isRefreshing()) {
                mRefreshLayout.setRefreshing(true);
            }
        }

        getBlog(bRefresh);
    }

    private void stopRefresh() {
        mQueryOld = null;
        mRefreshLayout.setRefreshing(false);
    }

    public void getBlog(final boolean bRefresh) {
        if (bRefresh) { // refreshing
            mnCurrentCount = 0;
            mbNeedMore = false;
        }

        // get blog data
        ParseQuery<ItemData> query = ParseQuery.getQuery(ItemData.class);

        String strSearch = mEditSearch.getText().toString();
        if (!TextUtils.isEmpty(strSearch)) {
            ParseQuery<ItemData> titleQuery = ParseQuery.getQuery(ItemData.class);
            titleQuery.whereMatches("title", strSearch, "i");

            ParseQuery<ItemData> addressQuery = ParseQuery.getQuery(ItemData.class);
            addressQuery.whereMatches("address", strSearch, "i");

            List<ParseQuery<ItemData>> listQuery = new ArrayList<ParseQuery<ItemData>>();
            listQuery.add(titleQuery);
            listQuery.add(addressQuery);

            query = ParseQuery.or(listQuery);
        }

        if (!CommonUtils.mbFilterDeed && !CommonUtils.mbFilterNeed) {
            maryBlog.clear();
            mnCurrentCount = 0;

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mAdapter.mbNeedMore = mbNeedMore;
                    mAdapter.notifyDataSetChanged();
                    stopRefresh();
                }
            }, 500);

            return;
        }
        else if (CommonUtils.mbFilterDeed && !CommonUtils.mbFilterNeed) {
            query.whereEqualTo("type", ItemData.ITEMTYPE_DEED);
        }
        else if (!CommonUtils.mbFilterDeed && CommonUtils.mbFilterNeed) {
            query.whereEqualTo("type", ItemData.ITEMTYPE_INNEED);
        }

        if (CommonUtils.mCategorySelected != null) {
            query.whereEqualTo("category", CommonUtils.mCategorySelected);
        }

        if (mnQueryType == QUERY_LAST) {
            query.orderByDescending("createdAt");
        }
        else {
            ParseGeoPoint point;

            if (CommonUtils.mCurrentLocation != null) {
                point = new ParseGeoPoint(CommonUtils.mCurrentLocation.getLatitude(), CommonUtils.mCurrentLocation.getLongitude());
            }
            else {
                UserData currentUser = (UserData) UserData.getCurrentUser();
                point = currentUser.getLocation();
            }

            query.whereWithinKilometers("location", point, CommonUtils.mnFilterDistance);
        }

        query.include("user");

        query.setSkip(mnCurrentCount);
        query.setLimit(mnCountOnce);

        query.findInBackground(new FindCallback<ItemData>() {
            @Override
            public void done(List<ItemData> list, ParseException e) {
                stopRefresh();

                if (e == null) {
                    if (bRefresh) {
                        mAdapter.notifyItemRangeRemoved(0, mAdapter.getItemCount());
                        maryBlog.clear();
                    }

                    if (list.size() > 0) {
                        mbNeedMore = (list.size() == mnCountOnce);

                        for (ItemData iData : list) {
                            maryBlog.add(iData);
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

                    mnCurrentCount = maryBlog.size();

                } else {
//                    Log.d(TAG, e.getLocalizedMessage());
                }
            }
        });

        mQueryOld = query;
    }

    @Override
    public void onRefresh() {
        getBlog(true);
    }

    public void reloadTable() {
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            getBlogWithProgress(true, true, true);
        }

        return false;
    }
}

