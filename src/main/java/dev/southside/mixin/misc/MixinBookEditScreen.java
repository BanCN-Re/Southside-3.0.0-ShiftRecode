package dev.southside.mixin.misc;

import dev.southside.render.ImeOverlayManager;
import dev.southside.utils.IImeTextTarget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.BookEditScreen;
import net.minecraft.client.util.SelectionManager;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BookEditScreen.class)
public abstract class MixinBookEditScreen extends Screen implements IImeTextTarget {
   @Shadow
   private boolean signing;
   @Shadow
   private boolean dirty;
   @Shadow
   private String title;
   @Shadow
   @Final
   private SelectionManager currentPageSelectionManager;
   @Shadow
   @Final
   private SelectionManager bookTitleSelectionManager;

   @Shadow
   protected abstract void invalidatePageContent();

   @Shadow
   protected abstract void updateButtons();

   protected MixinBookEditScreen(Text text) {
      super(text);
   }

   @Override
   public void commitText(String text) {
      if (this.signing) {
         this.bookTitleSelectionManager.insert(text);
         this.dirty = true;
         this.updateButtons();
      } else {
         this.currentPageSelectionManager.insert(text);
         this.invalidatePageContent();
      }
   }

   @Inject(method = "drawCursor", at = @At("HEAD"))
   private void updatePageInputArea(DrawContext context, @Coerce Object obj, boolean flag, CallbackInfo ci) {
      MixinBookEditScreen_Position mixinBookEditScreen_Position = (MixinBookEditScreen_Position)obj;
      int n = mixinBookEditScreen_Position.getCursorX() + (this.width - 192) / 2 + 36;
      int n2 = mixinBookEditScreen_Position.getCursorY() + 32;
      ImeOverlayManager.INSTANCE.performAction(this, context, n, n2, n + 1, n2 + 9 + 1);
   }

   @Inject(method = "render", at = @At("TAIL"))
   private void updateTitleInputArea(DrawContext context, int n, int n2, float f, CallbackInfo ci) {
      if (this.signing) {
         int n3 = Math.max(0, Math.min(this.title.length(), this.bookTitleSelectionManager.getSelectionStart()));
         int n4 = (this.width - 192) / 2
            + 36
            + (114 - this.textRenderer.getWidth(this.title + "_")) / 2
            + this.textRenderer.getWidth(this.title.substring(0, n3));
         ImeOverlayManager.INSTANCE.performAction(this, context, n4, 50.0F, n4 + 1, 50 + 9 + 1);
      }
   }
}
