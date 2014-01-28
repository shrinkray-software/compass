package au.com.shrinkray.sensors.activities;

import android.app.Activity;
import android.app.Fragment;
import android.hardware.Sensor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import au.com.shrinkray.sensors.R;
import au.com.shrinkray.sensors.adapters.SensorAdapter;

public class SensorsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sensors);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.sensors, menu);
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
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        private SensorAdapter mSensorAdapter;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_sensors, container, false);

            ListView sensorList = (ListView) rootView.findViewById(R.id.listSensors);

            mSensorAdapter = new SensorAdapter(getActivity());

            mSensorAdapter.addSensor(Sensor.TYPE_STEP_COUNTER);
            mSensorAdapter.addSensor(Sensor.TYPE_STEP_DETECTOR);
            mSensorAdapter.addSensor(Sensor.TYPE_ACCELEROMETER);
            mSensorAdapter.addSensor(Sensor.TYPE_MAGNETIC_FIELD);
            mSensorAdapter.addSensor(Sensor.TYPE_LIGHT);
            mSensorAdapter.addSensor(Sensor.TYPE_LINEAR_ACCELERATION);

            // sensorAdapter.addSensor(Sensor.TYPE_PRESSURE);
            // sensorAdapter.addSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
            // sensorAdapter.addSensor(Sensor.TYPE_GYROSCOPE);
            // sensorAdapter.addSensor(Sensor.TYPE_LINEAR_ACCELERATION);
            // sensorAdapter.addSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR);
            // sensorAdapter.addSensor(Sensor.TYPE_GRAVITY);
            // sensorAdapter.addSensor(Sensor.TYPE_LIGHT);

            sensorList.setAdapter(mSensorAdapter);

            return rootView;

        }

        @Override
        public void onStart() {
            super.onStart();

            mSensorAdapter.registerListeners();
        }

        @Override
        public void onStop() {
            mSensorAdapter.unregisterListeners();

            super.onStop();
        }
    }

}
