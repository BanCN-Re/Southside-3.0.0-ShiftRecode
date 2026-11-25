package dev.southside.utils;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import java.util.Objects;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;

public class IrcTextStyle {
   public static final IrcTextStyle INSTANCE = new IrcTextStyle(null, false, false, false, false, false, null, null);
   @SerializedName("color")
   public IrcTextColor color;
   @SerializedName("bold")
   public Boolean bold;
   @SerializedName("italic")
   public Boolean italic;
   @SerializedName("underlined")
   public Boolean underlined;
   @SerializedName("strikethrough")
   public Boolean strikethrough;
   @SerializedName("obfuscated")
   public Boolean obfuscated;
   @SerializedName("clickEvent")
   public IrcClickEvent clickEvent;
   @SerializedName("hoverEvent")
   public IrcHoverEvent hoverEvent;

   public static IrcTextStyle getIrcTextStyle() {
      return new IrcTextStyle(null, false, false, false, false, false, null, null);
   }

   public boolean isValid() {
      return Boolean.TRUE.equals(this.bold);
   }

   public boolean isValidEx() {
      return this == INSTANCE;
   }

   public IrcTextStyle getIrcTextStyle(int n) {
      return this.getIrcTextStyle(IrcTextColor.getIrcTextColor(n));
   }

   public boolean isValidAlt() {
      return Boolean.TRUE.equals(this.obfuscated);
   }

   public IrcTextStyle getIrcTextStyle(boolean flag) {
      this.bold = flag;
      return this;
   }

   public IrcTextStyle getIrcTextStyle(Formatting formatting) {
      IrcTextColor ircTextColor = formatting == null ? null : IrcTextColor.getIrcTextColor(formatting);
      return this.getIrcTextStyle(ircTextColor);
   }

   public Style getStyle() {
      Style empty = Style.EMPTY;
      if (this.color != null) {
         empty = empty.withColor(this.color.getTextColor());
      }

      if (this.bold != null) {
         empty = empty.withBold(this.bold);
      }

      if (this.italic != null) {
         empty = empty.withItalic(this.italic);
      }

      if (this.underlined != null) {
         empty = empty.withUnderline(this.underlined);
      }

      if (this.strikethrough != null) {
         empty = empty.withStrikethrough(this.strikethrough);
      }

      if (this.obfuscated != null) {
         empty = empty.withObfuscated(this.obfuscated);
      }

      if (this.clickEvent != null) {
         empty = empty.withClickEvent(this.clickEvent.getClickEvent());
      }

      if (this.hoverEvent != null) {
         empty = empty.withHoverEvent(this.hoverEvent.getHoverEvent());
      }

      return empty;
   }

   public IrcTextStyle getIrcTextStyleEx(boolean flag) {
      this.strikethrough = flag;
      return this;
   }

   public IrcTextStyle(IrcTextStyle other) {
      this(other.color, other.bold, other.italic, other.underlined, other.strikethrough, other.obfuscated, other.getClickEvent(), other.getHoverEvent());
   }

   public IrcTextStyle getIrcTextStyleEx(Formatting formatting) {
      return this.getIrcTextStyle(new Formatting[]{formatting});
   }

   private IrcTextStyle(IrcTextColor ircTextColor, Boolean val, Boolean val2, Boolean val3, Boolean val4, Boolean val5, IrcClickEvent ircClickEvent, IrcHoverEvent ircHoverEvent) {
      this.color = ircTextColor;
      this.bold = val;
      this.italic = val2;
      this.underlined = val3;
      this.strikethrough = val4;
      this.obfuscated = val5;
      this.clickEvent = ircClickEvent;
      this.hoverEvent = ircHoverEvent;
   }

   public IrcTextStyle getIrcTextStyle(IrcClickEvent ircClickEvent) {
      this.clickEvent = ircClickEvent;
      return this;
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.color, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent);
   }

   public IrcTextStyle getIrcTextStyle(IrcTextStyle other) {
      this.color = other.color;
      this.bold = other.bold;
      this.italic = other.italic;
      this.underlined = other.underlined;
      this.strikethrough = other.strikethrough;
      this.obfuscated = other.obfuscated;
      this.clickEvent = other.clickEvent;
      this.hoverEvent = other.hoverEvent;
      return this;
   }

   public IrcTextStyle getIrcTextStyleAlt(boolean flag) {
      this.italic = flag;
      return this;
   }

   public IrcTextStyle getIrcTextStyle(IrcHoverEvent ircHoverEvent) {
      this.hoverEvent = ircHoverEvent;
      return this;
   }

   public IrcTextColor getColor() {
      return this.color;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof IrcTextStyle ircTextStyle)
            ? false
            : Objects.equals(this.bold, ircTextStyle.bold)
               && Objects.equals(this.getColor(), ircTextStyle.getColor())
               && Objects.equals(this.italic, ircTextStyle.italic)
               && Objects.equals(this.obfuscated, ircTextStyle.obfuscated)
               && Objects.equals(this.strikethrough, ircTextStyle.strikethrough)
               && Objects.equals(this.underlined, ircTextStyle.underlined)
               && Objects.equals(this.clickEvent, ircTextStyle.clickEvent)
               && Objects.equals(this.hoverEvent, ircTextStyle.hoverEvent);
      }
   }

   public IrcTextStyle getIrcTextStyleImpl(boolean flag) {
      this.underlined = flag;
      return this;
   }

   public boolean isValidImpl() {
      return Boolean.TRUE.equals(this.underlined);
   }

   public IrcTextStyle getIrcTextStyleDirect(boolean flag) {
      this.obfuscated = flag;
      return this;
   }

   public IrcHoverEvent getHoverEvent() {
      return this.hoverEvent;
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder("{");
      IrcTextStyle.Inner inner = new IrcTextStyle.Inner(sb);
      inner.applyText("color", this.color);
      inner.applyText("bold", this.bold);
      inner.applyText("italic", this.italic);
      inner.applyText("underlined", this.underlined);
      inner.applyText("strikethrough", this.strikethrough);
      inner.applyText("obfuscated", this.obfuscated);
      inner.applyText("clickEvent", this.clickEvent);
      inner.applyText("hoverEvent", this.hoverEvent);
      sb.append("}");
      return sb.toString();
   }

   public boolean isValidDirect() {
      return Boolean.TRUE.equals(this.italic);
   }

   public IrcClickEvent getClickEvent() {
      return this.clickEvent;
   }

   public IrcTextStyle getIrcTextStyle(Formatting... formattingArray) {
      IrcTextColor color2 = this.color;
      Boolean bold2 = this.bold;
      Boolean italic2 = this.italic;
      Boolean strikethrough2 = this.strikethrough;
      Boolean underlined2 = this.underlined;
      Boolean obfuscated2 = this.obfuscated;

      for (Formatting formatting : formattingArray) {
         switch (formatting) {
            case OBFUSCATED:
               obfuscated2 = true;
               break;
            case BOLD:
               bold2 = true;
               break;
            case STRIKETHROUGH:
               strikethrough2 = true;
               break;
            case UNDERLINE:
               underlined2 = true;
               break;
            case ITALIC:
               italic2 = true;
               break;
            case RESET:
               color2 = null;
               bold2 = false;
               italic2 = false;
               strikethrough2 = false;
               underlined2 = false;
               obfuscated2 = false;
               break;
            default:
               color2 = IrcTextColor.getIrcTextColor(formatting);
         }
      }

      this.color = color2;
      this.bold = bold2;
      this.italic = italic2;
      this.strikethrough = strikethrough2;
      this.underlined = underlined2;
      this.obfuscated = obfuscated2;
      return this;
   }

   public boolean isValidSafe() {
      return Boolean.TRUE.equals(this.strikethrough);
   }

   public static IrcTextStyle getIrcTextStyle(JsonObject json) {
      IrcTextColor ircTextColor = json.has("1") ? IrcTextColor.getIrcTextColor(json.get("1").getAsJsonObject()) : null;
      Boolean boolean_ = getBoolean_(json, "2");
      Boolean boolean_2 = getBoolean_(json, "3");
      Boolean boolean_3 = getBoolean_(json, "4");
      Boolean boolean_4 = getBoolean_(json, "5");
      Boolean boolean_5 = getBoolean_(json, "6");
      IrcClickEvent ircClickEvent = json.has("7") ? IrcClickEvent.getIrcClickEvent(json.get("7").getAsJsonObject()) : null;
      IrcHoverEvent ircHoverEvent = json.has("8") ? IrcHoverEvent.getIrcHoverEvent(json.get("8").getAsJsonObject()) : null;
      return new IrcTextStyle(ircTextColor, boolean_, boolean_2, boolean_3, boolean_4, boolean_5, ircClickEvent, ircHoverEvent);
   }

   private static Boolean getBoolean_(JsonObject json, String text) {
      return json.has(text) ? json.get(text).getAsInt() == 1 : null;
   }

   public IrcTextStyle getIrcTextStyle(IrcTextColor ircTextColor) {
      this.color = ircTextColor;
      return this;
   }

   private static final class Inner {
      @SerializedName("hasEntries")
      private boolean hasEntries;
      @SerializedName("builder")
      private final StringBuilder builder;

      public Inner(StringBuilder sb) {
         this.builder = sb;
      }

      public void performAction() {
         if (this.hasEntries) {
            this.builder.append(',');
         }

         this.hasEntries = true;
      }

      public void applyText(String text, Boolean val) {
         if (val != null) {
            this.performAction();
            if (!val) {
               this.builder.append('!');
            }

            this.builder.append(text);
         }
      }

      public void applyText(String text, Object obj) {
         if (obj != null) {
            this.performAction();
            this.builder.append(text);
            this.builder.append('=');
            this.builder.append(obj);
         }
      }
   }

}
