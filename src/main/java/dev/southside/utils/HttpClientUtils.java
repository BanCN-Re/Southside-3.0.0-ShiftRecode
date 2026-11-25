package dev.southside.utils;

import com.google.gson.ExclusionStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import dev.southside.Southside;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

public class HttpClientUtils {
   public static final Gson GSON = new GsonBuilder()
      .setExclusionStrategies(new ExclusionStrategy[]{new GsonUtils.ExclusionStrategyImpl()})
      .registerTypeAdapter(Date.class, new IsoDateDeserializer())
      .create();
   private static String[] names;

   public static HttpClientUtils.Inner2 getInner2Ex(String text) {
      return getInner2(HttpClientUtils.Mode.POST, text);
   }

   public static HttpClientUtils.Inner2 getInner2(String text) {
      return getInner2(HttpClientUtils.Mode.GET, text);
   }

   public static HttpClientUtils.Inner2 getInner2(HttpClientUtils.Mode mode, String text) {
      return new HttpClientUtils.Inner2(mode, text);
   }

   static {
      setNames(null);
   }

   public static void setNames(String[] textArray) {
      names = textArray;
   }

   @FunctionalInterface
   public interface Inner<T> {
      T getT();
   }

   public static class Inner2 {
      @SerializedName("context")
      private final HttpClientContext context = HttpClientContext.create();
      @SerializedName("cookieStore")
      private final CookieStore cookieStore = new BasicCookieStore();
      @SerializedName("request")
      private final HttpRequestBase request;
      @SerializedName("requestConfig")
      private final Builder requestConfig = RequestConfig.custom();
      @SerializedName("keepAlive")
      private boolean keepAlive = true;
      @SerializedName("charset")
      private String charset = "UTF-8";
      private static final long DEFAULT_LONG = 4550796460345528320L;

      public Inner2(HttpClientUtils.Mode mode, String text) {
         this.request = (HttpRequestBase)(mode.equals(HttpClientUtils.Mode.GET) ? new HttpGet(text) : new HttpPost(text));
         this.getInner2Ex();
         this.context.setCookieStore(this.cookieStore);
         this.getInner2Direct("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Safari/537.36");
      }

      public HttpClientUtils.Inner2 getInner2(String text, int n) {
         this.requestConfig.setProxy(new HttpHost(text, n));
         return this;
      }

      public HttpClientUtils.Inner2 getInner2Ex() {
         this.requestConfig.setCookieSpec("default");
         return this;
      }

      public HttpClientUtils.Inner2 getInner2Alt() {
         this.keepAlive = false;
         return this;
      }

      public HttpClientUtils.Inner2 getInner2Ex(String text) {
         this.charset = text;
         return this;
      }

      public HttpClientUtils.Inner2 getInner2(String text, Object obj) {
         this.request.setHeader(text, obj.toString());
         return this;
      }

      public HttpClientUtils.Inner2 getInner2Alt(String text) {
         this.request.setHeader("Accept", text);
         return this;
      }

      public HttpClientUtils.Inner2 getInner2Impl() {
         return this.getInner2Alt("application/json, text/json, text/x-json, text/javascript");
      }

      public HttpClientUtils.Inner2 getInner2AltEx() {
         return this.getInner2Alt("*/*");
      }

      public HttpClientUtils.Inner2 getInner2(String text) {
         this.request.setHeader("Authorization", text);
         return this;
      }

      public HttpClientUtils.Inner2 getInner2Impl(String text) {
         return this.getInner2("Bearer " + text);
      }

      public HttpClientUtils.Inner2 getInner2Direct(String text) {
         this.request.setHeader("User-Agent", text);
         return this;
      }

      public HttpClientUtils.Inner2 getInner2Safe(String text) {
         this.request.setHeader("Cookie", text);
         return this;
      }

      public HttpClientUtils.Inner2 getInner2Custom(String text) {
         this.request.setHeader("Content-Type", ContentType.APPLICATION_FORM_URLENCODED.getMimeType());
         if (this.request instanceof HttpPost httpPost) {
            httpPost.setEntity(new StringEntity(text, ContentType.APPLICATION_FORM_URLENCODED.withCharset(this.charset)));
         }

         return this;
      }

      public HttpClientUtils.Inner2 getInner2(StringEntity stringEntity) {
         this.request.setHeader("Content-Type", ContentType.APPLICATION_FORM_URLENCODED.getMimeType());
         if (this.request instanceof HttpPost httpPost) {
            httpPost.setEntity(stringEntity);
         }

         return this;
      }

      public HttpClientUtils.Inner2 getInner2WithOffset(String text) {
         this.request.setHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType());
         if (this.request instanceof HttpPost httpPost) {
            httpPost.setEntity(new StringEntity(text, ContentType.APPLICATION_JSON.withCharset(this.charset)));
         }

         return this;
      }

      public HttpClientUtils.Inner2 getInner2WithOffset(Object obj) {
         return this.getInner2WithOffset(HttpClientUtils.GSON.toJson(obj));
      }

      public HttpClientUtils.Inner2 getInner2(byte[] bytes) {
         this.request.setHeader("Content-Type", ContentType.DEFAULT_BINARY.getMimeType());
         if (this.request instanceof HttpPost httpPost) {
            httpPost.setEntity(new ByteArrayEntity(bytes));
         }

         return this;
      }

      public HttpClientUtils.Inner2 getInner2(Function<String, byte[]> fn, String text) {
         return this.getInner2((byte[])fn.apply(text));
      }

      public HttpClientUtils.Inner2 getInner2(Function<String, byte[]> fn, Object obj) {
         return this.getInner2((byte[])fn.apply(HttpClientUtils.GSON.toJson(obj)));
      }

      private void performAction() {
         if (!this.keepAlive) {
            this.request.setHeader("Connection", "close");
         }

         this.request.setConfig(this.requestConfig.build());
      }

      public <T> T getT(HttpClientUtils.Inner<T> inner) {
         try {
            CloseableHttpClient closeableHttpClient = HttpClientBuilder.create().build();

            Object obj;
            try {
               this.performAction();
               closeableHttpClient.execute(this.request);
               obj = inner.getT();
            } catch (Throwable e) {
               if (closeableHttpClient != null) {
                  try {
                     closeableHttpClient.close();
                  } catch (Throwable e2) {
                     e.addSuppressed(e2);
                  }
               }

               throw e;
            }

            if (closeableHttpClient != null) {
               closeableHttpClient.close();
            }

            return (T)obj;
         } catch (IOException e3) {
            Southside.LOGGER.error("Unexpected error in HttpRequestFactory", e3);
            return null;
         }
      }

      public <T> T getT(HttpClientUtils.Inner3<T> inner3) {
         try {
            CloseableHttpClient closeableHttpClient = HttpClientBuilder.create().build();

            Object obj;
            try {
               this.performAction();
               CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(this.request);
               obj = inner3.getT(closeableHttpResponse);
            } catch (Throwable e) {
               if (closeableHttpClient != null) {
                  try {
                     closeableHttpClient.close();
                  } catch (Throwable e2) {
                     e.addSuppressed(e2);
                  }
               }

               throw e;
            }

            if (closeableHttpClient != null) {
               closeableHttpClient.close();
            }

            return (T)obj;
         } catch (IOException e3) {
            Southside.LOGGER.error("Unexpected error in HttpRequestFactory", e3);
            return null;
         }
      }

      public <T> T getT(HttpClientUtils.Inner4<T> inner4) {
         try {
            CloseableHttpClient closeableHttpClient = HttpClientBuilder.create().build();

            Object obj;
            try {
               this.performAction();
               CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(this.request, this.context);
               obj = inner4.getT(closeableHttpResponse.getEntity());
            } catch (Throwable e) {
               if (closeableHttpClient != null) {
                  try {
                     closeableHttpClient.close();
                  } catch (Throwable e2) {
                     e.addSuppressed(e2);
                  }
               }

               throw e;
            }

            if (closeableHttpClient != null) {
               closeableHttpClient.close();
            }

            return (T)obj;
         } catch (IOException e3) {
            Southside.LOGGER.error("Unexpected error in HttpRequestFactory", e3);
            return null;
         }
      }

      public void performActionEx() {
         this.getT((HttpClientUtils.Inner4<Void>)(httpEntity -> null));
      }

      public HttpEntity getHttpEntity() {
         return this.getT((HttpClientUtils.Inner4<HttpEntity>)(httpEntity -> httpEntity));
      }

      public InputStream getInputStream() {
         return this.getT((HttpClientUtils.Inner4<InputStream>)HttpClientUtils.Inner2::getInputStream);
      }

      public byte[] getByteList() {
         return this.getByteList((int)DEFAULT_LONG);
      }

      public byte[] getByteList(int n) {
         return this.getT((HttpClientUtils.Inner4<byte[]>)(httpEntity -> getByteList(n, httpEntity)));
      }

      public static byte[] getByteList(int n, HttpEntity httpEntity) {
         try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            byte[] bytes = new byte[n];

            int n2;
            while ((n2 = httpEntity.getContent().read(bytes)) > 0) {
               byteArrayOutputStream.write(bytes, 0, n2);
            }

            return byteArrayOutputStream.toByteArray();
         } catch (IOException e) {
            return null;
         }
      }

      private static InputStream getInputStream(HttpEntity httpEntity) {
         try {
            return httpEntity.getContent();
         } catch (IOException e) {
            return null;
         }
      }

      public String getDisplayText() {
         return new String(this.getByteList(), Charset.forName(this.charset));
      }

      public Stream<String> getStream() {
         return Stream.of(this.getDisplayText().split("\\r?\\n"));
      }

      public <T> T getT(Type type) {
         String displayText = this.getDisplayText();
         return (T)(displayText == null ? null : HttpClientUtils.GSON.fromJson(displayText, type));
      }

      public <T> T getT(Function<byte[], T> fn) {
         return (T)fn.apply(this.getByteList());
      }

      public <A, T> T getT(BiFunction<byte[], A, T> biFunction, A val) {
         return (T)biFunction.apply(this.getByteList(), val);
      }

      public <T> T getTEx(Function<String, T> fn) {
         return (T)fn.apply(this.getDisplayText());
      }

      public <A, T> T getTEx(BiFunction<String, A, T> biFunction, A val) {
         return (T)biFunction.apply(this.getDisplayText(), val);
      }

      public <T> T getT(Function<byte[], String> fn, Type type) {
         String text = (String)fn.apply(this.getByteList());
         return (T)(text == null ? null : HttpClientUtils.GSON.fromJson(text, type));
      }

      public <T> T getT(BiFunction<byte[], String, String> biFunction, String text, Type type) {
         String text2 = (String)biFunction.apply(this.getByteList(), text);
         return (T)(text2 == null ? null : HttpClientUtils.GSON.fromJson(text2, type));
      }

      public <T> T getTEx(BiFunction<String, String, String> biFunction, String text, Type type) {
         String text2 = (String)biFunction.apply(this.getDisplayText(), text);
         return (T)(text2 == null ? null : HttpClientUtils.GSON.fromJson(text2, type));
      }

      public <T> T getTEx(Function<String, String> fn, Type type) {
         String text = (String)fn.apply(this.getDisplayText());
         return (T)(text == null ? null : HttpClientUtils.GSON.fromJson(text, type));
      }
   }

   @FunctionalInterface
   public interface Inner3<T> {
      T getT(HttpResponse httpResponse);
   }

   @FunctionalInterface
   public interface Inner4<T> {
      T getT(HttpEntity httpEntity);
   }

   public enum Mode {
      GET,
      POST;


      // $VF: synthetic method
      private static HttpClientUtils.Mode[] createValues() {
         return new HttpClientUtils.Mode[]{GET, POST};
      }


   }
}
