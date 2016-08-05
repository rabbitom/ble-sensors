package net.erabbit.blesensor;

import android.app.Fragment;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import net.erabbit.common_lib.WaveformView;

/**
 * Created by Tom on 16/8/3.
 */
public class WaveformFragment extends Fragment implements View.OnClickListener {

    protected WaveformView waveformView;
    protected TextView title;
    protected Button hide;

    int defaultBackgroundColor;

    public WaveformView getWaveformView() {
        return waveformView;
    }

    public TextView getTitle() {
        return title;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_waveform, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        waveformView = (WaveformView) view.findViewById(R.id.waveform);
        title = (TextView) view.findViewById(R.id.title);
        hide = (Button) view.findViewById(R.id.hide);
        hide.setOnClickListener(this);
        defaultBackgroundColor = getResources().getColor(R.color.colorPrimary);
    }

    @Override
    public void onClick(View v) {
        if(v == hide) {
            hide();
        }
    }

    public void show(CharSequence titleText, int dimension) {
        View view = getView();
        if(view == null)
            return;
        view.setVisibility(View.VISIBLE);
        waveformView.startDrawing();
        title.setText(titleText);
        waveformView.setDimension(dimension);
        if(dimension > 1) {
            view.setBackgroundColor(Color.WHITE);
            waveformView.setBackgroundColor(Color.WHITE);
            title.setTextColor(defaultBackgroundColor);
        }
        else {
            view.setBackgroundColor(defaultBackgroundColor);
            waveformView.setBackgroundColor(defaultBackgroundColor);
            title.setTextColor(Color.WHITE);
        }
        waveformView.clearValues();
        waveformView.setValueRange(new float[]{0,0});
    }

    public void hide() {
        waveformView.stopDrawing();
        View view = getView();
        if(view != null)
            view.setVisibility(View.GONE);
    }
}
