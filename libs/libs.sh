#!/bin/bash

# enable shell extensions: this makes cleaning up very easy
shopt -s extglob


echo "
#######################################################
#     __    _ __         ______     __  __            #
#    / /   (_) /_  _____/ ____/__  / /_/ /____  _____ #
#   / /   / / __ \/ ___/ / __/ _ \/ __/ __/ _ \/ ___/ #
#  / /___/ / /_/ (__  ) /_/ /  __/ /_/ /_/  __/ /     #
# /_____/_/_.___/____/\____/\___/\__/\__/\___/_/      #
#                                                     #
#                     MegaDevs®                       #
#                                                     #
#######################################################"

SCRIPT_NAME=`basename $0`
TOBEKEPT=$SCRIPT_NAME"|android-support-v4.jar|armeabi|CWAC-AdapterWrapper.jar|CWAC-Bus.jar|CWAC-Cache.jar|CWAC-Endless.jar|CWAC-Task.jar"

##############################
# 			TWITTER			 #
##############################

TWITTER4J_DOWNLOAD_WEBSITE="http://twitter4j.org/en/"
TWITTER4J_LATEST_RELEASE="twitter4j-android-2.2.5.zip"
TWITTER4J_DESIRED_LIB="core"
TWITTER4J_TEMP_VARIABLE=${TWITTER4J_LATEST_RELEASE/-/-core-}
TWITTER4J_CORE_LIB=${TWITTER4J_TEMP_VARIABLE/.zip/.jar}

echo '---------------------'
echo 'Checking for Twitter libs..'
echo '--'

if [ -f $TWITTER4J_CORE_LIB -o -f $TWITTER4J_LATEST_RELEASE ];
	then echo '>> Twitter4J libs are already present, skipping to next library';
else
	echo '>> Twitter4J not found, downloading..'
	echo '--'
	wget $TWITTER4J_DOWNLOAD_WEBSITE$TWITTER4J_LATEST_RELEASE
	
	echo '>> Twitter4J successfully downloaded. Unzipping..'
	echo '--'
	unzip $TWITTER4J_LATEST_RELEASE >/dev/null
	
	echo '>> Unzipped. Moving library and cleaning up..'
	echo '--'
	mv lib/$TWITTER4J_CORE_LIB .
	TOBEKEPT=$TOBEKEPT"|"$TWITTER4J_CORE_LIB
	echo $TOBEKEPT
	rm -rf !($TOBEKEPT)	

	echo -e 'Twitter4J successfully downloaded. Moving to next library.. \n'
fi

##############################
# 			TUMBLR			 #
##############################

HTTPCLIENT_URL="http://apache.bfeel.it//httpcomponents/httpclient/binary/httpcomponents-client-4.1.2-bin.zip"
HTTPCLIENT_ZIP="httpcomponents-client-4.1.2-bin.zip"
HTTCLIENT_BASE_DIR="httpcomponents-client-4.1.2"
HTTPCLIENT_LIB="httpclient-4.1.2.jar"
SIGNPOST_URL="http://oauth-signpost.googlecode.com/files/"
SIGNPOST_CORE="signpost-core-1.2.1.1.jar"
SIGNPOST_COMMONS="signpost-commonshttp4-1.2.1.1.jar"

echo '---------------------'
echo 'Checking for Tumblr libs..'
echo '--'

if [ -f $HTTPCLIENT_LIB -a -f $SIGNPOST_CORE -a -f $SIGNPOST_COMMONS ] ;
	then echo '>> Tumblr libs are already present, skipping to next library';
else
	echo '>> Tumblr libs not found, downloading..'
	echo '--'
	wget $HTTPCLIENT_URL
	wget $SIGNPOST_URL$SIGNPOST_CORE
	wget $SIGNPOST_URL$SIGNPOST_COMMONS
	
	echo '>> Packages successfully downloaded. Unzipping..'
	echo '--'
	unzip $HTTPCLIENT_ZIP > /dev/null
	
	echo '>> Unzipped. Moving library and cleaning up..'
	echo '--'
	mv $HTTCLIENT_BASE_DIR/lib/$HTTPCLIENT_LIB .
	TOBEKEPT=$TOBEKEPT"|"$HTTPCLIENT_LIB"|"$SIGNPOST_CORE"|"$SIGNPOST_COMMONS
	rm -rf !($TOBEKEPT)	

	echo -e 'Tumblr successfully downloaded. Moving to next library.. \n'
fi

##############################
# 			FLICKR			 #
##############################

echo '---------------------'
echo 'Checking for Flickr libs..'
echo '--'

FLICKRJ_ANDROID_LIBS="flickrj-android-1.0.1.20111224194607.jar"
FLICKRJ_ANDROID_URL="http://flickrj-android.googlecode.com/files/flickrj-android-1.0.1.20111224194607.jar"

if [ -f $FLICKRJ_ANDROID_LIBS ] ;
	then echo '>> Flickr libs are already present, skipping to the next library';
else
	echo '>> Flickr libs not found, downloading..'
	echo '--'
	wget $FLICKRJ_ANDROID_URL

	echo -e 'Flickr successfully downloaded. Moving to next library.. \n'
fi



#find . -regex ".*\(sh\|jar\)$" | xargs rm -rf
#find . ! -iname $SCRIPT_NAME -o -iname $TWITTER4J_CORE_LIB -exec -rm -rf;