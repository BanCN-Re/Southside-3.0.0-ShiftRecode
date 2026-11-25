package dev.southside.utils;

import com.google.gson.annotations.SerializedName;
import java.util.LinkedHashMap;

public class OptionPage {
   @SerializedName("name")
   public final String name;
   @SerializedName("owner")
   public final ModData owner;
   @SerializedName("categories")
   public final LinkedHashMap<String, OptionCategory> categories = new LinkedHashMap<>();

   public OptionPage(String text, ModData modData) {
      this.name = text;
      this.owner = modData;
   }
}
