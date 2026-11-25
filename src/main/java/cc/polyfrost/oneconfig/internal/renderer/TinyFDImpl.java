package cc.polyfrost.oneconfig.internal.renderer;

import cc.polyfrost.oneconfig.renderer.TinyFD;
import java.io.File;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

public class TinyFDImpl implements TinyFD {
   @Override
   public File getFile2(@Nullable String text, @Nullable String text2, @Nullable String[] textArray, @Nullable String text3) {
      MemoryStack stack = MemoryStack.stackPush();

      File file;
      try {
         PointerBuffer pointerBuffer = getPointerBuffer(stack, textArray);
         String text4 = TinyFileDialogs.tinyfd_saveFileDialog(text == null ? "Save" : text, text2, pointerBuffer, text3);
         file = text4 == null ? null : new File(text4);
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

      return file;
   }

   @Override
   public File getFile(@Nullable String text, @Nullable String text2, @Nullable String[] textArray, @Nullable String text3) {
      MemoryStack stack = MemoryStack.stackPush();

      File file;
      try {
         PointerBuffer pointerBuffer = getPointerBuffer(stack, textArray);
         String text4 = TinyFileDialogs.tinyfd_openFileDialog(text == null ? "Open file" : text, text2, pointerBuffer, text3, false);
         file = text4 == null ? null : new File(text4);
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

      return file;
   }

   @Override
   public File[] getFileArray(@Nullable String text, @Nullable String text2, @Nullable String[] textArray, @Nullable String text3) {
      MemoryStack stack = MemoryStack.stackPush();

      Object obj;
      label60: {
         File[] fileArray;
         try {
            PointerBuffer pointerBuffer = getPointerBuffer(stack, textArray);
            String text4 = TinyFileDialogs.tinyfd_openFileDialog(text == null ? "Open files" : text, text2, pointerBuffer, text3, true);
            if (text4 == null) {
               obj = null;
               break label60;
            }

            String[] textArray2 = text4.split("\\|");
            File[] fileArray2 = new File[textArray2.length];

            for (int i = 0; i < textArray2.length; i++) {
               fileArray2[i] = new File(textArray2[i]);
            }

            fileArray = fileArray2;
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

         return fileArray;
      }

      if (stack != null) {
         stack.close();
      }

      return (File[])obj;
   }

   @Override
   public File openFolderSelector(@Nullable String text, @Nullable String text2) {
      String text3 = TinyFileDialogs.tinyfd_selectFolderDialog(text == null ? "Select folder" : text, text2);
      return text3 == null ? null : new File(text3);
   }

   @Override
   public boolean showMessageBox(String text, String text2, @NotNull String text3, String text4, boolean flag) {
      return TinyFileDialogs.tinyfd_messageBox(text, text2, text3, text4, flag);
   }

   @Override
   public int showNotification(String text, String text2, String text3) {
      return TinyFileDialogs.tinyfd_notifyPopup(text, text2, text3);
   }

   private static PointerBuffer getPointerBuffer(MemoryStack stack, @Nullable String[] textArray) {
      if (textArray != null && textArray.length != 0) {
         PointerBuffer pointerBuffer = stack.mallocPointer(textArray.length);

         for (String text : textArray) {
            pointerBuffer.put(stack.UTF8(text));
         }

         return (PointerBuffer)pointerBuffer.flip();
      } else {
         return null;
      }
   }
}
