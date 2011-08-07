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

public class Zone extends Item {
	SonosController sc;

	public Zone(String name, SonosController sc) {
		super(name, "Z:ZONE");
		this.sc = sc;
	}

	public Container select(Container parent) {
		Container c = new Container(title, parent);
		c.sc = sc;
		c.add(new Item("Albums","A:ALBUM"));
		c.add(new Item("Artists","A:ARTIST"));
		c.add(new Item("Songs","A:TRACKS"));
		c.add(new Item("Genres","A:GENRE"));
		c.add(new Item("Playlists","SQ:"));
		c.add(new Item("Queue","Q:0"));
		return c;
	}
}
