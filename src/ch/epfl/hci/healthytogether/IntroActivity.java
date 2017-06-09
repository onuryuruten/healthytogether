package ch.epfl.hci.healthytogether;

import ch.epfl.hci.happytogether.R;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

public class IntroActivity extends Activity {

	public static final int REQUEST_CODE_LOGIN = 1;
	public static final int REQUEST_CODE_REGISTER = 2;

	public Button loginBtn, startBtn;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		super.onCreate(savedInstanceState);

		boolean loggedIn = AppContext.getInstance().isUserCredentialsSet();
		SharedPreferences prefs = getSharedPreferences(
				Constants.PROPERTIES_NAME, MODE_PRIVATE);
		boolean isAlreadyPaired = prefs.getBoolean(
				Constants.PROP_KEY_GAME_STARTED, false);

		if (loggedIn) {
			// Previously, it redirected to Main2Activity. Check for
			// consistency.
			Intent intent;

			if (isAlreadyPaired) {
				intent = new Intent(IntroActivity.this, Main2Activity.class);
			} else {
				intent = new Intent(IntroActivity.this, SettingsActivity.class);
			}
			// startActivityForResult(intent, REQUEST_CODE_AUTHORIZE);
			intent.putExtra("fromLogin", true);
			this.startActivity(intent);
			this.finishActivity(0);
		} else {
			setContentView(R.layout.activity_intro);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			//Intent i_settings = new Intent(LoginActivity.this,IntroActivity.class);
			//startActivity(i_settings);			
			moveTaskToBack(true);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}

	public void loginClicked(View view) {
		Intent i = new Intent(this, LoginActivity.class);
		startActivityForResult(i, REQUEST_CODE_LOGIN);
	}

	public void registerClicked(View v) {
		Intent i = new Intent(this, RegisterActivity.class);
		startActivityForResult(i, REQUEST_CODE_REGISTER);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE_LOGIN) {
			if (resultCode == Activity.RESULT_OK) {
				SharedPreferences prefs = getSharedPreferences(
						Constants.PROPERTIES_NAME, MODE_PRIVATE);
				boolean isAlreadyPaired = prefs.getBoolean(
						Constants.PROP_KEY_GAME_STARTED, false);
				Intent i;
				// login was successful
				if (isAlreadyPaired) {
					i = new Intent(this, Main2Activity.class);
				} else {
					i = new Intent(this, SettingsActivity.class);
				}
				startActivity(i);
			}
		} else if (requestCode == REQUEST_CODE_REGISTER) {
			if (resultCode == Activity.RESULT_OK) {
				// register and login was successful. Previously, it redirected
				// to Main2Activity
				SharedPreferences prefs = getSharedPreferences(
						Constants.PROPERTIES_NAME, MODE_PRIVATE);
				boolean isAlreadyPaired = prefs.getBoolean(
						Constants.PROP_KEY_GAME_STARTED, false);				
				
				Intent i = new Intent(this, SettingsActivity.class);
				startActivity(i);
			}
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

}
