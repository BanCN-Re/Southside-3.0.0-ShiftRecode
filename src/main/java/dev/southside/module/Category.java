package dev.southside.module;

import com.google.gson.annotations.SerializedName;

public enum Category {
   Combat(Category.Mode.COMBAT),
   Movement(Category.Mode.MOVEMENT),
   World(Category.Mode.WORLD),
   Player(Category.Mode.PLAYER),
   Misc(Category.Mode.MISC),
   Render(Category.Mode.RENDER),
   Client(Category.Mode.CLIENT);

   @SerializedName("moduleCategory")
   public final Category.Mode moduleCategory;
   private static boolean active;

   Category(Category.Mode mode) {
      this.moduleCategory = mode;
   }

   // $VF: synthetic method
   private static Category[] getCategoryArray() {
      return new Category[]{Combat, Movement, World, Player, Misc, Render, Client};
   }


   public static void setActive(boolean flag) {
      active = flag;
   }


   public enum Mode {
      COMBAT(true, false),
      MOVEMENT(true, false),
      RENDER(true, false),
      WORLD(true, false),
      PLAYER(true, false),
      MISC(true, false),
      HUD(true, true),
      CONFIGS(false, true),
      LUA(true, true),
      CLIENT(true, true);

      @SerializedName("showInSidebar")
      public final boolean showInSidebar;
      @SerializedName("isHudCategory")
      public final boolean isHudCategory;

      Mode(boolean flag, boolean flag2) {
         this.showInSidebar = flag;
         this.isHudCategory = flag2;
      }

      // $VF: synthetic method
      private static Category.Mode[] createValues() {
         return new Category.Mode[]{COMBAT, MOVEMENT, RENDER, WORLD, PLAYER, MISC, HUD, CONFIGS, LUA, CLIENT};
      }


   }
}
