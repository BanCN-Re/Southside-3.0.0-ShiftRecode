package dev.southside.mixin.accessors;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.southside.render.MenuSceneRenderer;
import dev.southside.utils.CustomMenuController;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Mouse.class)
public abstract class IMouse {
   @WrapOperation(method = "onMouseButton", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;mouseClicked(DDI)Z"))
   private boolean menuClick(Screen screen, double d, double d2, int n, Operation<Boolean> operation) {
      if (!CustomMenuController.isCustomScreen(screen)) {
         return (Boolean)operation.call(new Object[]{screen, d, d2, n});
      }

      CustomMenuController.Entry entryExEx = CustomMenuController.getEntryExEx(screen);
      return !entryExEx.isValid(d, d2)
         ? CustomMenuController.isValid(screen, d, d2, n)
         : CustomMenuController.getMenuScreenLayout(screen).isValid(entryExEx.computeDouble(d), entryExEx.computeDoubleEx(d2), n);
   }

   @WrapOperation(method = "onMouseButton", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;mouseReleased(DDI)Z"))
   private boolean menuRelease(Screen screen, double d, double d2, int n, Operation<Boolean> operation) {
      if (!CustomMenuController.isCustomScreen(screen)) {
         return (Boolean)operation.call(new Object[]{screen, d, d2, n});
      }

      if (MenuSceneRenderer.INSTANCE.isKeyDown(n)) {
         return true;
      }

      CustomMenuController.Entry entryExEx = CustomMenuController.getEntryExEx(screen);
      return CustomMenuController.getMenuScreenLayout(screen).isValidEx(entryExEx.computeDouble(d), entryExEx.computeDoubleEx(d2), n);
   }

   @WrapOperation(method = "onMouseScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;mouseScrolled(DDDD)Z"))
   private boolean menuScroll(Screen screen, double d, double d2, double d3, double d4, Operation<Boolean> operation) {
      if (!CustomMenuController.isCustomScreen(screen)) {
         return (Boolean)operation.call(new Object[]{screen, d, d2, d3, d4});
      }

      CustomMenuController.Entry entryExEx = CustomMenuController.getEntryExEx(screen);
      return !entryExEx.isValid(d, d2) ? false : CustomMenuController.getMenuScreenLayout(screen).isValid(d4);
   }

   @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;mouseMoved(DD)V"))
   private void menuMove(Screen screen, double d, double d2, Operation<Void> operation) {
      if (!CustomMenuController.isCustomScreen(screen)) {
         operation.call(new Object[]{screen, d, d2});
      }
   }

   @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;mouseDragged(DDIDD)Z"))
   private boolean menuDrag(Screen screen, double d, double d2, int n, double d3, double d4, Operation<Boolean> operation) {
      if (!CustomMenuController.isCustomScreen(screen)) {
         return (Boolean)operation.call(new Object[]{screen, d, d2, n, d3, d4});
      }

      if (MenuSceneRenderer.INSTANCE.isValid(d3, d4)) {
         return true;
      }

      CustomMenuController.Entry entryExEx = CustomMenuController.getEntryExEx(screen);
      return CustomMenuController.getMenuScreenLayout(screen)
         .isHovered(entryExEx.computeDouble(d), entryExEx.computeDoubleEx(d2), n, d3 / entryExEx.getScale(), d4 / entryExEx.getScale());
   }
}
