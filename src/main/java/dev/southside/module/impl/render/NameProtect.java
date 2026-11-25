package dev.southside.module.impl.render;

import cc.polyfrost.oneconfig.config.core.OneColor;
import com.google.gson.annotations.SerializedName;
import dev.southside.Southside;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.values.BooleanValue;
import dev.southside.utils.ColorValue;
import dev.southside.utils.StringValue;

public class NameProtect extends Module {
   @SerializedName("replacementName")
   public final StringValue replacementName = new StringValue("Name", "请输入文本", false, false, "", 1, "Hidden");
   @SerializedName("replacementColor")
   public final ColorValue replacementColor = new ColorValue("Color", new OneColor(-10878977));
   @SerializedName("protectIrc")
   public final BooleanValue protectIrc = new BooleanValue("Protect IRC", "保护irc用户", true);
   public static final ColorValue descriptionUtil2 = new ColorValue("IRC Color", new OneColor(-10878977));

   public static boolean isValidEx() {
      NameProtect nameProtect = Southside.INSTANCE.getModuleManager().getModule(NameProtect.class);
      return nameProtect.isEnabled() && (Boolean)nameProtect.protectIrc.getValue();
   }

   public NameProtect() {
      super(Category.Render, "NameProtect", "Protects your name from being seen by yourself");
   }
}
