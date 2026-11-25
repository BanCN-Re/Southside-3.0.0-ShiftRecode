package dev.southside.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.southside.Southside;
import dev.southside.command.Command;
import dev.southside.command.args.StickerArgumentType;
import dev.southside.manager.StickerManager;
import java.util.List;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;

public class StickersCommand extends Command {
   private int computeInt(CommandContext<CommandSource> ctx) {
      Southside.INSTANCE.getStickerManager().updateAlt();
      this.applyText("§7Sticker disabled");
      return 1;
   }

   private int computeIntEx(CommandContext<CommandSource> ctx) {
      StickerManager stickerManager = Southside.INSTANCE.getStickerManager();
      List list = stickerManager.collectItems();
      if (list.isEmpty()) {
         this.applyText("§cNo stickers found. Place .png/.gif files in Southside-nextgen/stickers/");
      } else {
         this.applyText("§eAvailable stickers (" + list.size() + "):");
         String selectedSticker = stickerManager.getSelectedSticker();

         for (Object obj : list) {
            String text = (String)obj;
            if (text.equals(selectedSticker)) {
               this.applyText("§a  ▶ " + text + " §7(active)");
            } else {
               this.applyText("§7  • " + text);
            }
         }
      }

      return 1;
   }

   private int computeIntAlt(CommandContext<CommandSource> ctx) {
      Southside.INSTANCE.getStickerManager().updateEx();
      List list = Southside.INSTANCE.getStickerManager().collectItems();
      this.applyText("§aReloaded! Found " + list.size() + " sticker(s)");
      return 1;
   }

   private int computeIntImpl(CommandContext<CommandSource> ctx) {
      String text = ((String)ctx.getArgument("name", String.class)).trim();
      StickerManager stickerManager = Southside.INSTANCE.getStickerManager();
      List list = stickerManager.collectItems();
      if (list.contains(text)) {
         stickerManager.applyText(text);
         this.applyText("§aSticker set to: §f" + text);
      } else {
         this.applyText("§cSticker not found: §f" + text);
         this.applyText("§7Use .stickers list to see available stickers");
      }

      return 1;
   }

   private void applyText(String text) {
      if (mc.player != null) {
         mc.player.sendMessage(Text.literal(text), false);
      }
   }

   public StickersCommand() {
      super("stickers", "Manage HUD stickers", "sticker");
   }

   private int computeIntDirect(CommandContext<CommandSource> ctx) {
      this.applyText("§eUsage:");
      this.applyText("§7  .stickers list §8- List available stickers");
      this.applyText("§7  .stickers use <name> §8- Show a sticker");
      this.applyText("§7  .stickers off §8- Hide current sticker");
      this.applyText("§7  .stickers reload §8- Reload stickers folder");
      return 1;
   }

   @Override
   public void build(LiteralArgumentBuilder<CommandSource> literalArgumentBuilder) {
      literalArgumentBuilder.executes(this::computeIntDirect);
      literalArgumentBuilder.then(Command.literal("list").executes(this::computeIntEx));
      literalArgumentBuilder.then(Command.literal("use").then(Command.getRequiredArgumentBuilder("name", StickerArgumentType.getINSTANCE()).executes(this::computeIntImpl)));
      literalArgumentBuilder.then(Command.literal("off").executes(this::computeInt));
      literalArgumentBuilder.then(Command.literal("reload").executes(this::computeIntAlt));
   }
}
