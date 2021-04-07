package com.bysj.imageutil.ui.components;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

/**
 * 自定义图片控件类，可缩放。
 */

public class ZoomImageView extends ImageView implements ViewTreeObserver.OnGlobalLayoutListener, ScaleGestureDetector.OnScaleGestureListener, View.OnTouchListener {


    private boolean mIsFirst = true;

    private float mInitScale;//初始化的缩放值
    private float mMidScale;//双击到达的缩放值
    private float mMaxScale;//最大的缩放值

    private Matrix mScaleMatrix;

    private ScaleGestureDetector mScaleGestureDetector;

    private int mLastPointCount;//上一次多点触控的数量
    private float mLastX, mLastY;//上一次所有触摸点的中心点
    private int mTouchSlop;//作为比较判断是否为move的一个系统的参照值
    private boolean canDrag;//标识是否可以移动
    //是否需要进行左右、上下边界的检测
    private boolean shouldCheckTopAndBottom;
    private boolean shouldCheckLeftAndRight;

    public ZoomImageView(Context context) {
        this(context, null);
    }

    public ZoomImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ZoomImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mScaleMatrix = new Matrix();

        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        super.setScaleType(ScaleType.MATRIX);
        mScaleGestureDetector = new ScaleGestureDetector(context, this);
        setOnTouchListener(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getViewTreeObserver().removeGlobalOnLayoutListener(this);
    }

    /**
     * 获取ImageView加载完成的图片
     * 初始化位置和大小
     */
    @Override
    public void onGlobalLayout() {
        if (mIsFirst) {


            //得到控件的宽和高
            int width = getWidth();
            int height = getHeight();

            //获取图片，以及他的宽和高
            Drawable d = getDrawable();
            if (d == null) {
                return;
            }
            int dw = d.getIntrinsicWidth();
            int dh = d.getIntrinsicHeight();

            float scale = 1.0f;
            if (dw > width && dh < height) {
                scale = width * 1.0f / dw;
            }
            if (dw < width && dh > height) {
                scale = height * 1.0f / dh;
            }
            if ((dw > width && dh > height) || (dw > width && dh > height)) {
                scale = Math.min(width * 1.0f / dw, height * 1.0f / dh);
            }

            //得到初始化缩放比例
            mInitScale = scale;
            mMaxScale = mInitScale * 4;
            mMidScale = mInitScale * 2;

            //将图片移动到屏幕中心
            int dx = width / 2 - dw / 2;
            int dy = height / 2 - dh / 2;

            mScaleMatrix.postTranslate(dx, dy);
            mScaleMatrix.postScale(mInitScale, mInitScale, width / 2, height / 2);
            setImageMatrix(mScaleMatrix);

            mIsFirst = false;
        }

    }

    /**
     * 获取当前的缩放值
     *
     * @return
     */
    public float getScale() {
        float[] values = new float[9];
        mScaleMatrix.getValues(values);
        return values[Matrix.MSCALE_X];
    }

    /**
     * 在缩放是检查图片左右边界是否不再屏幕边界
     */
    private void checkBorderAndCenter() {
        RectF rectF = getMatrixRectF();
        float deltaX = 0;
        float deltaY = 0;

        int width = getWidth();
        int height = getHeight();

        //缩放时进行边界检测，防止出现白边
        if (rectF.width() > width) {
            if (rectF.left > 0) {
                deltaX -= rectF.left;
            }
            if (rectF.right < width) {
                deltaX += width - rectF.right;
            }
        }

        if (rectF.height() > height) {
            if (rectF.top > 0) {
                deltaY -= rectF.top;
            }
            if (rectF.bottom < height) {
                deltaY += height - rectF.bottom;
            }
        }

        //如果图片的高度或者宽度小于控件的高或者宽，则让其居中
        if (rectF.width() < width) {
            deltaX = width / 2 - rectF.right + rectF.width() / 2f;
        }
        if (rectF.height() < height) {
            deltaY = height / 2 - rectF.bottom + rectF.height() / 2f;
        }
        mScaleMatrix.postTranslate(deltaX, deltaY);
    }

    /**
     * 获取图片的l,r,t,b，以及宽高
     *
     * @return
     */
    private RectF getMatrixRectF() {
        Matrix matrix = mScaleMatrix;
        RectF rectF = new RectF();
        Drawable d = getDrawable();
        if (d != null) {
            rectF.set(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
            matrix.mapRect(rectF);
        }
        return rectF;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {

        float scale = getScale();
        float scaleFactor = detector.getScaleFactor();

        if (getDrawable() == null) {
            return true;
        }
        //缩放范围的控制
        if ((scale < mMaxScale && scaleFactor > 1.0f) || (scale > mInitScale && scaleFactor < 1.0f)) {
            //缩小后也不能小于最小值
            if (scale * scaleFactor < mInitScale) {
                scaleFactor = mInitScale / scale;
            }
            if (scale * scaleFactor > mMaxScale) {
                scaleFactor = mMaxScale / scale;
            }
            //缩放
            mScaleMatrix.postScale(scaleFactor, scaleFactor, detector.getFocusX(), detector.getFocusY());
            checkBorderAndCenter();
            setImageMatrix(mScaleMatrix);

        }

        return true;//保证事件能够继续
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        //让detector来处理
        mScaleGestureDetector.onTouchEvent(event);

        float x = 0;
        float y = 0;

        int pointCount = event.getPointerCount();
        for (int i = 0; i < pointCount; i++) {
            x += event.getX(i);
            y += event.getY(i);

        }
        x /= pointCount;
        y /= pointCount;

        if (mLastPointCount != pointCount) {
            canDrag = false;
            mLastX = x;
            mLastY = y;
        }
        mLastPointCount = pointCount;

        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                //与上一次中心点的位移差
                float dx = x - mLastX;
                float dy = y - mLastY;

                if (!canDrag) {
                    canDrag = isMoveAction(dx, dy);
                }
                if (canDrag) {
                    RectF rectF = getMatrixRectF();
                    if (getDrawable() != null) {
                        shouldCheckTopAndBottom = shouldCheckLeftAndRight = true;
                        //宽度小于控件宽度，不允许横向移动
                        if (rectF.width() < getWidth()) {
                            shouldCheckLeftAndRight = false;
                            dx = 0;
                        }
                        if (rectF.height() < getHeight()) {
                            shouldCheckTopAndBottom = false;
                            dy = 0;
                        }

                        mScaleMatrix.postTranslate(dx, dy);
                        checkBorderWhenTranslate();
                        setImageMatrix(mScaleMatrix);
                    }
                }
                mLastX = x;
                mLastY = y;

                break;
            case MotionEvent.ACTION_UP:
                mLastPointCount = 0;
                break;
            case MotionEvent.ACTION_CANCEL:
                mLastPointCount = 0;
                break;
        }
        return true;
    }

    /**
     * 在移动时进行边界检查
     */
    private void checkBorderWhenTranslate() {
        RectF rectF = getMatrixRectF();
        float deltaX = 0;
        float deltaY = 0;

        if (shouldCheckTopAndBottom) {
            if (rectF.top > 0) {
                deltaY = -rectF.top;
            }

            if (rectF.bottom < getHeight()) {
                deltaY = getHeight() - rectF.bottom;

            }
        }
        if (shouldCheckLeftAndRight) {
            if (rectF.left > 0) {
                //Log.d("TAG", "left = " + getLeft());
                deltaX = -rectF.left;
            }
            if (rectF.right < getWidth()) {
                deltaX = getWidth() - rectF.right;
            }
        }

        mScaleMatrix.postTranslate(deltaX, deltaY);

    }

    /**
     * 判断是否足以出发move
     *
     * @param dx
     * @param dy
     * @return
     */
    private boolean isMoveAction(float dx, float dy) {
        //判断前后两次的触摸点中心的距离是否达到move标准的最小值
        return Math.sqrt(dx * dx + dy * dy) > mTouchSlop;
    }
}
