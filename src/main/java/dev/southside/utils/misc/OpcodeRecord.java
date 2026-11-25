package dev.southside.utils.misc;

import com.google.gson.JsonObject;
import dev.southside.discordipc.Opcode;

public record OpcodeRecord(Opcode opcode, JsonObject data) {
   public Opcode getOpcode() {
      return this.opcode;
   }

   public JsonObject getData() {
      return this.data;
   }
}
