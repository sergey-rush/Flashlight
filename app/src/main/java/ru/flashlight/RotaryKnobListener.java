package ru.flashlight;

/**
 * Created by sergey-rush on 09.01.2018.
 */

interface RotaryKnobListener {
    public void onStateChange(boolean state) ;
    public void onRotate(int volume);
}
