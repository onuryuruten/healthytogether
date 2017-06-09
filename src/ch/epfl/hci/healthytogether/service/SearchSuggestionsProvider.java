package ch.epfl.hci.healthytogether.service;

import java.util.ArrayList;
import java.util.List;

import ch.epfl.hci.healthytogether.Constants;
import ch.epfl.hci.healthytogether.UserEntry;

import android.app.SearchManager;
import android.content.ContentValues;
import android.content.SearchRecentSuggestionsProvider;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;

public class SearchSuggestionsProvider extends SearchRecentSuggestionsProvider {
	static final String TAG = SearchSuggestionsProvider.class.getSimpleName();
	public static final String AUTHORITY = SearchSuggestionsProvider.class
			.getName();
	public static final int MODE = DATABASE_MODE_QUERIES | DATABASE_MODE_2LINES;
	private static final String[] COLUMNS = {
			"_id", // must include this column
			SearchManager.SUGGEST_COLUMN_TEXT_1,
			SearchManager.SUGGEST_COLUMN_TEXT_2,
			SearchManager.SUGGEST_COLUMN_INTENT_DATA,
			SearchManager.SUGGEST_COLUMN_INTENT_ACTION,
			SearchManager.SUGGEST_COLUMN_SHORTCUT_ID };

	public SearchSuggestionsProvider() {
		setupSuggestions(AUTHORITY, MODE);
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		String query = selectionArgs[0];
		if (query == null || query.length() == 0) {
			return null;
		}

		MatrixCursor cursor = new MatrixCursor(COLUMNS);

		try {
			List<UserEntry> list = callmyservice(query); // TODO: filter wrt the
															// query
			int n = 0;
			for (UserEntry obj : list) {
				if (obj.getUserName() != null) {
					cursor.addRow(createRow(new Integer(n), obj.getUserEmail(),
							obj.getUserName(), query));
					n++;
				}
			}
		} catch (Exception e) {
			Log.e(TAG, "Failed to lookup " + query, e);
		}
		return cursor;
	}

	/*
	 * TODO: resolve this into "querying in the list of users". Current Idea:
	 * load the list of all users when "InviteBuddyActivity" is called (Use the
	 * ServerHelper-Task pattern)
	 */
	private List<UserEntry> callmyservice(String query) {
		ArrayList<UserEntry> userEntries = new ArrayList<UserEntry>();
		String currentName = "";
		String currentEmail = "";

		for (int i = 0; i < Constants.userEntries.size(); i++) {
			currentName = Constants.userEntries.get(i).getUserName();
			currentEmail = Constants.userEntries.get(i).getUserEmail();
			if (currentName != null) {
				if (currentName.toLowerCase().contains(query.toLowerCase())
						|| currentEmail.toLowerCase().contains(
								query.toLowerCase())) {
					userEntries.add(new UserEntry(currentName, currentEmail));
				}
			}
		}

		// TODO Auto-generated method stub
		return userEntries;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		throw new UnsupportedOperationException();
	}

	private Object[] createRow(Integer id, String text1, String text2,
			String name) {
		return new Object[] { id, // _id
				text1, // text1
				text2, // text2
				text1, "android.intent.action.SEARCH", // action
				SearchManager.SUGGEST_NEVER_MAKE_SHORTCUT };
	}

}