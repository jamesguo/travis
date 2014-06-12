package pt.ua.travis.ui.travel;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.TextView;
import android.widget.Toast;
import pt.ua.travis.R;
import pt.ua.travis.backend.*;
import pt.ua.travis.core.TravisFragment;
import pt.ua.travis.ui.main.MainActivity;

import java.nio.charset.Charset;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class AuthenticationFragment extends TravisFragment implements NfcAdapter.CreateNdefMessageCallback {

    public interface OnAuthenticationCompleteListener {
        void onAuthenticationComplete(boolean valid);
    }

    private NfcAdapter mNfcAdapter;
    private MainActivity parentActivity;

    private Ride rideToAuthenticate;
    private User currentUser;
    private OnAuthenticationCompleteListener listener;

    public static AuthenticationFragment newInstance(Ride rideToAuthenticate, User currentUser, OnAuthenticationCompleteListener listener) {
        AuthenticationFragment f = new AuthenticationFragment();
        f.rideToAuthenticate = rideToAuthenticate;
        f.currentUser = currentUser;
        f.listener = listener;
        return f;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.parentActivity = (MainActivity) activity;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setContentView(R.layout.fragment_authentication);

        TextView textView = (TextView) parentActivity.findViewById(R.id.authenticate_text_view);

        if(currentUser instanceof Client) {
            textView.setText(R.string.authenticate_for_client);
        } else if (currentUser instanceof Taxi) {
            textView.setText(R.string.authenticate_for_taxi);
        }

        // Check for available NFC Adapter
        mNfcAdapter = NfcAdapter.getDefaultAdapter(parentActivity);
        if (mNfcAdapter == null) {
            Toast.makeText(parentActivity, "NFC is not available", Toast.LENGTH_LONG).show();
            return;
        }

        // Register callback
        mNfcAdapter.setNdefPushMessageCallback(this, parentActivity);

        setContentShown(true);
    }


    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        String text = currentUser.id();
        return new NdefMessage(
                new NdefRecord[]{createMimeRecord(
                        "application/com.example.android.beam", text.getBytes())
                        /**
                         * The Android Application Record (AAR) is commented out. When a device
                         * receives a push with an AAR in it, the application specified in the AAR
                         * is guaranteed to run. The AAR overrides the tag dispatch system.
                         * You can add it back in to guarantee that this
                         * activity starts when receiving a beamed message. For now, this code
                         * uses the tag dispatch system.
                         */
                        //,NdefRecord.createApplicationRecord("com.example.android.beam")
                }
        );
    }


    @Override
    public void onResume() {
        super.onResume();
        // Check to see that the Activity started due to an Android Beam
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(parentActivity.getIntent().getAction())) {
            boolean valid = processIntent(parentActivity.getIntent());
            listener.onAuthenticationComplete(valid);
        }
    }

    /**
     * Parses the NDEF Message from the intent and prints to the TextView
     */
    private boolean processIntent(Intent intent) {
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(
                NfcAdapter.EXTRA_NDEF_MESSAGES);
        // only one message sent during the beam
        NdefMessage msg = (NdefMessage) rawMsgs[0];
        // record 0 contains the MIME type, record 1 is the AAR, if present
        String otherID = new String(msg.getRecords()[0].getPayload());
        if(currentUser instanceof Client) {
            return rideToAuthenticate.taxi().id().equalsIgnoreCase(otherID);
        } else if (currentUser instanceof Taxi) {
            return rideToAuthenticate.client().id().equalsIgnoreCase(otherID);
        }
        return false;
    }


    /**
     * Creates a custom MIME type encapsulated in an NDEF record
     *
     * @param mimeType
     */
    public NdefRecord createMimeRecord(String mimeType, byte[] payload) {
        byte[] mimeBytes = mimeType.getBytes(Charset.forName("US-ASCII"));
        NdefRecord mimeRecord = new NdefRecord(
                NdefRecord.TNF_MIME_MEDIA, mimeBytes, new byte[0], payload);
        return mimeRecord;
    }
}