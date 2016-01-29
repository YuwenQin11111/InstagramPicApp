/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.threadsample;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.helpers.DefaultHandler;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.ContentValues;
import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

/**
 * RSSPullParser reads an RSS feed from the Picasa featured pictures site. It uses
 * several packages from the widely-known XMLPull API.
 *
 */
public class RSSPullParser extends DefaultHandler {
    // Global constants

    // Sets the initial size of the vector that stores data.
    private static final int VECTOR_INITIAL_SIZE = 500;

    // Storage for a single ContentValues for image data
    private static ContentValues mImage;
    
    // A vector that will contain all of the images
    private Vector<ContentValues> mImages;

    /**
     * A getter that returns the image data Vector
     * @return A Vector containing all of the image data retrieved by the parser
     */
    public Vector<ContentValues> getImages() {
        return mImages;
    }
    /**
     * This method parses XML in an input stream and stores parts of the data in memory
     *
     * @param inputStream a stream of data containing XML elements, usually a RSS feed
     * @param progressNotifier a helper class for sending status and logs
     * @throws XmlPullParserException defined by XMLPullParser; thrown if the thread is cancelled.
     * @throws IOException thrown if an IO error occurs during parsing
     * @throws JSONException 
     */
    public void parseXml(InputStream inputStream,
            BroadcastNotifier progressNotifier)
            throws XmlPullParserException, IOException, JSONException {

    	String jsonString = "";
    	byte[] buffer = new byte[1024*20];
    	int result = inputStream.read(buffer);
    	while (result > 0) {
    		byte[] buf = new byte[result];
    		System.arraycopy(buffer, 0, buf, 0, result);
    		jsonString += new String(buf);
    		result = inputStream.read(buffer);
    	}
    	
    	JSONObject jsonObject  = new JSONObject(jsonString);
    	JSONArray jsonArray = jsonObject.getJSONArray("data");

        // Sets the number of images read to 1
        int imageCount = 1;

        // Creates a new store for image URL data
        mImages = new Vector<ContentValues>(VECTOR_INITIAL_SIZE);

        int i = 0;
        // Loops indefinitely. The exit occurs if there are no more URLs to process
        while (i < jsonArray.length()) {
        	jsonObject = jsonArray.getJSONObject(i);
        	String standardResolutionImageUrl = jsonObject.getJSONObject("images").getJSONObject("standard_resolution").getString("url");
        	String lowResolutionImageUrl = jsonObject.getJSONObject("images").getJSONObject("low_resolution").getString("url");
        	
        	mImage = new ContentValues();
        	String imageUrlKey;
            String imageNameKey;
            String fileName;
            imageUrlKey = DataProviderContract.IMAGE_URL_COLUMN;
            imageNameKey = DataProviderContract.IMAGE_PICTURENAME_COLUMN;
            fileName = Uri.parse(standardResolutionImageUrl).getLastPathSegment();
            mImage.put(imageUrlKey, standardResolutionImageUrl);
            mImage.put(imageNameKey, fileName);
            
            imageUrlKey = DataProviderContract.IMAGE_THUMBURL_COLUMN;
            imageNameKey = DataProviderContract.IMAGE_THUMBNAME_COLUMN;
            fileName = Uri.parse(lowResolutionImageUrl).getLastPathSegment();
            mImage.put(imageUrlKey, lowResolutionImageUrl);
            mImage.put(imageNameKey, fileName);
            
            // Logs progress
            progressNotifier.notifyProgress("Parsed Image[" + imageCount + "]:"
                    + mImage.getAsString(DataProviderContract.IMAGE_URL_COLUMN));

            // Adds the current ContentValues to the ContentValues storage
            mImages.add(mImage);
            
            // Increments the count of the number of images stored.
            imageCount++;
            
            // Clears out the current ContentValues
            mImage = null;
            
        	i++;
        }
    }
}
