/*
 * Copyright 2010 Small Light Room CO., LTD.
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

package com.jiramot.foursquare.android;

/**
 * Encapsulation of a MainActivity: a demo for using api
 *
 * @author jiramot@gmail.com
 */
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.jiramot.foursquare.android.Foursquare.DialogListener;

public class FSAPITest extends Activity {
	Foursquare foursquare;

	private Button venue;
	private static int count = 0;

	String client = "AWL4NZYKTOTOBSM4H5B41FLHJFRXADW4MU3CNMTVV4EEUJW3";
	String clientSecret = "SQPMNWNCF4BD5NP1SUWZWWHVB4U35CLOJ1FGJBTIN3UE5X4H";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		venue = (Button) findViewById(R.id.button);
		OnClickListener venues = new OnClickListener() {
			@Override
			public void onClick(View v) {
				String aa = null;
				try {
					aa = searchVenue();
					JSONObject obj = new JSONObject(aa);
					//					Log.i("fsapitest", obj.toString(2));
					JSONObject response = obj.getJSONObject("response");
					JSONArray groups = response.getJSONArray("groups");
					JSONObject element = groups.getJSONObject(0);
					JSONArray items = element.getJSONArray("items");

					File sdcardpath = Environment.getExternalStorageDirectory();
					File f = new File(sdcardpath,"test"+count+".txt");
					FileOutputStream outStream = new FileOutputStream(f);
					ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(outStream));

					for (int i=0; i<items.length(); i++) {
						JSONObject item = items.getJSONObject(i);
						Log.i("corso", "item "+i+" -- "+item.getString("id"));
						Log.i("corso", "item "+i+" -- "+item.getString("name"));
					}
					
//					out.writeObject(items.toString(2));

					out.flush();
					out.close();
					outStream.close();

				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		};
		
		OnClickListener friends = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				venue.setText("friends");
				
				try {
					String result = foursquare.request("users/self/friends");
					JSONObject obj = new JSONObject(result);
					JSONObject response = obj.getJSONObject("response"); 
					JSONObject friends = response.getJSONObject("friends");
					JSONArray items = friends.getJSONArray("items");
					
					for (int i=0; i<items.length(); i++) {
						JSONObject item = items.getJSONObject(i);
						
						Log.i("debug", item.getString("id") + " - " + item.getString("firstName") + " " + item.getString("lastName"));
					}
					
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		};
		
		venue.setOnClickListener(friends);

		init(getIntent());

		//			JSONObject response = obj.getJSONObject("response");
		//			JSONArray venues = response.getJSONArray("groups");

		//			File sdcardpath = Environment.getExternalStorageDirectory();
		//			File f = new File(sdcardpath,"test"+count+".txt");
		//			FileOutputStream outStream = new FileOutputStream(f);
		//			ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(outStream));

		//			out.writeObject(obj.toString(2));

		//			for (int i=0; i<venues.length(); i++) {
		//				JSONObject venue = venues.getJSONObject(i);

		//				out.writeObject(venue.toString(2));


		//				Log.i("corso", venue.getString("id"));
		//				Log.i("corso", venue.getString("name"));
		//			}

		//			out.flush();
		//			out.close();
		//			outStream.close();
		//
		//			count++;
		//			Log.i("fsapitest", "calls: "+count);
		//
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onNewIntent(android.content.Intent)
	 */
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		init(intent);
	}

	private void init(Intent intent) {

		if (intent.getScheme() == null) {
			foursquare = new Foursquare(client, "fsapitest://connect");
			foursquare.authorize(this, new FoursquareAuthenDialogListener());
		}
	}

	private String searchVenue() throws MalformedURLException, IOException {
		Bundle b = new Bundle();
		b.putString("ll", "45.40, 11.88");
		return foursquare.request("venues/search", b);
	}

	private class FoursquareAuthenDialogListener implements DialogListener {

		@Override
		public void onComplete(Bundle values) {
			if(foursquare.isSessionValid())
				Log.i("fsapitest", "valid");
			else
				Log.i("fsapitest", "invalid");

			//				System.out.println("raw response: \n" + obj.toString(2));
			//				System.out.println("raw response: \n" + response.toString(2));
			//				Log.i("fsapitest", "query-response: "+ obj.getJSONArray("groups"));
		}

		@Override
		public void onFoursquareError(FoursquareError e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onError(DialogError e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onCancel() {
			// TODO Auto-generated method stub

		}

	}

}