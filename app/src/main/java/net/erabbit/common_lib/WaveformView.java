package net.erabbit.common_lib;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class WaveformView extends View {
	
	protected boolean customDraw = false;
	protected boolean inited = false;
	protected int width;
	protected int height;
	protected int bgColor;
	protected Paint paint = new Paint();
	
	protected float[] values;
	protected float[] points;
	protected int startPos;
	protected float minValue, maxValue;
	protected boolean fixedBounds;
	
	public WaveformView(Context context, AttributeSet set) {
		super(context, set);
	}
	
	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if(customDraw) {
			if(!inited) {
				paint.setColor(Color.WHITE);
				paint.setStyle(Style.STROKE);
				paint.setStrokeWidth(5);
				width = canvas.getWidth();
				height = canvas.getHeight();
				Log.i("draw", String.format("canvas width = %d, height = %d", width, height));
				if(canvas.isHardwareAccelerated())
					Log.i("draw", "hardware accelerated");
				inited = true;
			}
			if(points != null) {
				int pointCount = points.length;
				if((pointCount > 0) && (startPos < pointCount)){
					Path path = new Path();
					path.moveTo(startPos, points[startPos]);
					if(width < points.length)
						points = new float[width];
					float xOffset = (float)width / points.length;
					for(int i=startPos+1; i<pointCount; i++)
						path.lineTo(xOffset * i, points[i]);
					canvas.drawColor(bgColor);
					canvas.drawPath(path, paint);
				}
			}
		}
	}
	
	public void setup() {
		setLayerType(View.LAYER_TYPE_HARDWARE, null);
		customDraw = true;
	}
	
	public void done() {
		setLayerType(View.LAYER_TYPE_NONE, null);
		customDraw = false;
	}
/*	public void drawWave(int[] values, int min, int max) {
		int valueCount = values.length;
		if((points == null) || (points.length != valueCount))
			points = new float[valueCount];
		for(int i=0; i<valueCount; i++) {
			float value = Math.min(Math.max(values[i], min), max);
			points[i] = height - (value - min) / (max - min) * height;
		}
		this.invalidate();
	}
*/	
	public void setBackgroundColor(int color) {
		bgColor = color;
	}
	
	protected float makePoint(float value, float min, float max) {
		float fValue = Math.min(Math.max(value, min), max);
		return height - (fValue - min) / (max - min) * height;
	}
	
//	public void setWave(int[] configs) {
//		if(configs.length == 3)
//			setWave(configs[0], configs[1], configs[2]);
//	}
	public void setWave(int valueDensity, float[] valueRange) {
		if((valueRange != null) && (valueRange.length == 2))
			setWave(valueDensity, valueRange[0], valueRange[1]);
	}

	public void setWave(int valueCount, float minValue, float maxValue) {
		points = new float[valueCount];
		values = new float[valueCount];
		this.minValue = minValue;
		this.maxValue = maxValue;
		fixedBounds = (maxValue > minValue);
		startPos = valueCount;
	}
	
	public void addValues(float[] newValues, int valueCount) {
		int pointCount = points.length;
		if(valueCount < pointCount) {
			System.arraycopy(values, valueCount, values, 0, pointCount-valueCount);
			System.arraycopy(newValues, 0, values, pointCount-valueCount, valueCount);
		}
		else
			System.arraycopy(newValues, valueCount-pointCount, values, 0, pointCount);
		if(startPos > 0)
			startPos -= valueCount;
		if(startPos < 0)
			startPos = 0;
		if(fixedBounds)
			for(int i=startPos; i<pointCount; i++)
				points[i] = makePoint(values[i], minValue, maxValue);
		else {
			if(maxValue <= minValue) {
				minValue = values[startPos];
				maxValue = values[startPos];
			}
			for(int i=startPos; i<pointCount; i++) {
				minValue = Math.min(minValue, values[i]);
				maxValue = Math.max(maxValue, values[i]);
			}
			for(int j=startPos; j<pointCount; j++)
				points[j] = makePoint(values[j], minValue, maxValue);
		}
		invalidate();
	}
}
