package ch.epfl.hci.healthytogether.model;

import java.util.ArrayList;

import ch.epfl.hci.healthytogether.OneDayBadges;

/**
 * Already parsed result of a CheckBadgeTask
 */
public class BadgeResult {

	private boolean isError;
	private String mErrorMsg;

	private final int mBadgeCount;
	private final int mMaxBadgeType;

	private int userId;

	/**
	 * @param badgeCount
	 *            how many badges the user has received
	 * @param maxBadgeType
	 *            the highest badge the user received
	 */
	public BadgeResult(int userId, int badgeCount, int maxBadgeType) {
		this.userId = userId;
		mBadgeCount = badgeCount;
		mMaxBadgeType = maxBadgeType;
		isError = false;
	}

	public BadgeResult(String error) {
		mBadgeCount = -1;
		mMaxBadgeType = -1;
		isError = true;
		mErrorMsg = error;
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
