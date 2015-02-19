package com.stampur.android.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.stampur.android.R;
import com.stampur.android.Comment;

public class Adapter_Comment extends ArrayAdapter<Comment> {

    private Context context;
	private List<Comment> comments;
	private LayoutInflater inflater;
	
	public Adapter_Comment(Context context, int resource, int textViewResourceId,
	        List<Comment> objects) {
	    super(context, resource, textViewResourceId, objects);
        this.context = context;
	    this.comments = (objects != null) ? objects : new ArrayList<Comment>();
	    this.notifyDataSetChanged();
	    this.inflater =
	            (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	public int getCount() {
	    return comments.size();
	}
	
	public Comment getItem(int position) {
	    return comments.get(position);
	}
	
	public void setComments(List<Comment> comments) {
	    this.comments = comments;
	    this.notifyDataSetChanged();
	}
	
	@Override
	public void add(Comment comment) {
	    comments.add(comment);
	    this.notifyDataSetChanged();
	}
	
	@Override
	public void remove(Comment comment) {
	    comments.remove(comment);
	    this.notifyDataSetChanged();
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
	    CommentViewHolder holder;
	
	    if (convertView == null) {
	        convertView = inflater.inflate(R.layout.item_commentitem, null);
	        holder = new CommentViewHolder();
	        holder.author = (TextView) convertView.findViewById(R.id.commentitem_commentAuthor);
	        holder.timestamp = (TextView) convertView.findViewById(R.id.commentitem_commentTime);
	        holder.text = (TextView) convertView.findViewById(R.id.commentitem_commentText);
	        holder.number = (TextView) convertView.findViewById(R.id.commentitem_commentNumber);
            holder.replies = (ListView) convertView.findViewById(R.id.commentitem_repliesList);
	        convertView.setTag(holder);
	    } else {
	        holder = (CommentViewHolder) convertView.getTag();
	    }
	
	    Comment comment = comments.get(position);
	    holder.author.setText(comment.getAuthor());
	    holder.timestamp.setText(comment.getTimeString());
	    holder.text.setText(comment.getText());
        holder.number.setText((position + 1) + ".");
        holder.replies.setAdapter(
                new Adapter_Comment(context, R.layout.item_commentitem, -1, comment.getReplies()));

        //Make URLs/Links in the Comments appear well
        holder.text.setMovementMethod(LinkMovementMethod.getInstance());
        Linkify.addLinks(holder.text, Linkify.ALL);

	    return convertView;
	}
	
	private class CommentViewHolder {
	    TextView author;
	    TextView text;
	    TextView timestamp;
	    TextView number;
        ListView replies;
	}
}

