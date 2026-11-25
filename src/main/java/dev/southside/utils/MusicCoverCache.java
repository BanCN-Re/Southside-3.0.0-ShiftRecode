package dev.southside.utils;

import dev.southside.Southside;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class MusicCoverCache {
   private static final int DEFAULT_INT = 512;
   private static final int DEFAULT_INT_VALUE = 96;
   private static final ConcurrentHashMap<String, ImageAsset> imageAssetMap = new ConcurrentHashMap<>();
   private static final Set<String> stringsSet = ConcurrentHashMap.newKeySet();
   private static final Set<String> stringsSetValue = ConcurrentHashMap.newKeySet();
   private static final Path path = Path.of(System.getProperty("java.io.tmpdir"), "southside-covers");
   private static final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8L)).followRedirects(Redirect.NORMAL).build();
   private static final ExecutorService executorService = Executors.newFixedThreadPool(3, action -> {
      Thread thread = new Thread(action, "Southside-MusicCover");
      thread.setDaemon(true);
      return thread;
   });

   private MusicCoverCache() {
   }

   public static ImageAsset getImageAsset(String text) {
      return getImageAsset(text, 512);
   }

   public static ImageAsset getImageAssetEx(String text) {
      return getImageAsset(text, 96);
   }

   private static ImageAsset getImageAsset(String text, int n) {
      if (text != null && !text.isBlank()) {
         String text2 = text + "#" + n;
         ImageAsset image = imageAssetMap.get(text2);
         if (image != null) {
            return image;
         } else if (!stringsSetValue.contains(text2) && stringsSet.add(text2)) {
            getCompletableFuture(text, n).whenComplete((path2, t) -> {
               stringsSet.remove(text2);
               if (t == null && path2 != null) {
                  imageAssetMap.put(text2, new ImageAsset(path2.toString()));
               } else {
                  stringsSetValue.add(text2);
               }
            });
            return imageAssetMap.get(text2);
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   private static CompletableFuture<Path> getCompletableFuture(String text, int n) {
      return CompletableFuture.supplyAsync(
         () -> {
            try {
               HttpRequest httpRequest = HttpRequest.newBuilder(getUri(text, n))
                  .timeout(Duration.ofSeconds(10L))
                  .header("User-Agent", "Mozilla/5.0 Southside/1.0")
                  .header("Referer", "https://music.163.com/")
                  .GET()
                  .build();
               HttpResponse httpResponse = httpClient.send(httpRequest, BodyHandlers.ofByteArray());
               if (httpResponse.statusCode() / 100 == 2 && ((byte[])httpResponse.body()).length != 0) {
                  Files.createDirectories(path);
                  Path path2 = path.resolve(formatText(text) + "-" + n + ".img");
                  Files.write(path2, (byte[])httpResponse.body());
                  return path2;
               } else {
                  return null;
               }
            } catch (InterruptedException e) {
               Thread.currentThread().interrupt();
               return null;
            } catch (Exception e2) {
               Southside.LOGGER.debug("Cover download failed: {}", e2.getMessage());
               return null;
            }
         },
         executorService
      );
   }

   private static URI getUri(String text, int n) {
      URI uri = URI.create(text);
      String host = uri.getHost();
      String rawQuery = uri.getRawQuery();
      return host != null && host.toLowerCase(Locale.ROOT).endsWith(".music.126.net") && (rawQuery == null || !rawQuery.contains("param="))
         ? URI.create(text + (rawQuery == null ? "?" : "&") + "param=" + n + "y" + n)
         : uri;
   }

   private static String formatText(String text) {
      try {
         byte[] bytes = MessageDigest.getInstance("SHA-256").digest(text.getBytes());
         StringBuilder sb = new StringBuilder();

         for (int i = 0; i < 8; i++) {
            sb.append(String.format("%02x", bytes[i]));
         }

         return sb.toString();
      } catch (Exception e) {
         return Integer.toHexString(text.hashCode());
      }
   }

   static {
      short s = -9916;
      short s2 = -1671;
   }
}
