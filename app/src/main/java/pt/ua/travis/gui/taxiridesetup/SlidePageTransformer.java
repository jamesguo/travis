package pt.ua.travis.gui.taxiridesetup;

import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class SlidePageTransformer implements ViewPager.PageTransformer {
    private static final float SCALE_FACTOR = 0.95f;

    private final ViewPager mViewPager;

    public SlidePageTransformer(ViewPager viewPager) {
        this.mViewPager = viewPager;
    }

    @Override
    public void transformPage(View page, float position) {
        if (position <= 0) {

            // apply zoom effect and offset translation only for pages to
            // the left
            final float transformValue = Math.abs(Math.abs(position) - 1) * (1.0f - SCALE_FACTOR) + SCALE_FACTOR;
            int pageWidth = mViewPager.getWidth();
            final float translateValue = position * -pageWidth;
            page.setScaleX(transformValue);
            page.setScaleY(transformValue);
            if (translateValue > -pageWidth) {
                page.setTranslationX(translateValue);
            } else {
                page.setTranslationX(0);
            }
        }
    }

}
