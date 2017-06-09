package ch.epfl.hci.healthytogether;

import android.widget.RadioGroup;

/**
 * Message bubble
 */
public class OneMood {

	/** only used for received msgs (not for sent ones) */
	private int moodId;
	private String moodString;
	private String moodIntensity;
	private RadioGroup radioGroup;

	/**
	 * @param left
	 *            <code>true</code> if the user received the msg.
	 *            <code>false</code> if the user sent the msg, (the messages
	 *            sent by the user appear on the right).
	 * @param activity
	 */
	public OneMood(int moodIndex, String moodStr, String normalString) {
		super();
		this.setMoodId(moodIndex);
		this.setMoodString(moodStr);
		this.setMoodIntensity(normalString);
	}

	public OneMood(int moodIndex, String moodStr, String normalString, boolean isPositive) {
		super();

		if (isPositive) {
			this.setMoodId(moodIndex);
		} else {
			this.setMoodId(Constants.positiveMoods.length + moodIndex);
			
		}
		this.setMoodString(moodStr);
		this.setMoodIntensity(normalString);
	}

	public String getMoodString() {
		return moodString;
	}

	public void setMoodString(String moodString) {
		this.moodString = moodString;
	}

	public String getMoodIntensity() {
		return moodIntensity;
	}

	public void setMoodIntensity(String moodIntensity) {
		this.moodIntensity = moodIntensity;
	}

	public int getMoodId() {
		return moodId;
	}

	public void setMoodId(int moodId) {
		this.moodId = moodId;
	}

	public RadioGroup getRadioGroup() {
		return radioGroup;
	}

	public void setRadioGroup(RadioGroup radioGroup) {
		this.radioGroup = radioGroup;
	}

}