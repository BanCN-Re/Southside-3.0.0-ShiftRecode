package dev.southside.mixin.misc;

import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget.ServerEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ServerEntry.class)
public interface MixinMultiplayerServerListWidget_ServerEntry {
   @Invoker("update")
   void updateStatus();

   @Invoker("method_20138")
   void ping();
}
