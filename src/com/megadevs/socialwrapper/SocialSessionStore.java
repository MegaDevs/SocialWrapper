package com.megadevs.socialwrapper;

import java.util.Map;
import java.util.Vector;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

public class SocialSessionStore {

	public static boolean save(String socialKey, SocialNetwork session, Context context) {
		Editor editor =
			context.getSharedPreferences(socialKey, Context.MODE_PRIVATE).edit();
		Vector<String[]> data = session.getConnectionData();

		for(String[] s : data) {
			editor.putString(s[0], s[1]);
		}
		return editor.commit();
	}

	@SuppressWarnings("unchecked")
	public static void restore(String socialKey, SocialNetwork session, Context context) {

		SharedPreferences savedSession =
			context.getSharedPreferences(socialKey, Context.MODE_PRIVATE);

		Log.i("corso", "dimensione: "+savedSession.getAll().size());
		Map<String, String> i = (Map<String, String>) savedSession.getAll();

		boolean diocane = (i == null);
		Log.i("corso", "this biatch is " + diocane);
		
		if (i.get("accessTokenKey") != null) {
			Log.i("corso", "access token not null");
			Log.i("corso", i.get("accessTokenKey"));
		}
			
		session.setConnectionData((Map<String, String>) savedSession.getAll());
	}

	public static void clear(String socialKey, Context context) {
		Editor editor = 
			context.getSharedPreferences(socialKey, Context.MODE_PRIVATE).edit();
		editor.clear();
		editor.commit();
	}

}
