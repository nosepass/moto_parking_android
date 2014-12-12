package com.github.nosepass.motoparking;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.nosepass.motoparking.http.HttpService;
import com.github.nosepass.motoparking.http.Login;
import com.github.nosepass.motoparking.http.UpdateUser;
import com.github.nosepass.motoparking.views.MaterialProgressDrawable;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

/**
 * This shows user account details. Currently you can only change your nickname.
 */
public class AccountFragment extends Fragment {
    private static final String TAG = "AccountFragment";
    //private static final String CLSNAME = AccountFragment.class.getName();

    private SharedPreferences prefs;
    private TelephonyManager telephonyManager;
    private ConnectivityManager connectivityManager;
    private BroadcastReceiver loginCompleteReceiver = new ServerOpCompleteReceiver();

    @InjectView(R.id.content)
    ViewGroup content;
    @InjectView(R.id.progress)
    ViewGroup progress;
    @InjectView(R.id.progressBar)
    ProgressBar progressBar;
    @InjectView(R.id.progress_message)
    TextView progressMessage;
    @InjectView(R.id.nickname)
    EditText nickname;
    @InjectView(R.id.password)
    EditText password;
    @InjectView(R.id.setPassword)
    Button setPassword;
    @InjectView(R.id.newPassword)
    EditText newPassword;
    @InjectView(R.id.newPasswordConfirm)
    EditText newPasswordConfirm;
    @InjectView(R.id.save)
    Button save;

    public AccountFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        MyLog.v(TAG, "onCreate state=" + savedInstanceState);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        MyLog.v(TAG, "onCreateView");
        prefs = PreferenceManager.getDefaultSharedPreferences(container.getContext());
        Context c = container.getContext();
        telephonyManager = (TelephonyManager) c.getSystemService(Context.TELEPHONY_SERVICE);
        connectivityManager = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        View rootView = inflater.inflate(R.layout.fragment_account, container, false);
        ButterKnife.inject(this, rootView);

        Drawable d = getMaterialStyleLoaderIfNotLollipop(c, progressBar);
        if (d != null) {
            progressBar.setIndeterminateDrawable(d);
        }

        content.setVisibility(View.INVISIBLE);

        nickname.setText(prefs.getString(PrefKeys.NICKNAME, ""));
        password.setText(prefs.getString(PrefKeys.PASSWORD, ""));

        setPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSetPasswordClick();
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSaveClick();
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Login.COMPLETE_INTENT);
        filter.addAction(Login.FAIL_INTENT);
        filter.addAction(UpdateUser.COMPLETE_INTENT);
        filter.addAction(UpdateUser.FAIL_INTENT);
        getActivity().registerReceiver(loginCompleteReceiver, filter);
        HttpService.addSyncAction(getActivity(), new Login(prefs, telephonyManager), true);
        checkConnectivity();
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(loginCompleteReceiver);
    }

    private void checkConnectivity() {
        if (!MyUtil.isNetworkAvailable(connectivityManager)) {
            showContent();
            Crouton.makeText(getActivity(), R.string.account_no_network, Style.ALERT).show();
        }
    }

    private void showContent() {
        progress.setVisibility(View.INVISIBLE);
        content.setVisibility(View.VISIBLE);
    }

    private void showProgress(int progressMsgResId) {
        progressMessage.setText(progressMsgResId);
        progress.setVisibility(View.VISIBLE);
        content.setVisibility(View.INVISIBLE);
    }

    private void onSetPasswordClick() {
        MyLog.v(TAG, "onMoveClick");
    }

    private void onSaveClick() {
        MyLog.v(TAG, "onSaveClick");

        boolean hasError = validate();
        if (!hasError) {
            showProgress(R.string.account_progress_save);
            long userId = prefs.getLong(PrefKeys.USER_ID, -1);
            String newNick = nickname.getText() + "";
            String deviceId = telephonyManager.getDeviceId();
            HttpService.addSyncAction(getActivity(), new UpdateUser(userId, newNick, deviceId));
        }
    }

    private boolean validate() {
        nickname.setError(null);
        password.setError(null);

        String reqmsg = getString(R.string.error_field_required);
        boolean hasError = false;
        if (TextUtils.isEmpty(nickname.getText())) {
            nickname.setError(reqmsg);
            hasError = true;
        }
        if (TextUtils.isEmpty(password.getText())) {
            password.setError(reqmsg);
            hasError = true;
        }
        return hasError;
    }

    @SuppressWarnings("unchecked")
    private Drawable getMaterialStyleLoaderIfNotLollipop(Context c, View parent) {
        // Use a copy of the hidden support-v4 Material loader drawable
        // TODO use support-v4 via reflection instead of copying?
        if (MyUtil.beforeLollipop()) {
            MaterialProgressDrawable mpd = new MaterialProgressDrawable(c, parent);
            mpd.setBackgroundColor(0xFFFAFAFA);
            mpd.setAlpha(128);
            mpd.start();
            return mpd;
        }
        return null;
    }

    private class ServerOpCompleteReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent i) {
            MyLog.v(TAG, "got login attempt completion");
            String act = i.getAction();
            if (getActivity() != null) {
                showContent();
            }
            if (Login.COMPLETE_INTENT.equals(act)) {
                MyLog.v(TAG, "login succeeded");
            } else if (Login.FAIL_INTENT.equals(act)) {
                MyLog.v(TAG, "login failed");
                //boolean invalidUserOrPw = i.getBooleanExtra(Login.EXTRA_INVALID_CREDENTIALS, false);
                if (getActivity() != null) {
                    Crouton.makeText(getActivity(), R.string.account_save_error, Style.ALERT).show();
                }
            } else if (UpdateUser.COMPLETE_INTENT.equals(act)) {
                MyLog.v(TAG, "update succeeded");
                if (getActivity() != null) {
                    Crouton.makeText(getActivity(), R.string.account_save_success, Style.CONFIRM).show();
                }
            } else if (UpdateUser.FAIL_INTENT.equals(act)) {
                MyLog.v(TAG, "update failed");
                if (getActivity() != null) {
                    Crouton.makeText(getActivity(), R.string.account_save_error, Style.ALERT).show();
                }
            }
        }
    }
}
