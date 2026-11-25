package dev.southside.utils;

import com.google.gson.annotations.SerializedName;

public class DiscordUser {
   @SerializedName("id")
   public String id;
   @SerializedName("username")
   public String username;
   @SerializedName("discriminator")
   public String discriminator;
   @SerializedName("avatar")
   public String avatar;
   @SerializedName("bot")
   public boolean bot;
   @SerializedName("flags")
   public String flags;
   @SerializedName("premium_type")
   public int premium_type;
}
