package com.loom.loomcalendar;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.loom.calendarview.LoomCalendar;
import com.loom.calendarview.MonthPicker;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final MonthPicker monthCalendar = findViewById(R.id.monthCalendar);
        final LoomCalendar loomCalendar = findViewById(R.id.loomCalendar);

        monthCalendar.setOnMonthSelectEventListener(new MonthPicker.OnMonthSelectEventListener() {
            @Override
            public void onMonthSelected(int year, int month) {
                loomCalendar.setmCurMonth(month);
                loomCalendar.setmCurYear(year);
                monthCalendar.setVisibility(View.GONE);
                loomCalendar.setVisibility(View.VISIBLE);
            }
        });
    }
}
