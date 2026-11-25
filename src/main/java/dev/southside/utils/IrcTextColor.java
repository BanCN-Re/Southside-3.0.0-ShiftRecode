package dev.southside.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import java.util.Locale;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

public class IrcTextColor {
   @SerializedName("rgb")
   public final int rgb;
   @SerializedName("name")
   public final String name;
   private static final long DEFAULT_LONG = -2084964609374027777L;

   @Override
   public int hashCode() {
      return Integer.hashCode(this.rgb);
   }

   public static IrcTextColor getIrcTextColor(Formatting formatting) {
      Integer integer = formatting == null ? null : formatting.getColorValue();
      return integer == null ? null : new IrcTextColor(integer, formatting.name());
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj != null && this.getClass() == obj.getClass()) {
         IrcTextColor ircTextColor = (IrcTextColor)obj;
         return this.rgb == ircTextColor.rgb;
      } else {
         return false;
      }
   }

   public final String getDisplayText() {
      return String.format(Locale.ROOT, "#%06X", this.rgb);
   }

   public int getRgb() {
      return this.rgb;
   }

   public static IrcTextColor getIrcTextColor(JsonObject json) {
      JsonElement element = json.get("1");
      int asInt = json.get("2").getAsInt();
      return new IrcTextColor(asInt, element != null && !element.isJsonNull() ? element.getAsString() : null);
   }

   private IrcTextColor(int n, String text) {
      this.rgb = n & (int)DEFAULT_LONG;
      this.name = text;
   }

   public static IrcTextColor getIrcTextColor(int n) {
      return new IrcTextColor(n, null);
   }

   public String getDisplayTextEx() {
      return this.name != null ? this.name : this.getDisplayText();
   }

   @Override
   public String toString() {
      return this.getDisplayTextEx();
   }

   public IrcTextColor(IrcTextColor other) {
      this(other.rgb, other.name);
   }

   public TextColor getTextColor() {
      return TextColor.fromRgb(this.rgb);
   }
}
