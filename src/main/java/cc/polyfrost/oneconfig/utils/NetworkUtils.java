package cc.polyfrost.oneconfig.utils;

import com.google.gson.JsonElement;
import dev.southside.utils.JsonParserUtils;
import java.awt.Desktop;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public final class NetworkUtils {
   public static String formatText(String text, String text2, int n, boolean flag) {
      try (InputStreamReader inputStreamReader = new InputStreamReader(getInputStream(text, text2, n, flag), StandardCharsets.UTF_8)) {
         return org.apache.commons.io.IOUtils.toString(inputStreamReader);
      } catch (Exception e) {
         e.printStackTrace();
         return null;
      }
   }

   public static String formatText(String text) {
      return formatText(text, "OneConfig/1.0.0", 5000, false);
   }

   public static JsonElement toJson(String text, String text2, int n, boolean flag) {
      return JsonParserUtils.toJson(formatText(text, text2, n, flag));
   }

   public static JsonElement toJson(String text) {
      return toJson(text, "OneConfig/1.0.0", 5000, false);
   }

   public static boolean downloadFile(String text, File file, String text2, int n, boolean flag) {
      text = text.replace(" ", "%20");

      try (
         FileOutputStream fileOutputStream = new FileOutputStream(file);
         BufferedInputStream bufferedInputStream = new BufferedInputStream(getInputStream(text, text2, n, flag));
      ) {
         org.apache.commons.io.IOUtils.copy(bufferedInputStream, fileOutputStream);
         return true;
      } catch (Exception e) {
         e.printStackTrace();
         return false;
      }
   }

   public static boolean downloadFile(String text, File file) {
      return downloadFile(text, file, "OneConfig/1.0.0", 5000, false);
   }

   public static void applyText(String text) {
      try {
         Desktop.getDesktop().browse(URI.create(text));
      } catch (IOException e) {
      }
   }

   public static InputStream getInputStream(String text, String text2, int n, boolean flag) throws IOException {
      HttpURLConnection httpURLConnection = (HttpURLConnection)new URL(text).openConnection();
      httpURLConnection.setRequestMethod("GET");
      httpURLConnection.setUseCaches(flag);
      httpURLConnection.addRequestProperty("User-Agent", text2);
      httpURLConnection.setReadTimeout(n);
      httpURLConnection.setConnectTimeout(n);
      httpURLConnection.setDoOutput(true);
      return httpURLConnection.getInputStream();
   }
}
