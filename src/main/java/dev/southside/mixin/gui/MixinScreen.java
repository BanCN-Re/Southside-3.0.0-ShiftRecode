package dev.southside.mixin.gui;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.southside.utils.CustomMenuController;
import dev.southside.utils.IMenuScreenExt;
import dev.southside.utils.MenuScreenLayout;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Screen.class)
public abstract class MixinScreen implements IMenuScreenExt {
   @Unique
   private MenuScreenLayout southsideMenuPage;

   @Override
   public MenuScreenLayout menuPage() {
      if (this.southsideMenuPage == null) {
         this.southsideMenuPage = new MenuScreenLayout((Screen)(Object)this);
      }

      return this.southsideMenuPage;
   }

   @ModifyVariable(method = {"init(Lnet/minecraft/client/MinecraftClient;II)V", "resize"}, at = @At("HEAD"), argsOnly = true, ordinal = 0)
   private int menuPageWidth(int n) {
      return CustomMenuController.computeInt((Screen)(Object)this, n);
   }

   @ModifyVariable(method = {"init(Lnet/minecraft/client/MinecraftClient;II)V", "resize"}, at = @At("HEAD"), argsOnly = true, ordinal = 1)
   private int menuPageHeight(int n) {
      return CustomMenuController.computeIntEx((Screen)(Object)this, n);
   }

   @WrapMethod(method = "renderWithTooltip")
   private void renderMenuScene(DrawContext context, int n, int n2, float f, Operation<Void> operation) {
      Screen screen = (Screen)(Object)this;
      if (CustomMenuController.isCustomScreen(screen)) {
         CustomMenuController.openScreen(screen, context, n, n2, f);
      } else {
         operation.call(new Object[]{context, n, n2, f});
      }
   }
}
