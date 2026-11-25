package dev.southside.utils;

import com.google.gson.annotations.SerializedName;
import dev.southside.Southside;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.ShortBuffer;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.SampleBuffer;
import org.lwjgl.openal.AL10;
import org.lwjgl.system.MemoryUtil;

public final class MusicAudioPlayer {
   public static final MusicAudioPlayer INSTANCE = new MusicAudioPlayer();
   private static final int DEFAULT_INT = 8;
   private static final int DEFAULT_INT_VALUE = 8;
   private static final int DEFAULT_INT_DATA = 2304;
   public static final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10L)).followRedirects(Redirect.NORMAL).build();
   @SerializedName("requestSeq")
   private final AtomicLong requestSeq = new AtomicLong();
   @SerializedName("playback")
   private volatile MusicAudioPlayer.Inner2 playback;
   @SerializedName("state")
   private volatile MusicAudioPlayer.Mode state = MusicAudioPlayer.Mode.IDLE;
   @SerializedName("error")
   private volatile String error;
   @SerializedName("volume")
   private volatile float volume = 0.8F;
   @SerializedName("endCallback")
   private volatile Consumer<String> endCallback;

   private MusicAudioPlayer() {
   }

   public void setEndCallback(Consumer<String> consumer) {
      this.endCallback = consumer;
   }

   public void setProgress(float f) {
      this.volume = Math.max(0.0F, Math.min(1.0F, f));
   }

   public void applyText(String text, String text2) {
      this.performActionDirect();
      long l = this.requestSeq.incrementAndGet();
      MusicAudioPlayer.Inner2 inner2 = new MusicAudioPlayer.Inner2(text, text2, l);
      this.playback = inner2;
      this.state = MusicAudioPlayer.Mode.LOADING;
      this.error = null;
      inner2.thread.start();
   }

   public void performAction() {
      MusicAudioPlayer.Inner2 playback2 = this.playback;
      if (playback2 != null && this.state == MusicAudioPlayer.Mode.PLAYING) {
         playback2.paused = true;
         this.state = MusicAudioPlayer.Mode.PAUSED;
      }
   }

   public void performActionEx() {
      MusicAudioPlayer.Inner2 playback2 = this.playback;
      if (playback2 != null && this.state == MusicAudioPlayer.Mode.PAUSED) {
         playback2.paused = false;
         this.state = MusicAudioPlayer.Mode.PLAYING;
      }
   }

   public void performActionAlt() {
      if (this.state == MusicAudioPlayer.Mode.PAUSED) {
         this.performActionEx();
      } else {
         this.performAction();
      }
   }

   public void performActionImpl() {
      this.performActionDirect();
      this.state = MusicAudioPlayer.Mode.IDLE;
   }

   public void applyTextEx(String text, String text2) {
      Southside.LOGGER.warn("Song {} failed: {}", text, text2);
      this.performActionDirect();
      this.error = text2;
      this.state = MusicAudioPlayer.Mode.ERROR;
   }

   public void performAction(long l) {
      MusicAudioPlayer.Inner2 playback2 = this.playback;
      if (playback2 != null && !(playback2.frameMs <= 0.0F)) {
         playback2.seekTarget = Math.max(0L, l);
      }
   }

   public long getDurationMs() {
      MusicAudioPlayer.Inner2 playback2 = this.playback;
      return playback2 == null ? 0L : playback2.positionMs;
   }

   public long getDurationMsEx() {
      MusicAudioPlayer.Inner2 playback2 = this.playback;
      return playback2 == null ? 0L : playback2.durationMs;
   }

   public String getDisplayText() {
      MusicAudioPlayer.Inner2 playback2 = this.playback;
      return playback2 == null ? null : playback2.songId;
   }

   public MusicAudioPlayer.Mode getState() {
      return this.state;
   }

   public String getError() {
      return this.error;
   }

   private void performActionDirect() {
      this.requestSeq.incrementAndGet();
      MusicAudioPlayer.Inner2 playback2 = this.playback;
      this.playback = null;
      if (playback2 != null) {
         playback2.performAction();
      }
   }

   public void performAction(MusicAudioPlayer.Inner2 inner2) {
      MusicAudioPlayer.Inner inner = null;
      int n = 0;
      int[] values = new int[8];
      ShortBuffer shortBuffer = MemoryUtil.memAllocShort(18432);

      try {
         inner = MusicAudioPlayer.Inner.sendPacket(inner2.url, 0L);
         Southside.LOGGER.info("Music stream opened: {}", inner2.songId);
         long l = inner.bitstream.header_pos();
         int n2 = inner.computeInt(shortBuffer, 8);
         if (n2 == 0 || inner2.stopped) {
            return;
         }

         int outputFrequency = inner.decoder.getOutputFrequency();
         int n3 = inner.decoder.getOutputChannels() == 1 ? 4353 : 4355;
         inner2.frameMs = inner.frameMs;
         if (inner.length > 0L && inner.bitrate > 0) {
            inner2.durationMs = (inner.length - l) * 8000L / inner.bitrate;
         }

         AL10.alGetError();
         n = AL10.alGenSources();
         AL10.alGenBuffers(values);
         if (AL10.alGetError() == 0 && AL10.alIsSource(n)) {
            AL10.alSourcei(n, 514, 1);
            AL10.alSource3f(n, 4100, 0.0F, 0.0F, 0.0F);
            AL10.alSourcef(n, 4129, 0.0F);
            ArrayDeque arrayDeque = new ArrayDeque();

            for (int n4 : values) {
               arrayDeque.add(n4);
            }

            ArrayDeque arrayDeque2 = new ArrayDeque();
            long l2 = 0L;
            float f = -1.0F;
            performAction(n, (Integer)arrayDeque.poll(), n3, shortBuffer, outputFrequency);
            arrayDeque2.add((long)(n2 * inner2.frameMs));
            Southside.LOGGER.info("Music playback started: {} Hz", outputFrequency);
            boolean flag = false;
            boolean flag2 = false;

            label382:
            while (!inner2.stopped) {
               float volume2 = this.volume;
               if (volume2 != f) {
                  AL10.alSourcef(n, 4106, volume2 * volume2);
                  f = volume2;
               }

               long seekTarget = inner2.seekTarget;
               if (seekTarget >= 0L) {
                  inner2.seekTarget = -1L;
                  AL10.alSourceStop(n);
                  AL10.alSourcei(n, 4105, 0);
                  arrayDeque.clear();

                  for (int n5 : values) {
                     arrayDeque.add(n5);
                  }

                  arrayDeque2.clear();
                  flag2 = false;
                  flag = false;
                  long l3 = (long)((float)seekTarget / inner2.frameMs);
                  if (l3 < inner.framesRead) {
                     inner.performAction();
                     long l4 = inner.bitrate > 0 ? l + seekTarget * inner.bitrate / 8000L : 0L;
                     inner = MusicAudioPlayer.Inner.sendPacket(inner2.url, l4);
                     inner.framesRead = l4 > 0L ? l3 : 0L;
                  }

                  while (!inner2.stopped && inner.framesRead < l3 && inner.isValid()) {
                  }

                  l2 = (long)((float)inner.framesRead * inner2.frameMs);
                  inner2.positionMs = l2;
               } else {
                  int n6 = AL10.alGetSourcei(n, 4118);

                  while (n6-- > 0) {
                     arrayDeque.add(AL10.alSourceUnqueueBuffers(n));
                     Long val = (Long)arrayDeque2.poll();
                     if (val != null) {
                        l2 += val;
                     }
                  }

                  while (true) {
                     if (!flag && !arrayDeque.isEmpty() && !inner2.stopped && inner2.seekTarget < 0L) {
                        n2 = inner.computeInt(shortBuffer, 8);
                        if (n2 != 0) {
                           performAction(n, (Integer)arrayDeque.poll(), n3, shortBuffer, outputFrequency);
                           arrayDeque2.add((long)(n2 * inner2.frameMs));
                           continue;
                        }

                        flag = true;
                     }

                     int n7 = AL10.alGetSourcei(n, 4112);
                     if (inner2.paused) {
                        if (n7 == 4114) {
                           AL10.alSourcePause(n);
                        }
                     } else if (n7 != 4114 && !arrayDeque2.isEmpty()) {
                        AL10.alSourcePlay(n);
                        if (!flag2) {
                           flag2 = true;
                           this.state = MusicAudioPlayer.Mode.PLAYING;
                        }
                     }

                     float f2 = AL10.alGetSourcef(n, 4132);
                     inner2.positionMs = l2 + (long)(f2 * 1000.0F);
                     if (flag && arrayDeque2.isEmpty()) {
                        break label382;
                     }

                     Thread.sleep(10L);
                     break;
                  }
               }
            }

            if (!inner2.stopped && inner2.request == this.requestSeq.get()) {
               this.state = MusicAudioPlayer.Mode.IDLE;
               Consumer endCallback2 = this.endCallback;
               if (endCallback2 != null) {
                  endCallback2.accept(inner2.songId);
                  return;
               }
            }

            return;
         }

         this.performAction(inner2, "OpenAL unavailable");
      } catch (InterruptedException e) {
         Thread.currentThread().interrupt();
         return;
      } catch (Exception e2) {
         Southside.LOGGER.warn("Playback failed for {}", inner2.songId, e2);
         this.performAction(inner2, e2.getMessage() == null ? e2.getClass().getSimpleName() : e2.getMessage());
         return;
      } finally {
         if (n != 0) {
            AL10.alSourceStop(n);
            AL10.alSourcei(n, 4105, 0);
            AL10.alDeleteSources(n);
            AL10.alDeleteBuffers(values);
         }

         MemoryUtil.memFree(shortBuffer);
         if (inner != null) {
            inner.performAction();
         }
      }
   }

   private static void performAction(int n, int n2, int n3, ShortBuffer shortBuffer, int n4) {
      AL10.alBufferData(n2, n3, shortBuffer, n4);
      AL10.alSourceQueueBuffers(n, n2);
   }

   private void performAction(MusicAudioPlayer.Inner2 inner2, String text) {
      if (!inner2.stopped && inner2.request == this.requestSeq.get()) {
         Southside.LOGGER.warn("Playback failed for {}: {}", inner2.songId, text);
         this.error = text;
         this.state = MusicAudioPlayer.Mode.ERROR;
      }
   }

   private static final class Inner {
      @SerializedName("body")
      public final InputStream body;
      @SerializedName("bitstream")
      public final Bitstream bitstream;
      @SerializedName("decoder")
      public final Decoder decoder = new Decoder();
      @SerializedName("length")
      public final long length;
      @SerializedName("bitrate")
      public int bitrate;
      @SerializedName("frameMs")
      public float frameMs;
      @SerializedName("framesRead")
      public long framesRead;

      private Inner(InputStream in, long l) {
         this.body = in;
         this.bitstream = new Bitstream(new BufferedInputStream(in, 65536));
         this.length = l;
      }

      public static MusicAudioPlayer.Inner sendPacket(String text, long l) throws IOException, InterruptedException {
         Builder builder = HttpRequest.newBuilder(URI.create(text))
            .timeout(Duration.ofSeconds(20L))
            .header("User-Agent", "Mozilla/5.0 Southside/1.0")
            .header("Referer", "https://music.163.com/")
            .GET();
         if (l > 0L) {
            builder.header("Range", "bytes=" + l + "-");
         }

         HttpResponse httpResponse = MusicAudioPlayer.httpClient.send(builder.build(), BodyHandlers.ofInputStream());
         if (httpResponse.statusCode() / 100 != 2) {
            ((InputStream)httpResponse.body()).close();
            throw new IOException("HTTP " + httpResponse.statusCode());
         } else {
            long l2 = httpResponse.headers().firstValueAsLong("content-length").orElse(-1L);
            return new MusicAudioPlayer.Inner((InputStream)httpResponse.body(), l <= 0L && l2 >= 0L ? l2 : -1L);
         }
      }

      public int computeInt(ShortBuffer shortBuffer, int n) throws Exception {
         shortBuffer.clear();
         int n2 = 0;

         while (n2 < n) {
            Header header = this.bitstream.readFrame();
            if (header == null) {
               break;
            }

            this.performAction(header);
            SampleBuffer sampleBuffer = (SampleBuffer)this.decoder.decodeFrame(header, this.bitstream);
            this.bitstream.closeFrame();
            int bufferLength = sampleBuffer.getBufferLength();
            if (bufferLength > shortBuffer.remaining()) {
               break;
            }

            shortBuffer.put(sampleBuffer.getBuffer(), 0, bufferLength);
            n2++;
            this.framesRead++;
         }

         shortBuffer.flip();
         return n2;
      }

      public boolean isValid() throws Exception {
         Header header = this.bitstream.readFrame();
         if (header == null) {
            return false;
         }

         this.performAction(header);
         this.bitstream.closeFrame();
         this.framesRead++;
         return true;
      }

      private void performAction(Header header) {
         if (this.frameMs <= 0.0F) {
            this.frameMs = header.ms_per_frame();
         }

         if (this.bitrate <= 0) {
            this.bitrate = header.bitrate();
         }
      }

      public void performAction() {
         try {
            this.bitstream.close();
         } catch (Exception e) {
         }

         try {
            this.body.close();
         } catch (Exception e2) {
         }
      }
   }

   private final class Inner2 {
      @SerializedName("songId")
      public final String songId;
      @SerializedName("url")
      public final String url;
      @SerializedName("request")
      public final long request;
      @SerializedName("thread")
      public final Thread thread;
      @SerializedName("stopped")
      public volatile boolean stopped;
      @SerializedName("paused")
      public volatile boolean paused;
      @SerializedName("positionMs")
      public volatile long positionMs;
      @SerializedName("durationMs")
      public volatile long durationMs;
      @SerializedName("frameMs")
      public volatile float frameMs;
      @SerializedName("seekTarget")
      public volatile long seekTarget;
      private static final long DEFAULT_LONG = -1L;

      public Inner2(String text, String text2, long l) {
         this.seekTarget = DEFAULT_LONG;
         this.songId = text;
         this.url = text2;
         this.request = l;
         this.thread = new Thread(() -> MusicAudioPlayer.this.performAction(this), "Southside-MusicPlayer");
         this.thread.setDaemon(true);
      }

      public void performAction() {
         this.stopped = true;
         this.thread.interrupt();
      }
   }

   public enum Mode {
      IDLE,
      LOADING,
      PLAYING,
      PAUSED,
      ERROR;


      // $VF: synthetic method
      private static MusicAudioPlayer.Mode[] createValues() {
         return new MusicAudioPlayer.Mode[]{IDLE, LOADING, PLAYING, PAUSED, ERROR};
      }


   }
}
