package dev.southside.module.impl.render;

import com.google.gson.annotations.SerializedName;
import dev.southside.event.EventTarget;
import dev.southside.event.events.FovMultiplierEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.utils.NumberValue;

public class CustomFOV extends Module {
   @SerializedName("fov")
   public final NumberValue fov = new NumberValue("FOV", 90, 10, 300, 1);

   public CustomFOV() {
      super(Category.Render, "CustomFOV", "自定义视角场大小。");
   }

   @EventTarget
   public void onFovMultiplier(FovMultiplierEvent fovMultiplierEvent) {
      int n = (Integer)mc2.options.getFov().getValue();
      if (n != 0) {
         fovMultiplierEvent.setFovMultiplier2(((Number)this.fov.getValue()).floatValue() / n);
      }
   }
}
