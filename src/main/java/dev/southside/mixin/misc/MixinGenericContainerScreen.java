package dev.southside.mixin.misc;

import dev.southside.Southside;
import dev.southside.module.impl.player.Stealer;
import dev.southside.render.ProjectionUtils;
import dev.southside.utils.ChestTracker;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GenericContainerScreen.class)
public abstract class MixinGenericContainerScreen
   extends HandledScreen<GenericContainerScreenHandler>
   implements ScreenHandlerProvider<GenericContainerScreenHandler> {
   @Unique
   private void renderContainerOverlay(DrawContext context) {
      if (this.handler != null && Southside.INSTANCE.getModuleManager() != null) {
         ChestTracker chestTracker = Southside.INSTANCE.getChestTracker();
         BlockPos pos = chestTracker.findBlockPos();
         if (pos != null) {
            Vec3d vec = pos.toCenterPos();
            Vec2f vec2f = ProjectionUtils.getVec2f(vec.x, vec.y, vec.z, 0.0, 0.0);
            if (vec2f != null) {
               int x = (int)vec2f.x;
               int y = (int)vec2f.y;
               int n = (((GenericContainerScreenHandler)(Object)this.handler).slots.size() / 9 + 1) * 18 + 10;
               context.fill(x, y, x + 172, y + n, Integer.MIN_VALUE);
               context.drawTextWithShadow(this.textRenderer, "Container Contents", x + 5, y + 5, 16777215);
               int n2 = y + 20 + ((GenericContainerScreenHandler)(Object)this.handler).getRows() * 18;
               context.fill(x + 5, n2, x + 5 + 162, n2 + 1, -1);

               for (int i = 0; i < ((GenericContainerScreenHandler)(Object)this.handler).slots.size(); i++) {
                  Slot slot = (Slot)((GenericContainerScreenHandler)(Object)this.handler).slots.get(i);
                  ItemStack stack = slot.getStack();
                  if (!stack.isEmpty()) {
                     int n3 = i / 9;
                     int n4 = i % 9;
                     int n5 = x + 5 + n4 * 18;
                     int n6 = y + 20 + n3 * 18;
                     context.drawItem(stack, n5, n6);
                     context.drawStackOverlay(this.textRenderer, stack, n5, n6);
                  }
               }
            }
         }
      }
   }

   protected MixinGenericContainerScreen(GenericContainerScreenHandler genericContainerScreenHandler, PlayerInventory inventory, Text text) {
      super(genericContainerScreenHandler, inventory, text);
   }

   @Inject(
      method = "Lnet/minecraft/client/gui/screen/ingame/GenericContainerScreen;render(Lnet/minecraft/client/gui/DrawContext;IIF)V",
      at = @At("HEAD"),
      cancellable = true
   )
   private void onRenderHead(DrawContext context, int n, int n2, float f, CallbackInfo ci) {
      Stealer stealer = Southside.INSTANCE.getModuleManager().getModule(Stealer.class);
      if (stealer.isEnabled() && stealer.silentlyStealing.getValue()) {
         ci.cancel();
         this.renderContainerOverlay(context);
      }
   }
}
