#!/bin/bash

# enable shell extensions: this makes cleaning up very easy
shopt -s extglob

SCRIPT_NAME=`basename $0`
TWITTER4J_DOWNLOAD_WEBSITE="http://twitter4j.org/en/"
TWITTER4J_LATEST_RELEASE="twitter4j-android-2.2.5.zip"
TWITTER4J_DESIRED_LIB="core"
TWITTER4J_TEMP_VARIABLE=${TWITTER4J_LATEST_RELEASE/-/-core-}
TWITTER4J_CORE_LIB=${TWITTER4J_TEMP_VARIABLE/.zip/.jar}

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

echo '---------------------'
echo 'Checking for Twitter libs..'
echo '--'

if [ -f $TWITTER4J_CORE_LIB -o -f $TWITTER4J_LATEST_RELEASE ];
	then echo '>> Twitter4J already present, skipping to next library';
else
	echo '>> Twitter4J not found, downloading..'
	echo '--'
	wget $TWITTER4J_DOWNLOAD_WEBSITE$TWITTER4J_LATEST_RELEASE
	
	echo '>> Twitter$J successfully downloaded. Unzipping..'
	echo '--'
	unzip $TWITTER4J_LATEST_RELEASE >/dev/null
	
	echo '>> Unzipped. Moving library and cleaning up..'
	echo '--'
	mv lib/$TWITTER4J_CORE_LIB .
	rm -rf !($SCRIPT_NAME|$TWITTER4J_CORE_LIB)	

	echo -e 'Twitter4J successfully downloaded. Moving to next library.. \n'
fi

echo '---------------------'


#find . -regex ".*\(sh\|jar\)$" | xargs rm -rf
#find . ! -iname $SCRIPT_NAME -o -iname $TWITTER4J_CORE_LIB -exec -rm -rf;