package pt.ua.travis.ui.signup;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.parse.ParseException;
import pt.ua.travis.R;
import pt.ua.travis.backend.*;
import pt.ua.travis.core.BaseFragment;
import pt.ua.travis.ui.customviews.SquareImageView;

/**
 * This fragment shows the third screen of the signup process.
 *
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class SignUpThirdFragment extends BaseFragment implements OnClickListener {

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
            if (userType == null) {
                Toast.makeText(parentActivity, R.string.type_required, Toast.LENGTH_LONG).show();
            } else {
                register();
            }

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
