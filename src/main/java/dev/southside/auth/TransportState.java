package dev.southside.auth;

public enum TransportState {
   UNAVAILABLE,
   DISCONNECTED,
   CONNECTING,
   CONNECTED;


   // $VF: synthetic method
   private static TransportState[] getTransportStateArray() {
      return new TransportState[]{UNAVAILABLE, DISCONNECTED, CONNECTING, CONNECTED};
   }


}
