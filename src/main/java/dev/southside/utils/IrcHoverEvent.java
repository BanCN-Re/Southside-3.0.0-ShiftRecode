package dev.southside.utils;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import dev.southside.auth.IrcMessage;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.text.HoverEvent.Action;

public class IrcHoverEvent {
   @SerializedName("action")
   public IrcHoverEvent.Mode action;
   @SerializedName("content")
   public IrcMessage content;

   public IrcHoverEvent(IrcHoverEvent.Mode mode, IrcMessage ircMessage) {
      this.action = mode;
      this.content = ircMessage;
   }

   public HoverEvent getHoverEvent() {
      return new HoverEvent(this.action.getHoverEventAction(), this.content.getTextEx());
   }

   public static IrcHoverEvent getIrcHoverEvent(JsonObject json) {
      IrcHoverEvent.Mode mode = IrcHoverEvent.Mode.getMode(json.get("1").getAsInt());
      IrcMessage ircMessage = IrcMessage.getIrcMessage(json.get("2").getAsJsonObject());
      return new IrcHoverEvent(mode, ircMessage);
   }

   public enum Mode {
      SHOW_TEXT(true, Action.SHOW_TEXT);

      @SerializedName("name")
      public final String name = "SHOW_TEXT";
      @SerializedName("id")
      public final int id = 0;
      @SerializedName("enabled")
      public final boolean enabled;
      @SerializedName("hoverEventAction")
      public final Action<Text> hoverEventAction;

      public static IrcHoverEvent.Mode getMode(int n) {
         for (IrcHoverEvent.Mode mode : values()) {
            if (mode.id == n) {
               return mode;
            }
         }

         return null;
      }

      public String getName() {
         return this.name;
      }

      public static IrcHoverEvent.Mode getMode(String text) {
         return Enum.valueOf(IrcHoverEvent.Mode.class, text);
      }

      public boolean isEnabled() {
         return this.enabled;
      }

      Mode(boolean flag, Action<Text> action) {
         this.enabled = flag;
         this.hoverEventAction = action;
      }

      public Action<Text> getHoverEventAction() {
         return this.hoverEventAction;
      }

      @Override
      public String toString() {
         return "<action " + this.name + ">";
      }

      // $VF: synthetic method
      private static IrcHoverEvent.Mode[] createValues() {
         return new IrcHoverEvent.Mode[]{SHOW_TEXT};
      }
   }
}
