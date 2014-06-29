package pt.ua.travis.ui.travel;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockDialogFragment;
import com.beardedhen.androidbootstrap.BootstrapEditText;
import com.paypal.android.sdk.payments.PayPalAuthorization;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalService;
import pt.ua.travis.R;
import pt.ua.travis.ui.main.MainActivity;

import java.nio.charset.Charset;


public class PaymentDialog
        extends SherlockDialogFragment
        implements NfcAdapter.CreateNdefMessageCallback {

    public interface OnPaymentCompleteListener {
        void onPaymentComplete(boolean success);
    }

    private static final String CONFIG_ENVIRONMENT = PayPalConfiguration.ENVIRONMENT_NO_NETWORK;

    // note that these credentials will differ between live & sandbox environments.
    private static final String CONFIG_CLIENT_ID = "Adu1bhBf840z-2wroBCoLcm_kSagSg3lD8uw4QhVw71YqfjnzG-xJRSl45Yj";

    private static final int REQUEST_CODE_FUTURE_PAYMENT = 2;

    private static PayPalConfiguration config = new PayPalConfiguration()
            .environment(CONFIG_ENVIRONMENT)
            .clientId(CONFIG_CLIENT_ID)
            .merchantName("Travis");

    private MainActivity parentActivity;
    private BootstrapEditText priceInput;
    private NfcAdapter nfcAdapter;

    private OnPaymentCompleteListener listener;

    public static PaymentDialog newInstance(OnPaymentCompleteListener listener) {
        PaymentDialog d = new PaymentDialog();
        d.listener = listener;
        return d;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.parentActivity = (MainActivity) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_payment, null);

        TextView btnWrite2 = (TextView) v.findViewById(R.id.skip_payment_button);
        btnWrite2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                listener.onPaymentComplete(true);
            }
        });

        Intent intent = new Intent(parentActivity, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        parentActivity.startService(intent);

        priceInput = (BootstrapEditText) v.findViewById(R.id.price_form);

        nfcAdapter = NfcAdapter.getDefaultAdapter(parentActivity);

        if (nfcAdapter == null) {
            Toast.makeText(parentActivity, "NFC is not available", Toast.LENGTH_LONG).show();
        }

        nfcAdapter.setNdefPushMessageCallback(this, parentActivity);

        return v;
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
        dismiss();
        listener.onPaymentComplete(true);
        super.onDestroy();
    }

    @Override
    public NdefMessage createNdefMessage(NfcEvent nfcEvent) {
        String data = priceInput.getText().toString().trim();
        String mimeType = "application/travis.nfc";

        byte[] mimeBytes = mimeType.getBytes(Charset.forName("UTF-8"));
        byte[] dataBytes = data.getBytes(Charset.forName("UTF-8"));
        byte[] id = new byte[0];

        NdefRecord record = new NdefRecord(NdefRecord.TNF_MIME_MEDIA, mimeBytes, id, dataBytes);

        return new NdefMessage(new NdefRecord[]{record});
    }
}