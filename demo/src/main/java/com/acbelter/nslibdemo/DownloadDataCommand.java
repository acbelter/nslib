package com.acbelter.nslibdemo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.ResultReceiver;
import android.util.Log;
import com.acbelter.nslib.command.BaseNetworkServiceCommand;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

public class DownloadDataCommand extends BaseNetworkServiceCommand {
    private static final String TAG = "DownloadDataCommand";

    @Override
    protected void doExecute(Context context, Intent requestIntent, ResultReceiver callback) {
        String json = downloadDataFromNetwork(context);
        if (json != null) {
            Bundle data = new Bundle();
            data.putString("json", json);
            notifySuccess(data);
        } else {
            notifyFailure(null);
        }
    }

    private String downloadDataFromNetwork(Context context) {
        try {
            TimeUnit.SECONDS.sleep(5);
            return readToString(context.getResources().openRawResource(R.raw.data));
        } catch (InterruptedException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    private static String readToString(InputStream is) throws IOException {
        if (is == null) {
            return null;
        }

        StringBuilder builder = new StringBuilder();
        BufferedReader inReader = null;
        String line;
        try {
            inReader = new BufferedReader(new InputStreamReader(is));
            while ((line = inReader.readLine()) != null) {
                builder.append(line);
            }
        } finally {
            if (inReader != null) {
                try {
                    inReader.close();
                } catch (IOException e) {
                    Log.e(TAG, "Method readToString() can't close BufferedReader.");
                }
            }
        }

        return builder.toString();
    }

    public static final Parcelable.Creator<DownloadDataCommand> CREATOR =
            new Parcelable.Creator<DownloadDataCommand>() {
                @Override
                public DownloadDataCommand createFromParcel(Parcel in) {
                    return new DownloadDataCommand();
                }

                @Override
                public DownloadDataCommand[] newArray(int size) {
                    return new DownloadDataCommand[size];
                }
            };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
    }
}
