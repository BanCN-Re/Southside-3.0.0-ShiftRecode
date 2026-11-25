package dev.southside.mixin.hud;

import dev.southside.Southside;
import dev.southside.module.impl.render.Chatting;
import dev.southside.utils.IMinecraft;
import dev.southside.utils.ChatAnimationRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatHud.class)
public abstract class MixinChatHud {
   @Inject(method = "Lnet/minecraft/client/gui/hud/ChatHud;render(Lnet/minecraft/client/gui/DrawContext;IIIZ)V", at = @At("HEAD"), cancellable = true)
   public void onRenderHead(DrawContext context, int n, int n2, int n3, boolean flag, CallbackInfo ci) {
      if (IMinecraft.mc.world != null) {
         Chatting chatting = Southside.INSTANCE.getModuleManager().getModule(Chatting.class);
         if (chatting.isEnabled()) {
            ci.cancel();
            ChatAnimationRenderer.render(context, n, n2, n3);
         }
      }
   }

   @Shadow
   public abstract int getWidth();
}
