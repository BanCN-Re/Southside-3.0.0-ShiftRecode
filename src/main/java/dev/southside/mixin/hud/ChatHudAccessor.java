package dev.southside.mixin.hud;

import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.hud.ChatHudLine.Visible;
import net.minecraft.client.gui.hud.MessageIndicator.Icon;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ChatHud.class)
public interface ChatHudAccessor {
   @Accessor("visibleMessages")
   List<Visible> getVisibleMessages();

   @Accessor("scrolledLines")
   void setScrolledLines(int n);

   @Accessor("scrolledLines")
   int getScrolledLines();

   @Invoker("getIndicatorX")
   int invokeGetIndicatorX(Visible visible);

   @Invoker("toChatLineX")
   double invokeToChatLineX(double d);

   @Invoker("getHeight")
   int invokeGetHeight();

   @Accessor("client")
   MinecraftClient getClient();

   @Invoker("toChatLineY")
   double invokeToChatLineY(double d);

   @Invoker("getChatScale")
   double invokeGetChatScale();

   @Invoker("isChatFocused")
   boolean invokeIsChatFocused();

   @Invoker("addMessage")
   void invokeAddMessage(Text text);

   @Invoker("drawIndicatorIcon")
   void invokeDrawIndicatorIcon(DrawContext context, int n, int n2, Icon icon);

   @Invoker("getMessageOpacityMultiplier")
   static double invokeGetMessageOpacityMultiplier(int n) {
      return 0.0;
   }

   @Accessor("hasUnreadNewMessages")
   void setHasUnreadNewMessages(boolean flag);

   @Invoker("getLineHeight")
   int invokeGetLineHeight();

   @Invoker("isChatHidden")
   boolean invokeIsChatHidden();

   @Invoker("getMessageIndex")
   int invokeGetMessageIndex(double x, double y);

   @Invoker("getWidth")
   int invokeGetWidth();

   @Invoker("mouseClicked")
   boolean invokeMouseClicked(double x, double y);

   @Accessor("hasUnreadNewMessages")
   boolean getHasUnreadNewMessages();

   @Invoker("getVisibleLineCount")
   int invokeGetVisibleLineCount();

   @Accessor("messages")
   List<ChatHudLine> getMessages();
}
