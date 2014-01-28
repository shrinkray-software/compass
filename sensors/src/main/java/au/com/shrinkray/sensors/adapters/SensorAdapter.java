package au.com.shrinkray.sensors.adapters;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.TriggerEvent;
import android.hardware.TriggerEventListener;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import au.com.shrinkray.sensors.R;

/**
 * Created by neal on 27/01/2014.
 */
public class SensorAdapter extends BaseAdapter implements SensorEventListener {

    private Activity mActivity;
    private SensorManager mSensorManager;

    private TriggerEventListener mTriggerEventListener;

    private List<SensorWrapper> mSensors =  new ArrayList<SensorWrapper>();
    private SensorWrapper[] mSensorsByType = new SensorWrapper[128];

    public SensorAdapter( Activity activity ) {

        mActivity = activity;
        mSensorManager = (SensorManager) mActivity.getSystemService(Context.SENSOR_SERVICE);

        mTriggerEventListener = new TriggerEventListener() {
            @Override
            public void onTrigger(TriggerEvent event) {

            }
        };

    }

    @Override
    public int getCount() {
        return mSensors.size();
    }

    @Override
    public Object getItem(int position) {
        return mSensors.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void addSensor(int sensorType) {

        if ( sensorType < mSensorsByType.length && mSensorsByType[sensorType] == null ) {

            Sensor sensor = mSensorManager.getDefaultSensor(sensorType);

            if ( sensor == null ) {
                return;
            }

            SensorWrapper sensorWrapper = new SensorWrapper(sensor);

            mSensors.add(sensorWrapper);
            mSensorsByType[sensorType] = sensorWrapper;

            if ( sensor.getType() == Sensor.TYPE_SIGNIFICANT_MOTION ) {

            } else {
                mSensorManager.registerListener(this,sensor,SensorManager.SENSOR_DELAY_UI);
            }

        }

    }

    public void removeSensor(int sensorType) {

        for ( SensorWrapper sensor : mSensors ) {
            if ( sensor.getSensor().getType() == sensorType ) {
                mSensorManager.unregisterListener(this,sensor.getSensor());
            }
        }

    }

    public void registerListeners() {
        for ( SensorWrapper sensor : mSensors ) {
            mSensorManager.registerListener(this,sensor.getSensor(),SensorManager.SENSOR_DELAY_UI);
        }
    }

    public void unregisterListeners() {
        mSensorManager.unregisterListener(this);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {

        if ( view == null ) {
            view = mActivity.getLayoutInflater().inflate(R.layout.list_item_sensor,null);

            SensorViewHolder _viewHolder = new SensorViewHolder();

            ViewGroup sensorValuesLayout  = (ViewGroup) view.findViewById(R.id.layoutSensorValues);

            List<TextView> sensorValueTextViews = new ArrayList<TextView>();

            for ( int i=0; i < sensorValuesLayout.getChildCount(); i++ ) {
                View sensorValuesChild = sensorValuesLayout.getChildAt(i);
                if ( sensorValuesChild instanceof TextView ) {
                    sensorValueTextViews.add((TextView)sensorValuesChild);
                }
            }

            _viewHolder.sensorValueTextViews = sensorValueTextViews.toArray(new TextView[sensorValueTextViews.size()]);
            _viewHolder.descriptionTextView  = (TextView) view.findViewById(R.id.txtDescription);
            _viewHolder.vendorTextView = (TextView) view.findViewById(R.id.txtVendor);
            _viewHolder.powerTextView = (TextView) view.findViewById(R.id.txtPower);

            view.setTag(_viewHolder);
        }

        SensorViewHolder viewHolder = (SensorViewHolder) view.getTag();

        SensorWrapper sensorWrapper = mSensors.get(position);
        viewHolder.descriptionTextView.setText( sensorWrapper.getSensor().getName() );

        SensorEvent lastSensorEvent = sensorWrapper.getLastEvent();

        if ( lastSensorEvent != null) {

            for ( int i=0; i < viewHolder.sensorValueTextViews.length; i++ ) {
                if ( i < lastSensorEvent.values.length ){
                    viewHolder.sensorValueTextViews[i].setVisibility(View.VISIBLE);
                    viewHolder.sensorValueTextViews[i].setText(String.format("%,.3f", lastSensorEvent.values[i]));                } else {
                    viewHolder.sensorValueTextViews[i].setVisibility(View.GONE);
                    viewHolder.vendorTextView.setText(sensorWrapper.getSensor().getVendor());
                    viewHolder.powerTextView.setText(String.format("%fmA",sensorWrapper.getSensor().getPower()));
                }
            }
        }

        return view;
    }

    public static class SensorViewHolder {
        TextView[] sensorValueTextViews;
        TextView descriptionTextView;
        TextView vendorTextView;
        TextView powerTextView;
    }


    @Override
    public void onSensorChanged(SensorEvent event) {

        mSensorsByType[event.sensor.getType()].addSensorEvent(event);

        notifyDataSetChanged();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public static class SensorWrapper {

        private Sensor mSensor;
        private LinkedList<SensorEvent> mSensorEvents;

        public SensorWrapper(Sensor sensor) {
            mSensor = sensor;
            mSensorEvents = new LinkedList<SensorEvent>();
        }

        public void addSensorEvent( SensorEvent sensorEvent ) {
            mSensorEvents.push(sensorEvent);

            if ( mSensorEvents.size() > 4 ) {
                mSensorEvents.removeLast();
            }
        }

        public Sensor getSensor() {
            return mSensor;
        }

        public SensorEvent getLastEvent() {
            if ( mSensorEvents.size() > 0 ) {
                return mSensorEvents.getFirst();
            } else {
                return null;
            }
        }
    }

}
