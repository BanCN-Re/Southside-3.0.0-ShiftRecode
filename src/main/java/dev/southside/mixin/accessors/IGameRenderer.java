package dev.southside.mixin.accessors;

import net.minecraft.client.render.GameRenderer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GameRenderer.class)
public interface IGameRenderer {
   @Invoker("setPostProcessor")
   void invokeSetPostProcessor(Identifier id);

   @Invoker("clearPostProcessor")
   void invokeClearPostProcessor();
}
