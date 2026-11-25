package dev.southside.auth;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public abstract class IrcClient {
   private static volatile IrcClient INSTANCE = new IrcClient.Inner();

   public static IrcClient getINSTANCE() {
      return INSTANCE;
   }

   public static void performAction(IrcClient other) {
      Objects.requireNonNull(other, "replacement");
      if (INSTANCE != other) {
         INSTANCE.performAction();
         IrcState.clear();
         INSTANCE = other;
      }
   }

   public abstract TransportState getTransportState();

   public final boolean isValid() {
      return this.getTransportState() == TransportState.CONNECTED;
   }

   public abstract CompletableFuture<Void> getCompletableFuture();

   public abstract void performAction();

   public abstract boolean sendServerMessage(String text);

   public abstract boolean matches(String text);

   public abstract boolean isValid(String text, boolean flag);

   public abstract boolean isValidEx();

   private static final class Inner extends IrcClient {
      public Inner() {
      }

      @Override
      public TransportState getTransportState() {
         return TransportState.UNAVAILABLE;
      }

      @Override
      public CompletableFuture<Void> getCompletableFuture() {
         return CompletableFuture.failedFuture(new UnsupportedOperationException("No WebSocket backend is configured."));
      }

      @Override
      public void performAction() {
      }

      @Override
      public boolean sendServerMessage(String text) {
         return false;
      }

      @Override
      public boolean matches(String text) {
         return false;
      }

      @Override
      public boolean isValid(String text, boolean flag) {
         return false;
      }

      @Override
      public boolean isValidEx() {
         return false;
      }
   }
}
