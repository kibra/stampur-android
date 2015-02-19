package com.stampur.android;

import android.app.Application;
import android.content.Intent;

import com.stampur.android.service.StampurService;

/*
 * Stampur - Application Class
 * This class is used for all one-time initializations
 * onCreate() is called only once when the application is started
 *
 */
public class Stampur extends Application {	
	
	public static User currentUser;
	
	@Override
    public void onCreate() {
    	super.onCreate();
    	currentUser = new User();
        startService(new Intent(this, StampurService.class));
    }
	
	@Override
    public void onTerminate() {
        super.onTerminate();
        //unbindService(SS_connection);  
        stopService(new Intent(this, StampurService.class)); 
    } 
	
    public static User getUser(){ return currentUser; }
    
    public static void setUser(User user) { currentUser = user; }	
}
