package moe.codeest.enviews;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AnticipateInterpolator;

import com.shuyu.gsyvideoplayer.R;

/**
 * Created by codeest on 16/11/7.
 *
 */

public class ENPlayView extends View {

    public static int STATE_PLAY = 0;

    public static int STATE_PAUSE = 1;

    public static int DEFAULT_LINE_COLOR = Color.WHITE;

    public static int DEFAULT_BG_LINE_COLOR = 0xfffafafa;

    public static int DEFAULT_LINE_WIDTH = 14;

    public static int DEFAULT_BG_LINE_WIDTH = 12;

    public static int DEFAULT_DURATION = 1200;

    private int mCurrentState = STATE_PAUSE;

    private Paint mPaint, mBgPaint;

    private int mWidth, mHeight;

    private int mCenterX, mCenterY;

    private int mCircleRadius;

    private RectF mRectF, mBgRectF;

    private float mFraction = 1;

    private Path mPath, mDstPath;

    private PathMeasure mPathMeasure;

    private float mPathLength;

    private int mDuration;

    public ENPlayView(Context context) {
        super(context);
    }

    public ENPlayView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.play);
        int lineColor = ta.getColor(R.styleable.play_play_line_color, DEFAULT_LINE_COLOR);
        int bgLineColor = ta.getColor(R.styleable.play_play_bg_line_color, DEFAULT_BG_LINE_COLOR);
        int lineWidth = ta.getInteger(R.styleable.play_play_line_width, DEFAULT_LINE_WIDTH);
        int bgLineWidth = ta.getInteger(R.styleable.play_play_bg_line_width, DEFAULT_BG_LINE_WIDTH);
        ta.recycle();

        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setColor(lineColor);
        mPaint.setStrokeWidth(lineWidth);

        mBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBgPaint.setStyle(Paint.Style.STROKE);
        mBgPaint.setStrokeCap(Paint.Cap.ROUND);
        mBgPaint.setColor(bgLineColor);
        mBgPaint.setStrokeWidth(bgLineWidth);

        mPath = new Path();
        mDstPath = new Path();
        mPathMeasure = new PathMeasure();

        mDuration = DEFAULT_DURATION;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w * 9 / 10;
        mHeight = h * 9 / 10;
        mCircleRadius = mWidth / 10;
        mCenterX = w / 2;
        mCenterY = h / 2;
        mRectF = new RectF(mCenterX - mCircleRadius, mCenterY + 0.6f * mCircleRadius,
                mCenterX + mCircleRadius, mCenterY + 2.6f * mCircleRadius);
        mBgRectF = new RectF(mCenterX - mWidth / 2 ,mCenterY - mHeight / 2 ,mCenterX + mWidth / 2, mCenterY + mHeight / 2);
        mPath.moveTo(mCenterX - mCircleRadius, mCenterY + 1.8f * mCircleRadius);
        mPath.lineTo(mCenterX - mCircleRadius, mCenterY - 1.8f * mCircleRadius);
        mPath.lineTo(mCenterX + mCircleRadius, mCenterY);
        mPath.close();
        mPathMeasure.setPath(mPath, false);
        mPathLength = mPathMeasure.getLength();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(mCenterX, mCenterY, mWidth / 2, mBgPaint);
        if (mFraction < 0) {    //嗷~~ 弹性部分
            canvas.drawLine(mCenterX + mCircleRadius, mCenterY - 1.6f * mCircleRadius + 10 * mCircleRadius * mFraction,
                    mCenterX + mCircleRadius, mCenterY + 1.6f * mCircleRadius + 10 * mCircleRadius * mFraction, mPaint);

            canvas.drawLine(mCenterX - mCircleRadius, mCenterY - 1.6f * mCircleRadius,
                    mCenterX - mCircleRadius, mCenterY + 1.6f * mCircleRadius, mPaint);

            canvas.drawArc(mBgRectF, -105 , 360 , false, mPaint);
        } else if (mFraction <= 0.3) {  //嗷~~ 右侧直线和下方曲线
            canvas.drawLine(mCenterX + mCircleRadius, mCenterY - 1.6f * mCircleRadius + mCircleRadius * 3.2f / 0.3f * mFraction,
                    mCenterX + mCircleRadius, mCenterY + 1.6f * mCircleRadius, mPaint);

            canvas.drawLine(mCenterX - mCircleRadius, mCenterY - 1.6f * mCircleRadius,
                    mCenterX - mCircleRadius, mCenterY + 1.6f * mCircleRadius, mPaint);

            canvas.drawArc(mRectF, 0f, 180f / 0.3f * mFraction, false, mPaint);

            canvas.drawArc(mBgRectF, -105 + 360 * mFraction, 360 * (1 - mFraction), false, mPaint);
        } else if (mFraction <= 0.6) {  //嗷~~ 下方曲线和三角形
            canvas.drawArc(mRectF, 180f / 0.3f * (mFraction - 0.3f), 180 - 180f / 0.3f * (mFraction - 0.3f), false , mPaint);

            mDstPath.reset();
            //mDstPath.lineTo(0 ,0);
            mPathMeasure.getSegment(0.02f * mPathLength, 0.38f * mPathLength + 0.42f * mPathLength / 0.3f * (mFraction - 0.3f) ,
                    mDstPath, true);
            canvas.drawPath(mDstPath, mPaint);

            canvas.drawArc(mBgRectF, -105 + 360 * mFraction, 360 * (1 - mFraction), false, mPaint);
        } else if (mFraction <= 0.8) {  //嗷~~ 三角形
            mDstPath.reset();
            //mDstPath.lineTo(0 ,0);
            mPathMeasure.getSegment(0.02f * mPathLength + 0.2f * mPathLength / 0.2f * (mFraction - 0.6f)
                    , 0.8f * mPathLength + 0.2f * mPathLength / 0.2f * (mFraction - 0.6f) ,
                    mDstPath, true);
            canvas.drawPath(mDstPath, mPaint);

            canvas.drawArc(mBgRectF, -105 + 360 * mFraction, 360 * (1 - mFraction), false, mPaint);
        } else {    //嗷~~ 弹性部分
            mDstPath.reset();
            //mDstPath.lineTo(0 ,0);
            mPathMeasure.getSegment(10 * mCircleRadius * (mFraction - 1)
                    , mPathLength ,
                    mDstPath, true);
            canvas.drawPath(mDstPath, mPaint);
        }
    }

    public void play() {
        if (mCurrentState == STATE_PLAY) {
            return;
        }
        mCurrentState = STATE_PLAY;
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(1.f, 100.f);
        valueAnimator.setDuration(mDuration);
        valueAnimator.setInterpolator(new AnticipateInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mFraction = 1 - valueAnimator.getAnimatedFraction();
                invalidate();
            }
        });
        if (!valueAnimator.isRunning()) {
            valueAnimator.start();
        }
    }

    public void pause() {
        if (mCurrentState == STATE_PAUSE) {
            return;
        }
        mCurrentState = STATE_PAUSE;
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(1.f, 100.f);
        valueAnimator.setDuration(mDuration);
        valueAnimator.setInterpolator(new AnticipateInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mFraction = valueAnimator.getAnimatedFraction();
                invalidate();
            }
        });
        if (!valueAnimator.isRunning()) {
            valueAnimator.start();
        }
    }

    public int getCurrentState() {
        return mCurrentState;
    }

    public void setDuration(int duration) {
        mDuration = duration;
    }
}
