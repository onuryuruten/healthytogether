package ch.epfl.hci.healthytogether;

public class GroupMember
{

	//String userName;
	int uid;
	String userName;
	int fid;
	int avatarid;
	
	
	public GroupMember()
	{
		//userName = null;
		uid=-1;
		fid=-1;
		avatarid=0;
		userName="";
	
	}
	
	public GroupMember(String userName, int uid, int fid, int avatarid)
	{
		this.userName  = userName;
		this.uid=uid;
		this.fid=fid;
		this.avatarid=avatarid;
	}
	
	public void setUserName(String userName)
	{
		this.userName = userName;
	}
	
	public void setUid(int uid)
	{
		this.uid=uid;
	}
	
	public void setFid(int fid){
		this.fid=fid;
	}
	
	public void setAvatarid(int avatarid){
		this.avatarid=avatarid;
	}
	
	
	public String getUserName() {
		return userName;
	}

	public int getUid() {
		return uid;
	}
	
	public int getFid(){
		return fid;
	}
	public int getAvatarid(){
		return avatarid;
	}

	public String toString()
	{
		return " User ID: " + uid+" Friend ID: "+fid;
	}

}
