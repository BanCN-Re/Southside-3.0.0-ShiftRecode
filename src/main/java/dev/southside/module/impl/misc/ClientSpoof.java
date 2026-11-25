package dev.southside.module.impl.misc;

import com.google.gson.annotations.SerializedName;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.utils.ModeValue;

public class ClientSpoof extends Module {
   @SerializedName("spoofMode")
   public ModeValue spoofMode = new ModeValue("Spoof Mode", "Forge", "Vanilla", "Forge");

   public String getClient() {
      String displayTextEx = this.spoofMode.getDisplayTextEx();

      return switch (displayTextEx) {
         case "Vanilla" -> "vanilla";
         case "Forge" -> "forge";
         default -> "fabric";
      };
   }

   public ClientSpoof() {
      super(Category.Misc, "ClientSpoof", "Spoof your client");
   }
}
