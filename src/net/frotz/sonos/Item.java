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

public class Item {
	String title;
	String browse;
	String play;
	int flags;

	public Item(Item in) {
		title = in.title;
		browse = in.browse;
		play = in.play;
		flags = in.flags;
	}
	public Item(SonosItem in) {
		title = in.title.toString();
		browse = in.idURI.toString();
		play = in.playURI.toString();
		flags = in.flags;
	}
	public Item(String title, String browse) {
		this.title = title;
		this.browse = browse;
		this.play = "";
	}

	public Container select(Container c) {
		if ((flags & SonosItem.SONG) != 0)
			return null;

		Container child = new Container(title, c);
		if (!browse.startsWith("Q:")) {
			Item all = new Item(this);
			all.title = "All Tracks...";
			all.flags = SonosItem.SONG;
			child.add(all);
		}
		child.sc.browse(browse, child);
		return child;
	}
}
