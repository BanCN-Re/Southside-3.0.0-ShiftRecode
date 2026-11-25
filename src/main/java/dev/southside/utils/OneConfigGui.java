package dev.southside.utils;

import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.config.elements.BasicOption;
import cc.polyfrost.oneconfig.gui.SideBar;
import cc.polyfrost.oneconfig.gui.elements.ColorSelector;
import cc.polyfrost.oneconfig.internal.config.OneConfigConfig;
import cc.polyfrost.oneconfig.internal.config.Preferences;
import cc.polyfrost.oneconfig.utils.color.ColorPalette;
import com.google.gson.annotations.SerializedName;
import dev.southside.Southside;
import dev.southside.event.EventTarget;
import dev.southside.event.Stage;
import dev.southside.event.events.RenderStageEvent;
import dev.southside.render.ImeOverlayManager;
import dev.southside.utils.freecookie.FreeCookieAPI;
import java.io.IOException;
import java.util.ArrayList;
import org.jetbrains.annotations.NotNull;

public class OneConfigGui extends OneConfigScreen {
   private static final InputHandler INPUT_HANDLER = new InputHandler();
   public static OneConfigGui INSTANCE;
   @SerializedName("sideBar")
   private final SideBar sideBar = new SideBar();
   @SerializedName("textInputField")
   private final FreeCookieAPI textInputField = new FreeCookieAPI(248, 40, "Search...", false, false, OneConfigSvgs.SEARCH_SM, 12.0F);
   @SerializedName("previousPages")
   private final ArrayList<GuiPage> previousPages = new ArrayList<>();
   @SerializedName("nextPages")
   private final ArrayList<GuiPage> nextPages = new ArrayList<>();
   @SerializedName("backArrow")
   private final BasicElement backArrow = new BasicElement(40, 40, ColorPalette.INSTANCE4, true);
   @SerializedName("forwardArrow")
   private final BasicElement forwardArrow = new BasicElement(40, 40, ColorPalette.INSTANCE4, true);
   @SerializedName("currentColorSelector")
   public ColorSelector currentColorSelector;
   @SerializedName("recordingKeyBind")
   private ConfigKeyBindOption recordingKeyBind;
   @SerializedName("keyBindInputBlock")
   private ScissorRect keyBindInputBlock;
   @SerializedName("consumeEscapeUntilRelease")
   private boolean consumeEscapeUntilRelease;
   @SerializedName("currentPage")
   protected GuiPage currentPage;
   @SerializedName("prevPage")
   protected GuiPage prevPage;
   @SerializedName("pageAnimation")
   private UiAnimation pageAnimation;
   @SerializedName("lastClosedTime")
   private long lastClosedTime = -1L;
   @SerializedName("openingPagePending")
   private boolean openingPagePending = true;
   @SerializedName("screenOpen")
   private boolean screenOpen;
   @SerializedName("containerAnimation")
   private UiAnimation containerAnimation = new DummyAnimation(0.0F);
   @SerializedName("isClosed")
   public boolean isClosed = true;
   @SerializedName("shouldDisplayHud")
   private boolean shouldDisplayHud = false;
   @SerializedName("transparencyFactor")
   public float transparencyFactor = 0.0F;
   @SerializedName("animationScaleFactor")
   public float animationScaleFactor = 0.0F;
   @SerializedName("isDrawing")
   private boolean isDrawing;

   public OneConfigGui() {
      if (INSTANCE != null) {
         OneConfigEventManager.INSTANCE.unregister(INSTANCE);
      }

      INSTANCE = this;
      OneConfigEventManager.INSTANCE.register(INSTANCE);
   }

   public OneConfigGui(GuiPage guiPage) {
      this();
      this.currentPage = guiPage;
   }

   @Override
   public void performAction(int n, int n2) {
      super.performAction(n, n2);
      if (!this.screenOpen) {
         this.screenOpen = true;
         this.performActionSafe();
      }

      if (this.currentPage == null) {
         this.currentPage = new ModsPage(0);
         this.currentPage.parents.add(this.currentPage);
      }

      if (this.openingPagePending) {
         this.performActionImpl();
         this.openingPagePending = false;
      }

      if ((Boolean)Preferences.INSTANCE.guiOpenAnimation.getValue()) {
         this.shouldDisplayHud = false;
      }
   }

   private void performActionImpl() {
      boolean flag = !(Boolean)Preferences.INSTANCE.showPageAnimationOnOpen.getValue();
      if (!this.currentPage.parents.isEmpty() && this.currentPage.parents.get(0) instanceof ModsPage modsPage) {
         this.previousPages.clear();
         this.nextPages.clear();
         this.performAction(modsPage, false, flag);
      } else {
         switch (Preferences.INSTANCE.openingBehavior.getValue()) {
            case 0:
               if (!(this.currentPage instanceof ModsPage)) {
                  this.previousPages.clear();
                  this.performAction(new ModsPage(0), false, flag);
               }
               break;
            case 1:
               OptionPage defaultPage = Preferences.getPreferences().modData.defaultPage;
               if (!(this.currentPage instanceof ModConfigPage modConfigPage && modConfigPage.getPage() == defaultPage)) {
                  this.previousPages.clear();
                  this.performAction(new ModConfigPage(Preferences.getPreferences().modData.defaultPage, true), false, flag);
               }
            case 2:
            default:
               break;
            case 3:
               if (!(this.currentPage instanceof ModsPage)) {
                  long now = System.currentTimeMillis();
                  long l = now - this.lastClosedTime;
                  if (this.lastClosedTime != -1L && l > ((Number)Preferences.INSTANCE.timeUntilReset.getValue()).longValue() * 1000L) {
                     this.previousPages.clear();
                     this.performAction(new ModsPage(0), false, flag);
                  }
               }
         }
      }
   }

   @Override
   public void performAction(long l, InputHandler input) {
      this.isDrawing = true;

      try {
         this.render(l, input);
      } catch (RuntimeException e) {
         this.performActionDirect();
         if (input != null) {
            input.clear();
         }

         this.prevPage = null;
         this.pageAnimation = null;
         this.shouldDisplayHud = false;
         throw e;
      } finally {
         this.isDrawing = false;
         ScissorHelper.INSTANCE.clearScissors(l);
         NanoVGHelper.INSTANCE.resetTransform(l);
      }
   }

   private void render(long l, InputHandler input) {
      NanoVGHelper nvg = NanoVGHelper.INSTANCE;
      ScissorHelper scissor = ScissorHelper.INSTANCE;
      boolean flag = input == null;
      if ((Boolean)Preferences.INSTANCE.guiOpenAnimation.getValue()) {
         int n = (int)(((Number)Preferences.INSTANCE.animationTime.getValue()).floatValue() * 1000.0F);
         if (flag && (Boolean)Preferences.INSTANCE.guiClosingAnimation.getValue() && this.shouldDisplayHud) {
            if (this.containerAnimation.getProgressEx() != 0.0F) {
               switch (Preferences.INSTANCE.animationType.getValue()) {
                  case 0:
                     this.containerAnimation = new LinearAnimation(n, MathClampUtils.clamp(this.animationScaleFactor - 0.9F, 0.0F, 0.1F), 0.0F, false);
                     break;
                  case 1:
                     this.containerAnimation = new EaseInBackAnimation(n, MathClampUtils.clamp(this.animationScaleFactor, 0.0F, 1.0F), 0.0F, false);
               }
            }
         } else if (!flag && this.isClosed) {
            boolean flag2 = this.containerAnimation.isValid() && this.containerAnimation.getProgressEx() != 0.0F;
            switch (Preferences.INSTANCE.animationType.getValue()) {
               case 0:
                  this.containerAnimation = new LinearAnimation(
                     n, MathClampUtils.clamp(flag2 ? 0.1F : this.animationScaleFactor - 0.9F, 0.0F, 0.1F), 0.1F, false
                  );
                  break;
               case 1:
                  this.containerAnimation = new LinearAnimation(n, MathClampUtils.clamp(flag2 ? 1.0F : this.animationScaleFactor, 0.0F, 1.0F), 1.0F, false);
            }

            this.isClosed = false;
         }
      }

      float f = Math.max(0.0F, Preferences.INSTANCE.guiOpenAnimation.getValue() ? this.containerAnimation.getProgress() : 1.0F);
      switch (Preferences.INSTANCE.animationType.getValue()) {
         case 0:
            this.animationScaleFactor = MathClampUtils.clamp(0.9F + f, 0.9F, 1.0F);
            this.transparencyFactor = MathClampUtils.clamp(f * 10.0F, 0.0F, 1.0F);
            break;
         case 1:
            this.animationScaleFactor = this.transparencyFactor = f;
      }

      nvg.setAlpha(l, this.transparencyFactor);
      if (input == null) {
         input = INPUT_HANDLER;
      }

      float f2 = getProgress() * this.animationScaleFactor;
      int n2 = (int)((WindowMetrics.getFramebufferWidth() - 1280.0F * f2) / 2.0F / f2);
      int n3 = (int)((WindowMetrics.getFramebufferHeight() - 800.0F * f2) / 2.0F / f2);
      nvg.scale(l, f2, f2);
      input.performAction(f2, f2);
      nvg.draw(l, n2, n3, 1280.0F, 800.0F, 64.0F, 0.0F, 20.0F);
      ScissorRect clip = scissor.scissor(l, n2, n3, 224.0F, 800.0F);
      nvg.drawRoundedRect(l, n2, n3, 244.0F, 800.0F, OneConfigColors.GRAY_850_95, 20.0F);
      scissor.resetScissor(l, clip);
      ScissorRect clip2 = scissor.scissor(l, n2 + 224, n3, 1056.0F, 800.0F);
      nvg.drawRoundedRect(l, n2 + 224 - 20, n3, 1076.0F, 800.0F, OneConfigColors.GRAY_850, 20.0F);
      scissor.resetScissor(l, clip2);
      nvg.drawLine(l, n2 + 224, n3 + 72, n2 + 1280, n3 + 72, 1.0F, OneConfigColors.GRAY_700);
      nvg.drawLine(l, n2 + 224, n3, n2 + 222, n3 + 800, 1.0F, OneConfigColors.GRAY_700);
      nvg.draw(l, OneConfigSvgs.ICON_WHITE, n2 + 27.0F, n3 + 18.0F, 40.0F, 40.0F);
      nvg.drawTextEx(l, "SOUTHSIDE", n2 + 67.0F, n3 + 40.0F, OneConfigColors.WHITE, 15.0F, OneConfigFonts.BOLD);
      this.textInputField.draw(l, n2 + 1020, n3 + 16, input);
      this.sideBar.draw(l, n2, n3, input);
      this.backArrow.performAction(n2 + 240, n3 + 16, input);
      this.forwardArrow.performAction(n2 + 280, n3 + 16, input);
      if (this.previousPages.size() == 0) {
         this.backArrow.setDisabled(true);
         nvg.setAlpha(l, 0.5F);
      } else {
         this.backArrow.setDisabled(false);
         if (!this.backArrow.isHovered() || OneConfigPlatform.getMousePlatform().isKeyDown(0)) {
            nvg.setAlpha(l, 0.8F);
         }
      }

      nvg.draw(l, OneConfigSvgs.ARROW_LEFT, n2 + 250, n3 + 26, 20.0F, 20.0F, this.backArrow.currentColor);
      nvg.setAlpha(l, 1.0F);
      if (this.nextPages.size() == 0) {
         this.forwardArrow.setDisabled(true);
         nvg.setAlpha(l, 0.5F);
      } else {
         this.forwardArrow.setDisabled(false);
         if (!this.forwardArrow.isHovered() || OneConfigPlatform.getMousePlatform().isKeyDown(0)) {
            nvg.setAlpha(l, 0.8F);
         }
      }

      nvg.draw(l, OneConfigSvgs.ARROW_RIGHT, n2 + 290, n3 + 26, 20.0F, 20.0F, this.forwardArrow.currentColor);
      nvg.setAlpha(l, 1.0F);
      this.performAction(this.backArrow.isClicked(), this.forwardArrow.isClicked());
      scissor.scissor(l, n2 + 224, n3 + 72, 1056.0F, 728.0F);
      ScissorRect scissorRect = input.getScissorRect(n2, n3, 224.0F, 728.0F);
      if (this.prevPage != null && this.pageAnimation != null) {
         float f3 = this.pageAnimation.evaluate(GuiUtils.getProgress());
         if (!this.pageAnimation.isReverse()) {
            this.prevPage.drawEx(l, (int)(n2 + f3), n3 + 72, input);
            this.currentPage.drawEx(l, (int)(n2 - 1904 + f3), n3 + 72, input);
         } else {
            this.prevPage.drawEx(l, (int)(n2 - 1904 + f3), n3 + 72, input);
            this.currentPage.drawEx(l, (int)(n2 + f3), n3 + 72, input);
         }

         if (this.pageAnimation.isValid()) {
            this.prevPage = null;
         }
      } else {
         this.currentPage.drawEx(l, n2 + 224, n3 + 72, input);
      }

      scissor.clearScissors(l);
      input.remove(scissorRect);
      float f4 = n2 + 336;

      for (int i = 0; i < this.currentPage.parents.size(); i++) {
         String title = this.currentPage.parents.get(i).getTitle();
         float textWidth = nvg.getTextWidth(l, title, 24.0F, OneConfigFonts.SEMIBOLD);
         boolean isHovered = input.isHovered((int)f4, n3 + 24, (int)textWidth, 36.0F);
         int white_60 = OneConfigColors.WHITE_60;
         if (i == this.currentPage.parents.size() - 1) {
            white_60 = OneConfigColors.WHITE;
         } else if (isHovered && !OneConfigPlatform.getMousePlatform().isKeyDown(0)) {
            white_60 = OneConfigColors.WHITE_80;
         }

         nvg.drawTextEx(l, title, f4, n3 + 38, white_60, 24.0F, OneConfigFonts.SEMIBOLD);
         if (i != 0) {
            nvg.draw(l, OneConfigSvgs.CARET_RIGHT_BOLD, f4 - 28.0F, n3 + 25, 24.0F, 24.0F, white_60);
         }

         if (isHovered && input.isValid()) {
            this.performAction(this.currentPage.parents.get(i));
         }

         f4 += textWidth + 32.0F;
      }

      if (this.currentColorSelector != null) {
         this.currentColorSelector.render(l);
      }

      GuiNotificationManager.INSTANCE.draw(l, n2 + 224 + 528, n3 + 720 + 72, input);
   }

   public boolean isValid(ConfigKeyBindOption configKeyBindOption) {
      if (this.screenOpen && this.recordingKeyBind == null) {
         this.keyBindInputBlock = this.getInputHandler().getScissorRect();
         this.recordingKeyBind = configKeyBindOption;
         return true;
      } else {
         return false;
      }
   }

   public void performAction(ConfigKeyBindOption configKeyBindOption) {
      if (this.recordingKeyBind == configKeyBindOption) {
         this.getInputHandler().remove(this.keyBindInputBlock);
         this.keyBindInputBlock = null;
         this.recordingKeyBind = null;
      }
   }

   private void performActionDirect() {
      if (this.recordingKeyBind != null) {
         this.recordingKeyBind.performActionEx();
      }
   }

   @Override
   public void performActionEx(int n, int n2) {
      if (this.recordingKeyBind == null) {
         try {
            super.performActionEx(n, n2);
            this.textInputField.onKeyTyped(n);
            if (this.currentColorSelector != null) {
               this.currentColorSelector.onKeyTyped(n);
            }

            this.currentPage.onKeyTyped(n);
         } catch (Exception e) {
            e.printStackTrace();
         }
      }
   }

   @Override
   public void performAction(int n, int n2, int n3) {
      try {
         boolean flag = n == 256;
         if (flag && this.consumeEscapeUntilRelease) {
            return;
         }

         if (this.recordingKeyBind != null) {
            this.consumeEscapeUntilRelease = flag;
            this.recordingKeyBind.onKeyTypedEx(n);
            return;
         }

         if (flag && (this.textInputField.isValidEx() || this.currentPage != null && this.currentPage.isValid())) {
            return;
         }

         super.performAction(n, n2, n3);
         if (flag) {
            return;
         }

         this.textInputField.onKeyTypedEx(n);
         if (this.currentColorSelector != null) {
            this.currentColorSelector.onKeyTypedEx(n);
         }

         this.currentPage.onKeyTypedEx(n);
         if (this.textInputField.isToggled()) {
            return;
         }

         if (this.currentPage instanceof ModConfigPage modConfigPage) {
            OptionCategory optionCategory = modConfigPage.getPage().categories.get(modConfigPage.getSelectedCategory());
            if (optionCategory != null) {
               for (OptionSubcategory optionSubcategory : optionCategory.subcategories) {
                  for (BasicOption option : optionSubcategory.options) {
                     if (option.isValidEx() && option instanceof IFocusableOption ifocusableoption && ifocusableoption.isValidImpl()) {
                        return;
                     }
                  }
               }
            }
         }

         if (!FreeCookieAPI.isActiveValue()) {
            this.performAction(n == 263, n == 262);
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Override
   public void performActionEx(int n, int n2, int n3) {
      if (n == 256) {
         this.consumeEscapeUntilRelease = false;
      }
   }

   @Override
   public void onMouseClick(double d, double d2, int n) {
      try {
         super.onMouseClick((double)d, (double)d2, n);
         this.textInputField.onKeyTypedAlt(n);
         if (this.currentColorSelector != null) {
            this.currentColorSelector.onKeyTypedAlt(n);
         }

         this.currentPage.onMouseClick(d, d2, n);
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Override
   public void onMouseClickEx(double d, double d2, int n) {
      try {
         super.onMouseClickEx((double)d, (double)d2, n);
         this.textInputField.onKeyTypedImpl(n);
         if (this.currentColorSelector != null) {
            this.currentColorSelector.onKeyTypedImpl(n);
         }

         this.currentPage.onMouseClickEx(d, d2, n);
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   private void performAction(boolean flag, boolean flag2) {
      if (!flag || !flag2) {
         try {
            if (flag && this.previousPages.size() > 0) {
               this.nextPages.add(0, this.currentPage);
               this.performAction(this.previousPages.get(0), false);
               this.previousPages.remove(0);
            }

            if (flag2 && this.nextPages.size() > 0) {
               this.previousPages.add(0, this.currentPage);
               this.performAction(
                  this.nextPages.get(0),
                  new LinearAnimation((int)(((Number)Preferences.INSTANCE.pageAnimationDuration.getValue()).floatValue() * 1000.0F), 224.0F, 2128.0F, true),
                  false
               );
               this.nextPages.remove(0);
            }
         } catch (Throwable e) {
         }
      }
   }

   public void performAction(@NotNull GuiPage guiPage) {
      this.performAction(guiPage, true);
   }

   public void performAction(@NotNull GuiPage guiPage, boolean flag) {
      this.performAction(
         guiPage, new LinearAnimation((int)(((Number)Preferences.INSTANCE.pageAnimationDuration.getValue()).floatValue() * 1000.0F), 224.0F, 2128.0F, false), flag
      );
   }

   private void performAction(@NotNull GuiPage guiPage, boolean flag, boolean flag2) {
      if (flag2) {
         this.performActionEx(guiPage, flag);
      } else {
         this.performAction(guiPage, flag);
      }
   }

   private void performActionEx(@NotNull GuiPage guiPage, boolean flag) {
      this.performAction(guiPage, new DummyAnimation(2128.0F), flag);
   }

   public void performAction(@NotNull GuiPage guiPage, UiAnimation uiAnimation, boolean flag) {
      if (!(Boolean)Preferences.INSTANCE.showPageAnimations.getValue()) {
         uiAnimation = new DummyAnimation(uiAnimation.getProgressEx());
      }

      if (guiPage != this.currentPage) {
         this.performActionDirect();
         this.currentPage.performAction();
         ImeOverlayManager.INSTANCE.performActionImpl();
         this.textInputField.setInput("");
         if (guiPage.parents.size() == 0) {
            guiPage.parents.addAll(this.currentPage.parents);
            if (!guiPage.isBase()) {
               boolean flag2 = false;

               for (int i = 0; i < guiPage.parents.size(); i++) {
                  GuiPage guiPage2 = guiPage.parents.get(i);
                  if (guiPage2 == guiPage) {
                     flag2 = true;
                     guiPage.parents.subList(i + 1, guiPage.parents.size()).clear();
                     break;
                  }
               }

               if (!flag2) {
                  guiPage.parents.add(guiPage);
               }
            } else {
               guiPage.parents.clear();
               guiPage.parents.add(guiPage);
            }
         }

         this.sideBar.applyText(guiPage.parents.get(0).getTitle(), uiAnimation instanceof DummyAnimation);
         if (flag) {
            this.previousPages.add(0, this.currentPage);
            this.nextPages.clear();
         }

         if (this.prevPage == null) {
            this.prevPage = this.currentPage;
         }

         this.currentPage = guiPage;
         this.pageAnimation = uiAnimation;
      }
   }

   public void performAction(ColorSelector colorSelector) {
      if (this.currentColorSelector != null) {
         this.getOneColor2();
      }

      this.currentColorSelector = colorSelector;
   }

   public OneColor getOneColor2() {
      if (this.currentColorSelector == null) {
         return null;
      }

      this.currentColorSelector.performAction();
      OneColor color = this.currentColorSelector.getColor();
      this.currentColorSelector = null;
      return color;
   }

   public OneColor getOneColor() {
      return this.currentColorSelector == null ? null : this.currentColorSelector.getColor();
   }

   public static float getProgress() {
      float f = Preferences.INSTANCE.enableCustomScale.getValue()
         ? ((Number)Preferences.INSTANCE.customScale.getValue()).floatValue()
         : Math.min(WindowMetrics.getFramebufferWidth() / 1920.0F, WindowMetrics.getFramebufferHeight() / 1080.0F);
      if (f < 1.0F && !(Boolean)Preferences.INSTANCE.enableCustomScale.getValue()) {
         f = Math.min(Math.min(1.0F, WindowMetrics.getFramebufferWidth() / 1280.0F), Math.min(1.0F, WindowMetrics.getFramebufferHeight() / 800.0F));
      }

      return (float)(Math.floor(f / 0.05F) * 0.05F);
   }

   public static float getProgressEx() {
      float f = Preferences.INSTANCE.enableCustomNotificationScale.getValue()
         ? ((Number)Preferences.INSTANCE.customNotificationScale.getValue()).floatValue()
         : Math.min(WindowMetrics.getFramebufferWidth() / 1920.0F, WindowMetrics.getFramebufferHeight() / 1080.0F);
      if (f < 1.0F && !(Boolean)Preferences.INSTANCE.enableCustomNotificationScale.getValue()) {
         f = Math.min(Math.min(1.0F, WindowMetrics.getFramebufferWidth() / 1280.0F), Math.min(1.0F, WindowMetrics.getFramebufferHeight() / 800.0F));
      }

      return (float)(Math.floor(f / 0.05F) * 0.05F);
   }

   public String getInput() {
      return this.textInputField.getInput();
   }

   @Override
   public void performAction() {
      if (this.screenOpen) {
         this.screenOpen = false;
         boolean flag = false;

         try {
            this.performActionDirect();
            if (this.currentPage != null) {
               this.currentPage.performAction();
            }

            ImeOverlayManager.INSTANCE.performActionImpl();
            flag = true;
         } finally {
            this.openingPagePending = true;
            this.lastClosedTime = System.currentTimeMillis();
            this.isClosed = true;
            this.consumeEscapeUntilRelease = false;
            this.shouldDisplayHud = flag
               && (Boolean)Preferences.INSTANCE.guiOpenAnimation.getValue()
               && (Boolean)Preferences.INSTANCE.guiClosingAnimation.getValue();
            if (!this.shouldDisplayHud) {
               this.animationScaleFactor = this.transparencyFactor = 0.0F;
            }

            this.performActionSafe();
            super.performAction();
         }
      }
   }

   public void removed() {
      try {
         this.performAction();
      } finally {
         super.removed();
      }
   }

   private void performActionSafe() {
      try {
         Southside.INSTANCE.getModuleManager().performActionEx();
      } catch (IOException e) {
         Southside.LOGGER.error("Failed to save running configuration at ClickGUI transition", e);
      }

      Preferences.INSTANCE.saveToJson();
      OneConfigConfig.getInstance().saveToJson();
   }

   @EventTarget
   private void onPhaseEvent2(RenderStageEvent renderStageEvent) {
      if (this.shouldDisplayHud && renderStageEvent.phase != Stage.PRE) {
         if (OneConfigPlatform.getGuiPlatform().getScreen() != this) {
            NanoVGHelper.INSTANCE.performAction(l -> this.performAction(l, null));
            if (this.transparencyFactor <= 0.01) {
               this.shouldDisplayHud = false;
            }
         }
      }
   }

   @Override
   public boolean shouldPause() {
      return false;
   }

   @Override
   public boolean isValid() {
      return (Boolean)Preferences.INSTANCE.enableBlur.getValue();
   }

   public boolean isIsDrawing() {
      return this.isDrawing;
   }

   public static OneConfigGui getSideBarUtil() {
      return INSTANCE == null ? new OneConfigGui() : INSTANCE;
   }

   public static boolean isOneConfigGui() {
      return OneConfigPlatform.getGuiPlatform().getScreen() instanceof OneConfigGui;
   }

   static {
      INPUT_HANDLER.getScissorRect();
   }
}
