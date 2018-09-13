
package com.kviation.sample.orientation;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.annotation.Nullable;
import android.view.Surface;
import android.view.WindowManager;

public class Orientation implements SensorEventListener {

  public interface Listener {
    void onOrientationChanged(float azimuth, float pitch, float roll);
  }

  private static final int SENSOR_DELAY_MICROS = 10 * 1000; // 10ms

  private final WindowManager mWindowManager;

  private final SensorManager mSensorManager;

  private int CountDown = 5;
  private float StartAzi = 0;
  private float StartPit = 0;
  private float StartRol = 0;

  @Nullable
  private final Sensor mRotationSensor;

  private int mLastAccuracy;
  private Listener mListener;

  public Orientation(Activity activity) {
    mWindowManager = activity.getWindow().getWindowManager();
    mSensorManager = (SensorManager) activity.getSystemService(Activity.SENSOR_SERVICE);

    // Can be null if the sensor hardware is not available
    mRotationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
  }

  public void startListening(Listener listener) {
    if (mListener == listener) {
      return;
    }
    mListener = listener;
    if (mRotationSensor == null) {
      LogUtil.w("Rotation vector sensor not available; will not provide orientation data.");
      return;
    }
    mSensorManager.registerListener(this, mRotationSensor, SENSOR_DELAY_MICROS);
  }

  public void stopListening() {
    mSensorManager.unregisterListener(this);
    mListener = null;
  }

  @Override
  public void onAccuracyChanged(Sensor sensor, int accuracy) {
    if (mLastAccuracy != accuracy) {
      mLastAccuracy = accuracy;
    }
  }

  @Override
  public void onSensorChanged(SensorEvent event) {
    if (mListener == null) {
      return;
    }
    if (mLastAccuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
      return;
    }
    if (event.sensor == mRotationSensor) {
      updateOrientation(event.values);
    }
  }

  private float checkOverflow(float orient) {
    if (orient < -3) {
      return (orient + 6);
    } else if (orient > 3) {
      return (orient - 6);
    }
    return orient;
  }

  @SuppressWarnings("SuspiciousNameCombination")
  private void updateOrientation(float[] rotationVector) {
    float[] rotationMatrix = new float[9];
    SensorManager.getRotationMatrixFromVector(rotationMatrix, rotationVector);

    // Transform rotation matrix into azimuth/pitch/roll
    float[] orientation = new float[3];
    SensorManager.getOrientation(rotationMatrix, orientation);

    float azimuth = orientation[0];
    float pitch = orientation[1];
    float roll = orientation[2];

    if (CountDown == 0) {
      if (StartAzi == 0) {
        StartAzi = orientation[0];
        StartPit = orientation[1];
        StartRol = orientation[2];
      } else {
        azimuth = this.checkOverflow(orientation[0] - StartAzi);
        pitch = this.checkOverflow(orientation[1] - StartPit);
        roll = this.checkOverflow(orientation[2] - StartRol);

      }
    } else {
      CountDown--;
    }

    mListener.onOrientationChanged(azimuth, pitch, roll);
  }
}

