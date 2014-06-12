package pt.ua.travis.ui.login;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import com.andreabaccega.widget.FormEditText;
import pt.ua.travis.R;
import pt.ua.travis.core.TravisFragment;

/**
 * This fragment shows the first screen of the signup process.
 *
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class SignUpFirstFragment extends TravisFragment implements OnClickListener {

    public static final String TAG = "FIRST";

    private SignUpActivity parentActivity;
    private SignUpNavigationListener mCallback;

    private FormEditText firstName, lastName;
    private ImageView mNextBtn;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.parentActivity = (SignUpActivity) activity;

        // Set the callback activity to use
        try {
            mCallback = (SignUpNavigationListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement SignUpNavigationListener");
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setContentView(R.layout.fragment_signup_first);

        TextView welcomeMsg2 = (TextView) parentActivity.findViewById(R.id.welcome_text2);
        if(parentActivity.userIsAlreadyCreated()){
            welcomeMsg2.setText(R.string.welcome_text2_alt);
        }

        firstName = (FormEditText) parentActivity.findViewById(R.id.field_first_name);
        firstName.setText(parentActivity.getFirstName());
        lastName = (FormEditText) parentActivity.findViewById(R.id.field_last_name);
        lastName.setText(parentActivity.getLastName());

        mNextBtn = (ImageView) parentActivity.findViewById(R.id.first_next_btn);
        mNextBtn.setOnClickListener(this);

        setContentShown(true);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.first_next_btn) {

            boolean failed = false;
            if(!firstName.testValidity()) {
                firstName.setDanger();
                failed = true;
            } else {
                firstName.setSuccess();
            }

            if(!lastName.testValidity()) {
                lastName.setDanger();
                failed = true;
            } else {
                lastName.setSuccess();
            }

            if(!failed) {
                parentActivity.setFirstName(firstName.getText().toString());
                parentActivity.setLastName(lastName.getText().toString());
                mCallback.toSecond(TAG);
            }
        }
    }
}
