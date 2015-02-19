package com.stampur.android.ui;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;

import org.apache.http.util.ByteArrayBuffer;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.stampur.android.Comment;
import com.stampur.android.Message;
import com.stampur.android.R;
import com.stampur.android.Stamp;
import com.stampur.android.Stamp.Category;
import com.stampur.android.Stampur;
import com.stampur.android.User;
import com.stampur.android.service.StampurService;

public class Activity_ViewMessage extends Activity_Base {

	private int voteStatus;
	private final int UP = 1;
	private final int ZERO = 0;
	private final int DOWN = -1;
	
    private StampurService stampurService;

    private RelativeLayout messageBodyLayout;
    private Adapter_Comment commentAdapter;
    private ListView commentsList;

    private String messageID;
    private Stamp myStamp;
    private Message message;
    private Resources resources;
    private LinearLayout picsLayout;
    
    private TextView messageTitle;
    private TextView messageBody;
    private TextView numVotes;
    private LinearLayout messageStamps;
    private ImageView upArrow;
    private ImageView downArrow;

    private Intent intent;
    private static final String SLASH = "/";
    private static final String MESSAGEID = "messageID";

    private LayoutInflater inflater;
    private int parentMenus;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_viewmessage);

        resources = getResources();
        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        messageBodyLayout = (RelativeLayout) inflater.inflate(R.layout.layout_viewmessagebody, null);
        picsLayout = (LinearLayout) messageBodyLayout.findViewById(R.id.view_picsLayout);
        commentsList = (ListView) findViewById(R.id.viewmessage_commentsList);       

        bar.setTitle(getString(R.string.ViewMessage));
        bar.addActionIcon(R.drawable.back, new OnClickListener() {
            public void onClick(View v) {Activity_ViewMessage.this.finish();}
        });
        bar.addActionIcon(R.drawable.post, new OnClickListener() {
            public void onClick(View v) {openCommentEditor();}
        });

        intent = getIntent();
        String action = intent.getAction();
        if (Intent.ACTION_VIEW.equals(action)) {
            messageID = getMessageIDFromUrl(intent.getDataString());
        } else {
            messageID = intent.getStringExtra(MESSAGEID);
        } 

        if (isNetworkAvailable()) {
            initService();
            bindService(new Intent(this, StampurService.class), serviceConnection,
                    BIND_AUTO_CREATE);

            try {
                User thisUser = stampurService.getUser();
                SharedPreferences preferences =
                        Activity_ViewMessage.this.getSharedPreferences(StampurService.STAMPUR_PREFS,
                                0);
                myStamp = new Stamp(preferences.getString(StampurService.STAMPID, thisUser.getId()),
                        preferences.getString(StampurService.STAMP_LABEL, thisUser.getUsername()));
            } catch (Exception e) {

            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
    }

    private ServiceConnection serviceConnection;

    private void initService() {
        serviceConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName className, IBinder serviceBinder) {
                stampurService = ((StampurService.StampurService_Binder) serviceBinder).getService();
                displayMessage();
            }
            public void onServiceDisconnected(ComponentName arg0){  }
        };
    }

    public void displayMessage() {

        if (messageID == null || "".equals(messageID)) {
            this.finish();
            return;
        }

        message = stampurService.getMessage(messageID);

        Log.i("message", message.toString());
        Log.i("message", message.getTitle());
        Log.i("message", message.getId());
        Log.i("message", messageID);

        messageTitle = (TextView) messageBodyLayout.findViewById(R.id.viewmessage_messageTitle);
        messageBody = (TextView) messageBodyLayout.findViewById(R.id.viewmessage_messageBody);
        numVotes = (TextView) messageBodyLayout.findViewById(R.id.viewmessage_numVotes);
        messageStamps = (LinearLayout) messageBodyLayout.findViewById(R.id.viewmessage_stampLayout);    	
        upArrow = (ImageView) messageBodyLayout.findViewById(R.id.viewmessage_upvote);
        downArrow = (ImageView) messageBodyLayout.findViewById(R.id.viewmessage_downvote);      
		
        messageTitle.setText(message.getTitle());
        messageBody.setText(message.getBody());
        numVotes.setText(Integer.toString(message.getScore()));
        
        if(Stampur.currentUser.isUped(messageID)){ 
        	voteStatus = UP;
        	upArrow.setImageResource(R.drawable.up_arrow_active);       
        } else if(Stampur.currentUser.isDowned(messageID)){
        	voteStatus = DOWN;
        	downArrow.setImageResource(R.drawable.down_arrow_active);
        } else{ voteStatus = ZERO;}
        
        for(Stamp s : message.getStamps()){
        	TextView stamp = (TextView) View.inflate(this.getBaseContext(), R.layout.view_stamptextview, null);
            LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            llp.setMargins(2, 0, 2, 0); // llp.setMargins(left, top, right, bottom);
            TextView previous = (TextView) messageStamps.getChildAt((messageStamps.getChildCount() - 1)); 
            stamp.setLayoutParams(llp);              
            stamp.setText(s.toString());
        	if(s.getCategory().equals(Category.SPECIAL)) stamp.setTextColor(this.getResources().getColor(R.color.red));
        	messageStamps.addView(stamp);
        }

        commentsList.addHeaderView(messageBodyLayout);
        commentAdapter = new Adapter_Comment(this, R.layout.item_commentitem, -1, message.getComments());
        commentsList.setAdapter(commentAdapter);
        loadPictures();

//        //TODO: TEMP LOGIC
//        new Thread(new Runnable() {
//            public void run() {
//                loadPictures();
//            }
//        }).start();
    }
    
    public void upvote(View v){ 
    	Boolean b;
        if(voteStatus == UP){	        	
        	upArrow.setImageResource(R.drawable.up_arrow);
        	voteStatus = ZERO;
		    Stampur.currentUser.removeUped(messageID);
		    b = stampurService.upvoteMessage(messageID, false);
        } else{       
		    if(voteStatus == DOWN){
		    	downArrow.setImageResource(R.drawable.down_arrow);
		    	Stampur.currentUser.removeDowned(messageID);
		    }
		    upArrow.setImageResource(R.drawable.up_arrow_active);
		    voteStatus = UP;
		    Stampur.currentUser.addUped(messageID);
		    b = stampurService.upvoteMessage(messageID, true);
        }
        
        numVotes.setText(Integer.toString(stampurService.getMessageVotes(messageID)));
        Log.i("message:upvote", messageID);
	    Log.i("message:upvote RETURN", b.toString());
    }
    
    public void downvote(View v){      
    	Boolean b;
        if(voteStatus == DOWN){	        	
        	downArrow.setImageResource(R.drawable.down_arrow);
        	voteStatus = ZERO;
		    Stampur.currentUser.removeDowned(messageID);
		    b = stampurService.downvoteMessage(messageID, false);
        } else{       
        	if(voteStatus == UP){
		    	upArrow.setImageResource(R.drawable.up_arrow);
		    	Stampur.currentUser.removeUped(messageID);
		    }
		    downArrow.setImageResource(R.drawable.down_arrow_active);
		    voteStatus = DOWN;
		    Stampur.currentUser.addDowned(messageID);
		    b = stampurService.downvoteMessage(messageID, true);
        }
        
        numVotes.setText(Integer.toString(stampurService.getMessageVotes(messageID)));
        Log.i("message:downvote", messageID);
	    Log.i("message:downvote RETURN", b.toString());
    }
    
    

    //TODO: Optimize this Method
    private String getMessageIDFromUrl(String viewMessageUrl) {
        if (viewMessageUrl == null || "".equals(viewMessageUrl.trim())) {
            return viewMessageUrl;
        }

        viewMessageUrl = viewMessageUrl.trim();

        if (viewMessageUrl.endsWith(SLASH)) {
            viewMessageUrl = viewMessageUrl.substring(0, viewMessageUrl.length() - 1);
        }

        return viewMessageUrl.substring(viewMessageUrl.lastIndexOf(SLASH), viewMessageUrl.length());
    }

    private void loadPictures() {
        String stampurMediaServer = getString(R.string.StampurMediaServer);

        for (String picture : message.getPictures()) {

            try {

                FileInputStream fis;

                try {
                    fis = openFileInput(picture);
                } catch (FileNotFoundException fex) {
                    URL picUrl = new URL(stampurMediaServer + picture.replaceAll(" ", "%20"));
                    URLConnection picConnection = picUrl.openConnection();
                    InputStream is = picConnection.getInputStream();
                    BufferedInputStream bis = new BufferedInputStream(is);
                    ByteArrayBuffer baf = new ByteArrayBuffer(50);
                    int current = 0;
                    while ((current = bis.read()) != -1) {
                        baf.append((byte) current);
                    }

                    FileOutputStream fos = openFileOutput(picture, MODE_PRIVATE);
                    fos.write(baf.toByteArray());
                    fos.close();
                }

                fis = openFileInput(picture);
                Bitmap bitmap = BitmapFactory.decodeStream(fis);
                Bitmap compressed = Bitmap.createScaledBitmap(bitmap,
                        resources.getInteger(R.integer.PicThumbnailWidth),
                        resources.getInteger(R.integer.PicThumbnailHeight), false);

                ImageView image = (ImageView) View.inflate(this, R.layout.pictureitem, null);
                image.setImageBitmap(compressed);
                final String picName = picture;
                image.setOnClickListener(new OnClickListener() {
                    public void onClick(View view) {
                        showPicture(picName);
                    }
                });
                picsLayout.addView(image);
                fis.close();
            } catch (Exception ex) {
                //Ignore if file not found, and move on to the next picture ;)
                System.out.println(ex);
            }
        }
    }

    public void showPicture(String picture) {
        if (isNetworkAvailable()) {
            final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.showpicture);
            dialog.setTitle(getString(R.string.AttachedPicture));
            dialog.setCancelable(true);

            ImageView image = (ImageView) dialog.findViewById(R.id.showPicture);

            try {
                FileInputStream fis = openFileInput(picture);
                BitmapDrawable drawable = new BitmapDrawable(fis);
                image.setBackgroundDrawable(drawable);
                fis.close();

                /* TODO: Old COde. Needs to verify which works better after backend starts
                //          sending images with smaller size
                FileInputStream fis = openFileInput(picture);
                byte[] tempArray = new byte[16000];
                BitmapFactory.Options options = new Options();
                options.inPurgeable = true;
                options.inDensity = 100;
                options.inTargetDensity = 100;
                options.inScaled = true;
                options.inTempStorage = tempArray;

                Bitmap bitmap = BitmapFactory.decodeStream(fis, null, options);
                Display display = getWindowManager().getDefaultDisplay();
                Bitmap compressed =
                        Bitmap.createScaledBitmap(bitmap, display.getWidth(), display.getWidth(),
                                false);
                bitmap = null;
                image.setImageBitmap(compressed);
                fis.close();
                compressed = null;
                */
            } catch (Exception ex) {

            }

            dialog.show();
        }
    }

    public void openCommentEditor() {
        if (isNetworkAvailable()) {
            final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.commentpopup);
            dialog.setTitle(getString(R.string.CommentTitle));
            dialog.setCancelable(true);

            final EditText commentText = (EditText) dialog.findViewById(R.id.postCommentText);
            final CheckBox isAnonymous = (CheckBox) dialog.findViewById(R.id.isAnonymous);

            final Button sendComment = (Button) dialog.findViewById(R.id.sendCommentButton);

            commentText.addTextChangedListener(new TextWatcher() {
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                public void afterTextChanged(Editable editable) {
                    if (editable.length() > 0) {
                        sendComment.setEnabled(true);
                    } else {
                        sendComment.setEnabled(false);
                    }
                }
            });

            sendComment.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    boolean anonymous = !isAnonymous.isChecked();
                    String commentTextStr = commentText.getText().toString();
                    final Comment comment = new Comment(commentTextStr, new Date());

                    if (!anonymous) {
                        //Need to set this only if not anonymous
                        //since it's true by default for a Comment
                        comment.setAnonymous(anonymous);
                        comment.setCommenter(myStamp);
                    }

                    Thread t = new Thread(new Runnable() {
                        public void run() {
                            stampurService.postCommentToMessage(comment, messageID);
                        }
                    });

                    t.start();

                    //Add the comment to the Comment Adapter so that the user will see it immediately.
                    commentAdapter.add(comment);
                    dialog.dismiss();
                }
            });

            final Button cancelComment = (Button) dialog.findViewById(R.id.cancelCommentButton);
            cancelComment.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    dialog.cancel();
                }
            });
            dialog.show();
        }
    }

    private void hideVirtualKeyboard() {
        View focusedView = this.getCurrentFocus();
        if (focusedView != null) {
            InputMethodManager m =
                    (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (m != null) {
                m.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        parentMenus = menu.size();

        int groupId = 0;
        int menuItemOrder = Menu.NONE;
        int menuItemText = R.string.Upvote;
        MenuItem upvote = menu.add(groupId, parentMenus + 1, menuItemOrder, menuItemText);
        upvote.setIcon(R.drawable.gear);

        menuItemText = R.string.Downvote;
        MenuItem downvote = menu.add(groupId, parentMenus + 2, menuItemOrder, menuItemText);
        downvote.setIcon(R.drawable.gear);

        menuItemText = R.string.CommentTitle;
        MenuItem comment = menu.add(groupId, parentMenus + 3, menuItemOrder, menuItemText);
        comment.setIcon(R.drawable.gear);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(super.onOptionsItemSelected(item)) {
            return true;
        } else if (item.getItemId() == parentMenus + 1) {
            upvote(this.getCurrentFocus());
            return true;
        } else if (item.getItemId() == parentMenus + 2) {
            downvote(this.getCurrentFocus());
            return true;
        } else if (item.getItemId() == parentMenus + 3) {
            openCommentEditor();
            return true;
        }

        return false;
    }
}
