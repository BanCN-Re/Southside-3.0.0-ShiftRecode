package dev.southside.module.impl.player;

import com.google.gson.annotations.SerializedName;
import dev.southside.Southside;
import dev.southside.event.EventTarget;
import dev.southside.event.Stage;
import dev.southside.event.TransferOrigin;
import dev.southside.event.events.DirectionEvent;
import dev.southside.event.events.DrawContextEvent;
import dev.southside.event.events.ForwardEvent;
import dev.southside.event.events.XEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.impl.world.Scaffold;
import dev.southside.module.values.BooleanValue;
import dev.southside.utils.VoidCheckUtils;
import dev.southside.utils.OneConfigFonts;
import dev.southside.utils.NanoVGHelper;
import dev.southside.utils.IText;
import dev.southside.utils.NumberValue;
import dev.southside.utils.RotationManager;
import dev.southside.utils.ElapsedTimer;
import dev.southside.utils.PlayerMoveSimulator;
import dev.southside.utils.Rotation;
import dev.southside.utils.world.ProjectileUtil;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.item.EnderPearlItem;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.LookAndOnGround;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class AntiVoid extends Module {
   @SerializedName("fallDistance")
   public final NumberValue fallDistance = new NumberValue("Fall Distance", 3, 1, 10, 1);
   @SerializedName("debug")
   public final BooleanValue debug = new BooleanValue("Debug", false);
   @SerializedName("maxAttemptTimes")
   public final NumberValue maxAttemptTimes = new NumberValue("Max Attempt Times", 1, 2, 5);
   @SerializedName("currentSearch")
   private AntiVoid.Inner currentSearch;
   @SerializedName("attemptCount")
   private int attemptCount;
   @SerializedName("awaitingTeleport")
   private boolean awaitingTeleport;
   @SerializedName("throwAttempted")
   private boolean throwAttempted;
   @SerializedName("scaffoldAllowed")
   private boolean scaffoldAllowed;
   @SerializedName("activatedScaffold")
   private boolean activatedScaffold;
   @SerializedName("activatedStuck")
   private boolean activatedStuck;

   public AntiVoid() {
      super(Category.Player, "AntiVoid", "Automatically throws ender pearls when falling into the void");
   }

   @Override
   public void onDisable() {
      this.awaitingTeleport = false;
      this.throwAttempted = false;
      this.scaffoldAllowed = false;
      this.performActionImpl();
      this.performActionSafe();
      super.onDisable();
   }

   @EventTarget
   public void onX(XEvent xevent) {
      if (mc2.player != null && mc2.world != null) {
         if (mc2.player.isOnGround()) {
            this.attemptCount = 0;
            this.throwAttempted = false;
            this.awaitingTeleport = false;
            this.scaffoldAllowed = false;
            this.performActionImpl();
            this.performActionSafe();
         }

         if (xevent.getPhase() == Stage.POST) {
            this.performActionAlt();
         } else if (this.currentSearch == null
            && !this.awaitingTeleport
            && !this.throwAttempted
            && this.isValidEx()
            && mc2.player.getVelocity().y < -1.0
            && !mc2.player.isOnGround()
            && this.attemptCount < ((Number)this.maxAttemptTimes.getValue()).intValue()) {
            int n = this.computeInt();
            if (n == -1) {
               if ((Boolean)this.debug.getValue()) {
                  IText.performActionEx(Text.literal("AntiVoid: 热键栏中没有珍珠"));
               }

               return;
            }

            this.attemptCount++;
            mc2.player.getInventory().selectedSlot = n;
            this.performActionCustom();
         }
      }
   }

   private boolean isValidEx() {
      return mc2.player.getVelocity().y < 0.1
         && new PlayerMoveSimulator(mc2.player).findBlockPos(60) == null
         && !VoidCheckUtils.isValid(mc2.player.getY() + mc2.player.getEyeHeight(mc2.player.getPose()))
         && mc2.player.fallDistance > ((Number)this.fallDistance.getValue()).floatValue();
   }

   private void performActionAlt() {
      if (this.currentSearch != null) {
         AntiVoid.Entry result = this.currentSearch.result;
         if (result != null) {
            Rotation rotation = result.rotation;
            boolean flag = rotation != null && result.score >= 0.15;
            if ((Boolean)this.debug.getValue() && rotation != null && result.score < 0.15) {
               IText.performActionEx(Text.literal(String.format("§a[AntiVoid] §c分数过低 %.3f，放弃投掷（避免浪费珍珠）", result.score)));
            }

            if (flag && !this.throwAttempted) {
               this.awaitingTeleport = true;
               this.scaffoldAllowed = isValidBlock(result.landingBlock);
            }

            this.performActionImpl();
            if (flag && !this.throwAttempted) {
               this.sendPacket(rotation);
               this.throwAttempted = true;
            } else if ((Boolean)this.debug.getValue() && rotation == null) {
               IText.performActionEx(Text.literal("AntiVoid: 未能找到可用的珍珠角度"));
            }
         }
      }
   }

   private void sendPacket(Rotation rotation) {
      if (mc2.player != null && mc2.interactionManager != null) {
         mc2.player.setYaw(rotation.yaw);
         mc2.player.setPitch(rotation.pitch);
         float f = ((Double)mc2.options.getMouseSensitivity().getValue()).floatValue();
         float f2 = f * 0.6F + 0.2F;
         float f3 = f2 * f2 * f2 * 1.2F;
         rotation.yaw = rotation.yaw - rotation.yaw % f3;
         rotation.pitch = rotation.pitch - rotation.pitch % f3;
         mc2.player.networkHandler.sendPacket(new LookAndOnGround(rotation.yaw, rotation.pitch, mc2.player.isOnGround(), mc2.player.horizontalCollision));
         mc2.player.swingHand(Hand.MAIN_HAND);
         mc2.interactionManager.interactItem(mc2.player, Hand.MAIN_HAND);
         if ((Boolean)this.debug.getValue()) {
            IText.applyText(String.format("§a[AntiVoid] §f投掷珍珠 yaw=%.1f pitch=%.1f", rotation.yaw, rotation.pitch));
         }
      }
   }

   private int computeInt() {
      for (int i = 0; i < 9; i++) {
         if (mc2.player.getInventory().getStack(i).getItem() instanceof EnderPearlItem) {
            return i;
         }
      }

      return -1;
   }

   @EventTarget
   public void onDirection(DirectionEvent directionEvent) {
      if (directionEvent.getDirection() == TransferOrigin.RECEIVE && directionEvent.getPacket() instanceof PlayerPositionLookS2CPacket) {
         this.awaitingTeleport = false;
         if (this.scaffoldAllowed && mc2.player != null && !mc2.player.isOnGround()) {
            Scaffold scaffold = Southside.INSTANCE.getModuleManager().getModule(Scaffold.class);
            if (mc2.world != null && scaffold.computeIntAlt() > 0) {
               this.activatedScaffold = !scaffold.isEnabled();
               if (this.activatedScaffold) {
                  scaffold.setEnabled(true);
               }
            }
         }

         this.scaffoldAllowed = false;
         this.performActionDirect();
      }
   }

   private void performActionImpl() {
      if (this.currentSearch != null) {
         if (this.currentSearch.result == null) {
            this.currentSearch.cancelled = true;
         }

         this.currentSearch = null;
      }

      this.performActionDirect();
   }

   private void performActionDirect() {
      if (this.currentSearch == null && !this.awaitingTeleport && this.activatedStuck) {
         Southside.INSTANCE.getModuleManager().getModule(Stuck.class).setEnabled(false);
         this.activatedStuck = false;
      }
   }

   private void performActionSafe() {
      if (this.activatedScaffold) {
         Southside.INSTANCE.getModuleManager().getModule(Scaffold.class).setEnabled(false);
         this.activatedScaffold = false;
      }
   }

   @EventTarget
   public void onForward(ForwardEvent forwardEvent) {
      if (this.currentSearch != null) {
         forwardEvent.setCancelled(true);
      }
   }

   private void performActionCustom() {
      double y = mc2.player.getVelocity().y;
      ProjectileUtil projectileUtil = new ProjectileUtil(mc2.player.getX(), mc2.player.getY(), mc2.player.getZ(), y - 0.05, y + 0.05);
      this.currentSearch = new AntiVoid.Inner(projectileUtil, mc2.player.getYaw(), (Boolean)this.debug.getValue());
      this.currentSearch.start();
      Stuck stuck = Southside.INSTANCE.getModuleManager().getModule(Stuck.class);
      this.activatedStuck = !stuck.isEnabled();
      if (this.activatedStuck) {
         stuck.setEnabled(true);
      }
   }

   public static boolean isValidBlock(BlockPos pos) {
      if (pos != null && mc2.world != null) {
         BlockPos pos2 = pos.down();
         return !mc2.world.getBlockState(pos2).isSolidBlock(mc2.world, pos2);
      } else {
         return false;
      }
   }

   @EventTarget
   public void onDrawContext(DrawContextEvent drawContextEvent) {
      if ((Boolean)this.debug.getValue() && mc2.player != null && mc2.world != null) {
         double y = mc2.player.getVelocity().y;
         ProjectileUtil projectileUtil = new ProjectileUtil(mc2.player.getX(), mc2.player.getY(), mc2.player.getZ(), y - 0.01, y + 0.02);
         String text = "assessment: " + projectileUtil.computeDouble(RotationManager.getActiveRotation());
         String text2 = "(" + mc2.player.getYaw() + ", " + mc2.player.getPitch() + ")";
         NanoVGHelper nvg = NanoVGHelper.INSTANCE;
         nvg.performAction(true, l -> {
            nvg.drawTextEx(l, text, 20.0F, 20.0F, Color.WHITE.getRGB(), 12.0F, OneConfigFonts.REGULAR);
            nvg.drawTextEx(l, text2, 20.0F, 35.0F, Color.WHITE.getRGB(), 12.0F, OneConfigFonts.REGULAR);
         });
      }
   }

   private record Entry(Rotation rotation, BlockPos landingBlock, double score) {

      public Rotation getRotation() {
         return this.rotation;
      }

      public BlockPos getLandingBlock() {
         return this.landingBlock;
      }

      public double getScore() {
         return this.score;
      }
   }

   private record Entry2(float yaw, float pitch, double score) {

      public float getYaw() {
         return this.yaw;
      }

      public float getPitch() {
         return this.pitch;
      }

      public double getScore() {
         return this.score;
      }
   }

   private static final class Inner extends Thread {
      private static final long DEFAULT_LONG = 30L;
      private static final float[] DEFAULT_FLOATS = new float[]{-40.0F, -20.0F, 0.0F, 20.0F, 45.0F};
      @SerializedName("assessor")
      private final ProjectileUtil assessor;
      @SerializedName("initialYaw")
      private final float initialYaw;
      @SerializedName("debug")
      private final boolean debug;
      @SerializedName("cancelled")
      public volatile boolean cancelled;
      @SerializedName("result")
      public volatile AntiVoid.Entry result;
      @SerializedName("bestRotation")
      private Rotation bestRotation;
      @SerializedName("bestScore")
      private double bestScore = -1.0;
      @SerializedName("evaluationCount")
      private int evaluationCount;

      public Inner(ProjectileUtil projectileUtil, float f, boolean flag) {
         super("AntiVoid-Calculator");
         this.assessor = projectileUtil;
         this.initialYaw = MathHelper.wrapDegrees(f);
         this.debug = flag;
      }

      @Override
      public void run() {
         ElapsedTimer elapsedTimer = new ElapsedTimer();

         try {
            List list = this.collectItems(elapsedTimer);
            this.performAction(list, elapsedTimer);
            if (!list.isEmpty()) {
               AntiVoid.Entry2 first = (AntiVoid.Entry2)list.getFirst();
               this.bestScore = first.score;
               this.bestRotation = new Rotation(first.yaw, first.pitch);
            }

            int n = 0;

            for (Object obj : list) {
               AntiVoid.Entry2 entry2 = (AntiVoid.Entry2)obj;
               if (entry2.score <= 0.0 || this.isValid(elapsedTimer)) {
                  break;
               }

               n += this.computeInt(entry2.yaw, entry2.pitch, 15.0F, 10.0F, 3.0F, 0.85, elapsedTimer);
               if (this.bestScore >= 0.85) {
                  break;
               }
            }

            this.applyText("Phase 1", "局部细化", n, elapsedTimer);
            if (this.bestRotation != null && this.bestScore < 0.9 && !this.cancelled && elapsedTimer.getElapsedMs() < 30L) {
               int n2 = this.computeInt(this.bestRotation.yaw, this.bestRotation.pitch, 4.0F, 4.0F, 1.0F, 0.95, elapsedTimer);
               this.applyText("Phase 2", "精细微调", n2, elapsedTimer);
            }
         } finally {
            this.performAction(elapsedTimer);
         }
      }

      private boolean isValid(ElapsedTimer elapsedTimer) {
         return this.cancelled || elapsedTimer.getElapsedMs() > 30L;
      }

      private List<AntiVoid.Entry2> collectItems(ElapsedTimer elapsedTimer) {
         ArrayList list = new ArrayList(3);
         double d = (this.assessor.minVelocityY + this.assessor.maxVelocityY) * 0.5;

         for (int i = 0; i <= 8; i++) {
            float f = i == 8 ? this.initialYaw : MathHelper.wrapDegrees(i * 45.0F);

            for (float f2 : DEFAULT_FLOATS) {
               if (this.isValid(elapsedTimer)) {
                  return list;
               }

               double d2 = this.assessor.assessSingleRotation(new Rotation(f, clamp(f2)), d, this.debug);
               this.evaluationCount++;
               if (d2 > -1.0) {
                  int n = 0;

                  while (n < list.size() && !(d2 > ((AntiVoid.Entry2)list.get(n)).score)) {
                     n++;
                  }

                  if (n < 3) {
                     list.add(n, new AntiVoid.Entry2(f, f2, d2));
                     if (list.size() > 3) {
                        list.removeLast();
                     }
                  }
               }
            }
         }

         return list;
      }

      private int computeInt(float f, float f2, float f3, float f4, float f5, double d, ElapsedTimer elapsedTimer) {
         int n = 0;
         float f6 = f - f3;

         while (f6 <= f + f3) {
            for (float i = f2 - f4; i <= f2 + f4; i += f5) {
               if (this.isValid(elapsedTimer)) {
                  return n;
               }

               Rotation rotation = new Rotation(MathHelper.wrapDegrees(f6), clamp(i));

               double d2;
               try {
                  d2 = this.assessor.computeDouble(rotation);
               } catch (Exception e) {
                  continue;
               }

               this.evaluationCount++;
               n++;
               if (d2 > this.bestScore) {
                  this.bestScore = d2;
                  this.bestRotation = rotation;
               }

               if (this.bestScore >= d) {
                  return n;
               }
            }

            f6 += f5;
         }

         return n;
      }

      private static float clamp(float f) {
         return MathHelper.clamp(f, -89.9F, 89.9F);
      }

      private BlockPos findBlockPos() {
         if (this.bestRotation != null && AntiVoid.mc2.player != null && AntiVoid.mc2.world != null) {
            Rotation rotation = new Rotation(this.bestRotation.yaw, this.bestRotation.pitch);
            if (!(rotation.pitch <= 90.0F)) {
               rotation.pitch = 90.0F;
            }

            if (rotation.pitch < -90.0F) {
               rotation.pitch = -90.0F;
            }

            ProjectileUtil.Entry hitResult = this.assessor.raycast(rotation, (this.assessor.minVelocityY + this.assessor.maxVelocityY) * 0.5);
            return hitResult.getBlockHitResult() == null ? null : hitResult.getBlockHitResult().getBlockPos();
         } else {
            return null;
         }
      }

      private void performAction(ElapsedTimer elapsedTimer) {
         BlockPos pos = null;

         try {
            if (!this.cancelled) {
               pos = this.findBlockPos();
               if (this.debug) {
                  this.updateBlock(pos, elapsedTimer);
               }
            }
         } finally {
            this.result = new AntiVoid.Entry(this.bestRotation, pos, this.bestScore);
         }
      }

      private void performAction(List<AntiVoid.Entry2> list, ElapsedTimer elapsedTimer) {
         if (this.debug) {
            StringBuilder sb = new StringBuilder("§a[AntiVoid] §ePhase 0§f 候选筛选 (")
               .append(this.evaluationCount)
               .append(" evals, ")
               .append(elapsedTimer.getElapsedMs())
               .append("ms)");

            for (int i = 0; i < list.size() && ((AntiVoid.Entry2)list.get(i)).score > 0.0; i++) {
               AntiVoid.Entry2 entry2 = (AntiVoid.Entry2)list.get(i);
               sb.append(String.format("\n  §7#%d§f yaw=%.0f pitch=%.0f score=%.3f", i + 1, entry2.yaw, entry2.pitch, entry2.score));
            }

            applyText(sb.toString(), false);
         }
      }

      private void applyText(String text, String text2, int n, ElapsedTimer elapsedTimer) {
         if (this.debug) {
            String text3 = this.bestRotation == null
               ? "none"
               : String.format("yaw=%.1f pitch=%.1f score=%.3f", this.bestRotation.yaw, this.bestRotation.pitch, this.bestScore);
            applyText(String.format("§a[AntiVoid] §e%s§f %s (%d evals, %dms) best: %s", text, text2, n, elapsedTimer.getElapsedMs(), text3), false);
         }
      }

      private void updateBlock(BlockPos pos, ElapsedTimer elapsedTimer) {
         if (this.bestRotation == null) {
            applyText(String.format("§a[AntiVoid] §c失败§f 未找到可用角度 | %d evals %dms", this.evaluationCount, elapsedTimer.getElapsedMs()), true);
         } else {
            String text = pos == null ? "unknown" : String.format("(%d, %d, %d)", pos.getX(), pos.getY(), pos.getZ());
            applyText(
               String.format(
                  "§a[AntiVoid] §b完成§f yaw=%.1f pitch=%.1f score=%.3f | %d evals %dms | 落点=%s scaffold=%b",
                  this.bestRotation.yaw,
                  this.bestRotation.pitch,
                  this.bestScore,
                  this.evaluationCount,
                  elapsedTimer.getElapsedMs(),
                  text,
                  AntiVoid.isValidBlock(pos)
               ),
               false
            );
         }
      }

      private static void applyText(String text, boolean flag) {
         AntiVoid.mc2.execute(() -> {
            if (flag) {
               IText.performActionEx(Text.literal(text));
            } else {
               IText.applyText(text);
            }
         });
      }
   }
}
