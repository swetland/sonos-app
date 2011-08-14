/*
 * Copyright (C) 2011 Brian Swetland
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

import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.content.Context;

import android.os.Message;
import android.os.Handler;

import android.database.DataSetObservable;
import android.database.DataSetObserver;

public class Container implements ListAdapter, SonosListener, Handler.Callback {
	Handler handler;
	DataSetObservable dsolist;
	Context ctxt;
	Container parent;
	SonosController sc;
	Drawable box;

	Item list[];
	boolean update;
	int count;
	int visible;
	String name;
	int saved;

	public Container(String name, Container parent) {
		this.name = name;
		ctxt = parent.ctxt;
		sc = parent.sc;
		this.parent = parent;
		init();
	}
	public Container(String name, Context c) {
		this.name = name;
		ctxt = c;
		init();
	}
	private void init() {
		box = ctxt.getResources().getDrawable(R.drawable.box32);
		handler = new Handler(this);
		dsolist = new DataSetObservable();
		list = new Item[2048];
		saved = -1;
	}
	public boolean handleMessage(Message m) {
		boolean update;
		synchronized (list) {
			update = this.update;
			if (update) {
				this.update = false;
				visible = count;
			}
		}
		if (update)
			dsolist.notifyChanged();
		return true;
	}

	public void clear() {
		synchronized (list) {
			count = 0;
			visible = 0;
			update = false;
		}
		dsolist.notifyChanged();
	}
	public void add(Item x) {
		synchronized (list) {
			list[count++] = x;
			if (update)
				return;
			update = true;
		}
		Message msg = Message.obtain();
		msg.what = 1;
		handler.sendMessage(msg);
	}
	public void move(int from, int to) {
		synchronized (list) {
			Item tmp = list[to];
			list[to] = list[from];
			list[from] = tmp;
		}
		dsolist.notifyChanged();
	}
	public void remove(int pos) {
		synchronized (list) {
			if ((visible < 1) || (pos >= count))
				return;
			System.arraycopy(list, pos + 1, list, pos, count - pos - 1);
			visible--;
		}
		dsolist.notifyChanged();
	}

	/* SonosListener Interface */
	public void updateDone(String id) {
	}
	public void updateItem(String id, int idx, SonosItem in) {
		add(new Item(in));
	}

	/* ListAdapter Interface */
	public int getCount() {
		return visible;
	}
	public Object getItem(int pos) {
		return list[pos];
	}
	public long getItemId(int pos) {
		return (long) pos;
	}
	public int getItemViewType(int pos) {
		return 0;
	}
	public View getView(int pos, View v, ViewGroup parent) {
		LinearLayout ll;
		TextView tv;
		if (v != null) {
			ll = (LinearLayout) v;
			tv = (TextView) ll.getChildAt(0);
		} else {
			ll = new LinearLayout(ctxt);
			tv = new TextView(ctxt);
			tv.setHeight(48);
			tv.setTextSize(24.0f);
			tv.setGravity(Gravity.CENTER_VERTICAL);
			tv.setHorizontallyScrolling(true);
			tv.setCompoundDrawablePadding(5);
			tv.setCompoundDrawablesWithIntrinsicBounds(box,null,null,null);

			ll.setGravity(Gravity.BOTTOM);
			ll.addView(tv);
		}
		tv.setText(list[pos].title);
		return ll;
	}
	public int getViewTypeCount() {
		return 1;
	}
	public boolean hasStableIds() {
		return false;
	}
	public boolean isEmpty() {
		return true;
	}
	public void registerDataSetObserver(DataSetObserver dso) {
		dsolist.registerObserver(dso);
	}
	public void unregisterDataSetObserver(DataSetObserver dso) {
		dsolist.unregisterObserver(dso);
	}
	public boolean areAllItemsEnabled() {
		return true;
	}
	public boolean isEnabled(int pos) {
		return true;
	}
}
