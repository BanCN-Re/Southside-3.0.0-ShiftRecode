package dev.southside.mixin.misc;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestions;
import dev.southside.command.CommandManager;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.screen.ChatInputSuggestor.SuggestionWindow;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.command.CommandSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ChatInputSuggestor.class)
public abstract class MixinChatInputSuggestor {
   @Shadow
   @Final
   TextFieldWidget textField;
   @Shadow
   boolean completingSuggestions;
   @Shadow
   private ParseResults<CommandSource> parse;
   @Shadow
   private CompletableFuture<Suggestions> pendingSuggestions;
   @Shadow
   private SuggestionWindow window;

   @Inject(
      method = "Lnet/minecraft/client/gui/screen/ChatInputSuggestor;refresh()V",
      at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/StringReader;canRead()Z", remap = false),
      cancellable = true,
      locals = LocalCapture.CAPTURE_FAILHARD
   )
   public void onRefreshInvoke(CallbackInfo ci, String text, StringReader reader) {
      String text2 = ".";
      int len = text2.length();
      if (reader.canRead(len) && reader.getString().startsWith(text2, reader.getCursor())) {
         reader.setCursor(reader.getCursor() + len);
         if (this.parse == null) {
            this.parse = CommandManager.getINSTANCE().commandDispatcher.parse(reader, CommandManager.getINSTANCE().commandSource);
         }

         int cursor = this.textField.getCursor();
         if (cursor >= 1 && (this.window == null || !this.completingSuggestions)) {
            this.pendingSuggestions = CommandManager.getINSTANCE().commandDispatcher.getCompletionSuggestions(this.parse, cursor);
            this.pendingSuggestions.thenRun(this::showSuggestionsWhenReady);
         }

         ci.cancel();
      }
   }

   @Unique
   private void showSuggestionsWhenReady() {
      if (this.pendingSuggestions.isDone()) {
         this.showCommandSuggestions();
      }
   }

   @Shadow
   protected abstract void showCommandSuggestions();
}
