package ch.epfl.hci.healthytogether;

public class UserEntry {

	String userName;
	String userEmail;
	private int availability;
	
	/** Rationale for availability:
	 * 
	 * 	We would like to give higher priority to those who are single
	 * 
	 *  GroupCheckTask query would return the following values:
	 *  
	 *  1. a > 0 : The user has a buddy
	 *  2. a = 0 : The user has no buddy
	 *  3. a = -1: The user has invited someone, but he is not answered yet.
	 *  4. a = -2: The user has received some invitations.
	 *  
	 *  To increase the chances of pairing up, the system should suggest:
	 *  
	 *  2 > 3 > 4 > 1
	 * 
	 */

	public UserEntry() {
		userName = null;
		userEmail = null;
		setAvailability(0);
	}

	public UserEntry(String userName, String userEmail) {
		this.userName = userName;
		this.userEmail = userEmail;
	}

	public UserEntry(String userName, String userEmail, int availability) {
		this.userName = userName;
		this.userEmail = userEmail;
		this.setAvailability(availability);
	}	
	
	public void setUserName(String userName) {
		this.userName = userName;
	}

	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}

	public String getUserName() {
		return userName;
	}

	public String getUserEmail() {
		return userEmail;
	}

	public String toString() {
		return "User: " + userName + " Email: " + userEmail;
	}

	public int getAvailability() 
	{
		return availability;
	}

	public void setAvailability(int availability)
	{
		if(availability > 0) // "already has a buddy"
			this.availability = -3;
		else
			this.availability = availability;
	}

}
