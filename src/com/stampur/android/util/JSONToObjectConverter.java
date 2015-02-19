package com.stampur.android.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.stampur.android.Comment;
import com.stampur.android.Message;
import com.stampur.android.Stamp;
import com.stampur.android.Stamp.Category;
import com.stampur.android.User;

public class JSONToObjectConverter {

	private final static String USER_USERNAME = "username";
	private final static String USER_STAMPID = "stampid";
	private final static String USER_STAMPLABEL = "stamp_label";
	private final static String USER_STAMPDESCRIPTION = "stamp_description";
	private final static String USER_SAVED = "boxed";
	private final static String USER_UPVOTED = "uped";
	private final static String USER_DOWNVOTED = "downed";

    private final static String STAMP_ID = "id";
    private final static String STAMP_LABEL = "label";
    public final static String STAMP_CATEGORY = "category";
    private final static String STAMP_DESC = "description";
    private final static String STAMP_NUMUSERS = "num_users";

    private final static String MSG_ID = "id";
    private final static String MSG_TITLE = "title";
    private final static String MSG_BODY = "body";
    private final static String MSG_SCORE = "score";
    private final static String MSG_TIME = "timestamp";
    private final static String MSG_STAMPIDS = "stampids";
    private final static String MSG_COMMENTS= "comments";
    private final static String MSG_STAMPS= "stamps";
    private final static String MSG_PICS = "photos";
    private final static String MSG_NUMCOMMENTS = "num_replies";

    private final static String COMMENT_AUTHOR = "author";
    private final static String COMMENT_TEXT = "body";
    private final static String COMMENT_TIME = "timestamp";
    private final static String COMMENT_AUTHORID = "author_id";
    private final static String COMMENT_ANONYMOUS = "anonymous";
    private final static String COMMENT_REPLIES = "replies";


    public static Stamp jsonToStamp(JSONObject json) throws JSONException {
        return new Stamp(json.getString(STAMP_ID),
        		json.getString(STAMP_LABEL),
                json.getString(STAMP_DESC),
                Category.valueOf(json.getString(STAMP_CATEGORY).toUpperCase()),
                json.getInt(STAMP_NUMUSERS));
    }

    public static User jsonToUser(JSONObject json) throws JSONException {
    	ArrayList<String> uped = new ArrayList<String>();
    	ArrayList<String> downed = new ArrayList<String>();
    	ArrayList<String> saved = new ArrayList<String>();

    	String username = json.getString(USER_USERNAME);
    	String stamp_id = json.getString(USER_STAMPID);
    	String stamp_label = json.getString(USER_STAMPLABEL);
    	String stamp_descr = json.getString(USER_STAMPDESCRIPTION);

    	JSONArray u = json.getJSONArray(USER_UPVOTED);
    	JSONArray d = json.getJSONArray(USER_DOWNVOTED);
    	JSONArray s = json.getJSONArray(USER_SAVED);

    	for(int i = 0; i < u.length(); i++) uped.add(u.getString(i));
    	for(int i = 0; i < d.length(); i++) downed.add(d.getString(i));
    	for(int i = 0; i < s.length(); i++) saved.add(s.getString(i));

    	Stamp userStamp = new Stamp(stamp_id, stamp_label, stamp_descr);
    	User user = new User(stamp_id, username, userStamp, uped, downed, saved);

        return user;
    }

    public static Comment jsonToComment(JSONObject json) throws JSONException {

        //TODO: TEMP FIX
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String cmtDate = "";

        if (json.has(COMMENT_TIME)) {
            //TODO: TEMP FIX
            cmtDate = json.getString(COMMENT_TIME).split("T")[0];
        }

        ArrayList<Comment> replies = new ArrayList<Comment>();
        if (json.has(COMMENT_REPLIES)) {
            JSONArray replieslist = json.getJSONArray(MSG_COMMENTS);
            for (int i = 0; i < replieslist.length(); i++) {
                JSONObject replyobj = replieslist.getJSONObject(i);
                replies.add(jsonToComment(replyobj));
            }
        }

    	if (json.has(COMMENT_AUTHORID)) {
            try {
                if (null != json.getString(COMMENT_AUTHORID) &&
                        !"null".equalsIgnoreCase(json.getString(COMMENT_AUTHORID))) {
                    return new Comment(json.getString(COMMENT_TEXT), df.parse(cmtDate),
                            new Stamp(json.getString(COMMENT_AUTHORID),
                                    json.getString(COMMENT_AUTHOR)), replies);
                } else {
                    return new Comment(json.getString(COMMENT_TEXT), df.parse(cmtDate), null,
                            replies);
                }
			} catch (ParseException e) {
				e.printStackTrace();
                return new Comment(json.getString(COMMENT_TEXT), new Date(), replies);
			}
    	}

        return new Comment(json.getString(COMMENT_TEXT), new Date(), replies);
    }

    public static Message jsonToMessage(JSONObject json) throws JSONException {
        ArrayList<Stamp> stamps = new ArrayList<Stamp>();
        ArrayList<Comment> comments = new ArrayList<Comment>();
        ArrayList<String> pictures = new ArrayList<String>();

        if (json.has(MSG_STAMPS)) {
            JSONArray stamplist = json.getJSONArray(MSG_STAMPS);
            for (int i = 0; i < stamplist.length(); i++) {
                JSONObject stampobj = stamplist.getJSONObject(i);
                stamps.add(jsonToStamp(stampobj));
            }
        }

        if (json.has(MSG_COMMENTS)) {
            JSONArray commentslist = json.getJSONArray(MSG_COMMENTS);
            for (int i = 0; i < commentslist.length(); i++) {
                JSONObject commentobj = commentslist.getJSONObject(i);
                comments.add(jsonToComment(commentobj));
            }
        }

        if (json.has(MSG_PICS)) {
            JSONArray picslist = json.getJSONArray(MSG_PICS);
            for (int i = 0; i < picslist.length(); i++) {
                pictures.add(picslist.getString(i));
            }
        }

        //TODO: TEMP FIX
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Message message = null;
        String msgDate = "";

        if (json.has(MSG_TIME)) {
            //TODO: TEMP FIX
            msgDate = json.getString(MSG_TIME).split("T")[0];
        }

        int numComments = 0;
        if(json.has(MSG_NUMCOMMENTS)) {
            numComments = json.getInt(MSG_NUMCOMMENTS);
        }

        int score = 0;
        if(json.has(MSG_SCORE)) {
            score = json.getInt(MSG_SCORE);
        }

        try {
            message = new Message(json.getString(MSG_ID), json.getString(MSG_TITLE),
			        json.getString(MSG_BODY), stamps, pictures, comments,
			        df.parse(msgDate), score, numComments);
		} catch (ParseException e) {
            message = new Message(json.getString(MSG_ID), json.getString(MSG_TITLE),
                    json.getString(MSG_BODY), stamps, pictures, comments, new Date(), score,
                    numComments);
        }
        return message;
    }

    public static JSONObject messageToJSON(Message message) throws JSONException {
        JSONObject json = new JSONObject();
        json.put(MSG_ID, message.getId());
        json.put(MSG_TITLE, message.getTitle());
        json.put(MSG_BODY, message.getBody());
        json.put(MSG_TIME, message.getTimeString());

        Set<String> stampIds = new HashSet<String>();
        for (Stamp stamp : message.getStamps()) {
            stampIds.add(stamp.getId());
        }
        json.put(MSG_STAMPIDS, new JSONArray(stampIds));

        JSONArray comments = new JSONArray();
        for(Comment comment : message.getComments()) {
            comments.put(commentToJSON(comment));
        }
        json.put(MSG_COMMENTS, comments);
        json.put(MSG_PICS, new JSONArray(message.getPictures()));

        return json;
    }

    public static JSONObject stampToJSON(Stamp stamp) throws JSONException {
        JSONObject json = new JSONObject();
        json.put(STAMP_ID, stamp.getId());
        json.put(STAMP_LABEL, stamp.getLabel());
        json.put(STAMP_DESC, stamp.getDescription());
        json.put(STAMP_CATEGORY, stamp.getCategory().toString());
        json.put(STAMP_NUMUSERS, stamp.getNumUsers());
        return json;
    }

    public static JSONObject commentToJSON(Comment comment) throws JSONException {
        JSONObject json = new JSONObject();
        json.put(COMMENT_TEXT, comment.getText());
        json.put(COMMENT_TIME, comment.getTimeString());
        json.put(COMMENT_ANONYMOUS, comment.isAnonymous());
        json.put(COMMENT_REPLIES, comment.getReplies());

        if (comment.getCommenter() != null) {
            json.put(COMMENT_AUTHORID, comment.getCommenter().getId());
            json.put(COMMENT_AUTHOR, comment.getCommenter().getLabel());
        }

        return json;
    }

}
