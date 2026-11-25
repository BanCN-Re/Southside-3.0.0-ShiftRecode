package dev.southside.utils;

import com.google.gson.annotations.SerializedName;

public class OneConfigIcon {
   @SerializedName("icon")
   private final Object icon;
   @SerializedName("type")
   public final OneConfigIcon.Mode type;

   public OneConfigIcon(String text) {
      this.icon = text.endsWith(".svg") ? new SvgAsset(text) : new ImageAsset(text);
      this.type = text.endsWith(".svg") ? OneConfigIcon.Mode.SVG : OneConfigIcon.Mode.IMAGE;
   }

   public OneConfigIcon(SvgAsset svg) {
      this.icon = svg;
      this.type = OneConfigIcon.Mode.SVG;
   }

   public OneConfigIcon(ImageAsset image) {
      this.icon = image;
      this.type = OneConfigIcon.Mode.IMAGE;
   }

   public SvgAsset getSvgAsset() {
      return (SvgAsset)this.icon;
   }

   public ImageAsset getImageAsset() {
      return (ImageAsset)this.icon;
   }

   public void renderSvg(long vg, float x, float y, float width, float height, int color, float radius) {
      if (this.type == OneConfigIcon.Mode.SVG) {
         NanoVGHelper.INSTANCE.draw(vg, this.getSvgAsset(), x, y, width, height, color, radius);
      } else if (this.type == OneConfigIcon.Mode.IMAGE) {
         NanoVGHelper.INSTANCE.draw(vg, this.getImageAsset(), x, y, width, height, color);
      }
   }

   public void renderSvg(long vg, float x, float y, float width, float height, int color) {
      if (this.type == OneConfigIcon.Mode.SVG) {
         NanoVGHelper.INSTANCE.draw(vg, this.getSvgAsset(), x, y, width, height, color);
      } else if (this.type == OneConfigIcon.Mode.IMAGE) {
         NanoVGHelper.INSTANCE.draw(vg, this.getImageAsset(), x, y, width, height, color);
      }
   }

   public void renderSvg(long vg, float x, float y, float width, float height) {
      if (this.type == OneConfigIcon.Mode.SVG) {
         NanoVGHelper.INSTANCE.draw(vg, this.getSvgAsset(), x, y, width, height);
      } else if (this.type == OneConfigIcon.Mode.IMAGE) {
         NanoVGHelper.INSTANCE.draw(vg, this.getImageAsset(), x, y, width, height);
      }
   }

   public enum Mode {
      @SerializedName("0")
      SVG,
      @SerializedName("1")
      IMAGE;
   }
}
