package com.megadevs.socialwrapper;

import java.io.Serializable;


/**
 * This class models the concept of 'friend'/'follower' in a social network environment.
 * Each friend is identified by an ID (which varies with regard to the considered social
 * network), a name (it means name and surname, separated by a space) and an image (it may
 * not be set).
 * @author dextor
 *
 */
@SuppressWarnings("serial")
public class SocialFriend implements Serializable {
	private String id;
	private String name;
	private String imgURL;
	
	public SocialFriend(String id, String name, String imgURL) {
		super();
		this.id = id;
		this.name = name;
		this.imgURL = imgURL;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the imgURL
	 */
	public String getImgURL() {
		return imgURL;
	}

}
