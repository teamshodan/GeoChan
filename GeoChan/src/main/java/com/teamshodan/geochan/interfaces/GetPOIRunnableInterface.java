/*
 * Copyright 2014 Artem Chikin
 * Copyright 2014 Artem Herasymchuk
 * Copyright 2014 Tom Krywitsky
 * Copyright 2014 Henry Pabst
 * Copyright 2014 Bradley Simons
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.teamshodan.geochan.interfaces;

/**
 * Provides an interface for a Runnable that gets a point of interest
 * from ElasticSearch.
 * @author Artem Chikin
 *
 */
public interface GetPOIRunnableInterface {

    /** 
     * Handles the various possible states of the
     * Runnable that obtains the POI.
     * @param state the state
     */
	void handleGetPOIState(int state);
	
	/* Getters and setters */
	
	void setGetPOIThread(Thread thread);
	
	void setPOICache(String poi);
	
	String getPOICache();
	
}
