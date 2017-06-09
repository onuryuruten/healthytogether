package ch.epfl.hci.healthytogether;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
//import java.util.Locale;









import android.app.Activity;
import android.app.AlertDialog;
/*import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;*/
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
import android.text.Html;
/*import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;*/
import android.text.Spannable;
import android.text.style.BackgroundColorSpan;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;
import ch.epfl.hci.happytogether.R;
import ch.epfl.hci.healthytogether.communication.ServerHelper.CheckFitbitAuthenticationTask;
import ch.epfl.hci.healthytogether.communication.ServerHelper.CheckGroupTask;
import ch.epfl.hci.healthytogether.communication.ServerHelper.DeFriendTask;
import ch.epfl.hci.healthytogether.communication.ServerHelper.DeFriendTask2;
import ch.epfl.hci.healthytogether.communication.ServerHelper.GetFloorPledgeTask;
import ch.epfl.hci.healthytogether.communication.ServerHelper.GetStartDateForGameTask;
import ch.epfl.hci.healthytogether.communication.ServerHelper.GetStepPledgeTask;
import ch.epfl.hci.healthytogether.communication.ServerHelper.RetrieveBuddyEMailTask;
import ch.epfl.hci.healthytogether.communication.ServerHelper.RetrieveBuddyEMailTask2;
import ch.epfl.hci.healthytogether.communication.ServerHelper.RetrieveExistingBuddyInfoTask;
import ch.epfl.hci.healthytogether.communication.ServerHelper.RetrieveFloorCountTask;
import ch.epfl.hci.healthytogether.communication.ServerHelper.RetrieveStepCountTask;
//import ch.epfl.hci.healthytogether.communication.ServerHelper.SendMessageTask;
import ch.epfl.hci.healthytogether.communication.ServerHelper.SendTimelyLogTask;
import ch.epfl.hci.healthytogether.communication.ServerHelper.SetFloorPledgeTask;
import ch.epfl.hci.healthytogether.communication.ServerHelper.SetStepPledgeTask;
import ch.epfl.hci.healthytogether.communication.ServerHelper.SyncBackendWithFitbitTask;
import ch.epfl.hci.healthytogether.util.Utils;

/**
 * The main activity that shows the progress of the game (who did how many
 * steps, how many are remaining until the next badge is awarded) and the
 * received badges so far.
 * 
 */
public class Main2Activity extends Activity {

	// ASMA START VARIABLES
	private Spinner spinner;
	private Spinner spinnerFloor;
	public static int pledgeAmount;
	public static int pledgeAmountFloor;
	/*private int spinnerIndex;
	private int spinnerFloorIndex;
	private ViewGroup spinnerParent;
	private ViewGroup spinnerFloorParent;*/
	// private static int buddyPledgeAmount;
	// private static boolean mySuccessfullPledge = false;
	private static boolean successfullPledge = false;
	private static boolean successfullPledgeFloor=false;
	// private static int[] pledgeAmountHistory = new int[7];
	// private static boolean hasPledged;

	// ASMA END VARIABLES	
	
	private static final int REQUEST_CODE_AUTHORIZE = 4444;
	private static final String TAG = Main2Activity.class.toString();
	private int mySteps;
	private int buddySteps;
	private int myFloors;
	private int buddyFloors;
	private int datePivot;
	private int[] buddyStepHistory = new int[7];
	private int[] myStepHistory = new int[7];
	private int[] buddyFloorHistory = new int[7];
	private int[] myFloorHistory = new int[7];
	private String buddyName;

	
	private AlertDialog alertDialog;
	private AlertDialog pledgeAlertDialog;
	private int pledgeMode;
	private boolean previousActivityIsMessagesActivity;

	//private boolean creationComplete = false;
	private boolean congratulationsDisplayRequest [] =  new boolean[]{false,false};
	private boolean pledgeCongratulationsDisplayRequest =false;

	private boolean isActive = false;

	/* Dialogs and their watch flags */
	private ProgressDialog initialSyncDialog;
	private AlertDialog connectionDialog;
	private AlertDialog buddyDialog;
	private String buddyDialogMessage;
	
	
	private boolean displayCongratulations = false;
	private boolean isDisplayingConnectionErrorDialog = false;
	private boolean isDisplayingAuthorizationDialog = false;
	private boolean isDisplayingBuddyDialog = false;

	public static  String EXTRA_EXERCISE_TEMPLATE = "exercise_template";
	public static  String EXTRA_NUTRITION_TEMPLATE = "nutrition_template";
	public static  String EXTRA_MOOD_TEMPLATE = "mood_template";
	public String DEFAULT_NUTRITION = "";//getResources().getString(R.string.food);//"Food?";
	public String DEFAULT_EXERCISE = "";//getResources().getString(R.string.activity);//"Activity?";
	public String DEFAULT_MOOD = "";//getResources().getString(R.string.mood);//"Mood?";
	public String TOGETHER_STRING = "";//getResources().getString(R.string.together);
	public String ALONE_STRING = "";//getResources().getString(R.string.alone);
	public String DEFAULT_SOCIAL = "";//getResources().getString(R.string.social);//"Social?";

	
	public String[] positiveMoodTemplates;
	public String[] negativeMoodTemplates;
	public String[] moodIntesityTemplates;
	public String[] foodTemplates;
	public String[] sedentaryTemplates;
	public String[] exerciseTemplates;
	public String[] socialTemplates;
	
	
	public static boolean nutritionSet;
	public static boolean exerciseSet;
	public static boolean moodSet;
	public static boolean socialSet;
	public static boolean isTogether;
	public static boolean activitySet;

	boolean badgeDialogLaunched = false;

	public static String nutritionString;
	public static String exerciseString;
	public static String moodString;
	public static String socialString;

	boolean refreshDone;
	private long lastRefreshMillis;

	private Handler checkProgressHandler;

	public int tbwidth;
	private int theight;
	TabHost tab_host;

	public int doHistoryCheck = 0;
	
	public Date lastSyncTime = null;
	
	public Date floorCongratsTime = null;
	public Date stepsCongratsTime = null;
	
	public String inviterEmail;
	public ArrayList<String> invitationEmails = new ArrayList<String>();
	ArrayAdapter<String> items;
	
	private Runnable checkForProgressTask = new Runnable() {

		@Override
		public void run() {
			boolean connectionErrorReceived = false;

			/*if(Constants.VIEW_STEPS)
			{
				checkStepBadges();
			}
			else
			{
				checkFloorBadges();
			}*/
			
			if (!Utils.isConnectionPresent(Main2Activity.this)) {
				// Toast.makeText(Main2Activity.this,
				// "No internet connection. Please check your connection.",
				// Toast.LENGTH_LONG).show();
				connectionErrorReceived = true;
				

				
								
				updateBadgesWithSync(true);
				
				displayConnectionErrorMessage();
			} else {

				Constants.checkUserName();
				int userId = AppContext.getInstance().getUserId();

				String email = AppContext.getInstance().getEmail();
				CheckGroupTask checkGroupTask = new CheckGroupTask(email,
						userId) {
					@Override
					protected void onPostExecute(String result) 
					{
						
						if(!this.safeExecution)
						{
							if(!Utils.isConnectionPresent(Main2Activity.this))
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
								Utils.scheduleAlarmReceiver(Main2Activity.this);
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
								// notifyBuddyChangeMessage(msg);
								
								displayBuddyChangeDialog(msg);

								break;
							case RESPONSE_INCOMING_REQUEST_PENDING:

								// if(AppContext.getInstance().getFriendId() >
								// 0)
								// {
								showBuddyName(AppContext.getInstance()
										.getFriendId());
								// }

								RetrieveBuddyEMailTask2 t2 = new RetrieveBuddyEMailTask2(
										AppContext.getInstance().getEmail()) {

									@Override
									protected void onPostExecute(String result) 
									{
										
										
										if(!this.safeExecution)
										{
											if(!Utils.isConnectionPresent(Main2Activity.this))
											{
												displayConnectionErrorMessage();
											}
											return;
										}										
										
										
										final ArrayList<Boolean> hasFriend = new ArrayList<Boolean>();
										final String myResult = result;
										if (result.contains("not found")) {
											Log.i(TAG,
													"The user does not have any buddy.");

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
											protected void onPostExecute(String buddyEmail) 
											{
												
												if(!this.safeExecution)
												{
													if(!Utils.isConnectionPresent(Main2Activity.this))
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
													
												} 
												else 
												{
													showPendingBuddyInvitationNotification(buddyEmail);
												}
												
												showBuddyName(AppContext.getInstance().getFriendId());
											}
										};
										task.execute();
									}
								};
								t2.execute();
								showBuddyName(AppContext.getInstance().getFriendId());

								break;
							case RESPONSE_WAITING_FOR_ACCEPT:
								break;
							default:
								AppContext.getInstance()
										.setFriendId(resultCode);
								showBuddyName(resultCode);
								Constants.INVITATION_OUTCOME_DISPLAYED = true;
								checkStartDate();

								break;
							}
						} 
						else {
							// error log_tag
							// Log.e(TAG, "an error has occurred");
							// Toast.makeText(Main2Activity.this,
							// R.string.error_general,
							// Toast.LENGTH_LONG).show();
						}
					}

				};
				checkGroupTask.execute();

				
				//updatePledgeBar(0);
				//updatePledgeBar(1);	
				
				SharedPreferences prefs = getSharedPreferences(
						Constants.PROPERTIES_NAME, MODE_PRIVATE);
				if (isDifferentDay(prefs)) 
				{
					pledgeAmount = 0;
					pledgeAmountFloor = 0;
					enablePledge(0);
					enablePledge(1);
					
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

				//	SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
				//	SimpleDateFormat datef = new SimpleDateFormat("yyyy-MM-dd");
					GregorianCalendar gc = new GregorianCalendar();

				//	Date d = new Date();
					//String dayOfTheWeek = sdf.format(d);
					//String dateString = datef.format(d);

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
					//previousBtn.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_menu_edit));
				}

				updatePledgeBar(0);
				updatePledgeBar(1);				
				
				boolean doSynchronization = false;
				Date currentTime = new Date();
				
				
				long diff;
				
				if(lastSyncTime == null)
				{
					doSynchronization = true;
				}
				else
				{
					diff = Math.abs(currentTime.getTime() - lastSyncTime.getTime());
					
					if(diff > Constants.BADGE_CHECK_INTERVAL) // if 10 minutes have not passed...
					{
						doSynchronization = true;
						
					}
					
				}
				
				if(doSynchronization)
				{	
					lastSyncTime = currentTime;
					updateBadgesWithSync(false);
	
					if (doHistoryCheck == 2) {
						for (int i = 0; i < 7; i++) {
							automaticUpdate(i, false);
						}
	
						doHistoryCheck = 0;
					}
					// else
					// {
	
					// }
	
					doHistoryCheck++;
					
					// schedule next check
				}
				else
				{
					
					showProgressDialog(false);
				}
			}

			if (!connectionErrorReceived) {
				// showProgressDialog(false);
				checkProgressHandler.postDelayed(this,
						Constants.BADGE_CHECK_INTERVAL);
			}
		}
	};
	
/*
	private void notifyBuddyChangeMessage(String msg) {
		NotificationCompat.Builder builder = new NotificationCompat.Builder(
				Main2Activity.this)
				.setAutoCancel(true)
				// .setLargeIcon(BitmapFactory.decodeResource(getResources(),
				// R.drawable.notification_icon))
				.setSmallIcon(R.drawable.notification_icon)
				.setContentTitle(getResources().getString(R.string.buddy_change))
				.setDefaults(Notification.DEFAULT_ALL).setContentText(msg);
		// Creates an explicit intent for an Activity in your app

		Intent resultIntent = new Intent(Main2Activity.this,
				SettingsActivity.class);

		// The stack builder object will contain an artificial back stack for
		// the
		// started Activity.
		// This ensures that navigating backward from the Activity leads out of
		// your application to the Home screen.
		TaskStackBuilder stackBuilder = TaskStackBuilder
				.create(Main2Activity.this);
		// Adds the back stack for the Intent (but not the Intent itself)
		stackBuilder.addParentStack(SettingsActivity.class);
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
*/
	public void automaticUpdate(final int pivotIndex,
			final boolean updateTheDisplay) {
		//SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
		SimpleDateFormat datef = new SimpleDateFormat("yyyy-MM-dd");
		GregorianCalendar gc = new GregorianCalendar();

		Date d = new Date();
		//String dayOfTheWeek = sdf.format(d);
		//String dateString = datef.format(d);

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

				if (success && Utils.isConnectionPresent(Main2Activity.this)) {
					if (updateTheDisplay) {
						updateDisplay(pivotIndex == 0);
					}

					// also fetch latest buddy's steps
					SyncBackendWithFitbitTask syncBuddyTask = new SyncBackendWithFitbitTask(
							fid, uid, dateOfInterest) {

						@Override
						protected void onPostExecute(Boolean success) {
							if (success
									&& Utils.isConnectionPresent(Main2Activity.this)) {
								// continue
								writeStepProgress(pivotIndex);
								writeFloorProgress(pivotIndex);

								if (updateTheDisplay) {
									updateDisplay(pivotIndex == 0);
								}
								// write the updates

								// os.setText(osStr);
								// of.setText(ofStr);
							} else if (!Utils
									.isConnectionPresent(Main2Activity.this)) {
								showSyncFailedDialog();
							}
						}
					};
					syncBuddyTask.execute();
				} else if (Utils.isConnectionPresent(Main2Activity.this)) {
					// There is a problem going on with the fitbit
					// authentication. Prompt the user!
					showProgressDialog(false);
					checkFitbitAuthentication();
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

		//showBuddyName(0); 
		
		
		if (fid < 1) {

			String email = AppContext.getInstance().getEmail();
			CheckGroupTask checkGroupTask = new CheckGroupTask(email,
					AppContext.getInstance().getUserId()) {
				@Override
				protected void onPostExecute(String result) 
				{

					if(!this.safeExecution)
					{
						if(!Utils.isConnectionPresent(Main2Activity.this))
						{
							displayConnectionErrorMessage();
						}
						return;
					}					
					
					// Log.d(TAG, "response: " + result);
					if (Utils.isInteger(result)) {
						int resultCode = Integer.parseInt(result);
						switch (resultCode) {
						case RESPONSE_CODE_NO_BUDDY:
							break;
						case RESPONSE_INCOMING_REQUEST_PENDING:
							break;
						case RESPONSE_WAITING_FOR_ACCEPT:
							break;
						default:
							AppContext.getInstance().setFriendId(resultCode);
							showBuddyName(resultCode);
							break;
						}
					} else {
						// error log_tag
						// Log.e(TAG, "an error has occurred");
						// Toast.makeText(Main2Activity.this,
						// R.string.error_general, Toast.LENGTH_LONG).show();
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

	private int spinnerNum(int pledgeAmount) {
		switch (pledgeAmount) {
		case 2000:
			return 0;
		case 5000:
			return 1;
		case 10000:
			return 2;
		case 20000:
			return 3;
		case 40000:
			return 4;
		case 60000:
			return 5;
		default:
			// error
			return -1;

		}
	}
	
	private int spinnerNumFloor(int pledgeAmountFloor) {
		switch (pledgeAmountFloor) {
		case 2:
			return 0;
		case 5:
			return 1;
		case 10:
			return 2;
		case 15:
			return 3;
		case 20:
			return 4;
		case 430:
			return 5;
		default:
			// error
			return -1;

		}
	}

	
	//ASMA START PLEDGE INITIALIZATION FUNCTIONS
	private void initializePledge(int type){
		switch(type){
		case 0: //step
			//SPINNER for steps
			spinner = (Spinner) findViewById(R.id.pledge_amount_spinner);
			// Create an ArrayAdapter using the string array and a default spinner
			// layout
			ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
					this, R.array.pledge_amount, R.layout.my_spinner_item);
			// Specify the layout to use when the list of choices appears
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			// Apply the adapter to the spinner
			spinner.setAdapter(adapter);
			spinner.setSelection(3);
			
			android.widget.LinearLayout.LayoutParams p = new android.widget.LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
			//p.setMargins(left, top, right, 2);
			spinner.setLayoutParams(p);
			break;
		case 1://floor
			//SPINNER for steps
			spinnerFloor = (Spinner) findViewById(R.id.pledge_amount_spinner_floor);
			// Create an ArrayAdapter using the string array and a default spinner
			// layout
			ArrayAdapter<CharSequence> adapterFloor = ArrayAdapter.createFromResource(
					this, R.array.pledge_amount_floor, R.layout.my_spinner_item);
			// Specify the layout to use when the list of choices appears
			adapterFloor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			// Apply the adapter to the spinner
			spinnerFloor.setAdapter(adapterFloor);
			spinnerFloor.setSelection(3);	

			break;
		default:
				System.out.println("ERRORRRR in PLEDGE MODE");
		}
	}
	
	//ASMA END PLEDGE INITIALIZATION FUNCTIONS		
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//creationComplete = false;
		isActive = true;
		checkStartDate();
		setContentView(R.layout.activity_main2);
		
		Log.e(TAG,"Initiated Main2Activity");
		
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		initHistory();
		
		// ///////////// ASMA PLEDGING START

		initializePledge(0); //steps
		initializePledge(1); //floors

		Intent igr = getIntent();
		
		boolean shallIUpdate = igr.getBooleanExtra("updatePledges", false);
		if(shallIUpdate)
		{
			//Toast.makeText(this, "Yeah, I should update", Toast.LENGTH_SHORT).show();	
		}
		else
		{
			//Toast.makeText(this, "Nope, no need to update", Toast.LENGTH_SHORT).show();
		}
			


		// ////////////ASMA PLEDGING END

		// ASMA LEADERBOARD START
		/*
		 * MainActivityCommunity.leaderboard = new Leaderboard(this,
		 * R.layout.leaderboard_listview_item_row, Constants.team_data);
		 * 
		 * GetListOfUsersTask listOfUsers = new GetListOfUsersTask() {
		 * 
		 * @Override protected void onPostExecute(Boolean result) {
		 * update the list properly MainActivityCommunity.leaderboard.update();
		 * 
		 * 
		 * } }; listOfUsers.execute();
		 */
		// ASMA LEADERBOARD END		
		
		
		DEFAULT_NUTRITION = getResources().getString(R.string.food);//"Food?";
		DEFAULT_EXERCISE = getResources().getString(R.string.activity);//"Activity?";
		DEFAULT_MOOD = getResources().getString(R.string.mood);//"Mood?";
		TOGETHER_STRING = getResources().getString(R.string.together);
		ALONE_STRING = getResources().getString(R.string.alone);
		DEFAULT_SOCIAL = getResources().getString(R.string.social);//"Social?";		
		
		positiveMoodTemplates = getResources().getStringArray(R.array.positiveMoodTemplates);
		negativeMoodTemplates = getResources().getStringArray(R.array.negativeMoodTemplates);
		moodIntesityTemplates = getResources().getStringArray(R.array.moodIntensityTemplates);
		foodTemplates = getResources().getStringArray(R.array.foodTemplates);
		sedentaryTemplates = getResources().getStringArray(R.array.sedentaryTemplates);
		exerciseTemplates = getResources().getStringArray(R.array.exerciseTemplates);
		socialTemplates = getResources().getStringArray(R.array.socialTemplates);
				
		
		
		
		if (connectionDialog != null) {
			connectionDialog.dismiss();
		}

		if (initialSyncDialog != null) {
			initialSyncDialog.dismiss();
		}

		if (Utils.isConnectionPresent(Main2Activity.this)) {
			// Log.d(TAG,"showProgressDialog called from onCreate()");
			showProgressDialog(true);
			displayConnectionErrorDialog(false);
		} else {
			displayConnectionErrorMessage();
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

		tab_host.setOnTabChangedListener(new OnTabChangeListener(){
			@Override
			public void onTabChanged(String tabId) {
			    if(tabId.equals("Steps")|| tabId.equals(getResources().getString(R.string.steps_tab))) {
			    	//if(time2refresh(lastRefStepL))
			    		Constants.VIEW_STEPS = true;
			    		
			    		updatePledgeProgressBar(0);
			    		
			    }
			    if(tabId.equals("Floors") || tabId.equals(getResources().getString(R.string.floors_tab))) {
			    	//if(time2refresh(lastRefFloorL))
			    	Constants.VIEW_STEPS = false;
			    	
			    	updatePledgeProgressBar(1);
			    }
			}});
		
		tab_host.setCurrentTab(Constants.VIEW_STEPS ? 0 : 1);
		datePivot = 0;


		// updateBadgesWithSync(true); 

		// SharedPreferences prefs =
		// getSharedPreferences(Constants.PROPERTIES_NAME,
		// Context.MODE_PRIVATE);
/*		for (int i = 0; i < 7; i++) {

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
		}*/

		refreshDone = false;
		Constants.loggedIn = true;
		checkProgressHandler = new Handler();

		Intent intent = igr;//getIntent();
		previousActivityIsMessagesActivity = intent.getBooleanExtra(
				"fromLogin", false) || Constants.newLogin;

		String msg = intent.getStringExtra("message");

		if (msg != null) {
			displayGameStartMessage(msg);
		}

		Constants.checkUserName();
		updatePledgeAfterSync(0); // updatePledgeBar
		updatePledgeAfterSync(1);
		View tb = findViewById(R.id.topBarChart);
		tbwidth = tb.getMeasuredWidth();

		checkValidityOfPreviousDay();
		showProgressDialog(false);
	//	creationComplete = true;
		updateBadgesWithSync(true);
		checkFitbitAuthentication();
		updatePledgeAfterSync(0); // updatePledgeBar
		updatePledgeAfterSync(1);

	}

	public void initHistory() 
	{
		
		myStepHistory = new int[7];
		myFloorHistory = new int[7];
		
		SharedPreferences prefs = getSharedPreferences(
				Constants.PROPERTIES_NAME, Context.MODE_PRIVATE);
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
	}

	public void recordValue(int amount, int activityType, int meOrBuddy)
	{
		SharedPreferences prefs = getSharedPreferences(
				Constants.PROPERTIES_NAME, Context.MODE_PRIVATE);	
		Editor editor = prefs.edit();
		
		String field = "";
		
		if(activityType == 0) // steps
		{
			if(meOrBuddy == 0) // me
			{
				field = Constants.PROP_KEY_USER_HISTORY_COMPLETED_STEPS + "_" + datePivot;
			}
			else
			{
				field = Constants.PROP_KEY_BUDDY_HISTORY_COMPLETED_STEPS + "_" + datePivot;
			}
		}
		else // floors
		{
			if(meOrBuddy == 0)
			{
				field = Constants.PROP_KEY_USER_HISTORY_COMPLETED_FLOORS + "_" + datePivot;
			}
			else
			{
				field = Constants.PROP_KEY_BUDDY_HISTORY_COMPLETED_FLOORS + "_" + datePivot;
			}
		}
		
		editor.putInt(field, amount);
		editor.commit();
	}
	
	public void checkValidityOfPreviousDay() {
	/*	SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
		SimpleDateFormat datef = new SimpleDateFormat("yyyy-MM-dd");*/
		GregorianCalendar gc = new GregorianCalendar();

		/*	Date d = new Date();
		String dayOfTheWeek = sdf.format(d);
		String dateString = datef.format(d);*/

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
		
		/*if (previousActivityIsMessagesActivity) {
			Intent intent = new Intent(Main2Activity.this, Main2Activity.class);
			intent.putExtra("fromLogin", true);
			startActivity(intent);
		}*/
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

		tab_host.setCurrentTab(Constants.VIEW_STEPS ? 0 : 1);
		

		
		if (initialSyncDialog != null) 
		{
			try
			{
				initialSyncDialog.dismiss();
			}
			catch(Exception e)
			{
				
			}
		}

		if (!Utils.isConnectionPresent(Main2Activity.this)) {
			// Toast.makeText(Main2Activity.this,
			// "No internet connection. Please check your connection.",
			// Toast.LENGTH_LONG).show();
			displayConnectionErrorMessage();
			return;
		} else {
			// Log.d(TAG,"showProgressDialog called from onResume().");
			showProgressDialog(true);
		}

		//int userId = AppContext.getInstance().getUserId();

		checkProgressHandler.postDelayed(checkForProgressTask, 0); // we
																	// immediately
																	// check for
																	// new
																	// messages
		Constants.checkUserName();
		//String email = AppContext.getInstance().getEmail();
		updatePledgeAfterSync(0);//updatePledgeBar(0);
		updatePledgeAfterSync(1);//updatePledgeBar(1);		
		
		refreshDone = false;
		

	}

	public void setBuddyName(String username) 
	{
		buddyName = username;
		Constants.buddyName = buddyName;
		//SharedPreferences prefs;
	}

	protected void showBuddyName(int friendId) 
	{
		if(Constants.BUDDY_NAME_ACQUIRED)
		{
			setBuddyName(Constants.buddyName);
			
			TextView text = (TextView) findViewById(R.id.textViewOtherSteps);
			text.setText(Constants.buddyName);
			text = (TextView) findViewById(R.id.textViewOtherFloors);
			text.setText(Constants.buddyName);			
		}
		Constants.BUDDY_NAME_ACQUIRED = false;
		//Log.d(TAG, "Main2Activity, friend Id: " + friendId); retrievedBuddyId = 

		RetrieveExistingBuddyInfoTask task = new RetrieveExistingBuddyInfoTask(AppContext.getInstance().getUserId())
		{
			protected void onPostExecute(Boolean result)
			{
				
				if(result)
				{

					
					AppContext.getInstance().setFriendId(this.retrievedBuddyId);
					TextView text = (TextView) findViewById(R.id.textViewOtherSteps);
					text.setText(this.retrievedBuddyName);
					text = (TextView) findViewById(R.id.textViewOtherFloors);
					text.setText(this.retrievedBuddyName);
					setBuddyName(this.retrievedBuddyName);
					Constants.BUDDY_NAME_ACQUIRED = true;
				}
				else if(!Utils.isConnectionPresent(Main2Activity.this))
				{
					displayConnectionErrorMessage();
					
					if(Constants.buddyName != null && !Constants.buddyName.equalsIgnoreCase(getResources().getString(R.string.main_loading))
							&& !Constants.buddyName.equalsIgnoreCase("loading..."))
					{
					//AppContext.getInstance().setFriendId(this.retrievedBuddyId);
					TextView text = (TextView) findViewById(R.id.textViewOtherSteps);
					text.setText(Constants.buddyName);
					text = (TextView) findViewById(R.id.textViewOtherFloors);
					text.setText(Constants.buddyName);
					//setBuddyName(this.retrievedBuddyName);					
					}
					
				}
			}
		};
		task.execute();		
		
		/*
		RetrieveBuddyNameTask task = new RetrieveBuddyNameTask(friendId) {

			@Override
			protected void onPostExecute(String username) {
				Log.d(TAG, "Main2Activity, showBuddyName: " + username);
				if (username != null && Constants.BUDDY_NAME_ACQUIRED) {
					if (!username.startsWith("Error")
							&& !username.contains("error")
							&& !username.contains("Error")) {
						TextView text = (TextView) findViewById(R.id.textViewOtherSteps);
						text.setText(username + "'s");
						text = (TextView) findViewById(R.id.textViewOtherFloors);
						text.setText(username + "'s");
						setBuddyName(username);
						Constants.BUDDY_NAME_ACQUIRED = true;
					}
				}
				else
				{
					TextView text = (TextView) findViewById(R.id.textViewOtherSteps);
					text.setText(Constants.buddyName + "'s");
					text = (TextView) findViewById(R.id.textViewOtherFloors);
					text.setText(Constants.buddyName + "'s");
					setBuddyName(Constants.buddyName);
				}
			}
		};
		task.execute();*/

		/*
		 * RetrieveBuddyNameTask task= new RetrieveBuddyNameTask(friendId) {
		 * 
		 * @Override protected void onPostExecute(String username) { if(username
		 * != null && Constants.BUDDY_NAME_ACQUIRED) {
		 * if(!username.startsWith("Error") && !username.contains("error") &&
		 * !username.contains("Error")){ // TextView text= (TextView)
		 * findViewById(R.id.textViewPlayer); // text.setText(username);
		 * TextView text= (TextView) findViewById(R.id.textViewOtherSteps);
		 * text.setText(username + "'s"); text= (TextView)
		 * findViewById(R.id.textViewOtherFloors); text.setText(username +
		 * "'s"); setBuddyName(username); } } } }; task.execute();
		 */
	}

	private void showProgressDialog(final boolean show) 
	{
		try
		{
			if (!isDisplayingConnectionErrorDialog
					|| !isDisplayingAuthorizationDialog) {
				if (show) {
	
					if (isActive) {
						initialSyncDialog = ProgressDialog.show(this,
								getResources().getString(R.string.main_synchronization_title),
								getResources().getString(R.string.main_synchronization_content));
					}
				} else if (initialSyncDialog != null) 
				{
					try
					{
						initialSyncDialog.dismiss();
					}
					catch(Exception e)
					{
						
					}
					initialSyncDialog = null;
	
					if (congratulationsDisplayRequest[0] || congratulationsDisplayRequest[1]) {
					//	congratulationsDisplayRequest = false;
						
						//displayCongratulations()
						if(congratulationsDisplayRequest[0])
						{
							congratulationsDisplayRequest[0] = false;
							displayCongratulations(0);
						}
						else if(congratulationsDisplayRequest[1])
						{
							congratulationsDisplayRequest[1] = false;
							displayCongratulations(1);
						}
						
					}
					else if(pledgeCongratulationsDisplayRequest)
					{
						pledgeCongratulationsDisplayRequest = false;
					}
	
				}
			}
		}
		catch(Exception e)
		{
			
		}
		
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		updateDisplay(datePivot == 0);
	}

	// ASMA GO TO COMMUNITY START
	public void onCommunityButtonClicked(View v) {

		/*for (int i = 0; i < Constants.team_data.length; i++) {
			Constants.team_data[i].score = "Loading...";
			Constants.team_data[i].icon = 0;
			Constants.team_data[i].name = "";
		}*/

		//enablePledge(1);
		startCommunityActivity();

	}
	
	public void startCommunityActivity()
	{
		Intent community = new Intent(this, MainActivityCommunity.class);
		community.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(community);		
	}

	// ASMA GO TO COMMUNITY END
	
	public void updatePledgeAfterSync(int type)
	{
		switch(type)
		{
		case 0:
			RetrieveStepCountTask task = new RetrieveStepCountTask(AppContext
					.getInstance().getUserId(), 0) {

				@Override
				protected void onPostExecute(final Integer myStepCount) 
				{

					
					if (this.safeExecution && Utils.isConnectionPresent(Main2Activity.this)) 
					{
						
						int fid = getFriendId();
						RetrieveStepCountTask task2 = new RetrieveStepCountTask(
								fid, 0) {

							@Override
							protected void onPostExecute(Integer friendStepCount) 
							{

								if (this.safeExecution && Utils.isConnectionPresent(Main2Activity.this)) 
								{
									mySteps = myStepCount;
									buddySteps = friendStepCount;
									recordValue(myStepCount, 0, 0);
									recordValue(friendStepCount, 0, 1);
								}
								else
								{
							//		if(myStepHistory[0] > 0)
							//		{
										mySteps = myStepHistory[0];
							//		}
							//		if(buddyStepHistory[0] > 0)
							//		{
										buddySteps = buddyStepHistory[0];
							//		}
								/*	mySteps = myStepHistory[0];
									buddySteps = buddyStepHistory[0];*/
								}
								updatePledgeProgressBar(0);
							}
						};
						
						task2.execute();
					}
					else
					{
				//		if(myStepHistory[0] > 0)
				//		{
							mySteps = myStepHistory[0];
				//		}
				//		if(buddyStepHistory[0] > 0)
				//		{
							buddySteps = buddyStepHistory[0];
				//		}
						updatePledgeProgressBar(0);
					/*	mySteps = myStepHistory[0];
						buddySteps = buddyStepHistory[0];*/
					}					
				}
			};
				
			task.execute();			
			break;
		case 1:
			RetrieveFloorCountTask ftask = new RetrieveFloorCountTask(AppContext
					.getInstance().getUserId(), 0) {

				@Override
				protected void onPostExecute(final Integer myFloorCount) 
				{
					if (this.safeExecution && Utils.isConnectionPresent(Main2Activity.this)) 
					{
						int fid = getFriendId();
						RetrieveFloorCountTask ftask2 = new RetrieveFloorCountTask(
								fid, 0) {

							@Override
							protected void onPostExecute(Integer friendFloorCount) 
							{

								if (this.safeExecution && Utils.isConnectionPresent(Main2Activity.this)) 
								{
									myFloors = myFloorCount;
									buddyFloors = friendFloorCount;
									recordValue(myFloorCount, 1, 0);
									recordValue(friendFloorCount, 1, 1);									
								}
								else
								{
								//	if(myFloorHistory[0] > 0)
								//	{
										myFloors = myFloorHistory[0];
								//	}
								//	if(buddyFloorHistory[0] > 0)
								//	{
										buddyFloors = buddyFloorHistory[0];
								//	}
								/*	mySteps = myStepHistory[0];
									buddySteps = buddyStepHistory[0];*/
								}		
								updatePledgeProgressBar(1);
							}
						};
						
						ftask2.execute();
					}
					else
					{
					//	if(myFloorHistory[0] > 0)
					//	{
							myFloors = myFloorHistory[0];
					//	}
					//	if(buddyFloorHistory[0] > 0)
					//	{
							buddyFloors = buddyFloorHistory[0];
					//	}
						updatePledgeProgressBar(1);
					/*	mySteps = myStepHistory[0];
						buddySteps = buddyStepHistory[0];*/
					}	
				}
			};
				
			ftask.execute();					
		}
	}
	
	// ASMA PLEDGE FUNCTIONS START

	public void updatePledgeProgressBar(int type) 
	{
		SharedPreferences prefs = getSharedPreferences(
				Constants.PROPERTIES_NAME, Context.MODE_PRIVATE);
		int pledgeBarWidth;
		switch(type){
		case 0:
			if (pledgeAmount == 0)
			{
			//	Toast.makeText(this, "Quit because pledgeAmount = 0", Toast.LENGTH_SHORT).show();
				return;
			}
			LinearLayout pledgeContainer = (LinearLayout) findViewById(R.id.mypledgecontainer);

			
			/*if (mySteps == null)
				mySteps = 0;

			if (buddySteps == null)
				buddySteps = 0;*/

		//	Toast.makeText(this, "mySteps = " + mySteps + " buddysteps = " + buddySteps, Toast.LENGTH_SHORT).show();
			
			
			//Log.d(TAG,"pledgeContainer, measured width: " + pledgeContainer.getMeasuredWidth());

	
			
			if(pledgeContainer.getWidth() > 0)
			{
				pledgeBarWidth = pledgeContainer.getWidth();
				Editor editor = prefs.edit();
				editor.putInt(Constants.PLEDGE_BAR_LENGTH, pledgeBarWidth);
				editor.commit();
			}
			else
			{
				pledgeBarWidth = prefs.getInt(Constants.PLEDGE_BAR_LENGTH, 0);
			}
			
			int width1 = (mySteps * pledgeBarWidth
					/ Main2Activity.pledgeAmount);
			int width2 = (buddySteps * pledgeBarWidth
					/ Main2Activity.pledgeAmount);
			int width3;
			//width1 = (int)(width1*0.97);
			//width2 = (int)(width2*0.80);
			if(mySteps + buddySteps >= Main2Activity.pledgeAmount && (mySteps + buddySteps > 0))
			{
				width1= (int)((mySteps * pledgeBarWidth)/(mySteps + buddySteps));
				width2= (int)(0.97*(buddySteps * pledgeBarWidth)/(mySteps + buddySteps));
				width3 = 0;
			}
			else
			{
				width3 = (int)(0.94*(pledgeBarWidth - width1 - width2));
			}
			
			if(width3 <= 0)
			{
				//width3 = 2;
				//width3 =(int)( 0.03*pledgeBarWidth);
				width3 = 0;
				//width2 = (int)(0.97*(pledgeBarWidth - width1 - width3));
			}
			
			if(width2 <= 0)
			{
				width2 = (int)(0.03*pledgeBarWidth);
			}
			if(width1 <= 0)
			{
				width1 = (int)(0.03*pledgeBarWidth);
			}

			/*if(width1 >= pledgeBarWidth)
			{
				width1 = (int)(0.97*pledgeBarWidth);
				width2 = (int)(0.03*pledgeBarWidth);
				width3 = 0;
			}*/
			
			
			//width1 -=10;
			//width2 -=10;
			
			View pledgeBarMineMask = (View) findViewById(R.id.pledgeBarMineMask);
			//pledgeBarMineMask.setBackgroundDrawable(getResources().getDrawable(R.drawable.my_progress_mask_pledge));
			pledgeBarMineMask.setLayoutParams(new LinearLayout.LayoutParams(width1,
					LayoutParams.MATCH_PARENT));

			View pledgeBarMiddleMask = (View) findViewById(R.id.pledgeBarMiddleMask);
			pledgeBarMiddleMask.setLayoutParams(new LinearLayout.LayoutParams(
					width3, LayoutParams.MATCH_PARENT));

			View pledgeBarOtherMask = (View) findViewById(R.id.pledgeBarOtherMask);
			pledgeBarOtherMask.setLayoutParams(new LinearLayout.LayoutParams(
					width2, LayoutParams.MATCH_PARENT));			
			//pledgeBarOtherMask.setBackgroundDrawable(getResources().getDrawable(R.drawable.friend_progress_mask_pledge));


			TextView tmpTxt = (TextView) findViewById(R.id.textGoalPercent);

			int perc = (mySteps + buddySteps) * 100 / pledgeAmount;
			if (perc > 100)
				perc = 100;
			tmpTxt.setText(perc + "");
			if (!successfullPledge && (perc == 100))
				onPledgeSuccessfull(0);

			break;
		case 1:
			if (pledgeAmountFloor == 0)
				return;
			LinearLayout pledgeContainerFloor = (LinearLayout) findViewById(R.id.mypledgecontainerFloor);

			
			
			if(pledgeContainerFloor.getWidth() > 0)
			{
				pledgeBarWidth = pledgeContainerFloor.getWidth();
				Editor editor = prefs.edit();
				editor.putInt(Constants.PLEDGE_BAR_LENGTH, pledgeBarWidth);
				editor.commit();
			}
			else
			{
				pledgeBarWidth = prefs.getInt(Constants.PLEDGE_BAR_LENGTH, 0);
			}			
			
			/*if (myFloors == null)
				myFloors = 0;

			if (buddyFloors == null)
				buddyFloors = 0;*/

			int width1Floor = myFloors * pledgeBarWidth
					/ Main2Activity.pledgeAmountFloor;
			int width2Floor = buddyFloors * pledgeBarWidth
					/ Main2Activity.pledgeAmountFloor;
			int width3Floor;
			if(myFloors + buddyFloors >= Main2Activity.pledgeAmountFloor && (myFloors + buddyFloors > 0))
			{
				width1= (int)((myFloors * pledgeBarWidth)/(myFloors + buddyFloors));
				width2= (int)(0.97*(buddyFloors * pledgeBarWidth)/(myFloors + buddyFloors));
				width3Floor = 0;
			}			
			else
			{
				width3Floor = (int)(0.94*(pledgeBarWidth - width1Floor - width2Floor));
			}
			//int width3Floor = pledgeBarWidth - width1Floor - width2Floor;
			
			if(width3Floor <= 0)
			{
				width3Floor =0;//(int)( 0.03*pledgeBarWidth);
				//width2Floor = (int)(0.97*(pledgeBarWidth - width3Floor - width3Floor));
			}
			
			
			if(width2Floor <= 0)
			{
				width2Floor = (int)(0.03*pledgeBarWidth);
			}
			if(width1Floor <= 0)
			{
				width1Floor = (int)(0.03*pledgeBarWidth);
			}	
			
			View pledgeBarMineMaskFloor = (View) findViewById(R.id.pledgeBarMineMaskFloor);
			pledgeBarMineMaskFloor.setLayoutParams(new LinearLayout.LayoutParams(width1Floor,
					LayoutParams.MATCH_PARENT));

			View pledgeBarMiddleMaskFloor = (View) findViewById(R.id.pledgeBarMiddleMaskFloor);
			pledgeBarMiddleMaskFloor.setLayoutParams(new LinearLayout.LayoutParams(
					width3Floor, LayoutParams.MATCH_PARENT));

			View pledgeBarOtherMaskFloor = (View) findViewById(R.id.pledgeBarOtherMaskFloor);
			pledgeBarOtherMaskFloor.setLayoutParams(new LinearLayout.LayoutParams(
					width2Floor, LayoutParams.MATCH_PARENT));

			TextView tmpTxtFloor = (TextView) findViewById(R.id.textGoalPercentFloor);

			int percFloor = (myFloors + buddyFloors) * 100 / pledgeAmountFloor;
			if (percFloor > 100)
				percFloor = 100;
			tmpTxtFloor.setText(percFloor + "");
			if (!successfullPledgeFloor && (percFloor == 100))
				onPledgeSuccessfull(1);

			break;
		default:
				System.out.println("ERROR in PLEDGE mode");
		}
		
		// updateDisplay(false);

		/*
		 * View pledgeBox=(View) findViewById(R.id.pledgeBox);
		 * pledgeBox.setVisibility(View.INVISIBLE);
		 * pledgeBox.setVisibility(View.VISIBLE); pledgeBox.invalidate();
		 */

	}

	private void enablePledge(int type) {
		switch (type){
		case 0: //steps
			// activate spinner
			spinner.setEnabled(true);
			spinner.setVisibility(Spinner.VISIBLE);
			spinner.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT));//dpToPx(120), dpToPx(50) ) );
			 /* int optionId = someExpression ? R.layout.option1 : R.layout.option2;

			    View C = findViewById(R.id.C);
			    ViewGroup parent = (ViewGroup) C.getParent();
			    int index = parent.indexOfChild(C);
			    parent.removeView(C);
			    C = getLayoutInflater().inflate(optionId, parent, false);
			    parent.addView(C, index);*/			
			
			
			// button becomes visible
			Button tmpButton = (Button) findViewById(R.id.pledgeButton);
			LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(dpToPx(120), dpToPx(38));
			//params.setMargins(15, 0, 0, 0);
			tmpButton.setLayoutParams(params);

			// bar becomes invisible
			RelativeLayout tmpBar = (RelativeLayout) findViewById(R.id.pledgeBarChart);
			tmpBar.setLayoutParams(new LinearLayout.LayoutParams(
					RelativeLayout.LayoutParams.MATCH_PARENT, 0));
			
			TextView tv = (TextView)findViewById(R.id.pledgeAmountText);
			tv.setText("");
			tv.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
			
			break;
		case 1://floors
			// activate spinner
			spinnerFloor.setEnabled(true);
			spinnerFloor.setVisibility(Spinner.VISIBLE);
			
			spinnerFloor.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(120), dpToPx(50)) );
			
			// button becomes visible
			Button tmpButtonFloor = (Button) findViewById(R.id.pledgeButtonFloor);
			LinearLayout.LayoutParams paramsFloor=new LinearLayout.LayoutParams(dpToPx(120), dpToPx(38));
			//paramsFloor.setMargins(15, 0, 0, 0);
			tmpButtonFloor.setLayoutParams(paramsFloor);

			// bar becomes invisible
			RelativeLayout tmpBarFloor = (RelativeLayout) findViewById(R.id.pledgeBarChartFloor);
			tmpBarFloor.setLayoutParams(new LinearLayout.LayoutParams(
					RelativeLayout.LayoutParams.MATCH_PARENT, 0));
			
			TextView tvf = (TextView)findViewById(R.id.pledgeAmountFloorText);
			tvf.setText("");
			tvf.setLayoutParams(new LinearLayout.LayoutParams(0, 0));			
			break;
		default:
				System.out.println("Error in pledge mode");
		}
		

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
	
	private void disablePledge(int type) {
		switch(type){
		case 0: //step
			// deactivate spinner
			spinner.setEnabled(false);
			spinner.setVisibility(Spinner.INVISIBLE);
		/*	ViewGroup parent = (ViewGroup) spinner.getParent();
			int index = parent.indexOfChild(spinner);
			spinnerIndex = index;
			spinnerParent = parent;
			parent.removeView(spinner);*/
			//parent.addView(,index);
			
			TextView tv = (TextView)findViewById(R.id.pledgeAmountText);
			tv.setText(" " + pledgeAmount);
			LinearLayout.LayoutParams mparams =new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			tv.setLayoutParams(mparams);

				
			
			// button becomes invisible
			Button tmpButton = (Button) findViewById(R.id.pledgeButton);
			LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(153, 0);
			params.setMargins(15, 0, 0, 0);
			tmpButton.setLayoutParams(params);
			spinner.setLayoutParams(new LinearLayout.LayoutParams(0,0));
			// bar becomes visible

			RelativeLayout tmpBar = (RelativeLayout) findViewById(R.id.pledgeBarChart);
			tmpBar.setLayoutParams(new LinearLayout.LayoutParams(
					RelativeLayout.LayoutParams.MATCH_PARENT, dpToPx(30))); // 40
			break;
		case 1: //floor
			// deactivate spinner
			spinnerFloor.setEnabled(false);
			spinnerFloor.setVisibility(Spinner.INVISIBLE);
			
			TextView tvf = (TextView)findViewById(R.id.pledgeAmountFloorText);
			tvf.setText(" " + pledgeAmountFloor);
			LinearLayout.LayoutParams mparamsf =new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			tvf.setLayoutParams(mparamsf);		
			
			// button becomes invisible
			Button tmpButtonFloor = (Button) findViewById(R.id.pledgeButtonFloor);
			LinearLayout.LayoutParams paramsFloor=new LinearLayout.LayoutParams(153, 0);
			paramsFloor.setMargins(15, 0, 0, 0);
			tmpButtonFloor.setLayoutParams(paramsFloor);
			spinnerFloor.setLayoutParams(new LinearLayout.LayoutParams(0,0));
			// bar becomes visible

			RelativeLayout tmpBarFloor = (RelativeLayout) findViewById(R.id.pledgeBarChartFloor);
			tmpBarFloor.setLayoutParams(new LinearLayout.LayoutParams(
					RelativeLayout.LayoutParams.MATCH_PARENT, dpToPx(30))); // 40
			break;
		default:
			System.out.println("Error in pledge mode");
		}
		

	}

	
	
	public void updatePledgeBar(final int type) {
		SharedPreferences prefs = getSharedPreferences(
				Constants.PROPERTIES_NAME, Context.MODE_PRIVATE);
		switch(type){
		case 0:
			//if it is a new day, reset pledge amount

			if (isDifferentDay(prefs))
				pledgeAmount = 0;
			//look at database. Has the other teammate pledged? if so, update pledgeAmount
			if (pledgeAmount == 0) {
				GetStepPledgeTask task = new GetStepPledgeTask(AppContext
						.getInstance().getUserId()) {

					protected void onPostExecute(Pledge lastPledge) {
						
						if(!this.safeExecution)
						{
							if(!Utils.isConnectionPresent(Main2Activity.this))
							{
								displayConnectionErrorMessage();
							}
							return;
						}
						// it is a new day, reset data
						
						Calendar cal = Calendar.getInstance();
						SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
						String TODAY = formatter.format(cal.getTime());

						if (lastPledge != null && lastPledge.date.substring(0, 10).equals(TODAY))
							pledgeAmount = lastPledge.amount;
						
						//THE TEAMMATE PLEDGED
						if(pledgeAmount>0){
							spinner.setSelection(spinnerNum(pledgeAmount));
							disablePledge(type);
							updatePledgeAfterSync(type);
						}else{//NOT PLEDGED YET
							spinner.setSelection(3);
							enablePledge(type);
						}
					}
				};
				task.execute();
			} else {//THE CURRENT USER PLEDGED
				
				spinner.setSelection(spinnerNum(pledgeAmount));
				disablePledge(type);
				updatePledgeProgressBar(type);
				
			}

			break;
		case 1: //floor
			//if it is a new day, reset pledge amount

			if (isDifferentDay(prefs))
				pledgeAmountFloor = 0;
			//look at database. Has the other teammate pledged? if so, update pledgeAmount
			if (pledgeAmountFloor == 0) {
				GetFloorPledgeTask task = new GetFloorPledgeTask(AppContext
						.getInstance().getUserId()) {

					protected void onPostExecute(Pledge lastPledge) {
						// it is a new day, reset data
						
						if(!this.safeExecution)
						{
							if(!Utils.isConnectionPresent(Main2Activity.this))
							{
								displayConnectionErrorMessage();
							}
							return;
						}
						
						Calendar cal = Calendar.getInstance();
						SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
						String TODAY = formatter.format(cal.getTime());

						if (lastPledge != null && lastPledge.date.substring(0, 10).equals(TODAY))
							pledgeAmountFloor = lastPledge.amount;
						
						//THE TEAMMATE PLEDGED
						if(pledgeAmountFloor>0){
							spinnerFloor.setSelection(spinnerNumFloor(pledgeAmountFloor));
							disablePledge(type);
							updatePledgeAfterSync(type);
						}else{//NOT PLEDGED YET
							spinnerFloor.setSelection(3);
							enablePledge(type);
						}
					}
				};
				task.execute();
			} else {//THE CURRENT USER PLEDGED
				spinnerFloor.setSelection(spinnerNumFloor(pledgeAmountFloor));
				disablePledge(type);
				updatePledgeProgressBar(type);
				
			}
			break;
		default:
				System.out.println("Error in pledge mode");
		}
		
	}

	public void onPledgeConfirmButtonClicked(View v)
	{
		if(!Utils.isConnectionPresent(Main2Activity.this))
		{
			isDisplayingConnectionErrorDialog = false;
			displayConnectionErrorMessage();
		}
		else
		{
			if(pledgeMode == 0)
			{
				pledgeAmount = Constants.PLEDGE_AMOUNT[spinner.getSelectedItemPosition()];
				SetStepPledgeTask task = new SetStepPledgeTask(
						AppContext.getInstance().getFriendId(),
						pledgeAmount) {
					@Override
					protected void onPostExecute(Boolean result) {
						
						if(!this.safeExecution)
						{
							if(!Utils.isConnectionPresent(Main2Activity.this))
							{
								displayConnectionErrorMessage();
							}
							return;
						}
						
						SetStepPledgeTask task2 = new SetStepPledgeTask(
								AppContext.getInstance().getUserId(),
								pledgeAmount) {
							@Override
							protected void onPostExecute(Boolean result) {
								
								if(!this.safeExecution)
								{
									if(!Utils.isConnectionPresent(Main2Activity.this))
									{
										displayConnectionErrorMessage();
									}
									return;
								}
								updatePledgeProgressBar(0);
							}
						};
						task2.execute();
					}
				};
				task.execute();
	
				updatePledgeBar(0);
			}
			else
			{
				pledgeAmountFloor = Constants.PLEDGE_AMOUNT_FLOOR[spinnerFloor.getSelectedItemPosition()];
	  			SetFloorPledgeTask task = new SetFloorPledgeTask(
	  					AppContext.getInstance().getFriendId(),
	  					pledgeAmountFloor) {
	  				@Override
	  				protected void onPostExecute(Boolean result) 
	  				{
						if(!this.safeExecution)
						{
							if(!Utils.isConnectionPresent(Main2Activity.this))
							{
								displayConnectionErrorMessage();
							}
							return;
						}
	  					
	  					SetFloorPledgeTask task2 = new SetFloorPledgeTask(
	  							AppContext.getInstance().getUserId(),
	  							pledgeAmountFloor) {
	  						@Override
	  						protected void onPostExecute(Boolean result) 
	  						{
	  							if(!this.safeExecution)
	  							{
	  								if(!Utils.isConnectionPresent(Main2Activity.this))
	  								{
	  									displayConnectionErrorMessage();
	  								}
	  								return;
	  							}	  							
	  							updatePledgeProgressBar(1);
	  						}
	  					};
	  					task2.execute();
	  				}
	  			};
	  			task.execute();
	
	  			updatePledgeBar(1);			
			}
		}
		
		if(pledgeAlertDialog != null)
		{
			if(pledgeAlertDialog.isShowing())
			{
				try
				{
					pledgeAlertDialog.dismiss();
				}
				catch(Exception e)
				{
					
				}
				pledgeAlertDialog = null;
			}
		}
		
	}
	
	public void preparePledgeConfirmDialog(int pledgeType)
	{
		pledgeMode = pledgeType;
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.activity_pledgeconfirm_dialog,
				null);
		AlertDialog.Builder builder = new AlertDialog.Builder(this)
				.setView(layout);
		
		if(pledgeAlertDialog != null)
		{
			if(pledgeAlertDialog.isShowing())
			{
				try
				{
					pledgeAlertDialog.dismiss();
				}
				catch(Exception e)
				{
					
				}
				pledgeAlertDialog = null;
			}
		}
		
		pledgeAlertDialog = builder.create();		
		
		TextView tv_h = (TextView) layout.findViewById(R.id.alertinfo3);
		
		if(pledgeType == 0)
		{
			tv_h.setText(getResources().getString(R.string.pledge_confirm_content_step )+ ": " + Constants.PLEDGE_AMOUNT[spinner.getSelectedItemPosition()] + " " + getResources().getString(R.string.steps));
		}
		else
		{
			tv_h.setText(getResources().getString(R.string.pledge_confirm_content_floor) + ": " + Constants.PLEDGE_AMOUNT_FLOOR[spinnerFloor.getSelectedItemPosition()] + " " + getResources().getString(R.string.floors));
		}
		
		pledgeAlertDialog.show();
		
		
		
	}
	
	public void onPledgeButtonClicked(View v) 
	{

		if (spinner.isEnabled()) {
			
			preparePledgeConfirmDialog(0);
			/*
			pledgeMode = 0;
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			
			builder.setTitle(getResources().getString(R.string.pledge_confirm_title));
			builder.setMessage(getResources().getString(R.string.pledge_confirm_content_step )+ ": " + Constants.PLEDGE_AMOUNT[spinner.getSelectedItemPosition()] + " " + getResources().getString(R.string.steps));
			builder.setNegativeButton(getResources().getString(R.string.cancel_button), null);
			builder.setPositiveButton(getResources().getString(R.string.ok_button_string), new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) 
				{
					pledgeAmount = Constants.PLEDGE_AMOUNT[spinner.getSelectedItemPosition()];
	       			SetStepPledgeTask task = new SetStepPledgeTask(
	       					AppContext.getInstance().getFriendId(),
	       					pledgeAmount) {
	       				@Override
	       				protected void onPostExecute(Boolean result) {
	       					SetStepPledgeTask task2 = new SetStepPledgeTask(
	       							AppContext.getInstance().getUserId(),
	       							pledgeAmount) {
	       						@Override
	       						protected void onPostExecute(Boolean result) {
	       							updatePledgeProgressBar(0);
	       						}
	       					};
	       					task2.execute();
	       				}
	       			};
	       			task.execute();
	
	       			updatePledgeBar(0);
					
				}
			});
			
			builder.create().show();*/
			

		}

	}
	
	public void onPledgeButtonClickedFloor(View v) {
		

		if (spinnerFloor.isEnabled()) {
			
			preparePledgeConfirmDialog(1);
			/*
			pledgeMode = 1;
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			
			builder.setTitle(getResources().getString(R.string.pledge_confirm_title));
			builder.setMessage(getResources().getString(R.string.pledge_confirm_content_floor) + ": " + Constants.PLEDGE_AMOUNT_FLOOR[spinnerFloor.getSelectedItemPosition()] + " " + getResources().getString(R.string.floors));
			builder.setNegativeButton(getResources().getString(R.string.cancel_button), null);
			builder.setPositiveButton(getResources().getString(R.string.ok_button_string), new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) 
				{
					pledgeAmountFloor = Constants.PLEDGE_AMOUNT_FLOOR[spinnerFloor.getSelectedItemPosition()];
              			SetFloorPledgeTask task = new SetFloorPledgeTask(
              					AppContext.getInstance().getFriendId(),
              					pledgeAmountFloor) {
              				@Override
              				protected void onPostExecute(Boolean result) {
              					SetFloorPledgeTask task2 = new SetFloorPledgeTask(
              							AppContext.getInstance().getUserId(),
              							pledgeAmountFloor) {
              						@Override
              						protected void onPostExecute(Boolean result) 
              						{
              							updatePledgeProgressBar(1);
              						}
              					};
              					task2.execute();
              				}
              			};
              			task.execute();

              			updatePledgeBar(1);
					
				}
			});			
			builder.create().show();*/

		}

	}

	public void onPledgeSuccessfull(int type) 
	{
		if(datePivot!=0)
			return;

		if(pledgeAlertDialog != null)
			return;
		
		if(isDisplayingAuthorizationDialog || isDisplayingConnectionErrorDialog || isDisplayingBuddyDialog)
		{
			return;
		}
		
	/*	else if (initialSyncDialog != null) // wait for the progress bar to
			// finish.
		{
			//pledgeCongratulationsDisplayRequest = true;
			return;
		}*/
		
		SharedPreferences prefs = getSharedPreferences(Constants.PROPERTIES_NAME,Context.MODE_PRIVATE);
		
		
		if(stepsCongratsTime == null)
		{
			stepsCongratsTime = Constants.readStepPledgeCongratsDate(prefs);
		}
		
		Date d = new Date();
		if(type == 0 && stepsCongratsTime != null)
		{
			
			
			Calendar cal1 = Calendar.getInstance();
			Calendar cal2 = Calendar.getInstance();
			cal1.setTime(d);
			cal2.setTime(stepsCongratsTime);
			boolean sameDay = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
			                  cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);			
		
			if(sameDay)
				return;
		}		
		
		if(floorCongratsTime == null)
		{
			floorCongratsTime = Constants.readFloorPledgeCongratsDate(prefs);
		}
		
		if(type == 1 && floorCongratsTime != null)
		{
			
			
			Calendar cal1 = Calendar.getInstance();
			Calendar cal2 = Calendar.getInstance();
			cal1.setTime(d);
			cal2.setTime(floorCongratsTime);
			boolean sameDay = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
			                  cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);			
		
			if(sameDay)
				return;
		}
		
		
		
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.activity_popup_pledge_dialog,
				null);
		AlertDialog.Builder builder = new AlertDialog.Builder(this)
				.setView(layout);
		pledgeAlertDialog = builder.create();
		TextView tv = (TextView) layout.findViewById(R.id.pledge_alertinfo);
		
		String pledge_congrats = getResources().getString(R.string.congrats_title) + "\n\n";
		//String pledge_msg;
		
		switch(type){
		case 0:

			successfullPledge = true;
			Constants.embarkStepPledgeCongratsDate(prefs);
			stepsCongratsTime = d;
			pledge_congrats = getResources().getString(R.string.pledge_congrats_steps_msg);
			tv.setText(pledge_congrats);
			pledgeAlertDialog.show();
			
			break;
		case 1:

			successfullPledgeFloor = true;
			Constants.embarkFloorPledgeCongratsDate(prefs);
			floorCongratsTime = d;
			pledge_congrats = getResources().getString(R.string.pledge_congrats_floors_msg);
			tv.setText(pledge_congrats);
			pledgeAlertDialog.show();
			break;
		default:
				System.out.println("Error in pledge mode");
		}

		
	}

	// ASMA PLEDGE FUNCTIONS END	
	
	public void onTauntButtonClicked(View v) {
		if(Utils.isConnectionPresent(Main2Activity.this))
		{
			Intent i = new Intent(this, MessagesActivity.class);
			i.putExtra("cheer", false);
			startActivity(i);
		}
		else
		{
			isDisplayingConnectionErrorDialog = false;
			displayConnectionErrorMessage();
		}
	}

	public void onCheerButtonClicked(View v) {
		
		if(Utils.isConnectionPresent(Main2Activity.this))
		{
			Intent i = new Intent(this, MessagesActivity.class);
			i.putExtra("cheer", true);
			startActivity(i);
		}
		else
		{
			isDisplayingConnectionErrorDialog = false;
			displayConnectionErrorMessage();
		}
	}

	public void onHistoryButtonClicked(View v) {
		
		
		if(Utils.isConnectionPresent(Main2Activity.this))
		{
			Intent i = new Intent(this, HistoryActivity.class);
			i.putExtra("cheer", true);
			startActivity(i);
		}
		else
		{
			isDisplayingConnectionErrorDialog = false;
			displayConnectionErrorMessage();
		}
	}

	public void onBadgeHistoryButtonClicked(View v) {
		Intent i = new Intent(this, BadgesActivity.class);
		i.putExtra("cheer", true);
		startActivity(i);
	}

	public static void checkLogCompleteness() {
		/*
		 * if(moodSet && socialSet && activitySet) { Button confirmButton =
		 * (Button)findViewById(R.id.confirmButton);
		 * confirmButton.setVisibility(Button.VISIBLE);
		 * confirmButton.setClickable(true); } else { String missingFields =
		 * "Missing Fields:";
		 * 
		 * if(!moodSet) { missingFields += " Mood"; }
		 * 
		 * if(!socialSet) { missingFields += " Social"; }
		 * 
		 * if(!activitySet) { missingFields += " Activity"; }
		 * 
		 * Log.d(TAG, missingFields); }
		 */
	}

	// Adapter Class
	/*
	private class MyCustomAdapter extends BaseAdapter {

		private static final int TYPE_ITEM = 0;
		private static final int TYPE_SEPARATOR = 1;
		private static final int TYPE_MAX_COUNT = TYPE_SEPARATOR + 1;

		private ArrayList<String> mData = new ArrayList<String>();
		private LayoutInflater mInflater;

		private TreeSet<Integer> mSeparatorsSet = new TreeSet<Integer>();

		public MyCustomAdapter() {
			mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		public void addItem(final String item) {
			mData.add(item);
			notifyDataSetChanged();
		}

		public void addSeparatorItem(final String item) {
			mData.add(item);
			// save separator position
			mSeparatorsSet.add(mData.size() - 1);
			notifyDataSetChanged();
		}

		@Override
		public int getItemViewType(int position) {
			return mSeparatorsSet.contains(position) ? TYPE_SEPARATOR
					: TYPE_ITEM;
		}

		@Override
		public int getViewTypeCount() {
			return TYPE_MAX_COUNT;
		}

		public int getCount() {
			return mData.size();
		}

		public String getItem(int position) {
			return mData.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			int type = getItemViewType(position);
			if (convertView == null) {
				holder = new ViewHolder();
				switch (type) {
				case TYPE_ITEM:
					convertView = mInflater.inflate(R.layout.item1, null);
					holder.textView = (TextView) convertView
							.findViewById(R.id.text);
					break;
				case TYPE_SEPARATOR:
					convertView = mInflater.inflate(R.layout.item2, null);
					holder.textView = (TextView) convertView
							.findViewById(R.id.textSeparator);
					break;
				}
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.textView.setText(mData.get(position));
			return convertView;
		}

	}
 */
	public static class ViewHolder {
		public TextView textView;
	}

	public void onExerciseButtonClicked(View v) 
	{
		final TextView exerciseText = (TextView) findViewById(R.id.textExerciseLabel);
		final TextView nutritionText = (TextView) findViewById(R.id.textNutritionLabel);

		final ImageView exerciseButton = (ImageView) findViewById(R.id.exerciseButton);
		exerciseButton.setEnabled(false);
		exerciseButton.setClickable(false);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		LayoutInflater inflator = getLayoutInflater();
		View view = inflator.inflate(R.layout.exercise_tab_layout, null);

		TabHost tabHost = (TabHost) view
				.findViewById(R.id.edit_activity_tab_host);
		// tabHost.setup();
		tabHost.setup();

		View tabview = createTabView(tabHost.getContext(), getResources().getString(R.string.exercises_tab));
		TabSpec setContent = tabHost.newTabSpec(getResources().getString(R.string.exercises_tab))
				.setIndicator(tabview).setContent(R.id.show_exercise_tab);// new
																			// Intent(this,
																			// ExerciseCategory.class));
		tabHost.addTab(setContent);

		View tabview2 = createTabView(tabHost.getContext(), getResources().getString(R.string.sedentary_tab));
		TabSpec setContent2 = tabHost.newTabSpec(getResources().getString(R.string.sedentary_tab))
				.setIndicator(tabview2).setContent(R.id.show_sedentary_tab);

		tabHost.addTab(setContent2);

		ListView lwSedentary = (ListView) view
				.findViewById(R.id.sedentary_listview);
		ArrayAdapter<String> adaptor = new ArrayAdapter<String>(this,
				R.layout.simple2_list_item, sedentaryTemplates);
		lwSedentary.setAdapter(adaptor);
		lwSedentary.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int which,
					long arg3) {
				// Log.d(TAG,"----------------- Sedentary: Onur receives a score");
				String listItem = sedentaryTemplates[which];
				String langItem = sedentaryTemplates[which];
				if (!listItem.equals(getResources().getString(R.string.other))) {
					exerciseString = listItem;

					if (nutritionSet) {
/*						Toast.makeText(Main2Activity.this,
								"Food label deselected.", Toast.LENGTH_LONG)
								.show();*/
					}
					nutritionSet = false;
					exerciseSet = true;
					nutritionString = DEFAULT_NUTRITION;
					nutritionText.setText(DEFAULT_NUTRITION);

					exerciseText.setText(langItem,
							TextView.BufferType.SPANNABLE);
					Spannable WordtoSpan = (Spannable) exerciseText.getText();
					WordtoSpan.setSpan(new BackgroundColorSpan(0xFFFFFF00), 0,
							langItem.length(),
							Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					exerciseText.setText(WordtoSpan);
					activitySet = true;
					checkLogCompleteness();
					Constants.exerciseDialog.cancel();
				} else// (listItem.equals(getResources().getString(R.string.other)))
				{
					// launch an edit-text alert dialog...

					AlertDialog.Builder alert = new AlertDialog.Builder(
							Main2Activity.this);

					LayoutInflater inflater = getLayoutInflater();
					View viewHeader = inflater.inflate(R.layout.customtitle,
							null);
					TextView title = (TextView) viewHeader
							.findViewById(R.id.myTitle);
					title.setText(getResources().getString(R.string.customize_dialog_title));
					alert.setCustomTitle(viewHeader);
					// alert.setTitle("Customize Activity");
					alert.setMessage(getResources().getString(R.string.customize_dialog_content));

					// Set an EditText view to get user input
					final EditText input = new EditText(Main2Activity.this);// new
																			// EditText(this);
					alert.setView(input);

					alert.setPositiveButton(getResources().getString(R.string.ok_button_string),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {

									String listItem = input.getText()
											.toString();

									if (listItem != null
											&& listItem.length() > 0) {
										exerciseString = listItem;

									/*	if (nutritionSet) {
											Toast.makeText(Main2Activity.this,
													"Food label deselected.",
													Toast.LENGTH_LONG).show();
										}*/
										nutritionSet = false;
										exerciseSet = true;
										nutritionString = DEFAULT_NUTRITION;
										nutritionText
												.setText(DEFAULT_NUTRITION);

										exerciseText.setText(listItem,
												TextView.BufferType.SPANNABLE);
										Spannable WordtoSpan = (Spannable) exerciseText
												.getText();
										WordtoSpan
												.setSpan(
														new BackgroundColorSpan(
																0xFFFFFF00),
														0,
														listItem.length(),
														Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
										exerciseText.setText(WordtoSpan);
										activitySet = true;
										checkLogCompleteness();
										Constants.exerciseDialog.cancel();
									}
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

			}
		});

		ListView lwExercise = (ListView) view
				.findViewById(R.id.exercise_listview);
		ArrayAdapter<String> adaptor2 = new ArrayAdapter<String>(this,
				R.layout.simple2_list_item, exerciseTemplates);
		lwExercise.setAdapter(adaptor2);
		lwExercise.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int which,
					long arg3) {
			
				String listItem = exerciseTemplates[which];
				String langItem = exerciseTemplates[which];
				if (!listItem.equals(getResources().getString(R.string.other))) {
					exerciseString = listItem;

					/*if (nutritionSet) {
						Toast.makeText(Main2Activity.this,
								"Food label deselected.", Toast.LENGTH_LONG)
								.show();
					}*/
					nutritionSet = false;
					exerciseSet = true;
					nutritionString = DEFAULT_NUTRITION;
					nutritionText.setText(DEFAULT_NUTRITION);

					exerciseText.setText(langItem,
							TextView.BufferType.SPANNABLE);
					Spannable WordtoSpan = (Spannable) exerciseText.getText();
					WordtoSpan.setSpan(new BackgroundColorSpan(0xFFFFFF00), 0,
							langItem.length(),
							Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					exerciseText.setText(WordtoSpan);
					activitySet = true;
					checkLogCompleteness();
					Constants.exerciseDialog.cancel();
				} else// (listItem.equals(getResources().getString(R.string.other)))
				{
					// launch an edit-text alert dialog...

					AlertDialog.Builder alert = new AlertDialog.Builder(
							Main2Activity.this);

					LayoutInflater inflater = getLayoutInflater();
					View viewHeader = inflater.inflate(R.layout.customtitle,
							null);
					TextView title = (TextView) viewHeader
							.findViewById(R.id.myTitle);
					title.setText(getResources().getString(R.string.customize_dialog_title));
					alert.setCustomTitle(viewHeader);

					// alert.setTitle("Customize Activity");
					alert.setMessage(getResources().getString(R.string.customize_dialog_content));

					// Set an EditText view to get user input
					final EditText input = new EditText(Main2Activity.this);// new
																			// EditText(this);
					alert.setView(input);

					alert.setPositiveButton(getResources().getString(R.string.ok_button_string),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {

									String listItem = input.getText()
											.toString();

									if (listItem != null
											&& listItem.length() > 0) {
										exerciseString = listItem;

										/*if (nutritionSet) {
											Toast.makeText(Main2Activity.this,
													"Food label deselected.",
													Toast.LENGTH_LONG).show();
										}*/
										nutritionSet = false;
										exerciseSet = true;
										nutritionString = DEFAULT_NUTRITION;
										nutritionText
												.setText(DEFAULT_NUTRITION);

										exerciseText.setText(listItem,
												TextView.BufferType.SPANNABLE);
										Spannable WordtoSpan = (Spannable) exerciseText
												.getText();
										WordtoSpan
												.setSpan(
														new BackgroundColorSpan(
																0xFFFFFF00),
														0,
														listItem.length(),
														Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
										exerciseText.setText(WordtoSpan);
										activitySet = true;
										checkLogCompleteness();
										Constants.exerciseDialog.cancel();
									}
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

			}
		});

		for (int i = 0; i < tabHost.getChildCount(); i++) {
			if (i == 1) {
				tabHost.getTabWidget()
						.getChildAt(i)
						.setBackgroundResource(R.drawable.mytab_roundedcorners2);
			} else if (i == 0) {
				tabHost.getTabWidget().getChildAt(i)
						.setBackgroundResource(R.drawable.mytab_roundedcorners);
			}
		}

		LayoutInflater inflater = getLayoutInflater();
		View viewHeader = inflater.inflate(R.layout.customtitle, null);
		TextView title = (TextView) viewHeader.findViewById(R.id.myTitle);
		title.setText(getResources().getString(R.string.your_activity));
		builder.setCustomTitle(viewHeader);
		builder.setView(view);

		builder.setNegativeButton(getResources().getString(R.string.cancel_button), null);
		Constants.exerciseDialog = builder.create();
		Constants.exerciseDialog.setView(view, 0, 0, 0, 0);
		Constants.exerciseDialog.show();
		
		exerciseButton.setEnabled(true);
		exerciseButton.setClickable(true);		
		
	}

	public void onNutritionButtonClicked(View v) {
		final TextView exerciseText = (TextView) findViewById(R.id.textExerciseLabel);
		final TextView nutritionText = (TextView) findViewById(R.id.textNutritionLabel);

		// 1. Instantiate an AlertDialog.Builder with its constructor
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		LayoutInflater inflater = getLayoutInflater();
		View viewHeader = inflater.inflate(R.layout.customtitle, null);
		TextView title = (TextView) viewHeader.findViewById(R.id.myTitle);
		title.setText(getResources().getString(R.string.food_title));
		builder.setCustomTitle(viewHeader);

		// 2. Chain together various setter methods to set the dialog
		// characteristics
		builder// .setTitle("Food")
		.setItems(foodTemplates,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int position) {
						String listItem = foodTemplates[position];// .getItem(position);
						String langItem = foodTemplates[position];
						exerciseString = DEFAULT_EXERCISE;
						nutritionSet = true;

					/*	if (exerciseSet) {
							Toast.makeText(Main2Activity.this,
									"Activity label deselected.",
									Toast.LENGTH_LONG).show();
						}*/
						exerciseSet = false;
						nutritionString = listItem;
						exerciseText.setText(exerciseString);

						nutritionText.setText(langItem,
								TextView.BufferType.SPANNABLE);
						Spannable WordtoSpan = (Spannable) nutritionText
								.getText();
						WordtoSpan.setSpan(new BackgroundColorSpan(0xFFFFFF00),
								0, langItem.length(),
								Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						nutritionText.setText(WordtoSpan);

						activitySet = true;
						checkLogCompleteness();

					}

				}).setNegativeButton(getResources().getString(R.string.cancel_button), null);

		// 3. Get the AlertDialog from create()
		AlertDialog dialog = builder.create();
		dialog.show();

	}

	public void onMoodButtonClicked(View v) 
	{
		final TextView moodText = (TextView) findViewById(R.id.textMoodLabel);
		final ImageView moodButton = (ImageView) findViewById(R.id.moodButton);
		
		moodButton.setEnabled(false);
		moodButton.setClickable(false);

		showProgressDialog(true);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		LayoutInflater inflator = getLayoutInflater();
		View view = inflator.inflate(R.layout.mood_tab_layout, null);

		TabHost tabHost = (TabHost) view.findViewById(R.id.edit_mood_tab_host);
		// tabHost.setup();
		tabHost.setup();

		View tabview = createTabView(tabHost.getContext(), getResources().getString(R.string.positive_tab));
		TabSpec setContent = tabHost.newTabSpec(getResources().getString(R.string.positive_tab))
				.setIndicator(tabview).setContent(R.id.show_positive_tab);// new
																			// Intent(this,
																			// ExerciseCategory.class));
		tabHost.addTab(setContent);

		View tabview2 = createTabView(tabHost.getContext(), getResources().getString(R.string.negative_tab));
		TabSpec setContent2 = tabHost.newTabSpec(getResources().getString(R.string.negative_tab))
				.setIndicator(tabview2).setContent(R.id.show_negative_tab);

		tabHost.addTab(setContent2);

		// ListView lw = (ListView) findViewById(R.id.listView1);
		final MoodArrayAdapter positiveMoodAdapter = new MoodArrayAdapter(
				getApplicationContext(), R.layout.listitem_mood, this, true,this.moodIntesityTemplates);
		MoodArrayAdapter.selectedItemIndex = -1;
		MoodArrayAdapter.selectedRB = -1;
		positiveMoodAdapter.setHackable();
		for (int i = 0; i < positiveMoodTemplates.length; i++)// Constants.moodTemplates.length;
																// i++)
		{
			positiveMoodAdapter.add(new OneMood(i, positiveMoodTemplates[i],getResources().getString(R.string.normal),true));
		}

		ListView lwPositive = (ListView) view
				.findViewById(R.id.positive_listview);
		lwPositive.setAdapter(positiveMoodAdapter);

		final MoodArrayAdapter negativeMoodAdapter = new MoodArrayAdapter(
				getApplicationContext(), R.layout.listitem_mood, this, false,this.moodIntesityTemplates);

		for (int i = 0; i < negativeMoodTemplates.length; i++)// Constants.moodTemplates.length;
																// i++)
		{
			negativeMoodAdapter.add(new OneMood(i, negativeMoodTemplates[i],getResources().getString(R.string.normal),false));
		}

		ListView lwNegative = (ListView) view
				.findViewById(R.id.negative_listview);
		lwNegative.setAdapter(negativeMoodAdapter);

		for (int i = 0; i < tabHost.getChildCount(); i++) {
			if (i == 1) {
				tabHost.getTabWidget()
						.getChildAt(i)
						.setBackgroundResource(R.drawable.mytab_roundedcorners2);
			} else if (i == 0) {
				tabHost.getTabWidget().getChildAt(i)
						.setBackgroundResource(R.drawable.mytab_roundedcorners);
			}
		}

		// View viewHeader =inflator.inflate(R.layout.mood_tab_layout, null);

		LayoutInflater inflater = getLayoutInflater();
		View viewHeader = inflater.inflate(R.layout.customtitle, null);
		TextView title = (TextView) viewHeader.findViewById(R.id.myTitle);
		title.setText(getResources().getString(R.string.mood_title));
		builder.setCustomTitle(viewHeader);

		// builder.setTitle(R.);

		// AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder// .setTitle("Your mood")//setCustomTitle(tv)//setTitle("Your mood")
		.setView(view).setNegativeButton(getResources().getString(R.string.cancel_button), null)
				.setOnCancelListener(new DialogInterface.OnCancelListener() 
				{

					@Override
					public void onCancel(DialogInterface dialog) 
					{
						if (MoodArrayAdapter.selectedItemIndex >= 0) {
							OneMood md;
							if (MoodArrayAdapter.isSelectedItemPositive) {
								md = positiveMoodAdapter
										.getItemByID(MoodArrayAdapter.selectedItemIndex);
							} else {
								md = negativeMoodAdapter
										.getItemByID(MoodArrayAdapter.selectedItemIndex);
							}

							try {
								String listItem = md.getMoodString();
								if (md.getMoodIntensity().equals(Main2Activity.this.moodIntesityTemplates[0]))
									listItem = md.getMoodString().toLowerCase();// selectedItem.get(0).toLowerCase();
								else if (md.getMoodIntensity().equals(Main2Activity.this.moodIntesityTemplates[2]))
									listItem = md.getMoodString().toUpperCase()
											+ "!";

								moodString = md.getMoodString() // index-of translation
										+ md.getMoodIntensity(); // index-of translation
								moodText.setText(listItem,
										TextView.BufferType.SPANNABLE);
								Spannable WordtoSpan = (Spannable) moodText
										.getText();
								WordtoSpan.setSpan(new BackgroundColorSpan(
										0xFFFFFF00), 0, listItem.length(),
										Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
								moodText.setText(WordtoSpan);
							} catch (NullPointerException ex) {
								// Log.e(TAG, ex.getMessage());
							}

							// dialog.dismiss();
							moodSet = true;
							checkLogCompleteness();
							MoodArrayAdapter.selectedItemIndex = -1;
						}

					}

				});

		// 3. Get the AlertDialog from create()
		Constants.dialog = builder.create();
		

		
		Constants.dialog.setView(view, 0, 0, 0, 0);
		Constants.dialog.show();
		Button cancelB = Constants.dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
		
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
		
		moodButton.setEnabled(true);
		moodButton.setClickable(true);
		showProgressDialog(false);
		
	}

	public void onSocialButtonClicked(View v) {
		final TextView socialText = (TextView) findViewById(R.id.textSocialLabel);

		// 1. Instantiate an AlertDialog.Builder with its constructor
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		LayoutInflater inflater = getLayoutInflater();
		View viewHeader = inflater.inflate(R.layout.customtitle, null);
		TextView title = (TextView) viewHeader.findViewById(R.id.myTitle);
		title.setText(getResources().getString(R.string.social_title));
		builder.setCustomTitle(viewHeader);
		// 2. Chain together various setter methods to set the dialog
		// characteristics
		builder// .setTitle("Social Info")
		.setItems(socialTemplates,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int position) {
						socialString = socialTemplates[position];
						String langString = socialTemplates[position];
						if (position == 1) // together
						{
							socialString = TOGETHER_STRING;
							isTogether = true;
						} else // alone
						{
							socialString = ALONE_STRING;
							isTogether = false;
						}

						socialText.setText(langString,
								TextView.BufferType.SPANNABLE);
						socialSet = true;
						Spannable WordtoSpan = (Spannable) socialText.getText();

						WordtoSpan.setSpan(new BackgroundColorSpan(0xFFFFFF00),
								0, langString.length(),
								Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

						socialText.setText(WordtoSpan);

						checkLogCompleteness();

					}

				}).setNegativeButton(getResources().getString(R.string.cancel_button), null);

		// 3. Get the AlertDialog from create()
		AlertDialog dialog = builder.create();
		dialog.show();
		// Toast.makeText(this, "Social Button Callback.",
		// Toast.LENGTH_SHORT).show();

	}

	public void onLogConfirmButtonClicked(View v) {
		String entry;
		String messageStr;
		// TextView nutritionText;
		TextView exerciseText;
		TextView moodText;
		TextView socialText;
		TextView nutritionText;
		entry = "LOG ";
		messageStr = "Logged: ";

		if (socialSet || exerciseSet || nutritionSet || moodSet) //
		{
			Calendar c = Calendar.getInstance();
			int seconds = c.get(Calendar.SECOND);
			int minutes = c.get(Calendar.MINUTE);
			int hours = c.get(Calendar.HOUR_OF_DAY);
			int day = c.get(Calendar.DAY_OF_MONTH);
			int month = c.get(Calendar.MONTH)+1;
			int year = c.get(Calendar.YEAR);
			// nutritionText=(TextView)findViewById(R.id.textNutritionLabel);
			exerciseText = (TextView) findViewById(R.id.textExerciseLabel);
			moodText = (TextView) findViewById(R.id.textMoodLabel);
			socialText = (TextView) findViewById(R.id.textSocialLabel);
			nutritionText = (TextView) findViewById(R.id.textNutritionLabel);

			entry += "FOOD ";
			if (nutritionSet) {
				entry += nutritionString + " EXERCISE " + getResources().getString(R.string.none) + " ";
				messageStr += nutritionString + ", ";
			} else if (exerciseSet) {
				entry += getResources().getString(R.string.none) + " EXERCISE " + exerciseString + " ";
				messageStr += exerciseString + ", ";
			} else {
				entry += getResources().getString(R.string.none) + " EXERCISE " + getResources().getString(R.string.none) + " ";
			}

			entry += "MOOD ";
			if (moodSet) {
				entry += moodString + " ";
				messageStr += moodString + " ";
			} else {
				entry += getResources().getString(R.string.none) + " ";
			}

			entry += "SOCIAL ";
			if (socialSet) {
				entry += socialString;
				messageStr += socialString;
			} else {
				entry += getResources().getString(R.string.none);
				// messageStr += "Alone";
			}

			/*
			 * TextView t=(TextView)findViewById(R.id.textGameRoundLabel);
			 * 
			 * //t.setText("Step One: blast egg");
			 * //t.setText(HTML.fromHtml("Testing" + "<b>Issue</b>"));
			 * t.setText("Italic, highlighted, bold.",
			 * TextView.BufferType.SPANNABLE);
			 * 
			 * Spannable WordtoSpan = (Spannable) t.getText();
			 * 
			 * WordtoSpan.setSpan(new BackgroundColorSpan(0xFFFFFF00), 8, 19,
			 * Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			 * 
			 * t.setText(WordtoSpan);
			 */

			// Log.d(TAG, "Log to be stored: " + entry);
			/*if (!Utils.isConnectionPresent(this)) {
				// Toast.makeText(this,
				// "Could not send your log: No internet connection. Please try again later.",
				// Toast.LENGTH_LONG).show();
				isDisplayingConnectionErrorDialog = false;
				displayConnectionErrorDialog();
			} else {*/
				nutritionSet = false;
				moodSet = false;
				exerciseSet = false;
				socialSet = false;
				isTogether = false;
				activitySet = false;

				// nutritionString = DEFAULT_NUTRITION;
				exerciseString = DEFAULT_EXERCISE;
				moodString = DEFAULT_MOOD; //
				socialString = DEFAULT_SOCIAL;
				nutritionString = DEFAULT_NUTRITION;

				// nutritionText.setText(nutritionString);
				exerciseText.setText(exerciseString);
				moodText.setText(moodString);
				socialText.setText(socialString);
				nutritionText.setText(nutritionString);
				sendNewLog(entry, messageStr, year, month, day, hours, minutes,
						seconds);

			//}
			/*
			 * Button confirmButton = (Button)findViewById(R.id.confirmButton);
			 * confirmButton.setVisibility(Button.INVISIBLE);
			 * confirmButton.setClickable(false);
			 */
		} 
		else 
		{
			Toast.makeText(this,
					getResources().getString(R.string.incomplete_log),
					Toast.LENGTH_SHORT).show();
		}

	}

	private boolean sendNewLog(final String queryEntry,
			final String messageStr, int year, int month, int day, int hours,
			int minutes, int seconds) {
		// final String text = DatabaseUtils.sqlEscapeString(queryEntry);

		String secondString = (seconds < 10 ? ("0" + seconds) : ("" + seconds));
		String minuteString = (minutes < 10 ? ("0" + minutes) : ("" + minutes));
		String hourString = (hours < 10 ? ("0" + hours) : ("" + hours));
		String monthString = (month < 10 ? ("0" + month) : ("" + month));
		String dayString = (day < 10 ? ("0" + day) : ("" + day));
		String yearString = "" + year;

		final String wholeString = yearString + "-" + monthString + "-" + dayString
				+ " " + hourString + ":" + minuteString + ":" + secondString;
		OneLog log = new OneLog(queryEntry, wholeString,getResources().getString(R.string.none));

		Constants.logLists.add(log);
		/*if (!Constants.alertGiven) {
			Constants.holdOffTodaysClosestAlarm(hours, minutes);
		}*/
		Constants.alertGiven = false;

		OneLog.getClosestAlarmTime(log); // String closestDate =

		final String sqlText = log.toDBEntry(positiveMoodTemplates, negativeMoodTemplates, foodTemplates, sedentaryTemplates, exerciseTemplates, socialTemplates,moodIntesityTemplates,getResources().getString(R.string.none));// editText1.getText().toString().replaceAll("'",
											// "''");
		
		//Log.i(TAG, "original message: " + queryEntry);
		//Log.i(TAG, "modified message: " + sqlText);
		int uid = AppContext.getInstance().getUserId();
		int fid = uid;// AppContext.getInstance().getFriendId();
		
		
		
		SendTimelyLogTask task = new SendTimelyLogTask(uid, fid, sqlText, wholeString) {

			@Override
			protected void onPostExecute(Boolean success) {
				
				if (this.safeExecution && success) {
					// if(!sqlText.isEmpty()){
					// add the message to the view (user's msg appear on the
					// right)
					// adapter.add(new OneComment(false,
					// editText1.getText().toString()));
					// editText1.setText("");
					// scrollToBottom();
					// }
					Toast.makeText(Main2Activity.this, getResources().getString(R.string.log_successful),
							Toast.LENGTH_SHORT).show();
				} else if (!Utils.isConnectionPresent(Main2Activity.this)) {
					
					isDisplayingConnectionErrorDialog = false;
					displayConnectionErrorMessage();
					
					SharedPreferences prefs = getSharedPreferences(Constants.PROPERTIES_NAME,Context.MODE_PRIVATE);
					Constants.storeLogForRetry(prefs,sqlText,wholeString);
					displayLogStats();
					
				} else {
					Toast.makeText(Main2Activity.this,
							getResources().getString(R.string.log_send_error),
							Toast.LENGTH_SHORT).show();
				}
			}
		};
		task.execute();

		return true;
	}

	@SuppressWarnings("deprecation")
	protected void displayLogStats() 
	{

		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.activity_buddyaccept_dialog,
				null);
		AlertDialog.Builder builder = new AlertDialog.Builder(this)
				.setView(layout)
				.setOnCancelListener(
				new DialogInterface.OnCancelListener() {

					@Override
					public void onCancel(DialogInterface dialog) {
						//moveTaskToBack(true);
						// checkProgressHandler.postDelayed(checkForProgressTask,
						// 0);
					}
				});
		
		buddyDialog = builder.create();
		
		TextView tv1 = (TextView) layout.findViewById(R.id.alertinfo_bc);
		TextView tv2 = (TextView) layout.findViewById(R.id.alertinfo2_bc);
		
		ImageView iv = (ImageView) layout.findViewById(R.id.status_icon);
		iv.setBackgroundDrawable(getResources().getDrawable(R.drawable.info_icon));
		
		tv1.setText(getResources().getString(R.string.your_log));
		tv2.setText(getResources().getString(R.string.log_after_connection));
		//buddyDialogMessage = message;
		//Constants.FIRST_WELCOME_GIVEN = true;
		if (isActive) {
			buddyDialog.show();
		}
		
	}

	public void deFriend() {

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
					/*
					 * Intent i_change_buddy= new Intent(Main2Activity.this,
					 * InviteBuddyActivity.class);
					 * startActivity(i_change_buddy);
					 */
					// Toast.makeText(Main2Activity.this,
					// "You have left your buddy.", Toast.LENGTH_LONG).show();
					Constants.COUNTER_INVITE = false;
					Constants.INVITATION_ACCEPTED = false;
					Constants.INVITATION_SENT = false;
					Constants.INVITATION_RECEIVED = false;
					
					SharedPreferences prefs2 = getSharedPreferences(
							Constants.PROPERTIES_NAME, MODE_PRIVATE);					
					
					Constants.internalReset(Main2Activity.this,prefs2);
					Constants.externalReset();
					
					// Addition: so avoid the remnants 
					AppContext.getInstance().setFriendId(0);
					
					

					Editor editor = prefs2.edit();
					editor.putBoolean(Constants.PROP_KEY_GAME_STARTED, false);
					editor.commit();

					previousActivityIsMessagesActivity = false;
					Intent i_change_buddy = new Intent(Main2Activity.this,
							InviteBuddyActivity.class);
					i_change_buddy
							.putExtra(
									"message",
									getResources().getString(R.string.you_were_playing_with)
											+ " " + buddyName + " "
											+ getResources().getString(R.string.and_now_unpaired));
					// i_change_buddy.putExtra("removalSeen", -99);
					// notifyChangeMessage("You have successfully ");
					startActivity(i_change_buddy);
					//finish();
				} else if (!Utils.isConnectionPresent(Main2Activity.this)) {
					// Toast.makeText(Main2Activity.this,
					// "No internet connection. Please try again later.",
					// Toast.LENGTH_LONG).show();
					displayConnectionErrorMessage();
				} else {
					/*Toast.makeText(Main2Activity.this,
							getResources().getString(R.string.connection_toast_message),
							Toast.LENGTH_LONG).show();*/
					
					displayConnectionErrorMessage();
				}
			}
		};
		task.execute();
	}

	public void onChangeBuddyButtonClicked(View v) {
		// Toast.makeText(Main2Activity.this,
		// "Error: No internet connection available",
		// Toast.LENGTH_SHORT).show();
		
		if(alertDialog != null)
		{
			if(alertDialog.isShowing())
			{
				alertDialog.dismiss();
			}
		}
		
		deFriend();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main2, menu);
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
			
			
			if(Utils.isConnectionPresent(Main2Activity.this))
			{
				// AppContext.getInstance().setUserCredentialsSet(false);
				Intent i_settings = new Intent(this, SettingsActivity.class);
				// Intent i_auth= new Intent(this, LoginActivity.class);
				// Intent i_auth= new Intent(this,
				// AuthenticateFitbitActivity.class);
				Constants.MAIN_OR_COMMUNITY = false;
				startActivity(i_settings);
			}
			else
			{
				isDisplayingConnectionErrorDialog = false;
				displayConnectionErrorMessage();
			}
			return true;

		case R.id.menu_change_buddy:
			
			if(!Utils.isConnectionPresent(Main2Activity.this))
			{
					isDisplayingConnectionErrorDialog = false;
					displayConnectionErrorMessage();					
					return true;
			}
			
			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View layout = inflater.inflate(R.layout.activity_change_dialog,
					null);
			AlertDialog.Builder builder = new AlertDialog.Builder(this)
					.setView(layout);
			alertDialog = builder.create();
			if (isActive) {
				try
				{
					alertDialog.show();
				}
				catch(Exception e)
				{
					alertDialog = null;
				}
			}
			return true;
		case R.id.menu_sync_data:
			isDisplayingConnectionErrorDialog = false;
			// log_tag
			if (Utils.isConnectionPresent(Main2Activity.this)) {
				// Log.d(TAG, "Progress dialog called from manual sync.");
				//
				
				updateBadgesWithSync(true);
				//
				// if(isManual)
				// {
				checkFitbitAuthentication();
				// }
			} else {
				// Log.d(TAG,
				// "Manual sync can not be done due to connection error.");
				isDisplayingConnectionErrorDialog = false;
				displayConnectionErrorMessage();
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
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			moveTaskToBack(true); 
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
				updateBadgesWithSync(true);
				lastRefreshMillis = currentMillis;
			} else {
				/*Toast.makeText(Main2Activity.this,
						"The stats are already up to date", Toast.LENGTH_SHORT)
						.show();*/
			}

		} else {
			showSyncFailedDialog();
			// Toast.makeText(Main2Activity.this,
			// "Error: No internet connection available",
			// Toast.LENGTH_SHORT).show();
		}
	}

	public String updateDatePanel() {

		GregorianCalendar gc = new GregorianCalendar();
		Date d = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
		SimpleDateFormat datef = new SimpleDateFormat("yyyy-MM-dd");
		
		/*String dayOfTheWeek = sdf.format(d);
		String dateString = datef.format(d);*/

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

	public void showPreviousHistory(View v) {
		showProgressDialog(true);

		if (datePivot < 6) {
			datePivot++;
		} else {
			datePivot = 6;
		}

/*		SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
		SimpleDateFormat datef = new SimpleDateFormat("yyyy-MM-dd");*/
		GregorianCalendar gc = new GregorianCalendar();

		Date d = new Date();
		/*String dayOfTheWeek = sdf.format(d);
		String dateString = datef.format(d);*/

		gc.setTime(d);
		int dayBefore = gc.get(Calendar.DAY_OF_YEAR);
		gc.roll(Calendar.DAY_OF_YEAR, -datePivot);

		int dayAfter = gc.get(Calendar.DAY_OF_YEAR);
		if (dayAfter > dayBefore) {
			gc.roll(Calendar.YEAR, -datePivot);
		}
		gc.get(Calendar.DATE);
		java.util.Date yesterday = gc.getTime();
		SharedPreferences prefs = getSharedPreferences(
				Constants.PROPERTIES_NAME, Context.MODE_PRIVATE);
		if (!Constants.validDate(prefs, yesterday)) {
			// invalid time, so do not update
			// roll back to old datePivot
			if (datePivot > 0) {
				datePivot--;
			} else {
				datePivot = 0;
			}
			ImageView previousBtn = (ImageView) findViewById(R.id.previousLabel);
			previousBtn.setImageDrawable(getResources().getDrawable(
					R.drawable.previousicon_nonclickable));
			return;
		}

		updateDatePanel();

		int stepweightedSum = (int) (myStepHistory[datePivot]
				* Constants.SELF_WEIGHT + buddyStepHistory[datePivot]
				* Constants.BUDDY_WEIGHT);
		int stepbadge_cnt = checkStepBadgeCount(stepweightedSum);

		int floorweightedSum = (int) (myFloorHistory[datePivot]
				* Constants.SELF_WEIGHT + myFloorHistory[datePivot]
				* Constants.BUDDY_WEIGHT);
		int floorbadge_cnt = checkFloorBadgeCount(floorweightedSum);

		drawTopFloorBar(myFloorHistory[datePivot], buddyFloorHistory[datePivot]);
		drawBottomFloorBar(myFloorHistory[datePivot],
				buddyFloorHistory[datePivot], floorbadge_cnt);
		drawTopBar(myStepHistory[datePivot], buddyStepHistory[datePivot]);
		drawBottomBar(myStepHistory[datePivot], buddyStepHistory[datePivot],
				stepbadge_cnt);
		updateDisplay(datePivot == 0);
		showProgressDialog(false);
	}

	public void showLaterHistory(View v) {
		showProgressDialog(true);
		if (datePivot > 0) {
			datePivot--;
		} else {
			datePivot = 0;
		}
		
		updateDatePanel();
		int stepweightedSum = (int) (myStepHistory[datePivot]
				* Constants.SELF_WEIGHT + buddyStepHistory[datePivot]
				* Constants.BUDDY_WEIGHT);
		int stepbadge_cnt = checkStepBadgeCount(stepweightedSum);

		int floorweightedSum = (int) (myFloorHistory[datePivot]
				* Constants.SELF_WEIGHT + myFloorHistory[datePivot]
				* Constants.BUDDY_WEIGHT);
		int floorbadge_cnt = checkFloorBadgeCount(floorweightedSum);

		drawTopFloorBar(myFloorHistory[datePivot], buddyFloorHistory[datePivot]);
		drawBottomFloorBar(myFloorHistory[datePivot],
				buddyFloorHistory[datePivot], floorbadge_cnt);
		drawTopBar(myStepHistory[datePivot], buddyStepHistory[datePivot]);
		drawBottomBar(myStepHistory[datePivot], buddyStepHistory[datePivot],
				stepbadge_cnt);
		updateDisplay(datePivot == 0);
		showProgressDialog(false);
	}

	/*private void updateDisplay() {
		displayCongratulations = true;
		updateStepBadges();
		updateFloorBadges();
	}*/

	private void updateDisplay(boolean displayMessage) {
		displayCongratulations = displayMessage;
		updateStepBadges();
		updateFloorBadges();
	}

	/*
	 * 
	 * */
	private int checkStepBadgeCount(int weightedSum) {
		int badge_cnt = 0;

		for (int i = 0; i < Constants.BADGE_LEVELS.length; i++) {
			if (weightedSum >= Constants.BADGE_LEVELS[i]) {
				badge_cnt++;
			}
		}
		// Log.d(TAG,"The badge count is: " + badge_cnt);
		return Math.min(Constants.BADGE_LEVELS.length - 1, badge_cnt);
	}

	private int checkFloorBadgeCount(int weightedSum) {
		int floor_badge_cnt = 0;

		for (int i = 0; i < Constants.FLOOR_BADGE_LEVELS.length; i++) {
			if (weightedSum >= Constants.FLOOR_BADGE_LEVELS[i]) {
				floor_badge_cnt++;
			}
		}

		// Log.d(TAG,"The badge count is: " + floor_badge_cnt);
		return Math.min(Constants.FLOOR_BADGE_LEVELS.length - 1,
				floor_badge_cnt);
	}

	private void checkStepBadges() {
		SharedPreferences prefs = getSharedPreferences(
				Constants.PROPERTIES_NAME, Context.MODE_PRIVATE);
		int userCompletedSteps = prefs.getInt(
				Constants.PROP_KEY_USER_COMPLETED_STEPS, -1);
		int otherCompletedSteps = prefs.getInt(
				Constants.PROP_KEY_BUDDY_COMPLETED_STEPS, -1);
		int badgeLevel = prefs.getInt(Constants.PROP_KEY_BADGE_LEVEL, -1);
		if (userCompletedSteps == -1 && otherCompletedSteps == -1) {
			// first time when user runs the app
			// Toast.makeText(Main2Activity.this, "First time enter the game",
			// Toast.LENGTH_LONG).show();
			// Log.d(TAG, "First time enter the game.");
			// updateCompletedStepsAndBadgeLevel(prefs, mySteps, buddySteps, 0);
			// Log.d(TAG,"userCompletedSteps and otherCompletedSteps were not available in the local files");
			updateCompletedStepsAndBadgeLevel(prefs, 0, 0, 0);
		}

		// Get user and buddy's completed steps again
		userCompletedSteps = prefs.getInt(
				Constants.PROP_KEY_USER_COMPLETED_STEPS, -1);
		otherCompletedSteps = prefs.getInt(
				Constants.PROP_KEY_BUDDY_COMPLETED_STEPS, -1);
		badgeLevel = prefs.getInt(Constants.PROP_KEY_BADGE_LEVEL, -1);
		if (userCompletedSteps != -1 && otherCompletedSteps != -1) { // Safe
																		// check
			
				
			if (isDifferentDay(prefs)) 
			{
				// when it's another day all user steps has cleared to 0
				// Log.d(TAG, "another day, encountered, my steps: " +
				// mySteps+" buddySteps:"+buddySteps);
				updateCompletedStepsAndBadgeLevel(prefs, 0, 0, 0);
				badgeLevel = 0;
			}
			int weightedSum = (int) (mySteps * Constants.SELF_WEIGHT + buddySteps
					* Constants.BUDDY_WEIGHT);
			// Update bars
			int badge_cnt = checkStepBadgeCount(weightedSum);

			/*
			 * private int[] buddyStepHistory = new int[7]; private int[]
			 * myStepHistory = new int[7]; private int[] buddyFloorHistory = new
			 * int[7]; private int[] myFloorHistory = new int[7];
			 */
			myStepHistory[datePivot] = mySteps;
			buddyStepHistory[datePivot] = buddySteps;

			drawTopBar(mySteps, buddySteps);
			drawBottomBar(mySteps, buddySteps, badge_cnt);

			// int badge_cnt = checkStepBadgeCount(weightedSum);
			/*
			 * for(int i = 0; i < Constants.BADGE_LEVELS.length; i++) {
			 * if(weightedSum >= Constants.BADGE_LEVELS[i]) { badge_cnt++; } }
			 */
			boolean noNewBadge = false;
			if ((badge_cnt <= badgeLevel) && (datePivot == 0)) {
				noNewBadge = true;
			}

			// Display badges accordingly
			ImageView badge1 = (ImageView) findViewById(R.id.badge1);
			ImageView badge2 = (ImageView) findViewById(R.id.badge2);
			ImageView badge3 = (ImageView) findViewById(R.id.badge3);
			ImageView badge4 = (ImageView) findViewById(R.id.badge4);
			ImageView badge5 = (ImageView) findViewById(R.id.badge5);
			ImageView badge6 = (ImageView) findViewById(R.id.badge6);
			ImageView badge7 = (ImageView) findViewById(R.id.badge7);
			ImageView badge8 = (ImageView) findViewById(R.id.badge8);

			switch (badge_cnt) {
			case 0:
				badge1.setImageResource(R.drawable.no_badge);
				badge1.setVisibility(View.VISIBLE);

				badge2.setVisibility(View.INVISIBLE);
				badge3.setVisibility(View.INVISIBLE);
				badge4.setVisibility(View.INVISIBLE);
				badge5.setVisibility(View.INVISIBLE);
				badge6.setVisibility(View.INVISIBLE);
				badge7.setVisibility(View.INVISIBLE);
				badge8.setVisibility(View.INVISIBLE);
				// Log.d(TAG,
				// "!!!!!!!!!!!!current count "+badge_cnt+"----00000");
				break;
			case 1:
				badge1.setImageResource(R.drawable.ta);
				badge1.setVisibility(View.VISIBLE);
				badge2.setImageResource(R.drawable.no_badge);
				badge2.setVisibility(View.VISIBLE);

				badge3.setVisibility(View.INVISIBLE);
				badge4.setVisibility(View.INVISIBLE);
				badge5.setVisibility(View.INVISIBLE);
				badge6.setVisibility(View.INVISIBLE);
				badge7.setVisibility(View.INVISIBLE);
				badge8.setVisibility(View.INVISIBLE);
				updateBars(prefs, mySteps, buddySteps, weightedSum, 1,
						noNewBadge);
				// Log.d(TAG,
				// "!!!!!!!!!!!!current count "+badge_cnt+"----11111");
				break;
			case 2:
				badge1.setImageResource(R.drawable.ta);
				badge1.setVisibility(View.VISIBLE);
				badge2.setImageResource(R.drawable.tb);
				badge2.setVisibility(View.VISIBLE);
				badge3.setImageResource(R.drawable.no_badge);
				badge3.setVisibility(View.VISIBLE);

				badge4.setVisibility(View.INVISIBLE);
				badge5.setVisibility(View.INVISIBLE);
				badge6.setVisibility(View.INVISIBLE);
				badge7.setVisibility(View.INVISIBLE);
				badge8.setVisibility(View.INVISIBLE);
				updateBars(prefs, mySteps, buddySteps, weightedSum, 2,
						noNewBadge);
				// Log.d(TAG,
				// "!!!!!!!!!!!!current count "+badge_cnt+"----22222");
				break;
			case 3:
				badge1.setImageResource(R.drawable.ta);
				badge2.setImageResource(R.drawable.tb);
				badge3.setImageResource(R.drawable.tc);
				badge4.setImageResource(R.drawable.no_badge);
				badge1.setVisibility(View.VISIBLE);
				badge2.setVisibility(View.VISIBLE);
				badge3.setVisibility(View.VISIBLE);
				badge4.setVisibility(View.VISIBLE);

				badge5.setVisibility(View.INVISIBLE);
				badge6.setVisibility(View.INVISIBLE);
				badge7.setVisibility(View.INVISIBLE);
				badge8.setVisibility(View.INVISIBLE);
				updateBars(prefs, mySteps, buddySteps, weightedSum, 3,
						noNewBadge);
				// Log.d(TAG,
				// "!!!!!!!!!!!!current count "+badge_cnt+"----33333");
				break;
			case 4:
				badge1.setImageResource(R.drawable.ta);
				badge2.setImageResource(R.drawable.tb);
				badge3.setImageResource(R.drawable.tc);
				badge4.setImageResource(R.drawable.td);
				badge5.setImageResource(R.drawable.no_badge);
				badge1.setVisibility(View.VISIBLE);
				badge2.setVisibility(View.VISIBLE);
				badge3.setVisibility(View.VISIBLE);
				badge4.setVisibility(View.VISIBLE);
				badge5.setVisibility(View.VISIBLE);

				badge6.setVisibility(View.INVISIBLE);
				badge7.setVisibility(View.INVISIBLE);
				badge8.setVisibility(View.INVISIBLE);
				updateBars(prefs, mySteps, buddySteps, weightedSum, 4,
						noNewBadge);
				// Log.d(TAG,
				// "!!!!!!!!!!!!current count "+badge_cnt+"----44444");
				break;
			case 5:
				badge1.setImageResource(R.drawable.ta);
				badge2.setImageResource(R.drawable.tb);
				badge3.setImageResource(R.drawable.tc);
				badge4.setImageResource(R.drawable.td);
				badge5.setImageResource(R.drawable.te);
				badge6.setImageResource(R.drawable.no_badge);
				badge1.setVisibility(View.VISIBLE);
				badge2.setVisibility(View.VISIBLE);
				badge3.setVisibility(View.VISIBLE);
				badge4.setVisibility(View.VISIBLE);
				badge5.setVisibility(View.VISIBLE);
				badge6.setVisibility(View.VISIBLE);

				badge7.setVisibility(View.INVISIBLE);
				badge8.setVisibility(View.INVISIBLE);
				updateBars(prefs, mySteps, buddySteps, weightedSum, 5,
						noNewBadge);
				// Log.d(TAG,
				// "!!!!!!!!!!!!current count "+badge_cnt+"----55555");
				break;
			case 6:
				badge1.setImageResource(R.drawable.ta);
				badge2.setImageResource(R.drawable.tb);
				badge3.setImageResource(R.drawable.tc);
				badge4.setImageResource(R.drawable.td);
				badge5.setImageResource(R.drawable.te);
				badge6.setImageResource(R.drawable.tf);
				badge7.setImageResource(R.drawable.no_badge);

				badge1.setVisibility(View.VISIBLE);
				badge2.setVisibility(View.VISIBLE);
				badge3.setVisibility(View.VISIBLE);
				badge4.setVisibility(View.VISIBLE);
				badge5.setVisibility(View.VISIBLE);
				badge6.setVisibility(View.VISIBLE);
				badge7.setVisibility(View.VISIBLE);
				badge8.setVisibility(View.INVISIBLE);
				updateBars(prefs, mySteps, buddySteps, weightedSum, 6,
						noNewBadge);
				// Log.d(TAG,
				// "!!!!!!!!!!!!current count "+badge_cnt+"----66666");
				break;
			case 7:
				badge1.setImageResource(R.drawable.ta);
				badge2.setImageResource(R.drawable.tb);
				badge3.setImageResource(R.drawable.tc);
				badge4.setImageResource(R.drawable.td);
				badge5.setImageResource(R.drawable.te);
				badge6.setImageResource(R.drawable.tf);
				badge7.setImageResource(R.drawable.tg);
				badge8.setImageResource(R.drawable.no_badge);

				badge1.setVisibility(View.VISIBLE);
				badge2.setVisibility(View.VISIBLE);
				badge3.setVisibility(View.VISIBLE);
				badge4.setVisibility(View.VISIBLE);
				badge5.setVisibility(View.VISIBLE);
				badge6.setVisibility(View.VISIBLE);
				badge7.setVisibility(View.VISIBLE);
				badge8.setVisibility(View.VISIBLE);
				updateBars(prefs, mySteps, buddySteps, weightedSum, 7,
						noNewBadge);
				// Log.d(TAG,
				// "!!!!!!!!!!!!current count "+badge_cnt+"----77777");
				break;
			case 8:
				badge1.setImageResource(R.drawable.ta);
				badge2.setImageResource(R.drawable.tb);
				badge3.setImageResource(R.drawable.tc);
				badge4.setImageResource(R.drawable.td);
				badge5.setImageResource(R.drawable.te);
				badge6.setImageResource(R.drawable.tf);
				badge7.setImageResource(R.drawable.tg);
				badge8.setImageResource(R.drawable.th);
				badge1.setVisibility(View.VISIBLE);
				badge2.setVisibility(View.VISIBLE);
				badge3.setVisibility(View.VISIBLE);
				badge4.setVisibility(View.VISIBLE);
				badge5.setVisibility(View.VISIBLE);
				badge6.setVisibility(View.VISIBLE);
				badge7.setVisibility(View.VISIBLE);
				badge8.setVisibility(View.VISIBLE);
				updateBars(prefs, mySteps, buddySteps, weightedSum, 8,
						noNewBadge);
				// Log.d(TAG,
				// "!!!!!!!!!!!!current count "+badge_cnt+"----88888");
				break;

			default:
				// handle case for more than 8 badges
				badge1.setImageResource(R.drawable.ta);
				badge2.setImageResource(R.drawable.tb);
				badge3.setImageResource(R.drawable.tc);
				badge4.setImageResource(R.drawable.td);
				badge5.setImageResource(R.drawable.te);
				badge6.setImageResource(R.drawable.tf);
				badge7.setImageResource(R.drawable.tg);
				badge8.setImageResource(R.drawable.th);
				badge2.setVisibility(View.VISIBLE);
				badge3.setVisibility(View.VISIBLE);
				badge4.setVisibility(View.VISIBLE);
				badge5.setVisibility(View.VISIBLE);
				badge6.setVisibility(View.VISIBLE);
				badge7.setVisibility(View.VISIBLE);
				badge8.setVisibility(View.VISIBLE);
				updateBars(prefs, mySteps, buddySteps, weightedSum, badge_cnt,
						noNewBadge);
				// Log.d(TAG,
				// "!!!!!!!!!!!!current count "+badge_cnt+"----99999");
				break;
			}
			// Log.d(TAG, "current badge level:"+badge_cnt);
		}
	}

	private void checkFloorBadges() {
		SharedPreferences prefs = getSharedPreferences(
				Constants.PROPERTIES_NAME, Context.MODE_PRIVATE);
		int userCompletedFloors = prefs.getInt(
				Constants.PROP_KEY_USER_COMPLETED_FLOORS, -1);
		int otherCompletedFloors = prefs.getInt(
				Constants.PROP_KEY_BUDDY_COMPLETED_FLOORS, -1);
		int badgeLevel = prefs.getInt(Constants.PROP_KEY_FLOOR_BADGE_LEVEL, -1);

		if (userCompletedFloors == -1 && otherCompletedFloors == -1) {
			// first time when user runs the app
			// Toast.makeText(Main2Activity.this, "First time enter the game",
			// Toast.LENGTH_LONG).show();
			// Log.d(TAG, "First time enter the game.");
			// updateCompletedStepsAndBadgeLevel(prefs, mySteps, buddySteps, 0);
			// Log.d(TAG,"userCompletedFloors and otherCompletedFloors were not available in the local files");
			updateCompletedFloorsAndBadgeLevel(prefs, 0, 0, 0);
		}

		// Get user and buddy's completed steps again
		userCompletedFloors = prefs.getInt(
				Constants.PROP_KEY_USER_COMPLETED_FLOORS, -1);
		otherCompletedFloors = prefs.getInt(
				Constants.PROP_KEY_BUDDY_COMPLETED_FLOORS, -1);
		badgeLevel = prefs.getInt(Constants.PROP_KEY_FLOOR_BADGE_LEVEL, -1);

		if (userCompletedFloors != -1 && otherCompletedFloors != -1) { // Safe
																		// check

			//int relativeUserFloors = (int) (myFloors * Constants.SELF_WEIGHT);
			//int relativeBuddyFloors = (int) (buddyFloors * Constants.BUDDY_WEIGHT);

			if (isDifferentDay(prefs)) {
				// when it's another day all user steps has cleared to 0
				// Log.d(TAG, "another day, encountered, my floors: " +
				// myFloors+" buddyFloors:"+buddyFloors);
				updateCompletedFloorsAndBadgeLevel(prefs, 0, 0, 0);
				//relativeUserFloors = 0;
				//relativeBuddyFloors = 0;
				badgeLevel = 0;
			}
			int weightedSum = (int) (myFloors * Constants.SELF_WEIGHT + buddyFloors
					* Constants.BUDDY_WEIGHT);

			int badge_cnt = checkFloorBadgeCount(weightedSum);

			myFloorHistory[datePivot] = myFloors;
			buddyFloorHistory[datePivot] = buddyFloors;

			// Update bars
			drawTopFloorBar(myFloors, buddyFloors);
			drawBottomFloorBar(myFloors, buddyFloors, badge_cnt);
			// Log.d(TAG, "^^^^^^^^^^^^^^^^^^^^^ floor_badge_cnt: "+badge_cnt +
			// "weightedFloorSum: " + badge_cnt);

			boolean noNewBadge = false;
			if ((datePivot == 0) && (badge_cnt <= badgeLevel)) {
				noNewBadge = true;
			}
			// Update game round
			/*
			 * TextView gameRound = (TextView)findViewById(R.id.textGameRound);
			 * gameRound.setText(""+(badge_cnt+1));
			 */
			// Display badges accordingly
			ImageView badge1 = (ImageView) findViewById(R.id.floorbadge1);
			ImageView badge2 = (ImageView) findViewById(R.id.floorbadge2);
			ImageView badge3 = (ImageView) findViewById(R.id.floorbadge3);
			ImageView badge4 = (ImageView) findViewById(R.id.floorbadge4);
			ImageView badge5 = (ImageView) findViewById(R.id.floorbadge5);
			ImageView badge6 = (ImageView) findViewById(R.id.floorbadge6);
			ImageView badge7 = (ImageView) findViewById(R.id.floorbadge7);
			ImageView badge8 = (ImageView) findViewById(R.id.floorbadge8);
			switch (badge_cnt) {
			case 0:
				badge1.setImageResource(R.drawable.no_badge);
				badge1.setVisibility(View.VISIBLE);

				badge2.setVisibility(View.INVISIBLE);
				badge3.setVisibility(View.INVISIBLE);
				badge4.setVisibility(View.INVISIBLE);
				badge5.setVisibility(View.INVISIBLE);
				badge6.setVisibility(View.INVISIBLE);
				badge7.setVisibility(View.INVISIBLE);
				badge8.setVisibility(View.INVISIBLE);
				break;
			case 1:
				badge1.setImageResource(R.drawable.fa);
				badge2.setImageResource(R.drawable.no_badge);

				badge1.setVisibility(View.VISIBLE);
				badge2.setVisibility(View.VISIBLE);

				badge3.setVisibility(View.INVISIBLE);
				badge4.setVisibility(View.INVISIBLE);
				badge5.setVisibility(View.INVISIBLE);
				badge6.setVisibility(View.INVISIBLE);
				badge7.setVisibility(View.INVISIBLE);
				badge8.setVisibility(View.INVISIBLE);

				updateFloorBars(prefs, myFloors, buddyFloors, weightedSum, 1,
						noNewBadge);
				break;
			case 2:
				badge1.setImageResource(R.drawable.fa);
				badge2.setImageResource(R.drawable.fb);
				badge3.setImageResource(R.drawable.no_badge);

				badge1.setVisibility(View.VISIBLE);
				badge2.setVisibility(View.VISIBLE);
				badge3.setVisibility(View.VISIBLE);

				badge4.setVisibility(View.INVISIBLE);
				badge5.setVisibility(View.INVISIBLE);
				badge6.setVisibility(View.INVISIBLE);
				badge7.setVisibility(View.INVISIBLE);
				badge8.setVisibility(View.INVISIBLE);

				updateFloorBars(prefs, myFloors, buddyFloors, weightedSum, 2,
						noNewBadge);
				break;
			case 3:
				badge1.setImageResource(R.drawable.fa);
				badge2.setImageResource(R.drawable.fb);
				badge3.setImageResource(R.drawable.fc);
				badge4.setImageResource(R.drawable.no_badge);

				badge1.setVisibility(View.VISIBLE);
				badge2.setVisibility(View.VISIBLE);
				badge3.setVisibility(View.VISIBLE);
				badge4.setVisibility(View.VISIBLE);

				badge5.setVisibility(View.INVISIBLE);
				badge6.setVisibility(View.INVISIBLE);
				badge7.setVisibility(View.INVISIBLE);
				badge8.setVisibility(View.INVISIBLE);

				updateFloorBars(prefs, myFloors, buddyFloors, weightedSum, 3,
						noNewBadge);
				break;
			case 4:
				badge1.setImageResource(R.drawable.fa);
				badge2.setImageResource(R.drawable.fb);
				badge3.setImageResource(R.drawable.fc);
				badge4.setImageResource(R.drawable.fd);
				badge5.setImageResource(R.drawable.no_badge);

				badge1.setVisibility(View.VISIBLE);
				badge2.setVisibility(View.VISIBLE);
				badge3.setVisibility(View.VISIBLE);
				badge4.setVisibility(View.VISIBLE);
				badge5.setVisibility(View.VISIBLE);

				badge6.setVisibility(View.INVISIBLE);
				badge7.setVisibility(View.INVISIBLE);
				badge8.setVisibility(View.INVISIBLE);

				updateFloorBars(prefs, myFloors, buddyFloors, weightedSum, 4,
						noNewBadge);
				break;
			case 5:
				badge1.setImageResource(R.drawable.fa);
				badge2.setImageResource(R.drawable.fb);
				badge3.setImageResource(R.drawable.fc);
				badge4.setImageResource(R.drawable.fd);
				badge5.setImageResource(R.drawable.fe);
				badge6.setImageResource(R.drawable.no_badge);

				badge1.setVisibility(View.VISIBLE);
				badge2.setVisibility(View.VISIBLE);
				badge3.setVisibility(View.VISIBLE);
				badge4.setVisibility(View.VISIBLE);
				badge5.setVisibility(View.VISIBLE);
				badge6.setVisibility(View.VISIBLE);

				badge7.setVisibility(View.INVISIBLE);
				badge8.setVisibility(View.INVISIBLE);

				updateFloorBars(prefs, myFloors, buddyFloors, weightedSum, 5,
						noNewBadge);
				break;
			case 6:
				badge1.setImageResource(R.drawable.fa);
				badge2.setImageResource(R.drawable.fb);
				badge3.setImageResource(R.drawable.fc);
				badge4.setImageResource(R.drawable.fd);
				badge5.setImageResource(R.drawable.fe);
				badge6.setImageResource(R.drawable.ff);
				badge7.setImageResource(R.drawable.no_badge);

				badge1.setVisibility(View.VISIBLE);
				badge2.setVisibility(View.VISIBLE);
				badge3.setVisibility(View.VISIBLE);
				badge4.setVisibility(View.VISIBLE);
				badge5.setVisibility(View.VISIBLE);
				badge6.setVisibility(View.VISIBLE);
				badge7.setVisibility(View.VISIBLE);

				badge8.setVisibility(View.INVISIBLE);

				updateFloorBars(prefs, myFloors, buddyFloors, weightedSum, 6,
						noNewBadge);
				break;
			case 7:
				badge1.setImageResource(R.drawable.fa);
				badge2.setImageResource(R.drawable.fb);
				badge3.setImageResource(R.drawable.fc);
				badge4.setImageResource(R.drawable.fd);
				badge5.setImageResource(R.drawable.fe);
				badge6.setImageResource(R.drawable.ff);
				badge7.setImageResource(R.drawable.fg);
				badge8.setImageResource(R.drawable.no_badge);

				badge1.setVisibility(View.VISIBLE);
				badge2.setVisibility(View.VISIBLE);
				badge3.setVisibility(View.VISIBLE);
				badge4.setVisibility(View.VISIBLE);
				badge5.setVisibility(View.VISIBLE);
				badge6.setVisibility(View.VISIBLE);
				badge7.setVisibility(View.VISIBLE);
				badge8.setVisibility(View.VISIBLE);

				updateFloorBars(prefs, myFloors, buddyFloors, weightedSum, 7,
						noNewBadge);
				break;
			case 8:
				badge1.setImageResource(R.drawable.fa);
				badge2.setImageResource(R.drawable.fb);
				badge3.setImageResource(R.drawable.fc);
				badge4.setImageResource(R.drawable.fd);
				badge5.setImageResource(R.drawable.fe);
				badge6.setImageResource(R.drawable.ff);
				badge7.setImageResource(R.drawable.fg);
				badge8.setImageResource(R.drawable.fh);
				badge1.setVisibility(View.VISIBLE);
				badge2.setVisibility(View.VISIBLE);
				badge3.setVisibility(View.VISIBLE);
				badge4.setVisibility(View.VISIBLE);
				badge5.setVisibility(View.VISIBLE);
				badge6.setVisibility(View.VISIBLE);
				badge7.setVisibility(View.VISIBLE);
				badge8.setVisibility(View.VISIBLE);

				updateFloorBars(prefs, myFloors, buddyFloors, weightedSum, 8,
						noNewBadge);
				break;

			default:
				// handle case for more than 8 badges
				badge1.setImageResource(R.drawable.fa);
				badge2.setImageResource(R.drawable.fb);
				badge3.setImageResource(R.drawable.fc);
				badge4.setImageResource(R.drawable.fd);
				badge5.setImageResource(R.drawable.fe);
				badge6.setImageResource(R.drawable.ff);
				badge7.setImageResource(R.drawable.fg);
				badge8.setImageResource(R.drawable.fh);
				badge1.setVisibility(View.VISIBLE);
				badge2.setVisibility(View.VISIBLE);
				badge3.setVisibility(View.VISIBLE);
				badge4.setVisibility(View.VISIBLE);
				badge5.setVisibility(View.VISIBLE);
				badge6.setVisibility(View.VISIBLE);
				badge7.setVisibility(View.VISIBLE);
				badge8.setVisibility(View.VISIBLE);

				updateFloorBars(prefs, myFloors, buddyFloors, weightedSum,
						badge_cnt, noNewBadge);
				break;
			}

		}
	}

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
		// Toast.makeText(this,
		// "Update date: "+today+" isDifferentDate: "+isDifferentDay,
		// Toast.LENGTH_LONG).show();
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
	private void updateBars(SharedPreferences prefs, int previousUserSteps,
			int previousBuddySteps, int previousWeightedSum, int badge_cnt,
			boolean noNewBadge) {
		int newUserCompletedSteps = mySteps;
		int newBuddyCompletedSteps = buddySteps;

		
	/*	Log.i(TAG,"Parameter Layout: mySteps = " + mySteps);
		Log.i(TAG,"Parameter Layout: buddySteps = " + buddySteps);
		Log.i(TAG,"Parameter Layout: relativeUserSteps = " + relativeUserSteps);
		Log.i(TAG,"Parameter Layout: relativeBuddySteps = " + relativeBuddySteps);*/
		
		
		if (noNewBadge) {
			// still the same completed steps
			newUserCompletedSteps = prefs.getInt(
					Constants.PROP_KEY_USER_COMPLETED_STEPS, -1);
			newBuddyCompletedSteps = prefs.getInt(
					Constants.PROP_KEY_BUDDY_COMPLETED_STEPS, -1);
		} else {
			if (datePivot == 0) {
				if (!badgeDialogLaunched && (displayCongratulations)) {
					if (isActive) {
						displayCongratulations(0);
					}
				}

				updateCompletedStepsAndBadgeLevel(prefs, newUserCompletedSteps,
						newBuddyCompletedSteps, badge_cnt);
			}
			// int badgeLevel = prefs.getInt(Constants.PROP_KEY_BADGE_LEVEL,
			// -1);
			// int nextBadgeThreshold = Constants.BADGE_LEVELS[badge_cnt];
			// 

		}

		// Update bars
		drawTopBar(mySteps, buddySteps);
		drawBottomBar(mySteps, buddySteps, badge_cnt);
	}

	public void displayCongratulations(int type) 
	{
		
		if(datePivot!=0)
			return;
		
		if (isDisplayingAuthorizationDialog
				|| isDisplayingConnectionErrorDialog) {
			return;
		} else if (initialSyncDialog != null) // wait for the progress bar to
												// finish.
		{
			congratulationsDisplayRequest[type] = true;
		} else {
			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View layout = inflater
					.inflate(R.layout.activity_popup_dialog, null);
			AlertDialog.Builder builder = new AlertDialog.Builder(this)
					.setView(layout);
			
			//builder.setTitle(getResources().getString(R.string.congrats_title) + " Steps badges earned!");

			
			//alertinfo
			
			if(alertDialog == null)
			{
				alertDialog = builder.create();
				TextView tv = (TextView) layout.findViewById(R.id.alertinfo);
				if(type == 0)
				{
					tv.setText(getResources().getString(R.string.congrats_steps_title));
				}
				else
				{
					tv.setText(getResources().getString(R.string.congrats_floors_title));
				}
				alertDialog.show();
				badgeDialogLaunched = true;
				congratulationsDisplayRequest[type] = false;
			}
		}
	}

	/**
	 * Update the bars after winning badges
	 * 
	 * @param prefs
	 * @param previousUserFloors
	 * @param previousBuddyFloors
	 * @param previousWeightedSum
	 * @param noNewBadge
	 */
	private void updateFloorBars(SharedPreferences prefs,
			int previousUserFloors, int previousBuddyFloors,
			int previousWeightedSum, int badgeFloorLevel, boolean noNewBadge) {
		int newUserCompletedFloors = myFloors;
		int newBuddyCompletedFloors = buddyFloors;
		// int relativeUserFloors = (int)(myFloors*Constants.SELF_WEIGHT);
		// int relativeBuddyFloors = (int)(buddyFloors*Constants.BUDDY_WEIGHT);

		/*Log.i(TAG,"Parameter Layout: myFloors = " + myFloors);
		Log.i(TAG,"Parameter Layout: buddyFloors = " + buddyFloors);
		Log.i(TAG,"Parameter Layout: newUserCompletedFloors = " + newUserCompletedFloors);
		Log.i(TAG,"Parameter Layout: newBuddyCompletedFloors = " + newBuddyCompletedFloors);*/
		
		if (noNewBadge) {
			// still the same completed steps
			// newUserCompletedFloors =
			// prefs.getInt(Constants.PROP_KEY_USER_COMPLETED_FLOORS, -1);
			// relativeUserSteps = mySteps - newUserCompletedSteps;
			// relativeUserFloors = myFloors;
			// newBuddyCompletedFloors =
			// prefs.getInt(Constants.PROP_KEY_BUDDY_COMPLETED_FLOORS, -1);
			// relativeBuddySteps = buddySteps - newBuddyCompletedSteps;
			// relativeBuddyFloors = buddyFloors;
		} else {
			if (datePivot == 0) {
				/*
				 * LayoutInflater inflater = (LayoutInflater)
				 * getSystemService(Context.LAYOUT_INFLATER_SERVICE); View
				 * layout = inflater.inflate(R.layout.activity_popup_dialog,
				 * null); AlertDialog.Builder builder = new
				 * AlertDialog.Builder(this) .setView(layout); alertDialog =
				 * builder.create(); alertDialog.show();
				 */
				
				
					if (!badgeDialogLaunched && (displayCongratulations)) {
						if (isActive) {
							displayCongratulations(1);
						}
					}				
				
				updateCompletedFloorsAndBadgeLevel(prefs,
						newUserCompletedFloors, newBuddyCompletedFloors,
						badgeFloorLevel);
			}

		}

		// Update bars
		drawTopFloorBar(myFloors, buddyFloors);
		drawBottomFloorBar(myFloors, buddyFloors, badgeFloorLevel);
	}

	/**
	 * Update user & buddy completed steps with the current absolute steps
	 * fetched from backend.
	 * 
	 * @param prefs
	 */
	private void updateCompletedStepsAndBadgeLevel(SharedPreferences prefs,
			int newUserCompletedSteps, int newBuddyCompletedSteps,
			int newBadgeLevel) {
		// Toast.makeText(Main2Activity.this,
		// "Completed steps updated! my:"+newUserCompletedSteps+" buddy:"+newBuddyCompletedSteps,
		// Toast.LENGTH_LONG).show();
		Editor editor = prefs.edit();
		editor.putInt(Constants.PROP_KEY_USER_COMPLETED_STEPS,
				newUserCompletedSteps);
		editor.putInt(Constants.PROP_KEY_BUDDY_COMPLETED_STEPS,
				newBuddyCompletedSteps);
		editor.putInt(Constants.PROP_KEY_BADGE_LEVEL, newBadgeLevel);

		editor.putInt(Constants.PROP_KEY_USER_HISTORY_COMPLETED_STEPS + "_"
				+ datePivot, myStepHistory[datePivot]);
		editor.putInt(Constants.PROP_KEY_BUDDY_HISTORY_COMPLETED_STEPS + "_"
				+ datePivot, buddyStepHistory[datePivot]);
		/*
		 * public static final String PROP_KEY_USER_HISTORY_COMPLETED_FLOORS =
		 * "user_completed_history_floors"; public static final String
		 * PROP_KEY_BUDDY_HISTORY_COMPLETED_FLOORS =
		 * "buddy_completed_history_floors"; public static final String
		 * PROP_KEY_USER_HISTORY_COMPLETED_STEPS =
		 * "user_completed_history_steps"; public static final String
		 * PROP_KEY_BUDDY_HISTORY_COMPLETED_STEPS =
		 * "buddy_completed_history_steps";
		 */

		editor.commit();
	}

	private void updateCompletedFloorsAndBadgeLevel(SharedPreferences prefs,
			int newUserCompletedFloors, int newBuddyCompletedFloors,
			int newFloorBadgeLevel) {
		// Toast.makeText(Main2Activity.this,
		// "Completed steps updated! my:"+newUserCompletedSteps+" buddy:"+newBuddyCompletedSteps,
		// Toast.LENGTH_LONG).show();
		Editor editor = prefs.edit();
		editor.putInt(Constants.PROP_KEY_USER_COMPLETED_FLOORS,
				newUserCompletedFloors);
		editor.putInt(Constants.PROP_KEY_BUDDY_COMPLETED_FLOORS,
				newBuddyCompletedFloors);
		editor.putInt(Constants.PROP_KEY_FLOOR_BADGE_LEVEL, newFloorBadgeLevel);

		editor.putInt(Constants.PROP_KEY_USER_HISTORY_COMPLETED_FLOORS + "_"
				+ datePivot, myFloorHistory[datePivot]);
		editor.putInt(Constants.PROP_KEY_BUDDY_HISTORY_COMPLETED_FLOORS + "_"
				+ datePivot, buddyFloorHistory[datePivot]);
		/*
		 * public static final String PROP_KEY_USER_HISTORY_COMPLETED_FLOORS =
		 * "user_completed_history_floors"; public static final String
		 * PROP_KEY_BUDDY_HISTORY_COMPLETED_FLOORS =
		 * "buddy_completed_history_floors"; public static final String
		 * PROP_KEY_USER_HISTORY_COMPLETED_STEPS =
		 * "user_completed_history_steps"; public static final String
		 * PROP_KEY_BUDDY_HISTORY_COMPLETED_STEPS =
		 * "buddy_completed_history_steps";
		 */

		editor.commit();
	}

	// private void updateBadges() {
	private void updateBadgesWithSync(final boolean isManual) {

		if (!Utils.isConnectionPresent(this)) {
			updateDisplay(datePivot == 0);
			showProgressDialog(false);
			showSyncFailedDialog();
			return;
		}

		if(isManual)
		{
			lastSyncTime = new Date(); // enforce the last check time.
		}
		
		checkValidityOfPreviousDay();
		/*
		 * if(isManual) { Toast.makeText(this, "Synchronizing your data...",
		 * Toast.LENGTH_SHORT).show(); }
		 */

		
		// updateDisplay(datePivot == 0);
		// this slow us down?

		badgeDialogLaunched = false;
		final int uid = AppContext.getInstance().getUserId();
		final int fid = getFriendId(); // AppContext.getInstance().getFriendId();
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
				showBuddyName(fid);
				updateDisplay(datePivot == 0);
				if (datePivot != 0) // the user is currently viewing a past day.
				{
					automaticUpdate(datePivot, true);
				}

				if (isManual) {
					showProgressDialog(true);
				}
				super.onPreExecute();
			}

			@Override
			protected void onPostExecute(Boolean success) {
				if (success && Utils.isConnectionPresent(Main2Activity.this)) {
					// updateDisplay(datePivot == 0);
					// will slow down the process

					// also fetch latest buddy's steps

					SyncBackendWithFitbitTask syncBuddyTask = new SyncBackendWithFitbitTask(
							fid, uid) {

						@Override
						protected void onPostExecute(Boolean success) {

							if (success
									&& Utils.isConnectionPresent(Main2Activity.this)) {
								// continue
								updatePledgeBar(0);
								updatePledgeBar(1);
								
								updateDisplay(datePivot == 0);
								showProgressDialog(false);
							} else if (Utils
									.isConnectionPresent(Main2Activity.this)) {
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
				} else if (!Utils.isConnectionPresent(Main2Activity.this)) {
					showSyncFailedDialog();
				}
			}
		};

		syncOwnTask.execute();

		/*
		 * if(isManual) { try { syncOwnTask.get(); } catch (InterruptedException
		 * e) { e.printStackTrace(); } catch
		 * (ExecutionException e) { 
		 * e.printStackTrace(); } }
		 */

		if (!isManual) // Onur: I added this condition. Would this be a problem?
		{
			for (int d = 1; d < 7; d++) {
				if (d != datePivot) {
					automaticUpdate(d, false);
				}
			}
		}

	}

	private void checkFitbitAuthentication() {
		CheckFitbitAuthenticationTask authTask = new CheckFitbitAuthenticationTask(
				AppContext.getInstance().getUserId()) 
		{
			
			protected void onPostExecute(Boolean success) 
			{
				
				if(!this.safeExecution)
				{
					if(!Utils.isConnectionPresent(Main2Activity.this))
					{
						showSyncFailedDialog();
					}
					return;
				}
				
				if (!success && Utils.isConnectionPresent(Main2Activity.this) && this.outcome.contains("not validated")) {
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

	/* The "seamless" version of updateStepBadges */
	private void writeStepProgress(final int pivotIndex) {
		RetrieveStepCountTask task = new RetrieveStepCountTask(AppContext
				.getInstance().getUserId(), pivotIndex) {

			@Override
			protected void onPostExecute(final Integer myStepCount) 
			{
				if(this.safeExecution && Utils.isConnectionPresent(Main2Activity.this))
				{
					int fid = getFriendId();
					RetrieveStepCountTask task2 = new RetrieveStepCountTask(fid,
							pivotIndex) {
	
						@Override
						protected void onPostExecute(Integer friendStepCount)
						{
							if(this.safeExecution && Utils.isConnectionPresent(Main2Activity.this))
							{
								int myRetrievedSteps = myStepCount;
								int buddyRetrievedSteps = friendStepCount;
								// Log.d(TAG, "My steps are " + myRetrievedSteps);
								// Log.d(TAG, "Buddy's steps are " +
								// buddyRetrievedSteps);
								if (myRetrievedSteps == 0) {
									// Log.d(TAG,
									// "my steps is 0 fSteps: "+buddyRetrievedSteps);
								}
		
								// checkStepBadges();
		
								myStepHistory[pivotIndex] = myRetrievedSteps;
								buddyStepHistory[pivotIndex] = buddyRetrievedSteps;
		
								SharedPreferences prefs = getSharedPreferences(
										Constants.PROPERTIES_NAME, Context.MODE_PRIVATE);
								Editor editor = prefs.edit();
								editor.putInt(
										Constants.PROP_KEY_USER_HISTORY_COMPLETED_STEPS
												+ "_" + pivotIndex,
										myStepHistory[pivotIndex]);
								editor.putInt(
										Constants.PROP_KEY_BUDDY_HISTORY_COMPLETED_STEPS
												+ "_" + pivotIndex,
										buddyStepHistory[pivotIndex]);
								editor.commit();
							}
						}
					};
					task2.execute();
				}
			}
		};
		task.execute();

	}

	/* The "seamless" version of updateStepBadges */
	private void writeFloorProgress(final int pivotIndex) {
		RetrieveFloorCountTask task = new RetrieveFloorCountTask(AppContext
				.getInstance().getUserId(), pivotIndex) {

			@Override
			protected void onPostExecute(final Integer myFloorCount) {
				
				if(this.safeExecution && Utils.isConnectionPresent(Main2Activity.this))
				{
					int fid = getFriendId();
					RetrieveFloorCountTask task2 = new RetrieveFloorCountTask(fid,
							pivotIndex) {
	
						@Override
						protected void onPostExecute(Integer friendFloorCount) 
						{
							
							if(this.safeExecution && Utils.isConnectionPresent(Main2Activity.this))
							{
								int myRetrievedSteps = myFloorCount;
								int buddyRetrievedSteps = friendFloorCount;
								// Log.d(TAG, "My floors are " + myRetrievedSteps);
								// Log.d(TAG, "Buddy's floors are " +
								// buddyRetrievedSteps);
								if (myRetrievedSteps == 0) {
									// Log.d(TAG,
									// "my floors is 0 fSteps: "+buddyRetrievedSteps);
								}
		
								// checkStepBadges();
		
								myFloorHistory[pivotIndex] = myRetrievedSteps;
								buddyFloorHistory[pivotIndex] = buddyRetrievedSteps;
		
								SharedPreferences prefs = getSharedPreferences(
										Constants.PROPERTIES_NAME, Context.MODE_PRIVATE);
								Editor editor = prefs.edit();
								editor.putInt(
										Constants.PROP_KEY_USER_HISTORY_COMPLETED_FLOORS
												+ "_" + pivotIndex,
										myFloorHistory[pivotIndex]);
								editor.putInt(
										Constants.PROP_KEY_BUDDY_HISTORY_COMPLETED_FLOORS
												+ "_" + pivotIndex,
										buddyFloorHistory[pivotIndex]);
								editor.commit();
							}
						}
					};
					task2.execute();
				}
			}
		};
		task.execute();

	}

	private void updateStepBadges() {
		RetrieveStepCountTask task = new RetrieveStepCountTask(AppContext
				.getInstance().getUserId(), datePivot) {

			@Override
			protected void onPostExecute(final Integer myStepCount) 
			{
				if (this.safeExecution && Utils.isConnectionPresent(Main2Activity.this)) 
				{
					int fid = getFriendId();
					RetrieveStepCountTask task2 = new RetrieveStepCountTask(
							fid, datePivot) {

						@Override
						protected void onPostExecute(Integer friendStepCount) {

							if (this.safeExecution && Utils.isConnectionPresent(Main2Activity.this)) {
								mySteps = myStepCount;
								buddySteps = friendStepCount;
								recordValue(myStepCount, 0, 0);
								recordValue(friendStepCount, 0, 1);
								// Log.d(TAG, "My steps are " + mySteps);
								// Log.d(TAG, "Buddy's steps are " +
								// buddySteps);
								if (mySteps == 0) {
									// Log.d(TAG,
									// "my steps is 0 fSteps: "+buddySteps);
								}
								checkStepBadges();
							}
							else
							{
							//	if(myStepHistory[datePivot] > 0)
							//	{
									mySteps = myStepHistory[datePivot];
							//	}
							//	if(buddyStepHistory[datePivot] > 0)
							//	{
									buddySteps = buddyStepHistory[datePivot];
							//	}
								
								checkStepBadges();
							}
						}
					};
					task2.execute();
				}
				else
				{
					//if(myStepHistory[datePivot] > 0)
					//{
						mySteps = myStepHistory[datePivot];
					//}
					//if(buddyStepHistory[datePivot] > 0)
					//{
						buddySteps = buddyStepHistory[datePivot];
					//}
					checkStepBadges();
				}
			}
		};
		task.execute();
	}

	private void updateFloorBadges() {
		RetrieveFloorCountTask task = new RetrieveFloorCountTask(AppContext
				.getInstance().getUserId(), datePivot) {

			@Override
			protected void onPostExecute(final Integer myFloorCount) {
				if (this.safeExecution && Utils.isConnectionPresent(Main2Activity.this)) 
				{
					int fid = getFriendId();
					RetrieveFloorCountTask task2 = new RetrieveFloorCountTask(
							fid, datePivot) {

						@Override
						protected void onPostExecute(Integer friendFloorCount) {
							if (this.safeExecution &&  Utils.isConnectionPresent(Main2Activity.this)) {
								// Toast.makeText(Main2Activity.this,
								// "my step count: " + myStepCount +
								// " / friend: " + friendStepCount,
								// Toast.LENGTH_SHORT).show();
								myFloors = myFloorCount;
								buddyFloors = friendFloorCount;
								recordValue(myFloorCount, 1, 0);
								recordValue(friendFloorCount, 1, 1);
								
								// Log.d(TAG, "My floors are " + myFloors);
								// Log.d(TAG, "Buddy's floors are " +
								// buddyFloors);
								if (myFloors == 0) {
									// DEBUG
									// Toast.makeText(Main2Activity.this,
									// "mySteps is 0",
									// Toast.LENGTH_LONG).show();
									// Log.d(TAG,
									// "my floor is 0 f-floors: "+buddyFloors);
								}

								// mySteps = 9000;
								// buddySteps = 5000;
								checkFloorBadges();
							}
							else
							{
							//	if(myFloorHistory[datePivot] > 0)
							//	{
									myFloors = myFloorHistory[datePivot];
							//	}
							//	if(buddyFloorHistory[datePivot] > 0)
							//	{
									buddyFloors = buddyFloorHistory[datePivot];
							//	}
								checkFloorBadges();
							}
						}
					};
					task2.execute();
				}
				else
				{
				//	if(myFloorHistory[datePivot] > 0)
				//	{
						myFloors = myFloorHistory[datePivot];
				//	}
				//	if(buddyFloorHistory[datePivot] > 0)
				//	{
						buddyFloors = buddyFloorHistory[datePivot];
				//	}						
					checkFloorBadges();
				}
			}
		};
		task.execute();
	}

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
					Intent i = new Intent(Main2Activity.this,
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


	// causes the problem
	private void showSyncFailedDialog() {
		// Toast.makeText(Main2Activity.this,
		// "Cannot synchronize: No internet connection.",
		// Toast.LENGTH_LONG).show();
		displayConnectionErrorMessage();
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

	protected void drawTopBar(int userSteps, int otherSteps) 
	{
		
		SharedPreferences prefs = getSharedPreferences(
				Constants.PROPERTIES_NAME, Context.MODE_PRIVATE);
//		Log.i(TAG,"drawTopBar Layout: userSteps = " + userSteps);
//		Log.i(TAG,"drawTopBar Layout: otherSteps = " + otherSteps);
				
		
		View topBar = findViewById(R.id.topBarChart);
		int maxBarLengthPx = topBar.getWidth();
		
		if(maxBarLengthPx > 0)
		{
			Editor editor = prefs.edit();
			editor.putInt(Constants.PROGRESS_BAR_LENGTH, maxBarLengthPx);
			editor.commit();
		}
		else
		{
			maxBarLengthPx = prefs.getInt(Constants.PROGRESS_BAR_LENGTH, 0);
		}
	/*	while(maxBarLengthPx == 0)
		{
			maxBarLengthPx = topBar.getMeasuredWidth();
			Log.i(TAG,"Loop: maxBarLengthPx = " + maxBarLengthPx);
		}*/
		
	//	Log.i(TAG,"drawTopBar Layout: tbwidth(before) = " + tbwidth);
	//	Log.i(TAG,"drawTopBar Layout: maxBarLengthPx = " + maxBarLengthPx);
		tbwidth = maxBarLengthPx;
		//Log.i(TAG,"drawTopBar Layout: tbwidth(after) = " + tbwidth);
		// Log.d(TAG, "max bar length: " + maxBarLengthPx);
		int totalSteps = userSteps + otherSteps;
		// Draw my progress
		float mybarLength;
		float otherBarLength;
		if (userSteps / (float) totalSteps <= 0.03 && otherSteps != 0) {
			// just a placeholder
			mybarLength = (float) (0.03 * maxBarLengthPx);
			otherBarLength = (float) (0.97 * maxBarLengthPx);
		} else if (userSteps != 0 && otherSteps / (float) totalSteps <= 0.03) {
			mybarLength = (float) (0.97 * maxBarLengthPx);
			otherBarLength = (float) (0.03 * maxBarLengthPx);
		} else if (userSteps == 0 && otherSteps == 0) {
			mybarLength = (float) (0.03 * maxBarLengthPx);
			otherBarLength = (float) (0.03 * maxBarLengthPx);
		} else {
			mybarLength = (userSteps / (float) totalSteps) * maxBarLengthPx;
			otherBarLength = (otherSteps / (float) totalSteps) * maxBarLengthPx;
		}

		// Draw user's step progress
		//float mybarLengthDp = dpFromPx(mybarLength);
		// Log.d(TAG, "my bar length: " + mybarLength);
		View myProgress = findViewById(R.id.topBarMineMask);
		int currentHeight = myProgress.getHeight();
		theight = currentHeight;
		if(currentHeight > 0)
		{
			Editor editor = prefs.edit();
			editor.putInt(Constants.PROGRESS_BAR_HEIGHT, currentHeight);
			editor.commit();
		}
		else
		{
			currentHeight = prefs.getInt(Constants.PROGRESS_BAR_HEIGHT, 0);
			theight = currentHeight;
		}
		
		myProgress.setLayoutParams(new LayoutParams((int) mybarLength,
				(int) currentHeight));

		myProgress.invalidate();

		// Update user steps
		TextView myStepLabel = (TextView) findViewById(R.id.topBarMineLabel);
		myStepLabel.setText(userSteps + "");
		myStepLabel.invalidate();

		// Draw friend's step progress
		//float otherBarLengthDp = dpFromPx(otherBarLength);
		// Log.d(TAG, "friend's bar length: " + otherBarLength);
		View otherProgress = findViewById(R.id.topBarOtherMask);
		int otherCurrentHeight = theight;//otherProgress.getHeight();
		//theight = otherCurrentHeight;
		
		if(otherCurrentHeight > 0)
		{
			Editor editor = prefs.edit();
			editor.putInt(Constants.PROGRESS_BAR_HEIGHT, otherCurrentHeight);
			editor.commit();
		}
		else
		{
			otherCurrentHeight = prefs.getInt(Constants.PROGRESS_BAR_HEIGHT, 0);
			theight = otherCurrentHeight;
		}	
		
		
		
		otherProgress.setLayoutParams(new LayoutParams((int) otherBarLength,
				(int) otherCurrentHeight));
		otherProgress.invalidate();
		// Update other steps
		TextView otherStepLabel = (TextView) findViewById(R.id.topBarOtherLabel);
		otherStepLabel.setText(otherSteps + "");
		otherStepLabel.invalidate();

		// if(!(userSteps == 0) && !(otherSteps == 0))
		// {
		int remainingBarLength = maxBarLengthPx - (int) mybarLength
				- (int) otherBarLength - 5;
		// Log.d(TAG, "remaining steps length: " + ((int)remainingBarLength));
		View remainingProgress = findViewById(R.id.topBarMiddleMask);

		int remainingProgressCurrentHeight = theight;//remainingProgress.getHeight();
		remainingProgress
				.setLayoutParams(new LayoutParams((int) remainingBarLength,
						(int) remainingProgressCurrentHeight));
		remainingProgress.invalidate();

		// }

	}

	protected void drawTopFloorBar(int userFloors, int otherFloors) {
		// View topFloorBar =
		// findViewById(R.id.topBarChart);//findViewById(R.id.topBarFloorChart);
		int maxBarLengthPx = tbwidth;// = topFloorBar.getWidth();
		
		SharedPreferences prefs = getSharedPreferences(
				Constants.PROPERTIES_NAME, Context.MODE_PRIVATE);
		//int maxBarLengthPx = bottomBar.getWidth();
		
		if(maxBarLengthPx > 0)
		{
			Editor editor = prefs.edit();
			editor.putInt(Constants.PROGRESS_BAR_LENGTH, maxBarLengthPx);
			editor.commit();
		}
		else
		{
			maxBarLengthPx = prefs.getInt(Constants.PROGRESS_BAR_LENGTH, 0);
		}
		
		double percent1 = 0.03;
		double percent2 = 0.97;
		if (maxBarLengthPx == 0) {
			// Log.e(TAG, "qweqwqwqweqewqweqweqweqweqweqweReceived as 0");
		}
		// Log.d(TAG, "top floor bar: max bar length: " + maxBarLengthPx);
		int totalFloors = userFloors + otherFloors;
		// Draw my progress
		float myFloorBarLength;
		float otherFloorBarLength;
		if (userFloors / (float) totalFloors <= percent1 && otherFloors != 0) {
			// just a placeholder
			myFloorBarLength = (float) (percent1 * maxBarLengthPx);
			otherFloorBarLength = (float) (percent2 * maxBarLengthPx);
			// Log.d(TAG,"!!!(1)!!!!userFloors"+userFloors+"totalFloors"+totalFloors+"myFloorBarLength:"+myFloorBarLength+", otherFloorBarLength"+otherFloorBarLength);
		} else if (userFloors != 0
				&& otherFloors / (float) totalFloors <= percent1) {
			myFloorBarLength = (float) (percent2 * maxBarLengthPx);
			otherFloorBarLength = (float) (percent1 * maxBarLengthPx);
			// Log.d(TAG,"!!!(2)!!!!userFloors"+userFloors+"totalFloors"+totalFloors+"myFloorBarLength:"+myFloorBarLength+", otherFloorBarLength"+otherFloorBarLength);
		} else if (userFloors == 0 && otherFloors == 0) {
			myFloorBarLength = (float) (percent1 * maxBarLengthPx);
			otherFloorBarLength = (float) (percent1 * maxBarLengthPx);
			// Log.d(TAG,"!!!(3)!!!!userFloors"+userFloors+"totalFloors"+totalFloors+"myFloorBarLength:"+myFloorBarLength+", otherFloorBarLength"+otherFloorBarLength);
		} else {
			myFloorBarLength = Math.max((float) (percent1 * maxBarLengthPx),
					(userFloors / (float) totalFloors) * maxBarLengthPx);
			otherFloorBarLength = Math.max((float) (percent1 * maxBarLengthPx),
					(otherFloors / (float) totalFloors) * maxBarLengthPx);
			// Log.d(TAG,"!!!(4)!!!!userFloors"+userFloors+"totalFloors"+totalFloors+"myFloorBarLength:"+myFloorBarLength+", otherFloorBarLength"+otherFloorBarLength);
		}

		// Draw user's floor progress
		//float myFloorBarLengthDp = dpFromPx(myFloorBarLength);
		// Log.d(TAG, "*** my FLOOR bar length: " + myFloorBarLength);
		View myFloorProgress = findViewById(R.id.topBarFloorMineMask);
		int currentFloorHeight = theight;// myFloorProgress.getHeight();
		
		if(currentFloorHeight > 0)
		{
			Editor editor = prefs.edit();
			editor.putInt(Constants.PROGRESS_BAR_HEIGHT, currentFloorHeight);
			editor.commit();
		}
		else
		{
			currentFloorHeight = prefs.getInt(Constants.PROGRESS_BAR_HEIGHT, 0);
			theight = currentFloorHeight;
		}	
		
		myFloorProgress.setLayoutParams(new LayoutParams(
				(int) myFloorBarLength, (int) currentFloorHeight));
		myFloorProgress.invalidate();
		// Update user floors
		TextView myFloorLabel = (TextView) findViewById(R.id.topBarFloorMineLabel);
		myFloorLabel.setText(userFloors + "");
		myFloorLabel.invalidate();

		// Draw friend's floor progress
		//float otherFloorBarLengthDp = dpFromPx(otherFloorBarLength);
		// Log.d(TAG, "*** friend's FLOOR bar length: " + otherFloorBarLength);
		View otherFloorProgress = findViewById(R.id.topBarFloorOtherMask);
		int otherFloorCurrentHeight = theight;// otherFloorProgress.getHeight();
		
		otherFloorProgress.setLayoutParams(new LayoutParams(
				(int) otherFloorBarLength, (int) otherFloorCurrentHeight));
		otherFloorProgress.invalidate();
		// Update other floors
		TextView otherFloorLabel = (TextView) findViewById(R.id.topBarFloorOtherLabel);
		otherFloorLabel.setText(otherFloors + "");
		otherFloorLabel.invalidate();

		int remainingBarLength = maxBarLengthPx - (int) myFloorBarLength
				- (int) otherFloorBarLength - 5;
		// Log.d(TAG, "remaining steps length: " + ((int)remainingBarLength));
		View remainingProgress = findViewById(R.id.topBarFloorMiddleMask);

		int remainingProgressCurrentHeight = remainingProgress.getHeight();
		remainingProgress
				.setLayoutParams(new LayoutParams((int) remainingBarLength,
						(int) remainingProgressCurrentHeight));
		remainingProgress.invalidate();
	}

	protected void drawBottomBar(int userSteps, int otherSteps, int badge_cnt) {
		View bottomBar = findViewById(R.id.bottomBarChart);
		
		SharedPreferences prefs = getSharedPreferences(
				Constants.PROPERTIES_NAME, Context.MODE_PRIVATE);
		int maxBarLengthPx = bottomBar.getWidth();
		
		if(maxBarLengthPx > 0)
		{
			Editor editor = prefs.edit();
			editor.putInt(Constants.PROGRESS_BAR_LENGTH, maxBarLengthPx);
			editor.commit();
		}
		else
		{
			maxBarLengthPx = prefs.getInt(Constants.PROGRESS_BAR_LENGTH, 0);
		}
		
	/*	while(maxBarLengthPx == 0)
		{
			maxBarLengthPx = bottomBar.getMeasuredWidth();
		}*/
		
		int weightedUserSteps = (int) (userSteps * Constants.SELF_WEIGHT);
		int weightedOtherSteps = (int) (otherSteps * Constants.BUDDY_WEIGHT);

		/*SharedPreferences prefs = getSharedPreferences(
				Constants.PROPERTIES_NAME, Context.MODE_PRIVATE);*/
		// int badgeLevel = prefs.getInt(Constants.PROP_KEY_BADGE_LEVEL, -1);
		int badgeLevel = badge_cnt;
		int nextBadgeThreshold = Constants.BADGE_LEVELS[badgeLevel];

		// Draw my progress
		float mybarLength = (weightedUserSteps / (float) nextBadgeThreshold)
				* maxBarLengthPx;
		if ((weightedUserSteps / (float) nextBadgeThreshold) <= 0.03) {
			mybarLength = (float) (0.03 * maxBarLengthPx);
		}
		// Log.d(TAG, "my weighted bar length: " + ((int)mybarLength));
		View myProgress = findViewById(R.id.bottomBarMineMask);
		int currentHeight = myProgress.getHeight();
		
		if(theight > currentHeight)
		{
			currentHeight = theight;
		}
		if(currentHeight > 0)
		{
			Editor editor = prefs.edit();
			editor.putInt(Constants.PROGRESS_BAR_HEIGHT, currentHeight);
			editor.commit();
		}
		else
		{
			currentHeight = prefs.getInt(Constants.PROGRESS_BAR_HEIGHT, 0);
			theight = currentHeight;
		}	
		
		myProgress.setLayoutParams(new LayoutParams((int) mybarLength,
				(int) currentHeight));
		myProgress.invalidate();
		// Update user's weighted steps
		TextView myStepLabel = (TextView) findViewById(R.id.bottomBarMineLabel);
		myStepLabel.setText(weightedUserSteps + "");
		myStepLabel.invalidate();

		// Draw friend's progress
		float otherBarLength = (weightedOtherSteps / (float) nextBadgeThreshold)
				* maxBarLengthPx;
		if ((weightedOtherSteps / (float) nextBadgeThreshold) <= 0.03) {
			otherBarLength = (float) (0.03 * maxBarLengthPx);
		}
		
		
		// Log.d(TAG, "friend's weighted bar length: " + ((int)otherBarLength));
		View otherProgress = findViewById(R.id.bottomBarOtherMask);
		int otherCurrentHeight = theight; //otherProgress.getHeight();
		
		Log.d(Main2Activity.TAG, "my bar height: " + currentHeight + " , my bar width: " + mybarLength);
		
		Log.d(Main2Activity.TAG, "other bar height: " + otherCurrentHeight + " , other bar width: " + otherBarLength);
		otherProgress.setLayoutParams(new LayoutParams((int) otherBarLength,
				(int) otherCurrentHeight));
		otherProgress.invalidate();
		// Update other's weighted steps
		TextView otherStepLabel = (TextView) findViewById(R.id.bottomBarOtherLabel);
		otherStepLabel.setText(weightedOtherSteps + "");
		otherStepLabel.invalidate();

		// Draw the middle place holder width
		int remainingSteps = (int) ((nextBadgeThreshold - weightedUserSteps - weightedOtherSteps) / Constants.SELF_WEIGHT);
		if ((userSteps == 0) && (otherSteps == 0))
			remainingSteps = 500;

		float remainingBarLength = maxBarLengthPx - (int) mybarLength
				- (int) otherBarLength - 5;
		
		if(remainingBarLength < 0)
		{
			remainingBarLength = 1;
		}
		// Log.d(TAG, "remaining steps length: " + ((int)remainingBarLength));
		View remainingProgress = findViewById(R.id.bottomBarMiddleMask);

		int remainingProgressCurrentHeight = theight;//remainingProgress.getHeight();
		
		Log.d(Main2Activity.TAG, "remaining bar height: " + remainingProgressCurrentHeight + " , remaining bar width: " + remainingBarLength);
		
		remainingProgress
				.setLayoutParams(new LayoutParams((int) remainingBarLength,
						(int) remainingProgressCurrentHeight));
		
		remainingProgress.invalidate();
		// Update user's remaining steps
		TextView remainingStepL = (TextView) findViewById(R.id.textViewRemainingStepsLabel);
		remainingStepL.setText(getResources().getString(R.string.steps_until_next_badge) + " ");
		remainingStepL.invalidate();
		TextView remainingStepsLabel = (TextView) findViewById(R.id.textViewRemainingSteps);
		remainingStepsLabel.setText(Html.fromHtml("(<b>" + remainingSteps + "</b> " + getResources().getString(R.string.until_next_badge) + ") "));
		remainingStepsLabel.invalidate();

	}

	protected void drawBottomFloorBar(int userFloors, int otherFloors,
			int floor_badge_cnt) {
		// View bottomBar =
		// findViewById(R.id.bottomBarChart);//findViewById(R.id.bottomBarFloorChart);
		int maxBarLengthPx = tbwidth;// bottomBar.getWidth();
		
		SharedPreferences prefs = getSharedPreferences(
				Constants.PROPERTIES_NAME, Context.MODE_PRIVATE);
		
		
		if(maxBarLengthPx > 0)
		{
			Editor editor = prefs.edit();
			editor.putInt(Constants.PROGRESS_BAR_LENGTH, maxBarLengthPx);
			editor.commit();
		}
		else
		{
			maxBarLengthPx = prefs.getInt(Constants.PROGRESS_BAR_LENGTH, 0);
			tbwidth = maxBarLengthPx;
		}
		
		int weightedUserFloors = (int) (userFloors * Constants.SELF_WEIGHT);
		int weightedOtherFloors = (int) (otherFloors * Constants.BUDDY_WEIGHT);

		// SharedPreferences prefs =
		// getSharedPreferences(Constants.PROPERTIES_NAME,
		// Context.MODE_PRIVATE);
		// int floorbadgeLevel =
		// prefs.getInt(Constants.PROP_KEY_FLOOR_BADGE_LEVEL, -1);
		int floorbadgeLevel = floor_badge_cnt;
		int nextFloorBadgeThreshold = Constants.FLOOR_BADGE_LEVELS[floorbadgeLevel];
		// Log.d(TAG, "*****current floor level "+floorbadgeLevel);

		// Draw my floor progress
		float myFloorBarLength = (weightedUserFloors / (float) nextFloorBadgeThreshold)
				* maxBarLengthPx;
		if ((weightedUserFloors / (float) nextFloorBadgeThreshold) <= 0.03) {
			myFloorBarLength = (float) (0.03 * maxBarLengthPx);
		}
		// Log.d(TAG, "my weighted bar length: " + ((int)myFloorBarLength));
		View myFloorProgress = findViewById(R.id.bottomBarFloorMineMask);
		int currentFloorHeight = theight;// myFloorProgress.getHeight();
		
		if(currentFloorHeight > 0)
		{
			Editor editor = prefs.edit();
			editor.putInt(Constants.PROGRESS_BAR_HEIGHT, currentFloorHeight);
			editor.commit();
		}
		else
		{
			currentFloorHeight = prefs.getInt(Constants.PROGRESS_BAR_HEIGHT, 0);
			theight = currentFloorHeight;
		}		
		
		
		myFloorProgress.setLayoutParams(new LayoutParams(
				(int) myFloorBarLength, (int) currentFloorHeight));
		myFloorProgress.invalidate();
		// Update user's weighted floors
		TextView myFloorLabel = (TextView) findViewById(R.id.bottomBarFloorMineLabel);
		myFloorLabel.setText(weightedUserFloors + "");
		myFloorLabel.invalidate();

		// Draw friend's floor progress
		float otherFloorBarLength = (weightedOtherFloors / (float) nextFloorBadgeThreshold)
				* maxBarLengthPx;
		if ((weightedOtherFloors / (float) nextFloorBadgeThreshold) <= 0.03) {
			otherFloorBarLength = (float) (0.03 * maxBarLengthPx);
		}
		// Log.d(TAG, "friend's weighted bar length: " +
		// ((int)otherFloorBarLength));
		View otherFloorProgress = findViewById(R.id.bottomBarFloorOtherMask);
		int otherFloorCurrentHeight = theight;// otherFloorProgress.getHeight();
		otherFloorProgress.setLayoutParams(new LayoutParams(
				(int) otherFloorBarLength, (int) otherFloorCurrentHeight));
		otherFloorProgress.invalidate();
		// Update other's weighted steps
		TextView otherFloorLabel = (TextView) findViewById(R.id.bottomBarFloorOtherLabel);
		otherFloorLabel.setText(weightedOtherFloors + "");
		otherFloorLabel.invalidate();

		// Draw the middle FLOOR place holder width
		int remainingFloors = (int) ((nextFloorBadgeThreshold
				- weightedUserFloors - weightedOtherFloors) / Constants.SELF_WEIGHT);
		if ((userFloors == 0) && (otherFloors == 0))
			remainingFloors = 2;
		// Log.d(TAG,"******nextThreashold is: "+ nextFloorBadgeThreshold+
		// "floor_b_cnt: "+floor_badge_cnt +"weightedUserFloors" +
		// weightedUserFloors + "weightedOtherFloors" + weightedOtherFloors);

		/*int remainingFloorBarLength = maxBarLengthPx - (int) myFloorBarLength
				- (int) otherFloorBarLength - 5;*/
		
		float remainingFloorBarLength = maxBarLengthPx - (int) myFloorBarLength
				- (int) otherFloorBarLength - 5;
		
		if(remainingFloorBarLength < 0)
		{
			remainingFloorBarLength = 1;
		}
		
		// Log.d(TAG, "remaining steps length: " +
		// ((int)remainingFloorBarLength));
		View remainingFloorProgress = findViewById(R.id.bottomBarFloorMiddleMask);

		int remainingFloorProgressCurrentHeight = theight;//remainingFloorProgress.getHeight();
		remainingFloorProgress.setLayoutParams(new LayoutParams(
				(int) remainingFloorBarLength,
				(int) remainingFloorProgressCurrentHeight));
		remainingFloorProgress.invalidate();
		// Update user's remaining floors
		TextView remainingFloorL = (TextView) findViewById(R.id.textViewRemainingFloorsLabel);
		remainingFloorL.setText(getResources().getString(R.string.floors_until_next_badge) + " ");
		remainingFloorL.invalidate();
		TextView remainingFloorLabel = (TextView) findViewById(R.id.textViewRemainingFloors);
		remainingFloorLabel.setText(Html.fromHtml("(<b>" + remainingFloors + "</b> " + getResources().getString(R.string.until_next_badge) + ") "));
		remainingFloorLabel.invalidate();

		// Log.d(TAG,"!!!!!!!!!!!!nextBadgeThreashold is: "+
		// nextFloorBadgeThreshold + ", remainingFloors: " +remainingFloors);
	}

	/**
	 * This method converts device specific pixels to device independent pixels.
	 * 
	 * @param px
	 *            A value in px (pixels) unit. Which we need to convert into db
	 * @param context
	 *            Context to get resources and device specific display metrics
	 * @return A float value to represent db equivalent to px value
	 */
	/*private float dpFromPx(float px) {
		return px / getResources().getDisplayMetrics().density;
	}*/

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

	public void onPopupOkButtonClicked(View v) 
	{
		if(alertDialog != null)
		{
			if (alertDialog.isShowing()) { // safe check
				alertDialog.dismiss();
				alertDialog = null;
			}
		}
		
	}
	
	public void onBuddyMessageClosed(View v)
	{
		closeBuddyDialog();
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
	
	public void onBuddyChangeAcknowledged(View v) 
	{
		if(buddyDialog != null)
		{
			if (buddyDialog.isShowing()) { // safe check
				isDisplayingBuddyDialog = false;
				buddyDialog.dismiss();
				buddyDialog = null;

				Intent i_settings = new Intent(
						Main2Activity.this,
						SettingsActivity.class);

				previousActivityIsMessagesActivity = false;
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

				Constants.internalReset(Main2Activity.this,prefs2);
				Constants.externalReset();
				
				startActivity(i_settings);
				
			}
		}
	}
	
	
	public void onPledgePopupOkButtonClicked(View v)
	{
		if(pledgeAlertDialog != null)
		{
			if(pledgeAlertDialog.isShowing())
			{
				pledgeAlertDialog.dismiss();
				pledgeAlertDialog = null;
			}
		}
	}

	public void displayConnectionErrorDialog(boolean show) {
		if (show) {
			displayConnectionErrorMessage();
		} else if (connectionDialog != null) {
			try
			{
				connectionDialog.dismiss();
				connectionDialog = null;
			}
			catch(Exception e)
			{
				
			}
		}
	}

	public void displayConnectionErrorMessage() 
	{
		showProgressDialog(false);
		if (!isDisplayingConnectionErrorDialog) // creationComplete &&
		{

			if (isActive) {
				
				showProgressDialog(false);
				isDisplayingConnectionErrorDialog = true;
				Toast.makeText(Main2Activity.this, getResources().getString(R.string.connection_toast_message), Toast.LENGTH_LONG).show();
				
				showBuddyName(AppContext.getInstance().getFriendId());
				
				
				
				if(pledgeAmount > 0)
				{
					disablePledge(0);
					//updatePledgeProgressBar(0);
					updatePledgeAfterSync(0);
				}
				else
				{
					enablePledge(0);
				}
				
				if(pledgeAmountFloor > 0)
				{
					disablePledge(1);
					//updatePledgeProgressBar(1);
					updatePledgeAfterSync(1);
				}
				else
				{
					enablePledge(1);
				}			
				
				
				initHistory();
				
				SharedPreferences prefs = getSharedPreferences(
						Constants.PROPERTIES_NAME, Context.MODE_PRIVATE);

			
				
				if(mySteps > 0 || buddySteps > 0)
				{
					checkStepBadges();
				}
				else
				{
					mySteps = prefs.getInt(
							Constants.PROP_KEY_USER_HISTORY_COMPLETED_STEPS + "_" + 0,
							0);
					buddySteps = prefs.getInt(
							Constants.PROP_KEY_BUDDY_HISTORY_COMPLETED_STEPS + "_" + 0,
							0);
					checkStepBadges();
				}
				
				if(myFloors > 0 || buddyFloors > 0)
				{
					checkFloorBadges();
				}	
				else
				{
					myFloors = prefs.getInt(
							Constants.PROP_KEY_USER_HISTORY_COMPLETED_FLOORS + "_" + 0,
							0);
					buddyFloors = prefs
							.getInt(Constants.PROP_KEY_BUDDY_HISTORY_COMPLETED_FLOORS
									+ "_" + 0, 0);						
					
					checkFloorBadges();
				}
			}
		}
	}

	public void displayGameStartMessage(final String message)
	{
		//Toast.makeText(Main2Activity.this, message, Toast.LENGTH_LONG).show();
		if (!isDisplayingBuddyDialog) 
		{
			Constants.INVITATION_OUTCOME_DISPLAYED = true;
			isDisplayingBuddyDialog = true;
			showProgressDialog(false);
			
			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View layout = inflater.inflate(R.layout.activity_buddyaccept_dialog,
					null);
			AlertDialog.Builder builder = new AlertDialog.Builder(this)
					.setView(layout)
					.setOnCancelListener(
					new DialogInterface.OnCancelListener() {

						@Override
						public void onCancel(DialogInterface dialog) {
							
							isDisplayingBuddyDialog = false;
							//moveTaskToBack(true);
							// checkProgressHandler.postDelayed(checkForProgressTask,
							// 0);
						}
					});
			
			buddyDialog = builder.create();
			
			TextView tv = (TextView) layout.findViewById(R.id.alertinfo2_bc);
			Button b  = (Button)layout.findViewById(R.id.okbtn_bc);
			b.setText(getResources().getString(R.string.lets_play));
			
			tv.setText(message);
			buddyDialogMessage = message;
			Constants.FIRST_WELCOME_GIVEN = true;
			if (isActive) {
				buddyDialog.show();
			}			
		}
	}
	
	public void displayBuddyChangeDialog(final String message) {

		if (!isDisplayingBuddyDialog) {
			isDisplayingBuddyDialog = true;
			showProgressDialog(false);
			Constants.INVITATION_OUTCOME_DISPLAYED = false;
			
			SharedPreferences prefs2 = getSharedPreferences(
					Constants.PROPERTIES_NAME, MODE_PRIVATE);
			
			Constants.internalReset(Main2Activity.this,prefs2);
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

	public void setupInvitationsListView(View layout) 
	{
		Log.d(TAG,"setupInvitationsListView()");
		final ListView lv = (ListView)layout.findViewById(R.id.listViewBuddyEmail);
		
		
		items = new ArrayAdapter<String>(Main2Activity.this, R.layout.my_single_checklist_item, invitationEmails); // android.R.layout.simple_list_item_single_choice
		/*if(items == null)
		{
			
		}
		else
		{
			items.clear();
			for(int i = 0; i < invitationEmails.size(); i++)
			{
				items.add(invitationEmails.get(i));
			}
		}*/
		
		if(lv == null)
		{
			Log.e(TAG,"lv is null");
		}
		else if(items == null)
		{
			Log.e(TAG,"items is null");
		}
		else
		{
			Log.e(TAG,"" + items.getCount());
		}
		
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
				inviterEmail = email; //625
			}
		});
		
		
		Log.d(TAG,"LL");
		
		/*LinearLayout LL = (LinearLayout)layout.findViewById(R.id.accept_form);
		LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		LL.setLayoutParams(p);*/
		LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		p.setMargins(0, 0, 0, dpToPx(10));
		//dpToPx(10+36*(items.getCount())));/
		lv.setLayoutParams(p);
		
		Log.d(TAG,"end of setupInvitationsListView()");
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
						Toast.makeText(Main2Activity.this,getResources().getString(R.string.select_an_inviation_first), Toast.LENGTH_LONG).show();						
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
										LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, dpToPx(10+36*(items.getCount())));//LinearLayout.LayoutParams.WRAP_CONTENT);
										lv.setLayoutParams(p);										
										inviterEmail = items.getItem(0);
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
										LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, dpToPx(10+36*(items.getCount())));//LinearLayout.LayoutParams.WRAP_CONTENT);
										lv.setLayoutParams(p);
										inviterEmail = items.getItem(0);										
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
									if(!Utils.isConnectionPresent(Main2Activity.this))
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
						Toast.makeText(Main2Activity.this,getResources().getString(R.string.select_an_inviation_first), Toast.LENGTH_LONG).show();						
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
			
			// 
			String dialogContent =  dialogTV.getText().toString() + "\n" + buddyCandidate + "?";
			
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
				protected void onPostExecute(String result) 
				{
					
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
					else if(!Utils.isConnectionPresent(Main2Activity.this))
					{
						displayConnectionErrorMessage();
					}
				}
			};

			dtTask.execute();
		}
	}

	// /**
	// * Schedule AlarmManager to invoke CheckForMessageAlarmReceiver and cancel
	// any existing current PendingIntent
	// * we do this because we *also* invoke the receiver from a BOOT_COMPLETED
	// receiver
	// * so that we make sure the service runs either when app is
	// installed/started, or when device boots
	// */
	// private void scheduleAlarmReceiver() {
	// Log.i(TAG, "Configuring AlarmManager");
	// AlarmManager alarmMgr = (AlarmManager)
	// this.getSystemService(Context.ALARM_SERVICE);
	// PendingIntent pendingIntent =
	// PendingIntent.getBroadcast(this, 0, new Intent(this,
	// CheckForMessageAlarmReceiver.class),
	// PendingIntent.FLAG_CANCEL_CURRENT);
	//
	// // Use inexact repeating which is easier on battery (system can phase
	// events and not wake at exact times)
	// alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
	// Constants.ALARM_TRIGGER_AT_TIME,
	// Constants.ALARM_INTERVAL, pendingIntent);
	// }

	private void showMultiplePendingBuddyInvitationNotification(ArrayList<String> emails) {
		
		SharedPreferences prefs = getSharedPreferences(
				Constants.PROPERTIES_NAME, MODE_PRIVATE);
		boolean allowNotification = Constants.checkNotificationAllowance(prefs);
		
		if(allowNotification)
		{

			Log.i(TAG,"emails size =  " + emails.size());
			//ArrayList<String> inviters = new ArrayList<String>();

			if(invitationEmails == null)
			{
				invitationEmails = new ArrayList<String>();
			}
			
			invitationEmails.clear();
			for(int i = 0; i < emails.size(); i++)
			{
				invitationEmails.add(emails.get(i));
			}
			
			try
			{
				displayNewInvitationDialog(emails);
			}
			catch(Exception e)
			{
				Log.e(TAG,e.toString());
				e.printStackTrace();
			}
			Constants.embarkNotificationDate(prefs);
			/*NotificationCompat.Builder builder = new NotificationCompat.Builder(
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

			
			ArrayList<String> inviters = new ArrayList<String>();
			
			inviters.add(buddyEmail);
			
			//displayNewInvitationDialog(inviters);
			try
			{
				displayNewInvitationDialog(inviters);
			}
			catch(Exception e)
			{
				Log.e(TAG,e.toString());
			}			
			Constants.embarkNotificationDate(prefs);
		
			
		/*NotificationCompat.Builder builder = new NotificationCompat.Builder(
				this)
				.setAutoCancel(true)
				// .setLargeIcon(BitmapFactory.decodeResource(getResources(),
				// R.drawable.notification_icon))
				.setSmallIcon(R.drawable.notification_icon)
				.setContentTitle(getResources().getString(R.string.lets_play))
				.setDefaults(Notification.DEFAULT_ALL)
				.setContentText(buddyEmail + " " + getResources().getString(R.string.someone_is_waiting));
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

}
