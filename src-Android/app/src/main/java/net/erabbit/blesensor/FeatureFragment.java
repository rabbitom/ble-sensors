package net.erabbit.blesensor;

import android.app.Activity;
import android.app.Fragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import net.erabbit.common_lib.WaveformView;

/**
 * Created by Tom on 16/8/3.
 */
public class FeatureFragment extends Fragment implements View.OnClickListener, DialogInterface.OnClickListener {

    protected WaveformView waveformView;
    protected TextView title, maxValue, minValue, curValue;
    protected TextView[] texts;
    protected Switch featureSwitch;
    protected Button settings, hide;

    protected DialogIoTSensor.SensorFeature sensorFeature;

    int defaultBackgroundColor;

    public WaveformView getWaveformView() {
        return waveformView;
    }

    public Switch getFeatureSwitch() {
        return featureSwitch;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_feature, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        waveformView = (WaveformView) view.findViewById(R.id.waveform);
        title = (TextView) view.findViewById(R.id.title);
        curValue = (TextView) view.findViewById(R.id.curValue);
        maxValue = (TextView) view.findViewById(R.id.maxValue);
        minValue = (TextView) view.findViewById(R.id.minValue);
        texts = new TextView[]{title, maxValue, minValue, curValue};
        featureSwitch = (Switch) view.findViewById(R.id.featureSwitch);
        settings = (Button) view.findViewById(R.id.settings);
        settings.setOnClickListener(this);
        hide = (Button) view.findViewById(R.id.hide);
        hide.setOnClickListener(this);
        defaultBackgroundColor = getResources().getColor(R.color.colorPrimary);
    }

    @Override
    public void onClick(View v) {
        if(v == hide) {
            hide();
            IoTSensorActivity activity = (IoTSensorActivity)getActivity();
            activity.curFeatureIndex = -1;
        }
        else if(v == settings) {
            IoTSensorActivity activity = (IoTSensorActivity)getActivity();
            CharSequence[] choices = activity.getFeatureSettings(sensorFeature);
            new AlertDialog.Builder(activity).setItems(choices, this).show();
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        IoTSensorActivity activity = (IoTSensorActivity)getActivity();
        activity.onFeatureSettings(sensorFeature, which);
    }

    public void show(DialogIoTSensor.SensorFeature feature) {
        View view = getView();
        if(view == null)
            return;
        view.setVisibility(View.VISIBLE);
        waveformView.startDrawing();
        title.setText(feature.name());
        int dimension = feature.getDimension();
        waveformView.setDimension(dimension);
        if(dimension > 1) {
            view.setBackgroundColor(Color.WHITE);
            waveformView.setBackgroundColor(Color.WHITE);
            setTextColor(defaultBackgroundColor);
        }
        else {
            view.setBackgroundColor(defaultBackgroundColor);
            waveformView.setBackgroundColor(defaultBackgroundColor);
            setTextColor(Color.WHITE);
        }
        waveformView.clearValues();
        waveformView.setValueRange(new float[]{0,0});
        Activity activity = getActivity();
        if(activity != null) {
            featureSwitch.setTag(feature);
            featureSwitch.setOnClickListener((View.OnClickListener) activity);
        }
        else
            featureSwitch.setVisibility(View.GONE);
        sensorFeature = feature;
    }

    protected void setTextColor(int color) {
        for (TextView textView :
             texts) {
            textView.setTextColor(color);
        }
    }

    public void hide() {
        waveformView.stopDrawing();
        View view = getView();
        if(view != null)
            view.setVisibility(View.GONE);
    }
}
