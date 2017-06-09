package ch.epfl.hci.healthytogether;

import ch.epfl.hci.happytogether.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

/**
 * General error handler.
 * 
 */
public class ErrorHandler {

	private static final OnClickListener mEmptyClickListener = new OnClickListener() {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			dialog.dismiss();
		}
	};

	public static ErrorHandler create() {
		return new ErrorHandler();
	}

	public void handleError(Context context, String error,
			OnClickListener listener) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(error).setTitle(R.string.general_error_dialog_title);
		if (listener == null) {
			listener = mEmptyClickListener;
		}
		builder.setPositiveButton("OK", listener);
		AlertDialog dialog = builder.show();
	}
}
