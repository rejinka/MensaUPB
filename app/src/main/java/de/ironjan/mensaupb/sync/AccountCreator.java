package de.ironjan.mensaupb.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.SystemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to generate and add an account to the device's account list. Needed for the android
 * sync framework.
 */
@EBean
public class AccountCreator {

    private final Logger LOGGER = LoggerFactory.getLogger(AccountCreator.class.getSimpleName());

    @RootContext
    Context mContext;

    @SystemService
    AccountManager mAccountManager;

    private Account mAccount;

    /**
     * Returns the account associated with this app. Adds it to the account list if necessary
     *
     * @return the account associated with this app
     */
    public Account getAccount() {
        if (mAccount == null) {
            mAccount = new Account(ProviderContract.ACCOUNT, ProviderContract.ACCOUNT_TYPE);
            if (mAccountManager == null) {
                LOGGER.warn("AccountManager was null.");
                return mAccount;
            }
            boolean mAccountCreated = mAccountManager.addAccountExplicitly(mAccount, null, null);
            if (mAccountCreated) {
                LOGGER.info("Synchronization account added.");
            } else {
                LOGGER.warn("Account already existed.");
            }
        }
        return mAccount;
    }

    /**
     * @return the authority string
     */
    public String getAuthority() {
        return ProviderContract.AUTHORITY;
    }
}
