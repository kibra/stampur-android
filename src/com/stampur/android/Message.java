package com.stampur.android;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import android.text.Html;

public class Message {	
    
    private String id; //Message ID
    private String title; //Message Title   
    private String body; //Message Body    
    private Date time; //Message Timestamp    
    private ArrayList<Stamp> stamps = new ArrayList<Stamp>(); //Stamps
    private ArrayList<String> pictures; //Attached Pictures
    private ArrayList<Comment> comments; //Comments
    private int score; //Message Score (Upvotes - Downvotes)
    private int numComments; //Number of Comments
    // [Separate field Needed since the backend might return only this when the actual comments are not needed]

    private static DateFormat df = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault());

/*---CONSTRUCTORS-------------------------------------------------------------------------------------*/    
      
    public Message() {
        this("", "", "", new ArrayList<Stamp>(), new ArrayList<String>(), new Date());
    }

    public Message(String id, String title, String body, ArrayList<Stamp> stamps,
            ArrayList<String> pictures, Date time) {
        this(id, title, body, stamps, pictures, new ArrayList<Comment>(), new Date());
    }

    public Message(String id, String title, String body, ArrayList<Stamp> stamps,
            ArrayList<String> pictures, ArrayList<Comment> comments, Date time) {
        this.id = id;
        this.title = title;
        this.body = body;
        this.stamps = stamps;
        this.pictures = pictures;
        this.comments = comments;
        this.time = time;
    }

    public Message(String id, String title, String body, ArrayList<Stamp> stamps,
            ArrayList<String> pictures, ArrayList<Comment> comments, Date time, int score, int numComments) {
        this.id = id;
        this.title = title;
        this.body = body;
        this.stamps = stamps;
        this.pictures = pictures;
        this.comments = comments;
        this.time = time;
        this.score = score;
        this.numComments = numComments;
    }

/*---ACCESSORS/MUTATORS-------------------------------------------------------------------------------*/    
    
    public String getId(){ return id; }
    public String getTitle(){ return Html.fromHtml(title).toString(); }
    public String getBody(){ return Html.fromHtml(body).toString(); }
    public ArrayList<Stamp> getStamps(){ return stamps; }
    public ArrayList<String> getPictures(){ return pictures; }
    public Date getTime(){ return time; }    
    
    public void setId(String id){ this.id = id; }
    public void setBody(String body){ this.body = body; }
    public void setTitle(String title){ this.title = title; }
    public void setStamps(ArrayList<Stamp> stamps){ this.stamps = stamps; }
    public void setPictures(ArrayList<String> pictures){ this.pictures = pictures; }
    public void setTime(Date time){ this.time = time; }
    
/*---OTHER--------------------------------------------------------------------------------------------*/    

    
    public void addStamp(Stamp stamp){
        if(stamps == null) stamps = new ArrayList<Stamp>();
        stamps.add(stamp);
    }
    
    public void addPicture(String picture){
        if(pictures == null) pictures = new ArrayList<String>();
        pictures.add(picture);   
    }
    
    public String getTimeString() {
        return df.format(time);
    }

    public ArrayList<Comment> getComments() {
        return comments;
    }

    public void setComments(ArrayList<Comment> comments) {
        this.comments = comments;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void upVote() {
        this.score++;
    }

    public void downVote() {
        this.score--;
    }

    public void addComment(Comment comment) {
        this.comments.add(comment);
    }

    public int getNumComments() {
        return numComments;
    }

    public void setNumComments(int numComments) {
        this.numComments = numComments;
    }
}
