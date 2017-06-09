package ch.epfl.hci.healthytogether;

import java.util.Arrays;

import android.util.Log;

/**
 * Message bubble
 */
public class OneLog {
	public boolean foodSet;
	public boolean exerciseSet;
	public String activity;
	public String mood;
	public String social;
	
	
	public boolean translationDone;
	
	public String activityEnglish;
	public String moodEnglish;
	public String socialEnglish;
	
	public String rawEntry;
	public String logTime;
	public String noneString;

	/** only used for received msgs (not for sent ones) */
	private int logId;

	/**
	 * @param left
	 *            <code>true</code> if the user received the msg.
	 *            <code>false</code> if the user sent the msg, (the messages
	 *            sent by the user appear on the right).
	 * @param activity
	 */
	public OneLog(String rawText, String logTime, int logId,String noneString) {
		super();
		this.logTime = logTime;
		this.noneString = noneString;
		parseRawText(rawText);
		this.logId = logId;
	}

	public OneLog(String rawText, String logTime, String noneString) {
		super();
		this.logTime = logTime;
		this.noneString = noneString;
		parseRawText(rawText);
		logId = -1;
	}

	private void parseRawText(String rawText) {
		this.rawEntry = rawText;
		translationDone = false;
		String[] words = rawText.split(" ");

		// LOG FOOD <...> EXERCISE <...> MOOD <...> SOCIAL <...>

		
		if (!words[2].equals(noneString)) {
			foodSet = true;
			exerciseSet = false;
			activity = words[2];
		} else if (!words[4].equals(noneString)) {
			foodSet = false;
			exerciseSet = true;
			activity = words[4];
		} else {
			activity = noneString;
		}

		mood = words[6];
		social = words[8];
		
		if(social.equalsIgnoreCase("social") && words.length > 9)
		{
			social = words[9];
		}
		
	}

	public String toDBEntry(String[] positiveTemplates,
			String[] negativeTemplates, String[] foodTemplates,
			String[] sedentaryTemplates, String[] exerciseTemplates,
			String[] socialTemplates, String [] moodIntensityTemplates,String nativeNone) 
	{
		
		translateToEnglish(positiveTemplates,
				negativeTemplates, foodTemplates,
				sedentaryTemplates, exerciseTemplates,
				 socialTemplates, moodIntensityTemplates, nativeNone);
		
		// LOG FOOD <...> EXERCISE <...> MOOD <...> SOCIAL <...>
		String entry = "LOG FOOD " + (foodSet ? activityEnglish : "None")
				+ " EXERCISE " + (exerciseSet ? activityEnglish : "None") + " MOOD "
				+ moodEnglish + " SOCIAL " + socialEnglish;
		return entry;
	}

	public int getLogId() {
		return logId;
	}

	public static String getClosestAlarmTime(OneLog log) {
		return null;
	}

	
	/*
	 * This function assumes that the data (activity,mood,social) is received as english
	 * 
	 * */
	public void translateToNative(String[] positiveTemplates,
			String[] negativeTemplates, String[] foodTemplates,
			String[] sedentaryTemplates, String[] exerciseTemplates,
			String[] socialTemplates, String[] moodIntensityTemplates,String nativeNone) {
		
		activityEnglish = activity;
		moodEnglish = mood;
		socialEnglish = social;
		
		// mood
		if(mood.equals("None"))
		{
			mood = nativeNone;
		}
		else
		{
			boolean checkFlag = false;
			for(int i = 0; i < Constants.positiveMoods.length; i++)
			{
				for(int j = 0; j < Constants.moodIntensityTemplates.length; j++)
				{
					if(mood.equals(Constants.positiveMoods[i] + Constants.moodIntensityTemplates[j]))
					{
						mood = positiveTemplates[i] + moodIntensityTemplates[j];
						checkFlag = true;
						break;
					}
				}
				
				if(!checkFlag)
				{
					if(mood.equals(Constants.positiveMoods[i]))
					{
						mood = positiveTemplates[i] + moodIntensityTemplates[1];
						checkFlag = true;
						break;
					}						
				}				
				
			}			
			
			if(!checkFlag)
			{
				for(int i = 0; i < Constants.negativeMoods.length; i++)
				{
					for(int j = 0; j < Constants.moodIntensityTemplates.length; j++)
					{					
						if(mood.equals(Constants.negativeMoods[i] + Constants.moodIntensityTemplates[j]))
						{
							mood = negativeTemplates[i] + moodIntensityTemplates[j];
							checkFlag = true;
							break;
						}
					}
					
					if(!checkFlag)
					{
						if(mood.equals(Constants.negativeMoods[i]))
						{
							mood = negativeTemplates[i] + moodIntensityTemplates[1];
							checkFlag = true;
							break;
						}						
					}
					
				}					
			}			
		}
		
		if(activity.equals("None"))
		{
			activity = nativeNone;
		}
		else
		{
			boolean checkFlag = false;
			for(int i = 0; i < Constants.exerciseActivities.length; i++)
			{
				if(activity.equals(Constants.exerciseActivities[i]))
				{
					activity = exerciseTemplates[i];
					checkFlag = true;
					break;
				}
			}			
			
			if(!checkFlag)
			{
				for(int i = 0; i < Constants.sedentaryActivities.length; i++)
				{
					if(activity.equals(Constants.sedentaryActivities[i]))
					{
						activity = sedentaryTemplates[i];
						checkFlag = true;
						break;
					}
				}					
			}
			
		}
		
		if(social.equals("None"))
		{
			social = nativeNone;
		}
		else
		{
			for(int i = 0; i < Constants.socialTemplates.length; i++)
			{
				if(social.equalsIgnoreCase(Constants.socialTemplates[i]))
				{
					social = socialTemplates[i]; 
					break;
				}
			}
		}		
		
	}
	
	
	/*
	 * This assumes that the native language is set in (activity, mood, social)
	 * 
	 * */
	
	public void translateToEnglish(String[] positiveTemplates,
			String[] negativeTemplates, String[] foodTemplates,
			String[] sedentaryTemplates, String[] exerciseTemplates,
			String[] socialTemplates, String[] moodIntensityTemplates,String nativeNone)
	{
		activityEnglish = activity;
		moodEnglish = mood;
		socialEnglish = social;

		// mood
		if(moodEnglish.equals("None") || moodEnglish.equals(nativeNone))
		{
			moodEnglish = "None";
		}
		else
		{
			boolean checkFlag = false;
			for(int i = 0; i < positiveTemplates.length; i++)
			{
				for(int j = 0; j < moodIntensityTemplates.length; j++)
				{
					if(moodEnglish.equals(positiveTemplates[i] + moodIntensityTemplates[j]))
					{
						moodEnglish = Constants.positiveMoods[i] + Constants.moodIntensityTemplates[j];
						checkFlag = true;
						break;
					}
				}
				
				if(!checkFlag)
				{
					if(mood.equals(positiveTemplates[i]))
					{
						mood =  Constants.positiveMoods[i] + Constants.moodIntensityTemplates[1];
						checkFlag = true;
						break;
					}						
				}					
				
			}			
			
			if(!checkFlag)
			{
				for(int i = 0; i < negativeTemplates.length; i++)
				{
					
					for(int j = 0; j < moodIntensityTemplates.length; j++)
					{					
						if(moodEnglish.equals(negativeTemplates[i] + moodIntensityTemplates[j]))
						{
							moodEnglish = Constants.negativeMoods[i] + Constants.moodIntensityTemplates[j];
							checkFlag = true;
							break;
						}
					}
					
					if(!checkFlag)
					{
						if(mood.equals(negativeTemplates[i]))
						{
							mood =  Constants.negativeMoods[i] + Constants.moodIntensityTemplates[1];
							checkFlag = true;
							break;
						}						
					}						
				}					
			}			
		}
		
		if(activityEnglish.equals("None")  || activityEnglish.equals(nativeNone))
		{
			activityEnglish = "None";
		}
		else
		{
			boolean checkFlag = false;
			for(int i = 0; i < exerciseTemplates.length; i++)
			{
				if(activityEnglish.equals(exerciseTemplates[i]))
				{
					activityEnglish = Constants.exerciseActivities[i];
					checkFlag = true;
					break;
				}
			}			
			
			if(!checkFlag)
			{
				for(int i = 0; i < sedentaryTemplates.length; i++)
				{
					if(activityEnglish.equals(sedentaryTemplates[i]))
					{
						activityEnglish = Constants.sedentaryActivities[i];
						checkFlag = true;
						break;
					}
				}					
			}
			
			if(!checkFlag)
			{
				Log.e("OneLog","could not translate this activity: " + activity);
			}
			
		}
		
		if(socialEnglish.equals("None") || socialEnglish.equals(nativeNone))
		{
			socialEnglish = "None";
		}
		else
		{
			for(int i = 0; i < socialTemplates.length; i++)
			{
				if(socialEnglish.equals(socialTemplates[i]))
				{
					socialEnglish = Constants.socialTemplates[i]; 
					break;
				}
			}
		}		
		
	}

}