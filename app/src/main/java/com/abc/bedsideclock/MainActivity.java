package com.abc.bedsideclock;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity
        implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

    private static final String TAG = "BedsideClockActivity";
    private static final String SHARED_PREFS = "sharedPrefs";
    private static final String COLOR = "color";
    private static final float leftSwipeMargin = 10f; // in percent of screen width
    private static final float rightSwipeMargin = 10f;
    private static final int breakAlpha = 50;

    private TextView tcTime, tcDate, tvAlarm;
    private int screenWidth;
    private AlarmManager alarmManager;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private static final int[] colors = {Color.WHITE, Color.RED, Color.YELLOW,
            Color.GREEN, Color.BLUE, Color.CYAN, Color.MAGENTA };

    private int currentColor = colors[0];
    private GestureDetectorCompat mDetector;

    private TextView[] views = {null, null, null};

    private void setCurrentColor(int value)
    {
        currentColor = value;

        editor.putInt(COLOR, value);
        editor.apply();

        for (int i = 0; i < views.length; i++)
        {
            TextView view = views[i];

            if (view.getCurrentTextColor() == currentColor)
            {
                continue;
            }

            view.setTextColor(currentColor);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDetector = new GestureDetectorCompat(this,this);
        mDetector.setOnDoubleTapListener(this);

        sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        editor = sharedPreferences.edit();

        alarmManager = (AlarmManager)getSystemService(Activity.ALARM_SERVICE);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;

        tcTime = findViewById(R.id.tcTime);
        views[0] = tcTime;
        tcTime.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                String sraw = s.toString();
                String slc = sraw.toLowerCase();

                if (sraw.equals(slc)) {
                    return;
                }

                tcTime.setText(s.toString().toLowerCase());

                setAlarmMessage();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        tcDate = findViewById(R.id.tcDate);
        views[1] = tcDate;
        tvAlarm = findViewById(R.id.tvAlarm);
        views[2] = tvAlarm;

        int color = sharedPreferences.getInt(COLOR, colors[0]);
        setCurrentColor(color);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        Log.d(TAG,"onTouchEvent called, x = " + x + ", width = " + screenWidth + ", y = " + y + ", height = " + screenHeight);

        if (this.mDetector.onTouchEvent(event)) {
            return true;
        }

        return super.onTouchEvent(event);
    }

    private int getColorIndex(int color)
    {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);

        color = Color.rgb(red, green, blue);

        for(int i = 0; i < colors.length; i++)
        {
            if (colors[i] == color)
            {
                return i;
            }
        }

        return -1;
    }

    private void setNextColor()
    {
        int nextIndex = getColorIndex(currentColor);

        nextIndex++;

        if (nextIndex < 0 || nextIndex >= colors.length)
        {
            nextIndex = 0;
        }

        int color = colors[nextIndex];

        int alpha = Color.alpha(currentColor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);

        color = Color.argb(alpha, red, green, blue);
        setCurrentColor(color);
    }

    private void setAlpha(int alpha)
    {
        int red = Color.red(currentColor);
        int green = Color.green(currentColor);
        int blue = Color.blue(currentColor);

        int color = Color.argb(alpha,red,green,blue);
        setCurrentColor(color);
    }

    private int alphaByX(float x)
    {
        int leftMarginWidth =  (int)(leftSwipeMargin / 100f * screenWidth);
        int rightMarginWidth =  (int)(rightSwipeMargin / 100f * screenWidth);

        if (x <= leftMarginWidth)
        {
            return -1;
        }

        if (x >= screenWidth - rightMarginWidth)
        {
            return 256;
        }

        x -= leftMarginWidth;

        int workingWidth =  screenWidth - leftMarginWidth - rightMarginWidth;

        float ratio = x / workingWidth;

        if (ratio <= 0.5) // alphas from 0 to breakAlpha
        {
            return Math.round(ratio * 2 * breakAlpha);
        }

        // alphas from breakAlpha + 1 to 255
        return Math.round(ratio * 255);
    }

    private void setAlarmMessage()
    {
        @SuppressLint({"NewApi", "LocalSuppress"})
        AlarmManager.AlarmClockInfo info = alarmManager.getNextAlarmClock();

        if (info == null)
        {
            tvAlarm.setVisibility(View.GONE);
            return;
        }

        @SuppressLint({"NewApi", "LocalSuppress"})
        long timestamp = info.getTriggerTime();

        Date now = new Date();
        Date alarmDate = new Date(timestamp);

        long diff = timestamp - now.getTime();

        if (diff <= 0)
        {
            tvAlarm.setVisibility(View.GONE);
            return;
        }

        double days, hours, minutes;

        String daysStr = "", hoursStr = "", minutesStr = "";

        int millisecondsInDay = 1000 * 60 * 60 * 24;

        days = (double)diff / (double)millisecondsInDay;

        if (days > 1)
        {
            long daysLong = (long)days;
            daysStr = " " + Long.toString(daysLong) + " d";
            diff -= daysLong * millisecondsInDay;
        }

        int millisecondsInHour = 1000 * 60 * 60;

        hours = (double)diff / (double)millisecondsInHour;

        if (hours > 1)
        {
            long hoursLong = (long)hours;
            hoursStr = (hoursLong < 10 ? " 0" : " ") + Long.toString(hoursLong) + " h";
            diff -= hoursLong * millisecondsInHour;
        }

        int millisecondsInMinute = 1000 * 60;

        minutes = (double)diff / (double)millisecondsInMinute;

        if (minutes > 1)
        {
            long minutesLong = (long)minutes;
            minutesStr = (minutesLong < 10 ? " 0" : " ") + Long.toString(minutesLong) + " m";
        }

        DateFormat dateFormat1 = new SimpleDateFormat("dd MMM");
        DateFormat dateFormat2 = new SimpleDateFormat("h:mm a");

        String text = "Alarm on " + dateFormat1.format(timestamp) + " at " +
                dateFormat2.format(timestamp).toLowerCase() +
                " in" + daysStr + hoursStr + minutesStr;

        tvAlarm.setText(text);
        tvAlarm.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        setNextColor();
        //Log.d(TAG,"onSingleTapConfirmed called");
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

        float x = e2.getX();
        int value;
        value = alphaByX(x);

        if (value >= 0 && value <= 255)
        {
            setAlpha(value);
        }
        //Log.d(TAG,"onScroll called");
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }
}
