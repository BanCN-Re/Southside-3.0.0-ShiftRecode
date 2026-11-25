package dev.southside.mixin.misc;

import dev.southside.render.ImeOverlayManager;
import dev.southside.utils.IImeTextTarget;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AbstractSignEditScreen;
import net.minecraft.client.util.SelectionManager;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractSignEditScreen.class)
public abstract class MixinAbstractSignEditScreen extends Screen implements IImeTextTarget {
   @Shadow
   @Final
   protected SignBlockEntity blockEntity;
   @Shadow
   @Final
   private String[] messages;
   @Shadow
   private int currentRow;
   @Shadow
   private SelectionManager selectionManager;

   protected MixinAbstractSignEditScreen(Text text) {
      super(text);
   }

   @Override
   public void commitText(String text) {
      if (this.selectionManager != null) {
         this.selectionManager.insert(text);
      }
   }

   @Inject(method = "renderSignText", at = @At("TAIL"))
   private void updateInputArea(DrawContext context, CallbackInfo ci) {
      if (this.selectionManager != null) {
         String text = this.messages[this.currentRow];
         int n = Math.max(0, Math.min(text.length(), this.selectionManager.getSelectionStart()));
         int n2 = this.textRenderer.getWidth(text.substring(0, n)) - this.textRenderer.getWidth(text) / 2;
         int n3 = (this.currentRow - 2) * this.blockEntity.getTextLineHeight();
         ImeOverlayManager.INSTANCE.performAction(this, context, n2, n3, n2 + 1, n3 + this.blockEntity.getTextLineHeight());
      }
   }
}
