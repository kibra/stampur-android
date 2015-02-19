package com.stampur.android.ui;

import java.util.ArrayList;
import java.util.Collections;

import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.stampur.android.R;
import com.stampur.android.Stamp;
import com.stampur.android.Stamp.Category;
import com.stampur.android.Stampur;
import com.stampur.android.User;
import com.stampur.android.util.StampByNameComparator;
import com.stampur.android.util.StampByUsersComparator;

public class Adapter_Stamp extends BaseAdapter {
    private Activity act;
    private ArrayList<Stamp> stamp_list;
    private ArrayList<View> stampView_list;

    public Adapter_Stamp(Activity act, ArrayList<Stamp> stamp_list) {
    	this.act = act;
        this.stamp_list = new ArrayList<Stamp>(stamp_list);
        stampView_list = new ArrayList<View>();
    }

    public int getCount(){ return stamp_list.size(); }
    public Object getItem(int position){ return stamp_list.get(position); }
    public long getItemId(int position){ return stampView_list.get(position).getId(); }

    public void addStamp(Stamp s){ stamp_list.add(s); }
    public void removeStamp(Stamp s){ stamp_list.remove(s); }
    
    public void sortAlphabetic(){ Collections.sort(stamp_list, StampByNameComparator.getInstance()); }    
    public void sortPopular(){ Collections.sort(stamp_list, StampByUsersComparator.getInstance()); }

    // create a new StampView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
    	View rowView;
        TextView stampText;
        ImageView add_sub;
        Stamp s = (Stamp) this.getItem(position);        

        if (convertView == null)        	
        	rowView = act.getLayoutInflater().inflate(R.layout.item_stampbookitem, null, true);        
        else rowView = convertView;       

        stampText = (TextView) rowView.findViewById(R.id.stampbookitem_stampName);
        add_sub = (ImageView) rowView.findViewById(R.id.stampbookitem_plusMinus);

        User thisUser = Stampur.getUser();
		if(thisUser.hasStamp(s) == false){
			add_sub.setImageResource(R.drawable.plus);
			add_sub.setOnClickListener(new OnClickListener(){
				public void onClick(View v){((Activity_StampBook)act).plus(v);}});
		}else{
			add_sub.setImageResource(R.drawable.minus);
			add_sub.setOnClickListener(new OnClickListener(){
				public void onClick(View v){((Activity_StampBook)act).minus(v);}});
		}

        stampText.setTag(s.getId());
        stampText.setText(s.getLabel());
		rowView.setTag(s);

		rowView.setOnClickListener(new OnClickListener() {
	        public void onClick(View v){ ((Activity_StampBook) act).goToViewStamp(v); }
	    });

		
        if(s.getCategory().equals(Category.PEOPLE))stampText.setBackgroundResource(R.drawable.bg_stamp_user);
        else stampText.setBackgroundResource(R.drawable.bg_stamp);
		
		stampView_list.add(position, rowView);
        return rowView;
    }
}