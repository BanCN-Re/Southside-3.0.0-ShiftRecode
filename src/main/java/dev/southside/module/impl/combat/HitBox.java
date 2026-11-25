package dev.southside.module.impl.combat;

import com.google.gson.annotations.SerializedName;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.utils.NumberValue;
import net.minecraft.util.math.Box;

public class HitBox extends Module {
   @SerializedName("horizontalExpansion")
   public NumberValue horizontalExpansion = new NumberValue("XZExpand", 1.0, 0.0, 5.0, 0.01);
   @SerializedName("verticalExpansion")
   public NumberValue verticalExpansion = new NumberValue("YExpand", 0.0, 0.0, 5.0, 0.01);

   public HitBox() {
      super(Category.Combat, "HitBox", "HitBox");
   }

   public Box getBox(Box box) {
      double d = ((Number)this.horizontalExpansion.getValue()).floatValue() / 2.0F;
      double d2 = ((Number)this.verticalExpansion.getValue()).floatValue() / 2.0F;
      return new Box(box.minX - d, box.minY - d2, box.minZ - d, box.maxX + d, box.maxY + d2, box.maxZ + d);
   }
}
