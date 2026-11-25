package dev.southside.utils;

import com.google.gson.annotations.SerializedName;
import com.mojang.authlib.GameProfile;
import dev.southside.Southside;
import dev.southside.module.impl.render.PlayerAppearance;
import java.util.function.Supplier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.Session;
import net.minecraft.client.session.Session.AccountType;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.util.SkinTextures;

public final class PlayerSkinProfile {
   @SerializedName("profile")
   private GameProfile profile;
   @SerializedName("session")
   private Session session;
   @SerializedName("accountSkin")
   private Supplier<SkinTextures> accountSkin;

   public SkinTextures getSkinTextures() {
      MinecraftClient instance = MinecraftClient.getInstance();
      Session session2 = instance.getSession();
      GameProfile gameProfile = instance.getGameProfile();
      if (this.session != session2 || !gameProfile.equals(this.profile)) {
         this.session = session2;
         this.profile = gameProfile;
         this.accountSkin = session2.getAccountType() == AccountType.LEGACY
            ? () -> DefaultSkinHelper.getSkinTextures(gameProfile)
            : instance.getSkinProvider().getSkinTexturesSupplier(gameProfile);
      }

      SkinTextures skinTextures = this.accountSkin.get();
      if (session2.getAccountType() != AccountType.LEGACY) {
         return skinTextures;
      }

      PlayerAppearance playerAppearance = Southside.INSTANCE.getModuleManager().getModule(PlayerAppearance.class);
      return playerAppearance == null ? skinTextures : new SkinTextures(playerAppearance.getId(), null, null, null, skinTextures.model(), false);
   }
}
