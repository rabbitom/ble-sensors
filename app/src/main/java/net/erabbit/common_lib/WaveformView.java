package net.erabbit.common_lib;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

public class WaveformView extends View {
	
	protected boolean customDraw = false;
	protected boolean initialized = false;
	protected int width;
	protected int height;
	protected int bgColor;
	protected Paint paint;

	protected int dimension = 1;
	protected float[] values;
	protected float[] points;
	protected int startPos;//第一个采样点的位置
	protected int valueSize;//缓存采样点个数
	protected int valueLength;//实际保存的采样点个数
	protected float minValue, maxValue;
	protected boolean fixedRange = false;
	
	public WaveformView(Context context, AttributeSet set) {
		super(context, set);
	}
	
	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if(customDraw) {
			DisplayMetrics dm = getResources().getDisplayMetrics();
			float density = dm.density;
			if(!initialized) {
				paint = new Paint();
				paint.setColor(Color.WHITE);
				paint.setStyle(Style.STROKE);
				paint.setStrokeWidth(density);
				width = canvas.getWidth();
				height = canvas.getHeight();
				valueSize = (int)(width / density);
				values = new float[valueSize * dimension];
				points = new float[valueSize * dimension];
				Log.i("draw", String.format("canvas width = %d, height = %d", width, height));
				if(canvas.isHardwareAccelerated())
					Log.i("draw", "hardware accelerated");
				initialized = true;
			}
			if(valueLength > 0) {
				float start = (valueSize - valueLength) * density;
				Path path = new Path();
				path.moveTo(start, points[startPos * dimension]);
				for(int i=1; i<valueLength; i++)
					path.lineTo(start + density * i, points[((startPos + i) % valueSize) * dimension]);
				canvas.drawColor(bgColor);
				canvas.drawPath(path, paint);
			}
		}
	}
	
	public void startDrawing() {
		setLayerType(View.LAYER_TYPE_HARDWARE, null);
		customDraw = true;
	}
	
	public void stopDrawing() {
		setLayerType(View.LAYER_TYPE_NONE, null);
		customDraw = false;
	}

	public void setBackgroundColor(int color) {
		bgColor = color;
	}
	
	protected float makePoint(float value, float min, float max) {
		float fValue = Math.min(Math.max(value, min), max);
		return height - (fValue - min) / (max - min) * height;
	}
	
	public void clearValues() {
		startPos = 0;
		valueLength = 0;
	}

	public void setValueRange(float[] valueRange) {
		minValue = valueRange[0];
		maxValue = valueRange[1];
		fixedRange = (maxValue > minValue);
	}
	
	public void addValues(float[] newValues, int valueCount) {
		if((values == null) || (valueSize == 0))
			return;
		int insertPos = (startPos + valueLength) % valueSize;
		if(insertPos + valueCount < valueSize) {
			System.arraycopy(newValues, 0, values, insertPos * dimension, valueCount * dimension);
		}
		else {
			int appendLength = valueSize - insertPos;
			int rewindLength = valueCount - appendLength;
			System.arraycopy(newValues, 0, values, insertPos * dimension, appendLength * dimension);
			System.arraycopy(newValues, appendLength * dimension, values, 0, rewindLength * dimension);
		}
		if(valueLength < valueSize) {
			valueLength += valueCount;
			if(valueLength >= valueSize) {
				valueLength = valueSize;
				startPos = valueLength - valueSize;
			}
		}
		else {
			startPos = (insertPos + valueCount) % valueSize;
		}
		if(!fixedRange) {
			if(maxValue <= minValue) {
				minValue = values[0];
				maxValue = values[0];
			}
			for(int i=0; i<valueLength * dimension; i++) {
				minValue = Math.min(minValue, values[i]);
				maxValue = Math.max(maxValue, values[i]);
			}
		}
		for(int i=0; i<valueLength * dimension; i++)
			points[i] = makePoint(values[i], minValue, maxValue);
		invalidate();
	}
}
