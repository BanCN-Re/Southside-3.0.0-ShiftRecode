package dev.southside.utils;

import dev.southside.render.GlShader;
import dev.southside.render.shader.ShaderType;

public class VertexShader extends GlShader {
   public VertexShader(int n, String text, String text2) {
      super(ShaderType.VERTEX, n, text, text2);
   }
}
