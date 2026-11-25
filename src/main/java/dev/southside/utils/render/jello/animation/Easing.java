package dev.southside.utils.render.jello.animation;

import com.google.gson.annotations.SerializedName;
import java.util.function.Function;

public enum Easing {
   LINEAR(Easing::getDouble),
   EASE_IN_QUAD(Easing::getDoubleEx),
   EASE_OUT_QUAD(Easing::getDoubleAlt),
   EASE_IN_OUT_QUAD(Easing::getDoubleImpl),
   EASE_IN_CUBIC(Easing::getDoubleDirect),
   EASE_OUT_CUBIC(Easing::getDoubleSafe),
   EASE_IN_OUT_CUBIC(Easing::getDoubleCustom),
   EASE_IN_QUART(Easing::getDoubleWithOffset),
   EASE_OUT_QUART(Easing::getDoubleByIndex),
   EASE_IN_OUT_QUART(Easing::getDoubleInternal),
   EASE_IN_QUINT(Easing::getDoubleHelper),
   EASE_OUT_QUINT(Easing::getDoubleV2),
   EASE_IN_OUT_QUINT(Easing::getDoubleV3),
   EASE_IN_SINE(Easing::getDoubleV4),
   EASE_OUT_SINE(Easing::getDoubleV5),
   EASE_IN_OUT_SINE(Easing::getDoubleV6),
   EASE_IN_EXPO(Easing::getDoubleV7),
   EASE_OUT_EXPO(Easing::getDoubleV8),
   EASE_IN_OUT_EXPO(Easing::getDoubleV9),
   EASE_IN_CIRC(Easing::getDoubleV10),
   EASE_OUT_CIRC(Easing::getDoubleV11),
   EASE_IN_OUT_CIRC(Easing::getDoubleV12),
   SIGMOID(Easing::getDoubleV13),
   EASE_OUT_ELASTIC(Easing::getDoubleV14),
   EASE_IN_BACK(Easing::getDoubleV15),
   EASE_OUT_BACK(Easing::getDoubleV16),
   EASE_IN_OUT_ELASTIC(Easing::getDoubleV17),
   EASE_OUT_BOUNCE(Easing::getDoubleV18),
   EASE_IN_OUT_BOUNCE(Easing::getDoubleV19),
   EASE_IN_OUT_BACK(Easing::getDoubleV20);

   @SerializedName("function")
   public final Function<Double, Double> function;

   Easing(Function<Double, Double> fn) {
      this.function = fn;
   }

   public Function<Double, Double> getFunction() {
      return this.function;
   }

   public static Double getDouble(Double val) {
      return val;
   }

   public static Double getDoubleEx(Double val) {
      return val * val;
   }

   public static Double getDoubleAlt(Double val) {
      return val * (2.0 - val);
   }

   public static Double getDoubleImpl(Double val) {
      return val < 0.5 ? 2.0 * val * val : -1.0 + (4.0 - 2.0 * val) * val;
   }

   public static Double getDoubleDirect(Double val) {
      return val * val * val;
   }

   public static Double getDoubleSafe(Double val) {
      val = val - 1.0;
      return val * val * val + 1.0;
   }

   public static Double getDoubleCustom(Double val) {
      return val < 0.5 ? 4.0 * val * val * val : (val - 1.0) * (2.0 * val - 2.0) * (2.0 * val - 2.0) + 1.0;
   }

   public static Double getDoubleWithOffset(Double val) {
      return val * val * val * val;
   }

   public static Double getDoubleByIndex(Double val) {
      val = val - 1.0;
      return 1.0 - val * val * val * val;
   }

   public static Double getDoubleInternal(Double val) {
      double d;
      if (val < 0.5) {
         d = 8.0 * val * val * val * val;
      } else {
         val = val - 1.0;
         d = 1.0 - 8.0 * val * val * val * val;
      }

      return d;
   }

   public static Double getDoubleHelper(Double val) {
      return val * val * val * val * val;
   }

   public static Double getDoubleV2(Double val) {
      val = val - 1.0;
      return 1.0 + val * val * val * val * val;
   }

   public static Double getDoubleV3(Double val) {
      double d;
      if (val < 0.5) {
         d = 16.0 * val * val * val * val * val;
      } else {
         val = val - 1.0;
         d = 1.0 + 16.0 * val * val * val * val * val;
      }

      return d;
   }

   public static Double getDoubleV4(Double val) {
      return 1.0 - Math.cos(val * Math.PI / 2.0);
   }

   public static Double getDoubleV5(Double val) {
      return Math.sin(val * Math.PI / 2.0);
   }

   public static Double getDoubleV6(Double val) {
      return 1.0 - Math.cos(Math.PI * val / 2.0);
   }

   public static Double getDoubleV7(Double val) {
      return val == 0.0 ? 0.0 : Math.pow(2.0, 10.0 * val - 10.0);
   }

   public static Double getDoubleV8(Double val) {
      return val == 1.0 ? 1.0 : 1.0 - Math.pow(2.0, -10.0 * val);
   }

   public static Double getDoubleV9(Double val) {
      double d;
      if (val == 0.0) {
         d = 0.0;
      } else if (val == 1.0) {
         d = 1.0;
      } else if (val < 0.5) {
         d = Math.pow(2.0, 20.0 * val - 10.0) / 2.0;
      } else {
         d = (2.0 - Math.pow(2.0, -20.0 * val + 10.0)) / 2.0;
      }

      return d;
   }

   public static Double getDoubleV10(Double val) {
      return 1.0 - Math.sqrt(1.0 - val * val);
   }

   public static Double getDoubleV11(Double val) {
      val = val - 1.0;
      return Math.sqrt(1.0 - val * val);
   }

   public static Double getDoubleV12(Double val) {
      return val < 0.5 ? (1.0 - Math.sqrt(1.0 - 4.0 * val * val)) / 2.0 : (Math.sqrt(1.0 - 4.0 * (val - 1.0) * val) + 1.0) / 2.0;
   }

   public static Double getDoubleV13(Double val) {
      return 1.0 / (1.0 + Math.exp(-val));
   }

   public static Double getDoubleV14(Double val) {
      double d;
      if (val == 0.0) {
         d = 0.0;
      } else if (val == 1.0) {
         d = 1.0;
      } else {
         d = Math.pow(2.0, -10.0 * val) * Math.sin((val * 10.0 - 0.75) * (Math.PI * 2.0 / 3.0)) * 0.5 + 1.0;
      }

      return d;
   }

   public static Double getDoubleV15(Double val) {
      return 2.70158 * val * val * val - 1.70158 * val * val;
   }

   public static Double getDoubleV16(Double val) {
      return 1.0 + 2.70158 * Math.pow(val - 1.0, 3.0) + 1.70158 * Math.pow(val - 1.0, 2.0);
   }

   public static Double getDoubleV17(Double val) {
      double d = Math.PI * 4.0 / 9.0;
      double d2 = Math.sin((20.0 * val - 11.125) * d);
      double d3;
      if (val == 0.0) {
         d3 = 0.0;
      } else if (val == 1.0) {
         d3 = 1.0;
      } else if (val < 0.5) {
         d3 = -(Math.pow(2.0, 20.0 * val - 10.0) * d2) / 2.0;
      } else {
         d3 = Math.pow(2.0, -20.0 * val + 10.0) * d2 / 2.0 + 1.0;
      }

      return d3;
   }

   public static Double getDoubleV18(Double val) {
      double d = 7.5625;
      double d2 = 2.75;
      if (val < 1.0 / d2) {
         return d * val * val;
      } else if (val < 2.0 / d2) {
         val = val - 1.5 / d2;
         return d * val * val + 0.75;
      } else if (val < 2.5 / d2) {
         val = val - 2.25 / d2;
         return d * val * val + 0.9375;
      } else {
         val = val - 2.625 / d2;
         return d * val * val + 0.984375;
      }
   }

   public static Double getDoubleV19(Double val) {
      return val < 0.5
         ? (1.0 - EASE_OUT_BOUNCE.getFunction().apply(1.0 - 2.0 * val)) / 2.0
         : (1.0 + EASE_OUT_BOUNCE.getFunction().apply(2.0 * val - 1.0)) / 2.0;
   }

   public static Double getDoubleV20(Double val) {
      double d = 1.70158;
      double d2 = d * 1.525;
      return val < 0.5
         ? Math.pow(2.0 * val, 2.0) * ((d2 + 1.0) * 2.0 * val - d2) / 2.0
         : (Math.pow(2.0 * val - 2.0, 2.0) * ((d2 + 1.0) * (val * 2.0 - 2.0) + d2) + 2.0) / 2.0;
   }

   // $VF: synthetic method
   private static Easing[] getEasingArray() {
      return new Easing[]{
         LINEAR,
         EASE_IN_QUAD,
         EASE_OUT_QUAD,
         EASE_IN_OUT_QUAD,
         EASE_IN_CUBIC,
         EASE_OUT_CUBIC,
         EASE_IN_OUT_CUBIC,
         EASE_IN_QUART,
         EASE_OUT_QUART,
         EASE_IN_OUT_QUART,
         EASE_IN_QUINT,
         EASE_OUT_QUINT,
         EASE_IN_OUT_QUINT,
         EASE_IN_SINE,
         EASE_OUT_SINE,
         EASE_IN_OUT_SINE,
         EASE_IN_EXPO,
         EASE_OUT_EXPO,
         EASE_IN_OUT_EXPO,
         EASE_IN_CIRC,
         EASE_OUT_CIRC,
         EASE_IN_OUT_CIRC,
         SIGMOID,
         EASE_OUT_ELASTIC,
         EASE_IN_BACK,
         EASE_OUT_BACK,
         EASE_IN_OUT_ELASTIC,
         EASE_OUT_BOUNCE,
         EASE_IN_OUT_BOUNCE,
         EASE_IN_OUT_BACK
      };
   }


}
