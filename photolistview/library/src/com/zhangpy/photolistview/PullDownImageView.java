package com.zhangpy.photolistview;

import android.view.View;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import com.zhangpy.photolistview.R;

public class PullDownImageView extends View {
	private Bitmap mDrawBitmap;
	private Bitmap mScaleBitmap;
	private int devideWidth;
	private boolean onDraw = false;
	private boolean onRecycle = true;
	private int currentY = 0;

	public PullDownImageView(Context context) {
		super(context);
		init();
	}

	public PullDownImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public PullDownImageView(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	private void init() {
		devideWidth = UtilMethod.getInstance((Activity) getContext())
				.getDeviceWidth();
	}

	public void setImageResource(int resourceId) {
		Bitmap bitmap = BitmapFactory
				.decodeResource(getResources(), resourceId);
		this.setImageBitmap(bitmap);
	}

	public void setImageDrawable(Drawable drawable) {
		if (drawable instanceof BitmapDrawable) {
			Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
			setImageBitmap(bitmap);
		}
		Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
				drawable.getIntrinsicHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		drawable.draw(canvas);
		this.setImageBitmap(bitmap);
	}

	// createBitmap里面如果长宽相同的话，那么就返回相同的bitmap 对象。
	public void setImageBitmap(Bitmap bitmap) {
		mDrawBitmap = Bitmap.createScaledBitmap(bitmap, devideWidth,
				devideWidth, false);
		if (mScaleBitmap != null && !mScaleBitmap.isRecycled()) {
			mScaleBitmap.recycle();
		}
		mScaleBitmap = Bitmap.createBitmap(mDrawBitmap, 0, currentY,
				devideWidth, getHeight());
		onDraw = true;
		invalidate();
		if (onRecycle && bitmap != null && !bitmap.isRecycled()
				&& !bitmap.equals(mDrawBitmap)) {
			bitmap.recycle();
		}
	}

	public void setRecycle() {
		if (mDrawBitmap != null && !mDrawBitmap.isRecycled()) {
			mDrawBitmap.recycle();
		}
		if (mScaleBitmap != null && !mScaleBitmap.isRecycled()) {
			mScaleBitmap.recycle();
		}
	}

	boolean first = true;

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		if (h != 0 && oldh != h) {
			if (first) {
				first = false;
				if (mDrawBitmap == null) {
					Bitmap bitmap = BitmapFactory.decodeResource(
							getResources(), R.drawable.default_group_photo);
					setImageBitmap(bitmap);
				}
			}
			onDraw = true;
			if (mScaleBitmap != null && !mScaleBitmap.isRecycled()) {
				mScaleBitmap.recycle();
			}
			this.currentY = (devideWidth - getHeight()) / 2;
			mScaleBitmap = Bitmap.createBitmap(mDrawBitmap, 0, currentY,
					devideWidth, getHeight());
		}
		super.onSizeChanged(w, h, oldw, oldh);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (!onDraw)
			return;
		if (mScaleBitmap != null && !mScaleBitmap.isRecycled()) {
			canvas.drawBitmap(mScaleBitmap, 0, 0, null);
		}
		super.onDraw(canvas);
	}
}
