package de.ironjan.mensaupb.sync;

import android.accounts.*;
import android.content.*;

import org.androidannotations.annotations.*;
import org.slf4j.*;

@EBean
public class AccountCreator {
    /**
     * Neded for synchroniztation initialization
     */
    public static final String AUTHORITY = ProviderContract.AUTHORITY,

            ACCOUNT = ProviderContract.ACCOUNT;
    public static final String ACCOUNT_TYPE = ProviderContract.ACCOUNT_TYPE;
    private final Logger LOGGER = LoggerFactory.getLogger(AccountCreator.class.getSimpleName());
    @RootContext
    Context mContext;
    @SystemService
    AccountManager mAccountManager;
    private boolean mAccountCreated = false;
    private Account mAccount;

    public Account build(Context context) {
        if (mAccount == null) {
            mAccount = new Account(ACCOUNT, ACCOUNT_TYPE);
            mAccountCreated = mAccountManager.addAccountExplicitly(mAccount, null, null);
            if (mAccountCreated) {
                LOGGER.info("Synchronization account added.");
            } else {
                LOGGER.warn("Account already existed.");
            }
        }
        return mAccount;
    }


    public String getAuthority() {
        return AUTHORITY;
    }
}