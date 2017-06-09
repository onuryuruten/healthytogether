package ch.epfl.hci.healthytogether;

import java.util.ArrayList;

import ch.epfl.hci.healthytogether.communication.ServerHelper.GetListOfUsersTask;

import android.app.Activity;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class SearchableActivity extends ListActivity {
	private static final String TAG = SearchableActivity.class.getSimpleName();

	ArrayAdapter<String> adapter;
	ArrayList<Integer> validUsers;
	boolean atLeastOneItem;

	public SearchableActivity() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		adapter = new ArrayAdapter<String>(SearchableActivity.this,
				android.R.layout.simple_list_item_1);
		validUsers = new ArrayList<Integer>();
		// setContentView(R.layout.search_activity);
		atLeastOneItem = false;
		/*GetListOfUsersTask glut = new GetListOfUsersTask() {

			@Override
			protected void onPostExecute(Boolean result) {
				// Toast.makeText(InviteBuddyActivity.this,
				// "This is nicely working", Toast.LENGTH_LONG).show();

			}
		};

		glut.execute();*/

		this.setDefaultKeyMode(Activity.DEFAULT_KEYS_SEARCH_LOCAL);

		final Intent queryIntent = getIntent();

		final String queryAction = queryIntent.getAction();
		if (Intent.ACTION_SEARCH.equals(queryAction)) {
			// Toast.makeText(SearchableActivity.this,
			// "@Searchable: Action_Search caught", Toast.LENGTH_LONG).show();
			addSelection(queryIntent);
			// this.doSearchQuery(queryIntent);
		} else if (Intent.ACTION_VIEW.equals(queryAction)) {
			Toast.makeText(SearchableActivity.this,
					"@Searchable: Action_View caught", Toast.LENGTH_LONG)
					.show();
			addSelection(queryIntent);
			// this.doView(queryIntent);
		} else {
			String queryString = queryIntent.getStringExtra("user_query");
			Log.d(TAG, "Create intent NOT from search");

			if (queryString != null) {
				String currentName = "";
				String currentEmail = "";

				// Log.d(TAG, "@Searchable onCreate: Adding moods with filter");
				for (int i = 0; i < Constants.userEntries.size(); i++) {
					currentName = Constants.userEntries.get(i).getUserName();
					currentEmail = Constants.userEntries.get(i).getUserEmail();
					if (currentName != null) {
						if (currentName.toLowerCase().contains(
								queryString.toLowerCase())
								|| currentEmail.toLowerCase().contains(
										queryString.toLowerCase())) {
							adapter.add(Constants.userEntries.get(i)
									.getUserName()
									+ " ("
									+ Constants.userEntries.get(i)
											.getUserEmail() + ")");
							atLeastOneItem = true;
							validUsers.add(i);
						}

					}
				}

				if (!atLeastOneItem) {
					for (int i = 0; i < Constants.userEntries.size(); i++) {
						adapter.add(Constants.userEntries.get(i).getUserName()
								+ " ("
								+ Constants.userEntries.get(i).getUserEmail()
								+ ")");
						validUsers.add(i);
					}
					Toast.makeText(SearchableActivity.this,
							"No user matches your query. Listing all users...",
							Toast.LENGTH_LONG).show();
				}
			} else {
				// Log.d(TAG, "@Searchable onCreate: Adding all the moods");
				for (int i = 0; i < Constants.userEntries.size(); i++) {
					adapter.add(Constants.userEntries.get(i).getUserName()
							+ " ("
							+ Constants.userEntries.get(i).getUserEmail() + ")");
					validUsers.add(i);
				}
			}
		}

		setListAdapter(adapter);
		startSearch("", false, null, false);

	}

	@Override
	public void onNewIntent(final Intent queryIntent) {
		super.onNewIntent(queryIntent);
		final String queryAction = queryIntent.getAction();
		if (Intent.ACTION_SEARCH.equals(queryAction)) {
			// Toast.makeText(SearchableActivity.this,
			// "@Searchable: doSearchQuery", Toast.LENGTH_LONG).show();
			addSelection(queryIntent);
			// this.doSearchQuery(queryIntent);
		} else if (Intent.ACTION_VIEW.equals(queryAction)) {
			this.doView(queryIntent);
		}
	}

	private void addSelection(final Intent queryIntent) {
		String queryString = queryIntent.getDataString();// getDataString(); //
															// from suggestions

		if (queryString == null) {
			queryString = queryIntent.getStringExtra(SearchManager.QUERY);
			Intent i = new Intent(SearchableActivity.this,
					SearchableActivity.class);
			i.putExtra("user_query", queryString);
			i.setAction(Intent.ACTION_VIEW);
			startActivity(i);
			this.finish();
		} else if (queryString != null) {
			Constants.SELECTED_EMAIL = queryString;
			Intent i = new Intent(SearchableActivity.this,
					InviteBuddyActivity.class);
			i.putExtra("selectedInviteeAddress", queryString);
			i.setAction(Intent.ACTION_SEARCH);
			startActivity(i);
			this.finish();
		}

	}

	private void doSearchQuery(final Intent queryIntent) {
		String queryString = queryIntent.getDataString(); // from suggestions
		if (queryString == null) {
			queryString = queryIntent.getStringExtra(SearchManager.QUERY); // from
																			// search-bar
		}

		// display results here
		// bundle.putString("user_query", queryString);
		/*
		 * queryIntent.setData(Uri.fromParts("", "", queryString));
		 * queryIntent.setAction(Intent.ACTION_SEARCH);
		 * queryIntent.putExtra("user_query", queryString);
		 * //queryIntent.putExtras(bundle); startActivity(queryIntent);
		 */
	}

	private void doView(final Intent queryIntent) {
		/*
		 * String queryString = queryIntent.getDataString(); // from suggestions
		 * if (queryString == null) { queryString =
		 * queryIntent.getStringExtra(SearchManager.QUERY); // from search-bar }
		 */
		String queryString = queryIntent.getStringExtra("user_query");
		// Toast.makeText(SearchableActivity.this,
		// "@Searchable: doView with data: " + queryString,
		// Toast.LENGTH_LONG).show();
		Uri uri = queryIntent.getData();
		// String action = queryIntent.getAction();
		Intent intent = new Intent(SearchableActivity.this,
				SearchableActivity.class);
		intent.putExtra("user_query", queryString);
		intent.setData(uri);
		startActivity(intent);
		this.finish();
	}

	// TODO: complete
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);

		String selection = Constants.userEntries.get(validUsers.get(position))
				.getUserEmail();// adapter.getItem(position); // TODO: arrange
								// so that the email is selected
		Toast.makeText(SearchableActivity.this,
				"You have chosen: " + selection, Toast.LENGTH_LONG).show();
		Intent i = new Intent(SearchableActivity.this,
				InviteBuddyActivity.class);
		i.putExtra("selectedInviteeAddress", selection);
		Constants.SELECTED_EMAIL = selection;
		i.setAction(Intent.ACTION_SEARCH);
		startActivity(i);
		this.finish();
	}

}