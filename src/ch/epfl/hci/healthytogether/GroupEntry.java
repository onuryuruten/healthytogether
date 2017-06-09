package ch.epfl.hci.healthytogether;

import java.util.Comparator;

import ch.epfl.hci.healthytogether.communication.ServerHelper;

class FloorComparator implements Comparator<GroupEntry>{
    public int compare(GroupEntry g1, GroupEntry g2) {
		//int s[][]=new int [2][2];
/*		for(int i=0;i<2;i++)
			s[0][i]= ServerHelper.sequentialGetFloors(g1.mem[i].getUid(),Constants.communityDatePivot);
		for(int i=0;i<2;i++)
				s[1][i]= ServerHelper.sequentialGetFloors(g2.mem[i].getUid(),Constants.communityDatePivot);*/
		
/*		for(int i=0;i<2;i++)
			s[0][i]= ServerHelper.sequentialGetFloors(g1.mem[i].getUid(),Constants.communityDatePivot);
		for(int i=0;i<2;i++)
				s[1][i]= ServerHelper.sequentialGetFloors(g2.mem[i].getUid(),Constants.communityDatePivot);*/
		
		
		
		int sum1=g1.getFloors();//s[0][0]+s[0][1];
		
		int sum2=g2.getFloors();//s[1][0]+s[1][1];
		if (sum1 < sum2)
			return 1;
		if (sum1 > sum2)
			return -1;
		return 0;
    }
}
public class GroupEntry implements Comparable<GroupEntry> {

	private static final String TAG = GroupEntry.class.toString();
	
	public GroupMember mem[];
	public int icon;
	public int steps[];
	public boolean stepCheck[];
	public int floors[];
	public boolean floorCheck[];

	public GroupEntry() {
		mem = new GroupMember[2];
		mem[0] = new GroupMember();
		mem[1] = new GroupMember();
		steps = new int[2];
		floors = new int[2];
		stepCheck = new boolean[2];
		floorCheck = new boolean[2];
		icon=0;

	}

	public GroupEntry(GroupMember m0, GroupMember m1, int icon) {
		mem = new GroupMember[2];
		mem[0] = m0;
		mem[1] = m1;
		steps = new int[2];
		floors = new int[2];	
		stepCheck = new boolean[2];
		floorCheck = new boolean[2];		
		this.icon=icon;
	}
	
	public boolean isMember(int uid)
	{
		return (mem[0].getUid() == uid) || (mem[1].getUid() == uid);  
	}
	
	public boolean isStepsUpdated()
	{
		return stepCheck[0] && stepCheck[1];
	}
	
	public boolean isFloorsUpdated()
	{
		return floorCheck[0] && floorCheck[1];
	}	
	
	public int getAvailableSteps()
	{
		return (steps[0]+steps[1]);
	}
	
	public int getAvailableFloors()
	{
		return (floors[0]+floors[1]);
	}	
	
	public int updateSteps()
	{
		
		//return getSteps();
		int s[]=new int[2];
		for(int i=0;i<2;i++)
		{
			this.stepCheck[i] = false;
			//ServerHelper.getMemberSteps(this.mem[i].getUid(),Constants.communityDatePivot,this, i);
			s[i]= ServerHelper.sequentialGetSteps(this.mem[i].getUid(),Constants.communityDatePivot,this, i);
		}
		return (s[0]+s[1]);		
		
	}

	public int updateFloors()
	{
//		return getFloors();
		int s[]=new int[2];
		for(int i=0;i<2;i++)
			s[i]= ServerHelper.sequentialGetFloors(this.mem[i].getUid(),Constants.communityDatePivot,this, i);
		return (s[0]+s[1]);		
	}
	
	public int getSteps(){
		
		return getAvailableSteps();
		/*int s[]=new int[2];
		for(int i=0;i<2;i++)
			s[i]= ServerHelper.sequentialGetSteps(this.mem[i].getUid(),Constants.communityDatePivot,this, i);
		return (s[0]+s[1]);*/
	}
	
	public int getFloors(){
		
		return getAvailableFloors();
		/*
		int s[]=new int[2];
		for(int i=0;i<2;i++)
			s[i]= ServerHelper.sequentialGetFloors(this.mem[i].getUid(),Constants.communityDatePivot,this, i);
		return (s[0]+s[1]);*/
	}
	
	public boolean isActive(int type){
		if(type==0)
			return (this.getSteps()>0 || this.icon>0);
		
		//if type==1
		return (this.getFloors()>0|| this.icon>0);
	}
	
	
	public int compareTo(GroupEntry g) {
		/*int s[][]=new int [2][2];
		for(int i=0;i<2;i++)
			s[0][i]= ServerHelper.sequentialGetSteps(this.mem[i].getUid(),Constants.communityDatePivot);
		for(int i=0;i<2;i++)
				s[1][i]= ServerHelper.sequentialGetSteps(g.mem[i].getUid(),Constants.communityDatePivot);
		int sum1=s[0][0]+s[0][1];
		int sum2=s[1][0]+s[1][1];*/
		
		int sum1 = this.getSteps();
		int sum2 = g.getSteps();
		
		if (sum1 < sum2)
			return 1;
		if (sum1 > sum2)
			return -1;
		return 0;
		
		/*myLock=false;

		final GroupEntry gTemp0 = new GroupEntry(g.mem[0], g.mem[1]);
		final GroupEntry gTemp1 = new GroupEntry(this.mem[0], this.mem[1]);
		
		

		if (g.mem[0].getUid() != g.mem[1].getFid()
				|| g.mem[0].getFid() != g.mem[1].getUid()
				|| this.mem[0].getUid() != this.mem[1].getFid()
				|| this.mem[0].getFid() != this.mem[1].getUid())
			System.out.println("********ERRROOOOOOR, not teammates");

		// ////////////////////////////////////////////


		RetrieveStepCountTask task00 = new RetrieveStepCountTask(
				gTemp0.mem[0].getUid(), Constants.communityDatePivot) {
			@Override
			protected void onPostExecute(final Integer sc00) {
				RetrieveStepCountTask task01 = new RetrieveStepCountTask(
						gTemp0.mem[1].getUid(), Constants.communityDatePivot) {
					@Override
					protected void onPostExecute(final Integer sc01) {
						RetrieveStepCountTask task10 = new RetrieveStepCountTask(
								gTemp1.mem[0].getUid(),
								Constants.communityDatePivot) {

							@Override
							protected void onPostExecute(final Integer sc10) {
								RetrieveStepCountTask task11 = new RetrieveStepCountTask(
										gTemp1.mem[1].getUid(),
										Constants.communityDatePivot) {

									@Override
									protected void onPostExecute(
											Integer sc11) {
										int sum1 = sc00 + sc01;
										int sum2 = sc10 + sc11;
										
										System.out.println("groups *****");
										System.out.println(gTemp0.mem[0].getUid()+" "+gTemp0.mem[1].getUid());
										System.out.println(gTemp1.mem[0].getUid()+" "+gTemp1.mem[1].getUid());
										System.out.println("step sum " + sum1 + " " + sum2);
										
										if (sum1 > sum2)
											cmpReturnValue= 1;
										else if (sum1 < sum2)
											cmpReturnValue= -1;
										else cmpReturnValue= 0;
										myLock=true;

									}
								};
								task11.execute();

							}
						};
						task10.execute();

					}
				};
				task01.execute();
				
			}
		};
		task00.execute();

		//dirty way to handle the sequential order! :|

		while(myLock==false){
			//System.out.println("ASMA ***** not sync");
			try {
				Thread.currentThread().sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("ASMA **** synced OK");
		return cmpReturnValue;*/
		
	}

	public String toString() {
		return "user0: " + mem[0].toString() + "\t user1: " + mem[1].toString();
	}

}
