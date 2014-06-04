package pt.ua.travis.ui.customviews;

import android.content.Context;
import android.util.AttributeSet;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class SquareCircularImageView extends CircularImageView {

    public SquareCircularImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareCircularImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        int width = measureWidth(widthMeasureSpec);
        int height = super.measureHeight(heightMeasureSpec);
        setMeasuredDimension(height, height);
    }
}
