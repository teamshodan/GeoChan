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

package com.teamshodan.geochan.tasks;

import android.app.ProgressDialog;

import com.teamshodan.geochan.interfaces.GetPOIRunnableInterface;
import com.teamshodan.geochan.interfaces.PostImageRunnableInterface;
import com.teamshodan.geochan.interfaces.PostRunnableInterface;
import com.teamshodan.geochan.interfaces.TaskInterface;
import com.teamshodan.geochan.interfaces.UpdateRunnableInterface;
import com.teamshodan.geochan.managers.ThreadManager;
import com.teamshodan.geochan.models.Comment;
import com.teamshodan.geochan.models.GeoLocation;
import com.teamshodan.geochan.models.ThreadComment;
import com.teamshodan.geochan.runnables.GetPOIOnPostRunnable;
import com.teamshodan.geochan.runnables.PostImageRunnable;
import com.teamshodan.geochan.runnables.PostRunnable;
import com.teamshodan.geochan.runnables.UpdateRunnable;

/**
 * Responsible for the task of controlling the runnables that are responsible
 * for the various parts of posting a ThreadComment or Comment to ElasticSearch.
 * 
 * @author Artem Herasymchuk
 * @author Artem Chikin
 * 
 */
public class PostTask implements TaskInterface, GetPOIRunnableInterface,
		PostImageRunnableInterface, PostRunnableInterface,
		UpdateRunnableInterface {
	private Comment comment;
	private GeoLocation location;
	private ProgressDialog dialog;
	private String title;
	private String cache;
	private ThreadManager manager;
	private Thread thread;
	private boolean isEdit;
	private ThreadComment threadComment;
	private Runnable imageRunnable;
	private Runnable postRunnable;
	private Runnable updateRunnable;
	private Runnable getPOIRunnable;

	/**
	 * Constructs an instance of the task and its runnables.
	 */
	public PostTask() {
		imageRunnable = new PostImageRunnable(this);
		postRunnable = new PostRunnable(this);
		updateRunnable = new UpdateRunnable(this);
		getPOIRunnable = new GetPOIOnPostRunnable(this);
	}

	/**
	 * Initializes the instance of the task with the information needed to run
	 * it.
	 * 
	 * @param manager
	 *            instance of the ThreadManager
	 * @param comment
	 *            the Comment to be posted
	 * @param title
	 *            the title of the ThreadComment, or null if posting a Comment
	 * @param location
	 *            the GeoLocation
	 * @param dialog
	 *            a ProgressDialog inside the fragment to display the task's
	 *            progress
	 */
	public void initPostTask(ThreadManager manager, Comment comment,
			String title, GeoLocation location, ProgressDialog dialog, boolean isEdit) {
		this.manager = manager;
		this.comment = comment;
		this.title = title;
		this.location = location;
		this.dialog = dialog;
		this.threadComment = null;
		this.isEdit = isEdit;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleImageState(int state) {
		int outState;
		switch (state) {
		case PostImageRunnable.STATE_IMAGE_COMPLETE:
			outState = ThreadManager.POST_IMAGE_COMPLETE;
			break;
		case PostImageRunnable.STATE_IMAGE_FAILED:
			outState = ThreadManager.POST_IMAGE_FAILED;
			break;
		default:
			outState = ThreadManager.POST_IMAGE_RUNNING;
			break;
		}
		handleState(outState);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handlePostState(int state) {
		int outState;
		switch (state) {
		case PostRunnable.STATE_POST_COMPLETE:
			outState = ThreadManager.POST_COMPLETE;
			break;
		case PostRunnable.STATE_POST_FAILED:
			outState = ThreadManager.POST_FAILED;
			break;
		default:
			outState = ThreadManager.POST_RUNNING;
			break;
		}
		handleState(outState);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleUpdateState(int state) {
		int outState;
		switch (state) {
		case UpdateRunnable.STATE_UPDATE_COMPLETE:
			outState = ThreadManager.POST_TASK_COMPLETE;
			break;
		case UpdateRunnable.STATE_UPDATE_FAILED:
			outState = ThreadManager.UPDATE_FAILED;
			break;
		default:
			outState = ThreadManager.UPDATE_RUNNING;
			break;
		}
		handleState(outState);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleGetPOIState(int state) {
		int outState;
		switch (state) {
		case GetPOIOnPostRunnable.STATE_GET_POI_COMPLETE:
			outState = ThreadManager.POST_GET_POI_COMPLETE;
			break;
		case GetPOIOnPostRunnable.STATE_GET_POI_FAILED:
			outState = ThreadManager.POST_GET_POI_FAILED;
			break;
		default:
			outState = ThreadManager.POST_GET_POI_RUNNING;
			break;
		}
		handleState(outState);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleState(int state) {
		manager.handlePostState(this, state);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Thread getCurrentThread() {
		synchronized (manager) {
			return thread;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setCurrentThread(Thread thread) {
		synchronized (manager) {
			this.thread = thread;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void recycle() {
		comment = null;
		manager = null;
		title = null;
		cache = null;
		location = null;
	}

	/* Getters/setters for the interfaces this task implements */

	@Override
	public void setPostThread(Thread thread) {
		setCurrentThread(thread);
	}

	@Override
	public void setImageThread(Thread thread) {
		setCurrentThread(thread);
	}

	@Override
	public void setGetPOIThread(Thread thread) {
		setCurrentThread(thread);
	}

	@Override
	public void setUpdateThread(Thread thread) {
		setCurrentThread(thread);
	}

	@Override
	public void setPOICache(String cache) {
		this.cache = cache;
	}

	@Override
	public String getPOICache() {
		return cache;
	}

	/* Basic getters/setters below */

	public Comment getComment() {
		return comment;
	}

	public String getTitle() {
		return title;
	}

	public Runnable getGetPOIRunnable() {
		return getPOIRunnable;
	}

	public Runnable getImageRunnable() {
		return imageRunnable;
	}

	public Runnable getPostRunnable() {
		return postRunnable;
	}

	public Runnable getUpdateRunnable() {
		return updateRunnable;
	}

	public GeoLocation getLocation() {
		return location;
	}

	public ProgressDialog getDialog() {
		return dialog;
	}
	
	public boolean isEdit() {
		return isEdit;
	}

	public void setThreadComment(ThreadComment threadComment) {
		this.threadComment = threadComment;
	}

	public ThreadComment getThreadComment() {
		return threadComment;
	}
}
