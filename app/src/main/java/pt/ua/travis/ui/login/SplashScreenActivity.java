package pt.ua.travis.ui.login;

import android.content.*;
import android.os.Bundle;
import android.widget.ImageView;
import com.google.common.base.Strings;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ObjectAnimator;
import pt.ua.travis.R;
import pt.ua.travis.backend.User;
import pt.ua.travis.core.BaseActivity;
import pt.ua.travis.utils.TravisUtils;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class SplashScreenActivity extends BaseActivity {

    public static final String DO_AUTO_LOGIN = "no_auto_login";

    public static final String AUTO_EMAIL = "remember_user";
    public static final String AUTO_PASS = "remember_pass";

    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        setContentView(R.layout.activity_empty);

        imageView = (ImageView) findViewById(R.id.logo);

        // If the auto login skipper flag is present, attempt auto login.
        SharedPreferences prefs = this.getSharedPreferences("TravisPreferences", MODE_PRIVATE);
        boolean doAutoLogin = prefs.getBoolean(DO_AUTO_LOGIN, false);
        if(doAutoLogin) {
            String email = prefs.getString(AUTO_EMAIL, "");
            String pass = prefs.getString(AUTO_PASS, "");

            if(!Strings.isNullOrEmpty(email) && !Strings.isNullOrEmpty(pass)){
                // Credentials stored in the shared preferences are not null nor
                // empty, which means that they were previously set by the user.
                new LoginTask(email, pass).execute(new LoginTask.OnTaskEndedListener() {
                    @Override
                    public void onLoginSuccess(User loggedUser) {
                        LoginTask.goToMainActivity(SplashScreenActivity.this, loggedUser);
                    }

                    @Override
                    public void onNewCredentials() {
                        goToLoginScreen();
                    }

                    @Override
                    public void onWrongCredentials() {
                        goToLoginScreen();
                    }

                });
                return;
            }
        }

        goToLoginScreen();
    }

    private void goToLoginScreen() {
        final int moveUpValue = TravisUtils.dpToPx(this, -176);

        ObjectAnimator animator = ObjectAnimator.ofFloat(imageView, "translationY", 0, moveUpValue);
        animator.setDuration(1000);
        animator.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationEnd(Animator animation) {
                Intent intent = new Intent(SplashScreenActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onAnimationStart(Animator animation) {}

            @Override
            public void onAnimationCancel(Animator animation) {}

            @Override
            public void onAnimationRepeat(Animator animation) {}
        });
        animator.start();

    }
}
