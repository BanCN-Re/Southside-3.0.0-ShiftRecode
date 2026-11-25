package dev.southside.module.impl.render;

import com.google.gson.annotations.SerializedName;
import dev.southside.event.EventTarget;
import dev.southside.event.events.DrawContextEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.utils.OneConfigFonts;
import dev.southside.utils.NanoVGHelper;
import dev.southside.utils.NumberValue;
import dev.southside.utils.StringValue;

public class FakerVSBin extends Module {
   @SerializedName("scrollText")
   public final StringValue scrollText = new StringValue(
      "Scroll Text", "请输入滚动文本", false, false, "在屏幕上方滚动显示的文本内容", 2, "本影片由東方紅對戰平臺傾情贊助  本影片資源僅供學習交流使用,切勿用於其他用途"
   );
   @SerializedName("fontSize")
   public final NumberValue fontSize = new NumberValue("Font Size", 14.0F, 8.0F, 30.0F, 1.0F);
   @SerializedName("scrollSpeed")
   public final NumberValue scrollSpeed = new NumberValue("Scroll Speed", 2.0F, 0.5F, 10.0F, 0.5F);
   @SerializedName("scrollOffset")
   private float scrollOffset;

   public FakerVSBin() {
      super(Category.Render, "FakerVSBin", "滚动字幕赞助商广告。");
   }

   @Override
   public void onEnable() {
      this.scrollOffset = 0.0F;
   }

   @EventTarget
   public void onDrawContext(DrawContextEvent drawContextEvent) {
      String text = (String)this.scrollText.getValue();
      if (mc2.getWindow() != null && text != null && !text.isEmpty()) {
         NanoVGHelper nvg = NanoVGHelper.INSTANCE;
         nvg.performAction(true, l -> this.performAction(nvg, l, text));
      }
   }

   private void performAction(NanoVGHelper nvg, long l, String text) {
      float f = ((Number)this.fontSize.getValue()).floatValue();
      int scaledWidth = mc2.getWindow().getScaledWidth();
      float textWidth = nvg.getTextWidth(l, text, f, OneConfigFonts.WQY_MICROHEI);
      float f2 = scaledWidth - this.scrollOffset;
      float f3 = mc2.getWindow().getScaledHeight() / 5.0F - nvg.getTextHeight(l, f, OneConfigFonts.WQY_MICROHEI) / 2.0F;
      nvg.drawTextEx(l, text, f2 + 1.0F, f3 + 1.0F, -1778384896, f, OneConfigFonts.WQY_MICROHEI);
      nvg.drawTextEx(l, text, f2, f3, -419430401, f, OneConfigFonts.WQY_MICROHEI);
      this.scrollOffset = this.scrollOffset + ((Number)this.scrollSpeed.getValue()).floatValue();
      if (this.scrollOffset > scaledWidth + textWidth) {
         this.scrollOffset = 0.0F;
      }
   }
}
