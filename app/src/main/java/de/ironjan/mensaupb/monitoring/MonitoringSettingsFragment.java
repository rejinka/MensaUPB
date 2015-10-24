package de.ironjan.mensaupb.monitoring;

import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.CheckedChange;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;
import org.piwik.sdk.Piwik;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ironjan.mensaupb.MensaUpbApplication;
import de.ironjan.mensaupb.R;

/**
 * Fragment to enable the user to change the app's behaviour about user monitoring.
 */
@SuppressWarnings("WeakerAccess")
@EFragment(R.layout.fragment_monitoring_settings)
public class MonitoringSettingsFragment extends Fragment {

    private final Logger LOGGER = LoggerFactory.getLogger(getClass().getSimpleName());

    @ViewById
    SwitchCompat switchMonitoringEnabled;

    @StringRes
    String anonymousUserStatisticsEnabled, anonymousUserStatisticsDisabled;

    @AfterViews
    void fetchSettings() {
        boolean optOut = getGlobalSettings().isOptOut();
        boolean monitoringEnabled = !optOut;
        switchMonitoringEnabled.setChecked(monitoringEnabled);
        updateSwitchText();
    }

    @CheckedChange(R.id.switchMonitoringEnabled)
    void optOutChange() {
        boolean monitoringEnabled = switchMonitoringEnabled.isChecked();
        boolean optOut = !monitoringEnabled;
        getGlobalSettings().setOptOut(optOut);
        updateSwitchText();
    }

    private Piwik getGlobalSettings() {
        return ((MensaUpbApplication) getActivity().getApplication())
                .getPiwik();
    }

    private void updateSwitchText() {
        boolean monitoringEnabled = switchMonitoringEnabled.isChecked();
        if (monitoringEnabled) {
            switchMonitoringEnabled.setText(anonymousUserStatisticsEnabled);
        } else {
            switchMonitoringEnabled.setText(anonymousUserStatisticsDisabled);
        }
    }
}
