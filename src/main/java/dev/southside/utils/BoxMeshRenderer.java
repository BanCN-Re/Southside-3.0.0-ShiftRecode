package dev.southside.utils;

import dev.southside.render.pipeline.Mesh;
import java.awt.Color;
import net.minecraft.util.math.Box;

public class BoxMeshRenderer {
   public static Mesh renderMesh;
   public static Mesh renderMeshValue;

   public static void renderBox(Box box, Color color) {
      renderMeshValue.renderBoxEx(box, color);
   }

   public static void renderBoxEx(Box box, Color color) {
      renderMesh.renderBox(box, color);
   }
}
