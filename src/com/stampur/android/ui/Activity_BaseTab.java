package com.stampur.android.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;

import com.stampur.android.R;
import com.stampur.android.util.NetworkManager;

public abstract class Activity_BaseTab extends TabActivity {
	
	public static final int DEFAULT_SORTING_OPTION = R.id.tab_sortOption1;
	public int currentSortingOption;
	public Helper_ActionBar bar;		
	public TabHost tabHost;

    private final int NONETWORK = 1001;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        NetworkManager.init(this);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.layout_tabbage);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);  
		currentSortingOption = DEFAULT_SORTING_OPTION;
		makeBarHappen();
		makeTabsHappen();
	}
	
	@Override
    protected void onDestroy() {
        super.onDestroy();
    }
		
	private void makeTabsHappen(){
		tabHost = getTabHost();
		tabHost.setOnTabChangedListener(new OnTabChangeListener() {
			   public void onTabChanged(String arg0) {
				   Toast.makeText(Activity_BaseTab.this, arg0, Toast.LENGTH_SHORT).show();
			   }     
		}); 		
	}	
	
	private void makeBarHappen(){
		bar = (Helper_ActionBar) findViewById(R.id.actionBar);				
		bar.setHomeLogo(R.drawable.stampur_icon, new OnClickListener() {			
			public void onClick(View v) {
				 Intent menu_intent = new Intent(v.getContext(), Activity_MainMenu.class);
		         startActivityForResult(menu_intent, 0);
			}
		});				
	}
		
	public void sortBy(View v) {				
		TextView sortOption;
		SpannableString optionText;
		RelativeLayout sortBar = (RelativeLayout) v.getParent();
		int numChildren = sortBar.getChildCount();
		for(int i = 0; i < numChildren; i++){
			sortOption = (TextView) sortBar.getChildAt(i);			
			optionText = new SpannableString(sortOption.getText().toString());
			sortOption.setText(optionText);
		}	
				
		optionText = new SpannableString(((TextView) v).getText().toString());
		optionText.setSpan(new UnderlineSpan(), 0, optionText.length(), 0);	
		((TextView) v).setText(optionText);							
	}
	
	public void goToMenu(View v) {
		Intent menu_intent = new Intent(v.getContext(), Activity_MainMenu.class);
        startActivity(menu_intent);
        this.finish();
	}
	
	public void setupTab(final View tabContent, final String tabTitle) {		
		View tabWidgetView = LayoutInflater.from(tabHost.getContext()).inflate(R.layout.item_tabwidget, null);
		((TextView) tabWidgetView.findViewById(R.id.tabsText)).setText(tabTitle);		
		TabSpec setContent = tabHost.newTabSpec(tabTitle).setIndicator(tabWidgetView).setContent(new TabContentFactory(){
			public View createTabContent(String text){return tabContent;}
		});
		tabHost.getTabWidget().setDividerDrawable(R.drawable.shape_tabdivider);
		tabHost.addTab(setContent);
	}

    public boolean isNetworkAvailable() {
        NetworkManager.init(this);
        if (!NetworkManager.isNetworkAvailable()) {
            showDialog(NONETWORK);
            return false;
        }

        return true;
    }

    public Dialog onCreateDialog(int id, Bundle args) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        switch (id) {
            case NONETWORK:
                builder.setTitle(R.string.NoNetworkAvailable).setMessage(
                        R.string.NoNetworkTryAgain).setCancelable(false).setNegativeButton(
                        getString(R.string.NetworkDialogOk), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                break;
        }

        AlertDialog alert = builder.create();
        return alert;
    }
    
    /* LEGACY
    public void sortBy(View v) {
		SpannableString optionText;
		
		TextView old_Sort = (TextView)this.findViewById(currentSortingOption);
		TextView new_Sort = (TextView) v;
		
		optionText = new SpannableString(old_Sort.getText().toString());
		old_Sort.setText(optionText);
		
		optionText = new SpannableString(new_Sort.getText().toString());
		optionText.setSpan(new UnderlineSpan(), 0, optionText.length(), 0);	
		new_Sort.setText(optionText);
		
		currentSortingOption = new_Sort.getId();					
	}
	*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        int groupId = 0;
        int menuItemOrder = Menu.NONE;
        int menuItemText = R.string.Home;
        MenuItem home = menu.add(groupId, Menu.FIRST, menuItemOrder, menuItemText);
        home.setIcon(R.drawable.gear);

        menuItemText = R.string.Refresh;
        MenuItem refresh = menu.add(groupId, 2, menuItemOrder, menuItemText);
        refresh.setIcon(R.drawable.gear);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == 1) {
            goToMenu(this.getCurrentFocus());
            return true;
        } else if (item.getItemId() == 2) {
            //TODO: DO Something!!
            return true;
        }

        return false;
    }

}
