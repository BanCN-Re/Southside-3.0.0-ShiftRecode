package dev.southside.utils;

import com.google.gson.annotations.SerializedName;
import com.mojang.authlib.GameProfile;
import dev.southside.Southside;
import dev.southside.utils.SkyblockIslandTemplate.Entry;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.ServerStopped;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.Join;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.datafixer.Schemas;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.registry.entry.RegistryEntry.Reference;
import net.minecraft.resource.DataConfiguration;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServerLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.dimension.DimensionOptionsRegistryHolder;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.gen.WorldPreset;
import net.minecraft.world.gen.WorldPresets;
import net.minecraft.world.gen.chunk.FlatChunkGenerator;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorConfig;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.storage.LevelStorage;

public final class SkyblockPreviewWorld {
   private static final String DEFAULT_TEXT = "southside-island";
   private static final int DEFAULT_INT = 65;
   private static final double DEFAULT_DOUBLE = 65.0;
   private static final double DEFAULT_DOUBLE_VALUE = 0.5;
   private static final double DEFAULT_DOUBLE_DATA = 64.25;
   private static final double DEFAULT_DOUBLE_REF = 1.1;
   private static final double DEFAULT_DOUBLE_PRIMARY = 0.5;
   private static final double DEFAULT_DOUBLE_SECONDARY = -2.5;
   private static SkyblockPreviewWorld INSTANCE;
   private static SkyblockPreviewWorld INSTANCE2;
   @SerializedName("tempDirectory")
   private final Path tempDirectory;
   @SerializedName("joinedClientWorld")
   private boolean joinedClientWorld;
   @SerializedName("visitorSpawned")
   private boolean visitorSpawned;
   @SerializedName("visitor")
   private IslandVisitorEntity visitor;
   private static final long e = 200L;

   private SkyblockPreviewWorld(Path path) {
      this.tempDirectory = path;
   }

   public static boolean isValid() {
      return INSTANCE != null;
   }

   public static void openScreen(Screen screen) {
      MinecraftClient instance = MinecraftClient.getInstance();
      if (INSTANCE == null && INSTANCE2 == null && instance.world == null) {
         try {
            Path path = Files.createTempDirectory("southside-island-");
            INSTANCE = new SkyblockPreviewWorld(path);
            Southside.LOGGER.info("Opening the menu island world in {}", path);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> performAction(path), "Southside-Island-Cleanup"));
            LevelStorage levelStorage = new LevelStorage(
               path, path.resolve("backups"), LevelStorage.createSymlinkFinder(path.resolve("allowed_symlinks.txt")), Schemas.getFixer()
            );
            IntegratedServerLoader integratedServerLoader = new IntegratedServerLoader(instance, levelStorage);
            LevelInfo levelInfo = new LevelInfo(
               "southside-island", GameMode.CREATIVE, false, Difficulty.NORMAL, true, new GameRules(FeatureSet.empty()), DataConfiguration.SAFE_MODE
            );
            GeneratorOptions generatorOptions = new GeneratorOptions(0L, false, false);
            integratedServerLoader.createAndStart("southside-island", levelInfo, generatorOptions, SkyblockPreviewWorld::getDimensionOptionsRegistryHolder, screen);
         } catch (Throwable e2) {
            SkyblockPreviewWorld skyblockPreviewWorld = INSTANCE;
            INSTANCE = null;
            Southside.LOGGER.error("Could not open the Southside island world", e2);
            if (skyblockPreviewWorld != null) {
               skyblockPreviewWorld.performActionAlt();
            }
         }
      }
   }

   private static DimensionOptionsRegistryHolder getDimensionOptionsRegistryHolder(WrapperLookup wrapperLookup) {
      Reference orThrow = wrapperLookup.getOrThrow(RegistryKeys.WORLD_PRESET).getOrThrow(WorldPresets.FLAT);
      FlatChunkGeneratorConfig flatChunkGeneratorConfig = new FlatChunkGeneratorConfig(
         Optional.empty(), FlatChunkGeneratorConfig.getPlains(wrapperLookup.getOrThrow(RegistryKeys.BIOME)), List.of()
      );
      return ((WorldPreset)orThrow.value()).createDimensionsRegistryHolder().with(wrapperLookup, new FlatChunkGenerator(flatChunkGeneratorConfig));
   }

   private static void performAction(MinecraftServer server) {
      SkyblockPreviewWorld skyblockPreviewWorld = INSTANCE;
      if (skyblockPreviewWorld != null) {
         INSTANCE = null;
         INSTANCE2 = skyblockPreviewWorld;
         if (server instanceof IIntegratedServerExt iintegratedserverext) {
            iintegratedserverext.southside$setSkipSaveOnShutdown(true);
         }

         ServerWorld overworld = server.getOverworld();
         performAction(overworld);
         overworld.setSpawnPos(new BlockPos((int)Math.floor(0.5), 65, (int)Math.floor(-2.5)), 0.0F);
         Southside.LOGGER
            .info("Built the menu island at y={} ({} blocks)", 66, SkyblockIslandTemplate.getEntries().size() + SkyblockIslandTemplate.getEntriesValue().size());
      }
   }

   private static void performAction(ServerWorld world) {
      for (Entry entry : SkyblockIslandTemplate.getEntries()) {
         performAction(world, entry);
      }

      for (Entry entry2 : SkyblockIslandTemplate.getEntriesValue()) {
         performAction(world, entry2);
      }
   }

   private static void performAction(ServerWorld world, Entry entry) {
      BlockState state = entry.getState();
      if (state.contains(LeavesBlock.PERSISTENT)) {
         state = (BlockState)state.with(LeavesBlock.PERSISTENT, true);
      }

      world.setBlockState(entry.getPos().add(0, 65, 0), state, 3);
   }

   private static void performAction(ServerPlayerEntity player, MinecraftServer server) {
      if (INSTANCE2 != null) {
         ServerWorld overworld = server.getOverworld();
         if (player.getWorld() == overworld) {
            player.teleport(overworld, 0.5, 65.0, -2.5, Set.of(), 0.0F, 22.0F, false);
            Southside.LOGGER.info("Moved {} onto the menu island", player.getName().getString());
         }
      }
   }

   private static void performAction(MinecraftClient client) {
      SkyblockPreviewWorld skyblockPreviewWorld = INSTANCE2;
      if (skyblockPreviewWorld != null) {
         if (client.world != null && client.player != null) {
            skyblockPreviewWorld.joinedClientWorld = true;
            if (!skyblockPreviewWorld.visitorSpawned) {
               skyblockPreviewWorld.performActionEx(client);
            }
         } else {
            if (skyblockPreviewWorld.joinedClientWorld) {
               performAction();
            }
         }
      }
   }

   private void performActionEx(MinecraftClient client) {
      GameProfile gameProfile = new GameProfile(client.player.getUuid(), client.player.getGameProfile().getName());
      IslandVisitorEntity islandVisitorEntity = new IslandVisitorEntity(client.world, gameProfile, 0.5, 64.25, 1.1, 0.0F);
      client.world.addEntity(islandVisitorEntity);
      if (client.world.getEntityById(islandVisitorEntity.getId()) == null) {
         Southside.LOGGER.warn("The client world refused the menu visitor entity");
      } else {
         this.visitor = islandVisitorEntity;
         this.visitorSpawned = true;
         Southside.LOGGER.info("Seated the menu visitor at {}, {}, {}", 0.5, 64.25, 1.1);
      }
   }

   private static void performAction() {
      SkyblockPreviewWorld skyblockPreviewWorld = INSTANCE2;
      if (skyblockPreviewWorld != null) {
         if (skyblockPreviewWorld.visitor != null) {
            skyblockPreviewWorld.visitor.performActionImpl();
            skyblockPreviewWorld.visitor = null;
         }

         skyblockPreviewWorld.joinedClientWorld = false;
         skyblockPreviewWorld.visitorSpawned = false;
         INSTANCE2 = null;
         INSTANCE = null;
         skyblockPreviewWorld.performActionAlt();
      }
   }

   private static void performActionEx() {
      SkyblockPreviewWorld skyblockPreviewWorld = INSTANCE2;
      SkyblockPreviewWorld skyblockPreviewWorld2 = INSTANCE;
      INSTANCE2 = null;
      INSTANCE = null;
      if (skyblockPreviewWorld != null) {
         skyblockPreviewWorld.joinedClientWorld = false;
         skyblockPreviewWorld.visitor = null;
         skyblockPreviewWorld.visitorSpawned = false;
         skyblockPreviewWorld.performActionAlt();
      }

      if (skyblockPreviewWorld2 != null) {
         skyblockPreviewWorld2.performActionAlt();
      }
   }

   private void performActionAlt() {
      Path tempDirectory2 = this.tempDirectory;
      Thread thread = new Thread(() -> {
         performAction(tempDirectory2);
         if (!Files.exists(tempDirectory2)) {
            Southside.LOGGER.info("Removed temporary island world at {}", tempDirectory2);
         }
      }, "Southside-Island-Cleanup");
      thread.setDaemon(true);
      thread.start();
   }

   private static void performAction(Path path) {
      for (int i = 0; i < 6; i++) {
         if (!Files.exists(path)) {
            return;
         }

         try (Stream<Path> stream = Files.walk(path)) {
            stream.sorted(Comparator.reverseOrder()).forEach(path2 -> {
               try {
                  Files.deleteIfExists(path2);
               } catch (IOException e2) {
               }
            });
         } catch (IOException e3) {
         }

         if (!Files.exists(path)) {
            return;
         }

         try {
            Thread.sleep(e);
         } catch (InterruptedException e4) {
            Thread.currentThread().interrupt();
            return;
         }
      }

      Southside.LOGGER.warn("Could not fully remove the temporary island world at {}", path);
   }

   static {
      ServerLifecycleEvents.SERVER_STARTED.register(SkyblockPreviewWorld::performAction);
      ServerLifecycleEvents.SERVER_STOPPED.register((ServerStopped)server -> performActionEx());
      ServerPlayConnectionEvents.JOIN.register((Join)(serverPlayNetworkHandler, packetSender, server2) -> performAction(serverPlayNetworkHandler.getPlayer(), server2));
      ClientTickEvents.END_CLIENT_TICK.register(SkyblockPreviewWorld::performAction);
   }
}
