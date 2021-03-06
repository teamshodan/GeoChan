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

package com.teamshodan.geochan.json;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.teamshodan.geochan.models.Comment;
import com.teamshodan.geochan.models.GeoLocation;
import com.teamshodan.geochan.models.ThreadComment;

/**
 * Handles the serialization of a ThreadComment object into JSON format.
 * 
 * @author Artem Herasymchuk
 */
public class ThreadCommentJsonConverter implements JsonSerializer<ThreadComment>,
        JsonDeserializer<ThreadComment> {

    /*
     * (non-Javadoc)
     * 
     * @see com.google.gson.JsonSerializer#serialize(java.lang.Object,
     * java.lang.reflect.Type, com.google.gson.JsonSerializationContext)
     */
    /**
     * Serializes a ThreadComment object into JSON format.
     * 
     * @param thread the ThreadComment to serialize.
     * @param type the Type
     * @param context the JsonSerializationContext
     * 
     * @return A JsonElement representing the serialized ThreadComment.
     */
    @Override
    public JsonElement serialize(ThreadComment thread, Type type, JsonSerializationContext context) {
        JsonObject object = new JsonObject();
        object.addProperty("title", thread.getTitle());
        object.addProperty("threadDate", thread.getThreadDate().getTime());
        object.addProperty("hasImage", thread.getBodyComment().hasImage());
        object.addProperty("id", thread.getId());
        if (thread.getBodyComment().getLocation() != null) {
            object.addProperty("location", thread.getBodyComment().getLocation().getLatitude()
                    + "," + thread.getBodyComment().getLocation().getLongitude());
            if (thread.getBodyComment().getLocation().getLocationDescription() != null) {
                object.addProperty("locationDescription", thread.getBodyComment().getLocation()
                        .getLocationDescription());
            }
        } else {
            object.addProperty("location", "-999,-999");
        }
        object.addProperty("user", thread.getBodyComment().getUser());
        object.addProperty("hash", thread.getBodyComment().getHash());
        object.addProperty("textPost", thread.getBodyComment().getTextPost());
        if (thread.getBodyComment().hasImage()) {
            Bitmap bitmapThumb = thread.getBodyComment().getImageThumb();

            /*
             * http://stackoverflow.com/questions/9224056/android-bitmap-to-base64
             * -string
             */
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmapThumb.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
            byte[] byteThumbArray = byteArrayOutputStream.toByteArray();
            String encodedThumb = Base64.encodeToString(byteThumbArray, Base64.NO_WRAP);
            object.addProperty("imageThumbnail", encodedThumb);
        }
        return object;
    }

    /**
     * Deserializes a ThreadComment object from JSON format.
     * 
     * @param json the JsonElement
     * @param type the Type
     * @param context the JsonDeserializationContext
     * 
     * @return The deserialized ThreadComment.
     * 
     * @throws JsonParseException
     */
    @Override
    public ThreadComment deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject object = json.getAsJsonObject();
        String title = object.get("title").getAsString();
        long threadDate = object.get("threadDate").getAsLong();
        boolean hasImage = object.get("hasImage").getAsBoolean();
        String locationString = object.get("location").getAsString();
        List<String> locationEntries = Arrays.asList(locationString.split(","));
        double latitude = Double.parseDouble(locationEntries.get(0));
        double longitude = Double.parseDouble(locationEntries.get(1));
        String user = object.get("user").getAsString();
        String hash = object.get("hash").getAsString();
        String id = object.get("id").getAsString();
        String textPost = object.get("textPost").getAsString();
        String locationDescription = null;
        if (object.get("locationDescription") != null) {
            locationDescription = object.get("locationDescription").getAsString();
        }
        Bitmap thumbnail = null;
        if (hasImage) {
            /*
             * http://stackoverflow.com/questions/20594833/convert-byte-array-or-
             * bitmap-to-picture
             */
            // http://stackoverflow.com/a/5878773
            // Sando's workaround for running out of memory on decoding bitmaps.
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inDither = false; // Disable Dithering mode
            opts.inPurgeable = true; // Tell to gc that whether it needs free
                                     // memory, the Bitmap can be cleared
            opts.inInputShareable = true; // Which kind of reference will be
                                          // used to recover the Bitmap data
                                          // after being clear, when it will be
                                          // used in the future
            opts.inTempStorage = new byte[32 * 1024];

            String encodedThumb = object.get("imageThumbnail").getAsString();
            byte[] thumbArray = Base64.decode(encodedThumb, Base64.NO_WRAP);
            thumbnail = BitmapFactory.decodeByteArray(thumbArray, 0, thumbArray.length, opts);
        }
        GeoLocation location = new GeoLocation(latitude, longitude);
        location.setLocationDescription(locationDescription);
        final Comment c = new Comment(textPost, null, location, null);
        c.getCommentDate().setTime(threadDate);
        c.setUser(user);
        c.setHash(hash);
        c.setId(Long.parseLong(id));
        if (hasImage) {
            c.setImageThumb(thumbnail);
        }
        final ThreadComment comment = new ThreadComment(c, title);
        comment.setThreadDate(new Date(threadDate));
        comment.setId(Long.parseLong(id));
        return comment;
    }
}
