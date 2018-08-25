
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

  @Nullable
  private final Sensor mRotationSensor;

  private int mLastAccuracy;
  private Listener mListener;

  public Orientation(Activity activity) {
    mWindowManager = activity.getWindow().getWindowManager();
    mSensorManager = (SensorManager) activity.getSystemService(Activity.SENSOR_SERVICE);

    // Can be null if the sensor hardware is not available
    mRotationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR);
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

  @SuppressWarnings("SuspiciousNameCombination")
  private void updateOrientation(float[] rotationVector) {
    float[] rotationMatrix = new float[9];
    SensorManager.getRotationMatrixFromVector(rotationMatrix, rotationVector);

    // Transform rotation matrix into azimuth/pitch/roll
    float[] orientation = new float[3];
    SensorManager.getOrientation(rotationMatrix, orientation);

    // Convert radians to degrees
    float azimuth = orientation[0];// * -57;
    float pitch = orientation[1]; // * -57;
    float roll = orientation[2]; // * -57;

    mListener.onOrientationChanged(azimuth, pitch, roll);
  }
}

