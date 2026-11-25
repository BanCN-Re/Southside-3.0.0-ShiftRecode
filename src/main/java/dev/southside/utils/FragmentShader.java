package dev.southside.utils;

import dev.southside.render.GlShader;
import dev.southside.render.shader.ShaderType;

public class FragmentShader extends GlShader {
   public FragmentShader(int n, String text, String text2) {
      super(ShaderType.FRAGMENT, n, text, text2);
   }
}
