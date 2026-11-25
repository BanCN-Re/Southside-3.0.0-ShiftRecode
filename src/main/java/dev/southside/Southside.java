package dev.southside;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import dev.southside.auth.IrcClient;
import dev.southside.command.CommandManager;
import dev.southside.event.EventBus;
import dev.southside.manager.ConfigManager;
import dev.southside.manager.FriendManager;
import dev.southside.manager.IRCManager;
import dev.southside.manager.StickerManager;
import dev.southside.module.ModuleManager;
import dev.southside.module.impl.misc.AntiNick;
import dev.southside.nanovg.NanoFontRenderer;
import dev.southside.render.GlShader;
import dev.southside.render.NanoVGContext;
import dev.southside.render.ProgressBarOverlay;
import dev.southside.render.ShaderProgram;
import dev.southside.render.pipeline.Mesh;
import dev.southside.render.shader.ShaderFactory;
import dev.southside.render.shader.ShaderType;
import dev.southside.utils.AttackTracker;
import dev.southside.utils.BedTracker;
import dev.southside.utils.BlinkUtils;
import dev.southside.utils.BoxMeshRenderer;
import dev.southside.utils.ChestTracker;
import dev.southside.utils.FontHelper;
import dev.southside.utils.HiddenItemsKit;
import dev.southside.utils.IMinecraft;
import dev.southside.utils.MovementFreezeManager;
import dev.southside.utils.MovementUtils;
import dev.southside.utils.NanoFontManager;
import dev.southside.utils.OneConfigCore;
import dev.southside.utils.OneConfigEventManager;
import dev.southside.utils.OrbitEventBus;
import dev.southside.utils.RenderContextHolder;
import dev.southside.utils.RenderDrawBuffer;
import dev.southside.utils.RotationManager;
import dev.southside.utils.heypixel.VIPList;
import dev.southside.utils.misc.AIWalkController;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.nanovg.NanoVGGL3;
import org.lwjgl.opengl.GL;

public class Southside implements ClientModInitializer, IMinecraft {
   public static String label = "Southside";
   public static String labelValue = "3.0.0";
   public static Logger LOGGER = LogManager.getLogger("Southside");
   public static File FOLDER = new File(MinecraftClient.getInstance().runDirectory, "Southside-nextgen-reborn");
   public static Southside INSTANCE;
   public static Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   @SerializedName("eventBus")
   public EventBus eventBus = new Southside.Inner(this);
   @SerializedName("moduleManager")
   public ModuleManager moduleManager;
   @SerializedName("commandManager")
   public CommandManager commandManager;
   @SerializedName("configManager")
   public ConfigManager configManager;
   @SerializedName("friendManager")
   public FriendManager friendManager;
   @SerializedName("progressBarOverlay")
   public ProgressBarOverlay progressBarOverlay;
   @SerializedName("initialized")
   public boolean initialized = false;
   @SerializedName("chestTracker")
   public ChestTracker chestTracker;
   @SerializedName("stickerManager")
   public StickerManager stickerManager;
   @SerializedName("scheduler")
   public ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
   @SerializedName("shutdownStarted")
   public AtomicBoolean shutdownStarted = new AtomicBoolean(false);

   public void performAction() {
      if (this.shutdownStarted.compareAndSet(false, true)) {
         try {
            if (this.moduleManager != null) {
               this.moduleManager.performActionEx();
            }
         } catch (IOException e) {
            LOGGER.error("Unexpected error in Southside", e);
         }

         IrcClient.getINSTANCE().performAction();
         this.scheduler.shutdownNow();
         NanoVGContext.delete();
         OneConfigEventManager.INSTANCE.performActionEx();
      }
   }

   public ChestTracker getChestTracker() {
      return this.chestTracker;
   }

   public FriendManager getFriendManager() {
      return this.friendManager;
   }

   public void onInitializeClient() {
      info("Loading Southside");
      AntiNick.register();
      HiddenItemsKit.performAction();
   }

   public StickerManager getStickerManager() {
      return this.stickerManager;
   }

   public ProgressBarOverlay getProgressBarOverlay() {
      return this.progressBarOverlay;
   }

   public ScheduledExecutorService getScheduler() {
      return this.scheduler;
   }

   public ModuleManager getModuleManager() {
      return this.moduleManager;
   }

   public void performActionEx() {
      info("Start Initializing");
      short s = -17784;
      this.update();
      short s2 = -662;
      this.performActionAlt();
      this.performActionImpl();
      this.performActionDirect();
      info("Finish Initializing");
      Runtime.getRuntime().addShutdownHook(new Thread(this::performAction));
   }

   private void update() {
      this.eventBus.subscribeTarget(this);
      performAction(this.eventBus, BedTracker.INSTANCE);
      performAction(this.eventBus, MovementFreezeManager.INSTANCE);
      performAction(this.eventBus, BlinkUtils.INSTANCE);
      performAction(this.eventBus, AIWalkController.INSTANCE);
      performAction(this.eventBus, AttackTracker.INSTANCE);
      performAction(this.eventBus, IRCManager.INSTANCE);
      this.chestTracker = new ChestTracker();
      performAction(this.eventBus, this.chestTracker);
      this.stickerManager = new StickerManager();
      this.progressBarOverlay = new ProgressBarOverlay();
      performAction(this.eventBus, this.progressBarOverlay);
   }

   private void performActionAlt() {
      GlShader glShader = ShaderFactory.getGlShader(Identifier.of("southside", "g/pos_color.vert"), "v_pos_color", ShaderType.VERTEX);
      GlShader glShader2 = ShaderFactory.getGlShader(Identifier.of("southside", "g/pos_color.frag"), "f_pos_color", ShaderType.FRAGMENT);
      ShaderProgram shaderProgram = ShaderFactory.getShaderProgram(glShader, glShader2);
      BoxMeshRenderer.renderMesh = new Mesh(shaderProgram, VertexFormats.POSITION_COLOR, DrawMode.TRIANGLES);
      BoxMeshRenderer.renderMeshValue = new Mesh(shaderProgram, VertexFormats.POSITION_COLOR, DrawMode.DEBUG_LINES);
      NanoVGContext.timestamp = NanoVGGL3.nvgCreate(3);
      NanoVG.nvgShapeAntiAlias(NanoVGContext.timestamp, true);
      GL.createCapabilities();
      FontHelper.INSTANCE.initialize(NanoVGContext.timestamp);
      NanoFontManager.nanoFontRenderer = new NanoFontRenderer("harmony", "wqy_microhei");
      short s = -17781;
      NanoFontManager.nanoFontRenderers.add(NanoFontManager.nanoFontRenderer);
      short s2 = 23882;
      info("Initializing GUI vertex buffer");
      s = -17787;
      RenderContextHolder.renderDrawBufferValue = new RenderDrawBuffer(16384);
      s2 = 1075;
      info("Initializing text vertex buffer");
      RenderContextHolder.renderDrawBuffer = new RenderDrawBuffer(2097152);
   }

   private void performActionImpl() {
      this.moduleManager = new ModuleManager();
      this.moduleManager.performAction();
      this.configManager = new ConfigManager();
      this.configManager.performAction();
      this.commandManager = new CommandManager();
      this.commandManager.update();
      this.friendManager = new FriendManager();
   }

   private void performActionDirect() {
      new RotationManager();
      new MovementUtils().performActionEx();
      VIPList.performAction();
      OneConfigEventManager.INSTANCE.performAction();
      OneConfigCore.INSTANCE.performAction();
   }

   public boolean isInitialized() {
      return this.initialized;
   }

   public Southside() {
      INSTANCE = this;
      if (!FOLDER.exists()) {
         FOLDER.mkdirs();
      }
   }

   public void setInitialized(boolean flag) {
      this.initialized = flag;
   }

   public CommandManager getCommandManager() {
      return this.commandManager;
   }

   public ConfigManager getConfigManager() {
      return this.configManager;
   }

   public AtomicBoolean getShutdownStarted() {
      return this.shutdownStarted;
   }

   public EventBus getEventBus() {
      return this.eventBus;
   }

   private static void info(String text) {
      LOGGER.info(text);
   }

   private static void performAction(EventBus eventBus2, Object obj) {
      eventBus2.subscribe(obj);
   }

   public static class Inner extends OrbitEventBus {
      public static Lookup getLookup(Class<?> clazz) throws IllegalAccessException {
         return MethodHandles.privateLookupIn(clazz, MethodHandles.lookup());
      }

      public Inner(Southside southside) {
         this.registerLambdaFactory(Southside.Inner::getLookup);
      }
   }
}
