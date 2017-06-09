package ch.epfl.hci.healthytogether;

/**
 * Message bubble
 */
public class OneComment {
	public boolean left;
	public String comment;
	public String date;

	/** only used for received msgs (not for sent ones) */
	private int msgId;

	/**
	 * @param left
	 *            <code>true</code> if the user received the msg.
	 *            <code>false</code> if the user sent the msg, (the messages
	 *            sent by the user appear on the right).
	 * @param comment
	 */
	public OneComment(boolean left, String comment, String date, int msgId) {
		super();
		this.left = left;
		this.comment = comment;
		this.msgId = msgId;
		this.date = date;
	}

	public OneComment(boolean left, String comment) {
		super();
		this.left = left;
		this.comment = comment;
		msgId = -1;
	}

	public int getMsgId() {
		return msgId;
	}

	/**
	 * @return true if the msg is sent by the user, false if it is received
	 */
	public boolean isMine() {
		return !left;
	}

}