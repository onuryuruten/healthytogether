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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import ch.epfl.hci.happytogether.R;
import ch.epfl.hci.healthytogether.communication.ServerHelper.CheckGroupTask;
import ch.epfl.hci.healthytogether.communication.ServerHelper.CheckMessagesTask;
import ch.epfl.hci.healthytogether.communication.ServerHelper.RetrieveAllMessagesTask;
import ch.epfl.hci.healthytogether.communication.ServerHelper.RetrieveBuddyEMailTask;
import ch.epfl.hci.healthytogether.communication.ServerHelper.SendMessageTask;
import ch.epfl.hci.healthytogether.model.MessageMeta;
import ch.epfl.hci.healthytogether.util.Utils;

@SuppressLint("NewApi")
public class MessagesActivity extends Activity {

	boolean isCheer;

	private static final String TAG = MessagesActivity.class.getSimpleName();

	private DiscussArrayAdapter adapter;
	private ListView mListView;
	private EditText editText1;

	private Handler checkMessagesHandler;

	private static boolean messageLock = false;

	private boolean isActive = false;
	ProgressDialog dialog;

	private boolean isDisplayingConnectionErrorDialog = false;;
	private boolean isDisplayingBuddyDialog = false;
	private AlertDialog buddyDialog;
	private String buddyDialogMessage;

	private Runnable checkForNewMessagesTask = new Runnable() {

		@Override
		public void run() {
			if (!Utils.isConnectionPresent(MessagesActivity.this)) {
				// Toast.makeText(MessagesActivity.this,
				// "Could not update messages: No internet connection. Please try again later.",
				// Toast.LENGTH_LONG).show();
				// showProgressDialog(false);
				displayConnectionErrorDialog();
			} else {
				Constants.checkUserName();
				String email = AppContext.getInstance().getEmail();
				int userId = AppContext.getInstance().getUserId();

				CheckGroupTask checkGroupTask = new CheckGroupTask(email,
						userId) {

					@Override
					protected void onPostExecute(String result) {
						
						if(!this.safeExecution)
						{
							if(!Utils.isConnectionPresent(MessagesActivity.this))
							{
								displayConnectionErrorDialog();
							}
							return;
						}
						
						// Log.d(TAG, "response: " + result);
						if (Utils.isInteger(result)) {
							int resultCode = Integer.parseInt(result);
							Intent i;
							switch (resultCode) {
							case RESPONSE_CODE_NO_BUDDY:
								// show screen to invite buddy

								String msg = getResources().getString(R.string.buddy_stopped_playing);//"Your buddy has changed his mind, so you are no longer paired. Who do you want to play with?";
								/*Toast.makeText(MessagesActivity.this, msg,
										Toast.LENGTH_LONG).show();*/
								i = new Intent(MessagesActivity.this,
										SettingsActivity.class);
								i.putExtra("message", msg);
								i.putExtra("RequestAccepted", -1);
								Constants.COUNTER_INVITE = false;
								Constants.INVITATION_ACCEPTED = false;
								SharedPreferences prefs2 = getSharedPreferences(
										Constants.PROPERTIES_NAME, MODE_PRIVATE);
								Editor editor = prefs2.edit();
								editor.putBoolean(
										Constants.PROP_KEY_GAME_STARTED, false);
								editor.commit();
								startActivity(i);
								break;
							case RESPONSE_INCOMING_REQUEST_PENDING:
								// show screen to approve friend request
								SharedPreferences prefs = getSharedPreferences(
										Constants.PROPERTIES_NAME,
										Context.MODE_PRIVATE);
								if (!prefs.getBoolean(
										Constants.PROP_KEY_GAME_STARTED, false)) {
									/*
									 * i= new Intent(MessagesActivity.this,
									 * AcceptBuddyActivity.class);
									 * i.putExtra("RequestAccepted", -1);
									 * startActivity(i);
									 */

									RetrieveBuddyEMailTask task = new RetrieveBuddyEMailTask(
											AppContext.getInstance().getEmail()) {

										@Override
										protected void onPostExecute(
												String buddyEmail) {
											
											if(!this.safeExecution)
											{
												if(!Utils.isConnectionPresent(MessagesActivity.this))
												{
													displayConnectionErrorDialog();
												}
												return;
											}
											
											if (buddyEmail.contains("@@")) {
												// Perform multiple
												// notifications.
												// showMultiplePendingBuddyInvitationNotification();
												Log.d(TAG,
														"do show a couple of invitations");
											} else {
												Log.d(TAG,
														"do show a single invitation");
												// showPendingBuddyInvitationNotification(buddyEmail);
											}
										}
									};
									task.execute();
								}
								break;
							case RESPONSE_WAITING_FOR_ACCEPT:
								// show screen to remind buddy that he has a
								// pending invitation
								i = new Intent(MessagesActivity.this,
										PendingBuddyRequestActivity.class);
								startActivity(i);
								break;
							default:
								break;
							}
						} else {
							// error log_tag
							if (!Utils
									.isConnectionPresent(MessagesActivity.this)) {
								// Toast.makeText(MessagesActivity.this,
								// "Could not load messages: No internet connection. Please try again later.",
								// Toast.LENGTH_LONG).show();
								displayConnectionErrorDialog();
							}
							// Log.e(TAG, "an error has occurred");
						}

					}
				};
				checkGroupTask.execute();

				// Log.e(TAG, "**** RUNNING MESSAGE CHECKER");

				if (!Utils.isConnectionPresent(MessagesActivity.this)) {
					// Toast.makeText(MessagesActivity.this,
					// "Could not load messages: No internet connection. Please try again later.",
					// Toast.LENGTH_LONG).show();
					displayConnectionErrorDialog();
				} else {
					loadExistingMessages();
				}

			}

			// schedule next check
			showProgressDialog(false);
			checkMessagesHandler.postDelayed(this,
					Constants.MESSAGE_POLLING_INTERVAL);

		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		isActive = true;
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.activity_messages);

		isCheer = getIntent().getBooleanExtra("cheer", true);

		if (isCheer) {
			this.setTitle(getResources().getString(R.string.title_activity_select_cheer_template));
		} else {
			this.setTitle(getResources().getString(R.string.title_activity_select_taunt_template));
		}

		checkMessagesHandler = new Handler();

		mListView = (ListView) findViewById(R.id.listView1);

		adapter = new DiscussArrayAdapter(getApplicationContext(),
				R.layout.listitem_message);
		mListView.setAdapter(adapter);

		editText1 = (EditText) findViewById(R.id.typeMessage);
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
		// loadExistingMessages();
		showProgressDialog(true);
		checkMessagesHandler.postDelayed(checkForNewMessagesTask, 0);
	}

	private void showProgressDialog(final boolean show) {
		if (show) {
			if (isActive) {
				dialog = ProgressDialog.show(this, getResources().getString(R.string.messages_progress_title),
						getResources().getString(R.string.messages_progress_content));
			}
		} else if (dialog != null) {
			dialog.dismiss();
			dialog = null;
		}
	}

	public void displayConnectionErrorDialog() {
		if (!isDisplayingConnectionErrorDialog) {
			if (isActive) {
				showProgressDialog(false);
				AlertDialog.Builder builder;
				builder = new AlertDialog.Builder(MessagesActivity.this);
				builder.setTitle(getResources().getString(R.string.connection_error_title))
						.setMessage(
								getResources().getString(R.string.connection_error_message))
						// .setCancelable(false)
						.setOnCancelListener(
								new DialogInterface.OnCancelListener() {

									@Override
									public void onCancel(DialogInterface dialog) {
										moveTaskToBack(true);
										/*Intent i_main = new Intent(MessagesActivity.this, Main2Activity.class);
										startActivity(i_main);*/
										// checkMessagesHandler.postDelayed(checkForNewMessagesTask,
										// 0);
									}
								})
						.setNeutralButton(getResources().getString(R.string.connection_error_retry),
								new OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
										isDisplayingConnectionErrorDialog = false;
										showProgressDialog(true);
										checkMessagesHandler.postDelayed(
												checkForNewMessagesTask, 0);
									}
								});

				AlertDialog connectionDialog = builder.create();

				isDisplayingConnectionErrorDialog = true;
				connectionDialog.show();
			}

		}
	}

	protected void onDestroy() {
		super.onDestroy();
		isActive = false;
		isDisplayingConnectionErrorDialog = false;
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
		checkMessagesHandler.postDelayed(checkForNewMessagesTask, 0); // we
																		// immediately
																		// check
																		// for
																		// new
																		// messages
	}

	@Override
	protected void onPause() {
		super.onPause();
		isActive = false;
		isDisplayingConnectionErrorDialog = false;

		// need to stop the message poling loop
		// Log.e(TAG, "*** STOPPING MESSAGE POLLING");
		checkMessagesHandler.removeCallbacks(checkForNewMessagesTask);
	}

	private boolean sendNewMsg() {

		if (!messageLock) {
			messageLock = true;
			// final String text =
			// DatabaseUtils.sqlEscapeString(editText1.getText().toString());
			final String sqlText = editText1.getText().toString()
					.replaceAll("'", "''");
			int uid = AppContext.getInstance().getUserId();
			int fid = AppContext.getInstance().getFriendId();
			Toast.makeText(MessagesActivity.this, getResources().getString(R.string.sending_your_message),
					Toast.LENGTH_SHORT).show();
			SendMessageTask task = new SendMessageTask(uid, fid, sqlText) {

				@Override
				protected void onPostExecute(Boolean success) 
				{
					if (success) {
						if (!(sqlText == null) && !(sqlText == "")
								&& !(sqlText.length() == 0)) {
							// add the message to the view (user's msg appear on
							// the right)
							adapter.add(new OneComment(false, editText1
									.getText().toString()));
							editText1.setText("");
							scrollToBottom();
						}
						Toast.makeText(MessagesActivity.this, getResources().getString(R.string.message_sent),
								Toast.LENGTH_SHORT).show();
					} else {
						if (Constants.BUDDY_DECLINED) {
							Toast.makeText(
									MessagesActivity.this,
									getResources().getString(R.string.message_buddy_left),
									Toast.LENGTH_SHORT).show();
						} else {
							Toast.makeText(
									MessagesActivity.this,
									getResources().getString(R.string.message_sending_error),
									Toast.LENGTH_SHORT).show();
						}
					}
					messageLock = false; // re-enable it all.

				}
			};
			task.execute();
		} else {
			Toast.makeText(MessagesActivity.this,
					getResources().getString(R.string.message_ack_waiting), Toast.LENGTH_LONG)
					.show();
		}

		return true;
	}

	private void loadExistingMessages() {
		AppContext context = AppContext.getInstance();
		RetrieveAllMessagesTask task = new RetrieveAllMessagesTask(
				context.getUserId(), context.getFriendId()) {

			@Override
			protected void onPostExecute(ArrayList<MessageMeta> messages) {
				// need to inverse the messages list as we want the most recent
				// messages at the bottom

					if(!Utils.isConnectionPresent(MessagesActivity.this))
					{
						displayConnectionErrorDialog();
						return;
					}
					

				
				Collections.reverse(messages);

				boolean hasMessages = adapter.getCount() > 0;

				if (!hasMessages) {
					// the message list is populated for the first time
					// Log.d(TAG, "*** POPULATING MESSAGES INITIAL");
					addExistingMessages(messages);
				} else if (messages.size() > 0) {
					OneComment latestLocalMsg = adapter.getItem(adapter
							.getCount() - 1);
					MessageMeta latestMsg = messages.get(messages.size() - 1);
					boolean newMsgAvailable = !latestMsg.getmsgfrom().equals(
							AppContext.getInstance().getUserId() + "")
							&& // latest msg is own msg, we know we're up to
								// date as own msgs are always displayed after
								// sending
							Integer.parseInt(latestMsg.getmsgid()) > latestLocalMsg
									.getMsgId(); // we check if there's a msg
													// with a higher id on the
													// server
					if (newMsgAvailable) {
						// Log.e(TAG,
						// "*** NEW MESSAGE(S) AVAILABLE...RELOADING");
						markNewMessagesAsRead(adapter.getAllMessages(),
								messages);
						addExistingMessages(messages);
					} else {
						// Log.e(TAG, "*** NO NEW MESSAGE(S) AVAILABLE");
					}
				} else {
					// Log.e(TAG, "Server bug: 0 messages received");
				}

				showProgressDialog(false); // cancel the progress display
			}
		};
		task.execute();
	}

	/**
	 * we mark all new messages as read so they are not shown in the
	 * notification when the background checker checks.
	 */
	protected void markNewMessagesAsRead(List<OneComment> localMessages,
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
			}
		};
		task.execute();
		// }
	}

	private void addExistingMessages(ArrayList<MessageMeta> messages) {
		adapter = new DiscussArrayAdapter(getApplicationContext(),
				R.layout.listitem_message);
		mListView.setAdapter(adapter);

		// adapter.clear();
		// adapter.notifyDataSetChanged();

		int uid = AppContext.getInstance().getUserId();
		for (MessageMeta message : messages) {
			// Log.d(TAG, "Adding msg (id: " + message.getmsgid() + ") from " +
			// message.getmsgfrom() + ": " + message.getmsgtxt());
			boolean mine = message.getmsgfrom().equals(uid + "");
			boolean isLog = message.getmsgtxt().contains("LOG");
			String str = message.getmsgtime();
			if (!isLog) {
				OneComment bubble = new OneComment(!mine, message.getmsgtxt(),
						message.getmsgtime(), Integer.parseInt(message
								.getmsgid()));
				adapter.add(bubble);
			} else {
				OneLog log = new OneLog(message.getmsgtxt(),
						message.getmsgtime(), Integer.parseInt(message
								.getmsgid()), "None");
				Constants.logLists.add(log);
			}
		}
		scrollToBottom();
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

	public void onSendButtonClicked(View v) {
		// Prevent sending empty message
		String msg = editText1.getText().toString().trim();
		if (msg == null || msg == "" || msg.length() == 0) {
			return;
		}

		if (!Utils.isConnectionPresent(MessagesActivity.this)) {
			displayConnectionErrorDialog();
		}

		sendNewMsg();
	}

	@SuppressLint("NewApi")
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void onTemplateButtonClicked(View v) {
		// Show the template popup dialog
		showDialog(5);

		// final ListPopupWindow lpw = new
		// ListPopupWindow(MessagesActivity.this);
		// final ArrayAdapter arrayAdapter = new
		// ArrayAdapter(MessagesActivity.this, R.layout.simple_list_item,
		// listItems);
		// lpw.setAdapter(arrayAdapter);
		// lpw.setAnchorView(findViewById(R.id.msgView));
		// lpw.setWidth(480);
		// lpw.setHeight(400);
		// lpw.setPromptPosition(ListPopupWindow.POSITION_PROMPT_ABOVE);
		// // lpw.setPromptPosition(ListPopupWindow.POSITION_PROMPT_BELOW);
		// lpw.setOnItemClickListener(new OnItemClickListener() {
		// @Override
		// public void onItemClick(AdapterView<?> adapter, View view, int
		// position, long arg) {
		// String listItem = (String)arrayAdapter.getItem(position);
		// editText1.setText(listItem);
		// lpw.dismiss();
		// }
		// });
		// lpw.show();

	}

	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		if (id == 5) {
			boolean doCheer = getIntent().getBooleanExtra("cheer", true);
			final String[] listItems;
			if (doCheer) {
				listItems = getResources().getStringArray(R.array.cheerTemplates);//Constants.cheerTemplates; // TODO: replace!!!!
			} else {
				listItems = getResources().getStringArray(R.array.tauntTemplates); // TODO: replace!!!
			}
			final boolean[] itemsChecked = new boolean[listItems.length];
			for (int j = 0; j < itemsChecked.length; j++) {
				itemsChecked[j] = false;
			}
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(getResources().getString(R.string.pick_a_template));
			// builder.setItems(listItems, new DialogInterface.OnClickListener()
			// {
			builder.setNegativeButton(getResources().getString(R.string.cancel_button),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub

						}
					}).setMultiChoiceItems(listItems, null,
					new DialogInterface.OnMultiChoiceClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which,
								boolean isChecked) {
							// The 'which' argument contains the index position
							// of the selected item
							if (isChecked) {
								editText1.setText(listItems[which]);
								dismissDialog(5);
							}
							((AlertDialog) dialog).getListView()
									.setItemChecked(which, false);

						}
					});

			return builder.create();
		} else {
			return super.onCreateDialog(id, args);
		}
	}

	public void displayBuddyChangeDialog(final String message) {

		if (!isDisplayingBuddyDialog) {
			isDisplayingBuddyDialog = true;
			showProgressDialog(false);
			SharedPreferences prefs2 = getSharedPreferences(
					Constants.PROPERTIES_NAME, MODE_PRIVATE);
			Constants.internalReset(MessagesActivity.this,prefs2);
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
						MessagesActivity.this,
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

				Constants.internalReset(MessagesActivity.this,prefs2);
				Constants.externalReset();
				
				startActivity(i_settings);
				
			}
		}
	}
	
	/*
	public void displayBuddyChangeDialog(final String message) {

		if (!isDisplayingBuddyDialog) {
			isDisplayingBuddyDialog = true;
			showProgressDialog(false);

			// Addition: so avoid the remnants
			Constants.internalReset();
			Constants.externalReset();
			
			AlertDialog.Builder builder;
			builder = new AlertDialog.Builder(MessagesActivity.this);
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
											MessagesActivity.this,
											SettingsActivity.class);

									i_settings.putExtra("RequestAccepted", -1);
									i_settings.putExtra("message", message);
									i_settings.putExtra("removalSeen", -99);
									Constants.COUNTER_INVITE = false;
									Constants.INVITATION_ACCEPTED = false;
									Constants.INVITATION_SENT = false;
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

}