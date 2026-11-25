package dev.southside.utils;

import java.awt.Color;
import net.minecraft.client.util.math.MatrixStack;

public class WorldBoxRenderer {
   public static void render(MatrixStack matrices) {
      BoxMeshRenderer.renderMesh.performAction();
      BoxMeshRenderer.renderMeshValue.performAction();
      BoxMeshRenderer.renderMesh.getMesh(matrices);
      BoxMeshRenderer.renderMeshValue.getMesh(matrices);
      BoxMeshRenderer.renderMesh.performActionAlt();
      BoxMeshRenderer.renderMeshValue.performActionAlt();
   }

   public static void performAction() {
      BoxMeshRenderer.renderMesh.performActionImpl();
      BoxMeshRenderer.renderMeshValue.performActionImpl();
      BoxMeshRenderer.renderMesh.performActionEx();
      BoxMeshRenderer.renderMeshValue.performActionEx();
   }

   public static void performAction(double d, double d2, double d3, double d4, double d5, double d6, Color color) {
      BoxMeshRenderer.renderMeshValue
         .performAction(
            BoxMeshRenderer.renderMeshValue.getMesh(d, d2, d3).getMesh(color).getHeight(), BoxMeshRenderer.renderMeshValue.getMesh(d4, d5, d6).getMesh(color).getHeight()
         );
   }
}
