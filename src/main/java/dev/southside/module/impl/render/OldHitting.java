package dev.southside.module.impl.render;

import com.google.gson.annotations.SerializedName;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.values.BooleanValue;
import dev.southside.utils.NumberValue;
import dev.southside.utils.ModeValue;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class OldHitting extends Module {
   @SerializedName("mode")
   public final ModeValue mode = new ModeValue(
      "Mode",
      "Vanilla",
      "Vanilla",
      "ModernVanilla",
      "OneSeven",
      "Pushdown",
      "Swang",
      "Swank",
      "Swing",
      "Swong",
      "SwAing",
      "Remix",
      "Test",
      "Test1",
      "Null",
      "Jello",
      "Punch",
      "Rotate",
      "Winter",
      "Circle",
      "Tap",
      "Tap2",
      "Slide",
      "Scale",
      "Dekael",
      "Leaked",
      "Ninja",
      "Down"
   );
   @SerializedName("speed")
   public final NumberValue speed = new NumberValue("Speed", 1.0, -10, 10, 0.1);
   @SerializedName("blockedX")
   public final NumberValue blockedX = new NumberValue("BlockedX", 0, -0.5, 0.5, 0.01);
   @SerializedName("blockedY")
   public final NumberValue blockedY = new NumberValue("BlockedY", 0, -0.5, 0.5, 0.01);
   @SerializedName("blockedZ")
   public final NumberValue blockedZ = new NumberValue("BlockedZ", 0, -0.5, 0.5, 0.01);
   @SerializedName("mouseXOffset")
   public final NumberValue mouseXOffset = new NumberValue("X", 0, -0.5, 0.5, 0.01);
   @SerializedName("mouseYOffset")
   public final NumberValue mouseYOffset = new NumberValue("Y", 0, -0.5, 0.5, 0.01);
   @SerializedName("mouseZOffset")
   public final NumberValue mouseZOffset = new NumberValue("Z", 0, -0.5, 0.5, 0.01);
   @SerializedName("axeItemTransform")
   public final BooleanValue axeItemTransform = new BooleanValue("Axe", false);
   @SerializedName("animationTicks")
   private int animationTicks;

   public OldHitting() {
      super(Category.Render, "OldHitting", "重生之我在1.8。");
   }

   @Override
   public void onEnable() {
      this.animationTicks = 0;
   }

   public void render(MatrixStack matrices, float f, float f2, float f3, Arm arm) {
      matrices.translate(this.blockedX.getCurrentValue(), this.blockedY.getCurrentValue(), this.blockedZ.getCurrentValue());
      float f4 = MathHelper.sin(MathHelper.sqrt(f3) * (float) Math.PI);
      switch (this.mode.getDisplayTextEx()) {
         case "Vanilla":
            this.translate(matrices, f, f2);
            this.renderEx(matrices, f, f3);
            matrices.scale(0.9F, 0.9F, 0.9F);
            matrices.translate(-0.2F, 0.126F, 0.2F);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-102.25F));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(f * 15.0F));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(f * 80.0F));
            break;
         case "ModernVanilla":
            this.render(matrices, f * 0.48F, f2 * -0.6F);
            this.multiply(matrices, MathHelper.sin(f3 * f3 * (float) Math.PI) * -20.0F, 0.0F, 1.0F, 0.0F);
            this.multiply(matrices, f4 * -20.0F, 0.0F, 0.0F, 1.0F);
            this.multiply(matrices, f4 * -69.0F, 1.0F, 0.0F, 0.0F);
            this.multiply(matrices, -80.0F, 1.0F, 0.0F, 0.0F);
            matrices.scale(1.2F, 1.2F, 1.2F);
            break;
         case "OneSeven":
            this.translate(matrices, f, f2);
            this.renderEx(matrices, arm == Arm.RIGHT ? 1.0F : -1.0F, f3);
            this.render(matrices, arm);
            break;
         case "Pushdown":
            this.translate(matrices, f, f2);
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((arm == Arm.RIGHT ? 1.0F : -1.0F) * f4 * 10.0F));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(f4 * -35.0F));
            this.render(matrices, arm);
            break;
         case "Swang":
         case "Swank":
            matrices.translate(-0.1F, 0.15F, 0.0);
            this.transformFirstPersonItem(matrices, f, f2 / 2.0F, f3);
            float f5 = this.mode.matches("Swang") ? 2.0F : 1.0F;
            this.multiply(matrices, f4 * 30.0F / f5, -f4, -0.0F, 9.0F);
            this.multiply(matrices, f4 * 40.0F, 1.0F, -f4 / f5, -0.0F);
            this.doBlockTransformations(matrices);
            break;
         case "Swing":
            this.transformFirstPersonItem(matrices, f, f2 / 2.0F, f3);
            this.doBlockTransformations(matrices);
            break;
         case "Swong":
            this.transformFirstPersonItem(matrices, f, f2 / 2.0F, 0.0F);
            matrices.translate(0.0F, 0.3F, -0.0F);
            this.multiply(matrices, -f4 * 30.0F, 1.0F, 0.0F, 2.0F);
            this.multiply(matrices, -f4 * 26.0F, 1.5F, (float)(f4 / 1.2), 0.0F);
            this.doBlockTransformations(matrices);
            break;
         case "SwAing":
            matrices.translate(-0.1F, 0.15F, 0.0);
            matrices.translate(0.1F, -0.2F, 0.0);
            matrices.translate(0.6F, -0.48F, -0.79999995F);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(45.0F));
            this.multiply(matrices, f4 * -40.0F, 1.0F, -0.2F, 0.2F);
            matrices.scale(0.9F, 0.9F, 0.9F);
            this.doBlockTransformations(matrices);
            break;
         case "Remix":
            this.transformFirstPersonItem(matrices, f, f2, 0.83F);
            this.doBlockTransformations(matrices);
            float f6 = MathHelper.sin(MathHelper.sqrt(f3) * 3.83F);
            this.multiply(matrices, -f6 * 43.0F, 58.0F, 23.0F, 45.0F);
            break;
         case "Test":
            this.animationTicks++;
            matrices.translate(0.0F, -0.48F + f2 * -0.5F, -0.82F);
            matrices.translate(0.0F, 0.0F, MathHelper.sin(this.animationTicks * 0.12F) * 0.15F);
            float f7 = f4 * 0.05F;
            matrices.translate(0.0F, f7 * -0.1F, f7 * 0.25F);
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(90.0F));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-82.0F));
            matrices.scale(1.05F, 1.05F, 1.05F);
            break;
         case "Test1":
            this.transformFirstPersonItem(matrices, f, f2, 0.0F);
            this.doBlockTransformations(matrices);
            this.multiply(matrices, -f4 * 70.0F / 2.0F, -2.0F, -0.0F, 2.0F);
            this.multiply(matrices, -f4 * 70.0F, 1.0F, -0.4F, -0.0F);
            break;
         case "Null":
            this.transformFirstPersonItem(matrices, f, f2, 0.0F);
            this.doBlockTransformations(matrices);
            break;
         case "Jello":
            this.transformFirstPersonItem(matrices, f, f2, 0.0F);
            this.doBlockTransformations(matrices);
            matrices.translate(-0.05F, 0.3F, 0.0F);
            this.multiply(matrices, -f4 * 70.0F / 2.0F, -8.0F, -0.0F, 9.0F);
            this.multiply(matrices, -f4 * 70.0F, 1.0F, -0.4F, -0.0F);
            break;
         case "Punch":
            this.transformFirstPersonItem(matrices, f, f2, 0.0F);
            this.doBlockTransformations(matrices);
            matrices.translate(0.1F, 0.2F, 0.3F);
            this.multiply(matrices, -f4 * 30.0F, -5.0F, 0.0F, 9.0F);
            this.multiply(matrices, -f4 * 10.0F, 1.0F, -0.4F, -0.5F);
            break;
         case "Rotate":
            this.animationTicks++;
            matrices.translate(0.7, -0.4F, -0.8F);
            this.multiply(matrices, 50.0F, 1.0F, 0.0F, 0.0F);
            this.multiply(matrices, 50.0F, 0.0F, 0.0F, -1.0F);
            this.multiply(matrices, (float)(this.animationTicks * 0.2F * this.speed.getCurrentValue()), 0.0F, 0.0F, 1.0F);
            this.multiply(matrices, -25.0F, 1.0F, 0.0F, 0.0F);
            matrices.scale(0.9F, 0.9F, 0.9F);
            this.doBlockTransformations(matrices);
            break;
         case "Winter":
            matrices.translate(0.56F, -0.52F, -0.71999997F);
            matrices.translate(0.0F, f2 * -0.6F, 0.0F);
            this.multiply(matrices, 45.0F, 0.0F, 1.0F, 0.0F);
            this.multiply(matrices, f4 * -35.0F, 1.0F, 0.0F, 0.0F);
            matrices.scale(0.9F, 0.9F, 0.9F);
            this.doBlockTransformations(matrices);
            break;
         case "Circle":
            this.animationTicks += 2;
            matrices.translate(0.7F, -0.4F, -0.8F);
            this.multiply(matrices, (float)(this.animationTicks * 0.2F * this.speed.getCurrentValue()), 0.0F, 0.0F, 1.0F);
            this.multiply(matrices, 40.0F, 0.0F, 1.0F, 0.0F);
            this.multiply(matrices, 34.0F, 1.0F, 0.0F, 0.0F);
            matrices.scale(0.9F, 0.9F, 0.9F);
            this.doBlockTransformations(matrices);
            break;
         case "Tap":
            matrices.translate(0.0, -3.5, 0.0);
            matrices.translate(f * 0.56F, -0.52F, -0.72F);
            matrices.translate(f * 0.56F, -0.22F, -0.71999997F);
            this.multiply(matrices, 45.0F, 0.0F, 1.0F, 0.0F);
            this.multiply(matrices, f4 * -9.0F, 1.0F, 0.0F, 0.0F);
            this.multiply(matrices, -9.0F, 0.0F, 0.0F, 1.0F);
            matrices.translate(0.0, 3.2F, 0.0);
            this.multiply(matrices, -80.0F, 1.0F, 0.0F, 0.0F);
            matrices.scale(2.7F, 2.7F, 2.7F);
            break;
         case "Tap2":
         case "Slide":
            this.render(matrices, f * 0.648F, f2 * -0.6F);
            this.multiply(matrices, -80.0F, 1.0F, 0.0F, 0.0F);
            if (this.mode.matches("Tap2")) {
               this.multiply(matrices, -f4 * 10.0F, 1.0F, -2.0F, 3.0F);
            } else {
               this.multiply(matrices, -f4 * 20.0F, 1.0F, 0.0F, 0.0F);
            }

            matrices.scale(1.2F, 1.2F, 1.2F);
            break;
         case "Scale":
            this.render(matrices, f * 0.48F, f2 * -0.2F);
            this.multiply(matrices, -80.0F, 1.0F, 0.0F, 0.0F);
            float f8 = 1.2F - f4 * 0.3F;
            matrices.scale(f8, f8, f8);
            break;
         case "Dekael":
         case "Leaked":
            matrices.translate(f * 0.56F, -0.52F, -0.72F);
            matrices.translate(0.0, f2 * -0.6F, 0.0);
            this.multiplyEx(matrices, 77.0F, 0.0F, 1.0F, 0.0F);
            this.multiplyEx(matrices, -10.0F, 0.0F, 0.0F, 1.0F);
            this.multiplyEx(matrices, -80.0F, 1.0F, 0.0F, 0.0F);
            if (this.mode.matches("Dekael")) {
               this.multiplyEx(matrices, f4 * 10.0F, -4.0F, -2.0F, 5.0F);
               this.multiplyEx(matrices, f4 * 30.0F, 1.0F, 0.0F, -1.0F);
            } else {
               this.multiplyEx(matrices, f4 * 10.0F, 4.0F, 2.0F, -5.0F);
               this.multiplyEx(matrices, f4 * 40.0F, -1.0F, 0.0F, 1.0F);
            }
            break;
         case "Ninja":
            matrices.translate(f * 0.48F, -0.39F, -0.71999997F);
            matrices.translate(0.0, f2 * -0.6F, 0.0);
            this.multiply(matrices, 100.0F, 0.0F, 1.0F, 0.0F);
            this.multiply(matrices, -50.0F, 0.0F, 0.0F, 1.0F);
            float f9 = MathHelper.sin(f3 * (float) Math.PI);
            this.multiply(matrices, f9 * -10.0F, 0.0F, 1.0F, 0.0F);
            this.multiply(matrices, f9 * -30.0F, 0.0F, 0.0F, 1.0F);
            this.multiply(matrices, f9 * 109.0F, 1.0F, 0.0F, 0.0F);
            this.multiply(matrices, -90.0F, 1.0F, 0.0F, 0.0F);
            matrices.scale(1.2F, 1.2F, 1.2F);
            break;
         case "Down":
            this.render(matrices, f * 0.48F, f4 * -0.2F);
            this.multiply(matrices, -80.0F, 1.0F, 0.0F, 0.0F);
            matrices.scale(1.2F, 1.2F, 1.2F);
      }
   }

   private void translate(MatrixStack matrices, float f, float f2) {
      matrices.translate(f * 0.56F, -0.52F + f2 * -0.6F, -0.71999997F);
   }

   private void render(MatrixStack matrices, float f, float f2) {
      matrices.translate(f, -0.55F, -0.71999997F);
      matrices.translate(0.0, f2, 0.0);
      this.multiply(matrices, 77.0F, 0.0F, 1.0F, 0.0F);
      this.multiply(matrices, -10.0F, 0.0F, 0.0F, 1.0F);
   }

   private void renderEx(MatrixStack matrices, float f, float f2) {
      float f3 = MathHelper.sin(f2 * f2 * (float) Math.PI);
      float f4 = MathHelper.sin(MathHelper.sqrt(f2) * (float) Math.PI);
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(f * (45.0F + f3 * -20.0F)));
      matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(f * f4 * -20.0F));
      matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(f4 * -80.0F));
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(f * -45.0F));
   }

   private void transformFirstPersonItem(MatrixStack matrices, float f, float f2, float f3) {
      this.translate(matrices, f, f2);
      matrices.multiply(RotationAxis.POSITIVE_Y.rotation(45.0F));
      this.renderEx(matrices, f, f3);
      matrices.scale(0.9F, 0.9F, 0.9F);
   }

   private void render(MatrixStack matrices, Arm arm) {
      matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-102.25F));
      RotationAxis rotationAxis = arm == Arm.RIGHT ? RotationAxis.POSITIVE_Y : RotationAxis.NEGATIVE_Y;
      RotationAxis rotationAxis2 = arm == Arm.RIGHT ? RotationAxis.POSITIVE_Z : RotationAxis.NEGATIVE_Z;
      matrices.multiply(rotationAxis.rotationDegrees(13.365F));
      matrices.multiply(rotationAxis2.rotationDegrees(78.05F));
   }

   private void doBlockTransformations(MatrixStack matrices) {
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(30.0F));
      matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-80.0F));
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(60.0F));
   }

   private void multiply(MatrixStack matrices, float f, float f2, float f3, float f4) {
      matrices.multiply(new Quaternionf().fromAxisAngleDeg(f2, f3, f4, f));
   }

   private void multiplyEx(MatrixStack matrices, float f, float f2, float f3, float f4) {
      matrices.multiply(new Quaternionf().fromAxisAngleDeg(new Vector3f(f2, f3, f4).normalize(), f));
   }
}
