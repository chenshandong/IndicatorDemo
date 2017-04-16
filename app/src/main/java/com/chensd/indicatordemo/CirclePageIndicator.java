package com.chensd.indicatordemo;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class CirclePageIndicator extends View {
    private static final int INVALID_POINTER = -1;

    private float mRadius;
    //    private final Paint mPaintPageFill = new Paint(ANTI_ALIAS_FLAG);
    private final Paint mPaintStroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mPaintFill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mPaintLine = new Paint(Paint.ANTI_ALIAS_FLAG);
    private ViewPager mViewPager;
    private ViewPager.OnPageChangeListener mListener;
    private int mCurrentPage;
    private int mSnapPage;
    private float mPageOffset;
    private int mScrollState;
    private int mOrientation;
    private boolean mCentered;
    private boolean mSnap;

    private float cicleSpace;
    private int HORIZONTAL = 0;
    private int mMaxPage = 20;

    private List<Rect> pointRects = new ArrayList<>(); //用于保存圆点的区域

    public CirclePageIndicator(Context context) {
        this(context, null);
    }

    public CirclePageIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CirclePageIndicator(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (isInEditMode()) return;

        final Resources res = getResources();
        final int defaultFillColor = Color.parseColor("#7F7F7F");
        final int defaultStrokeColor = Color.parseColor("#D0D0D0");
        final float defaultRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, res.getDisplayMetrics());
        mCentered = true;

        mPaintStroke.setStyle(Paint.Style.FILL);
        mPaintStroke.setColor(defaultStrokeColor);
        mPaintFill.setStyle(Paint.Style.FILL);
        mPaintFill.setColor(defaultFillColor);
        mRadius = defaultRadius;

        cicleSpace = mRadius * 6;

        //增加进度线的模式
        mPaintLine.setStyle(Paint.Style.STROKE);
        mPaintLine.setColor(defaultStrokeColor);
        mPaintLine.setStrokeWidth(mRadius / 4);

    }


    public void setCentered(boolean centered) {
        mCentered = centered;
        invalidate();
    }

    public boolean isCentered() {
        return mCentered;
    }

    public void setDefaultColor(int strokeColor) {
        mPaintStroke.setColor(strokeColor);
        invalidate();
    }

    public void setFillColor(int fillColor) {
        mPaintFill.setColor(fillColor);
        invalidate();
    }

    public void setRadius(float radius) {
        mRadius = radius;
        cicleSpace = radius * 6;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mViewPager == null) {
            return;
        }
        final int count = mViewPager.getAdapter().getCount();
        if (count == 0) {
            return;
        }

        if (mCurrentPage >= count) {
            setCurrentItem(count - 1);
            return;
        }

        int longSize;
        int longPaddingBefore;
        int longPaddingAfter;
        int shortPaddingBefore;
        if (mOrientation == HORIZONTAL) {
            longSize = getWidth();
            longPaddingBefore = getPaddingLeft();
            longPaddingAfter = getPaddingRight();
            shortPaddingBefore = getPaddingTop();
        } else {
            longSize = getHeight();
            longPaddingBefore = getPaddingTop();
            longPaddingAfter = getPaddingBottom();
            shortPaddingBefore = getPaddingLeft();
        }

        float bigR = mRadius + 3;
        final float shortOffset = shortPaddingBefore + 2 * bigR; //距离控件顶部距离
        float longOffset = longPaddingBefore + mRadius;

        boolean isTooLong = false; // 是否太长超出屏幕
        if (mCentered) {
//            longOffset += ((longSize - longPaddingBefore - longPaddingAfter) / 2.0f) - ((count * cicleSpace) / 2.0f);
            longOffset += ((longSize - longPaddingBefore - longPaddingAfter) - (2 * bigR + (count - 1) * cicleSpace)) / 2;
//            Log.e("tag", "longOffset: " + longOffset);
            if (longOffset < 0) {
                isTooLong = true;
                Log.e("tag", "isTooLong: " + isTooLong);
                longOffset = longPaddingBefore + bigR;
            }
        }

        float dX;
        float dY;

        //Draw stroked circles
        pointRects.clear();
        for (int iLoop = 0; iLoop < count; iLoop++) {
            float drawLong = 0;
            if (isTooLong) {
                drawLong = longOffset + iLoop * ((getWidth() - longPaddingBefore - longPaddingAfter) / count);
            } else {
                drawLong = longOffset + (iLoop * cicleSpace);
            }
            if (mOrientation == HORIZONTAL) {
                dX = drawLong;
                dY = shortOffset;
            } else {
                dX = shortOffset;
                dY = drawLong;
            }
            // Only paint fill if not completely transparent
            Rect rect = null;
            if (getWidth() - longPaddingBefore - longPaddingAfter < bigR * count) {
                Log.e("tag", "length: " + (getWidth() - longPaddingBefore - longPaddingAfter) / count);
                rect = new Rect((int) (dX - (getWidth() - longPaddingBefore - longPaddingAfter) / count), 0, (int) (dX + (getWidth() - longPaddingBefore - longPaddingAfter) / count), (int) (dY + 4 * bigR));
            } else {
                rect = new Rect((int) (dX - 2 * bigR), 0, (int) (dX + 2 * bigR), (int) (dY + 4 * bigR));
            }
            pointRects.add(rect);
            if (count <= mMaxPage) {
                canvas.drawCircle(dX, dY, mRadius, mPaintStroke);
            }
        }
        if (count > mMaxPage) {
            if (isTooLong) {
                canvas.drawLine(longOffset - mRadius, shortOffset, longOffset - mRadius + (2 * bigR + (count - 1) * ((getWidth() - longPaddingBefore - longPaddingAfter) / count)), shortOffset, mPaintLine);
            } else {
                canvas.drawLine(longOffset - mRadius, shortOffset, longOffset - mRadius + (2 * bigR + (count - 1) * cicleSpace), shortOffset, mPaintLine);
            }
        }

        //Draw the filled circle according to the current scroll
        float cx = 0;
        if (isTooLong) {
            cx = (mSnap ? mSnapPage : mCurrentPage) * ((getWidth() - longPaddingBefore - longPaddingAfter) / count);
        } else {
            cx = (mSnap ? mSnapPage : mCurrentPage) * cicleSpace;
        }
        if (!mSnap) {
            if (isTooLong) {
                cx += mPageOffset * ((getWidth() - longPaddingBefore - longPaddingAfter) / count);
            } else {
                cx += mPageOffset * cicleSpace;
            }
        }
        if (mOrientation == HORIZONTAL) {
            dX = longOffset + cx;
            dY = shortOffset;
        } else {
            dX = shortOffset;
            dY = longOffset + cx;
        }
        canvas.drawCircle(dX, dY, bigR, mPaintFill);
    }

    public void setViewPager(ViewPager view) {
        if (mViewPager == view) {
            return;
        }
        if (mViewPager != null) {
            mViewPager.addOnPageChangeListener(null);
        }
        if (view.getAdapter() == null) {
            throw new IllegalStateException("ViewPager does not have adapter instance.");
        }
        mViewPager = view;
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                if (mSnap || mScrollState == ViewPager.SCROLL_STATE_IDLE) {
                    mCurrentPage = position;
                    mSnapPage = position;
                    invalidate();
                }

                if (mListener != null) {
                    mListener.onPageSelected(position);
                }
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                mCurrentPage = position;
                mPageOffset = positionOffset;
                invalidate();

                if (mListener != null) {
                    mListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                mScrollState = state;

                if (mListener != null) {
                    mListener.onPageScrollStateChanged(state);
                }
            }
        });
        invalidate();
    }

    public void setViewPager(ViewPager view, int initialPosition) {
        setViewPager(view);
        setCurrentItem(initialPosition);
    }

    public void setCurrentItem(int item) {
        if (mViewPager == null) {
            throw new IllegalStateException("ViewPager has not been bound.");
        }
        mViewPager.setCurrentItem(item);
        mCurrentPage = item;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mOrientation == HORIZONTAL) {
            setMeasuredDimension(measureLong(widthMeasureSpec), measureShort(heightMeasureSpec));
        } else {
            setMeasuredDimension(measureShort(widthMeasureSpec), measureLong(heightMeasureSpec));
        }
    }

    /**
     * Determines the width of this view
     *
     * @param measureSpec A measureSpec packed into an int
     * @return The width of the view, honoring constraints from measureSpec
     */
    private int measureLong(int measureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if ((specMode == MeasureSpec.EXACTLY) || (mViewPager == null)) {
            //We were told how big to be
            result = specSize;
        } else {
            //Calculate the width according the views count
            final int count = mViewPager.getAdapter().getCount();
            result = (int) (getPaddingLeft() + getPaddingRight()
                    + (count * 2 * mRadius) + (count - 1) * mRadius + 1);
            //Respect AT_MOST value if that was what is called for by measureSpec
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    /**
     * Determines the height of this view
     *
     * @param measureSpec A measureSpec packed into an int
     * @return The height of the view, honoring constraints from measureSpec
     */
    private int measureShort(int measureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            //We were told how big to be
            result = specSize;
        } else {
            //Measure the height
            result = (int) (2 * mRadius + getPaddingTop() + getPaddingBottom() + 1);
            //Respect AT_MOST value if that was what is called for by measureSpec
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        mCurrentPage = savedState.currentPage;
        mSnapPage = savedState.currentPage;
        requestLayout();
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);
        savedState.currentPage = mCurrentPage;
        return savedState;
    }

    static class SavedState extends BaseSavedState {
        int currentPage;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            currentPage = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(currentPage);
        }

        @SuppressWarnings("UnusedDeclaration")
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    //////////////////

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                checkRectPointMove(x, y);
                break;
            case MotionEvent.ACTION_UP:
                checkRectPointClick(x, y);
                break;
        }
        return true;
    }

    private void checkRectPointClick(float x, float y) {
        for (int i = 0; i < pointRects.size(); i++) {
            Rect rect = pointRects.get(i);
            if (x > rect.left && x < rect.right && y > rect.top && y < rect.bottom) {
                Log.e("point", "i:" + i);
                mViewPager.setCurrentItem(i);
            }
        }
    }

    private void checkRectPointMove(float x, float y) {
        for (int i = 0; i < pointRects.size(); i++) {
            Rect rect = pointRects.get(i);
            if (x > rect.left && x < rect.right) {
                Log.e("point", "i:" + i);
                mViewPager.setCurrentItem(i);
            }
        }
    }
}