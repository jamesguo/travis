package pt.ua.travis.ui.login;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockActivity;
import com.andreabaccega.widget.FormEditText;
import pt.ua.travis.R;
import pt.ua.travis.backend.*;
import pt.ua.travis.filepicker.FilePicker;
import pt.ua.travis.filepicker.FilePickerAPI;
import pt.ua.travis.filepicker.InkService;
import pt.ua.travis.ui.main.MainClientActivity;
import pt.ua.travis.ui.main.MainTaxiActivity;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class SignUpLastStepsActivity extends SherlockActivity {

    private static final String TAG = SignUpLastStepsActivity.class.getSimpleName();

    private ImageView photoHolder;
//    private AtomicReference<Bitmap> selectedBitmap;
    private Uri selectedUri;
//    private AccountType accountType;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getSupportActionBar().hide();

        photoHolder = (ImageView) findViewById(R.id.photo_holder);
//        selectedBitmap = new AtomicReference<Bitmap>();
    }

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode, Intent data) {
        if (requestCode == FilePickerAPI.REQUEST_CODE_GETFILE) {
            if (resultCode != RESULT_OK)
                //Result was cancelled by the user or there was an error
                return;
            selectedUri = data.getData();
            Log.e(TAG, "----------File path is " + selectedUri.toString());
            Log.e(TAG, "----------Ink file URL: " + data.getExtras().getString("fpurl"));
        }
    }

    public void onPickImageButtonClicked(View view){
        Intent intent = new Intent(this, FilePicker.class);
        intent.putExtra("services", new String[]{
                InkService.CAMERA,
                InkService.GALLERY,
                InkService.FACEBOOK,
                InkService.INSTAGRAM,
                InkService.DROPBOX,
                InkService.GDRIVE
        });
        startActivityForResult(intent, FilePickerAPI.REQUEST_CODE_GETFILE);
    }

    public void register(View view) {
        FormEditText firstName = (FormEditText) findViewById(R.id.firstname_field);
        FormEditText lastName = (FormEditText) findViewById(R.id.lastname_field);
        RadioGroup rg = (RadioGroup) findViewById(R.id.account_type_rg);

        FormEditText[] allFields = {firstName, lastName,};

        boolean allValid = true;
        for (FormEditText field : allFields) {
            allValid = field.testValidity() && allValid;
        }


        int checkedId = rg.getCheckedRadioButtonId();
        if (checkedId == -1 || selectedUri == null) {
            Toast.makeText(this,
                    getResources().getString(R.string.type_invalid),
                    Toast.LENGTH_SHORT)
                    .show();
            allValid = false;
        }

        if (allValid) {
            String type = ((RadioButton) findViewById(checkedId)).getText().toString();

            User u = null;
            if (type.equals(getResources().getString(R.string.taxi))) {
                u = Taxi.create();
            } else if (type.equals(getResources().getString(R.string.client))) {
                u = Client.create();
            }
            u.setName(firstName.getText().toString() + lastName.getText().toString());

            if (u instanceof Client) {
                PersistenceManager.save((Client) u, new Callback<Client>() {
                    @Override
                    public void onResult(Client result) {
                        continueRegister(result);
                    }
                });
            } else if (u instanceof Taxi) {
                PersistenceManager.save((Taxi) u, new Callback<Taxi>() {
                    @Override
                    public void onResult(Taxi result) {
                        continueRegister(result);
                    }
                });
            }
        }
    }


    private void continueRegister(final User savedUser){

        PersistenceManager.storeImage(this, savedUser.id(), selectedUri, new Callback<String>() {
            @Override
            public void onResult(String result) {
                Log.e("######################", result);
                savedUser.setImageUri(result);
                Log.e("######################", "5");
                final String e = "a@a.a";
                final String p = "aaa";
                savedUser.setEmail(e);
                savedUser.setPassword(p);

                if (savedUser instanceof Client) {
                    PersistenceManager.save((Client) savedUser, new Callback<Client>() {
                        @Override
                        public void onResult(Client result) {
                            Log.e("######################", "6");
                            PersistenceManager.attemptLogin(e, p);
                            Intent intent = new Intent(SignUpLastStepsActivity.this, MainClientActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });
                } else if (savedUser instanceof Taxi) {
                    PersistenceManager.save((Taxi) savedUser, new Callback<Taxi>() {
                        @Override
                        public void onResult(Taxi result) {
                            continueRegister(result);
                            Intent intent = new Intent(SignUpLastStepsActivity.this, MainTaxiActivity.class);
                            startActivity(intent);
//                            finish();
                        }
                    });
                }
            }
        });


    }
}