package dev.southside.module.impl.render;

import com.google.gson.annotations.SerializedName;
import dev.southside.event.EventTarget;
import dev.southside.event.events.DirectionEvent;
import dev.southside.event.events.StateEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.values.BooleanValue;
import dev.southside.utils.NumberValue;
import dev.southside.utils.ModeValue;
import dev.southside.utils.WorldTimeController;
import java.util.StringJoiner;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;

public class WorldManager extends Module {
   @SerializedName("time")
   public final BooleanValue time = new BooleanValue("Time", false);
   @SerializedName("timeMode")
   public final ModeValue timeMode = new ModeValue("Time Mode", "Pinned", "Pinned", "Fast");
   @SerializedName("timeValue")
   public final NumberValue timeValue;
   @SerializedName("timeSpeed")
   public final NumberValue timeSpeed;
   @SerializedName("weather")
   public final BooleanValue weather;
   @SerializedName("weatherMode")
   public final ModeValue weatherMode;
   public static boolean activeValue;
   public static boolean activeData;
   @SerializedName("timeOverride")
   private final WorldTimeController timeOverride;
   @SerializedName("weatherWorld")
   private ClientWorld weatherWorld;
   @SerializedName("serverRainGradient")
   private float serverRainGradient;
   @SerializedName("serverThunderGradient")
   private float serverThunderGradient;
   @SerializedName("serverRaining")
   private boolean serverRaining;
   @SerializedName("weatherOverridden")
   private boolean weatherOverridden;
   private static final long DEFAULT_LONG_VALUE = -5191111857238179816L;
   private static final long m = 1000L;

   public WorldManager() {
      super(Category.Render, "WorldManager");
      this.timeValue = new NumberValue("Time Value", 1, 0, (int)DEFAULT_LONG_VALUE, 1);
      this.timeSpeed = new NumberValue("Time Speed", 2.0, 0.5, 20.0, 0.5);
      this.weather = new BooleanValue("Weather", false);
      this.weatherMode = new ModeValue("Weather Mode", "Clear", "Clear", "Rain", "Thunder", "Snow");
      this.timeOverride = new WorldTimeController();
   }

   @Override
   public void postInit() {
      super.postInit();
      this.hideIf(this.timeMode.getLabel(), () -> !(Boolean)this.time.getValue());
      this.hideIf(this.timeValue.getLabel(), () -> !(Boolean)this.time.getValue() || !this.timeMode.matches("Pinned"));
      this.hideIf(this.timeSpeed.getLabel(), () -> !(Boolean)this.time.getValue() || !this.timeMode.matches("Fast"));
      this.hideIf(this.weatherMode.getLabel(), () -> !(Boolean)this.weather.getValue());
   }

   @Override
   public String getSuffix() {
      StringJoiner stringJoiner = new StringJoiner(" ");
      if ((Boolean)this.time.getValue()) {
         stringJoiner.add("Time:" + this.timeMode.getDisplayTextEx());
      }

      if ((Boolean)this.weather.getValue()) {
         stringJoiner.add("Weather:" + this.weatherMode.getDisplayTextEx());
      }

      return stringJoiner.toString();
   }

   @Override
   public void onEnable() {
      this.timeOverride.performAction(mc2.world);
      this.performActionEx(mc2.world);
   }

   @Override
   public void onDisable() {
      this.timeOverride.performActionAlt(mc2.world);
      this.performActionAlt();
      this.weatherWorld = null;
   }

   @EventTarget
   public void onDirection(DirectionEvent directionEvent) {
      if (!directionEvent.isValid()) {
         ClientWorld world = mc2.world;
         if ((Boolean)this.time.getValue() && directionEvent.getPacket() instanceof WorldTimeUpdateS2CPacket worldTimeUpdateS2CPacket) {
            directionEvent.setCancelled(true);
            mc2.execute(() -> {
               if (mc2.world == world) {
                  this.timeOverride.update(world, worldTimeUpdateS2CPacket);
                  if (!this.isEnabled() || !(Boolean)this.time.getValue()) {
                     this.timeOverride.performActionAlt(world);
                  }
               }
            });
         } else if ((Boolean)this.weather.getValue() && directionEvent.getPacket() instanceof GameStateChangeS2CPacket gameStateChangeS2CPacket && this.isValid(gameStateChangeS2CPacket)) {
            directionEvent.setCancelled(true);
            mc2.execute(() -> {
               if (world != null && mc2.world == world) {
                  if (this.weatherWorld != world) {
                     this.performActionEx(world);
                  }

                  if (gameStateChangeS2CPacket.getReason() == GameStateChangeS2CPacket.RAIN_STARTED) {
                     this.serverRaining = true;
                  } else if (gameStateChangeS2CPacket.getReason() == GameStateChangeS2CPacket.RAIN_STOPPED) {
                     this.serverRaining = false;
                  } else if (gameStateChangeS2CPacket.getReason() == GameStateChangeS2CPacket.RAIN_GRADIENT_CHANGED) {
                     this.serverRainGradient = gameStateChangeS2CPacket.getValue();
                  } else if (gameStateChangeS2CPacket.getReason() == GameStateChangeS2CPacket.THUNDER_GRADIENT_CHANGED) {
                     this.serverThunderGradient = gameStateChangeS2CPacket.getValue();
                  }

                  if (!this.weatherOverridden || !this.isEnabled() || !(Boolean)this.weather.getValue()) {
                     this.performActionAlt(world);
                  }

                  if (!this.isEnabled()) {
                     this.weatherWorld = null;
                  }
               }
            });
         }
      }
   }

   private boolean isValid(GameStateChangeS2CPacket gameStateChangeS2CPacket) {
      return gameStateChangeS2CPacket.getReason() == GameStateChangeS2CPacket.RAIN_STARTED
         || gameStateChangeS2CPacket.getReason() == GameStateChangeS2CPacket.RAIN_STOPPED
         || gameStateChangeS2CPacket.getReason() == GameStateChangeS2CPacket.RAIN_GRADIENT_CHANGED
         || gameStateChangeS2CPacket.getReason() == GameStateChangeS2CPacket.THUNDER_GRADIENT_CHANGED;
   }

   @EventTarget
   public void onState(StateEvent stateEvent) {
      ClientWorld world = mc2.world;
      if (this.weatherWorld != world) {
         this.performActionEx(world);
      }

      if (world == null) {
         this.timeOverride.performAction(null);
      } else {
         if (!(Boolean)this.time.getValue()) {
            this.timeOverride.performActionAlt(world);
         } else if (this.timeMode.matches("Pinned")) {
            this.timeOverride.performAction(world, ((Number)this.timeValue.getValue()).longValue() * m);
         } else {
            this.timeOverride.performAction(world, ((Number)this.timeSpeed.getValue()).doubleValue());
         }

         if ((Boolean)this.weather.getValue()) {
            this.performAction(world);
         } else {
            this.performActionAlt();
         }
      }
   }

   private void performAction(ClientWorld world) {
      if (!this.weatherOverridden) {
         this.performActionEx(world);
      }

      this.weatherOverridden = true;
      activeValue = this.weatherMode.matches("Snow");
      activeData = this.weatherMode.matches("Rain") || this.weatherMode.matches("Thunder");
      boolean flag = !this.weatherMode.matches("Clear");
      world.getLevelProperties().setRaining(flag);
      world.setRainGradient(flag ? 1.0F : 0.0F);
      world.setThunderGradient(this.weatherMode.matches("Thunder") ? 1.0F : 0.0F);
   }

   private void performActionEx(ClientWorld world) {
      this.weatherWorld = world;
      this.weatherOverridden = false;
      activeValue = false;
      activeData = false;
      if (world != null) {
         this.serverRainGradient = world.getRainGradient(1.0F);
         this.serverThunderGradient = world.getThunderGradient(1.0F);
         this.serverRaining = world.getLevelProperties().isRaining();
      }
   }

   private void performActionAlt() {
      activeValue = false;
      activeData = false;
      if (this.weatherOverridden && this.weatherWorld != null && this.weatherWorld == mc2.world) {
         this.performActionAlt(this.weatherWorld);
      }

      this.weatherOverridden = false;
   }

   private void performActionAlt(ClientWorld world) {
      world.getLevelProperties().setRaining(this.serverRaining);
      world.setRainGradient(this.serverRainGradient);
      world.setThunderGradient(this.serverThunderGradient);
   }
}
