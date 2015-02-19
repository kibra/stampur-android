package com.stampur.android.ui;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.stampur.android.Stampur;
import com.stampur.android.User;
import com.stampur.android.service.StampurService;

public class Activity_Launch extends Activity_Base {
	
	private static final String LOGTAG = "LoginActivity";
	
	public static final String STAMPUR_PREFS = "StampurPrefs";
	public static final String AUTH_TOKEN = "AuthToken";
	
	SharedPreferences settings;
	boolean hasAuthToken;
	
	private StampurService stampurService;
    private ServiceConnection serviceConnection;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		settings = getSharedPreferences(STAMPUR_PREFS, MODE_PRIVATE);
		hasAuthToken = settings.getBoolean("hasAuthToken", false);
		
		if (!hasAuthToken){
			goToLogin();
		}

        if (isNetworkAvailable()) {
            //initService();
            //this.bindService(new Intent(this, StampurService.class), serviceConnection, BIND_AUTO_CREATE);
            
            Intent bindIntent = new Intent(this, StampurService.class);
            initService();
            //startService(bindIntent);
            bindService(bindIntent, serviceConnection, 0);
        }
	}
	
	@Override
	protected void onDestroy() {
	    super.onDestroy();
	    if(serviceConnection != null) unbindService(serviceConnection);
	}

    private void initService() {
        serviceConnection = new ServiceConnection() {

            public void onServiceConnected(ComponentName className, IBinder serviceBinder) {
                stampurService =
                        ((StampurService.StampurService_Binder) serviceBinder).getService();
                String authToken = settings.getString(AUTH_TOKEN, "");

                Log.i(LOGTAG, authToken);

                User user = stampurService.getUser();

                if (user != null) {
                	Stampur.currentUser = user;
                    goToMenu();
                } else {
                    goToLogin();
                }
            }

            public void onServiceDisconnected(ComponentName arg0) {
            }
        };
    }
	
	private void goToMenu() {
		startActivity(new Intent(this, Activity_MainMenu.class));
		this.finish();
	}
	
	private void goToLogin() {
		startActivity(new Intent(this, Activity_Login.class));
		this.finish();
	}
    
}
