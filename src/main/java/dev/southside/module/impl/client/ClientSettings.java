package dev.southside.module.impl.client;

import com.google.gson.annotations.SerializedName;
import dev.southside.auth.IrcState;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.values.BooleanValue;
import dev.southside.utils.NumberValue;
import dev.southside.utils.ModeValue;
import dev.southside.utils.StringValue;

public class ClientSettings extends Module {
   @SerializedName("customMainMenu")
   public final BooleanValue customMainMenu = new BooleanValue(
      "Custom Main Menu", "Animated main menu only; all other screens keep their original interface", true
   );
   public static final ModeValue soundStyle;
   public static final NumberValue soundVolume;
   public static final ModeValue progressBarStyle;
   public static final NumberValue progressBarYOffset;
   public static final BooleanValue enableIrcChat;
   public static final BooleanValue ircFastChat;
   public static final StringValue ircChatPrefix;
   public static final BooleanValue ircAllowPvp;

   public ClientSettings() {
      super(Category.Client, "ClientSettings", "Setting for Client");
   }

   @Override
   public void postInit() {
      super.postInit();
      this.hideIf(ircChatPrefix.getLabel(), () -> !(Boolean)enableIrcChat.getValue());
      this.hideIf(ircFastChat.getLabel(), () -> !(Boolean)enableIrcChat.getValue());
      this.hideIf(soundVolume.getLabel(), () -> soundStyle.matches("None"));
      this.addListener(ircAllowPvp.getLabel(), (Runnable) IrcState::performAction);
   }

   @Override
   public void onEnable() {
      this.setEnabled(false);
   }

   // $VF: Irreducible bytecode was duplicated to produce valid code
   static {
      soundStyle = new ModeValue("Sound Style", "Default", "None", "Default", "Piano", "Sigma");
      soundVolume = new NumberValue("Volume", 1, 0.1, 1, 0.01);
      progressBarStyle = new ModeValue("ProgressBar Style", "Simple", "Simple", "Classic");
      progressBarYOffset = new NumberValue("ProgressBar Y Offset", 20, 0, 200, 1);
      enableIrcChat = new BooleanValue("Enable IRC Chat", "启用IRC聊天", true);
      ircFastChat = new BooleanValue("IRC FastChat", "irc快捷聊天", true);
      ircChatPrefix = new StringValue("IRCChat Prefix", "irc快捷聊天前缀", false, false, "prefix", 10, "!");
      ircAllowPvp = new BooleanValue("IRC Allow PVP", "开启irc pvp", false);
   }
}
