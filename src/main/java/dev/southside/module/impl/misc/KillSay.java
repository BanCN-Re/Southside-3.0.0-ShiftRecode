package dev.southside.module.impl.misc;

import com.google.gson.annotations.SerializedName;
import dev.southside.Southside;
import dev.southside.event.EventTarget;
import dev.southside.event.Stage;
import dev.southside.event.events.ClientWorldEvent;
import dev.southside.event.events.DirectionEvent;
import dev.southside.event.events.EntityEvent;
import dev.southside.event.events.EntityRemoveEvent;
import dev.southside.event.events.StateEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;

public class KillSay extends Module {
   private static final Path path = Southside.FOLDER.toPath().resolve("killsay.txt");
   private static final Pattern RANDOM_PATTERN = Pattern.compile("\\{random(?::(\\d+))?}");
   private static final String DEFAULT_TEXT = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
   private static final long DEFAULT_LONG_VALUE = 15000L;
   private static final List<String> messages = List.of(
      "# KillSay新建文本文档。{name} 是击杀玩家名称占位符, {random} 随机5个字符, {random:10} 随机10个字符",
      "Ｌ {name}!",
      "ＦＷ {name}",
      "{name} 不要忘记每天朗读《欣欣茎》！",
      "{name} 一破！卧龙出山 你已被 southside 客户端击毙",
      "{name} 双连！一战成名 你已被 southside 客户端击毙",
      "{name} 三连！举世皆惊 你已被 southside 客户端击毙",
      "{name} 四连！天下无敌 你已被 southside 客户端击毙",
      "{name} 五连！诛天灭地 你已被 southside 客户端击毙"
   );
   @SerializedName("lastAttackTimes")
   private final Map<Integer, Long> lastAttackTimes = new ConcurrentHashMap<>();
   @SerializedName("random")
   private final Random random = new Random();
   @SerializedName("killMessages")
   private volatile List<String> killMessages = List.of();

   public KillSay() {
      super(Category.Misc, "KillSay", "击杀玩家后发送自定义消息");
      this.performActionAlt();
   }

   private void performActionAlt() {
      if (!Files.exists(path)) {
         try {
            Files.createDirectories(path.getParent());
            Files.write(path, messages, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
         } catch (IOException e) {
            Southside.LOGGER.warn("Failed to create killsay.txt", e);
         }
      }
   }

   @Override
   public void onEnable() {
      this.performActionImpl();
      this.lastAttackTimes.clear();
   }

   private void performActionImpl() {
      ArrayList list = new ArrayList();

      try {
         if (Files.exists(path)) {
            for (String text : Files.readAllLines(path, StandardCharsets.UTF_8)) {
               String text2 = text.trim();
               if (!text2.isEmpty() && !text2.startsWith("#")) {
                  list.add(text2);
               }
            }
         }
      } catch (IOException e) {
         Southside.LOGGER.warn("Failed to load killsay.txt", e);
      }

      this.killMessages = List.copyOf(list);
   }

   private String formatText(String text) {
      Matcher matcher = RANDOM_PATTERN.matcher(text);
      StringBuilder sb = new StringBuilder();

      while (matcher.find()) {
         int n = 5;
         if (matcher.group(1) != null) {
            try {
               n = Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
               n = 50;
            }
         }

         n = Math.clamp(n, 1, 50);
         StringBuilder sb2 = new StringBuilder(n);

         for (int i = 0; i < n; i++) {
            sb2.append(
               "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
                  .charAt(this.random.nextInt("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".length()))
            );
         }

         matcher.appendReplacement(sb, sb2.toString());
      }

      matcher.appendTail(sb);
      return sb.toString();
   }

   @EventTarget
   public void onEntity(EntityEvent entityEvent) {
      if (entityEvent.phase == Stage.POST && entityEvent.entity instanceof PlayerEntity && entityEvent.entity != mc2.player && !AntiBot.isBot(entityEvent.entity)) {
         this.lastAttackTimes.put(entityEvent.entity.getId(), System.currentTimeMillis());
      }
   }

   @EventTarget
   public void onDirection(DirectionEvent directionEvent) {
      if (mc2.world != null && mc2.player != null && directionEvent.getPacket() instanceof EntityStatusS2CPacket entityStatusS2CPacket && entityStatusS2CPacket.getStatus() == 3) {
         this.updateEntity(entityStatusS2CPacket.getEntity(mc2.world));
      }
   }

   @EventTarget
   public void onEntityEvent2(EntityRemoveEvent entityRemoveEvent) {
      this.updateEntity(entityRemoveEvent.entity);
   }

   private void updateEntity(Entity entity) {
      if (entity instanceof PlayerEntity player
         && mc2.world != null
         && mc2.player != null
         && player != mc2.player
         && !AntiBot.isBot(player)
         && this.lastAttackTimes.remove(player.getId()) != null) {
         List killMessages2 = this.killMessages;
         if (mc2.getNetworkHandler() != null && !killMessages2.isEmpty()) {
            String text = player.getGameProfile() != null ? player.getGameProfile().getName() : player.getName().getString();
            String text2 = ((String)killMessages2.get(this.random.nextInt(killMessages2.size()))).replace("{name}", text);
            mc2.getNetworkHandler().sendChatMessage(this.formatText(text2));
         }
      }
   }

   @EventTarget
   public void onState(StateEvent stateEvent) {
      if (stateEvent.getState() == Stage.POST && !this.lastAttackTimes.isEmpty()) {
         long now = System.currentTimeMillis();
         this.lastAttackTimes.entrySet().removeIf(entry -> now - entry.getValue() > 15000L);
      }
   }

   @EventTarget
   public void onClientWorld(ClientWorldEvent clientWorldEvent) {
      this.lastAttackTimes.clear();
   }

   @Override
   public void onDisable() {
      this.lastAttackTimes.clear();
   }
}
