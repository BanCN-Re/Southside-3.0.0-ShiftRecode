package dev.southside.utils;

import cc.polyfrost.oneconfig.internal.config.OneConfigConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ConfigCore {
   public static List<ModData> modDatas = new ArrayList<>();

   public static void performAction() {
      for (ModData modData : modDatas) {
         modData.config.saveToJson();
      }
   }

   public static void performActionEx() {
      for (ModData modData : modDatas) {
         if (modData.config.isValid()) {
            modData.config.reInitialize();
         }
      }

      HudCore.performAction();
      KeyBindHandler.INSTANCE.performAction();
   }

   public static void performActionAlt() {
      for (ModData modData : modDatas) {
         if (modData.config.isValid()) {
            modData.config.resetToDefaults();
         }
      }

      HudCore.performAction();
      KeyBindHandler.INSTANCE.performAction();
   }

   public static void performActionImpl() {
      ArrayList<ModData> list = new ArrayList<>(modDatas);
      modDatas = list.stream().filter(modData -> OneConfigConfig.favoriteMods.contains(modData.name)).sorted().collect(Collectors.toList());
      list.removeAll(modDatas);
      modDatas.addAll(list.stream().sorted().collect(Collectors.toList()));
      OneConfigConfig.getInstance().saveToJson();
   }

   static {
      long l = 1890688126212972379L;
      long l2 = 1890688126212972357L ^ l;
      byte b = -1;
      long l3 = l2;
      Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(ConfigCore::performAction, 0L, l3, TimeUnit.SECONDS);
   }
}
