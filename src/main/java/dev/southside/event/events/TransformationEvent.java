package dev.southside.event.events;

import com.google.gson.annotations.SerializedName;
import dev.southside.utils.CancellableEvent;
import net.minecraft.client.render.model.json.Transformation;
import net.minecraft.client.util.math.MatrixStack;

public class TransformationEvent extends CancellableEvent {
   public static TransformationEvent INSTANCE = new TransformationEvent();
   @SerializedName("transformation")
   public Transformation transformation;
   @SerializedName("leftHanded")
   public boolean leftHanded;
   @SerializedName("matrixStack")
   public MatrixStack matrixStack;

   public static TransformationEvent getTransformationEvent(Transformation transformation2, boolean flag, MatrixStack matrices) {
      INSTANCE.setCancelled(false);
      INSTANCE.transformation = transformation2;
      INSTANCE.leftHanded = flag;
      INSTANCE.matrixStack = matrices;
      return INSTANCE;
   }
}
