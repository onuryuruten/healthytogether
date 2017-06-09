package ch.epfl.hci.healthytogether;

import java.util.ArrayList;
import java.util.TreeSet;

import ch.epfl.hci.healthytogether.Main2Activity.ViewHolder;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.TabHost.TabSpec;

public class ExerciseTabHost extends TabActivity {
	/*
	 * private static final String TAG= ExerciseTabHost.class.toString();
	 * 
	 * @Override public void onCreate(Bundle savedInstanceState) {
	 * super.onCreate(savedInstanceState);
	 * setContentView(R.layout.exercise_tab_layout);
	 * 
	 * TabHost tab_host = getTabHost(); tab_host.setup();
	 * tab_host.getTabWidget();//.setDividerDrawable(R.drawable.tab_divider);
	 * 
	 * View tabview = createTabView(tab_host.getContext(), "Exercises"); TabSpec
	 * setContent =
	 * tab_host.newTabSpec("Exercises").setIndicator(tabview).setContent(new
	 * Intent(this, ExerciseCategory.class)); tab_host.addTab(setContent);
	 * 
	 * View tabview2 = createTabView(tab_host.getContext(), "Sedentary");
	 * TabSpec setContent2 =
	 * tab_host.newTabSpec("Sedentary").setIndicator(tabview2).setContent(new
	 * Intent(this, SedentaryCategory.class)); tab_host.addTab(setContent2);
	 * 
	 * //tab_host.addTab(tab_host.newTabSpec("tab1").setIndicator("Exercises").
	 * setContent(new Intent(this, ExerciseCategory.class)));
	 * 
	 * // tab_host.addTab(tab_host.newTabSpec("tab2").setIndicator("Sedentary").
	 * setContent(new Intent(this, SedentaryCategory.class)));
	 * 
	 * View cv; for(int i = 0; i < tab_host.getTabWidget().getChildCount(); i++)
	 * {
	 * 
	 * if(i == 1) { Log.d(TAG,"ONUR, TABS " + i +
	 * ": appyling  mytab_roundedcorners2");
	 * tab_host.getTabWidget().getChildAt(i
	 * ).setBackgroundResource(R.drawable.mytab_roundedcorners2); } else if(i ==
	 * 0) { Log.d(TAG,"ONUR, TABS " + i + ": appyling  mytab_roundedcorners");
	 * tab_host.getTabWidget().getChildAt(i).setBackgroundResource(R.drawable.
	 * mytab_roundedcorners); }
	 * 
	 * cv = tab_host.getTabWidget().getChildAt(i); LinearLayout.LayoutParams
	 * currentLayout = (LinearLayout.LayoutParams) cv.getLayoutParams();
	 * currentLayout.setMargins(0, 2, 2, 0); } tab_host.setCurrentTab(0);
	 * 
	 * }
	 * 
	 * 
	 * private static View createTabView(final Context context, final String
	 * text) { View view =
	 * LayoutInflater.from(context).inflate(R.layout.tabs_bg, null); TextView tv
	 * = (TextView) view.findViewById(R.id.tabsText); tv.setText(text); return
	 * view; }
	 */
}
