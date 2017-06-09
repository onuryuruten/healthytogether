package ch.epfl.hci.healthytogether;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import ch.epfl.hci.happytogether.R;
import ch.epfl.hci.healthytogether.Main2Activity.ViewHolder;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class MoodArrayAdapter extends ArrayAdapter<OneMood> {

	private TextView moodOptionText;
	private RadioButton rb1;
	private RadioButton rb2;
	private RadioButton rb3;
	private RadioGroup rgrp;
	private List<OneMood> moodEntries = new ArrayList<OneMood>();
	private ArrayList<RadioButton> r1s = new ArrayList<RadioButton>();
	private ArrayList<Boolean> r1Set = new ArrayList<Boolean>();
	private ArrayList<RadioButton> r2s = new ArrayList<RadioButton>();
	private ArrayList<Boolean> r2Set = new ArrayList<Boolean>();
	private ArrayList<RadioButton> r3s = new ArrayList<RadioButton>();
	private ArrayList<Boolean> r3Set = new ArrayList<Boolean>();
	private ArrayList<Integer> moodInds = new ArrayList<Integer>();
	private LinearLayout wrapper;
	public static int selectedItemIndex;
	public static boolean isSelectedItemPositive;
	protected static int selectedRB = -1;

	public Activity activity;
	private OnCheckedChangeListener ocl;

	boolean hack = false;
	private ArrayList<String> mData = new ArrayList<String>();
	private TreeSet<Integer> mSeparatorsSet = new TreeSet<Integer>();
	private LayoutInflater mInflater;
	private boolean isPositiveList;
	private String[] intensityStrings;
	
	String [] positiveTemplates;
	String [] negativeTemplates;
	String [] allMoodTemplates;
	
	
	@Override
	public void add(OneMood object) {
		moodEntries.add(object);
		mData.add("");
		super.add(object);
	}

	public void addSeparatorItem(final String item) {
		// moodEntries.add(null);
		mData.add(item);
		// save separator position

		// if(moodEntries.size())
		// {
		mSeparatorsSet.add(mData.size() - 1);
		// }
		// notifyDataSetChanged();
	}

	public void setHackable() {
		hack = false;
	}

	public int size() {
		return moodEntries.size();
	}

	public MoodArrayAdapter(Context context, int textViewResourceId,
			Activity activity) {
		super(context, textViewResourceId);
		mInflater = (LayoutInflater) (activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
		this.activity = activity;
		isPositiveList = true;
		this.intensityStrings = new String[]{"Mild","Normal","Strong"};
	}
	
	public MoodArrayAdapter(Context context, int textViewResourceId,
			Activity activity,String[] intensityStrings) {
		super(context, textViewResourceId);
		mInflater = (LayoutInflater) (activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
		this.activity = activity;
		isPositiveList = true;
		this.intensityStrings = intensityStrings;
		positiveTemplates = activity.getResources().getStringArray(R.array.positiveMoodTemplates);
		negativeTemplates = activity.getResources().getStringArray(R.array.negativeMoodTemplates);
		allMoodTemplates = activity.getResources().getStringArray(R.array.allMoodTemplates);
	
	}	

	public MoodArrayAdapter(Context context, int textViewResourceId,
			Activity activity, boolean isPositive,String[] intensityStrings) {
		super(context, textViewResourceId);
		mInflater = (LayoutInflater) (activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
		this.activity = activity;
		this.isPositiveList = isPositive;
		this.intensityStrings= intensityStrings; 
		positiveTemplates = activity.getResources().getStringArray(R.array.positiveMoodTemplates);
		negativeTemplates = activity.getResources().getStringArray(R.array.negativeMoodTemplates);
		allMoodTemplates = activity.getResources().getStringArray(R.array.allMoodTemplates);
		
		
	}
	
	

	public int getCount() {
		return this.moodEntries.size();
	}

	public OneMood getItem(int index) {
		return this.moodEntries.get(index);
	}

	public OneMood getItemByID(int index) {
		for (int i = 0; i < moodEntries.size(); i++) {
			if (moodEntries.get(i).getMoodId() == index) {
				return this.moodEntries.get(i);
			}
		}
		return null;

	}

	public List<OneMood> getAllMessages() {
		return moodEntries;
	}

	public View getView(int position, View convertView, ViewGroup parent) {

		boolean inHeaders = mSeparatorsSet.contains(position);

		View row = convertView;

		if (!inHeaders) {
			// if (row == null)
			// {
			LayoutInflater inflater = (LayoutInflater) this.getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			row = inflater.inflate(R.layout.listitem_mood, parent, false);
			// }

			final int pos = position;
			final OneMood mood = getItem(position);

			moodOptionText = (TextView) row
					.findViewById(R.id.moodOptionTextView);

			rgrp = (RadioGroup) row.findViewById(R.id.moodRadioGroup);
			rgrp.clearCheck();

			mood.setRadioGroup(rgrp);

			rb1 = (RadioButton) row.findViewById(R.id.intensityRadioButton1);
			rb1.setText(intensityStrings[0]); // Mild
			rb1.setTextColor(Color.BLACK);
			rb1.setTag(mood.getMoodId() + "");
			rb1.setVisibility(RadioButton.VISIBLE);

			final String moodTag = "" + mood.getMoodId();

			rb2 = (RadioButton) row.findViewById(R.id.intensityRadioButton2);
			rb2.setText(intensityStrings[1]); // Normal
			rb2.setTextColor(Color.BLACK);
			rb2.setTag(mood.getMoodId() + "");
			rb2.setVisibility(RadioButton.VISIBLE);

			rb3 = (RadioButton) row.findViewById(R.id.intensityRadioButton3);
			rb3.setText(intensityStrings[2]); // Strong
			rb3.setTag(mood.getMoodId() + "");
			rb3.setTextColor(Color.BLACK);
			rb3.setVisibility(RadioButton.VISIBLE);

			moodInds.add(mood.getMoodId());
			/*System.out.println("Added mood: " + mood.getMoodString()
					+ ", index = " + mood.getMoodId());*/

			r1s.add(rb1);
			r1Set.add(false);
			r2s.add(rb2);
			r2Set.add(false);
			r3s.add(rb3);
			r3Set.add(false);

			rb1.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
					System.out.println("onCheckChanged story has started");

					// boolean isChecked = rb1.isChecked();
					if (!hack) {
						System.out.println("Time to hack!");
						hack = true;

						MoodArrayAdapter.selectedRB = 1;
						mood.setMoodIntensity(rb1.getText().toString());
						MoodArrayAdapter.selectedItemIndex = mood.getMoodId();
						MoodArrayAdapter.isSelectedItemPositive = isPositiveList;
						Constants.dialog.cancel();

						hack = false;
					}
				}

			});

			rb2.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton arg0, boolean arg1) {

					// boolean isChecked = rb2.isChecked();
					if (!hack) {
						hack = true;

						// MoodArrayAdapter.selectedItemIndex =
						// mood.getMoodId();
						MoodArrayAdapter.selectedRB = 2;
						mood.setMoodIntensity(rb2.getText().toString());
						MoodArrayAdapter.selectedItemIndex = mood.getMoodId();
						MoodArrayAdapter.isSelectedItemPositive = isPositiveList;
						Constants.dialog.cancel();

						hack = false;
					}

				}

			});

			rb3.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
					// MoodArrayAdapter.selectedItemIndex = mood.getMoodId();
					// mood.setMoodIntensity(rb3.getText().toString());

					// boolean isChecked = rb3.isChecked();
					if (!hack) {
						hack = true;

						// MoodArrayAdapter.selectedItemIndex =
						// mood.getMoodId();
						MoodArrayAdapter.selectedRB = 3;
						mood.setMoodIntensity(rb3.getText().toString());
						MoodArrayAdapter.selectedItemIndex = mood.getMoodId();
						MoodArrayAdapter.isSelectedItemPositive = isPositiveList;
						Constants.dialog.cancel();

						hack = false;
					}

				}

			});

			moodOptionText.setText(mood.getMoodString());
			moodOptionText.setTextColor(Color.BLACK);

			return row;
		} else {

			ViewHolder holder = null;
			// if (row == null)
			// {
			holder = new ViewHolder();
			row = mInflater.inflate(R.layout.item2, null);
			holder.textView = (TextView) row.findViewById(R.id.textSeparator);
			row.setTag(holder);
			// }
			// else
			// {
			// holder = (ViewHolder)row.getTag();
			// }
			holder.textView.setText(mData.get(position));
			return row;
		}
	}

	public static class ViewHolder {
		public TextView textView;
	}

	public Bitmap decodeToBitmap(byte[] decodedByte) {
		return BitmapFactory
				.decodeByteArray(decodedByte, 0, decodedByte.length);
	}

	public int getSelectedItemIndex() {
		return selectedItemIndex;
	}

	public void setSelectedItemIndex(int selectedItemIndex) {
		this.selectedItemIndex = selectedItemIndex;
	}

}