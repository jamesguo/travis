package pt.ua.travis.ui.login;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseException;
import com.parse.ParseUser;
import org.apache.commons.io.output.ByteArrayOutputStream;
import pt.ua.travis.R;
import pt.ua.travis.backend.*;
import pt.ua.travis.core.TravisApplication;
import pt.ua.travis.ui.customviews.SquareImageView;
import pt.ua.travis.ui.customviews.TravisFragment;
import pt.ua.travis.ui.main.MainClientActivity;
import pt.ua.travis.ui.main.MainTaxiActivity;

/**
 * This fragment shows the third screen of the signup process.
 *
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class SignUpThirdFragment extends TravisFragment implements OnClickListener {

    public static final String TAG = "THIRD";

    private SignUpActivity parentActivity;
    private SignUpNavigationListener mCallback;

    private String userType;

    private SquareImageView clientIcon;
    private SquareImageView taxiIcon;
    private TextView accountTypeName;
    private TextView accountTypeInfo;
    private ImageView mPrevBtn;
    private ImageView mDoneBtn;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.parentActivity = (SignUpActivity) activity;

        // Set the callback activity to use
        try {
            mCallback = (SignUpNavigationListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(
                    activity.toString() + " must implement SignUpNavigationListener");
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setContentView(R.layout.fragment_signup_third);
        View v = getView();

        accountTypeName = (TextView) v.findViewById(R.id.account_type_text_view_1);
        accountTypeInfo = (TextView) v.findViewById(R.id.account_type_text_view_2);

        clientIcon = (SquareImageView) v.findViewById(R.id.type_client_icon);
        clientIcon.setOnClickListener(this);
        taxiIcon = (SquareImageView) v.findViewById(R.id.type_taxi_icon);
        taxiIcon.setOnClickListener(this);

        mPrevBtn = (ImageView) v.findViewById(R.id.third_prev_btn);
        mPrevBtn.setOnClickListener(this);

        mDoneBtn = (ImageView) v.findViewById(R.id.third_done);
        mDoneBtn.setOnClickListener(this);

        setContentShown(true);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.third_prev_btn){
            mCallback.toSecond(TAG);

        } else if (v.getId() == R.id.third_done) {
            register();

        } else if (v.getId() == R.id.type_client_icon) {
            userType = PersistenceManager.TYPE_CLIENT;
            clientIcon.setBackgroundResource(R.drawable.signup_user_type_frame);
            taxiIcon.setBackgroundResource(android.R.color.white);
            accountTypeName.setText(R.string.client);
            accountTypeInfo.setText(R.string.picked_account_client);

        } else if (v.getId() == R.id.type_taxi_icon) {
            userType = PersistenceManager.TYPE_TAXI;
            clientIcon.setBackgroundResource(android.R.color.white);
            taxiIcon.setBackgroundResource(R.drawable.signup_user_type_frame);
            accountTypeName.setText(R.string.taxi);
            accountTypeInfo.setText(R.string.picked_account_taxi);
        }
    }

    public void register() {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                setContentShown(false);
            }

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    if (parentActivity.userIsAlreadyCreated()) {
                        PersistenceManager.registerNewUserFromSocialMedia(
                                parentActivity,
                                userType,
                                parentActivity.getEmail(),
                                parentActivity.getFirstName(),
                                parentActivity.getLastName(),
                                parentActivity.getImageData());
                    } else {
                        PersistenceManager.registerNewUser(
                                parentActivity,
                                userType,
                                parentActivity.getEmail(),
                                parentActivity.getPassword(),
                                parentActivity.getFirstName(),
                                parentActivity.getLastName(),
                                parentActivity.getImageData());
                    }
                }catch (ParseException ex) {
                    Log.e(TAG, "Error signing up the user", ex);
                }
                return null;
            }

        }.execute();

    }

}
