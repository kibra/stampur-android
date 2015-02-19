package com.stampur.android.ui;

import java.io.FileOutputStream;
import java.util.UUID;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.stampur.android.R;
import com.stampur.android.service.StampurService;

public class Activity_Settings extends Activity_Base {
	
	private static StampurService stampurService;

    private TextView stampLabel;
    private ImageView profilePicture;
    private Button changePicture;
    private TextView stampDescription;
    private Button editDescription;
    private Button updatePassword;
    private EditText currentPassword;
    private EditText newPassword;
    private EditText confirmPassword;

    private final int REQ_PICKIMAGE = 101;
    private final int REQ_CAPTUREIMAGE = 102;
    private final String DIALOG_MESSAGE = "DIALOGMESSAGE";
    private final int CHOOSEPICSOURCE = 1001;
    private final int INVALIDCURRENTPASSWORD = 1002;
    private final int INVALIDNEWPASSWORD = 1003;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState); 	
		setContentView(R.layout.layout_settings);

        stampLabel = (TextView) findViewById(R.id.settings_stampLabel);
        profilePicture = (ImageView) findViewById(R.id.settings_profilePicture);
        changePicture = (Button) findViewById(R.id.settings_chooseProfilePictureButton);
        changePicture.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                choosePictureSource();
            }
        });

        stampDescription = (TextView) findViewById(R.id.settings_stampDescription);
        editDescription = (Button) findViewById(R.id.settings_editDescriptionButton);
        editDescription.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                updateStampDescription();
            }
        });

        updatePassword = (Button) findViewById(R.id.settings_updatePasswordButton);
        updatePassword.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                updatePassword();
            }
        });

        currentPassword = (EditText) findViewById(R.id.settings_oldPass);
        newPassword = (EditText) findViewById(R.id.settings_newPass);
        newPassword.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0) {
                    updatePassword.setEnabled(true);
                } else {
                    updatePassword.setEnabled(false);
                }
            }
        });

        confirmPassword = (EditText) findViewById(R.id.settings_newPassConfirm);

		bar.setTitle(getString(R.string.SettingsTitle));
		bar.addActionIcon(R.drawable.post, new OnClickListener(){ public void onClick(View v) {finish();} });
        bindService(new Intent(this, StampurService.class), serviceConnection, Context.BIND_AUTO_CREATE);

        stampLabel.requestFocus();
	}
	
	@Override
	protected void onDestroy() {
	    super.onDestroy();
		unbindService(serviceConnection);
	}
	
	private ServiceConnection serviceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder serviceBinder) {
        	stampurService = ((StampurService.StampurService_Binder)serviceBinder).getService();
            SharedPreferences preferences =
                    Activity_Settings.this.getSharedPreferences(StampurService.STAMPUR_PREFS, 0);
            stampLabel.setText(
                    preferences.getString(StampurService.STAMP_LABEL, ""));
            stampDescription.setText(
                    preferences.getString(StampurService.STAMP_DESC, ""));
        }

        public void onServiceDisconnected(ComponentName arg0) {
        }
    };

    public void choosePictureSource() {
        Bundle bundle = new Bundle();
        bundle.putString(DIALOG_MESSAGE, getString(R.string.SettingsPickImageSource));
        showDialog(CHOOSEPICSOURCE, bundle);
    }

    private void takePictureAndAttach() {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        startActivityForResult(
                Intent.createChooser(intent, getString(R.string.SettingsSelectPicture)),
                REQ_CAPTUREIMAGE);
    }

    private void selectPicturesToAttach() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);
        startActivityForResult(
                Intent.createChooser(intent, getString(R.string.SettingsSelectPicture)),
                REQ_PICKIMAGE);
    }

    public Dialog onCreateDialog(int id, Bundle args) {
        String message = (String) args.get(DIALOG_MESSAGE);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        switch (id) {
            case CHOOSEPICSOURCE:
                builder.setTitle(message).setCancelable(true).setItems(R.array.ChoosePicSource,
                        new ImageSourceDialogListener());
                break;

            case INVALIDCURRENTPASSWORD:
            case INVALIDNEWPASSWORD:
                builder.setTitle(message).setCancelable(true).setNegativeButton(
                        getString(R.string.SettingsDialogCancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });
                break;
        }

        AlertDialog alert = builder.create();
        return alert;
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

    private void changeProfilePicture(Uri uri) {
        if (uri != null) {
            ContentResolver contentResolver = getContentResolver();
            Cursor cursor = contentResolver.query(uri,
                    new String[] { android.provider.MediaStore.Images.ImageColumns.DATA }, null,
                    null, null);
            cursor.moveToFirst();
            String imagePath = cursor.getString(0);
            cursor.close();

            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            changeProfilePicture(bitmap);
        }
    }

    public void changeProfilePicture(Intent intent) {
        Bundle extras = intent.getExtras();

        if ((extras != null) && (extras.getParcelable("data") != null)) {
            try {
                Bitmap bitmap = (Bitmap) extras.getParcelable("data");
                changeProfilePicture(bitmap);
                return;
            } catch (Exception e) {
                Log.e(this.getClass().getName(), e.toString());
            }
        }
    }

    private void changeProfilePicture(Bitmap bitmap) {
        if (bitmap != null) {
            final String fileName = storeImage(bitmap);
            profilePicture.setImageBitmap(bitmap);

            Thread t = new Thread(new Runnable() {
                public void run() {
                    String serverFileName = stampurService.uploadPictureToServer(fileName);
                    stampurService.changeProfilePicture(serverFileName);
                }
            });
            t.start();
        }
    }

    private String storeImage(Bitmap bitmap) {
        String image = UUID.randomUUID().toString() + ".jpg";

        try {
            FileOutputStream fos = openFileOutput(image, Context.MODE_PRIVATE);
            bitmap.compress(CompressFormat.JPEG, 80, fos);
            fos.close();
        } catch (Exception exception) {

        }
        return image;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case REQ_PICKIMAGE:
                if (data == null) {
                    return;
                }

                Uri uri = data.getData();
                if (null != uri) {
                    changeProfilePicture(uri);
                }
                break;

            case REQ_CAPTUREIMAGE:
                if (data == null) {
                    return;
                }

                changeProfilePicture(data);
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public void updatePassword() {
        final String currentPass = currentPassword.getText().toString();
        final String newPass = newPassword.getText().toString();
        final String confirmPass = confirmPassword.getText().toString();

        /*
        if("".equals(currentPass) || !currentPass.equals(stampurService.getCurrentPassword())) {
            Bundle bundle = new Bundle();
            bundle.putString(DIALOG_MESSAGE, getString(R.string.WrongCurrentPassword));
            showDialog(INVALIDCURRENTPASSWORD, bundle);
            return;
        } else */

        if (!newPass.equals(confirmPass)) {
            Bundle bundle = new Bundle();
            bundle.putString(DIALOG_MESSAGE, getString(R.string.WrongConfirmPassword));
            showDialog(INVALIDNEWPASSWORD, bundle);
            return;
        } else {
            Thread t = new Thread(new Runnable() {
                public void run() {
                    stampurService.updatePassword(newPass, currentPass);
                }
            });

            t.start();
            currentPassword.setText("");
            newPassword.setText("");
            confirmPassword.setText("");
            Toast toast = Toast.makeText(this, getString(R.string.PasswordUpdatedToast), Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
            toast.show();
        }
    }

    public void updateStampDescription() {
        final String oldDesc = stampDescription.getText().toString();

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.editstampdescription);
        dialog.setTitle(getString(R.string.UpdateStampDescription));
        dialog.setCancelable(true);

        final EditText descText = (EditText) dialog.findViewById(R.id.stampDescText);
        descText.setText(oldDesc);
        final Button updateDescButton = (Button) dialog.findViewById(R.id.updateDescButton);
        final Button cancelUpdateButton = (Button) dialog.findViewById(R.id.cancelUpdateButton);

        descText.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            public void afterTextChanged(Editable editable) {
                if(editable.length() > 0 && !oldDesc.equals(editable.toString())) {
                    updateDescButton.setEnabled(true);
                } else {
                    updateDescButton.setEnabled(false);
                }
            }
        });

        updateDescButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                final String newDescr = descText.getText().toString();

                Thread t = new Thread(new Runnable() {
                    public void run() {
                        stampurService.updateStampDescription(newDescr);
                    }
                });

                t.start();
                stampDescription.setText(newDescr);
                dialog.dismiss();
            }
        });

        cancelUpdateButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                dialog.cancel();
            }
        });
        dialog.show();
    }
}
