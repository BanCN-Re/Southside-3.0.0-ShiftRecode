package dev.southside.utils;

import java.util.OptionalDouble;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.RenderLayer.MultiPhaseParameters;
import net.minecraft.client.render.RenderPhase.LineWidth;
import net.minecraft.client.render.VertexFormat.DrawMode;

public final class MenuRenderLayers {
   public static final RenderLayer MENU_BLOCKS;
   public static final RenderLayer MENU_BLOCK_OUTLINE;
   public static final RenderLayer MENU_BLOCK_PARTICLES;
   public static final RenderLayer MENU_SHADOW;

   private MenuRenderLayers() {
   }

   // $VF: Irreducible bytecode was duplicated to produce valid code
   static {
      MENU_BLOCKS = RenderLayer.of(
         "southside_menu_blocks",
         VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL,
         DrawMode.QUADS,
         262144,
         false,
         false,
         MultiPhaseParameters.builder()
            .program(RenderPhase.ENTITY_CUTOUT_NONULL_PROGRAM)
            .texture(RenderPhase.MIPMAP_BLOCK_ATLAS_TEXTURE)
            .lightmap(RenderPhase.ENABLE_LIGHTMAP)
            .overlay(RenderPhase.ENABLE_OVERLAY_COLOR)
            .cull(RenderPhase.DISABLE_CULLING)
            .depthTest(RenderPhase.LEQUAL_DEPTH_TEST)
            .writeMaskState(RenderPhase.ALL_MASK)
            .target(RenderPhase.MAIN_TARGET)
            .build(false)
      );
      MENU_BLOCK_OUTLINE = RenderLayer.of(
         "southside_menu_block_outline",
         VertexFormats.LINES,
         DrawMode.LINES,
         1536,
         false,
         false,
         MultiPhaseParameters.builder()
            .program(RenderPhase.LINES_PROGRAM)
            .lineWidth(new LineWidth(OptionalDouble.empty()))
            .layering(RenderPhase.NO_LAYERING)
            .transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
            .cull(RenderPhase.DISABLE_CULLING)
            .depthTest(RenderPhase.LEQUAL_DEPTH_TEST)
            .writeMaskState(RenderPhase.COLOR_MASK)
            .target(RenderPhase.MAIN_TARGET)
            .build(false)
      );
      MENU_BLOCK_PARTICLES = RenderLayer.of(
         "southside_menu_block_particles",
         VertexFormats.POSITION_TEXTURE_COLOR_LIGHT,
         DrawMode.QUADS,
         65536,
         false,
         true,
         MultiPhaseParameters.builder()
            .program(RenderPhase.PARTICLE)
            .texture(RenderPhase.BLOCK_ATLAS_TEXTURE)
            .lightmap(RenderPhase.ENABLE_LIGHTMAP)
            .transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
            .cull(RenderPhase.DISABLE_CULLING)
            .depthTest(RenderPhase.LEQUAL_DEPTH_TEST)
            .writeMaskState(RenderPhase.ALL_MASK)
            .target(RenderPhase.MAIN_TARGET)
            .build(false)
      );
      MENU_SHADOW = RenderLayer.of(
         "southside_menu_shadow",
         VertexFormats.POSITION_COLOR,
         DrawMode.QUADS,
         4096,
         false,
         false,
         MultiPhaseParameters.builder()
            .program(RenderPhase.POSITION_COLOR_PROGRAM)
            .transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
            .cull(RenderPhase.DISABLE_CULLING)
            .depthTest(RenderPhase.LEQUAL_DEPTH_TEST)
            .writeMaskState(RenderPhase.COLOR_MASK)
            .target(RenderPhase.MAIN_TARGET)
            .build(false)
      );
   }
}
