package com.zhangpy.photolistview.sample;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.zhangpy.photolistview.PhotoListView;
import com.zhangpy.photolistview.PhotoListView.OnRefreshListener;
import com.zhangpy.photolistview.PhotoListView.onTapEventListener;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

public class MainActivity extends Activity implements OnRefreshListener,
		onTapEventListener {

	private ArrayAdapter<String> mArrayAdapter;
	private List<String> mData;
	private PhotoListView mPhotoListView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mPhotoListView = new PhotoListView(this);
		mData = getData();
		mArrayAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_expandable_list_item_1, mData);
		mPhotoListView.setAdapter(mArrayAdapter);
		mPhotoListView.setOnRefreshListener(this);
		mPhotoListView.setTapListener(this);
		setContentView(mPhotoListView);
	}

	private List<String> getData() {
		List<String> lists = new ArrayList<String>();
		for (int i = 0; i < 30; i++) {
			lists.add("测试数据" + i);
		}
		return lists;
	}

	@Override
	public void onRefresh() {
		new QueryDate().startExcute();
	}

	@Override
	public void onTap() {
		Toast.makeText(this, "on Tap Listener", Toast.LENGTH_SHORT).show();
	}

	class QueryDate extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			Random random = new Random();
			mData.add(0, "刷新数据" + random.nextInt(10));
			mArrayAdapter.notifyDataSetChanged();
			mPhotoListView.completeRefresh();
			super.onPostExecute(result);
		}

		@SuppressLint("NewApi")
		public void startExcute() {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				this.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
						(Void[]) null);
			} else {
				this.execute((Void[]) null);
			}
		}
	}

}
