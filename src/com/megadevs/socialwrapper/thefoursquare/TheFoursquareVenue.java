/**
 * 
 */
package com.megadevs.socialwrapper.thefoursquare;

import com.google.android.maps.GeoPoint;

/**
 * @author dextor
 *
 */
public class TheFoursquareVenue extends GeoPoint {

	// the Foursquare-assigned ID
	private String venueID;
	private String venueName;
	/**
	 * @param arg0
	 * @param arg1
	 */
	public TheFoursquareVenue(int latitudeE6, int longitudeE6, String id, String name) {
		super(latitudeE6, longitudeE6);
		venueID = id;
		venueName = name;
	}
	/**
	 * @return the venueID
	 */
	public String getVenueID() {
		return venueID;
	}
	/**
	 * @return the venueName
	 */
	public String getVenueName() {
		return venueName;
	}

}
