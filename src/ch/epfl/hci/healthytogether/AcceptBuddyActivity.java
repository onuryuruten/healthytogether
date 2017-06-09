package ch.epfl.hci.healthytogether;

import java.util.ArrayList;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import ch.epfl.hci.happytogether.R;
import ch.epfl.hci.healthytogether.communication.ServerHelper.AcceptBuddyRequestTask;
import ch.epfl.hci.healthytogether.communication.ServerHelper.DeFriendTask;
import ch.epfl.hci.healthytogether.communication.ServerHelper.DeFriendTask2;
import ch.epfl.hci.healthytogether.communication.ServerHelper.RetrieveBuddyEMailTask;
import ch.epfl.hci.healthytogether.communication.ServerHelper.RetrieveBuddyEMailTask2;
import ch.epfl.hci.healthytogether.communication.ServerHelper.RetrievePendingBuddyEMailTask;
import ch.epfl.hci.healthytogether.util.Utils;

/**
 * Shows a buddy invitation to the user and allows him to accept it.
 * 
 */
public class AcceptBuddyActivity extends Activity {

	private static final String TAG = AcceptBuddyActivity.class.getSimpleName();
	//private TextView mTextBuddyEmail;
	public static boolean invitationSelected = false;
	public static String selectedInvitation = "";
	ProgressDialog dialog;
	ArrayList<String> buddyEmails = new ArrayList<String>();
	ArrayAdapter<String> items;
	public static boolean hasFriend = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.activity_accept_buddy);
		Log.e(TAG,"Started AcceptBuddyActivity");

		final SharedPreferences prefs = getSharedPreferences(
				Constants.PROPERTIES_NAME, MODE_PRIVATE);
		//final Button switchToYourOwnInviationsButton = (Button) findViewById(R.id.switchToYourOwnInvitationsButton);

		if (prefs.getBoolean(Constants.PROP_KEY_GAME_STARTED, false)) {
		//	switchToYourOwnInviationsButton.setVisibility(Button.INVISIBLE);
		//	switchToYourOwnInviationsButton.setText(getResources().getString(R.string.back_to_game));
			// switchToYourOwnInviationsButton.setText("Respond Later");
			//Toast.makeText(AcceptBuddyActivity.this, "onCreate: Game is on!", Toast.LENGTH_LONG).show();
		}

		RetrievePendingBuddyEMailTask rpbetask = new RetrievePendingBuddyEMailTask(
				AppContext.getInstance().getUserId()) {

			@Override
			protected void onPostExecute(String email) {

				if (email.contains("not found")) {
					Constants.INVITATION_SENT = false;
				}

			}
		};
		rpbetask.execute();

		//mTextBuddyEmail = (TextView) findViewById(R.id.textViewBuddyEmail);
		//mTextBuddyEmail.setText(getResources().getString(R.string.main_loading));
		final String ownEmail = AppContext.getInstance().getEmail();
		final TextView explainText = (TextView)findViewById(R.id.you_have_an_invitation);
		
		//final Button b = (Button) findViewById(R.id.acceptButton);
		//final ImageButton viewAllInviationsButton = (ImageButton) findViewById(R.id.viewAllInvitationsButton);
		//final Button switchb = switchToYourOwnInviationsButton;
		//final Button rejectb = (Button) findViewById(R.id.rejectInvitationsButton);

		buddyEmails.clear();
		RetrieveBuddyEMailTask2 t2 = new RetrieveBuddyEMailTask2(ownEmail) 
		{

			@Override
			protected void onPostExecute(String result) 
			{
				final ArrayList<Boolean> hasFriend = new ArrayList<Boolean>();
				
				String output;
				if (result.contains("not found")) {
					Log.i(TAG, "The user does not have any buddy.");

					output = "";
					// Toast.makeText(AcceptBuddyActivity.this,
					// "You do not have any invitations remaining",
					// Toast.LENGTH_LONG).show();
				} else {
					hasFriend.add(true);
					output = result;
				}
				
				final String myResult = output;
				
				RetrieveBuddyEMailTask task = new RetrieveBuddyEMailTask(
						ownEmail) {
					@Override
					protected void onPostExecute(String buddyEmail) {
						if (buddyEmail.contains("@@")) 
						{
							
							Log.i(TAG, "buddy email contains @@");
							
							//if (hasFriend.isEmpty()) {
							//	mTextBuddyEmail.setText(getResources().getString(R.string.multiple_users));
							//	Log.i(TAG, "we have multiple inviters");
							//	invitationSelected = false;
							
								// Form a list of invitations
								String stringBody = buddyEmail;
								String notifierEmail = "";
								int delim = stringBody.indexOf("@@");
								
								// TODO: check if I need to uncomment:
								//ArrayList<String> buddyEmails = new ArrayList<String>();
								//buddyEmails.clear();
								while (delim > 0) 
								{
									notifierEmail = stringBody.substring(0,
											delim);
									if (notifierEmail.equals(myResult)) {
										// continue;
									} else {
										buddyEmails.add(notifierEmail);
										Log.i(TAG,"RetrieveBuddyEMailTask: We added the email: " + notifierEmail);
									}
									stringBody = stringBody
											.substring(delim + 2);
									delim = stringBody.indexOf("@@");
								}

								if (stringBody.length() > 0) 
								{
									if (stringBody.equals(myResult)) 
									{
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

								
								if(buddyEmails.size() > 0)
								{
									setupInvitationsListView();
									
									if(buddyEmails.size() > 1)
									{
										explainText.setText(getResources().getString(R.string.you_have_invitations));
									}
									else
									{
										explainText.setText(getResources().getString(R.string.you_have_an_invitation));
									}
								}
								
								
								/*if (buddyEmails.size() == 1) {
									invitationSelected = true;
									mTextBuddyEmail.setText(buddyEmails.get(0));
									viewAllInviationsButton.setVisibility(ImageButton.INVISIBLE);
									b.setVisibility(Button.VISIBLE);
								} else if (buddyEmails.size() > 1) {
									mTextBuddyEmail.setText(getResources().getString(R.string.multiple_users));
									Drawable details = getResources().getDrawable(android.R.drawable.ic_menu_info_details); 
									details.setColorFilter(getResources().getColor(R.color.dark_blue), Mode.SRC_IN);//new PorterDuffColorFilter(0xffff00,PorterDuff.Mode.MULTIPLY));
									viewAllInviationsButton.setImageDrawable(details);
									invitationSelected = false;
								}*/

							//}
							Log.i(TAG, "endof: buddy email contains @@");
						} else if (buddyEmail.contains("not found")) {
							String msg = getResources().getString(R.string.unfortunately_buddy_changed_mind);
							// Toast.makeText(AcceptBuddyActivity.this, msg,
							// Toast.LENGTH_LONG).show();
							Intent i_settings = new Intent(
									AcceptBuddyActivity.this,
									SettingsActivity.class);
							i_settings.putExtra("message", msg);
							startActivity(i_settings);
							finish();
						} else {
							invitationSelected = true;
							buddyEmails.add(buddyEmail);
							
							setupInvitationsListView();
							if(buddyEmails.size() > 1)
							{
								explainText.setText(getResources().getString(R.string.you_have_invitations));
							}
							else
							{
								explainText.setText(getResources().getString(R.string.you_have_an_invitation));
							}							
							//mTextBuddyEmail.setText(buddyEmail);
							//b.setVisibility(Button.VISIBLE);
						}
						//viewAllInviationsButton.setVisibility(Button.VISIBLE);
						//switchb.setVisibility(Button.VISIBLE);
						//rejectb.setVisibility(Button.VISIBLE);

						// after-t2 block begin

						final Intent i = getIntent();
						int val = i.getIntExtra("RequestAccepted", -1);
						final String invitingEmail = buddyEmail; // i.getStringExtra("femail");

						Log.i(TAG, "buddyEmail = " + buddyEmail);
						Log.i(TAG, "myResult = " + myResult);
						Log.i(TAG, "invitingEmail = " + invitingEmail);

						// if(invitingEmail != null)
						// {
						if (invitingEmail.contains("not found")) 
						{
							Log.i(TAG,"AcceptBuddyActivity says that it returns to SettingsActivity");
							String msg = getResources().getString(R.string.unfortunately_buddy_changed_mind);
							// Toast.makeText(AcceptBuddyActivity.this, msg,
							// Toast.LENGTH_LONG).show();
							Intent i_settings = new Intent(
									AcceptBuddyActivity.this,
									SettingsActivity.class);
							i_settings.putExtra("message", msg);
							startActivity(i_settings);
							finish();
						}
						// }

						/*
						 * This condition was set in a rather older time.
						 * The logic was, if an invitation was already sent, and
						 * the request was accepted (?)
						 * "skip this screen already, and start the game!"
						 */
						else if (val > 0 && Constants.INVITATION_SENT
								&& myResult.equals(invitingEmail)) 
						{
							Log.i(TAG, "I am bypassing the accept-buddy screen");
							int uid = AppContext.getInstance().getUserId();
							RetrievePendingBuddyEMailTask retrievePendingTask = new RetrievePendingBuddyEMailTask(
									uid) {

								@Override
								protected void onPostExecute(String email) {
									if (invitingEmail.contains(email)) {
										// mTextView.setText(email);
										Utils.scheduleAlarmReceiver(AcceptBuddyActivity.this);
										i.putExtra("RequestAccepted", -1);
										AcceptBuddyRequestTask abrTask = new AcceptBuddyRequestTask(
												AppContext.getInstance()
														.getUserId(), ownEmail,
												email) {

											@Override
											protected void onPostExecute(
													String result) {
												if (Utils.isInteger(result)) {
													Constants.INVITATION_RECEIVED = true;
													// The request was
													// successful and the server
													// returned the friend's ID
													AppContext
															.getInstance()
															.setFriendId(
																	Integer.parseInt(result));
													Utils.scheduleAlarmReceiver(AcceptBuddyActivity.this);

													// Remove the notification
													// (in case a new one has
													// been issued)
													NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
													notificationManager
															.cancel(Constants.NOTIFICATION_ID_PENDING_BUDDY_REQUEST);

													// Set property to indicate
													// invite has been accepted
													// and game started
													SharedPreferences prefs = getSharedPreferences(
															Constants.PROPERTIES_NAME,
															MODE_PRIVATE);
													Editor editor = prefs
															.edit();
													editor.putBoolean(
															Constants.PROP_KEY_GAME_STARTED,
															true);
													editor.commit();
													Constants
															.embarkAcceptanceDate(prefs); // i.e.,
																							// NOW
													Constants.INVITATION_OUTCOME_DISPLAYED = true;
													// Go to main screen
													Intent i_main = new Intent(
															AcceptBuddyActivity.this,
															Main2Activity.class);
													startActivity(i_main);
													finish();
												} else {
													if (result.equals("AC")
															|| result
																	.equals("NF")) {

														String msg = getResources().getString(R.string.unfortunately_buddy_changed_mind);
														// Toast.makeText(AcceptBuddyActivity.this,
														// msg,
														// Toast.LENGTH_LONG).show();
														Intent i_settings = new Intent(
																AcceptBuddyActivity.this,
																SettingsActivity.class);
														i_settings.putExtra(
																"message", msg);
														startActivity(i_settings);
														finish();

													} else if (!Utils
															.isConnectionPresent(AcceptBuddyActivity.this)) {
														Toast.makeText(
																AcceptBuddyActivity.this,
																getResources().getString(R.string.connection_toast_message),
																Toast.LENGTH_LONG)
																.show();
													}
													// }
													// else
													// {
													// An error has occurred
													// ErrorHandler.create().handleError(AcceptBuddyActivity.this,
													// result, null);
													// }
												}

											}
										};
										abrTask.execute();
									}

								}
							};
							retrievePendingTask.execute();

						} 
						else 
						{
							Constants.INVITATION_SENT = false;
							Log.i(TAG,"AcceptBuddyActivity says that the invitation_sent=false");
						}
						// after-t2 block end

					}
				};
				task.execute();
			}
		};
		t2.execute();

	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	private void showProgressDialog(final boolean show) {
		if (show) {
			dialog = ProgressDialog.show(this, getResources().getString(R.string.your_invitations_dialog_title),
					getResources().getString(R.string.your_invitations_dialog_content));
		} else if (dialog != null) {
			dialog.dismiss();
			dialog = null;
		}
	}

	private void showProgressDialog(final boolean show, String header,
			String message) {
		if (show) {
			dialog = ProgressDialog.show(this, header, message);
		} else if (dialog != null) {
			dialog.dismiss();
			dialog = null;
		}
	}

	/**
	 * Shows the progress UI and hides the invite buddy screen
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	/*private void showProgress(final boolean show) {
		final View mLoginFormView = findViewById(R.id.accept_form);
		final View mLoginStatusView = findViewById(R.id.list_inviters_status);
		// final TextView mLoginStatusMessageView = (TextView)
		// findViewById(R.id.login_status_message);

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
	}*/

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && !Constants.INVITATION_SENT) {
			//moveTaskToBack(true);
			
			Intent i_settings = new Intent(AcceptBuddyActivity.this, SettingsActivity.class);
			startActivity(i_settings);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_accept_buddy, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		case R.id.menu_settings:
			Intent i_settings = new Intent(this, SettingsActivity.class);
			startActivity(i_settings);
			return true;
	 	case R.id.menu_settings_logout:
			SharedPreferences prefs2 = getSharedPreferences(
					Constants.PROPERTIES_NAME, MODE_PRIVATE);
	 		return Utils.logout(this,prefs2);			
		}
		return super.onOptionsItemSelected(item);
	}

	protected void onConfirmInvitationClicked(String buddyCandidate) 
	{
		final SharedPreferences prefs = getSharedPreferences(
				Constants.PROPERTIES_NAME, MODE_PRIVATE);
		final String email = AppContext.getInstance().getEmail();
		final int uid = AppContext.getInstance().getUserId();
		
		Constants.INVITATION_SENT = false;
		
		if (prefs.getBoolean(Constants.PROP_KEY_GAME_STARTED, false)) {
			// defriend the other poor buddy.
			// Toast.makeText(AcceptBuddyActivity.this, "Defriend..",
			// Toast.LENGTH_LONG).show();
			// int uid = AppContext.getInstance().getUserId();
			final int fid = AppContext.getInstance().getFriendId();
			DeFriendTask dfOldBuddy = new DeFriendTask(uid, fid) {

				@Override
				protected void onPostExecute(Boolean result) {
					// Addition: so avoid the remnants
					SharedPreferences prefs2 = getSharedPreferences(
							Constants.PROPERTIES_NAME, MODE_PRIVATE);
					Constants.internalReset(AcceptBuddyActivity.this,prefs2);
					Constants.externalReset();
					if (fid > 0) {
						// Toast.makeText(AcceptBuddyActivity.this,
						// "Your are unpaired with your old buddy. Let's play with the new one!",
						// Toast.LENGTH_LONG).show();
					}
					performAcceptOperation(email, uid, prefs);
				}
			};
			dfOldBuddy.execute();
		} else {
			performAcceptOperation(email, uid, prefs);
		}		
	}
	
	private void displayBuddyConfirmDialog(final String buddyCandidate) 
	{				
				LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View layout = inflater.inflate(R.layout.activity_buddy_confirm_dialog,
						null);
				
				AlertDialog.Builder builder = new AlertDialog.Builder(this)
				.setView(layout)
				.setOnCancelListener(
				new DialogInterface.OnCancelListener() {
		
					@Override
					public void onCancel(DialogInterface dialog) {
						showProgressDialog(false);
					}
				});
				
				
				final AlertDialog dialog = builder.create();
				dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
				
				Button okBtn= (Button) layout.findViewById(R.id.okbtn);
				Button cancelBtn = (Button) layout.findViewById(R.id.cancelbtn);
				

				RelativeLayout.LayoutParams rclp = new RelativeLayout.LayoutParams(dpToPx(120), dpToPx(48));
				rclp.setMargins(dpToPx(10), 0, dpToPx(10), dpToPx(10));
				
				cancelBtn.setLayoutParams(rclp);
				
				cancelBtn.setOnClickListener(new View.OnClickListener() {
					
					public void onClick(View v) {
						showProgressDialog(false);
						dialog.cancel();
						
					}
				});		
				
				RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(dpToPx(120), dpToPx(48));
				rlp.setMargins(dpToPx(10), 0, dpToPx(10), dpToPx(10));
				rlp.addRule(RelativeLayout.RIGHT_OF,cancelBtn.getId());
				//rlp.
				okBtn.setLayoutParams(rlp);
				
				okBtn.setOnClickListener(new View.OnClickListener() {
					
					public void onClick(View v) {
						onConfirmInvitationClicked(buddyCandidate);
						dialog.cancel();
					}
				});				
				
				
				TextView dialogTV = (TextView) layout.findViewById(R.id.alertinfo3);
				
				// 
				String dialogContent =  dialogTV.getText().toString() + "\n" + buddyCandidate + "?";
				
				dialogTV.setText(dialogContent);
				
				dialog.show();

		
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
	
	
	public void onAcceptButtonClicked(View v) {

		if (Utils.isConnectionPresent(this)) {
	//		final String email = AppContext.getInstance().getEmail();
	//		final int uid = AppContext.getInstance().getUserId();

			showProgressDialog(true, getResources().getString(R.string.accept_dialog_title), getResources().getString(R.string.accept_dialog_content));
			// Toast.makeText(AcceptBuddyActivity.this, "Invitation selected = "
			// + invitationSelected, Toast.LENGTH_LONG).show();

			if (invitationSelected) {
				
				//final SharedPreferences prefs = getSharedPreferences(Constants.PROPERTIES_NAME, MODE_PRIVATE);

				displayBuddyConfirmDialog(selectedInvitation);
				/*
				if (prefs.getBoolean(Constants.PROP_KEY_GAME_STARTED, false)) {
					// defriend the other poor buddy.
					// Toast.makeText(AcceptBuddyActivity.this, "Defriend..",
					// Toast.LENGTH_LONG).show();
					// int uid = AppContext.getInstance().getUserId();
					final int fid = AppContext.getInstance().getFriendId();
					DeFriendTask dfOldBuddy = new DeFriendTask(uid, fid) {

						@Override
						protected void onPostExecute(Boolean result) {
							// Addition: so avoid the remnants
							SharedPreferences prefs2 = getSharedPreferences(
									Constants.PROPERTIES_NAME, MODE_PRIVATE);
							Constants.internalReset(prefs2);
							Constants.externalReset();
							if (fid > 0) {
								// Toast.makeText(AcceptBuddyActivity.this,
								// "Your are unpaired with your old buddy. Let's play with the new one!",
								// Toast.LENGTH_LONG).show();
							}
							performAcceptOperation(email, uid, prefs);
						}
					};
					dfOldBuddy.execute();
				} else {
					performAcceptOperation(email, uid, prefs);
				}*/
			} else {
				showProgressDialog(false);
				Toast.makeText(AcceptBuddyActivity.this,
						getResources().getString(R.string.select_an_inviation_first), Toast.LENGTH_LONG)
						.show();
			}
		} else {
			showProgressDialog(false);
			Toast.makeText(AcceptBuddyActivity.this,
					getResources().getString(R.string.connection_toast_message),
					Toast.LENGTH_LONG).show();
		}
	}

	public void performAcceptOperation(String email, int uid,
			final SharedPreferences prefs) {
		AcceptBuddyRequestTask task = new AcceptBuddyRequestTask(uid, email,
				selectedInvitation) {

			@Override
			protected void onPostExecute(String result) {
				if (Utils.isInteger(result)) {
					Constants.INVITATION_RECEIVED = true;
					// The request was successful and the server returned the
					// friend's ID
					AppContext.getInstance().setFriendId(
							Integer.parseInt(result));
					Utils.scheduleAlarmReceiver(AcceptBuddyActivity.this);

					// Remove the notification (in case a new one has been
					// issued)
					NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
					notificationManager
							.cancel(Constants.NOTIFICATION_ID_PENDING_BUDDY_REQUEST);

					// Set property to indicate invite has been accepted and
					// game started

					Editor editor = prefs.edit();
					editor.putBoolean(Constants.PROP_KEY_GAME_STARTED, true);
					Constants.INVITATION_OUTCOME_DISPLAYED = true;
					editor.commit();
					Constants.embarkAcceptanceDate(prefs); // i.e., NOW
					Constants.clearNotificationDate(prefs);
					/*Toast.makeText(AcceptBuddyActivity.this,
							getResources().getString(R.string.you_have_accepted) + " " + getResources().getString(R.string.lets_play),
							Toast.LENGTH_LONG).show();*/
					// Go to main screen
					showProgressDialog(false);
					Intent i = new Intent(AcceptBuddyActivity.this,
							Main2Activity.class);
					i.putExtra("message",getResources().getString(R.string.you_have_accepted) + " " + getResources().getString(R.string.lets_play));
					startActivity(i);
					finish();
				} else {
					// if(result.equals("AC"))
					// {

					if (!Utils.isConnectionPresent(AcceptBuddyActivity.this)) {
						Toast.makeText(
								AcceptBuddyActivity.this,
								getResources().getString(R.string.connection_toast_message),
								Toast.LENGTH_LONG).show();
					} else {
						showProgressDialog(false);
						String msg = getResources().getString(R.string.unfortunately_buddy_changed_mind);
						// Toast.makeText(AcceptBuddyActivity.this, msg,
						// Toast.LENGTH_LONG).show();
						Intent i = new Intent(AcceptBuddyActivity.this,
								SettingsActivity.class); // previously:
															// InviteBuddyActivity
						i.putExtra("message", msg);
						startActivity(i);
						finish();
					}
				}

			}
		};
		task.execute();
	}

	public void onSwitchToYourOwnInvitationsButtonClicked(View v) {
		final SharedPreferences prefs = getSharedPreferences(
				Constants.PROPERTIES_NAME, MODE_PRIVATE);
		if (prefs.getBoolean(Constants.PROP_KEY_GAME_STARTED, false)) {
			Intent i = new Intent(AcceptBuddyActivity.this, Main2Activity.class);
			startActivity(i);
		} else if (!Constants.INVITATION_SENT) {
			Toast.makeText(AcceptBuddyActivity.this,
					getResources().getString(R.string.no_outgoing_invitations), Toast.LENGTH_SHORT)
					.show();
		} else {
			RetrievePendingBuddyEMailTask task = new RetrievePendingBuddyEMailTask(
					AppContext.getInstance().getUserId()) {

				@Override
				protected void onPostExecute(String email) {
					if (email.contains("not found")) {
						Toast.makeText(AcceptBuddyActivity.this,
								getResources().getString(R.string.no_outgoing_invitations),
								Toast.LENGTH_SHORT).show();

						Constants.INVITATION_SENT = false;

					} else {
						Intent i = new Intent(AcceptBuddyActivity.this,
								PendingBuddyRequestActivity.class);
						startActivity(i);
					}
				}
			};
			task.execute();

		}
	}

	public void onRejectInvitationsButtonClicked(View v) {
		
		if(!invitationSelected)
		{
			Toast.makeText(AcceptBuddyActivity.this, getResources().getString(R.string.select_an_inviation_first), Toast.LENGTH_SHORT).show();
			return;
		}
		
		//TextView inviteeTV = (TextView)findViewById(R.id.textViewBuddyEmail);
		final ListView lv = (ListView)findViewById(R.id.listViewBuddyEmail);
		
		final String removalString = selectedInvitation;//lv.getSelectedItem() + "";
		
		Log.i(TAG, "Let us reject the invitations.");
		hasFriend = false;

		
		
		showProgressDialog(true, getResources().getString(R.string.rejecting_dialog_title),
				getResources().getString(R.string.rejecting_dialog_content));

		// Toast.makeText(AcceptBuddyActivity.this, "onViewAllClicked",
		// Toast.LENGTH_LONG).show();
		final String email = AppContext.getInstance().getEmail();
		//final TextView textViewBuddyEmail = (TextView) findViewById(R.id.textViewBuddyEmail);

		//buddyEmails.clear();

		final ArrayList<String> realFriendEmail = new ArrayList<String>();

		Log.i(TAG, "Instantiating RetrieveBuddyEMailTask2");
		RetrieveBuddyEMailTask2 rtask = new RetrieveBuddyEMailTask2(email) {
			@Override
			protected void onPostExecute(String buddyEmail) {
				Log.i(TAG, "RetrieveBuddyEMailTask2 output: " + buddyEmail);
				if (buddyEmail.contains("not found")) {
					Log.i(TAG, "The user does not have any buddy.");

				} else {
					realFriendEmail.add(buddyEmail);
					hasFriend = true;
				}
				// /////

				Log.i(TAG, "Instantiating RetrieveBuddyEMailTask");
				RetrieveBuddyEMailTask task = new RetrieveBuddyEMailTask(email) {

					@Override
					protected void onPostExecute(String buddyEmail) {

						if (buddyEmail.contains("not found")) {
							Log.i(TAG,
									"RetrieveBuddyEMailTask: The user does not have invitations.");

							showProgressDialog(false);
							String msg = getResources().getString(R.string.nothing_left);
							/*Toast.makeText(AcceptBuddyActivity.this, msg,
									Toast.LENGTH_LONG).show();*/
							if (hasFriend) // hasFriend
							{
								/*Intent intent = new Intent(
										AcceptBuddyActivity.this,
										Main2Activity.class);
								startActivity(intent);*/
								Intent i_main;
								
								if(!Constants.MAIN_OR_COMMUNITY)
								{
									i_main = new Intent(AcceptBuddyActivity.this, Main2Activity.class);
								}
								else
								{
									i_main = new Intent(AcceptBuddyActivity.this, MainActivityCommunity.class); 
									i_main.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
											| Intent.FLAG_ACTIVITY_SINGLE_TOP);				
								}
								startActivity(i_main);	
								
							} 
							else 
							{
								Intent intent = new Intent(
										AcceptBuddyActivity.this,
										SettingsActivity.class);
								intent.putExtra("message", msg);
								startActivity(intent);
							}
						} else {
							// Toast.makeText(AcceptBuddyActivity.this,
							// buddyEmail, Toast.LENGTH_LONG).show();
							if (buddyEmail.contains("@@")) {
								// Form a list of invitations
								String stringBody = buddyEmail;
								String notifierEmail = "";
								int delim = stringBody.indexOf("@@");

								while (delim > 0) {
									notifierEmail = stringBody.substring(0,
											delim);
									if (hasFriend && notifierEmail.equals(realFriendEmail
															.get(0))) {
										// continue;
									} 
									else if(!buddyEmails.contains(notifierEmail))
									{
										buddyEmails.add(notifierEmail);
										Log.i(TAG,"RetrieveBuddyEMailTask: We added the email: " + notifierEmail);
									}
									stringBody = stringBody.substring(delim + 2);
									delim = stringBody.indexOf("@@");
								}

								if (stringBody.length() > 0) {
									if (hasFriend && stringBody.equals(realFriendEmail.get(0))) {
										// continue;
										Log.i(TAG,
												"RetrieveBuddyEMailTask: We did not add the email: "
														+ stringBody);
									} else if(!buddyEmails.contains(stringBody)) {
										buddyEmails.add(stringBody);
										Log.i(TAG,"RetrieveBuddyEMailTask: We added the email: " + stringBody);
									}
								}
							} else {
								// mTextBuddyEmail.setText(buddyEmail);
								// b.setVisibility(Button.VISIBLE);
								if (hasFriend && buddyEmail.equals(realFriendEmail.get(0))) {

								} 
								else if(!buddyEmails.contains(buddyEmail)) {
									buddyEmails.add(buddyEmail);
								}

							}

							final ArrayList<Integer> emid = new ArrayList<Integer>();
							final TextView explainText = (TextView)findViewById(R.id.you_have_an_invitation);
							emid.add(0);
							
							for (int i = 0; i < buddyEmails.size(); i++) {
								// emid.clear();
								emid.set(0,i);
								
								if(removalString.equals(buddyEmails.get(i)))
								{
									Log.d(TAG, "Removing: " + buddyEmails.get(i));
									
									DeFriendTask2 taskd2 = new DeFriendTask2(
											AppContext.getInstance().getUserId(),
											buddyEmails.get(i)) {
	
										@Override
										protected void onPostExecute(Boolean success) {
	
											if (success) {
												// Go to the pending buddy activity
												// where the user can remind the
												// other player
												
												//Log.i(TAG,"I attempt to remove buddyEmails(" + emid.get(0) + ") = " + buddyEmails.get(emid.get(0)));
												//buddyEmails.remove(emid.get(0));
												buddyEmails.remove(removalString);
												items.notifyDataSetChanged();
												Constants.COUNTER_INVITE = false;
												Constants.INVITATION_ACCEPTED = false;
												final SharedPreferences prefs = getSharedPreferences(
														Constants.PROPERTIES_NAME, MODE_PRIVATE);
												Constants.clearNotificationDate(prefs);
												//if (emid.contains(buddyEmails.size() - 1)) {
													showProgressDialog(false);
	
													String msg = "";
	
													if (hasFriend) {
														msg = getResources().getString(R.string.rejected_requests_continue_playing);
													} else {
														msg = getResources().getString(R.string.nothing_left);
	
													}
													
													NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
													notificationManager
															.cancel(Constants.NOTIFICATION_ID_PENDING_BUDDY_REQUEST);
													
													//final ImageButton viewAllInviationsButton = (ImageButton) findViewById(R.id.viewAllInvitationsButton);
													final Button b = (Button) findViewById(R.id.acceptButton);
													
													/*if(buddyEmails.size() > 0)
													{
														// TODO: check this out...
														lv.setAdapter(items);
													}*/
													
													if(buddyEmails.size() > 1)
													{
														explainText.setText(getResources().getString(R.string.you_have_invitations));
													}
													else
													{
														explainText.setText(getResources().getString(R.string.you_have_an_invitation));
													}													
													
													
													if (buddyEmails.size() == 1) 
													{
														Log.i(TAG, "Remaining buddy(" + 0 + ") = " + buddyEmails.get(0));
													/*	Drawable details = getResources().getDrawable(android.R.drawable.ic_menu_info_details); 
														details.setColorFilter(getResources().getColor(R.color.dark_blue), Mode.SRC_IN);//new PorterDuffColorFilter(0xffff00,PorterDuff.Mode.MULTIPLY));
													//	viewAllInviationsButton.setImageDrawable(details);
													//	viewAllInviationsButton.setVisibility(ImageButton.INVISIBLE);
														
														// remain on this screen, show the last person.
														invitationSelected = true;
														mTextBuddyEmail.setText(buddyEmails.get(0));
														b.setVisibility(Button.VISIBLE);*/
														invitationSelected = true;
														//lv.setSe;
														lv.setItemChecked(0, true);
														selectedInvitation = items.getItem(0);
													} 
													else if (buddyEmails.size() > 1) 
													{
													/*  Drawable details = getResources().getDrawable(android.R.drawable.ic_menu_info_details); 
														details.setColorFilter(getResources().getColor(R.color.dark_blue), Mode.SRC_IN);//new PorterDuffColorFilter(0xffff00,PorterDuff.Mode.MULTIPLY));
													//	viewAllInviationsButton.setImageDrawable(details);

														for(int bud = 0; bud < buddyEmails.size(); bud++)
														{
															Log.i(TAG, "Remaining buddy(" + bud + ") = " + buddyEmails.get(bud));
														}
														// remain on this screen, show "Multipe Invitations"
														b.setVisibility(Button.INVISIBLE);
														mTextBuddyEmail.setText(getResources().getString(R.string.multiple_users));
														invitationSelected = false;*/
														invitationSelected = true;
														lv.setItemChecked(0, true);
														selectedInvitation = items.getItem(0);
													}
													else
													{
														Constants.INVITATION_RECEIVED = false;
														if(!hasFriend)
														{
															// Go back to settings.
															Intent i_settings = new Intent(
																	AcceptBuddyActivity.this,
																	SettingsActivity.class);
															i_settings.putExtra("message",
																	msg);
															startActivity(i_settings);
														}
														else
														{
															// if the user already has a friend, continue the game.
															Intent i_main;
															
															if(!Constants.MAIN_OR_COMMUNITY)
															{
																i_main = new Intent(AcceptBuddyActivity.this, Main2Activity.class);
															}
															else
															{
																i_main = new Intent(AcceptBuddyActivity.this, MainActivityCommunity.class); 
																i_main.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
																		| Intent.FLAG_ACTIVITY_SINGLE_TOP);				
															}
															startActivity(i_main);												
														}
													}
												//}
	
											} else {
												Toast.makeText(
														AcceptBuddyActivity.this,
														getResources().getString(R.string.connection_toast_message),
														Toast.LENGTH_LONG).show();
											}
										}
									};
									taskd2.execute();
									break;
								}
							}

						}
					}
				};
				task.execute();

				/*Toast.makeText(AcceptBuddyActivity.this,
						getResources().getString(R.string.rejecting_dialog_content),
						Toast.LENGTH_SHORT).show();*/

			}

		};
		rtask.execute();

	}

	public void setupInvitationsListView() 
	{
		final ListView lv = (ListView)findViewById(R.id.listViewBuddyEmail);
		items = new ArrayAdapter<String>(AcceptBuddyActivity.this, R.layout.my_single_checklist_item, buddyEmails); // android.R.layout.simple_list_item_single_choice
		lv.setAdapter(items);
		lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		lv.setItemChecked(0, true);
		selectedInvitation = items.getItem(0);
		invitationSelected = true;
		//yourListView.setSelection(0);
		//yourListView.getSelectedView().setSelected(true);
		
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) 
			{
				int i = arg2;//lv.getSelectedItemPosition();
				String email = buddyEmails.get(i);
				//Toast.makeText(AcceptBuddyActivity.this, "View.onClickListener: " + email, Toast.LENGTH_SHORT).show();
				selectedInvitation = email;
			}
		});
		
		
		LinearLayout LL = (LinearLayout)findViewById(R.id.accept_form);
		LayoutParams p = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		LL.setLayoutParams(p);
	}

	/*public void onViewAllInviationsButtonClicked(View v) {
		hasFriend = false;
		if (!Utils.isConnectionPresent(this)) {
			Toast.makeText(this,
					getResources().getString(R.string.connection_toast_message),
					Toast.LENGTH_LONG).show();
			return;
		}

		showProgressDialog(true);
		final ArrayAdapter<String> receivedInvitations = new ArrayAdapter<String>(
				AcceptBuddyActivity.this, android.R.layout.simple_list_item_1);

		// Toast.makeText(AcceptBuddyActivity.this, "onViewAllClicked",
		// Toast.LENGTH_LONG).show();
		final String email = AppContext.getInstance().getEmail();
		//final TextView textViewBuddyEmail = (TextView) findViewById(R.id.textViewBuddyEmail);
		final ArrayList<String> realFriendEmail = new ArrayList<String>();

		buddyEmails.clear(); // TODO: check if I should remove this.
		RetrieveBuddyEMailTask2 rtask = new RetrieveBuddyEMailTask2(email) {
			@Override
			protected void onPostExecute(String buddyEmail) {
				if (buddyEmail.contains("not found")) {
					// Toast.makeText(AcceptBuddyActivity.this,
					// "You do not have any invitations remaining",
					// Toast.LENGTH_LONG).show();
				} else {
					realFriendEmail.add(buddyEmail);
					hasFriend = true;
				}

				RetrieveBuddyEMailTask task = new RetrieveBuddyEMailTask(email) {
					@Override
					protected void onPostExecute(String buddyEmail) {
						if (buddyEmail.contains("not found")) {
							showProgressDialog(false);
							// Toast.makeText(AcceptBuddyActivity.this,
							// "Your inviters have changed their mind. No invitations remaining.",
							// Toast.LENGTH_LONG).show();
							Constants.INVITATION_RECEIVED = false;

							Intent i = new Intent(AcceptBuddyActivity.this,
									SettingsActivity.class);
							i.putExtra("message",
									getResources().getString(R.string.inviters_canceled_invitations));

							startActivity(i);

						} else {
							// Toast.makeText(AcceptBuddyActivity.this,
							// buddyEmail, Toast.LENGTH_LONG).show();
							if (buddyEmail.contains("@@")) {
								// Form a list of invitations
								String stringBody = buddyEmail;
								String notifierEmail = "";
								int delim = stringBody.indexOf("@@");

								while (delim > 0) {
									notifierEmail = stringBody.substring(0,
											delim);

									if (hasFriend
											&& notifierEmail
													.equals(realFriendEmail
															.get(0))) {
										// continue;
									} else {
										receivedInvitations.add(notifierEmail);
										buddyEmails.add(notifierEmail);
									}
									stringBody = stringBody
											.substring(delim + 2);
									delim = stringBody.indexOf("@@");
								}

								if (stringBody.length() > 0) {
									if (hasFriend
											&& stringBody
													.equals(realFriendEmail
															.get(0))) {
										// continue;
									} else {
										receivedInvitations.add(stringBody);
										buddyEmails.add(stringBody);
									}
								}
							} else {
								// mTextBuddyEmail.setText(buddyEmail);
								// b.setVisibility(Button.VISIBLE);
								if (hasFriend
										&& buddyEmail.equals(realFriendEmail
												.get(0))) {

								} else {
									buddyEmails.add(buddyEmail);
									receivedInvitations.add(buddyEmail);
								}

							}

							showProgressDialog(false);

							// Toast.makeText(AcceptBuddyActivity.this,
							// "list populated", Toast.LENGTH_LONG).show();

							if (!receivedInvitations.isEmpty()) 
							{
								
								//items = new ArrayAdapter<String>(Accept, textViewResourceId)
								
								AlertDialog.Builder alert = new AlertDialog.Builder(
										AcceptBuddyActivity.this);

								LayoutInflater inflater = getLayoutInflater();
								View viewHeader = inflater.inflate(
										R.layout.customtitle, null);
								TextView title = (TextView) viewHeader
										.findViewById(R.id.myTitle);
								title.setText(getResources().getString(R.string.buddy_choosing_title));
								alert.setCustomTitle(viewHeader);

								alert.setAdapter(receivedInvitations,
										new OnClickListener() {

											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
												textViewBuddyEmail
														.setText(receivedInvitations
																.getItem(which));
												invitationSelected = true;
												final Button b = (Button) findViewById(R.id.acceptButton);
												b.setVisibility(Button.VISIBLE);
											}
										}).setNegativeButton(getResources().getString(R.string.cancel_button), null);

								// alert.show();
								AlertDialog dialog = alert.create();
								dialog.show();
							}
						}
					}
				};
				task.execute();

			}
		};

		rtask.execute();

	}*/

}
