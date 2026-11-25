package dev.southside.module.impl.client;

import com.google.gson.annotations.SerializedName;
import dev.southside.gui.hud.WatermarkHud;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.utils.HudValue;
import dev.southside.utils.ModeValue;
import dev.southside.utils.StringValue;

public class Watermark extends Module {
   @SerializedName("style")
   public final ModeValue style = new ModeValue("Style", new String[]{"Southside", "Southside", "SigmaJello", "Old", "Retro"}, "Watermark Style", 1, 0);
   @SerializedName("clientName")
   public final StringValue clientName = new StringValue("Client Name", "请输入客户端名称", false, false, "Watermark显示的客户端名称", 1, "Southside");
   @SerializedName("watermark")
   public final HudValue watermark = new HudValue("Watermark", new WatermarkHud());

   public Watermark() {
      super(Category.Client, "Watermark", "漏狗", true);
   }

   public ModeValue getStyle() {
      return this.style;
   }
}
