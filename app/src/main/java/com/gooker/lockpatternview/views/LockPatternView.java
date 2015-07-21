package com.gooker.lockpatternview.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.gooker.lockpatternview.domain.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO: document your custom view class.
 */
public class LockPatternView extends View {

    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    /**
     * 是否已经 初始化完毕
     */
    private boolean isInited = false;
    /**
     *
     */
    private int mScreenWidth;
    /**
     *
     */
    private int mScreenHeight;

    private int mPointNormal = Color.BLUE;
    private int mPointError = Color.RED;
    private int mPointPressed = Color.GREEN;
    private int mLineNormal = Color.DKGRAY;
    private int mLinePresses = Color.YELLOW;


    private float mMovingX;
    private float mMovingY;

    /**
     * 半径
     */
    private int mPointRadius = 60;
    private int mLineWidth = 30 ;

    private List<Point> mPonitChecked = new ArrayList<>();

    private Point[][] mPoints = new Point[3][3];
    private boolean movingNoPoint;
    private boolean isSelected;


    public LockPatternView(Context context) {
        this(context, null);
    }

    public LockPatternView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LockPatternView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        /**
         * 获取屏幕的宽高
         */
        post(new Runnable() {
            @Override
            public void run() {

                mScreenWidth = getWidth();
                mScreenHeight = getHeight();
                log("mScreenWidth" + mScreenWidth + "\tmScreenHeight" + mScreenHeight);

            }
        });

    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //开始画圆形
        if (!isInited) {
            //计算原型坐标
            initPoint(canvas);
        }


        draw2Point(canvas);

        draw2Line(canvas);
//        log("ondrawing");
    }

    private void draw2Line(Canvas canvas) {
        if (mPonitChecked.size() > 0) {
            Point a = mPonitChecked.get(0);
            for (int i = 0; i < mPonitChecked.size(); i++) {
                line2Canvas(canvas, a, mPonitChecked.get(i));
                a = mPonitChecked.get(i);
            }
            if (movingNoPoint) {
                mPaint.setColor(mLinePresses);
                mPaint.setStrokeWidth(mLineWidth);
                canvas.drawLine(a.x, a.y, mMovingX, mMovingY, mPaint);
            }
        }

    }

    private void line2Canvas(Canvas canvas, Point a, Point b) {
        mPaint.setColor(mLineNormal);
        mPaint.setStrokeWidth(mLineWidth);
        canvas.drawLine(a.x, a.y, b.x, b.y, mPaint);
    }

    private double getDistance(Point a, Point b) {
        return Math.sqrt(Math.pow((Math.abs(a.x - b.x)), 2) + Math.pow(Math.abs(a.y - b.y), 2));
    }

    private void draw2Point(Canvas canvas) {
        for (Point[] mPs : mPoints) {
            for (Point p : mPs) {
                if (p.status == Point.PointStatus.NORMAL) {
                    mPaint.setColor(mPointNormal);
                } else if (p.status == Point.PointStatus.ERROR) {
                    mPaint.setColor(mPointError);
                } else if (p.status == Point.PointStatus.PRESSED) {
                    mPaint.setColor(mPointPressed);
                }
                canvas.drawCircle(p.x, p.y, mPointRadius, mPaint);
            }
        }
    }

    private void initPoint(Canvas canvas) {
        int top, w;
        if (mScreenHeight > mScreenWidth) {
            top = (mScreenHeight - mScreenWidth) / 2;
            w = mScreenWidth / 3;
        } else {
            top = (mScreenWidth - mScreenHeight) / 2;
            w = mScreenHeight / 3;
        }
        mPoints[0][0] = new Point(top, w, Point.PointStatus.NORMAL);
        mPoints[0][1] = new Point(top, w * 2, Point.PointStatus.NORMAL);
        mPoints[0][2] = new Point(top, w * 3, Point.PointStatus.NORMAL);

        mPoints[1][0] = new Point(top + w, w, Point.PointStatus.NORMAL);
        mPoints[1][1] = new Point(top + w, w * 2, Point.PointStatus.NORMAL);
        mPoints[1][2] = new Point(top + w, w * 3, Point.PointStatus.NORMAL);

        mPoints[2][0] = new Point(top + w * 2, w, Point.PointStatus.NORMAL);
        mPoints[2][1] = new Point(top + w * 2, w * 2, Point.PointStatus.NORMAL);
        mPoints[2][2] = new Point(top + w * 2, w * 3, Point.PointStatus.NORMAL);
        mPaint.setColor(mPointNormal);
        for (int i = 0; i < mPoints.length; i++) {
            for (int j = 0; j < mPoints[i].length; j++) {
                canvas.drawCircle(mPoints[i][j].x, mPoints[i][j].y, mPointRadius, mPaint);
            }
        }

        isInited = true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Point lastPoint = null;
        mMovingX = event.getX();
        mMovingY = event.getY();
        movingNoPoint = false;
        boolean isMovingFinish = false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                resetDraw();
                lastPoint = getPoint();
                if (null != lastPoint) {
                    isSelected = true;
                }
                break;
            case MotionEvent.ACTION_MOVE:

                if (isSelected) {
                    lastPoint = getPoint();
                    if (null == lastPoint) {
                        movingNoPoint = true;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                isMovingFinish = true;
                isSelected = false;
                break;
        }

        if (!isMovingFinish && isSelected && null != lastPoint) {
            if (isInChecked(lastPoint)) {
                movingNoPoint = true;
            } else {
                lastPoint.status = Point.PointStatus.PRESSED;
                mPonitChecked.add(lastPoint);
            }
        }

        if (isMovingFinish) {
            //绘制完毕
            if (mPonitChecked.size() == 1) {
                mPonitChecked.clear();
            } else if (mPonitChecked.size() > 1 && mPonitChecked.size() < 5) {
                errorPoint();
                //绘制圆形太少
                mPonitChecked.clear();
                log("draw failure");
            } else {
                //绘制成功
                log("draw success");
            }

        }
        postInvalidate();

        return true;
    }


    private void errorPoint() {
        for (Point p : mPonitChecked) {
            p.status = Point.PointStatus.ERROR;
        }
    }

    private void resetDraw() {
        for (Point p : mPonitChecked) {
            p.status = Point.PointStatus.NORMAL;
        }
        mPonitChecked.clear();

    }

    private void drawLine(Point lastPoint, float x, MotionEvent event, float y) {

    }

    private Point getPoint() {
        Point point = null;
        boolean isIn = false;
        for (int i = 0; i < mPoints.length; i++) {
            for (int j = 0; j < mPoints[i].length; j++) {
                if (isPointInCircle(mPoints[i][j])) {
                    point = mPoints[i][j];
                    break;
                }
            }
        }
        return point;
    }

    private boolean isInChecked(Point p) {
        return mPonitChecked.contains(p);
    }


    private boolean isPointInCircle(Point point) {
        int space = (int) Math.sqrt(Math.pow(Math.abs(point.x - mMovingX), 2) + Math.pow(Math.abs(point.y - mMovingY), 2));
        return space > mPointRadius ? false : true;
    }


    private void log(String msg) {
        Log.e("[LockPatternView]", msg);
    }
}
