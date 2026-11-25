package dev.southside.utils;

import dev.southside.gui.CustomScreen;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.sound.AbstractSoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.random.Random;

public class GuiPlatformImpl implements IGuiPlatform {
   private static final long DEFAULT_LONG = 114514L;

   @Override
   public Object getScreen() {
      return CustomScreen.getScreen();
   }

   @Override
   public void performAction(Object obj) {
      CustomScreen.openScreen((Screen)obj);
   }

   @Override
   public boolean isChatScreen() {
      return this.getScreen() instanceof ChatScreen;
   }

   @Override
   public boolean shouldShowDebugHud() {
      return MinecraftAccess.getMc().getDebugHud().shouldShowDebugHud();
   }

   @Override
   public void performAction() {
      GuiPlatformImpl.Inner inner = new GuiPlatformImpl.Inner(SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON, SoundCategory.PLAYERS, Random.create(DEFAULT_LONG));
      MinecraftAccess.getMc().getSoundManager().play(inner);
   }

   private static class Inner extends AbstractSoundInstance {
      public Inner(SoundEvent soundEvent, SoundCategory soundCategory, Random random) {
         super(soundEvent, soundCategory, random);
      }
   }
}
