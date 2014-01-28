package au.com.shrinkray.sensors.activities;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import au.com.shrinkray.sensors.R;
import au.com.shrinkray.sensors.views.CompassView;

public class CompassActivity extends Activity implements SensorEventListener {

    private SensorManager mSensorManager;

    private Sensor mStepCounterSensor;
    private Sensor mStepDetectorSensor;
    private Sensor mAccelerometerSensor;
    private Sensor mMagneticFieldSensor;

    // 33.8600° S, 151.2111° E
    private GeomagneticField mGeomagneticField =
            new GeomagneticField(33.86f,151.2111f,0,System.currentTimeMillis());

    private int mInitialStepCount = -1;
    private int mStepCount = 0;

    private SensorEvent mLastAccelerometerEvent;
    private SensorEvent mLastMagneticFieldEvent;


    private float[] mRotationMatrix = new float[9];
    private float[] mRemappedRotationMatrix = new float[9];
    private float[] mInclinationMatrix = new float[9];
    private float[] mOrientation = new float[3];

    private float mAzimuth;
    private float mPitch;
    private float mRoll;

    private float mLastSignificantAzimuth;

    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);

        outState.putFloat("azimuth",mAzimuth);
        outState.putFloat("pitch",mPitch);
        outState.putFloat("roll",mRoll);
        outState.putInt("initialStepCount", mInitialStepCount);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_treasure_map);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new CompassFragment())
                    .commit();
        }

        if ( savedInstanceState != null ) {
            mAzimuth = savedInstanceState.getFloat("azimuth",0);
            mPitch = savedInstanceState.getFloat("pitch",0);
            mRoll = savedInstanceState.getFloat("roll",0);
            mInitialStepCount = savedInstanceState.getInt("stepCount",-1);
        }

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        mStepCounterSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        mStepDetectorSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagneticFieldSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        mSensorManager.registerListener(this, mStepCounterSensor, SensorManager.SENSOR_DELAY_UI, 5000 * 1000);

    }

    @Override
    protected void onStart() {

        super.onStart();

        mSensorManager.unregisterListener(this, mStepCounterSensor);

        mSensorManager.registerListener(this, mStepCounterSensor, SensorManager.SENSOR_DELAY_UI, 500 * 1000);

        mSensorManager.registerListener(this, mAccelerometerSensor, SensorManager.SENSOR_DELAY_UI, 200 * 1000);
        mSensorManager.registerListener(this, mMagneticFieldSensor,SensorManager.SENSOR_DELAY_UI, 200 * 1000);

    }

    @Override
    protected void onStop() {

        mSensorManager.unregisterListener(this,mStepCounterSensor);

        mSensorManager.registerListener(this, mStepCounterSensor, SensorManager.SENSOR_DELAY_UI, 5000 * 1000);

        mSensorManager.unregisterListener(this,mAccelerometerSensor);
        mSensorManager.unregisterListener(this,mMagneticFieldSensor);

        super.onStop();
    }

    @Override
    protected void onDestroy() {

        // Unless we intend to take a wake lock for the life of the activity, keeping
        // this sensor attached is somewhat moot.
        // mSensorManager.unregisterListener(this,mStepDetectorSensor);
        mSensorManager.unregisterListener(this,mStepCounterSensor);

        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.compass, menu);

        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_raw_sensors) {
            Intent intent = new Intent(this,SensorsActivity.class);

            startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        CompassFragment compassFragment = (CompassFragment)getFragmentManager().findFragmentById(R.id.container);

        if ( event.sensor == mStepCounterSensor) {

            if ( mInitialStepCount == -1 ) {
                mInitialStepCount = (int)event.values[0];
                mStepCount = 0;
            } else {
                mStepCount = ((int)event.values[0]) - mInitialStepCount;
            }

            if ( compassFragment != null ) {
                compassFragment.updateUI(mAzimuth, mPitch, mRoll, mStepCount);
            }

            return;

        }

        if ( event.sensor == mAccelerometerSensor ) {
            mLastAccelerometerEvent = event;
        } else if ( event.sensor == mMagneticFieldSensor) {
            mLastMagneticFieldEvent = event;   
        }

        // Update the compass heading.
        if ( mLastAccelerometerEvent != null && mLastMagneticFieldEvent != null  ) {

            // Compute the rotation matrix.
            boolean success = SensorManager.getRotationMatrix(
                    mRotationMatrix,null,mLastAccelerometerEvent.values,mLastMagneticFieldEvent.values);

//            success &= SensorManager.remapCoordinateSystem(mRotationMatrix,
//                    SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, mRemappedRotationMatrix);

            if ( success ) {
                // Update the orientation values.
                mOrientation = SensorManager.getOrientation(mRemappedRotationMatrix,mOrientation);

                mAzimuth = mOrientation[0];
                mPitch = mOrientation[1];
                mRoll = mOrientation[2];

                if ( compassFragment != null ) {
                    compassFragment.updateUI(mAzimuth, mPitch, mRoll, mStepCount);
                }
            }

        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class CompassFragment extends Fragment {

        private TextView mAzimuthTextView;
        private TextView mPitchTextView;
        private TextView mRollTextView;

        private CompassView mCompassView;

        private TextView mStepCountTextView;

        private float mLastCompassAzimuthDegrees;

        public CompassFragment() {

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_compass, container, false);

            mAzimuthTextView = (TextView) rootView.findViewById(R.id.txtAzimuth);
            mPitchTextView = (TextView) rootView.findViewById(R.id.txtPitch);
            mRollTextView = (TextView) rootView.findViewById(R.id.txtRoll);

            mCompassView = (CompassView)rootView.findViewById(R.id.compass);

            mStepCountTextView = (TextView) rootView.findViewById(R.id.txtStepCount);

            updateUI(0,0,0,0);

            return rootView;

        }

        public void updateUI(float azimuth,float pitch, float roll, int stepCount) {

            float azimuthDegrees = (float)Math.toDegrees(azimuth);

            mAzimuthTextView.setText(String.format("%.3f\u00b0", azimuthDegrees));
            mPitchTextView.setText(String.format("%.3f\u00b0", Math.toDegrees(pitch)));
            mRollTextView.setText(String.format("%.3f\u00b0", Math.toDegrees(roll)));

            mStepCountTextView.setText(Integer.toString(stepCount));

            if ( Math.abs(mLastCompassAzimuthDegrees - azimuthDegrees) > 2.0f ) {
                mLastCompassAzimuthDegrees = azimuthDegrees;
                mCompassView.setAzimuth(azimuthDegrees);
            }

        }
    }

}
