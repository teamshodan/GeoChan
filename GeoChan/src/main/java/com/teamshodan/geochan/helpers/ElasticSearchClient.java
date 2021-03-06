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

package com.teamshodan.geochan.helpers;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.ClientConfig;

/**
 * This class is responsible for ElasticSearch operations. Contains server
 * connection data and methods to create a singleton
 * 
 * @author Artem Herasymchuk
 */
public class ElasticSearchClient {
	private static ElasticSearchClient instance = null;
	private static JestClient client;

	public static final String TYPE_COMMENT = "geoComment";
	public static final String TYPE_THREAD = "geoThread";
	public static final String TYPE_INDEX = "geoCommentList";
	public static final String TYPE_IMAGE = "geoImage";
	public static final String URL = "http://cmput301.softwareprocess.es:8080";
	public static final String URL_INDEX = "cmput301w14t08";

	private ElasticSearchClient() {
		ClientConfig config = new ClientConfig.Builder(URL).multiThreaded(true)
				.build();
		JestClientFactory factory = new JestClientFactory();
		factory.setClientConfig(config);
		client = factory.getObject();
	}

	/**
	 * Returns the single instance of the ElasticSearchClient.
	 * ElasticSearchClient follows the singleton design pattern, so only one
	 * instance of the class exists.
	 * 
	 * @return the instance of ElasticSearchClient.
	 * 
	 */
	public static ElasticSearchClient getInstance() {
		if (instance == null) {
			instance = new ElasticSearchClient();
		}
		return instance;
	}

	public JestClient getClient() {
		return client;
	}
}
