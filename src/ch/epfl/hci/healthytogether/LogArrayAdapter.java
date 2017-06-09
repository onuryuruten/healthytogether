package ch.epfl.hci.healthytogether;

import java.util.ArrayList;
import java.util.List;

import ch.epfl.hci.happytogether.R;
import ch.epfl.hci.healthytogether.communication.ServerHelper.ChangeLogTask;
import ch.epfl.hci.healthytogether.communication.ServerHelper.RemoveLogTask;
import ch.epfl.hci.healthytogether.communication.ServerHelper.SendMessageTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.text.Spannable;
import android.text.style.BackgroundColorSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListPopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

public class LogArrayAdapter extends ArrayAdapter<OneLog> {

	private Activity activity;
	private int resource;
	private LayoutInflater inflater = null;

	private Spinner mLogActivity;
	private ImageView moodView;
	private ImageView socialView;
	private ImageView deleteView;

	private TextView mLogTime;
	private TextView moodText;
	private TextView socialText;

	private List<OneLog> myLogs = new ArrayList<OneLog>();
	private LinearLayout wrapper;

	
	String [] foodTemplates;
	String [] exerciseTemplates;
	String [] sedentaryTemplates;
	String [] allActivityTemplates;
	String [] positiveTemplates;
	String [] negativeTemplates;
	String [] moodIntensityTemplates;
	String [] allMoodTemplates;
	String [] socialTemplates;	
	
	
	static final int TIME_DIALOG_ID = 999;

	@Override
	public void add(OneLog object) {
		myLogs.add(object);
		super.add(object);
	}

	public LogArrayAdapter(Activity _activity, int _resource,
			List<OneLog> _items) {
		super(_activity, _resource, _items);
		inflater = (LayoutInflater) _activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		// i always do this way, but i dont think this is the error
		// inflater = LayoutInflater.from(_activity.getBaseContext());
		resource = _resource;
		activity = _activity;
		
		foodTemplates = activity.getResources().getStringArray(R.array.foodTemplates);
		exerciseTemplates = activity.getResources().getStringArray(R.array.exerciseTemplates);
		sedentaryTemplates = activity.getResources().getStringArray(R.array.sedentaryTemplates);
		allActivityTemplates = activity.getResources().getStringArray(R.array.allActivityTemplates);
		positiveTemplates = activity.getResources().getStringArray(R.array.positiveMoodTemplates);
		negativeTemplates = activity.getResources().getStringArray(R.array.negativeMoodTemplates);
		allMoodTemplates = activity.getResources().getStringArray(R.array.allMoodTemplates);
		socialTemplates = activity.getResources().getStringArray(R.array.socialTemplates);
		moodIntensityTemplates = activity.getResources().getStringArray(R.array.moodIntensityTemplates);		
	}

	public LogArrayAdapter(Activity _activity, int _resource) {
		super(_activity, _resource);
		inflater = (LayoutInflater) _activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		// i always do this way, but i dont think this is the error
		// inflater = LayoutInflater.from(_activity.getBaseContext());
		resource = _resource;
		activity = _activity;
		
		foodTemplates = activity.getResources().getStringArray(R.array.foodTemplates);
		exerciseTemplates = activity.getResources().getStringArray(R.array.exerciseTemplates);
		sedentaryTemplates = activity.getResources().getStringArray(R.array.sedentaryTemplates);
		allActivityTemplates = activity.getResources().getStringArray(R.array.allActivityTemplates);
		positiveTemplates = activity.getResources().getStringArray(R.array.positiveMoodTemplates);
		negativeTemplates = activity.getResources().getStringArray(R.array.negativeMoodTemplates);
		allMoodTemplates = activity.getResources().getStringArray(R.array.allMoodTemplates);
		socialTemplates = activity.getResources().getStringArray(R.array.socialTemplates);
		moodIntensityTemplates = activity.getResources().getStringArray(R.array.moodIntensityTemplates);
		
	}

	public LogArrayAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
		
		foodTemplates = activity.getResources().getStringArray(R.array.foodTemplates);
		exerciseTemplates = activity.getResources().getStringArray(R.array.exerciseTemplates);
		sedentaryTemplates = activity.getResources().getStringArray(R.array.sedentaryTemplates);
		allActivityTemplates = activity.getResources().getStringArray(R.array.allActivityTemplates);
		positiveTemplates = activity.getResources().getStringArray(R.array.positiveMoodTemplates);
		negativeTemplates = activity.getResources().getStringArray(R.array.negativeMoodTemplates);
		allMoodTemplates = activity.getResources().getStringArray(R.array.allMoodTemplates);
		socialTemplates = activity.getResources().getStringArray(R.array.socialTemplates);
		moodIntensityTemplates = activity.getResources().getStringArray(R.array.moodIntensityTemplates);		
	}

	public int getCount() {
		return this.myLogs.size();
	}

	public OneLog getItem(int index) {
		return this.myLogs.get(index);
	}

	public List<OneLog> getAllMessages() {
		return myLogs;
	}

	public static class ViewHolder {
		/*
		 * TextView carYear; TextView carMake; TextView carModel; TextView
		 * carColor; TextView assetTag;
		 */
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View intermediateRow = convertView;
		final View row;
		final int pos;
		if (intermediateRow == null) {
			LayoutInflater inflater = (LayoutInflater) this.getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			row = inflater.inflate(R.layout.listitem_log, parent, false);
		} else {
			row = convertView;
		}

		wrapper = (LinearLayout) row.findViewById(R.id.hWrapper);

		pos = position;
		final OneLog log = getItem(position);
		
		//log.translateToNative(positiveTemplates, negativeTemplates, foodTemplates, sedentaryTemplates, exerciseTemplates, socialTemplates, moodIntensityTemplates, activity.getResources().getString(R.string.none));
		
		/*if (log == null) {
			System.out.println("log is null");
		}*/

		final int uid = AppContext.getInstance().getUserId();
		final int fid = uid;
		
		
		final String msg = log.toDBEntry(positiveTemplates,
				negativeTemplates, foodTemplates,
				sedentaryTemplates,exerciseTemplates,
				socialTemplates,moodIntensityTemplates,activity.getResources().getString(R.string.none));
		
		//log.translateToNative(positiveTemplates, negativeTemplates, foodTemplates, sedentaryTemplates, exerciseTemplates, socialTemplates);

		String logMinute;
		mLogActivity = (Spinner) row.findViewById(R.id.activityLabel);
		
		/*if (mLogActivity == null) {
			System.out.println("mLogActivity is null");
		}*/
		
		mLogTime = (TextView) row.findViewById(R.id.logTime); // TODO: complete
																// binding

		TextView fakeLogTime = (TextView) row.findViewById(R.id.logTimeX); // TODO:
																			// complete
																			// binding
		/*if (mLogTime == null) {
			System.out.println("mLogTime is null");
		}*/

		// TODO: add the
		moodView = (ImageView) row.findViewById(R.id.hMood);
		deleteView = (ImageView) row.findViewById(R.id.logDeleteImage);

		/*if (moodView == null) {
			System.out.println("moodView is null");
		}*/

		socialView = (ImageView) row.findViewById(R.id.hSocial);

		/*if (socialView == null) {
			System.out.println("socialView is null");
		}*/

		moodText = (TextView) row.findViewById(R.id.hTextMood);

		/*if (moodText == null) {
			System.out.println("moodText is null");
		}*/

		socialText = (TextView) row.findViewById(R.id.hSocialText);

		/*if (socialText == null) {
			System.out.println("socialText is null");
		}*/

		String moodDisplayStr = log.mood;

		if (log.mood.contains(activity.getResources().getString(R.string.normal))) {
			moodDisplayStr = (log.mood.substring(0, log.mood.indexOf(activity.getResources().getString(R.string.normal))));
		} else if (log.mood.contains(activity.getResources().getString(R.string.mild))) {
			moodDisplayStr = (log.mood.substring(0, log.mood.indexOf(activity.getResources().getString(R.string.mild))))
					.toLowerCase();
		} else if (log.mood.contains(activity.getResources().getString(R.string.strong))) {
			moodDisplayStr = (log.mood.substring(0, log.mood.indexOf(activity.getResources().getString(R.string.strong))))
					.toUpperCase() + "!";
		} else if (log.mood == null) {
			moodDisplayStr = activity.getResources().getString(R.string.none);
		}

		moodText.setText(moodDisplayStr);

		if (log.social == null) {
			socialText.setText(activity.getResources().getString(R.string.none));
		} else {
			socialText.setText(log.social);
		}
		//TODO: ....
		ArrayAdapter<String> dataAdapter;
		int activityIndex = allActivityTemplates.length - 1;

		if (!log.foodSet) {
			dataAdapter = new ArrayAdapter<String>(activity,
					android.R.layout.simple_spinner_item,
					allActivityTemplates);
			for (int i = 0; i < allActivityTemplates.length; i++) {
				if (log.activity.equals(allActivityTemplates[i])) {
					activityIndex = i;
					break;
				}
			}
		} else {
			dataAdapter = new ArrayAdapter<String>(activity,
					android.R.layout.simple_spinner_item,
					foodTemplates);
			for (int i = 0; i < foodTemplates.length; i++) {
				if (log.activity.equals(foodTemplates[i])) {
					activityIndex = i;
					break;
				}
			}
		}

		dataAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mLogActivity.setTag("" + position);
		mLogActivity.setAdapter(dataAdapter);

		mLogActivity
				.setOnItemSelectedListener((OnItemSelectedListener) activity);
		mLogActivity.setSelection(activityIndex);

		deleteView.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				RemoveLogTask task = new RemoveLogTask(uid, fid, msg) {

					@Override
					protected void onPostExecute(Boolean success) {
						if (success) {
							// if(!msg.isEmpty()){
							// add the message to the view (user's msg appear on
							// the right)
							// adapter.add(new OneComment(false,
							// editText1.getText().toString()));
							// editText1.setText("");
							// scrollToBottom();
							// }

							((HistoryActivity) activity).removeLog(log
									.getLogId());
							myLogs.remove(log);
							notifyDataSetChanged();
							Toast.makeText(activity, activity.getResources().getString(R.string.log_deleted),
									Toast.LENGTH_SHORT).show();
						} else {
							Toast.makeText(
									activity,
									activity.getResources().getString(R.string.connection_toast_message),
									Toast.LENGTH_SHORT).show();
						}
					}
				};
				task.execute();
			}
		});

		socialView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				final TextView socText = (TextView) row
						.findViewById(R.id.hSocialText);

				// 1. Instantiate an AlertDialog.Builder with its constructor
				AlertDialog.Builder builder = new AlertDialog.Builder(activity);

				// 2. Chain together various setter methods to set the dialog
				// characteristics
				builder.setTitle(activity.getResources().getString(R.string.social_title)).setItems(
						socialTemplates,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface arg0,
									int position) {
								log.social = socialTemplates[position];
								socText.setText(log.social);
								final String newMsg = log.toDBEntry(positiveTemplates,
										negativeTemplates, foodTemplates,
										sedentaryTemplates,exerciseTemplates,
										socialTemplates,moodIntensityTemplates,activity.getResources().getString(R.string.none));
								ChangeLogTask task = new ChangeLogTask(uid,
										fid, msg, newMsg) {

									@Override
									protected void onPostExecute(Boolean success) {
										if (success) {
											// if(!msg.isEmpty()){
											// add the message to the view
											// (user's msg appear on the right)
											// adapter.add(new OneComment(false,
											// editText1.getText().toString()));
											// editText1.setText("");
											// scrollToBottom();
											// }

											// ((HistoryActivity)activity).removeLog(log.getLogId());
											// Toast.makeText(activity,
											// "Social info changed to " +
											// log.social + ", please wait",
											// Toast.LENGTH_SHORT).show();
										} else {
											Toast.makeText(
													activity,
													activity.getResources().getString(R.string.connection_toast_message),
													Toast.LENGTH_SHORT).show();
										}
									}
								};
								task.execute();
							}

						});

				// 3. Get the AlertDialog from create()
				AlertDialog dialog = builder.create();
				dialog.show();

			}
		});

		moodView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				final MoodArrayAdapter maa = new MoodArrayAdapter(activity
						.getApplicationContext(), R.layout.listitem_mood,
						activity,activity.getResources().getStringArray(R.array.moodIntensityTemplates));
				MoodArrayAdapter.selectedItemIndex = -1;
				MoodArrayAdapter.selectedRB = -1;
				maa.setHackable();

				for (int i = 0; i < allMoodTemplates.length; i++) {
					maa.add(new OneMood(i,allMoodTemplates[i],activity.getResources().getString(R.string.normal)));
				}

				// lw.setAdapter(maa);
				AlertDialog.Builder builder = new AlertDialog.Builder(activity);
				builder.setTitle(activity.getResources().getString(R.string.mood_title))
						.setAdapter(maa, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								if (MoodArrayAdapter.selectedItemIndex >= 0) {
									OneMood md = maa
											.getItem(MoodArrayAdapter.selectedItemIndex);
									try {
										String listItem = md.getMoodString();
										if (md.getMoodIntensity()
												.equals(activity.getResources().getString(R.string.mild)))
											listItem = md.getMoodString()
													.toLowerCase();// selectedItem.get(0).toLowerCase();
										else if (md.getMoodIntensity().equals(
												activity.getResources().getString(R.string.strong)))
											listItem = md.getMoodString()
													.toUpperCase() + "!";

										final TextView mdText = (TextView) row
												.findViewById(R.id.hTextMood);
										mdText.setText(listItem);
										// mLogActivity.set
										log.mood = md.getMoodString()
												+ md.getMoodIntensity();

										final String newMsg = log.toDBEntry(positiveTemplates,
												negativeTemplates, foodTemplates,
												sedentaryTemplates,exerciseTemplates,
												socialTemplates,moodIntensityTemplates,activity.getResources().getString(R.string.none));
										ChangeLogTask task = new ChangeLogTask(
												uid, fid, newMsg, msg) {

											@Override
											protected void onPostExecute(
													Boolean success) {
												if (success) {
												} else {
													Toast.makeText(
															activity,
															activity.getResources().getString(R.string.connection_toast_message),
															Toast.LENGTH_SHORT)
															.show();
												}
											}
										};
										task.execute();
									} catch (NullPointerException ex) {
										Log.e("log_modify", ex.getMessage());
									}
									MoodArrayAdapter.selectedItemIndex = -1;
									dialog.dismiss();
								}

							}

						})
						.setOnCancelListener(
								new DialogInterface.OnCancelListener() {

									@Override
									public void onCancel(DialogInterface dialog) {
										if (MoodArrayAdapter.selectedItemIndex >= 0) {
											OneMood md = maa
													.getItem(MoodArrayAdapter.selectedItemIndex);
											try {
												String listItem = md
														.getMoodString();
												if (md.getMoodIntensity()
														.equals(activity.getResources().getString(R.string.mild)))
													listItem = md
															.getMoodString()
															.toLowerCase();// selectedItem.get(0).toLowerCase();
												else if (md.getMoodIntensity()
														.equals(activity.getResources().getString(R.string.strong)))
													listItem = md
															.getMoodString()
															.toUpperCase()
															+ "!";

												final TextView mdText = (TextView) row
														.findViewById(R.id.hTextMood);
												mdText.setText(listItem);
												// mLogActivity.set
												log.mood = md.getMoodString()
														+ md.getMoodIntensity();

												final String newMsg = log.toDBEntry(positiveTemplates,
														negativeTemplates, foodTemplates,
														sedentaryTemplates,exerciseTemplates,
														socialTemplates,moodIntensityTemplates,activity.getResources().getString(R.string.none));
												
												ChangeLogTask task = new ChangeLogTask(
														uid, fid, newMsg, msg) {

													@Override
													protected void onPostExecute(
															Boolean success) {
														if (success) {
														} else {
															Toast.makeText(
																	activity,
																	activity.getResources().getString(R.string.connection_toast_message),
																	Toast.LENGTH_SHORT)
																	.show();
														}
													}
												};
												task.execute();
											} catch (NullPointerException ex) {
												Log.e("log_modify",
														ex.getMessage());
											}
											MoodArrayAdapter.selectedItemIndex = -1;
										}

									}
								});

				// 3. Get the AlertDialog from create()
				Constants.dialog = builder.create();
				Constants.dialog.show();

			}
		});

		// logMinute = (log.minute < 10 ? ("0" + log.minute) : ("" +
		// log.minute));
		mLogTime.setText(log.logTime);
		fakeLogTime.setText(log.logTime);
		// TODO: modify these!
		mLogTime.setBackgroundResource(R.drawable.bubble_yellow);
		wrapper.setGravity(Gravity.LEFT);
		fakeLogTime.setBackgroundResource(R.drawable.bubble_yellow);
		// TODO: set the listeners

		// Set max width for bubbles
		DisplayMetrics displayMetrics = new DisplayMetrics();
		WindowManager wm = (WindowManager) this.getContext()
				.getApplicationContext()
				.getSystemService(Context.WINDOW_SERVICE); // the results will
															// be higher than
															// using the
															// activity context
															// object or the
															// getWindowManager()
															// shortcut
		wm.getDefaultDisplay().getMetrics(displayMetrics);
		int screenWidth = displayMetrics.widthPixels;
		TextView mMessageText = (TextView) row.findViewById(R.id.logTime); // TODO:
																			// check
																			// if
																			// legit!
		mMessageText.setMaxWidth((int) (screenWidth * 0.65));
		fakeLogTime.setMaxWidth((int) (screenWidth * 0.65));
		return row;
	}

	public Bitmap decodeToBitmap(byte[] decodedByte) {
		return BitmapFactory
				.decodeByteArray(decodedByte, 0, decodedByte.length);
	}

}