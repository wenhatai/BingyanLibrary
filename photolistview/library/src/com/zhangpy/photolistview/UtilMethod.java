package com.zhangpy.photolistview;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Point;
import android.view.Display;

public class UtilMethod {
	private int mDeviceHeight;
	private int mDeviceWidth;
	private float density;
	private static UtilMethod mUtilMethod;
	
	public static UtilMethod getInstance(Activity activity){
		if(mUtilMethod == null){
			mUtilMethod = new UtilMethod(activity);
		}
		return mUtilMethod;
	}
	
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi") 
	public UtilMethod(Activity activity) {
		Display display = activity.getWindowManager().getDefaultDisplay();
		if(Integer.valueOf(android.os.Build.VERSION.SDK_INT)<13){
			mDeviceHeight = display.getHeight();
			mDeviceWidth = display.getWidth();
		}else {
			Point size = new Point();
			display.getSize(size);
			mDeviceWidth = size.x;
			mDeviceHeight = size.y;
		}
		density = activity.getResources().getDisplayMetrics().density;
	}
	
	public float getDensity() {
		return density;
	}
	
	public int getDeviceHeight() {
		return mDeviceHeight;
	}
	
	public int getDeviceWidth() {
		return mDeviceWidth;
	}
}
