SocialWrapper
=============

Easily extensible, low coupling and highly useful Android library that allows simple interactions with the most famous social networks.

IMPORTANT -- PLEASE READ

This documentation is pretty rough and needs a proper revision. If you have any doubts or suggestions, please do not hesitate to contact us.

What is it?
-----------

The main purpose of this library is to give a fast and easy way for an Android developer to communicate and interact with the following social networks:

* Facebook
* Twitter
* Foursquare
* Flickr
* Tumblr

How does it work?
-----------------

The SocialWrapper entity (which is a singleton implementation) holds reference to the available social networks. It also provides a built-in session manager that handles saving/restoring/erasing for social sessions.

A SocialNetwork entity represents a social network instance. Given the strong heterogeneity between the available social networks, it wasn't possible to create a common interface for all of them: for example, the concept of Facebook's "post on Stefano's wall/timeline" may correspond to "tweet @Stefano" but it has nothing to do with Foursquare. For this particular reason a SocialNetwork entity must only implement a few methods, such as authentication and deauthentication.

In order to interact with a social network you must create an application (usually in the developer section - e.g. https://developers.facebook.com), where you are provided with some kind of keys: these parameters are essential for the communication and they must therefore be assigned properly to the corresponding SocialNetwork entity.

Basic example
-------------

Here is a very basic example of usage.

	SocialWrapper wrapper = SocialWrapper.getInstance();
	wrapper.setActivity(mContext);
	TheFacebook theFacebook = (TheFacebook) wrapper.getSocialNetwork(SocialWrapper.THEFACEBOOK);
	theFacebook.setParameters("your_app_id");
	theFacebook.authenticate(new TheFacebook.TheFacebookLoginCallback() {
		onLoginCallback(String result) {
			// login successful
		}

		onErrorCallback(String error) {
			// error occurred
		}

	});

Pay attention to this command

	wrapper.setActivity(mContext)

The setActivity() method must be called from within the activity containing the SocialWrapper instance. This is because some social networks, like Facebook and Foursquare, will use a custom Dialog UI that cannot be created without a valid Context.

Extensive example
-----------------

Another GitHub project called SocialWrapperTester will soon be uploaded. It contains an activity which provides the code to test every single social network method available on this library.

Contacts
--------

e-mail: info@megadevs.com

[MegaDevs official website](http://megadevs.com/)

[MegaDevs official blog](http://megazine.megadevs.com)
