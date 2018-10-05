package com.abc.bedsideclock;

import android.content.res.ColorStateList;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private TextView tcTime, tcDate;
    private GestureDetector gestureDetector;

    private int[] colors = {
            0xFFFFFFFF,
            0xF2FFFFFF,
            0xE6FFFFFF,
            0xD9FFFFFF,
            0xCCFFFFFF,
            0xBFFFFFFF,
            0xB3FFFFFF,
            0xA6FFFFFF,
            0x99FFFFFF,
            0x8CFFFFFF,
            0x80FFFFFF,
            0x73FFFFFF,
            0x66FFFFFF,
            0x59FFFFFF,
            0x4DFFFFFF,
            0x40FFFFFF,
            0x33FFFFFF,
            0x26FFFFFF,
            0x1AFFFFFF,
            0x0DFFFFFF,
            0x00FFFFFF
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        tcDate = findViewById(R.id.tcDate);

        AndroidGestureDetector androidGestureDetector = new AndroidGestureDetector();

        gestureDetector = new GestureDetector(MainActivity.this, androidGestureDetector);
    }

    class AndroidGestureDetector implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            setNextColor();
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    private void setNextColor() {

        int currentColor = tcTime.getCurrentTextColor();

        int currentIndex = -1;

        for (int i = 0; i < colors.length; i++) {
            if (colors[i] == currentColor) {
                currentIndex = i;
                break;
            }
        }

        currentIndex++;

        int nextColor = currentIndex >= colors.length || currentIndex < 0 ? 0 : currentIndex;

        tcTime.setTextColor(colors[nextColor]);
        tcDate.setTextColor(colors[nextColor]);
    }
}
