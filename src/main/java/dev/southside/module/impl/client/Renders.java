package dev.southside.module.impl.client;

import com.google.gson.annotations.SerializedName;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.values.BooleanValue;

public class Renders extends Module {
   public static Renders INSTANCE;
   @SerializedName("msaa")
   public final BooleanValue msaa = new BooleanValue("MSAA", "抗锯齿。", true);

   @Override
   public void onEnable() {
      this.setEnabled(false);
   }

   public Renders() {
      super(Category.Client, "Renders", "渲染。");
      INSTANCE = this;
   }
}
