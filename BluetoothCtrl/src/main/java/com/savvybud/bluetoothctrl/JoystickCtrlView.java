package com.savvybud.bluetoothctrl;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.util.logging.Logger;

/**
 * TODO: document your custom view class.
 */
public class JoystickCtrlView extends View {
    private String mExampleString; // TODO: use a default from R.string...
    private int mExampleColor = Color.RED; // TODO: use a default from R.color...
    private float mExampleDimension = 0; // TODO: use a default from R.dimen...
    private Drawable mExampleDrawable;

    private Paint mDrawPaint;

    private final int paintColor = Color.BLACK;

    private Paint mTextPaint;

    private GestureDetector mDetector;
    Logger log = Logger.getAnonymousLogger();

    public JoystickCtrlView(Context context) {
        super(context);
        init(null, 0);
    }

    private void setupPaint() {
        mDrawPaint = new Paint();
        mDrawPaint.setColor(paintColor);
        mDrawPaint.setAntiAlias(true);
        mDrawPaint.setStrokeWidth(5);
        mDrawPaint.setStyle(Paint.Style.STROKE);
        mDrawPaint.setStrokeJoin(Paint.Join.ROUND);
        mDrawPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    public JoystickCtrlView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public JoystickCtrlView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.JoystickCtrlView, defStyle, 0);

        mExampleString = a.getString(
                R.styleable.JoystickCtrlView_exampleString);
        mExampleColor = a.getColor(
                R.styleable.JoystickCtrlView_exampleColor,
                mExampleColor);
        // Use getDimensionPixelSize or getDimensionPixelOffset when dealing with
        // values that should fall on pixel boundaries.
        mExampleDimension = a.getDimension(
                R.styleable.JoystickCtrlView_exampleDimension,
                mExampleDimension);

        if (a.hasValue(R.styleable.JoystickCtrlView_exampleDrawable)) {
            mExampleDrawable = a.getDrawable(
                    R.styleable.JoystickCtrlView_exampleDrawable);
            mExampleDrawable.setCallback(this);
        }

        a.recycle();

        // Set up a default TextPaint object
        mTextPaint = new TextPaint();
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.LEFT);

        setupPaint();

        // Update TextPaint and text measurements from attributes
        invalidateTextPaintAndMeasurements();
        mDetector = new GestureDetector(JoystickCtrlView.this.getContext(), new mListener());
    }


    private void invalidateTextPaintAndMeasurements() {
        mTextPaint.setTextSize(mExampleDimension);
        mTextPaint.setColor(mExampleColor);
        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    // Account for padding
        float xpad = (float)(getPaddingLeft() + getPaddingRight());
        float ypad = (float)(getPaddingTop() + getPaddingBottom());

        float ww = (float)w - xpad;
        float hh = (float)h - ypad;

        // Figure out how big we can make the circle.
        float diameter = Math.min(ww, hh);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Try for a width based on our minimum
        int minw = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth();
        int w = resolveSizeAndState(minw, widthMeasureSpec, 1);

        // Whatever the width ends up being, ask for a height that would let the pie
        // get as big as it can
        int minh = MeasureSpec.getSize(w) + getPaddingBottom() + getPaddingTop();
        int h = resolveSizeAndState(minh, heightMeasureSpec, 0);

        setMeasuredDimension(w, h);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        boolean result = mDetector.onTouchEvent(event);
        if (!result) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                result = true;
                touching = false;
                invalidate();
            }
        }
        if(touching){
            if(event.getAction() == MotionEvent.ACTION_MOVE){
                x = event.getX();
                y = event.getY();
                log.info("X: "+x+" Y: "+y);
                this.invalidate();
            }
        }
        return result;
    }

    float x;
    float y;
    boolean touching = false;

    class mListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            touching = true;
            x = e.getX();
            y = e.getY();
            return true;
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // TODO: consider storing these as member variables to reduce
        // allocations per draw cycle.
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();

        int contentWidth = getWidth() - paddingLeft - paddingRight;
        int contentHeight = getHeight() - paddingTop - paddingBottom;

        Rect r = canvas.getClipBounds();
        int h = r.height()-paddingTop-paddingBottom;
        int w = r.width()-paddingLeft-paddingRight;
        int radius = Math.min(h,w)/2;

        System.out.println("Rect: " + r.toShortString());
        mDrawPaint.setStrokeWidth(5);
        mDrawPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mDrawPaint.setColor(Color.CYAN);
        canvas.drawCircle(r.centerX(), r.centerY(), radius - 20, mDrawPaint);
        mDrawPaint.setColor(Color.BLACK);
        mDrawPaint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(r.centerX(), r.centerY(), radius - 20, mDrawPaint);

        //draw axes
        mDrawPaint.setColor(Color.BLUE);
        mDrawPaint.setStyle(Paint.Style.STROKE);
        mDrawPaint.setStrokeWidth(2);
        canvas.drawLine(r.centerX() - radius, r.centerY(), r.centerX() + radius, r.centerY(), mDrawPaint);
        canvas.drawLine(r.centerX(), r.centerY() - radius, r.centerX(), r.centerY() + radius, mDrawPaint);

        //draw center
        mDrawPaint.setStyle(Paint.Style.FILL);
        mDrawPaint.setColor(Color.parseColor("#CD5C5C"));
        canvas.drawCircle(r.centerX(), r.centerY(), 20, mDrawPaint);

        if (touching){
            mDrawPaint.setStrokeWidth(4);
            //mDrawPaint.setStyle(Paint.Style.STROKE);
            canvas.drawLine(r.centerX(), r.centerY(), x,y, mDrawPaint);
            canvas.drawCircle(x, y, 50, mDrawPaint);
        }

        /*
        // Draw the text.
        canvas.drawText(mExampleString,
                paddingLeft + (contentWidth - mTextWidth) / 2,
                paddingTop + (contentHeight + mTextHeight) / 2,
                mTextPaint);

        // Draw the example drawable on top of the text.
        if (mExampleDrawable != null) {
            mExampleDrawable.setBounds(paddingLeft, paddingTop,
                    paddingLeft + contentWidth, paddingTop + contentHeight);
            mExampleDrawable.draw(canvas);
        }*/
    }

    /**
     * Gets the example string attribute value.
     *
     * @return The example string attribute value.
     */
    public String getExampleString() {
        return mExampleString;
    }

    /**
     * Sets the view's example string attribute value. In the example view, this string
     * is the text to draw.
     *
     * @param exampleString The example string attribute value to use.
     */
    public void setExampleString(String exampleString) {
        mExampleString = exampleString;
        invalidateTextPaintAndMeasurements();
    }

    /**
     * Gets the example color attribute value.
     *
     * @return The example color attribute value.
     */
    public int getExampleColor() {
        return mExampleColor;
    }

    /**
     * Sets the view's example color attribute value. In the example view, this color
     * is the font color.
     *
     * @param exampleColor The example color attribute value to use.
     */
    public void setExampleColor(int exampleColor) {
        mExampleColor = exampleColor;
        invalidateTextPaintAndMeasurements();
    }

    /**
     * Gets the example dimension attribute value.
     *
     * @return The example dimension attribute value.
     */
    public float getExampleDimension() {
        return mExampleDimension;
    }

    /**
     * Sets the view's example dimension attribute value. In the example view, this dimension
     * is the font size.
     *
     * @param exampleDimension The example dimension attribute value to use.
     */
    public void setExampleDimension(float exampleDimension) {
        mExampleDimension = exampleDimension;
        invalidateTextPaintAndMeasurements();
    }

    /**
     * Gets the example drawable attribute value.
     *
     * @return The example drawable attribute value.
     */
    public Drawable getExampleDrawable() {
        return mExampleDrawable;
    }

    /**
     * Sets the view's example drawable attribute value. In the example view, this drawable is
     * drawn above the text.
     *
     * @param exampleDrawable The example drawable attribute value to use.
     */
    public void setExampleDrawable(Drawable exampleDrawable) {
        mExampleDrawable = exampleDrawable;
    }

}
