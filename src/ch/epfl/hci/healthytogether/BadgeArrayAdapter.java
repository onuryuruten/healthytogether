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

public class BadgeArrayAdapter extends ArrayAdapter<OneDayBadges> {

	private Activity activity;
	private int resource;
	private LayoutInflater inflater = null;

	private List<OneDayBadges> myLogs = new ArrayList<OneDayBadges>();
	private LinearLayout wrapper, wrapper2;

	// TODO: complete.

	private ImageView[] badges;

	static final int TIME_DIALOG_ID = 999;

	@Override
	public void add(OneDayBadges object) {
		myLogs.add(object);
		super.add(object);
	}

	public BadgeArrayAdapter(Activity _activity, int _resource,
			List<OneDayBadges> _items) {
		super(_activity, _resource, _items);
		inflater = (LayoutInflater) _activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		// i always do this way, but i dont think this is the error
		// inflater = LayoutInflater.from(_activity.getBaseContext());
		resource = _resource;
		activity = _activity;
	}

	public BadgeArrayAdapter(Activity _activity, int _resource) {
		super(_activity, _resource);
		inflater = (LayoutInflater) _activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		// i always do this way, but i dont think this is the error
		// inflater = LayoutInflater.from(_activity.getBaseContext());
		resource = _resource;
		activity = _activity;
	}

	public BadgeArrayAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
	}

	public int getCount() {
		return this.myLogs.size();
	}

	public OneDayBadges getItem(int index) {
		return this.myLogs.get(index);
	}

	public List<OneDayBadges> getAllMessages() {
		return myLogs;
	}

	public static class ViewHolder {
		TextView carYear;
		TextView carMake;
		TextView carModel;
		TextView carColor;
		TextView assetTag;
	}

	// TODO: modify here!!!!
	public View getView(int position, View convertView, ViewGroup parent) {
		View intermediateRow = convertView;
		final View row;
		final int pos;
		if (intermediateRow == null) {
			LayoutInflater inflater = (LayoutInflater) this.getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			row = inflater.inflate(R.layout.listitem_badges, parent, false);
		} else {
			row = convertView;
		}

		wrapper = (LinearLayout) row.findViewById(R.id.bWrapper);

		pos = position;
		final OneDayBadges log = getItem(position);
		if (log == null) {
			System.out.println("log is null");
		}

		final int uid = AppContext.getInstance().getUserId();
		final int fid = AppContext.getInstance().getFriendId();

		badges = new ImageView[Constants.BADGE_COUNT];
		badges[0] = (ImageView) row.findViewById(R.id.bbadge1);
		badges[1] = (ImageView) row.findViewById(R.id.bbadge2);
		badges[2] = (ImageView) row.findViewById(R.id.bbadge3);
		badges[3] = (ImageView) row.findViewById(R.id.bbadge4);
		badges[4] = (ImageView) row.findViewById(R.id.bbadge5);
		badges[5] = (ImageView) row.findViewById(R.id.bbadge6);
		badges[6] = (ImageView) row.findViewById(R.id.bbadge7);
		badges[7] = (ImageView) row.findViewById(R.id.bbadge8);

		for (int i = 0; i < Constants.BADGE_COUNT; i++) {
			if (log.getBadge(i)) {
				// badges[i].setImageResource(R.drawable);

				switch (i) {
				case 0:
					badges[i].setImageResource(R.drawable.ta);
					break;
				case 1:
					badges[i].setImageResource(R.drawable.tb);
					break;
				case 2:
					badges[i].setImageResource(R.drawable.tc);
					break;
				case 3:
					badges[i].setImageResource(R.drawable.td);
					break;
				case 4:
					badges[i].setImageResource(R.drawable.te);
					break;
				case 5:
					badges[i].setImageResource(R.drawable.tf);
					break;
				case 6:
					badges[i].setImageResource(R.drawable.tg);
					break;
				case 7:
					badges[i].setImageResource(R.drawable.th);
					break;
				}

				badges[i].setVisibility(ImageView.VISIBLE);
			}
		}

		TextView fakeLogTime = (TextView) row.findViewById(R.id.badgeDayX);
		TextView fakeLogTime2 = (TextView) row.findViewById(R.id.badgeDay2X);
		fakeLogTime.setText(log.date);
		fakeLogTime2.setText(log.date);
		fakeLogTime.setBackgroundResource(R.drawable.bubble_yellow);
		fakeLogTime2.setBackgroundResource(R.drawable.bubble_yellow);
		wrapper.setGravity(log.uid == fid ? Gravity.LEFT : Gravity.RIGHT);

		wrapper2 = (LinearLayout) row.findViewById(R.id.bWrapper2);
		wrapper2.setGravity(log.uid == fid ? Gravity.LEFT : Gravity.RIGHT);

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
		fakeLogTime.setMaxWidth((int) (screenWidth * 0.65));
		fakeLogTime2.setMaxWidth((int) (screenWidth * 0.65));
		fakeLogTime.setVisibility(TextView.VISIBLE);
		return row;
	}

	public Bitmap decodeToBitmap(byte[] decodedByte) {
		return BitmapFactory
				.decodeByteArray(decodedByte, 0, decodedByte.length);
	}

}