package rs.applab.cursorwheel;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;

public class LuckyWheel extends FrameLayout {

    CursorWheelLayout cursorWheelLayout;

    /**
     * callback on menu item being selected
     */
    private OnSelectionListener mOnSelectionListener;

    /**
     * rotation time
     */
    public static final int DEFAULT_ROTATION_DURATION_MS = 5000;

    /**
     * how many time wheel will be rotated before stops
     */
    public static final int DEFAULT_ROTATIONS_COUNT = 5;

    /**
     *
     * defines rotation interpolation
     */
    public static final Interpolator DEFAULT_ANIMATION_INTERPOLATOR = new OvershootInterpolator(1.5f);


    public LuckyWheel(Context context) {
        super(context);
        init(context);

    }

    public LuckyWheel(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);

    }

    public LuckyWheel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);

    }

    public LuckyWheel(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.wheel_layout, null);
        addView(view);
        cursorWheelLayout = view.findViewById(R.id.view);
    }


    int selectedPosition = 0;
    int selectedAngle = 0;

    boolean animationInProgress = false;

    public void setSelection(final int position, final int rounds, final int rotationDuration, final Interpolator interpolator) {

        final int angleBetweenElements = 360 / cursorWheelLayout.getAdapter().getCount();
        final int itemForSelectAngle = angleBetweenElements * position;

        final int angleToSelect = 360 * rounds - itemForSelectAngle;
        RotateAnimation rotate = new RotateAnimation(0, angleToSelect, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(rotationDuration);
        rotate.setInterpolator(interpolator);
        rotate.setFillAfter(true);
        animationInProgress = true;
        rotate.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                Log.d("setAnimationListener", "onAnimationStart");
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                animationInProgress = false;
                selectedAngle = angleToSelect;
                Log.d("setAnimationListener", "onAnimationEnd for angle: " + selectedAngle + " position: " + position);
                mOnSelectionListener.onItemSelected(LuckyWheel.this, position);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                Log.d("setAnimationListener", "onAnimationRepeat");
            }
        });
        cursorWheelLayout.startAnimation(rotate);
    }

    public void setSelection(int position) {
        setSelection(position, DEFAULT_ROTATIONS_COUNT, DEFAULT_ROTATION_DURATION_MS, DEFAULT_ANIMATION_INTERPOLATOR);
    }

    public void setAdapter(CursorWheelLayout.CycleWheelAdapter adapter) {
        cursorWheelLayout.setAdapter(adapter);
    }


    public void setOnSelectionListener(OnSelectionListener onSelectionListener) {
        mOnSelectionListener = onSelectionListener;
    }


    /**
     * callback when item selected
     *
     */
    public interface OnSelectionListener {

        void onItemSelected(LuckyWheel parent, int pos);
    }

}

