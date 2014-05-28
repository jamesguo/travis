package pt.ua.travis.ui.customviews;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.*;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;
import pt.ua.travis.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class BlurDrawerLayout extends FrameLayout implements GestureDetector.OnGestureListener{

    public static final int LEFT_SIDE = 0;
    public static final int RIGHT_SIDE = 1;

    private ImageView imageViewShadow;
    private ImageView imageViewBackground;
    private LinearLayout layoutLeftDrawer;
    private LinearLayout layoutRightDrawer;
    private ScrollView scrollViewLeftDrawer;
    private ScrollView scrollViewRightDrawer;
    private ScrollView scrollViewDrawer;
    private Activity activity;
    private ViewGroup viewDecor;
    private LockedView viewActivity;
    private boolean isOpened;
    private GestureDetector gestureDetector;
    private float shadowAdjustScaleX;
    private float shadowAdjustScaleY;
    private List<View> ignoredViews;
    private List<BlurDrawerObject> leftDrawerObjects;
    private List<BlurDrawerObject> rightDrawerObject;
    private DisplayMetrics displayMetrics = new DisplayMetrics();
    private OnDrawerListener drawerListener;
    private float lastRawX;
    private boolean canScale = false;
    private int scaleDirection = LEFT_SIDE;
    private List<Integer> disabledSide = new ArrayList<Integer>();

    public BlurDrawerLayout(Context context) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.blur_drawer, this);
        scrollViewLeftDrawer = (ScrollView) findViewById(R.id.sv_left_drawer);
        scrollViewRightDrawer = (ScrollView) findViewById(R.id.sv_right_drawer);
        imageViewShadow = (ImageView) findViewById(R.id.iv_shadow);
        layoutLeftDrawer = (LinearLayout) findViewById(R.id.layout_left_drawer);
        layoutRightDrawer = (LinearLayout) findViewById(R.id.layout_right_drawer);
        imageViewBackground = (ImageView) findViewById(R.id.iv_background);
    }

    /**
     * Sets up the activity where blur drawer will be shown.
     */
    public void attachToActivity(Activity activity){
        initValue(activity);
        setShadowAdjustScaleXByOrientation();
        viewDecor.addView(this, 0);
        setViewPadding();
    }

    private void initValue(Activity activity){
        this.activity   = activity;
        leftDrawerObjects = new ArrayList<BlurDrawerObject>();
        rightDrawerObject = new ArrayList<BlurDrawerObject>();
        gestureDetector = new GestureDetector(this);
        ignoredViews    = new ArrayList<View>();
        viewDecor = (ViewGroup) activity.getWindow().getDecorView();
        viewActivity = new LockedView(this.activity);

        View mContent   = viewDecor.getChildAt(0);
        viewDecor.removeViewAt(0);
        viewActivity.setContent(mContent);
        viewDecor.addView(viewActivity, 0);
    }

    private void setShadowAdjustScaleXByOrientation(){
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            shadowAdjustScaleX = 0.034f;
            shadowAdjustScaleY = 0.12f;
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            shadowAdjustScaleX = 0.06f;
            shadowAdjustScaleY = 0.07f;
        }
    }

    /**
     * Sets the background picture.
     */
    public void setBackground(int imageResrouce){
        imageViewBackground.setImageResource(imageResrouce);
    }

    /**
     * Sets the visibility of the shadow under the activity view.
     */
    public void setShadowVisible(boolean isVisible){
        if (isVisible)
            imageViewShadow.setImageResource(R.drawable.shadow);
        else
            imageViewShadow.setImageBitmap(null);
    }

    /**
     * Adds a drawer object to the drawer in the specified side.
     */
    public void addDrawerObject(BlurDrawerObject drawerObject, int side){
        if (side == LEFT_SIDE){
            this.leftDrawerObjects.add(drawerObject);
            layoutLeftDrawer.addView(drawerObject);
        } else if(side == RIGHT_SIDE) {
            this.rightDrawerObject.add(drawerObject);
            layoutRightDrawer.addView(drawerObject);
        }
    }

    private void rebuildMenu(){
        layoutLeftDrawer.removeAllViews();
        layoutRightDrawer.removeAllViews();
        for(int i = 0; i < leftDrawerObjects.size() ; i ++)
            layoutLeftDrawer.addView(leftDrawerObjects.get(i), i);
        for(int i = 0; i < rightDrawerObject.size() ; i ++)
            layoutRightDrawer.addView(rightDrawerObject.get(i), i);
    }

    /**
     * Retrieves the drawer objects from the drawer in the specified side.
     */
    public List<BlurDrawerObject> getDrawerObjects(int side) {
        if (side == LEFT_SIDE)
            return leftDrawerObjects;
        else if(side == RIGHT_SIDE)
            return rightDrawerObject;
        else
            throw new IllegalArgumentException("Invalid side specified.");
    }

    /**
     * Sets a listener for opening and closing actions.
     */
    public void setDrawerListener(OnDrawerListener drawerListener) {
        this.drawerListener = drawerListener;
    }

    /**
     * Returns the configured listener for opening and closing actions.
     */
    public OnDrawerListener getDrawerListener() {
        return drawerListener;
    }

    /**
     * we need the call the method before the drawer show, because the
     * padding of activity can't get at the moment of onCreateView();
     */
    private void setViewPadding(){
        this.setPadding(viewActivity.getPaddingLeft(),
                viewActivity.getPaddingTop(),
                viewActivity.getPaddingRight(),
                viewActivity.getPaddingBottom());
    }

    /**
     * Opens the drawer from the specified side.
     */
    public void open(int side){
        if (sideIsDisabled(side))
            throw new IllegalArgumentException("You cannot open a disabled drawer side. Please enable this drawer side first.");
        setDrawerScale(side);

        isOpened = true;
        AnimatorSet scaleDown_activity = buildScaleDownAnimation(viewActivity, 0.5f, 0.5f);
        AnimatorSet scaleDown_shadow = buildScaleDownAnimation(imageViewShadow, 0.5f + shadowAdjustScaleX, 0.5f + shadowAdjustScaleY);
        AnimatorSet alpha_drawer = buildMenuAnimation(scrollViewDrawer, 1.0f);
        scaleDown_shadow.addListener(animationListener);
        scaleDown_activity.playTogether(scaleDown_shadow);
        scaleDown_activity.playTogether(alpha_drawer);
        scaleDown_activity.start();
    }

    /**
     * Closes the drawer from any side.
     */
    public void close(){

        isOpened = false;
        AnimatorSet scaleUp_activity = buildScaleUpAnimation(viewActivity, 1.0f, 1.0f);
        AnimatorSet scaleUp_shadow = buildScaleUpAnimation(imageViewShadow, 1.0f, 1.0f);
        AnimatorSet alpha_drawer = buildMenuAnimation(scrollViewDrawer, 0.0f);
        scaleUp_activity.addListener(animationListener);
        scaleUp_activity.playTogether(scaleUp_shadow);
        scaleUp_activity.playTogether(alpha_drawer);
        scaleUp_activity.start();
    }

    public void disableSide(int side){
        disabledSide.add(side);
    }

    private boolean sideIsDisabled(int side){
        return disabledSide.contains(side);
    }

    private void setDrawerScale(int side){
        if(side!=LEFT_SIDE && side!=RIGHT_SIDE){
            return;
        }

        int screenWidth = getScreenWidth();
        float pivotX;
        float pivotY = getScreenHeight() * 0.5f;

        if (side == LEFT_SIDE){
            scrollViewDrawer = scrollViewLeftDrawer;
            pivotX  = screenWidth * 1.5f;
        } else {
            scrollViewDrawer = scrollViewRightDrawer;
            pivotX  = screenWidth * -0.5f;
        }

        ViewHelper.setPivotX(viewActivity, pivotX);
        ViewHelper.setPivotY(viewActivity, pivotY);
        ViewHelper.setPivotX(imageViewShadow, pivotX);
        ViewHelper.setPivotY(imageViewShadow, pivotY);
        scaleDirection = side;
    }

    /**
     * Returns if the current drawer opened status.
     */
    public boolean isOpened() {
        return isOpened;
    }

    private OnClickListener viewActivityOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            if (isOpened()) close();
        }
    };

    private Animator.AnimatorListener animationListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animation) {
            if (isOpened){
                scrollViewDrawer.setVisibility(VISIBLE);
                if (drawerListener != null)
                    drawerListener.onDrawerOpened();
            }
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            // resets the view;
            if(isOpened){
                viewActivity.setTouchDisable(true);
                viewActivity.setOnClickListener(viewActivityOnClickListener);
            }else{
                viewActivity.setTouchDisable(false);
                viewActivity.setOnClickListener(null);
                scrollViewDrawer.setVisibility(GONE);
                if (drawerListener != null)
                    drawerListener.onDrawerClosed();
            }
        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    };

    /**
     * An helper method to build the "scaling down" animation.
     */
    private AnimatorSet buildScaleDownAnimation(View target,
                                                float targetScaleX,
                                                float targetScaleY){

        AnimatorSet scaleDown = new AnimatorSet();
        scaleDown.playTogether(
                ObjectAnimator.ofFloat(target, "scaleX", targetScaleX),
                ObjectAnimator.ofFloat(target, "scaleY", targetScaleY)
        );

        scaleDown.setInterpolator(AnimationUtils.loadInterpolator(activity,
                android.R.anim.decelerate_interpolator));
        scaleDown.setDuration(250);
        return scaleDown;
    }

    /**
     * An helper method to build the "scaling up" animation.
     */
    private AnimatorSet buildScaleUpAnimation(View target,
                                              float targetScaleX,
                                              float targetScaleY){

        AnimatorSet scaleUp = new AnimatorSet();
        scaleUp.playTogether(
                ObjectAnimator.ofFloat(target, "scaleX", targetScaleX),
                ObjectAnimator.ofFloat(target, "scaleY", targetScaleY)
        );

        scaleUp.setDuration(250);
        return scaleUp;
    }

    private AnimatorSet buildMenuAnimation(View target, float alpha){

        AnimatorSet alphaAnimation = new AnimatorSet();
        alphaAnimation.playTogether(
                ObjectAnimator.ofFloat(target, "alpha", alpha)
        );

        alphaAnimation.setDuration(250);
        return alphaAnimation;
    }

    /**
     * On some occasions, the slipping gesture function for locking/unlocking
     * drawer may have conflicts with widgets.
     * This option allows the user to set the drawer to ignore
     * motion events on the specified view.
     */
    public void addIgnoredView(View v){
        ignoredViews.add(v);
    }

    /**
     * Returns true if the motion event was performed on a view
     * that is in the ignored view list.
     */
    private boolean isIgnoredView(MotionEvent ev) {
        Rect rect = new Rect();
        for (View v : ignoredViews) {
            v.getGlobalVisibleRect(rect);
            if (rect.contains((int) ev.getX(), (int) ev.getY()))
                return true;
        }
        return false;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    private void setScaleDirectionByRawX(float currentRawX){
        if (currentRawX < lastRawX)
            setDrawerScale(RIGHT_SIDE);
        else
            setDrawerScale(LEFT_SIDE);
    }

    private float getTargetScale(float currentRawX){
        float scaleFloatX = ((currentRawX - lastRawX) / getScreenWidth()) * 0.75f;
        scaleFloatX = scaleDirection == RIGHT_SIDE ? - scaleFloatX : scaleFloatX;

        float targetScale = ViewHelper.getScaleX(viewActivity) - scaleFloatX;
        targetScale = targetScale > 1.0f ? 1.0f : targetScale;
        targetScale = targetScale < 0.5f ? 0.5f : targetScale;
        return targetScale;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        float currentActivityScaleX = ViewHelper.getScaleX(viewActivity);
        if (currentActivityScaleX == 1.0f)
            setScaleDirectionByRawX(ev.getRawX());

        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                canScale = !isIgnoredView(ev);
                break;

            case MotionEvent.ACTION_MOVE:
                if (!canScale || sideIsDisabled(scaleDirection))
                    break;
                if (currentActivityScaleX < 0.95)
                    scrollViewDrawer.setVisibility(VISIBLE);

                float targetScale = getTargetScale(ev.getRawX());
                ViewHelper.setScaleX(viewActivity, targetScale);
                ViewHelper.setScaleY(viewActivity, targetScale);
                ViewHelper.setScaleX(imageViewShadow, targetScale + shadowAdjustScaleX);
                ViewHelper.setScaleY(imageViewShadow, targetScale + shadowAdjustScaleY);
                ViewHelper.setAlpha(scrollViewDrawer, (1 - targetScale) * 2.0f);
                break;

            case MotionEvent.ACTION_UP:
                if (!canScale)
                    break;
                if (currentActivityScaleX > 0.75f){
                    close();
                }else{
                    open(scaleDirection);
                }
                break;
        }
        lastRawX = ev.getRawX();
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {}

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2) {
        return true;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {}

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2) {
        return false;
    }

    public int getScreenHeight(){
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }

    public int getScreenWidth(){
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

    public interface OnDrawerListener {

        /**
         * Will be called at the end of the drawer opening animation.
         */
        public void onDrawerOpened();

        /**
         * Will be called at the end of the drawer closing animation.
         */
        public void onDrawerClosed();
    }

}
