package dev.southside.module.impl.misc;

import com.google.gson.annotations.SerializedName;
import dev.southside.event.EventTarget;
import dev.southside.event.events.DirectionEvent;
import dev.southside.event.events.PlayerEntityEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.utils.IText;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.util.math.BlockPos;

public class MatchingBypass extends Module {
   private static final Pattern pattern = Pattern.compile("\\(\\s*(\\d+)\\s*/\\s*(\\d+)\\s*\\)");
   private static final long DEFAULT_LONG_VALUE = 5000L;
   @SerializedName("currentGameCount")
   private int currentGameCount = -1;
   @SerializedName("maxGameCount")
   private int maxGameCount = -1;
   @SerializedName("joinMessageTime")
   private long joinMessageTime = -1L;
   @SerializedName("pendingBypass")
   private boolean pendingBypass;
   private static final long DEFAULT_LONG_DATA = -719822951007911929L;

   public MatchingBypass() {
      super(Category.Misc, "MatchingBypass", false, false);
   }

   private boolean isInGlassCage() {
      if (mc2.world != null && mc2.player != null) {
         BlockPos blockPos = mc2.player.getBlockPos();
         return this.contains(blockPos.down()) || this.contains(blockPos.up());
      } else {
         return false;
      }
   }

   private boolean contains(BlockPos pos) {
      return mc2.world.getBlockState(pos).getBlock().toString().toLowerCase(Locale.ROOT).contains("glass");
   }

   private void applyText(String text) {
      if (text.contains("加入了游戏")) {
         Matcher matcher = pattern.matcher(text);
         if (matcher.find()) {
            int n;
            int n2;
            try {
               n = Integer.parseInt(matcher.group(1));
               n2 = Integer.parseInt(matcher.group(2));
            } catch (NumberFormatException e) {
               return;
            }

            this.currentGameCount = n;
            this.maxGameCount = n2;
            this.joinMessageTime = System.currentTimeMillis();
            this.pendingBypass = true;
         }
      }
   }

   @EventTarget
   public void onDirection(DirectionEvent directionEvent) {
      if (directionEvent.getPacket() instanceof ChatMessageS2CPacket chatMessageS2CPacket) {
         this.applyText(chatMessageS2CPacket.body().content());
      } else if (directionEvent.getPacket() instanceof GameMessageS2CPacket gameMessageS2CPacket) {
         this.applyText(gameMessageS2CPacket.content().getString());
      }
   }

   @EventTarget
   public void onPlayerEntity(PlayerEntityEvent playerEntityEvent) {
      if (this.pendingBypass && System.currentTimeMillis() - this.joinMessageTime >= 5000L) {
         if (mc2.player != null
            && mc2.player.networkHandler != null
            && this.isInGlassCage()
            && this.currentGameCount < this.maxGameCount
            && this.currentGameCount <= (int)DEFAULT_LONG_DATA) {
            mc2.player.networkHandler.sendCommand("play swrsolo");
            IText.applyText("bypass");
         }

         this.pendingBypass = false;
         this.currentGameCount = -1;
         this.maxGameCount = -1;
      }
   }
}
