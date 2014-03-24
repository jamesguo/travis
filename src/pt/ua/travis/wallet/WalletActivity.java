package pt.ua.travis.wallet;

import android.app.Activity;
import android.os.Bundle;
import android.provider.SyncStateContract;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.WalletConstants;
import pt.ua.travis.R;


/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class WalletActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{
    public GoogleApiClient gpc;
    static final int REQUEST_CODE_PRE_AUTH = 1010;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        gpc = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .setAccountName("joaop_pedrosa@hotmail.com")
                .addApi(Wallet.API, new Wallet.WalletOptions.Builder()
                        .setEnvironment(WalletConstants.THEME_HOLO_LIGHT)
                        .setTheme(WalletConstants.THEME_HOLO_LIGHT)
                        .build())
                .build();
    }

    public void onStart() {
        super.onStart();
        gpc.connect();
    }

    public void onStop() {
        super.onStop();
        gpc.disconnect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Wallet.checkForPreAuthorization(gpc, REQUEST_CODE_PRE_AUTH);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this,"Conexão Suspensa!",Toast.LENGTH_SHORT);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this,"Falha ao conectar com a Google Wallet!",Toast.LENGTH_SHORT);
    }
}