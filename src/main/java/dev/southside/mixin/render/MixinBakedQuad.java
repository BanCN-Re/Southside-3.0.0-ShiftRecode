package dev.southside.mixin.render;

import dev.southside.utils.IBakedQuadVertices;
import net.minecraft.client.render.model.BakedQuad;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BakedQuad.class)
public abstract class MixinBakedQuad implements IBakedQuadVertices {
   @Shadow
   @Final
   protected int[] vertexData;

   @Override
   public float vertexY(int n) {
      return Float.intBitsToFloat(this.vertexData[n * 8 + 1]);
   }

   @Override
   public float vertexX(int n) {
      return Float.intBitsToFloat(this.vertexData[n * 8]);
   }

   @Override
   public float vertexZ(int n) {
      return Float.intBitsToFloat(this.vertexData[n * 8 + 2]);
   }
}
