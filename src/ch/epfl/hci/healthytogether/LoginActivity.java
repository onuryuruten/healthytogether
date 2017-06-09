package ch.epfl.hci.healthytogether;

import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import ch.epfl.hci.happytogether.R;
import ch.epfl.hci.healthytogether.communication.ServerHelper;
import ch.epfl.hci.healthytogether.communication.ServerHelper.CheckGroupTask;
import ch.epfl.hci.healthytogether.communication.ServerHelper.RetrieveBuddyEMailTask;
import ch.epfl.hci.healthytogether.communication.ServerHelper.RetrieveBuddyEMailTask2;
import ch.epfl.hci.healthytogether.communication.ServerHelper.SendPasswordReminderTask;
import ch.epfl.hci.healthytogether.service.CheckForMessageService;
import ch.epfl.hci.healthytogether.util.Utils;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends Activity {

	/**
	 * The default email to populate the email field with.
	 */
	public static final String EXTRA_EMAIL = "com.example.android.authenticatordemo.extra.EMAIL";

	public static final int REQUEST_CODE_AUTHORIZE = 1;

	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private UserLoginTask mAuthTask = null;

	// Values for email and password at the time of the login attempt.
	private String mEmail;
	private String mPassword;

	// UI references.
	private EditText mEmailView;
	private EditText mPasswordView;
	private View mLoginFormView;
	private View mLoginStatusView;
	private TextView mLoginStatusMessageView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		boolean loggedIn = AppContext.getInstance().isUserCredentialsSet();

		SharedPreferences prefs = getSharedPreferences(
				Constants.PROPERTIES_NAME, MODE_PRIVATE);
		boolean isAlreadyPaired = prefs.getBoolean(
				Constants.PROP_KEY_GAME_STARTED, false);

		if (loggedIn) {
			Intent intent;

			if (isAlreadyPaired) {
				intent = new Intent(LoginActivity.this, Main2Activity.class);
			} else {
				intent = new Intent(LoginActivity.this, SettingsActivity.class);
			}

			// In the line below, it was previously Main2Activity.
			// Intent intent = new Intent(LoginActivity.this,
			// SettingsActivity.class);
			// startActivityForResult(intent, REQUEST_CODE_AUTHORIZE);
			intent.putExtra("fromLogin", true);
			this.startActivity(intent);
			this.finishActivity(0);
		} else {
			Constants.loggedIn = false;
			setContentView(R.layout.activity_login);
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			setupActionBar();

			
			// Set up the login form.
			mEmail = getIntent().getStringExtra(EXTRA_EMAIL);
			mEmailView = (EditText) findViewById(R.id.email);

			String email = AppContext.getInstance().getEmail();
			if (email != null) {
				Log.d("Login", "restoring saved email");
				mEmailView.setText(email);
			}

			mPasswordView = (EditText) findViewById(R.id.password);
			mPasswordView
					.setOnEditorActionListener(new TextView.OnEditorActionListener() {
						@Override
						public boolean onEditorAction(TextView textView,
								int id, KeyEvent keyEvent) {
							if (id == R.id.login || id == EditorInfo.IME_NULL) {
								attemptLogin();
								return true;
							}
							return false;
						}
					});
			String pwd = AppContext.getInstance().getPassword();
			if (pwd != null) {
				Log.d("Login", "restoring saved pass");
				mPasswordView.setText(pwd);
			}

			mLoginFormView = findViewById(R.id.login_form);
			mLoginStatusView = findViewById(R.id.login_status);
			mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);

			findViewById(R.id.sign_in_button).setOnClickListener(
					new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							attemptLogin();
						}
					});
			
			TextView tv = (TextView)findViewById(R.id.forgottxt);
			tv.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					recoverPassword();
					
				}
			});
			
			
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		boolean loggedIn = AppContext.getInstance().isUserCredentialsSet();
		SharedPreferences prefs = getSharedPreferences(
				Constants.PROPERTIES_NAME, MODE_PRIVATE);
		boolean isAlreadyPaired = prefs.getBoolean(
				Constants.PROP_KEY_GAME_STARTED, false);

		if (loggedIn) {
			Intent intent;

			if (isAlreadyPaired) {
				intent = new Intent(LoginActivity.this, Main2Activity.class);
			} else {
				intent = new Intent(LoginActivity.this, SettingsActivity.class);
			}
			// startActivityForResult(intent, REQUEST_CODE_AUTHORIZE);
			intent.putExtra("fromLogin", true);
			startActivity(intent);
		}

		// Onur: The code block below was applied "onResume" before
		// SettingsActivity was introduced.
		// Should check whether its absence would cause any trouble.

		/*
		 * if(loggedIn) { Intent intent = new Intent(LoginActivity.this,
		 * Main2Activity.class); //startActivityForResult(intent,
		 * REQUEST_CODE_AUTHORIZE); intent.putExtra("fromLogin", true);
		 * startActivity(intent); }
		 */
	}

	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent i_settings = new Intent(LoginActivity.this,IntroActivity.class);
			startActivity(i_settings);			
			//moveTaskToBack(true);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	/*
	 * @Override public boolean onKeyDown(int keyCode, KeyEvent event) { if
	 * (keyCode == KeyEvent.KEYCODE_BACK) { moveTaskToBack(true); return true; }
	 * return super.onKeyDown(keyCode, event); }
	 */

	/**
	 * 
	 * 
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			// Show the Up button in the action bar.
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
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
			// that hierarchy.
			NavUtils.navigateUpFromSameTask(this);
			return true;
		case R.id.menu_forgot_password:
			recoverPassword();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.activity_login, menu);
		return true;
	}

	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptLogin() {
		if (mAuthTask != null) {
			return;
		}

		// Reset errors.
		mEmailView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
		mEmail = mEmailView.getText().toString();
		mPassword = mPasswordView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password.
		if (TextUtils.isEmpty(mPassword)) {
			mPasswordView.setError(getString(R.string.error_field_required));
			focusView = mPasswordView;
			cancel = true;
		} else if (mPassword.length() < 4) {
			mPasswordView.setError(getString(R.string.error_invalid_password));
			focusView = mPasswordView;
			cancel = true;
		}

		// Check for a valid email address.
		if (TextUtils.isEmpty(mEmail)) {
			mEmailView.setError(getString(R.string.error_field_required));
			focusView = mEmailView;
			cancel = true;
		} else if (!mEmail.contains("@")) {
			mEmailView.setError(getString(R.string.error_invalid_email));
			focusView = mEmailView;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.

			Constants.newLogin = true;

			mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
			showProgress(true);
			mAuthTask = new UserLoginTask() {

			};
			mAuthTask.execute((Void) null);
		}
	}

	public void recoverPassword()
	{
	//	Toast.makeText(this, "too bad for you", Toast.LENGTH_SHORT).show();
		
	
			// launch an edit-text alert dialog...

			AlertDialog.Builder alert = new AlertDialog.Builder(
					LoginActivity.this);

			LayoutInflater inflater = getLayoutInflater();
			View viewHeader = inflater.inflate(R.layout.customtitle,
					null);
			TextView title = (TextView) viewHeader
					.findViewById(R.id.myTitle);
			title.setText(getResources().getString(R.string.forgot_password_title));
			alert.setCustomTitle(viewHeader);
			// alert.setTitle("Customize Activity");
			alert.setMessage(getResources().getString(R.string.forgot_password_content));

			// Set an EditText view to get user input
			final EditText input = new EditText(LoginActivity.this);// new
																	// EditText(this);
			alert.setView(input);

			alert.setPositiveButton(getResources().getString(R.string.ok_button_string),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {

							final String listItem = input.getText()
									.toString();
							SendPasswordReminderTask task = new SendPasswordReminderTask(listItem)
							{

								@Override
								protected void onPostExecute(Boolean success) 
								{
									// TODO Auto-generated method stub
									if(success)
									{
										Toast.makeText(LoginActivity.this, LoginActivity.this.getResources().getString(R.string.forgot_password_result) + " " + listItem, Toast.LENGTH_LONG).show();
									}
									else if(!Utils.isConnectionPresent(LoginActivity.this))
									{
										Toast.makeText(LoginActivity.this,LoginActivity.this.getResources().getString(R.string.connection_toast_message),Toast.LENGTH_SHORT).show();
									}
									else if(outcome.equalsIgnoreCase("not validated"))
									{
										Toast.makeText(LoginActivity.this, LoginActivity.this.getResources().getString(R.string.error_invalid_email) + ": " + listItem, Toast.LENGTH_LONG).show();
									}
									else
									{
										Toast.makeText(LoginActivity.this,LoginActivity.this.getResources().getString(R.string.connection_toast_message),Toast.LENGTH_SHORT).show();
									}
								}
								
							};
							task.execute();
							
						}
					});

			alert.setNegativeButton(getResources().getString(R.string.cancel_button),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							// Canceled.
						}
					});
			alert.show();
		

	
		
	}
	
	/*public void onPasswordRecoveryClicked(View v)
	{
		
	}*/
	
	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mLoginStatusView.setVisibility(View.VISIBLE);
			mLoginStatusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginStatusView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});

			mLoginFormView.setVisibility(View.VISIBLE);
			mLoginFormView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginFormView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	public class UserLoginTask extends AsyncTask<Void, Void, String> {
		@Override
		protected String doInBackground(Void... params) {
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("email", mEmail));
			nameValuePairs.add(new BasicNameValuePair("pswrd", mPassword));

			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(ServerHelper.URL_LOGIN);
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				if (Utils.isConnectionPresent(LoginActivity.this)) {
					HttpResponse response = httpclient.execute(httppost);
					HttpEntity entity = response.getEntity();
					String responseStr = EntityUtils.toString(entity);
					Log.i("LOGIN_CHECK", responseStr);

					if (responseStr.startsWith("OK")) {
						responseStr = Utils.removeNewLines(responseStr);
						Log.i("LOGIN_RESPONSE", responseStr.substring(2) + ")");
						return responseStr.substring(2);
					} else {
						return responseStr;
					}
				} else {
					Log.e("log_tag", "Error: No internet connection available");
					return getResources().getString(R.string.error) + ": " + getResources().getString(R.string.connection_toast_message);//"Error: No internet connection available";
				}
			} catch (Exception e) {
				Log.e("log_tag", "Error in http connection " + e.toString());
				//return "Error in http connection " + e.toString();
				return getResources().getString(R.string.error) + ": " + getResources().getString(R.string.connection_toast_message);
			}

			// for (String credential : DUMMY_CREDENTIALS) {
			// String[] pieces = credential.split(":");
			// if (pieces[0].equals(mEmail)) {
			// // Account exists, return true if the password matches.
			// return pieces[1].equals(mPassword);
			// }
			// }
		}

		@Override
		protected void onPostExecute(final String response) {
			mAuthTask = null;
			// showProgress(false);

			if (Utils.isInteger(response)) {
				// uid = user id if successful login, otherwise error code
				int uid = Integer.parseInt(response);

				if (uid == 0) {
					showProgress(false);
					mEmailView
							.setError(getString(R.string.error_incorrect_email));
					mEmailView.requestFocus();
				} else if (uid == -1) {
					showProgress(false);
					mPasswordView
							.setError(getString(R.string.error_incorrect_password));
					mPasswordView.requestFocus();
				} else {
					// login successful, store user id and continue

					AppContext.getInstance().setUserId(uid);
					AppContext.getInstance().setEmail(mEmail);
					AppContext.getInstance().setPassword(
							mPasswordView.getText().toString());

					// Onur: added the following line at Sept. 15
					AppContext.getInstance().setUserCredentialsSet(true);

					// SharedPreferences prefs =
					// getSharedPreferences(Constants.PROPERTIES_NAME,
					// Context.MODE_PRIVATE);

					RetrieveBuddyEMailTask2 rtask = new RetrieveBuddyEMailTask2(
							AppContext.getInstance().getEmail()) {
						@Override
						protected void onPostExecute(String buddyEmail) {
							showProgress(false);

							if (Utils.isConnectionPresent(LoginActivity.this)) {
								Log.d("LoginActivity - retrieve BuddyEmailTask",
										"response: " + buddyEmail);
								if (buddyEmail.contains("not found")) {
									// Toast.makeText(LoginActivity.this,
									// "The system can not find your buddy.",
									// Toast.LENGTH_LONG).show();
								} else {
									SharedPreferences prefs = getSharedPreferences(
											Constants.PROPERTIES_NAME,
											Context.MODE_PRIVATE);
									Editor editor = prefs.edit();
									editor.putBoolean(
											Constants.PROP_KEY_GAME_STARTED,
											true);
									editor.commit();
								}
								setResult(Activity.RESULT_OK);
								finish();
							} else {
								Toast.makeText(
										LoginActivity.this,
										getResources().getString(R.string.connection_toast_message),
										Toast.LENGTH_SHORT).show();
							}

						}
					};

					rtask.execute();

					/*
					 * CheckGroupTask checkGroupTask= new
					 * CheckGroupTask(AppContext.getInstance().getEmail(),
					 * AppContext.getInstance().getFriendId()) {
					 * 
					 * @Override protected void onPostExecute(String result) {
					 * showProgress(false);
					 * Log.d("LoginActivity - checkgrouptask", "response: " +
					 * result); if (Utils.isInteger(result)) { int resultCode =
					 * Integer.parseInt(result); switch (resultCode) { case
					 * RESPONSE_CODE_NO_BUDDY: break; case
					 * RESPONSE_INCOMING_REQUEST_PENDING: break; case
					 * RESPONSE_WAITING_FOR_ACCEPT: break; default:
					 * SharedPreferences prefs =
					 * getSharedPreferences(Constants.PROPERTIES_NAME,
					 * Context.MODE_PRIVATE); Editor editor = prefs.edit();
					 * editor.putBoolean(Constants.PROP_KEY_GAME_STARTED, true);
					 * editor.commit(); break; } } else
					 * if(!Utils.isConnectionPresent(LoginActivity.this)) {
					 * Toast.makeText(LoginActivity.this,
					 * "Internet connection error. Please try again later.",
					 * Toast.LENGTH_SHORT).show(); } else { // error log_tag
					 * Log.e("LoginActivity - checkgrouptask",
					 * "an error has occurred");
					 * //Toast.makeText(Main2Activity.this,
					 * R.string.error_general, Toast.LENGTH_LONG).show(); }
					 * 
					 * setResult(Activity.RESULT_OK); finish(); }
					 * 
					 * }; checkGroupTask.execute();
					 */

				}
			} else {
				showProgress(false);
				// an error has occurred
				if (response.startsWith("Error:") || response.startsWith(getResources().getString(R.string.error))) {
					mPasswordView.setError(response);
					mPasswordView.requestFocus();
				} else {
					new UserLoginTask().execute((Void) null);
				}

			}
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			showProgress(false);
		}
	}

	public void registerClicked(View v) {
		Intent i = new Intent(this, RegisterActivity.class);
		startActivity(i);
		// startActivityForResult(i, REQUEST_CODE_REGISTER);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE_AUTHORIZE) {
			if (resultCode == Activity.RESULT_OK) {
				setResult(Activity.RESULT_OK);
				Log.d("LoginActivity", "going back to IntroActivity");
				finish();
			}
		}
	}
}
