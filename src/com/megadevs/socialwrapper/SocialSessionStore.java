package com.megadevs.socialwrapper;

import java.util.Map;
import java.util.Vector;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;


/**
 * This class is used to store the various social networks' data, such as access tokens,
 * ids and so on. This data is stored in the application's prefs. 
 * @author dextor
 *
 */
public class SocialSessionStore {

	/**
	 * Saves the current social network session. The data that needs to be saved is obtained
	 * by calling the getConnectionData() method (see that for further informations).
	 * @param socialKey the SocialNetwork ID
	 * @param session the current SocialNetwork session
	 * @param context the application context (needed in order to get access to the prefs)
	 * @return a boolean value representing the success or failure
	 */
	public static boolean save(String socialKey, SocialNetwork session, Context context) {
		Editor editor =
			context.getSharedPreferences(socialKey, Context.MODE_PRIVATE).edit();
		Vector<String[]> data = session.getConnectionData();

		for(String[] s : data) {
			editor.putString(s[0], s[1]);
		}
		return editor.commit();
	}

	/**
	 * Restores a saved social network session. Once all the data has been retrieved, it is set
	 * with the setConnectionData() method (see that for further informations).
	 * @param socialKey the SocialNetwork ID
	 * @param session the current SocialNetwork session
	 * @param context the application context (needed in order to get access to the prefs)
	 */
	@SuppressWarnings("unchecked")
	public static void restore(String socialKey, SocialNetwork session, Context context) {

		SharedPreferences savedSession =
			context.getSharedPreferences(socialKey, Context.MODE_PRIVATE);
		
		session.setConnectionData((Map<String, String>) savedSession.getAll());
	}

	/**
	 * Deletes a saved social network session from the prefs. 
	 * @param socialKey the SocialNetwork ID, which indicates the session that needs to be cleared
	 * @param context the application context (needed in order to get access to the prefs)
	 */
	public static void clear(String socialKey, Context context) {
		Editor editor = 
			context.getSharedPreferences(socialKey, Context.MODE_PRIVATE).edit();
		editor.clear();
		editor.commit();
	}

}
