package com.llk.d.pagerecycler;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.llk.d.R;


public class CircleIndicator extends View {

    private Paint mFrantPain;
    private Paint mBgPaint;

    // Indicator的背景色画笔颜色
    private int mBgColor = Color.RED;

    // Indicator的前景色画笔颜色
    private int mForeColor = Color.BLUE;

    // Indicator数量
    private int mNumber;

    //Indicator半径
    private float mRadius = 10;

    // 移动的偏移量
    private float mOffset;

    //指示器间隔
    private float indicatorSpace = 3 * mRadius;

    // 指示器开始位置
    private float startOffset = 60;

    private int indicatorWidth = 100;
    private int indicatorHeight = 100;


    public CircleIndicator(Context context) {
        super(context);
    }

    public CircleIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CircleIndicator);
        indicatorSpace = typedArray.getDimension(R.styleable.CircleIndicator_circle_space, indicatorSpace);
        mRadius = typedArray.getDimension(R.styleable.CircleIndicator_circle_radius, mRadius);
        mBgColor = typedArray.getColor(R.styleable.CircleIndicator_circle_color_bg, mBgColor);
        mForeColor = typedArray.getColor(R.styleable.CircleIndicator_circle_color_selected, mForeColor);
        initPaint();
        typedArray.recycle();
    }

    private void initPaint() {
        mFrantPain = new Paint();
        mFrantPain.setAntiAlias(true);
        mFrantPain.setStyle(Paint.Style.FILL);
        mFrantPain.setColor(mForeColor);

        mBgPaint = new Paint();
        mBgPaint.setAntiAlias(true);
        mBgPaint.setStyle(Paint.Style.FILL);
        mBgPaint.setColor(mBgColor);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int i = 0; i < mNumber; i++) {
            canvas.drawCircle(startOffset + indicatorSpace * i, indicatorHeight / 2, mRadius, mBgPaint);
        }
        canvas.drawCircle(startOffset + mOffset, indicatorHeight / 2, mRadius, mFrantPain);

    }

    public void setOffset(int position) {
        if (mNumber == 0) return;
        position %= mNumber;
        mOffset = position * indicatorSpace;
        //重绘
        postInvalidate();
    }

    public void setNumber(int number) {
        mNumber = number;
        postInvalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        indicatorWidth = getMySize(500, widthMeasureSpec);
        indicatorHeight = getMySize(100, heightMeasureSpec);
        setMeasuredDimension(indicatorWidth, indicatorHeight);
//        startOffset = indicatorWidth / 2 - mRadius * mNumber - (indicatorSpace - 2 * mRadius) * (mNumber - 1) / 2 + mRadius;
        startOffset = indicatorWidth / 2 - indicatorSpace * (mNumber - 1) / 2;
    }

    private int getMySize(int defaultSize, int measureSpec) {
        int mySize = defaultSize;

        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);

        switch (mode) {
            case MeasureSpec.UNSPECIFIED: {//如果没有指定大小，就设置为默认大小
                mySize = defaultSize;
                break;
            }
            case MeasureSpec.AT_MOST: {//如果测量模式是最大取值为size
                //我们将大小取最大值,你也可以取其他值
                mySize = size;
                break;
            }
            case MeasureSpec.EXACTLY: {//如果是固定的大小，那就不要去改变它
                mySize = size;
                break;
            }
        }
        return mySize;
    }


}
