package com.stampur.android.ui;

import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.stampur.android.Message;
import com.stampur.android.R;
import com.stampur.android.service.StampurService;

public class Activity_Inbox extends Activity_BaseTab {
	
	private Context context;
	private static StampurService stampurService;
	
	private static String INBOXTAG = "";
	private static String PUBLICTAG = "";
	private static String SAVEDTAG = "";
	private static String SENTTAG = "";

	private Adapter_Message inbox;
	private Adapter_Message publicbox;
	private Adapter_Message saved;  
	private Adapter_Message sent;

    private int inboxPageCounter = 1;
    private int publicPageCounter = 1;
    private int savedPageCounter = 1;
    private int sentPageCounter = 1;
    
    private String inboxMessageOrder = StampurService.MESSAGEORDER_DEFAULT;
    private String publicMessageOrder = StampurService.MESSAGEORDER_DEFAULT;
    private String savedMessageOrder = StampurService.MESSAGEORDER_DEFAULT;
    private String sentMessageOrder = StampurService.MESSAGEORDER_DEFAULT;

    private int parentMenus;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
        bar.setTitle(getString(R.string.InboxTitle));
        bar.setProgressBar(new OnClickListener() {
            public void onClick(View v) { refresh(); }
        });
        bindService(new Intent(this, StampurService.class), serviceConnection, Context.BIND_AUTO_CREATE);
        this.context = this;

        INBOXTAG = getString(R.string.InboxUser);
        PUBLICTAG = getString(R.string.InboxPublic);
        SAVEDTAG = getString(R.string.InboxSaved);
        SENTTAG = getString(R.string.InboxSent);
	}
	
	@Override
	protected void onDestroy() {
	    super.onDestroy();
		unbindService(serviceConnection);
	}
	
	private ServiceConnection serviceConnection = new ServiceConnection(){
        public void onServiceConnected(ComponentName className, IBinder serviceBinder) {
        	stampurService = ((StampurService.StampurService_Binder)serviceBinder).getService();
        	//populateInbox();
        	new ServerRequest_PopulateInbox().execute();
        }
        public void onServiceDisconnected(ComponentName arg0){ }
    };   
		
    private void populateInbox(){  
        inbox = addTab(stampurService.getMessagesForUser(inboxPageCounter, inboxMessageOrder), INBOXTAG);
        publicbox = addTab(stampurService.getMessagesForStampurStamp(publicPageCounter, publicMessageOrder), PUBLICTAG);
        saved = addTab(stampurService.getMessagesSaved(savedPageCounter, savedMessageOrder), SAVEDTAG);     
        sent = addTab(stampurService.getMessagesSent(sentPageCounter, sentMessageOrder), SENTTAG);
    }
    
    private class ServerRequest_PopulateInbox extends AsyncTask<Void, Void, Void> {
		private ProgressDialog dialog;
		private ArrayList<Message> inboxMessages;
		private ArrayList<Message> publicMessages;
		private ArrayList<Message> savedMessages;
		private ArrayList<Message> sentMessages;
		
		@Override 
		public void onPreExecute(){
			dialog = ProgressDialog.show(Activity_Inbox.this, "", "Loading. Please wait...", true);
		}
		
		@Override
		public Void doInBackground(Void... params) {
			inboxMessages = stampurService.getMessagesForUser(inboxPageCounter, inboxMessageOrder);
	        publicMessages = stampurService.getMessagesForStampurStamp(publicPageCounter, publicMessageOrder);
	        savedMessages = stampurService.getMessagesSaved(savedPageCounter, savedMessageOrder); 
	        sentMessages = stampurService.getMessagesSent(sentPageCounter, sentMessageOrder);
			return null;
		}
		
		@Override
		public void onPostExecute(final Void unused) {
			dialog.hide();
			inbox = addTab(inboxMessages, INBOXTAG);
	        publicbox = addTab(publicMessages, PUBLICTAG);
	        saved = addTab(savedMessages, SAVEDTAG);     
	        sent = addTab(sentMessages, SENTTAG);	
		}		
	}	   
    
    private Adapter_Message addTab(ArrayList<Message> messages, String tabTag) { 
    	Adapter_Message messageAdapter = new Adapter_Message(this, R.layout.item_inboxitem, -1, messages);      
    	View tab = View.inflate(this.getBaseContext(), R.layout.item_tab, null);    	
    	ListView tabListView = (ListView) tab.findViewById(R.id.tab_list);
    	TextView loadMoreMsgsView = (TextView) View.inflate(this, R.layout.item_loadmoremessages, null);    	
    	       
        tabListView.setTag(tabTag);
        tabListView.addFooterView(loadMoreMsgsView);
        tabListView.setAdapter(messageAdapter);
        
        tabListView.setOnItemClickListener(new OnItemClickListener(){
        	public void onItemClick(AdapterView<?> adapterView, View view, int position, long l){
                goToMessage(view, position);
        	}
        });         
        
        loadMoreMsgsView.setOnClickListener(new OnClickListener() {
        	public void onClick(View view){ loadMoreMessages(view); }
        }); 
                
        tab.findViewById(R.id.tab_sortOption1).setTag(StampurService.MESSAGEORDER_HOT);
        tab.findViewById(R.id.tab_sortOption2).setTag(StampurService.MESSAGEORDER_TOP);
        tab.findViewById(R.id.tab_sortOption3).setTag(StampurService.MESSAGEORDER_TIME);
            
        TextView currentSortingView = (TextView) tab.findViewWithTag(StampurService.MESSAGEORDER_DEFAULT);	
		super.sortBy(currentSortingView);
        
        setupTab(tab, tabTag); //add views to tab host  
        
        return messageAdapter;
    }
	
    private void goToMessage(View v, int position) {
        Message clickedMessage;
        String parentTag = (String) ((View) v.getParent()).getTag();

        if (parentTag.equals(INBOXTAG)) {
            clickedMessage = inbox.getItem(position);
        } else if (parentTag.equals(PUBLICTAG)) {
            clickedMessage = publicbox.getItem(position);
        } else if (parentTag.equals(SAVEDTAG)) {
            clickedMessage = saved.getItem(position);
        } else {
            clickedMessage = sent.getItem(position);
        }
       
        deleteAllAppFiles(); //TODO wtf does this do
		Intent message_intent = new Intent(context, Activity_ViewMessage.class);
		message_intent.putExtra("messageID", clickedMessage.getId());
		context.startActivity(message_intent);
	}
    
    private void loadMoreMessages(View v){
    	ArrayList<Message> moreMessages = null;
    	Adapter_Message currentTab = null;
    	String parentTag = (String) ((View) v.getParent()).getTag();

    	v.setClickable(false); //Disable click to prevent clicking more than once

        if (parentTag.equals(INBOXTAG)) {
            inboxPageCounter++;
            moreMessages = stampurService.getMessagesForUser(inboxPageCounter, inboxMessageOrder);
            currentTab = inbox;            
        } else if (parentTag.equals(PUBLICTAG)) {
            publicPageCounter++;
            moreMessages = stampurService.getMessagesForStampurStamp(publicPageCounter, publicMessageOrder);
            currentTab = publicbox;
        } else if (parentTag.equals(SAVEDTAG)) {
            savedPageCounter++;
            moreMessages = stampurService.getMessagesSaved(savedPageCounter, savedMessageOrder);
            currentTab = saved;            
        } else if (parentTag.equals(SENTTAG)) {
            sentPageCounter++;
            moreMessages = stampurService.getMessagesSent(sentPageCounter, sentMessageOrder);
            currentTab = sent;
        } 
        
        if(moreMessages == null || currentTab == null){ 
        	displayToast(R.string.FAIL);
        } else{
	        if(moreMessages.size() == 0){
	        	displayToast(R.string.NoMoreMessages);
	        } else{
	            for (Message m : moreMessages) currentTab.add(m); 
	            currentTab.notifyDataSetChanged();
	        }
        }
        
        v.setClickable(true);  //Enable click after the messages are loaded
    }
    
    public void refresh(){
    	bar.toggleProgressBar();
    	
    	bar.toggleProgressBar();
    }
    
    public void updateMessages(View v){
    	super.sortBy(v);  
    	bar.showProgressBar();
    	v.setClickable(false);
    	
    	//new ServerRequest_updateMessages().execute(tabHost.getCurrentTabTag());
    	
    	
    	String tag = tabHost.getCurrentTabTag();
    	String sortBy = (String) v.getTag();
    	ArrayList<Message> updatedMessages = null;
		Adapter_Message currentTab = null;
		
    	
    	if(tag.equals(INBOXTAG)){
			inboxPageCounter = 1;
			inboxMessageOrder = sortBy;
			updatedMessages = stampurService.getMessagesForUser(inboxPageCounter, inboxMessageOrder);
			currentTab = inbox;
		}
		else if(tag.equals(PUBLICTAG)){
			publicPageCounter = 1;
			publicMessageOrder = sortBy;
			updatedMessages = stampurService.getMessagesForStampurStamp(publicPageCounter, publicMessageOrder);
			currentTab = publicbox;
		}
		else if(tag.equals(SAVEDTAG)){
			savedPageCounter = 1;
			savedMessageOrder = sortBy;
			updatedMessages = stampurService.getMessagesSaved(savedPageCounter, savedMessageOrder); 
			currentTab = saved;
		}
		else if(tag.equals(SENTTAG)){
			sentPageCounter = 1;
			sentMessageOrder = sortBy;
			updatedMessages = stampurService.getMessagesSent(sentPageCounter, sentMessageOrder);
			currentTab = saved;				
		}
		else{ }
    	
    	if(currentTab != null && updatedMessages != null){			
    		currentTab.clearMessages();
    		currentTab.addMessages(updatedMessages);
    		currentTab.notifyDataSetChanged();
			tabHost.childDrawableStateChanged(tabHost.getCurrentTabView());
			tabHost.refreshDrawableState();
		} else displayToast(R.string.FAIL);
		
    	v.setClickable(true);
    	bar.hideProgressBar();
    }        
    
    private void displayToast(int nomoremessages) {
        Toast toast = Toast.makeText(this, nomoremessages, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.show();
    }

    private void deleteAllAppFiles() {
        //Delete all files saved by the app
        //TODO: Move this to a separate util class/method and call this whenever necessary
        String[] fileList = this.fileList();
        for (String file : fileList) {
            this.deleteFile(file);
        }
    }

    private void goToPost() {
        if (isNetworkAvailable()) {
            Intent myIntent = new Intent(this, Activity_PostMessage.class);
            startActivityForResult(myIntent, 0);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        parentMenus = menu.size();

        int groupId = 0;
        int menuItemOrder = Menu.NONE;
        int menuItemText = R.string.Post;
        MenuItem settings = menu.add(groupId, parentMenus + 1, menuItemOrder, menuItemText);
        settings.setIcon(R.drawable.gear);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(super.onOptionsItemSelected(item)) {
            return true;
        } else if (item.getItemId() == parentMenus + 1) {
            goToPost();
            return true;
        }

        return false;
    }

}
    
/*=== LEGACY CODE SECTION (for reference) ===============================================================*/
    
    /*
    private class ServerRequest_updateMessages extends AsyncTask<String, Void, String> {
		//private ProgressDialog dialog;
		private ArrayList<Message> updatedMessages = null;
		private Adapter_Message currentTab = null;
		
		@Override 
		public void onPreExecute(){
			//dialog = ProgressDialog.show(Activity_Inbox.this, "", "Loading. Please wait...", true);
			bar.showProgressBar();
		}
		
		@Override
		public String doInBackground(String... params) {
			if(params[0].equals(INBOXTAG)){
				inboxPageCounter = 1;
				updatedMessages = stampurService.getMessagesForUser(inboxPageCounter, StampurService.MESSAGEORDER_DEFAULT);
				currentTab = inbox;
			}
			else if(params[0].equals(PUBLICTAG)){
				publicPageCounter = 1;
				updatedMessages = stampurService.getMessagesForStampurStamp(publicPageCounter);
				currentTab = publicbox;
			}
			else if(params[0].equals(SAVEDTAG)){
				savedPageCounter = 1;
				updatedMessages = stampurService.getMessagesSaved(savedPageCounter); 
				currentTab = saved;
			}
			else if(params[0].equals(SENTTAG)){
				sentPageCounter = 1;
				updatedMessages = stampurService.getMessagesSent(sentPageCounter);
				currentTab = saved;				
			}
			else{ }		
			
			return params[0];
		}
		
		@Override
		public void onPostExecute(String tabTag) {
			if(currentTab != null && updatedMessages != null){
				currentTab.clear();
				for(Message m : updatedMessages) currentTab.add(m);
				currentTab.notifyDataSetChanged();
				tabHost.childDrawableStateChanged(tabHost.getCurrentTabView());
			} else displayToast(R.string.FAIL);			
			//dialog.hide();
			bar.hideProgressBar();			
		}		
	}	
     */
    
    
    
    
    /*	
	private View userMessagesContents;
	private View publicMessagesContents;
	private View savedMessagesContents;
	private ListView userMessagesList;
	private ListView publicMessagesList;
	private ListView savedMessagesList;
    private TextView loadMoreUserMsgs;	
    */
    
    /*
    private void populateInbox() {
        ArrayList<Message> inboxMessages_list =
                stampurService.getMessagesForUser(loadUserMessagesCounter);
        ArrayList<Message> stampurstampMessages_list =
                stampurService.getMessagesForStampurStamp(loadPublicMessagesCounter);
        ArrayList<Message> savedMessages_list =
                stampurService.getMessagesSaved(loadSavedMessagesCounter);

        messageAdapterInbox = new Adapter_Message(this, R.layout.item_inboxitem, -1, inboxMessages_list);
        messageAdapterPublic = new Adapter_Message(this, R.layout.item_inboxitem, -1, stampurstampMessages_list);
        messageAdapterSaved = new Adapter_Message(this, R.layout.item_inboxitem, -1, savedMessages_list);

        userMessagesContents = View.inflate(this.getBaseContext(), R.layout.item_tab, null);
        publicMessagesContents = View.inflate(this.getBaseContext(), R.layout.item_tab, null);
        savedMessagesContents = View.inflate(this.getBaseContext(), R.layout.item_tab, null);

        userMessagesList = (ListView) userMessagesContents.findViewById(R.id.tab_list);
        userMessagesList.setTag(USERINBOXTAG);
        loadMoreUserMsgs = (TextView) View.inflate(this, R.layout.item_loadmoremessages, null);
        loadMoreUserMsgs.setOnClickListener(new LoadMoreMessagesClickListener());
        userMessagesList.addFooterView(loadMoreUserMsgs);
        userMessagesList.setAdapter(messageAdapterInbox);
        userMessagesList.setOnItemClickListener(new MessageClickListener());

        publicMessagesList = (ListView) publicMessagesContents.findViewById(R.id.tab_list);
        publicMessagesList.setTag(PUBLICINBOXTAG);
        TextView loadMorePublicMsgs = (TextView) View.inflate(this, R.layout.item_loadmoremessages, null);
        loadMorePublicMsgs.setOnClickListener(new LoadMoreMessagesClickListener());
        publicMessagesList.addFooterView(loadMorePublicMsgs);
        publicMessagesList.setAdapter(messageAdapterPublic);
        publicMessagesList.setOnItemClickListener(new MessageClickListener());

        savedMessagesList = (ListView) savedMessagesContents.findViewById(R.id.tab_list);
        savedMessagesList.setTag(SAVEDINBOXTAG);
        TextView loadMoreSavedMsgs = (TextView) View.inflate(this, R.layout.item_loadmoremessages, null);
        loadMoreSavedMsgs.setOnClickListener(new LoadMoreMessagesClickListener());
        savedMessagesList.addFooterView(loadMoreSavedMsgs);
        savedMessagesList.setAdapter(messageAdapterSaved);
        savedMessagesList.setOnItemClickListener(new MessageClickListener());

        //add views to tab host
        setupTab(userMessagesContents, USERINBOXTAG);
        setupTab(publicMessagesContents, PUBLICINBOXTAG);
        setupTab(savedMessagesContents, SAVEDINBOXTAG);
    }  
	*/

    /*
    public void goToMessage(View v, int position) {
        deleteAllAppFiles();

        Message clickedMessage;

        if(userMessagesList.equals(v.getParent())) {
            clickedMessage = messageAdapterInbox.getItem(position);
        }else if(publicMessagesList.equals(v.getParent())){
            clickedMessage = messageAdapterPublic.getItem(position);
        }else{
        	clickedMessage = messageAdapterSaved.getItem(position);
        }

		Intent message_intent = new Intent(context, Activity_ViewMessage.class);
		message_intent.putExtra("messageID", clickedMessage.getId());
		context.startActivity(message_intent);
	}
    */
    
    /*
    private void loadMoreMessages(View view) {
        if (userMessagesList.equals(view.getParent())) {
            loadUserMessagesCounter++;
            ArrayList<Message> messageList =
                    stampurService.getMessagesForUser(loadUserMessagesCounter);
            for (Message userMessage : messageList) {
                messageAdapterInbox.add(userMessage);
            }

            messageAdapterInbox.notifyDataSetChanged();
        } else if (publicMessagesList.equals(view.getParent())) {
            loadPublicMessagesCounter++;
            ArrayList<Message> messageList =
                    stampurService.getMessagesForStampurStamp(loadPublicMessagesCounter);
            for (Message userMessage : messageList) {
                messageAdapterPublic.add(userMessage);
            }
            messageAdapterPublic.notifyDataSetChanged();
        } else if (savedMessagesList.equals(view.getParent())) {
            loadSavedMessagesCounter++;
            ArrayList<Message> messageList =
                    stampurService.getMessagesSaved(loadSavedMessagesCounter);
            for (Message userMessage : messageList) {
                messageAdapterSaved.add(userMessage);
            }
            messageAdapterSaved.notifyDataSetChanged();
        }

        loadMoreUserMsgs.setClickable(true);  //Enable click after the messages are loaded
    }
	*/
    
/*=======================================================================================================*/
