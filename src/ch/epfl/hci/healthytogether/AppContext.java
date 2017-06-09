package ch.epfl.hci.healthytogether;

import ch.epfl.hci.happytogether.App;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * Global data storage, to prevent passing this data around via intent extras
 * all the time.
 * 
 * @author Danni LE
 * 
 */
public class AppContext {

	private static AppContext INSTANCE = new AppContext();
	// private int userId; // persisted
	private int mFriendId;
	// private String mEmail; // persisted

	private String userString;
	private String passwordString;
	private boolean userCredentialsSet;

	private AppContext() {
	}

	public static AppContext getInstance() {
		return INSTANCE;
	}

	public int getUserId() {
		Context context = App.getInstance().getApplicationContext();
		SharedPreferences prefs = context.getSharedPreferences(
				Constants.PROPERTIES_NAME, Context.MODE_PRIVATE);
		return prefs.getInt(Constants.PROP_KEY_USER_ID, -1);
		// return userId;
	}

	public int getFriendId() {
		return mFriendId;
	}

	public void setFriendId(int friendId) {
		mFriendId = friendId;
	}

	public void setUserId(int userId) {
		Context context = App.getInstance().getApplicationContext();
		SharedPreferences prefs = context.getSharedPreferences(
				Constants.PROPERTIES_NAME, Context.MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.putInt(Constants.PROP_KEY_USER_ID, userId);
		editor.commit();
		// this.userId = userId;
	}

	public void setEmail(String email) {
		Context context = App.getInstance().getApplicationContext();
		SharedPreferences prefs = context.getSharedPreferences(
				Constants.PROPERTIES_NAME, Context.MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.putString(Constants.PROP_KEY_EMAIL, email);
		editor.commit();
		// mEmail = email;
	}

	public void setPassword(String pwd) {
		Context context = App.getInstance().getApplicationContext();
		SharedPreferences prefs = context.getSharedPreferences(
				Constants.PROPERTIES_NAME, Context.MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.putString(Constants.PROP_KEY_PASSWORD, pwd);
		editor.commit();
		setPasswordString(pwd);
	}

	/**
	 * @return the stored password, or <code>null</code> if the user has not
	 *         logged in yet
	 */
	public String getPassword() {
		Context context = App.getInstance().getApplicationContext();
		SharedPreferences prefs = context.getSharedPreferences(
				Constants.PROPERTIES_NAME, Context.MODE_PRIVATE);
		return prefs.getString(Constants.PROP_KEY_PASSWORD, null);
	}

	/**
	 * @return the email address of the user
	 */
	public String getEmail() {
		Context context = App.getInstance().getApplicationContext();
		SharedPreferences prefs = context.getSharedPreferences(
				Constants.PROPERTIES_NAME, Context.MODE_PRIVATE);
		return prefs.getString(Constants.PROP_KEY_EMAIL, null);
		// return mEmail;
	}

	public String getUserString() {
		Context context = App.getInstance().getApplicationContext();
		SharedPreferences prefs = context.getSharedPreferences(
				Constants.PROPERTIES_NAME, Context.MODE_PRIVATE);
		return prefs.getString(Constants.PROP_KEY_UNAME, null);
	}

	public void setUserString(String userString) {
		this.userString = userString;
		Context context = App.getInstance().getApplicationContext();
		SharedPreferences prefs = context.getSharedPreferences(
				Constants.PROPERTIES_NAME, Context.MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.putString(Constants.PROP_KEY_UNAME, userString);
		editor.commit();
	}

	public String getPasswordString() {
		Context context = App.getInstance().getApplicationContext();
		SharedPreferences prefs = context.getSharedPreferences(
				Constants.PROPERTIES_NAME, Context.MODE_PRIVATE);
		return prefs.getString(Constants.PROP_KEY_PASSWORD, null);
		// return passwordString;
	}

	public void setPasswordString(String passwordString) {
		this.passwordString = passwordString;
	}

	public boolean isUserCredentialsSet() {
		Context context = App.getInstance().getApplicationContext();
		SharedPreferences prefs = context.getSharedPreferences(
				Constants.PROPERTIES_NAME, Context.MODE_PRIVATE);
		return prefs.getBoolean(Constants.PROP_KEY_CREDSET, false);
	}

	public void setUserCredentialsSet(boolean userCredentialsSet) {
		this.userCredentialsSet = userCredentialsSet;
		
		if(!userCredentialsSet)
		{
			Constants.FIRST_WELCOME_GIVEN = false;
		}
		
		Context context = App.getInstance().getApplicationContext();
		SharedPreferences prefs = context.getSharedPreferences(
				Constants.PROPERTIES_NAME, Context.MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.putBoolean(Constants.PROP_KEY_CREDSET, userCredentialsSet);
		editor.commit();
	}
}
