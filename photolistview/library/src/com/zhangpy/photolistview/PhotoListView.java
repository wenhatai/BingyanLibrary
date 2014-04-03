package com.zhangpy.photolistview;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.RotateDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.AbsListView.OnScrollListener;
import com.zhangpy.photolistview.R;

public class PhotoListView extends ListView implements OnScrollListener {
	private float originPro = 0.5f;
	private float refreshPro = 0.75f;

	private int mHeight;

	private float touchY;
	private float deltaY;

	private float initTouchY;
	private boolean isMoveing = false;

	private PullDownImageView mBgImageView;

	private ImageView mBarImageView;

	private int initHeight;

	private int mBarMarignHeight;

	private float desity;

	private RelativeLayout.LayoutParams mImageLayoutParams;

	private RelativeLayout.LayoutParams mBarLayoutParams;

	private boolean enableRefresh = true;

	private OnScrollListener mOnScrollListener;

	private int mRefreshHeight;

	private View headView;

	private Animation mBarRoateAnimation;

	private OnRefreshListener mOnRefreshListener;

	private enum State {
		TAP_TO_REFRESH, PULL_TO_REFRESH, RELEASE_TO_REFRESH, REFRESHING, UP, DOWN
	};

	private State state = State.TAP_TO_REFRESH;

	public interface onTapEventListener {
		public void onTap();
	}

	private onTapEventListener mTapListener;

	public void setTapListener(onTapEventListener mTapListener) {
		this.mTapListener = mTapListener;
	}

	public PhotoListView(Context context) {
		super(context);
		init();
	}

	public PhotoListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public PhotoListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public interface OnRefreshListener {
		public void onRefresh();
	}

	public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
		this.mOnRefreshListener = onRefreshListener;
	}

	private void init() {
		mBarMarignHeight = (int) getResources().getDimension(
				R.dimen.photolistview_height);
		mBarRoateAnimation = AnimationUtils.loadAnimation(getContext(),
				R.anim.rotate_circle_repeat);
		mBarRoateAnimation.setInterpolator(new LinearInterpolator());
		desity = UtilMethod.getInstance((Activity) getContext())
				.getDensity();
		setHeadView();
		super.setOnScrollListener(this);
	}

	public void setEnableRefresh(boolean enableRefresh) {
		this.enableRefresh = enableRefresh;
	}

	public void completeRefresh() {
		releaseView();
		state = State.TAP_TO_REFRESH;
	}

	public void setRefresh() {
		if (enableRefresh) {
			state = State.REFRESHING;
			mBarLayoutParams.topMargin = mBarMarignHeight;
			mBarImageView.setLayoutParams(mBarLayoutParams);
			mBarImageView.startAnimation(mBarRoateAnimation);
		}
	}

	public void onRefresh() {
		if (mOnRefreshListener != null) {
			mOnRefreshListener.onRefresh();
		}
	}

	public void setHeadView() {
		headView = LayoutInflater.from(getContext()).inflate(
				R.layout.head_photolistview, null);
		mBgImageView = (PullDownImageView) headView
				.findViewById(R.id.photo_album_image);
		mBarImageView = (ImageView) headView
				.findViewById(R.id.photo_refresh_bar);
		mBarLayoutParams = (android.widget.RelativeLayout.LayoutParams) mBarImageView
				.getLayoutParams();
		mHeight = UtilMethod.getInstance((Activity) getContext())
				.getDeviceWidth();
		initHeight = (int) (mHeight * originPro);
		mRefreshHeight = (int) (mHeight * refreshPro);
		mImageLayoutParams = new RelativeLayout.LayoutParams(
				AbsListView.LayoutParams.MATCH_PARENT, initHeight);
		mBgImageView.setLayoutParams(mImageLayoutParams);
		addHeaderView(headView);
	}

	public View getHeadView() {
		return headView;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (getFirstVisiblePosition() == 0 && enableRefresh
				&& state != State.REFRESHING) {
			return commOnTouchEvent(ev);
		} else {
			return super.onTouchEvent(ev);
		}
	}

	public boolean commOnTouchEvent(MotionEvent ev) {
		int action = ev.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			initTouchY = ev.getY();
			first = false;
			break;
		case MotionEvent.ACTION_UP:
			if (state == State.RELEASE_TO_REFRESH && mOnRefreshListener != null) {
				setRefresh();
				onRefresh();
				animation();
			} else if (state == State.PULL_TO_REFRESH
					|| mOnRefreshListener == null) {
				releaseView();
				animation();
			}
			if (getScrollY() == 0) {
				state = State.TAP_TO_REFRESH;
			}
			if (initTouchY == ev.getY() && mTapListener != null
					&& initTouchY < headView.getBottom()) {
				mTapListener.onTap();
			}
			isMoveing = false;
			first = true;
			touchY = 0;
			break;
		case MotionEvent.ACTION_MOVE:
			applyHeaderMove(ev);
			if (isMoveing) {
				return true;
			}
			break;
		default:
			break;
		}
		return super.onTouchEvent(ev);
	}

	boolean first = true;

	private void applyHeaderMove(MotionEvent ev) {
		if (first) {
			initTouchY = ev.getY();
			first = false;
		}
		touchY = ev.getY();
		deltaY = touchY - initTouchY;

		if (deltaY < 0 && state == State.TAP_TO_REFRESH) {
			state = State.UP;
		} else if (deltaY > 0 && state == State.TAP_TO_REFRESH) {
			state = State.DOWN;
		}
		if (state == State.UP) {
			deltaY = deltaY < 0 ? deltaY : 0;
			isMoveing = false;
		} else if (state == State.DOWN && headView.getTop() >= 0) {
			if (getScrollY() <= deltaY) {
				isMoveing = true;
			}
			deltaY = deltaY < 0 ? 0 : deltaY;
		}

		if (isMoveing) {
			int image_move_H = (int) (deltaY / 3);
			int currentHeight = initHeight + image_move_H;
			if (currentHeight > mHeight) {
				currentHeight = mHeight;
			}
			mImageLayoutParams.height = currentHeight;
			mBgImageView.setLayoutParams(mImageLayoutParams);
			if (mOnRefreshListener != null) {
				int levelHeight;
				if (currentHeight > mRefreshHeight) {
					levelHeight = mRefreshHeight - initHeight;
				} else {
					levelHeight = image_move_H;
				}
				float level = (float) (levelHeight * 1.0 / (mRefreshHeight - initHeight));
				int marginHeight = (int) (-mBarMarignHeight + mBarMarignHeight
						* 2 * level);
				mBarLayoutParams.topMargin = marginHeight;
				mBarImageView.setLayoutParams(mBarLayoutParams);
				int roate = image_move_H * 1000;
				RotateDrawable drawable = (RotateDrawable) mBarImageView
						.getDrawable();
				drawable.setLevel(roate);
			}
		}
	}

	@Override
	public void setOnScrollListener(OnScrollListener l) {
		this.mOnScrollListener = l;
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (mOnScrollListener != null) {
			mOnScrollListener.onScrollStateChanged(view, scrollState);
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		if (firstVisibleItem == 0 && isMoveing && state != State.REFRESHING
				&& state != State.TAP_TO_REFRESH) {
			if (headView.getBottom() >= mRefreshHeight
					&& state != State.RELEASE_TO_REFRESH) {
				state = State.RELEASE_TO_REFRESH;
			} else if (headView.getBottom() < mRefreshHeight
					&& state != State.PULL_TO_REFRESH) {
				state = State.PULL_TO_REFRESH;
			}
		}
		if (mOnScrollListener != null) {
			mOnScrollListener.onScroll(view, firstVisibleItem,
					visibleItemCount, totalItemCount);
		}
	}

	class CollapseAnimation extends Animation {
		int collHeight;
		int imageHeight;

		public CollapseAnimation(int imageHeight) {
			this.imageHeight = imageHeight;
			this.collHeight = this.imageHeight - initHeight;
		}

		protected void applyTransformation(float interpolatedTime,
				android.view.animation.Transformation t) {
			mImageLayoutParams.height = (int) (this.imageHeight - (interpolatedTime * this.collHeight));
			mBgImageView.setLayoutParams(mImageLayoutParams);
		};

		public boolean willChangeBounds() {
			return true;
		};
	};

	class ReleaseAnimation extends Animation {
		int marignTop;

		public ReleaseAnimation(int marginTop) {
			this.marignTop = marginTop;
		}

		@Override
		protected void applyTransformation(float interpolatedTime,
				Transformation t) {
			int marignHeight = (int) (marignTop - (mBarMarignHeight + marignTop)
					* interpolatedTime);
			mBarLayoutParams.topMargin = marignHeight;
			mBarImageView.setLayoutParams(mBarLayoutParams);
			if (marignHeight > 0) {
				int roate = marignHeight * 10000;
				RotateDrawable drawable = (RotateDrawable) mBarImageView
						.getDrawable();
				drawable.setLevel(roate);
			}
		}

		@Override
		public boolean willChangeBounds() {
			return true;
		}
	}

	public void setRecycle() {
		mBgImageView.setRecycle();
	}

	public void releaseView() {
		if (mOnRefreshListener != null) {
			ReleaseAnimation releaseAnimation = new ReleaseAnimation(
					mBarLayoutParams.topMargin);
			releaseAnimation
					.setDuration((int) (mBarMarignHeight * 15 / desity));
			mBarImageView.startAnimation(releaseAnimation);
		}
		state = State.TAP_TO_REFRESH;
	}

	public void animation() {
		int duration = (int) ((mImageLayoutParams.height - initHeight) * 2 / desity);
		if (duration > 0) {
			CollapseAnimation collapseAnimation = new CollapseAnimation(
					mImageLayoutParams.height);
			collapseAnimation.setDuration(duration);
			mBgImageView.startAnimation(collapseAnimation);
		}
	}
}
