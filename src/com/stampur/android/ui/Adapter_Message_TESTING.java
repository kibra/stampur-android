package com.stampur.android.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.stampur.android.R;
import com.stampur.android.Message;

public class Adapter_Message_TESTING extends ArrayAdapter<Message> {

    private List<Message> messages;
    private LayoutInflater inflater;

    public Adapter_Message_TESTING(Context context, int resource, int textViewResourceId,
            List<Message> objects) {
        super(context, resource, textViewResourceId, objects);
        this.messages = (objects != null) ? objects : new ArrayList<Message>();
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return messages.size();
    }

    public Message getItem(int position) {
        return messages.get(position);
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public List<Message> getMessages() {
        return this.messages;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
    	Message message = messages.get(position);
    	View rowView;
    	TextView messageTitle;
    	TextView messageBody;
    	TextView messageTimestamp;    	
    	TextView messageNumComments;
    	
        if(convertView == null) rowView = inflater.inflate(R.layout.item_inboxitem, null, true);           
        else rowView = convertView;
               
        messageTitle = (TextView) convertView.findViewById(R.id.inboxitem_messageTitle);
        messageBody = (TextView) convertView.findViewById(R.id.inboxitem_messageBody);
        messageTimestamp = (TextView) convertView.findViewById(R.id.inboxitem_messageTime);
       // messageStamps = (TextView) convertView.findViewById(R.id.inboxitem_messageStamps);
        messageNumComments = (TextView) convertView.findViewById(R.id.inboxitem_messageNumComments);       
        
        messageTitle.setText(message.getTitle());        
        messageBody.setText(message.getBody());
        messageTimestamp.setText(message.getTimeString());
       // messageStamps.setText(message.getStamps().toString());
        messageNumComments.setText("#Comments: " + Integer.toString(message.getNumComments()));       
        
        rowView.setTag(message.getId());        
        return rowView;
    }
}


