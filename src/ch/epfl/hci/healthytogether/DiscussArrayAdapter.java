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

public class DiscussArrayAdapter extends ArrayAdapter<OneComment> {

	private TextView mMessageText;
	private TextView mMessageTimeText;
	private List<OneComment> mMessages = new ArrayList<OneComment>();
	private LinearLayout wrapper;

	@Override
	public void add(OneComment object) {
		mMessages.add(object);
		super.add(object);
	}

	public DiscussArrayAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
	}

	public int getCount() {
		return this.mMessages.size();
	}

	public OneComment getItem(int index) {
		return this.mMessages.get(index);
	}

	public List<OneComment> getAllMessages() {
		return mMessages;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		if (row == null) {
			LayoutInflater inflater = (LayoutInflater) this.getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			row = inflater.inflate(R.layout.listitem_message, parent, false);
		}

		wrapper = (LinearLayout) row.findViewById(R.id.wrapper);

		OneComment coment = getItem(position);

		mMessageText = (TextView) row.findViewById(R.id.comment);
		mMessageTimeText = (TextView) row.findViewById(R.id.commentDate);

		mMessageText.setText(coment.comment);
		mMessageTimeText.setText(coment.date);
		mMessageTimeText.setTextColor(Color.BLACK);

		mMessageText
				.setBackgroundResource(coment.left ? R.drawable.bubble_yellow
						: R.drawable.bubble_orange);
		wrapper.setGravity(coment.left ? Gravity.LEFT : Gravity.RIGHT);

		/*
		 * if(!coment.left) { LayoutParams layoutParams=new
		 * LayoutParams(mMessageTimeText.getWidth(),
		 * mMessageTimeText.getHeight());
		 * layoutParams.setMargins(wrapper.getLeft(),
		 * mMessageTimeText.getTop(),mMessageText.getRight(),
		 * mMessageTimeText.getBottom());
		 * mMessageTimeText.setLayoutParams(layoutParams);
		 * //mMessageTimeText.setLayoutParams(params); }
		 */

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
		mMessageText.setMaxWidth((int) (screenWidth * 0.65));
		return row;
	}

	public Bitmap decodeToBitmap(byte[] decodedByte) {
		return BitmapFactory
				.decodeByteArray(decodedByte, 0, decodedByte.length);
	}

}