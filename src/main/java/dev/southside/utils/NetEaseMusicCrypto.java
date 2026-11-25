package dev.southside.utils;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public final class NetEaseMusicCrypto {
   private static final byte[] DEFAULT_BYTES = "e82ckenh8dichen8".getBytes(StandardCharsets.UTF_8);
   private static final String DEFAULT_TEXT = "e0b509f6259df8642dbc35662901477df22677ec152b5ff68ace615bb7b725152b3ab17a876aea8a5aa76d2e417629ec4ee341f56135fccf695280104e0312ecbda92557c93870114af6c9d05c4f7f0c3685b7a46bee255932575cce10b424d813cfe4875d3e82047b97ddef52741d546b8e289dc6935b3ece0462db0a22b8e7";
   private static final byte[] DEFAULT_BYTES_VALUE = "0CoJUm6Qyw8W8jud".getBytes(StandardCharsets.UTF_8);
   private static final byte[] DEFAULT_BYTES_DATA = "0102030405060708".getBytes(StandardCharsets.UTF_8);
   private static final long DEFAULT_LONG = 65537L;

   private NetEaseMusicCrypto() {
   }

   public static String formatText(byte[] bytes) {
      try {
         MessageDigest instance = MessageDigest.getInstance("MD5");
         byte[] bytes2 = instance.digest(bytes);
         return formatText(bytes2, false);
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }

   public static String formatText(byte[] bytes, boolean flag) {
      char[] chars = (flag ? "0123456789ABCDEF" : "0123456789abcdef").toCharArray();
      StringBuilder sb = new StringBuilder(bytes.length * 2);

      for (byte b : bytes) {
         sb.append(chars[b >> 4 & 15]);
         sb.append(chars[b & 15]);
      }

      return sb.toString();
   }

   public static byte[] getByteList() {
      return (byte[])DEFAULT_BYTES.clone();
   }

   public static Map<String, String> buildMap(String text) {
      byte[] bytes = new byte[16];
      new Random().nextBytes(bytes);
      String text2 = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

      for (int i = 0; i < 16; i++) {
         bytes[i] = (byte)text2.charAt((bytes[i] & 255) % text2.length());
      }

      return buildMap(text, bytes);
   }

   private static Map<String, String> buildMap(String text, byte[] bytes) {
      try {
         byte[] byteList = getByteList(text.getBytes(StandardCharsets.UTF_8), DEFAULT_BYTES_VALUE, DEFAULT_BYTES_DATA);
         short s = 6139;
         String text2 = Base64.getEncoder().encodeToString(byteList);
         long l = 6791622049088651865L;
         byte[] byteList2 = getByteList(text2.getBytes(StandardCharsets.UTF_8), bytes, DEFAULT_BYTES_DATA);
         String text3 = Base64.getEncoder().encodeToString(byteList2);
         byte[] bytes2 = new byte[16];

         for (int i = 0; i < 16; i++) {
            bytes2[i] = bytes[15 - i];
         }

         BigInteger bigInteger = new BigInteger(
            "e0b509f6259df8642dbc35662901477df22677ec152b5ff68ace615bb7b725152b3ab17a876aea8a5aa76d2e417629ec4ee341f56135fccf695280104e0312ecbda92557c93870114af6c9d05c4f7f0c3685b7a46bee255932575cce10b424d813cfe4875d3e82047b97ddef52741d546b8e289dc6935b3ece0462db0a22b8e7",
            16
         );
         s = -10374;
         BigInteger bigInteger2 = new BigInteger(1, bytes2).modPow(BigInteger.valueOf(DEFAULT_LONG), bigInteger);
         short s2 = -7109;
         String formatted = String.format("%0256x", bigInteger2);
         LinkedHashMap linkedHashMap = new LinkedHashMap();
         linkedHashMap.put("params", text3);
         linkedHashMap.put("encSecKey", formatted);
         return linkedHashMap;
      } catch (Exception e) {
         throw new RuntimeException("WEAPI encrypt failed", e);
      }
   }

   public static Map<String, String> buildMap(String text, String text2) {
      try {
         String text3 = formatText(("nobody" + text + "use" + text2 + "md5forencrypt").getBytes(StandardCharsets.UTF_8));
         String text4 = text + "-36cd479b6b5-" + text2 + "-36cd479b6b5-" + text3;
         byte[] byteList = getByteList(text4.getBytes(StandardCharsets.UTF_8), DEFAULT_BYTES);
         LinkedHashMap linkedHashMap = new LinkedHashMap();
         linkedHashMap.put("params", formatText(byteList, true));
         return linkedHashMap;
      } catch (Exception e) {
         throw new RuntimeException("EAPI encrypt failed", e);
      }
   }

   private static byte[] getByteList(byte[] bytes, byte[] bytes2, byte[] bytes3) throws Exception {
      Cipher instance = Cipher.getInstance("AES/CBC/PKCS5Padding");
      instance.init(1, new SecretKeySpec(bytes2, "AES"), new IvParameterSpec(bytes3));
      return instance.doFinal(bytes);
   }

   private static byte[] getByteList(byte[] bytes, byte[] bytes2) throws Exception {
      Cipher instance = Cipher.getInstance("AES/ECB/PKCS5Padding");
      instance.init(1, new SecretKeySpec(bytes2, "AES"));
      return instance.doFinal(bytes);
   }

   public static byte[] getByteListEx(byte[] bytes, byte[] bytes2) throws Exception {
      Cipher instance = Cipher.getInstance("AES/ECB/PKCS5Padding");
      instance.init(2, new SecretKeySpec(bytes2, "AES"));
      return instance.doFinal(bytes);
   }
}
