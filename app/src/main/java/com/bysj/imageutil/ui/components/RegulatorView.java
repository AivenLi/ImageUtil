package com.bysj.imageutil.ui.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.RequiresApi;

public class RegulatorView extends View {

    private final static int BTN_RADIUS=20;//拖动按钮的半径
    private final static int BTN_CIRCLE_RADIUS=6;//拖动按钮的圆心半径
    private final static int BAR_HEIGHT=6;//进度条的高度
    private String barColor="#a82894ff";
    private String circleColor="#902894ff";
    private String txtColor="#ffffffff";
    private float currentValue=50;//当前值
    private float maxValue=100;//最大值
    private float minValue=0;//最小值
    private boolean isShowText=false;//是否显示文字提示
    private boolean isCanAdjust=false;//是否可以调节
    /**
     * 该标志位主要作用如下：
     *     设置其为true，表示每当值发生变化时都会回调，
     *     设置其为false，只有当值变化过程结束后再回调。
     */
    private boolean changingCallback = true;
    /**
     * 触摸（或点击）事件是否结束
     */
    private boolean touchEnd;

    private Paint mPaint;
    private Rect mBound;
    public RegulatorView(Context context) {
        super(context);
        init();
    }

    public RegulatorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RegulatorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public RegulatorView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init(){
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(32);
        mBound = new Rect();//用于测量文字的宽度，以便准确无误地显示文字内容
        mPaint.getTextBounds(maxValue+"",0,(maxValue+"").length(),mBound);
    }

    private float preX;
    private boolean isMoveEvent=false;//用于判断当前事件是否为滑动事件
    private boolean isDownInBtn=false;//用于判断是不是点击在滑动按钮上
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(!isCanAdjust) return true;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchEnd = false;
                preX=event.getX();
                isMoveEvent=false;
                isDownInBtn=isDownInBtn(preX);
                break;
            case MotionEvent.ACTION_MOVE:
                touchEnd = false;
                int disX=(int) (event.getX() - preX);
                if(Math.abs(disX)>3) isMoveEvent=true;
                preX=event.getX();
                if(isDownInBtn){//响应移动事件
                    //计算当前值时，先计算出占比值再加上最小值，这样可以兼容负值计算
                    setCurrentValue((preX-getPaddingLeft()-BTN_RADIUS)/getBarWidth()*(maxValue-minValue)+minValue);
                }
                break;
            case MotionEvent.ACTION_UP:
                touchEnd = true;
                preX=event.getX();
//                if(!isMoveEvent){//响应点击事件
//                    if(!isDownInBtn){
//                        //计算当前值时，先计算出占比值再加上最小值，这样可以兼容负值计算
//
//                        setCurrentValue((preX-getPaddingLeft()-BTN_RADIUS)/getBarWidth()*(maxValue-minValue)+minValue);
//                    }
//                }
                setCurrentValue((preX-getPaddingLeft()-BTN_RADIUS)/getBarWidth()*(maxValue-minValue)+minValue);
                break;
            default:
                break;
        }
        return true;
    }

    private boolean isDownInBtn(float x){
        float left=getBarWidth()*(currentValue-minValue)/(maxValue-minValue)+getPaddingLeft();
        float right=getBarWidth()*(currentValue-minValue)/(maxValue-minValue)+getPaddingLeft()+BTN_RADIUS*2;
        if(x>=left&&x<=right) return true;
        return false;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
//        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
//        int width;
        int height ;
        if (heightMode == MeasureSpec.EXACTLY){
            height = heightSize;
        } else {
            height = getPaddingTop() + BTN_RADIUS*2 + getPaddingBottom();
        }
        setMeasuredDimension(widthSize, height);
    }


    @Override
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    protected void onDraw(Canvas canvas) {
        drawBg(canvas);//绘制整体背景
        drawBar(canvas);//绘制当前值的占比条
        drawBtn(canvas);//绘制当前值得调节按钮
        drawTxt(canvas);//绘制文本值（显示最大最小值及当前值）
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void drawBg(Canvas canvas){
        mPaint.setColor(Color.WHITE);
        float left=getPaddingLeft()+BTN_RADIUS;
        float top=(getMeasuredHeight()-BAR_HEIGHT)/2.0f;
        float right= getBarWidth()+left;
        float bottom=top+BAR_HEIGHT;
        canvas.drawRoundRect(left,top,right,bottom,2,2,mPaint);
//        canvas.drawRect(left,top,right,bottom,mPaint);
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void drawBar(Canvas canvas){
        mPaint.setColor(Color.parseColor(barColor));
        float left=getPaddingLeft()+BTN_RADIUS;
        float top=(getMeasuredHeight()-BAR_HEIGHT)/2;
        float right;
        if(maxValue!=minValue)//处理最大和最小值相等的情况
            right=getBarWidth()*(currentValue-minValue)/(maxValue-minValue)+left;
        else right= getBarWidth()+left;
        float bottom=top+BAR_HEIGHT;
        canvas.drawRoundRect(left,top,right,bottom,2,2,mPaint);
//        canvas.drawRect(left,top,right,bottom,mPaint);
    }

    private void drawBtn(Canvas canvas){
        mPaint.setColor(Color.parseColor(circleColor));
        float cx;
        if(maxValue!=minValue)//处理最大和最小值相等的情况
            cx=getBarWidth()*(currentValue-minValue)/(maxValue-minValue)+getPaddingLeft()+BTN_RADIUS;
        else cx=getBarWidth()+getPaddingLeft()+BTN_RADIUS;
        float cy=getMeasuredHeight()/2;
        canvas.drawCircle(cx,cy,BTN_RADIUS,mPaint);
        mPaint.setColor(Color.parseColor(barColor));
        canvas.drawCircle(cx,cy,BTN_CIRCLE_RADIUS,mPaint);
    }

    private void drawTxt(Canvas canvas){
        if(!isShowText) return;
        mPaint.setColor(Color.parseColor(txtColor));
        float x=getPaddingLeft()+BTN_RADIUS;
        float y=mPaint.getTextSize();
        //canvas.drawText(minValue+"",x,y,mPaint);
        float textWidth = mBound.width();
        x=getWidth()-getPaddingRight()-BTN_RADIUS-textWidth;
        //canvas.drawText(maxValue+"",x,y,mPaint);
        if(maxValue!=minValue)//处理最大和最小值相等的情况
            x=getBarWidth()*(currentValue-minValue)/(maxValue-minValue)+getPaddingLeft()+BTN_RADIUS*2;
        else x=getBarWidth()+getPaddingLeft()+BTN_RADIUS*2;
        canvas.drawText(currentValue+"",x,y,mPaint);
    }

    private int getBarWidth(){
        return getMeasuredWidth()-getPaddingLeft()-getPaddingRight()-BTN_RADIUS*2;
    }

    public float getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(float currentValue) {

        this.currentValue = Math.min(currentValue, maxValue);
        this.currentValue = Math.max(this.currentValue, minValue);
        this.currentValue = Math.round(this.currentValue);
        if ( changingCallback ) {

            if( onValueChangeListener != null ) {

                onValueChangeListener.onValueChange(this.currentValue);
            }
        } else {

            if ( touchEnd ) {

                if( onValueChangeListener != null ) {

                    onValueChangeListener.onValueChange(this.currentValue);
                }
            }
        }
        invalidate();
    }

    public void setCurrentValue(float currentValue, boolean callback) {

        this.currentValue = Math.min(currentValue, maxValue);
        this.currentValue = Math.max(this.currentValue, minValue);
        this.currentValue = Math.round(this.currentValue);
        if ( callback ) {

            if ( onValueChangeListener != null ) {

                onValueChangeListener.onValueChange(this.currentValue);
            }
        }
        invalidate();
    }

    public boolean isChangingCallback() {

        return changingCallback;
    }

    public void setChangingCallback(boolean changingCallback) {

        this.changingCallback = changingCallback;
    }

    public float getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(float maxValue) {
        this.maxValue = maxValue<minValue?minValue:maxValue;
        setCurrentValue(currentValue);
        mPaint.getTextBounds(this.maxValue+"",0,(this.maxValue+"").length(),mBound);
    }

    public float getMinValue() {
        return minValue;
    }

    public void setMinValue(float minValue) {
        this.minValue = minValue>maxValue?maxValue:minValue;
        setCurrentValue(currentValue);
    }

    public boolean isShowText() {
        return isShowText;
    }

    public void setShowText(boolean showText) {
        isShowText = showText;
    }

    public boolean isCanAdjust() {
        return isCanAdjust;
    }

    public void setCanAdjust(boolean canAdjust) {
        isCanAdjust = canAdjust;
    }

    private OnValueChangeListener onValueChangeListener=null;
    public OnValueChangeListener getOnValueChangeListener() {
        return onValueChangeListener;
    }

    public void setOnValueChangeListener(OnValueChangeListener onValueChangeListener) {
        this.onValueChangeListener = onValueChangeListener;
    }


    public interface OnValueChangeListener{
        void onValueChange(float value);
    }
}
