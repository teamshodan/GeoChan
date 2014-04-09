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

import com.teamshodan.geochan.models.CommentList;

/**
 * Provides an interface for a Runnable that gets
 * a list of comments from ElasticSearch.
 * @author Artem Herasymchuk
 *
 */
public interface GetCommentListRunnableInterface {

    /**
     * Handles the possible states of the Runnable
     * that gets the comment list.
     * @param state the state
     */
    void handleGetCommentListState(int state);
    
    /* Getters and setters */
    
    void setGetCommentListThread(Thread thread);

    void setCommentListCache(CommentList cache);

    CommentList getCommentListCache();
}
