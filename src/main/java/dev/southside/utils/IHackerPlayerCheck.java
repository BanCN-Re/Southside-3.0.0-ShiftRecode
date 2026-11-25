package dev.southside.utils;

import dev.southside.anticheat.player.PlayerData;

public interface IHackerPlayerCheck extends IHackerCheck {
   void checkPlayer(PlayerData data);
}
