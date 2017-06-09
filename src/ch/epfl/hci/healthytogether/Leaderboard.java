package ch.epfl.hci.healthytogether;


import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import ch.epfl.hci.happytogether.R;
import ch.epfl.hci.healthytogether.communication.ServerHelper.GetLeaderboardTask;
import ch.epfl.hci.healthytogether.util.Utils;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

class Team {
	int icon;
	// public String title;
	String score;
	String name;

	public Team() {
		icon = 0;
		score = "Loading...";
		name = "";
	}

	public Team(int icon,/* String title, */String score, String name) {
		this.icon = icon;
		// this.title = title;
		this.score = score;
		this.name = name;
	}

	public Team(Team inp) {
		icon = inp.icon;
		score = inp.score;
		name = inp.name;
	}

	public String toString() {
		return ("icon: " + icon + " score: " + score + " name: " + name);
	}
}

/*abstract class UpdateLeaderboardTask extends AsyncTask<Void, Void, Boolean> {
	public UpdateLeaderboardTask() {
	}

	@Override
	protected abstract Boolean doInBackground(Void... params);

	@Override
	protected abstract void onPostExecute(Boolean result);
}*/

public class Leaderboard extends ArrayAdapter<Team> {

	public boolean myLock = false;
	MainActivityCommunity context;
	int layoutResourceId;
	Team data[] = null;
	ArrayList<Team> data_list = null;
	//public UpdateLeaderboardTask task;
	private int type;

	public Leaderboard(int type, MainActivityCommunity context, int layoutResourceId,
			Team[] data) {
		super(context, layoutResourceId, data);
		this.type = type;
		this.layoutResourceId = layoutResourceId;
		this.context = context;
		this.data = data;
	}
	
	public Leaderboard(int type, MainActivityCommunity context, int layoutResourceId,
			ArrayList<Team> data) {
		super(context, layoutResourceId, data);
		this.type = type;
		this.layoutResourceId = layoutResourceId;
		this.context = context;
		this.data_list = data;
	}	
	

	/*public void update() {

		if (myLock == true) {
			return;
		}
		myLock = true;
		task = new UpdateLeaderboardTask() {

			protected Boolean doInBackground(Void... params) {

				switch (type) {
				case 0:

					Constants.leaderboardGroups.clear();
					for (int i = 0; i < Constants.allGroups.size(); i++)
						if (Constants.allGroups.get(i).isActive(type)) {
							Constants.leaderboardGroups.add(Constants.allGroups
									.get(i));
						}

					Collections.sort(Constants.leaderboardGroups);
					if (Constants.leaderboardGroups.size() > 0)
						Constants.winnerSteps = Constants.leaderboardGroups
								.get(0).getSteps();
					else
						Constants.winnerSteps = 10;
					if(Constants.winnerSteps==0)
						Constants.winnerSteps=10;
					Constants.winnerSteps = Constants.winnerSteps * 11 / 10;
					for (int i = 0; i < Constants.leaderboardGroups.size(); i++) {
						String groupName = "";
						if (Constants.leaderboardGroups.get(i).icon == 0) {
							String first = Constants.leaderboardGroups.get(i).mem[0]
									.getUserName();
							String second = Constants.leaderboardGroups.get(i).mem[1]
									.getUserName();
							groupName = first.charAt(0) + "+"
									+ second.charAt(0);
						}
						Leaderboard.this.data[i] = new Team(
								Constants.leaderboardGroups.get(i).icon,
								Constants.leaderboardGroups.get(i).getSteps()
										+ "", groupName);
					}

					for (int i = 0; i < Leaderboard.this.data.length; i++)
						Constants.team_data[i] = new Team(
								Leaderboard.this.data[i]);
					return true;
				case 1:

					Constants.leaderboardGroupsFloor.clear();
					for (int i = 0; i < Constants.allGroups.size(); i++)
						if (Constants.allGroups.get(i).isActive(type)) {
							Constants.leaderboardGroupsFloor.add(Constants.allGroups
									.get(i));
						}

					Collections.sort(Constants.leaderboardGroupsFloor, new FloorComparator());
					if (Constants.leaderboardGroupsFloor.size() > 0)
						Constants.winnerFloors = Constants.leaderboardGroupsFloor
								.get(0).getFloors();
					else
						Constants.winnerFloors = 10;
					if(Constants.winnerFloors==0)
						Constants.winnerFloors=10;
					Constants.winnerFloors = Constants.winnerFloors * 11 / 10;
					for (int i = 0; i < Constants.leaderboardGroupsFloor.size(); i++) {
						String groupName = "";
						if (Constants.leaderboardGroupsFloor.get(i).icon == 0) {
							String first = Constants.leaderboardGroupsFloor
									.get(i).mem[0].getUserName();
							String second = Constants.leaderboardGroupsFloor
									.get(i).mem[1].getUserName();
							groupName = first.charAt(0) + "+"
									+ second.charAt(0);
						}
						Leaderboard.this.data[i] = new Team(
								Constants.leaderboardGroupsFloor.get(i).icon,
								Constants.leaderboardGroupsFloor.get(i)
										.getFloors() + "", groupName);
					}

					for (int i = 0; i < Leaderboard.this.data.length; i++)
						Constants.team_data_floor[i] = new Team(
								Leaderboard.this.data[i]);

					return true;
				default:
					System.out.println("Error leaderboard mode");
					return false;
				}

			}

			// seems progressUpdate not working!:P
			// protected void onProgressUpdate(Void... progress){
			// for(int i=0;i<Constants.team_data.length;i++)
			// Constants.team_data[i].score="Loading...";
			// Leaderboard.this.notifyDataSetChanged();
			// System.out.println("on Progress update");
			// }
			@Override
			protected void onPostExecute(Boolean result) {
				Leaderboard.this.notifyDataSetChanged();
				context.showProgressDialog(false);
				myLock = false;
			}
		};
		task.execute();

	}	*/
	
	public void update() {

		if (myLock == true) {
			return;
		}
		myLock = true;
		//Leaderboard.this.clear();
		context.showProgressDialog(true);
		//Leaderboard.this.clear();
	//	Log.i("Leaderboard","Launching with parameters: (" + AppContext.getInstance().getUserId() + "," + Constants.communityDatePivot + "," + "1)");
/*		task = new UpdateLeaderboardTask() {

			protected Boolean doInBackground(Void... params) {*/

				GetLeaderboardTask glt = null;
				
				switch (type) {
				case 0:

					glt = new GetLeaderboardTask(AppContext.getInstance().getUserId(),Constants.communityDatePivot,1) {
						
						@Override
						protected void onPostExecute(Boolean result) 
						{
							
							int elementCount = 0;
							Log.i("Leaderboard","The GetLeaderboard post execute is called");

							if(!result || !this.safeExecution)
							{
								Log.e("Leaderboard", "Could not fetch the data!");
								context.showProgressDialog(false);
								if(!Utils.isConnectionPresent(context))
								{
									context.displayConnectionErrorDialog();
								}
								else
								{
									//Toast.makeText(context, "step update debug: check content", Toast.LENGTH_LONG).show();
								}
								return;
							}
									
							//Log.d("Leaderboard", "I have the data");
							
							if (Constants.leaderboardGroups.size() > 0) {
								Constants.winnerSteps = Constants.leaderboardGroups
										.get(0).getSteps();
							}
							else {
								Constants.winnerSteps = 10;
							}
							
							if(Constants.winnerSteps==0) {
								Constants.winnerSteps=10;
							}
							
							Constants.winnerSteps = Constants.winnerSteps * 11 / 10;
							//Leaderboard.this.data = new Team[Constants.leaderboardGroups.size()];
							
							
							for (int i = 0; i < Constants.leaderboardGroups.size(); i++) {
								String groupName = "";
								if (Constants.leaderboardGroups.get(i).icon == 0) {
									String first = Constants.leaderboardGroups.get(i).mem[0]
											.getUserName();
									String second = Constants.leaderboardGroups.get(i).mem[1]
											.getUserName();
									groupName = first.charAt(0) + "+"
											+ second.charAt(0);
								}
								
								
								if(Leaderboard.this.data_list.size() > i)
								{
									Team t = Leaderboard.this.data_list.get(i);
									Leaderboard.this.remove(t);
									Leaderboard.this.insert(new Team(
											Constants.leaderboardGroups.get(i).icon,
											Constants.leaderboardGroups.get(i).getSteps()
													+ "", groupName), i);
									
								/*	// for the time being...
									
									Leaderboard.this.insert(new Team(
											Constants.leaderboardGroups.get(i).icon,
											Constants.leaderboardGroups.get(i).getSteps()
													+ "", groupName), i);		*/
									// for the time being...
								/*	Leaderboard.this.add(new Team(
											Constants.leaderboardGroups.get(i).icon,
											Constants.leaderboardGroups.get(i).getSteps()
													+ "", groupName));			
									Leaderboard.this.add(new Team(
											Constants.leaderboardGroups.get(i).icon,
											Constants.leaderboardGroups.get(i).getSteps()
													+ "", groupName));	
									Leaderboard.this.add(new Team(
											Constants.leaderboardGroups.get(i).icon,
											Constants.leaderboardGroups.get(i).getSteps()
													+ "", groupName));			
									Leaderboard.this.add(new Team(
											Constants.leaderboardGroups.get(i).icon,
											Constants.leaderboardGroups.get(i).getSteps()
													+ "", groupName));		*/								
									
								}
								else
								{
									Leaderboard.this.add(new Team(
											Constants.leaderboardGroups.get(i).icon,
											Constants.leaderboardGroups.get(i).getSteps()
													+ "", groupName));
						
									
								}
								
								/*Leaderboard.this.data[i] = new Team(
										Constants.leaderboardGroups.get(i).icon,
										Constants.leaderboardGroups.get(i).getSteps()
												+ "", groupName);*/
							}
							Log.d("Leaderboard", "I have populated leaderboardStep");
							
							if(Leaderboard.this.data != null)
							{
								elementCount = Leaderboard.this.data.length;
							}
							else
							{
								elementCount = Leaderboard.this.data_list.size();
							}
							
							for (int i = 0; i < elementCount; i++) {
								Constants.team_data[i] = Leaderboard.this.getItem(i);//new Team(Leaderboard.this.data[i]);
							}
							Log.d("Leaderboard", "I have populated Constants.team_data");
							
							Constants.userStepRank = currentUserRank;
							//context.displayStats(0);
							
							/*context.listView.invalidate();
							LinearLayout whole=(LinearLayout) (context.findViewById(R.id.LinearLayout1));
							whole.invalidate();*/
							Log.d("Leaderboard", "I am now calling refreshLeaderboardDisplay(0)");
							context.refreshLeaderboardDisplay(0,false);
						}
						

					};
					
					
					/*Constants.leaderboardGroups.clear();
					for (int i = 0; i < Constants.allGroups.size(); i++)
						if (Constants.allGroups.get(i).isActive(type)) 
						{
							
							Constants.leaderboardGroups.add(Constants.allGroups
									.get(i));
						}

					for(int i = 0; i < Constants.leaderboardGroups.size(); i++)
					{
						Constants.leaderboardGroups.get(i).updateSteps();
					}
					
					
					Log.i("leaderboard", "All step counts are retreived");
					
					Collections.sort(Constants.leaderboardGroups);
					
					if (Constants.leaderboardGroups.size() > 0) {
						Constants.winnerSteps = Constants.leaderboardGroups
								.get(0).getSteps();
					}
					else {
						Constants.winnerSteps = 10;
					}
					
					if(Constants.winnerSteps==0) {
						Constants.winnerSteps=10;
					}
					
					Constants.winnerSteps = Constants.winnerSteps * 11 / 10;
					
					for (int i = 0; i < Constants.leaderboardGroups.size(); i++) {
						String groupName = "";
						if (Constants.leaderboardGroups.get(i).icon == 0) {
							String first = Constants.leaderboardGroups.get(i).mem[0]
									.getUserName();
							String second = Constants.leaderboardGroups.get(i).mem[1]
									.getUserName();
							groupName = first.charAt(0) + "+"
									+ second.charAt(0);
						}
						
						Leaderboard.this.data[i] = new Team(
								Constants.leaderboardGroups.get(i).icon,
								Constants.leaderboardGroups.get(i).getSteps()
										+ "", groupName);
					}

					for (int i = 0; i < Leaderboard.this.data.length; i++) {
						Constants.team_data[i] = new Team(
								Leaderboard.this.data[i]);
					}*/
					
					//return true;
					Leaderboard.this.notifyDataSetChanged();
					//context.showProgressDialog(false);
					myLock = false;					
					break;
				case 1:

					
					glt = new GetLeaderboardTask(AppContext.getInstance().getUserId(),Constants.communityDatePivot,0) {
						
						@Override
						protected void onPostExecute(Boolean result) 
						{
	
							int elementCount = 0;
							Log.i("Leaderboard","The GetLeaderboard post execute is called");

							if(!result || !this.safeExecution)
							{
								Log.e("Leaderboard", "Could not fetch the data!");
								
								if(!Utils.isConnectionPresent(context))
								{
									context.displayConnectionErrorDialog();
								}
								else
								{
									//Toast.makeText(context, "floor update debug: check content", Toast.LENGTH_LONG).show();
								}
								return;
							}
							
							Log.d("Leaderboard", "I have the data");
							
							if (Constants.leaderboardGroupsFloor.size() > 0)
								Constants.winnerFloors = Constants.leaderboardGroupsFloor
										.get(0).getFloors();
							else
								Constants.winnerFloors = 10;
							if(Constants.winnerFloors==0)
								Constants.winnerFloors=10;
							Constants.winnerFloors = Constants.winnerFloors * 11 / 10;
							for (int i = 0; i < Constants.leaderboardGroupsFloor.size(); i++) {
								String groupName = "";
								if (Constants.leaderboardGroupsFloor.get(i).icon == 0) {
									String first = Constants.leaderboardGroupsFloor
											.get(i).mem[0].getUserName();
									String second = Constants.leaderboardGroupsFloor
											.get(i).mem[1].getUserName();
									groupName = first.charAt(0) + "+"
											+ second.charAt(0);
								}
								
								
								
								if(Leaderboard.this.data_list.size() > i)
								{
									Team t = Leaderboard.this.data_list.get(i);
									Leaderboard.this.remove(t);
									Leaderboard.this.insert(new Team(
											Constants.leaderboardGroupsFloor.get(i).icon,
											Constants.leaderboardGroupsFloor.get(i).getFloors()
													+ "", groupName), i);
								}
								else
								{
									Leaderboard.this.add(new Team(
											Constants.leaderboardGroupsFloor.get(i).icon,
											Constants.leaderboardGroupsFloor.get(i).getFloors()
													+ "", groupName));
								}
																
							}
							
							Log.d("Leaderboard", "I have populated leaderboardFloor");

							if(Leaderboard.this.data != null)
							{
								elementCount = Leaderboard.this.data.length;
							}
							else
							{
								elementCount = Leaderboard.this.data_list.size();
							}
							
							for (int i = 0; i < elementCount; i++)
							{
								Constants.team_data_floor[i] = Leaderboard.this.getItem(i);/*new Team(
										Leaderboard.this.data[i]);*/
							}
							
							Log.d("Leaderboard", "I have populated Constants.team_data_floor");
							Constants.userFloorRank = currentUserRank;
							//context.displayStats(1);
							
							Log.d("Leaderboard", "I am now calling refreshLeaderboardDisplay(1)");
							context.refreshLeaderboardDisplay(1,false);
							
						}
					};
					
					/*Constants.leaderboardGroupsFloor.clear();
					for (int i = 0; i < Constants.allGroups.size(); i++)
						if (Constants.allGroups.get(i).isActive(type)) {
							//Constants.allGroups.get(i).updateFloors(); // Onur: first addition.
							Constants.leaderboardGroupsFloor.add(Constants.allGroups
									.get(i));
						}

					for(int i = 0; i < Constants.leaderboardGroupsFloor.size(); i++)
					{
						Constants.leaderboardGroupsFloor.get(i).updateFloors();
					}
					
					Collections.sort(Constants.leaderboardGroupsFloor, new FloorComparator());
					if (Constants.leaderboardGroupsFloor.size() > 0)
						Constants.winnerFloors = Constants.leaderboardGroupsFloor
								.get(0).getFloors();
					else
						Constants.winnerFloors = 10;
					if(Constants.winnerFloors==0)
						Constants.winnerFloors=10;
					Constants.winnerFloors = Constants.winnerFloors * 11 / 10;
					for (int i = 0; i < Constants.leaderboardGroupsFloor.size(); i++) {
						String groupName = "";
						if (Constants.leaderboardGroupsFloor.get(i).icon == 0) {
							String first = Constants.leaderboardGroupsFloor
									.get(i).mem[0].getUserName();
							String second = Constants.leaderboardGroupsFloor
									.get(i).mem[1].getUserName();
							groupName = first.charAt(0) + "+"
									+ second.charAt(0);
						}
						Leaderboard.this.data[i] = new Team(
								Constants.leaderboardGroupsFloor.get(i).icon,
								Constants.leaderboardGroupsFloor.get(i)
										.getFloors() + "", groupName);
					}

					for (int i = 0; i < Leaderboard.this.data.length; i++)
						Constants.team_data_floor[i] = new Team(
								Leaderboard.this.data[i]);*/

					//return true;
					Leaderboard.this.notifyDataSetChanged();
					//context.showProgressDialog(false);
					myLock = false;
					break;
				default:
					Log.e("Leaderboard","Error leaderboard mode");
					Leaderboard.this.notifyDataSetChanged();
					//context.showProgressDialog(false);
					myLock = false;
					//return false;
					
					
				}
				
				glt.execute();
				/*try {
					glt.get();
				} catch (InterruptedException e) 
				{
					Log.e("Leaderboard","The get-leaderboard task is interrupted and not working " + e.getMessage());
					e.printStackTrace();
				} catch (ExecutionException e) {
					Log.e("Leaderboard","The get-leaderboard task has execution exception. not working. " + e.getMessage());
					e.printStackTrace();
				}*/
				
				//return true;

			//}

			// seems progressUpdate not working!:P
			// protected void onProgressUpdate(Void... progress){
			// for(int i=0;i<Constants.team_data.length;i++)
			// Constants.team_data[i].score="Loading...";
			// Leaderboard.this.notifyDataSetChanged();
			// System.out.println("on Progress update");
			// }
			/*@Override
			protected void onPostExecute(Boolean result) {
				Leaderboard.this.notifyDataSetChanged();
				//context.showProgressDialog(false);
				myLock = false;
			}
		};
		task.execute();*/

	}

	/*
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		TeamHolder holder = null;

		if (row == null) {
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);

			holder = new TeamHolder();
			holder.imgIcon = (ImageView) row.findViewById(R.id.imgIcon);
			// holder.txtTitle = (TextView)row.findViewById(R.id.txtTitle);
			holder.txtScore = (TextView) row.findViewById(R.id.txtScore);
			holder.txtName = (TextView) row.findViewById(R.id.txtName);
			holder.BarMask = (View) row.findViewById(R.id.BarMask);

			row.setTag(holder);
		} else {
			holder = (TeamHolder) row.getTag();
		}

		Team team = data[position];

		holder.txtScore.setText(team.score);
		holder.imgIcon.setImageResource(Constants.getAvatarNum(team.icon));
		holder.txtName.setText(team.name);

		switch (type) {
		case 0:
			RelativeLayout bar = (RelativeLayout) row
					.findViewById(R.id.leaderboardBarChart);

			if (Constants.winnerSteps == 0)
				if (!Constants.team_data[0].score.equals("Loading...")) {
					Constants.winnerSteps = Integer
							.parseInt(Constants.team_data[0].score);
					Constants.winnerSteps = Constants.winnerSteps * 11 / 10;
					if(Constants.winnerSteps==0)
						Constants.winnerSteps=1;
				} else
					Constants.winnerSteps = 1;
			if (team.score.equals("Loading..."))
				team.score = "0";
			int width = Integer.parseInt(team.score) * bar.getWidth()
					/ Constants.winnerSteps;

			holder.BarMask.setLayoutParams(new LinearLayout.LayoutParams(width,
					LayoutParams.MATCH_PARENT));
			break;
		case 1:
			RelativeLayout barFloor = (RelativeLayout) row
					.findViewById(R.id.leaderboardBarChart);

			if (Constants.winnerFloors == 0)
				if (!Constants.team_data_floor[0].score.equals("Loading...")) {
					Constants.winnerFloors = Integer
							.parseInt(Constants.team_data_floor[0].score);
					Constants.winnerFloors = Constants.winnerFloors * 11 / 10;
					if(Constants.winnerFloors==0)
						Constants.winnerFloors=1;
				} else
					Constants.winnerFloors = 1;
			if (team.score.equals("Loading..."))
				team.score = "0";
			int widthFloor = Integer.parseInt(team.score) * barFloor.getWidth()
					/ Constants.winnerFloors;

			holder.BarMask.setLayoutParams(new LinearLayout.LayoutParams(
					widthFloor, LayoutParams.MATCH_PARENT));
			break;
		default:
			System.out.println("Error in leaderboard mode");
		}

		return row;
	}	*/
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View row = convertView;
		TeamHolder holder = null;

	/*	if((type == 0 && position > Constants.leaderboardGroups.size())
				|| type == 1 && position > Constants.leaderboardGroupsFloor.size())
		{
			return null;
		}*/

		
		if (row == null) {
			

			
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);
			
			
			holder = new TeamHolder();
			holder.imgIcon = (ImageView) row.findViewById(R.id.imgIcon);
			// holder.txtTitle = (TextView)row.findViewById(R.id.txtTitle);
			holder.txtScore = (TextView) row.findViewById(R.id.txtScore);
			holder.txtName = (TextView) row.findViewById(R.id.txtName);
			holder.BarMask = (View) row.findViewById(R.id.BarMask);

			row.setTag(holder);
		} else {
			holder = (TeamHolder) row.getTag();
		}

		Team team;
		if(data != null)
			team = data[position];
		else
			team = data_list.get(position);

		if(team.score.equalsIgnoreCase("Loading..."))
		{
			holder.txtScore.setText(context.getResources().getString(R.string.main_loading));
		}
		else
		{
			holder.txtScore.setText(team.score);
		}
		holder.imgIcon.setImageResource(Constants.getAvatarNum(team.icon));
		
		
		holder.txtName.setText( "  " + team.name);
		
		Log.e("Leaderboard.getView()", "adding: " + team.name + " with icon: " + team.icon + " and score = " + holder.txtScore.getText());
		final RelativeLayout bar = (RelativeLayout) row
				.findViewById(R.id.leaderboardBarChart);
		final RelativeLayout barFloor = bar;//(RelativeLayout) row.findViewById(R.id.leaderboardBarChart);
		
		
		/*bar.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

		    @Override
		    public void onGlobalLayout() {

		    	bar.getWidth();

		    }
		});
		
		barFloor.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

		    @Override
		    public void onGlobalLayout() {

		    	bar.getWidth();

		    }
		});*/
		
		//Display display = context.getWindowManager().getDefaultDisplay();
		//View view = findViewById(R.id.YOUR_VIEW_ID);
		
		WindowManager mWinMgr = (WindowManager)context.getSystemService(context.WINDOW_SERVICE);
		int displayWidth = mWinMgr.getDefaultDisplay().getWidth();
		int displayOffset = context.dpToPx(120);
		
		
		int myWidth = displayWidth - displayOffset;
		//int myHeight;
		int widthParam = 0;
		/*bar.measure(display.getWidth(), display.getHeight());

		final int myWidth = bar.getMeasuredWidth(); // view width
		final int myHeight = bar.getMeasuredHeight(); //view height*/
		switch (type) {
		case 0:

			if (Constants.winnerSteps == 0)
				if (!Constants.team_data[0].score.equalsIgnoreCase("Loading...")
						&& !Constants.team_data[0].score.equalsIgnoreCase(context.getResources().getString(R.string.main_loading))
						) {
					Constants.winnerSteps = Integer
							.parseInt(Constants.team_data[0].score);
					Constants.winnerSteps = Constants.winnerSteps * 11 / 10;
					if(Constants.winnerSteps==0)
						Constants.winnerSteps=1;
				} else
					Constants.winnerSteps = 1;
			
			/*if (team.score.equalsIgnoreCase("Loading...") || team.score.equalsIgnoreCase(context.getResources().getString(R.string.main_loading)))
				team.score = "0";*/
			
			
			/*if(bar.getWidth() > 0)
			{
				myWidth = bar.getWidth();
			}
			else
			{
				bar.measure(display.getWidth(), display.getHeight());
				myWidth = 4*bar.getMeasuredWidth(); // view width
				myHeight = bar.getMeasuredHeight(); //view height		s				
			}*/
		
			
			try
			{
				widthParam = Integer.parseInt(team.score);
			}
			catch(java.lang.NumberFormatException e)
			{
				widthParam = 0;
			}
			
			int width = widthParam *  myWidth//bar.getWidth() //
					/ Constants.winnerSteps;

			holder.BarMask.setLayoutParams(new LinearLayout.LayoutParams(width,
					LayoutParams.MATCH_PARENT));
			
			if(position == (Constants.userStepRank -1))
			{
				//holder.BarMask.setBackgroundColor(context.getResources().getColor(R.color.dark_orange));
				holder.BarMask.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.my_progress_mask));
				//holder.BarMask.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.friend_progress_mask));
			}
			else
			{
				//holder.BarMask.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.my_progress_mask));
				holder.BarMask.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.friend_progress_mask));
			}			
			
			
			holder.imgIcon.setOnClickListener(new OnClickListener()
		    {
		        @Override
		        public void onClick(View v)
		        {
		        	
		        	if(myLock)
		        		return;
		        	
					int ind = position;// - 1;
					if (ind < Constants.leaderboardGroups.size()) {
						String first = Constants.leaderboardGroups
								.get(ind).mem[0].getUserName();
						String second = Constants.leaderboardGroups
								.get(ind).mem[1].getUserName();
						
						if((ind+1) == Constants.userStepRank)
						{
							Toast.makeText(context,
									context.getResources().getString(R.string.your_team) + " (" + first +  " " + context.getResources().getString(R.string.and) + " " + second + ")",
									Toast.LENGTH_SHORT).show();
						}
						else
						{
							Toast.makeText(context,
									context.getResources().getString(R.string.team_of) + " " + first +  " " + context.getResources().getString(R.string.and) + " " + second,
									Toast.LENGTH_SHORT).show();
						}
					}	        	
		        }
		    });			
			
			break;
		case 1:
			

			if (Constants.winnerFloors == 0)
				if (!Constants.team_data_floor[0].score.equalsIgnoreCase("Loading...")
						&& !Constants.team_data_floor[0].score.equalsIgnoreCase(context.getResources().getString(R.string.main_loading))						
						) {
					Constants.winnerFloors = Integer
							.parseInt(Constants.team_data_floor[0].score);
					Constants.winnerFloors = Constants.winnerFloors * 11 / 10;
					if(Constants.winnerFloors==0)
						Constants.winnerFloors=1;
				} else
					Constants.winnerFloors = 1;
			
			
			/*if(barFloor.getWidth() > 0)
			{
				myWidth = barFloor.getWidth();
			}
			else
			{
				barFloor.measure(display.getWidth(), display.getHeight());
				myWidth = 4*barFloor.getMeasuredWidth(); // view width
				myHeight = barFloor.getMeasuredHeight(); //view height				
			}*/
			
			//myWidth = 4*bar.getMeasuredWidth(); // view width
		    //myHeight = bar.getMeasuredHeight(); //view height
			
			/*if (team.score.equalsIgnoreCase("Loading...") || team.score.equalsIgnoreCase(context.getResources().getString(R.string.main_loading)))
				team.score = "0";*/
			
			try
			{
				widthParam = Integer.parseInt(team.score);
			}
			catch(java.lang.NumberFormatException e)
			{
				widthParam = 0;
			}
			
			
			int widthFloor = widthParam * myWidth//barFloor.getWidth()
					/ Constants.winnerFloors;

			holder.BarMask.setLayoutParams(new LinearLayout.LayoutParams(
					widthFloor, LayoutParams.MATCH_PARENT));
			
			if(position == (Constants.userFloorRank -1))
			{
				//holder.BarMask.setBackgroundColor(context.getResources().getColor(R.color.dark_orange));
				holder.BarMask.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.my_progress_mask));
			}
			else
			{
				//holder.BarMask.setBackgroundColor(context.getResources().getColor(R.color.dark_blue));
				holder.BarMask.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.friend_progress_mask));
			}
			
			holder.imgIcon.setOnClickListener(new OnClickListener()
		    {
		        @Override
		        public void onClick(View v)
		        {
		        	
		        	if(myLock)
		        		return;
		        	
					int ind = position;// - 1;
					if (ind < Constants.leaderboardGroupsFloor.size()) {
						String first = Constants.leaderboardGroupsFloor
								.get(ind).mem[0].getUserName();
						String second = Constants.leaderboardGroupsFloor
								.get(ind).mem[1].getUserName();
						
						if((ind+1) == Constants.userFloorRank)
						{
							Toast.makeText(context,
									context.getResources().getString(R.string.your_team) + " (" + first +  " " + context.getResources().getString(R.string.and) + " " + second + ")",
									Toast.LENGTH_SHORT).show();
						}
						else
						{
						Toast.makeText(context,
								context.getResources().getString(R.string.team_of) + " " + first +  " " + context.getResources().getString(R.string.and) + " " + second,
								Toast.LENGTH_SHORT).show();
						}
					}	        	
		        }
		    });			
			break;
		default:
			System.out.println("Error in leaderboard mode");
		}

		//final String x = team.name;
  
	    row.invalidate();
		return row;
	}

	static class TeamHolder {
		ImageView imgIcon;
		View BarMask;
		// TextView txtTitle;
		TextView txtScore;
		TextView txtName;
	}
}