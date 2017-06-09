/**
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *	Copyright 2009 Andrew Rice (acr31@cam.ac.uk) and Vytautas Vaitukaitis (vv236@cam.ac.uk)
 */

/**
 * A geotagged message returned from the server. This class wraps the two
 * aspects of a message: the message text itself and the URL from which to
 * request the associated image.
 * 
 * @author acr31
 * 
 */

package ch.epfl.hci.healthytogether.model;

public class MessageMeta {

	private String msgid;

	private String msgfrom;

	private String msgtxt;

	private String msgtime;

	public String getmsgid() {
		return msgid;
	}

	public void setmsgid(String msgid) {
		this.msgid = msgid;
	}

	public String getmsgtxt() {
		return msgtxt;
	}

	public void setmsgtxt(String msgtxt) {
		this.msgtxt = msgtxt;
	}

	public String getmsgfrom() {
		return msgfrom;
	}

	public void setmsgfrom(String msgfrom) {
		this.msgfrom = msgfrom;
	}

	public String getmsgtime() {
		return msgtime;
	}

	public void setmsgtime(String msgtime) {
		this.msgtime = msgtime;
	}

}
