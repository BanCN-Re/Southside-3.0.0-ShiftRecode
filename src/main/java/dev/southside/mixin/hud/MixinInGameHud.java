package dev.southside.mixin.hud;

import dev.southside.Southside;
import dev.southside.module.impl.render.HUD;
import dev.southside.module.impl.render.NoRender;
import dev.southside.module.impl.render.ScoreBoard;
import dev.southside.utils.SmoothEasing;
import dev.southside.utils.OneConfigFonts;
import dev.southside.utils.NanoVGHelper;
import dev.southside.utils.IMinecraft;
import dev.southside.utils.render.jello.animation.Easing;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.option.AttackIndicator;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.profiler.Profilers;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class MixinInGameHud {
   @Shadow
   @Final
   private static Identifier HOTBAR_ATTACK_INDICATOR_BACKGROUND_TEXTURE;
   @Shadow
   @Final
   private static Identifier HOTBAR_ATTACK_INDICATOR_PROGRESS_TEXTURE;
   @Shadow
   @Final
   private static Identifier EXPERIENCE_BAR_BACKGROUND_TEXTURE;
   @Shadow
   @Final
   private static Identifier EXPERIENCE_BAR_PROGRESS_TEXTURE;
   @Shadow
   @Final
   private static Identifier FOOD_EMPTY_HUNGER_TEXTURE;
   @Shadow
   @Final
   private static Identifier FOOD_HALF_HUNGER_TEXTURE;
   @Shadow
   @Final
   private static Identifier FOOD_FULL_HUNGER_TEXTURE;
   @Shadow
   @Final
   private static Identifier AIR_BURSTING_TEXTURE;
   @Shadow
   @Final
   private static Identifier ARMOR_EMPTY_TEXTURE;
   @Shadow
   @Final
   private static Identifier FOOD_EMPTY_TEXTURE;
   @Shadow
   @Final
   private static Identifier ARMOR_FULL_TEXTURE;
   @Shadow
   @Final
   private static Identifier ARMOR_HALF_TEXTURE;
   @Shadow
   @Final
   private static Identifier FOOD_HALF_TEXTURE;
   @Shadow
   @Final
   private static Identifier FOOD_FULL_TEXTURE;
   @Shadow
   @Final
   private static Identifier AIR_TEXTURE;
   @Shadow
   @Final
   private MinecraftClient client;
   @Shadow
   @Final
   private Random random;
   @Shadow
   private long heartJumpEndTick;
   @Shadow
   private int ticks;
   @Shadow
   private int lastHealthValue;
   @Shadow
   private long lastHealthCheckTime;
   @Shadow
   private int renderHealthValue;
   @Unique
   private SmoothEasing hotbarSelectionAnimation;

   @Shadow
   private LivingEntity getRiddenEntity() {
      return null;
   }

   @Shadow
   protected abstract void renderHotbarItem(DrawContext context, int n, int n2, RenderTickCounter renderTickCounter, PlayerEntity player, ItemStack stack, int n3);

   @Shadow
   private void renderHealthBar(
      DrawContext context, PlayerEntity player, int n, int n2, int n3, int n4, float f, int n5, int n6, int n7, boolean flag
   ) {
   }

   @Inject(
      method = "Lnet/minecraft/client/gui/hud/InGameHud;renderStatusEffectOverlay(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V",
      at = @At("HEAD"),
      cancellable = true
   )
   public void onRenderStatusEffectOverlayHead(DrawContext context, RenderTickCounter renderTickCounter, CallbackInfo ci) {
      NoRender noRender = Southside.INSTANCE.getModuleManager().getModule(NoRender.class);
      if (noRender.isEnabled() && noRender.vanillaEffects.getValue()) {
         ci.cancel();
      }
   }

   @Unique
   private void renderCustomHotbar(DrawContext context, RenderTickCounter renderTickCounter) {
      if (this.hotbarSelectionAnimation == null) {
         this.hotbarSelectionAnimation = new SmoothEasing(Easing.EASE_IN_SINE, 150L);
      }

      HUD hud = Southside.INSTANCE.getModuleManager().getModule(HUD.class);
      PlayerEntity cameraPlayer = this.getCameraPlayer();
      if (cameraPlayer != null) {
         int n = context.getScaledWindowWidth() / 2;
         int n2 = n - 99;
         int n3 = context.getScaledWindowHeight() - 25;
         NanoVGHelper.INSTANCE.performAction(true, l -> NanoVGHelper.INSTANCE.drawRoundedRect(l, n2 - 1, n3 - 1, 200.0F, 24.0F, -1943919834, 6.0F));
         int n4 = n2 + cameraPlayer.getInventory().selectedSlot * 22;
         this.hotbarSelectionAnimation.computeDouble(n4);
         NanoVGHelper.INSTANCE.performAction(true, l2 -> {
            NanoVGHelper nvg = NanoVGHelper.INSTANCE;
            float f = hud.hotbarAnimation.getValue() ? (float)this.hotbarSelectionAnimation.getCurrentValue() : n4;
            nvg.drawRoundedRect(l2, f, n3, 22.0F, 22.0F, -1, 4.0F, 0.6F);
         });

         for (int i = 0; i < 9; i++) {
            int n5 = n2 + i * 22 + 3;
            ItemStack stack = (ItemStack)cameraPlayer.getInventory().main.get(i);
            this.renderHotbarItem(context, n5, n3 + 3, renderTickCounter, cameraPlayer, stack, i + 1);
         }

         ItemStack offHandStack = cameraPlayer.getOffHandStack();
         if (!offHandStack.isEmpty()) {
            Arm opposite = cameraPlayer.getMainArm().getOpposite();
            int n6 = opposite == Arm.LEFT ? n2 - 24 : n + 99 + 4;
            this.renderHotbarItem(context, n6, n3 + 3, renderTickCounter, cameraPlayer, offHandStack, 10);
         }

         if (this.client.options.getAttackIndicator().getValue() == AttackIndicator.HOTBAR) {
            float attackCooldownProgress = cameraPlayer.getAttackCooldownProgress(0.0F);
            if (attackCooldownProgress < 1.0F) {
               int n7 = n3 + 2;
               int n8 = cameraPlayer.getMainArm() == Arm.RIGHT ? n2 - 22 : n + 99 + 6;
               int n9 = (int)(attackCooldownProgress * 19.0F);
               context.drawGuiTexture(RenderLayer::getGuiTextured, HOTBAR_ATTACK_INDICATOR_BACKGROUND_TEXTURE, n8, n7, 18, 18);
               context.drawGuiTexture(
                  RenderLayer::getGuiTextured, HOTBAR_ATTACK_INDICATOR_PROGRESS_TEXTURE, 18, 18, 0, 18 - n9, n8, n7 + 18 - n9, 18, n9
               );
            }
         }
      }
   }

   @Inject(
      method = "Lnet/minecraft/client/gui/hud/InGameHud;renderHotbar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V",
      at = @At("HEAD"),
      cancellable = true
   )
   public void onRenderHotbarHead(DrawContext context, RenderTickCounter renderTickCounter, CallbackInfo ci) {
      HUD hud = Southside.INSTANCE.getModuleManager().getModule(HUD.class);
      if (hud.isEnabled() && hud.hotbar.getValue()) {
         this.renderCustomHotbar(context, renderTickCounter);
         ci.cancel();
      }
   }

   @Shadow
   private int getHeartCount(@Nullable LivingEntity living) {
      return 0;
   }

   @Shadow
   private PlayerEntity getCameraPlayer() {
      return null;
   }

   @Shadow
   private int getHeartRows(int n) {
      return 0;
   }

   @Overwrite
   private void renderStatusBars(DrawContext context) {
      PlayerEntity cameraPlayer = this.getCameraPlayer();
      if (cameraPlayer != null) {
         int n = MathHelper.ceil(cameraPlayer.getHealth());
         boolean flag = this.heartJumpEndTick > this.ticks && (this.heartJumpEndTick - this.ticks) / 3L % 2L == 1L;
         long now = Util.getMeasuringTimeMs();
         if (n < this.lastHealthValue && cameraPlayer.timeUntilRegen > 0) {
            this.lastHealthCheckTime = now;
            this.heartJumpEndTick = this.ticks + 20;
         } else if (n > this.lastHealthValue && cameraPlayer.timeUntilRegen > 0) {
            this.lastHealthCheckTime = now;
            this.heartJumpEndTick = this.ticks + 10;
         }

         if (now - this.lastHealthCheckTime > 1000L) {
            this.lastHealthValue = n;
            this.renderHealthValue = n;
            this.lastHealthCheckTime = now;
         }

         this.lastHealthValue = n;
         int renderHealthValue2 = this.renderHealthValue;
         this.random.setSeed(this.ticks * 312871);
         HungerManager hungerManager = cameraPlayer.getHungerManager();
         int foodLevel = hungerManager.getFoodLevel();
         int n2 = context.getScaledWindowWidth() / 2 - 91;
         int n3 = context.getScaledWindowWidth() / 2 + 91;
         int n4 = context.getScaledWindowHeight() - 39;
         float f = Math.max((float)cameraPlayer.getAttributeValue(EntityAttributes.MAX_HEALTH), Math.max(renderHealthValue2, n));
         int n5 = MathHelper.ceil(cameraPlayer.getAbsorptionAmount());
         int n6 = MathHelper.ceil((f + n5) / 2.0F / 10.0F);
         int n7 = Math.max(10 - (n6 - 2), 3);
         int n8 = n4 - (n6 - 1) * n7 - 10;
         HUD hud = Southside.INSTANCE.getModuleManager().getModule(HUD.class);
         if (hud.hotbar.getValue()) {
            n8 -= 4;
         }

         int n9 = n4 - 10;
         int armor = cameraPlayer.getArmor();
         int n10 = -1;
         if (cameraPlayer.hasStatusEffect(StatusEffects.REGENERATION)) {
            n10 = this.ticks % MathHelper.ceil(f + 5.0F);
         }

         Profilers.get().push("armor");
         if (armor > 0) {
            for (int i = 0; i < 10; i++) {
               int n11 = n2 + i * 8;
               int n12 = i * 2 + 1;
               Identifier id = n12 < armor ? ARMOR_FULL_TEXTURE : (n12 == armor ? ARMOR_HALF_TEXTURE : ARMOR_EMPTY_TEXTURE);
               context.drawGuiTexture(RenderLayer::getGuiTextured, id, n11, n8, 9, 9);
            }
         }

         Profilers.get().swap("health");
         if (hud.hotbar.getValue()) {
            n4 -= 4;
         }

         this.renderHealthBar(context, cameraPlayer, n2, n4, n7, n10, f, n, renderHealthValue2, n5, flag);
         LivingEntity riddenEntity = this.getRiddenEntity();
         int heartCount = this.getHeartCount(riddenEntity);
         if (heartCount == 0) {
            Profilers.get().swap("food");

            for (int j = 0; j < 10; j++) {
               int n13 = n4;
               Identifier id2;
               Identifier id3;
               Identifier id4;
               if (cameraPlayer.hasStatusEffect(StatusEffects.HUNGER)) {
                  id2 = FOOD_EMPTY_HUNGER_TEXTURE;
                  id3 = FOOD_HALF_HUNGER_TEXTURE;
                  id4 = FOOD_FULL_HUNGER_TEXTURE;
               } else {
                  id2 = FOOD_EMPTY_TEXTURE;
                  id3 = FOOD_HALF_TEXTURE;
                  id4 = FOOD_FULL_TEXTURE;
               }

               if (cameraPlayer.getHungerManager().getSaturationLevel() <= 0.0F && this.ticks % (foodLevel * 3 + 1) == 0) {
                  n13 = n4 + (this.random.nextInt(3) - 1);
               }

               int n14 = n3 - j * 8 - 9;
               context.drawGuiTexture(RenderLayer::getGuiTextured, id2, n14, n13, 9, 9);
               if (j * 2 + 1 < foodLevel) {
                  context.drawGuiTexture(RenderLayer::getGuiTextured, id4, n14, n13, 9, 9);
               } else if (j * 2 + 1 == foodLevel) {
                  context.drawGuiTexture(RenderLayer::getGuiTextured, id3, n14, n13, 9, 9);
               }
            }

            n9 -= 10;
         }

         Profilers.get().swap("air");
         int maxAir = cameraPlayer.getMaxAir();
         int n15 = Math.min(cameraPlayer.getAir(), maxAir);
         if (cameraPlayer.isSubmergedIn(FluidTags.WATER) || n15 < maxAir) {
            n9 -= (this.getHeartRows(heartCount) - 1) * 10;
            if (hud.hotbar.getValue()) {
               n9 -= 4;
            }

            int n16 = MathHelper.ceil((n15 - 2) * 10.0 / maxAir);
            int n17 = MathHelper.ceil(n15 * 10.0 / maxAir) - n16;

            for (int k = 0; k < n16 + n17; k++) {
               Identifier id5 = k < n16 ? AIR_TEXTURE : AIR_BURSTING_TEXTURE;
               context.drawGuiTexture(RenderLayer::getGuiTextured, id5, n3 - k * 8 - 9, n9, 9, 9);
            }
         }

         Profilers.get().pop();
      }
   }

   @Inject(
      method = "Lnet/minecraft/client/gui/hud/InGameHud;renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V",
      at = @At("HEAD"),
      cancellable = true
   )
   private void onRenderScoreboardSidebarHead(DrawContext context, ScoreboardObjective scoreboardObjective, CallbackInfo ci) {
      if (IMinecraft.mc.world != null) {
         ScoreBoard scoreBoard = Southside.INSTANCE.getModuleManager().getModule(ScoreBoard.class);
         if (scoreBoard.isEnabled()) {
            ci.cancel();
         }
      }
   }

   @Overwrite
   private void renderExperienceBar(DrawContext context, int n) {
      HUD hud = Southside.INSTANCE.getModuleManager().getModule(HUD.class);
      boolean flag = hud.hotbar.getValue();
      if (this.client.player.getNextLevelExperience() > 0) {
         Profilers.get().push("expBar");
         int n2 = (int)(this.client.player.experienceProgress * 183.0F);
         int n3 = context.getScaledWindowHeight() - 32;
         if (!flag) {
            n3 += 3;
         }

         if (flag) {
            this.renderCustomExperienceBar(n, n3, n2, this.client.player.experienceLevel);
         } else {
            context.drawGuiTexture(RenderLayer::getGuiTextured, EXPERIENCE_BAR_BACKGROUND_TEXTURE, n, n3, 182, 5);
            if (n2 > 0) {
               context.drawGuiTexture(RenderLayer::getGuiTextured, EXPERIENCE_BAR_PROGRESS_TEXTURE, 182, 5, 0, 0, n, n3, n2, 5);
            }

            this.renderVanillaExperienceLevel(context);
         }

         Profilers.get().pop();
      }
   }

   @Unique
   private void renderCustomExperienceBar(int n, int n2, int n3, int n4) {
      NanoVGHelper.INSTANCE.performAction(true, l -> {
         NanoVGHelper.INSTANCE.drawRoundedRect(l, n, n2, 182.0F, 4.0F, -13421773, 1.0F);
         if (n3 > 0) {
            NanoVGHelper.INSTANCE.drawRoundedRect(l, n, n2, n3, 3.0F, -11154347, 1.0F);
         }

         if (n4 > 0) {
            Profilers.get().push("expLevel");
            String text = String.valueOf(n4);
            float textWidth = NanoVGHelper.INSTANCE.getTextWidth(l, text, 9.0F, OneConfigFonts.BOLD);
            float f = n + (182.0F - textWidth) / 2.0F;
            float f2 = n2 - 5;
            NanoVGHelper.INSTANCE.drawTextEx(l, text, f - 1.0F, f2, -16777216, 9.0F, OneConfigFonts.BOLD);
            NanoVGHelper.INSTANCE.drawTextEx(l, text, f + 1.0F, f2, -16777216, 9.0F, OneConfigFonts.BOLD);
            NanoVGHelper.INSTANCE.drawTextEx(l, text, f, f2 - 1.0F, -16777216, 9.0F, OneConfigFonts.BOLD);
            NanoVGHelper.INSTANCE.drawTextEx(l, text, f, f2 + 1.0F, -16777216, 9.0F, OneConfigFonts.BOLD);
            NanoVGHelper.INSTANCE.drawTextEx(l, text, f, f2, -11154347, 9.0F, OneConfigFonts.BOLD);
            Profilers.get().pop();
         }
      });
   }

   @Unique
   private void renderVanillaExperienceLevel(DrawContext context) {
      if (this.client.player.experienceLevel > 0) {
         Profilers.get().push("expLevel");
         String text = String.valueOf(this.client.player.experienceLevel);
         int n = (context.getScaledWindowWidth() - this.getTextRenderer().getWidth(text)) / 2;
         int n2 = context.getScaledWindowHeight() - 31 - 4;
         context.drawText(this.getTextRenderer(), text, n + 1, n2, 0, false);
         context.drawText(this.getTextRenderer(), text, n - 1, n2, 0, false);
         context.drawText(this.getTextRenderer(), text, n, n2 + 1, 0, false);
         context.drawText(this.getTextRenderer(), text, n, n2 - 1, 0, false);
         context.drawText(this.getTextRenderer(), text, n, n2, 8453920, false);
         Profilers.get().pop();
      }
   }

   @Shadow
   public TextRenderer getTextRenderer() {
      return null;
   }
}
