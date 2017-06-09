/**
 * 
 */
package ch.epfl.hci.happytogether;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Mainly used to provide access to the global application context when not
 * working in an activity.
 */
public class App extends Application {

	private static App INSTANCE;
	public Context mContext;

	@Override
	public void onCreate() {
		super.onCreate();
		INSTANCE = this;
		mContext = getApplicationContext();

		// TODO only for debugging, reset preferences. To be removed afterwards
		// Log.w("WARNING", "Resetting preferences. only fur DEBUGING!");
		// SharedPreferences prefs =
		// getSharedPreferences(Constants.PROPERTIES_NAME, MODE_PRIVATE);
		// Editor editor = prefs.edit();
		// editor.putBoolean(Constants.PROP_KEY_GAME_STARTED, false);
		// editor.putBoolean(Constants.PROP_KEY_AUTHORIZED, false);
		// editor.commit();

		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				return null;
			}
		}.execute();
	}

	public static App getInstance() {
		return INSTANCE;
	}

}
