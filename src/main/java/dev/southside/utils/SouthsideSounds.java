package dev.southside.utils;

import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public final class SouthsideSounds {
   public static final Identifier SIGMA_ACTIVATE_ID;
   public static final Identifier SIGMA_DEACTIVATE_ID;
   public static final Identifier TOGGLE_ON_ID;
   public static final Identifier TOGGLE_OFF_ID;
   public static final Identifier PIANO_ON_ID;
   public static final Identifier PIANO_OFF_ID;
   public static final SoundEvent SIGMA_ACTIVATE;
   public static final SoundEvent SIGMA_DEACTIVATE;
   public static final SoundEvent TOGGLE_ON;
   public static final SoundEvent TOGGLE_OFF;
   public static final SoundEvent PIANO_ON;
   public static final SoundEvent PIANO_OFF;

   private SouthsideSounds() {
   }

   // $VF: Irreducible bytecode was duplicated to produce valid code
   static {
      SIGMA_ACTIVATE_ID = Identifier.of("southside:sactivate");
      SIGMA_DEACTIVATE_ID = Identifier.of("southside:sdeactivate");
      TOGGLE_ON_ID = Identifier.of("southside", "toggleon");
      TOGGLE_OFF_ID = Identifier.of("southside", "toggleoff");
      PIANO_ON_ID = Identifier.of("southside", "toggleon2");
      PIANO_OFF_ID = Identifier.of("southside", "toggleoff2");
      SIGMA_ACTIVATE = SoundEvent.of(SIGMA_ACTIVATE_ID);
      SIGMA_DEACTIVATE = SoundEvent.of(SIGMA_DEACTIVATE_ID);
      TOGGLE_ON = SoundEvent.of(TOGGLE_ON_ID);
      TOGGLE_OFF = SoundEvent.of(TOGGLE_OFF_ID);
      PIANO_ON = SoundEvent.of(PIANO_ON_ID);
      PIANO_OFF = SoundEvent.of(PIANO_OFF_ID);
   }
}
