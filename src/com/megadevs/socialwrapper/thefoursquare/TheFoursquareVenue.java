/**
 * 
 */
package com.megadevs.socialwrapper.thefoursquare;

/**
 * @author dextor
 *
 */
public class TheFoursquareVenue {

	// the Foursquare-assigned ID
	private String venueID;
	private String venueName;
	private String venueDistance;
	private double latitude;
	private double longitude;
	
	/**
	 * Default constructor for a TheFoursquareVenue object.
	 * @param latitudeE6 latitude coordinates for the venue
	 * @param longitudeE6 longitude coordinates for the venue
	 * @param distance distance of the venue from the current position
	 * @param id Foursquare ID of the venue
	 * @param name Foursquare name of the venue
	 */
	public TheFoursquareVenue(double latitude, double longitude, String id, String name, int distance) {
		venueID = id;
		venueName = name;
		this.latitude = latitude;
		this.longitude = longitude;
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
	/**
	 * @return the latitude
	 */
	public double getLatitude() {
		return latitude;
	}
	/**
	 * @return the longitude
	 */
	public double getLongitude() {
		return longitude;
	}

}
