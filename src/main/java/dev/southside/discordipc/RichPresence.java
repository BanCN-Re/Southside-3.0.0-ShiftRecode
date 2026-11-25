package dev.southside.discordipc;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

public class RichPresence {
   @SerializedName("details")
   private String details;
   @SerializedName("state")
   private String state;
   @SerializedName("assets")
   private RichPresence.Inner2 assets;
   @SerializedName("timestamps")
   private RichPresence.Inner timestamps;

   public void applyText(String text, String text2) {
      if (this.assets == null) {
         this.assets = new RichPresence.Inner2();
      }

      this.assets.smallImage = text;
      this.assets.smallText = text2;
   }

   public void setState(String text) {
      this.state = text;
   }

   public void performAction(long l) {
      if (this.timestamps == null) {
         this.timestamps = new RichPresence.Inner();
      }

      this.timestamps.end = l;
   }

   public void applyTextEx(String text, String text2) {
      if (this.assets == null) {
         this.assets = new RichPresence.Inner2();
      }

      this.assets.largeImage = text;
      this.assets.largeText = text2;
   }

   public void performActionEx(long l) {
      if (this.timestamps == null) {
         this.timestamps = new RichPresence.Inner();
      }

      this.timestamps.start = l;
   }

   public void setDetails(String text) {
      this.details = text;
   }

   public JsonObject toJson() {
      JsonObject json = new JsonObject();
      if (this.details != null) {
         json.addProperty("details", this.details);
      }

      if (this.state != null) {
         json.addProperty("state", this.state);
      }

      if (this.assets != null) {
         JsonObject json2 = new JsonObject();
         if (this.assets.largeImage != null) {
            json2.addProperty("large_image", this.assets.largeImage);
         }

         if (this.assets.largeText != null) {
            json2.addProperty("large_text", this.assets.largeText);
         }

         if (this.assets.smallImage != null) {
            json2.addProperty("small_image", this.assets.smallImage);
         }

         if (this.assets.smallText != null) {
            json2.addProperty("small_text", this.assets.smallText);
         }

         json.add("assets", json2);
      }

      if (this.timestamps != null) {
         JsonObject json3 = new JsonObject();
         if (this.timestamps.start != null) {
            json3.addProperty("start", this.timestamps.start);
         }

         if (this.timestamps.end != null) {
            json3.addProperty("end", this.timestamps.end);
         }

         json.add("timestamps", json3);
      }

      return json;
   }

   public static class Inner {
      @SerializedName("start")
      public Long start;
      @SerializedName("end")
      public Long end;
   }

   public static class Inner2 {
      @SerializedName("largeImage")
      public String largeImage;
      @SerializedName("largeText")
      public String largeText;
      @SerializedName("smallImage")
      public String smallImage;
      @SerializedName("smallText")
      public String smallText;
   }
}
