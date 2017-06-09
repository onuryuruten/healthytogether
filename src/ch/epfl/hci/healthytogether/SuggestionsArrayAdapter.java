package ch.epfl.hci.healthytogether;

import java.util.ArrayList;
import java.util.List;

import ch.epfl.hci.happytogether.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class SuggestionsArrayAdapter extends ArrayAdapter<String> {

	private TextView mMessageText;
	private List<String> mMessages = new ArrayList<String>();

	@Override
	public void add(String object) {
		mMessages.add(object);
		super.add(object);
	}

	public SuggestionsArrayAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
	}
	
	public SuggestionsArrayAdapter(Context context, int textViewResourceId, ArrayList<String> entries) 
	{
		super(context, textViewResourceId);
		
		for(int i = 0; i < entries.size(); i++)
		{
			add(entries.get(i));
		}
		
	}
	
	public SuggestionsArrayAdapter(Context context, int textViewResourceId, String[] entries) 
	{
		super(context, textViewResourceId);
		
		for(int i = 0; i < entries.length; i++)
		{
			add(entries[i]);
		}
		
	}
	
	

	public int getCount() {
		return this.mMessages.size();
	}

	public String getItem(int index) {
		return this.mMessages.get(index);
	}

	public List<String> getAllMessages() {
		return mMessages;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		if (row == null) {
			LayoutInflater inflater = (LayoutInflater) this.getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			row = inflater.inflate(R.layout.suggestion_list_item, parent, false);
		}

		//wrapper = (LinearLayout) row.findViewById(R.id.wrapper);

		String coment = getItem(position);

		mMessageText = (TextView) row.findViewById(R.id.rowTextView);
//		mMessageTimeText = (TextView) row.findViewById(R.id.commentDate);

		mMessageText.setText(coment);
//		mMessageTimeText.setText(coment.date);
//		mMessageTimeText.setTextColor(Color.BLACK);

		/*
		mMessageText
				.setBackgroundResource(coment.left ? R.drawable.bubble_yellow
						: R.drawable.bubble_orange);
		wrapper.setGravity(coment.left ? Gravity.LEFT : Gravity.RIGHT);

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
		TextView mMessageText = (TextView) row.findViewById(R.id.comment);
		mMessageText.setMaxWidth((int) (screenWidth * 0.65));*/
		return row;
	}

	public Bitmap decodeToBitmap(byte[] decodedByte) {
		return BitmapFactory
				.decodeByteArray(decodedByte, 0, decodedByte.length);
	}

}