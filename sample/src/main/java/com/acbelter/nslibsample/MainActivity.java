package com.acbelter.nslibsample;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.acbelter.nslib.NetworkApplication;
import com.acbelter.nslib.NetworkServiceCallbackListener;

public class MainActivity extends FragmentActivity implements NetworkServiceCallbackListener {
    private SimpleNetworkServiceHelper mServiceHelper;
    private TextView mDataTextView;
    private Button mGetDataButton;

    private int mRequestId = -1;

    private NetworkApplication getApp() {
        return (NetworkApplication) getApplication();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mServiceHelper = new SimpleNetworkServiceHelper(getApp().getNetworkServiceHelper());

        setContentView(R.layout.activity_main);
        mDataTextView = (TextView) findViewById(R.id.data_text_view);
        mGetDataButton = (Button) findViewById(R.id.get_data_button);
        mGetDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoadingDialogFragment loading = new LoadingDialogFragment();
                loading.show(getSupportFragmentManager(), LoadingDialogFragment.class.getSimpleName());
                mRequestId = mServiceHelper.downloadData();
            }
        });

        if (savedInstanceState != null) {
            mRequestId = savedInstanceState.getInt("request_id");
            mDataTextView.setText(savedInstanceState.getString("text"));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("request_id", mRequestId);
        outState.putCharSequence("text", mDataTextView.getText());
    }

    @Override
    protected void onResume() {
        super.onResume();
        mServiceHelper.addListener(this);

        if (mRequestId != -1 && !mServiceHelper.isPending(mRequestId)) {
            dismissLoadingDialog();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mServiceHelper.removeListener(this);
    }

    public SimpleNetworkServiceHelper getNetworkServiceHelper() {
        return mServiceHelper;
    }

    public void cancelCommand() {
        mServiceHelper.cancelRequest(mRequestId);
    }

    private void dismissLoadingDialog() {
        LoadingDialogFragment loading =
                (LoadingDialogFragment) getSupportFragmentManager()
                        .findFragmentByTag(LoadingDialogFragment.class.getSimpleName());
        if (loading != null) {
            loading.dismiss();
        }
    }

    public static class LoadingDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            ProgressDialog progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage(getString(R.string.loading));
            return progressDialog;
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            super.onCancel(dialog);
            ((MainActivity) getActivity()).cancelCommand();
        }
    }

    @Override
    public void onServiceCallback(int requestId, Intent requestIntent, int resultCode, Bundle data) {
        if (mServiceHelper.checkCommandClass(requestIntent, DownloadDataCommand.class)) {
            if (resultCode == DownloadDataCommand.RESPONSE_SUCCESS) {
                dismissLoadingDialog();

                String json = data.getString("json");
                if (json != null) {
                    mDataTextView.setText(json);
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.no_data), Toast.LENGTH_LONG).show();
                }
            } else if (resultCode == DownloadDataCommand.RESPONSE_PROGRESS) {
                // TODO For the future
            } else if (resultCode == DownloadDataCommand.RESPONSE_FAILURE) {
                dismissLoadingDialog();
                Toast.makeText(getApplicationContext(), getString(R.string.no_data), Toast.LENGTH_LONG).show();
            }
        }
    }
}
