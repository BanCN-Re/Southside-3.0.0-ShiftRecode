package dev.southside.mixin.entity;

import com.mojang.authlib.GameProfile;
import dev.southside.Southside;
import dev.southside.module.impl.render.PlayerAppearance;
import java.util.Locale;
import java.util.function.Supplier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.client.util.SkinTextures.Model;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerListEntry.class)
public abstract class MixinPlayerListEntry {
   @Unique
   private static boolean shouldUseDefaultSkins() {
      if (!MinecraftClient.getInstance().isInSingleplayer() && MinecraftClient.getInstance().world != null) {
         PlayerListHud playerListHud = MinecraftClient.getInstance().inGameHud.getPlayerListHud();
         if (playerListHud == null) {
            return false;
         }

         Text header = playerListHud.header;
         return header != null && header.getString().toLowerCase(Locale.ROOT).contains("布吉岛");
      } else {
         return false;
      }
   }

   @Inject(
      method = "Lnet/minecraft/client/network/PlayerListEntry;getSkinTextures()Lnet/minecraft/client/util/SkinTextures;",
      at = @At("TAIL"),
      cancellable = true
   )
   private void onGetSkinTexturesTail(CallbackInfoReturnable<SkinTextures> cir) {
      if (MinecraftClient.getInstance().player != null) {
         PlayerAppearance playerAppearance = Southside.INSTANCE.getModuleManager().getModule(PlayerAppearance.class);
         if (playerAppearance.isEnabled()) {
            PlayerListEntry playerListEntry = (PlayerListEntry)(Object)this;
            if (playerListEntry.getProfile().getId().equals(MinecraftClient.getInstance().player.getGameProfile().getId())) {
               if (playerAppearance.getCapeTexture() != null) {
                  SkinTextures returnValue = (SkinTextures)cir.getReturnValue();
                  Identifier id = playerAppearance.playerSkin.getValue() ? playerAppearance.getSkinTexture() : returnValue.texture();
                  String text = returnValue.textureUrl();
                  Identifier id2 = playerAppearance.clientCape.getValue() ? playerAppearance.getCapeTexture() : returnValue.capeTexture();
                  Identifier id3 = playerAppearance.clientCape.getValue() ? playerAppearance.getCapeTexture() : returnValue.capeTexture();
                  Model model = playerAppearance.playerSkin.getValue() ? Model.SLIM : returnValue.model();
                  boolean flag = returnValue.secure();
                  cir.setReturnValue(new SkinTextures(id, text, id2, id3, model, flag));
               }
            }
         }
      }
   }

   @Inject(
      method = "Lnet/minecraft/client/network/PlayerListEntry;texturesSupplier(Lcom/mojang/authlib/GameProfile;)Ljava/util/function/Supplier;",
      at = @At("HEAD"),
      cancellable = true
   )
   private static void onTexturesSupplierHead(GameProfile gameProfile, CallbackInfoReturnable<Supplier<SkinTextures>> cir) {
      if (shouldUseDefaultSkins()) {
         SkinTextures skinTextures = DefaultSkinHelper.getSkinTextures(gameProfile);
         cir.setReturnValue((Supplier<SkinTextures>)() -> skinTextures);
         cir.cancel();
      }
   }
}
