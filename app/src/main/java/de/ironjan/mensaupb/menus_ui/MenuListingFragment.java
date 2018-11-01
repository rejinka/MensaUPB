package de.ironjan.mensaupb.menus_ui;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ProgressBar;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.ironjan.mensaupb.BuildConfig;
import de.ironjan.mensaupb.R;
import de.ironjan.mensaupb.api.ApiFactory;
import de.ironjan.mensaupb.api.MensaUpbApi;
import de.ironjan.mensaupb.model.LocalizedMenu;
import de.ironjan.mensaupb.model.Menu;
import de.ironjan.mensaupb.model.MenuSorter;
import de.ironjan.mensaupb.prefs.InternalKeyValueStore_;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

@SuppressWarnings("WeakerAccess")
@EFragment(R.layout.fragment_menu_listing)
public class MenuListingFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    public static final String ARG_DATE = "date";
    public static final String ARG_LOCATION = "restaurant";
    public static final int TIMEOUT_30_SECONDS = 30000;
    public static final long TWO_MINUTES_IN_MS = 120000L;

    private final Logger LOGGER = LoggerFactory.getLogger(MenuListingFragment.class.getSimpleName());

    @ViewById(R.id.emptyExplanation)
    View mLoadingView;
    @ViewById(R.id.could_not_load_view)
    View mcouldNotLoadView;
    @ViewById(android.R.id.empty)
    View empty;
    @ViewById(android.R.id.list)
    StickyListHeadersListView list;
    @ViewById(R.id.progressBar)
    ProgressBar mProgressBar;

    @Pref
    InternalKeyValueStore_ mInternalKeyValueStore;

    private ArrayBasedMenuListingAdapter adapter;
    private MenusNavigationCallback navigationCallback;
    private long startedAt = Long.MAX_VALUE;

    public static MenuListingFragment getInstance(String dateAsKey, String restaurant) {
        MenuListingFragment fragment = new MenuListingFragment_();
        Bundle arguments = new Bundle();

        arguments.putString(MenuListingFragment.ARG_DATE, dateAsKey);
        arguments.putString(MenuListingFragment.ARG_LOCATION, restaurant);

        fragment.setArguments(arguments);
        return fragment;
    }

    @AfterViews
    void rememberStartupTime() {
        this.startedAt = System.currentTimeMillis();
    }

    @Override
    public void onAttach(Context context) {
        if (!(context instanceof MenusNavigationCallback)) {
            throw new IllegalArgumentException("MenuListingFragment can only be attached to an Activity implementing MenusNavigationCallback.");
        }
        super.onAttach(context);
        this.navigationCallback = (MenusNavigationCallback) context;

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
        MensaUpbApi api = new ApiFactory(getContext()).getApiImplementation();
        api.getSimpleMenus(getArgLocation(), getArgDate())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(menus -> {
                    List<LocalizedMenu> localizedMenus = new ArrayList<>(menus.size());
                    for (Menu m : menus) {
                        localizedMenus.add(new LocalizedMenu(m, isEnglish()));
                    }
                    return localizedMenus;
                })
                .map(menus -> MenuSorter.sort(menus))
                .subscribe(localizedMenus -> showMenus(localizedMenus));

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

    private boolean isEnglish() {
        return Locale.getDefault().getLanguage().startsWith(Locale.ENGLISH.toString());
    }

    private void showMenus(List<LocalizedMenu> menus) {
        adapter = new ArrayBasedMenuListingAdapter(getActivity(), menus);
        list.setAdapter(adapter);
        list.setAreHeadersSticky(false);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                            listItemClicked(position);
                                        }
                                    }
        );
        list.setEmptyView(empty);

    }

    private void updateEmptyView() {
        boolean wasLoadingForALongTime = System.currentTimeMillis() - this.startedAt > TWO_MINUTES_IN_MS;
        if (wasLoadingForALongTime) {
            if (mProgressBar != null) mProgressBar.setVisibility(View.GONE);
        }
    }

    @UiThread
    void updateLoadingMessage() {
        if (list != null) {
            mLoadingView.setVisibility(View.GONE);
            mcouldNotLoadView.setVisibility(View.VISIBLE);
        }
    }

    void listItemClicked(int pos) {
        if (BuildConfig.DEBUG) LOGGER.debug("listItemClicked({})", pos);

        final long _id = adapter.getItemId(pos);
        navigationCallback.showMenu(_id);

        if (BuildConfig.DEBUG) LOGGER.debug("listItemClicked({}) done", pos);
    }

    @Override
    public void onRefresh() {
        /** FIXME Reimplement with new API */
    }


}

