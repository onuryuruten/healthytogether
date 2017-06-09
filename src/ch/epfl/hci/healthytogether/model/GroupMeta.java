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

public class GroupMeta {
	/**
	 * The Group id
	 */
	private String groupid;
	/**
	 * The URL of the image for this group
	 */
	private String pictureUri;

	/**
	 * The name of group
	 */
	private String groupName;

	/**
	 * The privacy of this group
	 */
	private String privacy;

	/**
	 * Returns the id of group
	 */
	private String mnumber;

	/**
	 * Returns the id of group
	 */
	public String getGroupId() {
		return groupid;
	}

	/**
	 * Store group id
	 */
	public void setGroupId(String groupid) {
		this.groupid = groupid;
	}

	/**
	 * Returns the absolute URL for the image associated with this message
	 */
	public String getPictureUri() {
		return pictureUri;
	}

	/**
	 * Store the absolute image URL for this message
	 */
	public void setPictureUri(String pictureUri) {
		this.pictureUri = pictureUri;
	}

	/**
	 * Return the name of group
	 */
	public String getGroupName() {
		return groupName;
	}

	/**
	 * Store the text of the group name
	 */
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getMNumber() {
		return mnumber;
	}

	/**
	 * Store the text of the group name
	 */
	public void setMNumber(String mnumber) {
		this.mnumber = mnumber;
	}

	/**
	 * Return the name of group
	 */
	public String getPrivacy() {
		return privacy;
	}

	/**
	 * Store the text of the group name
	 */
	public void setPrivacy(String privacy) {
		this.privacy = privacy;
	}

	/**
	 * Two messages have the same hashcode if their picture URL and message text
	 * are the same
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((groupName == null) ? 0 : groupName.hashCode());
		result = prime * result
				+ ((pictureUri == null) ? 0 : pictureUri.hashCode());
		return result;
	}

	/**
	 * Two messages are equal if their picture URL and message text are equal
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GroupMeta other = (GroupMeta) obj;
		if (groupName == null) {
			if (other.groupName != null)
				return false;
		} else if (!groupName.equals(other.groupName))
			return false;
		if (pictureUri == null) {
			if (other.pictureUri != null)
				return false;
		} else if (!pictureUri.equals(other.pictureUri))
			return false;
		return true;
	}

}
