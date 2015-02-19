package com.stampur.android.ui;

import android.os.Bundle;
import android.widget.ListView;

import com.stampur.android.R;

public class Activity_ListMessages extends Activity_Base {

    private ListView messagesList;
    private Adapter_Message adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.listmessages_layout);

        messagesList = (ListView) findViewById(R.id.messagesList);
        //adapter = new MessageAdapter(null, null, null, null);
        messagesList.setAdapter(adapter);
    }

}