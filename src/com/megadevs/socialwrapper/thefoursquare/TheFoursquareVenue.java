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
	private String venueDistance;
	
	/**
	 * Default constructor for a TheFoursquareVenue object.
	 * @param latitudeE6 latitude coordinates for the venue
	 * @param longitudeE6 longitude coordinates for the venue
	 * @param distance distance of the venue from the current position
	 * @param id Foursquare ID of the venue
	 * @param name Foursquare name of the venue
	 */
	public TheFoursquareVenue(int latitudeE6, int longitudeE6, String id, String name, int distance) {
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
	/**
	 * @return the venueDistance
	 */
	public String getVenueDistance() {
		return venueDistance;
	}

}
