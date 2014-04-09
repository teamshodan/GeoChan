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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import com.teamshodan.geochan.helpers.ElasticSearchClient;
import com.teamshodan.geochan.helpers.ElasticSearchQueries;
import com.teamshodan.geochan.helpers.GsonHelper;
import com.teamshodan.geochan.models.ElasticSearchResponse;
import com.teamshodan.geochan.models.ElasticSearchSearchResponse;
import com.teamshodan.geochan.models.ThreadComment;
import com.teamshodan.geochan.models.ThreadList;
import com.teamshodan.geochan.tasks.GetThreadCommentsTask;
import io.searchbox.client.JestResult;
import io.searchbox.core.Search;


/**
 * Runnable for retrieving a ThreadComment objects in a separate thread of execution from
 * ElasticSearch. 
 * 
 * @author Artem Herasymchuk
 *
 */
public class GetThreadCommentsRunnable implements Runnable {
	private GetThreadCommentsTask task;
	private String type = ElasticSearchClient.TYPE_THREAD;
	public static final int STATE_GET_THREADS_FAILED = -1;
	public static final int STATE_GET_THREADS_RUNNING = 0;
	public static final int STATE_GET_THREADS_COMPLETE = 1;

	public GetThreadCommentsRunnable(GetThreadCommentsTask task) {
		this.task = task;
	}

	/**
	 * Forms a query and sends a Get request to ES, then processes
	 * retrieved data as an array of ThreadComment objects.
	 */
	@Override
	public void run() {
		task.setGetThreadCommentsThread(Thread.currentThread());
		android.os.Process
				.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
		task.handleGetThreadCommentsState(STATE_GET_THREADS_RUNNING);
		JestResult result = null;
		try {
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
			String query = ElasticSearchQueries.SEARCH_MATCH_ALL;
			Search search = new Search.Builder(query)
					.addIndex(ElasticSearchClient.URL_INDEX).addType(type)
					.build();
			result = ElasticSearchClient.getInstance().getClient()
					.execute(search);
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
			Type elasticSearchSearchResponseType = new TypeToken<ElasticSearchSearchResponse<ThreadComment>>() {
			}.getType();
			Gson gson = GsonHelper.getOnlineGson();
			ElasticSearchSearchResponse<ThreadComment> esResponse = gson
					.fromJson(result.getJsonString(),
							elasticSearchSearchResponseType);
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
			ArrayList<ThreadComment> list = new ArrayList<ThreadComment>();
			for (ElasticSearchResponse<ThreadComment> r : esResponse.getHits()) {
				ThreadComment object = r.getSource();
				list.add(object);
			}
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
			ThreadList.setThreads(list);
			task.handleGetThreadCommentsState(STATE_GET_THREADS_COMPLETE);
		} catch (Exception e) {
			//
		} finally {
			if (result == null || !result.isSucceeded()) {
				task.handleGetThreadCommentsState(STATE_GET_THREADS_FAILED);
			}
			// task.setGetCommentListThread(null);
			Thread.interrupted();
		}
	}
}
