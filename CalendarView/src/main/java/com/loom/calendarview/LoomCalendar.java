package com.loom.calendarview;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;

public class LoomCalendar extends LinearLayout {
    private Calendar mCalendar = Calendar.getInstance();
    private int mCurYear = mCalendar.get(Calendar.YEAR);
    private int mCurMonth = mCalendar.get(Calendar.MONTH) + 1;

    private DaysCalenderView calenderView;
    private TextView calendarTextView, calendarYear;
    private ImageView nextMonth, nextYear;
    private ImageView previousMonth, prevYear;

    public LoomCalendar(Context context) {
        this(context, null);
    }

    public LoomCalendar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoomCalendar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttr(context, attrs, defStyleAttr);
        init(context);
    }

    private void initAttr(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.LoomCalendar, defStyleAttr, -1);
        mCurMonth = a.getInt(R.styleable.LoomCalendar_startMonth, mCurMonth);
        mCurYear = a.getInt(R.styleable.LoomCalendar_startYear, mCurYear);
        a.recycle();
    }

    public void setmCurMonth(int mCurMonth) {
        this.mCurMonth = mCurMonth;
        changeDate();
    }

    public void setmCurYear(int mCurYear) {
        this.mCurYear = mCurYear;
        changeDate();
    }

    private void init(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.view_days_picker, this, false);
        addView(view);
        calenderView = view.findViewById(R.id.daysCalendar);
        nextMonth = view.findViewById(R.id.calendarRightButton);
        previousMonth = view.findViewById(R.id.calendarLeftButton);
        calendarTextView = view.findViewById(R.id.calendarTextView);

        nextYear = view.findViewById(R.id.calendarAddYear);
        prevYear = view.findViewById(R.id.calendarMinusYear);
        calendarYear = view.findViewById(R.id.calendarYearText);
        changeDate();
        nextMonth.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                addMonth();
            }
        });
        previousMonth.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                minusMonth();
            }
        });

        nextYear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                addYear();
                changeDate();
            }
        });
        prevYear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                minusYear();
                changeDate();
            }
        });
    }

    private void minusYear() {
        mCurYear = mCurYear - 1;
    }

    private void addYear() {
        mCurYear = mCurYear + 1;
    }

    public void addMonth() {
        if (mCurMonth == 12) {
            mCurMonth = 1;
        } else {
            mCurMonth = mCurMonth + 1;
        }
        changeDate();
    }

    public void minusMonth() {
        if (mCurMonth <= 1) {
            mCurMonth = 12;
            mCurYear = mCurYear - 1;
        } else {
            mCurMonth = mCurMonth - 1;
        }
        changeDate();
    }

    private void changeDate() {
        calendarYear.setText(String.format("%d", mCurYear));
        calendarTextView.setText(DateUtils.getMonthString(mCurMonth - 1, DateUtils.LENGTH_LONG));
        calenderView.setDate(mCurYear, mCurMonth, mCalendar.get(Calendar.DAY_OF_MONTH));
    }


}
