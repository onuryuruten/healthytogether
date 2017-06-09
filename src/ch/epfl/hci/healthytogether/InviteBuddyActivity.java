	
package ch.epfl.hci.healthytogether;

import java.util.ArrayList;
import java.util.Random;

import ch.epfl.hci.happytogether.R;
import ch.epfl.hci.healthytogether.communication.ServerHelper.AcceptBuddyRequestTask;
import ch.epfl.hci.healthytogether.communication.ServerHelper.CheckGroupTask;
import ch.epfl.hci.healthytogether.communication.ServerHelper.GetListOfUsersForInvitationTask;
import ch.epfl.hci.healthytogether.communication.ServerHelper.InviteBuddyTask;
import ch.epfl.hci.healthytogether.communication.ServerHelper.RetrieveBuddyEMailTask;
import ch.epfl.hci.healthytogether.communication.ServerHelper.RetrieveBuddyEMailTask2;
import ch.epfl.hci.healthytogether.util.Utils;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.NavUtils;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.graphics.PorterDuff.Mode;

public class InviteBuddyActivity extends Activity {
	String buddyName;
	String inviteeAddress;
	private static final String TAG = InviteBuddyActivity.class.toString();
	private Handler checkProgressHandler;
	ProgressDialog dialog;
	ArrayList<String> peopleEmails = new ArrayList<String>();
	ArrayList<String> peopleNames = new ArrayList<String>();
	ArrayList<Integer> peopleAvailabilities = new ArrayList<Integer>();
	public static boolean isSuggesting = true;
	public static boolean suggestionsAvailable = false;
	private static boolean suggestionSelected = false;
	private static String selectedEmail = "";
	
	public InviteBuddyActivity() {
		super();
	}

	private Runnable checkForProgressTask = new Runnable() {

		@Override
		public void run() {
			
			
			Log.i(TAG, "Started the thread.");
			
			int userId = AppContext.getInstance().getUserId();
			Constants.checkUserName();
			String email = AppContext.getInstance().getEmail();
			
			CheckGroupTask checkGroupTask = new CheckGroupTask(email, userId) {
				@Override
				protected void onPostExecute(String result) {
					Log.d(TAG, "response: " + result);
					setSuggestionButtonDrawable();
					if (Utils.isInteger(result)) {
						int resultCode = Integer.parseInt(result);
						Intent i;
						switch (resultCode) {
						case RESPONSE_CODE_NO_BUDDY:
							// show screen to invite buddy
							break;
						case RESPONSE_INCOMING_REQUEST_PENDING:
							//handleIncomingBuddyRequest(); 
							break;
						case RESPONSE_WAITING_FOR_ACCEPT:
							// show screen to remind buddy that he has a pending
							// invitation
							Log.i(TAG, "RESPONSE_WAITING_FOR_ACCEPT: Previously, I would try to go to PendingBuddyRequest");
							/*i = new Intent(InviteBuddyActivity.this,
									PendingBuddyRequestActivity.class);
							Log.i(TAG, "Switching to PendingBuddyRequest: checkForProgressTask - checkGroupTask.");
							Log.i(TAG, "checkGroupTask returned " + resultCode);
							startActivity(i);*/
							break;
						default: // the buddy name is already set!
							Log.i(TAG, "Switching to main: checkForProgressTask - checkGroupTask.");
							Log.i(TAG, "checkGroupTask returned " + resultCode);
							/*i = new Intent(InviteBuddyActivity.this,
									Main2Activity.class);
							startActivity(i);*/
							break;
						}
					} else {
						// error log_tag
						Log.e(TAG,
								"checkgrouptask @InviteBuddyActivity: an error has occurred");
					}
				}
			};
			checkGroupTask.execute();

			checkProgressHandler.postDelayed(this, Constants.ALARM_INTERVAL);
		}

	};
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.activity_invite_buddy);

		Log.e(TAG,"Started InviteBuddyActivity");
		Constants.INVITATION_SENT = false;
		Constants.INVITATION_OUTCOME_DISPLAYED = false;
		SharedPreferences prefs = getSharedPreferences(
				Constants.PROPERTIES_NAME, MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.putBoolean(Constants.PROP_KEY_GAME_STARTED, false);
		editor.commit();

		peopleEmails = new ArrayList<String>();
		peopleNames = new ArrayList<String>();
		peopleAvailabilities = new ArrayList<Integer>();
		
		Constants.clearAcceptanceDate(prefs);
		Intent intent = getIntent();
		String message;
		
		if(intent != null)
		{
			buddyName = intent.getStringExtra("oldBuddyName");
			message = intent.getStringExtra("message");
		}
		else
		{
			buddyName = "";
			message = null;//getIntent().getStringExtra("message");
		}
		
		
		
		TextView tv = (TextView) findViewById(R.id.inviteTextBody);
	
		
		if (message != null) {
			// CharSequence oldMsg = tv.getText();
			tv.setText(message + getResources().getString(R.string.who_do_you_want));
		} else {
			tv.setText(getResources().getString(R.string.invite_intro));
		}

		//inviteeAddress = getIntent().getStringExtra("selectedInviteeAddress");

		// Show the Up button in the action bar.
		forceToggle(true);
		showProgress(true);
		
		GetListOfUsersForInvitationTask glut = new GetListOfUsersForInvitationTask() {

			@Override
			protected void onPostExecute(Boolean result) {
				// Toast.makeText(InviteBuddyActivity.this,
				// "This is nicely working", Toast.LENGTH_LONG).show();

				Log.i(TAG, "glut finished. User Entries size = "
						+ Constants.userEntries.size());

				peopleEmails.clear();
				peopleNames.clear();
				peopleAvailabilities.clear();
				int lonelyCount = 0, pendingCount = 0, receivedCount = 0, playerCount = 0;
				int a = 0,b = 0,c = 0;
				// adapter = new
				// ArrayAdapter<String>(InviteBuddyActivity.this,
				// android.R.layout.simple_list_item_1);
				for (int i = 0; i < Constants.userEntries.size(); i++) 
				{
					UserEntry ue = Constants.userEntries.get(i);
					
					if(ue == null || ue.userName == null || ue.userEmail == null)
					{
						continue;
					}
					
					if(!ue.userName.equals(buddyName) && !ue.userEmail.equals(buddyName))
					{
						peopleEmails.add(ue.userEmail);
						peopleNames.add(ue.userName);
						peopleAvailabilities.add(ue.getAvailability());
						
						if(ue.getAvailability() == 0)
						{
							lonelyCount++;
						}
						else if(ue.getAvailability() == -1)
						{
							pendingCount++;
						}
						else if(ue.getAvailability() == -2)
						{
							receivedCount++;
						}
						else
						{
							playerCount++;
						}
						
						//Log.i(TAG, "We have got the params: n(lonely,pending,received,playing) = (" + lonelyCount + "," + pendingCount + "," + receivedCount + "," + playerCount + ")");
						
						a = 0;//getRandomPerson(lonelyCount, pendingCount, receivedCount, playerCount,-1,-1);
						b = 0;//getRandomPerson(lonelyCount, pendingCount, receivedCount, playerCount,a,-1);
						c = 0;//getRandomPerson(lonelyCount, pendingCount, receivedCount, playerCount,a,b);
					}
					// adapter.add(ue.userEmail);
				}

				
				prepareSuggestions(lonelyCount, pendingCount);
				
				if(a > -1 && b > -1 && c > -1)
				{
					//provideSuggestions(a,b,c,peopleNames, peopleEmails);
				}					
				
				final AutoCompleteTextView friendEmailET = (AutoCompleteTextView) findViewById(R.id.friendEmail);
				friendEmailET.setText("");
				// Get a reference to the AutoCompleteTextView in the layout
				// AutoCompleteTextView textView = (AutoCompleteTextView)
				// findViewById(R.id.autocomplete_country);
				// Get the string array
				String[] emails = new String[peopleEmails.size()];// getResources().getStringArray(R.array.countries_array);
				for (int i = 0; i < peopleEmails.size(); i++) {
					emails[i] = peopleEmails.get(i);
				}
				// Create the adapter and set it to the AutoCompleteTextView
				ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(
						InviteBuddyActivity.this,
						android.R.layout.simple_list_item_1, emails);
				friendEmailET.setAdapter(adapter2);

				friendEmailET.setAdapter(adapter2);
				friendEmailET.addTextChangedListener(new TextWatcher() {
					
					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {
						// TODO Auto-generated method stub
						onAutoCompleteTVClicked(null);
					}
					
					@Override
					public void beforeTextChanged(CharSequence s, int start, int count,
							int after) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void afterTextChanged(Editable s) {
						// TODO Auto-generated method stub
						
					}
				});
				friendEmailET.invalidate();

				showProgress(false);
				setSuggestionButtonDrawable();
			}
		};

		glut.execute();

		Constants.INVITATION_SENT = false;
		
		Log.i(TAG, "starting the checkprogress handler");
		

		checkProgressHandler = new Handler();
		checkProgressHandler.postDelayed(checkForProgressTask, 0); // we
																	// immediately
																	// check for
																	// new
																	// invitation
																	// state

	}

	
	public void setSuggestionButtonDrawable()
	{
		/*
		LinearLayout rl = (LinearLayout)findViewById(R.id.suggestion_form);
		ImageButton ib = (ImageButton)findViewById(R.id.editInvitationIcon);
		
		LinearLayout rl2 = (LinearLayout)findViewById(R.id.specify_buddy_form);
		ImageButton ib2 = (ImageButton)findViewById(R.id.editInvitationIcon2);
		if(!isSuggesting)
		{
			// 	android.graphics.PorterDuff.Mode
			Drawable details = getResources().getDrawable(android.R.drawable.ic_menu_info_details); 
			details.setColorFilter(getResources().getColor(R.color.dark_blue), Mode.SRC_IN);//new PorterDuffColorFilter(0xffff00,PorterDuff.Mode.MULTIPLY));
			
			ib.setImageDrawable(details);//getResources().getDrawable(android.R.drawable.ic_menu_info_details));
			ib2.setImageDrawable(details);//;getResources().getDrawable(android.R.drawable.ic_menu_info_details));
			rl2.setVisibility(LinearLayout.VISIBLE);
			rl.setVisibility(LinearLayout.GONE);			
		}
		else
		{
			Drawable edit = getResources().getDrawable(android.R.drawable.ic_menu_edit); 
			edit.setColorFilter(getResources().getColor(R.color.dark_blue), Mode.SRC_IN);//new PorterDuffColorFilter(0xffff00,PorterDuff.Mode.MULTIPLY));
			
			ib.setImageDrawable(edit);//getResources().getDrawable(android.R.drawable.ic_menu_edit));
			ib2.setImageDrawable(edit);//getResources().getDrawable(android.R.drawable.ic_menu_edit));
			rl2.setVisibility(LinearLayout.GONE);
			rl.setVisibility(LinearLayout.VISIBLE);
		}*/
	}
	
	public void forceToggle(boolean suggestionMode) // TODO: see if this is useful.
	{
//		isSuggesting = suggestionMode;
//		setSuggestionButtonDrawable();
		Log.i(TAG, "Suggestion mode = " + isSuggesting);
	}
	
	public void onToggleSuggestionsListClicked(View v)
	{
//		isSuggesting = !isSuggesting;
//		setSuggestionButtonDrawable();
		Log.i(TAG, "Suggestion mode = " + isSuggesting);
	}
	
	
	protected void provideSuggestions(int a, int b, int c,
			ArrayList<String> peopleNames2, ArrayList<String> peopleEmails2) 
	{
		// TODO Auto-generated method stub: suggest people at indices a, b and c.	
	}

	protected int getRandomValue(int boundary, int offset,ArrayList<Integer> existingIndices)
	{
		Random rand = new Random();
		
		int out = rand.nextInt(boundary) + offset; 
		
		if(existingIndices.contains(out))
		{
			while(existingIndices.contains(out))
			{
				out = rand.nextInt(boundary) + offset;
				Log.e(TAG, "Generating another random number for recommended user index.");
			}
		}
		
		return out;
	}
	
	protected int getRandomPerson(int lonelyCount, int pendingCount,
			int receivedCount, int playerCount, int a, int b) 
	{
		Random rand = new Random();
		
		int output = -1;
		int range = 0;
		
		
		
		if(lonelyCount > 0)
		{
			range += lonelyCount;
		}
		
		if(pendingCount > 0 )
		{
			range += pendingCount;
		}
		
		if(range == 0 && receivedCount > 0)
		{
			range += receivedCount;
		}
		
		if(range == 0 && playerCount > 0)
		{
			range += playerCount;
		}
		
		if(range == 0)
		{
			Log.e(TAG,"ERROR: all the counts are returned as empty");
			return -1;
		}
			
		output = rand.nextInt(range);
		
		if(a != -1 && b == -1)
		{
			while(output == a)
			{
				output = rand.nextInt(range);
			}
		}
		else if(a != -1 && b != -1)
		{
			while(output == a || output == b)
			{
				output = rand.nextInt(range);
			}
		}
		
		
		return output;
	}

	/*private void showProgressDialog(final boolean show) {
		if (show) {
			dialog = ProgressDialog.show(this, getResources().getString(R.string.invite_list_progress_title),
					getResources().getString(R.string.invite_list_progress_content));
		} else if (dialog != null) {
			dialog.dismiss();
			dialog = null;
		}
	}*/

	/**
	 * Shows the progress UI and hides the invite buddy screen
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		final View mLoginFormView = findViewById(R.id.invite_form);
		final View mLoginStatusView = findViewById(R.id.list_possible_invitees_status);

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

	protected void onPause() {
		super.onPause();
		// need to stop the message poling loop
		checkProgressHandler.removeCallbacks(checkForProgressTask);
	}

	protected void onResume() {
		super.onResume();
		Log.e(TAG,"InviteBuddy - resume");
		TextView tv = (TextView) findViewById(R.id.inviteTextBody);
		
		String message = getIntent().getStringExtra("message");
		if (message != null) {
			tv.setText(message);
		}
		forceToggle(true);
		//AutoCompleteTextView tv2 = (AutoCompleteTextView) findViewById(R.id.friendEmail);
		//tv2.setText("");
		GetListOfUsersForInvitationTask glut = new GetListOfUsersForInvitationTask() {

			@Override
			protected void onPostExecute(Boolean result) 
			{
				// Toast.makeText(InviteBuddyActivity.this,
				// "This is nicely working", Toast.LENGTH_LONG).show();

				Log.i(TAG, "glut finished. User Entries size = "
						+ Constants.userEntries.size());

				peopleEmails.clear();
				peopleNames.clear();
				peopleAvailabilities.clear();
				int lonelyCount = 0, pendingCount = 0, receivedCount = 0, playerCount = 0;
				int a = 0,b = 0,c = 0;
				// adapter = new
				// ArrayAdapter<String>(InviteBuddyActivity.this,
				// android.R.layout.simple_list_item_1);
				for (int i = 0; i < Constants.userEntries.size(); i++) 
				{
					UserEntry ue = Constants.userEntries.get(i);
					
					if(ue == null || ue.userName == null || ue.userEmail == null)
					{
						continue;
					}
					
					if(!ue.userName.equals(buddyName) && !ue.userEmail.equals(buddyName)
							&& !peopleEmails.contains(ue.userEmail)							)
					{
						peopleEmails.add(ue.userEmail);
						peopleNames.add(ue.userName);
						peopleAvailabilities.add(ue.getAvailability());
						
						
						if(ue.getAvailability() == 0)
						{
							lonelyCount++;
						}
						else if(ue.getAvailability() == -1)
						{
							pendingCount++;
						}
						else if(ue.getAvailability() == -2)
						{
							receivedCount++;
						}
						else
						{
							playerCount++;
						}
						
						//if(ue.getAvailability() == 0)
						//{
						//	Log.i(TAG, "Added this entry: (" + ue.getAvailability() +  ")"+ ue.userEmail);
						//}
					}
					// adapter.add(ue.userEmail);
				}

				prepareSuggestions(lonelyCount, pendingCount);
				
				if(a > -1 && b > -1 && c > -1)
				{
					//provideSuggestions(a,b,c,peopleNames, peopleEmails);
				}					
				
				final AutoCompleteTextView friendEmailET = (AutoCompleteTextView) findViewById(R.id.friendEmail);
				

				// Get a reference to the AutoCompleteTextView in the layout
				// AutoCompleteTextView textView = (AutoCompleteTextView)
				// findViewById(R.id.autocomplete_country);
				// Get the string array
				String[] emails = new String[peopleEmails.size()];// getResources().getStringArray(R.array.countries_array);
				for (int i = 0; i < peopleEmails.size(); i++) {
					emails[i] = peopleEmails.get(i);
				}
				// Create the adapter and set it to the AutoCompleteTextView
				ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(
						InviteBuddyActivity.this,
						android.R.layout.simple_list_item_1, emails);
				friendEmailET.setAdapter(adapter2);

				friendEmailET.setAdapter(adapter2);
				
				friendEmailET.invalidate();
				friendEmailET.setError(null);
				
				//if(!suggestionSelected)
			//	{
			//		friendEmailET.setText("");
			//		Toast.makeText(InviteBuddyActivity.this,"clear out",Toast.LENGTH_LONG).show();
			//	}
			//	else
		//		{
					friendEmailET.setText(selectedEmail);
		//			Toast.makeText(InviteBuddyActivity.this,"selected email = " + selectedEmail,Toast.LENGTH_LONG).show();
		//		}

				showProgress(false);
			}
		};

		glut.execute();		
		
		checkProgressHandler.postDelayed(checkForProgressTask, 0); // we
																	// immediately
																	// check for
																	// new
																	// invitation
																	// state

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent i_settings = new Intent(InviteBuddyActivity.this,SettingsActivity.class);
			Log.i(TAG, "switching to SettingsActivity: onKeyDown");
			startActivity(i_settings);			
			//moveTaskToBack(true);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_invite_buddy, menu);

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
		case R.id.menu_settings:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			Intent i_settings = new Intent(this, SettingsActivity.class);
			// Intent i_auth= new Intent(this,
			// AuthenticateFitbitActivity.class);
			Log.i(TAG, "switching to settings - on options item selected");
			startActivity(i_settings);

			// NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void onCancelNewInvitationButtonClicked(View v) 
	{
		
		Log.i(TAG, "Switching to main: onCancelNewInvitationButtonClicked");
		final AutoCompleteTextView friendEmail = (AutoCompleteTextView) findViewById(R.id.friendEmail);
		friendEmail.setText("");
		Intent i_cancel_invite = new Intent(InviteBuddyActivity.this, SettingsActivity.class);
		startActivity(i_cancel_invite);
	}

	public void onInviteButtonClicked(View v) {
		//EditText message = (EditText) findViewById(R.id.messageinput);
		final AutoCompleteTextView friendEmail = (AutoCompleteTextView) findViewById(R.id.friendEmail);
		final ListView slv = (ListView)findViewById(R.id.suggestions_listview);
		//int ownUserId = AppContext.getInstance().getUserId();
		
		//if(suggestion)
		
		String email = friendEmail.getText().toString().toLowerCase();
		
		if(email == null || email.isEmpty() || email.equals(""))
		{
			if(slv.getCheckedItemPosition() > 0)
				email = (String) slv.getAdapter().getItem(slv.getCheckedItemPosition());
		}
		
		if(email == null || email.isEmpty() || email.equals(""))
		{
			//Toast.makeText(InviteBuddyActivity.this, getResources().getString(R.string.error_incorrect_email), Toast.LENGTH_LONG).show();
			friendEmail.setError(getResources().getString(R.string.error_incorrect_email));
			friendEmail.requestFocus();
			return;
		}
			
		
		
		friendEmail.setError(null);

		if (Constants.validEmail(email)) {

			if (email.equals(AppContext.getInstance().getEmail())) {
				friendEmail.setError(getResources().getString(R.string.error_own_email));
				friendEmail.requestFocus();
			} else {

				Constants.EMAIL_NOT_FOUND = false;

				//displayBuddyConfirmDialog(email);
				onConfirmInvitationClicked(email);
				
			}
		} else {
			friendEmail.setError(getResources().getString(R.string.invalid_email));
			friendEmail.requestFocus();
			// Toast.makeText(this, "Please enter a valid email address",
			// Toast.LENGTH_LONG).show();
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
					}
				});
				
				
				final AlertDialog dialog = builder.create();
				
				
				Button okBtn= (Button) layout.findViewById(R.id.okbtn);
				Button cancelBtn = (Button) layout.findViewById(R.id.cancelbtn);
				
				
				okBtn.setOnClickListener(new View.OnClickListener() {
					
					public void onClick(View v) {
						onConfirmInvitationClicked(buddyCandidate);
						dialog.cancel();
					}
				});
				
				
				cancelBtn.setOnClickListener(new View.OnClickListener() {
					
					public void onClick(View v) {
						dialog.cancel();
					}
				});			
				
				TextView dialogTV = (TextView) layout.findViewById(R.id.alertinfo3);
				
				// 
				String dialogContent =  dialogTV.getText().toString() + "\n" + buddyCandidate + "?";
				
				dialogTV.setText(dialogContent);
				
				dialog.show();

		
	}

	protected void onConfirmInvitationClicked(String buddyCandidate) 
	{
		EditText message = (EditText) findViewById(R.id.messageinput);
		final AutoCompleteTextView friendEmail = (AutoCompleteTextView) findViewById(R.id.friendEmail);
		friendEmail.setText(buddyCandidate);
		int ownUserId = AppContext.getInstance().getUserId();
		final String email = friendEmail.getText().toString().toLowerCase();
		final InviteBuddyTask task = new InviteBuddyTask(email,
				ownUserId, message.getText().toString()) {

			@Override
			protected void onPostExecute(Boolean success) {
				if (success) {
					friendEmail.setText("");
					Constants.INVITATION_SENT = true;
					Constants.INVITEE = email;
					Constants.INVITATION_OUTCOME_DISPLAYED = false;
					final SharedPreferences prefs = getSharedPreferences(
							Constants.PROPERTIES_NAME, MODE_PRIVATE);
					// Go to the pending buddy activity where the user
					// can remind the other player
					if (!Constants.COUNTER_INVITE) {
						Editor editor = prefs.edit();
						editor.remove(Constants.PROPS_WARNING_DATE);
						editor.commit();
						Intent i = new Intent(InviteBuddyActivity.this,
								SettingsActivity.class); //PendingBuddyRequestActivity
						i.putExtra("message",getResources().getString(R.string.your_request_is_sent) + " \n" + email);
						i.putExtra("removalSeen",0);
						i.putExtra("buddyEmail", email);
						startActivity(i);
					} else // The user has already been invited by the
							// invitee.
					{

						AcceptBuddyRequestTask task = new AcceptBuddyRequestTask(
								AppContext.getInstance().getUserId(),
								AppContext.getInstance().getEmail(),
								email) {

							@Override
							protected void onPostExecute(String result) {
								if (Utils.isInteger(result)) {
									// The request was successful and
									// the server returned the friend's
									// ID
									AppContext
											.getInstance()
											.setFriendId(
													Integer.parseInt(result));
									Utils.scheduleAlarmReceiver(InviteBuddyActivity.this);

									// Remove the notification (in case
									// a new one has been issued)
									NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
									notificationManager
											.cancel(Constants.NOTIFICATION_ID_PENDING_BUDDY_REQUEST);

									// Set property to indicate invite
									// has been accepted and game
									// started

									Editor editor = prefs.edit();
									editor.putBoolean(
											Constants.PROP_KEY_GAME_STARTED,
											true);
									editor.remove(Constants.PROPS_WARNING_DATE);
									editor.commit();
									Constants
											.embarkAcceptanceDate(prefs); // i.e.,
																			// NOW
									// Go to main screen
									Log.e(TAG, "Invite Buddy going to the AcceptBuddyActivity.");
									Intent i = new Intent(
											InviteBuddyActivity.this,
											AcceptBuddyActivity.class); // AcceptBuddyActivity.class);
									i.putExtra("RequestAccepted", 10);
									startActivity(i);
									finish();
								} else {
									// An error has occurred
									ErrorHandler.create().handleError(
											InviteBuddyActivity.this,
											result, null);
								}

							}
						};
						task.execute();

					}
				} else {

					if (Constants.EMAIL_NOT_FOUND) {
						friendEmail
								.setError("This email is not registered to HealthyTogether.");
					} 
					else {
						friendEmail
								.setError("No internet connection. Please try again later.");
					}

					friendEmail.requestFocus();
				}
			}
		};
		task.execute();
		
	}

	public boolean onSearchRequested(View v) {
		return onSearchRequested();
	}

	@Override
	public boolean onSearchRequested() {
		if (!Utils.isConnectionPresent(this)) {
			Toast.makeText(this,
					getResources().getString(R.string.connection_toast_message),
					Toast.LENGTH_LONG).show();
			return true;
		}

		return true;
	}

	public void onNewIntent(final Intent queryIntent) {
		super.onNewIntent(queryIntent);

		if (!Utils.isConnectionPresent(this)) {
			Toast.makeText(this,
					getResources().getString(R.string.connection_toast_message),
					Toast.LENGTH_LONG).show();
			return;
		}

		// Toast.makeText(InviteBuddyActivity.this,
		// "@InviteBuddy: onNewIntent launched", Toast.LENGTH_LONG).show();
		final String queryAction = queryIntent.getAction();
		if (Intent.ACTION_SEARCH.equals(queryAction)) {

			AutoCompleteTextView friendEmailET = (AutoCompleteTextView) findViewById(R.id.friendEmail);
			friendEmailET.setText(Constants.SELECTED_EMAIL);

		} else if (Intent.ACTION_VIEW.equals(queryAction)) {

		} else {
		}
	}

	protected void handleIncomingBuddyRequest() 
	{

		RetrieveBuddyEMailTask2 t2 = new RetrieveBuddyEMailTask2(
				AppContext.getInstance().getEmail()) {

			@Override
			protected void onPostExecute(String result) {
				
				final ArrayList<Boolean> hasFriend = new ArrayList<Boolean>();
				//final String myResult = result;
				if (result.contains("not found")) {
					Log.i(TAG,
							"The user does not have any buddy.");

					// Toast.makeText(AcceptBuddyActivity.this,
					// "You do not have any invitations remaining",
					// Toast.LENGTH_LONG).show();
				} else {
					hasFriend.add(true);
				}
				RetrieveBuddyEMailTask task = new RetrieveBuddyEMailTask(
						AppContext.getInstance()
								.getEmail()) {

					@Override
					protected void onPostExecute(
							String buddyEmail) {
						if (buddyEmail.contains("@@")) {
							if (hasFriend.isEmpty()) {
								// Perform
								// multiple-buddy
								// notification.
								showMultiplePendingBuddyInvitationNotification();
							} 
							else {
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
		
/*		Log.d(TAG, "Incoming buddy invitation detected");
		String email = AppContext.getInstance().getEmail();

		RetrieveBuddyEMailTask task = new RetrieveBuddyEMailTask(email) {

			@Override
			protected void onPostExecute(String buddyEmail) {
				if (buddyEmail.contains("@@")) {
					// Perform multiple notifications.
					showMultiplePendingBuddyInvitationNotification();
				} else {
					showPendingBuddyInvitationNotification(buddyEmail);
				}
			}
		};
		task.execute();*/
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

		// The stack builder object will contain an artificial back stack for
		// the
		// started Activity.
		// This ensures that navigating backward from the Activity leads out of
		// your application to the Home screen.
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
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
	}

	public void onAutoCompleteTVClicked(View v)
	{
		ListView slv = (ListView)findViewById(R.id.suggestions_listview);
		suggestionSelected = false;
		slv.setItemChecked(slv.getCheckedItemPosition(), false);
		
		AutoCompleteTextView tv = (AutoCompleteTextView)findViewById(R.id.friendEmail);
		tv.setError(null);
	}
	

	public void prepareSuggestions(int lonelyCount, int pendingCount) {
		
		String[] suggestions;
		ArrayList<Integer> existingIndices = new ArrayList<Integer>();
		if(peopleEmails.size() > 0)
		{
			suggestionsAvailable = true;
			suggestions = new String[Math.min(Constants.SUGGESTION_COUNT,peopleEmails.size())];// getResources().getStringArray(R.array.countries_array);
			int m = 0;
			boolean lonelyDone = false;
			for (int s = 0; s < Constants.SUGGESTION_COUNT && s < peopleEmails.size(); s++) 
			{
				
				if(lonelyCount > 0)
				{
					
					if(lonelyDone && pendingCount > 0)
					{
						m = getRandomValue(pendingCount,lonelyCount,existingIndices);
						existingIndices.add(m);
					}								
					else
					{
						m = getRandomValue(lonelyCount,0,existingIndices);
						existingIndices.add(m);
						lonelyCount--;
						if(lonelyCount <= 0)
							lonelyDone = true;
					}

				}
				else
				{
					m = getRandomValue(peopleEmails.size(),0,existingIndices);
				}
				
				suggestions[s] = peopleEmails.get(m);
			}
		}
		else
		{
			suggestions = new String[]{getResources().getString(R.string.no_suggesions)};
			suggestionsAvailable = false;
		}
		
		for(int s = 0; s < suggestions.length; s++)
		{
			Log.i(TAG,"suggestions[" + s + "] = " + suggestions[s]);
		}
		
		//final ArrayAdapter<String> adapter = new ArrayAdapter<String>(
		
		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(InviteBuddyActivity.this, R.layout.my_single_checklist_item, suggestions); // android.R.layout.simple_list_item_single_choice 
				
				
				/*new SuggestionsArrayAdapter(
				InviteBuddyActivity.this,
				R.layout.my_single_checklist_item, suggestions); //android.R...._1 R.layout.suggestion_list_item, android.R.layout.simple_list_item_1,*/
		final ListView slv = (ListView)findViewById(R.id.suggestions_listview);
		slv.setAdapter(adapter);	
		slv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
//		slv.setItemChecked(0, true);
		
		slv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0,
					View arg1, int arg2, long arg3) 
			{
				
				suggestionSelected = true;
				 
				int i = slv.getCheckedItemPosition();
					
//				Toast.makeText(InviteBuddyActivity.this, "pos: " + i + ": " + adapter.getItem(arg2), Toast.LENGTH_SHORT).show();
				
				if(suggestionsAvailable)
				{
					
					AutoCompleteTextView tv = (AutoCompleteTextView) findViewById(R.id.friendEmail);
					tv.setError(null);
					tv.setText(adapter.getItem(arg2));
					selectedEmail = adapter.getItem(arg2);
					/*for(int i = 0; i < adapter.getCount(); i++)
					{
						CheckedTextView ctv = adapter.g;
					}*/
					
					slv.setItemChecked(arg2, true);
					
					//slv.setItemChecked(arg2, true);
					//Toast.makeText(InviteBuddyActivity.this, getResources().getString(R.string.no_suggesions), Toast.LENGTH_SHORT).show();
					//Toast.makeText(InviteBuddyActivity.this, adapter.getItem(arg2), Toast.LENGTH_SHORT).show();
					//displayBuddyConfirmDialog(adapter.getItem(arg2));
				}
				else
				{
					Toast.makeText(InviteBuddyActivity.this, getResources().getString(R.string.no_suggesions), Toast.LENGTH_SHORT).show();
				}
				
			}
		});
	}

}


/*
 * 
 * 					ListView slv = (ListView)findViewById(R.id.suggestions_listview);
					String[] suggestions;
					ArrayList<Integer> existingIndices = new ArrayList<Integer>();
					if(peopleEmails.size() > 0)
					{
						suggestionsAvailable = true;
						suggestions = new String[Math.min(Constants.SUGGESTION_COUNT,peopleEmails.size())];// getResources().getStringArray(R.array.countries_array);
						int m = 0;
						boolean lonelyDone = false;
						for (int s = 0; s < Constants.SUGGESTION_COUNT && s < peopleEmails.size(); s++) 
						{
							
							if(lonelyCount > 0)
							{
								
								if(lonelyDone && pendingCount > 0)
								{
									m = getRandomValue(pendingCount,lonelyCount,existingIndices);
									existingIndices.add(m);
								}								
								else
								{
									m = getRandomValue(lonelyCount,0,existingIndices);
									existingIndices.add(m);
									lonelyCount--;
									if(lonelyCount <= 0)
										lonelyDone = true;
								}

							}
							else
							{
								m = getRandomValue(peopleEmails.size(),0,existingIndices);
							}
							
							suggestions[s] = peopleEmails.get(m);
						}
					}
					else
					{
						suggestions = new String[]{getResources().getString(R.string.no_suggesions)};
						suggestionsAvailable = false;
					}
					
					for(int s = 0; s < suggestions.length; s++)
					{
						Log.i(TAG,"suggestions[" + s + "] = " + suggestions[s]);
					}
					
					final ArrayAdapter<String> adapter = new ArrayAdapter<String>(
							InviteBuddyActivity.this,
							R.layout.suggestion_list_item, suggestions); //android.R...._1
					slv.setAdapter(adapter);							
					slv.setOnItemClickListener(new OnItemClickListener() {

						@Override
						public void onItemClick(AdapterView<?> arg0,
								View arg1, int arg2, long arg3) 
						{
							if(suggestionsAvailable)
							{
								//Toast.makeText(InviteBuddyActivity.this, getResources().getString(R.string.no_suggesions), Toast.LENGTH_SHORT).show();
								//Toast.makeText(InviteBuddyActivity.this, adapter.getItem(arg2), Toast.LENGTH_SHORT).show();
								displayBuddyConfirmDialog(adapter.getItem(arg2));
							}
							else
							{
								Toast.makeText(InviteBuddyActivity.this, getResources().getString(R.string.no_suggesions), Toast.LENGTH_SHORT).show();
							}
							
						}
					});
 * 
 * */