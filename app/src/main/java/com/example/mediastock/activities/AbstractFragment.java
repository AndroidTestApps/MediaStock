package com.example.mediastock.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.view.ContextThemeWrapper;

/**
 * Created by dinu on 04/10/15.
 */
public abstract class AbstractFragment extends android.support.v4.app.Fragment{

    /**
     * Checks if the device is connected to the Internet
     *
     * @return true if connected, false otherwise
     */
    public boolean isOnline() {
        Context context = this.getActivity().getApplicationContext();

        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    public void showAlertDialog(){
        AlertDialog.Builder msg = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), android.R.style.Theme_Dialog));
        msg.setTitle("MediaStock");
        msg.setMessage("There is no internet connection!");
        msg.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });

        msg.show();
    }

}
