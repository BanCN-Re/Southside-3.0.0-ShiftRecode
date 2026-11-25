package dev.southside.utils;

import com.google.gson.annotations.SerializedName;

public interface OneConfigPlatform {
   static OneConfigPlatform getInstance() {
      return OneConfigPlatform.Inner.INSTANCE.platform;
   }

   static IMousePlatform getMousePlatform() {
      return OneConfigPlatform.Inner.INSTANCE.mousePlatform;
   }

   static IServerPlatform getServerPlatform() {
      return OneConfigPlatform.Inner.INSTANCE.serverPlatform;
   }

   static IGLPlatform getGLPlatform() {
      return OneConfigPlatform.Inner.INSTANCE.glPlatform;
   }

   static IGuiPlatform getGuiPlatform() {
      return OneConfigPlatform.Inner.INSTANCE.guiPlatform;
   }

   static II18nPlatform getI18nPlatform() {
      return OneConfigPlatform.Inner.INSTANCE.i18nPlatform;
   }

   boolean isOnThread();

   int getHeight();

   boolean isDevelopmentEnvironment();

   OneConfigPlatform.Mode getMode();

   class Inner {
      public static OneConfigPlatform.Inner INSTANCE = new OneConfigPlatform.Inner();
      @SerializedName("platform")
      public OneConfigPlatform platform = new FabricPlatformImpl();
      @SerializedName("mousePlatform")
      public IMousePlatform mousePlatform = new MousePlatformImpl();
      @SerializedName("serverPlatform")
      public IServerPlatform serverPlatform = new ServerPlatformImpl();
      @SerializedName("glPlatform")
      public IGLPlatform glPlatform = new GLPlatformImpl();
      @SerializedName("guiPlatform")
      public IGuiPlatform guiPlatform = new GuiPlatformImpl();
      @SerializedName("i18nPlatform")
      public II18nPlatform i18nPlatform = new I18nPlatformImpl();

      private Inner() {
      }
   }

   enum Mode {
      FORGE,
      FABRIC;

      private static int[] values;

      // $VF: synthetic method
      private static OneConfigPlatform.Mode[] createValues() {
         return new OneConfigPlatform.Mode[]{FORGE, FABRIC};
      }


      public static void setValues(int[] values2) {
         values = values2;
      }

   }
}
