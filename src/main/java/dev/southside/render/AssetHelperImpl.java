package dev.southside.render;

import cc.polyfrost.oneconfig.utils.IOUtils;
import com.google.gson.annotations.SerializedName;
import dev.southside.utils.SvgAsset;
import dev.southside.utils.ImageAsset;
import dev.southside.utils.AssetHelper;
import dev.southside.utils.NanoVGImage;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import org.lwjgl.nanovg.NSVGImage;
import org.lwjgl.nanovg.NanoSVG;
import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryUtil;

public final class AssetHelperImpl implements AssetHelper {
   public static final int DEFAULT_INT = 7;
   @SerializedName("imageHashMap")
   private final Map<String, NanoVGImage> imageHashMap = new HashMap<>();
   @SerializedName("svgHashMap")
   private final Map<String, NanoVGImage> svgHashMap = new HashMap<>();

   @Override
   public boolean loadImage(long l, String text, int n, Class<?> clazz) {
      if (!this.imageHashMap.containsKey(text)) {
         int[] values = new int[]{0};
         int[] values2 = new int[]{0};
         int[] values3 = new int[]{0};
         ByteBuffer byteBuffer2 = IOUtils.getByteBuffer2(text, clazz);
         if (byteBuffer2 == null) {
            return false;
         }

         ByteBuffer buffer = STBImage.stbi_load_from_memory(byteBuffer2, values, values2, values3, 4);
         if (buffer == null) {
            return false;
         }

         this.imageHashMap.put(text, new NanoVGImage(NanoVG.nvgCreateImageRGBA(l, values[0], values2[0], n, buffer), values[0], values2[0]));
         return true;
      } else {
         return true;
      }
   }

   @Override
   public boolean isValid(long l, ImageAsset image) {
      return this.loadImage(l, image.filePath, image.flags, image.getClass());
   }

   @Override
   public boolean isValid(long l, ImageAsset image, Class<?> clazz) {
      return this.loadImage(l, image.filePath, image.flags, clazz);
   }

   @Override
   public boolean isValid(long l, String text, Class<?> clazz) {
      return this.loadImage(l, text, 7, clazz);
   }

   @Override
   public boolean loadSvg(long vg, String text, float x, float y, int n) {
      String text2 = text + "-" + x + "-" + y;
      if (!this.svgHashMap.containsKey(text2)) {
         try {
            InputStream resourceAsStream = this.getClass().getResourceAsStream(text);
            if (resourceAsStream == null) {
               return false;
            }

            StringBuilder sb = new StringBuilder();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resourceAsStream));

            String text3;
            try {
               while ((text3 = bufferedReader.readLine()) != null) {
                  sb.append(text3);
               }
            } catch (Throwable e) {
               try {
                  bufferedReader.close();
               } catch (Throwable e2) {
                  e.addSuppressed(e2);
               }

               throw e;
            }

            bufferedReader.close();
            String text4 = sb.toString();
            NSVGImage nsvgimage = NanoSVG.nsvgParse(text4, "px", 96.0F);
            if (nsvgimage == null) {
               return false;
            }

            long l = NanoSVG.nsvgCreateRasterizer();
            int n2 = (int)nsvgimage.width();
            int n3 = (int)nsvgimage.height();
            float f = Math.max(x / n2, y / n3);
            n2 = (int)(n2 * f);
            n3 = (int)(n3 * f);
            ByteBuffer buffer = MemoryUtil.memAlloc(n2 * n3 * 4);
            NanoSVG.nsvgRasterize(l, nsvgimage, 0.0F, 0.0F, f, buffer, n2, n3, n2 * 4);
            NanoSVG.nsvgDeleteRasterizer(l);
            NanoSVG.nsvgDelete(nsvgimage);
            this.svgHashMap.put(text2, new NanoVGImage(NanoVG.nvgCreateImageRGBA(vg, n2, n3, n, buffer), n2, n3));
            return true;
         } catch (Exception e3) {
            System.err.println("Failed to parse SVG file");
            e3.printStackTrace();
            return false;
         }
      } else {
         return true;
      }
   }

   @Override
   public boolean isValid(long l, SvgAsset svg, float f, float f2) {
      return this.loadSvg(l, svg.filePath, f, f2, svg.flags);
   }

   @Override
   public boolean isValid(long l, SvgAsset svg, float f, float f2, Class<?> clazz) {
      return this.loadSvg(l, svg.filePath, f, f2, svg.flags);
   }

   @Override
   public boolean isValid(long vg, String text, float x, float y, Class<?> clazz) {
      return this.loadSvg(vg, text, x, y, 7);
   }

   @Override
   public int getImage(String text) {
      return this.imageHashMap.get(text).getImage();
   }

   @Override
   public NanoVGImage get(String text) {
      return this.imageHashMap.get(text);
   }

   @Override
   public void performAction(long l, String text) {
      NanoVG.nvgDeleteImage(l, this.imageHashMap.get(text).getImage());
      this.imageHashMap.remove(text);
   }

   @Override
   public void performAction(long l) {
      HashMap map = new HashMap<>(this.imageHashMap);

      for (Object obj : map.keySet()) {
         String text = (String)obj;
         NanoVG.nvgDeleteImage(l, this.imageHashMap.get(text).getImage());
         this.imageHashMap.remove(text);
      }
   }

   @Override
   public int computeInt(String text, float f, float f2) {
      String text2 = text + "-" + f + "-" + f2;
      return this.svgHashMap.get(text2).getImage();
   }

   @Override
   public NanoVGImage getEx(String text) {
      return this.svgHashMap.get(text);
   }

   @Override
   public void draw(long vg, String text, float x, float y) {
      String text2 = text + "-" + x + "-" + y;
      NanoVG.nvgDeleteImage(vg, this.imageHashMap.get(text2).getImage());
      this.svgHashMap.remove(text2);
   }

   @Override
   public void performActionEx(long l) {
      HashMap map = new HashMap<>(this.svgHashMap);

      for (Object obj : map.keySet()) {
         String text = (String)obj;
         NanoVG.nvgDeleteImage(l, this.svgHashMap.get(text).getImage());
         this.svgHashMap.remove(text);
      }
   }
}
