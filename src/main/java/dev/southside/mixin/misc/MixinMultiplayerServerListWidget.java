package dev.southside.mixin.misc;

import java.util.concurrent.ThreadPoolExecutor;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MultiplayerServerListWidget.class)
public interface MixinMultiplayerServerListWidget {
   @Accessor("SERVER_PINGER_THREAD_POOL")
   static ThreadPoolExecutor pinger() {
      throw new AssertionError();
   }
}
