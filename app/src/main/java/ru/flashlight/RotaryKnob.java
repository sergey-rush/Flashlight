package ru.flashlight;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * Created by Admin on 09.01.2018.
 */
public class RotaryKnob extends RelativeLayout implements GestureDetector.OnGestureListener {

    private RotaryKnobListener listener;
    private GestureDetector gestureDetector;
    private Context context;
    private float angle;
    private ImageView ivKnob;
    private Bitmap bmpRotorOn;
    private Bitmap bmpRotorOff;
    private boolean state = false;
    private int width = 0;
    private int height = 0;

    public void setListener(RotaryKnobListener listener) {
        this.listener = listener;
    }

    public void setState(boolean state) {
        this.state = state;
        ivKnob.setImageBitmap(state ? bmpRotorOn : bmpRotorOff);
    }

    public RotaryKnob(Context context, int back, int rotoron, int rotoroff, final int width, final int height) {
        super(context);
        this.context = context;
        gestureDetector = new GestureDetector(context, this);

        this.width = width;
        this.height = height;

        ImageView ivPanel = new ImageView(context);
        ivPanel.setImageResource(back);
        RelativeLayout.LayoutParams lpStator = new RelativeLayout.LayoutParams(width, height);
        lpStator.addRule(RelativeLayout.CENTER_IN_PARENT);
        addView(ivPanel, lpStator);

        Bitmap bitmapOn = BitmapFactory.decodeResource(context.getResources(), rotoron);
        Bitmap bitmapOff = BitmapFactory.decodeResource(context.getResources(), rotoroff);

        float scaleWidth = ((float) width) / bitmapOn.getWidth();
        float scaleHeight = ((float) height) / bitmapOn.getHeight();

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);

        bmpRotorOn = Bitmap.createBitmap(bitmapOn, 0, 0, bitmapOn.getWidth(), bitmapOn.getHeight(), matrix, true);
        bmpRotorOff = Bitmap.createBitmap(bitmapOff, 0, 0, bitmapOff.getWidth(), bitmapOff.getHeight(), matrix, true);

        ivKnob = new ImageView(context);
        ivKnob.setImageBitmap(bmpRotorOn);
        RelativeLayout.LayoutParams lpRotor = new RelativeLayout.LayoutParams(width, height);//LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lpRotor.addRule(RelativeLayout.CENTER_IN_PARENT);
        addView(ivKnob, lpRotor);

        setState(state);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (gestureDetector.onTouchEvent(event)) {
            return true;
        } else {
            return super.onTouchEvent(event);
        }
    }

    public boolean onDown(MotionEvent event) {
        float x = event.getX() / ((float) getWidth());
        float y = event.getY() / ((float) getHeight());
        angle = toDegrees(1 - x, 1 - y);
        //Log.d("TAG", "angle: " + Float.toString(angle));
        return true;
    }

    public boolean onScroll(MotionEvent motionEvent1, MotionEvent motionEvent2, float distanceX, float distanceY) {

        float x = motionEvent2.getX() / ((float) getWidth());
        float y = motionEvent2.getY() / ((float) getHeight());
        float rotationDegree = toDegrees(1 - x, 1 - y);

        //Log.d("TAG", "RotationDegree:" + Float.toString(rotationDegree));

        if (!Float.isNaN(rotationDegree)) {
            // instead of getting 0-> 180, -180 0 , we go for 0 -> 360
            float positionAngle = rotationDegree;
            if (rotationDegree < 0) {
                positionAngle = 360 + rotationDegree;
            }

            // deny full rotation, start start and stop point, and get a linear scale
            if (positionAngle > 210 || positionAngle < 150) {
                // rotate our imageview
                setPositionAngle(positionAngle);
                // get a linear scale
                float scaleDegrees = rotationDegree + 150; // given the current parameters, we go from 0 to 300
                // get position percent
                int percent = (int) (scaleDegrees / 3);
                if (listener != null) listener.onRotate(percent);
                return true;
            } else
                return false;
        } else {
            return false;
        }
    }

    public boolean onSingleTapUp(MotionEvent e) {

        float x = e.getX() / ((float) getWidth());
        float y = e.getY() / ((float) getHeight());
        float degrees = toDegrees(1 - x, 1 - y);
        Log.d("TAG", "Angle: " + Float.toString(angle) + "Degrees: " + Float.toString(degrees));
        // if we click up the same place where we clicked down, it's just a button press
        if (!Float.isNaN(angle) && !Float.isNaN(degrees) && Math.abs(degrees - angle) < 4) {
            Log.d("TAG", "State changed");
            setState(!state);
            if (listener != null) {
                listener.onStateChange(state);
            }
        }
        return true;
    }

    public void onShowPress(MotionEvent e) {
        // TODO Auto-generated method stub
    }

    public boolean onFling(MotionEvent arg0, MotionEvent arg1, float arg2, float arg3) {
        return false;
    }

    public void onLongPress(MotionEvent e) {

    }

    public void setPositionAngle(float deg) {

        if (deg >= 210 || deg <= 150) {
            if (deg > 180) {
                deg = deg - 360;
            }

            Matrix matrix = new Matrix();
            ivKnob.setScaleType(ImageView.ScaleType.MATRIX);
            matrix.postRotate(deg, getWidth() / 2, getHeight() / 2);
            ivKnob.setImageMatrix(matrix);
        }
    }

    public void setRotorPercentage(int percentage) {
        int positionAngle = percentage * 3 - 150;
        if (positionAngle < 0) {
            positionAngle = 360 + positionAngle;
        }
        setPositionAngle(positionAngle);
    }

    private float toDegrees(float x, float y) {
        return (float) -Math.toDegrees(Math.atan2(x - 0.5f, y - 0.5f));
    }
}