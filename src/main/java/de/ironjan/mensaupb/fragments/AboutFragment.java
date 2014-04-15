package de.ironjan.mensaupb.fragments;

import android.support.v4.app.*;
import android.text.method.*;
import android.widget.*;

import org.androidannotations.annotations.*;
import org.slf4j.*;

import de.ironjan.mensaupb.*;

/**
 * Actual content of {@link de.ironjan.mensaupb.activities.About}.
 */
@EFragment(R.layout.fragment_about)
public class AboutFragment extends Fragment {

    @ViewById(R.id.txtDependencies)
    @FromHtml(R.string.dependencies)
    TextView mTxtDependencies;

    @ViewById(R.id.txtAbout)
    @FromHtml(R.string.aboutText)
    TextView mTxtAbout;

    private final Logger LOGGER = LoggerFactory.getLogger(getClass().getSimpleName());

    @AfterViews
    void linkify() {
        final MovementMethod movementMethod = LinkMovementMethod.getInstance();
        mTxtDependencies.setMovementMethod(movementMethod);
        mTxtAbout.setMovementMethod(movementMethod);
        if (BuildConfig.DEBUG) LOGGER.debug("linkify() done");
    }
}
