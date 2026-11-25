package dev.southside.mixin.misc;

import dev.southside.render.ImeOverlayManager;
import dev.southside.utils.IImeTextTarget;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TextFieldWidget.class)
public abstract class MixinTextFieldWidget2 implements IImeTextTarget {
   @Shadow
   @Final
   private TextRenderer textRenderer;
   @Shadow
   private boolean editable;
   @Shadow
   private int firstCharacterIndex;

   @Override
   public void commitText(String text) {
      TextFieldWidget textField = (TextFieldWidget)(Object)this;
      if (textField.isFocused() && textField.active && textField.isVisible() && this.editable) {
         textField.write(text);
      }
   }

   @Inject(method = "setFocused", at = @At("TAIL"))
   private void changeInputFocus(boolean flag, CallbackInfo ci) {
      if (flag && this.editable) {
         ImeOverlayManager.INSTANCE.performAction(this);
      } else {
         ImeOverlayManager.INSTANCE.performActionEx(this);
      }
   }

   @Inject(method = "setEditable", at = @At("TAIL"))
   private void changeEditable(boolean flag, CallbackInfo ci) {
      if (!flag) {
         ImeOverlayManager.INSTANCE.performActionEx(this);
      }
   }

   @Inject(method = "renderWidget", at = @At("TAIL"))
   private void updateInputArea(DrawContext context, int n, int n2, float f, CallbackInfo ci) {
      TextFieldWidget textField = (TextFieldWidget)(Object)this;
      if (textField.isFocused() && textField.active && textField.isVisible() && this.editable) {
         int n3 = Math.max(this.firstCharacterIndex, Math.min(textField.getText().length(), textField.getCursor()));
         int n4 = textField.getX()
            + (textField.drawsBackground() ? 4 : 0)
            + Math.min(textField.getInnerWidth(), this.textRenderer.getWidth(textField.getText().substring(this.firstCharacterIndex, n3)));
         int n5 = textField.drawsBackground() ? textField.getY() + (textField.getHeight() - 8) / 2 : textField.getY();
         ImeOverlayManager.INSTANCE.performAction(this, context, n4, n5, n4 + 1, n5 + 9 + 1);
      } else {
         ImeOverlayManager.INSTANCE.performActionEx(this);
      }
   }
}
