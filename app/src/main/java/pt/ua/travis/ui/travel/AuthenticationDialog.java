package pt.ua.travis.ui.travel;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.*;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockDialogFragment;
import pt.ua.travis.R;
import pt.ua.travis.backend.Client;
import pt.ua.travis.backend.Ride;
import pt.ua.travis.backend.Taxi;
import pt.ua.travis.backend.User;
import pt.ua.travis.ui.main.MainActivity;
import pt.ua.travis.ui.main.MainClientActivity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;


/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class AuthenticationDialog extends SherlockDialogFragment {

    public interface OnAuthenticationCompleteListener {
        void onAuthenticationComplete(boolean valid);
    }

    private MainActivity parentActivity;

    private NfcAdapter nfcAdapter;
    private Tag nfcTag;
    private PendingIntent mNfcPendingIntent;
    private IntentFilter[] mWriteTagFilters;
    private IntentFilter[] mNdefExchangeFilters;

    private Ride rideToAuthenticate;
    private User currentUser;
    private OnAuthenticationCompleteListener listener;

    public static AuthenticationDialog newInstance(MainActivity parentActivity,
                                                   Ride rideToAuthenticate,
                                                   User currentUser,
                                                   OnAuthenticationCompleteListener listener) {
        AuthenticationDialog f = new AuthenticationDialog();
        f.parentActivity = parentActivity;
        f.rideToAuthenticate = rideToAuthenticate;
        f.currentUser = currentUser;
        f.listener = listener;
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setCancelable(false);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Holo_Light_Dialog_NoActionBar);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_authentication, null);
        TextView textView = (TextView) v.findViewById(R.id.authenticate_text_view);
        TextView btnWrite = (TextView) v.findViewById(R.id.authenticate_button);
        TextView btnWrite2 = (TextView) v.findViewById(R.id.skip_auth_button);
        btnWrite2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onAuthenticationComplete(true);
            }
        });

        if(currentUser instanceof Client) {
            textView.setText(R.string.authenticate_for_client);
            btnWrite.setVisibility(View.GONE);
        } else if (currentUser instanceof Taxi) {
            textView.setText(R.string.authenticate_for_taxi);
            btnWrite.setVisibility(View.VISIBLE);
            btnWrite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(nfcTag!=null) {
                        write(currentUser.id(), nfcTag);
                    }
                }
            });
        }

        nfcAdapter = NfcAdapter.getDefaultAdapter(parentActivity);
        mNfcPendingIntent = PendingIntent
                .getActivity(parentActivity, 0, new Intent(parentActivity, MainClientActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
        mWriteTagFilters = new IntentFilter[] { tagDetected };

        return v;
    }

    private NdefRecord createRecord(String text) throws UnsupportedEncodingException {

        //create the message in according with the standard
        String lang = "en";
        byte[] langBytes = lang.getBytes("US-ASCII");
        byte[] textBytes = text.getBytes();
        int langLength = langBytes.length;
        int textLength = textBytes.length;

        byte[] payload = new byte[1 + langLength + textLength];
        payload[0] = (byte) langLength;

        // copy lang bytes and text bytes into payload
        System.arraycopy(langBytes, 0, payload, 1, langLength);
        System.arraycopy(textBytes, 0, payload, 1 + langLength, textLength);

        return new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], payload);
    }

    private void write(String text, Tag tag) {

        try {
            NdefRecord[] records = { createRecord(text) };
            NdefMessage message = new NdefMessage(records);
            Ndef ndef = Ndef.get(tag);
            ndef.connect();
            ndef.writeNdefMessage(message);
            ndef.close();
        } catch (IOException ex) {
            Log.e("Authentication", "Error writing tag", ex);
        } catch (FormatException ex) {
            Log.e("Authentication", "Error writing tag", ex);
        }
    }
}
