package com.example.mooreli.unlockdemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by MooreLi on 2017/1/19.
 */

public class LockView extends View {
    private final String TAG = getClass().getSimpleName();
    private Context mContext;
    private Paint mCirclePaint;
    private Paint mSelectCirclePaint;
    private Paint mLinePaint;

    private final int CIRCLE_WIDTH = 15;
    private final int LINE_WIDTH = 5;
    private int mBoxWidth;

    private int mCircleWidth, mLineWidth;

    private int mWidth, mHeight;

    private Circle[] mCircles = new Circle[9];
    private Rect[] mRects = new Rect[9];

    private int mDownX, mDownY;
    private int mMoveX, mMoveY;
    private List<Circle> mSelectCircles;
    private Path mPath;
    private int mLastMoveX, mLastMoveY;
    private boolean isDrawing = false;
    /**
     * 是否可以重复绘制一个点
     */
    private boolean isCanRepet = false;
    /**
     * 解锁回调
     */
    private UnLockCallback mCallback;
    /**
     * 震动
     */
    private Vibrator mVibrator;
    /**
     * 是否显示绘制路径
     */
    private boolean isShowPath = true;
    /**
     * 是否绘制正确
     */
    private boolean isMeasureTrue = false;

    public LockView(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public LockView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public LockView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }


    private void init() {
        mCircleWidth = dip2px(CIRCLE_WIDTH);
        mLineWidth = dip2px(LINE_WIDTH);

        mCirclePaint = new Paint();
        mCirclePaint.setColor(Color.parseColor("#CCCCCC"));

        mSelectCirclePaint = new Paint();
        mSelectCirclePaint.setColor(Color.parseColor("#5CACEE"));

        mLinePaint = new Paint();
        mLinePaint.setColor(Color.parseColor("#5CACEE"));
        mLinePaint.setStrokeWidth(mLineWidth);
        mLinePaint.setStyle(Paint.Style.STROKE);

        this.setBackgroundColor(Color.parseColor("#FDF5E6"));

        mVibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);

        mBoxWidth = dip2px(100);
    }

    /**
     * 初始化每个圆的位置
     */
    private void initCircles() {
        int unitWidth = mWidth / 3;
        int unitHeight = mHeight / 3;
        for (int i = 0; i < mRects.length; i++) {
            mRects[i] = new Rect((i % 3) * unitWidth, (i / 3) * unitHeight, (i % 3 + 1) * unitWidth, (i / 3 + 1) * unitHeight);
            mCircles[i] = new Circle();
            mCircles[i].setX(mRects[i].centerX());
            mCircles[i].setY(mRects[i].centerY());
            mCircles[i].setTag(i + 1);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mDownX = (int) event.getX();
            mDownY = (int) event.getY();
            isDrawing = true;
            return true;
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            mMoveX = (int) event.getX();
            mMoveY = (int) event.getY();
            handleMove(mMoveX, mMoveY);
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            handleUp();
        }
        return super.onTouchEvent(event);
    }

    /**
     * 处理手指滑动事件
     *
     * @param moveX
     * @param moveY
     */
    private void handleMove(int moveX, int moveY) {
        for (int i = 0; i < mCircles.length; i++) {
            Circle circle = mCircles[i];
            /**
             * 在某一个圆内
             */
            if (moveX >= circle.getX() - mCircleWidth
                    && moveX <= circle.getX() + mCircleWidth
                    && moveY >= circle.getY() - mCircleWidth
                    && moveY <= circle.getY() + mCircleWidth) {
                if (mSelectCircles == null) {
                    mSelectCircles = new ArrayList<>();
                }
                /**
                 * 不能重复绘制同一个点
                 */
                if (!isCanRepet) {
                    //如果不存在则添加
                    if (!isExist(circle)) {
                        mSelectCircles.add(circle);
                        mVibrator.vibrate(50);
                    }
                } else {
                    mSelectCircles.add(circle);
                    mVibrator.vibrate(50);
                }
                break;
            }
        }
        invalidate();
    }

    /**
     * 手指抬起，连接所有经过的点，0.5秒后清除
     */
    private void handleUp() {
        if (mPath != null) {
            isDrawing = false;
            invalidate();
            mUpHandler.sendEmptyMessageDelayed(1, 500);
            /**
             * 回调结果
             */
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mSelectCircles.size(); i++) {
                sb.append(mSelectCircles.get(i).getTag());
            }
            if (mCallback != null) {
                mCallback.lockFinish(mSelectCircles.size(), sb.toString());
            }
        }
    }

    /**
     * 重置
     */
    private void resetCanvas() {
        mSelectCircles.clear();
        mPath.reset();
        mLastMoveX = 0;
        mLastMoveY = 0;
        isMeasureTrue = false;
        invalidate();
    }

    /**
     * 检查某个点是否选择过
     *
     * @param circle
     * @return
     */
    private boolean isExist(Circle circle) {
        if (mSelectCircles == null || mSelectCircles.size() == 0) {
            return false;
        }
        boolean flag = false;
        for (int i = 0; i < mSelectCircles.size(); i++) {
            if (circle.getX() == mSelectCircles.get(i).getX() && circle.getY() == mSelectCircles.get(i).getY()) {
                flag = true;
                break;
            }
        }
        return flag;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int i = 0; i < mCircles.length; i++) {
            canvas.drawCircle(mCircles[i].getX(), mCircles[i].getY(), mCircleWidth, mCirclePaint);
        }
        /**
         * 如果显示绘制路径
         */
        if (isShowPath) {
            /**
             * 设置颜色
             */
            if(isMeasureTrue){
                mLinePaint.setColor(Color.parseColor("#5CACEE"));
                mSelectCirclePaint.setColor(Color.parseColor("#5CACEE"));
            }else{
                mLinePaint.setColor(Color.RED);
                mSelectCirclePaint.setColor(Color.RED);
            }
            if (null != mSelectCircles && mSelectCircles.size() != 0) {
                canvas.drawCircle(mSelectCircles.get(0).getX(), mSelectCircles.get(0).getY(), mCircleWidth + dip2px(2), mSelectCirclePaint);
                for (int i = 0; i < mSelectCircles.size() - 1; i++) {
                    canvas.drawLine(mSelectCircles.get(i).getX(), mSelectCircles.get(i).getY(), mSelectCircles.get(i + 1).getX(), mSelectCircles.get(i + 1).getY(), mLinePaint);
                    canvas.drawCircle(mSelectCircles.get(i + 1).getX(), mSelectCircles.get(i + 1).getY(), mCircleWidth + dip2px(2), mSelectCirclePaint);
                }
                if (isDrawing) {
                    Circle lastSelect = mSelectCircles.get(mSelectCircles.size() - 1);
                    if (mPath == null) {
                        mPath = new Path();
                        mPath.moveTo(lastSelect.getX(), lastSelect.getY());
                    } else {
                        if (mLastMoveX != 0 && mLastMoveY != 0) {
                            mPath.setLastPoint(lastSelect.getX(), lastSelect.getY());
                        } else {
                            mPath.moveTo(lastSelect.getX(), lastSelect.getY());
                        }
                        mPath.lineTo(mMoveX, mMoveY);
                    }

                    canvas.drawPath(mPath, mLinePaint);

                    mLastMoveX = mMoveX;
                    mLastMoveY = mMoveY;
                }
            }
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // TODO: 2017/1/19 padding和margin的设置影响
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthModel = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightModel = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (widthModel == MeasureSpec.EXACTLY) {
            mWidth = widthSize;
        } else {
            mWidth = mBoxWidth*3;
        }
        if (heightModel == MeasureSpec.EXACTLY) {
            mHeight = heightSize;
        } else {
//            mHeight = getScreenHeight() / 3 * 2;
            mHeight = mBoxWidth*3;
        }
        setMeasuredDimension(mWidth, mHeight);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        initCircles();
    }

    private Handler mUpHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                resetCanvas();
            }
        }
    };


    private int dip2px(int dip) {
        float scale = mContext.getResources().getDisplayMetrics().density;
        return (int) (scale * dip + 0.5f);
    }

    private int sp2px(int sp) {
        float scale = mContext.getResources().getDisplayMetrics().densityDpi;
        return (int) (scale * sp + 0.5f);
    }

    private int getScreenWidth() {
        WindowManager manager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(metrics);
        return metrics.widthPixels;
    }

    private int getScreenHeight() {
        WindowManager manager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(metrics);
        return metrics.heightPixels;
    }

    class Circle {
        int x;
        int y;
        int tag;

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        public int getTag() {
            return tag;
        }

        public void setTag(int tag) {
            this.tag = tag;
        }
    }

    public interface UnLockCallback {
        void lockFinish(int length, String psw);
    }

    public void setOnUnLockListener(UnLockCallback listener) {
        this.mCallback = listener;
    }

    public void setIsShowPath(boolean isShowPath){
        this.isShowPath = isShowPath;
        invalidate();
    }

    public void setIsMeasureTrue(boolean isMeasureTrue){
        this.isMeasureTrue = isMeasureTrue;
        invalidate();
    }

}
