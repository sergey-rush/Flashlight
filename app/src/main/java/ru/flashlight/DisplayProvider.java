package ru.flashlight;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.DisplayMetrics;

/**
 * Created by sergey-rush on 09.01.2018.
 */

public class DisplayProvider extends Application {

    private static DisplayProvider instance;
    public float scaleRatio = 0;
    public int frameWidth = 0;
    public int frameHeight = 0;
    public int width = 0;
    public int height = 0;
    public int xPadding = 0;
    public int yPadding = 0;

    public DisplayProvider() {
        super();
        instance = this;
    }

    public static DisplayProvider getInstance() {
        if(instance == null) {
            synchronized(DisplayProvider.class) {
                if(instance == null) new DisplayProvider();
            }
        }
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public void onCreate(Activity context) {
        DisplayMetrics dm = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(dm);
        width = dm.widthPixels;
        height = dm.heightPixels;
        // scaleRatio factor
        scaleRatio = (float) width / 640.0f;

        frameWidth = width;
        frameHeight = (int) (960.0f * scaleRatio);
        yPadding = 0;
        xPadding = (width - frameWidth) / 2;
    }

    public int Scale(int value) {
        float result = (float) value * scaleRatio;
        int output = 0;
        if (result - (int) result >= 0.5) {
            output = ((int) result) + 1;
        } else {
            output = (int) result;
        }
        return output;
    }

    public Bitmap getScaledBitmap(Context context, float scaleRatiox, float scaleRatioy, int id) {
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), id);
        Matrix matrix = new Matrix();
        matrix.postScale(scaleRatiox, scaleRatioy);
        matrix.postRotate(0);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }
}

