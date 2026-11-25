package dev.southside.module.impl.misc;

import cc.polyfrost.oneconfig.utils.Notifications;
import com.google.gson.annotations.SerializedName;
import dev.southside.Southside;
import dev.southside.event.EventTarget;
import dev.southside.event.events.ClientWorldEvent;
import dev.southside.event.events.StateEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.impl.client.Notification;
import dev.southside.utils.EaseInOutCubicAnimation;
import dev.southside.utils.EntityTargetUtils;
import dev.southside.utils.FakePlayerEntity;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;

public class MurderFinder extends Module {
   private static final Set<Entity> notifiedPlayers = new HashSet<>();
   @SerializedName("detectedUuids")
   private final Set<UUID> detectedUuids = new HashSet<>();
   @SerializedName("lastHeldItems")
   private final Map<UUID, String> lastHeldItems = new HashMap<>();
   private static final long DEFAULT_LONG_VALUE = -3020811620679938144L;

   public MurderFinder() {
      super(Category.Misc, "MurderFinder", "Finds players who are murdering you.");
   }

   public static boolean isMurder(Entity entity) {
      return Southside.INSTANCE.getModuleManager().getModule(MurderFinder.class).isEnabled() && notifiedPlayers.contains(entity);
   }

   @EventTarget
   public void onState(StateEvent stateEvent) {
      if (mc2.world != null && mc2.player != null) {
         for (Entity entity : mc2.world.getEntities()) {
            if (entity instanceof LivingEntity living && living != mc2.player && !(entity instanceof FakePlayerEntity) && EntityTargetUtils.isValidTarget(entity)) {
               UUID uuid = living.getUuid();
               ItemStack mainHandStack = living.getMainHandStack();
               ItemStack offHandStack = living.getOffHandStack();
               String text = isMurder(mainHandStack) + "|" + isMurder(offHandStack);
               if (!text.equals(this.lastHeldItems.get(uuid))) {
                  this.detectedUuids.remove(uuid);
                  this.lastHeldItems.put(uuid, text);
               }

               this.updateEntity(living, mainHandStack);
               this.updateEntity(living, offHandStack);
            }
         }
      }
   }

   private static String isMurder(ItemStack stack) {
      return stack.isEmpty() ? "" : stack.getItem().toString() + stack.getComponents();
   }

   private void updateEntity(LivingEntity living, ItemStack stack) {
      if (!stack.isEmpty() && !this.detectedUuids.contains(living.getUuid()) && stack.getItem() instanceof SwordItem) {
         notifiedPlayers.add(living);
         this.detectedUuids.add(living.getUuid());
         String text = living instanceof PlayerEntity player && player.getGameProfile() != null ? player.getGameProfile().getName() : living.getDisplayName().getString();
         EaseInOutCubicAnimation easeInOutCubicAnimation = new EaseInOutCubicAnimation((int)DEFAULT_LONG_VALUE, 0.0F, 1.0F, false);
         Notifications.INSTANCE.send("Murder Finder!", text + " is Murder", Notification.iconUtil4, 4000.0F, () -> Notification.computeFloat(easeInOutCubicAnimation));
      }
   }

   private void clear() {
      notifiedPlayers.clear();
      this.detectedUuids.clear();
      this.lastHeldItems.clear();
   }

   @Override
   public void onDisable() {
      this.clear();
      super.onDisable();
   }

   @EventTarget
   public void onClientWorld(ClientWorldEvent clientWorldEvent) {
      this.clear();
   }
}
