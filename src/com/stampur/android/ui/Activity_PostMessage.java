package com.stampur.android.ui;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.UUID;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Thumbnails;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.stampur.android.R;
import com.stampur.android.Message;
import com.stampur.android.Stamp;
import com.stampur.android.service.StampurService;

public class Activity_PostMessage extends Activity_Base {

    private final String LOGTAG = "POSTMESSAGE";

    private final int REQ_ADDSTAMP = 100;
    private final int REQ_PICKIMAGE = 101;
    private final int REQ_CAPTUREIMAGE = 102;

    private EditText messageTitle;
    private EditText messageBody;

    private Message message;

    private ArrayList<Stamp> addedStamps;
    private ArrayList<String> addedPictures;

    private final String DIALOG_MESSAGE = "DIALOGMESSAGE";
    private final int DISCARDMSG = 1000;
    private final int MSGTITLEEMPTY = 1001;
    private final int MSGBODYEMPTY = 1002;
    private final int MSGSTAMPSEMPTY = 1003;
    private final int CHOOSEPICSOURCE = 1004;

    private Intent intent;

    private LinearLayout picsLayout;
    private LinearLayout stampsLayout;

    private int selectedPic;
    private Button placeholderStamps;
    private Button placeholderPics;

    private Resources resources;

    private StampurService stampurService;
    private ServiceConnection serviceConnection;

    private int parentMenus = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_layout);

        resources = getResources();
        messageTitle = (EditText) findViewById(R.id.post_messageTitle);
        messageBody = (EditText) findViewById(R.id.post_messageBody);
        picsLayout = (LinearLayout) findViewById(R.id.post_picsLayout);
        stampsLayout = (LinearLayout) findViewById(R.id.post_stampsLayout);
        placeholderStamps = (Button) findViewById(R.id.placeholderStampsButton);
        placeholderPics = (Button) findViewById(R.id.placeholderPicsButton);
        registerForContextMenu(placeholderPics);

        message = new Message();
        addedStamps = new ArrayList<Stamp>();
        addedPictures = new ArrayList<String>();

        bar.setTitle(getString(R.string.PostMessage));
        bar.addActionIcon(R.drawable.back, new OnClickListener() {
            public void onClick(View v) {Activity_PostMessage.this.finish();}
        });
        bar.addActionIcon(R.drawable.post, new OnClickListener() {
            public void onClick(View v) {postMessage();}
        });


        intent = getIntent();
        String action = intent.getAction();
        //Intent is SEND event (sending an image to Stampur app)
        if (Intent.ACTION_SEND.equals(action)) {
            picsLayout.removeView(placeholderPics);
            attachPictureFromIntent(intent);
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action)) {
            picsLayout.removeView(placeholderPics);
            attachMultiplePicturesFromIntent(intent);
        }

        if (isNetworkAvailable()) {
            initService();
            bindService(new Intent(this, StampurService.class), serviceConnection,
                    BIND_AUTO_CREATE);
        }
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
    public void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
        //TODO: Cleanup to save memory
    }

    private void initService() {
        serviceConnection = new ServiceConnection() {

            public void onServiceConnected(ComponentName className, IBinder serviceBinder) {
                stampurService =
                        ((StampurService.StampurService_Binder) serviceBinder).getService();
            }

            public void onServiceDisconnected(ComponentName arg0) {
            }
        };
    }

    public void attachPictureFromIntent(Intent intent) {
        Bundle extras = intent.getExtras();

        if (extras.containsKey(Intent.EXTRA_STREAM)) {
            try {
                Uri uri = (Uri) extras.getParcelable(Intent.EXTRA_STREAM);
                attachPicture(uri);
                return;
            } catch (Exception e) {
                Log.e(this.getClass().getName(), e.toString());
            }
        } else if (extras.containsKey(Intent.EXTRA_TEXT)) {
            return;
        }
    }

    public void attachMultiplePicturesFromIntent(Intent intent) {
        Bundle extras = intent.getExtras();

        if (extras.containsKey(Intent.EXTRA_STREAM)) {
            try {

                ArrayList<Parcelable> pictures = extras.getParcelableArrayList(Intent.EXTRA_STREAM);

                for (Parcelable parcelable : pictures) {
                    Uri uri = (Uri) parcelable;
                    attachPicture(uri);
                }

                return;
            } catch (Exception e) {
                Log.e(this.getClass().getName(), e.toString());
            }
        } else if (extras.containsKey(Intent.EXTRA_TEXT)) {
            return;
        }
    }

    public void postMessage() {
        if (isNetworkAvailable()) {
            String title = messageTitle.getText().toString();
            String body = messageBody.getText().toString();
            Bundle bundle = new Bundle();

            if (null == title || "".equals(title)) {
                bundle.putString(DIALOG_MESSAGE, getString(R.string.MessageTitleEmpty));
                showDialog(MSGTITLEEMPTY, bundle);
                return;
            } else if (null == body || "".equals(body)) {
                bundle.putString(DIALOG_MESSAGE, getString(R.string.MessageBodyEmpty));
                showDialog(MSGBODYEMPTY, bundle);
                return;
            } else if (addedStamps.isEmpty()) {
                bundle.putString(DIALOG_MESSAGE, getString(R.string.MessageStampsEmpty));
                showDialog(MSGSTAMPSEMPTY, bundle);
                return;
            } else {
                message.setTitle(title);
                message.setBody(body);
                message.setStamps(addedStamps);

                if (!addedPictures.isEmpty()) {
                    message.setPictures(addedPictures);
                }

                stampurService.postMessage(message);

                Toast toast = Toast.makeText(getApplicationContext(), R.string.MessagePosted,
                        Toast.LENGTH_SHORT);
                toast.show();

                setResult(RESULT_OK, intent);
                this.finish();
            }
        }
    }

    public void goToStampMessage(View v) {
        goToStampMessage();
    }

    private void goToStampMessage() {
        if (isNetworkAvailable()) {
            stampMessage();
        }
    }

    public void stampMessage() {
        Intent intent = new Intent(this, Activity_StampMessage.class);
        intent.putExtra("Origin", LOGTAG);
        intent.putParcelableArrayListExtra(getString(R.string.AddedStamps), addedStamps);
        startActivityForResult(intent, REQ_ADDSTAMP);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        parentMenus = menu.size();

        int groupId = 0;
        int menuItemOrder = Menu.NONE;
        int menuItemText = R.string.Post;
        MenuItem settings = menu.add(groupId, parentMenus + 1, menuItemOrder, menuItemText);
        settings.setIcon(R.drawable.gear);

        menuItemText = R.string.AddStamps;
        MenuItem addstamps = menu.add(groupId, parentMenus + 2, menuItemOrder, menuItemText);
        addstamps.setIcon(R.drawable.gear);

        menuItemText = R.string.AddPhotos;
        MenuItem addPhotos = menu.add(groupId, parentMenus + 3, menuItemOrder, menuItemText);
        addPhotos.setIcon(R.drawable.gear);

        menuItemText = R.string.Discard;
        MenuItem cancel = menu.add(groupId, parentMenus + 4, menuItemOrder, menuItemText);
        cancel.setIcon(R.drawable.gear);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() ==  1) {
            discardMessage();
            return true;
        } else if (item.getItemId() == parentMenus + 1) {
            postMessage();
            return true;
        } else if (item.getItemId() == parentMenus + 2) {
            goToStampMessage();
            return true;
        } else if (item.getItemId() == parentMenus + 3) {
            //Capture a Photo or Select Photo(s) from Gallery
            choosePictureSource();
            return true;
        } else if (item.getItemId() == parentMenus + 4) {
            //Discard Message and Return to Main Menu
            //After confirming with a Yes/No dialog
            discardMessage();
            return true;
        } else {
            Log.i(LOGTAG, "WARNING: Unknown Options Menu Item Selected");
        }

        return false;
    }

    /*
      * onActivityResult() is called when another Activity/Application returns with requested data
      * Handle the incoming data based on the "requestCode" you used while sending the request.
      *
      * @param: requestCode - integer request code that you used for the request
      * @param: resultCode - integer response code that is returned
      * @param: data - the Intent object that contains the data you requested
      *                (or null if nothing is returned)
      *
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case REQ_ADDSTAMP:
                stampsLayout.removeAllViews();
                addedStamps = data.getParcelableArrayListExtra(getString(R.string.AddedStamps));

                if (addedStamps.size() > 0) {
                    Toast stampToast = Toast.makeText(getApplicationContext(), R.string.StampsAdded,
                            Toast.LENGTH_SHORT);
                    stampToast.show();
                } else {
                    stampsLayout.addView(placeholderStamps);
                    return;
                }

                for (int i = 0; i < addedStamps.size(); i++) {
                    TextView stamp = (TextView) View.inflate(this, R.layout.stampitem, null);
                    stamp.setText(addedStamps.get(i).getLabel());
                    stamp.setTag(addedStamps.get(i));

                    stamp.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) { goToStampMessage(); }
                    });

                    stampsLayout.addView(stamp, i);
                }
                break;

            case REQ_PICKIMAGE:
                if (data == null) {
                    if (picsLayout.getChildCount() == 0) {
                        picsLayout.addView(placeholderPics);
                    }
                    return;
                }

                Uri uri = data.getData();
                if (null != uri) {
                    picsLayout.removeView(placeholderPics);
                    Toast picToast = Toast.makeText(getApplicationContext(), R.string.PicsAdded,
                            Toast.LENGTH_SHORT);
                    picToast.show();
                    attachPicture(uri);
                }
                break;

            case REQ_CAPTUREIMAGE:
                if (data == null) {
                    if (picsLayout.getChildCount() == 0) {
                        picsLayout.addView(placeholderPics);
                    }
                    return;
                }

                if (data.getExtras() != null && data.getExtras().getParcelable("data") != null) {
                    picsLayout.removeView(placeholderPics);
                    Bitmap bitmap = data.getExtras().getParcelable("data");
                    attachPicture(bitmap);
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public void choosePictureSource(View view) {
        choosePictureSource();
    }

    public void choosePictureSource() {
        Bundle bundle = new Bundle();
        bundle.putString(DIALOG_MESSAGE, getString(R.string.PostPickImageSource));
        showDialog(CHOOSEPICSOURCE, bundle);
    }

    private void selectPicturesToAttach() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.PostSelectPicture)),
                REQ_PICKIMAGE);
    }

    private void takePictureAndAttach() {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        startActivityForResult(Intent.createChooser(intent, getString(R.string.PostSelectPicture)),
                REQ_CAPTUREIMAGE);
    }

    private String storeImage(Bitmap bitmap) {
        String image = UUID.randomUUID().toString() + ".jpg";

        try {
            FileOutputStream fos = openFileOutput(image, Context.MODE_PRIVATE);
            bitmap.compress(CompressFormat.JPEG, 80, fos);
            fos.close();
            addedPictures.add(image);
        } catch (Exception exception) {

        }
        return image;
    }

    private void attachPicture(Bitmap bitmap) {
        if (bitmap != null) {
            String imageName = storeImage(bitmap);

            Bitmap compressed = Bitmap.createScaledBitmap(bitmap,
                    resources.getInteger(R.integer.PicThumbnailWidth),
                    resources.getInteger(R.integer.PicThumbnailHeight), false);

            ImageView image = (ImageView) View.inflate(this, R.layout.pictureitem, null);
            image.setTag(imageName);
            image.setImageBitmap(compressed);
            registerForContextMenu(image);
            picsLayout.addView(image);
        }
    }

    private void attachPicture(Uri uri) {
        if (uri != null) {
            String uriString = uri.toString();
            ContentResolver contentResolver = getContentResolver();
            Cursor cursor = contentResolver.query(uri,
                    new String[] { android.provider.MediaStore.Images.ImageColumns.DATA }, null,
                    null, null);
            cursor.moveToFirst();
            String imagePath = cursor.getString(0);
            cursor.close();

            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            String imageName = storeImage(bitmap);

            long imageId = getImageIdFromUri(uriString);
            Bitmap compressed = MediaStore.Images.Thumbnails.getThumbnail(contentResolver, imageId,
                    Thumbnails.MICRO_KIND, null);

            ImageView image = (ImageView) View.inflate(this, R.layout.pictureitem, null);
            image.setTag(imageName);
            image.setImageBitmap(compressed);
            registerForContextMenu(image);
            picsLayout.addView(image);
        }
    }

    private long getImageIdFromUri(String uriString) {
        String id = uriString.substring(uriString.lastIndexOf('/') + 1);
        return Long.valueOf(id);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();

        if (v instanceof ImageView) {
            selectedPic = addedPictures.indexOf(v.getTag());
            inflater.inflate(R.menu.picturescontext, menu);

            if (addedPictures.size() <= 1) {
                menu.getItem(1).setVisible(false);
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.removePic:
                removePicFromMessage(selectedPic);
                return true;
            case R.id.removeAllPics:
                removeAllPicsFromMessage();
                return true;
            case R.id.attachMorePics:
                choosePictureSource();
                return true;
            case R.id.cancelPicMenu:
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void removePicFromMessage(int position) {
        addedPictures.remove(position);
        picsLayout.removeViewAt(position);

        if (picsLayout.getChildCount() == 0) {
            picsLayout.addView(placeholderPics);
        }
    }

    private void removeAllPicsFromMessage() {
        addedPictures.clear();
        picsLayout.removeAllViews();
        picsLayout.addView(placeholderPics);
    }

    public Dialog onCreateDialog(int id, Bundle args) {
        String message = (String) args.get(DIALOG_MESSAGE);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        switch (id) {
            case MSGBODYEMPTY:
            case MSGTITLEEMPTY:
            case MSGSTAMPSEMPTY:
                builder.setMessage(message).setCancelable(true).setPositiveButton(
                        getString(R.string.Okay), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
                break;

            case DISCARDMSG:
                builder.setMessage(message).setCancelable(true).setPositiveButton(
                        getString(R.string.Discard), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        setResult(RESULT_CANCELED, intent);
                        Activity_PostMessage.this.finish();
                    }
                }).setNegativeButton(getString(R.string.Cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                break;

            case CHOOSEPICSOURCE:
                builder.setTitle(message).setCancelable(true).setItems(R.array.ChoosePicSource,
                        new ImageSourceDialogListener());
                break;
        }

        AlertDialog alert = builder.create();
        return alert;
    }

    private void discardMessage() {
        Bundle bundle = new Bundle();
        bundle.putString(DIALOG_MESSAGE, getString(R.string.DiscardConfirm));
        showDialog(DISCARDMSG, bundle);
    }

    @Override
    public void onBackPressed() {
        discardMessage();
    }

    private class ImageSourceDialogListener implements DialogInterface.OnClickListener {

        public void onClick(DialogInterface dialog, int itemId) {
            switch (itemId) {
                case 0:
                    takePictureAndAttach();
                    return;
                case 1:
                    selectPicturesToAttach();
                    return;
            }
        }
    }
}

