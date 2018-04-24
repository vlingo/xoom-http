// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.http.resource.serialization;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

public class JsonSerialization {
  private final static Gson gson;
  
  static {
    gson = new GsonBuilder()
        .registerTypeAdapter(Date.class, new DateSerializer())
        .registerTypeAdapter(Date.class, new DateDeserializer()).create();
  }

  public static <T> T deserialized(String serialization, final Class<T> type) {
    T instance = gson.fromJson(serialization, type);
    return instance;
  }

  public static <T> T deserialized(String serialization, final Type type) {
    T instance = gson.fromJson(serialization, type);
    return instance;
  }

  public static <T> List<T> deserializedList(String serialization, final Type listOfType) {
    final List<T> list = gson.fromJson(serialization, listOfType);
    return list;
  }
  
  public static String serialized(final Object instance) {
    final String serialization = gson.toJson(instance);
    return serialization;
  }

  public static <T> String serialized(final List<T> instance) {
    final Type listOfT = new TypeToken<List<T>>(){}.getType();
    final String serialization = gson.toJson(instance, listOfT);
    return serialization;
  }

  private static class DateSerializer implements JsonSerializer<Date> {
    public JsonElement serialize(Date source, Type typeOfSource, JsonSerializationContext context) {
        return new JsonPrimitive(Long.toString(source.getTime()));
    }
  }

  private static class DateDeserializer implements JsonDeserializer<Date> {
    public Date deserialize(JsonElement json, Type typeOfTarget, JsonDeserializationContext context) throws JsonParseException {
        long time = Long.parseLong(json.getAsJsonPrimitive().getAsString());
        return new Date(time);
    }
  }
}
