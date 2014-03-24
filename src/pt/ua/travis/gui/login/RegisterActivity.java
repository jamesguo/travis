package pt.ua.travis.gui.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;
import pt.ua.travis.R;
import pt.ua.travis.core.Account;
import pt.ua.travis.core.Taxi;
import pt.ua.travis.db.PersistenceManager;


/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class RegisterActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_layout);
    }


    public void sendregist(View view) {
        EditText nome = (EditText) findViewById(R.id.editnome);
        EditText pass = (EditText) findViewById(R.id.editpass);
        EditText imagem = (EditText) findViewById(R.id.editimagem);
        EditText username = (EditText) findViewById(R.id.editusername);
        RadioButton rd = (RadioButton) findViewById(R.id.radioButtontaxi);


        Taxi t = new Taxi(nome.getText().toString(),imagem.getText().toString());
        Account a = new Account(t, username.getText().toString(), pass.getText().toString());
        PersistenceManager.accounts.add(a);
        Toast.makeText(this,"Registo Efectuado!",Toast.LENGTH_SHORT);
        Intent intent = new Intent(this,LoginActivity.class);
        intent.putExtra("radio",rd.isChecked());
        startActivity(intent);
    }
}