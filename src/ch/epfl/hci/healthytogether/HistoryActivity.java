package ch.epfl.hci.healthytogether;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.Toast;
import ch.epfl.hci.happytogether.R;
import ch.epfl.hci.healthytogether.communication.ServerHelper.ChangeLogTask;
import ch.epfl.hci.healthytogether.communication.ServerHelper.CheckMessagesTask;
import ch.epfl.hci.healthytogether.communication.ServerHelper.RetrieveAllMessagesTask;
import ch.epfl.hci.healthytogether.communication.ServerHelper.SendMessageTask;
import ch.epfl.hci.healthytogether.model.MessageMeta;
import ch.epfl.hci.healthytogether.util.Utils;

@SuppressLint("NewApi")
public class HistoryActivity extends Activity implements OnItemSelectedListener {
	private static final String TAG = HistoryActivity.class.getSimpleName();

	private LogArrayAdapter adapter;
	// private ArrayList<OneLog> modifiedItems;
	// private ArrayList<Integer> modifiedItemIndices;

	private ListView mListView;
	// private EditText editText1;
	ArrayList<String> list_name;
	private Handler checkLogsHandler;
	private boolean isActive = false;

	String [] foodTemplates;
	String [] exerciseTemplates;
	String [] sedentaryTemplates;
	String [] positiveTemplates;
	String [] negativeTemplates;
	String [] moodIntensityTemplates;
	String [] socialTemplates;
	
	ProgressDialog dialog;

	private boolean isDisplayingConnectionErrorDialog = false;

	private Runnable checkForNewLogsTask = new Runnable() {

		@Override
		public void run() {
			// Log.e(TAG, "**** RUNNING LOG CHECKER");

			if (!Utils.isConnectionPresent(HistoryActivity.this)) {
				// showProgressDialog(false);
				// Toast.makeText(HistoryActivity.this,
				// "Could not update logs: No internet connection. Please try again later.",
				// Toast.LENGTH_LONG).show();
				displayConnectionErrorDialog();
			} else {
				loadExistingLogs();
			}

			// schedule next check
			checkLogsHandler.postDelayed(this,
					Constants.MESSAGE_POLLING_INTERVAL);
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		foodTemplates = getResources().getStringArray(R.array.foodTemplates);
		exerciseTemplates = getResources().getStringArray(R.array.exerciseTemplates);
		sedentaryTemplates = getResources().getStringArray(R.array.sedentaryTemplates);
		positiveTemplates = getResources().getStringArray(R.array.positiveMoodTemplates);
		negativeTemplates = getResources().getStringArray(R.array.negativeMoodTemplates);
		moodIntensityTemplates = getResources().getStringArray(R.array.moodIntensityTemplates);
		socialTemplates = getResources().getStringArray(R.array.socialTemplates);		
		isActive = true;
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.activity_history);
		checkLogsHandler = new Handler();
		// Constants.modifiedItems = new ArrayList<OneLog>();
		// Constants.modifiedItemIndices = new ArrayList<Integer>();
		mListView = (ListView) findViewById(R.id.historyListView1);

		// adapter = new LogArrayAdapter(getApplicationContext(),
		// R.layout.listitem_log);
		// adapter = new LogArrayAdapter(this, R.layout.listitem_log,
		// dataItems);
		adapter = new LogArrayAdapter(this, R.layout.listitem_log);
		mListView.setAdapter(adapter);
		mListView.setItemsCanFocus(true);
		Toast.makeText(this, getResources().getString(R.string.logs_dialog_content), Toast.LENGTH_SHORT).show();

		// editText1 = (EditText) findViewById(R.id.typeMessage);
		// editText1.setOnKeyListener(new OnKeyListener() {
		// public boolean onKey(View v, int keyCode, KeyEvent event) {
		// // If the event is a key-down event on the "enter" button
		// if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode ==
		// KeyEvent.KEYCODE_ENTER)) {
		// return sendNewMsg();
		// }
		// return false;
		// }
		//
		// });

		// // Load the existing messages and populate the view

		// loadExistingLogs();
		
		
		
		showProgressDialog(true);
		checkLogsHandler.postDelayed(checkForNewLogsTask, 0);
	}

	protected void onDestroy() {
		super.onDestroy();
		isActive = false;
		isDisplayingConnectionErrorDialog = false;
	}

	private void showProgressDialog(final boolean show) {
		if (show) {
			if (isActive) {
				dialog = ProgressDialog.show(this, getResources().getString(R.string.logs_dialog_title),
						getResources().getString(R.string.logs_dialog_content));
			}
		} else if (dialog != null) {
			dialog.dismiss();
			dialog = null;
		}
	}

	/**
	 * Shows the progress UI and hides the history screen
	 */
	/*
	 * @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2) private void
	 * showProgress(final boolean show) { // On Honeycomb MR2 we have the
	 * ViewPropertyAnimator APIs, which allow // for very easy animations. If
	 * available, use these APIs to fade-in // the progress spinner. final View
	 * mLoginFormView = findViewById(R.id.historyForm); final View
	 * mLoginFormView222 = findViewById(R.id.historyListView1); final View
	 * mLoginStatusView = findViewById(R.id.history_retrieval_status);
	 * 
	 * 
	 * 
	 * if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) { int
	 * shortAnimTime = getResources().getInteger(
	 * android.R.integer.config_shortAnimTime);
	 * 
	 * mLoginStatusView.setVisibility(View.VISIBLE);
	 * mLoginStatusView.animate().setDuration(shortAnimTime) .alpha(show ? 1 :
	 * 0) .setListener(new AnimatorListenerAdapter() {
	 * 
	 * @Override public void onAnimationEnd(Animator animation) {
	 * mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE); } });
	 * 
	 * mLoginFormView.setVisibility(View.VISIBLE);
	 * mLoginFormView.animate().setDuration(shortAnimTime) .alpha(show ? 0 : 1)
	 * .setListener(new AnimatorListenerAdapter() {
	 * 
	 * @Override public void onAnimationEnd(Animator animation) {
	 * mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE); } });
	 * 
	 * mLoginFormView222.setVisibility(View.VISIBLE);
	 * mLoginFormView222.animate().setDuration(shortAnimTime) .alpha(show ? 0 :
	 * 1) .setListener(new AnimatorListenerAdapter() {
	 * 
	 * @Override public void onAnimationEnd(Animator animation) {
	 * mLoginFormView222.setVisibility(show ? View.GONE : View.VISIBLE); } }); }
	 * else { // The ViewPropertyAnimator APIs are not available, so simply show
	 * // and hide the relevant UI components.
	 * mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
	 * mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
	 * mLoginFormView222.setVisibility(show ? View.GONE : View.VISIBLE); } }
	 */

	public void displayConnectionErrorDialog() {
		if (!isDisplayingConnectionErrorDialog) {
			if (isActive) {
				showProgressDialog(false);
				AlertDialog.Builder builder;
				builder = new AlertDialog.Builder(HistoryActivity.this);
				builder.setTitle(getResources().getString(R.string.connection_error_title))
						.setMessage(
								getResources().getString(R.string.connection_error_message))
						// .setCancelable(false)
						.setOnCancelListener(
								new DialogInterface.OnCancelListener() {

									@Override
									public void onCancel(DialogInterface dialog) {
										moveTaskToBack(true);
										/*Intent i_main = new Intent(HistoryActivity.this, Main2Activity.class);
										startActivity(i_main);*/
										// checkLogsHandler.postDelayed(checkForNewLogsTask,
										// 0);
									}
								})
						.setNeutralButton(getResources().getString(R.string.connection_error_retry),
								new OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// 
										isDisplayingConnectionErrorDialog = false;
										showProgressDialog(true);
										checkLogsHandler.postDelayed(
												checkForNewLogsTask, 0);
									}
								});

				AlertDialog connectionDialog = builder.create();

				isDisplayingConnectionErrorDialog = true;
				connectionDialog.show();
			}

		}
	}

	public void makeInfo(int pos) {
		// Log.i("makeInfo", "=" + pos);
	}

	@Override
	protected void onResume() {
		super.onResume();
		isActive = true;
		/*
		 * start the loop to check for new messages and immediately display them
		 * this is separate from the CheckForMessageService that runs all the
		 * time.
		 */
		checkLogsHandler.postDelayed(checkForNewLogsTask, 0); // we immediately
																// check for new
																// messages
	}

	@Override
	protected void onPause() {
		super.onPause();
		isActive = false;
		isDisplayingConnectionErrorDialog = false;
		// need to stop the message poling loop
		// Log.e(TAG, "*** STOPPING LOG POLLING");
		checkLogsHandler.removeCallbacks(checkForNewLogsTask);
	}


	/*
	 * private boolean sendNewMsg() { // final String text =
	 * DatabaseUtils.sqlEscapeString(editText1.getText().toString()); final
	 * String sqlText = editText1.getText().toString().replaceAll("'", "''");
	 * int uid = AppContext.getInstance().getUserId(); int fid=
	 * AppContext.getInstance().getFriendId(); SendMessageTask task= new
	 * SendMessageTask(uid, fid, sqlText) {
	 * 
	 * @Override protected void onPostExecute(Boolean success) { if (success) {
	 * if(!sqlText.isEmpty()){ // add the message to the view (user's msg appear
	 * on the right) adapter.add(new OneComment(false,
	 * editText1.getText().toString())); editText1.setText("");
	 * scrollToBottom(); } Toast.makeText(HistoryActivity.this, "Message sent.",
	 * Toast.LENGTH_SHORT).show(); } else { Toast.makeText(HistoryActivity.this,
	 * "Could not send message. Please try again later.",
	 * Toast.LENGTH_SHORT).show(); } } }; task.execute();
	 * 
	 * 
	 * return true; }
	 */

	private void loadExistingLogs() {
		AppContext context = AppContext.getInstance();
		RetrieveAllMessagesTask task = new RetrieveAllMessagesTask(
				context.getUserId(), context.getUserId()) {

			@Override
			protected void onPostExecute(ArrayList<MessageMeta> messages) {

				boolean hasMessages = adapter.getCount() > 0;

				if (!hasMessages) {
					// the message list is populated for the first time
					// Log.d(TAG, "*** POPULATING LOGS INITIAL");
					addExistingLogs(messages);
				} else if (messages.size() > 0) {

					OneLog latestLocalMsg = adapter
							.getItem(adapter.getCount() - 1);
					MessageMeta latestMsg = messages.get(messages.size() - 1);
					boolean newMsgAvailable = true;
					// latestMsg.getmsgfrom().equals(AppContext.getInstance().getUserId()+"")
					// && // latest msg is own msg, we know we're up to date as
					// own msgs are always displayed after sending
					// Integer.parseInt(latestMsg.getmsgid()) >
					// latestLocalMsg.getLogId(); // we check if there's a msg
					// with a higher id on the server
					if (newMsgAvailable) {
						// Log.e(TAG, "*** NEW LOG(S) AVAILABLE...RELOADING");
						markNewMessagesAsRead(adapter.getAllMessages(),
								messages);
						addExistingLogs(messages);
					} else {
						// Log.e(TAG, "*** NO NEW MESSAGE(S) AVAILABLE");
					}
				} else {
					// Log.e(TAG, "Server bug: 0 messages received");
				}

				showProgressDialog(false);
			}
		};

		task.execute();
	}

	/**
	 * we mark all new messages as read so they are not shown in the
	 * notification when the background checker checks. "Marking as read" means
	 * running the corresponding async task as many times as there are new
	 * messages.
	 */

	protected void markNewMessagesAsRead(List<OneLog> localMessages,
			ArrayList<MessageMeta> serverMessages) {
		int diff = serverMessages.size() - localMessages.size();
		if (diff < 0) {
			// Log.e(TAG, "error discarding messages, diff was " + diff);
			return;
		}

		// Log.d(TAG, "**** discarding " + diff + " messages");
		// for (int i = 0; i < diff; i++) {
		int uid = AppContext.getInstance().getUserId();
		CheckMessagesTask task = new CheckMessagesTask(uid) {

			@Override
			protected void onPostExecute(String message) { /*
															 * nothing to do
															 * here
															 */
				if(!this.safeExecution){
					Log.e(TAG, "No new messages received");
				}
			}
		};
		task.execute();
		// }
	}


	private void addExistingLogs(ArrayList<MessageMeta> messages) {

		// adapter = new LogArrayAdapter(getApplicationContext(),
		// R.layout.listitem_log);
		// adapter = new LogArrayAdapter(this, R.layout.listitem_log,
		// dataItems);
		adapter = new LogArrayAdapter(this, R.layout.listitem_log);
		// OneLog l = new
		// OneLog("LOG FOOD None EXERCISE Jogging MOOD Sad SOCIAL Alone",
		// "19:30",1);
		// adapter.add(l);
		/*
		 * if(Constants.logLists.size() > 0) { for(int l = 0; l <
		 * Constants.logLists.size(); l++) {
		 * 
		 * //adapter.add(Constants.logLists.get(l));
		 * //dataItems.add(Constants.logLists.get(l));
		 * adapter.add(Constants.logLists.get(l)); } }
		 * 
		 * Constants.logLists.clear();
		 */

		mListView.setAdapter(adapter);

		// adapter.clear();
		// adapter.notifyDataSetChanged();

		int uid = AppContext.getInstance().getUserId();
		int currentLogId, i;
		boolean atLeastOneLog = false;
		boolean atLeastOneIteration = false;
		boolean previouslyModified = false;

		for (MessageMeta message : messages) {
			// Log.d(TAG, "Adding msg (id: " + message.getmsgid() + ") from " +
			// message.getmsgfrom() + ": " + message.getmsgtxt());
			atLeastOneIteration = true;
			// boolean mine= message.getmsgfrom().equals(uid+"");
			boolean isLog = message.getmsgtxt().contains("LOG");
			previouslyModified = false;
			if (isLog) {
				currentLogId = Integer.parseInt(message.getmsgid());
				atLeastOneLog = true;
				/*
				 * for(i = 0; i < Constants.modifiedItemIndices.size(); i++) {
				 * if( currentLogId == Constants.modifiedItemIndices.get(i)) {
				 * previouslyModified = true; break; }
				 * 
				 * }
				 */

				String msg = message.getmsgtxt();
				
				// TODO: 1. Find the index of each dimension (put it as it is if necessary)
				// TODO: 2. Find the corresponding native language item.
				
				OneLog bubble = new OneLog(message.getmsgtxt(),
						message.getmsgtime(), currentLogId, "None");
				
				
				bubble.translateToNative(positiveTemplates,negativeTemplates,foodTemplates,sedentaryTemplates,exerciseTemplates,socialTemplates,moodIntensityTemplates,getResources().getString(R.string.none));
				
				adapter.add(bubble);
				// Log.d(TAG, "Added this: " + bubble.activity);
			}
		}

		if (atLeastOneIteration) {
			// Log.e(TAG, "At least one message or log was already parsed.");
			adapter.notifyDataSetChanged();
		}

		/*
		 * if(atLeastOneLog) { //Toast.makeText(HistoryActivity.this,
		 * "addExistingLogs: New logs awaiting!", Toast.LENGTH_SHORT).show(); }
		 */
		// adapter.notifyDataSetChanged();
		// scrollToBottom();
	}

	private void scrollToBottom() {
		// scroll to bottom V2 -- not working
		// adapter.notifyDataSetChanged();
		// mListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		//

		// scroll to bottom (works but not so smooth)
		mListView.post(new Runnable() {
			@Override
			public void run() {
				// Select the last row so it will scroll into view...
				mListView.setSelection(adapter.getCount() - 1);
			}
		});
	}

	public void changeLogSocial(int pos, int logId, String newSocial) {
		Toast.makeText(this, getResources().getString(R.string.social_info_changed), Toast.LENGTH_SHORT)
				.show();
		OneLog log = adapter.getItem(pos);
		int logId2 = log.getLogId();
		log.social = newSocial;
		int i;
		boolean previouslyModified = false;
		for (i = 0; i < Constants.modifiedItemIndices.size(); i++) {
			if (logId2 == Constants.modifiedItemIndices.get(i)) {
				previouslyModified = true;
				break;
			}
		}

		if (previouslyModified) {
			Constants.modifiedItems.remove(i);
			Constants.modifiedItemIndices.remove(i);
		}
		Constants.modifiedItems.add(log);
		Constants.modifiedItemIndices.add(logId2);
	}

	public void changeLogActivity(int pos, int logId, String listItem) 
	{
		OneLog log = adapter.getItem(pos);
		final String msg = log.toDBEntry(positiveTemplates,
				negativeTemplates, foodTemplates,
				sedentaryTemplates,exerciseTemplates,
				socialTemplates,moodIntensityTemplates,getResources().getString(R.string.none));
		
		int logId2 = log.getLogId();
		final Activity act = this;
		log.activity = listItem;

		final int uid = AppContext.getInstance().getUserId();
		final int fid = uid;
		final String newMsg = log.toDBEntry(positiveTemplates,
				negativeTemplates, foodTemplates,
				sedentaryTemplates,exerciseTemplates,
				socialTemplates,moodIntensityTemplates,getResources().getString(R.string.none));
		//final String actStr = log.activity;

		ChangeLogTask task = new ChangeLogTask(uid, fid, newMsg, msg) {

			@Override
			protected void onPostExecute(Boolean success) {
				if (success) {
					// if(!newMsg.isEmpty()){
					// add the message to the view (user's msg appear on the
					// right)
					// adapter.add(new OneComment(false,
					// editText1.getText().toString()));
					// editText1.setText("");
					// scrollToBottom();
					// }

					// ((HistoryActivity)activity).removeLog(log.getLogId());

					// Toast.makeText(act, "Mood info changed to " + actStr +
					// ", please wait...", Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(
							act,
							getResources().getString(R.string.connection_toast_message),
							Toast.LENGTH_SHORT).show();
				}
			}
		};
		task.execute();

		/*
		 * int i; boolean previouslyModified = false; for(i = 0; i <
		 * Constants.modifiedItemIndices.size(); i++) { if( logId2 ==
		 * Constants.modifiedItemIndices.get(i)) { previouslyModified = true;
		 * break; }
		 * 
		 * }
		 * 
		 * if(previouslyModified) { Constants.modifiedItems.remove(i);
		 * Constants.modifiedItemIndices.remove(i); }
		 * Constants.modifiedItems.add(log);
		 * Constants.modifiedItemIndices.add(logId2);
		 */

		/*
		 * if(log.foodSet) { Toast.makeText(this, "Food changed!",
		 * Toast.LENGTH_SHORT).show(); } else { Toast.makeText(this,
		 * "Exercise changed!", Toast.LENGTH_SHORT).show(); }
		 */

	}

	public void changeLogMood(int pos, int logId, String listItem) {
		OneLog log = adapter.getItem(pos);
		int logId2 = log.getLogId();
		log.mood = listItem;
		int i;
		boolean previouslyModified = false;
		for (i = 0; i < Constants.modifiedItemIndices.size(); i++) {
			if (logId2 == Constants.modifiedItemIndices.get(i)) {
				previouslyModified = true;
				break;
			}

		}

		if (previouslyModified) {
			Constants.modifiedItems.remove(i);
			Constants.modifiedItemIndices.remove(i);
		}
		Constants.modifiedItems.add(log);
		Constants.modifiedItemIndices.add(logId2);

		Toast.makeText(this, getResources().getString(R.string.mood_changed), Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos,
			long id) {
		// Spinner s = (Spinner)view;

		changeLogActivity(Integer.parseInt((String) parent.getTag()), -1,
				parent.getItemAtPosition(pos).toString());
		// s.setTop(pos);
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {

	}

	public void removeLog(int logId) {
		for (int i = 0; i < Constants.logLists.size(); i++) {
			if (Constants.logLists.get(i).getLogId() == logId) {
				Constants.logLists.remove(i);
			}
		}

	}



	/*
	 * public void onSendButtonClicked(View v) { // Prevent sending empty
	 * message
	 * 
	 * if(editText1.getText().toString().trim().isEmpty()){ return; }
	 * sendNewMsg();
	 * 
	 * Toast.makeText(this, "onSendButtonClicked: implement",
	 * Toast.LENGTH_SHORT); }
	 */

	/*
	 * @SuppressLint("NewApi")
	 * 
	 * @TargetApi(Build.VERSION_CODES.HONEYCOMB)
	 * 
	 * @SuppressWarnings({ "unchecked", "rawtypes" }) public void
	 * onTemplateButtonClicked(View v){ // Show the template popup dialog
	 * showDialog(5);
	 * 
	 * 
	 * // final ListPopupWindow lpw = new
	 * ListPopupWindow(MessagesActivity.this); // final ArrayAdapter
	 * arrayAdapter = new ArrayAdapter(MessagesActivity.this,
	 * R.layout.simple_list_item, listItems); // lpw.setAdapter(arrayAdapter);
	 * // lpw.setAnchorView(findViewById(R.id.msgView)); // lpw.setWidth(480);
	 * // lpw.setHeight(400); //
	 * lpw.setPromptPosition(ListPopupWindow.POSITION_PROMPT_ABOVE); ////
	 * lpw.setPromptPosition(ListPopupWindow.POSITION_PROMPT_BELOW); //
	 * lpw.setOnItemClickListener(new OnItemClickListener() { // @Override //
	 * public void onItemClick(AdapterView<?> adapter, View view, int position,
	 * long arg) { // String listItem = (String)arrayAdapter.getItem(position);
	 * // editText1.setText(listItem); // lpw.dismiss(); // } // }); //
	 * lpw.show();
	 * 
	 * }
	 */

	/*
	 * @Override protected Dialog onCreateDialog(int id, Bundle args) {
	 * if(id==5){ boolean doCheer= getIntent().getBooleanExtra("cheer", true);
	 * final String[] listItems; if(doCheer){ listItems=
	 * Constants.cheerTemplates; } else { listItems = Constants.tauntTemplates;
	 * } final boolean[] itemsChecked = new boolean[listItems.length]; for (int
	 * j = 0; j < itemsChecked.length; j++) { itemsChecked[j] = false; }
	 * Toast.makeText(this, "onCreateDialog: implement", Toast.LENGTH_SHORT);
	 * AlertDialog.Builder builder = new AlertDialog.Builder(this);
	 * builder.setTitle("Pick a template"); // builder.setItems(listItems, new
	 * DialogInterface.OnClickListener() {
	 * builder.setMultiChoiceItems(listItems, null, new
	 * DialogInterface.OnMultiChoiceClickListener() {
	 * 
	 * @Override public void onClick(DialogInterface dialog, int which, boolean
	 * isChecked) { // The 'which' argument contains the index position // of
	 * the selected item if (isChecked) { //editText1.setText(listItems[which]);
	 * 
	 * dismissDialog(5); } ((AlertDialog)
	 * dialog).getListView().setItemChecked(which, false);
	 * 
	 * } }); // // Set the action buttons // .setPositiveButton("OK", new
	 * DialogInterface.OnClickListener() { // @Override // public void
	 * onClick(DialogInterface dialog, int id) { // // User clicked OK, so save
	 * the mSelectedItems results somewhere // // or return them to the
	 * component that opened the dialog // editText1.setText(listItems[id]); //
	 * } // }) // .setNegativeButton("Cancel", new
	 * DialogInterface.OnClickListener() { // @Override // public void
	 * onClick(DialogInterface dialog, int id) { // // } // }); return
	 * builder.create(); }else{ return super.onCreateDialog(id, args); } }
	 */

}