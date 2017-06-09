package ch.epfl.hci.healthytogether;

import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import ch.epfl.hci.happytogether.R;
import ch.epfl.hci.healthytogether.communication.ServerHelper.AcceptBuddyRequestTask;
import ch.epfl.hci.healthytogether.communication.ServerHelper.CheckGroupTask;
import ch.epfl.hci.healthytogether.communication.ServerHelper.CheckInvitationDateTask;
import ch.epfl.hci.healthytogether.communication.ServerHelper.DeFriendTask;
import ch.epfl.hci.healthytogether.communication.ServerHelper.RemindPendingBuddyTask;
import ch.epfl.hci.healthytogether.communication.ServerHelper.RetrievePendingBuddyEMailTask;
import ch.epfl.hci.healthytogether.util.Utils;

/**
 * Allows the user to send a reminder to a buddy he invited.
 * 
 */
public class PendingBuddyRequestActivity extends Activity {

	private TextView mTextView;
	private AlertDialog alertDialog;
	private static final String TAG = PendingBuddyRequestActivity.class
			.toString();
	private Handler checkProgressHandler;
	public static String bdEM = "";
	public int gim = 0;
	public boolean sendWarningToUser = true;

	private boolean isInvitationStatusShowing = false;
	private AlertDialog invitationStatusDialog;

	private Runnable checkForProgressTask = new Runnable() {

		@Override
		public void run() {
			Log.e(TAG, ":::::::::::::::: RUNNING BUDDY CHECKER");

			final SharedPreferences prefs = getSharedPreferences(
					Constants.PROPERTIES_NAME, MODE_PRIVATE);
			// provide an e-mail to this person if the invitee did not respond
			// for some days.
			// sendWarningToUser serves to avoid a flood of e-mails to the user;
			// the warning is done once a day!
			sendWarningToUser = Constants.warningNotSentToday(prefs);

			CheckInvitationDateTask cidTask = new CheckInvitationDateTask(
					AppContext.getInstance().getUserId(), sendWarningToUser) {
				protected void onPostExecute(String dateStr) {
					if (dateStr != null) {
						if (dateStr.contains("IR")) {
							String msg = getResources().getString(R.string.invitation_rejected);
							msg = msg.replace("()", Constants.INVITEE);
							// Toast.makeText(PendingBuddyRequestActivity.this,
							// msg, Toast.LENGTH_LONG).show();
							Intent i = new Intent(
									PendingBuddyRequestActivity.this,
									SettingsActivity.class);
							i.putExtra("RequestAccepted", -1);
							i.putExtra("removalSeen", -1);
							i.putExtra("message", msg);
							// Toast.makeText(PendingBuddyRequestActivity.this,
							// msg, Toast.LENGTH_SHORT).show();

							Constants.INVITATION_CANCELED = false;
							Constants.COUNTER_INVITE = false;
							Constants.INVITATION_ACCEPTED = false;
							SharedPreferences prefs2 = getSharedPreferences(
									Constants.PROPERTIES_NAME, MODE_PRIVATE);
							Editor editor = prefs2.edit();
							editor.putBoolean(Constants.PROP_KEY_GAME_STARTED,
									false);
							editor.commit();
							startActivity(i);

						} else {
							// 1. Try to parse the date string.
							Date invitationDate = Constants
									.convertToDate(dateStr);

							// 2. If date parse is successful, retrieve the
							// "invitation date"

							if (invitationDate != null) {
								// TODO: 3. Compare the two dates, and show a
								// dialog.
								Date d = new Date();

								long difference = d.getTime()
										- invitationDate.getTime();

								if (sendWarningToUser
										&& difference > 24 * 3600000) {

									Editor editor = prefs.edit();
									editor.putString(
											Constants.PROPS_WARNING_DATE,
											dateStr);
									editor.commit();

									/*AlertDialog.Builder builder = new AlertDialog.Builder(
											PendingBuddyRequestActivity.this);*/

									long dayDifference = Math.round(Math
											.floor((difference * 1.0)
													/ (24 * 3600000)));
									
									displayInvitationStatusDialog(getResources().getString(R.string.long_waiting_it_has_been)
															+ " " + dayDifference
															+ " " + getResources().getString(R.string.long_waiting_days_since),0);
									
									// dayDifference = 2;
									/*builder.setTitle(
											getResources().getString(R.string.long_waiting_dialog_title))
											.setMessage(
													getResources().getString(R.string.long_waiting_it_has_been)
															+ dayDifference
															+ getResources().getString(R.string.long_waiting_days_since))
											.setPositiveButton(getResources().getString(R.string.ok_button_string), null);
									AlertDialog diag = builder.create();
									diag.show();*/
								} else if (!sendWarningToUser) {
									Log.i(TAG,
											"The long pending time was already sent to the user today");
								} else {
									Log.i(TAG, "One day has not passed yet");
								}

							} else {
								Log.e(TAG, "Could not parse the date string: "
										+ dateStr);
							}

						}

					} else if (!Utils
							.isConnectionPresent(PendingBuddyRequestActivity.this)) {
						Log.e(TAG,
								"Connection error; could not check the invitation date.");
					} else {
						// do nothing, but warn the system.
						Log.e(TAG,
								"CheckInvitationDateTask failed, with dateStr = null.");
					}
				}
			};
			cidTask.execute();

			RetrievePendingBuddyEMailTask rptask = new RetrievePendingBuddyEMailTask(
					AppContext.getInstance().getUserId()) {

				@Override
				protected void onPostExecute(String email) {
					bdEM = email;
					if (Constants.COUNTER_INVITE) {
						AcceptBuddyRequestTask task = new AcceptBuddyRequestTask(
								AppContext.getInstance().getUserId(),
								AppContext.getInstance().getEmail(), bdEM) {

							@Override
							protected void onPostExecute(String result) {
								if (Utils.isInteger(result)) {
									// The request was successful and the server
									// returned the friend's ID
									AppContext.getInstance().setFriendId(
											Integer.parseInt(result));
									Utils.scheduleAlarmReceiver(PendingBuddyRequestActivity.this);

									// Remove the notification (in case a new
									// one has been issued)
									NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
									notificationManager
											.cancel(Constants.NOTIFICATION_ID_PENDING_BUDDY_REQUEST);

									// Set property to indicate invite has been
									// accepted and game started

									Editor editor = prefs.edit();
									editor.putBoolean(
											Constants.PROP_KEY_GAME_STARTED,
											true);
									editor.commit();
									Constants.embarkAcceptanceDate(prefs); // i.e.,
																			// NOW
									// Go to main screen
									Intent i = new Intent(
											PendingBuddyRequestActivity.this,
											Main2Activity.class); //AcceptBuddyActivity
									i.putExtra("RequestAccepted", 10);
									startActivity(i);
									finish();
								} else {
									// An error has occurred
									ErrorHandler.create().handleError(
											PendingBuddyRequestActivity.this,
											result, null);
								}

							}
						};
						task.execute();
					}

					Constants.checkUserName();
					int userId = AppContext.getInstance().getUserId();

					String email2 = AppContext.getInstance().getEmail();
					CheckGroupTask checkGroupTask = new CheckGroupTask(email2,
							userId) {
						@Override
						protected void onPostExecute(String result) {
							Log.d(TAG, "response: " + result);
							if (Utils.isInteger(result)) {
								int resultCode = Integer.parseInt(result);
								Intent i;
								switch (resultCode) {
								case RESPONSE_CODE_NO_BUDDY:
									// show screen to invite buddy
									Utils.scheduleAlarmReceiver(PendingBuddyRequestActivity.this);
									String msg = "";
									if (Constants.INVITATION_RECEIVED) {
										Constants.INVITATION_RECEIVED = false;
										msg = getResources().getString(R.string.buddy_changed_mind);
									} else if (Constants.INVITATION_CANCELED) {
										Constants.INVITATION_CANCELED = false;
										msg = getResources().getString(R.string.your_invitation_canceled);
									} else {
										// check here! let's not make this
										// happen.
										// (i.e., when the buddy has accepted
										// your inv.)
										msg = getResources().getString(R.string.invitation_rejected);
										msg = msg.replace("()", Constants.INVITEE);
									}

									// Toast.makeText(PendingBuddyRequestActivity.this,
									// msg, Toast.LENGTH_LONG).show();
									i = new Intent(
											PendingBuddyRequestActivity.this,
											SettingsActivity.class);
									i.putExtra("RequestAccepted", -1);
									i.putExtra("message", msg);
									i.putExtra("removalSeen", -1);

									Constants.INVITATION_CANCELED = false;
									Constants.COUNTER_INVITE = false;
									Constants.INVITATION_ACCEPTED = false;
									SharedPreferences prefs2 = getSharedPreferences(
											Constants.PROPERTIES_NAME,
											MODE_PRIVATE);
									Editor editor = prefs2.edit();
									editor.putBoolean(
											Constants.PROP_KEY_GAME_STARTED,
											false);
									editor.commit();
									startActivity(i);
									break;
								case RESPONSE_INCOMING_REQUEST_PENDING:
									break;
								case RESPONSE_WAITING_FOR_ACCEPT:
									break;
								default:
									
									
									i = new Intent(
											PendingBuddyRequestActivity.this,
											Main2Activity.class);
									i.putExtra("message", getResources().getString(R.string.your_invitation_accepted) + ". " + getResources().getString(R.string.lets_play));
									startActivity(i);
									
									
									//displayInvitationStatusDialog(getResources().getString(R.string.your_invitation_accepted) + " " + getResources().getString(R.string.lets_play),1);
									
									/*Toast.makeText(
											PendingBuddyRequestActivity.this,
											getResources().getString(R.string.your_invitation_accepted) + " " + getResources().getString(R.string.lets_play),
											Toast.LENGTH_LONG).show();
									i = new Intent(
											PendingBuddyRequestActivity.this,
											Main2Activity.class);
									startActivity(i);*/
									// if resultCode is positive value, it's the
									// id of the friend
									// the user already has a buddy, in this
									// case the buddy's user id is returned
									break;
								}
							} else {
								// error log_tag
								Log.e(TAG, "an error has occurred");
								// Toast.makeText(Main2Activity.this,
								// R.string.error_general,
								// Toast.LENGTH_LONG).show();
							}
						}
					};
					checkGroupTask.execute();

				}
			};
			rptask.execute();

			checkProgressHandler.postDelayed(this, Constants.ALARM_INTERVAL);
		}
	};

	protected void onPause() {
		super.onPause();

		// need to stop the message poling loop
		Log.e(TAG, "*** STOPPING BUDDY POLLING");
		checkProgressHandler.removeCallbacks(checkForProgressTask);
	}

	protected void onResume() {
		super.onResume();
		checkProgressHandler.postDelayed(checkForProgressTask, 0);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pending_buddy_request);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		// Show the Up button in the action bar.
		// getActionBar().setDisplayHomeAsUpEnabled(true);
		Log.e(TAG,"Started PendingBuddyRequestActivity");
		Intent i = getIntent();
		String str = i.getStringExtra("buddyEmail");
		mTextView = (TextView) findViewById(R.id.textViewPendingBuddyEmail);

		if (str != null && !str.contains("not found")) {
			mTextView.setText(str);
			bdEM = str;
		} else {
			mTextView.setText(getResources().getString(R.string.main_loading));

			int uid = AppContext.getInstance().getUserId();
			RetrievePendingBuddyEMailTask task = new RetrievePendingBuddyEMailTask(
					uid) {

				@Override
				protected void onPostExecute(String email) 
				{
					if(!email.contains("not found in DB"))
					{
						mTextView.setText(email);
						Constants.INVITEE = email;
					}
				}
			};
			task.execute();

		}
		Constants.INVITATION_SENT = true;
		
		checkProgressHandler = new Handler();

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{
		if (keyCode == KeyEvent.KEYCODE_BACK) 
		{
			Intent i_settings = new Intent(PendingBuddyRequestActivity.this,SettingsActivity.class);
			startActivity(i_settings);
			//moveTaskToBack(true);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_pending_buddy_request, menu);
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
		case R.id.menu_authorize_fitbit:
			/*
			 * AppContext.getInstance().setUserCredentialsSet(false); Intent
			 * i_auth= new Intent(this, LoginActivity.class); //Intent i_auth=
			 * new Intent(this, AuthenticateFitbitActivity.class);
			 * startActivity(i_auth);
			 */
			Intent i_settings = new Intent(this, SettingsActivity.class);
			// Intent i_auth= new Intent(this,
			// AuthenticateFitbitActivity.class);
			startActivity(i_settings);
			return true;
	 	case R.id.menu_settings_logout:
			SharedPreferences prefs2 = getSharedPreferences(
					Constants.PROPERTIES_NAME, MODE_PRIVATE);
	 		return Utils.logout(this,prefs2);		
		}
		return super.onOptionsItemSelected(item);
	}

	public void onPopupOkButtonClicked(View v) {
		if (alertDialog.isShowing()) { // safe check
			alertDialog.dismiss();
		}
		
		Intent i_settings = new Intent(PendingBuddyRequestActivity.this,SettingsActivity.class);
		
		startActivity(i_settings);
	}

	public void onCancelInvitationButtonClicked(View v) {
		int uid = AppContext.getInstance().getUserId();

		// i.e., de-friend before the buddy id could ever be executed.
		DeFriendTask task = new DeFriendTask(AppContext.getInstance()
				.getUserId(), -1) {

			@Override
			protected void onPostExecute(Boolean success) {

				if (success) {
					Intent i_change_buddy = new Intent(
							PendingBuddyRequestActivity.this,
							SettingsActivity.class);
					Constants.INVITATION_CANCELED = true;
					Constants.INVITATION_SENT = false;
					if (Context.NOTIFICATION_SERVICE != null) {
						String ns = Context.NOTIFICATION_SERVICE;
						NotificationManager nMgr = (NotificationManager) getApplicationContext()
								.getSystemService(ns);
						nMgr.cancel(Constants.NOTIFICATION_ID_BUDDY_REQUEST_ACCEPTED);
						nMgr.cancel(Constants.NOTIFICATION_ID_NEW_MESSAGE);
					}
					String msg = getResources().getString(R.string.you_have_canceled_previous);
					// Toast.makeText(PendingBuddyRequestActivity.this, msg,
					// Toast.LENGTH_LONG).show();
					i_change_buddy.putExtra("message", msg);
					i_change_buddy.putExtra("removalSeen", 0);
					startActivity(i_change_buddy);
				} else {
					if (!Utils
							.isConnectionPresent(PendingBuddyRequestActivity.this)) {
						Toast.makeText(
								PendingBuddyRequestActivity.this,
								getResources().getString(R.string.connection_toast_message),
								Toast.LENGTH_LONG).show();
					}
				}
			}
		};
		task.execute();
	}

	public void onRemindButtonClicked(View v) {
		int uid = AppContext.getInstance().getUserId();
		String friendEmail = mTextView.getText().toString();
		RemindPendingBuddyTask task = new RemindPendingBuddyTask(uid,
				friendEmail) {

			@Override
			protected void onPostExecute(Boolean success) {
				Toast toast;
				if (success) {
					// toast= Toast.makeText(PendingBuddyRequestActivity.this,
					// "Reminder successfully sent", Toast.LENGTH_SHORT);
					// toast.show();
					LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					View layout = inflater.inflate(
							R.layout.activity_invitation_dialog, null);
					AlertDialog.Builder builder = new AlertDialog.Builder(
							PendingBuddyRequestActivity.this).setView(layout);
					alertDialog = builder.create();
					alertDialog.show();
				} else {
					toast = Toast.makeText(PendingBuddyRequestActivity.this,
							getResources().getString(R.string.connection_toast_message),
							Toast.LENGTH_SHORT);
					toast.show();
				}

			}
		};
		task.execute();
	}

	
	public void displayInvitationStatusDialog(String msg, final int infoMode) 
	{
			if (isInvitationStatusShowing) 
			{
				try
				{
					invitationStatusDialog.dismiss();
				}
				catch(Exception ex)
				{
					
				}
			}
			
			gim = infoMode;
			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View layout = inflater.inflate(R.layout.activity_buddychange_from_settings_dialog,
					null);
			AlertDialog.Builder builder = new AlertDialog.Builder(this)
					.setView(layout)
					.setOnCancelListener(
					new DialogInterface.OnCancelListener() {

						@Override
						public void onCancel(DialogInterface dialog) 
						{
							if(infoMode == 1)
							{
								// advance to the main activity.
								Intent i;
								i = new Intent(
										PendingBuddyRequestActivity.this,
										Main2Activity.class);
								startActivity(i);								
							}
							
							// checkProgressHandler.postDelayed(checkForProgressTask,
							// 0);
						}
					});
			
			invitationStatusDialog = builder.create();
			
			TextView tv = (TextView) layout.findViewById(R.id.alertinfo2_bc);
			ImageView iv = (ImageView) layout.findViewById(R.id.status_icon);
			
			TextView tv_h = (TextView) layout.findViewById(R.id.alertinfo_bc);
			tv_h.setText(getResources().getString(R.string.settings_activity_invitation_status));
			
			if(infoMode == -1)
			{
				iv.setBackgroundDrawable(getResources().getDrawable(R.drawable.bad_buddy_status));
			}
			else if(infoMode == 1)
			{
				iv.setBackgroundDrawable(getResources().getDrawable(R.drawable.good_buddy_status));
			}
			else
			{
				iv.setBackgroundDrawable(getResources().getDrawable(R.drawable.info_icon));
			}			
			
			tv.setText(msg);			
			
			invitationStatusDialog.show();
			isInvitationStatusShowing = true;
		

	}

	public void onBuddyChangeAcknowledged(View v)
	{
		if (isInvitationStatusShowing) 
		{
			
			if(gim < 1)
			{
				try
				{
					invitationStatusDialog.dismiss();
				}
				catch(Exception ex)
				{
					
				}
			}
			else
			{
				Intent i;
				i = new Intent(
						PendingBuddyRequestActivity.this,
						Main2Activity.class);
				startActivity(i);	
			}
			
			isInvitationStatusShowing = false;
		}		
	}
	
}
