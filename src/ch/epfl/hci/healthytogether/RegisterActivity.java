package ch.epfl.hci.healthytogether;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import ch.epfl.hci.happytogether.R;
import ch.epfl.hci.healthytogether.communication.ServerHelper;
import ch.epfl.hci.healthytogether.communication.ServerHelper.RegisterTask;
import ch.epfl.hci.healthytogether.util.Utils;

public class RegisterActivity extends Activity {

	public static final String TAG = RegisterActivity.class.getName();

	private static final int REQUEST_CODE_LOGIN = 1;

	public Button registerBtn;

	public EditText registerEmailTxt, PwdTxt, confirmPwdlTxt;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		registerEmailTxt = (EditText) findViewById(R.id.registeremail);

		PwdTxt = (EditText) findViewById(R.id.registerpwd);

		confirmPwdlTxt = (EditText) findViewById(R.id.confirmpwd);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_register, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void onRegisterClicked(View v) {
		final String username = ((EditText) findViewById(R.id.username))
				.getText().toString();

		registerEmailTxt.setError(null);
		confirmPwdlTxt.setError(null);

		final String emailStr = registerEmailTxt.getText().toString().toLowerCase();
		final String pwdStr = PwdTxt.getText().toString();
		String repwdStr = confirmPwdlTxt.getText().toString();

		if (username.length() == 0 || emailStr.length() == 0
				|| pwdStr.length() == 0 || repwdStr.length() == 0) {
			Toast.makeText(this, getResources().getString(R.string.all_fields_please),
					Toast.LENGTH_LONG).show();
		} else if (!pwdStr.equals(repwdStr)) {
			// Toast.makeText(this, "Passwords do not match",
			// Toast.LENGTH_LONG).show();
			confirmPwdlTxt.setError(getResources().getString(R.string.password_match_error));
			confirmPwdlTxt.requestFocus();
		} else if (!Constants.validEmail(emailStr)) {
			registerEmailTxt.setError(getResources().getString(R.string.error_incorrect_email));
			registerEmailTxt.requestFocus();
			// Toast.makeText(this, "Please enter a valid email address",
			// Toast.LENGTH_LONG).show();
		} else if ((pwdStr.length() < 4)) {
			PwdTxt.setError(getResources().getString(R.string.password_length));
			PwdTxt.requestFocus();
		} else {
			Log.d(TAG, "checking inputs");
			RegisterTask task = new RegisterTask(emailStr, pwdStr, username) 
			{
				protected void onPreExecute()
				{
					Button b = (Button)findViewById(R.id.registerbtn);
					b.setClickable(false);
					b.setEnabled(false);
				}
				
				@Override
				protected void onPostExecute(String msg) 
				{
					Button b = (Button)findViewById(R.id.registerbtn);
					b.setClickable(true);
					b.setEnabled(true);					
					
					if (Utils.isInteger(msg)) {
						int res = Integer.parseInt(msg);
						if (res == 0) 
						{
							registerEmailTxt.setError(getResources().getString(R.string.email_already_registered));
							registerEmailTxt.requestFocus();
							/*Toast.makeText(RegisterActivity.this,
									"Email already registered!",
									Toast.LENGTH_LONG).show();*/
						} else if (res > 0) {
							AppContext.getInstance().setUserId(res);
							AppContext.getInstance().setUserString(username);
							//AppContext.getInstance().setUserCredentialsSet(true);
							AppContext.getInstance().setEmail(emailStr);
							AppContext.getInstance().setPassword(pwdStr);
							AppContext.getInstance().setUserCredentialsSet(true);
							
							Log.d(RegisterActivity.TAG,"My user id is " + res );
							/*Toast.makeText(RegisterActivity.this,
									"Account created. Please login.",
									Toast.LENGTH_LONG).show();*/
							
							//Intent i = new Intent(	RegisterActivity.this,
							//		SettingsActivity.class);
							//startActivity(i);
							
							setResult(Activity.RESULT_OK);
							finish();
							
							/*AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
							builder.setTitle(getResources().getString(R.string.registration_complete_title))
									.setMessage(
											getResources().getString(R.string.registration_complete_content))
									.setCancelable(false)
									.setNeutralButton(getResources().getString(R.string.ok_button_string),
											new OnClickListener() {

												@Override
												public void onClick(DialogInterface dialog,
														int which) 
												{
													Intent i = new Intent(	RegisterActivity.this,
															LoginActivity.class);
														startActivityForResult(i, REQUEST_CODE_LOGIN);
												}
											});
							builder.create().show();*/

						} else {
							Toast.makeText(RegisterActivity.this,
									getResources().getString(R.string.connection_toast_message),
									Toast.LENGTH_LONG).show();
						}

					} else if (!Utils
							.isConnectionPresent(RegisterActivity.this)) {
						Toast.makeText(
								RegisterActivity.this,
								getResources().getString(R.string.connection_toast_message),
								Toast.LENGTH_LONG).show();
					} else {
						Log.e(TAG,"An error occurred: " + msg);
					}
				}

			};
			task.execute();
		}
	}

	public void loginClicked(View view) {
		Intent i = new Intent(this, LoginActivity.class);
		//startActivity(i);
		startActivityForResult(i, REQUEST_CODE_LOGIN);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE_LOGIN) {
			if (resultCode == Activity.RESULT_OK) {
				setResult(Activity.RESULT_OK);
				Log.d(TAG, "login ok, going back to IntroActivity");
				finish();
			}
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}
	
	
	

}
