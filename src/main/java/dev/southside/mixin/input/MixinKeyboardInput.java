package dev.southside.mixin.input;

import dev.southside.Southside;
import dev.southside.event.events.ForwardEvent;
import dev.southside.utils.KeyboardInputEvent;
import dev.southside.utils.RotationManager;
import dev.southside.utils.MovementUtils;
import dev.southside.utils.Rotation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.option.GameOptions;
import net.minecraft.util.PlayerInput;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public abstract class MixinKeyboardInput extends Input {
   @Shadow
   @Final
   private GameOptions settings;

   @Inject(method = "Lnet/minecraft/client/input/KeyboardInput;tick()V", at = @At("TAIL"), allow = 1)
   private void onTickTail(CallbackInfo ci) {
      this.playerInput = new PlayerInput(
         this.settings.forwardKey.isPressed(),
         this.settings.backKey.isPressed(),
         this.settings.leftKey.isPressed(),
         this.settings.rightKey.isPressed(),
         this.settings.jumpKey.isPressed(),
         this.settings.sneakKey.isPressed(),
         this.settings.sprintKey.isPressed()
      );
      this.updateMovementFromInput();
      ForwardEvent forwardEvent = (ForwardEvent)Southside.INSTANCE
         .getEventBus()
         .postCancellableEvent(
            new ForwardEvent(
               this.playerInput.forward(),
               this.playerInput.backward(),
               this.playerInput.left(),
               this.playerInput.right(),
               this.playerInput.sneak(),
               this.playerInput.jump(),
               this.playerInput.sprint(),
               false
            )
         );
      this.playerInput = new PlayerInput(forwardEvent.isForward(), forwardEvent.isBackward(), forwardEvent.isLeft(), forwardEvent.isRight(), forwardEvent.isJump(), forwardEvent.isSneak(), forwardEvent.isSprint());
      this.updateMovementFromInput();
      this.applyMoveFix(forwardEvent);
      this.updateMovementFromInput();
      Southside.INSTANCE.getEventBus().postEvent(new KeyboardInputEvent());
   }

   @Shadow
   private static float getMovementMultiplier(boolean flag, boolean flag2) {
      return 0.0F;
   }

   @Unique
   private void applyMoveFix(ForwardEvent forwardEvent) {
      if (!forwardEvent.isModified()
         && MinecraftClient.getInstance().player != null
         && !RotationManager.isValidEx()
         && RotationManager.isValid()
         && RotationManager.getCurrentRotation() != null) {
         Rotation activeRotation = RotationManager.getActiveRotation();
         float f = Math.max(Math.abs(this.movementForward), Math.abs(this.movementSideways));
         double d = MovementUtils.evaluate(MovementUtils.computeFloat(this.movementForward, this.movementSideways) - activeRotation.yaw);
         double d2 = Math.abs(d);
         float f2 = 0.0F;
         float f3 = 0.0F;
         if (d2 <= 67.5) {
            f2++;
         } else if (d2 >= 112.5) {
            f2--;
         }

         if (d >= 22.5 && d <= 157.5) {
            f3--;
         } else if (d <= -22.5 && d >= -157.5) {
            f3++;
         }

         f2 *= f;
         f3 *= f;
         this.playerInput = new PlayerInput(
            f2 == 1.0F, f2 != 1.0F && f2 != 0.0F, f3 == 1.0F, f3 != 1.0F && f3 != 0.0F, forwardEvent.isJump(), forwardEvent.isSneak(), forwardEvent.isSprint()
         );
      }
   }

   @Unique
   private void updateMovementFromInput() {
      this.movementForward = getMovementMultiplier(this.playerInput.forward(), this.playerInput.backward());
      this.movementSideways = getMovementMultiplier(this.playerInput.left(), this.playerInput.right());
   }
}
