package dev.southside.utils;

import cc.polyfrost.oneconfig.images.OneImage;
import cc.polyfrost.oneconfig.utils.color.ColorPalette;
import com.google.gson.annotations.SerializedName;
import dev.southside.utils.freecookie.FreeCookieAPI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class MusicPlayerPage extends GuiPage {
   private static final int DEFAULT_INT = 320;
   private static final int DEFAULT_INT_VALUE = 376;
   private static final int DEFAULT_INT_DATA = 664;
   private static final int DEFAULT_INT_REF = 32;
   private static final int DEFAULT_INT_PRIMARY = 320;
   private static final int DEFAULT_INT_SECONDARY = 44;
   private static final int DEFAULT_INT_TERTIARY = 40;
   private static final int DEFAULT_INT_EXTRA = 604;
   @SerializedName("controller")
   public final MusicPlayerController controller = MusicPlayerController.getINSTANCE();
   @SerializedName("search")
   private final FreeCookieAPI search = new FreeCookieAPI(664, 40, "Search NetEase Cloud Music...", false, false, OneConfigSvgs.SEARCH_SM, 12.0F);
   @SerializedName("seek")
   private final SliderWidget seek = new SliderWidget(320, 0.0F, 1.0F, 0.0F, 2);
   @SerializedName("volume")
   private final SliderWidget volume = new SliderWidget(192, 0.0F, 100.0F, 80.0F, 2);
   @SerializedName("previous")
   private final BasicButton previous = getTextUtil2(OneConfigSvgs.Inner.SKIP_PREV, this.controller::performActionCustom);
   @SerializedName("next")
   private final BasicButton next = getTextUtil2(OneConfigSvgs.Inner.SKIP_NEXT, this.controller::performActionSafe);
   @SerializedName("play")
   private final BasicButton play = new BasicButton(48, 48, OneConfigSvgs.Inner.PLAY, ColorPalette.INSTANCE2);
   @SerializedName("shuffle")
   private final BasicButton shuffle = getTextUtil2(OneConfigSvgs.Inner.SHUFFLE, () -> this.controller.shuffle = !this.controller.shuffle);
   @SerializedName("repeat")
   private final BasicButton repeat = getTextUtil2(OneConfigSvgs.Inner.REFRESH, () -> this.controller.loop = !this.controller.loop);
   @SerializedName("login")
   private final BasicButton login = new BasicButton(320, 32, "Sign in with QR to play VIP songs", ColorPalette.INSTANCE4);
   @SerializedName("logout")
   private final BasicButton logout = new BasicButton(76, 32, "Logout", ColorPalette.INSTANCE4);
   @SerializedName("closeLogin")
   private final BasicButton closeLogin = getTextUtil2(OneConfigSvgs.X_CLOSE, this.controller::performActionInternal);
   @SerializedName("refreshLogin")
   private final BasicButton refreshLogin = new BasicButton(176, 32, "Refresh QR", ColorPalette.INSTANCE3);
   @SerializedName("songs")
   private final MusicPlayerPage.Inner songs = new MusicPlayerPage.Inner();

   public MusicPlayerPage() {
      super("Music");
      this.search.setInput(this.controller.getKeywords());
      this.search.setTextSize(14.0F);
      this.play.setRunnable(this.controller::performActionWithOffset);
      this.login.setRunnable(this.controller::performActionByIndex);
      this.logout.setRunnable(this.controller::performActionHelper);
      this.refreshLogin.setRunnable(this.controller::performActionByIndex);
      this.shuffle.setToggleable(true);
      this.repeat.setToggleable(true);
   }

   @Override
   public void draw(long vg, int x, int y, InputHandler input) {
      this.controller.performAction();
      this.search.draw(vg, x + 376, y + 32, input);
      this.draw(vg, x + 16.0F, y + 32, input);
      NanoVGHelper nvg = NanoVGHelper.INSTANCE;
      float f = y + 32 + 40 + 16.0F;
      if (this.controller.getState() == MusicPlayerController.Mode2.LOADING) {
         nvg.drawTextEx(vg, "Loading...", x + 376, f + 14.0F, OneConfigColors.WHITE_50, 13.0F, OneConfigFonts.SEMIBOLD);
      } else if (this.controller.getState() == MusicPlayerController.Mode2.ERROR) {
         nvg.draw(vg, "Error: " + this.controller.getError(), x + 376, f + 14.0F, 664.0F, OneConfigColors.ERROR_600, 13.0F, OneConfigFonts.WQY_MICROHEI);
      } else if (this.controller.getResults().isEmpty()) {
         nvg.drawTextEx(vg, "No results found", x + 376, f + 14.0F, OneConfigColors.WHITE_50, 13.0F, OneConfigFonts.SEMIBOLD);
      }

      this.songs.drawEx(vg, x + 376, (int)(f + 20.0F), input);
   }

   private void draw(long l, float f, float f2, InputHandler input) {
      NanoVGHelper nvg = NanoVGHelper.INSTANCE;
      OneImage.Entry4 entry4 = this.controller.getEntry4();
      MusicAudioPlayer musicAudioPlayer = MusicAudioPlayer.INSTANCE;
      boolean flag = this.controller.loginState != MusicPlayerController.Mode.OFF && this.controller.loginState != MusicPlayerController.Mode.LOGGED_IN;
      if (flag) {
         this.drawEx(l, f, f2, input);
      } else {
         nvg.drawRoundedRect(l, f, f2, 320.0F, 320.0F, OneConfigColors.GRAY_700, 12.0F);
         ImageAsset image = entry4 == null ? null : MusicCoverCache.getImageAsset(entry4.getCoverUrl());
         if (image != null) {
            nvg.draw(l, image, f, f2, 320.0F, 320.0F, 12.0F);
         } else {
            nvg.draw(l, OneConfigSvgs.Inner.MUSIC, f + 160.0F - 18.0F, f2 + 160.0F - 18.0F, 36.0F, 36.0F, OneConfigColors.WHITE_50);
         }
      }

      float f3 = f2 + 320.0F + 34.0F;
      nvg.draw(l, entry4 == null ? "Nothing playing" : entry4.getTitle(), f, f3, 320.0F, OneConfigColors.WHITE, 18.0F, OneConfigFonts.SEMIBOLD);
      nvg.draw(
         l,
         entry4 == null ? "Search a song to start" : entry4.getArtist(),
         f,
         f3 + 22.0F,
         320.0F,
         OneConfigColors.WHITE_60,
         13.0F,
         OneConfigFonts.WQY_MICROHEI
      );
      long l2 = musicAudioPlayer.getDurationMsEx() > 0L ? musicAudioPlayer.getDurationMsEx() : (entry4 == null ? 0L : entry4.getDurationMs());
      long l3 = Math.min(musicAudioPlayer.getDurationMs(), l2 > 0L ? l2 : musicAudioPlayer.getDurationMs());
      float f4 = f3 + 52.0F;
      float f5 = f4 + 12.0F;
      nvg.drawTextEx(l, formatText(l3), f, f4, OneConfigColors.WHITE_50, 10.0F, OneConfigFonts.REGULAR);
      String text = formatText(l2);
      nvg.drawTextEx(
         l,
         text,
         f + 320.0F - nvg.getTextWidth(l, text, 10.0F, OneConfigFonts.REGULAR),
         f4,
         OneConfigColors.WHITE_50,
         10.0F,
         OneConfigFonts.REGULAR
      );
      this.seek.setDisabled(l2 <= 0L || musicAudioPlayer.getState() == MusicAudioPlayer.Mode.ERROR);
      boolean isDragging = this.seek.isDragging();
      if (!isDragging) {
         this.seek.setProgress(l2 > 0L ? Math.min(1.0F, (float)l3 / (float)l2) : 0.0F);
      }

      float progressEx = this.seek.getProgressEx();
      this.seek.draw(l, f, f5 - 2.0F, input);
      if (l2 > 0L && (isDragging || this.seek.isDragging()) && progressEx != this.seek.getProgressEx()) {
         musicAudioPlayer.performAction((long)(this.seek.getProgressEx() * (float)l2));
      }

      float f6 = f5 + 48.0F;
      this.previous.draw(l, f + 8.0F, f6 - 18.0F, input);
      boolean flag2 = entry4 != null && this.controller.matches(entry4.getId());
      this.play.setDisabled(flag2);
      this.play.setIcon1(flag2 ? null : (musicAudioPlayer.getState() == MusicAudioPlayer.Mode.PLAYING ? OneConfigSvgs.Inner.PAUSE : OneConfigSvgs.Inner.PLAY));
      this.play.draw(l, f + 82.0F, f6 - 24.0F, input);
      if (flag2) {
         nvg.draw(l, f + 106.0F, f6, 9.0F, 2.5F);
      }

      this.next.draw(l, f + 164.0F, f6 - 18.0F, input);
      this.shuffle.setToggled(this.controller.shuffle);
      this.repeat.setToggled(this.controller.loop);
      this.shuffle.draw(l, f + 320.0F - 76.0F, f6 - 18.0F, input);
      this.repeat.draw(l, f + 320.0F - 36.0F, f6 - 18.0F, input);
      float f7 = f6 + 56.0F;
      nvg.draw(l, OneConfigSvgs.Inner.VOLUME, f, f7 - 8.0F, 16.0F, 16.0F, OneConfigColors.WHITE_50);
      if (!this.volume.isDragging()) {
         this.volume.setProgress(this.controller.volume);
      }

      float progressEx2 = this.volume.getProgressEx();
      this.volume.draw(l, f + 28.0F, f7 - 4.0F, input);
      if (progressEx2 != this.volume.getProgressEx()) {
         this.controller.volume = Math.round(this.volume.getProgressEx());
         musicAudioPlayer.setProgress(this.controller.volume / 100.0F);
      }

      if (musicAudioPlayer.getState() == MusicAudioPlayer.Mode.ERROR && musicAudioPlayer.getError() != null) {
         nvg.draw(l, "Error: " + musicAudioPlayer.getError(), f, f7 + 24.0F, 320.0F, OneConfigColors.ERROR_600, 10.0F, OneConfigFonts.WQY_MICROHEI);
      }

      float f8 = f7 + 46.0F;
      if (this.controller.loginState == MusicPlayerController.Mode.LOGGED_IN) {
         nvg.drawCircle(l, f + 10.0F, f8 + 10.0F, 10.0F, OneConfigColors.GRAY_500);
         ImageAsset imageAssetEx = MusicCoverCache.getImageAssetEx(this.controller.avatarUrl);
         if (imageAssetEx != null) {
            nvg.draw(l, imageAssetEx, f, f8, 20.0F, 20.0F, 10.0F);
         }

         nvg.draw(
            l,
            this.controller.nickname == null ? "Signed in" : this.controller.nickname,
            f + 28.0F,
            f8 + 14.0F,
            210.0F,
            OneConfigColors.WHITE,
            12.0F,
            OneConfigFonts.WQY_MICROHEI
         );
         this.logout.draw(l, f + 320.0F - 76.0F, f8 - 6.0F, input);
      } else if (this.controller.loginState == MusicPlayerController.Mode.OFF) {
         this.login.draw(l, f, f8 - 6.0F, input);
      }
   }

   private void drawEx(long l, float f, float f2, InputHandler input) {
      short s = -24757;
      NanoVGHelper nvg = NanoVGHelper.INSTANCE;
      short s2 = 10143;
      nvg.drawRoundedRect(l, f, f2, 320.0F, 320.0F, OneConfigColors.GRAY_700, 12.0F);
      String text = "Scan to sign in";
      float textWidth = nvg.getTextWidth(l, text, 15.0F, OneConfigFonts.SEMIBOLD);
      nvg.drawTextEx(l, text, f + (320.0F - textWidth) / 2.0F, f2 + 36.0F, OneConfigColors.WHITE, 15.0F, OneConfigFonts.SEMIBOLD);
      this.closeLogin.draw(l, f + 320.0F - 40.0F, f2 + 4.0F, input);
      float f3 = 176.0F;
      float f4 = f + (320.0F - f3) / 2.0F;
      float f5 = f2 + 64.0F;
      nvg.drawRoundedRect(l, f4 - 4.0F, f5 - 4.0F, f3 + 8.0F, f3 + 8.0F, OneConfigColors.WHITE, 8.0F);
      String qrKey = this.controller.qrKey;
      if (qrKey != null) {
         ImageAsset imageAsset = MusicCoverCache.getImageAsset(formatText(qrKey));
         if (imageAsset != null) {
            nvg.draw(l, imageAsset, f4, f5, f3, f3);
         }
      }

      if (this.controller.loginState == MusicPlayerController.Mode.LOADING) {
         nvg.draw(l, f4 + f3 / 2.0F, f5 + f3 / 2.0F, 12.0F, 2.5F);
      }
      String text2 = switch (this.controller.loginState) {
         case LOADING -> "Loading QR code...";
         case WAIT_SCAN -> "Waiting for scan...";
         case WAIT_CONFIRM -> "Scanned - confirm on phone";
         case EXPIRED -> "QR expired";
         default -> "";
      };
      boolean flag = this.controller.loginState == MusicPlayerController.Mode.EXPIRED;
      float textWidth2 = nvg.getTextWidth(l, text2, 12.0F, OneConfigFonts.REGULAR);
      nvg.drawTextEx(
         l,
         text2,
         f + (320.0F - textWidth2) / 2.0F,
         f2 + (flag ? 264.0F : 280.0F),
         flag ? OneConfigColors.ERROR_600 : OneConfigColors.WHITE_60,
         12.0F,
         OneConfigFonts.REGULAR
      );
      if (flag) {
         this.refreshLogin.draw(l, f4, f2 + 282.0F, input);
      }
   }

   public void draw(long l, OneImage.Entry4 entry4, int n, float f, float f2, float f3, float f4, boolean flag) {
      NanoVGHelper nvg = NanoVGHelper.INSTANCE;
      boolean flag2 = this.controller.matches(entry4.getId());
      boolean flag3 = flag2 || entry4.getId().equals(MusicAudioPlayer.INSTANCE.getDisplayText());
      if (flag3 || flag) {
         nvg.drawRoundedRect(l, f, f2, f3 - 8.0F, f4 - 4.0F, flag3 ? OneConfigColors.GRAY_700 : OneConfigColors.GRAY_800, 8.0F);
      }

      if (flag3) {
         nvg.drawRoundedRect(l, f, f2 + 6.0F, 3.0F, f4 - 16.0F, OneConfigColors.PRIMARY_500, 1.5F);
      }

      float f5 = f + 8.0F;
      float f6 = f2 + 2.0F;
      nvg.drawRoundedRect(l, f5, f6, 32.0F, 32.0F, OneConfigColors.GRAY_500, 4.0F);
      ImageAsset imageAssetEx = MusicCoverCache.getImageAssetEx(entry4.getCoverUrl());
      if (imageAssetEx != null) {
         nvg.draw(l, imageAssetEx, f5, f6, 32.0F, 32.0F, 4.0F);
      }

      nvg.draw(
         l,
         entry4.getTitle(),
         f + 52.0F,
         f6 + 12.0F,
         f3 - 148.0F,
         flag3 ? OneConfigColors.PRIMARY_500 : OneConfigColors.WHITE_90,
         13.0F,
         OneConfigFonts.WQY_MICROHEI
      );
      nvg.draw(l, entry4.getArtist(), f + 52.0F, f6 + 28.0F, f3 - 148.0F, OneConfigColors.WHITE_50, 11.0F, OneConfigFonts.WQY_MICROHEI);
      if (flag2) {
         nvg.draw(l, f + f3 - 24.0F, f6 + 16.0F, 6.0F, 1.75F);
      } else {
         String text = formatText(entry4.getDurationMs());
         nvg.drawTextEx(
            l,
            text,
            f + f3 - 16.0F - nvg.getTextWidth(l, text, 10.0F, OneConfigFonts.REGULAR),
            f6 + 20.0F,
            OneConfigColors.WHITE_50,
            10.0F,
            OneConfigFonts.REGULAR
         );
      }
   }

   @Override
   public void onKeyTypedEx(int n) {
      boolean flag = this.search.isToggled() && (n == 257 || n == 335);
      this.search.onKeyTypedEx(n);
      this.controller.setKeywords(this.search.getInput());
      if (flag) {
         this.controller.applyText(this.search.getInput().trim());
         this.songs.performAction();
      }
   }

   @Override
   public void onKeyTyped(int n) {
      this.search.onKeyTyped(n);
      this.controller.setKeywords(this.search.getInput());
   }

   @Override
   public void onMouseClick(double d, double d2, int n) {
      this.search.onKeyTypedAlt(n);
   }

   @Override
   public void onMouseClickEx(double d, double d2, int n) {
      this.search.onKeyTypedImpl(n);
   }

   @Override
   public boolean isValid() {
      return this.search.isValidEx();
   }

   @Override
   public void performAction() {
      super.performAction();
      this.search.performActionEx();
      this.search.setInput();
      this.seek.performActionEx();
      this.volume.performActionEx();
      this.songs.performAction();
   }

   @Override
   public boolean isBase() {
      return true;
   }

   private static BasicButton getTextUtil2(SvgAsset svg, Runnable action) {
      BasicButton basicButton = new BasicButton(36, 36, svg, ColorPalette.INSTANCE4);
      basicButton.setRunnable(action);
      return basicButton;
   }

   private static String formatText(String text) {
      return "https://api.qrserver.com/v1/create-qr-code/?size=360x360&data="
         + URLEncoder.encode("https://music.163.com/login?codekey=" + text, StandardCharsets.UTF_8);
   }

   private static String formatText(long l) {
      long l2 = Math.max(0L, l / 1000L);
      return l2 >= 3600L ? l2 / 3600L + ":" + formatTextEx(l2 % 3600L / 60L) + ":" + formatTextEx(l2 % 60L) : l2 / 60L + ":" + formatTextEx(l2 % 60L);
   }

   private static String formatTextEx(long l) {
      return l < 10L ? "0" + l : Long.toString(l);
   }

   private final class Inner extends GuiPage {
      private static final int DEFAULT_INT = 60;
      @SerializedName("items")
      private List<OneImage.Entry4> items = List.of();
      @SerializedName("retry")
      private final BasicButton retry = new BasicButton(80, 32, "Retry", ColorPalette.INSTANCE3);

      public Inner() {
         super("Songs");
         this.retry.setRunnable(MusicPlayerPage.this.controller::performActionImpl);
      }

      @Override
      public void drawEx(long vg, int x, int y, InputHandler input) {
         this.items = MusicPlayerPage.this.controller.getResults();
         super.draw(vg, x, y, 664, 604, input);
         if (this.items == MusicPlayerPage.this.controller.getResults() && this.items.size() * 44 + super.scroll - 604.0F <= 176.0F) {
            MusicPlayerPage.this.controller.performActionAlt();
         }
      }

      @Override
      public int getSize() {
         return Math.max(604, this.items.size() * 44 + 60);
      }

      @Override
      public void draw(long vg, int x, int y, InputHandler input) {
         int n = Math.max(0, (int)(-super.scroll / 44.0F));
         int n2 = Math.min(this.items.size(), (int)Math.ceil((604.0F - super.scroll) / 44.0F));

         for (int i = n; i < n2; i++) {
            float f = y + i * 44;
            boolean isHovered = input.isHovered(x, f, 648.0F, 44.0F);
            MusicPlayerPage.this.draw(vg, this.items.get(i), i, x, f, 652.0F, 44.0F, isHovered);
            if (isHovered && input.isValid()) {
               MusicPlayerPage.this.controller.onKeyTyped(i);
            }
         }

         float f2 = y + this.items.size() * 44;
         NanoVGHelper nvg = NanoVGHelper.INSTANCE;
         if (MusicPlayerPage.this.controller.getState() == MusicPlayerController.Mode2.ERROR || MusicPlayerPage.this.controller.getLoadMoreError() != null) {
            String loadMoreError = MusicPlayerPage.this.controller.getLoadMoreError();
            nvg.draw(
               vg,
               loadMoreError == null ? "Could not load songs" : "Load failed: " + loadMoreError,
               x + 8.0F,
               f2 + 26.0F,
               544.0F,
               OneConfigColors.ERROR_600,
               12.0F,
               OneConfigFonts.WQY_MICROHEI
            );
            this.retry.draw(vg, x + 664 - 100.0F, f2 + 6.0F, input);
         } else if (MusicPlayerPage.this.controller.isLoadingMore()) {
            nvg.draw(vg, x + 16.0F, f2 + 22.0F, 6.0F, 1.75F);
            nvg.drawTextEx(vg, "Loading more songs...", x + 32.0F, f2 + 26.0F, OneConfigColors.WHITE_50, 12.0F, OneConfigFonts.SEMIBOLD);
         } else if (!this.items.isEmpty()) {
            nvg.drawTextEx(
               vg,
               MusicPlayerPage.this.controller.isHasMore() ? "Scroll to load more" : "All songs loaded",
               x + 8.0F,
               f2 + 26.0F,
               OneConfigColors.WHITE_50,
               12.0F,
               OneConfigFonts.SEMIBOLD
            );
         }
      }
   }
}
