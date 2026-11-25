package dev.southside.mixin.misc;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.southside.Southside;
import dev.southside.render.ImeOverlayManager;
import dev.southside.utils.CustomMenuController;
import dev.southside.utils.IText;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.ClickEvent.Action;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Screen.class)
public class MixinScreen {
   @Inject(method = "renderWithTooltip", at = @At("TAIL"))
   private void renderPreedit(DrawContext context, int n, int n2, float f, CallbackInfo ci) {
      if (!CustomMenuController.isActive()) {
         ImeOverlayManager.INSTANCE.render(context);
      }
   }

   @Inject(method = "Lnet/minecraft/client/gui/screen/Screen;handleTextClick(Lnet/minecraft/text/Style;)Z", at = @At("HEAD"), cancellable = true)
   private void onHandleTextClickHead(Style style, CallbackInfoReturnable<Boolean> cir) {
      if (style != null && style.getClickEvent() != null) {
         ClickEvent clickEvent = style.getClickEvent();
         if (clickEvent.getAction() == Action.RUN_COMMAND && clickEvent.getValue().startsWith(".")) {
            try {
               Southside.INSTANCE.getCommandManager().applyText(clickEvent.getValue().substring(1));
            } catch (CommandSyntaxException e) {
               String message = e.getMessage();
               if (message != null && MinecraftClient.getInstance().inGameHud != null) {
                  MinecraftClient.getInstance()
                     .inGameHud
                     .getChatHud()
                     .addMessage(Text.empty().append(IText.TEXT).append(Text.literal(message).setStyle(Style.EMPTY.withColor(Formatting.RED))));
               }
            }

            cir.setReturnValue(true);
         }
      }
   }
}
