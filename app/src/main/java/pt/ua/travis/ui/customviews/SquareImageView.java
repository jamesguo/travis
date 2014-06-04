package pt.ua.travis.ui.customviews;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class SquareImageView extends ImageView {

    public SquareImageView(Context context, AttributeSet set) {
        super(context, set);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }
}
