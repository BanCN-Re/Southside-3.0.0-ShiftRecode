package dev.southside.utils;

import dev.southside.Southside;
import dev.southside.auth.IrcState;
import dev.southside.auth.IrcUser;
import dev.southside.module.impl.client.Targets;
import dev.southside.module.impl.misc.AntiBot;
import dev.southside.module.impl.misc.Teams;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;

public class EntityTargetUtils {
   public static int computeInt(Entity entity, Entity entity2) {
      return (int)(entity.distanceTo(MinecraftClient.getInstance().player) - entity2.distanceTo(MinecraftClient.getInstance().player));
   }

   public static List<Entity> collectItems(float f) {
      return collectItems(-1.0F / 0.0F, f);
   }

   public static List<Entity> collectItems(float x, float y) {
      ArrayList<Entity> list = new ArrayList<>();

      for (Entity entity : MinecraftClient.getInstance().world.getEntities()) {
         if (isLivingEntity(entity)
            && entity.distanceTo(MinecraftClient.getInstance().player) >= x
            && entity.distanceTo(MinecraftClient.getInstance().player) <= y) {
            list.add(entity);
         }
      }

      list.sort(EntityTargetUtils::computeInt);
      return list;
   }

   public static boolean isLivingEntity(Entity entity) {
      if (entity instanceof LivingEntity && entity != MinecraftClient.getInstance().player && !(entity instanceof FakePlayerEntity) && isValidTarget(entity)) {
         if (entity instanceof PlayerEntity player) {
            if (Teams.isValidTarget(player)) {
               return false;
            }

            for (IrcUser ircUser : IrcState.ircUsersSet) {
               if (ircUser.getUuids().contains(player.getUuid().toString())) {
                  return false;
               }
            }

            return Southside.INSTANCE.getFriendManager().friends.contains(player.getName().getString()) ? false : !AntiBot.isBot(player);
         } else {
            return true;
         }
      } else {
         return false;
      }
   }

   public static boolean isValidTarget(Entity entity) {
      Targets targets = Targets.INSTANCE;
      if (entity instanceof PlayerEntity) {
         return (Boolean)targets.players.getValue();
      } else if (entity instanceof MobEntity && !(entity instanceof VillagerEntity) && !(entity instanceof AnimalEntity)) {
         return (Boolean)targets.mobs.getValue();
      } else if (entity instanceof VillagerEntity) {
         return (Boolean)targets.villagers.getValue();
      } else {
         return entity instanceof AnimalEntity ? (Boolean)targets.animals.getValue() : false;
      }
   }
}
