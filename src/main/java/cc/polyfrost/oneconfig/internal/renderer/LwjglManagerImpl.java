package cc.polyfrost.oneconfig.internal.renderer;

import cc.polyfrost.oneconfig.renderer.TinyFD;
import com.google.gson.annotations.SerializedName;
import dev.southside.render.AssetHelperImpl;
import dev.southside.render.NanoVGHelperImpl;
import dev.southside.render.ScissorHelperImpl;
import dev.southside.utils.FontHelper;
import dev.southside.utils.AssetHelper;
import dev.southside.utils.LwjglManager;
import dev.southside.utils.ScissorHelper;
import dev.southside.utils.NanoVGHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LwjglManagerImpl implements LwjglManager {
   public static final Logger LOGGER = LogManager.getLogger("OneConfig LWJGL Manager");
   public static final boolean DEFAULT_ENABLED = isValid();
   @SerializedName("nanoVGHelper")
   private NanoVGHelper nanoVGHelper;
   @SerializedName("scissorHelper")
   private ScissorHelper scissorHelper;
   @SerializedName("assetHelper")
   private AssetHelper assetHelper;
   @SerializedName("fontHelper")
   private FontHelper fontHelper;
   @SerializedName("tinyFD")
   private TinyFD tinyFD;

   public LwjglManagerImpl() {
      try {
         this.nanoVGHelper = new NanoVGHelperImpl();
         this.scissorHelper = new ScissorHelperImpl();
         this.assetHelper = new AssetHelperImpl();
         this.fontHelper = new FontHelperImpl();
         this.tinyFD = new TinyFDImpl();
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Override
   public NanoVGHelper getNanoVGHelper() {
      return this.nanoVGHelper;
   }

   @Override
   public ScissorHelper getScissorHelper() {
      return this.scissorHelper;
   }

   @Override
   public AssetHelper getAssetHelper() {
      return this.assetHelper;
   }

   @Override
   public FontHelper getFontHelper() {
      return this.fontHelper;
   }

   @Override
   public TinyFD getTinyFD() {
      return this.tinyFD;
   }

   public static boolean isValid() {
      try {
         Class.forName("org.lwjgl.glfw.CallbackBridge");
         LOGGER.warn("Pojav detected, letting Pojav handle LWJGL.");
         return true;
      } catch (ClassNotFoundException e) {
         return false;
      }
   }
}
