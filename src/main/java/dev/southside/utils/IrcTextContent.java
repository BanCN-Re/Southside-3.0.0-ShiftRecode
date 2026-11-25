package dev.southside.utils;

import com.google.gson.JsonObject;
import net.minecraft.text.TextContent;
import net.minecraft.text.PlainTextContent.Literal;

public interface IrcTextContent {
   IrcTextContent INSTANCE = new IrcTextContent.IContract18Impl();

   String getText();

   static IrcTextContent getIrcTextContent(String text) {
      return text.isEmpty() ? INSTANCE : new IrcTextContent.Entry(text);
   }

   default TextContent getTextContent() {
      return new Literal(this.getText());
   }

   static IrcTextContent getIrcTextContent(JsonObject json) {
      return getIrcTextContent(json.get("1").getAsString());
   }

   record Entry(String text) implements IrcTextContent {
      @Override
      public String getText() {
         return this.text;
      }

      @Override
      public String toString() {
         return "literal{" + this.text + "}";
      }

      public String getTextEx() {
         return this.text;
      }
   }

   class IContract18Impl implements IrcTextContent {
      @Override
      public String toString() {
         return "empty";
      }

      @Override
      public String getText() {
         return "";
      }
   }
}
