package dev.southside.module.impl.client;

import com.google.gson.annotations.SerializedName;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.values.BooleanValue;

public class Targets extends Module {
   public static Targets INSTANCE;
   @SerializedName("players")
   public final BooleanValue players = new BooleanValue("Players", true);
   @SerializedName("mobs")
   public final BooleanValue mobs = new BooleanValue("Mobs", false);
   @SerializedName("animals")
   public final BooleanValue animals = new BooleanValue("Animals", false);
   @SerializedName("villagers")
   public final BooleanValue villagers = new BooleanValue("Villagers", false);

   @Override
   public void onEnable() {
      this.setEnabled(false);
   }

   public Targets() {
      super(Category.Client, "Targets", "目标");
      INSTANCE = this;
   }
}
