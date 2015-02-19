package com.stampur.android.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.stampur.android.R;
import com.stampur.android.Message;
import com.stampur.android.Stamp;
import com.stampur.android.Stamp.Category;

public class Adapter_Message extends ArrayAdapter<Message> {

    private Context context;
    private List<Message> messages;
    private LayoutInflater inflater;

    public Adapter_Message(Context ctxt, int resource, int textViewResourceId,
            List<Message> objects) {
        super(ctxt, resource, textViewResourceId, objects);
        this.context = ctxt;
        this.messages = (objects != null) ? objects : new ArrayList<Message>();
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount(){ return messages.size(); }
    public Message getItem(int position){ return messages.get(position); }
    public void clearMessages(){ messages.clear(); }
    public void addMessages(List<Message> messages){ for(Message m : messages) this.messages.add(m); }
    public void setMessages(List<Message> messages){ this.messages = messages; }
    public List<Message> getMessages(){ return this.messages; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MessageViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_inboxitem, null);
            holder = new MessageViewHolder();
            holder.title = (TextView) convertView.findViewById(R.id.inboxitem_messageTitle);
            holder.timestamp = (TextView) convertView.findViewById(R.id.inboxitem_messageTime);
            holder.body = (TextView) convertView.findViewById(R.id.inboxitem_messageBody);
            holder.stamps = (LinearLayout) convertView.findViewById(R.id.inboxitem_stampLayout);
            holder.numofcomments = (TextView) convertView.findViewById(R.id.inboxitem_messageNumComments);
            convertView.setTag(holder);
        } else {
            holder = (MessageViewHolder) convertView.getTag();
        }

        Message message = messages.get(position);
        holder.title.setText(message.getTitle());
        holder.timestamp.setText(message.getTimeString());
        holder.body.setText(message.getBody());
        
        holder.stamps.removeViewsInLayout(1, (holder.stamps.getChildCount() - 1));
        for(Stamp s : message.getStamps()){
        	TextView stamp = (TextView) View.inflate(context, R.layout.view_stamptextview, null);               
            LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            llp.setMargins(2, 0, 2, 0); // llp.setMargins(left, top, right, bottom);
            stamp.setLayoutParams(llp);       	
        	stamp.setText(s.toString());
        	if(s.getCategory().equals(Category.SPECIAL)) stamp.setTextColor(context.getResources().getColor(R.color.red));
        	holder.stamps.addView(stamp);
        }

        if (message.getNumComments() < 2) {
            holder.numofcomments.setText(
                    context.getString(R.string.InboxComment, message.getNumComments()));
        } else {
            holder.numofcomments.setText(
                    context.getString(R.string.InboxComments, message.getNumComments()));
        }

        return convertView;
    }

    private static class MessageViewHolder {
        TextView title;
        TextView body;
        TextView timestamp;
        LinearLayout stamps;
        TextView numofcomments;
    }
}
