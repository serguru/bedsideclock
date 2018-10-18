package com.abc.bedsideclock;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Point;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import javax.xml.datatype.Duration;

public class MainActivity extends AppCompatActivity
        implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

    private static final String TAG = "BedsideClockActivity";
    private static final String SHARED_PREFS = "sharedPrefs";
    private static final String ALPHA = "alpha";
    private static final String COLORINDEX = "colorIndex";
    private static final int defaultAlpha = 100;
    private static final int defaultColorIndex = 0;
    private static final float leftSwipeMargin = 10f; // in percent of screen width
    private static final float rightSwipeMargin = 10f;
    private static final int breakAlpha = 50;

    private TextView tcTime, tcDate, tvAlarm;
    private int screenWidth, screenHeight;
    private AlarmManager alarmManager;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private int currentColorIndex = 0;

    private static final int[] colors = {Color.WHITE, Color.RED, Color.YELLOW,
            Color.GREEN, Color.BLUE, Color.CYAN, Color.MAGENTA };

    private GestureDetectorCompat mDetector;

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
        screenHeight = size.y;

        tcTime = findViewById(R.id.tcTime);
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
        tvAlarm = findViewById(R.id.tvAlarm);

        int alpha = loadAlpha();
        setAlpha(alpha);

        loadColorIndex();

        setControlsColor(colors[currentColorIndex]);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        float x = event.getX();
        float y = event.getY();

        int value;

        if (y < screenHeight / 2f)
        {
            value = alphaByX(x);

            if (value >= 0 && value <= 255)
            {
                setAlpha(value);
            }
        }


//        Log.d(TAG,"onTouchEvent called, x = " + x + ", width = " + screenWidth + ", y = " + y + ", height = " + screenHeight);

        if (this.mDetector.onTouchEvent(event)) {
            return true;
        }

        return super.onTouchEvent(event);
    }

    private void setNextColor()
    {
        currentColorIndex++;

        if (currentColorIndex < 0 || currentColorIndex >= colors.length)
        {
            currentColorIndex = 0;
        }

        saveColorIndex();

        setControlsColor(colors[currentColorIndex]);
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

    private void rgbByX(float x, int[] rgb)
    {
        rgb[0] = -1;
        rgb[1] = -1;
        rgb[2] = -1;

        int leftMarginWidth =  (int)(leftSwipeMargin / 100f * screenWidth);
        int rightMarginWidth =  (int)(rightSwipeMargin / 100f * screenWidth);

        if (x <= leftMarginWidth)
        {
            return;
        }

        if (x >= screenWidth - rightMarginWidth)
        {
            return;
        }

        x -= leftMarginWidth;

        int workingWidth =  screenWidth - leftMarginWidth - rightMarginWidth;

        int onethird = workingWidth / 3;

        if (x <= onethird)
        {
            rgb[0] = Math.round(x/onethird * 255);
            return;
        }

        if (x <= onethird * 2)
        {
            x -= onethird;
            rgb[1] = Math.round(x/onethird * 255);
            return;
        }

        x -= onethird * 2;
        rgb[2] = Math.round(x/onethird * 255);
    }


    private void setRGB(int[] rgb)
    {
        int currentColor = tcTime.getCurrentTextColor();

        int alpha = Color.alpha(currentColor);
        int red = rgb[0] >= 0 ? rgb[0] : Color.red(currentColor);
        int green = rgb[1] >= 0 ? rgb[1] : Color.green(currentColor);
        int blue = rgb[2] >= 0 ? rgb[2] : Color.blue(currentColor);

        int color = Color.argb(alpha,red,green,blue);

        setControlsColor(color);

    }

    private void setControlsColor(int color)
    {
        tcTime.setTextColor(color);
        tcDate.setTextColor(color);
        tvAlarm.setTextColor(color);
    }

    private void setAlpha(int alpha) {

        int currentColor = tcTime.getCurrentTextColor();

        if (alpha == Color.alpha(currentColor))
        {
            return;
        }

        saveAlpha(alpha);

        int red = Color.red(currentColor);
        int green = Color.green(currentColor);
        int blue = Color.blue(currentColor);

        int color = Color.argb(alpha,red,green,blue);

        setControlsColor(color);
    }

    private void saveAlpha(int alpha)
    {
        editor.putInt(ALPHA, alpha);
        editor.apply();
    }

    private int loadAlpha()
    {
        int alpha = sharedPreferences.getInt(ALPHA, defaultAlpha);
        return alpha;
    }

    private void saveColorIndex()
    {
        editor.putInt(COLORINDEX, currentColorIndex);
        editor.apply();
    }

    private void loadColorIndex()
    {
        currentColorIndex = sharedPreferences.getInt(COLORINDEX, defaultColorIndex);
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

        float y = e.getY();

        if (y >= screenHeight / 2f)
        {
            setNextColor();
        }

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
