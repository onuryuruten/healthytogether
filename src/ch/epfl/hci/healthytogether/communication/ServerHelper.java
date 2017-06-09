/*
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *	Copyright 2009 Andrew Rice (acr31@cam.ac.uk) and Vytautas Vaitukaitis (vv236@cam.ac.uk)
 */

package ch.epfl.hci.healthytogether.communication;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.os.AsyncTask;
import android.util.Log;
import ch.epfl.hci.happytogether.App;
import ch.epfl.hci.healthytogether.AppContext;
import ch.epfl.hci.healthytogether.Constants;
import ch.epfl.hci.healthytogether.ErrorHandler;
import ch.epfl.hci.healthytogether.GroupEntry;
import ch.epfl.hci.healthytogether.GroupMember;
import ch.epfl.hci.healthytogether.OneDayBadges;
import ch.epfl.hci.healthytogether.Pledge;
import ch.epfl.hci.healthytogether.UserEntry;
import ch.epfl.hci.healthytogether.model.BadgeArray;
import ch.epfl.hci.healthytogether.model.BadgeResult;
import ch.epfl.hci.healthytogether.model.GroupMeta;
import ch.epfl.hci.healthytogether.model.MessageMeta;
import ch.epfl.hci.healthytogether.util.Utils;

// is waiting
public class ServerHelper {

	private static final String TAG = ServerHelper.class.getSimpleName();

	private static final String URL_PREFIX = "http://path/to/server/";

	public static final String URL_LOGIN = URL_PREFIX + "checklogin.php";

	private static final String URL_REGISTER = URL_PREFIX + "checkregister.php";

	private static final String URL_GROUP = URL_PREFIX + "checkgroup.php";

	private static final String URL_BUDDY = URL_PREFIX + "checkbuddy.php";
	private static final String URL_BUDDY2 = URL_PREFIX + "checkbuddy2.php";

	private static final String URL_PENDING = URL_PREFIX + "checkpending.php";

	private static final String URL_GETSTARTDATE = URL_PREFIX
			+ "getstartingdateforgame.php";
	private static final String URL_ACCEPT = URL_PREFIX
			+ "acceptrequestnmail.php";

	private static final String URL_BADGE = URL_PREFIX + "checkbadge.php";

	private static final String URL_GETBADGE = URL_PREFIX + "getbadges.php"; // getbadges3.php
																				// has
																				// the
																				// buddy!

	private static final String URL_GETMSG = URL_PREFIX + "checkmsg.php";

	private static final String URL_MINUTES = URL_PREFIX + "getminutes.php";

	private static final String URL_REG = URL_PREFIX + "register.php";

	private static final String URL_INVITE = URL_PREFIX + "invitebuddy2.php";

	private static final String URL_CHECKINVITEDATE = URL_PREFIX
			+ "checkinvitation.php";

	private static final String URL_POKE = URL_PREFIX + "pokebuddy.php";

	private static final String URL_MSG = URL_PREFIX + "msgbuddy.php";
	
	private static final String URL_LOG = URL_PREFIX + "sendlog.php";

	private static final String URL_SET_LOG = URL_PREFIX + "changelog.php";

	private static final String URL_DEL_LOG = URL_PREFIX + "deletelog.php";

	private static final String URL_STEPS = URL_PREFIX + "getdatesteps.php";

	private static final String URL_FLOORS = URL_PREFIX + "getdatefloors.php";

	private static final String URL_REMIND = URL_PREFIX + "remindbuddy.php";

	private static final String URL_SEND_REMINDER = URL_PREFIX + "passwordreminder.php";
	
	//private static final String URL_MEMBERS = URL_PREFIX + "groupmembers.php";

	private static final String URL_GETMSGS = URL_PREFIX + "getmsgs.php";

	private static final String URL_BUDDYNAME = URL_PREFIX + "getbuddyname.php";
	private static final String URL_BUDDYINFO = URL_PREFIX + "getbuddyinfo.php";
	
	private static final String URL_DEFRIEND = URL_PREFIX + "defriend.php";

	private static final String URL_DEFRIEND2 = URL_PREFIX + "defriend2.php";

	private static final String URL_GET_USERS = URL_PREFIX + "getpeople.php";
	private static final String URL_GET_USERS_INV = URL_PREFIX + "getpeopleforinvitation.php";
	private static final String URL_GET_LEADERBOARD = URL_PREFIX + "retrieveLeaderboard.php";

	// ASMA START COMMUNITY
	private static final String URL_SET_AVATAR = URL_PREFIX + "setavatar.php";
	private static final String URL_GET_AVATAR = URL_PREFIX + "getavatar.php";
	private static final String URL_SET_STEP_PLEDGE = URL_PREFIX
			+ "setsteppledge.php";
	private static final String URL_GET_STEP_PLEDGE = URL_PREFIX
			+ "getsteppledge.php";
	private static final String URL_SET_FLOOR_PLEDGE = URL_PREFIX
			+ "setfloorpledge.php";
	private static final String URL_GET_FLOOR_PLEDGE = URL_PREFIX
			+ "getfloorpledge.php";

	private static final String URL_CHECK_FITBIT = URL_PREFIX
			+ "checkfitbitauth.php";
	
	private static final String URL_CANCEL_PLEDGES = URL_PREFIX
			+ "cancelpledges.php";
	
	public abstract static class CancelPledgesTask extends AsyncTask<Void, Void, Boolean>
	{
		public int uid;
		public String outcome;
		
		public CancelPledgesTask(int uid)
		{
			this.uid = uid;
		}
		
		protected Boolean doInBackground(Void... params) {
			// Log.d(TAG, "synching with fitbit servers for user " + mUserId);
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("uid", "" + uid));
		
			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(URL_CANCEL_PLEDGES);
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity entity = response.getEntity();
				String responseStr = EntityUtils.toString(entity);
		
				// Log.d("SYNC_FITBIT_CHECK",responseStr);
		
				// return true;
		
				if(responseStr.contains("error in querying"))
				{
					outcome = "error";
					return false;
				}
				else if (responseStr.contains("OK")) {
					outcome = "OK";
					return true;
				} 
				else if(responseStr.contains("XX")) {
					outcome = "not validated";
					// Constants.AUTHORIZATION_VALIDATED = false;
					// a network connection check should be done to display the
					// correct message.
					return false;
				}
				else
				{
					outcome = "error";
					return false;
				}
		
			} catch (Exception e) {
				// Log.e("CheckFitbitAuthenticationTask",
				// "Error in http connection "+e.toString());
				return false;
			}
		}
		
		@Override
		protected abstract void onPostExecute(Boolean success);		
		
	}
	
	public abstract static class SendPasswordReminderTask extends
	AsyncTask<Void, Void, Boolean> {
	
		public String email;
		public String outcome;
		
		public SendPasswordReminderTask(String email) {
			this.email = email;
		
		}
		
		
		@Override
		protected Boolean doInBackground(Void... params) {
			// Log.d(TAG, "synching with fitbit servers for user " + mUserId);
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("email", "" + email));
		
			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(URL_SEND_REMINDER);
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity entity = response.getEntity();
				String responseStr = EntityUtils.toString(entity);
		
				// Log.d("SYNC_FITBIT_CHECK",responseStr);
		
				// return true;
		
				if(responseStr.contains("error in querying"))
				{
					outcome = "error";
					return false;
				}
				else if (responseStr.contains("OK")) {
					outcome = "OK";
					Constants.AUTHORIZATION_VALIDATED = true;
					return true;
				} 
				else if(responseStr.contains("XX")) {
					outcome = "not validated";
					// Constants.AUTHORIZATION_VALIDATED = false;
					// a network connection check should be done to display the
					// correct message.
					return false;
				}
				else
				{
					outcome = "error";
					return false;
				}
		
			} catch (Exception e) {
				// Log.e("CheckFitbitAuthenticationTask",
				// "Error in http connection "+e.toString());
				return false;
			}
		}
		
		@Override
		protected abstract void onPostExecute(Boolean success);
	}	
	
	public abstract static class CheckFitbitAuthenticationTask extends
	AsyncTask<Void, Void, Boolean> {
	
		private final int mUserId;
		public String outcome;
		public boolean safeExecution;
		
		
		public CheckFitbitAuthenticationTask(int userId, int buddyId) {
			mUserId = userId;
			outcome = "init";
			safeExecution = false;
		
		}
		
		public CheckFitbitAuthenticationTask(int userId) {
			mUserId = userId;
			outcome = "init";
			safeExecution = false;
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			// Log.d(TAG, "synching with fitbit servers for user " + mUserId);
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("uid", "" + mUserId));
		
			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(URL_CHECK_FITBIT);
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity entity = response.getEntity();
				String responseStr = EntityUtils.toString(entity);
		
				// Log.d("SYNC_FITBIT_CHECK",responseStr);
		
				// return true;
		
				if(responseStr.contains("error in querying"))
				{
					outcome = "error";
					safeExecution = true;
					return false;
				}
				else if (responseStr.contains("OK")) {
					outcome = "OK";
					Constants.AUTHORIZATION_VALIDATED = true;
					safeExecution = true;
					return true;
				} 
				else if(responseStr.contains("does not have a record")) {
					outcome = "not validated";
					// Constants.AUTHORIZATION_VALIDATED = false;
					// a network connection check should be done to display the
					// correct message.
					safeExecution = true;
					return false;
				}
				else
				{
					outcome = "error";
					safeExecution = true;
					return false;
				}
		
			} catch (Exception e) {
				// Log.e("CheckFitbitAuthenticationTask",
				// "Error in http connection "+e.toString());
				safeExecution = false;
				return false;
			}
		}
		
		@Override
		protected abstract void onPostExecute(Boolean success);
	}	
	
	public abstract static class SetAvatarTask extends
	AsyncTask<Void, Void, Boolean> {
		private int uid;
		private int avatarid;
		public boolean safeExecution;
		
		/**
		 * @param userId
		 *            the id of the buddy for which the name should be
		 *            retrieved.
		 */
		public SetAvatarTask(int uid, int avatarid) {
			Constants.CONNECTION_ERROR = false;
			this.uid = uid;
			this.avatarid = avatarid;
			safeExecution = false;
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
		
			// Log.d(TAG, "Fetching the list of users");
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("uid", "" + uid));
			nameValuePairs
					.add(new BasicNameValuePair("avatarid", "" + avatarid));
		
			try {
		
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(URL_SET_AVATAR);
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity entity = response.getEntity();
				String responseStr = EntityUtils.toString(entity);
		
				if (responseStr.equals("???")) {
					safeExecution = true;
					return false;
				} else {
		
					MultipartEntity requestForm = new MultipartEntity();
					requestForm.addPart("uid", new StringBody("" + uid));
					requestForm.addPart("avatarid", new StringBody(""
							+ avatarid));
		
					/* Get a SAXParser from the SAXPArserFactory. */
					SAXParserFactory saxParserFactory = SAXParserFactory
							.newInstance();
					SAXParser saxParser = saxParserFactory.newSAXParser();
		
					/* Get the XMLReader of the SAXParser we created. */
					XMLReader xmlReader = saxParser.getXMLReader();
					/*
					 * Create a new ContentHandler and apply it to the
					 * XML-Reader
					 */
		
					UserXMLHandler msgHandler = new UserXMLHandler();
		
					xmlReader.setContentHandler(msgHandler);
		
					InputSource inputSource = new InputSource(
							obtainInputStream(URL_SET_AVATAR, requestForm));
		
					inputSource.setEncoding("utf-8");
		
					// System.out.println("before parse*************");
		
					xmlReader.parse(inputSource);
		
					// System.out.println("parsed correctly****************");
					safeExecution = true;
					return true;
				}
		
			} catch (Exception e) {
				// Log.e(TAG, "Error in http connection "+e.toString());
				Constants.CONNECTION_ERROR = true;
				safeExecution = false;
				return false;
			}
		}
		
		@Override
		protected abstract void onPostExecute(Boolean result);
	}	
	
	
	public abstract static class GetLeaderboardTask extends AsyncTask<Void, Void, Boolean> {
		private int uid;
		private int offset;
		private int isStep;
		public int currentUserRank;
		public boolean safeExecution;
	
		/**
		 * @param userId
		 *            the id of the buddy for which the name should be
		 *            retrieved.
		 */
		public GetLeaderboardTask(int uid, int offset, int isStep) {
			Constants.CONNECTION_ERROR = false;
			this.uid = uid;
			this.offset = offset;
			this.isStep = isStep;
			safeExecution = false;
		}
	
		@Override
		protected Boolean doInBackground(Void... params) {
	
			// Log.d(TAG, "Fetching the list of users");
			/*ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("uid", "" + uid));
			nameValuePairs.add(new BasicNameValuePair("offset", "" + offset));
			nameValuePairs.add(new BasicNameValuePair("isStep", "" + isStep));*/
	
			Log.i(TAG, "GetLeadeboardTask: doInBackground begin.");
			
			try {
	
			/*	HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(URL_GET_LEADERBOARD);
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				
				Log.i(TAG, "GetLeadeboardTask: executing the client");
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity entity = response.getEntity();
				String responseStr = EntityUtils.toString(entity);
	
				//Log.i("GetLeaderboardTask", "response: " + responseStr);
				
				if (responseStr.contains("???")) 
				{				
					return false;
				}*/ 
				//else 
				
					Log.i(TAG, "GetLeadeboardTask: MultipartEntity in progress");
					MultipartEntity requestForm = new MultipartEntity();
					
					requestForm.addPart("uid", new StringBody("" + uid));
					requestForm.addPart("offset", new StringBody("" + offset));
					requestForm.addPart("isStep", new StringBody("" + isStep));
	
					/* Get a SAXParser from the SAXPArserFactory. */
					SAXParserFactory saxParserFactory = SAXParserFactory
							.newInstance();
					SAXParser saxParser = saxParserFactory.newSAXParser();
	
					/* Get the XMLReader of the SAXParser we created. */
					XMLReader xmlReader = saxParser.getXMLReader();
					/*
					 * Create a new ContentHandler and apply it to the
					 * XML-Reader
					 */
	
					LeaderboardXMLHandler msgHandler = new LeaderboardXMLHandler();
	
					xmlReader.setContentHandler(msgHandler);
	
					
					
					InputSource inputSource = new InputSource(
							obtainInputStream(URL_GET_LEADERBOARD, requestForm));
	
					inputSource.setEncoding("utf-8");
	
					
					// System.out.println("before parse*************");
	
					xmlReader.parse(inputSource);
	
					// System.out.println("parsed correctly****************");
	
				//	ArrayList<GroupMember> groupMembers = msgHandler.getGroupMembers();
					
					ArrayList<GroupEntry> groupEntries = msgHandler.getRankedGroups();
					
					if(groupEntries.size() > 0)
					{
						if(isStep == 1)
						{
							fillStepLeaderboard(groupEntries);
						}
						else
						{
							fillFloorLeaderboard(groupEntries);
						}
					}
					else
					{
						safeExecution = true;
						return false;
					}
					
					
					
					safeExecution = true;					
					return true;
				
	
			} catch (Exception e) {
				// Log.e(TAG, "Error in http connection "+e.toString());
				Constants.CONNECTION_ERROR = true;
				safeExecution = false;
				return false;
			}
		}

		public void fillFloorLeaderboard(ArrayList<GroupEntry> groupEntries)
		{
			Constants.leaderboardGroupsFloor.clear();

			// I kept it because we don't want to have the same group, 2
			// times

			Constants.CURRENT_FLOORS_LEADERBOARD_ENTRY = groupEntries.size();
			for(int i = 0; i < groupEntries.size(); i++)
			{
				
				Constants.leaderboardGroupsFloor.add(groupEntries.get(i));
				if(groupEntries.get(i).isMember(AppContext.getInstance().getUserId()))
				{
					this.currentUserRank = (i+1);
				}
			}
		}
		
		public void fillStepLeaderboard(ArrayList<GroupEntry> groupEntries) {
			Constants.leaderboardGroups.clear();

			// I kept it because we don't want to have the same group, 2
			// times
			
			Constants.CURRENT_STEPS_LEADERBOARD_ENTRY = groupEntries.size();
			
			
			for(int i = 0; i < groupEntries.size(); i++)
			{
				Constants.leaderboardGroups.add(groupEntries.get(i));
				if(groupEntries.get(i).isMember(AppContext.getInstance().getUserId()))
				{
					this.currentUserRank = (i+1);
				}
			}
		}
	
		@Override
		protected abstract void onPostExecute(Boolean result);
	}

	public abstract static class GetStepPledgeTask extends
			AsyncTask<Void, Void, Pledge> {

		private int uid;
		public boolean safeExecution;

		/**
		 * @param userId
		 *            the id of the buddy for which the name should be
		 *            retrieved.
		 */
		public GetStepPledgeTask(int uid) {
			this.uid = uid;
			safeExecution = false;
		}

		@Override
		protected Pledge doInBackground(Void... params) {
			// Log.d(TAG, "Getting name of " + mUserId);
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("uid", "" + uid));

			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(URL_GET_STEP_PLEDGE);
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs,
						"UTF-8"));
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity entity = response.getEntity();
				String responseStr = EntityUtils.toString(entity);

				if (responseStr.startsWith("OK")) {
					// TODO: check to get correct date and amount ***********
					String pledgeDate = responseStr.substring(2, 21);
					String pledgeAmountStr = responseStr.substring(21,
							responseStr.length());
					Pledge res = new Pledge(pledgeDate,
							Integer.parseInt(pledgeAmountStr));
					safeExecution = true;
					return res;
				} else {
					safeExecution = true;
					return null;
				}

			} catch (Exception e) {
				// Log.e(TAG, "Error in http connection "+e.toString());
				safeExecution = false;
				return null;
			}
		}

		@Override
		protected abstract void onPostExecute(Pledge result);
	}

	public abstract static class SetStepPledgeTask extends
			AsyncTask<Void, Void, Boolean> {
		private int uid;
		private int step_pledge;
		public boolean safeExecution;
		// private String timestamp;

		/**
		 * @param userId
		 *            the id of the buddy for which the name should be
		 *            retrieved.
		 */
		public SetStepPledgeTask(int uid, int step_pledge) {
			Constants.CONNECTION_ERROR = false;
			this.uid = uid;
			this.step_pledge = step_pledge;
			/*
			 * Calendar cal = Calendar.getInstance(); SimpleDateFormat formatter
			 * = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); this.timestamp =
			 * formatter.format(cal.getTime());
			 */
			safeExecution = false;
		}

		@Override
		protected Boolean doInBackground(Void... params) {

			// Log.d(TAG, "Fetching the list of users");
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("uid", "" + uid));
			// nameValuePairs.add(new BasicNameValuePair("timestamp",
			// timestamp));
			nameValuePairs.add(new BasicNameValuePair("steppledge", ""
					+ step_pledge));

			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(URL_SET_STEP_PLEDGE);
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity entity = response.getEntity();
				String responseStr = EntityUtils.toString(entity);

			} catch (Exception e) {
				// Log.e(TAG, "Error in http connection "+e.toString());
				Constants.CONNECTION_ERROR = true;
				safeExecution = false;
				return false;
			}
			safeExecution = true;
			return true;
		}

		@Override
		protected abstract void onPostExecute(Boolean result);
	}

	public abstract static class GetFloorPledgeTask extends
			AsyncTask<Void, Void, Pledge> {

		private int uid;
		public boolean safeExecution;

		/**
		 * @param userId
		 *            the id of the buddy for which the name should be
		 *            retrieved.
		 */
		public GetFloorPledgeTask(int uid) {
			this.uid = uid;
			safeExecution = false;
		}

		@Override
		protected Pledge doInBackground(Void... params) {
			// Log.d(TAG, "Getting name of " + mUserId);
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("uid", "" + uid));

			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(URL_GET_FLOOR_PLEDGE);
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs,
						"UTF-8"));
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity entity = response.getEntity();
				String responseStr = EntityUtils.toString(entity);

				if (responseStr.startsWith("OK")) {
					// TODO: check to get correct date and amount ***********
					String pledgeDate = responseStr.substring(2, 21);
					String pledgeAmountStr = responseStr.substring(21,
							responseStr.length());
					Pledge res = new Pledge(pledgeDate,
							Integer.parseInt(pledgeAmountStr));
					safeExecution = true;
					return res;
				} else {
					safeExecution = true;
					return null;
				}

			} catch (Exception e) {
				// Log.e(TAG, "Error in http connection "+e.toString());
				safeExecution = false;
				return null;
			}
		}

		@Override
		protected abstract void onPostExecute(Pledge result);
	}

	public abstract static class SetFloorPledgeTask extends
			AsyncTask<Void, Void, Boolean> {
		private int uid;
		private int floor_pledge;
		public boolean safeExecution;
		// private String timestamp;

		/**
		 * @param userId
		 *            the id of the buddy for which the name should be
		 *            retrieved.
		 */
		public SetFloorPledgeTask(int uid, int floor_pledge) {
			Constants.CONNECTION_ERROR = false;
			this.uid = uid;
			this.floor_pledge = floor_pledge;
			/*
			 * Calendar cal = Calendar.getInstance(); SimpleDateFormat formatter
			 * = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); this.timestamp =
			 * formatter.format(cal.getTime());
			 */
			safeExecution = false;
		}

		@Override
		protected Boolean doInBackground(Void... params) {

			// Log.d(TAG, "Fetching the list of users");
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("uid", "" + uid));
			// nameValuePairs.add(new BasicNameValuePair("timestamp",
			// timestamp));
			nameValuePairs.add(new BasicNameValuePair("floorpledge", ""
					+ floor_pledge));

			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(URL_SET_FLOOR_PLEDGE);
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity entity = response.getEntity();
				String responseStr = EntityUtils.toString(entity);

			} catch (Exception e) {
				// Log.e(TAG, "Error in http connection "+e.toString());
				Constants.CONNECTION_ERROR = true;
				safeExecution = false;
				return false;
			}
			safeExecution  = true;
			return true;
		}

		@Override
		protected abstract void onPostExecute(Boolean result);
	}

	public abstract static class GetAvatarTask extends
			AsyncTask<Void, Void, Integer> {

		private int uid;
		public boolean safeExecution;

		/**
		 * @param userId
		 *            the id of the buddy for which the name should be
		 *            retrieved.
		 */
		public GetAvatarTask(int uid) {
			this.uid = uid;
			safeExecution = false;
		}

		@Override
		protected Integer doInBackground(Void... params) {
			// Log.d(TAG, "Getting name of " + mUserId);
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("uid", "" + uid));

			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(URL_GET_AVATAR);
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs,
						"UTF-8"));
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity entity = response.getEntity();
				String responseStr = EntityUtils.toString(entity);

				if (responseStr.startsWith("OK")) {
					String avatarStr = responseStr.substring(2,
							responseStr.length() - 2);
					safeExecution = true;
					return Integer.parseInt(avatarStr);
				} else {
					safeExecution = true;
					return 0;
				}

			} catch (Exception e) {
				// Log.e(TAG, "Error in http connection "+e.toString());
				safeExecution = false;
				return -1;
			}
		}

		@Override
		protected abstract void onPostExecute(Integer result);
	}

	// ASMA END COMMUNITY

	public abstract static class GetListOfUsersForInvitationTask extends
	AsyncTask<Void, Void, Boolean> {

		/**
		 * @param userId
		 *            the id of the buddy for which the name should be
		 *            retrieved.
		 */
		public GetListOfUsersForInvitationTask() {
			Constants.CONNECTION_ERROR = false;
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			// Log.d(TAG, "Fetching the list of users");
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("uid", "" + 0));
		
			try {
		
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(URL_GET_USERS_INV);
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity entity = response.getEntity();
				String responseStr = EntityUtils.toString(entity);
		
				//Log.i("glut_inv", "response: " + responseStr);
				
				if (responseStr.equals("???")) {
					return false;
				} else {
		
					MultipartEntity requestForm = new MultipartEntity();
					requestForm.addPart("uid", new StringBody("" + 0));
					requestForm.addPart("fid", new StringBody("" + 0));
		
					/* Get a SAXParser from the SAXPArserFactory. */
					SAXParserFactory saxParserFactory = SAXParserFactory
							.newInstance();
					SAXParser saxParser = saxParserFactory.newSAXParser();
		
					/* Get the XMLReader of the SAXParser we created. */
					XMLReader xmlReader = saxParser.getXMLReader();
					/*
					 * Create a new ContentHandler and apply it to the
					 * XML-Reader
					 */
		
					UserInvXMLHandler msgHandler = new UserInvXMLHandler();
		
					xmlReader.setContentHandler(msgHandler);
		
					InputSource inputSource = new InputSource(
							obtainInputStream(URL_GET_USERS_INV, requestForm));
		
					inputSource.setEncoding("utf-8");
		
					// System.out.println("before parse*************");
		
					xmlReader.parse(inputSource);
		
					// System.out.println("parsed correctly****************");
		
					ArrayList<UserEntry> userEntries = msgHandler.getMembers();
		
					if(Constants.userEntries == null)
					{
						Constants.userEntries = new ArrayList<UserEntry>();  
					}
					
					if(Constants.buddyName == null)
					{
						Constants.buddyName = "";
					}
					
					Constants.userEntries.clear();
		
					for (int i = 0; i < userEntries.size(); i++) 
					{
						if (	userEntries.get(i) != null
								&& userEntries.get(i).getUserName() != null
								&& Constants.validEmail(userEntries.get(i).getUserEmail())
								&& !userEntries.get(i).getUserName().equals(Constants.buddyName)
								&& !userEntries.get(i).getUserEmail().equals(Constants.buddyName)
								&& !userEntries.get(i).getUserEmail().equals(AppContext.getInstance().getEmail())) 
						{
							Constants.userEntries.add(userEntries.get(i));
						}
					}
		
		
					return true;
				}
		
			} catch (Exception e) {
				// Log.e(TAG, "Error in http connection "+e.toString());
				Constants.CONNECTION_ERROR = true;
				return false;
			}
		}
		
		@Override
		protected abstract void onPostExecute(Boolean result);
	}	
	
	
	public abstract static class GetListOfUsersTask extends
			AsyncTask<Void, Void, Boolean> {

		
		/**
		 * @param userId
		 *            the id of the buddy for which the name should be
		 *            retrieved.
		 */
		
		public boolean safeExecution;
		
		public GetListOfUsersTask() {
			Constants.CONNECTION_ERROR = false;
			safeExecution = false;
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			// Log.d(TAG, "Fetching the list of users");
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("uid", "" + 0));

			try {

				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(URL_GET_USERS);
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity entity = response.getEntity();
				String responseStr = EntityUtils.toString(entity);

				if (responseStr.equals("???")) 
				{
					safeExecution = false;
					return false;
				} else {

					MultipartEntity requestForm = new MultipartEntity();
					requestForm.addPart("uid", new StringBody("" + 0));
					requestForm.addPart("fid", new StringBody("" + 0));

					/* Get a SAXParser from the SAXPArserFactory. */
					SAXParserFactory saxParserFactory = SAXParserFactory
							.newInstance();
					SAXParser saxParser = saxParserFactory.newSAXParser();

					/* Get the XMLReader of the SAXParser we created. */
					XMLReader xmlReader = saxParser.getXMLReader();
					/*
					 * Create a new ContentHandler and apply it to the
					 * XML-Reader
					 */

					UserXMLHandler msgHandler = new UserXMLHandler();

					xmlReader.setContentHandler(msgHandler);

					InputSource inputSource = new InputSource(
							obtainInputStream(URL_GET_USERS, requestForm));

					inputSource.setEncoding("utf-8");

					// System.out.println("before parse*************");

					xmlReader.parse(inputSource);

					// System.out.println("parsed correctly****************");

					ArrayList<UserEntry> userEntries = msgHandler.getMembers();

					// ASMA START VARIABLES FOR COMMUNITY
					ArrayList<GroupMember> groupMemberEntries = msgHandler
							.getGroupMembers();
					// ASMA END VARIABLES FOR COMMUNITY

					/*Constants.userEntries.clear();

					for (int i = 0; i < userEntries.size(); i++) {
						if (userEntries.get(i).getUserName() != null
								&& !userEntries
										.get(i)
										.getUserEmail()
										.equals(AppContext.getInstance()
												.getEmail())) {
							Constants.userEntries.add(userEntries.get(i));
						}
					}*/

					// ASMA START UPDATES FOR COMMUNITY
					Constants.allGroups.clear();

					// I kept it because we don't want to have the same group, 2
					// times
					for (int i = 0; i < groupMemberEntries.size(); i++) 
					{
						
						if(groupMemberEntries.get(i) == null 
								|| groupMemberEntries.get(i).getUid() <= 0
								|| groupMemberEntries.get(i).getFid() <= 0)
						{
							continue;
						}
						
						// if (groupMemberEntries.get(i).getUserName()!=null){
						for (int j = i + 1; j < groupMemberEntries.size(); j++) 
						{
							if(groupMemberEntries.get(j) == null
									|| groupMemberEntries.get(j).getUid() <= 0
									|| groupMemberEntries.get(j).getFid() <= 0)
							{
								continue;
							}
							
							
							if (groupMemberEntries.get(j).getUid() == groupMemberEntries
									.get(i).getFid()
									&& groupMemberEntries.get(j).getFid() == groupMemberEntries
											.get(i).getUid())
								Constants.allGroups
										.add(new GroupEntry(groupMemberEntries
												.get(i), groupMemberEntries
												.get(j), groupMemberEntries
												.get(i).getAvatarid()));
						}
						// }
					}

					/*
					 * DEBUG INFO System.out.println("all groups list:\n");
					 * for(int i=0;i<Constants.allGroups.size();i++)
					 * System.out.println(Constants.allGroups.get(i));
					 */

					// ASMA END UPDATES FOR COMMUNITY

					/*
					 * for(int i = 0; i < Constants.userEntries.size(); i++) {
					 * Log.d(TAG, Constants.userEntries.get(i).toString()); }
					 */
					safeExecution = true;
					return true;
				}

			} catch (Exception e) {
				// Log.e(TAG, "Error in http connection "+e.toString());
				Constants.CONNECTION_ERROR = true;
				safeExecution = false;
				return false;
			}
		}

		@Override
		protected abstract void onPostExecute(Boolean result);
	}

	public abstract static class CheckInvitationDateTask extends
			AsyncTask<Void, Void, String> {
		private int uid;
		private int warnUser;

		public CheckInvitationDateTask(int uid, boolean warnUser) {
			this.uid = uid;
			this.warnUser = warnUser ? 1 : 0;
		}

		protected String doInBackground(Void... params) {
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("uid", "" + uid));
			nameValuePairs
					.add(new BasicNameValuePair("warnUser", "" + warnUser));

			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(URL_CHECKINVITEDATE);
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs,
						"UTF-8"));
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity entity = response.getEntity();
				String responseStr = EntityUtils.toString(entity);
				// Log.i("CHECK_INV_RESPONSE",responseStr);

				if (responseStr.startsWith("OK")) {
					return responseStr.substring(2);
				} else {
					return responseStr;
				}

			} catch (Exception e) {
				// Log.e(TAG, "Error in http connection "+e.toString());
				return null;
			}

		}

	}

	public abstract static class RetrieveExistingBuddyInfoTask extends
	AsyncTask<Void, Void, Boolean> {
		private int mUserId;
		
		public int retrievedBuddyId;
		public String retrievedBuddyName;
		
		public boolean connectionErrorRaised;
		public boolean safeExecution;
		
		/**
		 * @param userId
		 *            the id of the buddy for which the name should be
		 *            retrieved.
		 */
		public RetrieveExistingBuddyInfoTask(int userId) {
			mUserId = userId;
			connectionErrorRaised = false;
			safeExecution = false;
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			// Log.d(TAG, "Getting name of " + mUserId);
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("uid", "" + mUserId));
		
			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(URL_BUDDYINFO);
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs,
						"UTF-8"));
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity entity = response.getEntity();
				String responseStr = EntityUtils.toString(entity);
		
				if (responseStr.startsWith("OK")) 
				{
					responseStr = Utils.dataCheck(responseStr);
					
					//Log.i("RetrieveExistingBuddyInfoTask","responseStr = " + responseStr);
					String data = responseStr.substring(2);
					
					int splitPoint = data.indexOf(',');
					
					//Log.i("RetrieveExistingBuddyInfoTask","id to extract = " + data.substring(0, splitPoint));
					//Log.i("RetrieveExistingBuddyInfoTask","name to extract = " + data.substring(splitPoint+1));
					
					retrievedBuddyId = Integer.parseInt(data.substring(0, splitPoint));
					retrievedBuddyName = data.substring(splitPoint+1);					
					
					//Log.i("RetrieveExistingBuddyInfoTask","retrievedBuddyId = " + retrievedBuddyId);
					//Log.i("RetrieveExistingBuddyInfoTask", "retrievedBuddyName = " + retrievedBuddyName);

					
					
					Constants.BUDDY_NAME_ACQUIRED = true;
					safeExecution = true;
					return true;
				} 
				else 
				{
					safeExecution = true;
					return false;
				}
		
			} catch (Exception e) {
				
				String reason = e.toString();
				
				if(reason != null)
				{
					Log.e(TAG, "Error in http connection "+e.toString());
				}
				connectionErrorRaised = true;
				safeExecution = false;
				return false; //"Error in http connection " + e.toString();
			}
		}
		
		@Override
		protected abstract void onPostExecute(Boolean result);
		}	
	
	public abstract static class RetrieveBuddyNameTask extends
			AsyncTask<Void, Void, String> {

		private int mUserId;
		public boolean safeExecution;

		/**
		 * @param userId
		 *            the id of the buddy for which the name should be
		 *            retrieved.
		 */
		public RetrieveBuddyNameTask(int userId) {
			mUserId = userId;
			safeExecution = false;
		}

		@Override
		protected String doInBackground(Void... params) {
			// Log.d(TAG, "Getting name of " + mUserId);
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("uid", "" + mUserId));

			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(URL_BUDDYNAME);
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs,
						"UTF-8"));
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity entity = response.getEntity();
				String responseStr = EntityUtils.toString(entity);

				if (responseStr.startsWith("OK")) {
					responseStr = Utils.dataCheck(responseStr);
					Constants.BUDDY_NAME_ACQUIRED = true;
					safeExecution = true;
					return responseStr.substring(2);
				} else {
					safeExecution = true;
					return responseStr;
				}

			} catch (Exception e) {
				// Log.e(TAG, "Error in http connection "+e.toString());
				
				String reason = e.toString();
				
				if(reason == null)
				{
					reason = "";
				}
				
				safeExecution = false;
				return "Error in http connection " + reason;
			}
		}

		@Override
		protected abstract void onPostExecute(String result);
	}

	/**
	 * Triggers the synchronization of the backend with the fitbit server.
	 * 
	 */
	public abstract static class SyncBackendWithFitbitTask extends
			AsyncTask<Void, Void, Boolean> {

		private final int mUserId;
		private final int mBuddyId;
		private final String dateOfInterest;
		public boolean safeExecution;

		public SyncBackendWithFitbitTask(int userId, int buddyId) {
			mUserId = userId;
			mBuddyId = buddyId;
			dateOfInterest = null;
			safeExecution = false;
		}

		public SyncBackendWithFitbitTask(int userId, int buddyId, String date) {
			mUserId = userId;
			mBuddyId = buddyId;
			dateOfInterest = date;
			safeExecution = false;
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			// Log.d(TAG, "synching with fitbit servers for user " + mUserId);
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("uid", "" + mUserId));
			nameValuePairs.add(new BasicNameValuePair("fid", "" + mBuddyId));

			if (dateOfInterest != null) {
				nameValuePairs.add(new BasicNameValuePair("date", ""
						+ dateOfInterest));
			}

			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(
						"http://grpupc1.epfl.ch/~yu/htexp/synch5.php");
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity entity = response.getEntity();
				String responseStr = EntityUtils.toString(entity);

				//Log.d("SYNC_FITBIT_CHECK",responseStr);

				// return true;

				if (responseStr.contains("HTMessage Updated")
						|| responseStr.contains("SQL syntax;")) { 
																	// Improve
																	// check
																	// sync was
																	// successful
					Constants.AUTHORIZATION_VALIDATED = true;
					safeExecution = true;
					return true;
				} else {
					safeExecution = true;
					// sync did not succeed, user need to authorize again
					// The actual data we receive here is the fitbit authorize
					// page
					return false;
				}

			} catch (Exception e) {
				// Log.e("SyncBackendWithFitbitTask",
				// "Error in http connection "+e.toString());
				safeExecution = false;
				return false;
			}
		}

		@Override
		protected abstract void onPostExecute(Boolean success);
	}

	/*
	 * Remove an existing log.
	 */
	public abstract static class RemoveLogTask extends
			AsyncTask<Void, Void, Boolean> {

		private final int mBuddyId;
		private final int mUserId;
		private final String mMsg;

		public RemoveLogTask(int userId, int buddyId, String msg) {
			mUserId = userId;
			mBuddyId = buddyId;
			mMsg = msg;
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("uid", "" + mUserId));
			nameValuePairs.add(new BasicNameValuePair("fid", "" + mBuddyId));
			nameValuePairs.add(new BasicNameValuePair("msgtxt", mMsg));

			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(URL_DEL_LOG);
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				// Log.i(TAG,"Removing the log of " + mUserId +
				// " with message: " + mMsg);
				HttpResponse response = httpclient.execute(httppost);

				HttpEntity entity = response.getEntity();
				String responseStr = EntityUtils.toString(entity);
				responseStr = Utils.removeNewLines(responseStr);
				// Log.i("LOG_DEL_RESPONSE","("+responseStr+")");
				return responseStr.equals("OK");
			} catch (Exception e) {
				Log.e(TAG, "Error in http connection " + e.toString());
				return false;
			}
		}

		/**
		 * @param success
		 *            <code>true</code> if the message was successfully sent
		 */
		@Override
		protected abstract void onPostExecute(Boolean success);
	}

	public abstract static class ChangeLogTask extends
			AsyncTask<Void, Void, Boolean> {

		private final int mBuddyId;
		private final int mUserId;
		private final String mMsg;
		private final String oldMsg;

		public ChangeLogTask(int userId, int buddyId, String msg, String oMsg) {
			mUserId = userId;
			mBuddyId = buddyId;
			mMsg = msg;
			oldMsg = oMsg;
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("uid", "" + mUserId));
			nameValuePairs.add(new BasicNameValuePair("fid", "" + mBuddyId));
			nameValuePairs.add(new BasicNameValuePair("msgtxt", mMsg));
			nameValuePairs.add(new BasicNameValuePair("oldmsgtxt", oldMsg));

			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(URL_SET_LOG);
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				// Log.i(TAG,"Changing the log of " + mUserId +
				// " with message: <" + oldMsg + "> to <" + mMsg + ">");
				HttpResponse response = httpclient.execute(httppost);

				HttpEntity entity = response.getEntity();
				String responseStr = EntityUtils.toString(entity);
				responseStr = Utils.removeNewLines(responseStr);
				// Log.i("LOG_SET_RESPONSE","("+responseStr+")");
				return responseStr.equals("OK");
			} catch (Exception e) {
				Log.e(TAG, "Error in http connection " + e.toString());
				return false;
			}
		}

		/**
		 * @param success
		 *            <code>true</code> if the message was successfully sent
		 */
		@Override
		protected abstract void onPostExecute(Boolean success);
	}

	/**
	 * Sends a log to a database, with the specified date.
	 * 
	 * Maps to ServerHelper.msgBuddy
	 * 
	 */
	public abstract static class SendTimelyLogTask extends
			AsyncTask<Void, Void, Boolean> {

		private final int mBuddyId;
		private final int mUserId;
		private final String mMsg;
		//private final java.sql.Timestamp sqlDate;
		private final String sqlDate;
		public boolean safeExecution;
		

		public SendTimelyLogTask(int userId, int buddyId, String msg, String d) {
			mUserId = userId;
			mBuddyId = buddyId;
			mMsg = msg;
			
			sqlDate = d;
			/*SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			java.util.Date myDate = null;
			try {
				myDate = formatter.parse(d);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			
			if(myDate == null)
			{
				myDate = new Date();
				Log.e("SendTimelyLog","Send with current date; I had a problem in converting this string: " + d);
			}
				
			sqlDate = new java.sql.Timestamp(myDate.getTime());*/
			Constants.BUDDY_DECLINED = false;
			safeExecution = false;
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("uid", "" + mUserId));
			nameValuePairs.add(new BasicNameValuePair("fid", "" + mBuddyId));
			nameValuePairs.add(new BasicNameValuePair("msgtxt", mMsg));
			nameValuePairs.add(new BasicNameValuePair("time", sqlDate)); // .toString()

			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(URL_LOG);
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs,
						"UTF-8"));
				// Log.i(TAG,"Sending msg from " + mUserId + " to " + mBuddyId);
				HttpResponse response = httpclient.execute(httppost);

				HttpEntity entity = response.getEntity();
				String responseStr = EntityUtils.toString(entity);
				responseStr = Utils.removeNewLines(responseStr);
				// Log.i("MSG_SEND_RESPONSE","("+responseStr+")");
				Constants.BUDDY_DECLINED = responseStr.equals("RM");
				Log.d(TAG, "LOG_RESPONSE: " + responseStr);
				safeExecution = true;
				return responseStr.equals("OK");
			} catch (Exception e) {
				
				String reason = e.toString();
				safeExecution = false;
				
				if(reason != null)
					Log.e(TAG, "Error in http connection " + e.toString());
				return false;
			}
		}

		/**
		 * @param success
		 *            <code>true</code> if the message was successfully sent
		 */
		@Override
		protected abstract void onPostExecute(Boolean success);
	}
	
	
	/**
	 * Sends a message to a friend.
	 * 
	 * Maps to ServerHelper.msgBuddy
	 * 
	 */
	public abstract static class SendMessageTask extends
			AsyncTask<Void, Void, Boolean> {

		private final int mBuddyId;
		private final int mUserId;
		private final String mMsg;

		public SendMessageTask(int userId, int buddyId, String msg) {
			mUserId = userId;
			mBuddyId = buddyId;
			mMsg = msg;
			Constants.BUDDY_DECLINED = false;
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("uid", "" + mUserId));
			nameValuePairs.add(new BasicNameValuePair("fid", "" + mBuddyId));
			nameValuePairs.add(new BasicNameValuePair("msgtxt", mMsg));

			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(URL_MSG);
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs,
						"UTF-8"));
				// Log.i(TAG,"Sending msg from " + mUserId + " to " + mBuddyId);
				HttpResponse response = httpclient.execute(httppost);

				HttpEntity entity = response.getEntity();
				String responseStr = EntityUtils.toString(entity);
				responseStr = Utils.removeNewLines(responseStr);
				// Log.i("MSG_SEND_RESPONSE","("+responseStr+")");
				Constants.BUDDY_DECLINED = responseStr.equals("RM");
				// Log.d(TAG, "MESSAGE_RESPONSE: " + responseStr);
				return responseStr.equals("OK");
			} catch (Exception e) {
				Log.e(TAG, "Error in http connection " + e.toString());
				return false;
			}
		}

		/**
		 * @param success
		 *            <code>true</code> if the message was successfully sent
		 */
		@Override
		protected abstract void onPostExecute(Boolean success);
	}

	/**
	 * Retrieves the message history between 2 users (this means all messanges
	 * they exchanged).
	 * 
	 * Maps to ServerHelper.getMsgs
	 * 
	 */
	public abstract static class RetrieveAllMessagesTask extends
			AsyncTask<Void, Void, ArrayList<MessageMeta>> {

		private final int mBuddyId;
		private final int mUserId;

		public RetrieveAllMessagesTask(int userId, int buddyId) {
			mUserId = userId;
			mBuddyId = buddyId;
		}

		@Override
		protected ArrayList<MessageMeta> doInBackground(Void... params) {
			try {
				MultipartEntity requestForm = new MultipartEntity();
				requestForm.addPart("uid", new StringBody("" + mUserId));
				requestForm.addPart("fid", new StringBody("" + mBuddyId));
				// Log.d(TAG, "Getting messages from " + mUserId + " to " +
				// mBuddyId);

				/* Get a SAXParser from the SAXPArserFactory. */
				SAXParserFactory saxParserFactory = SAXParserFactory
						.newInstance();
				SAXParser saxParser = saxParserFactory.newSAXParser();

				/* Get the XMLReader of the SAXParser we created. */
				XMLReader xmlReader = saxParser.getXMLReader();
				/* Create a new ContentHandler and apply it to the XML-Reader */

				MsgXMLHandler msgHandler = new MsgXMLHandler();

				xmlReader.setContentHandler(msgHandler);

				InputSource inputSource = new InputSource(obtainInputStream(
						URL_GETMSGS, requestForm));

				inputSource.setEncoding("utf-8");

				xmlReader.parse(inputSource);

				return msgHandler.getMembers();
			} catch (Exception e) {
				e.printStackTrace();
				return new ArrayList<MessageMeta>();
			}
		}

		/**
		 * @return a list of all the messages
		 */
		@Override
		protected abstract void onPostExecute(ArrayList<MessageMeta> messages);
	}

	/**
	 * Retrieves the badges for a user
	 * 
	 * Maps to ServerHelper.checkBadges
	 */
	public abstract static class RetrieveBadgesTask extends
			AsyncTask<Void, Void, BadgeResult> {

		private int mUserId;

		/**
		 * @param userId
		 *            the id of the user for which the badges should be
		 *            retrieved.
		 */
		public RetrieveBadgesTask(int userId) {
			mUserId = userId;
		}

		@Override
		protected BadgeResult doInBackground(Void... params) {
			// Log.d(TAG, "Getting badges of " + mUserId);
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("uid", "" + mUserId));

			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(URL_BADGE);
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity entity = response.getEntity();
				// is = entity.getContent();
				String responseStr = EntityUtils.toString(entity);
				// Log.i("BADGE_CHECK",responseStr);

				if (responseStr.startsWith("OK")) {
					responseStr = Utils.removeNewLines(responseStr);
					// Log.i("BADGE_CHECK",responseStr.substring(2)+")");
					String res = responseStr.substring(2);
					String[] parts = res.split(",");
					int badgeCount = Integer.parseInt(parts[0]);
					int badgeType = Integer.parseInt(parts[1]);
					// step_sum = Integer.parseInt(parts[2]); // XX CRASH!
					// Log.d(TAG, "Retrieved badges of " + mUserId + ": " +
					// badgeCount + " / highest: " + badgeType);
					BadgeResult result = new BadgeResult(mUserId, badgeCount,
							badgeType);
					return result;
				} else {
					return new BadgeResult("Error2");
				}

			} catch (Exception e) {
				Log.e("RetrieveBadgesTask",
						"Error in http connection " + e.toString());
				return new BadgeResult("Error3");
			}
		}

		@Override
		protected abstract void onPostExecute(BadgeResult result);
	}

	public abstract static class CheckBadgesTask extends
			AsyncTask<Void, Void, ArrayList<BadgeArray>> {

		private int mUserId;
		private int bbadge_type;

		/**
		 * @param userId
		 *            the id of the user for which the badges should be
		 *            retrieved.
		 */
		public CheckBadgesTask(int userId) {
			mUserId = userId;
		}

		@Override
		protected ArrayList<BadgeArray> doInBackground(Void... params) {
			// Log.d(TAG, "Getting badges of " + mUserId);
			// ArrayList<NameValuePair> nameValuePairs = new
			// ArrayList<NameValuePair>();
			ArrayList<BadgeArray> arrs = new ArrayList<BadgeArray>();

			for (int i = 0; i < Constants.BADGE_COUNT; i++) {
				bbadge_type = i + 1;
				InputSource inputSource = null;
				try {
					MultipartEntity requestForm = new MultipartEntity();
					requestForm.addPart("uid", new StringBody("" + mUserId));
					requestForm.addPart("badge_type", new StringBody(""
							+ bbadge_type));
					// Log.d(TAG, "Getting badges of " + mUserId +
					// " with level " + bbadge_type);

					/* Get a SAXParser from the SAXPArserFactory. */
					SAXParserFactory saxParserFactory = SAXParserFactory
							.newInstance();
					SAXParser saxParser = saxParserFactory.newSAXParser();

					/* Get the XMLReader of the SAXParser we created. */
					XMLReader xmlReader = saxParser.getXMLReader();
					/*
					 * Create a new ContentHandler and apply it to the
					 * XML-Reader
					 */

					BadgesXMLHandler msgHandler = new BadgesXMLHandler();

					xmlReader.setContentHandler(msgHandler);

					inputSource = new InputSource(obtainInputStream(
							URL_GETBADGE, requestForm));

					inputSource.setEncoding("utf-8");

					xmlReader.parse(inputSource);

					BadgeArray arr = new BadgeArray();

					arr.setBadgesList(msgHandler.getMembers());

					for (int j = 0; j < arr.getBadgesList().size(); j++) {
						arr.getBadgesList().get(j).activateBadges(bbadge_type);
					}

					arrs.add(arr);
				} catch (Exception e) {
					e.printStackTrace();
					Log.e(TAG, "*************Error in fetching badges!!!");
					if (inputSource != null) {
						Log.e(TAG, inputSource.toString());
					}
					// return new ArrayList<BadgeArray>();
				}
			}
			return arrs;

		}

		@Override
		protected abstract void onPostExecute(ArrayList<BadgeArray> result);
	}

	/**
	 * Checks if there are new messages for a user and retrieves them. Actually
	 * returns the NEXT unread message from the server. For each request only a
	 * SINGLE message is returned!
	 * 
	 * Maps to ServerHelper.checkMessages
	 */
	public abstract static class CheckMessagesTask extends
			AsyncTask<Void, Void, String> {

		private int mUserId;
		public boolean safeExecution;

		/**
		 * @param userId
		 *            the id of the user for which the messages should be
		 *            retrieved.
		 */
		public CheckMessagesTask(int userId) {
			mUserId = userId;
			safeExecution = false;
		}

		@Override
		protected String doInBackground(Void... params) {
			// Log.d(TAG, "Getting messages of " + mUserId);
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("uid", "" + mUserId));

			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(URL_GETMSG);
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity entity = response.getEntity();
				// is = entity.getContent();
				String responseStr = EntityUtils.toString(entity);
				// Log.i("MSG_CHECK",responseStr);

				if (responseStr.startsWith("OK")) {
					responseStr = Utils.removeNewLines(responseStr);
					// Log.i("MSG_CHECK",responseStr.substring(2)+")");
					safeExecution = true;
					return responseStr.substring(2);
				} else {
					safeExecution = true;
					return "";
				}

			} catch (Exception e) {
				
				String reason = e.toString();
				if(reason == null)
				{
					reason = "unspecified error";
				}
				
				ErrorHandler.create().handleError(App.getInstance(),"Error in http connection " + e.toString(), null);
				Log.e("TAG", "Error in http connection " + e.toString());
				safeExecution = false;
				return "";
			}
		}

		@Override
		protected abstract void onPostExecute(String message);
	}

	/**
	 * Retrieves the step count of the user with the given id. Onur: the
	 * retrieval is from HealthyTogether database! Maps to
	 * ServerHelper.getCurrentStep
	 */
	public abstract static class RetrieveStepCountTask extends
			AsyncTask<Void, Void, Integer> {

		private int mUserId;
		private int dateOffset;
		public boolean safeExecution;

		/**
		 * @param userId
		 *            the id of the user for which the step count hsould be
		 *            retrieved.
		 */
		public RetrieveStepCountTask(int userId, int datePivot) {
			mUserId = userId;
			dateOffset = datePivot;
			safeExecution = false;
		}

		@Override
		protected Integer doInBackground(Void... params) {
			// Log.d(TAG, "Getting steps of " + mUserId);
			int step = 0;

			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("uid", "" + mUserId));
			nameValuePairs
					.add(new BasicNameValuePair("offset", "" + dateOffset));

			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(URL_STEPS);
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity entity = response.getEntity();
				// is = entity.getContent();
				String responseStr = EntityUtils.toString(entity);
				// Log.i("STEP_CHECK",responseStr);

				if (responseStr.startsWith("OK")) {
					responseStr = Utils.dataCheck(responseStr);
					String str = responseStr.substring(2).split(",")[0];
					step = Integer.parseInt(str);
					// Log.d(TAG,"*************GET_STEPS: "+step +
					// ", UID="+mUserId);
					safeExecution = true;
					return step;
				} else {
					safeExecution = true;
					return 0;
				}
			} catch (Exception e) {
				safeExecution = false;
				String reason = e.toString();
				
				if(reason != null)
					Log.e("RetrieveStepCountTask","Error in http connection " + e.toString());
				return 0;
			}

		}

		@Override
		protected abstract void onPostExecute(Integer stepCount);
	}

	/*public abstract static class RetrieveStepCountTask extends
	AsyncTask<Void, Void, Integer> {
	
	}*/
	
	
	// Wrapper function for the alternative of "sequentialGetSteps"
	public static void getMemberSteps(int uid1, int datePivot, final GroupEntry ge, final int ind)
	{
		RetrieveStepCountTask task = new RetrieveStepCountTask(uid1, datePivot)
		{
			protected void onPostExecute(Integer step1)
			{
				if(!this.safeExecution)
				{
					ge.steps[ind] = step1;
					ge.stepCheck[ind] = true;
				}
			}
		};
		task.execute();
	}
	
	public static void getMemberFloors(int uid1, int datePivot, final GroupEntry ge, final int ind)
	{
		RetrieveFloorCountTask task = new RetrieveFloorCountTask(uid1, datePivot)
		{
			protected void onPostExecute(Integer f1)
			{
				if(!this.safeExecution)
				{
					ge.floors[ind] = f1;
					ge.floorCheck[ind] = true;
				}
			}
		};
		task.execute();
	}	
	
	// ASMA START GETTING STEPS
	// for the sake of synchronization and the fact that something should happen
	// in order
	// I wrote this function. It just returns number of steps but in a
	// sequential not thread based manner	
	
	public static Integer sequentialGetSteps(int userId, int datePivot, GroupEntry ge, int ind) {
		int step = 0;

		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("uid", "" + userId));
		nameValuePairs.add(new BasicNameValuePair("offset", "" + datePivot));

		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(URL_STEPS);
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			// is = entity.getContent();
			String responseStr = EntityUtils.toString(entity);
			// Log.i("STEP_CHECK",responseStr);

			if (responseStr.startsWith("OK")) {
				responseStr = Utils.dataCheck(responseStr);
				String str = responseStr.substring(2).split(",")[0];
				step = Integer.parseInt(str);
				// Log.d(TAG,"*************GET_STEPS: "+step +
				// ", UID="+mUserId);
				ge.steps[ind] = step;
				return step;
			} else {
				ge.steps[ind] = 0;
				return 0;
			}
		} catch (Exception e) {
			Log.e("RetrieveStepCountTask",
					"Error in http connection " + e.toString());
			return 0;
		}
	}

	public static Integer sequentialGetFloors(int userId, int datePivot, GroupEntry ge, int ind) {
		int floor = 0;

		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("uid", "" + userId));
		nameValuePairs.add(new BasicNameValuePair("offset", "" + datePivot));

		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(URL_FLOORS);
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			// is = entity.getContent();
			String responseStr = EntityUtils.toString(entity);
			// Log.i("STEP_CHECK",responseStr);

			if (responseStr.startsWith("OK")) {
				responseStr = Utils.dataCheck(responseStr);
				String str = responseStr.substring(2).split(",")[0];
				floor = Integer.parseInt(str);
				// Log.d(TAG,"*************GET_STEPS: "+step +
				// ", UID="+mUserId);
				ge.floors[ind] = floor;
				return floor;
			} else {
				ge.floors[ind] = 0;
				return 0;
			}
		} catch (Exception e) {
			Log.e("RetrieveFloorCountTask",
					"Error in http connection " + e.toString());
			return 0;
		}
	}

	// ASMA END GETTING STEPS & FLOORS

	/**
	 * Retrieves the step count of the user with the given id. Onur: the
	 * retrieval is from HealthyTogether database! Maps to
	 * ServerHelper.getCurrentStep
	 */
	public abstract static class RetrieveFloorCountTask extends
			AsyncTask<Void, Void, Integer> {

		private int mUserId;
		private int offset;
		public boolean safeExecution;

		/**
		 * @param userId
		 *            the id of the user for which the step count hsould be
		 *            retrieved.
		 */
		public RetrieveFloorCountTask(int userId, int datePivot) {
			mUserId = userId;
			offset = datePivot;
			safeExecution = false;
		}

		@Override
		protected Integer doInBackground(Void... params) {
			// Log.d(TAG, "Getting floors of " + mUserId);
			int floor = 0;

			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("uid", "" + mUserId));
			nameValuePairs.add(new BasicNameValuePair("offset", "" + offset));

			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(URL_FLOORS);
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity entity = response.getEntity();
				// is = entity.getContent();
				String responseStr = EntityUtils.toString(entity);
				// Log.i("FLOOR_CHECK",responseStr);

				if (responseStr.startsWith("OK")) {
					responseStr = Utils.dataCheck(responseStr);
					String str = responseStr.substring(2).split(",")[0];
					floor = Integer.parseInt(str);
					safeExecution = true;
					return floor;
				} else {
					safeExecution = true;
					return 0;
				}
			} catch (Exception e) {
				
				String reason = e.toString();
				
				if(reason != null)
					Log.e("RetrieveFloorCountTask",
							"Error in http connection " + e.toString());
				
				safeExecution = false;
				return 0;
			}
		}

		@Override
		protected abstract void onPostExecute(Integer stepCount);
	}

	/**
	 * Checks if the user already has formed a group with a buddy. If a positive
	 * value is returned it is the user id of the buddy otherwise a status code.
	 */
	public abstract static class CheckGroupTask extends
			AsyncTask<Void, Void, String> {

		public static final int RESPONSE_CODE_NO_BUDDY = 0;
		public static final int RESPONSE_WAITING_FOR_ACCEPT = -1;
		public static final int RESPONSE_INCOMING_REQUEST_PENDING = -2;

		private String mEemail;
		private int mUserId;
		public boolean safeExecution;

		public CheckGroupTask(String email, int userId) {
			mEemail = email;
			mUserId = userId;
			safeExecution = false;
		}

		@Override
		protected String doInBackground(Void... params) {
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("email", mEemail));
			nameValuePairs.add(new BasicNameValuePair("uid", "" + mUserId));

			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(URL_GROUP);
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity entity = response.getEntity();
				// is = entity.getContent();
				String responseStr = EntityUtils.toString(entity);
				// Log.i("GROUP_CHECK",responseStr);

				if (responseStr.startsWith("OK")) {
					responseStr = Utils.removeNewLines(responseStr);
					// Log.i("GROUP_RESPONSE",responseStr.substring(2)+")");
					safeExecution = true;
					return responseStr.substring(2);

				} else {
					safeExecution = true;
					return responseStr;
				}

			} catch (Exception e) {
				
				safeExecution = false;
				
				String reason = e.toString();
				
				if(reason != null)
				{
					Log.e("CheckGroupTask",
							"Error in http connection " + e.toString());
					return "Error in http connection " + e.toString();
				}
				
				return null;
			}
		}

		@Override
		protected abstract void onPostExecute(String result);
	}

	/**
	 * Sends a reminder email to the pending buddy.
	 * 
	 * Maps to ServerHelper.remindBuddy.
	 */
	public abstract static class RemindPendingBuddyTask extends
			AsyncTask<Void, Void, Boolean> {

		private String mBuddyEemail;
		private int mUserId;

		public RemindPendingBuddyTask(int userId, String buddyEmail) {
			mBuddyEemail = buddyEmail;
			mUserId = userId;
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("femail", mBuddyEemail));
			nameValuePairs.add(new BasicNameValuePair("uid", "" + mUserId));

			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(URL_REMIND);
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity entity = response.getEntity();
				String responseStr = EntityUtils.toString(entity);
				// Log.i("REMIND_RESPONSE",responseStr);

				return responseStr.equals("OK");
			} catch (Exception e) {
				Log.e("RemindPendingBuddyTask",
						"Error in http connection " + e.toString());
				return false;
			}
		}

		/**
		 * @return <code>true</code> if the reminder was successfully sent
		 */
		@Override
		protected abstract void onPostExecute(Boolean success);
	}

	/**
	 * Retrieves the email of the pending (not accepted yet) buddy.
	 * 
	 * Maps to ServerHelper.checkPending.
	 */
	public abstract static class RetrievePendingBuddyEMailTask extends
			AsyncTask<Void, Void, String> {

		private int userId;

		public RetrievePendingBuddyEMailTask(int myUserId) {
			userId = myUserId;
		}

		@Override
		protected String doInBackground(Void... params) {
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("uid", "" + userId));

			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(URL_PENDING);
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity entity = response.getEntity();
				// is = entity.getContent();
				String responseStr = EntityUtils.toString(entity);
				// Log.i("PENDING_CHECK",responseStr);

				if (responseStr.startsWith("OK")) {
					responseStr = Utils.dataCheck(responseStr);
					// Log.i("PENDING_RESPONSE",responseStr.substring(2)+")");

					Constants.INVITATION_ACCEPTED = true;

					return responseStr.substring(2);

				} else if (responseStr.startsWith("CI")) {
					Constants.COUNTER_INVITE = true;
					responseStr = Utils.dataCheck(responseStr);
					// Log.i("PENDING_RESPONSE",responseStr.substring(2)+")");

					return responseStr.substring(2);
				} else {

					return responseStr;
				}

			} catch (Exception e) {
				Log.e("RetrievePendingBuddyEMailTask",
						"Error in http connection " + e.toString());
				return "Error in http connection " + e.toString();
			}
		}

		@Override
		protected abstract void onPostExecute(String email);
	}

	/**
	 * Retrieves the email of the buddy. Maps to ServerHelper.checkBuddy.
	 * 
	 */
	public abstract static class RetrieveBuddyEMailTask2 extends
			AsyncTask<Void, Void, String> {

		private String mMyEmail;
		public boolean safeExecution;
		
		
		public RetrieveBuddyEMailTask2(String myEmail) {
			mMyEmail = myEmail;
			safeExecution = false;
		}

		@Override
		protected String doInBackground(Void... params) {
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("email", mMyEmail));

			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(URL_BUDDY2);
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity entity = response.getEntity();
				// is = entity.getContent();
				String responseStr = EntityUtils.toString(entity);
				// Log.i("BUDDY_CHECK",responseStr);

				if (responseStr.startsWith("OK")) {
					responseStr = Utils.removeNewLines(responseStr);
					// Log.i("BUDDY_RESPONSE",responseStr.substring(2)+")");
					safeExecution = true;
					return responseStr.substring(2);
				} else {
					safeExecution = true;
					return responseStr;
				}

			} catch (Exception e) {
				
				safeExecution = false;
				String reason = e.toString();
				if(reason != null)
				{
					Log.e("RetrieveBuddyEMailTask2", "Error in http connection "
							+ e.toString());
					return "Error in http connection " + e.toString();
				}
				
				return null;
			}
		}

		@Override
		protected abstract void onPostExecute(String result);
	}

	/**
	 * Retrieves the email of the buddy. Maps to ServerHelper.checkBuddy.
	 * 
	 */
	public abstract static class RetrieveBuddyEMailTask extends
			AsyncTask<Void, Void, String> {

		private String mMyEmail;
		public boolean safeExecution;
		
		public RetrieveBuddyEMailTask(String myEmail) {
			mMyEmail = myEmail;
			safeExecution = false;
		}

		@Override
		protected String doInBackground(Void... params) {
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("email", mMyEmail));

			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(URL_BUDDY);
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity entity = response.getEntity();
				// is = entity.getContent();
				String responseStr = EntityUtils.toString(entity);
				// Log.i("BUDDY_CHECK",responseStr);

				if (responseStr.startsWith("OK")) {
					responseStr = Utils.removeNewLines(responseStr);
					// Log.i("BUDDY_RESPONSE",responseStr.substring(2)+")");
					safeExecution = true;
					return responseStr.substring(2);
				} else {
					safeExecution = true;
					return responseStr;
				}

			} catch (Exception e) {
				safeExecution = false;
				String reason = e.toString();
				if(reason != null)
				{
					Log.e("RetrieveBuddyEMailTask", "Error in http connection "
							+ e.toString());
					return "Error in http connection " + e.toString();
				}
				
				return null;
			}
		}

		@Override
		protected abstract void onPostExecute(String result);
	}

	public abstract static class GetStartDateForGameTask extends
			AsyncTask<Void, Void, String> {
		private int mUserId;
		private String mEmail;
		private String mBuddyEMail;
		
		public boolean safeExecution;

		public GetStartDateForGameTask(int userId, String email,
				String buddyEMail) {
			mUserId = userId;
			mEmail = email;
			mBuddyEMail = buddyEMail;
			safeExecution = false;
		}

		@Override
		protected String doInBackground(Void... params) {
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("uid", "" + mUserId));
			nameValuePairs.add(new BasicNameValuePair("email", "" + mEmail));
			nameValuePairs.add(new BasicNameValuePair("femail", ""
					+ mBuddyEMail));

			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(URL_GETSTARTDATE);
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity entity = response.getEntity();
				// is = entity.getContent();
				String responseStr = EntityUtils.toString(entity);
				// Log.i("ACCEPT_CHECK",responseStr);

				if (responseStr.startsWith("OK")) {
					responseStr = Utils.removeNewLines(responseStr);
					// Log.i("ACCEPT_CHECK",responseStr.substring(2)+")");

					// YYYY-mm-dd
					if (responseStr.length() > 2) {
						safeExecution = true;
						return responseStr.substring(2, 12);
					} else {
						safeExecution = true;
						return "NF";
					}

				} else {
					safeExecution = true;
					return responseStr;
				}

			} catch (Exception e) {
				
				String reason = e.toString();
				
				if(reason != null)
				{
					Log.e("GetStartDateForGameTask", "Error in http connection "
							+ e.toString());
					return "Error in http connection " + e.toString();
				}
				return null;
			}
		}

		@Override
		protected abstract void onPostExecute(String result);

	}

	public abstract static class AcceptBuddyRequestTask extends
			AsyncTask<Void, Void, String> {

		private int mUserId;
		private String mEmail;
		private String mBuddyEMail;

		public AcceptBuddyRequestTask(int userId, String email,
				String buddyEMail) {
			mUserId = userId;
			mEmail = email;
			mBuddyEMail = buddyEMail;
		}

		@Override
		protected String doInBackground(Void... params) {
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("uid", "" + mUserId));
			nameValuePairs.add(new BasicNameValuePair("email", "" + mEmail));
			nameValuePairs.add(new BasicNameValuePair("femail", ""
					+ mBuddyEMail));

			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(URL_ACCEPT);
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity entity = response.getEntity();
				// is = entity.getContent();
				String responseStr = EntityUtils.toString(entity);
				// Log.i("ACCEPT_CHECK",responseStr);

				if (responseStr.startsWith("OK")) {
					responseStr = Utils.removeNewLines(responseStr);
					// Log.i("ACCEPT_CHECK",responseStr.substring(2)+")");

					return responseStr.substring(2);

				} else {

					return responseStr;
				}

			} catch (Exception e) {
				Log.e("AcceptBuddyRequestTask",
						"Error in http connection " + e.toString());
				return "Error in http connection " + e.toString();
			}
		}

		@Override
		protected abstract void onPostExecute(String result);
	}

	public static String checkGroup(String emailStr, int uid) {

		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("email", emailStr));
		nameValuePairs.add(new BasicNameValuePair("uid", "" + uid));

		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(URL_GROUP);
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			// is = entity.getContent();
			String responseStr = EntityUtils.toString(entity);
			// Log.i("GROUP_CHECK",responseStr);

			if (responseStr.startsWith("OK")) {
				responseStr = Utils.removeNewLines(responseStr);
				// Log.i("GROUP_RESPONSE",responseStr.substring(2)+")");

				return responseStr.substring(2);

			} else {

				return responseStr;
			}

		} catch (Exception e) {
			Log.e("checkGroup", "Error in http connection " + e.toString());
			return "Error in http connection " + e.toString();
		}
	}

	/**
	 * Maps to ServerHelper.checkRegister.
	 */
	public abstract static class RegisterTask extends
			AsyncTask<Void, Void, String> {

		private String mEemail;
		private String mPassword;
		private String mName;

		public RegisterTask(String email, String password, String name) {
			mEemail = email;
			mPassword = password;
			mName = name;
		}

		@Override
		protected String doInBackground(Void... params) {
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("name", mName));
			nameValuePairs.add(new BasicNameValuePair("email", mEemail));
			nameValuePairs.add(new BasicNameValuePair("pswrd", mPassword));

			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(URL_REGISTER);
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs,
						"UTF-8"));
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity entity = response.getEntity();
				String responseStr = EntityUtils.toString(entity);
				// Log.i("REG_CHECK",responseStr);

				if (responseStr.startsWith("OK")) {
					responseStr = Utils.removeNewLines(responseStr);
					// Log.i("REG_RESPONSE",responseStr.substring(2)+")");
					return responseStr.substring(2);
				} else {
					return responseStr;
				}

			} catch (Exception e) {
				Log.e("RegisterTask",
						"Error in http connection " + e.toString());
				return "Error in http connection " + e.toString();
			}
		}

		@Override
		protected abstract void onPostExecute(String result);
	}

	public static int[] checkMinutes(String uid) {

		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("uid", uid));
		int[] minutes = new int[4];

		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(URL_MINUTES);
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			// is = entity.getContent();
			String responseStr = EntityUtils.toString(entity);
			// Log.i("MIN_CHECK",responseStr);

			if (responseStr.startsWith("OK")) {
				responseStr = Utils.removeNewLines(responseStr);
				// Log.i("MIN_CHECK",responseStr.substring(2)+")");

				String[] temp = responseStr.substring(2).split(",");

				for (int i = 0; i < 4; i++)
					minutes[i] = Integer.parseInt(temp[i]);

				return minutes;

			} else {

				return null;
			}

		} catch (Exception e) {
			Log.e("checkMinutes", "Error in http connection " + e.toString());
			return null;
		}
	}

	/**
	 * Invites another user to become a buddy.
	 */
	public abstract static class InviteBuddyTask extends
			AsyncTask<Void, Void, Boolean> {

		private String mBuddyEemail;
		private int mUserId;
		private String mMsg;
		private boolean counterInvite;

		public InviteBuddyTask(String otherEmail, int userId, String msg) {
			mBuddyEemail = otherEmail;
			mUserId = userId;
			mMsg = msg;
			counterInvite = false;
			Constants.COUNTER_INVITE = false;
		}

		public boolean isCounterInvite() {
			return counterInvite;
		}

		public void setCounterInvite(boolean buddyInvitedAsWell) {
			counterInvite = buddyInvitedAsWell;
			Constants.COUNTER_INVITE = counterInvite;
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("femail", mBuddyEemail));
			nameValuePairs.add(new BasicNameValuePair("uid", "" + mUserId));
			nameValuePairs.add(new BasicNameValuePair("msg", mMsg));

			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(URL_INVITE);
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity entity = response.getEntity();
				// Log.i("INVITE_uid",""+mUserId);
				// Log.i("INVITE_femail",mBuddyEemail);
				String responseStr = EntityUtils.toString(entity);
				// Log.i("INVITE_RESPONSE",responseStr);

				if (responseStr.startsWith("OK")) {
					return true;
				} else if (responseStr.startsWith("CI")) {
					setCounterInvite(true);
					return true;
				} else if (responseStr.startsWith("AP")) // Already playing
				{
					return true;// false;
				} else if (responseStr.startsWith("NE")) // Non-existant
				{
					Constants.EMAIL_NOT_FOUND = true;
					return true;// false;
				} else {
					return false;
				}

			} catch (Exception e) {
				Log.e("InviteBuddyTask",
						"Error in http connection " + e.toString());
				return false;
			}
		}

		@Override
		protected abstract void onPostExecute(Boolean result);
	}

	/**
	 * Invites another user to become a buddy.
	 */
	public abstract static class DeFriendTask2 extends
			AsyncTask<Void, Void, Boolean> {

		private int mUserId;
		private String femail;
		public boolean exceptionRaised;
		public String exceptionReason;

		public DeFriendTask2(int userId, String friendEmail) {
			mUserId = userId;
			femail = friendEmail;
			// Log.d(TAG,"*****************DEFRIEND: uid "+mUserId+"************ fid "+fId);
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("uid", "" + mUserId));
			nameValuePairs.add(new BasicNameValuePair("femail", "" + femail));
			// Log.d(TAG,"*****************DEFRIEND: uid "+mUserId+"************ femail "+femail);

			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(URL_DEFRIEND2);
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity entity = response.getEntity();
				// Log.i("INVITE_uid",""+mUserId);
				// Log.i("INVITE_femail",mBuddyEemail);
				String responseStr = EntityUtils.toString(entity);
				// Log.i("DEFRIEND2_RESPONSE",responseStr);

				if (responseStr.contains("OK")) {
					return true;
				} else {
					return false;
				}

			} catch (Exception e) 
			{
				
				exceptionRaised = true;
				
				String reason = e.toString();
				
				if(reason != null)
				{
					Log.e("DeFriendTask2", "Error in http connection " + e.toString());
					exceptionReason = e.toString();
				}				
				
				//Log.e("DeFriendTask2",	"Error in http connection " + e.toString());
				return false;
			}
		}

		@Override
		protected abstract void onPostExecute(Boolean result);
	}

	/**
	 * Invites another user to become a buddy.
	 */
	public abstract static class DeFriendTask extends
			AsyncTask<Void, Void, Boolean> {

		private int mUserId;
		private int fId;
		public boolean exceptionRaised;
		public String exceptionReason;

		public DeFriendTask(int userId, int friendId) {
			mUserId = userId;
			fId = friendId;
			// Log.d(TAG,"*****************DEFRIEND: uid "+mUserId+"************ fid "+fId);
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("uid", "" + mUserId));
			nameValuePairs.add(new BasicNameValuePair("fid", "" + fId));
			// Log.d(TAG,"*****************DEFRIEND: uid "+mUserId+"************ fid "+fId);

			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(URL_DEFRIEND);
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity entity = response.getEntity();
				// Log.i("INVITE_uid",""+mUserId);
				// Log.i("INVITE_femail",mBuddyEemail);
				String responseStr = EntityUtils.toString(entity);
				// Log.i("DEFRIEND_RESPONSE",responseStr);

				if (responseStr.contains("OK")) {
					return true;
				} else {
					return false;
				}

			} 
			catch (Exception e) {
				
				exceptionRaised = true;
				
				String reason = e.toString();
				
				if(reason != null)
				{
					Log.e("DeFriendTask", "Error in http connection " + e.toString());
					exceptionReason = e.toString();
				}
				
				return false;
			}
		}

		@Override
		protected abstract void onPostExecute(Boolean result);
	}

	public static boolean pokeBuddy(String uid, String fid, int flag) {
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("uid", uid));
		nameValuePairs.add(new BasicNameValuePair("fid", fid));
		nameValuePairs.add(new BasicNameValuePair("flag", Integer
				.toString(flag)));

		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(URL_POKE);
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			// Log.i("POKE_uid",uid);
			// Log.i("POKE_fid",fid);
			String responseStr = EntityUtils.toString(entity);

			responseStr = responseStr.replaceAll("\n", "");
			responseStr = responseStr.replaceAll("\r", "");
			// Log.i("POKE_RESPONSE","("+responseStr+")");

			if (responseStr.equals("OK")) {

				return true;
			} else {

				return false;
			}

		} catch (Exception e) {
			Log.e("pokeBuddy", "Error in http connection " + e.toString());
			return false;
		}

	}

	public static boolean delLog(String uid, String fid, String msgtxt) {
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("uid", uid));
		nameValuePairs.add(new BasicNameValuePair("fid", fid));
		nameValuePairs.add(new BasicNameValuePair("msgtxt", msgtxt));

		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(URL_DEL_LOG);
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			// Log.i(TAG,"Removing the log of " + uid + " with message: " +
			// msgtxt);
			HttpResponse response = httpclient.execute(httppost);

			HttpEntity entity = response.getEntity();
			String responseStr = EntityUtils.toString(entity);
			responseStr = Utils.removeNewLines(responseStr);
			// Log.i("LOG_DEL_RESPONSE","("+responseStr+")");
			return responseStr.equals("OK");
		} catch (Exception e) {
			Log.e(TAG, "Error in http connection " + e.toString());
			return false;
		}
	}

	public static boolean msgBuddy(String uid, String fid, String msgtxt) {
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("uid", uid));
		nameValuePairs.add(new BasicNameValuePair("fid", fid));
		nameValuePairs.add(new BasicNameValuePair("msgtxt", msgtxt));

		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(URL_MSG);
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			// Log.i("MESSAGE_uid",uid);
			// Log.i("MESSAGE_fid",fid);
			String responseStr = EntityUtils.toString(entity);

			responseStr = responseStr.replaceAll("\n", "");
			responseStr = responseStr.replaceAll("\r", "");
			// Log.i("POKE_RESPONSE","("+responseStr+")");

			if (responseStr.equals("OK")) {

				return true;
			} else {

				return false;
			}

		} catch (Exception e) {
			Log.e("msgBuddy", "Error in http connection " + e.toString());
			return false;
		}

	}

	/**
	 * Submit this message to the server.
	 * 
	 * @param message
	 *            is the text of the message to submit
	 * @param pictureFileName
	 *            is the filename of a file on the phone's storage containing
	 *            the image to include
	 * @param lat
	 *            is the latitude of the current location
	 * @param lng
	 *            is the longitude of the current location
	 */
	public static boolean register(String uid, String first_name,
			String last_name, String name, String sex, String link) {

		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("uid", uid));
		nameValuePairs.add(new BasicNameValuePair("first_name", first_name));
		nameValuePairs.add(new BasicNameValuePair("last_name", last_name));
		nameValuePairs.add(new BasicNameValuePair("name", name));
		nameValuePairs.add(new BasicNameValuePair("sex", sex));
		nameValuePairs.add(new BasicNameValuePair("link", link));

		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(URL_REG);
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			// is = entity.getContent();
			String responseStr = null; // HERE
			responseStr = EntityUtils.toString(entity);
			// Log.i("UPLOAD_RESPONSE",responseStr);

			if (responseStr.equals("OK")) {

				return true;
			} else {

				return false;
			}

		} catch (Exception e) {
			Log.e("register", "Error in http connection " + e.toString());
			return false;
		}

	}

	public static boolean registerGroup(String uid, String first_name,
			String last_name, String name, String sex, String link) {

		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("uid", uid));
		nameValuePairs.add(new BasicNameValuePair("first_name", first_name));
		nameValuePairs.add(new BasicNameValuePair("last_name", last_name));
		nameValuePairs.add(new BasicNameValuePair("name", name));
		nameValuePairs.add(new BasicNameValuePair("sex", sex));
		nameValuePairs.add(new BasicNameValuePair("link", link));

		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(URL_REG);
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			// is = entity.getContent();
			String responseStr = null; // HERE
			responseStr = EntityUtils.toString(entity);
			// Log.i("UPLOAD_RESPONSE",responseStr);

			if (responseStr.equals("OK")) {

				return true;
			} else {

				return false;
			}

		} catch (Exception e) {
			Log.e("registerGroup", "Error in http connection " + e.toString());
			return false;
		}

	}

	/*******************************************************
	 * /*******************************************************
	 * /*******************************************************
	 * /*******************************************************
	 * /*******************************************************
	 * /*******************************************************
	 * 
	 */

	/**
	 * Make a POST request to the URL using the form given and return an
	 * InputStream for the response body
	 */
	private static InputStream obtainInputStream(String urlAddress,
			MultipartEntity form) throws ClientProtocolException, IOException {
		DefaultHttpClient client = new DefaultHttpClient();
		HttpUriRequest request = new HttpPost(urlAddress);
		client.getParams().setBooleanParameter("http.protocol.expect-continue",
				false);

		// set the form as an entity of the request
		((HttpEntityEnclosingRequestBase) request).setEntity(form);

		// execute the request
		HttpResponse response = client.execute(request);

		// HttpEntity entity = response.getEntity();
		// Log.d("XML_RESPONSE",EntityUtils.toString(entity));

		// get the response input stream
		return response.getEntity().getContent();

		// is = entity.getContent();

	}

	/**
	 * Helper class to parse the XML response from the server
	 */
	private static class MyXMLHandler extends
			org.xml.sax.helpers.DefaultHandler {
		private StringBuffer buffer = new StringBuffer();
		private ArrayList<GroupMeta> mGroups = new ArrayList<GroupMeta>();
		private GroupMeta group;

		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			buffer.setLength(0);
			if (localName.equals("group")) {
				group = new GroupMeta();
				group.setGroupId(attributes.getValue("id"));
				// System.out.println("group :" + groupid);
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {

			if (localName.equals("group")) {
				mGroups.add(group);
			} else if (localName.equals("name")) {
				group.setGroupName(buffer.toString());
				// System.out.println("name :" + buffer.toString());
			} else if (localName.equals("privacy")) {
				group.setPrivacy(buffer.toString());
				// System.out.println("privacy :" + buffer.toString());
			} else if (localName.equals("Mnumber")) {
				group.setMNumber(buffer.toString());
				// System.out.println("privacy :" + buffer.toString());
			} else if (localName.equals("image")) {
				group.setPictureUri(URL_PREFIX + buffer.toString());
				// System.out.println("image :" + buffer.toString());
			}

		}

		@Override
		public void characters(char[] ch, int start, int length) {
			buffer.append(ch, start, length);
		}

		protected ArrayList<GroupMeta> getGroups() {
			return mGroups;
		}

	}

	private static class MsgXMLHandler extends
			org.xml.sax.helpers.DefaultHandler {
		private StringBuffer buffer = new StringBuffer();
		private ArrayList<MessageMeta> msgList = new ArrayList<MessageMeta>();
		private MessageMeta msg;

		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			buffer.setLength(0);
			// Log.d("TAG","!!!!!!!!!!"+uri);
			if (localName.equals("msg")) {

				msg = new MessageMeta();
				msg.setmsgid(attributes.getValue("id"));
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {

			if (localName.equals("msg")) {
				msgList.add(msg);
			} else if (localName.equals("msgid")) {
				msg.setmsgtxt(buffer.toString());
				// System.out.println("msgid :" + buffer.toString());
			} else if (localName.equals("msgfrom")) {
				msg.setmsgfrom(buffer.toString());
				// System.out.println("msgfrom :" + buffer.toString());
			} else if (localName.equals("msgtxt")) {
				msg.setmsgtxt(buffer.toString());
				// System.out.println("msgtxt :" + buffer.toString());
			} else if (localName.equals("msgtime")) {
				msg.setmsgtime(buffer.toString());
				// System.out.println("msgtime :" + buffer.toString());
			}

		}

		@Override
		public void characters(char[] ch, int start, int length) {
			buffer.append(ch, start, length);
		}

		protected ArrayList<MessageMeta> getMembers() {
			return msgList;
		}

	}



private static class LeaderboardXMLHandler extends
		org.xml.sax.helpers.DefaultHandler {
	private StringBuffer buffer = new StringBuffer();
	private ArrayList<UserEntry> msgList = new ArrayList<UserEntry>();
	//private UserEntry msg;
	
	
	private ArrayList<GroupMember> membersList = new ArrayList<GroupMember>();
	private ArrayList<GroupEntry> groupsList = new ArrayList<GroupEntry>();
	
	
	private GroupEntry gMsg;
	//private GroupMember gMsg;
	
	
	
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		buffer.setLength(0);
		if (localName.equals("dyad")) {
		
			gMsg = new GroupEntry();
			

		}
		else if (localName.equals("error")) {
			
			//gMsg = new GroupEntry();
			

		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
	
		if (localName.equals("dyad")) 
		{
			//msgList.add(msg);
			//membersList.add(gMsg);
			boolean addFlag = true;
			for(int i = 0; i < groupsList.size(); i++)
			{
				if(groupsList.get(i).mem[0].getUid() == gMsg.mem[0].getUid()
						|| groupsList.get(i).mem[0].getUid() == gMsg.mem[0].getFid())
				{
					addFlag = false;
				}
			}
			if(addFlag)
			{
				groupsList.add(gMsg);
				membersList.add(gMsg.mem[0]);
				membersList.add(gMsg.mem[1]);
			}
		} 
		else if (localName.equals("uname")) 
		{
			(gMsg.mem[0]).setUserName(buffer.toString());
			//msg.setUserName(buffer.toString());
			//gMsg.setUserName(buffer.toString());
			// gMsg.setUserName(buffer.toString());
			// System.out.println("msgid :" + buffer.toString());
		} 
		else if (localName.equals("fname")) 
		{
			(gMsg.mem[1]).setUserName(buffer.toString());
			//msg.setUserName(buffer.toString());
			//gMsg.setUserName(buffer.toString());
			// gMsg.setUserName(buffer.toString());
			// System.out.println("msgid :" + buffer.toString());
		} 		
		else if (localName.equals("uid")) 
		{
			(gMsg.mem[0]).setUid(Integer.parseInt(buffer.toString()));
			(gMsg.mem[1]).setFid(Integer.parseInt(buffer.toString()));
		} 
		else if (localName.equals("fid"))
		{
			(gMsg.mem[1]).setUid(Integer.parseInt(buffer.toString()));
			(gMsg.mem[0]).setFid(Integer.parseInt(buffer.toString()));			
			//gMsg.setFid(Integer.parseInt(buffer.toString()));
		}
		else if (localName.equals("avatarid"))
		{
			(gMsg.mem[0]).setAvatarid(Integer.parseInt(buffer.toString()));			
			(gMsg.mem[1]).setAvatarid(Integer.parseInt(buffer.toString()));
			gMsg.icon = Integer.parseInt(buffer.toString());
		}
		else if(localName.equals("floorCount"))
		{
			gMsg.floors[0] = Integer.parseInt(buffer.toString());
			gMsg.floors[1] = 0; // does not matter for the time being.
		}
		else if(localName.equals("stepCount"))
		{
			gMsg.steps[0] = Integer.parseInt(buffer.toString());
			gMsg.steps[1] = 0; // does not matter for the time being.
		}
		else if (localName.equals("error")) {
			
			//gMsg = new GroupEntry();

		}		
	
	}
	
	@Override
	public void characters(char[] ch, int start, int length) {
		buffer.append(ch, start, length);
	}
	
	protected ArrayList<UserEntry> getMembers() {
		return msgList;
	}
	
	protected ArrayList<GroupMember> getGroupMembers() {
		return membersList;
	}
	
	protected ArrayList<GroupEntry> getRankedGroups() {
		return groupsList;
	}	
	
}	
	
	
	private static class UserXMLHandler extends
			org.xml.sax.helpers.DefaultHandler {
		private StringBuffer buffer = new StringBuffer();
		private ArrayList<UserEntry> msgList = new ArrayList<UserEntry>();
		private UserEntry msg;

		// ASMA START
		private ArrayList<GroupMember> membersList = new ArrayList<GroupMember>();
		private GroupMember gMsg;

		// ASMA END

		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			buffer.setLength(0);
			// Log.d("TAG","!!!!!!!!!!"+uri);
			if (localName.equals("user")) {

				msg = new UserEntry();
				gMsg = new GroupMember();
				// msg.setmsgid(attributes.getValue("id"));
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {

			if (localName.equals("user")) {
				msgList.add(msg);
				membersList.add(gMsg);
			} else if (localName.equals("name")) {
				msg.setUserName(buffer.toString());
				gMsg.setUserName(buffer.toString());
				// gMsg.setUserName(buffer.toString());
				// System.out.println("msgid :" + buffer.toString());
			} else if (localName.equals("email")) {
				msg.setUserEmail(buffer.toString());
				// System.out.println("msgfrom :" + buffer.toString());
			} else if (localName.equals("uid")) {
				gMsg.setUid(Integer.parseInt(buffer.toString()));
			} else if (localName.equals("fid"))
				gMsg.setFid(Integer.parseInt(buffer.toString()));
			else if (localName.equals("avatarid"))
				gMsg.setAvatarid(Integer.parseInt(buffer.toString()));
			else if(localName.equals("availability"))
				msg.setAvailability(Integer.parseInt(buffer.toString()));

		}

		@Override
		public void characters(char[] ch, int start, int length) {
			buffer.append(ch, start, length);
		}

		protected ArrayList<UserEntry> getMembers() {
			return msgList;
		}

		protected ArrayList<GroupMember> getGroupMembers() {
			return membersList;
		}

	}

	private static class UserInvXMLHandler extends
	org.xml.sax.helpers.DefaultHandler {
		private StringBuffer buffer = new StringBuffer();
		private ArrayList<UserEntry> msgList = new ArrayList<UserEntry>();
		private UserEntry msg;
		
		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			buffer.setLength(0);
			// Log.d("TAG","!!!!!!!!!!"+uri);
			if (localName.equals("user")) {
		
				msg = new UserEntry();
				// msg.setmsgid(attributes.getValue("id"));
			}
		}
		
		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {
		
			if (localName.equals("user")) {
				msgList.add(msg);
			} else if (localName.equals("name")) 
			{
				msg.setUserName(buffer.toString());
				// gMsg.setUserName(buffer.toString());
				// System.out.println("msgid :" + buffer.toString());
			} else if (localName.equals("email")) {
				msg.setUserEmail(buffer.toString());
				// System.out.println("msgfrom :" + buffer.toString());
			}
			else if(localName.equals("availability"))
				msg.setAvailability(Integer.parseInt(buffer.toString()));
		
		}
		
		@Override
		public void characters(char[] ch, int start, int length) {
			buffer.append(ch, start, length);
		}
		
		protected ArrayList<UserEntry> getMembers() {
			return msgList;
		}
		
	}
	
	
	
	private static class BadgesXMLHandler extends
			org.xml.sax.helpers.DefaultHandler {
		private StringBuffer buffer = new StringBuffer();
		private ArrayList<OneDayBadges> msgList = new ArrayList<OneDayBadges>();
		private OneDayBadges msg;

		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			buffer.setLength(0);
			if (localName.equals("badge")) {
				msg = new OneDayBadges();
				msg.setLogId(Integer.parseInt(attributes.getValue("id")));
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {

			if (localName.equals("badge")) {
				msgList.add(msg);
			} else if (localName.equals("badgetime")) {
				msg.date = buffer.toString();
			} else if (localName.equals("badgesteps")) {
				msg.badgesteps = (buffer.toString());
			}

		}

		@Override
		public void characters(char[] ch, int start, int length) {
			buffer.append(ch, start, length);
		}

		protected ArrayList<OneDayBadges> getMembers() {
			return msgList;
		}

	}

}
