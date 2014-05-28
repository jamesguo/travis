package pt.ua.travis.ui.customviews;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import pt.ua.travis.R;
import pt.ua.travis.backend.User;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class BlurDrawerUser extends BlurDrawerObject {

    public BlurDrawerUser(Context context, User user) {
        super(context);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.blur_user, this);

        CircularImageView iv_photo = (CircularImageView) findViewById(R.id.iv_photo);
        Picasso.with(context).load(user.imageUri()).into(iv_photo);

        TextView tv_name = (TextView) findViewById(R.id.tv_title);
        tv_name.setText(user.name());
    }
}
