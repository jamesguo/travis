package pt.ua.travis.ui.customviews;

import android.view.View;
import uk.co.senab.actionbarpulltorefresh.library.viewdelegates.ViewDelegate;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class CustomViewDelegate implements ViewDelegate {

    @Override
    public boolean isReadyForPull(View view, float v, float v2) {
        return true;
    }
}
