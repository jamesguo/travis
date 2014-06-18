package pt.ua.travis.core;

import android.app.Activity;
import android.app.Dialog;
import android.content.*;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.squareup.picasso.Picasso;
import org.json.JSONException;
import pt.ua.travis.R;
import pt.ua.travis.ui.customviews.CircularImageView;
import pt.ua.travis.utils.TravisUtils;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public abstract class BaseActivity extends SherlockFragmentActivity {

    private static final String TAG = BaseActivity.class.getSimpleName();

    private static final int REQUEST_CODE_PAYMENT = 1;

    private Dialog wifiNotification;

    @Override
    protected void onResume() {
        super.onResume();

        // check if wifi is connected right now
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (!mWifi.isConnected()) {
            showWirelessNotification();
        }

        // enable a wifi connection listener
        registerReceiver(wifiStateReceiver, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(wifiStateReceiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PAYMENT) {
            if (resultCode == Activity.RESULT_OK) {
                PaymentConfirmation confirm = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirm != null) {
                    try {
                        Log.i(TAG, confirm.toJSONObject().toString(4));
                        Log.i(TAG, confirm.getPayment().toJSONObject().toString(4));
                        String paymentAmount = confirm.getPayment().getAmountAsLocalizedString();


                        Toast.makeText(this,"PaymentConfirmation info received from PayPal", Toast.LENGTH_LONG).show();

                    } catch (JSONException e) {
                        Log.e(TAG, "an extremely unlikely failure occurred: ", e);
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.i(TAG, "The user canceled.");
            } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
                Log.i(TAG, "An invalid Payment or PayPalConfiguration was submitted. Please see the docs.");
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void showTravisNotification(String text, String imageUri, NotificationColor color){
        final Dialog overlayInfo = new Dialog(this);
        overlayInfo.requestWindowFeature(Window.FEATURE_NO_TITLE);

        Window window = overlayInfo.getWindow();
        window.setBackgroundDrawable(new ColorDrawable(getResources().getColor(color.resID)));
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        WindowManager.LayoutParams params = overlayInfo.getWindow().getAttributes();
        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = 0;
        params.width = TravisUtils.getScreenWidth(this);
        overlayInfo.setCancelable(false);
        overlayInfo.setContentView(R.layout.notification_bar);

        TextView textView = (TextView) overlayInfo.findViewById(R.id.travis_notification_text);
        textView.setText(text);

        if(imageUri != null) {
            CircularImageView imageView = (CircularImageView) overlayInfo.findViewById(R.id.travis_notification_photo);
            Picasso.with(this).load(imageUri).into(imageView);
        }

        overlayInfo.show();

        final Handler handler  = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (overlayInfo.isShowing()) {
                    overlayInfo.dismiss();
                }
            }
        };

        overlayInfo.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                handler.removeCallbacks(runnable);
            }
        });

        handler.postDelayed(runnable, 2500);
    }

    public void showWirelessNotification(){
        if(wifiNotification ==null) {
            wifiNotification = new Dialog(this);
            wifiNotification.requestWindowFeature(Window.FEATURE_NO_TITLE);

            Window window = wifiNotification.getWindow();
            window.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.travis_color)));
//        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
//                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
//        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

            WindowManager.LayoutParams params = wifiNotification.getWindow().getAttributes();
            params.gravity = Gravity.TOP | Gravity.LEFT;
            params.x = 0;
            params.y = 0;
            params.width = TravisUtils.getScreenWidth(this);
            wifiNotification.setCancelable(false);
            wifiNotification.setContentView(R.layout.notification_bar_wifi);
            wifiNotification.show();
        }
    }

    public void closeWifiNotification(){
        if (wifiNotification != null && wifiNotification.isShowing()) {
            wifiNotification.dismiss();
            wifiNotification = null;
        }
    }

    private BroadcastReceiver wifiStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int extraWifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);

            switch (extraWifiState) {

                case WifiManager.WIFI_STATE_DISABLED:
                case WifiManager.WIFI_STATE_DISABLING:
                    showWirelessNotification();
                    break;
                case WifiManager.WIFI_STATE_ENABLED:
                    closeWifiNotification();
                    break;
            }
        }
    };



    public enum NotificationColor {
        DEFAULT(R.color.travis_color),
        GREEN(R.color.taxi_available_border),
        RED(R.color.taxi_unavailable_border);

        private int resID;

        private NotificationColor(int resID) {
            this.resID = resID;
        }
    }
}
