package ch.epfl.hci.healthytogether;

/**
 * Message bubble
 */
public class OneDayBadges {
	public boolean[] validBadges;
	public String date;
	public String badgesteps;

	/** only used for received msgs (not for sent ones) */
	private int logId;
	public int uid;

	/**
	 * @param left
	 *            <code>true</code> if the user received the msg.
	 *            <code>false</code> if the user sent the msg, (the messages
	 *            sent by the user appear on the right).
	 * @param activity
	 */
	public OneDayBadges(int uid, String date, int logId) {
		super();
		setBadgeArray();
		this.date = date;
		this.logId = logId;
		this.uid = uid;
	}

	public OneDayBadges(int uid, String logTime) {
		super();
		this.date = logTime;
		logId = -1;
		this.uid = uid;
		setBadgeArray();
	}

	public OneDayBadges() {
		super();
		this.uid = AppContext.getInstance().getUserId();
		setBadgeArray();
	}

	public int getLogId() {
		return this.logId;
	}

	public void setLogId(int logid) {
		this.logId = logid;
	}

	public void setBadgeArray() {
		validBadges = new boolean[Constants.BADGE_COUNT];

		for (int i = 0; i < Constants.BADGE_COUNT; i++) {
			validBadges[i] = false;
		}
	}

	public void activateBadges(int[] currentBadges) {
		if (validBadges == null) {
			setBadgeArray();
		}
		for (int i = 0; i < currentBadges.length; i++) {
			if (currentBadges[i] < Constants.BADGE_COUNT) {
				validBadges[currentBadges[i]] = true;
			}

		}
	}

	public void activateBadges(int badgeLevel) {
		if (badgeLevel > 0) {
			if (validBadges == null) {
				setBadgeArray();
			}
			for (int i = 0; i < Math.min(badgeLevel, Constants.BADGE_COUNT); i++) {
				validBadges[i] = true;
			}
		}
	}

	public boolean getBadge(int i) {
		if (i < Constants.BADGE_COUNT)
			return validBadges[i];

		return false;
	}

	public boolean[] getValidBadges() {
		return validBadges;
	}

	public int getLargestBadge() {
		for (int i = Constants.BADGE_COUNT - 1; i >= 0; i--) {
			if (validBadges[i]) {
				return i;
			}
		}
		return -1;
	}

}