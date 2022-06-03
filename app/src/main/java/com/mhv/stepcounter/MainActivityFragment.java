package com.mhv.stepcounter;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;


/**
 *
 */
@RequiresApi(api = Build.VERSION_CODES.N)
public class MainActivityFragment extends Fragment implements SensorEventListener {

    //Sensor variables
    private SensorManager sensorManager;
    private Sensor stepDetectorSensor;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private float[] accelValues;
    private float[] magnetValues;

    //Class attributes
    private int stepCount = 0;
    private long stepTimestamp = 0;
    private long startTime = 0;
    long timeInMilliseconds = 0;
    long elapsedTime = 0;
    long updatedTime = 0;
    private double distance = 0;

    //Component declaration
    private TextView dayRecordText;
    private TextView stepText;
    private TextView timeText;
    private TextView orientationText;
    private TextView distanceText;
    private TextView achievedText;
    private TextView speedText;
    private TextView date;

    //Date
    SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
    String today = format.format(new Date());

    DBHelper db; //Database object
    private boolean active = false; //Checks if counting is active
    private final Handler handler = new Handler(); //Used for showing time

    //Used to show record of the day
    private SharedPreferences sharedPreferences;
    private int dayStepRecord;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        if (stepDetectorSensor == null)
            showErrorDialog();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        //Initialization of text in labels
        dayRecordText = view.findViewById(R.id.dayRecordText);
        stepText = view.findViewById(R.id.stepText);
        timeText = view.findViewById(R.id.timeText);
        speedText = view.findViewById(R.id.speedText);
        distanceText = view.findViewById(R.id.distanceText);
        orientationText = view.findViewById(R.id.orientationText);
        achievedText = view.findViewById(R.id.achievedText);
        date = view.findViewById(R.id.dateLabel);
        db = new DBHelper(getContext());
        setViewDefaultValues();

        //Start button event
        final Button startButton = view.findViewById(R.id.startButton);
        if (startButton != null) {
            startButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!active) {
                        startButton.setText(R.string.pause);
                        startButton.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.darkGray));
                        sensorManager.registerListener(MainActivityFragment.this, stepDetectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
                        sensorManager.registerListener(MainActivityFragment.this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
                        sensorManager.registerListener(MainActivityFragment.this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
                        startTime = SystemClock.uptimeMillis();
                        handler.postDelayed(timerRunnable, 0);
                        active = true;

                    } else {
                        startButton.setText(R.string.start);
                        startButton.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.lightGray));
                        sensorManager.unregisterListener(MainActivityFragment.this, stepDetectorSensor);
                        sensorManager.unregisterListener(MainActivityFragment.this, accelerometer);
                        sensorManager.unregisterListener(MainActivityFragment.this, magnetometer);
                        elapsedTime += timeInMilliseconds;
                        handler.removeCallbacks(timerRunnable);
                        active = false;
                    }
                }
            });
        }

        //Reset button event
        Button resetButton = view.findViewById(R.id.resetButton);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                if (startButton.getText().equals("START!")) {
                    setViewDefaultValues();
                    stepCount = 0;
                    distance = 0;
                    elapsedTime = 0;
                }else {
                    Toast.makeText(getContext(), "Click the pause button first.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Settings button event
        Button settingsButton = view.findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(intent);
            }
        });

        //Insert button event
        Button DBButton = view.findViewById(R.id.DBButton);
        DBButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    if (stepCount > 0) {
                        String stepsTXT = String.valueOf(stepCount);
                        String dateTXT = today;

                        Boolean checkInsertData = db.insertData(stepsTXT, dateTXT);
                        if (checkInsertData)
                            Toast.makeText(getContext(), "New Entry Inserted", Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(getContext(), "New Entry Not Inserted", Toast.LENGTH_SHORT).show();
                        stepCount = 0;
                        distance = 0;
                        elapsedTime = 0;
                        setViewDefaultValues();
                    }else {
                        Toast.makeText(getContext(), "New Entry Not Inserted", Toast.LENGTH_SHORT).show();
                    }
            }
        });

        //View button event
        Button DBViewer = view.findViewById(R.id.DBViewer);
        DBViewer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Cursor res = db.getData();
                if(res.getCount()==0){
                    Toast.makeText(getContext(), "No Entry Exists", Toast.LENGTH_SHORT).show();
                    return;
                }
                StringBuilder buffer = new StringBuilder();
                while(res.moveToNext()){
                    buffer.append("Steps: ").append(res.getString(0)).append("\n");
                    buffer.append("Date: ").append(res.getString(1)).append("\n\n");
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setCancelable(true);
                builder.setTitle("User Entries");
                builder.setMessage(buffer.toString());
                builder.show();
            }
        });
        return view;
    }


    //Sets attributes to default values
    private void setViewDefaultValues() {

        stepText.setText(String.format(getResources().getString(R.string.steps), 0));
        timeText.setText(String.format(getResources().getString(R.string.time), "0:00:00"));
        speedText.setText(String.format(getResources().getString(R.string.speed), 0));
        distanceText.setText(String.format(getResources().getString(R.string.distance), "0"));
        orientationText.setText(String.format(getResources().getString(R.string.orientation), ""));
        date.setText(String.format("Today's date: %s",today));
    }


    @Override
    public void onResume() {
        super.onResume();
        dayStepRecord = sharedPreferences.getInt(SettingsActivity.DAY_STEP_RECORD, 3) * 1000;
        dayRecordText.setText(String.format(getResources().getString(R.string.record), dayStepRecord));
    }


    @Override
    public void onPause() {
        super.onPause();
        //Shuts down sensor activity
        sensorManager.unregisterListener(this, accelerometer);
        sensorManager.unregisterListener(this, magnetometer);
        sensorManager.unregisterListener(this, stepDetectorSensor);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {

        //Takes sensor values
        switch (event.sensor.getType()) {
            case (Sensor.TYPE_ACCELEROMETER):
                accelValues = event.values;
                break;
            case (Sensor.TYPE_MAGNETIC_FIELD):
                magnetValues = event.values;
                break;
            case (Sensor.TYPE_STEP_DETECTOR):
                countSteps(event.values[0]);
                calculateSpeed(event.timestamp);
                break;
        }

        if (accelValues != null && magnetValues != null) {
            float[] rotation = new float[9];
            float[] orientation = new float[3];
            if (SensorManager.getRotationMatrix(rotation, null, accelValues, magnetValues)) {
                SensorManager.getOrientation(rotation, orientation);
                float azimuthDegree = (float) (Math.toDegrees(orientation[0]) + 360) % 360;
                float orientationDegree = Math.round(azimuthDegree);
                getOrientation(orientationDegree);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //Not used
    }


    //Function counts steps and calculates distance
    private void countSteps(float step) {

        //Counting steps
        stepCount += (int) step;
        stepText.setText(String.format(getResources().getString(R.string.steps), stepCount));

        //Calculates distance
        distance = stepCount * 0.8; //Average step distance of an adult
        String distanceString = String.format("%.2f", distance);
        distanceText.setText(String.format(getResources().getString(R.string.distance), distanceString));

        //Record of the day
        if (stepCount >= dayStepRecord)
            achievedText.setVisibility(View.VISIBLE);
    }


    //Calculates steps per minute
    private void calculateSpeed(long eventTimeStamp) {
        long timestampDifference = eventTimeStamp - stepTimestamp;
        stepTimestamp = eventTimeStamp;
        double stepTime = timestampDifference / 1000000000.0;
        int speed = (int) (60 / stepTime);
        speedText.setText(String.format(getResources().getString(R.string.speed), speed));
    }


    //Shows the orientation
    private void getOrientation(float orientationDegree) {
        String compassOrientation;
        if (orientationDegree >= 0 && orientationDegree < 90) {
            compassOrientation = "North";
        } else if (orientationDegree >= 90 && orientationDegree < 180) {
            compassOrientation = "East";
        } else if (orientationDegree >= 180 && orientationDegree < 270) {
            compassOrientation = "South";
        } else {
            compassOrientation = "West";
        }
        orientationText.setText(String.format(getResources().getString(R.string.orientation), compassOrientation));
    }


    //Shows the time that passed since the start
    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            timeInMilliseconds = SystemClock.uptimeMillis() - startTime;

            updatedTime = elapsedTime + timeInMilliseconds;

            int seconds = (int) (updatedTime / 1000);
            int minutes = seconds / 60;
            int hours = minutes / 60;
            seconds = seconds % 60;
            minutes = minutes % 60;

            String timeString = String.format("%d:%s:%s", hours, String.format("%02d", minutes), String.format("%02d", seconds));

            if (isAdded()) {
                timeText.setText(String.format(getResources().getString(R.string.time), timeString));
            }
            handler.postDelayed(this, 0);
        }
    };


    //Dialog window shows that device doesn't have the necessary hardware
    private void showErrorDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setMessage("Necessary step sensors not available!");

        alertDialogBuilder.setPositiveButton("Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getActivity().finish();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }


}


