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
import pt.ua.travis.core.Client;
import pt.ua.travis.core.Taxi;
import pt.ua.travis.core.User;
import pt.ua.travis.db.PersistenceManager;
import pt.ua.travis.utils.Tools;


/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class RegisterActivity extends SherlockActivity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);
        getSupportActionBar().hide();
    }


    public void register(View view) {
        FormEditText firstName = (FormEditText) findViewById(R.id.firstname_field);
        FormEditText lastName = (FormEditText) findViewById(R.id.lastname_field);
        FormEditText userName = (FormEditText) findViewById(R.id.usernameR_field);
        FormEditText photoUrl = (FormEditText) findViewById(R.id.photo_field);
        FormEditText password = (FormEditText) findViewById(R.id.passR_field);
        RadioGroup rg = (RadioGroup) findViewById(R.id.account_type_rg);

        FormEditText[] allFields    = { firstName, lastName, userName, photoUrl, password };

        boolean allValid = true;
        for (FormEditText field: allFields) {
            allValid = field.testValidity() && allValid;
        }


        int checkedId = rg.getCheckedRadioButtonId();
        if(checkedId == -1)
            allValid = false;

        if (allValid) {
            String taxi = getResources().getString(R.string.taxi);
            String client = getResources().getString(R.string.client);
            String type = ((RadioButton) findViewById(checkedId)).getText().toString();


            String pass = password.getText().toString();
            pass = Tools.passwordToDigestSHA1(pass);

            User u = null;
            if(type.equals(taxi)){
                u = new Taxi(
                        userName.getText().toString(),
                        pass,
                        firstName.getText().toString()+lastName.getText().toString(),
                        photoUrl.getText().toString());
            } else if(type.equals(client)){
                u = new Client(
                        userName.getText().toString(),
                        pass,
                        firstName.getText().toString()+lastName.getText().toString(),
                        photoUrl.getText().toString());
            }

            PersistenceManager.addUser(u);
            Toast.makeText(this, getResources().getString(R.string.register_ok), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this,LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }
}