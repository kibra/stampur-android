package com.stampur.android.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;

import com.stampur.android.R;
import com.stampur.android.util.NetworkManager;

public class Activity_MainMenu extends Activity {

    private final int NONETWORK = 1001;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.layout_mainmenu);
        LinearLayout top = (LinearLayout) this.findViewById(R.id.mainmenu_topLevelLayout);
        this.getLayoutInflater().inflate(R.layout.fragment_dashboard, top);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void goToPost(View v) {
        if (isNetworkAvailable()) {
            Intent myIntent = new Intent(this, Activity_PostMessage.class);
            startActivityForResult(myIntent, 0);
        }
    }

    public void goToInbox(View v) {
        if (isNetworkAvailable()) {
            Intent myIntent = new Intent(this, Activity_Inbox.class);
            startActivityForResult(myIntent, 0);
        }
    }

    public void goToStampBook(View v) {
        if (isNetworkAvailable()) {
            Intent intent = new Intent(v.getContext(), Activity_StampBook.class);
            startActivityForResult(intent, 0);
        }
    }

    public void goToSettings(View v) {
        if (isNetworkAvailable()) {
            Intent intent = new Intent(v.getContext(), Activity_Settings.class);
            startActivityForResult(intent, 0);
        }
    }

    public boolean isNetworkAvailable() {
        NetworkManager.init(this);
        if (!NetworkManager.isNetworkAvailable()) {
            showDialog(NONETWORK);
            return false;
        }

        return true;
    }

    public Dialog onCreateDialog(int id, Bundle args) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        switch (id) {
            case NONETWORK:
                builder.setTitle(R.string.NoNetworkAvailable).setMessage(
                        R.string.NoNetworkTryAgain).setCancelable(false).setNegativeButton(
                        getString(R.string.NetworkDialogOk), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                break;
        }

        AlertDialog alert = builder.create();
        return alert;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        int groupId = 0;
        int menuItemOrder = Menu.NONE;
        int menuItemText = R.string.Refresh;
        MenuItem settings = menu.add(groupId, 1, menuItemOrder, menuItemText);
        settings.setIcon(R.drawable.gear);

        menuItemText = R.string.Logout;
        MenuItem addstamps = menu.add(groupId, 2, menuItemOrder, menuItemText);
        addstamps.setIcon(R.drawable.gear);

        menuItemText = R.string.Quit;
        MenuItem addPhotos = menu.add(groupId, 3, menuItemOrder, menuItemText);
        addPhotos.setIcon(R.drawable.gear);

        return true;
    }

}