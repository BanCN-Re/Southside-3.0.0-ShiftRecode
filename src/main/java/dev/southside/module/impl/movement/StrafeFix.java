package dev.southside.module.impl.movement;

import com.google.gson.annotations.SerializedName;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.values.BooleanValue;
import dev.southside.utils.ModeValue;
import dev.southside.utils.rotation.MoveFixMode;

public class StrafeFix extends Module {
   @SerializedName("mode")
   public final ModeValue mode = new ModeValue("Mode", "Silent", "Silent", "Strict", "Off");
   @SerializedName("targetStrafe")
   public final BooleanValue targetStrafe = new BooleanValue("Target Strafe", false);

   public StrafeFix() {
      super(Category.Movement, "StrafeFix", "MOVEFIX ", true);
   }

   public MoveFixMode getMoveFixMode() {
      String displayTextEx = this.mode.getDisplayTextEx();
      if (displayTextEx.equals("Silent") && (Boolean)this.targetStrafe.getValue() && mc2.options != null && mc2.options.jumpKey.isPressed()) {
         return MoveFixMode.Strict;
      }

      return switch (displayTextEx) {
         case "Silent" -> MoveFixMode.Silent;
         case "Strict" -> MoveFixMode.Strict;
         default -> MoveFixMode.Off;
      };
   }
}
