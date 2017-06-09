package ch.epfl.hci.healthytogether;

import ch.epfl.hci.happytogether.R;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class Avatars extends BaseAdapter {

	Context context;
	int layoutResourceId;

	Integer[] avatarID = new Integer[] { R.drawable.avatar0,
			R.drawable.avatar1, R.drawable.avatar2, R.drawable.avatar3,
			R.drawable.avatar4, R.drawable.avatar5, R.drawable.avatar6,
			R.drawable.avatar7, R.drawable.avatar8, R.drawable.avatar9,
			R.drawable.avatar10, R.drawable.avatar11, R.drawable.avatar12,
			R.drawable.avatar13, R.drawable.avatar14 };

	public Avatars(Context context, int layoutResourceId) {
		super();
		this.context = context;
		this.layoutResourceId = layoutResourceId;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return avatarID.length;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View gridView;

		if (convertView == null) {

			gridView = new View(context);

			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			gridView = inflater.inflate(layoutResourceId, null);

			ImageView imageView = (ImageView) gridView
					.findViewById(R.id.image_item);

			imageView.setImageResource(avatarID[position]);

		} else {
			gridView = (View) convertView;
		}

		return gridView;

	}

}