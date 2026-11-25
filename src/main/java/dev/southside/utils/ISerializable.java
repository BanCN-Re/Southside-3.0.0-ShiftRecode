package dev.southside.utils;

import com.google.gson.JsonObject;

public interface ISerializable<T> {
   T fromJson(JsonObject json);

   JsonObject toJson();
}
