package de.ironjan.mensaupb.fragments;


import android.app.Activity;
import android.content.*;
import android.os.*;
import android.support.v4.app.*;
import android.support.v4.widget.*;
import android.view.*;
import android.widget.Toast;

import org.androidannotations.annotations.*;
import org.androidannotations.annotations.sharedpreferences.*;
import org.slf4j.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import de.ironjan.mensaupb.BuildConfig;
import de.ironjan.mensaupb.R;
import de.ironjan.mensaupb.activities.*;
import de.ironjan.mensaupb.adapters.*;
import de.ironjan.mensaupb.prefs.*;
import de.ironjan.mensaupb.stw.opening_times.OpeningTimesKeeper;
import de.ironjan.mensaupb.sync.*;
import se.emilsjolander.stickylistheaders.*;

@SuppressWarnings("WeakerAccess")
@EFragment(R.layout.fragment_menu_listing)
public class MenuListingFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    public static final String ARG_DATE = "date";
    public static final String ARG_LOCATION = "restaurant";
    public static final int TIMEOUT_30_SECONDS = 30000;

    private final Logger LOGGER = LoggerFactory.getLogger(MenuListingFragment.class.getSimpleName());

    @ViewById(android.R.id.empty)
    View mLoadingView;
    @ViewById(R.id.closed)
    View mClosed;
    @ViewById(android.R.id.list)
    StickyListHeadersListView list;
    @Pref
    InternalKeyValueStore_ mInternalKeyValueStore;

    @Bean
    AccountCreator mAccountCreator;
    private MenuListingAdapter adapter;
    private MenusNavigationCallback navigationCallback;

    public static MenuListingFragment getInstance(String dateAsKey, String restaurant) {
        MenuListingFragment fragment = new MenuListingFragment_();
        Bundle arguments = new Bundle();

        arguments.putString(MenuListingFragment.ARG_DATE, dateAsKey);
        arguments.putString(MenuListingFragment.ARG_LOCATION, restaurant);

        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        if (!(activity instanceof MenusNavigationCallback)) {
            throw new IllegalArgumentException("MenuListingFragment can only be attached to an Activity implementing MenusNavigationCallback.");
        }
        super.onAttach(activity);
        this.navigationCallback = (MenusNavigationCallback) activity;

    }

    private String getArgLocation() {
        String location = getArguments().getString(ARG_LOCATION);
        return location.replaceAll("\\*", "%");
    }

    private String getArgDate() {
        return getArguments().getString(ARG_DATE);
    }

    @AfterViews
    void loadContent() {
        adapter = new MenuListingAdapter(getActivity(), getArgDate(), getArgLocation());
        adapter.setViewBinder(new MenuDetailViewBinder());
        getLoaderManager().initLoader(0, null, adapter);
        list.setAdapter(adapter);
        list.setAreHeadersSticky(false);
        updateEmptyView();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(TIMEOUT_30_SECONDS);
                    updateLoadingMessage();
                } catch (InterruptedException e) {
                    // ignored
                }

            }
        }).start();
    }

    private void updateEmptyView() {
        if (OpeningTimesKeeper.isOpenOn(getArgLocation(), getArgDate())) {
            list.setEmptyView(mLoadingView);
        } else {
            list.setEmptyView(mClosed);
        }
    }

    @UiThread
    void updateLoadingMessage() {
        long lastSyncTimeStamp = mInternalKeyValueStore.lastSyncTimeStamp().get();
        if (0L == lastSyncTimeStamp) {
            updateEmptyView();
        } else {
            list.setEmptyView(mClosed);
        }
    }

    @ItemClick
    void listItemClicked(int pos) {
        if (BuildConfig.DEBUG) LOGGER.debug("listItemClicked({})", pos);

        final long _id = adapter.getItemId(pos);
        navigationCallback.showMenu(_id);

        if (BuildConfig.DEBUG) LOGGER.debug("listItemClicked({}) done", pos);
    }

    @Override
    public void onRefresh() {
        Bundle settingsBundle = new Bundle();
        settingsBundle.putBoolean(
                ContentResolver.SYNC_EXTRAS_MANUAL, true);
        settingsBundle.putBoolean(
                ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        ContentResolver.requestSync(mAccountCreator.getAccount(), AccountCreator.AUTHORITY, settingsBundle);
        LOGGER.debug("Sync requested.");
    }

    @OptionsItem(R.id.ab_showtimes)
    void showTimes() {
        String msg = DateFormat.getTimeInstance().format(
                        OpeningTimesKeeper.hasCheapFoodUntil(getArgLocation(), getArgDate()));
        Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
    }

}

