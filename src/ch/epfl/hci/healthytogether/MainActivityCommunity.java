package ch.epfl.hci.healthytogether;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NavUtils;
import android.util.DisplayMetrics;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;
import ch.epfl.hci.happytogether.R;
import ch.epfl.hci.healthytogether.communication.ServerHelper.CheckFitbitAuthenticationTask;
import ch.epfl.hci.healthytogether.communication.ServerHelper.CheckGroupTask;
import ch.epfl.hci.healthytogether.communication.ServerHelper.DeFriendTask2;
import ch.epfl.hci.healthytogether.communication.ServerHelper.GetAvatarTask;
import ch.epfl.hci.healthytogether.communication.ServerHelper.GetListOfUsersTask;
import ch.epfl.hci.healthytogether.communication.ServerHelper.GetStartDateForGameTask;
import ch.epfl.hci.healthytogether.communication.ServerHelper.RetrieveBuddyEMailTask;
import ch.epfl.hci.healthytogether.communication.ServerHelper.RetrieveBuddyEMailTask2;
import ch.epfl.hci.healthytogether.communication.ServerHelper.RetrieveExistingBuddyInfoTask;
import ch.epfl.hci.healthytogether.communication.ServerHelper.SetAvatarTask;
import ch.epfl.hci.healthytogether.communication.ServerHelper.SyncBackendWithFitbitTask;
import ch.epfl.hci.healthytogether.util.Utils;

/**
 * The main activity that shows the progress of the game (who did how many
 * steps, how many are remaining until the next badge is awarded) and the
 * received badges so far.
 * 
 */
public class MainActivityCommunity extends Activity {
	// ASMA START VARIABLES
	public ListView listView;
	public ListView listViewFloor;
	public static Leaderboard leaderboard;
	public static Leaderboard leaderboardFloor;
	
	private Date lastRefStepL=null;
	private Date lastRefFloorL=null;
	
	// private static int leaderboardRank=-1;
	// private static int myAvatar=0;
	// private static int myId=0;

	// ASMA END VARIABLES

	private static final int REQUEST_CODE_AUTHORIZE = 4444;
	private static final String TAG = MainActivityCommunity.class.toString();
	private Integer datePivot;
	private int[] buddyStepHistory = new int[7];
	private int[] myStepHistory = new int[7];
	private int[] buddyFloorHistory = new int[7];
	private int[] myFloorHistory = new int[7];
	private String buddyName;

	private AlertDialog alertDialog;
	private boolean previousActivityIsMessagesActivity;
	boolean displayCongratulations = false;
	private boolean isDisplayingConnectionErrorDialog = false;
	private boolean isDisplayingAuthorizationDialog = false;
	private boolean isDisplayingBuddyDialog = false;
	private boolean progressDialogShown = false;
	//private boolean creationComplete = false;

	private boolean isActive = false;

	private ProgressDialog initialSyncDialog;
	private AlertDialog connectionDialog;
	private AlertDialog buddyDialog;
	private String buddyDialogMessage;
	
	
	private String inviterEmail;
	private ArrayList<String> invitationEmails = new ArrayList<String>();
	ArrayAdapter<String> items;



	boolean refreshDone;
	private long lastRefreshMillis;

	private Handler checkProgressHandler;

	public boolean[] isRefreshed = new boolean[2];
	
	public int tbwidth;
	//private int theight;
	TabHost tab_host;

	public int doHistoryCheck = 0;

	private Runnable checkForProgressTask = new Runnable() {

		@Override
		public void run() {
			boolean connectionErrorReceived = false;

			if (!Utils.isConnectionPresent(MainActivityCommunity.this)) {
				// Toast.makeText(Main2Activity.this,
				// Toast.LENGTH_LONG).show();
				connectionErrorReceived = true;
				//displayConnectionErrorDialog(tab_host.getCurrentTab());
			} else {

				checkUserName();
				int userId = AppContext.getInstance().getUserId();

				// checkProgressHandler.postDelayed(checkForProgressTask, 0); //
				// we immediately check for new messages

				String email = AppContext.getInstance().getEmail();
				CheckGroupTask checkGroupTask = new CheckGroupTask(email,
						userId) {
					@Override
					protected void onPostExecute(String result) {
						
						if(!this.safeExecution)
						{
							if(!Utils.isConnectionPresent(MainActivityCommunity.this))
							{
								displayConnectionErrorDialog();
							}
							return;
						}	
						
						if (Utils.isInteger(result)) {
							int resultCode = Integer.parseInt(result);
							//Intent i;
							switch (resultCode) {
							case RESPONSE_CODE_NO_BUDDY:
								// show screen to invite buddy
								Utils.scheduleAlarmReceiver(MainActivityCommunity.this);
								String msg = "";
								if (Constants.INVITATION_RECEIVED) {
									Constants.INVITATION_RECEIVED = false;
									msg = getResources().getString(R.string.buddy_stopped_playing);//"Your buddy has stopped playing with you.";
								} else if (AppContext.getInstance()
										.getFriendId() > 0) {
									Constants.INVITATION_RECEIVED = false;
									msg = getResources().getString(R.string.buddy_stopped_playing);
								} else if (Constants.INVITATION_CANCELED) {
									Constants.INVITATION_CANCELED = false;
									msg = getResources().getString(R.string.nothing_left);
								} else {
									msg = getResources().getString(R.string.main_no_buddy_warning);//"You do not have a buddy to play with. Please invite a person to continue playing.";
								}

								// New message
								displayBuddyChangeDialog(msg);

								break;
							case RESPONSE_INCOMING_REQUEST_PENDING:
								showBuddyName(AppContext.getInstance()
										.getFriendId());
								RetrieveBuddyEMailTask2 t2 = new RetrieveBuddyEMailTask2(
										AppContext.getInstance().getEmail()) {

									@Override
									protected void onPostExecute(String result) {
										
										
										if(!this.safeExecution)
										{
											if(!Utils.isConnectionPresent(MainActivityCommunity.this))
											{
												displayConnectionErrorMessage();
											}
											return;
										}
										
										final ArrayList<Boolean> hasFriend = new ArrayList<Boolean>();
										final String myResult = result;
										if (result.contains("not found")) {
											/*Log.i(TAG,
													"The user does not have any buddy.");*/

											// Toast.makeText(AcceptBuddyActivity.this,
											// "You do not have any invitations remaining",
											// Toast.LENGTH_LONG).show();
										} else {
											hasFriend.add(true);
										}
										/*
										 * Intent resultIntent = new
										 * Intent(Main2Activity.this,
										 * AcceptBuddyActivity.class);
										 * resultIntent
										 * .putExtra("RequestAccepted", -1);
										 * startActivity(resultIntent);
										 */
										RetrieveBuddyEMailTask task = new RetrieveBuddyEMailTask(
												AppContext.getInstance()
														.getEmail()) {

											@Override
											protected void onPostExecute(
													String buddyEmail) {
												
												if(!this.safeExecution)
												{
													if(!Utils.isConnectionPresent(MainActivityCommunity.this))
													{
														displayConnectionErrorMessage();
													}
													return;
												}
												if (buddyEmail.contains("@@")) {
													
													ArrayList<String> buddyEmails = new ArrayList<String>();
													
													// Form a list of
													// invitations
													String stringBody = buddyEmail;
													String notifierEmail = "";
													int delim = stringBody.indexOf("@@");
													//ArrayList<String> buddyEmails = new ArrayList<String>();
													while (delim > 0) 
													{
														notifierEmail = stringBody.substring(0, delim);
														if (!hasFriend.isEmpty() && notifierEmail.equals(myResult)) 
														{
															// do not add.
														} 
														else 
														{
															buddyEmails.add(notifierEmail);
														}
														stringBody = stringBody.substring(delim + 2);
														delim = stringBody.indexOf("@@");
													}

													if (stringBody.length() > 0) 
													{
														if (!hasFriend.isEmpty() && stringBody.equals(myResult)) 
														{
															// continue;
														} 
														else 
														{
															buddyEmails.add(stringBody);
														}
													}	
													
													if (buddyEmails.size() == 1) 
													{
														showPendingBuddyInvitationNotification(buddyEmails.get(0));
													} 
													else 
													{
														showMultiplePendingBuddyInvitationNotification(buddyEmails);
													}													
													/*if (hasFriend.isEmpty()) {
														// Perform
														// multiple-buddy
														// notification.
														showMultiplePendingBuddyInvitationNotification();
													} else {
														// Form a list of
														// invitations
														String stringBody = buddyEmail;
														String notifierEmail = "";
														int delim = stringBody
																.indexOf("@@");
														ArrayList<String> buddyEmails = new ArrayList<String>();
														while (delim > 0) {
															notifierEmail = stringBody
																	.substring(
																			0,
																			delim);
															if (notifierEmail
																	.equals(myResult)) {
																// continue;
															} else {
																buddyEmails
																		.add(notifierEmail);
															}
															stringBody = stringBody
																	.substring(delim + 2);
															delim = stringBody
																	.indexOf("@@");
														}

														if (stringBody.length() > 0) {
															if (stringBody
																	.equals(myResult)) {
																// continue;
															} else {
																buddyEmails
																		.add(stringBody);
															}
														}

														if (buddyEmails.size() == 1) {
															showPendingBuddyInvitationNotification(buddyEmails
																	.get(0));
														} else {
															showMultiplePendingBuddyInvitationNotification();
														}
													}*/
												} else {
													showPendingBuddyInvitationNotification(buddyEmail);
												}
												showBuddyName(AppContext
														.getInstance()
														.getFriendId());
											}
										};
										task.execute();
									}
								};
								t2.execute();
								showBuddyName(AppContext.getInstance()
										.getFriendId());

								break;
							case RESPONSE_WAITING_FOR_ACCEPT:
								break;
							default:
								AppContext.getInstance()
										.setFriendId(resultCode);
								showBuddyName(resultCode);

								checkStartDate();

								break;
							}
						} else {
							// error log_tag
							// Log.e(TAG, "an error has occurred");
							// Toast.makeText(Main2Activity.this,
							// R.string.error_general,
							// Toast.LENGTH_LONG).show();
						}
					}

				};
				checkGroupTask.execute();


				SharedPreferences prefs = getSharedPreferences(
						Constants.PROPERTIES_NAME, MODE_PRIVATE);
				if (isDifferentDay(prefs)) {
					for (int i = 1; i < 7; i++) {
						myFloorHistory[i] = myFloorHistory[i - 1];
						myStepHistory[i] = myStepHistory[i - 1];
						buddyFloorHistory[i] = buddyFloorHistory[i - 1];
						buddyStepHistory[i] = buddyStepHistory[i - 1];
					}
					myFloorHistory[0] = 0;
					myStepHistory[0] = 0;
					buddyFloorHistory[0] = 0;
					buddyStepHistory[0] = 0;

					GregorianCalendar gc = new GregorianCalendar();


					// Check if "the previous day" is valid with respect to game
					// acceptance date
					int dayBefore = gc.get(Calendar.DAY_OF_YEAR);
					gc.roll(Calendar.DAY_OF_YEAR, -1);

					int dayAfter = gc.get(Calendar.DAY_OF_YEAR);
					if (dayAfter > dayBefore) {
						gc.roll(Calendar.YEAR, -1);
					}
					gc.get(Calendar.DATE);

					java.util.Date previousDay = gc.getTime();
					ImageView previousBtn = (ImageView) findViewById(R.id.previousLabel);

					if (Constants.validDate(prefs, previousDay)) {
						previousBtn.setImageDrawable(getResources()
								.getDrawable(R.drawable.previousicon));
					}
					
					
				}

				
				if (doHistoryCheck == 2) {
					for (int i = 0; i < 7; i++) {
						automaticUpdate(i, false);
					}

					doHistoryCheck = 0;
				}

				doHistoryCheck++;
			}

			if (!connectionErrorReceived) {
				checkProgressHandler.postDelayed(this,
						Constants.BADGE_CHECK_INTERVAL);
			}
		}
	};
	
	

	public void automaticUpdate(final int pivotIndex,
			final boolean updateTheDisplay) {
		//SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
		SimpleDateFormat datef = new SimpleDateFormat("yyyy-MM-dd");
		GregorianCalendar gc = new GregorianCalendar();

		Date d = new Date();
/*		String dayOfTheWeek = sdf.format(d);
		String dateString = datef.format(d);*/

		gc.setTime(d);
		int dayBefore = gc.get(Calendar.DAY_OF_YEAR);
		gc.roll(Calendar.DAY_OF_YEAR, -pivotIndex);

		int dayAfter = gc.get(Calendar.DAY_OF_YEAR);
		if (dayAfter > dayBefore) {
			gc.roll(Calendar.YEAR, -pivotIndex);
		}
		gc.get(Calendar.DATE);
		java.util.Date yesterday = gc.getTime();
		SharedPreferences prefs = getSharedPreferences(
				Constants.PROPERTIES_NAME, Context.MODE_PRIVATE);
		if (!Constants.validDate(prefs, yesterday)) {
			// invalid time, so do not update
			return;
		}

		final String dateOfInterest = datef.format(yesterday);
		final int uid = AppContext.getInstance().getUserId();
		final int fid = getFriendId();// AppContext.getInstance().getFriendId();

		SyncBackendWithFitbitTask syncOwnTask = new SyncBackendWithFitbitTask(
				uid, fid, dateOfInterest) {

			@Override
			protected void onPostExecute(Boolean success) {

				if (success
						&& Utils.isConnectionPresent(MainActivityCommunity.this)) {
					if (updateTheDisplay) {
						updateDisplay(pivotIndex == 0);
					}

					// also fetch latest buddy's steps
					SyncBackendWithFitbitTask syncBuddyTask = new SyncBackendWithFitbitTask(
							fid, uid, dateOfInterest) {

						@Override
						protected void onPostExecute(Boolean success) {
							if (success
									&& Utils.isConnectionPresent(MainActivityCommunity.this)) {
								// continue

								if (updateTheDisplay) {
									updateDisplay(pivotIndex == 0);
								}
								// write the updates

								// os.setText(osStr);
								// of.setText(ofStr);
							} else if (!Utils
									.isConnectionPresent(MainActivityCommunity.this)) {
								showSyncFailedDialog();
							}
						}
					};
					syncBuddyTask.execute();
				} else if (Utils
						.isConnectionPresent(MainActivityCommunity.this)) {
					// There is a problem going on with the fitbit
					// authentication. Prompt the user!
					showAuthorizationDialog(); // TODO: is this a good place??
				} else {
					showSyncFailedDialog();
				}
			}
		};
		syncOwnTask.execute();
	}

	// TODO: make sure this function is robust when the Internet connection is lost.
	private int getFriendId() {
		final ArrayList<Boolean> dirtyTrick = new ArrayList<Boolean>();
		int fid = AppContext.getInstance().getFriendId();
		
		if (fid < 1) {

			String email = AppContext.getInstance().getEmail();
			CheckGroupTask checkGroupTask = new CheckGroupTask(email,
					AppContext.getInstance().getUserId()) {
				@Override
				protected void onPostExecute(String result) {
					
					if(!this.safeExecution)
					{
						if(!Utils.isConnectionPresent(MainActivityCommunity.this))
						{
							displayConnectionErrorDialog();
						}
						return;
					}	
					
					// Log.d(TAG, "response: " + result);
					if (Utils.isInteger(result)) {
						int resultCode = Integer.parseInt(result);
						//Intent i;
						switch (resultCode) {
						case RESPONSE_CODE_NO_BUDDY:
							break;
						case RESPONSE_INCOMING_REQUEST_PENDING:
							break;
						case RESPONSE_WAITING_FOR_ACCEPT:
							break;
						default:
							AppContext.getInstance().setFriendId(resultCode);
							/*Log.d(TAG,
									"call showBuddyName from getFriendId - checkgrouptask");*/
							showBuddyName(resultCode);
							break;
						}
					} else {

					}
					dirtyTrick.add(true);
				}
			};
			checkGroupTask.execute();

			/*
			 * while(dirtyTrick.isEmpty()) {
			 * 
			 * }
			 */

			fid = AppContext.getInstance().getFriendId();
			return fid;

		} else {
			return fid;
		}
	}

	public void refreshLeaderboard(final int type, boolean guard)
	{
		Log.d(TAG,"entered into: refreshLeaderboard(" + type + ")");
		showProgressDialog(true);
		
		int m;
		if(type == 1)
		{
			m = Constants.leaderboardGroupsFloor.size();
		}
		else
		{
			m = Constants.leaderboardGroups.size();
		}
		
		Log.d(TAG,"The m-value is: " + m);
		if(m > 1)
		{
			refreshLeaderboardDisplay(type,guard);
		}
		/*...
		int multiplier = Constants.leaderboardGroups.size();
		
		if(multiplier > Constants.MAXIMUM_LEADERBOARD_ENTRY)
		{
			multiplier = Constants.MAXIMUM_LEADERBOARD_ENTRY;
		}
		
		int designatedHeight = dpToPx(dpOffset*(multiplier+1));
		
		listView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, designatedHeight));
		listView.invalidate();
		LinearLayout whole=(LinearLayout) (findViewById(R.id.LinearLayout1));	*/	
		
		final int uid = AppContext.getInstance().getUserId();
		
		final int fid = AppContext.getInstance().getFriendId();
		
		Log.e(TAG,"fid = " + fid);
		
		//final int fid = getFriendId(); // AppContext.getInstance().getFriendId();
		showBuddyName(fid);
		/*
		 * Log.d(TAG, "call showBuddyName from updateBadgesWithSync");
		 * showBuddyName(fid);
		 * 
		 * if(datePivot != 0) // the user is currently viewing a past day. {
		 * automaticUpdate(datePivot,true); }
		 */

		// First we check if we can synchronize the backend with the fitbit
		// server
		SyncBackendWithFitbitTask syncOwnTask = new SyncBackendWithFitbitTask(
				uid, fid) {

			@Override
			protected void onPreExecute() {
				//Log.d(TAG, "call showBuddyName from updateBadgesWithSync");
		//		showBuddyName(fid);
		//		updateDisplay(datePivot == 0);
		/*		if (datePivot != 0) // the user is currently viewing a past day.
				{
					automaticUpdate(datePivot, true);
				}*/
				showProgressDialog(true);
				/*if (isManual) {
			
				}*/
				super.onPreExecute();
			}

			@Override
			protected void onPostExecute(Boolean success) {
				if (success && Utils.isConnectionPresent(MainActivityCommunity.this)) {
					// updateDisplay(datePivot == 0);
					// will slow down the process

					// also fetch latest buddy's steps

					SyncBackendWithFitbitTask syncBuddyTask = new SyncBackendWithFitbitTask(
							fid, uid) {

						@Override
						protected void onPostExecute(Boolean success) {

							if (success
									&& Utils.isConnectionPresent(MainActivityCommunity.this)) {
								// continue
							//	updatePledgeBar(0);
							//	updatePledgeBar(1);
								
								/*if(type == 0 && listView.getAdapter() != null)
								{
									showProgressDialog(true);
								}
								else if(type == 1 && listViewFloor.getAdapter() != null)
								{
									showProgressDialog(true);
								}	*/							
								downloadLeaderboard(type);
								
							//	updateDisplay(datePivot == 0);
							} else if (Utils.isConnectionPresent(MainActivityCommunity.this)) {
								// There is a problem going on with the fitbit
								// authentication. Prompt the user!
								showProgressDialog(false);
								checkFitbitAuthentication();
							} else {
								showProgressDialog(false);
								// Notify the user that sync failed and give him
								// the possibility to manually authorize
								showSyncFailedDialog();
							}
						}
					};
					syncBuddyTask.execute();

					/*
					 * if(isManual) { try { syncBuddyTask.get(); } catch
					 * (InterruptedException e) {
					 * e.printStackTrace(); } catch (ExecutionException e)
					 * { e.printStackTrace();
					 * } }
					 */
				} else if (!Utils.isConnectionPresent(MainActivityCommunity.this)) {
					showSyncFailedDialog();
				}
				else
				{
					showSyncFailedDialog();
				}
			}
		};

		syncOwnTask.execute();		
	}
	
	private void downloadLeaderboard(final int type) 
	{
		Log.d(TAG,"entered into: downloadLeaderboard(" + type + ")");
		checkUserName();
		int userId = AppContext.getInstance().getUserId();

		// checkProgressHandler.postDelayed(checkForProgressTask, 0); //
		// we immediately check for new messages

		String email = AppContext.getInstance().getEmail();
		CheckGroupTask checkGroupTask = new CheckGroupTask(email,
				userId) {
			
			
			@Override
			protected void onPostExecute(String result) {
				
				if(!this.safeExecution)
				{
					if(!Utils.isConnectionPresent(MainActivityCommunity.this))
					{
						displayConnectionErrorMessage();
					}
					return;
				}
				
				if (Utils.isInteger(result)) {
					int resultCode = Integer.parseInt(result);
					//Intent i;
					switch (resultCode) {
					case RESPONSE_CODE_NO_BUDDY:
						// show screen to invite buddy
						Utils.scheduleAlarmReceiver(MainActivityCommunity.this);
						String msg = "";
						if (Constants.INVITATION_RECEIVED) {
							Constants.INVITATION_RECEIVED = false;
							msg = getResources().getString(R.string.buddy_stopped_playing);//"Your buddy has stopped playing with you.";
						} else if (AppContext.getInstance()
								.getFriendId() > 0) {
							Constants.INVITATION_RECEIVED = false;
							msg = getResources().getString(R.string.buddy_stopped_playing);
						} else if (Constants.INVITATION_CANCELED) {
							Constants.INVITATION_CANCELED = false;
							msg = getResources().getString(R.string.nothing_left);
						} else {
							msg = getResources().getString(R.string.main_no_buddy_warning);//"You do not have a buddy to play with. Please invite a person to continue playing.";
						}

						// New message
						displayBuddyChangeDialog(msg);

						break;
					case RESPONSE_INCOMING_REQUEST_PENDING:
						showBuddyName(AppContext.getInstance()
								.getFriendId());
						RetrieveBuddyEMailTask2 t2 = new RetrieveBuddyEMailTask2(
								AppContext.getInstance().getEmail()) {

							@Override
							protected void onPostExecute(String result) {
								
								if(!this.safeExecution)
								{
									if(!Utils.isConnectionPresent(MainActivityCommunity.this))
									{
										displayConnectionErrorMessage();
									}
									return;
								}
								
								final ArrayList<Boolean> hasFriend = new ArrayList<Boolean>();
								final String myResult = result;
								if (result.contains("not found")) {
									/*Log.i(TAG,
											"The user does not have any buddy.");*/
								} else {
									hasFriend.add(true);
								}

								RetrieveBuddyEMailTask task = new RetrieveBuddyEMailTask(
										AppContext.getInstance()
												.getEmail()) {

									@Override
									protected void onPostExecute(
											String buddyEmail) {
										
										if(!this.safeExecution)
										{
											if(!Utils.isConnectionPresent(MainActivityCommunity.this))
											{
												displayConnectionErrorMessage();
											}
											return;
										}
										
										if (buddyEmail.contains("@@")) 
										{
											ArrayList<String> buddyEmails = new ArrayList<String>();
											
											// Form a list of
											// invitations
											String stringBody = buddyEmail;
											String notifierEmail = "";
											int delim = stringBody.indexOf("@@");
											//ArrayList<String> buddyEmails = new ArrayList<String>();
											while (delim > 0) 
											{
												notifierEmail = stringBody.substring(0, delim);
												if (!hasFriend.isEmpty() && notifierEmail.equals(myResult)) 
												{
													// do not add.
												} 
												else 
												{
													buddyEmails.add(notifierEmail);
												}
												stringBody = stringBody.substring(delim + 2);
												delim = stringBody.indexOf("@@");
											}

											if (stringBody.length() > 0) 
											{
												if (!hasFriend.isEmpty() && stringBody.equals(myResult)) 
												{
													// continue;
												} 
												else 
												{
													buddyEmails.add(stringBody);
												}
											}	
											
											if (buddyEmails.size() == 1) 
											{
												showPendingBuddyInvitationNotification(buddyEmails.get(0));
											} 
											else 
											{
												showMultiplePendingBuddyInvitationNotification(buddyEmails);
											}											
											
											/*
											if (hasFriend.isEmpty()) {
												// Perform
												// multiple-buddy
												// notification.
												showMultiplePendingBuddyInvitationNotification();
											} else {
												// Form a list of
												// invitations
												String stringBody = buddyEmail;
												String notifierEmail = "";
												int delim = stringBody
														.indexOf("@@");
												ArrayList<String> buddyEmails = new ArrayList<String>();
												while (delim > 0) {
													notifierEmail = stringBody
															.substring(
																	0,
																	delim);
													if (notifierEmail
															.equals(myResult)) {
														// continue;
													} else {
														buddyEmails
																.add(notifierEmail);

													}
													stringBody = stringBody
															.substring(delim + 2);
													delim = stringBody
															.indexOf("@@");
												}

												if (stringBody.length() > 0) {
													if (stringBody
															.equals(myResult)) {
														// continue;
													} else {
														buddyEmails
																.add(stringBody);
													}
												}

												if (buddyEmails.size() == 1) {
													showPendingBuddyInvitationNotification(buddyEmails
															.get(0));
												} else {
													showMultiplePendingBuddyInvitationNotification();
												}
											}*/
										} else {
											showPendingBuddyInvitationNotification(buddyEmail);
										}
										showBuddyName(AppContext
												.getInstance()
												.getFriendId());
										refreshAfterSanityCheck(type);
									}
								};
								task.execute();
							}
						};
						t2.execute();
						showBuddyName(AppContext.getInstance()
								.getFriendId());
						break;
					case RESPONSE_WAITING_FOR_ACCEPT:
						break;
					default:
						AppContext.getInstance()
								.setFriendId(resultCode);
						showBuddyName(resultCode);

						checkStartDate();
						refreshAfterSanityCheck(type);
						break;
					}
				} else {
					// error log_tag
					// Log.e(TAG, "an error has occurred");
					// Toast.makeText(Main2Activity.this,
					// R.string.error_general,
					// Toast.LENGTH_LONG).show();
				}
			}

		};
		checkGroupTask.execute();
		
	}
	private void refreshAfterSanityCheck(final int type)
	{
		final int myUid = AppContext.getInstance().getUserId();
		
		Log.d(TAG,"entered into: refreshAfterSanityCheck(" + type + ")");
		
		if(type == 0){
		
			lastRefStepL=Calendar.getInstance().getTime();
			// Change button face to the avatar
			GetAvatarTask task = new GetAvatarTask(myUid) 
			{
				
				protected void onPreExecute()
				{
					super.onPreExecute();
				}

				@Override
				protected void onPostExecute(Integer result) {
					
					// change the button
					if(!this.safeExecution || !Utils.isConnectionPresent(MainActivityCommunity.this))
					{
						
						//displayConnectionErrorDialog(type);
						displayConnectionErrorMessage();
						return;
					}
					
					
					ImageButton btn = (ImageButton) findViewById(R.id.imageButtonFace);
					
						
					btn.setBackgroundResource(Constants.getAvatarNum(result));

					if(result > 0)
					{
						TextView tv = (TextView) findViewById(R.id.textViewFace);
						tv.setVisibility(TextView.INVISIBLE);
						btn.setVisibility(ImageButton.INVISIBLE);
					}
					else
					{
						TextView tv = (TextView) findViewById(R.id.textViewFace);
						tv.setVisibility(TextView.VISIBLE);
						btn.setVisibility(ImageButton.VISIBLE);						
					}
					
					for (int i = 0; i < Constants.allGroups.size(); i++)
						if (Constants.allGroups.get(i).mem[0].getUid() == myUid
								|| Constants.allGroups.get(i).mem[1].getUid() == myUid)
							Constants.allGroups.get(i).icon = result;

					// reset all groups
					if(listView.getAdapter() == null)
					{
						showProgressDialog(true);
						for (int i = 0; i < Constants.team_data.length; i++) {
							Constants.team_data[i].score = "Loading...";//getResources().getString(R.string.main_loading);
							Constants.team_data[i].icon = 0;
							Constants.team_data[i].name = "";
						}
					}
					/*listView.setAdapter(leaderboard);
					listView.setOnItemClickListener(new OnItemClickListener() {
						@Override
						public void onItemClick(AdapterView<?> arg0, View arg1,
								int position, long arg3) {
							if (!leaderboard.myLock) {
								int ind = position - 1;
								if (ind < Constants.leaderboardGroups.size()) {
									String first = Constants.leaderboardGroups
											.get(ind).mem[0].getUserName();
									String second = Constants.leaderboardGroups
											.get(ind).mem[1].getUserName();
									Toast.makeText(MainActivityCommunity.this,
											getResources().getString(R.string.team_of) + " " + first +  " " + getResources().getString(R.string.and) + " " + second,
											Toast.LENGTH_SHORT).show();
								}
							}
						}
					});*/
					//leaderboard.clear();
					leaderboard.update();
					
				}
			};
			task.execute();
		}
		else
		{
			lastRefFloorL=Calendar.getInstance().getTime();
			// Change button face to the avatar
			GetAvatarTask taskFloor = new GetAvatarTask(myUid) {

				protected void onPreExecute()
				{
					super.onPreExecute();
				}				
				
				@Override
				protected void onPostExecute(Integer result) {
					
					if(!this.safeExecution || !Utils.isConnectionPresent(MainActivityCommunity.this))
					{
						//displayConnectionErrorDialog(type);	
						displayConnectionErrorMessage();
						return;
					}
					
					// change the button
					ImageButton btnFloor = (ImageButton) findViewById(R.id.imageButtonFaceFloors);
					btnFloor.setBackgroundResource(Constants.getAvatarNum(result));
					
					//ImageButton btn = (ImageButton) findViewById(R.id.imageButtonFace);
					
					
					//btn.setBackgroundResource(Constants.getAvatarNum(result));

					if(result > 0)
					{
						TextView tv = (TextView) findViewById(R.id.textViewFaceFloors);
						tv.setVisibility(TextView.INVISIBLE);
						btnFloor.setVisibility(ImageButton.INVISIBLE);
					}
					else
					{
						TextView tv = (TextView) findViewById(R.id.textViewFaceFloors);
						tv.setVisibility(TextView.VISIBLE);
						btnFloor.setVisibility(ImageButton.VISIBLE);					
					}

					for (int i = 0; i < Constants.allGroups.size(); i++)
						if (Constants.allGroups.get(i).mem[0].getUid() == myUid
								|| Constants.allGroups.get(i).mem[1].getUid() == myUid)
							Constants.allGroups.get(i).icon = result;

					// reset all groups
					if(listViewFloor.getAdapter() == null)
					{
						for (int i = 0; i < Constants.team_data_floor.length; i++) {
							Constants.team_data_floor[i].score = "Loading...";//getResources().getString(R.string.main_loading);
							Constants.team_data_floor[i].icon = 0;
							Constants.team_data_floor[i].name = "";
						}
					}
					/*listViewFloor.setAdapter(leaderboardFloor);
					listViewFloor.setOnItemClickListener(new OnItemClickListener() {
						@Override
						public void onItemClick(AdapterView<?> arg0, View arg1,
								int position, long arg3) {
							if (!leaderboardFloor.myLock) {
								int ind = position - 1;
								if (ind < Constants.leaderboardGroupsFloor.size()) {
									String first = Constants.leaderboardGroupsFloor
											.get(ind).mem[0].getUserName();
									String second = Constants.leaderboardGroupsFloor
											.get(ind).mem[1].getUserName();
									Toast.makeText(MainActivityCommunity.this,
											getResources().getString(R.string.team_of) + " " + first +  " " + getResources().getString(R.string.and) + " " + second,
											Toast.LENGTH_SHORT).show();
								}
							}
						}
					});*/
					// listView.invalidate();
					// LinearLayout whole=(LinearLayout)
					// findViewById(R.id.LinearLayout1);
					// whole.invalidate();
					//leaderboardFloor.clear();
					leaderboardFloor.update();
				}
			};
			taskFloor.execute();
		}
	/*	default:
				System.out.println("Error in leaderboard mode");
		}*/
		
	}

	private boolean time2refresh(Date inp){
		if(inp==null)
			return true;
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date now=Calendar.getInstance().getTime();
		
		String oldDate = df.format(now);
		String newDate = df.format(inp);
		
		if(!newDate.substring(0,13).equals(oldDate.substring(0, 13)))
			return true;
		int oldMin=Integer.parseInt(oldDate.substring(14, 16));
		int newMin=Integer.parseInt(newDate.substring(14, 16));
		return (newMin-oldMin >5); 
	}
	
	
	private void initializeLeaderboard(int type){
		switch(type){
		case 0:
			listView = (ListView) findViewById(R.id.leaderboard);

			View header = (View) getLayoutInflater().inflate(
					R.layout.leaderboard_listview_hearder_row, null);
			listView.addHeaderView(header);

			listView.setAdapter(leaderboard);
			break;
		case 1:
			listViewFloor = (ListView) findViewById(R.id.leaderboardFloor);

			View headerFloor = (View) getLayoutInflater().inflate(
					R.layout.leaderboard_listview_hearder_row, null);
			listViewFloor.addHeaderView(headerFloor);

			listViewFloor.setAdapter(leaderboardFloor);
			break;
		default:
				System.out.println("Error in leaderboard mode");
		}
		
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		
		isRefreshed[0] = false;
		isRefreshed[1] = false;
		//creationComplete = false;
		isActive = true;
		checkStartDate();

		setContentView(R.layout.activity_main_community);

		// ASMA START LEADERBOARD

		ArrayList<Team> customTeam = new ArrayList<Team>();
		ArrayList<Team> customTeamFloor = new ArrayList<Team>();
		
		Log.i(TAG, "Step entries, as constants noted: " + Constants.CURRENT_STEPS_LEADERBOARD_ENTRY);
		Log.i(TAG, "Floor entries, as constants noted: " + Constants.CURRENT_FLOORS_LEADERBOARD_ENTRY);
		
		if (true)//leaderboard == null)
		{
			//Toast.makeText(this, "initializing leaderboard layout", Toast.LENGTH_SHORT).show();
			customTeam.clear();
			
			if(leaderboard != null)
			{
				Log.i(TAG,"Chose to add what is existent in leaderboard data");
				
				for(int i = 0; i < Constants.CURRENT_STEPS_LEADERBOARD_ENTRY; i++)
				{
					Log.i(TAG,"Steps: added item no. " + i);
					customTeam.add(Constants.team_data[i]);
				}	
				/*Constants.team_data[0] = new Team(0, "Loading...","");
				customTeam.add(Constants.team_data[0]);*/	
				/*for(int i = 0; i < leaderboard.getCount(); i++)
				{
					customTeam.add(Constants.team_data[i]); // leaderboard.getItem(i));//
				}*/
			}
			else if(Constants.leaderboardGroups.size() > 0)
			{
				Log.i(TAG,"Chose to add what is existent in leaderboardGroups data");
				for(int i = 0; i < Constants.leaderboardGroups.size(); i++)
				{
					customTeam.add(Constants.team_data[i]);
				}
					
			}
			else if( Constants.CURRENT_STEPS_LEADERBOARD_ENTRY > 0)
			{
				Log.i(TAG,"Chose to add what with respect to CURRENT_STEPS_LEADERBOARD_ENTRY");
				for(int i = 0; i < Constants.CURRENT_STEPS_LEADERBOARD_ENTRY; i++)
				{
					customTeam.add(Constants.team_data[i]);
				}				
			}
			else
			{
				Log.i(TAG,"Chose to add an empty step data");
				//customTeam.add(new Team(0, "Loading...",""));//Constants.team_data[0]);
				Constants.team_data[0] = new Team(0, "Loading...","");
				customTeam.add(Constants.team_data[0]);				
				
			}
			
			/*leaderboard = new Leaderboard(0,this,
					R.layout.leaderboard_listview_item_row, Constants.team_data);*/
			leaderboard = new Leaderboard(0,this,
					R.layout.leaderboard_listview_item_row, customTeam);			
		}
		if (true)//leaderboardFloor == null)
		{
			//customTeam.add(Constants.team_data_floor[0]);
			customTeamFloor.clear();
			if(leaderboardFloor != null)
			{
				Log.i(TAG,"Chose to add what is existent in leaderboardFloor data");
				for(int i = 0; i < Constants.CURRENT_FLOORS_LEADERBOARD_ENTRY; i++)
				{
					Log.i(TAG,"Floors: added item no. " + i);
					customTeamFloor.add(Constants.team_data_floor[i]);
				}						
			/*	Constants.team_data_floor[0] = new Team(0, "Loading...","");
				customTeamFloor.add(Constants.team_data_floor[0]);*/				
			/*	for(int i = 0; i < leaderboardFloor.getCount(); i++)
				{
					customTeamFloor.add(Constants.team_data_floor[i]); // leaderboardFloor.getItem(i));//
				}*/
			}
			else if(Constants.leaderboardGroupsFloor.size() > 0)
			{
				Log.i(TAG,"Chose to add what is existent in leaderboardGroupsFloor data");
				for(int i = 0; i < Constants.leaderboardGroupsFloor.size(); i++)
				{
					customTeamFloor.add(Constants.team_data_floor[i]);
				}
					
			}
			else if( Constants.CURRENT_FLOORS_LEADERBOARD_ENTRY > 0)
			{
				Log.i(TAG,"Chose to add what with respect to CURRENT_FLOORS_LEADERBOARD_ENTRY");
				for(int i = 0; i < Constants.CURRENT_FLOORS_LEADERBOARD_ENTRY; i++)
				{
					customTeamFloor.add(Constants.team_data_floor[i]);
				}				
			}			
			else
			{
				Log.i(TAG,"Chose to add an empty floor data");
				//customTeam.add(Constants.team_data_floor[0]);
				Constants.team_data_floor[0] = new Team(0, "Loading...","");
				customTeamFloor.add(Constants.team_data_floor[0]);
				//customTeam.add(new Team(0, "Loading...",""));
			}			
			/*leaderboardFloor = new Leaderboard(1,this,
					R.layout.leaderboard_listview_item_row, Constants.team_data_floor);*/
			leaderboardFloor = new Leaderboard(1,this,
					R.layout.leaderboard_listview_item_row, customTeamFloor);
		}
		
		
		if (Utils.isConnectionPresent(MainActivityCommunity.this)) {
			//showProgressDialog(true);
			displayConnectionErrorDialog(false);
		} else {
			displayConnectionErrorDialog();
		}		
		
		final SharedPreferences prefs = getSharedPreferences(
				Constants.PROPERTIES_NAME, Context.MODE_PRIVATE);


		initializeLeaderboard(0);
		initializeLeaderboard(1);
		
		
		GetListOfUsersTask listOfUsers = new GetListOfUsersTask() {

			@Override
			protected void onPostExecute(Boolean result) {
				
				
				if(this.safeExecution)
				{
					lastRefStepL=Calendar.getInstance().getTime();
					
					//showProgressDialog(true);
					
					//showProgressDialog(true);
					refreshLeaderboard(Constants.VIEW_STEPS ? 0 : 1,false);
					
					/*
					if(isDifferentDay(prefs)) 
					{
						// must ensure that the user has created an entry
						showProgressDialog(true);
						refreshLeaderboard(Constants.VIEW_STEPS ? 0 : 1);
					}
					else
					{
						// just do the refresh without the entire story.
						downloadLeaderboard(Constants.VIEW_STEPS ? 0 : 1);
					}*/
				}
				else
				{
					if(!Utils.isConnectionPresent(MainActivityCommunity.this))
					{
						displayConnectionErrorMessage();
					}
				}

			}
		};
		listOfUsers.execute();
		

		// ASMA END LEADERBARD

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		if (connectionDialog != null) {
			connectionDialog.dismiss();
		}

		if (initialSyncDialog != null) {
			initialSyncDialog.dismiss();
		}

		if (Utils.isConnectionPresent(MainActivityCommunity.this)) {
			showProgressDialog(true);
			displayConnectionErrorDialog(false);
		} else {
			displayConnectionErrorDialog();
		}

		tab_host = (TabHost) findViewById(R.id.edit_item_tab_host);
		tab_host.setup();
		tab_host.getTabWidget();// .setDividerDrawable(R.drawable.tab_divider);

		// setupTab(new TextView(this), "Steps");

		View tabview = createTabView(tab_host.getContext(), getResources().getString(R.string.steps_tab));

		TabSpec setContent = tab_host.newTabSpec(getResources().getString(R.string.steps_tab)).setIndicator(tabview)
				.setContent(R.id.show_step_tab);
		tab_host.addTab(setContent);

		View tabview2 = createTabView(tab_host.getContext(), getResources().getString(R.string.floors_tab));
		TabSpec setContent2 = tab_host.newTabSpec(getResources().getString(R.string.floors_tab))
				.setIndicator(tabview2).setContent(R.id.show_floor_tab);
		tab_host.addTab(setContent2);
		View cv;
		for (int i = 0; i < tab_host.getTabWidget().getChildCount(); i++) {

			if (i == 1) {
				// Log.d(TAG,"ONUR, TABS " + i +
				// ": appyling  mytab_roundedcorners2");
				tab_host.getTabWidget()
						.getChildAt(i)
						.setBackgroundResource(R.drawable.mytab_roundedcorners2);
			} else if (i == 0) {
				// Log.d(TAG,"ONUR, TABS " + i +
				// ": appyling  mytab_roundedcorners");
				tab_host.getTabWidget().getChildAt(i)
						.setBackgroundResource(R.drawable.mytab_roundedcorners);
			}

			cv = tab_host.getTabWidget().getChildAt(i);
			LinearLayout.LayoutParams currentLayout = (LinearLayout.LayoutParams) cv
					.getLayoutParams();
			currentLayout.setMargins(0, 2, 2, 0);
		}

		//ASMA START TAB CHANGED
		tab_host.setOnTabChangedListener(new OnTabChangeListener(){
			@Override
			public void onTabChanged(String tabId) {
			    if(tabId.equals("Steps")|| tabId.equals(getResources().getString(R.string.steps_tab))) {
			    	if(time2refresh(lastRefStepL))
			    		Constants.VIEW_STEPS = true;
			    		if(!isRefreshed[0])
			    		{
			    			isRefreshed[0] = true;
			    			refreshLeaderboard(0,false);
			    		}
			    }
			    if(tabId.equals("Floors") || tabId.equals(getResources().getString(R.string.floors_tab))) {
			    	if(time2refresh(lastRefFloorL))
			    		Constants.VIEW_STEPS = false;
			    		if(!isRefreshed[1])
			    		{
			    			isRefreshed[1] = true;
			    			refreshLeaderboard(1,false);
			    		}
			    }
			}});
		//ASMA END TAB CHANGED
		
		tab_host.setCurrentTab(Constants.VIEW_STEPS ? 0 : 1);
		//int currentTab = tab_host.getCurrentTab();
		
		//refreshLeaderboard(currentTab);
		datePivot = 0;

		/*SharedPreferences prefs = getSharedPreferences(
				Constants.PROPERTIES_NAME, Context.MODE_PRIVATE);*/
		// updateBadgesWithSync(false); 

		// SharedPreferences prefs =
		// getSharedPreferences(Constants.PROPERTIES_NAME,
		// Context.MODE_PRIVATE);
		for (int i = 0; i < 7; i++) {

			myStepHistory[i] = prefs.getInt(
					Constants.PROP_KEY_USER_HISTORY_COMPLETED_STEPS + "_" + i,
					0);
			myFloorHistory[i] = prefs.getInt(
					Constants.PROP_KEY_USER_HISTORY_COMPLETED_FLOORS + "_" + i,
					0);
			buddyStepHistory[i] = prefs.getInt(
					Constants.PROP_KEY_BUDDY_HISTORY_COMPLETED_STEPS + "_" + i,
					0);
			buddyFloorHistory[i] = prefs
					.getInt(Constants.PROP_KEY_BUDDY_HISTORY_COMPLETED_FLOORS
							+ "_" + i, 0);
		}

		refreshDone = false;
		Constants.loggedIn = true;
		checkProgressHandler = new Handler();

		/*
		 * Intent intent = getIntent(); previousActivityIsMessagesActivity =
		 * intent.getBooleanExtra("fromLogin", false) || Constants.newLogin;
		 * 
		 * String msg = intent.getStringExtra("message");
		 * 
		 * if(msg != null) { Toast.makeText(MainActivityCommunity.this, msg,
		 * Toast.LENGTH_LONG).show(); }
		 * 
		 * 
		 * checkUserName(); View tb = findViewById(R.id.topBarChart); tbwidth =
		 * tb.getWidth();
		 */

		checkValidityOfPreviousDay();
		//creationComplete = true;

	}

	public void checkValidityOfPreviousDay() {
		//SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
		//SimpleDateFormat datef = new SimpleDateFormat("yyyy-MM-dd");
		GregorianCalendar gc = new GregorianCalendar();

		//Date d = new Date();
		//String dayOfTheWeek = sdf.format(d);
		//String dateString = datef.format(d);

		// Check if "the previous day" is valid with respect to game acceptance
		// date
		int dayBefore = gc.get(Calendar.DAY_OF_YEAR);
		gc.roll(Calendar.DAY_OF_YEAR, -1);

		int dayAfter = gc.get(Calendar.DAY_OF_YEAR);
		if (dayAfter > dayBefore) {
			gc.roll(Calendar.YEAR, -1);
		}
		gc.get(Calendar.DATE);

		java.util.Date previousDay = gc.getTime();

		ImageView previousBtn = (ImageView) findViewById(R.id.previousLabel);

		SharedPreferences prefs2 = getSharedPreferences(
				Constants.PROPERTIES_NAME, Context.MODE_PRIVATE);

		if (!Constants.validDate(prefs2, previousDay)) {
			previousBtn.setImageDrawable(getResources().getDrawable(
					R.drawable.previousicon_nonclickable));
		}
	}

	private static View createTabView(final Context context, final String text) {
		View view = LayoutInflater.from(context)
				.inflate(R.layout.tabs_bg, null);
		TextView tv = (TextView) view.findViewById(R.id.tabsText);
		tv.setText(text);
		return view;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		isActive = false;
		isDisplayingConnectionErrorDialog = false;
		isDisplayingAuthorizationDialog = false;
		if (previousActivityIsMessagesActivity) {
			Intent intent = new Intent(MainActivityCommunity.this,
					MainActivityCommunity.class);
			intent.putExtra("fromLogin", true);
			startActivity(intent);
		}
	}

	protected void onPause() {
		super.onPause();
		isActive = false;
		isDisplayingConnectionErrorDialog = false;
		isDisplayingAuthorizationDialog = false;
		// need to stop the message poling loop
		// Log.e(TAG, "*** STOPPING MESSAGE POLLING");
		checkProgressHandler.removeCallbacks(checkForProgressTask);
	}

	@Override
	protected void onResume() {
		super.onResume();
		isActive = true;
		/*
		 * if(connectionDialog != null) { connectionDialog.dismiss(); }
		 */
		if(			(Constants.VIEW_STEPS && tab_host.getCurrentTab() != 0 )
				|| (!Constants.VIEW_STEPS && tab_host.getCurrentTab() != 1 ))
		{
			tab_host.setCurrentTab(Constants.VIEW_STEPS ? 0 : 1);
		}
		if (initialSyncDialog != null) {
			try
			{
				initialSyncDialog.dismiss();
			}
			catch(Exception e)
			{
				
			}
		}

		if (!Utils.isConnectionPresent(MainActivityCommunity.this)) {
			// Toast.makeText(Main2Activity.this,
			// "No internet connection. Please check your connection.",
			// Toast.LENGTH_LONG).show();
			displayConnectionErrorDialog();
			return;
		} else {
			isDisplayingConnectionErrorDialog = false;
			// Log.d(TAG,"showProgressDialog called from onResume().");
			//showProgressDialog(true);
		}

		//int userId = AppContext.getInstance().getUserId();

		checkProgressHandler.postDelayed(checkForProgressTask, 0); // we
																	// immediately
																	// check for
																	// new
																	// messages
		Constants.checkUserName();
		//String email = AppContext.getInstance().getEmail();
		refreshDone = false;

	}

	public static void checkUserName() {
		if (AppContext.getInstance().isUserCredentialsSet()) {
			if (AppContext.getInstance().getUserString() == null) {
				String str = AppContext.getInstance().getEmail();
				String[] s = str.split("@");
				AppContext.getInstance().setUserString(s[0]);
				// Log.d(TAG, "Set the user string to: " + s[0]);
			}
		}
	}

	public void setBuddyName(String username) {
		buddyName = username;
		Constants.buddyName = buddyName;
	}

	protected void showBuddyName(int friendId) 
	{
		if(Constants.BUDDY_NAME_ACQUIRED)
		{
			setBuddyName(Constants.buddyName);
		}
		Constants.BUDDY_NAME_ACQUIRED = false;
		//Log.d(TAG, "Main2Activity, friend Id: " + friendId);

		RetrieveExistingBuddyInfoTask task = new RetrieveExistingBuddyInfoTask(AppContext.getInstance().getUserId())
		{
			protected void onPostExecute(Boolean result)
			{
				
				if(result && this.safeExecution)
				{
					/*AppContext.getInstance().setFriendId(this.retrievedBuddyId);
					TextView text = (TextView) findViewById(R.id.textViewOtherSteps);
					text.setText(this.retrievedBuddyName);
					text = (TextView) findViewById(R.id.textViewOtherFloors);
					text.setText(this.retrievedBuddyName);
					setBuddyName(this.retrievedBuddyName);*/
					Constants.BUDDY_NAME_ACQUIRED = true;
				}
				else if(!Utils.isConnectionPresent(MainActivityCommunity.this))
				{
					displayConnectionErrorDialog();
				}
			}
		};
		task.execute();		
		
	}

	public void showProgressDialog(final boolean show) {
		if (!isDisplayingConnectionErrorDialog
				|| !isDisplayingAuthorizationDialog) {
			if (show) {

				/*if (isActive && !progressDialogShown) {
					initialSyncDialog = ProgressDialog.show(this,
							getResources().getString(R.string.leaderboard_synchronization_title),
							getResources().getString(R.string.leaderboard_synchronization_message));
					progressDialogShown  = true;
				}*/
			} else if (initialSyncDialog != null) {
				/*initialSyncDialog.dismiss();
				initialSyncDialog = null;
				progressDialogShown = false;*/
			}
		}
	}
	
	public void showManualProgressDialog(final boolean show) {
		if (!isDisplayingConnectionErrorDialog
				|| !isDisplayingAuthorizationDialog) {
			if (show) {

				if (isActive && !progressDialogShown) {
					initialSyncDialog = ProgressDialog.show(this,
							getResources().getString(R.string.leaderboard_synchronization_title),
							getResources().getString(R.string.leaderboard_synchronization_message));
					progressDialogShown  = true;
				}
			} else if (initialSyncDialog != null) {
				initialSyncDialog.dismiss();
				initialSyncDialog = null;
				progressDialogShown = false;
			}
		}
	}	
	

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		updateDisplay(datePivot == 0);
	}

	// ASMA START FUNCTIONS
	public void onGroupButtonClicked(View v) {

		returnToHome();
	}

	public void returnToHome()
	{
		Intent group = new Intent(this, Main2Activity.class);
		group.putExtra("updatePledges", true);
		startActivity(group);
	}
	
	public void onRefreshLeaderboardButtonClicked(View v) {
		showManualProgressDialog(true);
		refreshLeaderboard(0,true);

	}
	
	public void onRefreshLeaderboardButtonClickedFloor(View v) {
		showManualProgressDialog(true);
		refreshLeaderboard(1,true);

	}

	public void onChangeFaceButtonClicked(View v) 
	{
		changeMyAvatar();
	}
	
	public void changeMyAvatar()
	{
		GetAvatarTask task = new GetAvatarTask(AppContext.getInstance()
				.getUserId()) {

			@Override
			protected void onPostExecute(Integer result) 
			{
				
				if(!this.safeExecution || !Utils.isConnectionPresent(MainActivityCommunity.this))
				{
					//displayConnectionErrorDialog(2);
					displayConnectionErrorMessage();
					return;
				}
				
				if (result > 0) {
					Toast.makeText(MainActivityCommunity.this,
							getResources().getString(R.string.avatar_already_set),
							Toast.LENGTH_SHORT).show();
					ImageButton btn = tab_host.getCurrentTab() == 0 ? (ImageButton) findViewById(R.id.imageButtonFace) : (ImageButton) findViewById(R.id.imageButtonFaceFloors);
					TextView tv = tab_host.getCurrentTab() == 0 ? (TextView) findViewById(R.id.textViewFace) : (TextView) findViewById(R.id.textViewFaceFloors);
					btn.setVisibility(ImageButton.INVISIBLE);
					tv.setVisibility(TextView.INVISIBLE);
				} else {
					// 1. Instantiate an AlertDialog.Builder with its
					// constructor
					final AlertDialog.Builder builder = new AlertDialog.Builder(
							MainActivityCommunity.this);

					LayoutInflater inflater = getLayoutInflater();
					View viewHeader = inflater.inflate(R.layout.customtitle,
							null);
					TextView title = (TextView) viewHeader
							.findViewById(R.id.myTitle);
					title.setText(getResources().getString(R.string.choose_your_avatar));
					builder.setCustomTitle(viewHeader);

					View gridView = inflater.inflate(R.layout.grid_main, null);
					builder.setView(gridView);
					GridView grid = (GridView) gridView
							.findViewById(R.id.avatarGrid);
					Avatars avatars = new Avatars(MainActivityCommunity.this,
							R.layout.grid_row);
					grid.setAdapter(avatars);

					builder.setPositiveButton(getResources().getString(R.string.cancel_button),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dlg,
										int sumthin) {

								}
							});

					final AlertDialog dialog = builder.create();
					dialog.show();
					
					Button cancelB = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
					
					if(cancelB != null)
					{
						cancelB.setBackgroundDrawable(getResources().getDrawable(R.drawable.cancelbutton_gray));
						cancelB.setTextColor(Color.WHITE);
						cancelB.setTypeface(null, Typeface.BOLD);
						LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, dpToPx(38));
						lp.setMargins(dpToPx(10), dpToPx(5), dpToPx(10), dpToPx(15));
						cancelB.setLayoutParams(lp);
					/*	cancelB.setOnClickListener(new View.OnClickListener() {
							
							@Override
							public void onClick(View v) {
								
								Constants.dialog.cancel();
								
							}
						});*/
					}
					else
					{
						//Toast.makeText(this, "cancelBut", Toast.LENGTH_LONG).show();
					}

					grid.setOnItemClickListener(new OnItemClickListener() {
						public void onItemClick(AdapterView parent, View v,
								int position, long id) {
							final int myUid = AppContext.getInstance()
									.getUserId();
							final int avatarId = position;
							SetAvatarTask task = new SetAvatarTask(myUid,
									position) {

								
								@Override
								protected void onPostExecute(Boolean result) 
								{
									
									if(!this.safeExecution || !Utils.isConnectionPresent(MainActivityCommunity.this))
									{
										dialog.cancel();
										//displayConnectionErrorDialog(2);
										displayConnectionErrorMessage();
										return;
									}
									
									TextView tv = (TextView)findViewById(R.id.textViewFace);
									ImageButton btn = (ImageButton) findViewById(R.id.imageButtonFace);
									btn.setBackgroundResource(Constants
											.getAvatarNum(avatarId));
									btn.setVisibility(ImageButton.INVISIBLE);
									tv.setVisibility(TextView.INVISIBLE);
									
									tv = (TextView)findViewById(R.id.textViewFaceFloors);
									btn = (ImageButton) findViewById(R.id.imageButtonFaceFloors);
									btn.setBackgroundResource(Constants
											.getAvatarNum(avatarId));
									btn.setVisibility(ImageButton.INVISIBLE);
									tv.setVisibility(TextView.INVISIBLE);

									for (int i = 0; i < Constants.allGroups
											.size(); i++)
										if (Constants.allGroups.get(i).mem[0]
												.getUid() == myUid
												|| Constants.allGroups.get(i).mem[1]
														.getUid() == myUid)
											Constants.allGroups.get(i).icon = avatarId;

									for (int i = 0; i < Constants.leaderboardGroups
											.size(); i++)
										if (Constants.leaderboardGroups.get(i).mem[0]
												.getUid() == myUid
												|| Constants.leaderboardGroups
														.get(i).mem[1].getUid() == myUid) {
											Constants.leaderboardGroups.get(i).icon = avatarId;
											Constants.team_data[i].icon = avatarId;
											if (avatarId != 0)
												Constants.team_data[i].name = "";
										}

									if (!leaderboard.myLock)
									{
										leaderboard.notifyDataSetChanged();
									}

								}
							};
							task.execute();
							// ImageButton btn= (ImageButton)
							// findViewById(R.id.imageButtonFace);
							// btn.setBackgroundResource(Constants.getAvatarNum(position));
							dialog.cancel();
						}
					});

				}

			}
		};
		task.execute();		
	}
	

	// ASMA END FUNCTIONS

	public void onTauntButtonClicked(View v) {
		Intent i = new Intent(this, MessagesActivity.class);
		i.putExtra("cheer", false);
		startActivity(i);
	}

	public void onCheerButtonClicked(View v) {
		Intent i = new Intent(this, MessagesActivity.class);
		i.putExtra("cheer", true);
		startActivity(i);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_maincommunity, menu);
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
			// AppContext.getInstance().setUserCredentialsSet(false);
			Intent i_settings = new Intent(this, SettingsActivity.class);
			// Intent i_auth= new Intent(this, LoginActivity.class);
			// Intent i_auth= new Intent(this,
			// AuthenticateFitbitActivity.class);
			Constants.MAIN_OR_COMMUNITY = true; 
			startActivity(i_settings);
			return true;

		case R.id.menu_sync_data:

			// log_tag
			if (Utils.isConnectionPresent(MainActivityCommunity.this)) {
				// Log.d(TAG, "Progress dialog called from manual sync.");
				
				int currentTab = tab_host.getCurrentTab();
				isDisplayingConnectionErrorDialog = false;
				showManualProgressDialog(true);
				refreshLeaderboard(currentTab,true);

			} else {
				// Log.d(TAG,
				// "Manual sync can not be done due to connection error.");
				displayConnectionErrorDialog();
			}

			return true;
	 	case R.id.menu_settings_logout:
			SharedPreferences prefs2 = getSharedPreferences(
					Constants.PROPERTIES_NAME, MODE_PRIVATE);
	 		return Utils.logout(this,prefs2);				
			/*
			 * DialogInterface.OnClickListener dialogClickListener = new
			 * DialogInterface.OnClickListener() {
			 * 
			 * @Override public void onClick(DialogInterface dialog, int which)
			 * { switch (which){ case DialogInterface.BUTTON_POSITIVE: //Yes
			 * button clicked deFriend();
			 * 
			 * break;
			 * 
			 * case DialogInterface.BUTTON_NEGATIVE: //No button clicked break;
			 * } } };
			 * 
			 * AlertDialog.Builder builder = new AlertDialog.Builder(this);
			 * builder
			 * .setMessage("Are you sure about changing a buddy?").setPositiveButton
			 * ("Yes", dialogClickListener) .setNegativeButton("No",
			 * dialogClickListener).show();
			 */

		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) 
		{
		//	moveTaskToBack(true);
		//	Intent i_main = new Intent(MainActivityCommunity.this,Main2Activity.class);
		//	startActivity(i_main);
			
			returnToHome();
			
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	public void onRefreshButtonClicked(View v) {
		if (Utils.isConnectionPresent(this)) {
			boolean validRefresh = false;
			Calendar c = Calendar.getInstance();

			long currentMillis = c.getTimeInMillis();

			if (!refreshDone) {
				validRefresh = true;
				refreshDone = true;
			} else if (Math.abs(lastRefreshMillis - currentMillis) < Constants.BADGE_CHECK_INTERVAL) {
				validRefresh = false;
			}

			if (validRefresh) {
				// updateBadgesWithSync(true);
				lastRefreshMillis = currentMillis;
			} else {
				Toast.makeText(MainActivityCommunity.this,
						getResources().getString(R.string.statistics_up_to_date), Toast.LENGTH_SHORT)
						.show();
			}

		} else {
			showSyncFailedDialog();
			// Toast.makeText(Main2Activity.this,
			// "Error: No internet connection available",
			// Toast.LENGTH_SHORT).show();
		}
	}

	public String updateDatePanel() {
		SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
		SimpleDateFormat datef = new SimpleDateFormat("yyyy-MM-dd");
		GregorianCalendar gc = new GregorianCalendar();

		Date d = new Date();
		//String dayOfTheWeek = sdf.format(d);
		//String dateString = datef.format(d);

		gc.setTime(d);
		int dayBefore = gc.get(Calendar.DAY_OF_YEAR);
		gc.roll(Calendar.DAY_OF_YEAR, -datePivot);

		int dayAfter = gc.get(Calendar.DAY_OF_YEAR);
		if (dayAfter > dayBefore) {
			gc.roll(Calendar.YEAR, -datePivot);
		}
		gc.get(Calendar.DATE);
		java.util.Date yesterday = gc.getTime();

		TextView dateTxt = (TextView) findViewById(R.id.dateText);
		ImageView previousBtn = (ImageView) findViewById(R.id.previousLabel);
		ImageView nextBtn = (ImageView) findViewById(R.id.nextLabel);

		if (datePivot == 0) {
			dateTxt.setText(getResources().getString(R.string.today));
		} else if (datePivot == 1) {
			dateTxt.setText(getResources().getString(R.string.yesterday));
		} else {
			dateTxt.setText(sdf.format(yesterday) + ", "
					+ datef.format(yesterday));
		}

		// Check if "the previous day" is valid with respect to game acceptance
		// date
		dayBefore = gc.get(Calendar.DAY_OF_YEAR);
		gc.roll(Calendar.DAY_OF_YEAR, -1);

		dayAfter = gc.get(Calendar.DAY_OF_YEAR);
		if (dayAfter > dayBefore) {
			gc.roll(Calendar.YEAR, -1);
		}
		gc.get(Calendar.DATE);
		java.util.Date previousDay = gc.getTime();
		SharedPreferences prefs = getSharedPreferences(
				Constants.PROPERTIES_NAME, Context.MODE_PRIVATE);

		if (datePivot == 6 || !Constants.validDate(prefs, previousDay)) {
			previousBtn.setImageDrawable(getResources().getDrawable(
					R.drawable.previousicon_nonclickable));
		} else {
			previousBtn.setImageDrawable(getResources().getDrawable(
					R.drawable.previousicon));
		}

		if (datePivot == 0) {
			nextBtn.setImageDrawable(getResources().getDrawable(
					R.drawable.nexticon_nonclickable));

		} else {
			nextBtn.setImageDrawable(getResources().getDrawable(
					R.drawable.nexticon));
		}

		return datef.format(yesterday);

	}

	private void updateDisplay(boolean displayMessage) {
		displayCongratulations = displayMessage;
	}

	/*
	 * 
	 * */

	private boolean isDifferentDay(SharedPreferences prefs) {
		String prevDate = prefs.getString(Constants.PROP_PREV_DATE, null);
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
		String today = formatter.format(cal.getTime());
		if (prevDate == null) {
			// first time
			// Toast.makeText(this, "First time with date: "+today,
			// Toast.LENGTH_LONG).show();
			Editor editor = prefs.edit();
			editor.putString(Constants.PROP_PREV_DATE, today);
			editor.commit();
			prevDate = today;
		}
		boolean isDifferentDay = !today.equals(prevDate);
		if (isDifferentDay) {
			// update property with new day
			Editor editor = prefs.edit();
			editor.putString(Constants.PROP_PREV_DATE, today);
			editor.commit();
			prevDate = today;

		}

		
		return isDifferentDay;
	}

	/**
	 * Update the bars after winning badges
	 * 
	 * @param prefs
	 * @param previousUserSteps
	 * @param previousBuddySteps
	 * @param previousWeightedSum
	 * @param noNewBadge
	 */

	/**
	 * Update the bars after winning badges
	 * 
	 * @param prefs
	 * @param previousUserFloors
	 * @param previousBuddyFloors
	 * @param previousWeightedSum
	 * @param noNewBadge
	 */

	/**
	 * Update user & buddy completed steps with the current absolute steps
	 * fetched from backend.
	 * 
	 * @param prefs
	 */

	private void showAuthorizationDialog() {

		/*
		 * 
		 * if(creationComplete && !Constants.AUTHORIZATION_VALIDATED &&
		 * !isDisplayingConnectionErrorDialog &&
		 * !isDisplayingAuthorizationDialog)
		 */

		if (isActive) {
			isDisplayingAuthorizationDialog = true;
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(
					getResources().getString(R.string.not_validated))
					.setTitle(getResources().getString(R.string.sync_failed));
			builder.setPositiveButton(getResources().getString(R.string.yes), new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					isDisplayingAuthorizationDialog = false;
					Intent i = new Intent(MainActivityCommunity.this,
							AuthenticateFitbitActivity.class);
					startActivityForResult(i, REQUEST_CODE_AUTHORIZE);
				}
			});
			builder.setNegativeButton(getResources().getString(R.string.no), new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					isDisplayingAuthorizationDialog = false;
					// Main2Activity.this.updateStepBadges();
					// Main2Activity.this.updateFloorBadges();
				}
			});
			builder.show();
		}

	}


	private void showSyncFailedDialog() {
		// Toast.makeText(Main2Activity.this,
		// "Cannot synchronize: No internet connection.",
		// Toast.LENGTH_LONG).show();
		displayConnectionErrorDialog();
		/*
		 * AlertDialog.Builder builder = new AlertDialog.Builder(this);
		 * builder.setMessage(
		 * "Could not synchronize with fitbit server. Would you like to authorize again? If you click 'No' your steps might not be up to date"
		 * ).setTitle("Synchronisation failed");
		 * builder.setPositiveButton("Yes", new OnClickListener() {
		 * 
		 * @Override public void onClick(DialogInterface dialog, int which) {
		 * dialog.dismiss(); Intent i= new
		 * Intent(Main2Activity.this,AuthenticateFitbitActivity.class);
		 * startActivityForResult(i, REQUEST_CODE_AUTHORIZE); } });
		 * builder.setNegativeButton("No", new OnClickListener() {
		 * 
		 * @Override public void onClick(DialogInterface dialog, int which) {
		 * dialog.dismiss(); updateBadges(); } }); builder.show();
		 */
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE_AUTHORIZE) {
			if (resultCode == Activity.RESULT_OK) {
				// user authorized again, retry (optimization: could just call
				// udpateBadges() without sync here since the user already did
				// this manually)
				updateDisplay(datePivot == 0);
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	public void onPopupOkButtonClicked(View v) {
		if (alertDialog.isShowing()) { // safe check
			alertDialog.dismiss();
		}
	}

	public void displayConnectionErrorDialog(boolean show) {
		if (show) {
			displayConnectionErrorDialog();
		} else if (connectionDialog != null) {
			isDisplayingConnectionErrorDialog = false;
			connectionDialog.dismiss();
		}
	}

	public void displayConnectionErrorDialog(final int retryFunction) {
		if (!isDisplayingConnectionErrorDialog) // creationComplete &&
		{

			if (isActive) {
				showProgressDialog(false);

				AlertDialog.Builder builder;
				builder = new AlertDialog.Builder(MainActivityCommunity.this);
				builder.setTitle(getResources().getString(R.string.connection_error_title))
						.setMessage(
								getResources().getString(R.string.connection_error_message))
						// .setCancelable(false)
						.setOnCancelListener(
								new DialogInterface.OnCancelListener() {

									@Override
									public void onCancel(DialogInterface dialog) {
										moveTaskToBack(true);
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
									/*	Log.d(TAG,
												"showProgressDialog called from connection error dialog.");*/
										
										switch(retryFunction)
										{
											case 0:
												refreshLeaderboard(0,true);
												break;
											case 1:
												refreshLeaderboard(1,true);
												break;
											case 2:
												changeMyAvatar();
												break;
											default:
													showProgressDialog(true);
													checkProgressHandler.postDelayed(
															checkForProgressTask, 0);
										};
										
									}
								});

				connectionDialog = builder.create();

				isDisplayingConnectionErrorDialog = true;
				connectionDialog.show();
			}
		}
	}
	
	public void displayConnectionErrorMessage()
	{
		isDisplayingConnectionErrorDialog = false;
		displayConnectionErrorDialog();
	}
	
	public void displayConnectionErrorDialog() {

		if (!isDisplayingConnectionErrorDialog) // creationComplete &&
		{

			if (isActive) {
				showProgressDialog(false);
				isDisplayingConnectionErrorDialog = true;
				Toast.makeText(MainActivityCommunity.this, getResources().getString(R.string.connection_toast_message), Toast.LENGTH_LONG).show();
				
				/*
				AlertDialog.Builder builder;
				builder = new AlertDialog.Builder(MainActivityCommunity.this);
				builder.setTitle(getResources().getString(R.string.connection_error_title))
						.setMessage(
								getResources().getString(R.string.connection_error_message))
						// .setCancelable(false)
						.setOnCancelListener(
								new DialogInterface.OnCancelListener() {

									@Override
									public void onCancel(DialogInterface dialog) {
										moveTaskToBack(true);
									}
								})
						.setNeutralButton(getResources().getString(R.string.connection_error_retry),
								new OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										isDisplayingConnectionErrorDialog = false;
										
										showProgressDialog(true);
										checkProgressHandler.postDelayed(
												checkForProgressTask, 0);
									}
								});

				connectionDialog = builder.create();

				isDisplayingConnectionErrorDialog = true;
				connectionDialog.show();*/
			}
		}
	}

	public void displayBuddyChangeDialog(final String message) {

		if (!isDisplayingBuddyDialog) {
			isDisplayingBuddyDialog = true;
			showProgressDialog(false);
			SharedPreferences prefs2 = getSharedPreferences(
					Constants.PROPERTIES_NAME, MODE_PRIVATE);
			Constants.internalReset(MainActivityCommunity.this,prefs2);
			Constants.externalReset();
			
			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View layout = inflater.inflate(R.layout.activity_buddychange_dialog,
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
			
			buddyDialog = builder.create();
			
			TextView tv = (TextView) layout.findViewById(R.id.alertinfo2_bc);
			
			tv.setText(message);
			buddyDialogMessage = message;
			if (isActive) {
				buddyDialog.show();
			}			
			
			/*
			AlertDialog.Builder builder;
			builder = new AlertDialog.Builder(Main2Activity.this);
			builder.setTitle(getResources().getString(R.string.buddy_change))
					.setMessage(message)
					// .setCancelable(false)
					.setOnCancelListener(
							new DialogInterface.OnCancelListener() {

								@Override
								public void onCancel(DialogInterface dialog) {
									moveTaskToBack(true);
									// checkProgressHandler.postDelayed(checkForProgressTask,
									// 0);
								}
							})
					.setNeutralButton(getResources().getString(R.string.return_to_settings),
							new OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									isDisplayingBuddyDialog = false;
									buddyDialog.dismiss();

									Intent i_settings = new Intent(
											Main2Activity.this,
											SettingsActivity.class);

									i_settings.putExtra("RequestAccepted", -1);
									i_settings.putExtra("message", message);
									i_settings.putExtra("removalSeen", -99);
									Constants.COUNTER_INVITE = false;
									Constants.INVITATION_ACCEPTED = false;
									Constants.INVITATION_SENT = false;
									
									AppContext.getInstance().setFriendId(0);
									
									SharedPreferences prefs2 = getSharedPreferences(
											Constants.PROPERTIES_NAME,
											MODE_PRIVATE);
									Editor editor = prefs2.edit();
									editor.putBoolean(
											Constants.PROP_KEY_GAME_STARTED,
											false);
									editor.commit();

									startActivity(i_settings);
								}
							});

			buddyDialog = builder.create();
			buddyDialog.show();
			// connectionDialog = builder.create();

			// isDisplayingConnectionErrorDialog = true;
			// connectionDialog.show();*/
		}
	}	
	
	public void onBuddyChangeAcknowledged(View v) 
	{
		if(buddyDialog != null)
		{
			if (buddyDialog.isShowing()) { // safe check
				isDisplayingBuddyDialog = false;
				buddyDialog.dismiss();
				buddyDialog = null;

				Intent i_settings = new Intent(
						MainActivityCommunity.this,
						SettingsActivity.class);

				i_settings.putExtra("RequestAccepted", -1);
				i_settings.putExtra("message", buddyDialogMessage);
				i_settings.putExtra("removalSeen", -99);
				Constants.COUNTER_INVITE = false;
				Constants.INVITATION_ACCEPTED = false;
				Constants.INVITATION_SENT = false;
				
				AppContext.getInstance().setFriendId(0);
				
				SharedPreferences prefs2 = getSharedPreferences(
						Constants.PROPERTIES_NAME,
						MODE_PRIVATE);
				Editor editor = prefs2.edit();
				editor.putBoolean(
						Constants.PROP_KEY_GAME_STARTED,
						false);
				editor.commit();
				
				Constants.internalReset(MainActivityCommunity.this,prefs2);
				Constants.externalReset();

				startActivity(i_settings);
				
			}
		}
	}	
	
	/*public void displayBuddyChangeDialog(final String message) {

		if (!isDisplayingBuddyDialog) {
			isDisplayingBuddyDialog = true;
			showProgressDialog(false);

			// Addition: so avoid the remnants
			Constants.internalReset();
			Constants.externalReset();
			
			AlertDialog.Builder builder;
			builder = new AlertDialog.Builder(MainActivityCommunity.this);
			builder.setTitle(getResources().getString(R.string.buddy_change))
					.setMessage(message)
					// .setCancelable(false)
					.setOnCancelListener(
							new DialogInterface.OnCancelListener() {

								@Override
								public void onCancel(DialogInterface dialog) {
									moveTaskToBack(true);
									// checkProgressHandler.postDelayed(checkForProgressTask,
									// 0);
								}
							})
					.setNeutralButton(getResources().getString(R.string.return_to_settings),
							new OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									isDisplayingBuddyDialog = false;
									buddyDialog.dismiss();

									Intent i_settings = new Intent(
											MainActivityCommunity.this,
											SettingsActivity.class);

									i_settings.putExtra("RequestAccepted", -1);
									i_settings.putExtra("message", message);
									i_settings.putExtra("removalSeen", -99);
									Constants.COUNTER_INVITE = false;
									Constants.INVITATION_ACCEPTED = false;
									Constants.INVITATION_SENT = false;
									
									AppContext.getInstance().setFriendId(0);
									
									SharedPreferences prefs2 = getSharedPreferences(
											Constants.PROPERTIES_NAME,
											MODE_PRIVATE);
									Editor editor = prefs2.edit();
									editor.putBoolean(
											Constants.PROP_KEY_GAME_STARTED,
											false);
									editor.commit();

									startActivity(i_settings);
								}
							});

			buddyDialog = builder.create();
			buddyDialog.show();
			// connectionDialog = builder.create();

			// isDisplayingConnectionErrorDialog = true;
			// connectionDialog.show();
		}
	}*/

	public void checkStartDate() {
		final SharedPreferences prefs = getSharedPreferences(
				Constants.PROPERTIES_NAME, MODE_PRIVATE);
		String startDate = prefs.getString(
				Constants.PROP_INVITATION_ACCEPTANCE_DATE, null);

		if (startDate == null) {
			GetStartDateForGameTask dtTask = new GetStartDateForGameTask(
					AppContext.getInstance().getUserId(), AppContext
							.getInstance().getEmail(), "---") {

				@Override
				protected void onPostExecute(String result) {

					if(this.safeExecution && result != null)
					{
						if (!result.contains("NF")) {
							Editor editor = prefs.edit();
							editor.putString(
									Constants.PROP_INVITATION_ACCEPTANCE_DATE,
									result);
							editor.commit();
						}
					}
					else if(!Utils.isConnectionPresent(MainActivityCommunity.this))
					{
						displayConnectionErrorMessage();
					}					
					
					/*if (!result.contains("NF")) {
						Editor editor = prefs.edit();
						editor.putString(
								Constants.PROP_INVITATION_ACCEPTANCE_DATE,
								result);
						editor.commit();
					}*/
				}
			};

			dtTask.execute();
		}
	}

	public void closeBuddyDialog(boolean allowNextDialog)
	{
		closeBuddyDialog();
		
		if(allowNextDialog)
		{				
			SharedPreferences prefs = getSharedPreferences(Constants.PROPERTIES_NAME,MODE_PRIVATE);
			Constants.clearNotificationDate(prefs);
		}
	}
	
	public void closeBuddyDialog()
	{
		if(buddyDialog != null)
		{
			if (buddyDialog.isShowing()) { // safe check
				isDisplayingBuddyDialog = false;
				try
				{
					buddyDialog.dismiss();
				}
				catch(Exception e)
				{
					
				}
				buddyDialog = null;
			}
		}
	}
	
	public void setupInvitationsListView(View layout) 
	{
		final ListView lv = (ListView)layout.findViewById(R.id.listViewBuddyEmail);
		items = new ArrayAdapter<String>(MainActivityCommunity.this, R.layout.my_single_checklist_item, invitationEmails); // android.R.layout.simple_list_item_single_choice
		lv.setAdapter(items);
		lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		lv.setItemChecked(0, true);
		inviterEmail = items.getItem(0);
		AcceptBuddyActivity.invitationSelected = true;
		//yourListView.setSelection(0);
		//yourListView.getSelectedView().setSelected(true);
		
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) 
			{
				int i = arg2;//lv.getSelectedItemPosition();
				String email = invitationEmails.get(i);
				//Toast.makeText(AcceptBuddyActivity.this, "View.onClickListener: " + email, Toast.LENGTH_SHORT).show();
				inviterEmail = email;
			}
		});
		
		//LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, dpToPx(10+36*(items.getCount())));//LinearLayout.LayoutParams.WRAP_CONTENT);
		LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		p.setMargins(0, 0, 0, dpToPx(10));
		lv.setLayoutParams(p);
		/*LinearLayout LL = (LinearLayout)layout.findViewById(R.id.accept_form);
		//LayoutParams p = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		LL.setLayoutParams(p);*/
	}	
	
	
	public void displayNewInvitationDialog(final ArrayList<String> invitations)
	{
		
		Log.d(TAG, "Displaying the new invitation dialog.");

		if (!isDisplayingBuddyDialog && invitations.size() > 0) 
		{
			
			isDisplayingBuddyDialog = true;
			AcceptBuddyActivity.invitationSelected = false;
			
			
			invitationEmails.clear();
			for(int i = 0; i < invitations.size(); i++)
			{
				invitationEmails.add(invitations.get(i));
			}
			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			final View layout = inflater.inflate(R.layout.activity_new_invitation_dialog,null);
			
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			
			builder	.setView(layout)
					.setOnCancelListener(
						new DialogInterface.OnCancelListener() {
				
							@Override
							public void onCancel(DialogInterface dialog) 
							{
								closeBuddyDialog();
							}
						});
				
			
			buddyDialog = builder.create();
			
			Button acceptBtn = (Button) layout.findViewById(R.id.acceptButton);
			//ImageButton seeOthersBtn = (ImageButton) layout.findViewById(R.id.viewAllInvitationsButton);
			Button rejectBtn = (Button) layout.findViewById(R.id.rejectInvitationsButton);
			//LinearLayout LL = (LinearLayout) layout.findViewById(R.id.viewAllInvitationsLayer);
			
			
			//final TextView textViewBuddyEmail = (TextView)layout.findViewById(R.id.textViewBuddyEmail);
			final TextView explainText = (TextView)layout.findViewById(R.id.you_have_an_invitation);
			if(invitations.size() > 0)
			{
				setupInvitationsListView(layout);	
				
				if(invitations.size() == 1) // one inviter.
				{
					explainText.setText(getResources().getString(R.string.you_have_an_invitation));
					//LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0,0);
					//LL.setLayoutParams(p);
					//seeOthersBtn.setLayoutParams(p);
				//	LinearLayout.LayoutParams p2 = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,0);
				//	acceptBtn.setLayoutParams(p2);
				/*	textViewBuddyEmail.setText(invitations.get(0));
					seeOthersBtn.setVisibility(ImageButton.INVISIBLE);
					AcceptBuddyActivity.invitationSelected = true;*/				
				}
				else // multiple inviters
				{
					explainText.setText(getResources().getString(R.string.you_have_invitations));	
				}
			}
			
			
			/*if(invitations.size() == 1) // one inviter.
			{
				
				//LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0,0);
				//LL.setLayoutParams(p);
				//seeOthersBtn.setLayoutParams(p);
			//	LinearLayout.LayoutParams p2 = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,0);
			//	acceptBtn.setLayoutParams(p2);
				textViewBuddyEmail.setText(invitations.get(0));
				seeOthersBtn.setVisibility(ImageButton.INVISIBLE);
				AcceptBuddyActivity.invitationSelected = true;				
			}
			else // multiple inviters
			{
				
				Drawable details = getResources().getDrawable(android.R.drawable.ic_menu_info_details); 
				details.setColorFilter(getResources().getColor(R.color.dark_blue), Mode.SRC_IN);//new PorterDuffColorFilter(0xffff00,PorterDuff.Mode.MULTIPLY));
				seeOthersBtn.setImageDrawable(details);
				
				textViewBuddyEmail.setText(getResources().getString(R.string.multiple_users));
				AcceptBuddyActivity.invitationSelected = false;
			}*/
			
			/*seeOthersBtn.setOnClickListener(new View.OnClickListener() 
			{
				
				@Override
				public void onClick(View v) 
				{
						AlertDialog.Builder alert = new AlertDialog.Builder(Main2Activity.this);
						final ArrayAdapter<String> receivedInvitations = new ArrayAdapter<String>(
								Main2Activity.this, android.R.layout.simple_list_item_1);	
						
						for(int i = 0; i < invitationEmails.size(); i++)
						{
							receivedInvitations.add(invitationEmails.get(i));
						}
						
						LayoutInflater inflater = getLayoutInflater();
						View viewHeader = inflater.inflate(R.layout.customtitle, null);
						TextView title = (TextView) viewHeader.findViewById(R.id.myTitle);
						title.setText(getResources().getString(R.string.buddy_choosing_title));
						alert.setCustomTitle(viewHeader);
	
						alert.setAdapter(receivedInvitations,new OnClickListener() {
	
									@Override
									public void onClick(DialogInterface dialog,int which) 
									{
												textViewBuddyEmail.setText(receivedInvitations.getItem(which));
												AcceptBuddyActivity.invitationSelected = true;
									}
								})
								.setNegativeButton(getResources().getString(R.string.cancel_button), null);
	
						// alert.show();
						AlertDialog dialog = alert.create();
						dialog.show();
					
				}
			});*/
			
			
			acceptBtn.setOnClickListener(new View.OnClickListener() {
				
				public void onClick(View v) 
				{
					if(AcceptBuddyActivity.invitationSelected)
					{
						//final ListView lv = (ListView)findViewById(R.id.listViewBuddyEmail);
						//inviterEmail = lv.getSelectedItem();//textViewBuddyEmail.getText().toString();
						Log.i(TAG,"textViewBuddyEmail.getText = " + inviterEmail);//textViewBuddyEmail.getText());
						//Toast.makeText(Main2Activity.this,"textViewBuddyEmail.getText = " + inviterEmail,Toast.LENGTH_LONG).show();//textViewBuddyEmail.getText());
						displayBuddyConfirmDialog(inviterEmail + "");
					}
					else
					{
						Toast.makeText(MainActivityCommunity.this,getResources().getString(R.string.select_an_inviation_first), Toast.LENGTH_LONG).show();						
					}
				}
			});			
			
			rejectBtn.setOnClickListener(new View.OnClickListener() {
				
				public void onClick(View v) 
				{
					if(AcceptBuddyActivity.invitationSelected)
					{
						
						//TextView inviteeTV = textViewBuddyEmail;
						final String removalString = inviterEmail;//inviteeTV.getText() + "";	
						DeFriendTask2 taskd2 = new DeFriendTask2(AppContext.getInstance().getUserId(), removalString) 
						{ 
							@Override
							protected void onPostExecute(Boolean success) 
							{

								if (success) 
								{
									//closeBuddyDialog();
									invitationEmails.remove(removalString);
									items.notifyDataSetChanged();
									
									if (invitationEmails.size() == 1) 
									{
										Log.i(TAG, "Remaining buddy(" + 0 + ") = " + invitationEmails.get(0));
										// remain on this screen, show the last person.
										AcceptBuddyActivity.invitationSelected = true;
										explainText.setText(getResources().getString(R.string.you_have_an_invitation));
										final ListView lv = (ListView)layout.findViewById(R.id.listViewBuddyEmail);
										lv.setItemChecked(0, true);
										inviterEmail = items.getItem(0);
										LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, dpToPx(10+36*(items.getCount())));//LinearLayout.LayoutParams.WRAP_CONTENT);
										lv.setLayoutParams(p);
										//textViewBuddyEmail.setText(invitationEmails.get(0));
									} 
									else if (invitationEmails.size() > 1) 
									{
										for(int bud = 0; bud < invitationEmails.size(); bud++)
										{
											Log.i(TAG, "Remaining buddy(" + bud + ") = " + invitationEmails.get(bud));
										}
										// remain on this screen, show "Multipe Invitations"
										//textViewBuddyEmail.setText(getResources().getString(R.string.multiple_users));
										final ListView lv = (ListView)layout.findViewById(R.id.listViewBuddyEmail);
										lv.setItemChecked(0, true);
										inviterEmail = items.getItem(0);
										LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, dpToPx(10+36*(items.getCount())));//LinearLayout.LayoutParams.WRAP_CONTENT);
										lv.setLayoutParams(p);
										AcceptBuddyActivity.invitationSelected = true;
									}
									else
									{
										Constants.INVITATION_RECEIVED = false;
										closeBuddyDialog(true);
									}
								}
								else
								{
									if(!Utils.isConnectionPresent(MainActivityCommunity.this))
									{
										displayConnectionErrorMessage();
									}
									else
									{
										displayConnectionErrorMessage();
									}
								}
								
								
							}
						};
						
						taskd2.execute();
			
							
					}
					else
					{
						Toast.makeText(MainActivityCommunity.this,getResources().getString(R.string.select_an_inviation_first), Toast.LENGTH_LONG).show();						
					}
				}
			});	
			
			
			buddyDialog.show();
		}
		
	}
	
	public void displayBuddyConfirmDialog(final String buddyCandidate)
	{

			inviterEmail = buddyCandidate;
			
			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View layout = inflater.inflate(R.layout.activity_buddy_confirm_dialog,
					null);
			
			AlertDialog.Builder builder = new AlertDialog.Builder(this)
			.setView(layout)
			.setOnCancelListener(
			new DialogInterface.OnCancelListener() {
	
				@Override
				public void onCancel(DialogInterface dialog) {
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
					onConfirmBuddyClicked();
					dialog.cancel();
				}
			});
			
			
		
			
			TextView dialogTV = (TextView) layout.findViewById(R.id.alertinfo3);
			
			String dialogContent = dialogTV.getText() + "\n" + buddyCandidate + "?";
			
			dialogTV.setText(dialogContent);
			
			dialog.show();

	}
	
	
	/*
	 * Main2Activity.onConfirmBuddyClicked() is accessible when: 
	 *
	 * 1. Interaction perspective: the user accepts an incoming invitation.
	 * 2. System perspective: After displayBuddyConfirmDialog(String buddyCandidate) is launced
	 * 		(i.e., the "activity_buddy_confirm_dialog" popup was inflated)
	 * 
	 * */
	
	public void onConfirmBuddyClicked()
	{
		final SharedPreferences prefs = getSharedPreferences(
				Constants.PROPERTIES_NAME, MODE_PRIVATE);		
		
		Utils.performAcceptOperation(this, prefs, 	AppContext.getInstance().getUserId(), 
												 	AppContext.getInstance().getEmail(), 
												 	inviterEmail);
		
		
		if(buddyDialog != null)
		{
			if(buddyDialog.isShowing())
			{
				buddyDialog.dismiss();
				buddyDialog = null;
			}
		}
		
	}	
	
	private void showMultiplePendingBuddyInvitationNotification(ArrayList<String> emails) {
		
		SharedPreferences prefs = getSharedPreferences(
				Constants.PROPERTIES_NAME, MODE_PRIVATE);
		boolean allowNotification = Constants.checkNotificationAllowance(prefs);
		
		if(allowNotification)
		{

			Constants.embarkNotificationDate(prefs);
			displayNewInvitationDialog(emails);
			/*
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
			resultIntent.putExtra("RequestAccepted", -1);
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
					builder.build());*/
		
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
			ArrayList<String> inviters = new ArrayList<String>();
			
			inviters.add(buddyEmail);
			
			displayNewInvitationDialog(inviters);		
			
		/*
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
				builder.build());*/
		}
	}

	public void displayStats(int i) 
	{
		String activity;
		String pastOrCurrentDay;
		String suffix;
		String endSentence;
		int itemToShow;
		if(i == 0) // Steps
		{
			activity = getResources().getString(R.string.steps);
			itemToShow = Constants.userStepRank;
		}
		else
		{
			activity = getResources().getString(R.string.floors);
			itemToShow = Constants.userFloorRank;
		}
		
		activity = activity.replaceAll(" ", "");
		
		activity = (activity.substring(0, 1)).toUpperCase() + "" +  activity.substring(1,activity.length()); 
		
		if(Constants.communityDatePivot == 0)
		{
			pastOrCurrentDay = getResources().getString(R.string.you_are_ranked);
			endSentence = getResources().getString(R.string.today).toLowerCase();
		}
		else
		{
			pastOrCurrentDay = getResources().getString(R.string.you_were_ranked);
			endSentence = getResources().getString(R.string.this_day).toLowerCase();
		}
		
		
		if(itemToShow  == 1)
		{
			suffix = getResources().getString(R.string.first_suffix);
		}
		else if(itemToShow == 2)
		{
			suffix = getResources().getString(R.string.second_suffix);
		}
		else if(itemToShow == 3)
		{
			suffix = getResources().getString(R.string.third_suffix);
		}
		else if((itemToShow%10) == 1)
		{
			suffix = getResources().getString(R.string.late_first_suffix);
		}
		else if((itemToShow%10) == 2)
		{
			suffix = getResources().getString(R.string.late_second_suffix);
		}
		else if((itemToShow%10) == 3)
		{
			suffix = getResources().getString(R.string.late_third_suffix);
		}
		else
		{
			suffix = getResources().getString(R.string.rest_suffix);		
		}
		
		
		Toast.makeText(this, activity + ": " + pastOrCurrentDay + " " + itemToShow + suffix + " " + endSentence, Toast.LENGTH_LONG).show();
		
	}	
	
	private void checkFitbitAuthentication() {
		CheckFitbitAuthenticationTask authTask = new CheckFitbitAuthenticationTask(
				AppContext.getInstance().getUserId()) 
		{
			
			protected void onPostExecute(Boolean success) {
				
				if(!this.safeExecution)
				{
					if(!Utils.isConnectionPresent(MainActivityCommunity.this))
					{
						showSyncFailedDialog();
					}
				}
				
				if (!success && Utils.isConnectionPresent(MainActivityCommunity.this) && this.outcome.contains("not validated")) {
					showAuthorizationDialog();
				} else if (success) {
					// Toast.makeText(Main2Activity.this,
					// "Good, you have your fitbit authentication!",
					// Toast.LENGTH_SHORT).show();
				} else
				{
					showSyncFailedDialog();
				}
				
			}
		};

		authTask.execute();
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
	/*
	public int dpToPx(int dp) {
	    DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
	    int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));       
	    return px;
	}	
	
	public int pxToDp(int px) {
	    DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
	    int dp = Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
	    return dp;
	}*/
	
	public void refreshLeaderboardDisplay(int i, boolean guard) 
	{
		final int dpOffset = Constants.LEADERBOARD_DP_OFFSET;
		Log.d(TAG,"Entered to refreshLeaderboardDisplay(" + i + ")");
		if(i == 1)
		{

			if(listViewFloor.getAdapter() == null)
			{
				listViewFloor.setAdapter(leaderboardFloor);
				listViewFloor.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int position, long arg3) {
						Log.i("RefreshLeaderboard", "myLock = " + leaderboardFloor.myLock);
						if (!leaderboardFloor.myLock) {
							int ind = position - 1;
							if (ind < Constants.leaderboardGroupsFloor.size()) {
								String first = Constants.leaderboardGroupsFloor
										.get(ind).mem[0].getUserName();
								String second = Constants.leaderboardGroupsFloor
										.get(ind).mem[1].getUserName();
								if((ind+1) == Constants.userFloorRank)
								{
									Toast.makeText( MainActivityCommunity.this,
											getResources().getString(R.string.your_team) + " (" + first +  " " + getResources().getString(R.string.and) + " " + second + ")",
											Toast.LENGTH_SHORT).show();
								}
								else
								{
									Toast.makeText(MainActivityCommunity.this,
											getResources().getString(R.string.team_of) + " " + first +  " " + getResources().getString(R.string.and) + " " + second,
											Toast.LENGTH_SHORT).show();
								}	
							}
						}
					}
				});				
			}
			else
			{
				leaderboardFloor.notifyDataSetChanged();
				listViewFloor.setAdapter(leaderboardFloor);
				//((Leaderboard)listViewFloor.getAdapter()).notifyDataSetChanged();
				
				listViewFloor.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int position, long arg3) {
						Log.i("RefreshLeaderboard", "myLock = " + leaderboardFloor.myLock);
						if (!leaderboardFloor.myLock) {
							int ind = position - 1;
							if (ind < Constants.leaderboardGroupsFloor.size()) {
								String first = Constants.leaderboardGroupsFloor
										.get(ind).mem[0].getUserName();
								String second = Constants.leaderboardGroupsFloor
										.get(ind).mem[1].getUserName();
								if((ind+1) == Constants.userFloorRank)
								{
									Toast.makeText( MainActivityCommunity.this,
											getResources().getString(R.string.your_team) + " (" + first +  " " + getResources().getString(R.string.and) + " " + second + ")",
											Toast.LENGTH_SHORT).show();
								}
								else
								{
									Toast.makeText(MainActivityCommunity.this,
											getResources().getString(R.string.team_of) + " " + first +  " " + getResources().getString(R.string.and) + " " + second,
											Toast.LENGTH_SHORT).show();
								}	
							}
						}
					}
				});						
			}
			
			
			int multiplier = leaderboardFloor.getCount();//Constants.leaderboardGroupsFloor.size();
			
			if(multiplier > Constants.MAXIMUM_LEADERBOARD_ENTRY)
			{
				multiplier = Constants.MAXIMUM_LEADERBOARD_ENTRY;
			}			
			
			
			
			int designatedHeight = dpToPx(dpOffset*(multiplier+1));
			
			listViewFloor.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, designatedHeight));

			listViewFloor.invalidate();
			LinearLayout whole=(LinearLayout) (findViewById(R.id.LinearLayout1));
			whole.invalidate();
			listViewFloor.invalidate();
			
		}
		else
		{
			if(listView.getAdapter() == null)
			{
				listView.setAdapter(leaderboard);
				listView.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int position, long arg3) {
						Log.i("RefreshLeaderboard", "myLock = " + leaderboard.myLock);
						if (!leaderboard.myLock) {
							int ind = position - 1;
							if (ind < Constants.leaderboardGroups.size()) {
								String first = Constants.leaderboardGroups
										.get(ind).mem[0].getUserName();
								String second = Constants.leaderboardGroups
										.get(ind).mem[1].getUserName();
								if((ind+1) == Constants.userStepRank)
								{
									Toast.makeText( MainActivityCommunity.this,
											getResources().getString(R.string.your_team) + " (" + first +  " " + getResources().getString(R.string.and) + " " + second + ")",
											Toast.LENGTH_SHORT).show();
								}
								else
								{
									Toast.makeText(MainActivityCommunity.this,
											getResources().getString(R.string.team_of) + " " + first +  " " + getResources().getString(R.string.and) + " " + second,
											Toast.LENGTH_SHORT).show();
								}	
							}
						}
					}
				});
				
			}
			else
			{
				leaderboard.notifyDataSetChanged();
				listView.setAdapter(leaderboard);
				listView.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int position, long arg3) {
						Log.i("RefreshLeaderboard", "myLock = " + leaderboard.myLock);
						if (!leaderboard.myLock) {
							int ind = position - 1;
							if (ind < Constants.leaderboardGroups.size()) {
								String first = Constants.leaderboardGroups
										.get(ind).mem[0].getUserName();
								String second = Constants.leaderboardGroups
										.get(ind).mem[1].getUserName();
								
								if((ind+1) == Constants.userStepRank)
								{
									Toast.makeText( MainActivityCommunity.this,
											getResources().getString(R.string.your_team) + " (" + first +  " " + getResources().getString(R.string.and) + " " + second + ")",
											Toast.LENGTH_SHORT).show();
								}
								else
								{
									Toast.makeText(MainActivityCommunity.this,
											getResources().getString(R.string.team_of) + " " + first +  " " + getResources().getString(R.string.and) + " " + second,
											Toast.LENGTH_SHORT).show();
								}								

							}
						}
					}
				});				
			}
			
			int multiplier = leaderboard.getCount();//Constants.leaderboardGroups.size();
			
			if(multiplier > Constants.MAXIMUM_LEADERBOARD_ENTRY)
			{
				multiplier = Constants.MAXIMUM_LEADERBOARD_ENTRY;
			}
			
			int designatedHeight = dpToPx(dpOffset*(multiplier+1));
			
			listView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, designatedHeight));
			listView.invalidate();
			LinearLayout whole=(LinearLayout) (findViewById(R.id.LinearLayout1));
			
			//whole.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			whole.invalidate();
			listView.invalidate();
		}
		
		if(!guard)
			showManualProgressDialog(false);
		//displayStats(i);
		Log.d(TAG,"Out of refreshLeaderboardDisplay(" + i + ")");
	}
	
}

