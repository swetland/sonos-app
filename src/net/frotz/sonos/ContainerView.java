/*
 * Copyright (C) 2011 Brian Swetland
 *
 * Borrows much from the Android Music Player TouchInterceptor class:
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.frotz.sonos;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.util.AttributeSet;

public class ContainerView extends ListView {
	ImageView mDragView;
	WindowManager mWinMgr;
	WindowManager.LayoutParams mWLP;

	int mDragPosition;

	int mDiscardZone;
	int mDiscardColor;
	int mDragMode;

	ContainerView.Listener mCVL;

	public ContainerView(Context ctxt, AttributeSet attrs) {
		super(ctxt, attrs);
		mDiscardColor = 0xFF00FF00;
	}

	public void setDragListener(ContainerView.Listener cvl) {
		mCVL = cvl;
	}
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			int x = (int) ev.getX();
			int y = (int) ev.getY();
			int pos = pointToPosition(x,y);
			mDiscardZone = (getWidth() * 2) / 3;

			System.err.println("DOWN @("+x+","+y+") -> " + pos);
			if (pos < 0)
				break;
			if (x > 64)
				break;

			mDragPosition = pos;
			if (!mCVL.onDragStart(mDragPosition))
				break;

			View item = (View)
				getChildAt(pos - getFirstVisiblePosition());
			item.setDrawingCacheEnabled(true);
			Bitmap bm = Bitmap.createBitmap(item.getDrawingCache());
			startDragging(bm, x, y);
			return false;
		}
		return super.onInterceptTouchEvent(ev);
	}

	public boolean onTouchEvent(MotionEvent ev) {
		if (mDragView != null) {
			int x = (int) ev.getX();
			int y = (int) ev.getY();
			int action = ev.getAction();
			switch (action) {
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				stopDragging();
				if (x > mDiscardZone)
					if ((mDragMode & DND_DISCARD) != 0)
						mCVL.onDragDiscard(mDragPosition);
				break;
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_MOVE:
				keepDragging(x,y);
				return true;
			}
		}
		return super.onTouchEvent(ev);
	}

	private void startDragging(Bitmap bm, int x, int y) {
		mWLP = new WindowManager.LayoutParams();
		mWLP.gravity = Gravity.TOP | Gravity.LEFT;
		mWLP.x = x;
		mWLP.y = y;
		mWLP.height = mWLP.WRAP_CONTENT;
		mWLP.width = mWLP.WRAP_CONTENT;
		mWLP.flags = mWLP.FLAG_NOT_FOCUSABLE |
			mWLP.FLAG_NOT_TOUCHABLE | mWLP.FLAG_KEEP_SCREEN_ON |
			mWLP.FLAG_LAYOUT_IN_SCREEN | mWLP.FLAG_LAYOUT_NO_LIMITS;
		mWLP.format = PixelFormat.TRANSLUCENT;
		mWLP.windowAnimations = 0;

		Context ctxt = getContext();
		ImageView v = new ImageView(ctxt);
		v.setPadding(0, 0, 0, 0);
		v.setImageBitmap(bm);

		mWinMgr = (WindowManager)
			getContext().getSystemService(Context.WINDOW_SERVICE);
		mWinMgr.addView(v, mWLP);
		mDragView = v;
	}
	private void stopDragging() {
		if (mDragView == null)
			return;
		mDragView.setVisibility(GONE);
		mWinMgr.removeView(mDragView);
		mDragView.setImageDrawable(null);
		mDragView = null;
	}
	private void keepDragging(int x, int y) {
		if ((mDragMode & DND_DISCARD) != 0) {
			if (x > mDiscardZone) {
				mDragView.setColorFilter(mDiscardColor, PorterDuff.Mode.MULTIPLY);
			} else {
				mDragView.clearColorFilter();
			}
		}
		mWLP.x = x;
		mWLP.y = y;
		mWinMgr.updateViewLayout(mDragView,mWLP);
	}

	public void setDiscardColor(int color) {
		mDiscardColor = color;
	}
	public void setDragMode(int flags) {
		mDragMode = flags;
	}
	public static interface Listener {
		void onDragDiscard(int pos);
		boolean onDragStart(int pos);
	}

	public static final int DND_DISCARD = 1;
	public static final int DND_REORDER = 2;

}

