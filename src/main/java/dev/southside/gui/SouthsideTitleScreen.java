package dev.southside.gui;

import com.google.gson.annotations.SerializedName;
import dev.southside.utils.IASCompat;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerWarningScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class SouthsideTitleScreen extends Screen {
   @SerializedName("contentTop")
   private int contentTop;

   public SouthsideTitleScreen() {
      super(Text.literal("Southside"));
   }

   protected void init() {
      this.contentTop = Math.max(20, (this.height - 285) / 2);
      int n = this.contentTop + 66;
      byte b = 34;
      int n2 = this.width - b * 2;
      this.addDrawableChild(
         ButtonWidget.builder(Text.translatable("menu.singleplayer"), buttonWidget -> this.client.setScreen(new SelectWorldScreen(this)))
            .dimensions(b, n, n2, 32)
            .build()
      );
      this.addDrawableChild(
         ButtonWidget.builder(
               Text.translatable("menu.multiplayer"),
               buttonWidget2 -> this.client
                  .setScreen((Screen)(this.client.options.skipMultiplayerWarning ? new MultiplayerScreen(this) : new MultiplayerWarningScreen(this)))
            )
            .dimensions(b, n + 40, n2, 32)
            .build()
      );
      this.addDrawableChild(
         ButtonWidget.builder(Text.translatable("menu.options"), buttonWidget3 -> this.client.setScreen(new OptionsScreen(this, this.client.options)))
            .dimensions(b, n + 80, n2, 32)
            .build()
      );
      int n3 = n + 120;
      if (FabricLoader.getInstance().isModLoaded("ias")) {
         this.addDrawableChild(
            ButtonWidget.builder(Text.literal("账号管理"), buttonWidget4 -> this.client.setScreen(IASCompat.getScreen(this))).dimensions(b, n3, n2, 32).build()
         );
         n3 += 40;
      }

      this.addDrawableChild(ButtonWidget.builder(Text.translatable("menu.quit"), buttonWidget5 -> this.client.scheduleStop()).dimensions(b, n3, n2, 32).build());
   }

   public boolean shouldCloseOnEsc() {
      return false;
   }
}
