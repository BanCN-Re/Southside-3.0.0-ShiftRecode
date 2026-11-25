package dev.southside.utils;

import dev.southside.mixin.hud.ChatHudAccessor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.client.gui.hud.ChatHudLine.Visible;
import net.minecraft.util.math.MathHelper;

public class ChatAnimationRenderer {
   private static final MinecraftClient mc = MinecraftClient.getInstance();
   private static final Map<Integer, ChatLineAnimation> chatLineAnimationMap = new HashMap<>();
   private static final Set<Integer> integersSet = new HashSet<>();
   private static final Map<Integer, Integer> integerMap = new HashMap<>();
   private static final List<Visible> visibles = new ArrayList<>();
   private static int count = -1;
   private static ChatLineAnimation chatLineAnimation;
   private static ChatLineAnimation chatLineAnimationValue;
   private static boolean active;
   private static CubicBezierAnimation cubicBezierAnimation;
   private static int countValue;
   private static float factor;
   private static CubicBezierAnimation cubicBezierAnimationValue;
   private static int countData = Integer.MIN_VALUE;
   private static float factorValue;

   public static void render(DrawContext context, int n, int n2, int n3) {
      ChatHudAccessor chatHud = (ChatHudAccessor)mc.inGameHud.getChatHud();
      if (!chatHud.invokeIsChatHidden()) {
         int n4 = chatHud.invokeGetVisibleLineCount();
         int size = chatHud.getVisibleMessages().size();
         int scrolledLines = chatHud.getScrolledLines();
         boolean flag = chatHud.invokeIsChatFocused();
         boolean flag2 = flag || n4 > 0;
         if (active != flag2) {
            if (chatLineAnimationValue == null) {
               chatLineAnimationValue = new ChatLineAnimation(200, 0.0F, 0.0F, active ? 1.0F : 0.0F, 0.0F, 0.0F);
            }

            chatLineAnimationValue.performActionEx(200, 0.0F, 0.0F, flag2 ? 1.0F : 0.0F);
            active = flag2;
         }

         if (countValue != scrolledLines) {
            countValue = scrolledLines;
            float f = cubicBezierAnimation != null && !cubicBezierAnimation.isValid() ? cubicBezierAnimation.getProgress() : factor;
            cubicBezierAnimation = new CubicBezierAnimation(new float[]{0.25F, 0.01F, 0.25F, 1.0F}, 300.0F, f, scrolledLines, false);
         }

         float f2 = scrolledLines;
         if (cubicBezierAnimation != null && !cubicBezierAnimation.isValid()) {
            f2 = cubicBezierAnimation.getProgress();
         }

         factor = f2;
         if (size <= 0) {
            render(context, chatHud, chatLineAnimationValue == null ? 1.0F : chatLineAnimationValue.getProgressImpl());
         } else {
            performAction(chatHud, scrolledLines, n4);
            performAction(chatHud, n);
            onKeyTyped(n);
            performActionEx(chatHud, scrolledLines);
            render(context, chatHud, n, n2, n3, flag, flag2, f2, n4, chatLineAnimationValue == null ? 1.0F : chatLineAnimationValue.getProgressImpl());
         }
      }
   }

   private static void performAction(ChatHudAccessor chatHudAccessor, int n, int n2) {
      visibles.clear();
      int n3 = n + n2;

      for (int i = n; i < Math.min(n3, chatHudAccessor.getVisibleMessages().size()); i++) {
         if (i >= 0) {
            Visible visible = chatHudAccessor.getVisibleMessages().get(i);
            if (visible != null) {
               visibles.add(visible);
            }
         }
      }
   }

   private static void onKeyTyped(int n) {
      for (Visible visible : visibles) {
         int n2 = Objects.hash(visible.addedTime(), visible.content());
         if (!integersSet.contains(n2)) {
            getChatLineAnimation(n2, n);
         }
      }
   }

   private static void performAction(ChatHudAccessor chatHudAccessor, int n) {
      if (count != n) {
         count = n;
         List visibleMessages = chatHudAccessor.getVisibleMessages();
         if (visibleMessages.isEmpty()) {
            chatLineAnimationMap.clear();
            integersSet.clear();
            integerMap.clear();
         } else {
            HashSet set = new HashSet(visibleMessages.size());

            for (Object obj : visibleMessages) {
               Visible visible = (Visible)obj;
               if (visible != null) {
                  set.add(Objects.hash(visible.addedTime(), visible.content()));
               }
            }

            chatLineAnimationMap.keySet().removeIf(integer -> !set.contains(integer));
            integersSet.removeIf(integer2 -> !set.contains(integer2));
            integerMap.keySet().removeIf(integer3 -> !set.contains(integer3));
         }
      }
   }

   private static ChatLineAnimation getChatLineAnimation(int n, int n2) {
      ChatLineAnimation chatLineAnimation2 = chatLineAnimationMap.get(n);
      if (chatLineAnimation2 != null) {
         return chatLineAnimation2;
      }

      integersSet.add(n);
      integerMap.put(n, n2);
      chatLineAnimation2 = new ChatLineAnimation(800, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
      chatLineAnimation2.performActionEx(800, 0.0F, 0.0F, 1.0F);
      chatLineAnimationMap.put(n, chatLineAnimation2);
      return chatLineAnimation2;
   }

   private static void render(
      DrawContext context, ChatHudAccessor chatHudAccessor, int n, int n2, int n3, boolean flag, boolean flag2, float f, int n4, float f2
   ) {
      NanoVGHelper nvg = NanoVGHelper.INSTANCE;
      float f3 = (float)chatHudAccessor.invokeGetChatScale();
      int scaledWindowHeight = context.getScaledWindowHeight();
      context.getMatrices().push();
      context.getMatrices().scale(f3, f3, 1.0F);
      context.getMatrices().translate(4.0F, 0.0F, 0.0F);
      int n5 = MathHelper.floor((scaledWindowHeight - 40) / f3);
      int n6 = chatHudAccessor.invokeGetMessageIndex(chatHudAccessor.invokeToChatLineX(n2), chatHudAccessor.invokeToChatLineY(n3));
      double d = (Double)chatHudAccessor.getClient().options.getChatOpacity().getValue() * 0.9 + 0.1;
      double d2 = (Double)chatHudAccessor.getClient().options.getTextBackgroundOpacity().getValue();
      double d3 = (Double)chatHudAccessor.getClient().options.getChatLineSpacing().getValue();
      int n7 = chatHudAccessor.invokeGetLineHeight();
      int n8 = (int)Math.round(-8.0 * (d3 + 1.0) + 4.0 * d3);
      ChatAnimationRenderer.Entry entry = getEntry(chatHudAccessor, n, flag, flag2);
      if (chatLineAnimation == null) {
         chatLineAnimation = new ChatLineAnimation(200, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F);
      }

      if (f2 > 0.0F) {
         chatLineAnimation.performAction(200, entry.getWidth(), entry.getHeight(), 1.0F);
      } else {
         chatLineAnimation.performAction(200, 0.0F, 0.0F, 0.0F);
      }

      float progressEx = chatLineAnimation.getProgressEx();
      float progressByIndex = chatLineAnimation.getProgressByIndex();
      float f4 = chatLineAnimation.getProgressWithOffset() * f2;
      if ((entry.getHeight() > 0 || progressByIndex > 0.1F) && f4 > 0.0F) {
         int n9 = (int)(255.0 * d2 * f4);
         nvg.performAction(true, l -> performAction(nvg, n5, progressByIndex, progressEx, n9, l));
      }

      float f5 = -4.0F;
      float f6 = n5 - progressByIndex + 3.0F;
      float f7 = progressEx + 6.0F;
      float f8 = n5 - 1.0F;
      int n10 = MathHelper.floor((f5 + 4.0F - 2.0F) * f3);
      int n11 = MathHelper.floor((f6 - 4.0F) * f3);
      int n12 = MathHelper.ceil((f7 + 4.0F + 2.0F) * f3);
      int n13 = MathHelper.ceil((f8 + 3.0F) * f3);
      context.enableScissor(n10, n11, n12, n13);
      render(context, chatHudAccessor, n, flag, flag2, d, n7, n8, n5, n6, f, n4, f2, factorValue);
      context.disableScissor();
      context.getMatrices().pop();
   }

   private static void render(
      DrawContext context,
      ChatHudAccessor chatHudAccessor,
      int n,
      boolean flag,
      boolean flag2,
      double d,
      int n2,
      int n3,
      int n4,
      int n5,
      float f,
      int n6,
      float f2,
      float f3
   ) {
      int n7 = Math.max(0, (int)Math.floor(f) - 1);
      int n8 = Math.min(chatHudAccessor.getVisibleMessages().size(), (int)Math.ceil(f) + n6 + 1);

      for (int i = n7; i < n8; i++) {
         Visible visible = chatHudAccessor.getVisibleMessages().get(i);
         if (visible != null) {
            int n9 = n - visible.addedTime();
            int n10 = Objects.hash(visible.addedTime(), visible.content());
            if (n9 < 200 || flag || !flag2) {
               double d2 = flag ? 1.0 : ChatHudAccessor.invokeGetMessageOpacityMultiplier(n9);
               int n11 = (int)(255.0 * d2 * d);
               if (n11 > 3) {
                  ChatLineAnimation chatLineAnimation2 = getChatLineAnimation(n10, n);
                  if (chatLineAnimation2.getProgressAlt() < 0.999F) {
                     chatLineAnimation2.performActionEx(800, 0.0F, 0.0F, 1.0F);
                  }

                  float progressImpl = chatLineAnimation2.getProgressImpl();
                  float clamped = MathHelper.clamp((n9 + 1.0F) / 5.0F, 0.0F, 1.0F);
                  float f4 = progressImpl * f2 * clamped;
                  int n12 = (int)(n11 * f4);
                  if (n12 > 3) {
                     float f5 = i - f;
                     int n13 = n4 - (int)(f5 * n2);
                     int n14 = n13 + (int)f3;
                     int n15 = n14 + n3;
                     MessageIndicator messageIndicator = visible.indicator();
                     if (messageIndicator != null) {
                        int n16 = messageIndicator.indicatorColor() & 16777215 | n12 << 24;
                        context.fill(-4, n14 - n2, -2, n14, n16);
                        if (i == n5 && messageIndicator.icon() != null) {
                           int n17 = chatHudAccessor.invokeGetIndicatorX(visible);
                           chatHudAccessor.invokeDrawIndicatorIcon(context, n17, n15 + 9, messageIndicator.icon());
                        }
                     }

                     context.drawTextWithShadow(chatHudAccessor.getClient().textRenderer, visible.content(), (int)chatLineAnimation2.getProgressDirect(), n15, 16777215 + (n12 << 24));
                  }
               }
            }
         }
      }
   }

   private static ChatAnimationRenderer.Entry getEntry(ChatHudAccessor chatHudAccessor, int n, boolean flag, boolean flag2) {
      int n2 = 0;
      int n3 = 0;

      for (Visible visible : visibles) {
         int n4 = n - visible.addedTime();
         if (n4 < 200 || flag || !flag2) {
            n2 += chatHudAccessor.invokeGetLineHeight();
            int width = chatHudAccessor.getClient().textRenderer.getWidth(visible.content());
            if (width > n3) {
               n3 = width;
            }
         }
      }

      return new ChatAnimationRenderer.Entry(n2, n3);
   }

   private static void performAction(NanoVGHelper nvg, int n, float f, float f2, int n2, long l) {
      nvg.drawRoundedRect(l, -4.0F, n - f, f2 + 10.0F, f + 2.0F, n2 << 24, 3.0F);
   }

   private static void render(DrawContext context, ChatHudAccessor chatHudAccessor, float f) {
      if (chatLineAnimation != null) {
         chatLineAnimation.performAction(200, 0.0F, 0.0F, 0.0F);
      }

      float f2 = (float)chatHudAccessor.invokeGetChatScale();
      int scaledWindowHeight = context.getScaledWindowHeight();
      int n = MathHelper.floor((scaledWindowHeight - 40) / f2);
      context.getMatrices().push();
      context.getMatrices().scale(f2, f2, 1.0F);
      context.getMatrices().translate(4.0F, 0.0F, 0.0F);
      double d = (Double)chatHudAccessor.getClient().options.getTextBackgroundOpacity().getValue();
      if (chatLineAnimation != null) {
         float progressEx = chatLineAnimation.getProgressEx();
         float progressByIndex = chatLineAnimation.getProgressByIndex();
         float f3 = chatLineAnimation.getProgressWithOffset() * f;
         if (f3 > 0.0F) {
            int n2 = (int)(255.0 * d * f3);
            context.fill(-4, n - (int)progressByIndex, (int)progressEx + 8, n, n2 << 24);
         }
      }

      context.getMatrices().pop();
   }

   private static void performActionEx(ChatHudAccessor chatHudAccessor, int n) {
      if (chatHudAccessor.getVisibleMessages().isEmpty()) {
         factorValue = 0.0F;
         countData = Integer.MIN_VALUE;
      } else {
         Visible first = chatHudAccessor.getVisibleMessages().getFirst();
         int n2 = Objects.hash(first.addedTime(), first.content());
         if (countData != Integer.MIN_VALUE && n2 != countData && n == 0) {
            float f = chatHudAccessor.invokeGetLineHeight();
            cubicBezierAnimationValue = new CubicBezierAnimation(new float[]{0.22F, 1.0F, 0.36F, 1.0F}, 180.0F, f, 0.0F, false);
         }

         countData = n2;
         factorValue = cubicBezierAnimationValue != null && !cubicBezierAnimationValue.isValid() ? cubicBezierAnimationValue.getProgress() : 0.0F;
      }
   }

   static {
      short s = 31223;
      long l = 3890229061942643954L;
   }

   private record Entry(int height, int width) {
      public int getHeight() {
         return this.height;
      }

      public int getWidth() {
         return this.width;
      }
   }
}
