package dev.southside.utils.heypixel;

import cc.polyfrost.oneconfig.config.core.OneColor;
import com.google.gson.annotations.SerializedName;
import dev.southside.Southside;
import dev.southside.auth.IrcUser;
import dev.southside.event.EventTarget;
import dev.southside.event.Stage;
import dev.southside.event.events.ServerJoinEvent;
import dev.southside.event.events.StateEvent;
import dev.southside.manager.IRCManager;
import dev.southside.module.impl.render.HUD;
import dev.southside.module.impl.render.NameProtect;
import dev.southside.utils.IMinecraft;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.CharacterVisitor;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

public final class VIPList implements IMinecraft {
   private static final Map<String, String> vips = new HashMap<>(
      Map.of(
         "\ue0f8",
         "[VIP1]",
         "\ue0f9",
         "[VIP2]",
         "\ue0fa",
         "[VIP3]",
         "\ue0fb",
         "[VIP4]",
         "\ue0fc",
         "[VIP5]",
         "\ue0fd",
         "[VIP6]",
         "\ue0fe",
         "[VIP7]",
         "\ue0ff",
         "[VIP8]"
      )
   );
   public static final List<VIPList.Entry> entries = new CopyOnWriteArrayList<>();
   private static final Map<String, String> stringMap = new ConcurrentHashMap<>();
   private static final int DEFAULT_INT = Formatting.GRAY.getColorValue();
   private static int count = 0;
   private static VIPList INSTANCE;

   private VIPList() {
   }

   public static void performAction() {
      VIPList viplist = new VIPList();
      INSTANCE = viplist;
      Southside.INSTANCE.getEventBus().subscribe(viplist);
   }

   private void performActionEx() {
      HUD hud = Southside.INSTANCE.getModuleManager().getModule(HUD.class);
      NameProtect nameProtect = Southside.INSTANCE.getModuleManager().getModule(NameProtect.class);
      String text = (String)hud.serverReplaceTo.getValue();
      boolean isEnabled = nameProtect.isEnabled();
      String text2 = (String)nameProtect.replacementName.getValue();
      int n = ((OneColor)nameProtect.replacementColor.getValue()).fromHsb();
      int n2 = vips.hashCode();
      String text3 = mc.player != null ? mc.player.getGameProfile().getName() : "";
      int n3 = Objects.hash(text, isEnabled, text2, n, n2, text3, IRCManager.stringMap);
      if (n3 != count || entries.isEmpty()) {
         count = n3;
         this.applyText(text, nameProtect, text3);
         IRCManager.stringMap.forEach(VIPList::performAction);
      }
   }

   private void applyText(String text, NameProtect nameProtect, String text2) {
      entries.clear();
      stringMap.clear();
      entries.add(new VIPList.Entry("布吉岛", text, List.of()));
      if (nameProtect.isEnabled() && !text2.isEmpty()) {
         String text3 = (String)nameProtect.replacementName.getValue();
         List list = List.of(new VIPList.Entry2(0, text3.length() - 1, ((OneColor)nameProtect.replacementColor.getValue()).fromHsb()));
         entries.add(new VIPList.Entry(text2, text3, list));
      }

      vips.forEach(VIPList::applyText);
   }

   private static void performAction(IrcUser ircUser, String text) {
      String text2 = "(" + ircUser.getUsername() + ")";
      List list = List.of(
         new VIPList.Entry2(0, 0, DEFAULT_INT),
         new VIPList.Entry2(1, text2.length() - 2, ((OneColor)NameProtect.descriptionUtil2.getValue()).fromHsb()),
         new VIPList.Entry2(text2.length() - 1, text2.length() - 1, DEFAULT_INT)
      );
      entries.add(new VIPList.Entry(text, text2, list));
   }

   private static void applyText(String text, String text2) {
      entries.add(
         new VIPList.Entry(
            text,
            text2,
            List.of(
               new VIPList.Entry2(0, 0, DEFAULT_INT),
               new VIPList.Entry2(1, text2.length() - 2, Formatting.GREEN.getColorValue()),
               new VIPList.Entry2(text2.length() - 1, text2.length() - 1, DEFAULT_INT)
            )
         )
      );
   }

   public static String formatText(String text) {
      return text != null && !text.isEmpty() ? stringMap.computeIfAbsent(text, VIPList::formatTextEx) : text;
   }

   public static String formatTextEx(String text) {
      StringBuilder sb = new StringBuilder();
      char[] chars = text.toCharArray();
      boolean flag = false;
      int n = 0;

      while (n < chars.length) {
         char ch = chars[n++];
         if (ch == 167) {
            flag = true;
            sb.append(ch);
         } else if (flag) {
            flag = false;
            sb.append(ch);
         } else {
            boolean flag2 = false;
            Iterator it = entries.iterator();

            while (true) {
               if (it.hasNext()) {
                  VIPList.Entry entry = (VIPList.Entry)it.next();
                  int n2 = computeInt(entry, chars, n - 1);
                  if (n2 == -1) {
                     continue;
                  }

                  n += n2 - 1;
                  sb.append(entry.getReplacement());
                  flag2 = true;
               }

               if (!flag2) {
                  sb.append(ch);
               }
               break;
            }
         }
      }

      return sb.toString();
   }

   private static int computeInt(VIPList.Entry entry, char[] chars, int n) {
      char[] chars2 = entry.getSource().toCharArray();
      int n2 = n;
      int n3 = 0;
      boolean flag = false;

      while (n3 < chars2.length) {
         if (n2 > chars.length - 1) {
            return -1;
         }

         char ch = chars[n2++];
         if (ch == 167) {
            flag = true;
         } else if (flag) {
            flag = false;
         } else if (ch != chars2[n3++]) {
            return -1;
         }
      }

      return n2 - n;
   }

   @EventTarget
   public void onCustomEvent3(ServerJoinEvent serverJoinEvent) {
      entries.clear();
      stringMap.clear();
      count = 0;
   }

   @EventTarget
   public void onState(StateEvent stateEvent) {
      if (MinecraftClient.getInstance() != null && MinecraftClient.getInstance().player != null && MinecraftClient.getInstance().world != null) {
         if (stateEvent.getState() == Stage.PRE) {
            this.performActionEx();
         }
      }
   }

   public static final class Class5481Impl implements OrderedText {
      @SerializedName("glyphs")
      private final List<VIPList.Entry3> glyphs = new ArrayList<>();

      public Class5481Impl(OrderedText orderedText) {
         ArrayList list = new ArrayList();
         orderedText.accept((n, style, n2) -> {
            list.add(new VIPList.Entry3(style, n2));
            return true;
         });
         int n3 = 0;

         while (n3 < list.size()) {
            VIPList.Entry3 entry3 = (VIPList.Entry3)list.get(n3);
            int n4 = this.computeInt(n3, list, entry3.getStyle());
            if (n4 > 0) {
               n3 += n4;
            } else {
               this.glyphs.add(entry3);
               n3++;
            }
         }
      }

      private static TextColor getTextColor(int n, List<VIPList.Entry2> list) {
         if (list != null) {
            for (VIPList.Entry2 entry2 : list) {
               if (n >= entry2.getFirstIndex() && n <= entry2.getLastIndex()) {
                  return TextColor.fromRgb(entry2.getColor());
               }
            }
         }

         return null;
      }

      private int computeInt(int n, List<VIPList.Entry3> list, Style style) {
         for (VIPList.Entry entry : VIPList.entries) {
            String source = entry.getSource();
            if (!source.isEmpty()) {
               boolean flag = true;

               for (int i = 0; i < source.length(); i++) {
                  int n2 = n + i;
                  if (list.size() <= n2 || ((VIPList.Entry3)list.get(n2)).getCodePoint() != source.charAt(i)) {
                     flag = false;
                     break;
                  }
               }

               if (flag) {
                  int n3 = 0;

                  for (char ch : entry.getReplacement().toCharArray()) {
                     TextColor textColor = getTextColor(n3, entry.getColorRanges());
                     this.glyphs.add(new VIPList.Entry3(textColor == null ? style : style.withColor(textColor), ch));
                     n3++;
                  }

                  return source.length();
               }
            }
         }

         return 0;
      }

      public boolean accept(CharacterVisitor characterVisitor) {
         int n = 0;

         for (VIPList.Entry3 entry3 : this.glyphs) {
            if (!characterVisitor.accept(n, entry3.getStyle(), entry3.getCodePoint())) {
               return false;
            }

            n++;
         }

         return true;
      }
   }

   private record Entry(String source, String replacement, List<VIPList.Entry2> colorRanges) {
      public String getSource() {
         return this.source;
      }

      public String getReplacement() {
         return this.replacement;
      }

      public List<VIPList.Entry2> getColorRanges() {
         return this.colorRanges;
      }
   }

   private record Entry2(int firstIndex, int lastIndex, int color) {
      public int getFirstIndex() {
         return this.firstIndex;
      }

      public int getLastIndex() {
         return this.lastIndex;
      }

      public int getColor() {
         return this.color;
      }
   }

   private record Entry3(Style style, int codePoint) {
      public Style getStyle() {
         return this.style;
      }

      public int getCodePoint() {
         return this.codePoint;
      }
   }
}
