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

import android.app.Activity;
import android.app.ListActivity;
import android.os.Bundle;
import android.os.Message;
import android.os.Handler;
import android.util.Log;

import android.view.View;
import android.view.KeyEvent;
import android.view.Window;
import android.widget.ListView;
import android.widget.TextView;

public class Main extends ListActivity 
	implements Discover.Listener {
	private static final String TAG = "Sonos";
	Discover discover;
	Container zones;
	Container active;
	SonosController sc;
	View bar;
	TextView rightTitle;
	TextView leftTitle;

	protected void onCreate(Bundle b) {
		Log.d(TAG,"--- onCreate() ---");
		super.onCreate(b);

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.main);
        	getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);

		bar = findViewById(R.id.controls);

		leftTitle = (TextView) findViewById(R.id.title_left_text);
        	leftTitle.setText("Sonos");
        	rightTitle = (TextView) findViewById(R.id.title_right_text);
		rightTitle.setText("Zones");

		zones = new Container("Zones", this);
		active = zones;
		setListAdapter(active);
		bar.setVisibility(View.GONE);

		discover = new Discover(this);
	}
	public void doPrev(View v) {
		active.sc.prev();
	}
	public void doNext(View v) {
		active.sc.next();
	}
	public void doPlay(View v) {
		active.sc.play(null);
	}
	public void doPause(View v) {
		active.sc.pause();
	}
	protected void onDestroy() {
		Log.d(TAG,"--- onDestroy() ---");
		super.onDestroy();
		discover.done();
	}
	public boolean onKeyDown(int code, KeyEvent evt) {
		switch (code) {
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			if (active.sc != null)
				active.sc.volume(-5);
			return true;
		case KeyEvent.KEYCODE_VOLUME_UP:
			if (active.sc != null)
				active.sc.volume(5);
			return true;
		case KeyEvent.KEYCODE_BACK:
			if (active.parent != null)
				setActive(active.parent);
			return true;
		default:
			System.err.println("CODE: " +code);
			return false;
		}
	}
	void setActive(Container c) {
		if (active == zones) {
			bar.setVisibility(View.VISIBLE);
			leftTitle.setText(c.name);
		}
		active = c;
		if (active == zones) {
			bar.setVisibility(View.GONE);
			leftTitle.setText("Sonos");
			rightTitle.setText("Zones");
		} else {
			if (active.parent == zones)
				rightTitle.setText("");
			else
				rightTitle.setText(active.name);
		}
		setListAdapter(active);
	}

	public boolean onKeyUp(int code, KeyEvent evt) {
		return true;
	}
	protected void onListItemClick(ListView lv, View v, int pos, long id) {
		Item item = (Item) active.getItem(pos);
		System.err.println(">> " + item.browse + ", " + item.play + " <<");
		Container c = item.select(active);
		if (c != null)
			setActive(c);
	}
	public void found(String host) {
		Log.d(TAG, "found: " + host);
		SonosController sc = new SonosController(host);
		sc.init(zones);	
	}
}