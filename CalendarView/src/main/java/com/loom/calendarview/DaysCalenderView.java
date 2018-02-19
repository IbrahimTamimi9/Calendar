package com.loom.calendarview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextPaint;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Scroller;

import java.util.Calendar;

class DaysCalenderView extends View {

    private static final int COLUMNS = 7;
    private static final int ROWS = 5;
    private TextPaint mHeaderTextPaint;
    private int mWidth;
    private int mCellWidth;
    private int mCellHeight;
    private Rect mHeaderTxtRect;
    private int mHeight;
    private String[] mWeekTexts;
    private int mHeaderTxtVerticalMargin = (int) (6 * getResources().getDisplayMetrics().density);
    private int mMonthTxtVerticalMargin = (int) (4 * getResources().getDisplayMetrics().density);
    private int mHeaderHeight;
    private int mLineWidth = 1;
    private Paint mBackPaint;
    private Calendar mCalendar = Calendar.getInstance();
    private int mCurYear = mCalendar.get(Calendar.YEAR);
    private int mCurMonth = mCalendar.get(Calendar.MONTH) + 1;
    private int mCurDay = mCalendar.get(Calendar.DAY_OF_MONTH);
    private int mCurWeekDay = mCalendar.get(Calendar.DAY_OF_WEEK);
    private int mFirstDayOfWeek = mCalendar.getFirstDayOfWeek();
    private int mCurMonthDays = getMonthDays(mCurYear, mCurMonth);
    private int mLastMonthDays = getMonthDays(mCurYear, mCurMonth - 1);
    private int mFirstWeekDayOfMonth;
    private int mRows = ROWS;
    private DateInfo[][] mDateMatrix;
    private TextPaint mDateTextPaint;
    private Rect mDateTxtRect;
    private Rect mCellRect;
    private int mSelectedI;
    private int mSelectedJ;
    private int mLastI;
    private int mLastJ;
    private OnSelectListener mOnSelectListener;

    private float mSmallTextSize = getResources().getDimensionPixelSize(R.dimen.text_size_small);
    private float mNormalTextSize = getResources().getDimensionPixelSize(R.dimen.text_size_normal);

    private float mTranslateY = 0;
    private float mLastX;
    private float mLastY;
    private int mNextMonthDays;
    private DateInfo[][] mLastDateMatrix;
    private int mLastRows;
    private int mNextRows;
    private DateInfo[][] mNextDateMatrix;
    private int mScrollY;
    private int mTouchSlop;
    private Scroller mScroller;
    private int mMonthPos = 0;


    public DaysCalenderView(Context context) {
        this(context, null);
    }

    public DaysCalenderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DaysCalenderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        mScroller = new Scroller(context);

        mDateTextPaint = new TextPaint();
        mDateTextPaint.setColor(Color.BLACK);
        mDateTextPaint.setStyle(Paint.Style.FILL);
        mDateTextPaint.setTextSize(mNormalTextSize);
        mDateTxtRect = new Rect();
        mDateTextPaint.getTextBounds("28", 0, 2, mDateTxtRect);

        mWeekTexts = new String[]{"Sun", "Mon", "Tue", "Wed", "Thur", "Fri", "Sat"};
        mHeaderTextPaint = new TextPaint();
        mHeaderTextPaint.setTextSize(mSmallTextSize);
        mHeaderTextPaint.setStyle(Paint.Style.FILL);
        mHeaderTextPaint.setColor(Color.BLACK);
        mHeaderTxtRect = new Rect();
        mHeaderTextPaint.getTextBounds("Sat", 0, 1, mHeaderTxtRect);
        mHeaderTxtVerticalMargin =
                getResources().getDimensionPixelSize(R.dimen.text_vertical_margin);
        mHeaderHeight = mHeaderTxtRect.bottom - mHeaderTxtRect.top
                + 2 * mHeaderTxtVerticalMargin;

        mWidth = context.getResources().getDisplayMetrics().widthPixels;

        mBackPaint = new Paint();
        mBackPaint.setColor(Color.LTGRAY);
        mBackPaint.setStrokeWidth(mLineWidth);

        mCellWidth = (int) ((mWidth + 0f - mLineWidth * (COLUMNS - 1)) / COLUMNS);
        mCellHeight = (int) (0.8f * mCellWidth);
        mCellRect = new Rect();


        initData();

    }

    private void initData() {
        mCurYear = mCalendar.get(Calendar.YEAR);
        mCurMonth = mCalendar.get(Calendar.MONTH) + 1;
        mCurDay = mCalendar.get(Calendar.DAY_OF_MONTH);
        mCurWeekDay = mCalendar.get(Calendar.DAY_OF_WEEK);
        mFirstDayOfWeek = mCalendar.getFirstDayOfWeek();
        mCurMonthDays = getMonthDays(mCurYear, mCurMonth);
        mLastMonthDays = getMonthDays(mCurYear + ((mCurMonth - 1) <= 0 ? -1 : 0), (mCurMonth - 1) <= 0 ? 12 : mCurMonth - 1);
        mNextMonthDays = getMonthDays(mCurYear + ((mCurMonth + 1) > 12 ? 1 : 0), (mCurMonth + 1) > 12 ? 1 : mCurMonth + 1);
        if (mOnSelectListener != null) {
            mOnSelectListener.onDateSelected(mCurYear, mCurMonth, mCurDay);
        }
        mCalendar.set(mCurYear, mCurMonth - 1, 1);
        mFirstWeekDayOfMonth = mCalendar.get(Calendar.DAY_OF_WEEK);

        mRows = (mCurMonthDays + mFirstWeekDayOfMonth + COLUMNS - 1) / COLUMNS;

        mHeight = mHeaderHeight + (mLineWidth + mCellHeight) * mRows + mLineWidth;

        calculateDateMatrix();
    }

    private void calculateLastMonthDateMatrix() {
        int lastMonthShowDays = mFirstWeekDayOfMonth - mFirstDayOfWeek;
        int lastMonthHiddenDays = mLastMonthDays - lastMonthShowDays;

        mLastRows = (lastMonthHiddenDays + COLUMNS - 1) / COLUMNS;
        int lastEmptyDays = mLastRows * COLUMNS - lastMonthHiddenDays;
        mLastDateMatrix = new DateInfo[mLastRows][COLUMNS];
        int cellIndex = 0;
        for (int i = 0; i < mLastRows; i++) {
            for (int j = 0; j < COLUMNS; j++) {
                cellIndex++;
                DateInfo info = new DateInfo();
                info.status = DateInfo.DISABLED;
                if (cellIndex <= lastEmptyDays) {
                    info.date = 0;
                } else {
                    info.date = cellIndex - lastEmptyDays;
                }
                mLastDateMatrix[i][j] = info;
            }
        }
    }

    private void calculateCurDateMatrix() {
        int lastMonthShowDays = mFirstWeekDayOfMonth - mFirstDayOfWeek;
        int cellIndex = 0;
        mDateMatrix = new DateInfo[mRows][COLUMNS];
        for (int i = 0; i < mRows; i++) {
            for (int j = 0; j < COLUMNS; j++) {
                cellIndex++;
                DateInfo dateInfo = new DateInfo();
                if (cellIndex <= lastMonthShowDays) {
                    dateInfo.date = mLastMonthDays - lastMonthShowDays + cellIndex;
                    dateInfo.status = DateInfo.DISABLED;
                } else if (cellIndex <= mCurMonthDays + lastMonthShowDays) {

                    if (cellIndex - lastMonthShowDays == mCurDay) {
                        dateInfo.status = DateInfo.SELECTED;
                        dateInfo.date = cellIndex - lastMonthShowDays;
                        mSelectedI = i;
                        mSelectedJ = j;
                    } else {
                        dateInfo.status = DateInfo.ENABLED;
                        dateInfo.date = cellIndex - lastMonthShowDays;
                    }

                } else {
                    dateInfo.date = cellIndex - lastMonthShowDays - mCurMonthDays;
                    dateInfo.status = DateInfo.DISABLED;
                }
                mDateMatrix[i][j] = dateInfo;
            }
        }
    }

    private void calculateNextMonthDateMartrix() {
        int nextMonthShowDays = mDateMatrix[mRows - 1][COLUMNS - 1].date > 7 ? 0 : mDateMatrix[mRows - 1][COLUMNS - 1].date;
        int nextMonthHiddenDays = mNextMonthDays - nextMonthShowDays;
        mNextRows = (nextMonthHiddenDays + COLUMNS - 1) / COLUMNS;
        mNextDateMatrix = new DateInfo[mNextRows][COLUMNS];
        int cellIndex = 0;
        for (int i = 0; i < mNextRows; i++) {
            for (int j = 0; j < COLUMNS; j++) {
                cellIndex++;
                DateInfo info = new DateInfo();
                info.status = DateInfo.DISABLED;
                if (cellIndex <= nextMonthHiddenDays) {
                    info.date = cellIndex + nextMonthShowDays;
                } else {
                    info.date = 0;
                }
                mNextDateMatrix[i][j] = info;
            }
        }
    }

    private void calculateDateMatrix() {
        calculateLastMonthDateMatrix();
        calculateCurDateMatrix();
        calculateNextMonthDateMartrix();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(mWidth, mHeight);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        float x = event.getX();
        float y = event.getY();
        if (y < mHeaderHeight) return false;

        int action = event.getAction();
        int j = (int) (x / (mCellWidth + mLineWidth));
        int i = (int) ((y - mHeaderHeight) / (mCellHeight + mLineWidth));

        if (action == MotionEvent.ACTION_DOWN) {
            mMonthPos = 0;
            if (mScroller != null && !mScroller.isFinished()) {
                mScroller.abortAnimation();
            }
            mTranslateY = 0;
            mLastX = x;
            mLastY = y;
            mScrollY = getScrollY();

            mLastI = i;
            mLastJ = j;
            if (mDateMatrix[i][j].status != DateInfo.DISABLED) {
                mDateMatrix[i][j].status = DateInfo.PRESSING;
            }
        } else if (action == MotionEvent.ACTION_MOVE) {


            if (Math.abs(y - mLastY) > mTouchSlop) {
                mTranslateY = 0;//y - mLastY;

                scrollTo(0, (int) (mScrollY + mLastY - y));
            } else {
                mTranslateY = 0;
            }

            if (i != mLastI || j != mLastJ) {
                if (mDateMatrix[mLastI][mLastJ].status != DateInfo.DISABLED) {
                    if (mLastI != mSelectedI || mLastJ != mSelectedJ) {
                        mDateMatrix[mLastI][mLastJ].status = DateInfo.ENABLED;
                    } else {
                        mDateMatrix[mLastI][mLastJ].status = DateInfo.SELECTED;
                    }
                }

            }
        } else if (action == MotionEvent.ACTION_UP) {
            int scrollY = getScrollY();
            if (scrollY > mHeight / 3f) {
                mMonthPos = 1;
                int scrollAmount = ((mFirstWeekDayOfMonth - mFirstDayOfWeek + mCurMonthDays < mRows * COLUMNS ? -1 : 0) + mRows) * (mCellHeight + mLineWidth);
                mScroller.startScroll(0, scrollY, 0, scrollAmount - scrollY);
            } else if (scrollY < -mHeight / 3f) {
                mMonthPos = -1;
                int scrollAmount = mLastRows * (mCellHeight + mLineWidth);
                mScroller.startScroll(0, scrollY, 0, -scrollAmount - scrollY);
            } else {
                mMonthPos = 0;
                mScroller.startScroll(0, scrollY, 0, -scrollY);

                mTranslateY = 0;
                if (i == mLastI && j == mLastJ && mDateMatrix[mLastI][mLastJ].status != DateInfo.DISABLED) {
                    mDateMatrix[mSelectedI][mSelectedJ].status = DateInfo.ENABLED;
                    mDateMatrix[i][j].status = DateInfo.SELECTED;
                    mSelectedI = i;
                    mSelectedJ = j;
                    if (mOnSelectListener != null) {
                        mOnSelectListener.onDateSelected(mCurYear, mCurMonth, mDateMatrix[i][j].date);
                    }

                }
            }

        } else {
            scrollTo(0, 0);
            mDateMatrix[mLastI][mLastJ].status = DateInfo.ENABLED;
            mTranslateY = 0;
        }
        invalidate();
        return true;
    }

    @Override
    public void computeScroll() {
        if (mScroller != null) {

            if (mScroller.computeScrollOffset()) {
                scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
                postInvalidate();
            } else {
                if (mMonthPos > 0) {
                    mMonthPos = 0;
                    setDate(mCurYear + ((mCurMonth + 1) > 12 ? 1 : 0), (mCurMonth + 1) > 12 ? 1 : mCurMonth + 1, mCurDay);
                } else if (mMonthPos < 0) {
                    mMonthPos = 0;
                    setDate(mCurYear + ((mCurMonth - 1) <= 0 ? -1 : 0), (mCurMonth - 1) <= 0 ? 12 : mCurMonth - 1, mCurDay);
                }
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        if (getScrollY() < 0) {
            drawLastMonthDays(canvas);
        } else if (getScrollY() > 0) {
            drawNextMonthDays(canvas);
        }

        drawCurMonthDays(canvas);

        canvas.save();
        canvas.translate(0, getScrollY());
        mBackPaint.setStyle(Paint.Style.STROKE);
        mBackPaint.setStrokeWidth(1);
        mBackPaint.setColor(Color.parseColor("#cccccc"));
        canvas.drawRect(0, 0, mWidth, mHeight, mBackPaint);
        canvas.restore();
    }

    private void drawNextMonthDays(Canvas canvas) {
        float translateY = mRows * (mCellHeight + mLineWidth);
        canvas.save();
        canvas.translate(0, translateY);
        mBackPaint.setStyle(Paint.Style.FILL);

        int firstDayI = -1, firstDayJ = -1;
        float dateX, dateY;
        int cellX, cellY;
        float deltaY = 0;
        for (int i = 0; i < mNextRows; i++) {
            dateY = mHeaderHeight + (mLineWidth + mCellHeight) * i + mLineWidth + (mCellHeight + mDateTxtRect.height()) / 2f;
            cellY = mHeaderHeight + mLineWidth + (mCellHeight + mLineWidth) * i;

            for (int j = 0; j < COLUMNS; j++) {
                DateInfo dateInfo = mNextDateMatrix[i][j];
                deltaY = 0;
                if (dateInfo.date == 1) {
                    deltaY = mSmallTextSize / 2f + mMonthTxtVerticalMargin;
                    firstDayI = i;
                    firstDayJ = j;

                }

                mDateTextPaint.setTextSize(mNormalTextSize);

                float charWidth = mDateTextPaint.measureText(dateInfo.date + "");
                dateX = (mCellWidth - charWidth) / 2f + (mCellWidth + mLineWidth) * j;

                cellX = (mCellWidth + mLineWidth) * j - mLineWidth;

                mBackPaint.setColor(Color.parseColor("#eeeeee"));
                mDateTextPaint.setColor(Color.BLACK);

                mCellRect.set(cellX, cellY, cellX + mCellWidth, cellY + mCellHeight);
                canvas.drawRect(mCellRect, mBackPaint);

                if (dateInfo.date > 0) {
                    canvas.drawText(dateInfo.date + "", dateX, dateY + deltaY, mDateTextPaint);
                }
            }

        }

        float rowLineX, rowLineY;
        mBackPaint.setColor(Color.parseColor("#cccccc"));
        mBackPaint.setStyle(Paint.Style.STROKE);
        rowLineX = 0;
        for (int i = 0; i < mNextRows; i++) {
            rowLineY = mHeaderHeight + (mCellHeight + mLineWidth) * i;
            canvas.drawLine(rowLineX, rowLineY, rowLineX + mWidth, rowLineY, mBackPaint);
        }
        float colLineX, colLineY;
        colLineY = mHeaderHeight;
        for (int i = 0; i < COLUMNS - 1; i++) {
            colLineX = (mCellWidth + mLineWidth) * i + mCellWidth;
            canvas.drawLine(colLineX, colLineY, colLineX, colLineY + mHeight - mHeaderHeight, mBackPaint);
        }


        int nextMonth = mCurMonth + 1 > 12 ? 1 : mCurMonth + 1;
        mDateTextPaint.setTextSize(mSmallTextSize);
        float monthWidth = mDateTextPaint.measureText(getLastMonthText(nextMonth));

        if (firstDayI >= 0 && firstDayJ >= 0) {
            dateX = (mCellWidth - monthWidth) / 2f + (mCellWidth + mLineWidth) * firstDayJ;
            dateY = mHeaderHeight + (mLineWidth + mCellHeight) * firstDayI
                    + mLineWidth + (mCellHeight - mMonthTxtVerticalMargin - mDateTxtRect.height() + mSmallTextSize) / 2f;
            canvas.drawText(getLastMonthText(nextMonth), dateX, dateY, mDateTextPaint);
        }
//        }

        canvas.restore();
    }

    private void drawLastMonthDays(Canvas canvas) {

        float translateY = -mLastRows * (mCellHeight + mLineWidth);
        canvas.save();
        canvas.translate(0, translateY);

        mBackPaint.setStyle(Paint.Style.FILL);

        int firstDayI = 0, firstDayJ = 0;
        float dateX, dateY;
        int cellX, cellY;
        float deltaY = 0;
        for (int i = 0; i < mLastRows; i++) {
            dateY = mHeaderHeight + (mLineWidth + mCellHeight) * i + mLineWidth + (mCellHeight + mDateTxtRect.height()) / 2f;
            cellY = mHeaderHeight + mLineWidth + (mCellHeight + mLineWidth) * i;

            for (int j = 0; j < COLUMNS; j++) {
                DateInfo dateInfo = mLastDateMatrix[i][j];
                deltaY = 0;
                if (dateInfo.date == 1) {
                    deltaY = mSmallTextSize / 2f + mMonthTxtVerticalMargin;
                    firstDayI = i;
                    firstDayJ = j;

                }

                mDateTextPaint.setTextSize(mNormalTextSize);

                float charWidth = mDateTextPaint.measureText(dateInfo.date + "");
                dateX = (mCellWidth - charWidth) / 2f + (mCellWidth + mLineWidth) * j;

                cellX = (mCellWidth + mLineWidth) * j - mLineWidth;


                mBackPaint.setColor(Color.parseColor("#eeeeee"));
                mDateTextPaint.setColor(Color.BLACK);


                mCellRect.set(cellX, cellY, cellX + mCellWidth, cellY + mCellHeight);
                canvas.drawRect(mCellRect, mBackPaint);

                if (dateInfo.date > 0) {
                    canvas.drawText(dateInfo.date + "", dateX, dateY + deltaY, mDateTextPaint);
                }
            }

        }
        float rowLineX, rowLineY;
        mBackPaint.setColor(Color.parseColor("#cccccc"));
        mBackPaint.setStyle(Paint.Style.STROKE);
        rowLineX = 0;
        for (int i = 0; i < mLastRows; i++) {
            rowLineY = mHeaderHeight + (mCellHeight + mLineWidth) * i;
            canvas.drawLine(rowLineX, rowLineY, rowLineX + mWidth, rowLineY, mBackPaint);
        }
        float colLineX, colLineY;
        colLineY = mHeaderHeight;
        for (int i = 0; i < COLUMNS - 1; i++) {
            colLineX = (mCellWidth + mLineWidth) * i + mCellWidth;
            canvas.drawLine(colLineX, colLineY, colLineX, colLineY + mHeight - mHeaderHeight, mBackPaint);
        }


        int lastMonth = mCurMonth - 1 <= 0 ? 12 : mCurMonth - 1;
        mDateTextPaint.setTextSize(mSmallTextSize);
        float monthWidth = mDateTextPaint.measureText(getLastMonthText(lastMonth));

        dateX = (mCellWidth - monthWidth) / 2f + (mCellWidth + mLineWidth) * firstDayJ;
        dateY = mHeaderHeight + (mLineWidth + mCellHeight) * firstDayI
                + mLineWidth + (mCellHeight - mMonthTxtVerticalMargin - mDateTxtRect.height() + mSmallTextSize) / 2f;
        canvas.drawText(getLastMonthText(lastMonth), dateX, dateY, mDateTextPaint);

        canvas.restore();
    }

    private String getLastMonthText(int lastMonth) {
        return DateUtils.getMonthString(lastMonth - 1, DateUtils.LENGTH_SHORT);
    }

    private void drawCurMonthDays(Canvas canvas) {
        canvas.save();
        canvas.translate(0, mTranslateY);

        mBackPaint.setStyle(Paint.Style.FILL);

        int firstDayI = 0, firstDayJ = 0;
        float dateX, dateY;
        int cellX, cellY;
        float deltaY = 0;
        for (int i = 0; i < mRows; i++) {
            dateY = mHeaderHeight + (mLineWidth + mCellHeight) * i + mLineWidth + (mCellHeight + mDateTxtRect.height()) / 2f;
            cellY = mHeaderHeight + mLineWidth + (mCellHeight + mLineWidth) * i;

            for (int j = 0; j < COLUMNS; j++) {
                DateInfo dateInfo = mDateMatrix[i][j];
                deltaY = 0;
                if (dateInfo.date == 1 && dateInfo.status != DateInfo.DISABLED) {
                    deltaY = mSmallTextSize / 2f + mMonthTxtVerticalMargin;
                    firstDayI = i;
                    firstDayJ = j;

                }

                mDateTextPaint.setTextSize(mNormalTextSize);


                float charWidth = mDateTextPaint.measureText(dateInfo.date + "");
                dateX = (mCellWidth - charWidth) / 2f + (mCellWidth + mLineWidth) * j;

                cellX = (mCellWidth + mLineWidth) * j - mLineWidth;


                if (dateInfo.status == DateInfo.DISABLED) {
                    mBackPaint.setColor(Color.parseColor("#eeeeee"));
                    mDateTextPaint.setColor(Color.BLACK);

                } else if (dateInfo.status == DateInfo.SELECTED) {
                    mBackPaint.setColor(Color.parseColor("#2C9CFE"));
                    mDateTextPaint.setColor(Color.WHITE);
                    deltaY = mSmallTextSize / 2f + mMonthTxtVerticalMargin;
                } else if (dateInfo.status == DateInfo.PRESSING) {
                    mBackPaint.setColor(Color.parseColor("#026ECC"));
                    mDateTextPaint.setColor(Color.WHITE);
                    if (mSelectedI == i && mSelectedJ == j) {
                        deltaY = mSmallTextSize / 2f + mMonthTxtVerticalMargin;
                    }
                } else {
                    mBackPaint.setColor(Color.WHITE);
                    mDateTextPaint.setColor(Color.BLACK);
                }


                mCellRect.set(cellX, cellY, cellX + mCellWidth, cellY + mCellHeight);
                canvas.drawRect(mCellRect, mBackPaint);

                canvas.drawText(dateInfo.date + "", dateX, dateY + deltaY, mDateTextPaint);
            }

        }

        float rowLineX, rowLineY;
        mBackPaint.setColor(Color.parseColor("#cccccc"));
        mBackPaint.setStyle(Paint.Style.STROKE);
        rowLineX = 0;
        for (int i = 0; i < mRows + 1; i++) {
            rowLineY = mHeaderHeight + (mCellHeight + mLineWidth) * i;
            canvas.drawLine(rowLineX, rowLineY, rowLineX + mWidth, rowLineY, mBackPaint);
        }
        float colLineX, colLineY;
        colLineY = mHeaderHeight;
        for (int i = 0; i < COLUMNS - 1; i++) {
            colLineX = (mCellWidth + mLineWidth) * i + mCellWidth;
            canvas.drawLine(colLineX, colLineY, colLineX, colLineY + mHeight - mHeaderHeight, mBackPaint);
        }

        mDateTextPaint.setTextSize(mSmallTextSize);
        mDateTextPaint.setColor(Color.WHITE);
        float monthWidth = mDateTextPaint.measureText(getCurrentMonth());
        dateX = (mCellWidth - monthWidth) / 2f + (mCellWidth + mLineWidth) * mSelectedJ;
        dateY = mHeaderHeight + (mLineWidth + mCellHeight) * mSelectedI
                + mLineWidth + (mCellHeight - mMonthTxtVerticalMargin - mDateTxtRect.height() + mSmallTextSize) / 2f;
        canvas.drawText(getCurrentMonth(), dateX, dateY, mDateTextPaint);

        if (mDateMatrix[firstDayI][firstDayJ].status != DateInfo.SELECTED) {
            mDateTextPaint.setTextSize(mSmallTextSize);
            if (mDateMatrix[firstDayI][firstDayJ].status != DateInfo.PRESSING) {
                mDateTextPaint.setColor(Color.GRAY);
            }
            dateX = (mCellWidth - monthWidth) / 2f + (mCellWidth + mLineWidth) * firstDayJ;
            dateY = mHeaderHeight + (mLineWidth + mCellHeight) * firstDayI
                    + mLineWidth + (mCellHeight - mMonthTxtVerticalMargin - mDateTxtRect.height() + mSmallTextSize) / 2f;
            canvas.drawText(getCurrentMonth(), dateX, dateY, mDateTextPaint);
        }
        canvas.restore();

        canvas.save();
        canvas.translate(0, getScrollY());

        float textX, textY;
        mBackPaint.setStyle(Paint.Style.FILL);
        mBackPaint.setColor(Color.WHITE);
        canvas.drawRect(0, 0, mWidth, mHeaderHeight, mBackPaint);
        textY = mHeaderTxtVerticalMargin + mHeaderTxtRect.height();
        for (int i = 0; i < COLUMNS; i++) {
            int textIndex = (i + mFirstDayOfWeek - 1) % mWeekTexts.length;

            if (mWeekTexts[textIndex].contains("Sun")
                    || mWeekTexts[textIndex].contains("Sat")) {
                mHeaderTextPaint.setColor(Color.parseColor("#ff0000"));
            } else {
                mHeaderTextPaint.setColor(Color.BLACK);
            }

            float charWidth = mHeaderTextPaint.measureText(mWeekTexts[textIndex]);
            textX = (mCellWidth - charWidth) / 2f + (mCellWidth + mLineWidth) * i;

            canvas.drawText(mWeekTexts[(i + mFirstDayOfWeek - 1) % mWeekTexts.length], textX, textY, mHeaderTextPaint);
        }
        canvas.restore();

    }

    private String getCurrentMonth() {
        return DateUtils.getMonthString(mCurMonth - 1, DateUtils.LENGTH_SHORT);
    }

    private int getMonthDays(int year, int month) {
        int days = 0;
        switch (month) {
            case 1:
            case 3:
            case 5:
            case 7:
            case 8:
            case 10:
            case 12:
                days = 31;
                break;
            case 2:
                days = 28 + (isLeapYear(year) ? 1 : 0);
                break;
            case 4:
            case 6:
            case 9:
            case 11:
                days = 30;
                break;
            default:
                break;
        }
        return days;
    }

    private boolean isLeapYear(int year) {
        return (year % 4 == 0) && (year % 100 != 0) || (year % 400 == 0);
    }


    public void setDate(int year, int month, int day) {
        if (month < 1) {
            month = 1;
        } else if (month > 12) {
            month = 12;
        }

        if (year < 1970) {
            year = 1970;
        }

        int maxDays = getMonthDays(year, month);
        if (day < 1) {
            day = 1;
        } else if (day > getMonthDays(year, month)) {
            day = maxDays;
        }

        mCalendar.set(year, month - 1, day);
        initData();
        requestLayout();
        scrollTo(0, 0);
        invalidate();
    }

    public int[] getSelectedDate() {
        return new int[]{mCurYear, mCurMonth, mDateMatrix[mSelectedI][mSelectedJ].date};
    }


    public void setOnSelectListener(OnSelectListener pListener) {
        mOnSelectListener = pListener;
    }

    public interface OnSelectListener {

        void onDateSelected(int year, int month, int day);
    }

}