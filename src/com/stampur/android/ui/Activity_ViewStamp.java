package com.stampur.android.ui;

import java.util.ArrayList;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.stampur.android.R;
import com.stampur.android.Message;
import com.stampur.android.Stamp;
import com.stampur.android.Stamp.Category;
import com.stampur.android.service.StampurService;

public class Activity_ViewStamp extends Activity_Base {

	private StampurService stampurService;
    private ServiceConnection serviceConnection;

	private Adapter_Message messageAdapter;

    //The Stamp that is currenty being viewed/displayed
    private Stamp stamp;

    private int messagesPageCounter = 1;

    private ListView messagesList;

	@Override
	public void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_viewstamp);

        messagesList = (ListView) this.findViewById(R.id.viewstamp_list);
		
        bar.setTitle(getString(R.string.ViewStamp));
		bar.addActionIcon(R.drawable.back,
				new OnClickListener() {
					public void onClick(View v){Activity_ViewStamp.this.finish();}
				}
		);

        if (isNetworkAvailable()) {
            initService();
            bindService(new Intent(this, StampurService.class), serviceConnection,
                    BIND_AUTO_CREATE);
        }
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
                displayStamp();
            }

            public void onServiceDisconnected(ComponentName arg0) {
            }
        };
    }
    
	private void displayStamp(){
		ArrayList<Message> messages;
		
		TextView stampImage = (TextView) this.findViewById(R.id.viewstamp_stampName);
		TextView stampNumUsers = (TextView) this.findViewById(R.id.viewstamp_numUsers);
		TextView stampDescription =  (TextView) this.findViewById(R.id.viewstamp_stampDescription);		
        TextView loadMoreMsgsView = (TextView) View.inflate(this, R.layout.item_loadmoremessages, null);
        
        messagesList.addFooterView(loadMoreMsgsView);
        loadMoreMsgsView.setOnClickListener(new OnClickListener() {
            public void onClick(View view) { loadMoreMessages(view); }
        });

        String StampID = this.getIntent().getStringExtra("StampID");
		
		if(StampID != null){			
			Log.i("ViewStamp", StampID);
			stamp = stampurService.getStamp(StampID);
			Log.i("ViewStamp", stamp.toString());
			stampImage.setText(stamp.getLabel());
			stampNumUsers.setText(stamp.getNumUsers() + " users");
			stampDescription.setText(stamp.getDescription());
						
			if(stamp.getCategory().equals(Category.PEOPLE))stampImage.setBackgroundResource(R.drawable.bg_stamp_user);
	        else stampImage.setBackgroundResource(R.drawable.bg_stamp);
			
			messages = stampurService.getMessagesForStamp(stamp.getLabel(), messagesPageCounter, StampurService.MESSAGEORDER_DEFAULT);
			//t.setText(t.getText().toString() + " NUM: " + messages.size());
			messageAdapter = new Adapter_Message(this, R.layout.item_inboxitem, -1, messages);				
			messagesList.setAdapter(messageAdapter);
	        messagesList.setOnItemClickListener(new OnItemClickListener(){
		        public void onItemClick(AdapterView<?> parent, View v, int pos, long id){ goToMessage(v, pos); }
		    });
	        messagesList.refreshDrawableState();
		}
		else{
			stampImage.setText("-----");
			stampDescription.setText("ERROR: STAMP DOES NOT EXIST");
		}
	}

    private void goToMessage(View v, int position) {
        if (isNetworkAvailable()) {
            Message clickedMessage;
            clickedMessage = messageAdapter.getItem(position);
            Intent message_intent = new Intent(this.getBaseContext(), Activity_ViewMessage.class);
            message_intent.putExtra("messageID", clickedMessage.getId());
            this.startActivity(message_intent);
        }
    }

    private void loadMoreMessages(View v) {
        if (isNetworkAvailable()) {
            ArrayList<Message> moreMessages;
            v.setClickable(false); //Disable click to prevent clicking more than once

            messagesPageCounter++;
            moreMessages =
                    stampurService.getMessagesForStamp(stamp.getLabel(), messagesPageCounter, StampurService.MESSAGEORDER_DEFAULT);

            if (moreMessages.size() == 0) {
                displayToast(R.string.NoMoreMessages);
            } else {
                for (Message m : moreMessages) {
                    messageAdapter.add(m);
                }
                messageAdapter.notifyDataSetChanged();
            }

            v.setClickable(true);  //Enable click after the messages are loaded
        }
    }

    private void displayToast(int messageResId) {
        Toast toast = Toast.makeText(this, messageResId, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.show();
    }

}
