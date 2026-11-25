package dev.southside.module.impl.misc;

import com.google.gson.annotations.SerializedName;
import dev.southside.Southside;
import dev.southside.anticheat.check.impl.ScaffoldBatchCheck;
import dev.southside.anticheat.player.PlayerData;
import dev.southside.event.EventTarget;
import dev.southside.event.events.ClientPlayerEntityEvent;
import dev.southside.event.events.ClientWorldEvent;
import dev.southside.event.events.DirectionEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.values.BooleanValue;
import dev.southside.utils.TowerCheck;
import dev.southside.utils.BlinkStutterCheck;
import dev.southside.utils.FastWebCheck;
import dev.southside.utils.RotationSnapCheck;
import dev.southside.utils.IHackerTickCheck;
import dev.southside.utils.IHackerPlayerCheck;
import dev.southside.utils.IHackerCheck;
import dev.southside.utils.IText;
import dev.southside.utils.NumberValue;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

public class HackerDetector extends Module {
   @SerializedName("log")
   public final BooleanValue log = new BooleanValue("Log", false);
   @SerializedName("notification")
   public final BooleanValue notification = new BooleanValue("Notification", true);
   @SerializedName("persist")
   public final BooleanValue persist = new BooleanValue("Persist", false);
   @SerializedName("flagVL")
   public final NumberValue flagVL = new NumberValue("Flag VL", 10.0, 1.0, 100.0);
   @SerializedName("fastWebCheck")
   public final BooleanValue fastWebCheck = new BooleanValue("FastWeb Check", true);
   @SerializedName("blinkCheck")
   public final BooleanValue blinkCheck = new BooleanValue("Blink Check", true);
   @SerializedName("scaffoldCheck")
   public final BooleanValue scaffoldCheck = new BooleanValue("Scaffold Check", true);
   @SerializedName("testSelf")
   public final BooleanValue testSelf = new BooleanValue("Test (Self)", false);
   @SerializedName("violationDecay")
   public final NumberValue violationDecay = new NumberValue("VL Decay/s", 0.5, 0.0, 5.0, 0.1);
   @SerializedName("profiles")
   private final Map<UUID, PlayerData> profiles = new ConcurrentHashMap<>();
   @SerializedName("checks")
   private volatile List<IHackerCheck> checks = List.of();
   @SerializedName("savedViolationLevels")
   private Map<UUID, Float> savedViolationLevels = Map.of();
   private static final long DEFAULT_LONG_VALUE = 1212123973765038090L;

   public HackerDetector() {
      super(Category.Misc, "HackerDetector", "Detects hackers");
   }

   @Override
   public void onEnable() {
      this.checks = List.of(new FastWebCheck(), new BlinkStutterCheck(), new ScaffoldBatchCheck(), new RotationSnapCheck(), new TowerCheck());
      this.savedViolationLevels = this.persist.getValue() ? buildMap() : Map.of();
      super.onEnable();
   }

   public PlayerData getPlayerData(UUID uuid) {
      return this.profiles.get(uuid);
   }

   public boolean isValidTarget(Entity entity) {
      PlayerData data = entity == null ? null : this.profiles.get(entity.getUuid());
      return data != null && data.getViolationLevel() > ((Number)this.flagVL.getValue()).intValue();
   }

   private boolean isValid(IHackerCheck ihackercheck) {
      if (ihackercheck instanceof FastWebCheck) {
         return (Boolean)this.fastWebCheck.getValue();
      } else {
         return ihackercheck instanceof BlinkStutterCheck
            ? (Boolean)this.blinkCheck.getValue()
            : (ihackercheck instanceof ScaffoldBatchCheck || ihackercheck instanceof RotationSnapCheck || ihackercheck instanceof TowerCheck)
               && (Boolean)this.scaffoldCheck.getValue();
      }
   }

   @EventTarget
   public void onDirection(DirectionEvent directionEvent) {
      for (IHackerCheck ihackercheck : this.checks) {
         if (ihackercheck instanceof IHackerTickCheck ihackertickcheck && this.isValid(ihackercheck)) {
            ihackertickcheck.performAction();
         }
      }
   }

   @EventTarget
   public void onClientPlayerEntity(ClientPlayerEntityEvent clientPlayerEntityEvent) {
      if (mc2.player != null && mc2.world != null && mc2.player.age > (int)DEFAULT_LONG_VALUE) {
         float f = ((Number)this.violationDecay.getValue()).floatValue() / 20.0F;
         float f2 = ((Number)this.flagVL.getValue()).intValue();

         for (Entity entity : mc2.world.getEntities()) {
            if (entity instanceof PlayerEntity player && (Boolean)this.testSelf.getValue() == (player == mc2.player)) {
               PlayerData data = this.profiles.computeIfAbsent(player.getUuid(), uuid -> {
                  PlayerData data2 = new PlayerData(player);
                  if ((Boolean)this.persist.getValue()) {
                     data2.setViolationLevel(this.savedViolationLevels.getOrDefault(uuid, 0.0F));
                  }

                  return data2;
               });
               if (f > 0.0F && data.getViolationLevel() > 0.0F) {
                  data.setViolationLevel(Math.max(0.0F, data.getViolationLevel() - f));
               }

               for (IHackerCheck ihackercheck : this.checks) {
                  if (ihackercheck instanceof IHackerPlayerCheck ihackerplayercheck && this.isValid(ihackercheck)) {
                     ihackerplayercheck.checkPlayer(data);
                  }
               }

               if (data.getViolationLevel() > f2 && data.isFlagAnnouncementReady()) {
                  IText.performActionEx(Text.literal(player.getName().getString() + " flagged as hacker (VL: " + String.format("%.1f", data.getViolationLevel()) + ")"));
                  data.setFlagAnnouncementReady(false);
               }

               if (data.getViolationLevel() <= f2 * 0.5F) {
                  data.setFlagAnnouncementReady(true);
               }
            }
         }
      }
   }

   private static Path getPath() {
      return Southside.FOLDER.toPath().resolve("hackers.dat");
   }

   private static Map<UUID, Float> buildMap() {
      try (ObjectInputStream objectInputStream = new ObjectInputStream(Files.newInputStream(getPath()))) {
         if (!(objectInputStream.readObject() instanceof Map map)) {
            return Map.of();
         } else {
            HashMap map2 = new HashMap();

            for (Object obj : map.entrySet()) {
               Entry entry = (Entry)obj;
               if (entry.getKey() instanceof UUID uuid && entry.getValue() instanceof Float val) {
                  map2.put(uuid, val);
               }
            }

            return map2;
         }
      } catch (ClassNotFoundException | IOException e) {
         return Map.of();
      }
   }

   private void performActionAlt() {
      if ((Boolean)this.persist.getValue() && !this.profiles.isEmpty()) {
         HashMap map = new HashMap();
         this.profiles.forEach((uuid, data) -> map.put(uuid, data.getViolationLevel()));
         this.savedViolationLevels = map;

         try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(Files.newOutputStream(getPath()))) {
            objectOutputStream.writeObject(map);
         } catch (IOException e) {
            Southside.LOGGER.warn("Failed to save hackers.dat", e);
         }
      }
   }

   @EventTarget
   public void onClientWorld(ClientWorldEvent clientWorldEvent) {
      if (mc2.world != null) {
         this.performActionAlt();
         this.profiles.clear();
      }
   }

   @Override
   public void onDisable() {
      this.performActionAlt();
      this.checks = List.of();
      this.profiles.clear();
      super.onDisable();
   }
}
