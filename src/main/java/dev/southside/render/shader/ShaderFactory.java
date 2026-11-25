package dev.southside.render.shader;

import dev.southside.render.ShaderStorageBuffer;
import dev.southside.render.GlShader;
import dev.southside.render.ShaderProgram;
import dev.southside.render.UniformBufferObject;
import dev.southside.utils.IShaderConsumer;
import dev.southside.utils.IMinecraft;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.Reference;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Formatter;
import java.util.Locale;
import java.util.Map;
import net.minecraft.util.Identifier;
import org.apache.commons.io.IOUtils;
import org.lwjgl.opengl.GL45C;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShaderFactory {
   private static final Logger LOGGER = LoggerFactory.getLogger(ShaderFactory.class);
   private static final int DEFAULT_INT = 136;
   private static final Map<String, GlShader> compiledShaders = new Object2ObjectOpenHashMap();
   private static final Map<String, UniformBufferObject> ubo = new Object2ObjectOpenHashMap();
   private static final Map<String, ShaderStorageBuffer> ssbo = new Object2ObjectOpenHashMap();

   public static GlShader getGlShader(ByteBuffer buffer, String text, ShaderType shaderType) {
      int n = GL45C.glCreateShader(shaderType.glShaderType);
      if (n == 0) {
         return null;
      }

      try {
         MemoryStack stack = MemoryStack.stackPush();

         try {
            long l = stack.npointer(buffer);
            long l2 = stack.nint(buffer.remaining());
            GL45C.nglShaderSource(n, 1, l, l2);
         } catch (Throwable e) {
            if (stack != null) {
               try {
                  stack.close();
               } catch (Throwable e2) {
                  e.addSuppressed(e2);
               }
            }

            throw e;
         }

         if (stack != null) {
            stack.close();
         }
      } finally {
         Reference.reachabilityFence(buffer);
      }

      GL45C.glCompileShader(n);
      String text2 = MemoryUtil.memUTF8(buffer);
      if (GL45C.glGetShaderi(n, 35713) == 0) {
         String text3 = GL45C.glGetShaderInfoLog(n);
         GL45C.glDeleteShader(n);
         applyText(text2, text3);
         return null;
      } else {
         GlShader glShader = new GlShader(shaderType, n, text, text2);
         compiledShaders.put(text, glShader);
         return glShader;
      }
   }

   public static GlShader getGlShader(String text, String text2, ShaderType shaderType) {
      return getGlShader(MemoryUtil.memUTF8(text), text2, shaderType);
   }

   public static GlShader getGlShader(Identifier id, String text, ShaderType shaderType) {
      try (InputStream in = IMinecraft.mc.getResourceManager().open(id)) {
         return getGlShader(IOUtils.toString(in, StandardCharsets.UTF_8), text, shaderType);
      } catch (IOException e) {
         throw new RuntimeException("Failed to compile shader ", e);
      }
   }

   public static ShaderProgram getShaderProgram(GlShader... glShaderArray) {
      return getShaderProgram(false, null, glShaderArray);
   }

   public static ShaderProgram getShaderProgram(boolean flag, GlShader... glShaderArray) {
      return getShaderProgram(flag, null, glShaderArray);
   }

   public static ShaderProgram getShaderProgram(IShaderConsumer ishaderconsumer, GlShader... glShaderArray) {
      return getShaderProgram(false, ishaderconsumer, glShaderArray);
   }

   public static ShaderProgram getShaderProgram(boolean flag, IShaderConsumer ishaderconsumer, GlShader... glShaderArray) {
      int n = GL45C.glCreateProgram();

      for (GlShader glShader : glShaderArray) {
         glShader.attachShader(n);
      }

      GL45C.glLinkProgram(n);
      if (GL45C.glGetProgrami(n, 35714) == 0) {
         try {
            String text = GL45C.glGetProgramInfoLog(n);
            String[] textArray = new String[glShaderArray.length];

            for (int i = 0; i < glShaderArray.length; i++) {
               textArray[i] = glShaderArray[i].getName() + " / " + glShaderArray[i].getType().displayName;
            }

            String[] textArray2 = new String[glShaderArray.length];

            for (int j = 0; j < glShaderArray.length; j++) {
               textArray2[j] = glShaderArray[j].getSource();
            }

            performAction(textArray, textArray2, text);
            return null;
         } finally {
            GL45C.glDeleteProgram(n);
         }
      } else {
         for (GlShader glShader2 : glShaderArray) {
            glShader2.detachShader(n);
            if (glShader2.isValidEx()) {
               compiledShaders.remove(glShader2.getName()).performAction();
            }
         }

         ShaderProgram program = new ShaderProgram(n);
         if (flag) {
            program.applyText("MatrixBlock");
         }

         if (ishaderconsumer != null) {
            ishaderconsumer.performAction(program);
         }

         return program;
      }
   }

   public static UniformBufferObject getUniformBufferObject(int n) {
      UniformBufferObject uniformBufferObject = new UniformBufferObject();
      uniformBufferObject.allocate(n);
      return uniformBufferObject;
   }

   public static UniformBufferObject getUniformBufferObject(String text) {
      return ubo.computeIfAbsent(text, text2 -> getUniformBufferObject(136));
   }

   public static UniformBufferObject get(String text) {
      return ubo.get(text);
   }

   public static void applyText(String text) {
      UniformBufferObject uniformBufferObject = get(text);
      if (uniformBufferObject != null) {
         uniformBufferObject.bindBufferBase();
      }
   }

   public static ShaderStorageBuffer getShaderStorageBuffer(int n) {
      ShaderStorageBuffer shaderStorageBuffer = new ShaderStorageBuffer();
      shaderStorageBuffer.allocate((long)n);
      return shaderStorageBuffer;
   }

   public static ShaderStorageBuffer getShaderStorageBuffer(String text, int n) {
      return ssbo.computeIfAbsent(text, text2 -> getShaderStorageBuffer(n));
   }

   public static ShaderStorageBuffer getEx(String text) {
      return ssbo.get(text);
   }

   public static void applyText(String text, int n) {
      ShaderStorageBuffer ex = getEx(text);
      if (ex != null) {
         ex.bindBufferBase(n);
      }
   }

   public static void close() {
      ubo.values().forEach(UniformBufferObject::close);
      ssbo.values().forEach(ShaderStorageBuffer::close);
      ubo.clear();
      ssbo.clear();
   }

   private static void applyText(String text, String text2) {
      Formatter formatter = new Formatter();
      formatter.format("Shader compilation error%n");
      formatter.format("------------------------%n");
      String[] textArray = text.split("\n");

      for (int i = 0; i < textArray.length; i++) {
         formatter.format(Locale.ROOT, "%4d\t%s%n", i + 1, textArray[i]);
      }

      formatter.format(text2);
      LOGGER.error(formatter.toString());
   }

   private static void performAction(String[] textArray, String[] textArray2, String text) {
      Formatter formatter = new Formatter();
      formatter.format("Program linking error%n");
      formatter.format("---------------------%n");

      for (int i = 0; i < textArray.length; i++) {
         if (textArray2[i] != null) {
            formatter.format("%s%n", textArray[i]);
            String[] textArray3 = textArray2[i].split("\n");

            for (int j = 0; j < textArray3.length; j++) {
               formatter.format(Locale.ROOT, "%4d\t%s%n", j + 1, textArray3[j]);
            }
         }
      }

      formatter.format(text);
      LOGGER.error(formatter.toString());
   }
}
