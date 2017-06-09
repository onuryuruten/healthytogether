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

public class MemberMeta {

	private String uid;

	private String firstname;

	private String lastname;

	private String name;

	private String gender;

	private String pictureUri;

	private String profileUri;

	public String getuid() {
		return uid;
	}

	public void setuid(String uid) {
		this.uid = uid;
	}

	public String getname() {
		return name;
	}

	public void setname(String name) {
		this.name = name;
	}

	public String getfirstname() {
		return firstname;
	}

	public void setfirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getlastname() {
		return lastname;
	}

	public void setlastname(String lastname) {
		this.lastname = lastname;
	}

	public String getgender() {
		return gender;
	}

	public void setgender(String gender) {
		this.gender = gender;
	}

	public String getpictureUri() {
		return pictureUri;
	}

	public void setpictureUri(String pictureUri) {
		this.pictureUri = pictureUri;
	}

	public String getprofileUri() {
		return profileUri;
	}

	public void setprofileUri(String profileUri) {
		this.profileUri = profileUri;
	}

}
