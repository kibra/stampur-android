package com.stampur.android;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import android.text.Html;

public class Comment {

    private String text; //Comment Text
    private boolean anonymous = true; //Anonymity: true by default
    private Stamp commenter; //Commenter's Personal Stamp
    private Date time; //Comment Timestamp
    private ArrayList<Comment> replies; //Nested Replies for the Comment

    private final static String ANONYMOUS = "Anonymous";

    private static DateFormat
                df = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault());

    public Comment(String text, Date time) {
        this(text, time, null, new ArrayList<Comment>());
    }

    public Comment(String text, Date time, ArrayList<Comment> replies) {
        this(text, time, null, replies);
    }

    public Comment(String text, Date time, Stamp commenter, ArrayList<Comment> replies) {
        this.text = text;
        this.time = time;
        this.replies = replies;

        if (commenter != null) {
            this.anonymous = false;
            this.commenter = commenter;
        }
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    public void setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
    }

    public String getText() {
        return Html.fromHtml(text).toString();
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getTime() {
        return time;
    }

    public String getTimeString() {
        return df.format(time);
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public Stamp getCommenter() {
        return commenter;
    }

    public void setCommenter(Stamp commenter) {
        this.commenter = commenter;
    }

    public ArrayList<Comment> getReplies() {
        return replies;
    }

    public void setReplies(ArrayList<Comment> replies) {
        this.replies = replies;
    }

    public String getAuthor() {
        if(!anonymous && (commenter != null)) {
            return commenter.getLabel();
        }

        return ANONYMOUS;
    }

    @Override
    public String toString() {
        return this.getText();
    }
}
