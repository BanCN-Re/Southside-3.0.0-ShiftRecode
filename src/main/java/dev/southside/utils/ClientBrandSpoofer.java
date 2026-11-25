package dev.southside.utils;

import dev.southside.Southside;
import dev.southside.module.impl.misc.ClientSpoof;

public final class ClientBrandSpoofer {
   private ClientBrandSpoofer() {
   }

   public static String formatText(String text) {
      return text;
   }

   public static String formatTextEx(String text) {
      ClientSpoof clientSpoof = Southside.INSTANCE.getModuleManager().getModule(ClientSpoof.class);
      return clientSpoof.isEnabled() ? clientSpoof.getClient() : text;
   }
}
