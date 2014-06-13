package pt.ua.travis.ui.signup;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.dropbox.chooser.android.DbxChooser;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;
import com.kbeanie.imagechooser.api.ChooserType;
import com.kbeanie.imagechooser.api.ChosenImage;
import com.kbeanie.imagechooser.api.ImageChooserListener;
import com.kbeanie.imagechooser.api.ImageChooserManager;
import org.apache.commons.io.IOUtils;
import pt.ua.travis.R;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Dialog that shows multiple service logos to pick a photo, including "Take a Picture"
 * and "Pick from the gallery".
 *
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class PhotoPickerDialog extends SherlockDialogFragment implements ImageChooserListener {

    private static final String TAG = PhotoPickerDialog.class.getSimpleName();

    public static final int GDRIVE_REQUEST = 3;

    public static final int DBX_CHOOSER_REQUEST = 5;

    public static final int FB_CHOOSER_REQUEST = 99;

    public interface OnPhotoPickedListener {

//        /**
//         * An action that should occur when the user picks an image from the camera,
//         * the gallery or any of the social APIs, and the value returned is an URI.
//         *
//         * @param imageUri the uri of the picked image
//         */
//        void onUriReturned(Uri imageUri);

        /**
         * An action that should occur when the user picks an image from the camera,
         * the gallery or any of the social APIs, and the value returned is a byte array.
         *
         * @param array the data of the picked image
         */
        void onByteArrayReturned(byte[] array);
    }

    private SherlockFragmentActivity parentActivity;

    private OnPhotoPickedListener listener;

    private ImageChooserManager imageChooserManager;

    private static AtomicReference<GoogleApiClient> googleApiReference = new AtomicReference<GoogleApiClient>();

    private boolean hideButtons;


    public static PhotoPickerDialog newInstance(SherlockFragmentActivity parentActivity,
                                                OnPhotoPickedListener listener) {
        PhotoPickerDialog p = new PhotoPickerDialog();
        p.parentActivity = parentActivity;
        p.listener = listener;
        return p;
    }

    public static void setGoogleApiClient(GoogleApiClient client) {
        googleApiReference.set(client);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {

            if(hideButtons){
                LinearLayout pickerProgress = (LinearLayout) getView().findViewById(R.id.picker_progress);
                LinearLayout pickerIcons = (LinearLayout) getView().findViewById(R.id.picker_icons);
                pickerProgress.setVisibility(View.VISIBLE);
                pickerIcons.setVisibility(View.GONE);
            }

            if (requestCode == GDRIVE_REQUEST) {
                String encodedDriveId = data.getStringExtra("result");
                DriveId pickedDriveId = DriveId.decodeFromString(encodedDriveId);

                final GoogleApiClient apiClient = googleApiReference.get();
                final DriveFile pickedFile = Drive.DriveApi.getFile(apiClient, pickedDriveId);

                new PhotoRetrieveTask() {
                    @Override
                    protected byte[] doInBackground(Void... params) {
                        DriveApi.ContentsResult result = pickedFile
                                .openContents(apiClient, DriveFile.MODE_READ_ONLY, null)
                                .await();
                        apiClient.disconnect();

                        if (result.getStatus().isSuccess()) {
                            InputStream in = result.getContents().getInputStream();
                            try {
                                return IOUtils.toByteArray(in);
                            } catch (IOException ex) {
                                Log.e(TAG, "Error while reading the picked Google Drive photo.", ex);
                            }
                        }
                        return null;
                    }
                }.execute();


            } else if (requestCode == ChooserType.REQUEST_PICK_PICTURE ||
                    requestCode == ChooserType.REQUEST_CAPTURE_PICTURE) {
                imageChooserManager.submit(requestCode, data);


            } else if (requestCode == DBX_CHOOSER_REQUEST) {
                DbxChooser.Result result = new DbxChooser.Result(data);
                returnByteArrayFromURL(result.getLink().toString());


            }
//            else if (requestCode == FB_CHOOSER_REQUEST) {
//                if (data == null) return;
//                String photoUrl = data.getStringExtra(FBPhotoPickerActivity.PHOTO_URL);
//                if (photoUrl != null) {
//                    returnByteArrayFromURL(photoUrl);
//                }
//
//
//            }
        }
    }


    @Override
    public void onImageChosen(ChosenImage chosenImage) {
        returnByteArrayFromStorage("file://" + chosenImage.getFilePathOriginal());
    }


    @Override
    public void onError(String s) {
        Log.e(TAG, s);
    }


    private void returnByteArrayFromStorage(final String uriString) {
        Log.e(TAG, "Retrieving data from file: " + uriString);

        new PhotoRetrieveTask() {
            @Override
            protected byte[] doInBackground(Void... params) {
                try {
                    InputStream input = parentActivity.getContentResolver().openInputStream(Uri.parse(uriString));
                    return IOUtils.toByteArray(input);
                } catch (IOException ex) {
                    Log.e(TAG, "Error while reading the picked photo.", ex);
                }
                return null;
            }
        }.execute();
    }

    public void returnByteArrayFromURL(final String urlString) {
        Log.e(TAG, "Retrieving data from url: " + urlString);

        new PhotoRetrieveTask(){
            @Override
            protected byte[] doInBackground(Void... params) {
                try {
                    URL url = new URL(urlString);

                    //create the new connection
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                    //set up the connection
                    urlConnection.setRequestMethod("GET");
//                    urlConnection.setDoOutput(true);
                    urlConnection.connect();

                    //this will be used in reading the data from the internet
                    InputStream inputStream = urlConnection.getInputStream();
                    return IOUtils.toByteArray(inputStream);

                } catch (IOException ex) {
                    Log.e(TAG, "Error picking image", ex);
                }
                return null;
            }

        }.execute();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_photo_picker, null);

        // configures the button click listeners, where done button runs the
        // implemented interface specified in the constructor
        ImageButton cameraButton = (ImageButton) v.findViewById(R.id.picker_camera);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    imageChooserManager = new ImageChooserManager(parentActivity, ChooserType.REQUEST_CAPTURE_PICTURE);
                    imageChooserManager.setImageChooserListener(PhotoPickerDialog.this);
                    imageChooserManager.choose();
                } catch (Exception ex) {
                    Log.e(TAG, "Error picking image", ex);
                }
            }
        });

        ImageButton galleryButton = (ImageButton) v.findViewById(R.id.picker_gallery);
        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    imageChooserManager = new ImageChooserManager(parentActivity, ChooserType.REQUEST_PICK_PICTURE);
                    imageChooserManager.setImageChooserListener(PhotoPickerDialog.this);
                    imageChooserManager.choose();
                } catch (Exception ex) {
                    Log.e(TAG, "Error picking image", ex);
                }
            }
        });

        ImageButton dropboxButton = (ImageButton) v.findViewById(R.id.picker_dropbox);
        dropboxButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DbxChooser dbxChooser = new DbxChooser("35i01bve2n2nprv");
                dbxChooser.forResultType(DbxChooser.ResultType.PREVIEW_LINK)
                        .launch(parentActivity, DBX_CHOOSER_REQUEST);
            }
        });

        ImageButton gdriveButton = (ImageButton) v.findViewById(R.id.picker_gdrive);
        gdriveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(parentActivity, GoogleDrivePicker.class);
                parentActivity.startActivityForResult(intent, GDRIVE_REQUEST);
            }
        });

        return v;
    }

    private abstract class PhotoRetrieveTask extends AsyncTask<Void, Void, byte[]>{

        @Override
        protected final void onPostExecute(byte[] data) {
            super.onPostExecute(data);
            listener.onByteArrayReturned(data);
        }
    }
}
