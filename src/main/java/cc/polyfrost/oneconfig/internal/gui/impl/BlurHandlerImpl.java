package cc.polyfrost.oneconfig.internal.gui.impl;

import cc.polyfrost.oneconfig.internal.config.Preferences;
import com.google.gson.annotations.SerializedName;
import dev.southside.event.EventTarget;
import dev.southside.event.Stage;
import dev.southside.event.events.RenderStageEvent;
import dev.southside.event.events.ScreenEvent;
import dev.southside.mixin.accessors.IGameRenderer;
import dev.southside.utils.MinecraftAccess;
import dev.southside.utils.IBlurScreen;
import dev.southside.utils.IBlurHandler;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.render.DefaultFramebufferSet;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BlurHandlerImpl implements IBlurHandler {
   public static final Identifier SOUTHSIDE_BLUR_ID = Identifier.of("southside:blur");
   private static final float DEFAULT_FLOAT = 60.0F;
   private static final float DEFAULT_FLOAT_VALUE = 0.25F;
   private static final float DEFAULT_FLOAT_DATA = 0.01F;
   private static final float DEFAULT_FLOAT_REF = 0.005F;
   private static final float DEFAULT_FLOAT_PRIMARY = 1.0F;
   private static final float DEFAULT_FLOAT_SECONDARY = 0.01F;
   private static final float DEFAULT_FLOAT_TERTIARY = 0.01F;
   private static final long DEFAULT_LONG = 1000000000L;
   private static final long DEFAULT_LONG_VALUE = 3000L;
   @SerializedName("logger")
   public final Logger logger = LogManager.getLogger("OneConfig - Blur");
   @SerializedName("blurRequested")
   public boolean blurRequested;
   @SerializedName("currentRadius")
   public float currentRadius;
   @SerializedName("appliedRadius")
   public float appliedRadius = -1.0F;
   @SerializedName("appliedSampleRate")
   public float appliedSampleRate = -1.0F;
   @SerializedName("postEffectProcessor")
   public PostEffectProcessor postEffectProcessor;
   @SerializedName("lastFrameNanos")
   public long lastFrameNanos = Long.MIN_VALUE;
   @SerializedName("nextLogMillis")
   public long nextLogMillis;

   public float computeFloat(long l) {
      if (this.lastFrameNanos == Long.MIN_VALUE) {
         this.lastFrameNanos = l;
         return 0.0F;
      } else {
         long l2 = l - this.lastFrameNanos;
         this.lastFrameNanos = l;
         return l2 <= 0L ? 0.0F : Math.min(0.25F, (float)l2 / 1.0E9F);
      }
   }

   public void applyText(String text, Exception e) {
      long now = System.currentTimeMillis();
      if (now >= this.nextLogMillis) {
         this.logger.error(text, e);
         this.nextLogMillis = now + 3000L;
      }
   }

   public float moveTowards(float x, float y, float z) {
      if (!(z <= 0.0F) && x != y) {
         return x < y ? Math.min(y, x + z) : Math.max(y, x - z);
      } else {
         return x;
      }
   }

   public void applyText(String text) {
      if (this.isValid()) {
         try {
            ((IGameRenderer)MinecraftAccess.getMc().gameRenderer).invokeClearPostProcessor();
            this.postEffectProcessor = null;
            this.appliedRadius = -1.0F;
            this.appliedSampleRate = -1.0F;
            this.lastFrameNanos = Long.MIN_VALUE;
            this.logger.info("Blur shader disabled ({})", text);
         } catch (Exception e) {
            this.applyText("Error disabling blur shader (" + text + ")", e);
         }
      }
   }

   @EventTarget
   public void onScreen(ScreenEvent screenEvent) {
      this.performAction(screenEvent.screen);
   }

   @Override
   public void performAction(Object obj) {
      if (MinecraftAccess.getClientWorld() == null) {
         this.blurRequested = false;
         this.currentRadius = 0.0F;
         this.appliedRadius = -1.0F;
         this.appliedSampleRate = -1.0F;
         this.postEffectProcessor = null;
         this.lastFrameNanos = Long.MIN_VALUE;
         this.applyText("world not ready");
      } else {
         this.appliedRadius = -1.0F;
         this.appliedSampleRate = -1.0F;
         boolean flag = (Boolean)Preferences.getPreferences().enableBlur.getValue();
         this.blurRequested = flag && obj instanceof IBlurScreen iblurscreen && iblurscreen.isValid();
         if (this.blurRequested) {
            if (!this.isValidEx()) {
               try {
                  this.logger.info("Attempting to load blur shader: {}", SOUTHSIDE_BLUR_ID);
                  ((IGameRenderer)MinecraftAccess.getMc().gameRenderer).invokeSetPostProcessor(SOUTHSIDE_BLUR_ID);
                  this.postEffectProcessor = null;
                  this.appliedRadius = -1.0F;
                  this.appliedSampleRate = -1.0F;
                  this.lastFrameNanos = Long.MIN_VALUE;
                  if (this.isValid()) {
                     this.logger.info("Blur shader loaded successfully");
                  } else {
                     this.logger.warn("Blur shader failed to load - no active OneConfig blur detected");
                  }
               } catch (Exception e) {
                  this.applyText("Failed to load blur shader: " + SOUTHSIDE_BLUR_ID, e);
                  this.applyText("failed to load");
               }
            }
         } else if (!flag) {
            this.logger.info("Blur preference disabled, fading out");
         }
      }
   }

   public void update(float x, float y) {
      if (this.isValid()) {
         try {
            PostEffectProcessor postEffectProcessor2 = this.postEffectProcessor;
            if (postEffectProcessor2 == null) {
               postEffectProcessor2 = MinecraftAccess.getMc().getShaderLoader().loadPostEffect(SOUTHSIDE_BLUR_ID, DefaultFramebufferSet.MAIN_ONLY);
               this.postEffectProcessor = postEffectProcessor2;
            }

            if (postEffectProcessor2 != null) {
               postEffectProcessor2.setUniforms("Radius", x);
               postEffectProcessor2.setUniforms("SampleRate", y);
               postEffectProcessor2.setUniforms("DualSampleEnabled", 0.0F);
               this.appliedRadius = x;
               this.appliedSampleRate = y;
            }
         } catch (Exception e) {
            this.postEffectProcessor = null;
            this.applyText("Failed to update blur uniforms: radius=" + x + ", sampleRate=" + y, e);
         }
      }
   }

   public boolean isValid() {
      Identifier postProcessorId = MinecraftAccess.getMc().gameRenderer.getPostProcessorId();
      return postProcessorId != null && postProcessorId.equals(SOUTHSIDE_BLUR_ID);
   }

   @EventTarget
   public void onPhaseEvent2(RenderStageEvent renderStageEvent) {
      if (renderStageEvent.phase == Stage.PRE) {
         long now = System.nanoTime();
         if (MinecraftAccess.getClientWorld() == null) {
            this.blurRequested = false;
            this.currentRadius = 0.0F;
            this.appliedRadius = -1.0F;
            this.appliedSampleRate = -1.0F;
            this.postEffectProcessor = null;
            this.lastFrameNanos = Long.MIN_VALUE;
            this.applyText("world not ready");
         } else if (this.blurRequested || !(this.currentRadius <= 0.01F) || this.isValid()) {
            Preferences preferences = Preferences.getPreferences();
            float f = Math.max(0.0F, ((Number)preferences.blurMaxRadius.getValue()).floatValue());
            float f2 = Math.max(0.005F, ((Number)preferences.blurFadeStep.getValue()).floatValue()) * 60.0F;
            float f3 = Math.max(1.0F, ((Number)preferences.blurSampleRate.getValue()).floatValue());
            float f4 = this.blurRequested ? f : 0.0F;
            float f5 = this.computeFloat(now);
            float f6 = f2 * f5;
            this.currentRadius = this.moveTowards(this.currentRadius, f4, f6);
            if (this.currentRadius <= 0.01F) {
               this.currentRadius = 0.0F;
               if (!this.blurRequested) {
                  this.appliedRadius = -1.0F;
                  this.appliedSampleRate = -1.0F;
                  this.postEffectProcessor = null;
                  this.applyText("fade finished");
               }
            } else if (this.isValid()) {
               float f7 = Math.max(this.currentRadius, 0.01F);
               boolean flag = Math.abs(f7 - this.appliedRadius) < 0.01F;
               boolean flag2 = Math.abs(f3 - this.appliedSampleRate) < 0.01F;
               if (!flag || !flag2) {
                  this.update(f7, f3);
               }
            } else if (!this.isValidEx()) {
               try {
                  ((IGameRenderer)MinecraftAccess.getMc().gameRenderer).invokeSetPostProcessor(SOUTHSIDE_BLUR_ID);
                  this.postEffectProcessor = null;
                  this.appliedRadius = -1.0F;
                  this.appliedSampleRate = -1.0F;
               } catch (Exception e) {
                  this.applyText("Failed to re-load blur shader during fade update", e);
               }
            }
         }
      }
   }

   public boolean isValidEx() {
      return MinecraftAccess.getMc().gameRenderer.getPostProcessorId() != null;
   }
}
