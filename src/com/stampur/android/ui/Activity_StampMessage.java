package com.stampur.android.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.stampur.android.R;
import com.stampur.android.Stamp;
import com.stampur.android.Stamp.Category;
import com.stampur.android.service.StampurService;
import com.stampur.android.util.StampByNameComparator;

public class Activity_StampMessage extends Activity_Base {

    /* Grid & Adapter for Selected Stamps */
    private GridView addedStampsGrid;
    private StampAdapter addedStampsAdapter;

    /* Grid & Adapter for Available Stamps */
    private GridView availableStampsGrid;
    private StampAdapter availableStampsAdapter;

    /* Spinner to pick te type of Stamps */
    private Spinner pickStampType;
    private ArrayAdapter<String> pickStampAdapter;

    /* Search Stamps */
    private RelativeLayout searchLayout;
    private EditText searchBar;
    private ImageButton searchIcon;

    private Intent intent;

    private ArrayList<Stamp> beforeSearchList;
    private String selectedCategory;

    private Resources resources;
    private StampurService stampurService;
    private ServiceConnection serviceConnection;

    private ProgressDialog progressDialog;

    private ArrayList<Stamp> categoryStamps;

    // Need handler for callbacks to the UI thread
    private final Handler handler = new Handler();

    private final String DIALOG_MESSAGE = "DIALOGMESSAGE";
    private final int MAXSTAMPSREACHED = 1001;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.stampmessage_layout);
        resources = getResources();

        //Initialize Search Bar & Button
        searchLayout = (RelativeLayout) findViewById(R.id.searchStampsLayout);
        searchBar = (EditText) findViewById(R.id.searchBar);
        searchBar.setOnEditorActionListener(new SearchEditorAction());
        searchIcon = (ImageButton) findViewById(R.id.searchIcon);
        searchIcon.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                hideVirtualKeyboard();
                searchStamps(searchBar.getText().toString());
            }
        });

        //Spinner to pick the type of Stamp
        pickStampType = (Spinner) findViewById(R.id.pickStampType);

        //Add Action Bar       
        bar.setTitle(getString(R.string.StampMessage));
        //TODO: This would be changed to "Done" later
        bar.addActionIcon(R.drawable.back, new OnClickListener() {
            public void onClick(View v) {
                goBackToPostMessage();
            }
        });
        bar.addActionIcon(R.drawable.search_button, new OnClickListener() {
            public void onClick(View v) {
                if (View.GONE == searchLayout.getVisibility()) {
                    searchLayout.setVisibility(View.VISIBLE);
                } else {
                    hideVirtualKeyboard();
                    searchLayout.setVisibility(View.GONE);
                    resetSearch();
                }
            }
        });

        if (isNetworkAvailable()) {
            initService();
            bindService(new Intent(this, StampurService.class), serviceConnection,
                    BIND_AUTO_CREATE);
        }
    }
    
    @Override
    public void onStop() {
        super.onStop();
    }
    
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindService(serviceConnection);
	}
	
	@Override
    public void onBackPressed() {
		goBackToPostMessage();
		super.onBackPressed();
    }

    private void initService() {
        serviceConnection = new ServiceConnection() {

            public void onServiceConnected(ComponentName className, IBinder serviceBinder) {
                stampurService =
                        ((StampurService.StampurService_Binder) serviceBinder).getService();
                //Initialize both the Selected Stamps & Available Stamps Grids
                initStampGrids();

                //Initialize the Stamps Type Spinner
                initStampTypes();
                //Get the Stamps that have already been selected from the PostMessageActivity
                intent = getIntent();
                Bundle bundle = intent.getExtras();

                if (bundle != null && bundle.containsKey("Origin") &&
                        "POSTMESSAGE".equals(bundle.get("Origin"))) {
                    ArrayList<Stamp> addedStamps =
                            intent.getParcelableArrayListExtra(getString(R.string.AddedStamps));
                    addedStampsAdapter.setStamps(addedStamps);

                    for (Stamp addedStamp : addedStamps) {
                        availableStampsAdapter.remove(addedStamp);
                    }

                    beforeSearchList = new ArrayList<Stamp>(availableStampsAdapter.getStamps());
                    intent.removeExtra("Origin");
                }
            }

            public void onServiceDisconnected(ComponentName arg0) {
            }
        };
    }

    private final Runnable updateResults = new Runnable() {
        public void run() {
            showStampsOnUI();
        }
    };

    /*
      * Method to initialize the two Stamp Grids - Selected Stamps & Available Stamps
      *         -   and to set the adapters for both the Grids
      *
     */
    private void initStampGrids() {
        addedStampsGrid = (GridView) findViewById(R.id.addedStampsGrid);
        addedStampsGrid.setOnItemClickListener(new OnStampItemClickListener());
        availableStampsGrid = (GridView) findViewById(R.id.availableStampsGrid);
        availableStampsGrid.setOnItemClickListener(new OnStampItemClickListener());

        if (addedStampsAdapter == null) {
            addedStampsAdapter = new StampAdapter(this, R.layout.stampitem, R.id.stampItem,
                    new ArrayList<Stamp>());
        }

        if (availableStampsAdapter == null) {
            availableStampsAdapter = new StampAdapter(this, R.layout.stampitem, R.id.stampItem,
                    new ArrayList<Stamp>());
        }

        addedStampsGrid.setAdapter(addedStampsAdapter);
        availableStampsGrid.setAdapter(availableStampsAdapter);
    }

    /*
      * Method to initialize the Stamp Types Spinner
      *
     */
    private void initStampTypes() {
        Category[] allTypes = Category.values();
        ArrayList<String> stampTypesArray = new ArrayList<String>();
        stampTypesArray.add(getString(R.string.AllStamps));
        stampTypesArray.add(getString(R.string.UserStamps));
        for (Category category : allTypes) {
            if(!category.equals(Category.PEOPLE)) {
                stampTypesArray.add(category.toString());
            }
        }

        pickStampAdapter = new ArrayAdapter<String>(this, R.layout.stamptypeitem, stampTypesArray);
        pickStampType.setAdapter(pickStampAdapter);
        pickStampType.setOnItemSelectedListener(new OnStampTypeSelectedListener());
        selectedCategory = pickStampAdapter.getItem(0);
    }

    private void hideVirtualKeyboard() {
        View focusedView = Activity_StampMessage.this.getCurrentFocus();
        if (focusedView != null) {
            InputMethodManager m = (InputMethodManager) Activity_StampMessage.this.getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            if (m != null) {
                m.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
            }
        }
    }

    /*
      * Method to search for Stamps
      * Displays the Stamps that match the searchText in the "Available Stamps" Grid
      *
      * @param: searchText: The text string to be searched for
     */
    private void searchStamps(String searchText) {
        if (null == searchText || "".equals(searchText)) {
            resetSearch();
            return;
        }

        //Reset Search since the last search returned no Stamps
        if (availableStampsAdapter.getCount() == 0) {
            resetSearch();
        }

        //Search for Stamps
        ArrayList<Stamp> searchedStamps = new ArrayList<Stamp>();
        for (int i = 0; i < availableStampsAdapter.getCount(); i++) {
            Stamp stamp = availableStampsAdapter.getItem(i);
            String stampLabel = stamp.getLabel().toLowerCase();
            if (null != stampLabel && stampLabel.contains(searchText.toLowerCase())) {
                searchedStamps.add(stamp);
            }
        }

        availableStampsAdapter.setStamps(searchedStamps);
    }

    /*
      * Method to reset the Search Stamps
      * Resets the "Available Stamps" after a Search
      *
     */
    private void resetSearch() {
        availableStampsAdapter.setStamps(beforeSearchList);
    }

    private void loadStamps(String type) {
        if (isNetworkAvailable()) {
            if (getString(R.string.AllStamps).equalsIgnoreCase(type)) {
                categoryStamps = stampurService.getPublicStamps();
            } else if (getString(R.string.UserStamps).equalsIgnoreCase(type)) {
                categoryStamps = stampurService.getUserStampsForPostMessage();
            } else if (getString(R.string.SpecialStamps).equalsIgnoreCase(type)) {
                categoryStamps =
                        stampurService.getPublicStamps(Category.valueOf(type.toUpperCase()));
                categoryStamps.add(stampurService.getStamp(getThisUserStampId()));
            } else {
                categoryStamps =
                        stampurService.getPublicStamps(Category.valueOf(type.toUpperCase()));
            }

            categoryStamps.removeAll(addedStampsAdapter.getStamps());
            //availableStampsAdapter.setStamps(categoryStamps);
            //beforeSearchList = new ArrayList<Stamp>(availableStampsAdapter.getStamps());
        }
    }

    private void showStampsOnUI() {
        availableStampsAdapter.setStamps(categoryStamps);
        beforeSearchList = new ArrayList<Stamp>(availableStampsAdapter.getStamps());
        dismissProgressDialog();
    }

    /*
      * Method to send the list of Selected Stamps back to PostMessageActivity
      *
    */
    private void goBackToPostMessage() {
        ArrayList<Stamp> addedStamps = new ArrayList<Stamp>(addedStampsAdapter.stamps);
        intent.putParcelableArrayListExtra(getString(R.string.AddedStamps), addedStamps);
        setResult(RESULT_OK, intent);
        this.finish();
    }

    private String getThisUserStampId() {
        SharedPreferences preferences =
                getSharedPreferences(StampurService.STAMPUR_PREFS, MODE_PRIVATE);
        return preferences.getString(StampurService.STAMPID, null);
    }

    /*
      * An Adapter for Stamps
      *     - Holds a list of Stamps
      *     - Selected Stamp Grid & Available Stamp Grid have an Adapter each
      *
    */
    private class StampAdapter extends ArrayAdapter<Stamp> {

        private List<Stamp> stamps;
        private StampByNameComparator comparator;
        private LayoutInflater inflater;

        private StampAdapter(Context context, int resource, int textViewResourceId,
                List<Stamp> objects) {
            super(context, resource, textViewResourceId, objects);
            this.stamps = (objects != null) ? objects : new ArrayList<Stamp>();
            this.comparator = StampByNameComparator.getInstance();
            this.inflater =
                    (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            sortAndNotify();
        }

        public int getCount() {
            return stamps.size();
        }

        public Stamp getItem(int position) {
            return stamps.get(position);
        }

        public void setStamps(List<Stamp> stamps) {
            this.stamps = stamps;
            sortAndNotify();
        }

        public List<Stamp> getStamps() {
            return this.stamps;
        }

        @Override
        public void add(Stamp stamp) {
            stamps.add(stamp);
            sortAndNotify();
        }

        @Override
        public void remove(Stamp stamp) {
            stamps.remove(stamp);
            sortAndNotify();
        }

        private void sortAndNotify() {
            Collections.sort(stamps);
            this.sort(comparator);
            this.notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            StampViewHolder holder;

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.stampitem, null);
                holder = new StampViewHolder();
                holder.label = (TextView) convertView.findViewById(R.id.stampItem);
                convertView.setTag(holder);
            } else {
                holder = (StampViewHolder) convertView.getTag();
            }

            Stamp stamp = stamps.get(position);
            holder.label.setText(stamp.getLabel());
            return convertView;
        }

        private class StampViewHolder {
            TextView label;
        }
    }

    private class OnStampItemClickListener implements OnItemClickListener {

        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            swapStampsOnPosition(view, position);
        }
    }

    /*
      * Method called when a Stamp is clicked in either of the Grids
      * Swaps Stamps from one grid to another, depending on where it was before clicking
      *
      * @param: view - The View that as clicked
      * @param: position - position of the Stamp in the StampAdapter
      *
    */
    private void swapStampsOnPosition(View view, int position) {
        if (addedStampsGrid.equals(view.getParent())) {
            Stamp clickedStamp = addedStampsAdapter.getItem(position);
            addedStampsAdapter.remove(clickedStamp);

            if (selectedCategory.equalsIgnoreCase(clickedStamp.getCategory().toString()) ||
                    selectedCategory.equalsIgnoreCase(getString(R.string.AllStamps))) {
                availableStampsAdapter.add(clickedStamp);
            }

            if (!beforeSearchList.contains(clickedStamp) &&
                    (selectedCategory.equalsIgnoreCase(clickedStamp.getCategory().toString())) ||
                    selectedCategory.equalsIgnoreCase(getString(R.string.AllStamps))) {
                beforeSearchList.add(clickedStamp);
            }
        } else if (availableStampsGrid.equals(view.getParent())) {

            if (addedStampsAdapter.getCount() == resources.getInteger(R.integer.MaxStamps)) {
                Bundle bundle = new Bundle();
                bundle.putString(DIALOG_MESSAGE, getString(R.string.MaxStampsReached));
                showDialog(MAXSTAMPSREACHED, bundle);
                return;
            }

            Stamp clickedStamp = availableStampsAdapter.getItem(position);
            availableStampsAdapter.remove(clickedStamp);
            addedStampsAdapter.add(clickedStamp);
            beforeSearchList.remove(clickedStamp);
        }
    }

    private class SearchEditorAction implements OnEditorActionListener {

        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (!searchBar.hasFocus()) {
                return false;
            }

            switch (actionId) {
                case EditorInfo.IME_ACTION_SEARCH:
                    searchBar.clearFocus();
                    hideVirtualKeyboard();
                    searchStamps(searchBar.getText().toString());
                    return true;
                default:
                    break;
            }

            return false;
        }
    }

    private class OnStampTypeSelectedListener implements OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            showProgessDialog(getString(R.string.LoadingStampsPleaseWait));

            selectedCategory = pickStampAdapter.getItem(pos);

            Thread t = new Thread() {
                public void run() {
                    loadStamps(selectedCategory);
                    handler.post(updateResults);
                }
            };
            t.start();
        }

        public void onNothingSelected(AdapterView<?> parent) {
        }
    }

    public Dialog onCreateDialog(int id, Bundle args) {
        String message = (String) args.get(DIALOG_MESSAGE);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        switch (id) {
            case MAXSTAMPSREACHED:
                builder.setTitle(message).setCancelable(true).setPositiveButton(
                        getString(R.string.StampDialogOk), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                break;
        }

        AlertDialog alert = builder.create();
        return alert;
    }

    private void showProgessDialog(String message) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(message);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void dismissProgressDialog() {
        progressDialog.dismiss();
    }

}
