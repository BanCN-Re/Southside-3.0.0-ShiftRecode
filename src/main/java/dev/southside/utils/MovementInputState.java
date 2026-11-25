package dev.southside.utils;

import com.google.gson.annotations.SerializedName;
import dev.southside.event.events.ForwardEvent;
import net.minecraft.client.input.Input;

public class MovementInputState {
   @SerializedName("forward")
   public boolean forward;
   @SerializedName("backward")
   public boolean backward;
   @SerializedName("left")
   public boolean left;
   @SerializedName("right")
   public boolean right;
   @SerializedName("sneak")
   public boolean sneak;
   @SerializedName("jump")
   public boolean jump;

   public boolean isLeft() {
      return this.left;
   }

   public static MovementInputState getMovementInputState(ForwardEvent forwardEvent) {
      return new MovementInputState(forwardEvent.isForward(), forwardEvent.isBackward(), forwardEvent.isLeft(), forwardEvent.isRight(), forwardEvent.isSneak(), forwardEvent.isJump());
   }

   public boolean isJump() {
      return this.jump;
   }

   public void setForward(boolean flag) {
      this.forward = flag;
   }

   public void setBackward(boolean flag) {
      this.backward = flag;
   }

   public boolean isSneak() {
      return this.sneak;
   }

   public void setSneak(boolean flag) {
      this.sneak = flag;
   }

   public void setRight(boolean flag) {
      this.right = flag;
   }

   public boolean isBackward() {
      return this.backward;
   }

   public void setLeft(boolean flag) {
      this.left = flag;
   }

   public boolean isRight() {
      return this.right;
   }

   public static MovementInputState getMovementInputState(Input input) {
      return new MovementInputState(
         input.playerInput.forward(),
         input.playerInput.backward(),
         input.playerInput.left(),
         input.playerInput.right(),
         input.playerInput.sneak(),
         input.playerInput.jump()
      );
   }

   public boolean isForward() {
      return this.forward;
   }

   public MovementInputState(boolean flag, boolean flag2, boolean flag3, boolean flag4, boolean flag5, boolean flag6) {
      this.forward = flag;
      this.backward = flag2;
      this.left = flag3;
      this.right = flag4;
      this.sneak = flag5;
      this.jump = flag6;
   }

   public void setJump(boolean flag) {
      this.jump = flag;
   }
}
