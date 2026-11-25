package dev.southside.module.impl.render;

import com.google.gson.annotations.SerializedName;
import dev.southside.Southside;
import dev.southside.auth.IrcState;
import dev.southside.auth.IrcUser;
import dev.southside.event.EventTarget;
import dev.southside.event.events.DrawContextEvent;
import dev.southside.manager.FriendManager;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.impl.misc.AntiBot;
import dev.southside.module.impl.misc.GodItemDetector;
import dev.southside.module.impl.misc.HackerDetector;
import dev.southside.module.impl.misc.MurderFinder;
import dev.southside.module.impl.misc.Teams;
import dev.southside.module.values.BooleanValue;
import dev.southside.render.ProjectionUtils;
import dev.southside.render.NanoVGTransform;
import dev.southside.utils.OneConfigFonts;
import dev.southside.utils.EntityTargetUtils;
import dev.southside.utils.ScissorHelper;
import dev.southside.utils.NanoVGHelper;
import dev.southside.utils.NumberValue;
import dev.southside.utils.ModeValue;
import dev.southside.utils.FakePlayerEntity;
import dev.southside.utils.ScissorRect;
import java.awt.Color;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public class NameTags extends Module {
   private static final Color DEFAULT_COLOR = new Color(34, 35, 38, 191);
   private static final Color DEFAULT_COLOR_VALUE = new Color(0, 0, 0, 76);
   private static final float DEFAULT_FLOAT = -62.5F;
   private static final float DEFAULT_FLOAT_VALUE = 52.5F;
   private static final float DEFAULT_FLOAT_DATA = 7.5F;
   @SerializedName("style")
   public final ModeValue style = new ModeValue("Style", new String[]{"Classic", "Summer", "Retro", "Simple"}, "Choose nametag style", 2, 0);
   @SerializedName("distanceScale")
   public final BooleanValue distanceScale = new BooleanValue("Distance Scale", false);
   @SerializedName("showGodItem")
   public final BooleanValue showGodItem = new BooleanValue("Show GodItem", true);
   @SerializedName("showMurder")
   public final BooleanValue showMurder = new BooleanValue("Show Murder", true);
   @SerializedName("hideVanillaName")
   public final BooleanValue hideVanillaName = new BooleanValue("Hide Vanilla Name", true);
   @SerializedName("hideMe")
   public final BooleanValue hideMe = new BooleanValue("Hide Me", true);
   @SerializedName("customScale")
   public final BooleanValue customScale = new BooleanValue("Custom Scale", false);
   @SerializedName("backgroundGlow")
   public final BooleanValue backgroundGlow = new BooleanValue("Background Glow", false);
   @SerializedName("displayIRC")
   public final BooleanValue displayIRC = new BooleanValue("Display IRC", true);
   @SerializedName("scale")
   public final NumberValue scale = new NumberValue("Scale", 1.0F, 0.1F, 10.0F, 0.1F);

   public NameTags() {
      super(Category.Render, "NameTags", "视奸。。。");
   }

   @Override
   public void postInit() {
      super.postInit();
      this.hideIf(this.customScale.getLabel(), this.distanceScale::getValue);
      this.hideIf(this.scale.getLabel(), () -> !(Boolean)this.customScale.getValue() || (Boolean)this.distanceScale.getValue());
   }

   @EventTarget
   public void onDrawContext(DrawContextEvent drawContextEvent) {
      if (mc2.world != null) {
         for (Entity entity : mc2.world.getEntities()) {
            if (entity instanceof LivingEntity living && isValidTarget(entity)) {
               this.updateEntity(living, drawContextEvent.getTickDelta());
            }
         }
      }
   }

   private static boolean isValidTarget(Entity entity) {
      return !(entity instanceof FakePlayerEntity) && EntityTargetUtils.isValidTarget(entity);
   }

   private void updateEntity(LivingEntity living, float f) {
      if (living != mc2.player || !(Boolean)this.hideMe.getValue() && !mc2.options.getPerspective().isFirstPerson()) {
         String string = living.getDisplayName().getString();
         IrcUser ircUser = null;
         boolean flag = false;
         boolean flag2 = false;
         if (living instanceof PlayerEntity player) {
            if ((Boolean)this.displayIRC.getValue()) {
               String text = player.getUuid().toString();

               for (IrcUser ircUser2 : IrcState.ircUsersSet) {
                  if (ircUser2.getUuids().contains(text)) {
                     ircUser = ircUser2;
                  }
               }
            }

            if (player.getGameProfile() != null) {
               string = player.getGameProfile().getName();
               FriendManager friendManager = Southside.INSTANCE.getFriendManager();
               flag = friendManager != null && friendManager.friends.contains(string);
            }

            flag2 = isValidTarget(player) && Teams.isValidTarget(player);
         }

         boolean isBot = AntiBot.isBot(living);
         boolean isValidTarget = Southside.INSTANCE.getModuleManager().getModule(HackerDetector.class).isValidTarget(living);
         Vec3d lerpedPos = living.getLerpedPos(f);
         double d = (living.getWidth() + 0.2) / 2.0;
         double d2 = living.getHeight() + (living.isSneaking() ? -0.3 : 0.2) + 0.05;
         Vec2f vec2f = ProjectionUtils.getVec2f(lerpedPos.x, lerpedPos.y, lerpedPos.z, d, d2);
         if (vec2f != null) {
            string = this.formatText(living, string);
            if ((Boolean)this.showMurder.getValue() && MurderFinder.isMurder(living)) {
               string = formatText(string, "Murder", Formatting.DARK_RED);
            }

            if (flag2) {
               string = formatText(string, "Team", Formatting.GREEN);
            }

            if (flag) {
               string = formatText(string, "Friend", Formatting.GREEN);
            }

            if (isBot) {
               string = formatText(string, "Bot", Formatting.RED);
            }

            if (isValidTarget) {
               string = formatText(string, "Hacker", Formatting.DARK_RED);
            }

            if (ircUser != null) {
               String text2 = Formatting.AQUA + "(" + ircUser.getUsername() + ")";
               string = NameProtect.isValidEx() ? text2 : text2 + Formatting.RESET + string;
            }

            String text3 = string;
            NanoVGHelper.INSTANCE.performAction(true, l -> this.draw(l, living, f, vec2f, text3));
         }
      }
   }

   private String formatText(LivingEntity living, String text) {
      GodItemDetector godItemDetector = GodItemDetector.getGodItemDetector();
      if ((Boolean)this.showGodItem.getValue() && godItemDetector != null && godItemDetector.isEnabled()) {
         GodItemDetector.Mode mode = GodItemDetector.Mode.values()[godItemDetector.godItemTags.getValue()];
         if (mode != GodItemDetector.Mode.BOTH && mode != GodItemDetector.Mode.NAMETAG) {
            return text;
         }

         if (GodItemDetector.notifiedPlayers.contains(living)) {
            text = formatText(text, "GodAxe", Formatting.GOLD);
         }

         if (GodItemDetector.maceHolders.contains(living)) {
            text = formatText(text, "Crystal", Formatting.DARK_PURPLE);
         }

         if (GodItemDetector.endCrystalHolders.contains(living)) {
            text = formatText(text, "GApple", Formatting.LIGHT_PURPLE);
         }

         if (GodItemDetector.godAxeHolders.contains(living)) {
            text = formatText(text, "Mace", Formatting.YELLOW);
         }

         return text;
      } else {
         return text;
      }
   }

   private static String formatText(String text, String text2, Formatting formatting) {
      String text3 = String.valueOf(formatting);
      String text4 = String.valueOf(Formatting.RESET);
      String text5 = text;
      String text6 = text4;
      String text7 = text2;
      return text3 + "[" + text7 + "] " + text6 + text5;
   }

   private void draw(long l, LivingEntity living, float f, Vec2f vec2, String text) {
      double dist = mc2.gameRenderer.getCamera().getPos().distanceTo(living.getLerpedPos(f));
      double d = this.customScale.getValue()
         ? this.scale.getCurrentValueEx()
         : (this.distanceScale.getValue() ? MathHelper.clamp(1.0 - dist * 0.01, 0.5, 2.147483647E9) : 1.0);
      NanoVGTransform.scale(vec2.x, vec2.y, (float)d);

      try {
         NanoVGHelper.INSTANCE.translate(l, vec2.x, vec2.y);
         switch (this.style.getDisplayTextAlt()) {
            case "Classic":
               this.draw(l, living, text, false);
               break;
            case "Retro":
               this.draw(l, living, text, true);
               break;
            case "Summer":
               this.draw(l, living, text, dist, true);
               break;
            case "Simple":
               this.draw(l, living, text, dist, false);
         }
      } finally {
         NanoVGTransform.restore();
      }
   }

   private void draw(long l, LivingEntity living, String text, boolean flag) {
      short s = 17077;
      NanoVGHelper nvg = NanoVGHelper.INSTANCE;
      ScissorHelper scissor = ScissorHelper.INSTANCE;
      short s2 = -21016;
      String formatted = String.format("Health: %.1f / %.1f", living.getHealth(), living.getMaxHealth());
      float f = flag
         ? Math.max(nvg.getTextWidth(l, text, 18.0F, OneConfigFonts.SEMIBOLD), nvg.getTextWidth(l, formatted, 12.0F, OneConfigFonts.REGULAR)) + 18.0F
         : 197.5F;
      float f2 = -f / 2.0F;
      float f3 = f * clamp(living);
      float textHeight = nvg.getTextHeight(l, 12.0F, OneConfigFonts.REGULAR);
      float f4 = -62.5F + (((flag ? 60.0F : 52.5F) - 30.0F) / 2.0F + textHeight / 2.0F);
      if ((Boolean)this.backgroundGlow.getValue()) {
         nvg.drawRoundedRect(l, f2, -62.5F, f, 52.5F, 10.0F, 0.0F, 7.5F, DEFAULT_COLOR);
      }

      if (flag) {
         ScissorRect clip = scissor.scissor(l, f2 + f3, -62.5F, f - f3, 52.5F);

         try {
            draw(l, f2, f, f4, textHeight, text, formatted, DEFAULT_COLOR.getRGB(), Color.WHITE.getRGB());
         } finally {
            scissor.resetScissor(l, clip);
         }

         ScissorRect clip2 = scissor.scissor(l, f2, -62.5F, f3, 52.5F);

         try {
            draw(l, f2, f, f4, textHeight, text, formatted, Integer.MAX_VALUE, Color.BLACK.getRGB());
         } finally {
            scissor.resetScissor(l, clip2);
         }
      } else {
         nvg.drawRoundedRect(l, f2, -62.5F, f, 52.5F, DEFAULT_COLOR.getRGB(), 7.5F);
         ScissorRect clip3 = scissor.scissor(l, f2, -14.0F, f3, 4.0F);

         try {
            nvg.drawRoundedRectVaried(l, f2, -62.5F, f, 52.5F, Color.WHITE.getRGB(), 0.0F, 0.0F, 7.5F, 7.5F);
         } finally {
            scissor.resetScissor(l, clip3);
         }

         nvg.draw(l, text, f2 + 9.0F, f4, Color.WHITE.getRGB(), 13.0F, OneConfigFonts.SEMIBOLD);
         nvg.draw(l, formatted, f2 + 9.0F, f4 + textHeight + 4.5F, Color.WHITE.getRGB(), 8.0F, OneConfigFonts.REGULAR);
      }
   }

   private static void draw(long vg, float x, float y, float width, float height, String text, String text2, int n, int n2) {
      NanoVGHelper nvg = NanoVGHelper.INSTANCE;
      nvg.drawRoundedRect(vg, x, -62.5F, y, 52.5F, n, 7.5F);
      nvg.draw(vg, text, x + 9.0F, width, n2, 18.0F, OneConfigFonts.SEMIBOLD);
      nvg.draw(vg, text2, x + 9.0F, width + height + 4.5F, n2, 12.0F, OneConfigFonts.REGULAR);
   }

   private void draw(long l, LivingEntity living, String text, double d, boolean flag) {
      NanoVGHelper nvg = NanoVGHelper.INSTANCE;
      String text2 = " " + (int)living.getHealth() + "HP";
      String text3 = " " + String.format("%.1f", d) + "m";
      float textHeight = nvg.getTextHeight(l, 13.0F, OneConfigFonts.SEMIBOLD);
      float textWidth = nvg.getTextWidth(l, text, 13.0F, OneConfigFonts.SEMIBOLD);
      float textWidth2 = nvg.getTextWidth(l, text2, 13.0F, OneConfigFonts.SEMIBOLD);
      float textWidth3 = nvg.getTextWidth(l, text3, 13.0F, OneConfigFonts.SEMIBOLD);
      float f = textWidth + textWidth2 + textWidth3 + 20.0F;
      float f2 = textHeight + 20.0F;
      float f3 = -f / 2.0F;
      float f4 = -f2 / 2.0F;
      if (flag) {
         if ((Boolean)this.backgroundGlow.getValue()) {
            nvg.drawRoundedRect(l, f3, f4, f, f2, 10.0F, 0.0F, 7.5F, DEFAULT_COLOR_VALUE);
         }

         nvg.drawRoundedRect(l, f3, f4, f, f2, DEFAULT_COLOR_VALUE.getRGB(), 7.5F);
      } else {
         nvg.drawRect(l, f3, f4, f, f2, -1275068416);
      }

      float f5 = f4 + 10.0F + textHeight / 2.0F;
      float f6 = f3 + 10.0F;
      nvg.draw(l, text, f6, f5, Color.WHITE.getRGB(), 13.0F, OneConfigFonts.SEMIBOLD);
      f6 += textWidth;
      nvg.draw(l, text2, f6, f5, Color.WHITE.getRGB(), 13.0F, OneConfigFonts.SEMIBOLD);
      f6 += textWidth2;
      nvg.draw(l, text3, f6, f5, flag ? -8355712 : -4934476, 13.0F, OneConfigFonts.SEMIBOLD);
      if (!flag) {
         nvg.drawRect(l, f3, f4 + f2, f, 2.0F, 520093695);
         nvg.drawRect(l, f3, f4 + f2, f * clamp(living), 2.0F, -587202561);
      }
   }

   private static float clamp(LivingEntity living) {
      return MathHelper.clamp(living.getHealth() / living.getMaxHealth(), 0.0F, 1.0F);
   }
}
