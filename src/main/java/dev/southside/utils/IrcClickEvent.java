package dev.southside.utils;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.ClickEvent.Action;

public class IrcClickEvent {
   @SerializedName("clickAction")
   public final IrcClickEvent.Mode clickAction;
   @SerializedName("value")
   public final String value;
   private static final long DEFAULT_LONG = 8528931298889170975L;

   public IrcClickEvent.Mode getClickAction() {
      return this.clickAction;
   }

   public IrcClickEvent(IrcClickEvent.Mode mode, String text) {
      this.clickAction = mode;
      this.value = text;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj != null && this.getClass() == obj.getClass()) {
         IrcClickEvent ircClickEvent = (IrcClickEvent)obj;
         return this.clickAction == ircClickEvent.clickAction && this.value.equals(ircClickEvent.value);
      } else {
         return false;
      }
   }

   public static IrcClickEvent getIrcClickEvent(JsonObject json) {
      IrcClickEvent.Mode mode = IrcClickEvent.Mode.getMode(json.get("1").getAsInt());
      String asString = json.get("2").getAsString();
      return new IrcClickEvent(mode, asString);
   }

   @Override
   public String toString() {
      return "ClickEvent{action=" + this.clickAction.toString() + ", value='" + this.value + "'}";
   }

   public String getValue() {
      return this.value;
   }

   public ClickEvent getClickEvent() {
      return new ClickEvent(this.clickAction.clickEventAction, this.value);
   }

   @Override
   public int hashCode() {
      return (int)DEFAULT_LONG * this.clickAction.hashCode() + this.value.hashCode();
   }

   public enum Mode {
      OPEN_URL("open_url", true, 0, Action.OPEN_URL),
      OPEN_FILE("open_file", false, 1, Action.OPEN_FILE),
      RUN_COMMAND("run_command", true, 2, Action.RUN_COMMAND),
      SUGGEST_COMMAND("suggest_command", true, 3, Action.SUGGEST_COMMAND),
      CHANGE_PAGE("change_page", true, 4, Action.CHANGE_PAGE),
      COPY_TO_CLIPBOARD("copy_to_clipboard", true, 5, Action.COPY_TO_CLIPBOARD);

      @SerializedName("allowedInVanilla")
      public final boolean allowedInVanilla;
      @SerializedName("id")
      public final String id;
      @SerializedName("numericId")
      public final int numericId;
      @SerializedName("clickEventAction")
      public final Action clickEventAction;
      private static final long DEFAULT_LONG = 6268079987656491014L;

      public boolean isAllowedInVanilla() {
         return this.allowedInVanilla;
      }

      public static IrcClickEvent.Mode getMode(int n) {
         for (IrcClickEvent.Mode mode : values()) {
            if (mode.numericId == n) {
               return mode;
            }
         }

         return null;
      }

      public static IrcClickEvent.Mode getMode(String text) {
         return Enum.valueOf(IrcClickEvent.Mode.class, text);
      }

      Mode(String text, boolean flag, int n, Action action) {
         this.id = text;
         this.allowedInVanilla = flag;
         this.numericId = n;
         this.clickEventAction = action;
      }

      public String getId() {
         return this.id;
      }

      // $VF: synthetic method
      private static IrcClickEvent.Mode[] getModeList() {
         IrcClickEvent.Mode[] modeArray = new IrcClickEvent.Mode[(int)DEFAULT_LONG];
         modeArray[0] = OPEN_URL;
         modeArray[1] = OPEN_FILE;
         modeArray[2] = RUN_COMMAND;
         modeArray[3] = SUGGEST_COMMAND;
         modeArray[4] = CHANGE_PAGE;
         modeArray[5] = COPY_TO_CLIPBOARD;
         return modeArray;
      }


   }
}
