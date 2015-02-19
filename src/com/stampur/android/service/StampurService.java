package com.stampur.android.service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.stampur.android.R;
import com.stampur.android.Comment;
import com.stampur.android.Message;
import com.stampur.android.Stamp;
import com.stampur.android.Stamp.Category;
import com.stampur.android.Stampur;
import com.stampur.android.User;
import com.stampur.android.util.JSONToObjectConverter;

public class StampurService extends Service {

	private static final String LOGTAG = "StampurService";
	
	public static final String STAMPUR_PREFS = "StampurPrefs";
	public static final String AUTH_TOKEN = "AuthToken";
	public static final String USER_PASSWORD = "UserPassword";

	public static final String STAMPID = "stampid";
	public static final String STAMP_LABEL = "stamp_label";
	public static final String STAMP_DESC = "stamp_description";
	
	public static final String MESSAGEORDER_TOP = "top";
	public static final String MESSAGEORDER_HOT = "hot";
	public static final String MESSAGEORDER_TIME = "time";
	public static final String MESSAGEORDER_DEFAULT = MESSAGEORDER_TIME;

	private String stampurJSONserver;

	private final IBinder mBinder = new StampurService_Binder();

	public class StampurService_Binder extends Binder
	{
    	public StampurService getService()
    	{
            Helper_Http.setImageUploadServer(getString(R.string.StampurImageUploadServer));
    		stampurJSONserver = getString(R.string.StampurJSONserver);
            return StampurService.this;
        }
    }

	@Override
	public IBinder onBind(Intent intent){
        Helper_Http.setImageUploadServer(getString(R.string.StampurImageUploadServer));
		return mBinder;
	}

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Helper_Http.setImageUploadServer(getString(R.string.StampurImageUploadServer));
        return START_STICKY;
    }

    @Override
	public void onDestroy() {
		
	}
    
    //working
    public boolean authenticate(String email, String password)
    {
    	String authToken = Helper_Http.get_string_from_url("http://www.stampur.com/auth/login?email=" + email + "&password=" + password);
    	SharedPreferences settings = getSharedPreferences(STAMPUR_PREFS,MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		
    	boolean gotAuthToken = authToken != null;
    	
    	if (gotAuthToken)
    	{
    		editor.putBoolean("hasAuthToken", true);
    		editor.putString(AUTH_TOKEN, authToken);
            Log.i(LOGTAG,"goodauth");

            //TODO: Temp Logic
            editor.putString(USER_PASSWORD, password);
    	} else {
    		editor.putBoolean("hasAuthToken", false);
    		Log.i(LOGTAG,"badauth");
    	}
    	editor.commit();
    	return gotAuthToken;
    }
    
    //working
    public User getUser() 
    {
    	SharedPreferences settings = getSharedPreferences(STAMPUR_PREFS, MODE_PRIVATE);
    	String authToken = settings.getString(AUTH_TOKEN, "");
    	JSONObject jsonreturn = Helper_Http.get_json_from_url(stampurJSONserver + "getUser?auth_tkt=" + authToken);
    	Log.i("AUTHTOKEN", authToken);
    	SharedPreferences.Editor editor = settings.edit();

    	if (jsonreturn !=null) {
    		try {
    			editor.putString(STAMPID, jsonreturn.getString(STAMPID));
    			editor.putString(STAMP_LABEL, jsonreturn.getString(STAMP_LABEL));
    			editor.putString(STAMP_DESC, jsonreturn.getString(STAMP_DESC));
                editor.commit();
    			return JSONToObjectConverter.jsonToUser(jsonreturn);
    		} catch (JSONException e) {
    			e.printStackTrace();
    		}
    	}
    	return null;
    }
    
/*------------------------------------------------------------------------------------------------------------*/
/*---MESSAGES-------------------------------------------------------------------------------------------------*/
/*------------------------------------------------------------------------------------------------------------*/
    
    //working
    public Message getMessage(String id)
    {
        try {
            JSONObject jsonreturn = Helper_Http.get_json_from_url(stampurJSONserver + "message/" + id);
            return JSONToObjectConverter.jsonToMessage(jsonreturn);
        } catch (JSONException e) {
        	Log.e(LOGTAG, e.toString());
            return new Message();
        }
    }
    
    private boolean voteOnMessage(String messageid, JSONObject vote){
    	SharedPreferences settings = getSharedPreferences(STAMPUR_PREFS, MODE_PRIVATE);
    	String authToken = settings.getString(AUTH_TOKEN, "");    	
    	Helper_Http.post_json_to_url(stampurJSONserver + "message/" + messageid + "?auth_tkt=" + authToken,	vote);
    	return true;
    }   
    
    public boolean upvoteMessage(String messageid, boolean uped){    	
    	JSONObject postObject = new JSONObject();    	
		try{ postObject.accumulate("uped", uped); } 
		catch (JSONException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return voteOnMessage(messageid, postObject);			
    }
    
    public boolean downvoteMessage(String messageid, boolean downed){
    	JSONObject postObject = new JSONObject();    	
		try{ postObject.accumulate("downed", downed); } 
		catch (JSONException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return voteOnMessage(messageid, postObject);
    }

    public int getMessageVotes(String messageid)
    {
    	SharedPreferences settings = getSharedPreferences(STAMPUR_PREFS, MODE_PRIVATE);
    	String authToken = settings.getString(AUTH_TOKEN, "");
        JSONObject json = Helper_Http.get_json_from_url(
                stampurJSONserver + "message/" + messageid + "?auth_tkt=" + authToken);
        int score = 0;

        try {
            //TODO: TEMP LOGIC. CHANGE LATER!
            if (json.has("score")) {
                score = json.getInt("score");
            }
		} catch (JSONException e) {
			e.printStackTrace();
		}
        return score;
    }
    
    //MESSAGE GETTING 
    
    public ArrayList<Message> getMessagesForUser(int pagenumber, String sortBy){
    	String authToken = getSharedPreferences(STAMPUR_PREFS, MODE_PRIVATE).getString(AUTH_TOKEN, "");   	
        JSONObject jsonreturn = Helper_Http.get_json_from_url(
        		stampurJSONserver + "stampbook_messages/" + pagenumber + 
        		"?auth_tkt=" + authToken + "&order=" + sortBy);        
        return parseMessages(jsonreturn);
    }
    
    public ArrayList<Message> getMessagesForStampurStamp(int pagenumber, String sortBy){    	
        JSONObject jsonreturn = Helper_Http.get_json_from_url(
                stampurJSONserver + "stampurstamp_messages/" + pagenumber + "?order=" + sortBy);     
        return parseMessages(jsonreturn);
    }
    
    public ArrayList<Message> getMessagesForStamp(String stampName, int pagenumber, String sortBy){    	
        JSONObject jsonreturn = Helper_Http.get_json_from_url(
                stampurJSONserver + "stamp/" + stampName.replaceAll(" ", "%20") + "/" + pagenumber
                + "?order=" + sortBy);     
        return parseMessages(jsonreturn);
    }
    
    public ArrayList<Message> getMessagesSaved(int pagenumber, String sortBy){     	
		String authToken = getSharedPreferences(STAMPUR_PREFS, MODE_PRIVATE).getString(AUTH_TOKEN, "");    	
    	JSONObject jsonreturn = Helper_Http.get_json_from_url(
    			stampurJSONserver + "saved_messages/" + pagenumber + 
    			"?auth_tkt=" + authToken + "&order=" + sortBy);      
    	return parseMessages(jsonreturn);
    }
    
    public ArrayList<Message> getMessagesSent(int pagenumber, String sortBy){ 
    	String authToken = getSharedPreferences(STAMPUR_PREFS, MODE_PRIVATE).getString(AUTH_TOKEN, "");    	
    	JSONObject jsonreturn = Helper_Http.get_json_from_url(
    			stampurJSONserver + "sent_messages/" + pagenumber +
    			"?auth_tkt=" + authToken + "&order=" + sortBy);      
    	return parseMessages(jsonreturn);
    }
    
    private ArrayList<Message> parseMessages(JSONObject jsonreturn){    			
    	ArrayList<Message> returnArray = new ArrayList<Message>();       	
        try{
	    	JSONArray returnedarray = jsonreturn.getJSONArray("messages_array");
	    	for(int i = 0; i < returnedarray.length(); i++)
                returnArray.add(JSONToObjectConverter.jsonToMessage(returnedarray.getJSONObject(i)));            
		} catch(JSONException e){
    		e.printStackTrace();
    		return null;
		}
    	return returnArray;
    }   
    
    
    public boolean postCommentToMessage(Comment comment, String messageid)
    {
    	SharedPreferences settings = getSharedPreferences(STAMPUR_PREFS, MODE_PRIVATE);
    	String authToken = settings.getString(AUTH_TOKEN, "");
    	
    	JSONObject postObject = new JSONObject();
    	
    	try {
    		postObject.accumulate("add_reply", JSONToObjectConverter.commentToJSON(comment));
			Helper_Http.post_json_to_url(stampurJSONserver + "message/" + messageid + "?auth_tkt=" + authToken,
					postObject);
		} catch (JSONException e) {
			e.printStackTrace();
			return false;
		}
    	return true;
    	
    }
    
    public void postMessage(final Message toPost) {
        Thread t = new Thread(new Runnable() {
            public void run() {

                if (toPost.getPictures() != null && toPost.getPictures().size() != 0) {
                    for (int i = 0; i < toPost.getPictures().size(); i++) {
                        String fileName = toPost.getPictures().get(i);

                        System.out.println("--Post.getPictures() loop start");
                        System.out.println(fileName);

                        FileInputStream fis = null;

                        try {
                            fis = openFileInput(fileName);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }

                        System.out.println(fileName);

                        byte[] byte_buffer = null;
                        try {

                            System.out.println(fis.getChannel().size());
                            byte_buffer = new byte[(int) fis.getChannel().size()];

                            if (fis.read(byte_buffer) < 0) {
                                return;
                            }

                            toPost.getPictures().set(i,
                                    Helper_Http.post_image_to_media(fileName, byte_buffer));

                            System.out.println(toPost.getPictures().get(i));

                            fis.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                try {
                    Helper_Http.post_json_to_url(stampurJSONserver + "post_message",
                            JSONToObjectConverter.messageToJSON(toPost));
                } catch (JSONException e) {
                    return;
                }
            }
        });

        t.start();
    }

    public void changeProfilePicture(String serverFileName) {
    	JSONObject postObject = new JSONObject();
    	SharedPreferences settings = getSharedPreferences(STAMPUR_PREFS, MODE_PRIVATE);
    	String authToken = settings.getString(AUTH_TOKEN, "");
		try {
			postObject.accumulate("update_stamp_photo", serverFileName);
			Helper_Http.post_json_to_url(stampurJSONserver + "getUser?auth_tkt=" + authToken, postObject);
		} catch (JSONException e) {
			e.printStackTrace();
		}
    }

    public String uploadPictureToServer(String fileName) {
        FileInputStream fis = null;
        String serverFileName = null;

        try {
            fis = openFileInput(fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            //System.out.println(fis.getChannel().size());
            byte[] byte_buffer = new byte[(int) fis.getChannel().size()];

            if (fis.read(byte_buffer) < 0) {
                return serverFileName;
            }

            serverFileName = Helper_Http.post_image_to_media(fileName, byte_buffer);
            System.out.println(serverFileName);
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            return serverFileName;
        }
    }

    public void updateStampDescription(String description) {  	
    	JSONObject postObject = new JSONObject();
    	SharedPreferences settings = getSharedPreferences(STAMPUR_PREFS, MODE_PRIVATE);
    	String authToken = settings.getString(AUTH_TOKEN, "");
		try {
			postObject.accumulate("edit_stamp_description", description);
			Helper_Http.post_json_to_url(stampurJSONserver + "getUser?auth_tkt=" + authToken, postObject);
		} catch (JSONException e) {
			e.printStackTrace();
		}
    }

    public void updatePassword(String newPassword, String oldPassword) {
    	JSONObject postObject = new JSONObject();
    	SharedPreferences settings = getSharedPreferences(STAMPUR_PREFS, MODE_PRIVATE);
    	String authToken = settings.getString(AUTH_TOKEN, "");
		try {
			postObject.accumulate("new_password", newPassword);
			postObject.accumulate("old_password", newPassword);
			Helper_Http.post_json_to_url(stampurJSONserver + "getUser?auth_tkt=" + authToken, postObject);
		} catch (JSONException e) {
			e.printStackTrace();
		}
    }

    public String getCurrentPassword() {
        //TODO: (Kyle)

        //TODO: TEMP LOGIC
        SharedPreferences settings = getSharedPreferences(STAMPUR_PREFS, MODE_PRIVATE);
    	return settings.getString(USER_PASSWORD, "");
    }
    
/*------------------------------------------------------------------------------------------------------------*/
/*---STAMPS---------------------------------------------------------------------------------------------------*/
/*------------------------------------------------------------------------------------------------------------*/
    
    //working
    public Stamp getStamp(String id){
        JSONObject jsonreturn = Helper_Http.get_json_from_url(
                stampurJSONserver + "stamp/" + id.replaceAll(" ", "%20") + "/1");
        System.out.println(jsonreturn.toString());
        try {
            return JSONToObjectConverter.jsonToStamp(jsonreturn.getJSONObject("stamp"));
        } catch (JSONException ex) {
            return null;
        }
    }
    
    //working
    public ArrayList<Stamp> getPublicStamps(Category category)
    {
    	ArrayList<Stamp> returnArray = new ArrayList<Stamp>();
    	JSONObject jsonreturn = Helper_Http.get_json_from_url(stampurJSONserver + category + "_stamps/");

        if(jsonreturn == null) {
            return returnArray;
        }

    	try
		{
	    	JSONArray returnedarray = jsonreturn.getJSONArray("stamp_array");
	    	System.out.println(jsonreturn.toString());
	    	for(int i = 0; i < returnedarray.length(); i++)
	    	{
				
				returnArray.add(JSONToObjectConverter.jsonToStamp(returnedarray.getJSONObject(i)));
			}
		}
    	catch (JSONException e)
		{
    		e.printStackTrace();
    		return returnArray;
		}
    	return returnArray;
    }
    
    //working
    public ArrayList<Stamp> getPublicStamps()
    {
    	ArrayList<Stamp> returnArray = new ArrayList<Stamp>();
    	JSONObject jsonreturn = Helper_Http.get_json_from_url(stampurJSONserver + "public_stamps");
    	
    	try
		{
	    	JSONArray returnedarray = jsonreturn.getJSONArray("stamp_array");
	    	System.out.println(jsonreturn.toString());
	    	for(int i = 0; i < returnedarray.length(); i++)
	    	{
				
				returnArray.add(JSONToObjectConverter.jsonToStamp(returnedarray.getJSONObject(i)));
			}
		}
    	catch (JSONException e)
		{
    		e.printStackTrace();
    		return returnArray;
		}
    	return returnArray;
    }
    
    public ArrayList<Stamp> getUserStamps(){
    	SharedPreferences settings = getSharedPreferences(STAMPUR_PREFS, MODE_PRIVATE);
    	String authToken = settings.getString(AUTH_TOKEN, "");
    	ArrayList<Stamp> returnArray = new ArrayList<Stamp>();
    	JSONObject jsonreturn = Helper_Http.get_json_from_url(stampurJSONserver + "my_stamps" + "?auth_tkt=" + authToken);
    	try
		{
	    	JSONArray returnedarray = jsonreturn.getJSONArray("stamp_array");
	    	System.out.println(jsonreturn.toString());
	    	for(int i = 0; i < returnedarray.length(); i++)
	    	{
				
				returnArray.add(JSONToObjectConverter.jsonToStamp(returnedarray.getJSONObject(i)));
			}
		}
    	catch (JSONException e)
		{
    		e.printStackTrace();
    		return returnArray;
		}
    	return returnArray;
    	
    }

    public ArrayList<Stamp> getUserStampsForPostMessage() {
        SharedPreferences settings = getSharedPreferences(STAMPUR_PREFS, MODE_PRIVATE);
        String authToken = settings.getString(AUTH_TOKEN, "");
        ArrayList<Stamp> returnArray = new ArrayList<Stamp>();
        JSONObject jsonreturn = Helper_Http.get_json_from_url(
                stampurJSONserver + "my_stamps" + "?auth_tkt=" + authToken);
        try {
            JSONArray returnedarray = jsonreturn.getJSONArray("stamp_array");
            System.out.println(jsonreturn.toString());
            for (int i = 0; i < returnedarray.length(); i++) {
                JSONObject json = returnedarray.getJSONObject(i);
                if (!Category.PEOPLE.equals(Category.valueOf(
                        json.getString(JSONToObjectConverter.STAMP_CATEGORY).toUpperCase()))) {
                    returnArray.add(JSONToObjectConverter.jsonToStamp(json));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return returnArray;
    }

   public boolean addStamp(String id) {
		SharedPreferences settings = getSharedPreferences(STAMPUR_PREFS, MODE_PRIVATE);
		String authToken = settings.getString(AUTH_TOKEN, "");
		
		JSONObject postObject = new JSONObject();
		
		try {
			postObject.accumulate("add_stamp", id);
		Helper_Http.post_json_to_url(stampurJSONserver + "stampbook/" + "?auth_tkt=" + authToken,
						postObject);
			} catch (JSONException e) {
				e.printStackTrace();
				return false;
			}
		return true;
   }
   
   public boolean removeStamp(String id) {
		SharedPreferences settings = getSharedPreferences(STAMPUR_PREFS, MODE_PRIVATE);
		String authToken = settings.getString(AUTH_TOKEN, "");
		
		JSONObject postObject = new JSONObject();
		
		try {
			postObject.accumulate("remove_stamp", id);
			Helper_Http.post_json_to_url(stampurJSONserver + "stampbook/" + "?auth_tkt=" + authToken,
					postObject);
			} catch (JSONException e) {
				e.printStackTrace();
				return false;
			}
		return true;
   }
	
}
/*
 public boolean upvoteMessage(String messageid, boolean uped){
    	SharedPreferences settings = getSharedPreferences(STAMPUR_PREFS, MODE_PRIVATE);
    	String authToken = settings.getString(AUTH_TOKEN, "");
    	JSONObject postObject = new JSONObject();
    	try {
    		postObject.accumulate("uped", uped);    		
			Helper_Http.post_json_to_url(stampurJSONserver + "message/" + messageid + "?auth_tkt=" + authToken,	postObject);
		} catch (JSONException e) {
			e.printStackTrace();
			return false;
		}
    	return true;
    }
    
    public boolean downvoteMessage(String messageid, boolean downed){
    	SharedPreferences settings = getSharedPreferences(STAMPUR_PREFS, MODE_PRIVATE);
    	String authToken = settings.getString(AUTH_TOKEN, "");
    	JSONObject postObject = new JSONObject();
    	try {
    		postObject.accumulate("downed", downed);
			Helper_Http.post_json_to_url(stampurJSONserver + "message/" + messageid + "?auth_tkt=" + authToken, postObject);
		} catch (JSONException e) {
			e.printStackTrace();
			return false;
		}
    	return true;
    }
*/

