package dev.southside.auth;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import dev.southside.utils.IrcTextStyle;
import dev.southside.utils.IrcTextContent;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class IrcMessage {
   @SerializedName("content")
   public final IrcTextContent content;
   @SerializedName("children")
   public final List<IrcMessage> children;
   @SerializedName("style")
   public IrcTextStyle style;

   public static IrcMessage getIrcMessage(JsonObject json) {
      IrcTextContent ircTextContent = IrcTextContent.getIrcTextContent(json.get("1").getAsJsonObject());
      ArrayList list = new ArrayList();

      for (JsonElement element : json.get("2").getAsJsonArray()) {
         list.add(getIrcMessage(element.getAsJsonObject()));
      }

      IrcTextStyle ircTextStyle = IrcTextStyle.getIrcTextStyle(json.get("3").getAsJsonObject());
      return new IrcMessage(ircTextContent, list, ircTextStyle);
   }

   public static IrcMessage getIrcMessage(Date date) {
      return getIrcMessage(date.toString());
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj != null && this.getClass() == obj.getClass()) {
         IrcMessage ircMessage = (IrcMessage)obj;
         return Objects.equals(this.content, ircMessage.content) && Objects.equals(this.children, ircMessage.children) && Objects.equals(this.style, ircMessage.style);
      } else {
         return false;
      }
   }

   public IrcMessage getIrcMessage(String text, Object... objArray) {
      String text2 = formatText(text, objArray);
      return text2.isEmpty() ? this : this.getIrcMessage(getIrcMessage(text2));
   }

   public static IrcMessage getIrcMessage2() {
      return getIrcMessage(IrcTextContent.INSTANCE);
   }

   public Text getTextEx() {
      MutableText text = MutableText.of(this.content.getTextContent()).setStyle(this.style.getStyle());

      for (IrcMessage ircMessage : this.children) {
         text.append(ircMessage.getTextEx());
      }

      return text;
   }

   public static IrcMessage getIrcMessage(UUID uuid) {
      return getIrcMessage(uuid.toString());
   }

   public static IrcMessage getIrcMessage(String text) {
      return getIrcMessage(IrcTextContent.getIrcTextContent(text));
   }

   public IrcTextStyle getStyle() {
      return this.style;
   }

   public IrcMessage(IrcTextContent ircTextContent, List<IrcMessage> list, IrcTextStyle ircTextStyle) {
      this.content = ircTextContent;
      this.children = list;
      this.style = ircTextStyle;
   }

   public static IrcMessage getIrcMessage(URI uri) {
      return getIrcMessage(uri.toString());
   }

   public String getDisplayText() {
      StringBuilder sb = new StringBuilder(this.content.getText());

      for (IrcMessage ircMessage : this.children) {
         sb.append(ircMessage.getDisplayText());
      }

      return sb.toString();
   }

   public IrcTextContent getContent() {
      return this.content;
   }

   public IrcMessage getIrcMessage(Formatting formatting) {
      this.getIrcMessage2(this.getStyle().getIrcTextStyleEx(formatting));
      return this;
   }

   public IrcMessage getIrcMessage(int n) {
      this.getIrcMessage2(this.getStyle().getIrcTextStyle(n));
      return this;
   }

   public static IrcMessage getIrcMessage(IrcTextContent ircTextContent) {
      return new IrcMessage(ircTextContent, new ArrayList<>(), IrcTextStyle.getIrcTextStyle());
   }

   public IrcMessage getIrcMessage2(IrcTextStyle ircTextStyle) {
      this.style = ircTextStyle;
      return this;
   }

   public IrcMessage getIrcMessage3() {
      return new IrcMessage(this.getContent(), new ArrayList<>(this.getChildren()), this.getStyle());
   }

   public static String formatText(String text, Object... objArray) {
      if (objArray.length == 0) {
         return text;
      }

      Matcher matcher = Pattern.compile("\\{}").matcher(text);
      StringBuilder sb = new StringBuilder();

      for (Object obj : objArray) {
         if (!matcher.find()) {
            break;
         }

         matcher.appendReplacement(sb, Matcher.quoteReplacement(String.valueOf(obj)));
      }

      matcher.appendTail(sb);
      return sb.toString();
   }

   public IrcMessage getIrcMessage(Formatting... formattingArray) {
      this.getIrcMessage2(this.getStyle().getIrcTextStyle(formattingArray));
      return this;
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.content, this.style, this.children);
   }

   public IrcMessage getIrcMessage(IrcMessage other) {
      this.children.add(other);
      return this;
   }

   public IrcMessage getIrcMessage(IrcTextStyle ircTextStyle) {
      this.getIrcMessage2(ircTextStyle.getIrcTextStyle(this.getStyle()));
      return this;
   }

   public List<IrcMessage> getChildren() {
      return this.children;
   }

   public IrcMessage getIrcMessage() {
      return getIrcMessage(this.getContent());
   }

   public static IrcMessage getIrcMessage2(String text) {
      return text != null ? getIrcMessage(text) : getIrcMessage2();
   }
}
