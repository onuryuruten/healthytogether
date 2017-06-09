/**
 * 
 */
package ch.epfl.hci.healthytogether.service;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import ch.epfl.hci.happytogether.R;
import ch.epfl.hci.healthytogether.AcceptBuddyActivity;
import ch.epfl.hci.healthytogether.AppContext;
import ch.epfl.hci.healthytogether.Constants;
import ch.epfl.hci.healthytogether.HistoryActivity;
import ch.epfl.hci.healthytogether.LoginActivity;
import ch.epfl.hci.healthytogether.Main2Activity;
import ch.epfl.hci.healthytogether.MessagesActivity;
import ch.epfl.hci.healthytogether.SettingsActivity;
import ch.epfl.hci.healthytogether.communication.ServerHelper;
import ch.epfl.hci.healthytogether.communication.ServerHelper.CheckGroupTask;
import ch.epfl.hci.healthytogether.communication.ServerHelper.CheckMessagesTask;
import ch.epfl.hci.healthytogether.communication.ServerHelper.RetrieveBuddyEMailTask;
import ch.epfl.hci.healthytogether.communication.ServerHelper.RetrieveBuddyEMailTask2;
import ch.epfl.hci.healthytogether.communication.ServerHelper.RetrieveStepCountTask;
import ch.epfl.hci.healthytogether.communication.ServerHelper.SyncBackendWithFitbitTask;
import ch.epfl.hci.healthytogether.util.Utils;

import com.commonsware.cwac.wakeful.WakefulIntentService;

/**
 * Checks whether a buddy invitation or a message is available on the server. If
 * so displays a notification. The user can click on the notification to start
 * the app.
 * 
 * This class uses the {@link WakefulIntentService} as base class from
 * https://github.com/commonsguy/cwac-wakeful to work around certain sleep
 * timing problems with the phone (e.g. phone goes to sleep before the service
 * is finished).
 */
public class CheckForMessageService extends WakefulIntentService {

	private static final String TAG = CheckForMessageService.class.toString();

	public CheckForMessageService() {
		super("CheckForMessageService");
	}

	@Override
	protected void doWakefulWork(Intent arg0) {
		Log.i(TAG, "checking for notifications");

		int userId = AppContext.getInstance().getUserId();
		String email = AppContext.getInstance().getEmail();
		if (email == null) {
			Log.i(TAG, "email not specified, aborting");
			return;
		}

		if (!connectionPresent()) {
			Log.i(TAG, "network not available, aborting");
			return;
		}

		CheckGroupTask checkGroupTask = new CheckGroupTask(email, userId) {

			@Override
			protected void onPostExecute(String result) {
				
				if(!this.safeExecution)
				{
				/*	if(!Utils.isConnectionPresent(SettingsActivity.this))
					{
						displayConnectionErrorDialog();
					}*/
					return;
				}	
				
				if (Utils.isInteger(result)) {
					int resultCode = Integer.parseInt(result);
					if(!AppContext.getInstance().isUserCredentialsSet())
					{
						
					}
					else if (resultCode == ServerHelper.CheckGroupTask.RESPONSE_INCOMING_REQUEST_PENDING) {
						// there's a pending buddy request: Another user asked
						// to play with this user
						Log.i(TAG,
								"Incoming buddy request detected, showing notification...");
						
						handleIncomingBuddyRequest();
					} else if (resultCode > 0) {
						// if resultCode is positive value, it's the id of the
						// friend
						// We need to check whether this means that the friend
						// has accepted the invite and show a
						SharedPreferences prefs = getSharedPreferences(
								Constants.PROPERTIES_NAME, MODE_PRIVATE);
						boolean gameAlreadyStarted = prefs.getBoolean(
								Constants.PROP_KEY_GAME_STARTED, false);
						if (gameAlreadyStarted) {
							// the user already has a buddy and an ongoing game,
							// in this case we check if there's an incoming
							// message
							handleCheckForMessage();
						} else {
							// the other user has just accept the friend
							// request. We update the properties and show a
							// notification
							handleBuddyRequestAccepted(prefs);
						}

						//logNotificationRoutine();

					}
				}
			}

			private void logNotificationRoutine() {
				if (Constants.newLogin) {
					String logStr = "";
					if (AppContext.getInstance().isUserCredentialsSet()
							&& AppContext.getInstance().getUserString() != null) {
						logStr = ""
								+ AppContext.getInstance().getUserString()
								+ getResources().getString(R.string.you_should_log);//", please log your mood, social info and activity";
					} else {
						logStr = getResources().getString(R.string.whats_up);
					}

					notifyForLog(logStr);
					Constants.newLogin = false;
				}

				Calendar c = Calendar.getInstance();

				int day = c.get(Calendar.DAY_OF_MONTH);
				int hour = c.get(Calendar.HOUR_OF_DAY);
				int minutes = c.get(Calendar.MINUTE);
				long distanceToAlarm = 0;
				// long timestamp = c.getTimeInMillis();

				Log.d(TAG, "***********Now is " + hour + "hours " + minutes
						+ " minutes");

				boolean closeToAlarm = false;

				for (int i = 0; i < Constants.notificationHours.length; i++) {

					distanceToAlarm = 3600000
							* (hour - Constants.notificationHours[i]) + 60000
							* (minutes - Constants.notificationMinutes[i]);

					Log.d(TAG, "Next alert: " + Constants.notificationHours[i]
							+ " hours " + Constants.notificationMinutes[i]
							+ " minutes");
					Log.d(TAG, "distance to alarm: " + distanceToAlarm);

					closeToAlarm = (distanceToAlarm < 0) // the time is earlier
															// than this
															// notification,
							&& (Math.abs(distanceToAlarm) < Constants.LOG_ALARM_INTERVAL); // but
																							// not
																							// that
																							// far.

					if (closeToAlarm) {
						if (Constants.validAlerts[i]
								&& !Constants.alertSetOff[i]) {
							Log.d(TAG, "Alert for "
									+ Constants.notificationHours[i] + "hrs "
									+ Constants.notificationMinutes[i]
									+ " mins is set off.");
							String notificationString = getResources().getString(R.string.it_is_around)
									+ Constants.notificationHours[i];
							if (Constants.notificationMinutes[i] > 0) {
								notificationString += "h"
										+ Constants.notificationMinutes[i];
							}
							notificationString += getResources().getString(R.string.you_should_log);
							notifyForLog(notificationString);
							Constants.alertGiven = true;
							Constants.alertSetOff[i] = true;
							break;
						} else {
							/*
							 * The alert was held off for some reason (e.g.
							 * logging within a close time) The user will be
							 * alerted the next day, the same time.
							 */
							Constants.validAlerts[i] = true;
						}
					} else {
						Constants.alertSetOff[i] = false;
					}

				}

			}

		};
		Log.i(TAG, "checking group status...");
		checkGroupTask.execute();
	}

	/**
	 * Triggers the synch.php script to synchronize the backend with the latest
	 * fitbit data. The main purpose of this is to keep the PHP session alive
	 * where the fitbit oauth token is stored. We don't do anything here if the
	 * synchronization fails.
	 */
	protected void syncBackendWithFitbit() {
		int uid = AppContext.getInstance().getUserId();
		int buddyid = AppContext.getInstance().getFriendId();

		SyncBackendWithFitbitTask task = new SyncBackendWithFitbitTask(uid,
				buddyid) {

			@Override
			protected void onPostExecute(Boolean success) {
				Log.d(TAG, "fitbit2backend sync" + (success ? " " : " not")
						+ " done");
			}
		};
		task.execute();
	}

	/**
	 * The buddy request sent by this user has been accepted. Show a
	 * notification and when the user clicks on the notification go to the main
	 * activity.
	 */
	private void handleBuddyRequestAccepted(SharedPreferences prefs) {
		Log.d(TAG, "Buddy invitation has been accepted by other user ");
		// update the properties
		Editor editor = prefs.edit();
		editor.putBoolean(Constants.PROP_KEY_GAME_STARTED, true);
		editor.commit();

		// show the notification
		NotificationCompat.Builder builder = new NotificationCompat.Builder(
				this).setAutoCancel(true).setDefaults(Notification.DEFAULT_ALL)
				.setOnlyAlertOnce(true)
				.setSmallIcon(R.drawable.notification_icon)
				.setContentTitle(getResources().getString(R.string.lets_play))
				.setContentText(getResources().getString(R.string.your_invitation_accepted));
		Constants.INVITATION_ACCEPTED = true;
		Intent resultIntent = new Intent(this, Main2Activity.class);
		resultIntent.putExtra("message",
				getResources().getString(R.string.your_invitation_accepted) + ". " + getResources().getString(R.string.lets_play));

		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		stackBuilder.addParentStack(Main2Activity.class);
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
				PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentIntent(resultPendingIntent);
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		// the notification id allows updating the notification later on.
		mNotificationManager.notify(
				Constants.NOTIFICATION_ID_BUDDY_REQUEST_ACCEPTED,
				builder.build());
	}

	/*
	 * 
	 * 
	 * 
	 * 
	 * */
	private void notifyForLog(String notificationString) {
		// TODO Auto-generated method stub
		NotificationCompat.Builder builder;

		String contentTitle = getResources().getString(R.string.log_request_title);

		if (AppContext.getInstance().isUserCredentialsSet()
				&& AppContext.getInstance().getUserString() != null) {
			contentTitle = getResources().getString(R.string.log_time_begin)
					+ AppContext.getInstance().getUserString() + "!";
		}

		/*
		 * String logStr = "";
		 * if(AppContext.getInstance().isUserCredentialsSet()) { logStr =
		 * "What's up, " + AppContext.getInstance().getUserString() + "?"; }
		 * else { logStr = "What have you been up to?"; }
		 * 
		 * notifyForLog(logStr);
		 */

		builder = new NotificationCompat.Builder(CheckForMessageService.this)
				// CheckForMessageService.this)
				.setAutoCancel(true).setDefaults(Notification.DEFAULT_ALL)
				.setOnlyAlertOnce(true)
				.setSmallIcon(R.drawable.notification_icon)
				.setContentTitle(contentTitle)
				.setContentText(notificationString);

		Intent resultIntent = new Intent(CheckForMessageService.this,
				Main2Activity.class);
		TaskStackBuilder stackBuilder = TaskStackBuilder
				.create(CheckForMessageService.this);
		stackBuilder.addParentStack(MessagesActivity.class);
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
				PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentIntent(resultPendingIntent);
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		// the notification id allows updating the notification later on.
		mNotificationManager.notify(Constants.NOTIFICATION_ID_NEW_MESSAGE,
				builder.build());
	}

	private void handleCheckForMessage() {
		Log.d(TAG, "Game has already started, checking for messages...");
		CheckMessagesTask task = new CheckMessagesTask(AppContext.getInstance()
				.getUserId()) {

			@Override
			protected void onPostExecute(String msg) {
				
				if(!this.safeExecution){
					Log.e(TAG, "No new messages received");
				}
				else if (msg == null || msg.trim().length() == 0) {
					Log.d(TAG, "No new messages received");
				} else {

					Log.d(TAG, "Received message: " + msg);
					// show the notification
					NotificationCompat.Builder builder;
					Intent resultIntent;
					boolean isMessage;

					if (!msg.contains("LOG")) {
						isMessage = true;
						builder = new NotificationCompat.Builder(
								CheckForMessageService.this)
								.setAutoCancel(true)
								.setDefaults(Notification.DEFAULT_ALL)
								.setOnlyAlertOnce(true)
								.setSmallIcon(R.drawable.notification_icon)
								.setContentTitle(getResources().getString(R.string.new_message_title))
								.setContentText(
										getResources().getString(R.string.new_message_content));
					} else {
						isMessage = false;

						builder = null;

						/*
						 * builder = new
						 * NotificationCompat.Builder(CheckForMessageService
						 * .this) .setAutoCancel(true)
						 * .setDefaults(Notification.DEFAULT_ALL)
						 * .setOnlyAlertOnce(true)
						 * .setSmallIcon(R.drawable.notification_icon)
						 * .setContentTitle("Log received") .setContentText(
						 * "Your log has been stored in the database.");
						 */
					}

					if (isMessage) {
						resultIntent = new Intent(CheckForMessageService.this,
								MessagesActivity.class);
					} else {
						resultIntent = new Intent(CheckForMessageService.this,
								HistoryActivity.class);
					}

					// TODO: implement the same thing for daily
					// notifications...
					TaskStackBuilder stackBuilder = TaskStackBuilder
							.create(CheckForMessageService.this);
					stackBuilder.addParentStack(MessagesActivity.class);
					stackBuilder.addNextIntent(resultIntent);
					PendingIntent resultPendingIntent = stackBuilder
							.getPendingIntent(0,
									PendingIntent.FLAG_UPDATE_CURRENT);

					if (builder != null) {
						builder.setContentIntent(resultPendingIntent);
						NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
						// the notification id allows updating the notification
						// later on.
						mNotificationManager.notify(
								Constants.NOTIFICATION_ID_NEW_MESSAGE,
								builder.build());
					}
				}

			}
		};
		task.execute();
	}

	protected void handleIncomingBuddyRequest() {
		Log.d(TAG, "Incoming buddy invitation detected");
		String email = AppContext.getInstance().getEmail();
		RetrieveBuddyEMailTask2 t2 = new RetrieveBuddyEMailTask2(AppContext
				.getInstance().getEmail()) {

			@Override
			protected void onPostExecute(String result) {
				
				if(!this.safeExecution)
				{
					return;
				}
				// TODO Auto-generated method stub
				final ArrayList<Boolean> hasFriend = new ArrayList<Boolean>();
				final String myResult = result;
				if (result.contains("not found")) {
					Log.i(TAG, "The user does not have any buddy.");

					// Toast.makeText(AcceptBuddyActivity.this,
					// "You do not have any invitations remaining",
					// Toast.LENGTH_LONG).show();
				} else {
					hasFriend.add(true);
				}
				/*
				 * Intent resultIntent = new Intent(Main2Activity.this,
				 * AcceptBuddyActivity.class);
				 * resultIntent.putExtra("RequestAccepted", -1);
				 * startActivity(resultIntent);
				 */
				RetrieveBuddyEMailTask task = new RetrieveBuddyEMailTask(
						AppContext.getInstance().getEmail()) {

					
					@Override
					protected void onPostExecute(String buddyEmail) {
						
						if(!this.safeExecution)
						{
							return;
						}
						if (buddyEmail.contains("@@")) {
							if (hasFriend.isEmpty()) {
								// Perform multiple-buddy notification.
								showMultiplePendingBuddyInvitationNotification();
							} else {
								// Form a list of invitations
								String stringBody = buddyEmail;
								String notifierEmail = "";
								int delim = stringBody.indexOf("@@");
								ArrayList<String> buddyEmails = new ArrayList<String>();
								while (delim > 0) {
									notifierEmail = stringBody.substring(0,
											delim);
									if (notifierEmail.equals(myResult)) {
										// continue;
									} else {
										buddyEmails.add(notifierEmail);
										Log.i(TAG,
												"RetrieveBuddyEMailTask: We added the email: "
														+ notifierEmail);
									}
									stringBody = stringBody
											.substring(delim + 2);
									delim = stringBody.indexOf("@@");
								}

								if (stringBody.length() > 0) {
									if (stringBody.equals(myResult)) {
										// continue;
										Log.i(TAG,
												"RetrieveBuddyEMailTask: We did not add the email: "
														+ stringBody);
									} else {
										buddyEmails.add(stringBody);
										Log.i(TAG,
												"RetrieveBuddyEMailTask: We added the email: "
														+ stringBody);
									}
								}

								if (buddyEmails.size() == 1) {
									showPendingBuddyInvitationNotification(buddyEmails
											.get(0));
								} else {
									showMultiplePendingBuddyInvitationNotification();
								}
							}
						} else {
							showPendingBuddyInvitationNotification(buddyEmail);
						}
					}
				};
				task.execute();
			}
		};
		t2.execute();
		/*
		 * RetrieveBuddyEMailTask task= new RetrieveBuddyEMailTask(email) {
		 * 
		 * @Override protected void onPostExecute(String buddyEmail) {
		 * if(buddyEmail.contains("@@")) { // Perform multiple notifications.
		 * showMultiplePendingBuddyInvitationNotification(); } else {
		 * showPendingBuddyInvitationNotification(buddyEmail); } } };
		 * task.execute();
		 */
	}

	public boolean connectionPresent() {
		ConnectivityManager cMgr = (ConnectivityManager) this
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cMgr.getActiveNetworkInfo();
		if ((netInfo != null) && (netInfo.getState() != null)) {
			return netInfo.getState().equals(State.CONNECTED);
		}
		return false;
	}

	private void showMultiplePendingBuddyInvitationNotification() {

		SharedPreferences prefs = getSharedPreferences(
				Constants.PROPERTIES_NAME, MODE_PRIVATE);		
		
		boolean allowNotification = Constants.checkNotificationAllowance(prefs);
		
		if(allowNotification)
		{

			Constants.embarkNotificationDate(prefs);	
			
		NotificationCompat.Builder builder = new NotificationCompat.Builder(
				this)
				.setAutoCancel(true)
				// .setLargeIcon(BitmapFactory.decodeResource(getResources(),
				// R.drawable.notification_icon))
				.setSmallIcon(R.drawable.notification_icon)
				.setContentTitle(getResources().getString(R.string.multiple_invitations_title))
				.setDefaults(Notification.DEFAULT_ALL)
				.setContentText(
						getResources().getString(R.string.multiple_invitations_content));
		// Creates an explicit intent for an Activity in your app
		Constants.INVITATION_RECEIVED = true;
		Intent resultIntent = new Intent(this, AcceptBuddyActivity.class);

		// The stack builder object will contain an artificial back stack for
		// the
		// started Activity.
		// This ensures that navigating backward from the Activity leads out of
		// your application to the Home screen.
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		// Adds the back stack for the Intent (but not the Intent itself)
		stackBuilder.addParentStack(AcceptBuddyActivity.class);
		// Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
				PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentIntent(resultPendingIntent);
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		// the notification id allows updating the notification later on.
		mNotificationManager.notify(
				Constants.NOTIFICATION_ID_PENDING_BUDDY_REQUEST,
				builder.build());
		}

	}

	private void showPendingBuddyInvitationNotification(String buddyEmail) {
		if (buddyEmail.contains("not found")) {
			return;
		}

		SharedPreferences prefs = getSharedPreferences(
				Constants.PROPERTIES_NAME, MODE_PRIVATE);
		
		boolean allowNotification = Constants.checkNotificationAllowance(prefs);
		
		if(allowNotification)
		{

			Constants.embarkNotificationDate(prefs);	
		
			NotificationCompat.Builder builder = new NotificationCompat.Builder(
					this)
					.setAutoCancel(true)
					// .setLargeIcon(BitmapFactory.decodeResource(getResources(),
					// R.drawable.notification_icon))
					.setSmallIcon(R.drawable.notification_icon)
					.setContentTitle(getResources().getString(R.string.lets_play))
					.setDefaults(Notification.DEFAULT_ALL)
					.setContentText(buddyEmail + getResources().getString(R.string.someone_is_waiting));
			// Creates an explicit intent for an Activity in your app
			Constants.INVITATION_RECEIVED = true;
			Intent resultIntent = new Intent(this, AcceptBuddyActivity.class);
			resultIntent.putExtra("RequestAccepted", -1);
			resultIntent.putExtra("femail", buddyEmail);
	
			// The stack builder object will contain an artificial back stack for
			// the
			// started Activity.
			// This ensures that navigating backward from the Activity leads out of
			// your application to the Home screen.
			TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
			// Adds the back stack for the Intent (but not the Intent itself)
			stackBuilder.addParentStack(AcceptBuddyActivity.class);
			// Adds the Intent that starts the Activity to the top of the stack
			stackBuilder.addNextIntent(resultIntent);
			PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
					PendingIntent.FLAG_UPDATE_CURRENT);
			builder.setContentIntent(resultPendingIntent);
			NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			// the notification id allows updating the notification later on.
			mNotificationManager.notify(
					Constants.NOTIFICATION_ID_PENDING_BUDDY_REQUEST,
					builder.build());
		}
	}

	private void showBuddyInvitationAcceptedNotification(String buddyEmail) {

	}
}
