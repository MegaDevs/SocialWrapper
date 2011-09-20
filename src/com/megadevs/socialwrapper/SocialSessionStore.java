/*
 * Copyright 2010 Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
	public static HashMap<String, String> restore(String socialKey,
    		SocialNetwork session, Context context) {
        
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
