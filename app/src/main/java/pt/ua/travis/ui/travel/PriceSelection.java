package pt.ua.travis.ui.travel;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import org.json.JSONException;
import pt.ua.travis.R;

import java.math.BigDecimal;


public class PriceSelection extends PaymentActivity {

    private static final String TAG = "paymentExample";
    private static final int REQUEST_CODE_PAYMENT = 1;
    private static final String CURRENCY = "EUR";
    String p;
    EditText preco;
    Button button;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.priceselection);

         preco = (EditText) findViewById(R.id.editText);
         button = (Button) findViewById(R.id.button);
    }

    public void onResume() {
        super.onResume();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (preco.getText().toString().equals("")) {
                    Toast.makeText(PriceSelection.this,"Insira o preço da viagem!",Toast.LENGTH_SHORT).show();
                }
                 else {
                    p = preco.getText().toString();
                    BigDecimal value = new BigDecimal(p);
                    PayPalPayment tour = new PayPalPayment(value , CURRENCY, "Preço Viagem", PayPalPayment.PAYMENT_INTENT_AUTHORIZE);

                    String aux = tour.getAmountAsLocalizedString();
                    Log.d("CODE",aux);
                    Intent intent = new Intent(PriceSelection.this, com.paypal.android.sdk.payments.PaymentActivity.class);
                    intent.putExtra(com.paypal.android.sdk.payments.PaymentActivity.EXTRA_PAYMENT, tour);
                    startActivityForResult(intent, REQUEST_CODE_PAYMENT);
                }
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PAYMENT) {
            if (resultCode == Activity.RESULT_OK) {
                PaymentConfirmation confirm =
                        data.getParcelableExtra(com.paypal.android.sdk.payments.PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirm != null) {
                    try {
                        Log.i(TAG, confirm.toJSONObject().toString(4));
                        Log.i(TAG, confirm.getPayment().toJSONObject().toString(4));
                        Log.i("LLL", confirm.getPayment().toJSONObject().toString());
                        /**
                         *  TODO: send 'confirm' (and possibly confirm.getPayment() to your server for verification
                         * or consent completion.
                         * See https://developer.paypal.com/webapps/developer/docs/integration/mobile/verify-mobile-payment/
                         * for more details.
                         *
                         * For sample mobile backend interactions, see
                         * https://github.com/paypal/rest-api-sdk-python/tree/master/samples/mobile_backend
                         */
                        Toast.makeText(
                                getApplicationContext(),
                                "Pagamento recebido com sucesso!", Toast.LENGTH_LONG)
                                .show();

                        Intent main = new Intent(this,PaymentActivity.class); //Activity executada após efectuar o pagamento
                        startActivity(main);
                    } catch (JSONException e) {
                        Log.e(TAG, "an extremely unlikely failure occurred: ", e);
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.i(TAG, "The user canceled.");
            } else if (resultCode == com.paypal.android.sdk.payments.PaymentActivity.RESULT_EXTRAS_INVALID) {
                Log.i(
                        TAG,
                        "An invalid Payment or PayPalConfiguration was submitted. Please see the docs.");
            }
        }
    }
}