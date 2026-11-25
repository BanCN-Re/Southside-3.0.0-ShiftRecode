package dev.southside.utils;

import cc.polyfrost.oneconfig.images.OneImage;
import com.google.gson.annotations.SerializedName;
import dev.southside.Southside;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public final class MusicPlayerController {
   private static final MusicPlayerController INSTANCE = new MusicPlayerController();
   private static final String DEFAULT_TEXT = "3778678";
   private static final int DEFAULT_INT = 50;
   @SerializedName("client")
   private final OneImage client = new OneImage();
   @SerializedName("playRequest")
   private final AtomicLong playRequest = new AtomicLong();
   @SerializedName("defaultLoaded")
   private final AtomicBoolean defaultLoaded = new AtomicBoolean();
   @SerializedName("results")
   private volatile List<OneImage.Entry4> results = List.of();
   @SerializedName("keywords")
   private volatile String keywords = "";
   @SerializedName("state")
   private volatile MusicPlayerController.Mode2 state = MusicPlayerController.Mode2.IDLE;
   @SerializedName("error")
   private volatile String error;
   @SerializedName("loadingMore")
   private volatile boolean loadingMore;
   @SerializedName("hasMore")
   private volatile boolean hasMore;
   @SerializedName("loadMoreError")
   private volatile String loadMoreError;
   @SerializedName("listRequest")
   private long listRequest;
   @SerializedName("nextOffset")
   private int nextOffset;
   @SerializedName("activeQuery")
   private String activeQuery;
   @SerializedName("playlistIds")
   private CompletableFuture<List<String>> playlistIds;
   @SerializedName("currentIndex")
   private volatile int currentIndex = -1;
   @SerializedName("resolvingSongId")
   private volatile String resolvingSongId;
   @SerializedName("shuffle")
   public volatile boolean shuffle;
   @SerializedName("loop")
   public volatile boolean loop;
   @SerializedName("volume")
   public volatile int volume = 80;
   @SerializedName("loginState")
   public volatile MusicPlayerController.Mode loginState = MusicPlayerController.Mode.OFF;
   @SerializedName("nickname")
   public volatile String nickname;
   @SerializedName("avatarUrl")
   public volatile String avatarUrl;
   @SerializedName("qrKey")
   public volatile String qrKey;
   @SerializedName("polling")
   private volatile boolean polling;
   private static final long DEFAULT_LONG = 2500L;

   public static MusicPlayerController getINSTANCE() {
      return INSTANCE;
   }

   private MusicPlayerController() {
      MusicAudioPlayer.INSTANCE.setEndCallback(this::applyTextAlt);
   }

   public MusicPlayerController.Mode2 getState() {
      return this.state;
   }

   public String getError() {
      return this.error;
   }

   public String getKeywords() {
      return this.keywords;
   }

   public List<OneImage.Entry4> getResults() {
      return this.results;
   }

   public int getCurrentIndex() {
      return this.currentIndex;
   }

   public boolean isLoadingMore() {
      return this.loadingMore;
   }

   public boolean isHasMore() {
      return this.hasMore;
   }

   public String getLoadMoreError() {
      return this.loadMoreError;
   }

   public synchronized OneImage.Entry4 getEntry4() {
      List results2 = this.results;
      int currentIndex2 = this.currentIndex;
      return currentIndex2 >= 0 && currentIndex2 < results2.size() ? (OneImage.Entry4)results2.get(currentIndex2) : null;
   }

   public synchronized void performAction() {
      if (this.defaultLoaded.compareAndSet(false, true)) {
         if (this.results.isEmpty()) {
            this.performActionEx();
         }
      }
   }

   public synchronized void performActionEx() {
      this.applyTextEx(null);
   }

   public synchronized void applyText(String text) {
      String text2 = text.trim();
      this.applyTextEx(text2.isEmpty() ? null : text2);
   }

   private void applyTextEx(String text) {
      this.defaultLoaded.set(true);
      this.listRequest++;
      this.activeQuery = text;
      this.keywords = text == null ? "" : text;
      this.playlistIds = null;
      this.results = List.of();
      this.currentIndex = -1;
      this.playRequest.incrementAndGet();
      this.resolvingSongId = null;
      this.nextOffset = 0;
      this.hasMore = false;
      this.loadingMore = false;
      this.loadMoreError = null;
      this.state = MusicPlayerController.Mode2.LOADING;
      this.error = null;
      this.performActionDirect();
   }

   public synchronized void performActionAlt() {
      if (this.state == MusicPlayerController.Mode2.IDLE && !this.loadingMore && this.hasMore && this.loadMoreError == null) {
         this.loadingMore = true;
         this.performActionDirect();
      }
   }

   public synchronized void performActionImpl() {
      if (this.state == MusicPlayerController.Mode2.ERROR) {
         this.applyTextEx(this.activeQuery);
      } else if (this.loadMoreError != null) {
         this.loadMoreError = null;
         this.performActionAlt();
      }
   }

   private void performActionDirect() {
      long listRequest2 = this.listRequest;
      int nextOffset2 = this.nextOffset;

      try {
         CompletableFuture<OneImage.Entry3> completableFuture;
         if (this.activeQuery == null) {
            if (this.playlistIds == null) {
               this.playlistIds = this.client.getCompletableFuture2("3778678");
            }

            completableFuture = this.playlistIds.thenCompose(list -> this.client.setColorAtPos((List<String>)list, nextOffset2, 50));
         } else {
            completableFuture = this.client.setColorAtPos(this.activeQuery, nextOffset2, 50);
         }

         completableFuture.whenComplete((entry3, t) -> this.draw(listRequest2, nextOffset2, entry3, t));
      } catch (RuntimeException e) {
         this.draw(listRequest2, nextOffset2, null, e);
      }
   }

   public void setKeywords(String text) {
      this.keywords = text;
   }

   private synchronized void draw(long l, int n, OneImage.Entry3 entry3, Throwable t) {
      if (l == this.listRequest) {
         if (t != null) {
            String text = formatText(t);
            if (this.loadingMore) {
               this.loadMoreError = text;
            } else {
               this.error = text;
               this.state = MusicPlayerController.Mode2.ERROR;
            }

            this.loadingMore = false;
            Southside.LOGGER.warn("Music list request failed: {}", text);
         } else {
            LinkedHashMap linkedHashMap = new LinkedHashMap();

            for (OneImage.Entry4 entry4 : this.results) {
               linkedHashMap.put(entry4.getId(), entry4);
            }

            for (OneImage.Entry4 entry42 : entry3.getSongs()) {
               linkedHashMap.putIfAbsent(entry42.getId(), entry42);
            }

            this.results = List.copyOf(linkedHashMap.values());
            this.nextOffset = entry3.getNextOffset();
            this.hasMore = entry3.isHasMore() && this.nextOffset > n;
            this.state = MusicPlayerController.Mode2.IDLE;
            this.loadingMore = false;
         }
      }
   }

   public synchronized void onKeyTyped(int n) {
      List results2 = this.results;
      if (n >= 0 && n < results2.size()) {
         this.currentIndex = n;
         this.performAction((OneImage.Entry4)results2.get(n));
      }
   }

   public void performAction(OneImage.Entry4 entry4) {
      long l = this.playRequest.incrementAndGet();
      this.resolvingSongId = entry4.getId();
      MusicAudioPlayer.INSTANCE.setProgress(this.volume / 100.0F);
      this.client.getCompletableFuture(entry4.getId()).whenComplete((text, t) -> {
         if (l == this.playRequest.get()) {
            this.resolvingSongId = null;
            Southside.LOGGER.info("Music stream lookup for {}: {}", entry4.getTitle(), t != null ? formatText(t) : text);
            if (t == null && text != null && !text.isBlank()) {
               MusicAudioPlayer.INSTANCE.applyText(entry4.getId(), text);
            } else {
               String text2 = t == null ? "No playable audio stream" : "Stream lookup failed: " + formatText(t);
               MusicAudioPlayer.INSTANCE.applyTextEx(entry4.getId(), text2);
               Southside.LOGGER.warn("Music stream unavailable for {}: {}", entry4.getTitle(), text2);
            }
         }
      });
   }

   public boolean matches(String text) {
      if (text == null) {
         return false;
      }

      if (text.equals(this.resolvingSongId)) {
         return true;
      }

      MusicAudioPlayer musicAudioPlayer = MusicAudioPlayer.INSTANCE;
      return musicAudioPlayer.getState() == MusicAudioPlayer.Mode.LOADING && text.equals(musicAudioPlayer.getDisplayText());
   }

   public synchronized void performActionSafe() {
      List results2 = this.results;
      if (!results2.isEmpty()) {
         if (this.shuffle) {
            this.currentIndex = (int)(Math.random() * results2.size());
         } else {
            this.currentIndex = (this.currentIndex + 1) % results2.size();
         }

         this.performAction((OneImage.Entry4)results2.get(this.currentIndex));
      }
   }

   public synchronized void performActionCustom() {
      List results2 = this.results;
      if (!results2.isEmpty()) {
         this.currentIndex = this.currentIndex <= 0 ? results2.size() - 1 : this.currentIndex - 1;
         this.performAction((OneImage.Entry4)results2.get(this.currentIndex));
      }
   }

   public void performActionWithOffset() {
      MusicAudioPlayer.INSTANCE.performActionAlt();
   }

   private void applyTextAlt(String text) {
      if (this.loop) {
         OneImage.Entry4 entry4 = this.getEntry4();
         if (entry4 != null) {
            this.performAction(entry4);
            return;
         }
      }

      this.performActionSafe();
   }

   public void performActionByIndex() {
      this.loginState = MusicPlayerController.Mode.LOADING;
      this.nickname = null;
      this.avatarUrl = null;
      this.qrKey = null;
      this.client.getCompletableFuture().whenComplete((text, t) -> {
         if (t == null && text != null && !text.isBlank()) {
            this.qrKey = text;
            if (this.loginState == MusicPlayerController.Mode.LOADING) {
               this.loginState = MusicPlayerController.Mode.WAIT_SCAN;
            }

            this.performActionV2();
         } else {
            if (this.loginState == MusicPlayerController.Mode.LOADING) {
               this.loginState = MusicPlayerController.Mode.EXPIRED;
            }
         }
      });
   }

   public void performActionInternal() {
      this.loginState = MusicPlayerController.Mode.OFF;
      this.qrKey = null;
   }

   public void performActionHelper() {
      this.client.whenComplete();
      this.nickname = null;
      this.avatarUrl = null;
      this.loginState = MusicPlayerController.Mode.OFF;
   }

   private void performActionV2() {
      if (!this.polling) {
         this.polling = true;
         Thread thread = new Thread(() -> {
            try {
               while (this.polling && this.qrKey != null) {
                  Thread.sleep(DEFAULT_LONG);
                  String qrKey2 = this.qrKey;
                  if (qrKey2 == null) {
                     return;
                  }

                  OneImage.Entry2 entry2 = this.client.getCompletableFuture3(qrKey2).exceptionally(t -> null).join();
                  if (entry2 != null) {
                     switch (entry2.getCode()) {
                        case 800:
                           this.loginState = MusicPlayerController.Mode.EXPIRED;
                           return;
                        case 801:
                           this.loginState = MusicPlayerController.Mode.WAIT_SCAN;
                           break;
                        case 802:
                           this.loginState = MusicPlayerController.Mode.WAIT_CONFIRM;
                           break;
                        case 803:
                           this.client.getCompletableFuture2().thenAccept(entry -> {
                              if (entry != null) {
                                 this.nickname = entry.getNickname();
                                 this.avatarUrl = entry.getAvatarUrl();
                              }

                              this.loginState = MusicPlayerController.Mode.LOGGED_IN;
                              this.qrKey = null;
                           });
                           return;
                     }
                  }
               }
            } catch (InterruptedException e) {
               Thread.currentThread().interrupt();
            } finally {
               this.polling = false;
            }
         }, "Southside-MusicQR");
         thread.setDaemon(true);
         thread.start();
      }
   }

   public synchronized void performActionV3() {
      this.listRequest++;
      this.playlistIds = null;
      this.activeQuery = null;
      this.defaultLoaded.set(false);
      this.nextOffset = 0;
      this.hasMore = false;
      this.loadingMore = false;
      this.loadMoreError = null;
      this.error = null;
      this.playRequest.incrementAndGet();
      this.resolvingSongId = null;
      this.polling = false;
      this.qrKey = null;
      MusicAudioPlayer.INSTANCE.performActionImpl();
      this.results = List.of();
      this.currentIndex = -1;
      this.state = MusicPlayerController.Mode2.IDLE;
   }

   private static String formatText(Throwable t) {
      Throwable t2 = t.getCause() == null ? t : t.getCause();
      String message = t2.getMessage();
      return message != null && !message.isBlank() ? message : t2.getClass().getSimpleName();
   }

   public enum Mode {
      OFF,
      LOADING,
      WAIT_SCAN,
      WAIT_CONFIRM,
      EXPIRED,
      LOGGED_IN;

      private static int[] values;
      private static final long DEFAULT_LONG = -2812553346601189370L;

      // $VF: synthetic method
      private static MusicPlayerController.Mode[] getModeList() {
         MusicPlayerController.Mode[] modeArray = new MusicPlayerController.Mode[(int)DEFAULT_LONG];
         modeArray[0] = OFF;
         modeArray[1] = LOADING;
         modeArray[2] = WAIT_SCAN;
         modeArray[3] = WAIT_CONFIRM;
         modeArray[4] = EXPIRED;
         modeArray[5] = LOGGED_IN;
         return modeArray;
      }


      public static void setValues(int[] values2) {
         values = values2;
      }

   }

   public enum Mode2 {
      IDLE,
      LOADING,
      ERROR;


      // $VF: synthetic method
      private static MusicPlayerController.Mode2[] getMode2Array() {
         return new MusicPlayerController.Mode2[]{IDLE, LOADING, ERROR};
      }


   }
}
