package pt.ua.travis.ui.login;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.Window;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import pt.ua.travis.R;
import pt.ua.travis.ui.customviews.CircularImageView;
import pt.ua.travis.ui.customviews.TravisFragment;
import pt.ua.travis.utils.CommonKeys;

import java.util.Arrays;

/**
 * This activity holds the Fragments of the introduction and coordinates
 * communication.
 *
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class SignUpActivity extends SherlockFragmentActivity implements SignUpNavigationListener, View.OnClickListener {
    private boolean userIsAlreadyCreated;

    private String firstName = "";
    private String lastName = "";
    private byte[] imageData = null;

    private String userEmail = null;
    private String userPass = null;


    private CircularImageView photoHolder;
    private PhotoPickerDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_signup);

        Intent intent = getIntent();
        userIsAlreadyCreated = intent.getBooleanExtra(CommonKeys.LOGGED_IN_FROM_SOCIAL, false);
        if(userIsAlreadyCreated){
            firstName = intent.getStringExtra(CommonKeys.FIRST_NAME);
            lastName = intent.getStringExtra(CommonKeys.LAST_NAME);
            imageData = intent.getByteArrayExtra(CommonKeys.IMAGE_BYTES);
        }

        userEmail = intent.getStringExtra(CommonKeys.EMAIL);
        userPass = intent.getStringExtra(CommonKeys.PASS);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.signup_fragment_container, new SignUpFirstFragment(), SignUpFirstFragment.TAG);
        fragmentTransaction.commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        dialog.onActivityResult(requestCode, resultCode, data);
    }

    public boolean userIsAlreadyCreated() {
        return userIsAlreadyCreated;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getLastName() {
        return lastName;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public String getEmail() {
        return userEmail;
    }

    public String getPassword() {
        return userPass;
    }

    public void setPhotoHolder(CircularImageView photoHolder) {
        this.photoHolder = photoHolder;
    }

    @Override
    public void toFirst(String fromTag) {
        goToFragment(fromTag, SignUpFirstFragment.TAG, new SignUpFirstFragment());
    }

    @Override
    public void toSecond(String fromTag) {
        goToFragment(fromTag, SignUpSecondFragment.TAG, new SignUpSecondFragment());
    }

    @Override
    public void toThird(String fromTag) {
        goToFragment(fromTag, SignUpThirdFragment.TAG, new SignUpThirdFragment());
    }

    @Override
    public void done() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(SignUpThirdFragment.TAG);
        if (fragment != null) {
            fragmentTransaction.remove(
                    getSupportFragmentManager().findFragmentByTag(SignUpThirdFragment.TAG));
        }
        fragmentTransaction.commit();
    }

    private void goToFragment(String oldFragmentTag, String newFragmentTag, TravisFragment newFragment) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(oldFragmentTag);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.signup_fragment_container, newFragment, newFragmentTag)
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);

        if (fragment != null) {
            fragmentTransaction.remove(getSupportFragmentManager().findFragmentByTag(oldFragmentTag));
        }

        fragmentTransaction.commit();
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        dialog = PhotoPickerDialog.newInstance(this, new PhotoPickerDialog.OnPhotoPickedListener() {
            @Override
            public void onByteArrayReturned(final byte[] array) {
                Log.e("-----------------", Arrays.toString(array));
                if(array != null) {
                    imageData = array;

                    new AsyncTask<Void, Void, BitmapDrawable>(){
                        @Override
                        protected void onPreExecute() {
                            super.onPreExecute();
                            photoHolder.setImageDrawable(getResources().getDrawable(R.drawable.progress_bar));
                        }

                        @Override
                        protected BitmapDrawable doInBackground(Void... params) {
                            Bitmap bm = BitmapFactory.decodeByteArray(array, 0, array.length);
                            return new BitmapDrawable(getResources(), bm);
//                            DisplayMetrics dm = new DisplayMetrics();
//                            getWindowManager().getDefaultDisplay().getMetrics(dm);
//                            photoHolder.setMinimumHeight(dm.heightPixels);
//                            photoHolder.setMinimumWidth(dm.widthPixels);
//                            return null;
                        }

                        @Override
                        protected void onPostExecute(BitmapDrawable bm) {
                            super.onPostExecute(bm);
                            photoHolder.setImageDrawable(bm);
                        }
                    }.execute();
                }
                dialog.dismissAllowingStateLoss();
            }
        });

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(dialog, null);
        ft.commitAllowingStateLoss();
    }
}
