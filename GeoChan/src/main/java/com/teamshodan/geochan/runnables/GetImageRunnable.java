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

package com.teamshodan.geochan.runnables;

import android.graphics.Bitmap;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import com.teamshodan.geochan.helpers.ElasticSearchClient;
import com.teamshodan.geochan.helpers.GsonHelper;
import com.teamshodan.geochan.models.ElasticSearchResponse;
import com.teamshodan.geochan.tasks.GetImageTask;
import io.searchbox.client.JestResult;
import io.searchbox.core.Get;

/**
 * Runnable for retrieving a bitmap object in a separate thread of execution from
 * ElasticSearch. 
 * 
 * @author Artem Chikin
 *
 */
public class GetImageRunnable implements Runnable {

	private GetImageTask task;
	private String type = ElasticSearchClient.TYPE_IMAGE;
	public static final int STATE_GET_IMAGE_FAILED = -1;
	public static final int STATE_GET_IMAGE_RUNNING = 0;
	public static final int STATE_GET_IMAGE_COMPLETE = 1;

	public GetImageRunnable(GetImageTask task) {
		this.task = task;
	}

	/**
	 * Forms a query and sends a get request to ES,
	 * then processes retrieved data as a bitmap and
	 * sends it to the task's cache.
	 */
	@Override
	public void run() {
		task.setGetImageThread(Thread.currentThread());
		android.os.Process
				.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
		task.handleGetImageState(STATE_GET_IMAGE_RUNNING);
		JestResult result = null;
		try {
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
			
			Get get = new Get.Builder(ElasticSearchClient.URL_INDEX,
					task.getId()).type(type).build();
			result = ElasticSearchClient.getInstance().getClient().execute(get);

			Type type = new TypeToken<ElasticSearchResponse<Bitmap>>() {
			}.getType();
			Gson gson = GsonHelper.getOnlineGson();
			ElasticSearchResponse<Bitmap> esResponse = gson.fromJson(
					result.getJsonString(), type);
			
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
			
			task.setImageCache(esResponse.getSource());
			task.handleGetImageState(STATE_GET_IMAGE_COMPLETE);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (result == null || !result.isSucceeded()) {
				task.handleGetImageState(STATE_GET_IMAGE_FAILED);
			}
			// task.setGetImageThread(null);
			Thread.interrupted();
		}

	}

}
