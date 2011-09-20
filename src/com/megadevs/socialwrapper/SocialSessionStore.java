package com.megadevs.socialwrapper;

import java.util.HashMap;
import java.util.Vector;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

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
	public static HashMap<String, String> restore(String socialKey, Context context) {
        
    	SharedPreferences savedSession =
            context.getSharedPreferences(socialKey, Context.MODE_PRIVATE);

    	return (HashMap<String, String>) savedSession.getAll();
    }

    public static void clear(String socialKey, Context context) {
        Editor editor = 
            context.getSharedPreferences(socialKey, Context.MODE_PRIVATE).edit();
        editor.clear();
        editor.commit();
    }
    
}
