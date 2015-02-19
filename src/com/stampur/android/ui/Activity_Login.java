package com.stampur.android.ui;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.stampur.android.R;
import com.stampur.android.Stampur;
import com.stampur.android.User;
import com.stampur.android.service.StampurService;

public class Activity_Login extends Activity_Base {
	
	private static final String LOGTAG = "LoginActivity";
	
	public static final String STAMPUR_PREFS = "StampurPrefs";
	public static final String AUTH_TOKEN = "AuthToken";
		
	private EditText uname;
	private EditText pass;
	public ProgressDialog progDial; 	
	
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
			setContentView(R.layout.login_layout);
		    uname = (EditText) findViewById(R.id.username);
		    pass = (EditText) findViewById(R.id.password);		    
		}
		initService();
        bindService(new Intent(this, StampurService.class), serviceConnection,
                BIND_AUTO_CREATE);
        
	}
	
	@Override
	protected void onDestroy() {
	    super.onDestroy();
		unbindService(serviceConnection);
	}

    private void initService() {

        serviceConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName className, IBinder serviceBinder) {
                stampurService =
                        ((StampurService.StampurService_Binder) serviceBinder).getService();
                if (isNetworkAvailable()) {
                	if (hasAuthToken) {
                        String authToken = settings.getString(AUTH_TOKEN, "");

                        Log.i(LOGTAG, authToken);

                        Stampur.setUser(stampurService.getUser());

                        if (Stampur.getUser() != null) {
                            Log.i(LOGTAG, Stampur.getUser().getEmail());
                            goToMenu();
                        }
                    } else {
                        setContentView(R.layout.login_layout);
                        uname = (EditText) findViewById(R.id.username);
                        pass = (EditText) findViewById(R.id.password);
                        authenticate(null);
                    }
                }
                
            }

            public void onServiceDisconnected(ComponentName arg0) {
            }
        };
    }

    public void authenticate(View view) {
        if (isNetworkAvailable()) {
            uname = (EditText) findViewById(R.id.username);
            pass = (EditText) findViewById(R.id.password);
            if (stampurService.authenticate(uname.getText().toString(),
                    pass.getText().toString())) {
                String authToken = settings.getString(AUTH_TOKEN, "");

                Log.i(LOGTAG, authToken);

                Stampur.setUser(stampurService.getUser());

                if (Stampur.currentUser != null) {
                    Log.i(LOGTAG, Stampur.currentUser.getEmail());
                    goToMenu();
                }
                setContentView(R.layout.login_layout);
            } else {
                Toast toast = Toast.makeText(getApplicationContext(), R.string.AuthError,
                        Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }	

    private void goToMenu() {
        if (isNetworkAvailable()) {
            startActivity(new Intent(this, Activity_MainMenu.class));
            this.finish();
        }
    }
    
}
