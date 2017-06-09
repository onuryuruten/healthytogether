package ch.epfl.hci.healthytogether;

import java.util.ArrayList;

import ch.epfl.hci.happytogether.R;
import android.R.bool;
import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;

public class DynamicTable extends Activity {
	TableLayout table;

	ArrayList<String> list_name;

	int color_blue = -16776961;
	int color_gray = -7829368;
	int color_black = -16777216;
	int color_white = -1;

	final int CHECK_BUTTON_ID = 982301;
	ArrayList<Integer> ids_check;
	ArrayList<Boolean> bool_check;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_history);
		// TODO: figure out the table layout!!!
		table = (TableLayout) findViewById(R.id.historyListView1);

		list_name = new ArrayList<String>();

		list_name.add("Close");
		list_name.add("Cristiano");
		list_name.add("David");
		list_name.add("Fernando");
		list_name.add("Messi");
		list_name.add("Kaka");
		list_name.add("Wayne");

		bool_check = new ArrayList<Boolean>();
		ids_check = new ArrayList<Integer>();
		createTableRows();

	}

	public void createTableRows() {
		for (int i = 0; i < list_name.size(); i++) {
			TableRow table_row = new TableRow(this);
			TextView tv_name = new TextView(this);
			Button btn_check = new Button(this);
			ImageView img_line = new ImageView(this);

			table_row.setLayoutParams(new LayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
			table_row.setBackgroundColor(color_white);
			table_row.setGravity(Gravity.CENTER_HORIZONTAL);

			tv_name.setText(list_name.get(i));
			tv_name.setTextColor(color_blue);
			tv_name.setTextSize(16);
			tv_name.setTypeface(Typeface.DEFAULT_BOLD);
			tv_name.setWidth(150);

			btn_check.setLayoutParams(new LayoutParams(30, 30));
			btn_check.setBackgroundResource(R.drawable.cheer_friend);

			img_line.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
					2));
			img_line.setBackgroundResource(R.drawable.textboxborder);

			table_row.addView(tv_name);
			table_row.addView(btn_check);

			table.addView(table_row);
			table.addView(img_line);

			int id = i + CHECK_BUTTON_ID;
			btn_check.setId(id);

			ids_check.add(id);
			bool_check.add(false);
			// ids_check[i] = id;

			btn_check.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					for (int j = 0; j < ids_check.size(); j++) {
						Button btn_check_1 = (Button) findViewById(ids_check
								.get(j));
						if (v.getId() == ids_check.get(j))
							if (bool_check.get(j)) {
								// checked...
								btn_check_1
										.setBackgroundResource(R.drawable.healthytogether);
								bool_check.set(j, false);
							} else {
								// unchecked...
								btn_check_1
										.setBackgroundResource(R.drawable.healthytogether);
								bool_check.set(j, true);
							}
					}
				}
			});

		}
	}
}