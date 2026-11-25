package dev.southside.module.impl.misc;

import com.google.gson.annotations.SerializedName;
import dev.southside.Southside;
import dev.southside.discordipc.DiscordIPC;
import dev.southside.discordipc.RichPresence;
import dev.southside.event.EventTarget;
import dev.southside.event.Stage;
import dev.southside.event.events.ScreenEvent;
import dev.southside.event.events.StateEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.utils.CustomPayloadHelper;
import dev.southside.utils.NumberValue;
import dev.southside.utils.ModeValue;
import dev.southside.utils.StringValue;
import dev.southside.utils.math.RandomUtils;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.client.gui.screen.CreditsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.AddServerScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.DirectConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.AccessibilityOptionsScreen;
import net.minecraft.client.gui.screen.option.ChatOptionsScreen;
import net.minecraft.client.gui.screen.option.ControlsOptionsScreen;
import net.minecraft.client.gui.screen.option.LanguageOptionsScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.option.SkinOptionsScreen;
import net.minecraft.client.gui.screen.option.SoundOptionsScreen;
import net.minecraft.client.gui.screen.option.VideoOptionsScreen;
import net.minecraft.client.gui.screen.pack.PackScreen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.EditGameRulesScreen;
import net.minecraft.client.gui.screen.world.EditWorldScreen;
import net.minecraft.client.gui.screen.world.LevelLoadingScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.realms.gui.screen.RealmsScreen;

public class DiscordPresence extends Module {
   private static final long DEFAULT_LONG_VALUE = 1407245483277287544L;
   private static final int DEFAULT_INT = 200;
   public static final RichPresence rpc = new RichPresence();
   private static final Map<String, String> stringMap = new LinkedHashMap<>();
   @SerializedName("line1Text1")
   public final StringValue line1Text1 = new StringValue("Line 1 Text 1", "{player}", false, false, "First line message", 100, "{player}");
   @SerializedName("line1Text2")
   public final StringValue line1Text2 = new StringValue("Line 1 Text 2", "{server}", false, false, "First line message", 100, "{server}");
   @SerializedName("line1UpdateDelay")
   public final NumberValue line1UpdateDelay = new NumberValue("Line 1 Update Delay", 200, 50, 1000);
   @SerializedName("line1SelectMode")
   public final ModeValue line1SelectMode = new ModeValue("Line 1 Select Mode", "Sequential", "Sequential", "Random");
   @SerializedName("line2Text1")
   public final StringValue line2Text1 = new StringValue(
      "Line 2 Text 1", "Southside - {client.version}", false, false, "Second line message", 100, "Southside - {client.version}"
   );
   @SerializedName("line2Text2")
   public final StringValue line2Text2 = new StringValue(
      "Line 2 Text 2", "{player.mode} Mode", false, false, "Second line message", 100, "{player.mode} Mode"
   );
   @SerializedName("line2Text3")
   public final StringValue line2Text3 = new StringValue(
      "Line 2 Text 3", "Playing on {server.difficulty} difficulty.", false, false, "Second line message", 100, "Playing on {server.difficulty} difficulty."
   );
   @SerializedName("line2Text4")
   public final StringValue line2Text4 = new StringValue(
      "Line 2 Text 4", "{server.player_count} Players online", false, false, "Second line message", 100, "{server.player_count} Players online"
   );
   @SerializedName("line2UpdateDelay")
   public final NumberValue line2UpdateDelay = new NumberValue("Line 2 Update Delay", 200, 50, 1000);
   @SerializedName("line2SelectMode")
   public final ModeValue line2SelectMode = new ModeValue("Line 2 Select Mode", "Sequential", "Sequential", "Random");
   @SerializedName("currentBadge")
   private DiscordPresence.Mode currentBadge;
   @SerializedName("badgeUpdateTicks")
   private int badgeUpdateTicks;
   @SerializedName("forceRefresh")
   private boolean forceRefresh;
   @SerializedName("menuPresenceUpdated")
   private boolean menuPresenceUpdated;
   @SerializedName("line1Texts")
   private final List<String> line1Texts = new ArrayList<>();
   @SerializedName("line1UpdateTicks")
   private int line1UpdateTicks;
   @SerializedName("line1SequentialIndex")
   private int line1SequentialIndex;
   @SerializedName("line2Texts")
   private final List<String> line2Texts = new ArrayList<>();
   @SerializedName("line2UpdateTicks")
   private int line2UpdateTicks;
   @SerializedName("line2SequentialIndex")
   private int line2SequentialIndex;

   public DiscordPresence() {
      super(Category.Misc, "Discord Presence", "南方Discord大王。");
   }

   @Override
   public void onEnable() {
      DiscordIPC.isValid(1407245483277287544L, null);
      rpc.performActionEx(System.currentTimeMillis() / 1000L);
      rpc.applyTextEx("southside", "SouthSide Reborn 1.21.4");
      this.currentBadge = DiscordPresence.Mode.LecitronArts;
      this.performActionAlt();
      this.performActionImpl();
      this.badgeUpdateTicks = 0;
      this.line1UpdateTicks = 0;
      this.line2UpdateTicks = 0;
      this.menuPresenceUpdated = false;
      this.line1SequentialIndex = 0;
      this.line2SequentialIndex = 0;
   }

   @Override
   public void onDisable() {
      DiscordIPC.performActionEx();
   }

   @EventTarget
   public void onState(StateEvent stateEvent) {
      if (stateEvent.state == Stage.POST) {
         boolean isValidEx = this.isValidEx();
         if (CustomPayloadHelper.isValid()) {
            isValidEx |= this.isValidAlt();
            isValidEx |= this.isValidImpl();
         } else if (!this.menuPresenceUpdated) {
            rpc.setDetails(Module.mc.getSession().getUsername());
            rpc.setState(unregisterCustomState(Module.mc.currentScreen));
            isValidEx = true;
         }

         if (isValidEx) {
            DiscordIPC.performAction(rpc);
         }

         this.forceRefresh = false;
         this.menuPresenceUpdated = !CustomPayloadHelper.isValid();
      }
   }

   private boolean isValidEx() {
      if (this.badgeUpdateTicks < 200 && !this.forceRefresh) {
         this.badgeUpdateTicks++;
         return false;
      } else {
         this.currentBadge.performAction();
         this.badgeUpdateTicks = 0;
         return true;
      }
   }

   private boolean isValidAlt() {
      if (this.line1UpdateTicks < ((Number)this.line1UpdateDelay.getValue()).intValue() && !this.forceRefresh) {
         this.line1UpdateTicks++;
         return false;
      }

      if (!this.line1Texts.isEmpty()) {
         int n;
         if (this.line1SelectMode.matches("Sequential")) {
            if (this.line1SequentialIndex >= this.line1Texts.size()) {
               this.line1SequentialIndex = 0;
            }

            n = this.line1SequentialIndex++;
         } else {
            n = RandomUtils.RAND.nextInt(this.line1Texts.size());
         }

         rpc.setDetails(this.formatText(this.line1Texts.get(n)));
      }

      this.line1UpdateTicks = 0;
      return true;
   }

   private boolean isValidImpl() {
      if (this.line2UpdateTicks < ((Number)this.line2UpdateDelay.getValue()).intValue() && !this.forceRefresh) {
         this.line2UpdateTicks++;
         return false;
      }

      if (!this.line2Texts.isEmpty()) {
         int n;
         if (this.line2SelectMode.matches("Sequential")) {
            if (this.line2SequentialIndex >= this.line2Texts.size()) {
               this.line2SequentialIndex = 0;
            }

            n = this.line2SequentialIndex++;
         } else {
            n = RandomUtils.RAND.nextInt(this.line2Texts.size());
         }

         rpc.setState(this.formatText(this.line2Texts.get(n)));
      }

      this.line2UpdateTicks = 0;
      return true;
   }

   public void performActionAlt() {
      this.performAction(this.line1Texts, this.line1Text1, this.line1Text2);
   }

   public void performActionImpl() {
      this.performAction(this.line2Texts, this.line2Text1, this.line2Text2, this.line2Text3, this.line2Text4);
   }

   private void performAction(List<String> list, StringValue... stringValueArray) {
      list.clear();

      for (StringValue stringValue : stringValueArray) {
         String text = (String)stringValue.getValue();
         list.add(text != null && !text.isEmpty() ? text : (String)stringValue.getDefaultValue());
      }

      this.forceRefresh = true;
   }

   public String formatText(String text) {
      if (text != null && !text.isEmpty()) {
         String text2 = text;
         if (Southside.INSTANCE != null) {
            text2 = text2.replace("{client.version}", Southside.labelValue);
         }

         if (Module.mc.getSession() != null) {
            text2 = text2.replace("{player}", Module.mc.getSession().getUsername());
         }

         if (Module.mc.player != null) {
            String text3 = Module.mc.player.isCreative() ? "Creative" : (Module.mc.player.isSpectator() ? "Spectator" : "Survival");
            text2 = text2.replace("{player.mode}", text3);
         }

         if (Module.mc.getCurrentServerEntry() != null) {
            text2 = text2.replace("{server}", Module.mc.getCurrentServerEntry().name);
         } else if (Module.mc.isInSingleplayer()) {
            text2 = text2.replace("{server}", "Singleplayer");
         } else {
            text2 = text2.replace("{server}", "Main Menu");
         }

         if (Module.mc.getNetworkHandler() != null && Module.mc.getNetworkHandler().getPlayerList() != null) {
            text2 = text2.replace("{server.player_count}", String.valueOf(Module.mc.getNetworkHandler().getPlayerList().size()));
         } else {
            text2 = text2.replace("{server.player_count}", "0");
         }

         if (Module.mc.world != null) {
            text2 = text2.replace("{server.difficulty}", Module.mc.world.getDifficulty().getName());
         } else {
            text2 = text2.replace("{server.difficulty}", "Unknown");
         }

         return text2.isEmpty() ? "Southside User" : text2;
      } else {
         return "Southside";
      }
   }

   @EventTarget
   public void onScreen(ScreenEvent screenEvent) {
      if (!CustomPayloadHelper.isValid()) {
         this.menuPresenceUpdated = false;
      }
   }

   private static String unregisterCustomState(Screen screen) {
      if (screen instanceof TitleScreen) {
         return "Looking at title screen";
      }

      if (screen instanceof SelectWorldScreen) {
         return "Selecting world";
      }

      if (screen instanceof CreateWorldScreen || screen instanceof EditGameRulesScreen) {
         return "Creating world";
      }

      if (screen instanceof EditWorldScreen) {
         return "Editing world";
      }

      if (screen instanceof LevelLoadingScreen) {
         return "Loading world";
      }

      if (screen instanceof MultiplayerScreen) {
         return "Selecting server";
      }

      if (screen instanceof AddServerScreen) {
         return "Adding server";
      }

      if (screen instanceof ConnectScreen || screen instanceof DirectConnectScreen) {
         return "Connecting to server";
      }

      if (screen instanceof OptionsScreen
         || screen instanceof SkinOptionsScreen
         || screen instanceof SoundOptionsScreen
         || screen instanceof VideoOptionsScreen
         || screen instanceof ControlsOptionsScreen
         || screen instanceof LanguageOptionsScreen
         || screen instanceof ChatOptionsScreen
         || screen instanceof PackScreen
         || screen instanceof AccessibilityOptionsScreen) {
         return "Changing options";
      }

      if (screen instanceof CreditsScreen) {
         return "Reading credits";
      }

      if (screen instanceof RealmsScreen) {
         return "Browsing Realms";
      }

      if (screen != null) {
         String name = screen.getClass().getName();

         for (Entry entry : stringMap.entrySet()) {
            if (name.startsWith((String)entry.getKey())) {
               return (String)entry.getValue();
            }
         }
      }

      return "In main menu";
   }

   public static void registerCustomState(String text, String text2) {
      stringMap.put(text, text2);
   }

   public static void unregisterCustomState(String text) {
      stringMap.remove(text);
   }

   static {
      short s = -7053;
      short s2 = 13965;
      registerCustomState("ru.vidtu.ias.screen.AccountScreen", "Switching account");
      registerCustomState("com.terraformersmc.modmenu.gui", "Browsing mods");
      registerCustomState("me.jellysquid.mods.sodium.client", "Changing options");
   }

   public enum Mode {
      yige("yige", "yige"),
      LecitronArts("lecitronarts", "LecitronArts");

      @SerializedName("smallImageKey")
      private final String smallImageKey;
      @SerializedName("smallImageText")
      private final String smallImageText;

      Mode(String text, String text2) {
         this.smallImageKey = text;
         this.smallImageText = text2;
      }

      public void performAction() {
         DiscordPresence.rpc.applyText(this.smallImageKey, this.smallImageText);
      }

      // $VF: synthetic method
      private static DiscordPresence.Mode[] createValues() {
         return new DiscordPresence.Mode[]{yige, LecitronArts};
      }


   }
}
