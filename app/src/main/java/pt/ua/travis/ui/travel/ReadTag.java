package pt.ua.travis.ui.travel;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PaymentActivity;
import pt.ua.travis.R;

import java.math.BigDecimal;

public class ReadTag extends Activity {

    private static final String TAG = ReadTag.class.getSimpleName();
    private static final int REQUEST_CODE_PAYMENT = 1;
    private static final String CURRENCY = "EUR";
    // NFC-related variables
    private NfcAdapter nfcAdapter;
    private PendingIntent nfcPendingIntent;
    IntentFilter[] readTagFilters;

    private TextView textViewData;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (nfcAdapter == null) {
            Toast.makeText(this, "Your device does not support NFC. Cannot run this demo.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        checkNfcEnabled();

        nfcPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        IntentFilter ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            ndefDetected.addDataType("application/travis.nfc");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("Could not add MIME type.", e);
        }
        Toast.makeText(this, "NFC onCreate - ReadTagActivity", Toast.LENGTH_LONG).show();
        readTagFilters = new IntentFilter[]{ndefDetected};
    }


    @Override
    protected void onResume() {
        super.onResume();

        checkNfcEnabled();

        if (getIntent().getAction() != null) {
            if (getIntent().getAction().equals(NfcAdapter.ACTION_NDEF_DISCOVERED)) {
                NdefMessage[] msgs = getNdefMessagesFromIntent(getIntent());
                NdefRecord record = msgs[0].getRecords()[0];
                byte[] payload = record.getPayload();

                //Aqui e onde ele detecta a TAG e inicia a actividade que queremos!
                String payloadString = new String(payload);
                BigDecimal value = new BigDecimal(payloadString);
                String travelCostString = getString(R.string.travel_cost);
                PayPalPayment tour = new PayPalPayment(value , CURRENCY, travelCostString, PayPalPayment.PAYMENT_INTENT_AUTHORIZE);
                Intent intent = new Intent(this, PaymentActivity.class);
                intent.putExtra(PaymentActivity.EXTRA_PAYMENT, tour);

                startActivityForResult(intent, REQUEST_CODE_PAYMENT);
            }
        }
        Toast.makeText(this, "NFC onResume - ReadTagActivity", Toast.LENGTH_LONG).show();
        nfcAdapter.enableForegroundDispatch(this, nfcPendingIntent, readTagFilters, null);
    }


    @Override
    protected void onPause() {
        super.onPause();

        Toast.makeText(this, "NFC onPause - ReadTagActivity", Toast.LENGTH_LONG).show();
        nfcAdapter.disableForegroundDispatch(this);
    }


    @Override
    protected void onNewIntent(Intent intent) {
        Toast.makeText(this, "NFC onNewIntent - ReadTagActivity", Toast.LENGTH_LONG).show();
        if (intent.getAction().equals(NfcAdapter.ACTION_NDEF_DISCOVERED)) {
            NdefMessage[] msgs = getNdefMessagesFromIntent(intent);
            confirmDisplayedContentOverwrite(msgs[0]);

        } else if (intent.getAction().equals(NfcAdapter.ACTION_TAG_DISCOVERED)) {
            Toast.makeText(this, "This NFC tag has no NDEF data.", Toast.LENGTH_LONG).show();
        }
    }


    private NdefMessage[] getNdefMessagesFromIntent(Intent intent) {
        // Parse the intent
        NdefMessage[] msgs = null;
        String action = intent.getAction();
        Toast.makeText(this, "NFC getNdefMessagesFromIntent - ReadTagActivity", Toast.LENGTH_LONG).show();

        if (action.equals(NfcAdapter.ACTION_TAG_DISCOVERED) || action.equals(NfcAdapter.ACTION_NDEF_DISCOVERED)) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }

            } else {
                // Unknown tag type
                byte[] empty = new byte[]{};
                NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, empty, empty);
                NdefMessage msg = new NdefMessage(new NdefRecord[]{record});
                msgs = new NdefMessage[]{msg};
            }

        } else {
            Log.e(TAG, "Unknown intent.");
            finish();
        }
        return msgs;
    }


    private void confirmDisplayedContentOverwrite(final NdefMessage msg) {
        final String data = textViewData.getText().toString().trim();
        Toast.makeText(this, "NFC confirmDisplayedContentOverwrite - ReadTagActivity", Toast.LENGTH_LONG).show();

        new AlertDialog.Builder(this).setTitle("New tag found!").setMessage("Do you wanna show the content of this tag?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int id)
                    {
                        String payload = new String(msg.getRecords()[0].getPayload());

                        textViewData.setText(new String(payload));
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int id)
            {
                textViewData.setText(data);
                dialog.cancel();
            }
        }).show();
    }


    private void checkNfcEnabled() {
        Toast.makeText(this, "NFC checkNfcEnabled - ReadTagActivity", Toast.LENGTH_LONG).show();
        Boolean nfcEnabled = nfcAdapter.isEnabled();
        if (!nfcEnabled) {
            new AlertDialog.Builder(ReadTag.this).setTitle(getString(R.string.text_warning_nfc_is_off))
                    .setMessage(getString(R.string.text_turn_on_nfc)).setCancelable(false)
                    .setPositiveButton(getString(R.string.text_update_settings), new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int id)
                        {
                            startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
                        }
                    }).create().show();
        }
    }
}