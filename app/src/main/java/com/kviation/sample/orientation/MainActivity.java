package com.kviation.sample.orientation;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements Orientation.Listener {

  private Orientation mOrientation;
  private AttitudeIndicator mAttitudeIndicator;
  private BufferedWriter file;
  private static long startTime;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    mOrientation = new Orientation(this);
    mAttitudeIndicator = (AttitudeIndicator) findViewById(R.id.attitude_indicator);

    // file prepare
      File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
      String name = "sensor_export.csv";
      File filename = new File(directory, name);
      try {
          this.file = new BufferedWriter(new FileWriter(filename));
      } catch (IOException e) {
          e.printStackTrace();
      }
  }

  @Override
  protected void onStart() {
    super.onStart();
    startTime = System.currentTimeMillis();
    mOrientation.startListening(this);
  }

  @Override
  protected void onStop() {
    super.onStop();
    mOrientation.stopListening();
    try {
        file.close();
    } catch (IOException e) {
        e.printStackTrace();
    }
  }

  @Override
  public void onOrientationChanged(float azimuth, float pitch, float roll) {
      long recordingTime = System.currentTimeMillis() - startTime;
      mAttitudeIndicator.setAttitude(pitch, roll);
    write(recordingTime, new float[]{azimuth, pitch, roll});
  }

  private void write(long recordingTime, float[] values) {
      if (this.file == null) {
          return;
      }

      StringBuilder line = new StringBuilder();
      if (values != null) {
          for (float value : values) {
              line.append(",").append(Float.toString(value));
          }
      }

      String finalLine = Long.toString(recordingTime) + line.toString();

      try {
          file.write(finalLine);
          file.newLine();
      } catch (IOException e) {
          e.printStackTrace();
      }
  }
}
