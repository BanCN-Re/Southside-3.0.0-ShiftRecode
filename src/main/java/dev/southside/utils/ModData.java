package dev.southside.utils;

import cc.polyfrost.oneconfig.config.Config;
import com.google.gson.annotations.SerializedName;
import dev.southside.module.Category;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ModData implements Comparable<ModData> {
   @SerializedName("name")
   @NotNull
   public final String name;
   @SerializedName("modType")
   public final Category modType;
   @SerializedName("modIcon")
   @Nullable
   public final String modIcon;
   @SerializedName("iconWidth")
   public final int iconWidth;
   @SerializedName("iconHeight")
   public final int iconHeight;
   @SerializedName("migrator")
   @Nullable
   public final IConfigMigrator migrator;
   @SerializedName("defaultPage")
   @NotNull
   public final OptionPage defaultPage;
   @SerializedName("config")
   public Config config;

   public ModData(@NotNull String text, Category category, @Nullable String text2, int n, int n2, @Nullable IConfigMigrator iconfigmigrator) {
      this.name = text;
      this.modType = category;
      this.modIcon = text2;
      this.iconWidth = n;
      this.iconHeight = n2;
      this.migrator = iconfigmigrator;
      this.defaultPage = new OptionPage(text, this);
   }

   public ModData(String text, Category category, @Nullable String text2, IConfigMigrator iconfigmigrator) {
      this(text, category, text2, -1, -1, iconfigmigrator);
   }

   public ModData(String text, Category category, @Nullable String text2, int n, int n2) {
      this(text, category, text2, n, n2, null);
   }

   public ModData(String text, Category category, @Nullable String text2) {
      this(text, category, text2, -1, -1, null);
   }

   public ModData(String text, Category category, @Nullable IConfigMigrator iconfigmigrator) {
      this(text, category, null, -1, -1, iconfigmigrator);
   }

   public ModData(String text, Category category) {
      this(text, category, null, -1, -1, null);
   }

   public int compareTo(@NotNull ModData other) {
      return this.name.compareTo(other.name);
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      } else if (obj == this) {
         return true;
      } else {
         return obj instanceof ModData modData ? this.name.equals(modData.name) : false;
      }
   }
}
