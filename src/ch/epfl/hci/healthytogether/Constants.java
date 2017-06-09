package ch.epfl.hci.healthytogether;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.epfl.hci.happytogether.R;
import ch.epfl.hci.healthytogether.communication.ServerHelper.CancelPledgesTask;
import ch.epfl.hci.healthytogether.communication.ServerHelper.SendTimelyLogTask;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.SystemClock;
import android.util.Log;

public class Constants {

	public static final double SELF_WEIGHT = 0.5;
	public static final double BUDDY_WEIGHT = 0.5;

	// public static final double SELF_WEIGHT = 0.8;
	// public static final double BUDDY_WEIGHT = 0.2;

	public static final String PROPERTIES_NAME = "ch.epfl.hci.healthytogether.prefs";
	public static final String PROP_KEY_AUTHORIZED = "fitbit_authorized";
	public static final String PROP_KEY_USER_ID = "user_id";
	public static final String PROP_KEY_EMAIL = "email";
	public static final String PROP_KEY_PASSWORD = "pwd";
	public static final String PROP_KEY_UNAME = "uname";
	public static final String PROP_KEY_CREDSET = "credset";
	// Store the steps used up for already completed games.
	public static final String PROP_KEY_USER_COMPLETED_STEPS = "user_completed_steps";
	public static final String PROP_KEY_BUDDY_COMPLETED_STEPS = "buddy_completed_steps";
	public static final String PROP_KEY_BADGE_LEVEL = "badge_level";
	public static final String PROP_KEY_USER_COMPLETED_FLOORS = "user_completed_floors";
	public static final String PROP_KEY_BUDDY_COMPLETED_FLOORS = "buddy_completed_floors";
	public static final String PROP_KEY_FLOOR_BADGE_LEVEL = "floor_badge_level";

	public static final String PROP_KEY_USER_HISTORY_COMPLETED_FLOORS = "user_completed_history_floors";
	public static final String PROP_KEY_BUDDY_HISTORY_COMPLETED_FLOORS = "buddy_completed_history_floors";
	public static final String PROP_KEY_USER_HISTORY_COMPLETED_STEPS = "user_completed_history_steps";
	public static final String PROP_KEY_BUDDY_HISTORY_COMPLETED_STEPS = "buddy_completed_history_steps";

	
	public static final String PROP_STEP_PLEDGE_CONGRATS_DATE = "step_pledge_congrats_date";
	public static final String PROP_FLOOR_PLEDGE_CONGRATS_DATE = "floor_pledge_congrats_date";
	
	public static final String PROP_LAST_INVITATION_NOTIFICATION_DATE = "previous_invitation_notif_date";
	
	public static final String PROP_PREV_DATE = "date_month_year";

	
	
	public static final String PROP_INVITATION_ACCEPTANCE_DATE = "acceptance_date";
	
	private static final String PROP_LOG_STACK_INDEX = "log_stack_index";
	private static final String PROP_STACKED_LOG = "prop_stacked_log";
	private static final String PROP_STACKED_DATE = "prop_stacked_date";
	
	private static boolean logLock = false;
	
	
	/**
	 * Indicates the user has accepted the invite and is now playing with a
	 * buddy
	 */
	public static final String PROP_KEY_GAME_STARTED = "invite_accepted";

	public static String PROPS_WARNING_DATE = "last_warning_date";

	
	
	// In real life, use AlarmManager.INTERVALs with longer periods of time
	// for dev you can shorten this to 10000 or such,
	public static final long ALARM_INTERVAL = 600000; // 60000; //  DEV ONLY
	public static final long LOG_ALARM_INTERVAL = 600000; //  DEV ONLY
	// public static final long ALARM_INTERVAL =
	// AlarmManager.INTERVAL_FIFTEEN_MINUTES;
	public static final long ALARM_TRIGGER_AT_TIME = SystemClock
			.elapsedRealtime() + 30000;

	public static final int NOTIFICATION_ID_PENDING_BUDDY_REQUEST = 1;
	public static final int NOTIFICATION_ID_BUDDY_REQUEST_ACCEPTED = 2;
	public static final int NOTIFICATION_ID_NEW_MESSAGE = 3;

	public static final int SUGGESTION_COUNT = 3;


	public static boolean MAIN_OR_COMMUNITY = false;
	public static boolean VIEW_STEPS = true;
	
	public static final int MAXIMUM_LEADERBOARD_ENTRY = 40;
	public static int CURRENT_STEPS_LEADERBOARD_ENTRY = 0;
	public static int CURRENT_FLOORS_LEADERBOARD_ENTRY = 0;
	
	/* These are our values to calculate user's badges. */
	public static int[] BADGE_LEVELS = new int[] {

	500, // 1
			1000, // 2
			2000, // 3
			4000, // 4
			6000, // 5
			8000, // 6
			10000, // 7
			20000, // 8
			20000 };

	public static int[] FLOOR_BADGE_LEVELS = new int[] { 2, // 1
			5, // 2
			10, // 3
			15, // 4
			20, // 5
			30, // 6
			40, // 7
			50, // 8
			50 };

	/*
	 * Badges/step progress is automatically checked every 600 seconds = 10
	 * minutes
	 */
	public static final int BADGE_CHECK_INTERVAL = 600000;

	/** Polling interval (30s) only used in the {@link MessagesActivity} */
	public static final int MESSAGE_POLLING_INTERVAL = 600000; // onur:
																// incremented
																// to 600s = 5
																// minutes

	public static int selectedMoodIndex = -1;

	public static AlertDialog dialog;
	public static AlertDialog exerciseDialog;

	public static String SELECTED_EMAIL = "";

	public static boolean COUNTER_INVITE = false;
	public static boolean EMAIL_NOT_FOUND = false;

	public static boolean loggedIn = false;
	public static boolean CONNECTION_ERROR;
	public static ArrayList<UserEntry> userEntries = new ArrayList<UserEntry>();
	public static boolean INVITATION_ACCEPTED = false;
	public static int PENDING_INVITATION_COUNT = 0;
	public static boolean BUDDY_DECLINED = false;
	public static boolean INVITATION_RECEIVED = false;
	protected static boolean INVITATION_CANCELED = false;
	
	//ASMA COMMUNITY VARIABLES START
	public static ArrayList<GroupEntry> allGroups=new ArrayList<GroupEntry>();
	public static ArrayList<GroupEntry> leaderboardGroups=new ArrayList<GroupEntry>();
	public static ArrayList<GroupEntry> leaderboardGroupsFloor=new ArrayList<GroupEntry>();
	public static int communityDatePivot=0;
	public static boolean iconMark[]=new boolean[15];
	public static Team team_data[]=new Team[] {
		new Team(0, "Loading...",""),
		new Team(0, "Loading...",""),
		new Team(0, "Loading...",""),
		new Team(0, "Loading...",""),
		new Team(0, "Loading...",""),
		new Team(0, "Loading...",""),
		new Team(0, "Loading...",""),
		new Team(0, "Loading...",""),
		new Team(0, "Loading...",""),
		new Team(0, "Loading...",""),
		new Team(0, "Loading...",""),
		new Team(0, "Loading...",""),
		new Team(0, "Loading...",""),
		new Team(0, "Loading...",""),
		new Team(0, "Loading...",""),
		new Team(0, "Loading...",""),
		new Team(0, "Loading...",""),
		new Team(0, "Loading...",""),
		new Team(0, "Loading...",""),
		new Team(0, "Loading...",""),
		new Team(0, "Loading...",""),
		new Team(0, "Loading...",""),
		new Team(0, "Loading...",""),
		new Team(0, "Loading...",""),
		new Team(0, "Loading...",""),
		new Team(0, "Loading...",""),
		new Team(0, "Loading...",""),
		new Team(0, "Loading...",""),
		new Team(0, "Loading...",""),
		new Team(0, "Loading...",""),
		new Team(0, "Loading...",""),
		new Team(0, "Loading...",""),
		new Team(0, "Loading...",""),
		new Team(0, "Loading...",""),
		new Team(0, "Loading...",""),
		new Team(0, "Loading...",""),
		new Team(0, "Loading...",""),
		new Team(0, "Loading...",""),
		new Team(0, "Loading...",""),
		new Team(0, "Loading...","")};
	
	public static Team team_data_floor[]=new Team[] {
		new Team(0, "Loading...",""),
		new Team(0, "Loading...",""),
		new Team(0, "Loading...",""),
		new Team(0, "Loading...",""),
		new Team(0, "Loading...",""),
		new Team(0, "Loading...",""),
		new Team(0, "Loading...",""),
		new Team(0, "Loading...",""),
		new Team(0, "Loading...",""),
		new Team(0, "Loading...",""),
		new Team(0, "Loading...",""),
		new Team(0, "Loading...",""),
		new Team(0, "Loading...",""),
		new Team(0, "Loading...",""),
		new Team(0, "Loading...",""),
		new Team(0, "Loading...",""),
		new Team(0, "Loading...",""),
		new Team(0, "Loading...",""),
		new Team(0, "Loading...",""),
		new Team(0, "Loading...",""),
		new Team(0, "Loading...",""),
		new Team(0, "Loading...",""),
		new Team(0, "Loading...",""),
		new Team(0, "Loading...",""),
		new Team(0, "Loading...",""),
		new Team(0, "Loading...",""),
		new Team(0, "Loading...",""),
		new Team(0, "Loading...",""),
		new Team(0, "Loading...",""),
		new Team(0, "Loading...",""),
		new Team(0, "Loading...",""),
		new Team(0, "Loading...",""),
		new Team(0, "Loading...",""),
		new Team(0, "Loading...",""),
		new Team(0, "Loading...",""),
		new Team(0, "Loading...",""),
		new Team(0, "Loading...",""),
		new Team(0, "Loading...",""),
		new Team(0, "Loading...",""),
		new Team(0, "Loading...","")};
	public static int winnerSteps;
	public static int winnerFloors;
	
	public static int userStepRank = 1;
	public static int userFloorRank = 1;
	
	public static int avatarList[]=new int[]{
		R.drawable.avatar0,
		R.drawable.avatar1,
		R.drawable.avatar2,
		R.drawable.avatar3,
		R.drawable.avatar4,
		R.drawable.avatar5,
		R.drawable.avatar6,
		R.drawable.avatar7,
		R.drawable.avatar8,
		R.drawable.avatar9,
		R.drawable.avatar10,
		R.drawable.avatar11,
		R.drawable.avatar12,
		R.drawable.avatar13,
		R.drawable.avatar14
	};
	public final static int AVATAR_NUM=15;
	public final static int getAvatarNum(int i){
		if(i < 0 )
			return avatarList[0];
		
		return avatarList[i];
	}
	//ASMA COMMUNITY VARIABLES END
	
	public static final int[] PLEDGE_AMOUNT = new int[]
	{
			2000,
			5000,
			10000,
			20000,
			40000,
			60000
	};
	
	public static final int[] PLEDGE_AMOUNT_FLOOR = new int[]
	{
			2,
			5,
			10,
			15,
			20,
			30
	};
	
	//ASMA PLEDGING VARIABLES END	
	
	
	public static void internalReset(Activity activity, SharedPreferences prefs)
	{
		Constants.COUNTER_INVITE = false;
		Constants.INVITATION_ACCEPTED = false;
		Constants.INVITATION_RECEIVED = false;
		//Constants.INVITATION_SENT = false;
		Constants.INVITATION_SENT = false;
		Constants.INVITATION_OUTCOME_DISPLAYED =false;
		
		
		Main2Activity.pledgeAmount = 0;
		Main2Activity.pledgeAmountFloor = 0;
		leaderboardGroups.clear();
		leaderboardGroups.clear();
		
		if(MainActivityCommunity.leaderboard != null)
			MainActivityCommunity.leaderboard.clear();
		
		if(MainActivityCommunity.leaderboardFloor != null)	
			MainActivityCommunity.leaderboardFloor.clear();
		
		MainActivityCommunity.leaderboard = null;
		MainActivityCommunity.leaderboardFloor = null;
		
		for(int i = 0; i < team_data.length; i++)
		{
			team_data[i] = new Team(0, "Loading...","");
		}
		
		for(int i = 0; i < team_data_floor.length; i++)
		{
			team_data_floor[i] = new Team(0, "Loading...","");
		}		
		
		CURRENT_FLOORS_LEADERBOARD_ENTRY = 0;
		CURRENT_STEPS_LEADERBOARD_ENTRY = 0;
		
		AppContext.getInstance().setFriendId(0);
		Editor editor = prefs.edit();
		editor.putBoolean(
				Constants.PROP_KEY_GAME_STARTED,
				false);
		editor.commit();
		
		NotificationManager notificationManager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager
				.cancel(Constants.NOTIFICATION_ID_PENDING_BUDDY_REQUEST);		
		
		clearNotificationDate(prefs);
		clearAcceptanceDate(prefs);
		clearStepPledgeCongratsDate(prefs);
		clearFloorPledgeCongratsDate(prefs);
		
	}
	
	public static void externalReset()
	{
		// i.e., cancel the pledges you had for today.
		CancelPledgesTask task = new CancelPledgesTask(AppContext.getInstance().getUserId()) {
			
			@Override
			protected void onPostExecute(Boolean success) {
				
			}
		};
		task.execute();
	}

	/**
	 * Cheer Message Template
	 */

	/*
	 * "According to the World Health Organization, " +
	 * "aerobic activities improve cardio-respiratory " +
	 * "and muscular fitness. Consider walking more " +
	 * "steps during the work break.",
	 * 
	 * "Aerobic activities improve cardio-respiratory " +
	 * "and muscular fitness. Do some walking exercises " + "during the break.",
	 * "80% of the users using this app walked " +
	 * "10,000 steps per day. We suggest the same for you."
	 */

	public static final String[] cheerTemplates = { "We can make it! Rise up!",
			"You are doing quite well. Let's finish this off!",
			"We can earn a badge real soon. Let's go for a walk!", };
	public static final String[] tauntTemplates = {
			"Haha, I'm walking more steps than you. Better catch up!",
			"Who's slow now? Try catching up!",
			"Come on! Is that all you got? You can do better!" };

	public static final String[] allActivities = new String[] {
			"Jogging", // "exercises"
			"Walking", "Swimming", "Lifting", "Cycling", "Skating", "Tennis",
			"Skiing", "Dancing", "TV", // Here we start "sedentary" activities,
										// starting at 9th index
			"Reading", "Working", "Meeting", "Rest", "Sitting", "Other" };

	public static final String[] exerciseActivities = new String[] { "Cycling",
			"Dancing",
			"Jogging", // "exercises"
			"Lifting", "Skating", "Skiing", "Swimming", "Tennis", "Walking",
			"Other" 
	};

	public static final String[] sedentaryActivities = new String[] {
			"Meeting", "Reading", "Rest", "Sitting", "TV", // Here we start
															// "sedentary"
															// activities,
															// starting at 9th
															// index
			"Working", "Other" };

	public static final String[] nutritionTemplates = new String[] {
			"Breakfast", "Lunch", "Dinner", "Snack" };

	public static final String[] socialTemplates = new String[] { "Alone",
			"Together" };

	public static final String[] moodTemplates = new String[] {
			"Happy", // Positive Moods
			"Surprised", "Amused", "Interested", "Relieved", "Joyful",
			"Tender", "Proud", "Stressed", // Negative Moods, starting at 8th
											// index
			"Shy", "Regret", "Sad", "Contempt", "Fear", "Disgusted", "Angry" };

	public static final String[] positiveMoods = new String[] { "Amused",
			"Happy", // Positive Moods
			"Interested", "Joyful", "Proud", "Relieved", "Surprised", "Tender" };

	public static final String[] negativeMoods = new String[] { "Angry",
			"Contempt", "Disgusted", "Fear", "Regret", "Sad", "Shy", "Stressed" // Negative
																				// Moods,
																				// starting
																				// at
																				// 8th
																				// index
	};

	public static final String[] moodIntensityTemplates = new String[] { "Mild", "Normal", "Strong" };

	/*
	 * 
	 * "Hopeful", "Interested", "Amused", "Proud", "Happy", "Joyful", "Tender",
	 * "Awesome", "Relieved", "Surprised", "Nostalgic", "Pitiful", "Sad",
	 * "Worried", "Embarrassed", "Guilty", "Disappointed", "Jealous",
	 * "Disgusted", "Scornful", "Angry"
	 */
	/*
	 * public static final int[] moodColors = new int[] { Color.rgb(125, 0,
	 * 160),// "Disgusted", Color.MAGENTA,//"Angry", Color.RED,//"Proud",
	 * Color.rgb(125, 125, 0),//"Joyful", Color.rgb(125, 160, 0),//"Hopeful",
	 * Color.GREEN,//"Interested", Color.CYAN,//"Sad", Color.rgb(0, 125, 125)};
	 * //"Ashamed"
	 */

	public static List<OneLog> logLists = new ArrayList<OneLog>();
	public static ArrayList<OneLog> modifiedItems = new ArrayList<OneLog>();
	public static ArrayList<Integer> modifiedItemIndices = new ArrayList<Integer>();
	public static boolean newLogin = false;

	public static final int[] notificationHours = new int[] { 10, 12, 15, 17,
			20 };
	public static final int[] notificationMinutes = new int[] { 0, 30, 0, 30, 0 };

	public static final int BADGE_COUNT = 8;
	private static final long NOTIFICATION_INTERVAL = 86400000; // currently, one day!
	public static final String PLEDGE_BAR_LENGTH = "pledge_bar_length";
	public static final String PROGRESS_BAR_LENGTH = "progress_bar_length";
	public static final String PROGRESS_BAR_HEIGHT = "progress_bar_height";
	
	
	public static boolean BUDDY_NAME_ACQUIRED = false;
	public static boolean INVITATION_SENT = false;
	public static String INVITEE = "";

	public static boolean[] validAlerts = new boolean[] { true, true, true,
			true, true, };

	public static boolean[] alertSetOff = new boolean[] { false, false, false,
			false, false, };

	public static boolean alertGiven = false;
	public static boolean AUTHORIZATION_VALIDATED = false;
	public static String buddyName = "";
	public static boolean INVITATION_OUTCOME_DISPLAYED = false;
	public static boolean FIRST_WELCOME_GIVEN = true;
	public static int LEADERBOARD_DP_OFFSET = 60;

	public static boolean validEmail(String email) 
	{
		if(email == null || email.isEmpty() || email.equals(""))
			return false;
		
		
		Pattern p = Pattern.compile(".+@.+\\.[a-z]+");
		Matcher m = p.matcher(email);
		boolean matchFound = m.matches();
		return matchFound;
	}

	public static void holdOffTodaysClosestAlarm(int hours, int minutes) {

		long distanceToAlarm;
		for (int i = 0; i < Constants.notificationHours.length; i++) {
			distanceToAlarm = 3600000
					* (hours - Constants.notificationHours[i]) + 60000
					* (minutes - Constants.notificationMinutes[i]);

			if (distanceToAlarm < 0 && validAlerts[i]
					&& Math.abs(distanceToAlarm) < 3600000) // The current time
															// is earlier than
															// this notification
			{
				validAlerts[i] = false;
				break;
			}
		}

	}

	// public static final String PROP_INVITATION_ACCEPTANCE_DATE =
	// "acceptance_date";

	public static void embarkAcceptanceDate(SharedPreferences prefs) {
		SimpleDateFormat datef = new SimpleDateFormat("yyyy-MM-dd");
		Date d = new Date();

		String dateString = datef.format(d);
		Editor editor = prefs.edit();
		editor.putString(Constants.PROP_INVITATION_ACCEPTANCE_DATE, dateString);
		editor.commit();
	}

	public static void clearAcceptanceDate(SharedPreferences prefs) {
		Editor editor = prefs.edit();
		editor.remove(Constants.PROP_INVITATION_ACCEPTANCE_DATE);
		editor.commit();
	}

	// SharedPreferences prefs = getSharedPreferences(Constants.PROPERTIES_NAME,
	// Context.MODE_PRIVATE);
	public static Date readAcceptanceDate(final SharedPreferences prefs) {
		String startDate = prefs.getString(
				Constants.PROP_INVITATION_ACCEPTANCE_DATE, null);

		return convertToDate(startDate);
	}
	
	public static void embarkStepPledgeCongratsDate(SharedPreferences prefs) 
	{
		SimpleDateFormat datef = new SimpleDateFormat("yyyy-MM-dd");
		Date d = new Date();

		String dateString = datef.format(d);
		Editor editor = prefs.edit();
		editor.putString(Constants.PROP_STEP_PLEDGE_CONGRATS_DATE, dateString);
		editor.commit();
	}
	
	// taunt_friend
	
	public static void clearStepPledgeCongratsDate(SharedPreferences prefs)
	{
		//SimpleDateFormat datef = new SimpleDateFormat("yyyy-MM-dd");
		//Date d = new Date();

		//String dateString = null;//datef.format(d);
		Editor editor = prefs.edit();
		editor.remove(Constants.PROP_STEP_PLEDGE_CONGRATS_DATE);
		//editor.putString(Constants.PROP_LAST_INVITATION_NOTIFICATION_DATE, dateString);
		editor.commit();
	}
	
	public static Date readStepPledgeCongratsDate(final SharedPreferences prefs)
	{
		String startDate = prefs.getString(
				Constants.PROP_STEP_PLEDGE_CONGRATS_DATE, null);

		return convertToDate(startDate);
	}
	
	public static void embarkFloorPledgeCongratsDate(SharedPreferences prefs) 
	{
		SimpleDateFormat datef = new SimpleDateFormat("yyyy-MM-dd");
		Date d = new Date();

		String dateString = datef.format(d);
		Editor editor = prefs.edit();
		editor.putString(Constants.PROP_FLOOR_PLEDGE_CONGRATS_DATE, dateString);
		editor.commit();
	}
	
	// taunt_friend
	
	public static void clearFloorPledgeCongratsDate(SharedPreferences prefs)
	{
		//SimpleDateFormat datef = new SimpleDateFormat("yyyy-MM-dd");
		//Date d = new Date();

		//String dateString = null;//datef.format(d);
		Editor editor = prefs.edit();
		editor.remove(Constants.PROP_FLOOR_PLEDGE_CONGRATS_DATE);
		//editor.putString(Constants.PROP_LAST_INVITATION_NOTIFICATION_DATE, dateString);
		editor.commit();
	}
	
	public static Date readFloorPledgeCongratsDate(final SharedPreferences prefs)
	{
		String startDate = prefs.getString(
				Constants.PROP_FLOOR_PLEDGE_CONGRATS_DATE, null);

		return convertToDate(startDate);
	}	
	
	

	public static Date convertToDate(String startDate) {
		if (startDate != null) {
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			try {
				Date d = df.parse(startDate);
				return d;
			} catch (ParseException e) {
				return null;
			}
		} else {
			return null;
		}
	}

	public static boolean warningNotSentToday(SharedPreferences prefs) {
		Date d = new Date(); // today.

		String warningDateStr = prefs.getString(Constants.PROPS_WARNING_DATE,
				null);

		Date warningDate = convertToDate(warningDateStr);

		
		if (warningDate == null) {
			return true;
		}
		int compareOutcome = d.compareTo(warningDate);

		return compareOutcome > 0; // the warning is after date

	}

	public static void embarkNotificationDate(SharedPreferences prefs) 
	{
		SimpleDateFormat datef = new SimpleDateFormat("yyyy-MM-dd");
		Date d = new Date();

		String dateString = datef.format(d);
		Editor editor = prefs.edit();
		editor.putString(Constants.PROP_LAST_INVITATION_NOTIFICATION_DATE, dateString);
		editor.commit();
	}
	
	// taunt_friend
	
	public static void clearNotificationDate(SharedPreferences prefs)
	{
		//SimpleDateFormat datef = new SimpleDateFormat("yyyy-MM-dd");
		//Date d = new Date();

		//String dateString = null;//datef.format(d);
		Editor editor = prefs.edit();
		editor.remove(Constants.PROP_LAST_INVITATION_NOTIFICATION_DATE);
		//editor.putString(Constants.PROP_LAST_INVITATION_NOTIFICATION_DATE, dateString);
		editor.commit();
	}
	
	
	public static Date readPreviousNotificationDate(final SharedPreferences prefs) {
		String startDate = prefs.getString(
				Constants.PROP_LAST_INVITATION_NOTIFICATION_DATE, null);

		return convertToDate(startDate);
	}
	
	
	public static boolean checkNotificationAllowance(SharedPreferences prefs)
	{
		Date previousNotification = readPreviousNotificationDate(prefs);
		
		if(previousNotification == null)
		{
			return true;
		}
		else
		{
			Date now = new Date();
			long nowMillis = now.getTime();
			long previousMillis = previousNotification.getTime();
			
			return (nowMillis - previousMillis > Constants.NOTIFICATION_INTERVAL);
			
			//Days.daysbetween(new DateTime(previousNotification), new DateTime(now));//now.compareTo(previousNotification);
		}
		
	}
	
	
	public static boolean validDate(SharedPreferences prefs, Date requestedDate) {
		Date acceptance = readAcceptanceDate(prefs);

		if (requestedDate == null) {
			Log.e("DATE_CHECK", "The requestedDate is received as null!!");
			return false;
		}

		if (acceptance != null) {
			int compareOutcome = requestedDate.compareTo(acceptance);

			return compareOutcome >= 0; // after or the same date as the
										// acceptance date
		} else {
			return true;
		}
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

	public static void storeLogForRetry(SharedPreferences prefs, String sqlText, String wholeString) 
	{
		int latestItemOnStack = prefs.getInt(Constants.PROP_LOG_STACK_INDEX, -1);
		latestItemOnStack = latestItemOnStack + 1;
		Editor editor = prefs.edit();
		editor.putString(Constants.PROP_STACKED_LOG  + "_" + latestItemOnStack, sqlText);
		editor.putString(Constants.PROP_STACKED_DATE + "_" + latestItemOnStack, wholeString);
		editor.putInt(Constants.PROP_LOG_STACK_INDEX, latestItemOnStack);
		editor.commit();
	}

	public static void sendLatestLog(final SharedPreferences prefs)
	{
		
		if (logLock) {
			return;
		}
		logLock = true;		
		
		final int latestItemOnStack = prefs.getInt(Constants.PROP_LOG_STACK_INDEX, -1);
		
		if(latestItemOnStack > -1)
		{
			String sqlText = prefs.getString(Constants.PROP_STACKED_LOG  + "_" + latestItemOnStack,null);
			String wholeString = prefs.getString(Constants.PROP_STACKED_DATE  + "_" + latestItemOnStack,null);
			
			if(sqlText != null && wholeString != null)
			{
				SendTimelyLogTask t = new SendTimelyLogTask(AppContext.getInstance().getUserId(),AppContext.getInstance().getUserId(),
															sqlText,wholeString) {
					
					@Override
					protected void onPostExecute(Boolean success) 
					{
						if(!this.safeExecution)
						{
							// do nothing!

						}						
						else if(success)
						{
							int index = latestItemOnStack;
							Editor editor = prefs.edit();
							editor.remove(Constants.PROP_STACKED_LOG  + "_" + index);
							editor.remove(Constants.PROP_STACKED_DATE  + "_" + index);
							index --;
							editor.putInt(Constants.PROP_LOG_STACK_INDEX, index);
							editor.commit();
						}
						else
						{
							// do nothing.
						}
						
						logLock = false;
						
					}
				};
				t.execute();
			}
			else
			{
				Log.e("sendLatestLog", "Trying to send a non-existant log: " + latestItemOnStack);
				logLock = false;
			}			
		}
		else
		{
			logLock = false;
		}
	}
	
	


}
