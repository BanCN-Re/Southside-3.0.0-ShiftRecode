package dev.southside.utils;

public interface ScissorHelper {
   ScissorHelper INSTANCE = LwjglManager.INSTANCE.getScissorHelper();

   ScissorRect scissor(long vg, float x, float y, float width, float height);

   void resetScissor(long l, ScissorRect clip);

   void clearScissors(long l);

   void save();

   void restore(long l);
}
