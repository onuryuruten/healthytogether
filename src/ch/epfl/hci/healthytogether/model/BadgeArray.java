package ch.epfl.hci.healthytogether.model;

import java.util.ArrayList;

import ch.epfl.hci.healthytogether.OneDayBadges;

/**
 * Already parsed result of a CheckBadgeTask
 */
public class BadgeArray {

	private boolean isError;
	private String mErrorMsg;

	private int mBadgeCount;
	private int mMaxBadgeType;

	private int userId;

	private ArrayList<OneDayBadges> allBadgeRecords;

	/**
	 * @param badgeCount
	 *            how many badges the user has received
	 * @param maxBadgeType
	 *            the highest badge the user received
	 */
	public BadgeArray(int userId, int badgeCount, int maxBadgeType) {
		this.userId = userId;
		mBadgeCount = badgeCount;
		mMaxBadgeType = maxBadgeType;
		isError = false;
		allBadgeRecords = new ArrayList<OneDayBadges>();
	}

	public BadgeArray() {
		allBadgeRecords = new ArrayList<OneDayBadges>();
	}

	public BadgeArray(String error) {
		mBadgeCount = -1;
		mMaxBadgeType = -1;
		isError = true;
		mErrorMsg = error;
	}

	public void addBadge(OneDayBadges badge) {
		allBadgeRecords.add(badge);
	}

	public ArrayList<OneDayBadges> getBadgesList() {
		return allBadgeRecords;
	}

	public void setBadgesList(ArrayList<OneDayBadges> odb) {
		allBadgeRecords = odb;
	}

	public boolean isError() {
		return isError;
	}

	public int getmBadgeCount() {
		return mBadgeCount;
	}

	/**
	 * @return the highest badge the user received
	 */
	public int getmMaxBadgeType() {
		return mMaxBadgeType;
	}

	public int getUserId() {
		// TODO Auto-generated method stub
		return userId;
	}

	public String getBadgeId() {
		// TODO Auto-generated method stub
		return "UserBadge";
	}

}
