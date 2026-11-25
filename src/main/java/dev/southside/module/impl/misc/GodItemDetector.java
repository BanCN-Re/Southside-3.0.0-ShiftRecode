package dev.southside.module.impl.misc;

import cc.polyfrost.oneconfig.utils.Notifications;
import com.google.gson.annotations.SerializedName;
import dev.southside.Southside;
import dev.southside.event.EventTarget;
import dev.southside.event.TransferOrigin;
import dev.southside.event.events.ClientWorldEvent;
import dev.southside.event.events.DirectionEvent;
import dev.southside.event.events.StateEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.impl.client.Notification;
import dev.southside.utils.EaseInOutCubicAnimation;
import dev.southside.utils.EntityTargetUtils;
import dev.southside.utils.OneConfigIcon;
import dev.southside.utils.ModeValue;
import dev.southside.utils.FakePlayerEntity;
import dev.southside.utils.OneConfigScreen;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.EntityAnimationS2CPacket;

public class GodItemDetector extends Module {
   public static final Set<Entity> notifiedPlayers = new HashSet<>();
   public static final Set<Entity> godAxeHolders = new HashSet<>();
   public static final Set<Entity> maceHolders = new HashSet<>();
   public static final Set<Entity> endCrystalHolders = new HashSet<>();
   public static final Set<Entity> eGappleHolders = new HashSet<>();
   @SerializedName("godItemTags")
   public final ModeValue godItemTags = new ModeValue(
      "God Item Tags", new String[]{"Disabled", "NameTag", "Detector", "Both"}, "Select which module renders God item tags", 4, 1
   );
   @SerializedName("detectedUuids")
   private final Set<UUID> detectedUuids = new HashSet<>();
   @SerializedName("lastHeldItems")
   private final Map<UUID, String> lastHeldItems = new HashMap<>();
   private static final long DEFAULT_LONG_VALUE = -6594822240334770272L;

   public GodItemDetector() {
      super(Category.Misc, "GodItemDetector", "防止被黑客偷袭，，，");
   }

   public static GodItemDetector getGodItemDetector() {
      if (Southside.INSTANCE != null && Southside.INSTANCE.getModuleManager() != null) {
         try {
            return Southside.INSTANCE.getModuleManager().getModule(GodItemDetector.class);
         } catch (NoSuchElementException e) {
            return null;
         }
      } else {
         return null;
      }
   }

   public static boolean isValidTarget(Entity entity) {
      GodItemDetector godItemDetector = getGodItemDetector();
      return godItemDetector != null && godItemDetector.isEnabled() && eGappleHolders.contains(entity);
   }

   @EventTarget
   public void onState(StateEvent stateEvent) {
      if (mc2.world != null && mc2.player != null) {
         for (Entity entity : mc2.world.getEntities()) {
            if (entity instanceof LivingEntity living && living != mc2.player && !(entity instanceof FakePlayerEntity) && EntityTargetUtils.isValidTarget(entity)) {
               UUID uuid = living.getUuid();
               ItemStack mainHandStack = living.getMainHandStack();
               ItemStack offHandStack = living.getOffHandStack();
               String text = formatText(mainHandStack) + "|" + formatText(offHandStack);
               if (!text.equals(this.lastHeldItems.get(uuid))) {
                  this.detectedUuids.remove(uuid);
                  this.lastHeldItems.put(uuid, text);
               }

               this.checkAndMarkItem(living, mainHandStack, false);
               this.checkAndMarkItem(living, offHandStack, true);
            }
         }
      }
   }

   private static String formatText(ItemStack stack) {
      return stack.isEmpty() ? "" : stack.getItem().toString() + stack.getComponents();
   }

   private static String formatText(Entity entity) {
      return entity instanceof PlayerEntity player && player.getGameProfile() != null ? player.getGameProfile().getName() : entity.getDisplayName().getString();
   }

   private void checkAndMarkItem(LivingEntity living, ItemStack stack, boolean flag) {
      if (!stack.isEmpty() && !this.detectedUuids.contains(living.getUuid())) {
         Set set;
         String text;
         if (stack.isOf(Items.GOLDEN_AXE) && stack.getComponents().toString().contains("一刀999!")) {
            set = notifiedPlayers;
            text = "GodAxe Detected!";
         } else if (stack.isOf(Items.MACE)) {
            set = godAxeHolders;
            text = "Mace Detected!";
         } else if (stack.isOf(Items.ENCHANTED_GOLDEN_APPLE)) {
            set = endCrystalHolders;
            text = "AppleCare+ Detected!";
         } else if (stack.isOf(Items.END_CRYSTAL)) {
            set = maceHolders;
            text = "EndCrystal Detected!";
         } else {
            if (!stack.isOf(Items.SLIME_BALL) || stack.contains(DataComponentTypes.CUSTOM_NAME)) {
               return;
            }

            set = eGappleHolders;
            text = "KBBall Detected!";
         }

         set.add(living);
         this.detectedUuids.add(living.getUuid());
         String text2 = formatText(living);
         String string = stack.getName().getString();
         String text3 = flag ? " on offhand" : "";
         String text4 = string;
         applyText(text, text2 + " has " + text4 + text3, Notification.iconUtil4);
      }
   }

   private static void applyText(String text, String text2, OneConfigIcon oneConfigIcon) {
      if (mc2.world != null || mc2.currentScreen instanceof OneConfigScreen) {
         EaseInOutCubicAnimation easeInOutCubicAnimation = new EaseInOutCubicAnimation((int)DEFAULT_LONG_VALUE, 0.0F, 1.0F, false);
         Notifications.INSTANCE.send(text, text2, oneConfigIcon, 4000.0F, () -> Notification.computeFloat(easeInOutCubicAnimation));
      }
   }

   @EventTarget
   public void onDirection(DirectionEvent directionEvent) {
      if (directionEvent.getDirection() == TransferOrigin.RECEIVE
         && mc2.world != null
         && directionEvent.getPacket() instanceof EntityAnimationS2CPacket entityAnimationS2CPacket
         && entityAnimationS2CPacket.getAnimationId() == 0) {
         Entity entityById = mc2.world.getEntityById(entityAnimationS2CPacket.getEntityId());
         if (entityById != null && notifiedPlayers.remove(entityById)) {
            this.detectedUuids.remove(entityById.getUuid());
            short s = -895;
            this.lastHeldItems.remove(entityById.getUuid());
            short s2 = 20478;
            applyText("GodAxe Consumed!", formatText(entityById) + "'s GodAxe has been used up!", Notification.iconUtil3);
         }
      }
   }

   private void performActionAlt() {
      notifiedPlayers.clear();
      godAxeHolders.clear();
      maceHolders.clear();
      endCrystalHolders.clear();
      eGappleHolders.clear();
      this.detectedUuids.clear();
      this.lastHeldItems.clear();
   }

   @EventTarget
   public void onClientWorld(ClientWorldEvent clientWorldEvent) {
      this.performActionAlt();
   }

   @Override
   public void onDisable() {
      this.performActionAlt();
      super.onDisable();
   }

   public enum Mode {
      DISABLED,
      NAMETAG,
      DETECTOR,
      BOTH;


      // $VF: synthetic method
      private static GodItemDetector.Mode[] createValues() {
         return new GodItemDetector.Mode[]{DISABLED, NAMETAG, DETECTOR, BOTH};
      }


   }

   public enum Mode2 {
      NAMETAG,
      DETECTOR;


      // $VF: synthetic method
      private static GodItemDetector.Mode2[] getMode2Array() {
         return new GodItemDetector.Mode2[]{NAMETAG, DETECTOR};
      }


   }
}
