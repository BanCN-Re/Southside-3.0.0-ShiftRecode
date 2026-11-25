package dev.southside.module;

import cc.polyfrost.oneconfig.config.Config;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;
import dev.southside.Southside;
import dev.southside.gui.hud.ArrayListHud;
import dev.southside.module.impl.client.ClientSettings;
import dev.southside.module.impl.client.MCF;
import dev.southside.module.impl.client.Notification;
import dev.southside.module.impl.client.Renders;
import dev.southside.module.impl.client.TabGUI;
import dev.southside.module.impl.client.Targets;
import dev.southside.module.impl.client.Theme;
import dev.southside.module.impl.client.Watermark;
import dev.southside.module.impl.combat.AntiFireball;
import dev.southside.module.impl.combat.AutoClicker;
import dev.southside.module.impl.combat.AutoCrystal;
import dev.southside.module.impl.combat.AutoKnockBack;
import dev.southside.module.impl.combat.AutoRod;
import dev.southside.module.impl.combat.BackTrack;
import dev.southside.module.impl.combat.BedAura;
import dev.southside.module.impl.combat.BowAimAssist;
import dev.southside.module.impl.combat.BowSpammer;
import dev.southside.module.impl.combat.Criticals;
import dev.southside.module.impl.combat.HitBox;
import dev.southside.module.impl.combat.KBController;
import dev.southside.module.impl.combat.KillAura;
import dev.southside.module.impl.combat.SuperKnockBack;
import dev.southside.module.impl.combat.TimerRange;
import dev.southside.module.impl.combat.TriggerBot;
import dev.southside.module.impl.combat.Velocity;
import dev.southside.module.impl.misc.AntiBot;
import dev.southside.module.impl.misc.AntiExploit;
import dev.southside.module.impl.misc.AntiNick;
import dev.southside.module.impl.misc.AntiStaff;
import dev.southside.module.impl.misc.AutoPixelParty;
import dev.southside.module.impl.misc.AutoPlay;
import dev.southside.module.impl.misc.BlockHunter;
import dev.southside.module.impl.misc.ChatBypass;
import dev.southside.module.impl.misc.ClientSpoof;
import dev.southside.module.impl.misc.Disabler;
import dev.southside.module.impl.misc.DiscordPresence;
import dev.southside.module.impl.misc.FakeLag;
import dev.southside.module.impl.misc.FuckOffHand;
import dev.southside.module.impl.misc.GodItemDetector;
import dev.southside.module.impl.misc.HackerDetector;
import dev.southside.module.impl.misc.KillSay;
import dev.southside.module.impl.misc.MatchingBypass;
import dev.southside.module.impl.misc.MurderFinder;
import dev.southside.module.impl.misc.Teams;
import dev.southside.module.impl.movement.FastWeb;
import dev.southside.module.impl.movement.Flight;
import dev.southside.module.impl.movement.GrimFly;
import dev.southside.module.impl.movement.InventoryMove;
import dev.southside.module.impl.movement.KeepSprint;
import dev.southside.module.impl.movement.LongJump;
import dev.southside.module.impl.movement.NoJumpDelay;
import dev.southside.module.impl.movement.NoPush;
import dev.southside.module.impl.movement.NoSlow;
import dev.southside.module.impl.movement.Speed;
import dev.southside.module.impl.movement.Spider;
import dev.southside.module.impl.movement.Sprint;
import dev.southside.module.impl.movement.StrafeFix;
import dev.southside.module.impl.movement.TP2Bed;
import dev.southside.module.impl.movement.Timer;
import dev.southside.module.impl.player.AntiVoid;
import dev.southside.module.impl.player.AntiWeb;
import dev.southside.module.impl.player.AutoBucket;
import dev.southside.module.impl.player.AutoOffhand;
import dev.southside.module.impl.player.AutoPot;
import dev.southside.module.impl.player.AutoSoup;
import dev.southside.module.impl.player.AutoTool;
import dev.southside.module.impl.player.AutoTotem;
import dev.southside.module.impl.player.AutoTrap;
import dev.southside.module.impl.player.Blink;
import dev.southside.module.impl.player.BlockIn;
import dev.southside.module.impl.player.Eagle;
import dev.southside.module.impl.player.GhostHand;
import dev.southside.module.impl.player.HealthBypass;
import dev.southside.module.impl.player.InvManager;
import dev.southside.module.impl.player.KeyPearl;
import dev.southside.module.impl.player.NoFall;
import dev.southside.module.impl.player.NoteBot;
import dev.southside.module.impl.player.Stealer;
import dev.southside.module.impl.player.Stuck;
import dev.southside.module.impl.render.BedShow;
import dev.southside.module.impl.render.BrokenClouds;
import dev.southside.module.impl.render.Chatting;
import dev.southside.module.impl.render.ChestESP;
import dev.southside.module.impl.render.Compass;
import dev.southside.module.impl.render.CriticalParticle;
import dev.southside.module.impl.render.CustomFOV;
import dev.southside.module.impl.render.Detector;
import dev.southside.module.impl.render.ESP2D;
import dev.southside.module.impl.render.FakerVSBin;
import dev.southside.module.impl.render.FullBright;
import dev.southside.module.impl.render.Glow;
import dev.southside.module.impl.render.HUD;
import dev.southside.module.impl.render.InventoryHUD;
import dev.southside.module.impl.render.IronESP;
import dev.southside.module.impl.render.Item2D;
import dev.southside.module.impl.render.ItemHighlight;
import dev.southside.module.impl.render.ItemHud;
import dev.southside.module.impl.render.ItemPhysics;
import dev.southside.module.impl.render.ItemTag;
import dev.southside.module.impl.render.KillEffect;
import dev.southside.module.impl.render.LineBreakFormattor;
import dev.southside.module.impl.render.MotionBlur;
import dev.southside.module.impl.render.MotionCamera;
import dev.southside.module.impl.render.NameProtect;
import dev.southside.module.impl.render.NameTags;
import dev.southside.module.impl.render.NoRender;
import dev.southside.module.impl.render.OldHitting;
import dev.southside.module.impl.render.PaperDoll;
import dev.southside.module.impl.render.PlayerAppearance;
import dev.southside.module.impl.render.PotionEffects;
import dev.southside.module.impl.render.RainLove;
import dev.southside.module.impl.render.ScoreBoard;
import dev.southside.module.impl.render.TNTips;
import dev.southside.module.impl.render.TargetHUD;
import dev.southside.module.impl.render.Trajectories;
import dev.southside.module.impl.render.ViewClip;
import dev.southside.module.impl.render.WorldManager;
import dev.southside.module.impl.world.AntiLiquid;
import dev.southside.module.impl.world.ChestAura;
import dev.southside.module.impl.world.ChestWreck;
import dev.southside.module.impl.world.EntityEditor;
import dev.southside.module.impl.world.FastPlace;
import dev.southside.module.impl.world.LightningTracker;
import dev.southside.module.impl.world.Scaffold;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.Formatting;

public class ModuleManager {
   public static File CONFIG = Southside.FOLDER.toPath().resolve("module.json").toFile();
   @SerializedName("modules")
   public List<Module> modules = new ArrayList<>();

   public void performAction() {
      Southside.INSTANCE.getEventBus().subscribe(this);
      this.performAction(new dev.southside.module.impl.client.ArrayList());
      this.performAction(new ClientSettings());
      this.performAction(new Notification());
      this.performAction(new Watermark());
      this.performAction(new Targets());
      this.performAction(new Renders());
      this.performAction(new TabGUI());
      this.performAction(new SuperKnockBack());
      this.performAction(new KBController());
      this.performAction(new AutoKnockBack());
      this.performAction(new BowAimAssist());
      this.performAction(new BowSpammer());
      this.performAction(new AntiFireball());
      this.performAction(new AutoClicker());
      this.performAction(new AutoCrystal());
      this.performAction(new TimerRange());
      this.performAction(new TriggerBot());
      this.performAction(new BedAura());
      this.performAction(new Criticals());
      this.performAction(new BackTrack());
      this.performAction(new Velocity());
      this.performAction(new KillAura());
      this.performAction(new AutoSoup());
      this.performAction(new AutoPot());
      this.performAction(new AutoRod());
      this.performAction(new HitBox());
      this.performAction(new DiscordPresence());
      this.performAction(new GodItemDetector());
      this.performAction(new HackerDetector());
      this.performAction(new KillSay());
      this.performAction(new AutoPixelParty());
      this.performAction(new MatchingBypass());
      this.performAction(new FuckOffHand());
      this.performAction(new MurderFinder());
      this.performAction(new AntiExploit());
      this.performAction(new ClientSpoof());
      this.performAction(new ChatBypass());
      this.performAction(new Disabler());
      this.performAction(new AutoPlay());
      this.performAction(new AntiStaff());
      this.performAction(new AntiNick());
      this.performAction(new FakeLag());
      this.performAction(new AntiBot());
      this.performAction(new Teams());
      this.performAction(new MCF());
      this.performAction(new BlockHunter());
      this.performAction(new InventoryMove());
      this.performAction(new NoJumpDelay());
      this.performAction(new KeepSprint());
      this.performAction(new StrafeFix());
      this.performAction(new LongJump());
      this.performAction(new FastWeb());
      this.performAction(new Flight());
      this.performAction(new Spider());
      this.performAction(new NoSlow());
      this.performAction(new NoPush());
      this.performAction(new Sprint());
      this.performAction(new Speed());
      this.performAction(new Timer());
      this.performAction(new TP2Bed());
      this.performAction(new GrimFly());
      this.performAction(new HealthBypass());
      this.performAction(new AutoOffhand());
      this.performAction(new InvManager());
      this.performAction(new AutoBucket());
      this.performAction(new GhostHand());
      this.performAction(new AutoTotem());
      this.performAction(new Stealer());
      this.performAction(new AutoTrap());
      this.performAction(new AntiVoid());
      this.performAction(new AutoTool());
      this.performAction(new KeyPearl());
      this.performAction(new AntiWeb());
      this.performAction(new NoFall());
      this.performAction(new NoteBot());
      this.performAction(new Blink());
      this.performAction(new Stuck());
      this.performAction(new Eagle());
      this.performAction(new BlockIn());
      this.performAction(new LightningTracker());
      this.performAction(new AntiLiquid());
      this.performAction(new ChestWreck());
      this.performAction(new ChestAura());
      this.performAction(new FastPlace());
      this.performAction(new Scaffold());
      this.performAction(new EntityEditor());
      this.performAction(new LineBreakFormattor());
      this.performAction(new PlayerAppearance());
      this.performAction(new PotionEffects());
      this.performAction(new ItemHighlight());
      this.performAction(new Trajectories());
      this.performAction(new NameProtect());
      this.performAction(new ItemPhysics());
      this.performAction(new ScoreBoard());
      this.performAction(new FullBright());
      this.performAction(new OldHitting());
      this.performAction(new KillEffect());
      this.performAction(new CriticalParticle());
      this.performAction(new WorldManager());
      this.performAction(new TargetHUD());
      this.performAction(new PaperDoll());
      this.performAction(new Chatting());
      this.performAction(new ChestESP());
      this.performAction(new IronESP());
      this.performAction(new CustomFOV());
      this.performAction(new MotionCamera());
      this.performAction(new MotionBlur());
      this.performAction(new RainLove());
      this.performAction(new BrokenClouds());
      this.performAction(new NoRender());
      this.performAction(new ViewClip());
      this.performAction(new Detector());
      this.performAction(new NameTags());
      this.performAction(new ItemTag());
      this.performAction(new Item2D());
      this.performAction(new ItemHud());
      this.performAction(new BedShow());
      this.performAction(new Compass());
      this.performAction(new ESP2D());
      this.performAction(new Theme());
      this.performAction(new Glow());
      this.performAction(new HUD());
      this.performAction(new TNTips());
      this.performAction(new FakerVSBin());
      this.performAction(new InventoryHUD());

      for (Module module : this.modules) {
         module.modData.config = module;
         boolean isEnabledByDefault = module.isEnabledByDefault();
         if (isEnabledByDefault) {
            module.setEnabled(true);
         }
      }

      try {
         this.performActionAlt();
      } catch (IOException e) {
         Southside.LOGGER.error("Failed to load module configuration", e);
      }

      for (Module module2 : this.modules) {
         module2.initialize();
         module2.postInit();
      }
   }

   public static void getModule(JsonObject json, Module module) {
      module.fromJson(json);
   }

   public static boolean isValid(Class<?> clazz, Module module) {
      return module.getClass().equals(clazz);
   }

   public <T extends Module> T getModule(Class<T> clazz) {
      return (T)clazz.cast(this.modules.stream().filter(module -> isValid(clazz, module)).findFirst().orElseThrow());
   }

   public void performActionEx() throws IOException {
      if (!CONFIG.exists()) {
         CONFIG.createNewFile();
      }

      JsonObject json = new JsonObject();

      for (Module module : Southside.INSTANCE.getModuleManager().getModules()) {
         JsonObject json2 = module.toJson();
         json.add(module.modData.name, json2);
      }

      PrintWriter printWriter = new PrintWriter(new FileWriter(CONFIG));
      printWriter.println(Southside.GSON.toJson(json));
      printWriter.close();
   }

   public static boolean isValid(String text, Module module) {
      return module.getNameKey().equalsIgnoreCase(text);
   }

   public String formatText(Module module) {
      if ((Boolean)ArrayListHud.showSuffix.getValue()) {
         return module.getSuffix().isEmpty() ? module.getNameKey() : String.format("%s %s%s", module.getNameKey(), Formatting.GRAY, module.getSuffix());
      } else {
         return module.getNameKey();
      }
   }

   public List<Module> getModules() {
      return this.modules;
   }

   public static boolean isValid(Category category, Module module) {
      return module.getCategory() == category;
   }

   public static void performAction(JsonElement element, Module module) {
      JsonObject asJsonObject = element.getAsJsonObject();
      if (asJsonObject.has(module.modData.name)) {
         module.loadFromJson(asJsonObject);
      }
   }

   public void performActionAlt() throws IOException {
      if (!CONFIG.exists()) {
         this.performActionEx();
      }

      JsonElement element = JsonParser.parseReader(new BufferedReader(new FileReader(CONFIG)));
      if (!(element instanceof JsonNull)) {
         JsonObject asJsonObject = element.getAsJsonObject();
         this.modules.forEach(module -> getModule(asJsonObject, module));
         this.performActionEx();
      }
   }

   public void performAction(Module module) {
      String nameKey = module.getNameKey();
      String text = nameKey;
      if (this.modules.stream().filter(module2 -> isValid(text, module2)).findFirst().orElse(null) != null) {
         Southside.LOGGER.warn("Duplicate module: {}", module.getNameKey());
      }

      this.modules.add(module);
   }

   public String formatTextEx(Module module) {
      if ((Boolean)ArrayListHud.showSuffix.getValue()) {
         return module.getSuffix().isEmpty() ? module.getNameKey() : String.format("%s %s", module.getNameKey(), module.getSuffix());
      } else {
         return module.getNameKey();
      }
   }

   public List collectItems() {
      return this.modules.stream().filter(Config::isEnabled).toList();
   }

   public void performAction(List<Module> list) throws IOException {
      JsonElement element = JsonParser.parseReader(new BufferedReader(new FileReader(CONFIG)));
      if (!(element instanceof JsonNull)) {
         list.forEach(module -> performAction(element, module));
      }
   }
}
