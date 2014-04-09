package pt.ua.travis.gui.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockActivity;
import com.andreabaccega.widget.FormEditText;
import pt.ua.travis.R;
import pt.ua.travis.backend.entities.Client;
import pt.ua.travis.backend.entities.Taxi;
import pt.ua.travis.backend.entities.User;
import pt.ua.travis.utils.Utils;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class SignUpLastStepsActivity extends SherlockActivity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);
        getSupportActionBar().hide();
    }

    public void register(View view) {
        FormEditText firstName = (FormEditText) findViewById(R.id.firstname_field);
        FormEditText lastName = (FormEditText) findViewById(R.id.lastname_field);
        FormEditText email = (FormEditText) findViewById(R.id.usernameR_field);
        FormEditText photoUrl = (FormEditText) findViewById(R.id.photo_field);
        FormEditText password = (FormEditText) findViewById(R.id.passR_field);
        RadioGroup rg = (RadioGroup) findViewById(R.id.account_type_rg);

        FormEditText[] allFields = { firstName, lastName, email, photoUrl, password };

        boolean allValid = true;
        for (FormEditText field: allFields) {
            allValid = field.testValidity() && allValid;
        }


        int checkedId = rg.getCheckedRadioButtonId();
        if(checkedId == -1) {
            Toast.makeText(this,
                    getResources().getString(R.string.type_invalid),
                    Toast.LENGTH_SHORT)
                    .show();
            allValid = false;
        }

        if (allValid) {
            String type = ((RadioButton) findViewById(checkedId)).getText().toString();
            String pass = password.getText().toString();
            pass = Utils.generateSHA1DigestFromString(pass);

            User u = null;
            if(type.equals(getResources().getString(R.string.taxi))){
                u = new Taxi();
            } else if(type.equals(getResources().getString(R.string.client))){
                u = new Client();
            }
            u.setEmail(email.getText().toString());
            u.setPasswordDigest(pass);
            u.setName(firstName.getText().toString() + lastName.getText().toString());
            u.setImageUri(photoUrl.getText().toString());
            u.save();

            Toast.makeText(this,
                    getResources().getString(R.string.register_ok),
                    Toast.LENGTH_SHORT)
                    .show();

            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }
}