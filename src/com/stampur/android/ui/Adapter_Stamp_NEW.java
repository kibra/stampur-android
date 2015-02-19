package com.stampur.android.ui;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.stampur.android.R;
import com.stampur.android.Stamp;
import com.stampur.android.Stamp.Category;
import com.stampur.android.Stampur;
import com.stampur.android.User;


public class Adapter_Stamp_NEW extends BaseExpandableListAdapter{

/*--- Fields -------------------------------------------------------------------------*/
	 	    
	private final Activity act;
    private ArrayList<View> stampViewList;
	private HashMap<Stamp.Category, ArrayList<Stamp>> stampList = new HashMap<Stamp.Category, ArrayList<Stamp>>();	 
	private HashMap<Integer, Stamp.Category> categoryList = new HashMap<Integer, Stamp.Category>();
 
/*--- Constructors --------------------------------------------------------------------*/
	
	public Adapter_Stamp_NEW(Activity act, ArrayList<Stamp> stamp_list) {
    	this.act = act;
   	
    	ArrayList<Stamp> stampsForCategory;
    	//sorts stamps by categories and adds them to the data set
    	for(Stamp.Category c : Stamp.Category.values()){
    		stampsForCategory = new ArrayList<Stamp>();
    		for(Stamp s : stamp_list) if(s.getCategory().equals(c)) stampsForCategory.add(s);
    		this.AddGroup(c, stampsForCategory);
    	}   	

        stampViewList = new ArrayList<View>();
    }   
 
/*--- Methods --------------------------------------------------------------------------*/

	public boolean hasStableIds(){ return false;}    
    public boolean isChildSelectable(int groupPosition, int childPosition){ return false; }
	
	
    private void AddGroup(Stamp.Category c, ArrayList<Stamp> stampsForCategory){
        stampList.put(c, stampsForCategory);  
        categoryList.put(categoryList.size(), c);          
    }
 
    
    public Object getChild(int groupPosition, int childPosition){
        if(categoryList.containsKey(groupPosition))
        	return stampList.get(categoryList.get(groupPosition)).get(childPosition);
        else return null;
    }
    
    public long getChildId(int groupPosition, int childPosition){  
        return 0;
    }
        
    public int getChildrenCount(int groupPosition){
        if(categoryList.containsKey(groupPosition))
            return stampList.get(categoryList.get(groupPosition)).size();
        else return 0;
    }
        
    public Object getGroup(int groupPosition){
        if(categoryList.containsKey(groupPosition))
            return stampList.get(categoryList.get(groupPosition));
        else return null;
    }
    
    public int getGroupCount(){ return categoryList.size(); }   
    public long getGroupId(int groupPosition){ return 0; }    
    public void addStamp(Stamp s){ stampList.get(s.getCategory()).add(s); }    
    public void removeStamp(Stamp s){ stampList.get(s.getCategory()).remove(s); }
    
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent){
        
    	View rowView;
        TextView stampText;
        ImageView add_sub;
        Stamp s = (Stamp) this.getChild(groupPosition, childPosition);

        if(convertView == null)        	
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

		//stampViewList.add((groupPosition * categoryList.size() + childPosition), rowView);
        return rowView;    	
    }

    public View getGroupView(int groupPos, boolean isExpanded, View convertView, ViewGroup parent){
        
    	View rowView;
        TextView groupName;        
    	
    	if(convertView == null)        	
        	rowView = act.getLayoutInflater().inflate(R.layout.item_stampbook_groupitem, null, true);        
        else rowView = convertView;      
    	
    	groupName = (TextView) rowView.findViewById(R.id.stampbook_groupitem_groupName);
        groupName.setText(categoryList.get(groupPos).toString());
        
        return rowView;
    }
    
    
}