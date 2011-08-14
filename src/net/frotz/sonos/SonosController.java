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

class SonosController implements Runnable {
	Object lock;
	Sonos sonos;
	String uri;

	Action queue;
	Action free;

	public SonosController(String host) {
		queue = new Action();
		queue.next = queue;
		queue.prev = queue;
		free = new Action();
		free.next = free;
		free.prev = free;
		sonos = new Sonos(host);
		(new Thread(this)).start();
	}

	void action(int what, String arg) {
		Action a = obtain();
		a.what = what;
		a.s = arg;
		post(a);
	}
	void action(int what, int arg) {
		Action a = obtain();
		a.what = what;
		a.i = arg;
		post(a);
	}
	public void init(Container zc) {
		Action a = obtain();
		a.what = INIT;
		a.c = zc;
		post(a);
	}
	public void browse(String uri, Container c) {
		Action a = obtain();
		a.what = BROWSE;
		a.s = uri;
		a.c = c;
		post(a);
	}
	public void volume(int delta) {
		action(VOLUME, delta);
	}
	public void seek(int track) {
		action(SEEK, track);
	}
	public void move(int from, int to) {
		Action a = obtain();
		a.what = MOVE;
		a.i = from;
		a.ii = to;
		post(a);
	}
	public void play(String x) {
		action(PLAY, x);
	}
	public void enqueue(String x) {
		action(ENQUEUE, x);
	}
	public void remove(String x) {
		action(REMOVE, x);
	}
	public void clearQueue() {
		action(CLEAR_QUEUE, null);
	}
	public void next() {
		action(NEXT, null);
	}
	public void prev() {
		action(PREV, null);
	}
	public void pause() {
		action(PAUSE, null);
	}
	void dispatch(Action act) {
		switch (act.what) {
		case INIT:
			String name = sonos.getZoneName();
			Zone zone = new Zone(name, this);
			act.c.add(zone);
			break;
		case BROWSE:
			sonos.browse(act.s, act.c);
			break;
		case ENQUEUE:
			sonos.add(act.s);
			break;
		case MOVE:
			sonos.move(act.i, act.ii);
			break;
		case SEEK:
			sonos.seekTrack(act.i);
			sonos.play();
			break;
		case PLAY:
			if (act.s != null)
				sonos.set(act.s);
			sonos.play();
			break;
		case PAUSE:
			sonos.pause();
			break;
		case NEXT:
			sonos.next();
			break;
		case PREV:
			sonos.prev();
			break;
		case VOLUME:
			int vol = sonos.volume() + act.i;
			if (vol > 50)
				vol = 50; // 50 is *LOUD*
			sonos.volume(vol);
			break;
		case CLEAR_QUEUE:
			sonos.removeAll();
			break;
		case REMOVE:
			sonos.remove(act.s);
		}
	}

	private Action obtain() {
		synchronized (free) {
			Action a = free.next;
			if (a == free)
				return new Action();
			a.next.prev = a.prev;
			a.prev.next = a.next;
			a.next = null;
			a.prev = null;
			return a;
		}
	}
	private void dispose(Action a) {
		synchronized (free) {
			a.next = free;
			a.prev = free.prev;
			a.next.prev = a;
			a.prev.next = a;
		}
	}
	private void post(Action a) {
		System.err.println("[SonosController] queue '"+a+"'");
		synchronized (queue) {
			a.next = queue;
			a.prev = queue.prev;
			a.next.prev = a;
			a.prev.next = a;
			queue.notify();
		}
	}
	private Action receive() {
		try {
			synchronized (queue) {
				Action a = queue.next;
				if (a != queue) {
					a.next.prev = a.prev;
					a.prev.next = a.next;
					a.next = null;
					a.prev = null;
					return a;
				}
				queue.wait();
			} 
		} catch (InterruptedException e) {
		}
		return null;
	}
	public void run() {
		for (;;) {
			System.err.println("[SonosController] waiting...");
			Action act = receive();
			if (act != null) {
				dispatch(act);
				dispose(act);
			}
		}
	}

	static class Action {
		Action next;
		Action prev;
		Container c;
		int what;
		String s;
		int i, ii;
	}

	private final static int BROWSE = 1;
	private final static int PLAY = 2;
	private final static int VOLUME = 3;
	private final static int PREV = 4;
	private final static int NEXT = 5;
	private final static int SEEK = 6;
	private final static int ENQUEUE = 7;
	private final static int INIT = 8;
	private final static int PAUSE = 9;
	private final static int CLEAR_QUEUE = 10;
	private final static int REMOVE = 11;
	private final static int MOVE = 12;
}

