package dev.southside.discordipc;

public enum Opcode {
   Handshake,
   Frame,
   Close,
   Ping,
   Pong;

   private static final Opcode[] opcodeArray;

   public static Opcode valueOf(int n) {
      return opcodeArray[n];
   }

   // $VF: synthetic method
   private static Opcode[] getOpcodeArray() {
      return new Opcode[]{Handshake, Frame, Close, Ping, Pong};
   }

   static {
      opcodeArray = values();
   }


}
