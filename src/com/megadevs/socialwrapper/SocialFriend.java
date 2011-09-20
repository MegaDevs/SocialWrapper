package com.megadevs.socialwrapper;

import java.io.Serializable;

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
