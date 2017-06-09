package ch.epfl.hci.healthytogether;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import ch.epfl.hci.happytogether.R;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SedentaryCategory extends ListActivity {

	List<String> cCategory;
	MyCustomAdapter mAdapter;
	ListAdapter strAdapter;
	private static final String TAG = SedentaryCategory.class.toString();

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		catList(); // Loads list
		setListAdapter(mAdapter);// new ArrayAdapter<String>(this,
									// R.layout,cCategory));
		// setListAdapter(new
		// ArrayAdapter<String>(this,R.layout.simple_list_item,Constants.sedentaryActivities));//new
		// ArrayAdapter<String>(this, R.layout,cCategory));
		// strAdapter = new
		// ListAdapter(this,R.layout.simple_list_item,Constants.sedentaryActivities));//new
		// ArrayAdapter<String>(this, R.layout,cCategory));

	}

	public void onResume() {
		super.onResume();
		setListAdapter(mAdapter);
	}

	public void catList() {
		mAdapter = new MyCustomAdapter();
		/*for (int i = 0; i < Constants.sedentaryActivities.length; i++) {
			mAdapter.addItem(Constants.sedentaryActivities[i]);
		}*/

	}

	// Adapter Class
	private class MyCustomAdapter extends BaseAdapter {

		private static final int TYPE_ITEM = 0;
		private static final int TYPE_SEPARATOR = 1;
		private static final int TYPE_MAX_COUNT = TYPE_SEPARATOR + 1;

		private ArrayList<String> mData = new ArrayList<String>();
		private LayoutInflater mInflater;

		private TreeSet<Integer> mSeparatorsSet = new TreeSet<Integer>();

		public MyCustomAdapter() {
			mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		public void addItem(final String item) {
			mData.add(item);
			notifyDataSetChanged();
		}

		public void addSeparatorItem(final String item) {
			mData.add(item);
			// save separator position
			mSeparatorsSet.add(mData.size() - 1);
			notifyDataSetChanged();
		}

		@Override
		public int getItemViewType(int position) {
			return mSeparatorsSet.contains(position) ? TYPE_SEPARATOR
					: TYPE_ITEM;
		}

		@Override
		public int getViewTypeCount() {
			return TYPE_MAX_COUNT;
		}

		public int getCount() {
			return mData.size();
		}

		public String getItem(int position) {
			return mData.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			int type = getItemViewType(position);
			System.out.println("getView " + position + " " + convertView
					+ " type = " + type);
			if (convertView == null) {
				holder = new ViewHolder();
				switch (type) {
				case TYPE_ITEM:
					convertView = mInflater.inflate(R.layout.item1, null);
					holder.textView = (TextView) convertView
							.findViewById(R.id.text);
					break;
				case TYPE_SEPARATOR:
					convertView = mInflater.inflate(R.layout.item2, null);
					holder.textView = (TextView) convertView
							.findViewById(R.id.textSeparator);
					break;
				}
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.textView.setText(mData.get(position));
			return convertView;
		}

	}

	public static class ViewHolder {
		public TextView textView;
	}

}
