package dev.southside.mixin;

import dev.southside.Southside;
import dev.southside.event.Stage;
import dev.southside.event.events.ClientWorldEvent;
import dev.southside.event.events.MinecraftClientEvent;
import dev.southside.event.events.PreTickEvent;
import dev.southside.event.events.DisconnectEvent;
import dev.southside.event.events.PhaseEvent;
import dev.southside.event.events.RenderStageEvent;
import dev.southside.event.events.ScreenEvent;
import dev.southside.event.events.StateEvent;
import dev.southside.module.impl.render.OldHitting;
import dev.southside.render.ImeOverlayManager;
import dev.southside.utils.InteractionStateFlags;
import dev.southside.utils.TimerSpeedManager;
import dev.southside.utils.ItemUseDispatcher;
import dev.southside.utils.CustomMenuController;
import dev.southside.utils.PingSpoofManager;
import dev.southside.utils.IMinecraftClientExt;
import dev.southside.utils.ClientTickEvent;
import dev.southside.utils.ServerPacketManager;
import dev.southside.utils.RotationManager;
import dev.southside.utils.MovementUtils;
import dev.southside.utils.BlockLookUtils;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.RunArgs;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.screen.Overlay;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerWarningScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.realms.gui.screen.RealmsGenericErrorScreen;
import net.minecraft.client.realms.gui.screen.RealmsMainScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.RenderTickCounter.Dynamic;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.toast.TutorialToast;
import net.minecraft.client.tutorial.TutorialManager;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.Window;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.item.PotionItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.consume.UseAction;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public abstract class MixinMinecraftClient implements IMinecraftClientExt {
   @Shadow
   public Screen currentScreen;
   @Final
   @Shadow
   private static Logger LOGGER;
   @Shadow
   public int attackCooldown;
   @Shadow
   public int itemUseCooldown;
   @Shadow
   @Final
   private Window window;
   @Shadow
   Thread thread;
   @Shadow
   private boolean disconnecting;
   @Shadow
   public boolean skipGameRender;
   @Final
   @Shadow
   private SoundManager soundManager;
   @Shadow
   @Nullable
   public ClientPlayerEntity player;
   @Shadow
   @Nullable
   public HitResult crosshairTarget;
   @Shadow
   @Final
   public GameOptions options;
   @Shadow
   @Final
   public Dynamic renderTickCounter;
   @Shadow
   @Final
   public GameRenderer gameRenderer;
   @Shadow
   @Final
   public WorldRenderer worldRenderer;
   @Shadow
   @Final
   public InGameHud inGameHud;
   @Shadow
   @Final
   private static Text SOCIAL_INTERACTIONS_NOT_AVAILABLE;
   @Shadow
   @Final
   private NarratorManager narratorManager;
   @Shadow
   @Nullable
   private TutorialToast socialInteractionsToast;
   @Shadow
   @Final
   private TutorialManager tutorialManager;
   @Shadow
   @Nullable
   public ClientPlayerInteractionManager interactionManager;
   @Shadow
   @Nullable
   private Overlay overlay;
   @Shadow
   @Final
   public Mouse mouse;
   @Shadow
   @Nullable
   public ClientWorld world;
   @Unique
   private AtomicBoolean screenCloseFlag;
   @Unique
   private AtomicBoolean tickObserved;
   @Unique
   private AtomicInteger initialTickCount;

   @Inject(method = "render", at = @At("HEAD"))
   private void beginTextInputFrame(boolean flag, CallbackInfo ci) {
      ImeOverlayManager.INSTANCE.performAction();
   }

   @Inject(method = "render", at = @At("RETURN"))
   private void endTextInputFrame(boolean flag, CallbackInfo ci) {
      ImeOverlayManager.INSTANCE.performActionEx();
   }

   @Inject(method = "setScreen", at = @At("RETURN"))
   private void updateTextInputScreen(Screen screen, CallbackInfo ci) {
      ImeOverlayManager.INSTANCE.performActionAlt();
   }

   @Inject(method = "close", at = @At("HEAD"))
   private void closeTextInput(CallbackInfo ci) {
      ImeOverlayManager.INSTANCE.performActionSafe();
   }

   @Inject(method = "Lnet/minecraft/client/MinecraftClient;doItemUse()V", at = @At("HEAD"), cancellable = true)
   public void onDoItemUseHead(CallbackInfo ci) {
      ci.cancel();
      ItemUseDispatcher.performAction();
   }

   @Redirect(
      method = "Lnet/minecraft/client/MinecraftClient;tick()V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;handleInputEvents()V")
   )
   private void redirectTick(MinecraftClient client) {
   }

   @Shadow
   public void setScreen(@Nullable Screen screen) {
   }

   @Inject(method = "Lnet/minecraft/client/MinecraftClient;<init>(Lnet/minecraft/client/RunArgs;)V", at = @At("TAIL"))
   public void onInitTail(RunArgs runArgs, CallbackInfo ci) {
      Southside.INSTANCE.performActionEx();
   }

   @ModifyVariable(
      method = "Lnet/minecraft/client/MinecraftClient;setScreen(Lnet/minecraft/client/gui/screen/Screen;)V",
      at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;currentScreen:Lnet/minecraft/client/gui/screen/Screen;", shift = Shift.AFTER),
      ordinal = 0,
      argsOnly = true
   )
   private Screen modifySetScreen(Screen screen) {
      return CustomMenuController.getScreen(this.currentScreen, screen);
   }

   @Shadow
   public abstract Window getWindow();

   @Inject(
      method = "Lnet/minecraft/client/MinecraftClient;tick()V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;updateCrosshairTarget(F)V", shift = Shift.BEFORE)
   )
   public void onTickInvoke2(CallbackInfo ci) {
      if (MinecraftClient.getInstance().player != null) {
         BlockLookUtils.vector = MinecraftClient.getInstance().player.getEyePos();
      }

      RotationManager.updateRotations();
   }

   @Shadow
   protected abstract void openChatScreen(String text);

   @Inject(method = "Lnet/minecraft/client/MinecraftClient;setWorld(Lnet/minecraft/client/world/ClientWorld;)V", at = @At("HEAD"))
   public void onSetWorldHead(ClientWorld world2, CallbackInfo ci) {
      Southside.INSTANCE.getEventBus().dispatch(new ClientWorldEvent(world2));
      MovementUtils.active = false;
      PingSpoofManager.linkedBlockingQueue.clear();
      ServerPacketManager.clear();
   }

   @Inject(method = "Lnet/minecraft/client/MinecraftClient;setScreen(Lnet/minecraft/client/gui/screen/Screen;)V", at = @At("HEAD"), cancellable = true)
   private void onSetScreenHead(Screen screen, CallbackInfo ci) {
      Screen currentScreen2 = this.currentScreen;
      if (screen == null
         && !(currentScreen2 instanceof CreateWorldScreen)
         && !(currentScreen2 instanceof MultiplayerScreen)
         && !(currentScreen2 instanceof MultiplayerWarningScreen)
         && !(currentScreen2 instanceof RealmsMainScreen)
         && !(currentScreen2 instanceof OptionsScreen)
         && !(currentScreen2 instanceof RealmsGenericErrorScreen)) {
         if (this.screenCloseFlag == null) {
            this.screenCloseFlag = new AtomicBoolean(true);
         }

         if (!this.screenCloseFlag.get()) {
            this.screenCloseFlag.set(true);
         }
      }

      if (((ScreenEvent)Southside.INSTANCE.getEventBus().postCancellable(new ScreenEvent(screen))).isCancelled()) {
         ci.cancel();
      }
   }

   @Inject(
      method = "Lnet/minecraft/client/MinecraftClient;handleInputEvents()V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z", ordinal = 0, shift = Shift.BEFORE),
      cancellable = true
   )
   private void onHandleInputEventsInvoke2(CallbackInfo ci) {
      if (Southside.INSTANCE != null && Southside.INSTANCE.getEventBus() != null) {
         PreTickEvent preTickEvent = new PreTickEvent();
         Southside.INSTANCE.getEventBus().postCancellable(preTickEvent);
         if (preTickEvent.isCancelled()) {
            ci.cancel();
         }
      }
   }

   @Inject(at = @At("HEAD"), method = "Lnet/minecraft/client/MinecraftClient;stop()V")
   private void onStopHead(CallbackInfo ci) {
      try {
         Southside.INSTANCE.performAction();
      } catch (Exception e) {
         Southside.LOGGER.error("Unexpected error in MixinMinecraftClient", e);
      }
   }

   @Shadow
   public abstract boolean isConnectedToLocalServer();

   @Shadow
   @Nullable
   public abstract ClientPlayNetworkHandler getNetworkHandler();

   @Override
   public boolean hasTicked() {
      return this.tickObserved.get();
   }

   @Inject(method = "Lnet/minecraft/client/MinecraftClient;disconnect(Lnet/minecraft/client/gui/screen/Screen;Z)V", at = @At("HEAD"))
   private void onDisconnectHead(Screen screen, boolean flag, CallbackInfo ci) {
      Southside.INSTANCE.getEventBus().dispatch(new DisconnectEvent());
   }

   @Shadow
   protected abstract void doItemPick();

   @Inject(method = "Lnet/minecraft/client/MinecraftClient;tick()V", at = @At("TAIL"))
   public void onTickTail(CallbackInfo ci) {
      Southside.INSTANCE.getEventBus().dispatch(new StateEvent(Stage.POST));
      InteractionStateFlags.active = false;
      InteractionStateFlags.activeValue = false;
   }

   @Inject(
      method = "Lnet/minecraft/client/MinecraftClient;handleInputEvents()V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z", shift = Shift.AFTER, ordinal = 0)
   )
   private void onHandleInputEventsInvoke(CallbackInfo ci) {
      if (!this.player.hasVehicle() && this.crosshairTarget != null && Southside.INSTANCE.getModuleManager().getModule(OldHitting.class).isEnabled()) {
         UseAction useAction = this.player.getActiveItem().getUseAction();
         boolean flag = this.player.getActiveItem().getItem() instanceof SwordItem
            || useAction.equals(UseAction.EAT)
            || useAction.equals(UseAction.BOW)
            || this.player.getActiveItem().getItem() instanceof PotionItem;
         if (flag && this.options.attackKey.isPressed() && this.crosshairTarget.getType() == Type.BLOCK) {
            this.player.swingHand(Hand.MAIN_HAND, false);
         }
      }
   }

   @Shadow
   protected abstract boolean doAttack();

   @Inject(
      method = "Lnet/minecraft/client/MinecraftClient;render(Z)V",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/render/GameRenderer;render(Lnet/minecraft/client/render/RenderTickCounter;Z)V",
         shift = Shift.BEFORE
      )
   )
   public void beforeGameRendererRender(CallbackInfo ci) {
      Southside.INSTANCE.getEventBus().dispatch(new RenderStageEvent(Stage.PRE, this.renderTickCounter.getTickDelta(false)));
   }

   @Override
   public boolean getScreenCloseFlag() {
      return this.screenCloseFlag.get();
   }

   @Shadow
   @Nullable
   public abstract Entity getCameraEntity();

   @Override
   public void setAttackCooldown(int n) {
      this.attackCooldown = n;
   }

   @Shadow
   protected abstract void handleBlockBreaking(boolean flag);

   @Inject(
      method = "Lnet/minecraft/client/MinecraftClient;render(Z)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/Framebuffer;draw(II)V", shift = Shift.AFTER)
   )
   public void afterFramebufferDraw(CallbackInfo ci) {
      Southside.INSTANCE.getEventBus().dispatch(new PhaseEvent(Stage.POST));
   }

   @Inject(
      method = "Lnet/minecraft/client/MinecraftClient;tick()V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;handleInputEvents()V", shift = Shift.BEFORE)
   )
   public void beforeInputTick(CallbackInfo ci) {
      ServerPacketManager.performActionEx();
      Southside.INSTANCE.getEventBus().postCancellable(new MinecraftClientEvent());
   }

   @Inject(method = "Lnet/minecraft/client/MinecraftClient;tick()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sound/MusicTracker;tick()V"))
   public void onTickInvoke(CallbackInfo ci) {
      if (this.tickObserved == null) {
         this.tickObserved = new AtomicBoolean(false);
      }

      if (this.initialTickCount == null) {
         this.initialTickCount = new AtomicInteger(0);
      }

      if (!this.tickObserved.get()) {
         this.tickObserved.set(true);
      }

      if (this.initialTickCount.get() <= 50) {
         this.initialTickCount.getAndIncrement();
      }
   }

   @Inject(
      method = "Lnet/minecraft/client/MinecraftClient;render(Z)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;runTasks()V", shift = Shift.BEFORE)
   )
   private void beforeRenderTaskRun(CallbackInfo ci) {
      Southside.INSTANCE.getEventBus().postCancellable(new ClientTickEvent());
   }

   @Shadow
   public void updateWindowTitle() {
   }

   @Inject(
      method = "Lnet/minecraft/client/MinecraftClient;render(Z)V",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/render/GameRenderer;render(Lnet/minecraft/client/render/RenderTickCounter;Z)V",
         shift = Shift.AFTER
      )
   )
   public void afterGameRendererRender(CallbackInfo ci) {
      Southside.INSTANCE.getEventBus().dispatch(new RenderStageEvent(Stage.POST, this.renderTickCounter.getTickDelta(false)));
   }

   @Inject(
      method = "Lnet/minecraft/client/MinecraftClient;render(Z)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/Framebuffer;draw(II)V", shift = Shift.BEFORE)
   )
   public void beforeFramebufferDraw(CallbackInfo ci) {
      Southside.INSTANCE.getEventBus().dispatch(new PhaseEvent(Stage.PRE));
   }

   @Override
   public void attack() {
      this.doAttack();
   }

   @Inject(method = "Lnet/minecraft/client/MinecraftClient;getWindowTitle()Ljava/lang/String;", at = @At("RETURN"), cancellable = true)
   private void onGetWindowTitleReturn(CallbackInfoReturnable<String> cir) {
      cir.setReturnValue("southside.exe - " + Southside.labelValue);
   }

   @Inject(method = "Lnet/minecraft/client/MinecraftClient;tick()V", at = @At("HEAD"))
   public void onTickHead(CallbackInfo ci) {
      TimerSpeedManager.setTimerSpeed(1.0F);
      Southside.INSTANCE.getEventBus().dispatch(new StateEvent(Stage.PRE));
      PingSpoofManager.performActionEx();
      ServerPacketManager.performActionAlt();
   }

   @Override
   public void setScreenCloseFlag(boolean flag) {
      this.screenCloseFlag.set(flag);
   }

   @Override
   public int getInitialTickCount() {
      return this.initialTickCount.get();
   }
}
