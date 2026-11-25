package dev.southside.utils.misc;

public record AdjustedYawRecord(double adjustedYaw, int deltaDegrees, double errorDegrees) {
   public int getDeltaDegrees() {
      return this.deltaDegrees;
   }

   @Override
   public String toString() {
      return String.format("adjusted=%.2f°, delta=%d°, error=%.2f°", this.adjustedYaw, this.deltaDegrees, this.errorDegrees);
   }

   public double getErrorDegrees() {
      return this.errorDegrees;
   }

   public double getAdjustedYaw() {
      return this.adjustedYaw;
   }

   public double getAdjustedYaw2() {
      return this.adjustedYaw;
   }

   public int getDeltaDegrees2() {
      return this.deltaDegrees;
   }

   public double getErrorDegrees2() {
      return this.errorDegrees;
   }
}
