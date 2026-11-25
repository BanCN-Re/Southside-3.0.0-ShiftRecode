package dev.southside.utils;

import com.google.gson.annotations.SerializedName;
import net.minecraft.entity.LivingEntity;

public class EntityRenderBounds {
   @SerializedName("entity")
   public LivingEntity entity;
   @SerializedName("value1")
   public float value1;
   @SerializedName("value2")
   public float value2;
   @SerializedName("value3")
   public float value3;
   @SerializedName("value4")
   public float value4;
   @SerializedName("value5")
   public float value5;
   @SerializedName("value6")
   public float value6;

   public EntityRenderBounds(LivingEntity living, float f, float f2, float f3, float f4, float f5, float f6) {
      this.entity = living;
      this.value1 = f;
      this.value2 = f2;
      this.value3 = f3;
      this.value4 = f4;
      this.value5 = f5;
      this.value6 = f6;
   }

   public LivingEntity getEntity() {
      return this.entity;
   }

   public void setEntity(LivingEntity living) {
      this.entity = living;
   }

   public float getValue1() {
      return this.value1;
   }

   public void setValue1(float f) {
      this.value1 = f;
   }

   public float getValue2() {
      return this.value2;
   }

   public void setValue2(float f) {
      this.value2 = f;
   }

   public float getValue3() {
      return this.value3;
   }

   public void setValue3(float f) {
      this.value3 = f;
   }

   public float getValue4() {
      return this.value4;
   }

   public void setValue4(float f) {
      this.value4 = f;
   }

   public float getValue5() {
      return this.value5;
   }

   public void setValue5(float f) {
      this.value5 = f;
   }

   public float getValue6() {
      return this.value6;
   }

   public void setValue6(float f) {
      this.value6 = f;
   }
}
