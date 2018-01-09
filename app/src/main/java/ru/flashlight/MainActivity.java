package ru.flashlight;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    private DisplayProvider displayProvider = DisplayProvider.getInstance();
    private Handler timerHandler = new Handler();
    private int volume;
    private Camera camera;
    private boolean state;
    private boolean isFlashOn;
    private boolean hasFlash;
    private Parameters params;
    private MediaPlayer mp;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();
        hasFlash = getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        if (!hasFlash) {
            AlertDialog alert = new AlertDialog.Builder(MainActivity.this).create();
            alert.setTitle(R.string.error);
            alert.setMessage(context.getString(R.string.no_flash_message));
            alert.setButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            alert.show();
            return;
        }
        initCamera();

        displayProvider.onCreate(this);

        RelativeLayout panel = new RelativeLayout(this);
        setContentView(panel);

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        final TextView tvPercentage = new TextView(this);
        tvPercentage.setText("");
        layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        panel.addView(tvPercentage, layoutParams);

        int width = displayProvider.Scale(250);
        int height = displayProvider.Scale(250);

        RotaryKnob rv = new RotaryKnob(this, R.drawable.knob_panel, R.drawable.knob_on, R.drawable.knob_off, width, height);
        layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        panel.addView(rv, layoutParams);

        rv.setRotorPercentage(50);

        rv.setListener(new RotaryKnobListener() {
            public void onStateChange(boolean state) {

                toggleState(state);
            }

            public void onRotate(final int volume) {

                tvPercentage.post(new Runnable() {
                    public void run() {
                        setVolume(volume);
                        tvPercentage.setText("\n" + volume + "%\n");
                    }
                });
            }
        });
    }

    private void setVolume(int volume){
        this.volume = volume;
        timerHandler.removeCallbacks(timerRunnable);
        timerHandler.postDelayed(timerRunnable, 0);
    }

    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            toggleFlash();
            int delay = 0;
            if (volume > 0) {
                delay = volume * 100;
                timerHandler.postDelayed(this, delay);
            }
        }
    };

    private void toggleState(boolean state) {
        if (camera == null || params == null) {
            return;
        }
        this.state = state;
        this.volume = 0;
        playSound();
        if (state) {
            timerHandler.postDelayed(timerRunnable, 0);
        } else {
            timerHandler.removeCallbacks(timerRunnable);
            turnFlashOff();
        }
    }

    private void toggleFlash() {
        if (isFlashOn) {
            turnFlashOff();
        } else {
            turnFlashOn();
        }
    }

    private void turnFlashOn() {
        if (state) {
            try {
                params.setFlashMode(Parameters.FLASH_MODE_TORCH);
                camera.setParameters(params);
                camera.startPreview();
                isFlashOn = true;
            } catch (NullPointerException nex) {
                nex.printStackTrace();
            }
        }
    }

    private void turnFlashOff() {
        try {
            params.setFlashMode(Parameters.FLASH_MODE_OFF);
            camera.setParameters(params);
            camera.stopPreview();
            isFlashOn = false;
        } catch (NullPointerException nex) {
            nex.printStackTrace();
        }
    }

    private void initCamera() {
        if (camera == null) {
            try {
                camera = Camera.open();
                params = camera.getParameters();
            } catch (RuntimeException e) {
                Log.e("TAG", e.getMessage());
            }
        }
    }

    private void playSound() {
        mp = MediaPlayer.create(MainActivity.this, R.raw.switch_sound);
        mp.setOnCompletionListener(new OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                // TODO Auto-generated method stub
                mp.release();
            }
        });
        mp.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // on pause turn off the flash
        //turnOffFlash();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // on resume turn on the flash
        //if (hasFlash)
            //turnOnFlash();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // on starting the app get the camera params
        //getCamera();
    }

    @Override
    protected void onStop() {
        super.onStop();

        // on stop release the camera
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }
}