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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;

import com.stampur.android.Message;
import com.stampur.android.R;
import com.stampur.android.Stamp;
import com.stampur.android.Stampur;
import com.stampur.android.User;
import com.stampur.android.service.StampurService;

public class Activity_StampBook extends Activity_BaseTab {

	private static final String STAMPORDER_ALPHABETIC = "alphab";
	private static final String STAMPORDER_POPULARITY = "popular";
	private static final String STAMPORDER_GROUP = "group";
	private static final String STAMPORDER_DEFAULT = STAMPORDER_ALPHABETIC;
	
	private User user;
	private StampurService stampurService;
	
	private static final String TABTAG1 = "My Stampbook";
	private static final String TABTAG2 = "Browse Stamps";
		
	//private Adapter_Stamp adapterTab1;
	//private Adapter_Stamp adapterTab2;
	
	private Adapter_Stamp_NEW adapterTab1_group;
	private Adapter_Stamp_NEW adapterTab2_group;
	private Adapter_Stamp adapterTab1_list;
	private Adapter_Stamp adapterTab2_list;
	
	private String sortOption1;
	private String sortOption2;

    private int parentMenus = 0;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {				
		super.onCreate(savedInstanceState);		
		bar.setTitle("Stamp Book");	
		bar.addActionIcon(R.drawable.post, new OnClickListener(){ public void onClick(View v){ goToCreateStamp(); }});
        user = Stampur.getUser();
        bindService(new Intent(this, StampurService.class), serviceConnection, Context.BIND_AUTO_CREATE);
        
	}
	
	@Override
	protected void onDestroy() {
	    super.onDestroy();
		unbindService(serviceConnection);
	}
	
	private ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder serviceBinder) {
        	stampurService = ((StampurService.StampurService_Binder)serviceBinder).getService();        	
        	new ServerRequest_PopulateStampbook().execute();
        }
        public void onServiceDisconnected(ComponentName arg0){  }
    };
		
	private class ServerRequest_PopulateStampbook extends AsyncTask<Void, Void, Void> {
		private ProgressDialog dialog;
		private ArrayList<Stamp> userStamps;
		private ArrayList<Stamp> publicStamps;
		
		@Override 
		public void onPreExecute(){
			dialog = ProgressDialog.show(Activity_StampBook.this, "", "Loading. Please wait...", true);
		}
		
		@Override
		public Void doInBackground(Void... params) {
			userStamps = stampurService.getUserStamps();
			publicStamps = stampurService.getPublicStamps();
			return null;
		}
		
		@Override
		public void onPostExecute(final Void unused) {
			dialog.hide();
			user.setStampbook(userStamps);
			populateStampbook(userStamps, publicStamps);
				
		}		
	}	
	
	private void populateStampbook(ArrayList<Stamp> userStamps, ArrayList<Stamp> publicStamps){
		adapterTab1_group = new Adapter_Stamp_NEW(this, userStamps); 
		adapterTab1_list = new Adapter_Stamp(this, userStamps);
		
		adapterTab2_group = new Adapter_Stamp_NEW(this, publicStamps); 
		adapterTab2_list = new Adapter_Stamp(this, publicStamps);
		
		adapterTab1_list.sortAlphabetic();
		adapterTab2_list.sortAlphabetic();
		
		sortOption1 = STAMPORDER_DEFAULT;   
		sortOption2 = STAMPORDER_DEFAULT;  	
		
		createTab(adapterTab1_group, adapterTab1_list, TABTAG1, sortOption1);
		createTab(adapterTab2_group, adapterTab2_list, TABTAG2, sortOption2);		
	}	
	
	private void createTab(Adapter_Stamp_NEW groupAdapter, Adapter_Stamp listAdapter, String tabTag, String sortOption){		
		View tab = View.inflate(this.getBaseContext(), R.layout.item_tab2, null); 
		
    	ExpandableListView tabListView_Expandable = (ExpandableListView) tab.findViewById(R.id.tab2_stampList1);
    	tabListView_Expandable.setTag(tabTag);		
		tabListView_Expandable.setAdapter(groupAdapter);
		
    	ListView tabListView = (ListView) tab.findViewById(R.id.tab2_stampList2);
    	tabListView.setTag(tabTag);		
		tabListView.setAdapter(listAdapter);
				
		tab.findViewById(R.id.tab2_sortOption1).setTag(STAMPORDER_ALPHABETIC);
        tab.findViewById(R.id.tab2_sortOption2).setTag(STAMPORDER_POPULARITY);
        tab.findViewById(R.id.tab2_sortOption3).setTag(STAMPORDER_GROUP);       
             		
		super.sortBy(tab.findViewWithTag(sortOption));		
	    setupTab(tab, tabTag); //add views to tab host          
    }	
	
	public void updateTab(View v){
    	bar.showProgressBar();
    	v.setClickable(false);
    	super.sortBy(v);  
    	    	    	
    	String tag = tabHost.getCurrentTabTag();    	
    	String sortBy = (String) v.getTag();
    	
    	View tab = tabHost.getCurrentView();
		ExpandableListView tabListView_Expandable = (ExpandableListView) tab.findViewById(R.id.tab2_stampList1);
		ListView tabListView = (ListView) tab.findViewById(R.id.tab2_stampList2);    		
    	
		Adapter_Stamp listAdapter;
		
    	if(tag.equals(TABTAG1)){		
    		sortOption1 = sortBy;
    		listAdapter = adapterTab1_list;
		}
		else if(tag.equals(TABTAG2)){			
			sortOption2 = sortBy;
			listAdapter = adapterTab2_list;
		}
		else{ listAdapter = new Adapter_Stamp(null, null);} //TODO major fail here, deal with this
    	
    	if(sortBy.equals(STAMPORDER_ALPHABETIC)){
    		listAdapter.sortAlphabetic();
    		listAdapter.notifyDataSetChanged();
			tabListView_Expandable.setVisibility(View.GONE);
			tabListView.setVisibility(View.VISIBLE);
    	} else if(sortBy.equals(STAMPORDER_POPULARITY)){	
    		listAdapter.sortPopular();
    		listAdapter.notifyDataSetChanged();
			tabListView_Expandable.setVisibility(View.GONE);
			tabListView.setVisibility(View.VISIBLE);
		} else{
			tabListView.setVisibility(View.GONE);
			tabListView_Expandable.setVisibility(View.VISIBLE);
		}
    	
    	v.setClickable(true);
    	bar.hideProgressBar();
    }
		

	public void plus(View v){			
		Stamp stamp = (Stamp) ((View) v.getParent()).getTag();
		stampurService.addStamp(stamp.getId()); //add stamp on server		
		user.addStamp(stamp); //add stamp to local stampbook
		adapterTab1_group.addStamp(stamp); //add stamp to adapter
		adapterTab1_list.addStamp(stamp); //add stamp to other adapter
		adapterTab1_group.notifyDataSetChanged();
		adapterTab2_group.notifyDataSetChanged();
		adapterTab1_list.notifyDataSetChanged();
		adapterTab2_list.notifyDataSetChanged();
		tabHost.childDrawableStateChanged((View) v.getParent());		
	}

	public void minus(View v){			
		Stamp stamp = (Stamp) ((View) v.getParent()).getTag();
		stampurService.removeStamp(stamp.getId()); //remove stamp on server
		user.removeStamp(stamp); //remove stamp from local stampbook
		adapterTab1_group.removeStamp(stamp); //add stamp to adapter
		adapterTab1_list.removeStamp(stamp); //add stamp to other adapter
		adapterTab1_group.notifyDataSetChanged();
		adapterTab2_group.notifyDataSetChanged();
		adapterTab1_list.notifyDataSetChanged();
		adapterTab2_list.notifyDataSetChanged();
		tabHost.childDrawableStateChanged((View) v.getParent());
	}	
	
	public void goToViewStamp(View v) {
		Intent intent = new Intent(this, Activity_ViewStamp.class);
		intent.putExtra("StampID", v.getTag().toString());
        startActivityForResult(intent, 0);
	}

	public void goToCreateStamp() {
		Intent intent = new Intent(this, Activity_CreateStamp.class);
        startActivity(intent);
	}

/*
private Adapter_Stamp_NEW addTab(ArrayList<Stamp> stamps, String tabTag) {		
		Adapter_Stamp stampAdapter = new Adapter_Stamp(this, stamps); 		
		View tab = View.inflate(this.getBaseContext(), R.layout.item_tab, null); 		
    	ListView tabListView = (ListView) tab.findViewById(R.id.tab_list);			
    	
		tabListView.setTag(tabTag);		
		tabListView.setAdapter(stampAdapter);
		
	    tabListView.setOnItemClickListener(new OnItemClickListener() {
	        public void onItemClick(AdapterView<?> parent, View v, int position, long id){ goToViewStamp(v); }
	    });
	   
	    setupTab(tab, tabTag); //add views to tab host           
        return stampAdapter;
    }
*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        parentMenus = menu.size();

        int groupId = 0;
        int menuItemOrder = Menu.NONE;
        int menuItemText = R.string.CreateStamp;
        MenuItem settings = menu.add(groupId, parentMenus + 1, menuItemOrder, menuItemText);
        settings.setIcon(R.drawable.gear);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(super.onOptionsItemSelected(item)) {
            return true;
        } else if (item.getItemId() == parentMenus + 1) {
            goToCreateStamp();
            return true;
        }

        return false;
    }
	
}