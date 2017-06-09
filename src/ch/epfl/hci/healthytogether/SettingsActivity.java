package ch.epfl.hci.healthytogether;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import ch.epfl.hci.happytogether.R;
import ch.epfl.hci.healthytogether.communication.ServerHelper.CheckFitbitAuthenticationTask;
import ch.epfl.hci.healthytogether.communication.ServerHelper.CheckGroupTask;
import ch.epfl.hci.healthytogether.communication.ServerHelper.DeFriendTask;
import ch.epfl.hci.healthytogether.communication.ServerHelper.RetrieveBuddyEMailTask;
import ch.epfl.hci.healthytogether.communication.ServerHelper.RetrieveBuddyEMailTask2;
import ch.epfl.hci.healthytogether.communication.ServerHelper.RetrieveBuddyNameTask;
import ch.epfl.hci.healthytogether.communication.ServerHelper.RetrieveExistingBuddyInfoTask;
import ch.epfl.hci.healthytogether.communication.ServerHelper.RetrieveStepCountTask;
import ch.epfl.hci.healthytogether.util.Utils;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RelativeLayout.LayoutParams;
import android.support.v4.app.NavUtils;

public class SettingsActivity extends Activity {

	public static final String TAG = SettingsActivity.class.getSimpleName();

	private boolean isDisplayingConnectionErrorDialog = false;
	private boolean isInvitationStatusShowing = false;
	private AlertDialog invitationStatusDialog;
	// override default behaviour of the browser
	// private class MyWebViewClient extends WebViewClient {
	// @Override
	// public boolean shouldOverrideUrlLoading(WebView view, String url) {
	// view.loadUrl(url);
	// return true;
	// }
	// }

	private boolean authorizationDone = false;
	private boolean isActive = false;
	private boolean isPaired = false;
	private boolean messageFromMainMenu = false;
	private int checkGroupTaskResult = 0;
	private int incomingInvitationsCount = 0;
	private ArrayList<String> buddyEmails = new ArrayList<String>();
	private AlertDialog alertDialog;
	private String buddyName;

	private ProgressDialog initialSyncDialog;
	private AlertDialog connectionDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		isActive = true;
		setContentView(R.layout.activity_settings);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		if (Utils.isConnectionPresent(SettingsActivity.this)) {
			// Log.d(TAG,"showProgressDialog called from onCreate()");
			displayConnectionErrorDialog(false);
			settingsScreenSetup();
		} else {
			displayConnectionErrorDialog();
		}

		// settingsScreenSetup();

		Intent i = getIntent();
		String msg = i.getStringExtra("message");
		int val = i.getIntExtra("removalSeen", 0);

		messageFromMainMenu = (val == -99);

		if (msg != null && !messageFromMainMenu) 
		{
			displayInvitationStatusDialog(msg,(int)(Math.signum(val)));
		}

		messageFromMainMenu = false;
	}

	
	/*
	 * msg = as retrieved from other activities or from CheckGroupTask result here.
	 * infoMode:
	 * -1 = bad news (rejected/de-friended)
	 * 0  = neutral (received/sent the invitation)
	 * 1  = good news (buddy accepted invitation!!)
	 * 
	 * */
	
	public void displayInvitationStatusDialog(String msg, int infoMode) {
		if (!isDisplayingConnectionErrorDialog) {
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

			/*AlertDialog.Builder builder = new AlertDialog.Builder(this);

			builder.setTitle(getResources().getString(R.string.settings_activity_invitation_status));//"Invitation Status");
			builder.setMessage(msg);
			builder.setPositiveButton(getResources().getString(R.string.ok_button_string), new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
				
					isInvitationStatusShowing = false;
				}
			}).setOnCancelListener(new OnCancelListener() {

				@Override
				public void onCancel(DialogInterface dialog) {
					isInvitationStatusShowing = false;
				}
			});*/
			
			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View layout = inflater.inflate(R.layout.activity_buddychange_from_settings_dialog,
					null);
			AlertDialog.Builder builder = new AlertDialog.Builder(this)
					.setView(layout)
					.setOnCancelListener(
					new DialogInterface.OnCancelListener() {

						@Override
						public void onCancel(DialogInterface dialog) {
							moveTaskToBack(true);
							
							// checkProgressHandler.postDelayed(checkForProgressTask,
							// 0);
						}
					});
			
			invitationStatusDialog = builder.create();
			
			TextView tv_h = (TextView) layout.findViewById(R.id.alertinfo_bc);
			tv_h.setText(getResources().getString(R.string.settings_activity_invitation_status));
			
			TextView tv = (TextView) layout.findViewById(R.id.alertinfo2_bc);
			
			ImageView iv = (ImageView) layout.findViewById(R.id.status_icon);
			
			if(infoMode == -1)
			{
				iv.setBackgroundDrawable(getResources().getDrawable(R.drawable.bad_buddy_status));
				SharedPreferences prefs2 = getSharedPreferences(
						Constants.PROPERTIES_NAME, MODE_PRIVATE);
				Constants.INVITATION_SENT = false;
				Constants.INVITATION_RECEIVED = false;
				Editor editor = prefs2.edit();
				editor.putBoolean(Constants.PROP_KEY_GAME_STARTED, false);
				editor.commit();				
			}
			else if(infoMode == 1)
			{
				iv.setBackgroundDrawable(getResources().getDrawable(R.drawable.good_buddy_status));
				SharedPreferences prefs2 = getSharedPreferences(
						Constants.PROPERTIES_NAME, MODE_PRIVATE);
				Editor editor = prefs2.edit();
				editor.putBoolean(Constants.PROP_KEY_GAME_STARTED, true);
				editor.commit();
				
				isPaired = true;
				Button b = (Button)layout.findViewById(R.id.okbtn_bc);
				b.setText(getResources().getString(R.string.lets_play));
				b.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) 
					{
						if(invitationStatusDialog != null && invitationStatusDialog.isShowing())
						{
							try
							{
								invitationStatusDialog.dismiss();
							}
							catch(Exception e)
							{
								invitationStatusDialog = null;
							}
							isInvitationStatusShowing = false;
						}
						attemptContinue(false);
					}
				});
				
				
				
			}
			else
			{
				iv.setBackgroundDrawable(getResources().getDrawable(R.drawable.info_icon));
			}			
			
			tv.setText(msg);			
			
			try
			{
				invitationStatusDialog.show();
				isInvitationStatusShowing = true;
			}
			catch(Exception e)
			{
				invitationStatusDialog = null;
			}
		}

	}

	public void onBuddyChangeAcknowledged(View v)
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
			isInvitationStatusShowing = false;
			
		}		
	}
	
	private void settingsScreenSetup() {
		showProgressDialog(false);
		isPaired = false;
		incomingInvitationsCount = 0;
		SharedPreferences prefs = getSharedPreferences(
				Constants.PROPERTIES_NAME, MODE_PRIVATE);

		// Step 0: Let's make sure that this user is addressed correctly!
		setName(AppContext.getInstance().getUserId(), true);

		// Step 1: check if the user has completed fitbit authentication.
		CheckFitbitAuthenticationTask authTask = new CheckFitbitAuthenticationTask(
				AppContext.getInstance().getUserId()) {

			protected void onPreExecute() {
				showProgressDialog(true);
				super.onPreExecute();
			}

			@Override
			protected void onPostExecute(Boolean success) {
				// showProgressDialog(false);
				if (!this.safeExecution || !Utils.isConnectionPresent(SettingsActivity.this)) {
					displayConnectionErrorDialog();
				}
				else if(!success && this.outcome.equalsIgnoreCase("error"))
				{
					displayConnectionErrorDialog();
				}


				TextView tv = (TextView) findViewById(R.id.fbCurrentStatus);

				if (success) {
					tv.setText(getResources().getString(R.string.settings_fitbit_authenticated));
					
					hideAuthenticationUI(); 
					hideInvitationUI(false);
				} else {
					tv.setText(getResources().getString(R.string.settings_fitbit_not_authenticated));
					hideInvitationUI(true);
					hideContinueHTUI(true);
					showProgressDialog(false);
					return;
				}

				tv.invalidate();

				// Step 2: check the user's social status..
				Log.d(TAG, "Friend id before checkgrouptask is: "
						+ AppContext.getInstance().getFriendId());
				countIncomingInvitations(); // Let us make sure that this code
											// is good.

				CheckGroupTask socialTask = new CheckGroupTask(AppContext
						.getInstance().getEmail(), AppContext.getInstance()
						.getUserId()) {
					protected void onPreExecute() {
						// showProgressDialog(true);
						super.onPreExecute();
					}

					@Override
					protected void onPostExecute(String result) 
					{

						if (!Utils.isConnectionPresent(SettingsActivity.this)) {
							showProgressDialog(false);
							displayConnectionErrorDialog();
						}
						
						if(!this.safeExecution)
						{
							if(!Utils.isConnectionPresent(SettingsActivity.this))
							{
								displayConnectionErrorDialog();
							}
							return;
						}						
						
						setName(AppContext.getInstance().getFriendId(), false);

						Button outgoingButton;
						TextView socialTV = (TextView) findViewById(R.id.socialCurrentStatus);
						String msg = "";
						// Log.d(TAG, "response: " + result);
						if (Utils.isInteger(result)) {
							int resultCode = Integer.parseInt(result);
							checkGroupTaskResult = resultCode;
							// Intent i;
							switch (resultCode) {
							case RESPONSE_CODE_NO_BUDDY:
								// Addition: so avoid the remnants
								SharedPreferences prefs2 = getSharedPreferences(
										Constants.PROPERTIES_NAME, MODE_PRIVATE);
								Constants.internalReset(SettingsActivity.this,prefs2);
								Constants.externalReset();
								Log.d(TAG,
										"Friend id after checkgrouptask is: "
												+ AppContext.getInstance()
														.getFriendId());
								
								/*SharedPreferences prefs2 = getSharedPreferences(
										Constants.PROPERTIES_NAME, MODE_PRIVATE);*/
								boolean previouslyPlaying = prefs2.getBoolean(Constants.PROP_KEY_GAME_STARTED, false);
								
								if(previouslyPlaying)
								{
									Constants.INVITATION_RECEIVED = false;
									Constants.INVITATION_OUTCOME_DISPLAYED = true;
									socialTV.setText(getResources().getString(R.string.buddy_stopped_playing));
									socialTV.invalidate();
									displayInvitationStatusDialog(getResources().getString(R.string.buddy_stopped_playing),-1);
								}
								else if (Constants.INVITATION_SENT) {
									Constants.INVITATION_SENT = false;
									Constants.INVITATION_OUTCOME_DISPLAYED = true;
									String msg2 = getResources().getString(R.string.invitation_rejected);
									msg2 = msg2.replace("()", Constants.INVITEE);
									
									socialTV.setText(msg2);//getResources().getString(R.string.invitation_rejected));
									socialTV.invalidate();
									displayInvitationStatusDialog(msg2,-1);
								} else if (Constants.INVITATION_RECEIVED) {
									Constants.INVITATION_RECEIVED = false;
									Constants.INVITATION_OUTCOME_DISPLAYED = true;
									socialTV.setText(getResources().getString(R.string.buddy_stopped_playing));
									socialTV.invalidate();
									displayInvitationStatusDialog(getResources().getString(R.string.buddy_stopped_playing),-1);
								} else {
									socialTV.setText(getResources().getString(R.string.settings_no_buddy_warning));
									socialTV.invalidate();
								}

								outgoingButton = (Button) findViewById(R.id.outgoingInvitations);
								outgoingButton.setText(getResources().getString(R.string.send_invitation));
								outgoingButton.invalidate();

								break;
							case RESPONSE_INCOMING_REQUEST_PENDING:
								// Question this: (a) the user has also sent an
								// invitation
								// (b) the user is already playing

								String finalMsg = "";

								if (isPaired) {
									// setName(AppContext.getInstance().getFriendId(),false);
									/*String intermediate = "with " + buddyName;
									
									if(buddyName == null)
									{
										intermediate = "the game";
									}*/
									
									finalMsg = getResources().getString(R.string.invited_while_playing);
									
								/*	finalMsg = "You are currently playing "
											+ intermediate
											+ ", but you also have some other invitations. Check if you would like to accept them.";*/
									displayInvitationStatusDialog(finalMsg,0);

									
									setName(AppContext.getInstance()
											.getFriendId(),
											false,
											getResources().getString(R.string.but_you_also_have));

								} else if (Constants.INVITATION_SENT) {
									finalMsg = getResources().getString(R.string.received_invitation_while_waiting);

									displayInvitationStatusDialog(finalMsg,0);
									socialTV.setText(finalMsg);
									socialTV.invalidate();

									outgoingButton = (Button) findViewById(R.id.outgoingInvitations);
									outgoingButton
											.setText(getResources().getString(R.string.outgoing_invitation));
									outgoingButton.invalidate();
								} else {
									finalMsg = getResources().getString(R.string.received_invitation);
									
									displayInvitationStatusDialog(finalMsg,0);
									socialTV.setText(finalMsg);
									socialTV.invalidate();
								}

								break;
							case RESPONSE_WAITING_FOR_ACCEPT:
								// Question this: (a) the user has also received
								// an invitation.
								socialTV.setText(getResources().getString(R.string.invitation_pending));
								socialTV.invalidate();

								outgoingButton = (Button) findViewById(R.id.outgoingInvitations);
								outgoingButton.setText(getResources().getString(R.string.outgoing_invitation));
								outgoingButton.invalidate();

								break;
							default:
								isPaired = true;
								
								if(!Constants.INVITATION_OUTCOME_DISPLAYED)
								{
									Constants.INVITATION_OUTCOME_DISPLAYED = true;
									displayInvitationStatusDialog(getResources().getString(R.string.your_invitation_accepted) + ". " + getResources().getString(R.string.lets_play), 1);
								}
								
								setName(resultCode, false);
								break;
								
							}
							
							//if(!isPaired)
							//{
							hideContinueHTUI(!isPaired);
							//}
							showProgressDialog(false);
						} else {
							// error log_tag
							Log.e(TAG,
									"an error has occurred. The result from checkgrouptask: "
											+ result);
						}

					}
				};

				socialTask.execute();
			}
		};
		authTask.execute();
		/*
		 * try { authTask.get(); // socialTask.get(); } catch
		 * (InterruptedException e) {
		 * e.printStackTrace(); } catch (ExecutionException e) {
		 * Auto-generated catch block e.printStackTrace(); }
		 * 
		 * showProgressDialog(false);
		 */

	}

	protected void onDestroy() {
		super.onDestroy();
		isActive = false;
		isDisplayingConnectionErrorDialog = false;
		isPaired = false;
		incomingInvitationsCount = 0;
	}

	protected void onPause() {
		super.onPause();
		isActive = false;
		isDisplayingConnectionErrorDialog = false;
		isPaired = false;
		incomingInvitationsCount = 0;
	}

	@Override
	protected void onResume() {
		super.onResume();
		isActive = true;

		if (Utils.isConnectionPresent(SettingsActivity.this)) {
			// Log.d(TAG,"showProgressDialog called from onCreate()");
			displayConnectionErrorDialog(false);
			settingsScreenSetup();
		} else {
			displayConnectionErrorDialog();
		}

		/*
		 * if (mAuthorizationStarted) { // the user came back from the fitbit
		 * website and has (hopefully) performed the authorization
		 * onAuthorizeCompleteButtonClicked(null); }
		 */
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			//moveTaskToBack(true);
			//Button btn = (Button) findViewById(R.id.continueHTButton);
			//btn.performClick();
			attemptContinue(true);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	 @Override 
	 public boolean onCreateOptionsMenu(Menu menu) 
	 { // Inflate the menu; this adds items to the action bar if it is present.$
		 getMenuInflater().inflate(R.menu.activity_settings, menu);
		 return true; 
	 }	
	

	 @Override public boolean onOptionsItemSelected(MenuItem item) 
	 { 
		 switch (item.getItemId()) 
		 { 
		 	case android.R.id.home: 
		 		NavUtils.navigateUpFromSameTask(this);
		 		return true;
		 	case R.id.menu_settings_logout:
				SharedPreferences prefs2 = getSharedPreferences(
						Constants.PROPERTIES_NAME, MODE_PRIVATE);
		 		return Utils.logout(this,prefs2);
		 }
		 return super.onOptionsItemSelected(item);
	 }


	protected void setName(final int id, final boolean isCurrentUser,
			final String extraMsg) {
		
		if(!isCurrentUser)
		{
			retrieveBuddyName(extraMsg);
			return;
		}
		
		Constants.BUDDY_NAME_ACQUIRED = false;
		RetrieveBuddyNameTask task = new RetrieveBuddyNameTask(id) 
		{

			@Override
			protected void onPostExecute(String username) 
			{
				if(!this.safeExecution)
				{
					if(!Utils.isConnectionPresent(SettingsActivity.this))
						displayConnectionErrorDialog();
					return;
				}
				
				if (username != null && Constants.BUDDY_NAME_ACQUIRED) 
				{
					if (!username.startsWith("Error")
							&& !username.contains("error")
							&& !username.contains("Error")) {

						if (isCurrentUser) {
							//TextView selfTV = (TextView) findViewById(R.id.userNameInSettings);
							AppContext.getInstance().setUserString(username);
							
							if(!Constants.FIRST_WELCOME_GIVEN)
							{
								Constants.FIRST_WELCOME_GIVEN = true;
					//			selfTV.setText(getResources().getString(R.string.welcome) + " " + username + "!");
					//			selfTV.invalidate();
							}
							else
							{
					//			selfTV.setText(getResources().getString(R.string.settings_alternate_title) ); // + " " + username + "!"
					//			selfTV.invalidate();								
							}
						} else {
							buddyName = username;
							Constants.buddyName = buddyName;
							Constants.BUDDY_NAME_ACQUIRED = true;
							AppContext.getInstance().setFriendId(id);

							TextView socialTV = (TextView) findViewById(R.id.socialCurrentStatus);
							socialTV.setText(getResources().getString(R.string.currently_with)
									+ " " + username + extraMsg);
							socialTV.invalidate();

							Button outgoingButton = (Button) findViewById(R.id.outgoingInvitations);
							outgoingButton.setText(getResources().getString(R.string.leave_buddy));
							outgoingButton.invalidate();
						}

					}
				}
				else
				{
					String displayMsg = getResources().getString(R.string.invited_while_playing);
					TextView socialTV = (TextView) findViewById(R.id.socialCurrentStatus);
					socialTV.setText(displayMsg);
					socialTV.invalidate();

					Button outgoingButton = (Button) findViewById(R.id.outgoingInvitations);
					outgoingButton.setText(getResources().getString(R.string.invite_someone_else));
					outgoingButton.invalidate();
				}
			}
		};
		task.execute();

		/*
		 * try { task.get(); } catch (InterruptedException e) { 
		 * Auto-generated catch block e.printStackTrace(); } catch
		 * (ExecutionException e) {
		 * e.printStackTrace(); }
		 */

	}

	protected void retrieveBuddyName(final String extraMsg)
	{
		
		RetrieveExistingBuddyInfoTask task = new RetrieveExistingBuddyInfoTask(AppContext.getInstance().getUserId())
		{
			protected void onPostExecute(Boolean result)
			{
				
				if(result)
				{
					buddyName = this.retrievedBuddyName;
					Constants.buddyName = this.retrievedBuddyName;
					AppContext.getInstance().setFriendId(this.retrievedBuddyId);
					TextView socialTV = (TextView) findViewById(R.id.socialCurrentStatus);
					socialTV.setText(getResources().getString(R.string.currently_with)
							+ " " + this.retrievedBuddyName + extraMsg);
					socialTV.invalidate();
			
					Button outgoingButton = (Button) findViewById(R.id.outgoingInvitations);
					outgoingButton.setText(getResources().getString(R.string.leave_buddy));
					outgoingButton.invalidate();
				}
				else if(!Utils.isConnectionPresent(SettingsActivity.this))
				{
					displayConnectionErrorDialog();
				}
			}
		};
		task.execute();
		
	}
	
	protected void setName(final int id, final boolean isCurrentUser) 
	{
		
		if(!isCurrentUser)
		{
			retrieveBuddyName(".");
			return;
		}
		
		Constants.BUDDY_NAME_ACQUIRED = false;
		RetrieveBuddyNameTask task = new RetrieveBuddyNameTask(id) 
		{

			@Override
			protected void onPostExecute(String username) {
				
				if(!this.safeExecution)
				{
					if(!Utils.isConnectionPresent(SettingsActivity.this))
						displayConnectionErrorDialog();
					return;
				}
				
				if (!Utils.isConnectionPresent(SettingsActivity.this)) {
					displayConnectionErrorDialog();
				}

				if (username != null && Constants.BUDDY_NAME_ACQUIRED) 
				{
					if (!username.startsWith("Error")
							&& !username.contains("error")
							&& !username.contains("Error")) {
						// TextView text= (TextView)
						// findViewById(R.id.textViewPlayer);
						// text.setText(username);
						/*
						 * TextView text= (TextView)
						 * findViewById(R.id.textViewOtherSteps);
						 * text.setText(username + "'s"); text= (TextView)
						 * findViewById(R.id.textViewOtherFloors);
						 * text.setText(username + "'s");
						 */

						if (isCurrentUser) {
							//TextView selfTV = (TextView) findViewById(R.id.userNameInSettings);
							AppContext.getInstance().setUserString(username);
							if(!Constants.FIRST_WELCOME_GIVEN)
							{
								Constants.FIRST_WELCOME_GIVEN = true;
							//	selfTV.setText(getResources().getString(R.string.welcome) + " " + username + "!");
							}
							else
							{
							//	selfTV.setText(getResources().getString(R.string.settings_alternate_title));								
							}
							
							//selfTV.invalidate();
						} else {
							buddyName = username;
							Constants.buddyName = username;
							AppContext.getInstance().setFriendId(id);
							TextView socialTV = (TextView) findViewById(R.id.socialCurrentStatus);
							socialTV.setText(getResources().getString(R.string.currently_with)
									+ " " + username);
							socialTV.invalidate();

							Button outgoingButton = (Button) findViewById(R.id.outgoingInvitations);
							outgoingButton.setText(getResources().getString(R.string.leave_buddy));
							outgoingButton.invalidate();
						}

					}
				}
			}
		};
		task.execute();

		/*
		 * try { task.get(); } catch (InterruptedException e) {
		 * Auto-generated catch block e.printStackTrace(); } catch
		 * (ExecutionException e) {
		 * e.printStackTrace(); }
		 */
		// showProgressDialog(false);

	}

	/**
	 * Open the fitbit authentication website in the android browser
	 */
	public void onOpenFitbiWebsiteButtonClicked(View v) {
		int uid = AppContext.getInstance().getUserId();

		String fitbitUrl = "http://grpupc1.epfl.ch/~yu/htexp/synch4.php?uid="
				+ uid;
		Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(fitbitUrl));
		Log.d(TAG, "Authorizing on fitbit website: " + fitbitUrl);
		startActivity(i);
		//mAuthorizationStarted = true;
	}

	public void countIncomingInvitations() {
		incomingInvitationsCount = 0;

		buddyEmails.clear();
		final Button incomingButton = (Button) findViewById(R.id.incomingInvitations);

		incomingButton.setText(getResources().getString(R.string.settings_incoming_request) + " (" + "..." + ")");

		// hasFriend = false;
		if (!Utils.isConnectionPresent(this)) {
			displayConnectionErrorDialog();
			// Toast.makeText(this,
			// "No internet connection. Please try again later.",
			// Toast.LENGTH_LONG).show();
			return;
		}

		final ArrayAdapter<String> receivedInvitations = new ArrayAdapter<String>(
				SettingsActivity.this, android.R.layout.simple_list_item_1);

		//
		final String email = AppContext.getInstance().getEmail();
		//final TextView textViewBuddyEmail = (TextView) findViewById(R.id.textViewBuddyEmail);
		final ArrayList<String> realFriendEmail = new ArrayList<String>();

		RetrieveBuddyEMailTask2 rtask = new RetrieveBuddyEMailTask2(email) {
			@Override
			protected void onPostExecute(String buddyEmail) {
				
				if(!this.safeExecution)
				{
				//	if(!Utils.isConnectionPresent(SettingsActivity.this))
				//	{
						displayConnectionErrorDialog();
				//	}
					return;
				}
				
				if (buddyEmail.contains("not found")) {

				} else {
					Log.i(TAG,
							"The person already has a buddy. The e-mail of this person is: "
									+ buddyEmail);
					realFriendEmail.add(buddyEmail);
					isPaired = true;
				}

				RetrieveBuddyEMailTask task = new RetrieveBuddyEMailTask(email) {
					@Override
					protected void onPostExecute(String buddyEmail) {
						
						if(!this.safeExecution)
						{
							//if(!Utils.isConnectionPresent(Main2Activity.this))
							//{
								displayConnectionErrorDialog();
							//}
							return;
						}						
						
						
						Log.d(TAG, "RetrieveBuddyEmailTask: " + buddyEmail);

						
						
						
						if (buddyEmail.contains("not found")) {
							// showProgressDialog(false);
							// Toast.makeText(SettingsActivity.this,
							// "Your inviters have changed their mind. No invitations remaining.",
							// Toast.LENGTH_LONG).show();
							Log.i(TAG, "This user does not have any requests.");
							Constants.INVITATION_RECEIVED = false;
							incomingButton.setText( getResources().getString(R.string.settings_incoming_request)+ " (" + 0
									+ ")");
							
							LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0,0);
							incomingButton.setLayoutParams(p);							
							
							incomingButton.invalidate();
							
						} else {
							//
							if (buddyEmail.contains("@@")) {
								// Form a list of invitations
								String stringBody = buddyEmail;
								String notifierEmail = "";
								int delim = stringBody.indexOf("@@");

								while (delim > 0) {
									notifierEmail = stringBody.substring(0,
											delim);
									Log.d(TAG, "notifierEmail = "
											+ notifierEmail);

									if (isPaired
											&& realFriendEmail
													.contains(notifierEmail))// notifierEmail.equals(realFriendEmail.get(0)))
									{
										// continue;
									} else {
										receivedInvitations.add(notifierEmail);

										if (!buddyEmails
												.contains(notifierEmail)) {
											Log.d(TAG,
													"I decided to add this: "
															+ notifierEmail);
											buddyEmails.add(notifierEmail);
											incomingInvitationsCount++;
										}
									}
									stringBody = stringBody
											.substring(delim + 2);
									delim = stringBody.indexOf("@@");
								}

								if (stringBody.length() > 0) {
									if (isPaired
											&& realFriendEmail
													.contains(stringBody))// stringBody.equals(realFriendEmail.get(0)))
									{
										// continue;
									} else {
										if (!buddyEmails.contains(stringBody)) {
											Log.d(TAG,
													"I decided to add this: "
															+ stringBody);
											receivedInvitations.add(stringBody);
											buddyEmails.add(stringBody);
											incomingInvitationsCount++;
										}
									}
								}
							} else {
								// mTextBuddyEmail.setText(buddyEmail);
								// b.setVisibility(Button.VISIBLE);
								if (isPaired
										&& realFriendEmail.contains(buddyEmail))// buddyEmail.equals(realFriendEmail.get(0)))
								{

								} else {
									if (!buddyEmails.contains(buddyEmail)) {
										Log.d(TAG, "I decided to add this: "
												+ buddyEmail);
										incomingInvitationsCount++;
										buddyEmails.add(buddyEmail);
										receivedInvitations.add(buddyEmail);
									}

								}

							}

							Log.d(TAG,
									"buddyEmails count = " + buddyEmails.size());
							Log.d(TAG, "incomingInvitations count = "
									+ incomingInvitationsCount);

							incomingButton.setText(getResources().getString(R.string.settings_incoming_request) + " ("
									+ incomingInvitationsCount + ")");
							
							if(incomingInvitationsCount < 1)
							{
								LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0,0);
								incomingButton.setLayoutParams(p);
							}
							else
							{
								LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,dpToPx(38));
								p.setMargins(0, dpToPx(10), 0, dpToPx(10));
								incomingButton.setLayoutParams(p);							
							}
							
							
							incomingButton.invalidate();
							
							
							
						}
					}
				};
				task.execute();

			}
		};

		rtask.execute();

	}

	public void onContinueToHTButtonClicked(View v) 
	{
		attemptContinue(false);
	}

	public void attemptContinue(boolean fromBackButton) {
		if (isPaired) 
		{
			// Constants.BUDD

			String ns = Context.NOTIFICATION_SERVICE;
			NotificationManager nMgr = (NotificationManager) getApplicationContext()
					.getSystemService(ns);
			nMgr.cancel(Constants.NOTIFICATION_ID_BUDDY_REQUEST_ACCEPTED);

			Intent i_main;
			
			if(!Constants.MAIN_OR_COMMUNITY)
			{
				Log.i(TAG, "Switching to main: attemptContinue");
				i_main = new Intent(this, Main2Activity.class);
			}
			else
			{
				i_main = new Intent(this, MainActivityCommunity.class); 
				i_main.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
						| Intent.FLAG_ACTIVITY_SINGLE_TOP);				
			}
			startActivity(i_main);
		} 
		else 
		{
			if(!fromBackButton)
			{
				Toast.makeText(this, getResources().getString(R.string.settings_no_buddy_warning),
					Toast.LENGTH_SHORT).show();
			}
			else
			{
				moveTaskToBack(true);
			}
		}
	}

	public void onChangeBuddyButtonFromSettingsClicked(View v) {
		final ProgressDialog pd = ProgressDialog.show(this, getResources().getString(R.string.switching_buddy_string),
				getResources().getString(R.string.canceling_game));

		DeFriendTask task = new DeFriendTask(AppContext.getInstance()
				.getUserId(), AppContext.getInstance().getFriendId()) {

			@Override
			protected void onPostExecute(Boolean success) {
				pd.dismiss();
				if (success) {
					// Go to the pending buddy activity where the user can
					// remind the other player
					Constants.COUNTER_INVITE = false;
					Constants.INVITATION_ACCEPTED = false;
					Constants.INVITATION_SENT = false;
					Constants.INVITATION_RECEIVED = false;
					
					SharedPreferences prefs2 = getSharedPreferences(
							Constants.PROPERTIES_NAME, MODE_PRIVATE);
					// Addition: so avoid the remnants
					Constants.internalReset(SettingsActivity.this,prefs2);
					Constants.externalReset();

					AppContext.getInstance().setFriendId(0);
					
					/*SharedPreferences prefs2 = getSharedPreferences(
							Constants.PROPERTIES_NAME, MODE_PRIVATE);*/
					Editor editor = prefs2.edit();
					editor.putBoolean(Constants.PROP_KEY_GAME_STARTED, false);
					editor.commit();

					Intent i_change_buddy = new Intent(SettingsActivity.this,
							InviteBuddyActivity.class);
					i_change_buddy
							.putExtra(
									"message",
									getResources().getString(R.string.you_were_playing_with)
											+ " " + buddyName + " "
											+ getResources().getString(R.string.and_now_unpaired));
					startActivity(i_change_buddy);
				} else if (!Utils.isConnectionPresent(SettingsActivity.this)) {
					Toast.makeText(SettingsActivity.this,
							getResources().getString(R.string.connection_toast_message),
							Toast.LENGTH_LONG).show();
					// displayConnectionErrorDialog();
				} else {
					Toast.makeText(SettingsActivity.this,
							getResources().getString(R.string.connection_toast_message),
							Toast.LENGTH_LONG).show();
				}
			}
		};
		task.execute();

	}

	public void onPopupOkButtonFromSettingsClicked(View v) {
		if (alertDialog.isShowing()) { // safe check
			alertDialog.dismiss();
		}
	}

	public void onOutgoingInvitationsButtonClicked(View v) {
		// Toast.makeText(this, "onOutgoingInvitationsButtonClicked",
		// Toast.LENGTH_SHORT).show();

		if (checkGroupTaskResult == 0) {
			// The text of the button is supposed to be "Send an invitation"
			Intent i_invite = new Intent(SettingsActivity.this,
					InviteBuddyActivity.class);
			startActivity(i_invite);
		} else if (checkGroupTaskResult == -1) {
			Intent i_pending = new Intent(SettingsActivity.this,
					PendingBuddyRequestActivity.class);
			startActivity(i_pending);
		} else {

			if (isPaired) {

				LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View layout = inflater.inflate(
						R.layout.activity_change_from_settings_dialog, null);
				AlertDialog.Builder builder = new AlertDialog.Builder(this)
						.setView(layout);
				alertDialog = builder.create();
				alertDialog.show();
			} else if (Constants.INVITATION_SENT) {
				Intent i_pending = new Intent(SettingsActivity.this,
						PendingBuddyRequestActivity.class);
				startActivity(i_pending);
			} else if (incomingInvitationsCount > 0) {

				Intent i_invite = new Intent(SettingsActivity.this,
						InviteBuddyActivity.class);
				startActivity(i_invite);
			}

		}

	}

	public void onIncomingInvitationsButtonClicked(View v) {

		if (incomingInvitationsCount == 0) {
			Toast.makeText(this, getResources().getString(R.string.settings_no_invitation),
					Toast.LENGTH_SHORT).show();
		} else {
			Intent i_incoming = new Intent(SettingsActivity.this,
					AcceptBuddyActivity.class);
			startActivity(i_incoming);

		}
	}

	public void onSeeReceivedInvitationsButtonClicked(View v) {
		Intent i_accept = new Intent(this, AcceptBuddyActivity.class);
		startActivity(i_accept);
	}

	public void onLogoutButtonClicked(View v) 
	{
		SharedPreferences prefs2 = getSharedPreferences(
				Constants.PROPERTIES_NAME, MODE_PRIVATE);
		Utils.logout(this,prefs2);
	}

	public void onDebugButtonClicked(View v) {
		RetrieveStepCountTask task = new RetrieveStepCountTask(AppContext
				.getInstance().getUserId(), 0) {

			@Override
			protected void onPostExecute(Integer stepCount) {
				
				if(!this.safeExecution)
				{
					Toast.makeText(SettingsActivity.this, getResources().getString(R.string.connection_toast_message), Toast.LENGTH_LONG).show();
				}
				else
				{
					Toast.makeText(SettingsActivity.this,
							"step count: " + stepCount, Toast.LENGTH_SHORT).show();
				}
			}
		};
		task.execute();
	}

	public void displayConnectionErrorDialog(boolean show) {
		if (show) {
			displayConnectionErrorDialog();
		} else if (connectionDialog != null) {
			try
			{
				connectionDialog.dismiss();
			}
			catch(Exception ex)
			{
				
			}
		}
	}

	public void displayConnectionErrorDialog() {
		if (!isDisplayingConnectionErrorDialog) // creationComplete &&
		{

			if (isActive) {
				showProgressDialog(false);

				AlertDialog.Builder builder;
				builder = new AlertDialog.Builder(SettingsActivity.this);
				builder.setTitle(getResources().getString(R.string.connection_error_title))
						.setMessage(
								getResources().getString(R.string.connection_error_message))
						// .setCancelable(false)
						.setOnCancelListener(
								new DialogInterface.OnCancelListener() {

									@Override
									public void onCancel(DialogInterface dialog) {
										//moveTaskToBack(true);
										Log.i(TAG, "Switching to main: displayConnectionErrorDialog");
										Intent i_main = new Intent(SettingsActivity.this, Main2Activity.class);
										startActivity(i_main);
										// checkProgressHandler.postDelayed(checkForProgressTask,
										// 0);
									}
								})
						.setNeutralButton(getResources().getString(R.string.connection_error_retry),
								new OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {

										isDisplayingConnectionErrorDialog = false;
										Log.d(TAG,
												"showProgressDialog called from connection error dialog.");
										// showProgressDialog(true);
										settingsScreenSetup();
										// checkProgressHandler.postDelayed(checkForProgressTask,
										// 0);
									}
								});

				connectionDialog = builder.create();

				isDisplayingConnectionErrorDialog = true;
				connectionDialog.show();
			}
		}
	}

	private void showProgressDialog(final boolean show) {
		if (!isDisplayingConnectionErrorDialog) {
			if (show) {

				if (isActive) {
					initialSyncDialog = ProgressDialog.show(this, getResources().getString(R.string.settings_progress_title),
							getResources().getString(R.string.settings_progress_message));
				}
			} else if (initialSyncDialog != null) {
				initialSyncDialog.dismiss();
				initialSyncDialog = null;
			}
		}
	}
	
	
	private void hideContinueHTUI(boolean hide)
	{
		TextView t2 = (TextView)findViewById(R.id.game_info);
		TextView t3 = (TextView)findViewById(R.id.settings_reminder);
		Button b1 = (Button)findViewById(R.id.continueHTButton);
		//View v = (View)findViewById(R.id.game_info_margin);
		
		LinearLayout.LayoutParams p1, p2;
		
		if(hide)
		{
			p1 = new LinearLayout.LayoutParams(0,0);
			p2 = new LinearLayout.LayoutParams(0,0);
		}
		else
		{
			p1 = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,dpToPx(38));
			p2 = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);//);
		}
		
		t2.setLayoutParams(p1);
		t3.setLayoutParams(p2);
		//v.setLayoutParams(p2);
		
		//left, top, right, bottom);
		if(hide)
		{
			p1.setMargins(0, 0, 0, 0);
		}
		else
		{
			p1.setMargins(0, dpToPx(10), 0, dpToPx(10));
		}
		
		b1.setLayoutParams(p1);
	}
	
	private void hideInvitationUI(boolean hide)
	{
		authorizationDone = !hide;
		
		TextView t0 = (TextView)findViewById(R.id.socialCurrentStatus);
		
		TextView t2 = (TextView)findViewById(R.id.socialSettingsHeader);
		Button b0 = (Button)findViewById(R.id.outgoingInvitations);
		Button b1 = (Button)findViewById(R.id.incomingInvitations);
		View v = (View)findViewById(R.id.fbAuthenticationBorder);
		
		LinearLayout.LayoutParams p1, p2;
		
		if(hide)
		{
			p1 = new LinearLayout.LayoutParams(0,0);
			p2 = new LinearLayout.LayoutParams(0,0);
			t0.setText(getResources().getString(R.string.settings_alternate_title));
		}
		else
		{
			p1 = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,dpToPx(38));
			p2 = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,dpToPx(1));
		}
		
		t2.setLayoutParams(p1);
		
		//b1.setLayoutParams(p1);
		v.setLayoutParams(p2);
		
		//left, top, right, bottom);
		if(hide)
		{
			p1.setMargins(0, 0, 0, 0);
		}
		else
		{
			p1.setMargins(0, dpToPx(10), 0, dpToPx(10));
		}
		b0.setLayoutParams(p1);
		b1.setLayoutParams(p1);		
	}
	
	private void hideAuthenticationUI()
	{
		
		TextView t1 = (TextView)findViewById(R.id.info);
		//TextView t2 = (TextView)findViewById(R.id.fbAuthinfo);
		TextView t3 = (TextView)findViewById(R.id.fbCurrentStatus);
		Button b1 = (Button)findViewById(R.id.fbAuthenticationButton);
		View v = (View)findViewById(R.id.fbAuthenticationBorder);
		
		LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0,0);
		
		t1.setLayoutParams(p);
	//	t2.setLayoutParams(p);
		t3.setLayoutParams(p);
		b1.setLayoutParams(p);
		v.setLayoutParams(p);
	}
	
	
	
	public int dpToPx(int dp) {
	    /*DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
	    
	    float m = Utils.getScrenPixelMultiplier(this, displayMetrics);
	    
	    int px = Math.round(dp * (m / DisplayMetrics.DENSITY_DEFAULT)); // displayMetrics.xdpi       
	    return px;*/
		//Resources r = getResources();
		float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,  getResources().getDisplayMetrics());//r.getDisplayMetrics());
		
		return Math.round(px);
	}	
	
	public int pxToDp(int px) {
	    DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
	    float m = Utils.getScrenPixelMultiplier(this, displayMetrics);
	    int dp = Math.round(px / (m / DisplayMetrics.DENSITY_DEFAULT)); //displayMetrics.xdpi
	    return dp;
	}
	
	

}
