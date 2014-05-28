package pt.ua.travis.ui.login;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.actionbarsherlock.app.SherlockActivity;
import com.andreabaccega.widget.FormEditText;
import com.chute.android.photopickerplus.util.intent.PhotoPickerPlusIntentWrapper;
import com.chute.sdk.v2.model.AssetModel;
import com.chute.sdk.v2.model.enums.AccountType;
import com.squareup.picasso.Picasso;
import pt.ua.travis.R;
import pt.ua.travis.backend.*;
import pt.ua.travis.ui.main.MainClientActivity;
import pt.ua.travis.ui.main.MainTaxiActivity;
import pt.ua.travis.utils.Validate;

import java.util.ArrayList;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class SignUpLastStepsActivity extends SherlockActivity {

    public static final String KEY_SELECTED_ITEMS = "keySelectedItems";

    private ImageView photoHolder;
    private ArrayList<AssetModel> selectedMediaList;
    private Uri selectedImageUri;
    private AccountType accountType;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getSupportActionBar().hide();

        photoHolder = (ImageView) findViewById(R.id.photo_holder);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        PhotoPickerPlusIntentWrapper wrapper = new PhotoPickerPlusIntentWrapper(data);
        if(Validate.argExists(wrapper.getIntent(), PhotoPickerPlusIntentWrapper.KEY_PHOTO_COLLECTION)){

            selectedMediaList = wrapper.getMediaCollection();
            if (accountType != null) {
                accountType = wrapper.getAccountType();
            }
            if(!selectedMediaList.isEmpty()){
                AssetModel selectedAsset = selectedMediaList.get(0);
                selectedImageUri = Uri.parse(selectedAsset.getUrl());
                Picasso.with(this)
                        .load(selectedImageUri)
                        .resize(140, 180)
                        .centerCrop()
                        .into(photoHolder);

                Log.e("--------------", selectedImageUri.toString());
            }
        }
    }


    public void onPickImageButtonClicked(View view){
        PhotoPickerPlusIntentWrapper wrapper = new PhotoPickerPlusIntentWrapper(this);
        wrapper.startActivityForResult(this);
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
        if (checkedId == -1 || selectedImageUri == null) {
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
                u = new Taxi();
            } else if (type.equals(getResources().getString(R.string.client))) {
                u = new Client();
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

        PersistenceManager.storeImage(this, savedUser.id(), selectedImageUri, new Callback<String>() {
            @Override
            public void onResult(String result) {
                savedUser.setImageUri(result);

                if (savedUser instanceof Client) {
                    PersistenceManager.save((Client) savedUser, new Callback<Client>() {
                        @Override
                        public void onResult(Client result) {
                            Intent intent = new Intent(SignUpLastStepsActivity.this, MainClientActivity.class);
                            startActivity(intent);
//                            finish();
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