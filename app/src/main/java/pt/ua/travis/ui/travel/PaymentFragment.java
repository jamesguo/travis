package pt.ua.travis.ui.travel;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
//import com.doomonafireball.betterpickers.numberpicker.NumberPicker;
//import com.doomonafireball.betterpickers.numberpicker.NumberPickerBuilder;
//import com.doomonafireball.betterpickers.numberpicker.NumberPickerDialogFragment;
import com.paypal.android.sdk.payments.*;
import org.json.JSONException;
import pt.ua.travis.R;
import pt.ua.travis.core.TravisFragment;
import pt.ua.travis.ui.main.MainActivity;

import java.nio.charset.Charset;


public class PaymentFragment extends TravisFragment implements NfcAdapter.CreateNdefMessageCallback
//        , NumberPickerDialogFragment.NumberPickerDialogHandler
{
    /**
     * - Set to PaymentActivity.ENVIRONMENT_PRODUCTION to move real money.
     *
     * - Set to PaymentActivity.ENVIRONMENT_SANDBOX to use your test credentials
     * from https://developer.paypal.com
     *
     * - Set to PayPalConfiguration.ENVIRONMENT_NO_NETWORK to kick the tires
     * without communicating to PayPal's servers.
     */
    private static final String CONFIG_ENVIRONMENT = PayPalConfiguration.ENVIRONMENT_NO_NETWORK;

    // note that these credentials will differ between live & sandbox environments.
    private static final String CONFIG_CLIENT_ID = "Adu1bhBf840z-2wroBCoLcm_kSagSg3lD8uw4QhVw71YqfjnzG-xJRSl45Yj";

    private static final int REQUEST_CODE_FUTURE_PAYMENT = 2;

    private static PayPalConfiguration config = new PayPalConfiguration()
            .environment(CONFIG_ENVIRONMENT)
            .clientId(CONFIG_CLIENT_ID)
                    // The following are only used in PayPalFuturePaymentActivity.
            .merchantName("Travis")
            .merchantPrivacyPolicyUri(Uri.parse("https://www.example.com/privacy"))
            .merchantUserAgreementUri(Uri.parse("https://www.example.com/legal"));

    private MainActivity parentActivity;

    private static final String TAG = "paymentExample";
    private static final int REQUEST_CODE_PAYMENT = 1;
    TextView priceInput;
    NfcAdapter nfcAdapter;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.parentActivity = (MainActivity) activity;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setContentView(R.layout.fragment_payment);

        // start paypal service
        Intent intent = new Intent(parentActivity, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        parentActivity.startService(intent);

        priceInput = (TextView) parentActivity.findViewById(R.id.price_input);

        final Button pricePicker = (Button) parentActivity.findViewById(R.id.price_picker);
        priceInput.setText("--");
        pricePicker.setText("Set Number");
        pricePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                NumberPickerBuilder npb = new NumberPickerBuilder()
//                        .setFragmentManager(getChildFragmentManager())
//                        .setStyleResId(R.style.BetterPickersDialogFragment_Light);
//                npb.show();
            }
        });
        nfcAdapter = NfcAdapter.getDefaultAdapter(parentActivity);

        if (nfcAdapter == null) {
            Toast.makeText(parentActivity, "NFC is not available", Toast.LENGTH_LONG).show();
            return;
        }
        Log.d("NFC","onCreate - Beam Activity");

        nfcAdapter.setNdefPushMessageCallback(this, parentActivity);

        setContentShown(true);
    }

//    public void pickmePressed(View pressed) {
//        setContentView(R.layout.activity_main);
//
//        Intent intent = new Intent(parentActivity, PayPalService.class);
//        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
//        parentActivity.startService(intent);
//    }
//
//    public void onBuyPressed(View pressed) {
//        // PAYMENT_INTENT_SALE will cause the payment to complete immediately.
//        // Change PAYMENT_INTENT_SALE to PAYMENT_INTENT_AUTHORIZE to only authorize payment and
//        // capture funds later.
//
//        Intent intent = new Intent(parentActivity, PaymentFragment.class);
//        startActivity(intent);
//    }

    private void sendAuthorizationToServer(PayPalAuthorization authorization) {
        authorization.getAuthorizationCode();
    }

    @Override
    public void onDestroy() {
        // Stop service when done
        parentActivity.stopService(new Intent(parentActivity, PayPalService.class));
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PAYMENT) {
            if (resultCode == Activity.RESULT_OK) {
                PaymentConfirmation confirm =
                        data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirm != null) {
                    try {
                        Log.i(TAG, confirm.toJSONObject().toString(4));
                        Log.i(TAG, confirm.getPayment().toJSONObject().toString(4));
                        String paymentAmount = confirm.getPayment().getAmountAsLocalizedString();
                        // TODO GET PAYMENT

                        Toast.makeText(parentActivity,
                                "PaymentConfirmation info received from PayPal", Toast.LENGTH_LONG)
                                .show();

                    } catch (JSONException e) {
                        Log.e(TAG, "an extremely unlikely failure occurred: ", e);
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.i(TAG, "The user canceled.");
            } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
                Log.i(TAG, "An invalid Payment or PayPalConfiguration was submitted. Please see the docs.");
            }
        }
    }

    @Override
    public NdefMessage createNdefMessage(NfcEvent nfcEvent) {
        NdefMessage message = null;

        if (priceInput.getText().equals("--")) {
            Toast.makeText(parentActivity, getString(R.string.insert_price), Toast.LENGTH_SHORT).show();
        }

        else {

            String data = priceInput.getText().toString().trim();
            Log.d("NFC", "createNdefMessage - Beam Activity");

            String mimeType = "application/com.elsinga.sample.nfc";

            byte[] mimeBytes = mimeType.getBytes(Charset.forName("UTF-8"));
            byte[] dataBytes = data.getBytes(Charset.forName("UTF-8"));
            byte[] id = new byte[0];

            NdefRecord record = new NdefRecord(NdefRecord.TNF_MIME_MEDIA, mimeBytes, id, dataBytes);

            message  = new NdefMessage(new NdefRecord[]{record});

        }
        return message;
    }

//    @Override
//    public void onDialogNumberSet(int reference, int number, double decimal, boolean isNegative, double fullNumber) {
//        priceInput.setText(fullNumber+"€");
//    }
}