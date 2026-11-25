package dev.southside.utils;

import com.google.gson.annotations.SerializedName;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;
import com.sun.jna.platform.win32.BaseTSD.DWORD_PTR;
import com.sun.jna.platform.win32.BaseTSD.ULONG_PTR;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.LPARAM;
import com.sun.jna.platform.win32.WinDef.LRESULT;
import com.sun.jna.platform.win32.WinDef.WPARAM;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.StdCallLibrary.StdCallCallback;
import dev.southside.Southside;
import dev.southside.render.ImeOverlayManager;
import dev.southside.utils.misc.TextRecord;
import java.nio.charset.StandardCharsets;
import org.lwjgl.glfw.GLFWNativeWin32;

public final class Win32ImeBackend implements AutoCloseable {
   private static final int DEFAULT_INT = 8;
   private static final int DEFAULT_INT_VALUE = 130;
   private static final int DEFAULT_INT_DATA = 256;
   private static final int DEFAULT_INT_REF = 257;
   private static final int DEFAULT_INT_PRIMARY = 258;
   private static final int DEFAULT_INT_SECONDARY = 269;
   private static final int DEFAULT_INT_TERTIARY = 270;
   private static final int DEFAULT_INT_EXTRA = 271;
   private static final int DEFAULT_INT_ALT = 641;
   private static final int DEFAULT_INT_EX = 646;
   private static final int DEFAULT_INT_A = 8;
   private static final int DEFAULT_INT_B = 16;
   private static final int DEFAULT_INT_C = 128;
   private static final int DEFAULT_INT_D = 2048;
   private static final ULONG_PTR ulong_ptr = new ULONG_PTR(1397967181L);
   private static final LRESULT lresult = new LRESULT(0L);
   @SerializedName("manager")
   private final ImeOverlayManager manager;
   @SerializedName("window")
   private final HWND window;
   @SerializedName("imm")
   private final Win32ImeBackend.StdCallLibraryImpl3 imm = (Win32ImeBackend.StdCallLibraryImpl3)Native.load("imm32", Win32ImeBackend.StdCallLibraryImpl3.class);
   @SerializedName("controls")
   private final Win32ImeBackend.StdCallLibraryImpl controls = (Win32ImeBackend.StdCallLibraryImpl)Native.load("comctl32", Win32ImeBackend.StdCallLibraryImpl.class);
   @SerializedName("user")
   private final Win32ImeBackend.StdCallLibraryImpl2 user = (Win32ImeBackend.StdCallLibraryImpl2)Native.load("user32", Win32ImeBackend.StdCallLibraryImpl2.class);
   @SerializedName("callback")
   private final Win32ImeBackend.StdCallCallbackImpl callback = this::getLresult;
   @SerializedName("originalContext")
   private final Pointer originalContext;
   @SerializedName("inputContext")
   private final Pointer inputContext;
   @SerializedName("ownsContext")
   private final boolean ownsContext;
   @SerializedName("installed")
   private boolean installed;
   @SerializedName("managed")
   private boolean managed;
   @SerializedName("textInputEnabled")
   private boolean textInputEnabled;
   @SerializedName("composing")
   private boolean composing;
   @SerializedName("suppressEnter")
   private boolean suppressEnter;
   @SerializedName("suppressEscape")
   private boolean suppressEscape;
   @SerializedName("failed")
   private boolean failed;

   public Win32ImeBackend(long l, ImeOverlayManager imeOverlayManager) {
      this.manager = imeOverlayManager;
      this.window = new HWND(new Pointer(GLFWNativeWin32.glfwGetWin32Window(l)));
      this.originalContext = this.imm.ImmGetContext(this.window);
      if (this.originalContext != null) {
         this.imm.ImmReleaseContext(this.window, this.originalContext);
      }

      this.ownsContext = this.originalContext == null;
      this.inputContext = this.ownsContext ? this.imm.ImmCreateContext() : this.originalContext;
      if (this.inputContext == null) {
         throw new IllegalStateException("Could not create an IME input context");
      }

      this.installed = this.controls.SetWindowSubclass(this.window, this.callback, ulong_ptr, new DWORD_PTR(0L));
      if (!this.installed) {
         if (this.ownsContext) {
            this.imm.ImmDestroyContext(this.inputContext);
         }

         throw new IllegalStateException("Could not attach the IME window handler");
      }
   }

   public boolean isValid() {
      return this.installed && !this.failed;
   }

   public void performAction() {
      if (!this.managed && this.isValid()) {
         this.managed = true;
         this.textInputEnabled = false;
         this.imm.ImmAssociateContext(this.window, null);
      }
   }

   public void performActionEx() {
      if (this.managed && !this.textInputEnabled && this.isValid()) {
         this.textInputEnabled = true;
         this.suppressEnter = this.isKeyDown(13);
         this.suppressEscape = this.isKeyDown(27);
         this.imm.ImmAssociateContext(this.window, this.inputContext);
         this.performActionDirect();
      }
   }

   public void performActionAlt() {
      if (this.managed && this.textInputEnabled) {
         this.performActionDirect();
         this.textInputEnabled = false;
         this.imm.ImmAssociateContext(this.window, null);
      }
   }

   public void performActionImpl() {
      if (this.managed) {
         this.performActionDirect();
         this.textInputEnabled = false;
         this.managed = false;
         this.imm.ImmAssociateContext(this.window, this.originalContext);
      }
   }

   public void performActionDirect() {
      this.composing = false;
      if (this.textInputEnabled) {
         this.imm.ImmNotifyIME(this.inputContext, 21, 4, 0);
      }

      this.manager.performAction((TextRecord)null);
   }

   public void performAction(int n, int n2, int n3, int n4) {
      if (this.textInputEnabled && this.isValid()) {
         Win32ImeBackend.Inner4 inner4 = new Win32ImeBackend.Inner4();
         inner4.style = 128;
         inner4.position.x = n;
         inner4.position.y = n4;
         inner4.area.left = n;
         inner4.area.top = n2;
         inner4.area.right = Math.max(n + 1, n3);
         inner4.area.bottom = Math.max(n2 + 1, n4);
         this.imm.ImmSetCandidateWindow(this.inputContext, inner4);
         Win32ImeBackend.Inner3 inner3 = new Win32ImeBackend.Inner3();
         inner3.style = 2;
         inner3.position.x = n;
         inner3.position.y = n2;
         this.imm.ImmSetCompositionWindow(this.inputContext, inner3);
      }
   }

   private LRESULT getLresult(HWND hwnd, int n, WPARAM wparam, LPARAM lparam, ULONG_PTR ulong_ptr2, DWORD_PTR dword_ptr) {
      try {
         if (n == 130) {
            this.close();
         } else if (n == 8) {
            this.manager.performActionDirect();
         } else if (this.managed && this.textInputEnabled && !this.failed) {
            switch (n) {
               case 256:
                  int n2 = wparam.intValue();
                  if (n2 != 13 || !this.composing && !this.suppressEnter) {
                     if (n2 != 27 || !this.composing && !this.suppressEscape) {
                        if (!this.composing || n2 != 8 && n2 != 9 && n2 != 46 && (n2 < 33 || n2 > 40)) {
                           break;
                        }

                        return lresult;
                     }

                     this.suppressEscape = true;
                     return lresult;
                  }

                  this.suppressEnter = true;
                  return lresult;
               case 257:
                  if (wparam.intValue() == 13) {
                     this.suppressEnter = false;
                  }

                  if (wparam.intValue() == 27) {
                     this.suppressEscape = false;
                  }
                  break;
               case 258:
                  if (!this.composing && (wparam.intValue() != 13 || !this.suppressEnter)) {
                     break;
                  }

                  return lresult;
               case 269:
                  this.composing = true;
                  return lresult;
               case 270:
                  this.suppressEnter = this.suppressEnter | this.isKeyDown(13);
                  this.suppressEscape = this.suppressEscape | this.isKeyDown(27);
                  this.composing = false;
                  this.manager.performAction((TextRecord)null);
                  return lresult;
               case 271:
                  this.onKeyTyped(lparam.intValue());
                  return lresult;
               case 641:
                  return this.controls.DefSubclassProc(hwnd, n, wparam, new LPARAM(lparam.longValue() & -2147483649L));
               case 646:
                  return lresult;
            }
         }
      } catch (Throwable e) {
         this.failed = true;

         try {
            this.performActionImpl();
         } catch (Throwable e2) {
         }

         Southside.LOGGER.warn("IME integration disabled after a window callback failure", e);
      }

      return this.controls.DefSubclassProc(hwnd, n, wparam, lparam);
   }

   private boolean isKeyDown(int n) {
      return (this.user.GetKeyState(n) & '耀') != 0;
   }

   private void onKeyTyped(int n) {
      if ((n & 2048) != 0) {
         this.suppressEnter = this.suppressEnter | this.isKeyDown(13);
         this.manager.performAction((TextRecord)null);
         this.manager.applyText(this.formatText(2048));
      }

      if ((n & 152) != 0) {
         String text = this.formatText(8);
         this.composing = !text.isEmpty();
         if (!this.composing) {
            this.manager.performAction((TextRecord)null);
            return;
         }

         int n2 = this.imm.ImmGetCompositionStringW(this.inputContext, 128, null, 0);
         byte[] byteList = this.getByteList(16);
         int n3 = -1;
         int n4 = -1;

         for (int i = 0; i < Math.min(text.length(), byteList.length); i++) {
            if (byteList[i] == 1 || byteList[i] == 3) {
               if (n3 < 0) {
                  n3 = i;
               }

               n4 = i + 1;
            }
         }

         this.manager.performAction(new TextRecord(text, n2, Math.max(0, n3), Math.max(0, n4)));
      } else if (n == 0 || (n & 2048) != 0) {
         this.composing = false;
         this.manager.performAction((TextRecord)null);
      }
   }

   private String formatText(int n) {
      return new String(this.getByteList(n), StandardCharsets.UTF_16LE);
   }

   private byte[] getByteList(int n) {
      int n2 = this.imm.ImmGetCompositionStringW(this.inputContext, n, null, 0);
      if (n2 > 0 && n2 <= 65536) {
         Memory memory = new Memory(n2);

         byte[] bytes;
         try {
            int n3 = this.imm.ImmGetCompositionStringW(this.inputContext, n, memory, n2);
            bytes = n3 <= 0 ? new byte[0] : memory.getByteArray(0L, Math.min(n3, n2));
         } catch (Throwable e) {
            try {
               memory.close();
            } catch (Throwable e2) {
               e.addSuppressed(e2);
            }

            throw e;
         }

         memory.close();
         return bytes;
      } else {
         return new byte[0];
      }
   }

   @Override
   public void close() {
      if (this.installed) {
         this.performActionImpl();
         if (this.controls.RemoveWindowSubclass(this.window, this.callback, ulong_ptr)) {
            this.installed = false;
            if (this.ownsContext) {
               this.imm.ImmDestroyContext(this.inputContext);
            }
         }
      }
   }

   @FieldOrder({"left", "top", "right", "bottom"})
   public static class Inner extends Structure {
      @SerializedName("left")
      public int left;
      @SerializedName("top")
      public int top;
      @SerializedName("right")
      public int right;
      @SerializedName("bottom")
      public int bottom;
   }

   @FieldOrder({"x", "y"})
   public static class Inner2 extends Structure {
      @SerializedName("x")
      public int x;
      @SerializedName("y")
      public int y;
   }

   @FieldOrder({"style", "position", "area"})
   public static class Inner3 extends Structure {
      @SerializedName("style")
      public int style;
      @SerializedName("position")
      public Win32ImeBackend.Inner2 position = new Win32ImeBackend.Inner2();
      @SerializedName("area")
      public Win32ImeBackend.Inner area = new Win32ImeBackend.Inner();
   }

   @FieldOrder({"index", "style", "position", "area"})
   public static class Inner4 extends Structure {
      @SerializedName("index")
      public int index;
      @SerializedName("style")
      public int style;
      @SerializedName("position")
      public Win32ImeBackend.Inner2 position = new Win32ImeBackend.Inner2();
      @SerializedName("area")
      public Win32ImeBackend.Inner area = new Win32ImeBackend.Inner();
   }

   public interface StdCallCallbackImpl extends StdCallCallback {
      LRESULT callback(HWND hwnd, int n, WPARAM wparam, LPARAM lparam, ULONG_PTR ulong_ptr, DWORD_PTR dword_ptr);
   }

   public interface StdCallLibraryImpl extends StdCallLibrary {
      boolean SetWindowSubclass(HWND hwnd, Win32ImeBackend.StdCallCallbackImpl stdCallCallbackImpl, ULONG_PTR ulong_ptr, DWORD_PTR dword_ptr);

      boolean RemoveWindowSubclass(HWND hwnd, Win32ImeBackend.StdCallCallbackImpl stdCallCallbackImpl, ULONG_PTR ulong_ptr);

      LRESULT DefSubclassProc(HWND hwnd, int n, WPARAM wparam, LPARAM lparam);
   }

   public interface StdCallLibraryImpl2 extends StdCallLibrary {
      short GetKeyState(int n);
   }

   public interface StdCallLibraryImpl3 extends StdCallLibrary {
      Pointer ImmGetContext(HWND hwnd);

      boolean ImmReleaseContext(HWND hwnd, Pointer pointer);

      Pointer ImmCreateContext();

      boolean ImmDestroyContext(Pointer pointer);

      Pointer ImmAssociateContext(HWND hwnd, Pointer pointer);

      boolean ImmNotifyIME(Pointer pointer, int n, int n2, int n3);

      int ImmGetCompositionStringW(Pointer pointer, int n, Pointer pointer2, int n2);

      boolean ImmSetCandidateWindow(Pointer pointer, Win32ImeBackend.Inner4 inner4);

      boolean ImmSetCompositionWindow(Pointer pointer, Win32ImeBackend.Inner3 inner3);
   }
}
