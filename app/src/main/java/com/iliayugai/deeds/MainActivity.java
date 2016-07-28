package com.iliayugai.deeds;

import android.app.Activity;
import android.app.LocalActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.iliayugai.deeds.adapter.CategoryAdapter;
import com.iliayugai.deeds.adapter.UserItemAdapter;
import com.iliayugai.deeds.model.CategoryData;
import com.iliayugai.deeds.model.GeneralData;
import com.iliayugai.deeds.model.UserData;
import com.iliayugai.deeds.utils.CommonUtils;
import com.iliayugai.deeds.utils.DeedCallbackInterface;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity implements AdapterView.OnItemClickListener, SeekBar.OnSeekBarChangeListener, View.OnClickListener, SwipeRefreshLayout.OnRefreshListener, TextView.OnEditorActionListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String TAB_HOME = "Home";
    private static final String TAB_FAVOURITE = "Favourite";
    private static final String TAB_ADD = "Add";
    private static final String TAB_PROFILE = "Profile";
    private static final String TAB_NOTIFY = "Notification";

    // LocationManager
    private LocalActivityManager mLocalActivityManager;
    private TabHost mTabHost;

    // sliding menu
    private SlidingMenu mMenu;

    // filter page
    private Switch mSwFilterDeed, mSwFilterNeed;
    private TextView mTextDist;
    private SeekBar mSBFilterDist;
    private ListView mLVFilterCate;
    private CategoryAdapter mAdFilterCate;

    //
    // left page
    //
    private TextView mTextDeedNum;
    private TextView mTextDoneNum;
    private EditText mEditSearch;
    private ImageView mImgviewSearch;

    private SwipeRefreshLayout mRefreshLayout;

    ArrayList<UserData> maryUser = new ArrayList<UserData>();

    private RecyclerView mRecyclerView;
    private UserItemAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;

    private int mnCountOnce = 12;
    private int mnCurrentCount = 0;
    private boolean mbNeedMore = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // init
        mTabHost = (TabHost) findViewById(R.id.tabhost); // The activity TabHost
        mLocalActivityManager = new LocalActivityManager(this, false);
        mLocalActivityManager.dispatchCreate(savedInstanceState);
        mTabHost.setup(mLocalActivityManager);

        TabHost.TabSpec spec; // Reusable TabSpec for each tab
        Intent intent; // Reusable Intent for each tab

        mTabHost.getTabWidget().setDividerDrawable(R.drawable.tab_normal_bg);

        // Create an Intent to launch an Activity for the tab (to be reused)
        intent = new Intent().setClass(this, HomeActivity.class);
        spec = mTabHost.newTabSpec(TAB_HOME)
                .setIndicator(createTabView(TAB_HOME))
                .setContent(intent);
        mTabHost.addTab(spec);

        // Do the same for the other tabs
        intent = new Intent().setClass(this, FavouriteActivity.class);
        spec = mTabHost.newTabSpec(TAB_FAVOURITE)
                .setIndicator(createTabView(TAB_FAVOURITE))
                .setContent(intent);
        mTabHost.addTab(spec);

        intent = new Intent().setClass(this, AddActivity.class);
        spec = mTabHost.newTabSpec(TAB_ADD)
                .setIndicator(createTabView(TAB_ADD))
                .setContent(intent);
        mTabHost.addTab(spec);

        intent = new Intent().setClass(this, ProfileActivity.class);
        spec = mTabHost.newTabSpec(TAB_PROFILE)
                .setIndicator(createTabView(TAB_PROFILE))
                .setContent(intent);
        mTabHost.addTab(spec);

        intent = new Intent().setClass(this, NotifyActivity.class);
        spec = mTabHost.newTabSpec(TAB_NOTIFY)
                .setIndicator(createTabView(TAB_NOTIFY))
                .setContent(intent);
        mTabHost.addTab(spec);

        //set tab which one you want open first time 0 or 1 or 2
        mTabHost.setCurrentTab(0);

        mTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                mMenu.showContent(true);

                reloadTable();
            }
        });

        // init menu
        initSlidingMenu(savedInstanceState);

        CommonUtils.mMainActivity = this;

        initLocation();

        // get category data
        ParseQuery<CategoryData> query = ParseQuery.getQuery(CategoryData.class);
        query.addAscendingOrder("createdAt");
        query.setCachePolicy(ParseQuery.CachePolicy.CACHE_THEN_NETWORK);

        query.findInBackground(new FindCallback<CategoryData>() {
            @Override
            public void done(List<CategoryData> list, ParseException e) {
                if (e == null) {
                    CommonUtils.maryCategory.clear();

                    for (CategoryData cData : list) {
                        CommonUtils.maryCategory.add(cData);
                    }
                }
            }
        });

        // get favourite data
        UserData currentUser = (UserData) UserData.getCurrentUser();
        currentUser.getFavouriteItem(new DeedCallbackInterface() {
            @Override
            public void onSuccess() {
                reloadTable();
            }
        });

        // link user to installation
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        if (installation != null) {
            installation.put("user", currentUser);
            installation.saveInBackground();
        }
    }

    private void reloadTable() {
        String strTag = mTabHost.getCurrentTabTag();

        if (strTag.equals(TAB_HOME)) {
            HomeActivity activityHome = (HomeActivity)mLocalActivityManager.getActivity(strTag);
            activityHome.reloadTable();
        }
        else if (strTag.equals(TAB_FAVOURITE)) {
            FavouriteActivity activityFavourite = (FavouriteActivity)mLocalActivityManager.getActivity(strTag);
            activityFavourite.onButSearch();
        }
        else if (strTag.equals(TAB_PROFILE)) {
            ProfileActivity activityProfile = (ProfileActivity)mLocalActivityManager.getActivity(strTag);
            activityProfile.reloadTable();
        }
    }

    private void initLocation() {
        LocationManager locationManager;
        String serviceName = Context.LOCATION_SERVICE;

        locationManager = (LocationManager)getSystemService(serviceName);

        boolean bGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean bNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        Location location = null;
        String strProvoider = "";

        if (bGpsEnabled) {
            strProvoider = LocationManager.GPS_PROVIDER;
            location = locationManager.getLastKnownLocation(strProvoider);
        }

        if (location == null && bNetworkEnabled) {
            strProvoider = LocationManager.NETWORK_PROVIDER;
            location = locationManager.getLastKnownLocation(strProvoider);
        }

        updateNewLocation(location);

        if (strProvoider.length() > 0) {
            locationManager.requestLocationUpdates(strProvoider, 200000, 100, mLocationListener);
        }
    }

    private final LocationListener mLocationListener = new LocationListener()
    {
        public void onLocationChanged(Location location) {
//            Log.d(TAG, "onLocationChanged");
            updateNewLocation(location);
        }

        public void onProviderDisabled(String provider) {
//            Log.d(TAG, "onProviderDisabled");
            updateNewLocation(null);
        }

        public void onProviderEnabled(String provider) {
//            Log.d(TAG, "onProviderEnabled");
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
//            Log.d(TAG, "onStatusChanged provider: " + provider + ", status: " + status);
        }
    };


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    @Override
    protected void onResume() {
        super.onResume();
        mLocalActivityManager.dispatchResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mLocalActivityManager.dispatchPause(isFinishing());
    }

    private View createTabView(String text) {
        View view = LayoutInflater.from(this).inflate(R.layout.layout_tab_button, null);
        TextView tv = (TextView) view.findViewById(R.id.textView);
        tv.setText(text);

        ImageView iv = (ImageView) view.findViewById(R.id.imageView);

        switch (text) {
            case TAB_HOME:
                iv.setImageResource(R.drawable.ic_tab_home);
                break;

            case TAB_FAVOURITE:
                iv.setImageResource(R.drawable.ic_tab_favourite);
                break;

            case TAB_ADD:
                iv.setImageResource(R.drawable.ic_tab_add);
                break;

            case TAB_PROFILE:
                iv.setImageResource(R.drawable.ic_tab_profile);
                break;

            case TAB_NOTIFY:
                iv.setImageResource(R.drawable.ic_tab_notification);
                break;
        }

        return view;
    }

    private void initSlidingMenu(Bundle restore) {
        final Resources res = getResources();

        mMenu = new SlidingMenu(this);
        mMenu.setMode(SlidingMenu.LEFT_RIGHT);

        mMenu.setSlidingEnabled(false);
        mMenu.setBehindOffsetRes(R.dimen.sliding_menu_offset);
        mMenu.setShadowDrawable(R.drawable.menu_shadow);
        mMenu.setShadowWidthRes(R.dimen.shadow_width);
        mMenu.setSecondaryShadowDrawable(R.drawable.menu_shadow_right);

        mMenu.setFadeDegree(0.35f);

        mMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
        mMenu.attachToActivity(this, SlidingMenu.SLIDING_WINDOW);

        mMenu.setMenu(R.layout.layout_left_menu);
        mMenu.setSecondaryMenu(R.layout.layout_right_menu);

        // init controls in them
        //
        // init right page
        //
        mSwFilterDeed = (Switch)mMenu.findViewById(R.id.switch_deed);
        mSwFilterNeed = (Switch)mMenu.findViewById(R.id.switch_need);

        mSBFilterDist = (SeekBar)mMenu.findViewById(R.id.seekbar_dist);
        mSBFilterDist.setOnSeekBarChangeListener(this);

        mTextDist = (TextView)mMenu.findViewById(R.id.text_dist);

        mLVFilterCate = (ListView)mMenu.findViewById(R.id.listview_category);
        mAdFilterCate = new CategoryAdapter(this, CommonUtils.maryCategory, CommonUtils.mCategorySelected);
        mLVFilterCate.setAdapter(mAdFilterCate);
        mLVFilterCate.setOnItemClickListener(this);

        Button button = (Button)mMenu.findViewById(R.id.but_update);
        button.setOnClickListener(this);

        //
        // init left page
        //
        mTextDeedNum = (TextView)mMenu.findViewById(R.id.text_deed_num);
        mTextDoneNum = (TextView)mMenu.findViewById(R.id.text_done_num);
        mEditSearch = (EditText)mMenu.findViewById(R.id.edit_search);
        mEditSearch.setOnEditorActionListener(this);

        mImgviewSearch = (ImageView)mMenu.findViewById(R.id.imgview_search);
        mImgviewSearch.setOnClickListener(this);

        // init list
        mRefreshLayout = (SwipeRefreshLayout)mMenu.findViewById(R.id.swiperefresh);
        mRefreshLayout.setOnRefreshListener(this);

        mRecyclerView = (RecyclerView)mMenu.findViewById(R.id.list);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mAdapter = new UserItemAdapter(maryUser, this);
        mRecyclerView.setAdapter(mAdapter);

        getItemCount();
    }

    public void getItemCount() {
        ParseQuery<GeneralData> query = ParseQuery.getQuery(GeneralData.class);

        query.getFirstInBackground(new GetCallback<GeneralData>() {
            @Override
            public void done(GeneralData generalData, ParseException e) {
                if (e == null) {
                    CommonUtils.mnItemcount = generalData.getItemcount();
                    CommonUtils.mnItemdone = generalData.getItemdone();

                    setItemText();
                }
            }
        });

        setItemText();
    }

    public void setItemText() {
        mTextDeedNum.setText("" + CommonUtils.mnItemcount);
        mTextDoneNum.setText("" + CommonUtils.mnItemdone);
    }

    public void onButLeftMenu() {
        if (!mMenu.isMenuShowing()) {
            getItemCount();
        }

        mMenu.toggle(true);
    }

    public void onButRightMenu() {
        if (mMenu.isSecondaryMenuShowing()) {
            mMenu.showContent(true);
        }
        else {
            // restore to the saved one
            mSwFilterDeed.setChecked(CommonUtils.mbFilterDeed);
            mSwFilterNeed.setChecked(CommonUtils.mbFilterNeed);
            mSBFilterDist.setProgress(CommonUtils.mnFilterDistance);

            mAdFilterCate.mCategorySelected = CommonUtils.mCategorySelected;
            mAdFilterCate.notifyDataSetChanged();

            mMenu.showSecondaryMenu(true);
        }
    }

    private void updateNewLocation(Location location) {

        Log.d(TAG, "updateNewLocation");

        if (location != null) {
            CommonUtils.mCurrentLocation = location;
        }
        else {
            Toast.makeText(MainActivity.this, "Can't get location info", Toast.LENGTH_LONG).show();
        }
    }

    // filter listview delegate
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (id == 0) {
            mAdFilterCate.mCategorySelected = null;
        }
        else {
            CategoryData cData = CommonUtils.maryCategory.get((int)id - 1);
            mAdFilterCate.mCategorySelected = cData;
        }

        mAdFilterCate.notifyDataSetChanged();
    }

    // seek bar delegate
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        mTextDist.setText(progress + " KM");
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        switch (id) {
            case R.id.imgview_search:
                dismissKeyboard();
                onButSearch(true, true);
                break;

            case R.id.but_update:
                CommonUtils.mbFilterDeed = mSwFilterDeed.isChecked();
                CommonUtils.mbFilterNeed = mSwFilterNeed.isChecked();
                CommonUtils.mnFilterDistance = mSBFilterDist.getProgress();

                CommonUtils.mCategorySelected = mAdFilterCate.mCategorySelected;

                // close side bar
                mMenu.showContent(true);

                // refresh home list
                String strTag = mTabHost.getCurrentTabTag();
                if (strTag.equals(TAB_HOME)) {
                    HomeActivity activityHome = (HomeActivity)mLocalActivityManager.getActivity(strTag);
                    activityHome.getBlogWithProgress(true, true, true);
                }

                break;
        }
    }

    public void onButSearch(boolean animation, final boolean refresh) {
        if (animation) {
            mRefreshLayout.setRefreshing(true);
        }

        if (refresh) {
            mnCurrentCount = 0;
        }

        // get user data
        ParseQuery<UserData> query = ParseQuery.getQuery(UserData.class);
        query.whereMatches("fullname", mEditSearch.getText().toString(), "i");

        query.setSkip(mnCurrentCount);
        query.setLimit(mnCountOnce);

        query.findInBackground(new FindCallback<UserData>() {
            @Override
            public void done(List<UserData> list, ParseException e) {
                stopRefresh();

                if (e == null) {
                    if (refresh) {
                        mAdapter.notifyItemRangeRemoved(0, mAdapter.getItemCount());
                        maryUser.clear();
                    }

                    if (list.size() > 0) {
                        mbNeedMore = (list.size() == mnCountOnce);

                        for (UserData uData : list) {
                            maryUser.add(uData);
                        }
                    }
                    else {
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

                    mnCurrentCount = maryUser.size();
                }
            }
        });
    }

    private void stopRefresh() {
        mRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onRefresh() {
        onButSearch(false, true);
    }

    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            onButSearch(true, true);
        }

        return false;\
    }

    public void dismissKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        String strTag = mTabHost.getCurrentTabTag();

        if (strTag.equals(TAB_ADD)) {
            AddActivity activity = (AddActivity)mLocalActivityManager.getActivity(strTag);
            activity.onActivityResult(requestCode, resultCode, data);
        }
    }
}
