package rs.applab.cursorwheel;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Region;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.RelativeLayout;

import androidx.annotation.ColorInt;


/**
 * The base cycle wheel menu layout with cursor
 *
 * @author chensuilun
 * @attr R.styleable.CursorWheelLayout_wheelSelectedAngle
 * @attr R.styleable.CursorWheelLayout_wheelPaddingRadio
 * @attr R.styleable.CursorWheelLayout_wheelCenterRadio
 * @attr R.styleable.CursorWheelLayout_wheelItemRadio
 * @attr R.styleable.CursorWheelLayout_wheelFlingValue
 * @attr R.styleable.CursorWheelLayout_wheelCursorColor
 * @attr R.styleable.CursorWheelLayout_wheelCursorHeight
 * @attr R.styleable.CursorWheelLayout_wheelItemRotateMode
 * @attr R.styleable.CursorWheelLayout_wheelGuideLineWidth
 * @attr R.styleable.CursorWheelLayout_wheelGuideLineColor
 */


public class CursorWheelLayout extends ViewGroup {

    private static final String TAG = "CircleMenuLayout";
    /**
     * size of menu item relative to parent
     */
    private static final float RADIO_DEFAULT_CHILD_DIMENSION = 1 / 5f;

    /**
     * size of center item relative to parent
     */
    private static final float RADIO_DEFAULT_CENTER_DIMENSION = 1 / 3f;
    /**
     * ignore the origin padding ,real padding size determine by parent size
     */
    private static final float RADIO_PADDING_LAYOUT = 1 / 40f;


    private static final int INVALID_POSITION = -1;


    private static final float DEFAULT_SELECTED_ANGLE = 0;

    /**
     * Angle a touch can wander before we think the user is flinging
     */
    private static final int FLINGABLE_VALUE = 300;

    /**
     *
     */
    private static final int NOCLICK_VALUE = 3;
    /**
     * default cursor color
     */
    private static final
    @ColorInt
    int DEFAULT_CURSOR_COLOR = 0xFFFFC52A;

    /**
     * default wheel background color
     */
    private static final
    @ColorInt
    int DEFAULT_WHEEL_BG_COLOR = 0xe513171c;

    public static final
    @ColorInt
    int DEFAULT_GUIDE_LINE_COLOR = 0xff727272;

    //DP
    public static final int DEFAULT_TRIANGLE_HEIGHT = 13;

    //DP
    public static final int DEFAULT_GUIDE_LINE_WIDTH = 0;
    /**
     * Don't rotate my item.DEFAULT
     */
    public static final int ITEM_ROTATE_MODE_NONE = 0;

    public static final int ITEM_ROTATE_MODE_INWARD = 1;

    public static final int ITEM_ROTATE_MODE_OUTWARD = 2;


    /**
     * CircleMenuLayout 's size
     */
    private int mRootDiameter;

    /**
     * Angle a touch can wander before we think the user is flinging
     */
    private int mFlingableValue = FLINGABLE_VALUE;


    private float mPadding;


    private double mStartAngle = 0;
    /**
     * menu 's source data
     */
    private CycleWheelAdapter mWheelAdapter;

    /**
     *
     */
    private int mMenuItemCount;

    /**
     *
     */
    private float mTmpAngle;

    /**
     *
     */
    private long mDownTime;

    /**
     * weather is fling now
     */
    private boolean mIsFling;
    /**
     *
     */
    private boolean mIsDraging;

    /**
     *
     */
    private float mLastX;
    private float mLastY;

    /**
     * [0,360)
     */
    private double mSelectedAngle = DEFAULT_SELECTED_ANGLE;
    /**
     * The currently selected item's child.
     */
    private View mSelectedView;
    /**
     * The position of the selected View
     */
    private int mSelectedPosition = INVALID_POSITION;

    /**
     * The temp selected item's child.
     */
    private View mTempSelectedView;

    /**
     * The position of the temp selected item's child.
     */
    private int mTempSelectedPosition = INVALID_POSITION;


    /**
     * 判断是否需要滚动到最中间，某些时候由于选中角度和布局中心的角度一样，所以不需要在进行一次移动
     */
    private boolean mNeedSlotIntoCenter;

//    private FlingRunnable mFlingRunnable = new FlingRunnable();

    /**
     * draw cursor
     */
    private Paint mCursorPaint;
    /**
     * draw wheel bg
     */
    private Paint mWheelPaint;
    /**
     * path of cursor
     */
    private Path mTrianglePath;

    private int mTriangleHeight;


    private int mGuideLineWidth;

    private int mGuideLineColor;

    private boolean mShowGuideLine = false;

    /**
     * callback on menu item being click
     */
    private OnMenuItemClickListener mOnMenuItemClickListener;


    private boolean mIsFirstLayout = true;

    private
    @ColorInt
    int mWheelBgColor;
    private
    @ColorInt
    int mCursorColor;
    private float mMenuRadioDimension;
    private float mCenterRadioDimension;
    private float mPaddingRadio;

    private boolean mIsDebug = false;

    private Path mWheelBgPath = new Path();

    private Matrix mBgMatrix = new Matrix();

    private Region mBgRegion = new Region();

    private Path mGuidePath = new Path();

    private Paint mGuidePaint;


    private int mItemRotateMode = ITEM_ROTATE_MODE_NONE;


    public CursorWheelLayout(Context context) {
        this(context, null);
//        initWheel(context, null);
    }


    public CursorWheelLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CursorWheelLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initWheel(context, attrs);
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CursorWheelLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initWheel(context, attrs);
    }

    private void initWheel(Context context, AttributeSet attrs) {
        setPadding(0, 0, 0, 0);
        final float density = context.getResources().getDisplayMetrics().density;
        mTriangleHeight = (int) (DEFAULT_TRIANGLE_HEIGHT * density + 0.5);
        mGuideLineWidth = (int) (DEFAULT_GUIDE_LINE_WIDTH * density + 0.5);
        mSelectedAngle = DEFAULT_SELECTED_ANGLE;
        mStartAngle = mSelectedAngle;
        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CursorWheelLayout);
            mSelectedAngle = ta.getFloat(R.styleable.CursorWheelLayout_wheelSelectedAngle, DEFAULT_SELECTED_ANGLE);
            if (mSelectedAngle > 360) {
                mSelectedAngle %= 360;
            }
            mStartAngle = mSelectedAngle;
            mWheelBgColor = ta.getColor(R.styleable.CursorWheelLayout_wheelBackgroundColor, DEFAULT_WHEEL_BG_COLOR);
            mCursorColor = ta.getColor(R.styleable.CursorWheelLayout_wheelCursorColor, DEFAULT_CURSOR_COLOR);
            mTriangleHeight = ta.getDimensionPixelOffset(R.styleable.CursorWheelLayout_wheelCursorHeight, mTriangleHeight);
            mMenuRadioDimension = ta.getFloat(R.styleable.CursorWheelLayout_wheelItemRadio, RADIO_DEFAULT_CHILD_DIMENSION);
            mCenterRadioDimension = ta.getFloat(R.styleable.CursorWheelLayout_wheelCenterRadio, RADIO_DEFAULT_CENTER_DIMENSION);
            mPaddingRadio = ta.getFloat(R.styleable.CursorWheelLayout_wheelPaddingRadio, RADIO_PADDING_LAYOUT);
            mGuideLineWidth = ta.getDimensionPixelOffset(R.styleable.CursorWheelLayout_wheelGuideLineWidth, mGuideLineWidth);
//            mGuideLineColor = ta.getColor(R.styleable.CursorWheelLayout_wheelGuideLineColor, DEFAULT_GUIDE_LINE_COLOR);
            mItemRotateMode = ta.getInt(R.styleable.CursorWheelLayout_wheelItemRotateMode, ITEM_ROTATE_MODE_NONE);
            ta.recycle();
        }
        init(context);
    }

    private void init(Context context) {
        setWillNotDraw(false);
        mCursorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCursorPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mCursorPaint.setStrokeWidth(5);
        mCursorPaint.setColor(mCursorColor);
        mCursorPaint.setDither(true);

        mWheelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mWheelPaint.setStyle(Paint.Style.FILL);
        mWheelPaint.setColor(mWheelBgColor);
        mWheelPaint.setDither(true);

        mGuidePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mGuidePaint.setStrokeWidth(mGuideLineWidth);
        mGuidePaint.setColor(mGuideLineColor);
        mGuidePaint.setDither(true);
        mGuidePaint.setStyle(Paint.Style.STROKE);

        mTrianglePath = new Path();
    }

    public void setWheelBackground(@ColorInt int backgroundColor) {
        mWheelBgColor = backgroundColor;
        mWheelPaint.setColor(mWheelBgColor);
    }

    public void showGuideLine(boolean showGuideLine) {
        mShowGuideLine = showGuideLine;
    }

    public void setGuideLineColor(@ColorInt int guideColor) {
        mGuideLineColor = guideColor;
        mGuidePaint.setColor(mGuideLineColor);
    }

    public void setGuideLineWidth(int guideLineWidth) {
        mGuideLineWidth = guideLineWidth;
        mGuidePaint.setStrokeWidth(mGuideLineWidth);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredWidth = getDefaultWidth();
        int desiredHeight = desiredWidth;
        int widthSpec = resolveSizeAndState(desiredWidth, widthMeasureSpec);
        int heightSpec = resolveSizeAndState(desiredHeight, heightMeasureSpec);
        setMeasuredDimension(widthSpec, heightSpec);

        mRootDiameter = Math.max(getMeasuredWidth(), getMeasuredHeight());

        final int count = getChildCount();
        // menu item 's size
        int childSize = (int) (mRootDiameter * mMenuRadioDimension);
        // menu item 's MeasureSpec
        int childMode = MeasureSpec.EXACTLY;

        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);

            if (child.getVisibility() == GONE) {
                continue;
            }

            int makeMeasureSpec = -1;

            if (child.getId() == R.id.id_wheel_menu_center_item) {
                makeMeasureSpec = MeasureSpec.makeMeasureSpec(
                        (int) (mRootDiameter * mCenterRadioDimension),
                        childMode);
            } else {
                makeMeasureSpec = MeasureSpec.makeMeasureSpec(childSize,
                        childMode);
            }
            child.measure(makeMeasureSpec, makeMeasureSpec);
        }
        mPadding = mPaddingRadio * mRootDiameter;
    }

    private int resolveSizeAndState(int desireSize, int measureSpec) {
        int result = desireSize;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (specMode) {
            case MeasureSpec.UNSPECIFIED:
                result = desireSize;
                break;
            case MeasureSpec.AT_MOST:
                if (specSize < desireSize) {
                    result = specSize;
                } else {
                    result = desireSize;
                }
                break;
            case MeasureSpec.EXACTLY:
                result = specSize;
                break;
        }
        return result;
    }

    public View getCenterItem() {
        return findViewById(R.id.id_wheel_menu_center_item);
    }


    /**
     * init the triangle path
     */
    private void initTriangle() {
        int layoutRadial = (int) (mRootDiameter / 2.0);
        mTrianglePath.moveTo(layoutRadial - mTriangleHeight, 0);
        mTrianglePath.lineTo(layoutRadial, 0 - mTriangleHeight / 2.0f);
        mTrianglePath.lineTo(layoutRadial, 0 + mTriangleHeight / 2.0f);
        mTrianglePath.close();
    }


    /**
     * @param mOnMenuItemClickListener
     */
    public void setOnMenuItemClickListener(
            OnMenuItemClickListener mOnMenuItemClickListener) {
        this.mOnMenuItemClickListener = mOnMenuItemClickListener;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int layoutDiameter = mRootDiameter;
        int layoutRadial = (int) (layoutDiameter / 2.0);

        final int childCount = getChildCount();

        int left, top;
        // size of menu item
        int cWidth = (int) (layoutDiameter * mMenuRadioDimension);

        float angleDelay;
        if (getCenterItem() != null) {
            angleDelay = 360 / (getChildCount() - 1);
        } else {
            angleDelay = 360 / (getChildCount());
        }
        //angle diff [0,360)
        double minimumAngleDiff = -1;
        double angleDiff;
        double includedAngle = 0;
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);

            if (child.getId() == R.id.id_wheel_menu_center_item) {
                continue;
            }
            if (child.getVisibility() == GONE) {
                continue;
            }

            mStartAngle %= 360;
            includedAngle = mStartAngle;
//            }
            //menu 's angle relative to 0°
            child.setTag(R.id.id_wheel_view_angle, mStartAngle);
            angleDiff = Math.abs(mSelectedAngle - includedAngle);
            angleDiff = angleDiff >= 180 ? 360 - angleDiff : angleDiff;
            //find the intentional selected item
            if (minimumAngleDiff == -1 || minimumAngleDiff > angleDiff) {
                minimumAngleDiff = angleDiff;
                mTempSelectedView = child;
                if (getCenterItem() != null) {
                    mTempSelectedPosition = i - 1;
                } else {
                    mTempSelectedPosition = i;
                }
                //allowable error
                mNeedSlotIntoCenter = ((int) minimumAngleDiff) != 0;
            }
            // 计算，中心点到menu item中心的距离
            float tmp = layoutRadial - cWidth / 2 - mPadding;

            // {tmp*cos(a)-1/2*width}即menu item相对中心点的横坐标
            left = layoutRadial
                    + (int) Math.round(tmp
                    * Math.cos(Math.toRadians(mStartAngle)) - 1 / 2f
                    * (cWidth));

            //{tmp*sin(a)-1/2*height}即menu item相对中心点的纵坐标
            top = layoutRadial
                    + (int) Math.round(tmp
                    * Math.sin(Math.toRadians(mStartAngle)) - 1 / 2f
                    * (cWidth));

            child.layout(left, top, left + cWidth, top + cWidth);
            float angel;
            switch (mItemRotateMode) {
                case ITEM_ROTATE_MODE_NONE:
                    angel = 0;
                    break;
                case ITEM_ROTATE_MODE_INWARD:
                    angel = (float) (-90 + mStartAngle);
                    break;
                case ITEM_ROTATE_MODE_OUTWARD:
                    angel = (float) (90 + mStartAngle);
                    break;
                default:
                    angel = 0;
                    break;
            }
            child.setPivotX(cWidth / 2.0f);
            child.setPivotY(cWidth / 2.0f);
            child.setRotation(angel);
            mStartAngle += angleDelay;

        }
        //layout center menu
        View cView = findViewById(R.id.id_wheel_menu_center_item);
        if (cView != null) {
            // 设置center item位置
            int cl = layoutRadial - cView.getMeasuredWidth() / 2;
            int cr = cl + cView.getMeasuredWidth();
            cView.layout(cl, cl, cr, cr);
        }
        if (mIsFirstLayout) {
            mIsFirstLayout = false;
//            scrollIntoSlots();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mBgMatrix.reset();
        initTriangle();
        int radial = (int) (mRootDiameter / 2.0f);
        mWheelBgPath.addCircle(0, 0, radial, Path.Direction.CW);
    }

    @Override
    public void requestLayout() {
        mIsFirstLayout = true;
        super.requestLayout();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //Draw Wheel's Background
        canvas.save();
        int radial = (int) (mRootDiameter / 2.0f);
        canvas.translate(radial, radial);
        if (mBgMatrix.isIdentity()) {
            canvas.getMatrix().invert(mBgMatrix);
        }
        canvas.drawPath(mWheelBgPath, mWheelPaint);
        canvas.restore();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        canvas.save();
        canvas.translate(mRootDiameter / 2f, mRootDiameter / 2f);
        canvas.rotate((float) (mSelectedAngle), 0, 0);
        canvas.drawPath(mTrianglePath, mCursorPaint);
        canvas.restore();
        if (mIsDebug || mShowGuideLine) {
            canvas.save();
            canvas.translate(mRootDiameter / 2f, mRootDiameter / 2f);
            canvas.drawCircle(0, 0, 10, mCursorPaint);
            float angleDelay;
            int startIndex;
            if (getCenterItem() != null) {
                angleDelay = 360 / (getChildCount() - 1);
                startIndex = 1;
            } else {
                angleDelay = 360 / (getChildCount());
                startIndex = 0;
            }

            if (mIsDebug) {

                for (int i = 0; i < 360; i += angleDelay) {
                    canvas.save();
                    canvas.rotate(i);
                    mCursorPaint.setTextAlign(Paint.Align.RIGHT);
                    mCursorPaint.setTextSize(28);
                    canvas.drawText(i + "°", mRootDiameter / 2.f, 0, mCursorPaint);
                    canvas.restore();
                }
            }
            canvas.restore();
            canvas.save();
            canvas.translate(mRootDiameter / 2f, mRootDiameter / 2f);
            View child = getChildAt(startIndex);
            int startAngel = (int) (((Double) child.getTag(R.id.id_wheel_view_angle) + angleDelay / 2.f) % 360);
            for (int i = startIndex; i < getChildCount(); i++) {
                canvas.save();
                canvas.rotate(startAngel);
                canvas.drawLine(0, 0, mRootDiameter / 2f, 0, mCursorPaint);
                mCursorPaint.setTextAlign(Paint.Align.RIGHT);
                mCursorPaint.setTextSize(38);
                startAngel += angleDelay;
                canvas.restore();
            }
            canvas.restore();
        }
        float angleDelay;
        int startIndex;
        if (getCenterItem() != null) {
            angleDelay = 360 / (getChildCount() - 1);
            startIndex = 1;
        } else {
            angleDelay = 360 / (getChildCount());
            startIndex = 0;
        }
        if (mShowGuideLine) {
            if (mGuideLineWidth > 0 && getChildCount() - startIndex == mMenuItemCount) {
                canvas.save();
                canvas.translate(mRootDiameter / 2f, mRootDiameter / 2f);
                View child = getChildAt(startIndex);
                if (child != null && child.getTag(R.id.id_wheel_view_angle) != null) {
                    int startAngel = (int) (((Double) child.getTag(R.id.id_wheel_view_angle) + angleDelay / 2.f) % 360);
                    for (int i = startIndex; i < getChildCount(); i++) {
                        canvas.save();
                        canvas.rotate(startAngel);
                        mGuidePath.reset();
                        mGuidePath.moveTo(0, 0);
                        mGuidePath.lineTo(mRootDiameter / 2.f, 0);
                        canvas.drawPath(mGuidePath, mGuidePaint);
                        startAngel += angleDelay;
                        canvas.restore();
                    }
                }
                canvas.restore();
            }
        }
    }

    public void setDebug(boolean debug) {
        mIsDebug = debug;
    }

    public void setAdapter(CycleWheelAdapter adapter) {
        if (adapter == null) {
            throw new IllegalArgumentException("Can not set a null adbapter to CursorWheelLayout!!!");
        }

        if (mWheelAdapter != null) {
            if (mWheelDataSetObserver != null) {
                mWheelAdapter.unregisterDataSetObserver(mWheelDataSetObserver);
            }
            removeAllViews();
            mWheelDataSetObserver = null;
        }
        mWheelAdapter = adapter;
        mWheelDataSetObserver = new WheelDataSetObserver();
        mWheelAdapter.registerDataSetObserver(mWheelDataSetObserver);
        addMenuItems();
    }

    public CycleWheelAdapter getAdapter() {
        return mWheelAdapter;
    }

    private void onDateSetChanged() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onDateSetChanged() called with: " + "");
        }
        removeAllViews();
        addMenuItems();
        mStartAngle = mSelectedAngle;
        mSelectedPosition = INVALID_POSITION;
        mTempSelectedPosition = INVALID_POSITION;
        mSelectedView = null;
        mTempSelectedView = null;
        mIsDraging = false;
        mIsFling = false;
        requestLayout();
    }


    /**
     * add menu item to this layout
     */
    RelativeLayout itemsView;

    private void addMenuItems() {
        if (mWheelAdapter == null || mWheelAdapter.getCount() == 0) {
            throw new IllegalArgumentException("Empty menu source!");
        }
        mMenuItemCount = mWheelAdapter.getCount();
        View view;
        for (int i = 0; i < mMenuItemCount; i++) {
            final int j = i;
            view = mWheelAdapter.getView(this, i);
            //it will be ignore the origin onClickListener
            view.setOnClickListener(new InnerClickListener(i));
            addView(view);
        }
    }

    /**
     * @param mPadding
     */
    public void setPadding(float mPadding) {
        this.mPadding = mPadding;
        invalidate();
    }

    /**
     * @return
     */
    private int getDefaultWidth() {
        WindowManager wm = (WindowManager) getContext().getSystemService(
                Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return Math.min(outMetrics.widthPixels, outMetrics.heightPixels);
    }

    /**
     * Interface definition for a callback to be invoked when a view is clicked.
     *
     * @author chensuilun
     */
    public interface OnMenuItemClickListener {

        void onItemClick(View view, int pos);

    }

    /**
     * @author chensuilun
     */
    private class InnerClickListener implements OnClickListener {
        private final int mPosition;

        public InnerClickListener(int position) {
            mPosition = position;
        }

        @Override
        public void onClick(View v) {
            if (mSelectedView == v || mTempSelectedView == v) {
                return;
            }
            mIsDraging = false;
            mIsFling = false;
            mTempSelectedPosition = mPosition;
            mTempSelectedView = v;
            mNeedSlotIntoCenter = true;
            if (mOnMenuItemClickListener != null) {
                mOnMenuItemClickListener.onItemClick(v, mPosition);
            }
        }
    }

    public int getSelectedPosition() {
        return mSelectedPosition;
    }

    /**
     * @param position
     */
    public void setSelection(int position) {
        setSelection(position, 1);
    }

    public int getItemRotateMode() {
        return mItemRotateMode;
    }

    public void setSelection(final int position, final int rounds) {
        RotateAnimation rotate = new RotateAnimation(0, 180 * 10, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(5000);
        rotate.setInterpolator(new OvershootInterpolator(0.5f));
        startAnimation(rotate);
    }


    @Override
    protected void onDetachedFromWindow() {
        removeAllViews();
        super.onDetachedFromWindow();
        mIsFirstLayout = false;
    }

    /**
     * to do whatever you want to perform the selected view
     *
     * @param v
     */
    protected void onInnerItemSelected(View v) {

    }

    /**
     * to do whatever you want to perform the unselected view
     *
     * @param v
     */
    protected void onInnerItemUnselected(View v) {

    }


    private WheelDataSetObserver mWheelDataSetObserver;

    /**
     * @author chensuilun
     */
    public class WheelDataSetObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            super.onChanged();
            onDateSetChanged();
        }
    }

    /**
     * An Adapter object acts as a bridge between an {@link CursorWheelLayout} and the
     * underlying data for that view. The Adapter provides access to the data items.
     * The Adapter is also responsible for making a {@link View} for
     * each item in the data set.
     *
     * @author chensuilun
     */
    public static abstract class CycleWheelAdapter {

        private final DataSetObservable mDataSetObservable = new DataSetObservable();

        public void registerDataSetObserver(DataSetObserver observer) {
            mDataSetObservable.registerObserver(observer);
        }

        public void unregisterDataSetObserver(DataSetObserver observer) {
            mDataSetObservable.unregisterObserver(observer);
        }

        /**
         * Notifies the attached observers that the underlying data has been changed
         * and any View reflecting the data set should refresh itself.
         */
        public void notifyDataSetChanged() {
            mDataSetObservable.notifyChanged();
        }

        /**
         * How many menu items are in the data set represented by this Adapter.
         *
         * @return Count of items.
         */
        public abstract int getCount();

        /**
         * Get a View that displays the data at the specified position in the data set.
         *
         * @param parent
         * @param position
         * @return
         */
        public abstract View getView(View parent, int position);

        /**
         * Get the data item associated with the specified position in the data set.
         *
         * @param position Position of the item whose data we want within the adapter's
         *                 data set.
         * @return The data at the specified position.
         */
        public abstract Object getItem(int position);
    }

}
