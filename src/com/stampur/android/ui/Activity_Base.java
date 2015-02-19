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
import android.view.View.OnClickListener;
import android.view.Window;

import com.stampur.android.R;
import com.stampur.android.util.NetworkManager;

public abstract class Activity_Base extends Activity {

	public Helper_ActionBar bar;

    private final int NONETWORK = 1001;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        NetworkManager.init(this);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.base_layout);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
		makeBarHappen();
	}
	
	@Override
    protected void onDestroy() {
        super.onDestroy();
    }
	
	private void makeBarHappen(){
		bar = (Helper_ActionBar) findViewById(R.id.actionBar);				
		bar.setHomeLogo(R.drawable.stampur_icon, new OnClickListener() {			
			public void onClick(View v) {
				 Intent menu_intent = new Intent(v.getContext(), Activity_MainMenu.class);
		         startActivityForResult(menu_intent, 0);
			}
		});				
	}
		
	public void goToInbox(View v) {
		Intent intent = new Intent(v.getContext(), Activity_Inbox.class);
        startActivityForResult(intent, 0);
	}
	
	public void goToMenu(View v) {
		Intent menu_intent = new Intent(v.getContext(), Activity_MainMenu.class);
        startActivity(menu_intent);
        this.finish();
	}

    @Override
    public void onBackPressed() {
        super.onBackPressed();
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
        int menuItemText = R.string.Home;
        MenuItem home = menu.add(groupId, Menu.FIRST, menuItemOrder, menuItemText);
        home.setIcon(R.drawable.gear);

        menuItemText = R.string.Refresh;
        MenuItem refresh = menu.add(groupId, 2, menuItemOrder, menuItemText);
        refresh.setIcon(R.drawable.gear);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == 1) {
            goToMenu(this.getCurrentFocus());
            return true;
        } else if (item.getItemId() == 2) {
            //TODO: DO Something!!
            return true;
        }

        return false;
    }

}
