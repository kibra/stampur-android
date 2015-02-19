package com.stampur.android;

import java.util.ArrayList;

import android.location.Location;

public class User {
   
    private String id;  //User ID    
    private String username; //User Name    
    private String fullname; //User's Real Name    
    private String email; //User Email    
    private Gender gender; //User's Gender  
    private Stamp userStamp; //user's own stamp
    
    private Location homeLocation;  //User's Primary Location    
    private Location currentLocation; //User's Current Location   
    
    private ArrayList<Stamp> stampbook;  //User's Stampbook  
    private ArrayList<String> upvoted;  //upvoted message IDs
    private ArrayList<String> downvoted;  //downvoted message IDs
    private ArrayList<String> saved;  //downvoted message IDs    
    
    public enum Gender{ MALE, FEMALE; } //Public Inner Class, represents Gender
    
/*---CONSTRUCTORS-------------------------------------------------------------------------------------*/    

    public User(ArrayList<Stamp> stampbook){
    	this.stampbook = (ArrayList<Stamp>) stampbook.clone();
    }
    
    public User(String id, String username, Stamp userStamp, 
    		ArrayList<String> upvoted, ArrayList<String> downvoted, ArrayList<String> saved){
    	
    	this.id = id;
    	this.username = username;
    	this.userStamp = userStamp;
    	this.upvoted = upvoted;
    	this.downvoted = downvoted;
    	this.saved = saved;
    }
    
    
    public User(){ }
    
/*---ACCESSORS-------------------------------------------------------------------------------------*/    
        
    public String getId(){ return id; }
    public String getUsername(){ return username; }
    public String getFullname(){ return fullname; }
    public String getEmail(){ return email; }
    public Gender getGender(){ return gender; }
    public Location getHomeLocation(){ return homeLocation; }
    public Location getCurrentLocation(){ return currentLocation; }
    public ArrayList<Stamp> getStampbook(){ return stampbook; }

    public void setId(String id){ this.id = id; }
    public void setUsername(String username){ this.username = username; }
    public void setFullname(String fullname){ this.fullname = fullname; }
    public void setEmail(String email){ this.email = email; }
    public void setGender(Gender gender){ this.gender = gender; }
    public void setHomeLocation(Location homeLocation){ this.homeLocation = homeLocation; }
    public void setCurrentLocation(Location currentLocation){ this.currentLocation = currentLocation; }
    public void setStampbook(ArrayList<Stamp> stampbook){ this.stampbook = stampbook; }

/*---OTHER-----------------------------------------------------------------------------------------*/    

    public boolean hasStamp(Stamp stamp){ return this.stampbook.contains(stamp); }
    public void addStamp(Stamp stamp){ this.stampbook.add(stamp); }
    public void removeStamp(Stamp stamp){ this.stampbook.remove(stamp); }
    
    public boolean isUped(String messageID){
    	if(upvoted.contains(messageID)) return true;
    	else return false;
    }
    
    public boolean isDowned(String messageID){
    	if(downvoted.contains(messageID)) return true;
    	else return false;
    }
    
    public boolean isSaved(String messageID){
    	if(saved.contains(messageID)) return true;
    	else return false;
    }
    
    public void addUped(String messageID){ upvoted.add(messageID); }
    public void addDowned(String messageID){ downvoted.add(messageID); }
    public void addSaved(String messageID){ saved.add(messageID); }
    
    public void removeUped(String messageID){ upvoted.remove(messageID); }
    public void removeDowned(String messageID){ downvoted.remove(messageID); }
    public void removeSaved(String messageID){ saved.remove(messageID);}

    
}
