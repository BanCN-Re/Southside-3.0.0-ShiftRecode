package dev.southside.utils.freecookie;

import cc.polyfrost.oneconfig.libs.universal.UKeyboard;
import com.google.gson.annotations.SerializedName;
import dev.southside.event.EventTarget;
import dev.southside.event.Stage;
import dev.southside.event.events.RenderStageEvent;
import dev.southside.render.ImeOverlayManager;
import dev.southside.utils.InputHandler;
import dev.southside.utils.OneConfigEventManager;
import dev.southside.utils.SvgAsset;
import dev.southside.utils.OneConfigColors;
import dev.southside.utils.NanoVGTextWrapper;
import dev.southside.utils.OneConfigFonts;
import dev.southside.utils.MathClampUtils;
import dev.southside.utils.IImeTextTarget;
import dev.southside.utils.OneConfigPlatform;
import dev.southside.utils.ScissorHelper;
import dev.southside.utils.NanoVGHelper;
import dev.southside.utils.OneConfigGui;
import dev.southside.utils.BasicElement;
import dev.southside.utils.ScissorRect;
import java.util.ArrayList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

public class FreeCookieAPI extends BasicElement implements IImeTextTarget {
   @SerializedName("defaultText")
   protected final String defaultText;
   @SerializedName("multiLine")
   protected final boolean multiLine;
   @SerializedName("radius")
   private float radius;
   @SerializedName("boarderThickness")
   private float boarderThickness = 1.0F;
   @SerializedName("textSize")
   private float textSize = 12.0F;
   @SerializedName("input")
   public String input;
   @SerializedName("selectedText")
   protected String selectedText;
   @SerializedName("password")
   protected boolean password;
   @SerializedName("caretPos")
   public int caretPos;
   @SerializedName("x")
   protected float x;
   @SerializedName("y")
   protected float y;
   @SerializedName("start")
   protected float start;
   @SerializedName("end")
   protected float end;
   @SerializedName("startLine")
   protected int startLine;
   @SerializedName("endLine")
   protected int endLine;
   @SerializedName("vg")
   protected long vg;
   @SerializedName("prevCaret")
   public int prevCaret = 0;
   @SerializedName("isDoubleClick")
   protected boolean isDoubleClick = false;
   @SerializedName("onlyNums")
   public boolean onlyNums = false;
   @SerializedName("errored")
   public boolean errored = false;
   @SerializedName("centered")
   protected boolean centered = false;
   @SerializedName("icon")
   protected SvgAsset icon;
   @SerializedName("wrappedText")
   protected ArrayList<String> wrappedText = null;
   @SerializedName("clickTimeD1")
   private long clickTimeD1;
   @SerializedName("pendingDoubleClick")
   private boolean pendingDoubleClick;
   @SerializedName("inputHandler")
   private InputHandler inputHandler;
   private static final long DEFAULT_LONG = 300L;
   @SerializedName("lines")
   private int lines = 1;
   private static boolean active = false;
   public static boolean activeValue = false;

   public FreeCookieAPI(int n, int n2, String text, boolean flag, boolean flag2, SvgAsset svg, float f) {
      super(n, n2, false);
      if (!active) {
         active = true;
         OneConfigEventManager.INSTANCE.register(new FreeCookieAPI.Inner());
      }

      this.multiLine = flag;
      this.defaultText = text;
      this.password = flag2;
      this.input = "";
      this.icon = svg;
      this.radius = f;
   }

   public FreeCookieAPI(int n, int n2, String text, boolean flag, boolean flag2, SvgAsset svg) {
      this(n, n2, text, flag, flag2, null, 12.0F);
   }

   public FreeCookieAPI(int n, int n2, String text, boolean flag, boolean flag2) {
      this(n, n2, text, flag, flag2, null, 12.0F);
   }

   public FreeCookieAPI(int n, int n2, boolean flag, String text) {
      this(n, n2, text, false, false, null, 12.0F);
      this.centered = flag;
   }

   public FreeCookieAPI(int n, int n2, boolean flag, float f) {
      this(n, n2, "", false, false, null, f);
      this.setCentered(flag);
   }

   public static boolean isKeyDown(int n) {
      return n != 167 && n >= 32 && n != 127;
   }

   public void setOnlyNums(boolean flag) {
      this.onlyNums = flag;
   }

   public String getInput() {
      return this.input;
   }

   public void setInput(String text) {
      this.input = text;
   }

   public boolean isPassword() {
      return this.password;
   }

   public void setPassword(boolean flag) {
      this.password = flag;
   }

   public void setCentered(boolean flag) {
      this.centered = flag;
   }

   public boolean isErrored() {
      return this.errored;
   }

   public void setErrored(boolean flag) {
      this.errored = flag;
   }

   @Override
   public void draw(long l, float f, float f2, InputHandler input2) {
      ScissorHelper scissor = ScissorHelper.INSTANCE;
      NanoVGHelper nvg = NanoVGHelper.INSTANCE;
      this.x = f;
      this.y = f2;
      this.vg = l;

      try {
         this.inputHandler = input2;
         nvg.drawRoundedRect(
            l,
            f + this.boarderThickness,
            f2 + this.boarderThickness,
            super.width - this.boarderThickness * 2.0F,
            super.height - this.boarderThickness * 2.0F,
            OneConfigColors.GRAY_800,
            this.radius
         );
         int n = this.errored ? OneConfigColors.ERROR_700 : OneConfigColors.GRAY_700;
         if (!super.toggled) {
            nvg.drawRoundedRect(l, f, f2, super.width - 0.5F, super.height - 0.5F, n, this.radius, this.boarderThickness);
         } else {
            nvg.drawRoundedRect(
               l,
               f,
               f2,
               super.width - 0.5F,
               super.height - 0.5F,
               this.errored ? OneConfigColors.ERROR_600 : OneConfigColors.PRIMARY_500,
               this.radius,
               this.boarderThickness
            );
         }

         ScissorRect clip = scissor.scissor(l, f, f2, super.width, super.height);
         super.performAction(f, f2, input2);
         if (OneConfigPlatform.getMousePlatform().isKeyDown(0) && !super.hovered) {
            this.setInput();
            super.toggled = false;
         }

         int n2 = super.toggled ? OneConfigColors.WHITE : OneConfigColors.WHITE_60;
         if (!super.toggled) {
            this.caretPos = this.input.length();
         }

         if (this.caretPos > this.input.length()) {
            this.caretPos = this.input.length();
         }

         if (this.prevCaret > this.input.length()) {
            this.prevCaret = this.input.length();
         }

         if (this.caretPos < 0) {
            this.caretPos = 0;
         }

         if (this.prevCaret < 0) {
            this.prevCaret = 0;
         }

         if (this.icon != null) {
            nvg.draw(l, this.icon, f + 10.0F, f2 + super.height / 2.0F - 10.0F, 20.0F, 20.0F, n2);
            f += 36.0F;
            this.x = f;
         }

         StringBuilder sb = new StringBuilder();
         float f3;
         if (this.multiLine) {
            this.wrappedText = NanoVGTextWrapper.collectItems(l, this.input, super.width - 24, this.textSize, OneConfigFonts.REGULAR);
            this.lines = this.wrappedText.size();
            if (!super.toggled) {
               this.caretPos = this.wrappedText.get(this.wrappedText.size() - 1).length();
            }

            int clamped = (int)MathClampUtils.clamp(this.computeInt(this.caretPos), 0.0F, this.wrappedText.size() - 1);
            f3 = nvg.getTextWidth(l, this.wrappedText.get(clamped).substring(0, this.computeIntEx(this.caretPos)), this.textSize, OneConfigFonts.REGULAR);
         } else if (!this.password) {
            f3 = nvg.getTextWidth(l, this.input.substring(0, this.caretPos), this.textSize, OneConfigFonts.REGULAR);
         } else {
            for (int i = 0; i < this.input.length(); i++) {
               sb.append("*");
            }

            f3 = nvg.getTextWidth(l, sb.substring(0, this.caretPos), this.textSize, OneConfigFonts.REGULAR);
         }

         float f4 = this.computeFloat(l, this.input) / 2.0F;
         if (this.start != 0.0F && this.end != 0.0F && super.toggled) {
            if (!this.multiLine) {
               nvg.drawRect(l, this.start, f2 + super.height / 2.0F - 10.0F, this.end, 20.0F, OneConfigColors.GRAY_300);
            } else if (this.startLine == this.endLine) {
               nvg.drawRect(l, this.start, f2 + 10.0F + 24 * this.startLine, this.end, 20.0F, OneConfigColors.GRAY_300);
            } else {
               nvg.drawRect(l, this.start, f2 + 10.0F + 24 * this.startLine, super.width - 24, 20.0F, OneConfigColors.GRAY_300);

               for (int j = this.startLine + 1; j < this.endLine; j++) {
                  nvg.drawRect(l, f + 12.0F, f2 + 10.0F + 24 * j, super.width - 24, 20.0F, OneConfigColors.GRAY_300);
               }

               nvg.drawRect(l, f + 12.0F, f2 + 10.0F + 24 * this.endLine, this.end, 20.0F, OneConfigColors.GRAY_300);
            }
         }

         if (super.hovered && OneConfigPlatform.getMousePlatform().isKeyDown(0) && !this.isDoubleClick) {
            if (this.multiLine) {
               int n3 = Math.max(0, Math.min(this.wrappedText.size() - 1, (int)Math.floor((input2.getProgressEx() - f2 - 10.0F) / 24.0F)));
               this.caretPos = this.computeInt(input2.getProgress(), this.wrappedText.get(n3));

               for (int k = 0; k < n3; k++) {
                  this.caretPos = this.caretPos + this.wrappedText.get(k).length();
               }
            } else {
               this.caretPos = this.computeInt(input2.getProgress(), this.input);
            }

            if (this.caretPos > this.prevCaret) {
               if (!this.centered) {
                  this.start = f + 12.0F + this.computeFloat(l, this.input.substring(0, this.prevCaret));
               } else {
                  this.start = f + super.width / 2.0F - f4 + this.computeFloat(l, this.input.substring(0, this.prevCaret));
               }

               this.end = this.computeFloat(l, this.input.substring(this.prevCaret, this.caretPos));
               this.selectedText = this.input.substring(this.prevCaret, this.caretPos);
            } else {
               if (!this.centered) {
                  this.start = f + 12.0F + this.computeFloat(l, this.input.substring(0, this.prevCaret));
               } else {
                  this.start = f + super.width / 2.0F - f4 + this.computeFloat(l, this.input.substring(0, this.prevCaret));
               }

               this.end = -this.computeFloat(l, this.input.substring(this.caretPos, this.prevCaret));
               this.selectedText = this.input.substring(this.caretPos, this.prevCaret);
            }
         }

         float globalAlpha = nvg.getGlobalAlpha();
         if (super.toggled && !super.disabled && MinecraftClient.getInstance().currentScreen instanceof OneConfigGui) {
            float f5 = this.centered && !this.multiLine ? f + super.width / 2.0F - f4 + f3 : f + f3 + 12.0F;
            float f6 = this.multiLine ? f2 + 20.0F + this.computeInt(this.caretPos) * 24 : f2 + super.height / 2.0F;
            ImeOverlayManager.INSTANCE.performAction(this, l, f5, f6 - 10.0F, f5 + 1.0F, f6 + 10.0F, this.password);
         } else {
            ImeOverlayManager.INSTANCE.performActionEx(this);
         }

         if (super.disabled) {
            nvg.setAlpha(l, 0.5F);
         }

         if (super.toggled) {
            if (this.multiLine) {
               float f7 = f2 + 20.0F + this.computeInt(this.caretPos) * 24;
               nvg.drawLine(l, f + f3 + 12.0F, f7 - 10.0F, f + f3 + 12.0F, f7 + 10.0F, 1.0F, OneConfigColors.WHITE);
            } else if (!this.centered) {
               nvg.drawLine(
                  l,
                  f + f3 + 12.0F,
                  f2 + super.height / 2.0F - 10.0F,
                  f + f3 + 12.0F,
                  f2 + super.height / 2.0F + 10.0F,
                  1.0F,
                  OneConfigColors.WHITE
               );
            } else {
               nvg.drawLine(
                  l,
                  f + super.width / 2.0F - f4 + f3,
                  f2 + super.height / 2.0F - 10.0F,
                  f + super.width / 2.0F - f4 + f3,
                  f2 + super.height / 2.0F + 10.0F,
                  1.0F,
                  OneConfigColors.WHITE
               );
            }
         }

         if (this.input.equals("")) {
            if (this.multiLine) {
               nvg.drawTextEx(l, this.defaultText, f + 12.0F, f2 + 16.0F, n2, this.textSize, OneConfigFonts.REGULAR);
            } else if (!this.centered) {
               nvg.drawTextEx(l, this.defaultText, f + 12.0F, f2 + super.height / 2.0F, n2, this.textSize, OneConfigFonts.REGULAR);
            } else {
               nvg.drawTextEx(
                  l, this.defaultText, f + super.width / 2.0F - f4, f2 + super.height / 2.0F, n2, this.textSize, OneConfigFonts.REGULAR
               );
            }
         }

         if (!this.password) {
            if (this.multiLine) {
               float f8 = f2 + 20.0F;

               for (String text : this.wrappedText) {
                  nvg.drawTextEx(l, text, f + 12.0F, f8, n2, this.textSize, OneConfigFonts.REGULAR);
                  f8 += 24.0F;
               }
            } else if (!this.centered) {
               nvg.drawTextEx(l, this.input, f + 12.0F, f2 + super.height / 2.0F, n2, this.textSize, OneConfigFonts.REGULAR);
            } else {
               nvg.drawTextEx(l, this.input, f + super.width / 2.0F - f4, f2 + super.height / 2.0F, n2, this.textSize, OneConfigFonts.REGULAR);
            }
         } else {
            nvg.drawTextEx(l, sb.toString(), f + 12.0F, f2 + super.height / 2.0F, n2, this.textSize, OneConfigFonts.REGULAR);
         }

         nvg.setAlpha(l, globalAlpha);
         scissor.resetScissor(l, clip);
      } catch (Exception e) {
         e.printStackTrace();
      }

      if (super.toggled) {
         activeValue = true;
      }
   }

   public void onKeyTyped(int n) {
      if ((!UKeyboard.hasControlDown() || ImeOverlayManager.INSTANCE.isCommittingText()) && Character.isValidCodePoint(n)) {
         this.commitText(Character.toString(n));
      }
   }

   @Override
   public void commitText(String text) {
      if (super.toggled && !super.disabled) {
         StringBuilder sb = new StringBuilder();
         text.codePoints()
            .filter(n -> isKeyDown(n) && Character.isDefined(n) && (!this.onlyNums || Character.isDigit(n) || n == 46))
            .forEach(sb::appendCodePoint);
         if (!sb.isEmpty()) {
            int n2 = Math.max(0, Math.min(this.input.length(), this.caretPos));
            int n3 = n2;
            if (this.selectedText != null) {
               n2 = Math.min(this.input.length(), Math.max(0, Math.min(this.caretPos, this.prevCaret)));
               n3 = Math.max(n2, Math.min(this.input.length(), Math.max(this.caretPos, this.prevCaret)));
            }

            this.input = this.input.substring(0, n2) + sb + this.input.substring(n3);
            this.caretPos = n2 + sb.length();
            this.prevCaret = this.caretPos;
            this.selectedText = null;
            this.start = this.end = 0.0F;
         }
      }
   }

   @Override
   public void performActionEx() {
      super.toggled = false;
      this.selectedText = null;
      this.start = this.end = 0.0F;
   }

   public void onKeyTypedEx(int n) {
      if (n == 335) {
         n = 257;
      }

      try {
         if (super.toggled) {
            if (Screen.isCopy(n)) {
               if (this.selectedText != null && this.start != 0.0F && this.end != 0.0F) {
                  MinecraftClient.getInstance().keyboard.setClipboard(this.selectedText);
               }

               return;
            }

            if (Screen.isPaste(n)) {
               try {
                  String clipboard = MinecraftClient.getInstance().keyboard.getClipboard();
                  this.commitText(clipboard);
                  return;
               } catch (Exception e) {
                  e.printStackTrace();
               }
            }

            if (n == 261) {
               this.input = "";
            }

            if (UKeyboard.hasControlDown()) {
               if (n == 259 && !UKeyboard.isKeyDownEx(n)) {
                  try {
                     this.input = this.input.substring(0, this.input.lastIndexOf(" "));
                     this.caretPos = this.input.length();
                  } catch (Exception e2) {
                     this.input = "";
                     this.caretPos = 0;
                  }

                  return;
               }

               if (Screen.isSelectAll(n)) {
                  this.prevCaret = 0;
                  this.caretPos = this.input.length();
                  this.start = !this.centered ? this.x + 12.0F : this.x + super.width / 2.0F - this.computeFloat(this.vg, this.input) / 2.0F;
                  this.selectedText = this.input;
                  if (!this.multiLine) {
                     this.end = this.computeFloat(this.vg, this.input);
                  }

                  if (this.multiLine) {
                     this.end = this.computeFloat(this.vg, this.wrappedText.get(this.wrappedText.size() - 1));
                     this.startLine = 0;
                     this.endLine = this.wrappedText.size() - 1;
                  }

                  return;
               }

               if (Screen.isCut(n)) {
                  if (this.selectedText == null || this.start == 0.0F || this.end == 0.0F) {
                     return;
                  }

                  MinecraftClient.getInstance().keyboard.setClipboard(this.selectedText);
                  n = 259;
               }

               if (n == 263) {
                  this.caretPos = this.input.substring(0, this.caretPos).lastIndexOf(32) + 1;
               }

               if (n == 262) {
                  this.caretPos = this.input.indexOf(32, this.caretPos);
                  if (this.caretPos == -1) {
                     this.caretPos = this.input.length();
                  }
               }
            }

            if (n == 259) {
               if (this.input.length() > 0) {
                  if (this.start != 0.0F && this.end != 0.0F) {
                     this.start = 0.0F;
                     this.end = 0.0F;
                     if (this.caretPos > this.prevCaret) {
                        this.input = this.input.substring(0, this.prevCaret) + this.input.substring(this.caretPos);
                        this.caretPos = this.prevCaret;
                     }

                     if (this.caretPos < this.prevCaret) {
                        this.input = this.input.substring(0, this.caretPos) + this.input.substring(this.prevCaret);
                     }

                     this.prevCaret = this.caretPos;
                     this.selectedText = null;
                     return;
                  }

                  if (this.caretPos > 0) {
                     int n2 = this.input.offsetByCodePoints(this.caretPos, -1);
                     this.input = this.input.substring(0, n2) + this.input.substring(this.caretPos);
                     this.caretPos = n2;
                  }

                  this.prevCaret = this.caretPos;
                  this.selectedText = null;
               }

               return;
            }

            if (n == 258) {
               if (this.onlyNums) {
                  return;
               }

               this.input = this.input + "    ";
               this.caretPos += 4;
               return;
            }

            if (n == 262) {
               if (this.caretPos < this.input.length()) {
                  this.caretPos = this.input.offsetByCodePoints(this.caretPos, 1);
               }

               this.selectedText = null;
               this.prevCaret = this.caretPos;
               if (this.start != 0.0F && this.end != 0.0F) {
                  this.start = 0.0F;
                  this.end = 0.0F;
               }

               return;
            }

            if (n == 263) {
               if (this.caretPos > 0) {
                  this.caretPos = this.input.offsetByCodePoints(this.caretPos, -1);
               }

               this.selectedText = null;
               this.prevCaret = this.caretPos;
               if (this.start != 0.0F && this.end != 0.0F) {
                  this.start = 0.0F;
                  this.end = 0.0F;
               }

               return;
            }

            if (n != 265 && n != 266) {
               if (n != 264 && n != 267) {
                  if (n == 257) {
                     this.setInput();
                     super.toggled = false;
                     if (this.start != 0.0F && this.end != 0.0F) {
                        this.start = 0.0F;
                        this.end = 0.0F;
                     }
                  }

                  if (n == 269) {
                     this.setInput();
                     super.toggled = false;
                  }

                  return;
               }

               this.caretPos = this.input.length();
               if (this.start != 0.0F && this.end != 0.0F) {
                  this.start = 0.0F;
                  this.end = 0.0F;
               }

               return;
            }

            this.caretPos = 0;
            if (this.start != 0.0F && this.end != 0.0F) {
               this.start = 0.0F;
               this.end = 0.0F;
            }

            return;
         }
      } catch (Exception e3) {
         e3.printStackTrace();
      }
   }

   @Override
   public void performAction() {
      super.toggled = true;
   }

   public void onKeyTypedAlt(int n) {
      if (super.hovered && n == 0) {
         if (this.multiLine) {
            int n2 = Math.max(0, Math.min(this.wrappedText.size() - 1, (int)Math.floor((this.inputHandler.getProgressEx() - this.y - 10.0F) / 24.0F)));
            this.caretPos = this.computeInt(this.inputHandler.getProgress(), this.wrappedText.get(n2));
         } else {
            this.prevCaret = this.computeInt(this.inputHandler.getProgress(), this.input);
         }

         long now = System.currentTimeMillis();
         if (now - this.clickTimeD1 < 300L) {
            this.performAction(this.inputHandler);
            this.isDoubleClick = true;
            this.pendingDoubleClick = false;
         } else {
            this.pendingDoubleClick = true;
         }

         this.clickTimeD1 = now;
      }
   }

   public void onKeyTypedImpl(int n) {
      if (super.hovered && n == 0) {
         if (this.pendingDoubleClick && !this.isDoubleClick) {
            this.start = 0.0F;
            this.end = 0.0F;
            this.prevCaret = this.caretPos;
         }

         this.isDoubleClick = false;
         this.pendingDoubleClick = false;
      }
   }

   private void performAction(InputHandler input2) {
      this.prevCaret = this.input.substring(0, this.caretPos).lastIndexOf(32) + 1;
      this.caretPos = this.input.indexOf(32, this.caretPos);
      if (this.caretPos == -1) {
         this.caretPos = this.input.length();
      }

      this.selectedText = this.input.substring(this.prevCaret, this.caretPos);
      if (this.multiLine) {
         int n = Math.max(0, Math.min(this.wrappedText.size() - 1, (int)Math.floor((input2.getProgressEx() - this.y - 10.0F) / 24.0F)));
         this.startLine = n;
         this.endLine = n;
         this.start = this.x + 12.0F + this.computeFloat(this.vg, this.wrappedText.get(n).substring(0, this.computeIntEx(this.prevCaret)));
         this.end = this.computeFloat(this.vg, this.wrappedText.get(n).substring(this.computeIntEx(this.prevCaret), this.computeIntEx(this.caretPos)));
      } else {
         this.start = this.x + 12.0F + this.computeFloat(this.vg, this.input.substring(0, this.prevCaret));
         this.end = this.computeFloat(this.vg, this.input.substring(this.prevCaret, this.caretPos));
      }
   }

   private int computeInt(float f, String text) {
      if (this.centered) {
         f -= 12.0F;
      }

      String text2 = "";

      for (char ch : text.toCharArray()) {
         if (f - this.x - 12.0F < 0.0F) {
            return 0;
         }

         if (f - this.x - 12.0F > this.computeFloat(this.vg, text)) {
            return text.length();
         }

         text2 = text2 + ch;
         int n = (int)this.computeFloat(this.vg, text2);
         if (n >= f - this.x - 16.0F) {
            return text2.length();
         }
      }

      return 0;
   }

   public void setInput() {
      ImeOverlayManager.INSTANCE.performActionEx(this);
   }

   public boolean isValidEx() {
      if (!super.toggled) {
         return false;
      }

      this.performActionEx();
      this.setInput();
      return true;
   }

   private float computeFloat(long l, String text) {
      if (this.password) {
         StringBuilder sb = new StringBuilder();

         while (sb.length() < text.length()) {
            sb.append('*');
         }

         text = sb.toString();
      }

      return NanoVGHelper.INSTANCE.getTextWidth(l, text, this.textSize, OneConfigFonts.REGULAR);
   }

   private int computeInt(int n) {
      int n2 = 0;

      for (int i = 0; i < this.wrappedText.size(); i++) {
         n2 += this.wrappedText.get(i).length();
         if (n2 >= n - 1) {
            return i;
         }
      }

      return 0;
   }

   private float computeFloat(int n) {
      int n2 = 0;
      NanoVGHelper nvg = NanoVGHelper.INSTANCE;

      for (String text : this.wrappedText) {
         float textWidth = nvg.getTextWidth(this.vg, text, this.textSize, OneConfigFonts.REGULAR);
         if (!(n2 + textWidth < n)) {
            return nvg.getTextWidth(this.vg, text.substring(0, n - n2), this.textSize, OneConfigFonts.REGULAR);
         }

         n2 = (int)(n2 + textWidth);
      }

      return 0.0F;
   }

   private int computeIntEx(int n) {
      int n2 = 0;

      for (String text : this.wrappedText) {
         int len = text.length();
         if (n2 + len >= n - 1) {
            return n - n2;
         }

         n2 += len;
      }

      return 0;
   }

   public int getLines() {
      return this.lines;
   }

   public void setBoarderThickness(float f) {
      this.boarderThickness = f;
   }

   public void setTextSize(float f) {
      this.textSize = f;
   }

   public static boolean isActiveValue() {
      return activeValue;
   }

   private static class Inner {
      public Inner() {
      }

      @EventTarget
      private void onPhaseEvent2(RenderStageEvent renderStageEvent) {
         if (renderStageEvent.phase == Stage.PRE) {
            FreeCookieAPI.activeValue = false;
         }
      }
   }
}
