package net.erabbit.blesensor;

import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.AlertDialog;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import net.erabbit.bluetooth.BleDeviceMsgHandler;
import net.erabbit.common_lib.WaveformView;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Tom on 16/7/21.
 */
public class IoTSensorActivity extends AppCompatActivity
        implements BleDeviceMsgHandler.BleDeviceMsgListener, View.OnClickListener {

    protected class FeatureViewHolder {
        TextView featureName;
        TextView featureValue;
        Switch featureSwitch;

        public FeatureViewHolder(View cell) {
            featureName = (TextView) cell.findViewById(R.id.featureName);
            featureValue = (TextView) cell.findViewById(R.id.featureValue);
            featureSwitch = (Switch) cell.findViewById(R.id.featureSwitch);
        }

        public void reset(DialogIoTSensor.SensorFeature feature) {
            featureName.setText(feature.name());
            featureSwitch.setTag(feature);
            updateStatus(feature);
            updateValue(feature);
        }

        public void updateValue(DialogIoTSensor.SensorFeature feature) {
            featureValue.setText(getSensorFeatureValueString(feature));
        }

        public void updateStatus(DialogIoTSensor.SensorFeature feature) {
            featureSwitch.setChecked(feature.isEnabled());
        }
    }

    protected class FeatureAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {

        @Override
        public int getCount() {
            return sensor.getFeatureCount();
        }

        @Override
        public Object getItem(int position) {
            return sensor.getFeature(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            curFeatureIndex = position;
            DialogIoTSensor.SensorFeature feature = (DialogIoTSensor.SensorFeature) getItem(position);
            featureFragment.show(feature);
            if(waveformView == null)
                waveformView = featureFragment.getWaveformView();
            if(feature == DialogIoTSensor.SensorFeature.MAGNETOMETER)
                waveformView.setGrids(0, 0);
            else
                waveformView.clearGrids();
            featureFragment.getFeatureSwitch().setChecked(feature.isEnabled() && allSensorSwitch.isChecked());
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View cell = convertView;
            FeatureViewHolder holder;
            if (cell == null) {
                cell = getLayoutInflater().inflate(R.layout.iot_sensor_feature_item, parent, false);
                Switch featureSwitch = (Switch) cell.findViewById(R.id.featureSwitch);
                featureSwitch.setOnClickListener(IoTSensorActivity.this);
                holder = new FeatureViewHolder(cell);
                cell.setTag(holder);
            }
            else
                holder = (FeatureViewHolder)cell.getTag();
            DialogIoTSensor.SensorFeature feature = (DialogIoTSensor.SensorFeature) getItem(position);
            holder.reset(feature);
            return cell;
        }
    }

    protected BluetoothDevice btDevice;

    protected DialogIoTSensor sensor;
    protected ListView featureList;
    protected FeatureAdapter featureAdapter;

    protected int curFeatureIndex= -1;

    protected Switch allSensorSwitch;
    protected FeatureFragment featureFragment;
    protected WaveformView waveformView;

    protected AlertDialog progressDlg;

    protected BleDeviceMsgHandler deviceHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        btDevice = getIntent().getParcelableExtra("BluetoothDevice");
        if(btDevice == null)
            finish();
        sensor = (DialogIoTSensor)BLESensorApp.getInstance().findDevice(btDevice.getAddress());
        if(sensor == null) {
            sensor = new DialogIoTSensor(btDevice);
            BLESensorApp.getInstance().getDevices().add(sensor);
        }
        setContentView(R.layout.activity_iot_sensor);
        featureList = (ListView)findViewById(R.id.featureList);
        featureAdapter = new FeatureAdapter();
        featureList.setAdapter(featureAdapter);
        featureList.setOnItemClickListener(featureAdapter);
        allSensorSwitch = (Switch)findViewById(R.id.allSensorSwitch);
        if(allSensorSwitch != null)
            allSensorSwitch.setOnClickListener(this);
        setTitle(sensor.getBtName("IoT Sensor"));
        deviceHandler = new BleDeviceMsgHandler(this);
        if(!sensor.isConnected()) {
            progressDlg = ProgressDialog.show(this, getString(R.string.connecting_title), getString(R.string.connecting_msg), true, true, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    finish();
                }
            });
            sensor.connect(this, deviceHandler);
        }
        FragmentManager fragmentManager = getFragmentManager();
        featureFragment = (FeatureFragment)fragmentManager.findFragmentById(R.id.featureDetail);
        featureFragment.hide();
    }

    @Override
    public void finish() {
        if(sensor != null) {
            if(sensor.isConnected()) {
                if(featureFragment.isVisible()) {
                    featureFragment.hide();
                    return;
                }
                else
                    sensor.disconnect();
            }
        }
        super.finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDeviceReady(String btAddress) {
        progressDlg.dismiss();
        TextView versionText = (TextView)findViewById(R.id.versionText);
        if(versionText != null)
            versionText.setText(getString(R.string.firmware_version, sensor.getFirmwareVersion()));
        featureAdapter.notifyDataSetChanged();
    }

    @Override
    public void onConnectFailed(String btAddress) {

    }

    @Override
    public void onConnectEnded(String btAddress) {
        progressDlg.dismiss();
        Toast.makeText(this, R.string.disconnected_msg, Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onReceivedData(byte[] data) {

    }

    @Override
    public void onValueChanged(String btAddress, int valueId, int valueParam) {
        switch(valueId) {
            case DialogIoTSensor.VALUE_OF_SENSOR_SWITCH: {
                allSensorSwitch.setChecked(valueParam > 0);
            }
                break;
            case DialogIoTSensor.VALUE_OF_SENSOR_FEATURE: {
                int position = valueParam;
                DialogIoTSensor.SensorFeature sensorFeature = sensor.getFeature(position);
                FeatureViewHolder vh = getFeatureViewHolder(position);
                if(vh != null)
                    vh.updateValue(sensorFeature);
                if((curFeatureIndex == position) && (waveformView != null)) {
                    waveformView.addValues(sensorFeature.getValues(), 1);
                    featureFragment.curValue.setText(getSensorFeatureValueString(sensorFeature));
                    featureFragment.maxValue.setText(getString(R.string.max, sensorFeature.getValueString(waveformView.getMaxValue())));
                    featureFragment.minValue.setText(getString(R.string.min, sensorFeature.getValueString(waveformView.getMinValue())));
                }
            }
                break;
        }
    }

    protected FeatureViewHolder getFeatureViewHolder(int position) {
        int firstVisiblePosition = featureList.getFirstVisiblePosition();
        int lastVisiblePosition = featureList.getLastVisiblePosition();
        if ((position >= firstVisiblePosition) && (position <= lastVisiblePosition)) {
            View view = featureList.getChildAt(position - firstVisiblePosition);
            if (view.getTag() instanceof FeatureViewHolder)
                return (FeatureViewHolder) view.getTag();
        }
        return null;
    }

    @Override
    public void onClick(View v) {
        if(v == allSensorSwitch) {
            sensor.switchSensor(allSensorSwitch.isChecked());
        }
        else if(v.getId() == R.id.featureSwitch) {
            DialogIoTSensor.SensorFeature sensorFeature = (DialogIoTSensor.SensorFeature)v.getTag();
            sensor.switchSensorFeature(sensorFeature, ((Switch)v).isChecked());
            FeatureViewHolder vh = getFeatureViewHolder(sensor.features.indexOf(sensorFeature));
            if(vh != null)
                vh.updateStatus(sensorFeature);
        }
    }

    public CharSequence[] getFeatureSettings(DialogIoTSensor.SensorFeature feature) {
        if(feature == DialogIoTSensor.SensorFeature.MAGNETOMETER)
            return new CharSequence[]{getString(R.string.frequency), getString(R.string.calibration)};
        else
            return new CharSequence[]{getString(R.string.frequency)};
    }

    Timer calibrationTimer;
    static final int calibrationTimeout = 10000;
    static final int calibrationInterval = 200;
    ProgressDialog calibrationDialog;

    public void onFeatureSettings(final DialogIoTSensor.SensorFeature feature, int index) {
        if(index == 0)
            Log.d("on feature settings", "frequency");
        else {
            if((feature == DialogIoTSensor.SensorFeature.MAGNETOMETER) && (index == 1)) {
                if(calibrationDialog == null)
                    calibrationDialog = new ProgressDialog(this);
                calibrationDialog.setIndeterminate(false);
                calibrationDialog.setMax(calibrationTimeout);
                calibrationDialog.setTitle(R.string.calibration);
                calibrationDialog.setMessage(getString(R.string.calibration_instruction));
                calibrationTimer = new Timer();
                calibrationTimer.schedule(new TimerTask() {
                    int timeSpan = 0;
                    @Override
                    public void run() {
                        timeSpan += calibrationInterval;
                        calibrationDialog.setProgress(timeSpan);
                        if(timeSpan > calibrationTimeout) {
                            calibrationTimer.cancel();
                            calibrationDialog.dismiss();
                            feature.stopCalibration();
                            Log.d("on feature settings", "stop calibration");
                        }
                    }
                }, 0, calibrationInterval);
                calibrationDialog.show();
                feature.startCalibration();
                Log.d("on feature settings", "start calibration");
            }
        }
    }

    protected String getSensorFeatureValueString(DialogIoTSensor.SensorFeature feature) {
        String featureValueString = feature.getValueString();
        if(feature == DialogIoTSensor.SensorFeature.MAGNETOMETER) {
            featureValueString += sensor.getMagnetoAngleString();
            featureValueString += sensor.getMagnetoDirectionString(getResources().getStringArray(R.array.directions));
        }
        return featureValueString;
    }

}
