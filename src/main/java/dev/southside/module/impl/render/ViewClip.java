package dev.southside.module.impl.render;

import com.google.gson.annotations.SerializedName;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.utils.NumberValue;

public class ViewClip extends Module {
   @SerializedName("scale")
   public final NumberValue scale = new NumberValue("Scale", 1.0F, 0.1F, 10.0F, 0.1F);

   public ViewClip() {
      super(Category.Render, "ViewClip", "无视方块阻挡的相机");
   }
}
