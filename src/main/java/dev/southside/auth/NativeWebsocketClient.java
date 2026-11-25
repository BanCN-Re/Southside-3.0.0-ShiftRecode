package dev.southside.auth;

import com.google.gson.annotations.SerializedName;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.net.http.WebSocket.Listener;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import net.minecraft.client.MinecraftClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class NativeWebsocketClient extends IrcClient {
   private static final Logger LOGGER = LogManager.getLogger(NativeWebsocketClient.class);
   private static final int DEFAULT_INT = 3;
   private static final int DEFAULT_INT_VALUE = 5;
   private static final int DEFAULT_INT_DATA = 30;
   @SerializedName("windowOpen")
   private final AtomicBoolean windowOpen = new AtomicBoolean();
   @SerializedName("publicHttp")
   private final HttpClient publicHttp = HttpClient.newHttpClient();
   @SerializedName("privateHttp")
   private HttpClient privateHttp;
   @SerializedName("maintenance")
   private final ScheduledExecutorService maintenance = Executors.newSingleThreadScheduledExecutor(action -> {
      Thread thread = new Thread(action, "Southside IRC connection");
      thread.setDaemon(true);
      return thread;
   });
   @SerializedName("receiveLock")
   public final Object receiveLock = new Object();
   @SerializedName("transportLock")
   public final Object transportLock = new Object();
   @SerializedName("nativeHandle")
   public volatile long nativeHandle = this.create0();
   @SerializedName("socket")
   public volatile WebSocket socket;
   @SerializedName("activeListener")
   public NativeWebsocketClient.ListenerImpl activeListener;
   @SerializedName("pendingSend")
   public CompletableFuture<WebSocket> pendingSend = CompletableFuture.completedFuture(null);
   @SerializedName("transportState")
   public volatile TransportState transportState = TransportState.DISCONNECTED;
   @SerializedName("keepConnected")
   private volatile boolean keepConnected;
   @SerializedName("lastReceived")
   public long lastReceived;
   @SerializedName("reconnecting")
   private boolean reconnecting;
   @SerializedName("retryAt")
   private long retryAt;
   @SerializedName("retrySeconds")
   private int retrySeconds = 3;

   private native long create0();

   private native boolean showLogin0(long l);

   private native boolean reconnect0(long l);

   public native void onOpen0(long l);

   public native void receive0(long l, String text);

   private native void disconnected0(long l);

   private native boolean authenticated0(long l);

   private native void release0(long l);

   private native boolean chat0(long l, byte[] bytes);

   private native boolean presence0(long l, String text, boolean flag);

   public static boolean isValidAlt() {
      NativeWebsocketClient nativeWebsocketClient = new NativeWebsocketClient();
      IrcClient.performAction(nativeWebsocketClient);
      return nativeWebsocketClient.isValidImpl();
   }

   private boolean isValidImpl() {
      if (!this.windowOpen.compareAndSet(false, true)) {
         throw new IllegalStateException("Login window is already open");
      }

      try {
         if (this.keepConnected) {
            throw new IllegalStateException("IRC connection is already managed");
         }

         long nativeHandle2 = this.nativeHandle;
         if (nativeHandle2 == 0L) {
            throw new IllegalStateException("Native login connection is closed");
         }

         if (!this.showLogin0(nativeHandle2)) {
            return false;
         }

         synchronized (this.receiveLock) {
            if (this.nativeHandle != nativeHandle2) {
               return false;
            }

            this.keepConnected = true;
            this.lastReceived = System.nanoTime();
            this.maintenance.scheduleWithFixedDelay(this::performActionEx, 3L, 3L, TimeUnit.SECONDS);
            this.maintenance.scheduleWithFixedDelay(this::performActionAlt, 3L, 1L, TimeUnit.SECONDS);
         }

         return true;
      } finally {
         this.windowOpen.set(false);
      }
   }

   private void connectTransport(String text) {
      URI uri = URI.create(text);
      if ("wss".equals(uri.getScheme()) && uri.getHost() != null) {
         WebSocket webSocket;
         NativeWebsocketClient.ListenerImpl listenerImpl;
         synchronized (this.receiveLock) {
            synchronized (this.transportLock) {
               if (this.nativeHandle == 0L) {
                  throw new IllegalStateException("Native login connection is closed");
               }

               listenerImpl = new NativeWebsocketClient.ListenerImpl(this.nativeHandle);
               this.activeListener = listenerImpl;
               webSocket = this.socket;
               this.socket = null;
               this.transportState = TransportState.CONNECTING;
            }
         }

         if (webSocket != null) {
            webSocket.abort();
         }

         try {
            this.getHttpClient(uri).newWebSocketBuilder().connectTimeout(Duration.ofSeconds(10L)).buildAsync(uri, listenerImpl).join();
         } catch (RuntimeException e) {
            synchronized (this.transportLock) {
               if (this.activeListener == listenerImpl && this.socket == null) {
                  this.activeListener = null;
                  this.transportState = TransportState.DISCONNECTED;
               }
            }

            throw e;
         }
      } else {
         throw new IllegalArgumentException("Southside requires a WSS endpoint");
      }
   }

   @Override
   public TransportState getTransportState() {
      long nativeHandle2 = this.nativeHandle;
      if (nativeHandle2 != 0L && this.transportState == TransportState.CONNECTED && this.authenticated0(nativeHandle2)) {
         return TransportState.CONNECTED;
      } else {
         return this.transportState == TransportState.CONNECTED ? TransportState.CONNECTING : this.transportState;
      }
   }

   @Override
   public CompletableFuture<Void> getCompletableFuture() {
      if (this.isValid()) {
         return CompletableFuture.completedFuture(null);
      } else {
         return this.keepConnected
            ? CompletableFuture.failedFuture(new IllegalStateException("IRC is reconnecting in the background"))
            : CompletableFuture.runAsync(() -> {
               if (!this.isValidImpl()) {
                  throw new IllegalStateException("Native login was closed without authentication");
               }
            });
      }
   }

   @Override
   public void performAction() {
      synchronized (this.receiveLock) {
         long l;
         WebSocket webSocket;
         synchronized (this.transportLock) {
            l = this.nativeHandle;
            this.nativeHandle = 0L;
            this.keepConnected = false;
            this.transportState = TransportState.DISCONNECTED;
            webSocket = this.socket;
            this.socket = null;
            this.activeListener = null;
            this.pendingSend = CompletableFuture.completedFuture(null);
         }

         this.maintenance.shutdownNow();
         if (webSocket != null) {
            webSocket.abort();
         }

         this.performActionImpl();
         if (l != 0L) {
            this.release0(l);
         }

         IrcState.clear();
      }
   }

   private void performActionEx() {
      WebSocket socket2 = this.socket;
      if (socket2 != null && !socket2.isOutputClosed() && !socket2.isInputClosed()) {
         socket2.sendPing(ByteBuffer.allocate(0)).exceptionally(t -> null);
      }
   }

   private void performActionAlt() {
      long l;
      synchronized (this.receiveLock) {
         l = this.nativeHandle;
         if (l == 0L) {
            return;
         }

         WebSocket socket2 = this.socket;
         if (socket2 != null) {
            boolean flag = System.nanoTime() - this.lastReceived > TimeUnit.SECONDS.toNanos(5L);
            if (!socket2.isInputClosed() && !socket2.isOutputClosed() && !flag) {
               return;
            }

            this.isValid(socket2);
         }
      }

      if (!this.reconnecting || System.nanoTime() - this.retryAt >= 0L) {
         if (!this.reconnecting) {
            this.reconnecting = true;
            LOGGER.info("IRC disconnected; reconnecting.");
         }

         boolean flag2 = false;

         try {
            flag2 = this.reconnect0(l);
         } catch (RuntimeException e) {
            LOGGER.warn("IRC reconnect failed: {}", e.getMessage());
         }

         synchronized (this.receiveLock) {
            if (this.nativeHandle != l) {
               return;
            }

            WebSocket socket3 = this.socket;
            if (flag2 && socket3 != null && !socket3.isInputClosed() && !socket3.isOutputClosed()) {
               this.reconnecting = false;
               this.retrySeconds = 3;
               LOGGER.info("IRC reconnected and authenticated.");
               MinecraftClient instance = MinecraftClient.getInstance();
               if (instance != null) {
                  instance.execute(IrcState::performAction);
               }

               return;
            }

            if (socket3 != null) {
               this.isValid(socket3);
            }
         }

         this.retryAt = System.nanoTime() + TimeUnit.SECONDS.toNanos(this.retrySeconds);
         this.retrySeconds = Math.min(30, this.retrySeconds * 2);
      }
   }

   public boolean isValid(WebSocket webSocket) {
      synchronized (this.receiveLock) {
         long l;
         synchronized (this.transportLock) {
            l = this.nativeHandle;
            if (l == 0L || this.socket != webSocket) {
               return false;
            }

            this.socket = null;
            this.activeListener = null;
            this.transportState = TransportState.DISCONNECTED;
            this.pendingSend = CompletableFuture.completedFuture(null);
         }

         webSocket.abort();
         this.disconnected0(l);
         IrcState.clear();
         return true;
      }
   }

   @Override
   public boolean sendServerMessage(String text) {
      synchronized (this.transportLock) {
         WebSocket socket2 = this.socket;
         if (text != null && socket2 != null && !socket2.isOutputClosed() && !this.pendingSend.isCompletedExceptionally()) {
            this.pendingSend = this.pendingSend.thenCompose(webSocket -> socket2.sendText(text, true));
            return true;
         } else {
            return false;
         }
      }
   }

   @Override
   public boolean matches(String text) {
      return this.isValid() && text != null && this.chat0(this.nativeHandle, text.getBytes(StandardCharsets.UTF_8));
   }

   @Override
   public boolean isValid(String text, boolean flag) {
      return this.isValid() && text != null && this.presence0(this.nativeHandle, text, flag);
   }

   @Override
   public boolean isValidEx() {
      return this.isValid("", false);
   }

   private void receiveApplicationPacket(int n, byte[] bytes) {
      String text = new String(bytes, StandardCharsets.UTF_8);
      switch (n) {
         case 20:
            IrcState.offer(text);
            break;
         case 21:
            IrcState.applyText(text);
            break;
         case 22:
            IrcState.applyTextEx(text);
      }
   }

   private synchronized HttpClient getHttpClient(URI uri) {
      String host = uri.getHost();
      if (host.indexOf(58) < 0 && !host.matches("[0-9.]+")) {
         return this.publicHttp;
      }

      if (this.privateHttp == null) {
         this.privateHttp = getHttpClient();
      }

      return this.privateHttp;
   }

   private synchronized void performActionImpl() {
      this.publicHttp.shutdownNow();
      if (this.privateHttp != null) {
         this.privateHttp.shutdownNow();
      }
   }

   private static HttpClient getHttpClient() {
      try (InputStream resourceAsStream = NativeWebsocketClient.class.getResourceAsStream("/auth-ca.pem")) {
         if (resourceAsStream == null) {
            throw new IOException("Missing Southside authentication CA");
         }

         KeyStore instance = KeyStore.getInstance("PKCS12");
         instance.load(null, null);
         instance.setCertificateEntry("southside-auth", CertificateFactory.getInstance("X.509").generateCertificate(resourceAsStream));
         short s = -22775;
         TrustManagerFactory instance2 = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
         short s2 = 26223;
         instance2.init(instance);
         SSLContext instance3 = SSLContext.getInstance("TLS");
         instance3.init(null, instance2.getTrustManagers(), null);
         return HttpClient.newBuilder().sslContext(instance3).build();
      } catch (IOException | GeneralSecurityException e) {
         throw new IllegalStateException("Could not load the Southside authentication CA", e);
      }
   }

   private static void performActionDirect() {
      String text = "/native/windows-x86_64/southside_auth.dll";

      try (InputStream resourceAsStream = NativeWebsocketClient.class.getResourceAsStream(text)) {
         if (resourceAsStream == null) {
            throw new IOException("Missing bundled native login DLL: " + text);
         }

         Path path = Files.createTempDirectory("southside-native-");
         Path path2 = path.resolve("southside_auth.dll");
         path.toFile().deleteOnExit();
         path2.toFile().deleteOnExit();
         Files.copy(resourceAsStream, path2, StandardCopyOption.REPLACE_EXISTING);
         System.load(path2.toAbsolutePath().toString());
      } catch (IOException e) {
         throw new ExceptionInInitializerError(e);
      }
   }

   static {
      performActionDirect();
   }

   private final class ListenerImpl implements Listener {
      @SerializedName("handle")
      private final long handle;
      @SerializedName("fragments")
      private final StringBuilder fragments = new StringBuilder();

      public ListenerImpl(long l) {
         this.handle = l;
      }

      @Override
      public void onOpen(WebSocket webSocket) {
         synchronized (NativeWebsocketClient.this.receiveLock) {
            synchronized (NativeWebsocketClient.this.transportLock) {
               if (NativeWebsocketClient.this.nativeHandle != this.handle || NativeWebsocketClient.this.activeListener != this) {
                  webSocket.abort();
                  return;
               }

               NativeWebsocketClient.this.lastReceived = System.nanoTime();
               NativeWebsocketClient.this.socket = webSocket;
               NativeWebsocketClient.this.pendingSend = CompletableFuture.completedFuture(webSocket);
               NativeWebsocketClient.this.transportState = TransportState.CONNECTED;
            }

            NativeWebsocketClient.this.onOpen0(this.handle);
            webSocket.request(1L);
         }
      }

      @Override
      public CompletionStage<?> onText(WebSocket webSocket, CharSequence charSequence, boolean flag) {
         synchronized (NativeWebsocketClient.this.receiveLock) {
            if (NativeWebsocketClient.this.socket != webSocket) {
               return null;
            }

            NativeWebsocketClient.this.lastReceived = System.nanoTime();
            this.fragments.append(charSequence);
            if (flag) {
               NativeWebsocketClient.this.receive0(this.handle, this.fragments.toString());
               this.fragments.setLength(0);
            }

            webSocket.request(1L);
            return null;
         }
      }

      @Override
      public CompletionStage<?> onPong(WebSocket webSocket, ByteBuffer buffer) {
         synchronized (NativeWebsocketClient.this.receiveLock) {
            if (NativeWebsocketClient.this.socket != webSocket) {
               return null;
            }

            NativeWebsocketClient.this.lastReceived = System.nanoTime();
            webSocket.request(1L);
            return null;
         }
      }

      @Override
      public void onError(WebSocket webSocket, Throwable t) {
         if (NativeWebsocketClient.this.isValid(webSocket)) {
            System.err.println("连接错误：" + t.getMessage());
         }
      }

      @Override
      public CompletionStage<?> onClose(WebSocket webSocket, int n, String text) {
         NativeWebsocketClient.this.isValid(webSocket);
         return null;
      }
   }
}
