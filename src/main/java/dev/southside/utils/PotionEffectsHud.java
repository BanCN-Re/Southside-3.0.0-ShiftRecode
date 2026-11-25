package dev.southside.utils;

import cc.polyfrost.oneconfig.hud.Hud;
import com.google.gson.annotations.SerializedName;
import dev.southside.module.impl.render.PotionEffects;
import dev.southside.render.UIMatrixStack;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class PotionEffectsHud extends Hud {
   private static final float DEFAULT_FLOAT = 150.0F;
   @SerializedName("effectRows")
   private final Map<String, PotionEffectsHud.Inner> effectRows = new HashMap<>();
   @SerializedName("listHeight")
   private final SmoothFloat listHeight = new SmoothFloat(4.0F, 200.0F);

   public PotionEffectsHud(float x, float y, float z) {
      super(x, y, 1, z);
   }

   @Override
   public void draw(DrawContext context, UIMatrixStack matrices, float f, float f2, float f3, boolean flag) {
      if (mc.player != null) {
         Collection statusEffects = mc.player.getStatusEffects();
         if (!(this.listHeight.getValue() < 1.0F) || flag || !statusEffects.isEmpty()) {
            ScissorHelper scissor = ScissorHelper.INSTANCE;
            NanoVGHelper.INSTANCE.performAction(true, l -> this.draw(l, scissor, f, f2, f3, flag, statusEffects));
            if (!flag) {
               context.enableScissor((int)f, (int)f2, (int)(f + 150.0F * f3), (int)(f2 + this.listHeight.getValue()));

               for (PotionEffectsHud.Inner inner : this.effectRows.values()) {
                  inner.render(context, f, f2, f3);
               }

               context.disableScissor();
            }
         }
      }
   }

   @Override
   public float computeFloat(float f, boolean flag) {
      return 150.0F * f;
   }

   @Override
   public float setScale(float f, boolean flag) {
      return this.listHeight == null ? 37.5F * f : this.listHeight.targetValue;
   }

   private void draw(long l, ScissorHelper scissor, float f, float f2, float f3, boolean flag, Collection<StatusEffectInstance> collection) {
      ScissorRect clip = scissor.scissor(l, f, f2, 150.0F * f3, this.listHeight.getValue());
      if (!flag) {
         float f4 = 0.0F;

         for (Object obj : new ArrayList(collection)) {
            StatusEffectInstance statusEffectInstance = (StatusEffectInstance)obj;
            String string = ((StatusEffect)statusEffectInstance.getEffectType().value()).getName().getString();
            PotionEffectsHud.Inner inner = this.effectRows.computeIfAbsent(string, text -> new PotionEffectsHud.Inner(statusEffectInstance));
            inner.effect = statusEffectInstance;
            inner.slideOffset.setTarget(f4);
            inner.draw(l, f, f2, f3);
            f4 += (PotionEffects.style.matches("Simple") ? 46.0F : 37.5F) * f3;
         }

         for (PotionEffectsHud.Inner inner2 : this.effectRows.values()) {
            if (!collection.contains(inner2.effect)) {
               inner2.slideOffset.setTarget(-75.0F * f3);
               inner2.draw(l, f, f2, f3);
            }
         }

         this.listHeight.setTarget(f4 > 0.0F ? f4 : 0.0F);
      }

      scissor.resetScissor(l, clip);
   }

   private static final class Inner {
      @SerializedName("slideOffset")
      public final SmoothFloat slideOffset = new SmoothFloat(3.0F, 0.0F);
      @SerializedName("initialDuration")
      private final int initialDuration;
      @SerializedName("effect")
      public StatusEffectInstance effect;

      public Inner(StatusEffectInstance statusEffectInstance) {
         this.effect = statusEffectInstance;
         this.initialDuration = statusEffectInstance.getDuration();
      }

      public void draw(long l, float f, float f2, float f3) {
         if (!(this.slideOffset.getValue() < -30.0F) && IMinecraft.mc.player != null) {
            float f4 = this.slideOffset.getValue() + f2;
            NanoVGHelper nvg = NanoVGHelper.INSTANCE;
            String text = null;
            if (IMinecraft.mc.world != null) {
               text = StatusEffectUtil.getDurationText(this.effect, 1.0F, IMinecraft.mc.world.getTickManager().getTickRate()).getString();
            }

            if (PotionEffects.style.matches("Simple")) {
               this.draw(l, nvg, f, f4, f3, text);
            } else {
               this.draw(l, nvg, f, f4, f2, f3, text);
            }
         }
      }

      private void draw(long l, NanoVGHelper nvg, float f, float f2, float f3, float f4, String text) {
         float f5 = 150.0F * f4;
         float f6 = f5 * (1.0F * this.effect.getDuration() / this.initialDuration);
         float f7 = 36.5F * f4;
         int size = IMinecraft.mc.player.getStatusEffects().size();
         PotionEffectsHud.Inner.Mode mode;
         if (size == 1) {
            mode = PotionEffectsHud.Inner.Mode.SINGLE;
         } else if (!(f2 + f7 >= f3 + 35.5F * f4 * size) && !(f2 <= f3 + 35.5F * f4)) {
            mode = PotionEffectsHud.Inner.Mode.MIDDLE;
         } else {
            mode = f2 + f7 <= f3 + 35.5F * f4 * size ? PotionEffectsHud.Inner.Mode.FIRST : PotionEffectsHud.Inner.Mode.LAST;
         }

         ScissorHelper scissor = ScissorHelper.INSTANCE;
         ScissorRect clip = scissor.scissor(l, f + f6, f2 + f4, f5 - f6, f7);
         this.draw(l, nvg, f, f2, f4, text, mode, 1291845632, -1);
         scissor.resetScissor(l, clip);
         ScissorRect clip2 = scissor.scissor(l, f, f2 + f4, f6, f7);
         this.draw(l, nvg, f, f2, f4, text, mode, -2013265921, -16777216);
         scissor.resetScissor(l, clip2);
      }

      private void draw(long l, NanoVGHelper nvg, float f, float f2, float f3, String text, PotionEffectsHud.Inner.Mode mode, int n, int n2) {
         float f4 = 150.0F * f3;
         float f5 = 36.5F * f3;
         if (mode == PotionEffectsHud.Inner.Mode.MIDDLE) {
            nvg.drawRect(l, f, f2 + f3, f4, f5, n);
         } else {
            float f6 = 5.0F * f3;
            float f7 = mode != PotionEffectsHud.Inner.Mode.SINGLE && mode != PotionEffectsHud.Inner.Mode.FIRST ? 0.0F : f6;
            float f8 = mode != PotionEffectsHud.Inner.Mode.SINGLE && mode != PotionEffectsHud.Inner.Mode.LAST ? 0.0F : f6;
            nvg.drawRoundedRectVaried(l, f, f2 + f3, f4, f5, n, f7, f7, f8, f8);
         }

         nvg.drawTextEx(l, this.getText().getString(), f + 32.5F * f3, f2 + 18.75F * f3, n2, 11.25F * f3, OneConfigFonts.SEMIBOLD);
         if (text != null) {
            float f9 = f + 147.5F * f3 - nvg.getTextWidth(l, text, 8.75F * f3, OneConfigFonts.REGULAR);
            nvg.drawTextEx(l, text, f9, f2 + 31.25F * f3, n2, 8.75F * f3, OneConfigFonts.REGULAR);
         }
      }

      private void draw(long l, NanoVGHelper nvg, float f, float f2, float f3, String text) {
         float f4 = 150.0F * f3;
         float f5 = 40.0F * f3;
         nvg.drawRect(l, f, f2 + f3, f4, f5, -1275068416);
         String string = this.getText().getString();
         float f6 = 11.25F * f3;
         float f7 = f2 + f3 + f5 / 2.0F;
         nvg.drawTextEx(l, string, f + 32.5F * f3, f7, -1, f6, OneConfigFonts.SEMIBOLD);
         if (text != null) {
            float textWidth = nvg.getTextWidth(l, text, f6, OneConfigFonts.SEMIBOLD);
            nvg.drawTextEx(l, text, f + f4 - textWidth - 5.0F * f3, f7, -4934476, f6, OneConfigFonts.SEMIBOLD);
         }

         float f8 = f2 + f3 + f5 - 2.0F * f3;
         float f9 = 2.0F * f3;
         float f10 = 1.0F * this.effect.getDuration() / this.initialDuration;
         nvg.drawRect(l, f, f8, f4, f9, 520093695);
         if (f10 > 0.0F) {
            nvg.drawRect(l, f, f8, f4 * f10, f9, -587202561);
         }
      }

      public void render(DrawContext context, float f, float f2, float f3) {
         if (!(this.slideOffset.getValue() < -30.0F)) {
            float f4 = this.slideOffset.getValue() + f2;
            Sprite sprite = IMinecraft.mc.getStatusEffectSpriteManager().getSprite(this.effect.getEffectType());
            MatrixStack matrices = context.getMatrices();
            matrices.push();
            matrices.translate(f, f4, 0.0F);
            matrices.scale(f3 * 1.25F, f3 * 1.25F, f3 * 1.25F);
            context.drawSpriteStretched(RenderLayer::getGuiTextured, sprite, 5, 5, 18, 18);
            matrices.pop();
         }
      }

      private Text getText() {
         MutableText text = ((StatusEffect)this.effect.getEffectType().value()).getName().copy();
         if (this.effect.getAmplifier() >= 1 && this.effect.getAmplifier() <= 9) {
            text.append(ScreenTexts.SPACE).append(Text.translatable("enchantment.level." + (this.effect.getAmplifier() + 1)));
         }

         return text;
      }

      private enum Mode {
         SINGLE,
         FIRST,
         MIDDLE,
         LAST;


         // $VF: synthetic method
         private static PotionEffectsHud.Inner.Mode[] createValues() {
            return new PotionEffectsHud.Inner.Mode[]{SINGLE, FIRST, MIDDLE, LAST};
         }


      }
   }
}
