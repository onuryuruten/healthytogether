package ch.epfl.hci.healthytogether;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
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
import ch.epfl.hci.healthytogether.communication.ServerHelper.CheckBadgesTask;
import ch.epfl.hci.healthytogether.communication.ServerHelper.CheckMessagesTask;
import ch.epfl.hci.healthytogether.communication.ServerHelper.RetrieveAllMessagesTask;
import ch.epfl.hci.healthytogether.communication.ServerHelper.RetrieveBadgesTask;
import ch.epfl.hci.healthytogether.communication.ServerHelper.SendMessageTask;
import ch.epfl.hci.healthytogether.model.BadgeArray;
import ch.epfl.hci.healthytogether.model.BadgeResult;
import ch.epfl.hci.healthytogether.model.MessageMeta;


@SuppressLint("NewApi")
public class BadgesActivity extends Activity {
	private static final String TAG = BadgesActivity.class.getSimpleName();

	private BadgeArrayAdapter adapter;
	// private ArrayList<OneLog> modifiedItems;
	// private ArrayList<Integer> modifiedItemIndices;

	private ListView mListView;
	// private EditText editText1;
	ArrayList<String> list_name;
	private Handler checkBadgesHandler;

	private Runnable checkForBadgesTask = new Runnable() {

		@Override
		public void run() {
			Log.e(TAG, "**** RUNNING BADGES CHECKER");
			loadExistingBadges();
			// schedule next check
			checkBadgesHandler
					.postDelayed(this, Constants.BADGE_CHECK_INTERVAL);
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.activity_badges);
		checkBadgesHandler = new Handler();
		// Constants.modifiedItems = new ArrayList<OneLog>();
		// Constants.modifiedItemIndices = new ArrayList<Integer>();
		mListView = (ListView) findViewById(R.id.badgesListView1);

		// adapter = new LogArrayAdapter(getApplicationContext(),
		// R.layout.listitem_log);
		// adapter = new LogArrayAdapter(this, R.layout.listitem_log,
		// dataItems);
		adapter = new BadgeArrayAdapter(this, R.layout.listitem_badges);
		mListView.setAdapter(adapter);
		mListView.setItemsCanFocus(true);
		Toast.makeText(this, "Getting your badges...", Toast.LENGTH_SHORT)
				.show();
	}

	public void makeInfo(int pos) {
		Log.i("makeInfo", "=" + pos);
	}

	@Override
	protected void onResume() {
		super.onResume();
		/*
		 * start the loop to check for new messages and immediately display them
		 * this is separate from the CheckForMessageService that runs all the
		 * time.
		 */
		checkBadgesHandler.postDelayed(checkForBadgesTask, 0); // we immediately
																// check for new
																// messages
	}

	@Override
	protected void onPause() {
		super.onPause();

		// need to stop the message poling loop
		Log.e(TAG, "*** STOPPING LOG POLLING");
		checkBadgesHandler.removeCallbacks(checkForBadgesTask);
	}

	private void loadExistingBadges() {
		int uid = AppContext.getInstance().getUserId();
		CheckBadgesTask task = new CheckBadgesTask(uid) {

			@Override
			protected void onPostExecute(ArrayList<BadgeArray> result) {
				if (result.size() > 0) {
					addExistingBadges(result);
				}
			}

		};
		task.execute();

	}

	private void addExistingBadges(ArrayList<BadgeArray> badgearrs) {
		adapter = new BadgeArrayAdapter(this, R.layout.listitem_badges);
		mListView.setAdapter(adapter);

		int uid = AppContext.getInstance().getUserId();
		boolean atLeastOneIteration = false;

		ArrayList<OneDayBadges> dayBadges;// = new ArrayList<OneDayBadges>();
		// ArrayList<String> buddyDates = new ArrayList<String>();
		// ArrayList<Integer> buddyMaxVals = new ArrayList<Integer>();
		ArrayList<String> dates = new ArrayList<String>();
		ArrayList<Integer> maxVals = new ArrayList<Integer>();
		ArrayList<Integer> eids = new ArrayList<Integer>();

		for (BadgeArray badgearr : badgearrs) {
			dayBadges = badgearr.getBadgesList();

			if (dayBadges.isEmpty())
				continue;

			for (int i = 0; i < dayBadges.size(); i++) {
				OneDayBadges bd = dayBadges.get(i);
				String date = bd.date;
				int badgeIndex = bd.getLargestBadge();
				int userId = bd.uid;

				if (dates.contains(date)) {
					int ind = dates.indexOf(date);
					if (maxVals.get(ind) < badgeIndex) {
						maxVals.set(ind, badgeIndex);
						eids.set(ind, bd.getLogId());
					}

				} else {
					dates.add(date);
					maxVals.add(badgeIndex);
					eids.add(bd.getLogId());
				}

			}

		}

		for (int i = 0; i < dates.size(); i++) {
			// TODO: complete
			atLeastOneIteration = true;
			OneDayBadges bubble = new OneDayBadges(uid, dates.get(i),
					eids.get(i));
			bubble.activateBadges(maxVals.get(i));
			adapter.add(bubble);
			Log.d(TAG, "Added the badges for: " + dates.get(i));

		}

		if (atLeastOneIteration) {
			// Log.e(TAG, "At least one message or log was already parsed.");
			adapter.notifyDataSetChanged();
		}

	}

	// TODO: modify or remove?
	private void scrollToBottom() {
		mListView.post(new Runnable() {
			@Override
			public void run() {
				mListView.setSelection(adapter.getCount() - 1);
			}
		});
	}

}