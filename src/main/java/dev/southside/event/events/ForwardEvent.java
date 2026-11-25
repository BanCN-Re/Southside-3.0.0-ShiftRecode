package dev.southside.event.events;

import com.google.gson.annotations.SerializedName;
import dev.southside.utils.CancellableEvent;

public class ForwardEvent extends CancellableEvent {
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
   @SerializedName("sprint")
   public boolean sprint;
   @SerializedName("modified")
   public boolean modified;

   public ForwardEvent(boolean flag, boolean flag2, boolean flag3, boolean flag4, boolean flag5, boolean flag6, boolean flag7, boolean flag8) {
      this.forward = flag;
      this.backward = flag2;
      this.left = flag3;
      this.right = flag4;
      this.sneak = flag5;
      this.jump = flag6;
      this.sprint = flag7;
      this.modified = flag8;
   }

   public boolean isForward() {
      return this.forward;
   }

   public void setForward(boolean flag) {
      this.forward = flag;
   }

   public boolean isBackward() {
      return this.backward;
   }

   public void setBackward(boolean flag) {
      this.backward = flag;
   }

   public boolean isLeft() {
      return this.left;
   }

   public void setLeft(boolean flag) {
      this.left = flag;
   }

   public boolean isRight() {
      return this.right;
   }

   public void setRight(boolean flag) {
      this.right = flag;
   }

   public boolean isSneak() {
      return this.sneak;
   }

   public void setSneak(boolean flag) {
      this.sneak = flag;
   }

   public boolean isJump() {
      return this.jump;
   }

   public void setJump(boolean flag) {
      this.jump = flag;
   }

   public boolean isSprint() {
      return this.sprint;
   }

   public void setSprint(boolean flag) {
      this.sprint = flag;
   }

   public boolean isModified() {
      return this.modified;
   }

   public void setModified(boolean flag) {
      this.modified = flag;
   }
}
