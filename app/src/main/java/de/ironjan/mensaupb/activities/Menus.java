package de.ironjan.mensaupb.activities;


import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.Trace;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;

import de.ironjan.mensaupb.BuildConfig;
import de.ironjan.mensaupb.R;
import de.ironjan.mensaupb.adapters.WeekdayHelper;
import de.ironjan.mensaupb.adapters.WeekdayPagerAdapter;
import de.ironjan.mensaupb.prefs.InternalKeyValueStore_;
import de.ironjan.mensaupb.stw.Restaurant;
import de.ironjan.mensaupb.stw.opening_times.OpeningTimesKeeper;
import de.ironjan.mensaupb.sync.AccountCreator;

@SuppressWarnings("WeakerAccess")
@SuppressLint("Registered")
@EActivity(R.layout.activity_menu_listing)
@OptionsMenu(R.menu.main)
public class Menus extends ActionBarActivity implements ActionBar.OnNavigationListener, MenusNavigationCallback {

    public static final String KEY_DAY_OFFSET = "KEY_DAY_OFFSET";
    public static final String KEY_DATE = "KEY_DATE";
    public static final String KEY_RESTAURANT = "KEY_RESTAURANT";
    private final Logger LOGGER = LoggerFactory.getLogger(Menus.class.getSimpleName());
    @Extra(value = KEY_DATE)
    String dateAsString = null;

    @ViewById(R.id.pager)
    ViewPager mViewPager;
    @ViewById(R.id.pager_title_strip)
    PagerTabStrip mPagerTabStrip;
    String[] mRestaurantKeys = Restaurant.getKeys();
    Integer[] mRestaurantNameIds = Restaurant.getNameStringIds();
    @Bean
    WeekdayHelper mwWeekdayHelper;
    @Bean
    AccountCreator mAccountCreator;
    @Extra(value = KEY_RESTAURANT)
    String restaurant = mRestaurantKeys[0];
    @InstanceState
    int mLocation = 0;
    @InstanceState
    int mDayOffset = 0;
    @Pref
    InternalKeyValueStore_ mInternalKeyValueStore;
    @Bean
    WeekdayHelper mWeekdayHelper;
    @StringRes(R.string.openUntil)
    String openUntil;
    private WeekdayPagerAdapter[] adapters;
    private WeekdayPagerAdapter mWeekdayPagerAdapter;

    @Trace
    @AfterViews
    @Background
    void init() {
        mLocation = mInternalKeyValueStore.lastLocation().get();
        initPager();
        initActionBar();
    }

    @Trace
    @UiThread
    void initActionBar() {
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        int restaurantCount = mRestaurantNameIds.length;
        String[] mDisplayedRestaurantNames = new String[restaurantCount];
        final Resources resources = getResources();
        for (int i = 0; i < restaurantCount; i++) {
            mDisplayedRestaurantNames[i] = resources.getString(mRestaurantNameIds[i]);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(actionBar.getThemedContext(),
                android.R.layout.simple_spinner_item, android.R.id.text1,
                mDisplayedRestaurantNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        actionBar.setListNavigationCallbacks(adapter, this);
        actionBar.setSelectedNavigationItem(mLocation);
    }

    @Trace
    @UiThread
    void initPager() {
        mPagerTabStrip.setTabIndicatorColorResource(R.color.iconBg);
        mPagerTabStrip.setDrawFullUnderline(true);

        loadPagerAdapter(mLocation);
    }

    @Background
    @Trace
    void loadPagerAdapter(int i) {
        if (BuildConfig.DEBUG) LOGGER.debug("loadPagerAdapter({})", i);
        mWeekdayPagerAdapter =
                getPagerAdapter(i);
        if (BuildConfig.DEBUG) LOGGER.info("Got adapter: {}", mWeekdayPagerAdapter);
        if (mWeekdayPagerAdapter != null) {
            switchAdapterTo(mDayOffset);
        }
    }

    @UiThread
    void switchAdapterTo(int currentItem) {
        LOGGER.warn("switch to {}", currentItem);
        mViewPager.setAdapter(mWeekdayPagerAdapter);
        mViewPager.setCurrentItem(currentItem);
    }

    @Trace
    WeekdayPagerAdapter getPagerAdapter(int i) {
        if (BuildConfig.DEBUG) LOGGER.debug("getPagerAdapter({})", i);
        if (adapters == null) {
            adapters = new WeekdayPagerAdapter[mRestaurantKeys.length];
        }
        if (adapters[i] == null) {
            createNewAdapter(i);
        }
        return adapters[i];
    }

    @Trace
    @Background
    void createNewAdapter(int i) {
        if (BuildConfig.DEBUG) LOGGER.debug("createNewAdapter({})", i);
        adapters[i] =
                new WeekdayPagerAdapter(this, getSupportFragmentManager(), mRestaurantKeys[i]);
        loadPagerAdapter(i);
    }


    @Override
    @Trace
    public boolean onNavigationItemSelected(int i, long l) {
        mLocation = i;
        if (BuildConfig.DEBUG)
            LOGGER.debug("onNavigationItemSelected({},{}), location := {}", new Object[]{i, l, mLocation});
        mDayOffset = mViewPager.getCurrentItem();
        loadPagerAdapter(i);

        restaurant = mRestaurantKeys[i];
        return true;
    }


    @Override
    public void showMenu(long _id) {
        MenuDetails_.intent(this).menuId(_id).start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mInternalKeyValueStore.edit().lastLocation().put(mLocation).apply();
    }


    @OptionsItem(R.id.ab_showtimes)
    void showTimes() {
        Date time = OpeningTimesKeeper.hasCheapFoodUntil(restaurant, mwWeekdayHelper.getNextWeekDayAsKey(mDayOffset));
        String formattedTime = new SimpleDateFormat("HH:mm").format(time);
        String msg = String.format(openUntil, formattedTime);
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    @OptionsItem(R.id.ab_refresh)
    void refreshClicked() {
        Bundle settingsBundle = new Bundle();
        settingsBundle.putBoolean(
                ContentResolver.SYNC_EXTRAS_MANUAL, true);
        settingsBundle.putBoolean(
                ContentResolver.SYNC_EXTRAS_EXPEDITED, true);

        ContentResolver.requestSync(mAccountCreator.getAccount(), mAccountCreator.getAuthority(), settingsBundle);
    }

    @OptionsItem(R.id.ab_about)
    void aboutClicked() {
        About_.intent(this).start();
    }
}
