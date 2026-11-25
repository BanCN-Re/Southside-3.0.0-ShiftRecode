package dev.southside.mixin.misc;

import dev.southside.render.ImeOverlayManager;
import dev.southside.utils.IImeTextTarget;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.EditBox;
import net.minecraft.client.gui.widget.EditBoxWidget;
import net.minecraft.client.gui.widget.ScrollableTextFieldWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EditBoxWidget.class)
public abstract class MixinEditBoxWidget extends ScrollableTextFieldWidget implements IImeTextTarget {
   @Shadow
   @Final
   private EditBox editBox;
   @Shadow
   @Final
   private TextRenderer textRenderer;

   protected MixinEditBoxWidget(int n, int n2, int n3, int n4, Text text) {
      super(n, n2, n3, n4, text);
   }

   @Override
   public void commitText(String text) {
      if (this.isFocused() && this.active && this.visible) {
         this.editBox.replaceSelection(text);
      }
   }

   @Inject(method = "setFocused", at = @At("TAIL"))
   private void changeInputFocus(boolean flag, CallbackInfo ci) {
      if (flag) {
         ImeOverlayManager.INSTANCE.performAction(this);
      } else {
         ImeOverlayManager.INSTANCE.performActionEx(this);
      }
   }

   @Inject(method = "renderContents", at = @At("TAIL"))
   private void updateInputArea(DrawContext context, int n, int n2, float f, CallbackInfo ci) {
      if (this.isFocused() && this.active && this.visible) {
         int currentLineIndex = this.editBox.getCurrentLineIndex();
         MixinEditBox_Substring line = (MixinEditBox_Substring)(Object)this.editBox.getLine(currentLineIndex);
         int n3 = this.getTextX() + this.textRenderer.getWidth(this.editBox.getText().substring(line.getBeginIndex(), this.editBox.getCursor()));
         int n4 = this.getTextY() + currentLineIndex * 9;
         ImeOverlayManager.INSTANCE.performAction(this, context, n3, n4, n3 + 1, n4 + 9 + 1);
      } else {
         ImeOverlayManager.INSTANCE.performActionEx(this);
      }
   }
}
