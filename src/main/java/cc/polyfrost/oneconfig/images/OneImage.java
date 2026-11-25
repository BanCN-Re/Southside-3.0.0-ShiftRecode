package cc.polyfrost.oneconfig.images;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import dev.southside.utils.NetEaseMusicCrypto;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.zip.GZIPInputStream;

public final class OneImage {
   private static final String DEFAULT_TEXT = "https://music.163.com";
   private static final String DEFAULT_TEXT_VALUE = "https://interface3.music.163.com";
   private static final String DEFAULT_TEXT_DATA = "NeteaseMusic 9.0.90/5038 (iPhone; iOS 16.2; zh_CN)";
   private static final String DEFAULT_TEXT_REF = "9.5.61";
   private static final String DEFAULT_TEXT_PRIMARY = "12";
   private static final Gson GSON = new Gson();
   @SerializedName("http")
   private final HttpClient http;
   @SerializedName("deviceId")
   private final String deviceId;
   @SerializedName("cookies")
   private final Map<String, String> cookies = Collections.synchronizedMap(new LinkedHashMap<>());

   public OneImage() {
      this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10L)).followRedirects(Redirect.NORMAL).build();
      this.deviceId = getDisplayText();
   }

   private static String getDisplayText() {
      byte[] bytes = new byte[26];
      new Random().nextBytes(bytes);
      return NetEaseMusicCrypto.formatText(bytes, true);
   }

   public CompletableFuture<OneImage.Entry3> setColorAtPos(String text, int n, int n2) {
      JsonObject json = new JsonObject();
      json.addProperty("s", text);
      json.addProperty("type", 1);
      json.addProperty("limit", n2);
      json.addProperty("offset", n);
      json.addProperty("total", true);
      return this.getCompletableFuture("/api/cloudsearch/pc", json)
         .thenCompose(
            json2 -> {
               JsonObject asJsonObject = json2.getAsJsonObject("result");
               JsonArray array = asJsonObject == null ? null : asJsonObject.getAsJsonArray("songs");
               if (array != null && !array.isEmpty()) {
                  int n3 = Math.min(array.size(), n2);
                  int n4 = n + n3;
                  boolean flag = asJsonObject.has("songCount") && !asJsonObject.get("songCount").isJsonNull()
                     ? n4 < asJsonObject.get("songCount").getAsInt()
                     : (asJsonObject.has("hasMore") && !asJsonObject.get("hasMore").isJsonNull() ? asJsonObject.get("hasMore").getAsBoolean() : n3 == n2);
                  List list = collectItems(array, n2);
                  return this.getCompletableFuture(list, n2)
                     .exceptionally(obj -> list)
                     .thenApply(obj2 -> new OneImage.Entry3((List<OneImage.Entry4>)obj2, n4, flag));
               } else {
                  return CompletableFuture.completedFuture(new OneImage.Entry3(List.of(), n, false));
               }
            }
         );
   }

   public CompletableFuture<List<String>> getCompletableFuture2(String text) {
      JsonObject json = new JsonObject();
      json.addProperty("id", Long.parseLong(text));
      json.addProperty("n", 0);
      json.addProperty("s", 0);
      return this.getCompletableFuture("/api/v6/playlist/detail", json).thenApply(json2 -> {
         JsonObject asJsonObject = json2.getAsJsonObject("playlist");
         JsonArray array = asJsonObject == null ? null : asJsonObject.getAsJsonArray("trackIds");
         if (array != null && !array.isEmpty()) {
            ArrayList list = new ArrayList();

            for (JsonElement element : array) {
               JsonObject asJsonObject2 = element.getAsJsonObject();
               if (asJsonObject2.has("id")) {
                  list.add(asJsonObject2.get("id").getAsString());
               }
            }

            return List.copyOf(list);
         } else {
            return List.of();
         }
      });
   }

   public CompletableFuture<OneImage.Entry3> setColorAtPos(List<String> list, int n, int n2) {
      int n3 = Math.min(n, list.size());
      int n4 = Math.min(n3 + n2, list.size());
      List list2 = list.subList(n3, n4).stream().map(text -> new OneImage.Entry4(text, "", "", null, 0L)).toList();
      return this.getCompletableFuture(list2, n2).thenApply(obj -> new OneImage.Entry3((List<OneImage.Entry4>)obj, n4, n4 < list.size()));
   }

   private CompletableFuture<List<OneImage.Entry4>> getCompletableFuture(List<OneImage.Entry4> list, int n) {
      if (list.isEmpty()) {
         return CompletableFuture.completedFuture(List.of());
      }

      JsonArray array = new JsonArray();

      for (OneImage.Entry4 entry4 : list) {
         JsonObject json = new JsonObject();
         json.addProperty("id", Long.parseLong(entry4.getId()));
         array.add(json);
      }

      JsonObject json2 = new JsonObject();
      json2.addProperty("c", GSON.toJson(array));
      return this.getCompletableFuture3("/api/v3/song/detail", json2).thenApply(json3 -> {
         JsonArray asJsonArray = json3.getAsJsonArray("songs");
         if (asJsonArray == null) {
            throw new IllegalStateException("Missing song details");
         } else {
            return collectItems(asJsonArray, n);
         }
      });
   }

   public CompletableFuture<String> getCompletableFuture() {
      JsonObject json = new JsonObject();
      json.addProperty("type", 3);
      return this.getCompletableFuture("/api/login/qrcode/unikey", json)
         .thenApply(json2 -> json2.has("unikey") && !json2.get("unikey").isJsonNull() ? json2.get("unikey").getAsString() : null);
   }

   public CompletableFuture<OneImage.Entry2> getCompletableFuture3(String text) {
      JsonObject json = new JsonObject();
      json.addProperty("key", text);
      json.addProperty("type", 3);
      return this.getCompletableFuture2("/api/login/qrcode/client/login", json)
         .thenApply(json2 -> new OneImage.Entry2(json2.has("code") ? json2.get("code").getAsInt() : -1));
   }

   public CompletableFuture<OneImage.Entry> getCompletableFuture2() {
      return this.getCompletableFuture3("/api/nuser/account/get", new JsonObject()).thenApply(json -> {
         if (json.has("profile") && !json.get("profile").isJsonNull()) {
            JsonObject asJsonObject = json.getAsJsonObject("profile");
            String text = asJsonObject.has("nickname") && !asJsonObject.get("nickname").isJsonNull() ? asJsonObject.get("nickname").getAsString() : "User";
            String text2 = asJsonObject.has("avatarUrl") && !asJsonObject.get("avatarUrl").isJsonNull() ? asJsonObject.get("avatarUrl").getAsString() : null;
            return new OneImage.Entry(text, text2);
         } else {
            return null;
         }
      });
   }

   public void whenComplete() {
      this.getCompletableFuture3("/api/logout", new JsonObject()).whenComplete((json, t) -> this.cookies.clear());
   }

   public CompletableFuture<String> getCompletableFuture(String text) {
      JsonObject json = new JsonObject();
      json.addProperty("ids", "[" + text + "]");
      json.addProperty("br", 320000);
      return this.getCompletableFuture3("/api/song/enhance/player/url", json).thenApply(json2 -> {
         JsonArray asJsonArray = json2.getAsJsonArray("data");
         if (asJsonArray != null && !asJsonArray.isEmpty()) {
            JsonElement element = asJsonArray.get(0).getAsJsonObject().get("url");
            if (element != null && !element.isJsonNull()) {
               String asString = element.getAsString();
               return asString.isBlank() ? null : asString;
            } else {
               return null;
            }
         } else {
            return null;
         }
      });
   }

   private CompletableFuture<JsonObject> getCompletableFuture3(String text, JsonObject json) {
      json.addProperty("csrf_token", this.cookies.getOrDefault("__csrf", ""));
      String text2 = GSON.toJson(json);
      Map map = NetEaseMusicCrypto.buildMap(text2);
      return this.getCompletableFuture("https://music.163.com" + text.replace("/api/", "/weapi/"), map).thenApply(var -> this.toJson((String)var));
   }

   private CompletableFuture<JsonObject> getCompletableFuture(String text, JsonObject json) {
      return this.getCompletableFuture4(text, json).thenApply(this::toJson);
   }

   private CompletableFuture<JsonObject> getCompletableFuture2(String text, JsonObject json) {
      return this.getCompletableFuture4(text, json).thenApply(text2 -> {
         JsonObject json2 = (JsonObject)GSON.fromJson(text2, JsonObject.class);
         if (json2 == null) {
            throw new IllegalStateException("Empty response");
         } else {
            return json2;
         }
      });
   }

   private CompletableFuture<String> getCompletableFuture4(String text, JsonObject json) {
      json.add("header", this.toJson());
      String text2 = GSON.toJson(json);
      Map map = NetEaseMusicCrypto.buildMap(text, text2);
      return this.getCompletableFuture("https://interfacepc.music.163.com" + text.replace("/api/", "/eapi/"), map);
   }

   private JsonObject toJson() {
      JsonObject json = new JsonObject();
      json.addProperty("os", "pc");
      json.addProperty("appver", "9.5.61");
      json.addProperty("osver", "12");
      json.addProperty("deviceId", this.deviceId);
      json.addProperty("channel", "netease");
      json.addProperty("__csrf", this.cookies.getOrDefault("__csrf", ""));
      json.addProperty("versioncode", "140");
      json.addProperty("mobilename", "");
      json.addProperty("buildver", String.valueOf(System.currentTimeMillis() / 1000L));
      json.addProperty("resolution", "1920x1080");
      String text = this.cookies.get("MUSIC_U");
      if (text != null && !text.isBlank()) {
         json.addProperty("MUSIC_U", text);
      }

      return json;
   }

   private JsonObject toJson(String text) {
      JsonObject json = (JsonObject)GSON.fromJson(text, JsonObject.class);
      if (json == null) {
         throw new IllegalStateException("Empty response");
      } else {
         int n = json.has("code") ? json.get("code").getAsInt() : 200;
         if (n != 200) {
            throw new IllegalStateException("API code " + n);
         } else {
            return json;
         }
      }
   }

   private CompletableFuture<String> getCompletableFuture(String text, Map<String, String> map) {
      StringBuilder sb = new StringBuilder();

      for (Map.Entry entry : map.entrySet()) {
         if (sb.length() > 0) {
            sb.append('&');
         }

         sb.append(URLEncoder.encode((String)entry.getKey(), StandardCharsets.UTF_8))
            .append('=')
            .append(URLEncoder.encode((String)entry.getValue(), StandardCharsets.UTF_8));
      }

      Builder builder = HttpRequest.newBuilder(URI.create(text))
         .timeout(Duration.ofSeconds(15L))
         .header("User-Agent", "NeteaseMusic 9.0.90/5038 (iPhone; iOS 16.2; zh_CN)")
         .header("Referer", "https://music.163.com/")
         .header("Content-Type", "application/x-www-form-urlencoded")
         .POST(BodyPublishers.ofString(sb.toString()));
      if (!this.cookies.isEmpty()) {
         builder.header("Cookie", this.getDisplayTextEx());
      }

      return this.getCompletableFuture(builder.build());
   }

   private CompletableFuture<String> getCompletableFuture(HttpRequest httpRequest) {
      return this.http.sendAsync(httpRequest, BodyHandlers.ofByteArray()).thenApply(httpResponse -> {
         if (httpResponse.statusCode() / 100 != 2) {
            throw new IllegalStateException("HTTP " + httpResponse.statusCode());
         }

         this.performAction((HttpResponse<?>)httpResponse);
         byte[] bytes = httpResponse.body();
         if (bytes.length >= 16 && bytes.length % 16 == 0) {
            try {
               byte[] byteListEx = NetEaseMusicCrypto.getByteListEx(bytes, NetEaseMusicCrypto.getByteList());
               bytes = byteListEx.length > 1 && (byteListEx[0] & 255) == 31 && (byteListEx[1] & 255) == 139 ? getByteList(byteListEx) : byteListEx;
            } catch (Exception e) {
            }
         }

         return new String(bytes, StandardCharsets.UTF_8);
      });
   }

   private void performAction(HttpResponse<?> httpResponse) {
      for (String text : httpResponse.headers().allValues("set-cookie")) {
         String[] textArray = text.split(";")[0].split("=", 2);
         if (textArray.length == 2 && !textArray[0].isBlank()) {
            this.cookies.put(textArray[0].trim(), textArray[1].trim());
         }
      }
   }

   private String getDisplayTextEx() {
      StringBuilder sb = new StringBuilder();

      for (Map.Entry entry : this.cookies.entrySet()) {
         if (sb.length() > 0) {
            sb.append("; ");
         }

         sb.append((String)entry.getKey()).append('=').append((String)entry.getValue());
      }

      return sb.toString();
   }

   private static byte[] getByteList(byte[] bytes) {
      try (
         GZIPInputStream gzipinputstream = new GZIPInputStream(new ByteArrayInputStream(bytes));
         ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      ) {
         byte[] bytes2 = new byte[8192];

         int n;
         while ((n = gzipinputstream.read(bytes2)) > 0) {
            byteArrayOutputStream.write(bytes2, 0, n);
         }

         return byteArrayOutputStream.toByteArray();
      } catch (Exception e) {
         return bytes;
      }
   }

   private static List<OneImage.Entry4> collectItems(JsonArray array, int n) {
      ArrayList list = new ArrayList();

      for (JsonElement element : array) {
         if (list.size() >= n) {
            break;
         }

         JsonObject asJsonObject = element.getAsJsonObject();
         if (asJsonObject.has("id")) {
            String asString = asJsonObject.get("id").getAsString();
            String text = formatText(asJsonObject, "name", "Unknown");
            String text2 = "Unknown Artist";
            JsonArray array2 = asJsonObject.has("ar") && asJsonObject.get("ar").isJsonArray()
               ? asJsonObject.getAsJsonArray("ar")
               : (asJsonObject.has("artists") && asJsonObject.get("artists").isJsonArray() ? asJsonObject.getAsJsonArray("artists") : null);
            if (array2 != null) {
               StringBuilder sb = new StringBuilder();

               for (JsonElement element2 : array2) {
                  if (sb.length() > 0) {
                     sb.append('/');
                  }

                  sb.append(formatText(element2.getAsJsonObject(), "name", ""));
               }

               if (!sb.isEmpty()) {
                  text2 = sb.toString();
               }
            }

            String text3 = null;
            JsonObject json = asJsonObject.has("al") && asJsonObject.get("al").isJsonObject()
               ? asJsonObject.getAsJsonObject("al")
               : (asJsonObject.has("album") && asJsonObject.get("album").isJsonObject() ? asJsonObject.getAsJsonObject("album") : null);
            if (json != null && json.has("picUrl") && !json.get("picUrl").isJsonNull()) {
               String asString2 = json.get("picUrl").getAsString();
               if (!asString2.isBlank()) {
                  text3 = asString2;
               }
            }

            long l = asJsonObject.has("dt") ? asJsonObject.get("dt").getAsLong() : (asJsonObject.has("duration") ? asJsonObject.get("duration").getAsLong() : 0L);
            list.add(new OneImage.Entry4(asString, text, text2, text3, l));
         }
      }

      return list;
   }

   private static String formatText(JsonObject json, String text, String text2) {
      return json.has(text) && !json.get(text).isJsonNull() ? json.get(text).getAsString() : text2;
   }

   public void clear() {
      this.cookies.clear();
   }

   public record Entry(String nickname, String avatarUrl) {
      public String getNickname() {
         return this.nickname;
      }

      public String getAvatarUrl() {
         return this.avatarUrl;
      }
   }

   public record Entry2(int code) {
      public int getCode() {
         return this.code;
      }
   }

   public record Entry3(List<OneImage.Entry4> songs, int nextOffset, boolean hasMore) {
      public List<OneImage.Entry4> getSongs() {
         return this.songs;
      }

      public int getNextOffset() {
         return this.nextOffset;
      }

      public boolean isHasMore() {
         return this.hasMore;
      }
   }

   public record Entry4(String id, String title, String artist, String coverUrl, long durationMs) {
      public String getId() {
         return this.id;
      }

      public String getTitle() {
         return this.title;
      }

      public String getArtist() {
         return this.artist;
      }

      public String getCoverUrl() {
         return this.coverUrl;
      }

      public long getDurationMs() {
         return this.durationMs;
      }
   }
}
