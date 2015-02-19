package com.stampur.android.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.stampur.android.R;
import com.stampur.android.Stamp;


public class Activity_CreateStamp extends Activity_Base {
	
	private final String LOGTAG = "CREATESTAMP";

    private EditText stampLabel;
    private EditText stampDescription;
    
    private final String DIALOG_MESSAGE = "DIALOGMESSAGE";
    private final int DISCARDMSG = 1000;
    private final int MSGTITLEEMPTY = 1001;
    private final int MSGBODYEMPTY = 1002;
    private final int MSGSTAMPSEMPTY = 1003;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);        
		setContentView(R.layout.layout_createstamp);

        stampLabel = (EditText) findViewById(R.id.createstamp_editLabel);
        stampDescription = (EditText) findViewById(R.id.createstamp_editDescription);      
       
		bar.setTitle("Create Stamp");		
		bar.addActionIcon(R.drawable.back, 
				new OnClickListener(){ public void onClick(View v){Activity_CreateStamp.this.finish();}});
		bar.addActionIcon(R.drawable.post, new OnClickListener(){ public void onClick(View v){ postStamp(); }});
	}

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
	public void onStop(){
		super.onStop();
	}

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //TODO: Cleanup to save memory
    }
    
    public void postStamp(){
        String label = stampLabel.getText().toString();
        String descr = stampDescription.getText().toString();
        Bundle bundle = new Bundle();

        if(null == label || "".equals(label)){
            bundle.putString(DIALOG_MESSAGE, getString(R.string.MessageTitleEmpty));
            showDialog(MSGTITLEEMPTY, bundle);
            return;
        }else if(null == descr || "".equals(descr)){
            bundle.putString(DIALOG_MESSAGE, getString(R.string.MessageBodyEmpty));
            showDialog(MSGBODYEMPTY, bundle);
            return;
        }else{
        	@SuppressWarnings("unused")
			Stamp stamp = new Stamp("", label, descr);                     
            
            //TODO: Do stuff to actually create the stamp!
            //MainMenu_Activity.IS_service.put_message(message_attributes);

            //Toast toast = Toast.makeText(getApplicationContext(), R.string.StampPosted, Toast.LENGTH_SHORT);
            //toast.show();
            this.finish();
        }
	}	

    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {
        super.onCreateOptionsMenu( menu );

        int groupId = 0;
        int menuItemOrder = Menu.NONE;        
        MenuItem settings = menu.add( groupId, Menu.FIRST, menuItemOrder, R.string.Post);
        settings.setIcon( R.drawable.gear );

        menuItemOrder = Menu.NONE;        
        MenuItem addstamps = menu.add( groupId, 2, menuItemOrder, R.string.AddStamps);
        addstamps.setIcon( R.drawable.gear );

        menuItemOrder = Menu.NONE;      ;
        MenuItem addPhotos = menu.add( groupId, 3, menuItemOrder, R.string.AddPhotos);
        addPhotos.setIcon( R.drawable.gear );

        menuItemOrder = Menu.NONE;       
        MenuItem cancel = menu.add( groupId, 4, menuItemOrder, R.string.Discard);
        cancel.setIcon( R.drawable.gear );

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case 1:                
                return true;
            case 2:            	
                return true;
            case 3:                
                break;
            case 4: //Discard stamp and return after confirming with a Yes/No dialog
                discardStamp();
                return true;
            default:
                Log.i(LOGTAG, "WARNING: Unknown Options Menu Item Selected");
                break;
        }
        return false;
    }   

    public Dialog onCreateDialog(int id, Bundle args) {
        String message = (String) args.get(DIALOG_MESSAGE);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        switch (id) {
            case MSGBODYEMPTY:
            case MSGTITLEEMPTY:
            case MSGSTAMPSEMPTY:
                builder.setMessage(message).setCancelable(true).setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) { dialog.cancel(); }
                        });
                break;

            case DISCARDMSG:
                builder.setMessage(message).setCancelable(true).setPositiveButton("YES",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id){ Activity_CreateStamp.this.finish(); }
                        }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id){ dialog.cancel(); }
                        });
                break;
        }

        AlertDialog alert = builder.create();
        return alert;
    }

    private void discardStamp() {
        Bundle bundle = new Bundle();
        bundle.putString(DIALOG_MESSAGE, getString(R.string.DiscardConfirm));
        showDialog(DISCARDMSG, bundle);
    }

    @Override
    public void onBackPressed() {
        discardStamp();
    }
}
