package dev.southside.render;

import cc.polyfrost.oneconfig.internal.config.Preferences;
import com.google.gson.annotations.SerializedName;
import com.sun.jna.Platform;
import dev.southside.Southside;
import dev.southside.utils.IImeTextTarget;
import dev.southside.utils.Win32ImeBackend;
import dev.southside.utils.OneConfigGui;
import dev.southside.utils.misc.TextRecord;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.Window;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.joml.Vector3f;
import org.lwjgl.nanovg.NanoVG;

public final class ImeOverlayManager {
   public static final ImeOverlayManager INSTANCE = new ImeOverlayManager();
   private static final Identifier PREEDIT_ID = Identifier.of("southside", "widget/preedit");
   @SerializedName("backend")
   private Win32ImeBackend backend;
   @SerializedName("initializationFailed")
   private boolean initializationFailed;
   @SerializedName("enabled")
   private boolean enabled;
   @SerializedName("committingText")
   private boolean committingText;
   @SerializedName("owner")
   private IImeTextTarget owner;
   @SerializedName("screen")
   private Screen screen;
   @SerializedName("preedit")
   private TextRecord preedit;
   @SerializedName("preeditStartedAt")
   private long preeditStartedAt;
   @SerializedName("frame")
   private long frame;
   @SerializedName("ownerFrame")
   private long ownerFrame;
   @SerializedName("password")
   private boolean password;
   @SerializedName("caretLeft")
   private float caretLeft;
   @SerializedName("caretTop")
   private float caretTop;
   @SerializedName("caretRight")
   private float caretRight;
   @SerializedName("caretBottom")
   private float caretBottom;
   @SerializedName("areaLeft")
   private int areaLeft = Integer.MIN_VALUE;
   @SerializedName("areaTop")
   private int areaTop;
   @SerializedName("areaRight")
   private int areaRight;
   @SerializedName("areaBottom")
   private int areaBottom;

   private ImeOverlayManager() {
   }

   public void performAction() {
      this.frame++;
      MinecraftClient instance = MinecraftClient.getInstance();
      boolean flag = Platform.isWindows() && Preferences.INSTANCE != null && (Boolean)Preferences.INSTANCE.imeSupport.getValue();
      if (flag && this.backend == null && !this.initializationFailed) {
         try {
            this.backend = new Win32ImeBackend(instance.getWindow().getHandle(), this);
         } catch (RuntimeException | LinkageError e) {
            this.initializationFailed = true;
            Southside.LOGGER.warn("Could not initialize Windows text input; using GLFW input", e);
         }
      }

      boolean flag2 = flag && this.backend != null && this.backend.isValid();
      if (this.enabled != flag2) {
         if (!flag2) {
            this.performActionDirect();
         }

         this.enabled = flag2;
         if (this.backend != null) {
            if (this.enabled) {
               this.backend.performAction();
            } else {
               this.backend.performActionImpl();
            }
         }
      }

      if (this.screen != instance.currentScreen) {
         this.performActionImpl();
      } else if (!instance.isWindowFocused()) {
         this.performActionDirect();
      }
   }

   public void performActionEx() {
      if (this.owner != null && this.ownerFrame != this.frame) {
         this.performActionImpl();
      }
   }

   public void performActionAlt() {
      if (this.screen != MinecraftClient.getInstance().currentScreen) {
         this.performActionImpl();
      }
   }

   public void performAction(IImeTextTarget iimetexttarget) {
      MinecraftClient instance = MinecraftClient.getInstance();
      if (this.enabled && instance.currentScreen != null && instance.isWindowFocused()) {
         if (this.owner != iimetexttarget || this.screen != instance.currentScreen) {
            this.performActionImpl();
            this.owner = iimetexttarget;
            this.screen = instance.currentScreen;
            this.password = false;
            this.areaLeft = Integer.MIN_VALUE;
            this.backend.performActionEx();
         }

         this.ownerFrame = this.frame;
      }
   }

   public void performActionEx(IImeTextTarget iimetexttarget) {
      if (this.owner == iimetexttarget) {
         this.performActionImpl();
      }
   }

   public void performActionImpl() {
      IImeTextTarget owner2 = this.owner;
      this.performActionDirect();
      if (owner2 != null) {
         owner2.performActionEx();
      }
   }

   public void performActionDirect() {
      this.owner = null;
      this.screen = null;
      this.preedit = null;
      this.areaLeft = Integer.MIN_VALUE;
      if (this.backend != null) {
         this.backend.performActionAlt();
      }
   }

   public void performActionSafe() {
      this.performActionImpl();
      this.enabled = false;
      if (this.backend != null) {
         this.backend.close();
      }
   }

   public void applyText(String text) {
      MinecraftClient instance = MinecraftClient.getInstance();
      IImeTextTarget owner2 = this.owner;
      if (this.enabled && owner2 != null && this.screen == instance.currentScreen && instance.isWindowFocused() && !text.isEmpty()) {
         this.committingText = true;

         try {
            text.codePoints().takeWhile(n -> this.owner == owner2 && this.screen == instance.currentScreen).forEach(n2 -> {
               if (this.screen instanceof OneConfigGui oneConfigGui) {
                  oneConfigGui.performActionEx(n2, 0);
               } else if (this.screen != null) {
                  boolean flag = false;

                  for (char ch : Character.toChars(n2)) {
                     flag |= this.screen.charTyped(ch, 0);
                  }

                  if (!flag) {
                     owner2.commitText(Character.toString(n2));
                  }
               } else {
                  owner2.commitText(Character.toString(n2));
               }
            });
         } finally {
            this.committingText = false;
         }
      }
   }

   public boolean isCommittingText() {
      return this.committingText;
   }

   public void performAction(TextRecord textRecord) {
      this.preedit = this.owner == null ? null : textRecord;
      this.preeditStartedAt = Util.getMeasuringTimeMs();
   }

   public boolean isKeyDown(int n) {
      if (this.enabled && this.owner != null && n > 65535 && Character.isValidCodePoint(n) && this.screen == MinecraftClient.getInstance().currentScreen) {
         this.applyText(Character.toString(n));
         return true;
      } else {
         return false;
      }
   }

   public void performAction(IImeTextTarget iimetexttarget, DrawContext context, float f, float f2, float f3, float f4) {
      this.performAction(iimetexttarget);
      if (this.owner == iimetexttarget) {
         Vector3f vec = context.getMatrices().peek().getPositionMatrix().transformPosition(new Vector3f(f, f2, 0.0F));
         Vector3f vec2 = context.getMatrices().peek().getPositionMatrix().transformPosition(new Vector3f(f3, f4, 0.0F));
         Window window = MinecraftClient.getInstance().getWindow();
         float scaleFactor = (float)window.getScaleFactor();
         this.performAction(vec.x * scaleFactor, vec.y * scaleFactor, vec2.x * scaleFactor, vec2.y * scaleFactor, false);
      }
   }

   public void performAction(IImeTextTarget iimetexttarget, long l, float f, float f2, float f3, float f4, boolean flag) {
      this.performAction(iimetexttarget);
      if (this.owner == iimetexttarget) {
         float[] floats = new float[6];
         NanoVG.nvgCurrentTransform(l, floats);
         float f5 = floats[0] * f + floats[2] * f2 + floats[4];
         float f6 = floats[1] * f + floats[3] * f2 + floats[5];
         float f7 = floats[0] * f3 + floats[2] * f4 + floats[4];
         float f8 = floats[1] * f3 + floats[3] * f4 + floats[5];
         this.performAction(f5, f6, f7, f8, flag);
      }
   }

   private void performAction(float f, float f2, float f3, float f4, boolean flag) {
      Window window = MinecraftClient.getInstance().getWindow();
      float scaleFactor = (float)window.getScaleFactor();
      this.caretLeft = Math.min(f, f3) / scaleFactor;
      this.caretTop = Math.min(f2, f4) / scaleFactor;
      this.caretRight = Math.max(f, f3) / scaleFactor;
      this.caretBottom = Math.max(f2, f4) / scaleFactor;
      this.password = flag;
      if (this.preedit == null) {
         this.performAction(this.caretLeft, this.caretTop, this.caretRight, this.caretBottom);
      }
   }

   private void performAction(float x, float y, float width, float height) {
      Window window = MinecraftClient.getInstance().getWindow();
      float f = (float)(window.getScaleFactor() * window.getWidth() / Math.max(1, window.getFramebufferWidth()));
      float f2 = (float)(window.getScaleFactor() * window.getHeight() / Math.max(1, window.getFramebufferHeight()));
      int n = Math.round(x * f);
      int n2 = Math.round(y * f2);
      int n3 = Math.max(n + 1, Math.round(width * f));
      int n4 = Math.max(n2 + 1, Math.round(height * f2));
      if (n != this.areaLeft || n2 != this.areaTop || n3 != this.areaRight || n4 != this.areaBottom) {
         this.areaLeft = n;
         this.areaTop = n2;
         this.areaRight = n3;
         this.areaBottom = n4;
         this.backend.performAction(n, n2, n3, n4);
      }
   }

   public void render(DrawContext context) {
      MinecraftClient instance = MinecraftClient.getInstance();
      TextRecord preedit2 = this.preedit;
      if (this.enabled && preedit2 != null && this.owner != null && this.ownerFrame == this.frame && this.screen == instance.currentScreen) {
         TextRenderer textRenderer = instance.textRenderer;
         String text = this.password ? "*".repeat(preedit2.getText().length()) : preedit2.getText();
         int n = Math.max(1, instance.getWindow().getScaledWidth() - 10);
         short s = 3340;
         int width = textRenderer.getWidth(text);
         long l = 6415081167768843497L;
         int n2 = Math.min(n, Math.max(1, width));
         byte b = 9;
         int n3 = Math.max(5, Math.min(Math.round(this.caretLeft), instance.getWindow().getScaledWidth() - n2 - 5));
         int n4 = Math.round(this.caretBottom) + 4;
         if (n4 + b + 5 > instance.getWindow().getScaledHeight()) {
            n4 = Math.round(this.caretTop) - 4 - b;
         }

         n4 = Math.max(5, n4);
         int width2 = textRenderer.getWidth(text.substring(0, preedit2.getCaret()));
         int n5 = Math.max(0, width2 - n2 + 1);
         context.getMatrices().push();
         context.getMatrices().translate(0.0F, 0.0F, 500.0F);
         context.drawGuiTexture(RenderLayer::getGuiTextured, PREEDIT_ID, n3 - 5, n4 - 5, n2 + 10, b + 10);
         context.enableScissor(n3, n4 - 1, n3 + n2 + 1, n4 + b + 2);
         if (preedit2.getSelectionEnd() > preedit2.getSelectionStart()) {
            int n6 = textRenderer.getWidth(text.substring(0, preedit2.getSelectionStart())) - n5;
            int n7 = textRenderer.getWidth(text.substring(0, preedit2.getSelectionEnd())) - n5;
            context.fill(n3 + n6, n4 + b, n3 + n7, n4 + b + 1, -16777216);
         }

         context.drawText(textRenderer, text, n3 - n5, n4, -16777216, false);
         if ((Util.getMeasuringTimeMs() - this.preeditStartedAt) / 300L % 2L == 0L) {
            context.fill(n3 + width2 - n5, n4 - 1, n3 + width2 - n5 + 1, n4 + b, -16777216);
         }

         context.disableScissor();
         context.getMatrices().pop();
         this.performAction(n3 - 5, n4 - 5, n3 + n2 + 5, n4 + b + 5);
      }
   }

   static {
      short s = -267;
      short s2 = 25097;
   }
}
