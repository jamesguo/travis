package pt.ua.travis.ui.signup;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import pt.ua.travis.R;
import pt.ua.travis.core.BaseFragment;
import pt.ua.travis.ui.customviews.CircularImageView;

/**
 * This fragment shows the second screen of the signup process.
 *
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class SignUpSecondFragment extends BaseFragment implements OnClickListener {

    public static final String TAG = "SECOND";

    private SignUpActivity parentActivity;
    private SignUpNavigationListener mCallback;

    private ImageView mPrevBtn;
    private ImageView mNextBtn;

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
        setContentView(R.layout.fragment_signup_second);

        TextView welcomeMsg3 = (TextView) parentActivity.findViewById(R.id.welcome_text3);

        CircularImageView photoHolder = (CircularImageView) parentActivity.findViewById(R.id.pick_photo_holder);
        parentActivity.setPhotoHolder(photoHolder);
        byte[] imageData = parentActivity.getImageData();
        if(imageData!=null){
            photoHolder.setImageBitmap(BitmapFactory.decodeByteArray(imageData, 0, imageData.length));
        }
        photoHolder.setOnClickListener(parentActivity);

        if(parentActivity.userIsAlreadyCreated()){
            welcomeMsg3.setText(R.string.welcome_text3_alt);
        }

        mPrevBtn = (ImageView) parentActivity.findViewById(R.id.second_prev_btn);
        mPrevBtn.setOnClickListener(this);
        mNextBtn = (ImageView) parentActivity.findViewById(R.id.second_next_btn);
        mNextBtn.setOnClickListener(this);

        setContentShown(true);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.second_prev_btn) {
            mCallback.toFirst(TAG);

        } else if(v.getId() == R.id.second_next_btn){
            if(parentActivity.getImageData()==null) {
                Toast.makeText(parentActivity, R.string.photo_required, Toast.LENGTH_LONG).show();
                return;
            }

            mCallback.toThird(TAG);
        }
    }
}
