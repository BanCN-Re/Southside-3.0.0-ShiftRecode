package dev.southside.event;

public enum TransferOrigin {
   SEND,
   RECEIVE;


   // $VF: synthetic method
   private static TransferOrigin[] getTransferOriginArray() {
      return new TransferOrigin[]{SEND, RECEIVE};
   }


}
