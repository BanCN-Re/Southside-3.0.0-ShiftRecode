package dev.southside.utils;

import com.google.gson.annotations.SerializedName;
import dev.southside.gui.SouthsideTitleScreen;
import dev.southside.mixin.misc.MixinConnectScreen;
import dev.southside.mixin.misc.MixinControlsListWidget_CategoryEntry;
import dev.southside.mixin.misc.MixinControlsListWidget_KeyBindingEntry;
import dev.southside.mixin.misc.MixinEditGameRulesScreen_NamedRuleWidget;
import dev.southside.mixin.misc.MixinEditGameRulesScreen_RuleCategoryWidget;
import dev.southside.mixin.misc.MixinMultiplayerServerListWidget;
import dev.southside.mixin.misc.MixinMultiplayerServerListWidget_ServerEntry;
import dev.southside.mixin.misc.MixinPackListWidget;
import dev.southside.mixin.misc.MixinPackListWidget_ResourcePackEntry;
import dev.southside.mixin.misc.MixinSimpleOption_OptionSliderWidgetImpl;
import dev.southside.mixin.misc.MixinWorldListWidget;
import dev.southside.mixin.misc.MixinWorldListWidget_WorldEntry;
import dev.southside.render.MenuTextFieldRenderer;
import dev.southside.render.MenuOverlayRenderer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ParentElement;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget.ServerEntry;
import net.minecraft.client.gui.screen.pack.PackListWidget.ResourcePackEntry;
import net.minecraft.client.gui.screen.pack.ResourcePackOrganizer.Pack;
import net.minecraft.client.gui.screen.world.WorldListWidget;
import net.minecraft.client.gui.screen.world.WorldListWidget.WorldEntry;
import net.minecraft.client.gui.widget.AbstractTextWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.gui.widget.OptionListWidget;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.network.ServerInfo.Status;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

public final class MenuScreenLayout {
   private static final int DEFAULT_INT = 252;
   private static final int DEFAULT_INT_VALUE = 36;
   private static final int DEFAULT_INT_DATA = 42;
   @SerializedName("screen")
   private final Screen screen;
   @SerializedName("originalPositions")
   private final Map<ClickableWidget, MenuScreenLayout.Entry2> originalPositions = new IdentityHashMap<>();
   @SerializedName("fields")
   private final Map<TextFieldWidget, MenuTextFieldRenderer> fields = new IdentityHashMap<>();
   @SerializedName("body")
   private final List<MenuScreenLayout.Entry4> body = new ArrayList<>();
   @SerializedName("footer")
   private final List<MenuScreenLayout.Entry3> footer = new ArrayList<>();
   @SerializedName("visibleControls")
   private final List<MenuScreenLayout.Entry3> visibleControls = new ArrayList<>();
   @SerializedName("visibleRows")
   private final List<MenuScreenLayout.Entry> visibleRows = new ArrayList<>();
   @SerializedName("options")
   private final List<OptionListWidget> options = new ArrayList<>();
   @SerializedName("captured")
   private ClickableWidget captured;
   @SerializedName("lastFocused")
   private ClickableWidget lastFocused;
   @SerializedName("scroll")
   private float scroll;
   @SerializedName("maxScroll")
   private float maxScroll;
   @SerializedName("bodyTop")
   private int bodyTop;
   @SerializedName("bodyBottom")
   private int bodyBottom;
   @SerializedName("shownAt")
   private long shownAt;

   public MenuScreenLayout(Screen screen2) {
      this.screen = screen2;
   }

   public void performAction() {
      this.captured = null;
      this.lastFocused = null;
      this.shownAt = Util.getMeasuringTimeMs();
   }

   public void render(DrawContext context, int n, int n2) {
      this.body.clear();
      this.footer.clear();
      this.options.clear();
      this.visibleControls.clear();
      this.visibleRows.clear();
      boolean flag = this.screen instanceof SouthsideTitleScreen;
      ArrayList list = new ArrayList(this.screen.children());

      for (Object obj : list) {
         Element element = (Element)obj;
         if (element instanceof ClickableWidget widget) {
            this.originalPositions.computeIfAbsent(widget, widget2 -> new MenuScreenLayout.Entry2(widget2.getY(), widget2.getY() >= this.screen.height - 56));
         }
      }

      list.sort(Comparator.comparingInt(obj2 -> obj2 instanceof ClickableWidget widget3 ? this.originalPositions.get(widget3).y : 0));

      for (Object obj3 : list) {
         Element element2 = (Element)obj3;
         this.performAction(element2, List.of(this.screen), true, flag);
      }

      if (this.captured == null) {
         for (OptionListWidget optionListWidget : this.options) {
            optionListWidget.applyAllPendingValues();
         }
      }

      if (!flag) {
         String text = this.screen instanceof MixinConnectScreen mixinConnectScreen ? mixinConnectScreen.status().getString() : this.screen.getNarratedTitle().getString();
         String string = this.screen.getTitle().getString();
         if (text.startsWith(string)) {
            text = text.substring(string.length()).strip();
         }

         if (!text.isBlank()) {
            this.body.add(0, MenuScreenLayout.Entry4.getEntry4(text));
         }
      }

      int n3 = 0;

      for (MenuScreenLayout.Entry4 entry4 : this.body) {
         n3 += this.computeInt(entry4, flag);
      }

      if (flag) {
         this.bodyTop = Math.max(56, (this.screen.height - n3 + 28) / 2);
         MenuOverlayRenderer.render(context, 40.0F, this.bodyTop - 24);
      } else {
         MenuOverlayRenderer.render(context, this.screen.getTitle().getString(), 28.0F, 31.0F, 24.0F, OneConfigColors.WHITE);
         this.bodyTop = 66;
      }

      int n4 = this.footer.size() > 4 ? 3 : Math.max(1, Math.min(2, this.footer.size()));
      int n5 = (this.footer.size() + n4 - 1) / n4;
      this.bodyBottom = this.screen.height - 18 - n5 * 36;
      this.maxScroll = Math.max(0, n3 - (this.bodyBottom - this.bodyTop));
      this.scroll = Math.max(0.0F, Math.min(this.scroll, this.maxScroll));
      context.enableScissor(24, this.bodyTop - 2, this.screen.width - 24, this.bodyBottom);
      int n6 = this.bodyTop - Math.round(this.scroll);
      int n7 = 0;
      int n8 = flag ? Math.min(252, this.screen.width - 56) : this.screen.width - 56;

      for (MenuScreenLayout.Entry4 entry42 : this.body) {
         int n9 = this.computeInt(entry42, flag);
         boolean flag2 = n6 + n9 >= this.bodyTop && n6 < this.bodyBottom;
         int n10 = flag ? 28 : 28 + this.computeInt(flag2 ? n7++ : 0);
         if (entry42.control != null) {
            MenuScreenLayout.Entry3 control = entry42.control;
            boolean flag3 = control.widget instanceof TextFieldWidget;
            int n11 = flag3 ? 34 : (flag ? 36 : 30);
            control.widget.setDimensionsAndPosition(n8, n11, n10, n6 + (flag3 ? 18 : 0));
            if (n6 + n9 >= this.bodyTop && n6 < this.bodyBottom) {
               if (flag3) {
                  MenuOverlayRenderer.render(context, control.widget.getMessage().getString(), n10 + 2, n6 + 8, 10.0F, OneConfigColors.WHITE_60);
               }

               this.render(context, control, n, n2);
            }
         } else if (entry42.entry != null) {
            entry42.list.setDimensionsAndPosition(this.screen.width - 56, this.bodyBottom - this.bodyTop, 28, this.bodyTop);
            if (flag2) {
               String text2 = "";
               String text3;
               if (entry42.entry instanceof ServerEntry serverEntry) {
                  ServerInfo server = serverEntry.getServer();
                  MixinMultiplayerServerListWidget_ServerEntry mixinMultiplayerServerListWidget_ServerEntry = (MixinMultiplayerServerListWidget_ServerEntry)serverEntry;
                  if (server.getStatus() == Status.INITIAL) {
                     server.setStatus(Status.PINGING);
                     server.label = Text.empty();
                     server.playerCountLabel = Text.empty();
                     MixinMultiplayerServerListWidget.pinger().submit(mixinMultiplayerServerListWidget_ServerEntry::ping);
                  }

                  mixinMultiplayerServerListWidget_ServerEntry.updateStatus();
                  text3 = server.name;
                  text2 = server.label != null && !server.label.getString().isBlank() ? server.label.getString() : server.address;
               } else if (entry42.entry instanceof WorldEntry worldEntry) {
                  text3 = worldEntry.getLevelDisplayName();
                  text2 = ((MixinWorldListWidget_WorldEntry)(Object)worldEntry).summary().getDetails().getString();
               } else if (entry42.entry instanceof ResourcePackEntry resourcePackEntry) {
                  Pack pack = ((MixinPackListWidget_ResourcePackEntry)resourcePackEntry).pack();
                  text3 = pack.getDisplayName().getString();
                  text2 = pack.getDescription().getString();
               } else if (entry42.entry instanceof net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget.Entry entry) {
                  text3 = entry.getNarration().getString();
               } else {
                  text3 = entry42.label;
               }

               boolean flag4 = n >= n10 && n < n10 + this.screen.width - 56 && n2 >= n6 && n2 < n6 + n9 - 6;
               MenuOverlayRenderer.render(context, text3, text2, n10, n6, this.screen.width - 56, n9 - 6, entry42.list.getSelectedOrNull() == entry42.entry, flag4);
               if (entry42.entry instanceof MixinPackListWidget_ResourcePackEntry mixinPackListWidget_ResourcePackEntry) {
                  Pack pack2 = mixinPackListWidget_ResourcePackEntry.pack();
                  if (pack2.canMoveTowardStart()) {
                     MenuOverlayRenderer.render(context, n10 + this.screen.width - 80, n6 + 12, true);
                  }

                  if (pack2.canMoveTowardEnd()) {
                     MenuOverlayRenderer.render(context, n10 + this.screen.width - 80, n6 + 38, false);
                  }
               }

               this.visibleRows.add(new MenuScreenLayout.Entry(entry42, n10, n6, this.screen.width - 56, n9 - 6));
            }
         } else if (flag2) {
            MenuOverlayRenderer.render(context, entry42.label, n10 + 2, n6 + 12, this.screen.width - 60);
         }

         n6 += n9;
      }

      context.disableScissor();

      for (int i = 0; i < this.footer.size(); i++) {
         int n12 = (this.screen.width - 56 - (n4 - 1) * 8) / n4;
         MenuScreenLayout.Entry3 entry3 = this.footer.get(i);
         entry3.widget
            .setDimensionsAndPosition(n12, 28, 28 + i % n4 * (n12 + 8) + this.computeInt(n7 + i), this.bodyBottom + 8 + i / n4 * 36);
         this.render(context, entry3, n, n2);
      }

      if (this.maxScroll > 0.0F) {
         MenuOverlayRenderer.render(context, this.screen.width - 17, this.bodyTop, this.bodyBottom - this.bodyTop, n3, this.scroll);
      }

      for (MenuScreenLayout.Entry4 entry43 : this.body) {
         if (entry43.control != null && entry43.control.widget.isFocused() && entry43.control.widget != this.lastFocused) {
            ClickableWidget widget4 = entry43.control.widget;
            if (widget4.getY() < this.bodyTop) {
               this.scroll = this.scroll + (widget4.getY() - this.bodyTop);
            } else if (widget4.getBottom() > this.bodyBottom) {
               this.scroll = this.scroll + (widget4.getBottom() - this.bodyBottom);
            }

            this.lastFocused = widget4;
         }
      }
   }

   private void performAction(Element element, List<ParentElement> list, boolean flag, boolean flag2) {
      if (element instanceof EntryListWidget entryListWidget) {
         if (entryListWidget instanceof MixinPackListWidget mixinPackListWidget) {
            this.body.add(MenuScreenLayout.Entry4.getEntry4(mixinPackListWidget.title().getString()));
         }

         if (entryListWidget instanceof WorldListWidget) {
            MixinWorldListWidget mixinWorldListWidget = (MixinWorldListWidget)entryListWidget;
            List list2 = mixinWorldListWidget.loadedWorlds();
            if (list2 != mixinWorldListWidget.visibleWorlds()) {
               mixinWorldListWidget.showWorlds(list2);
            }
         }

         if (entryListWidget instanceof OptionListWidget optionListWidget) {
            this.options.add(optionListWidget);
         }

         List list3 = collectItems(list, entryListWidget);

         for (Object obj : entryListWidget.children()) {
            Element element2 = (Element)obj;
            if (element2 instanceof MixinControlsListWidget_CategoryEntry mixinControlsListWidget_CategoryEntry) {
               this.body.add(MenuScreenLayout.Entry4.getEntry4(mixinControlsListWidget_CategoryEntry.category().getString()));
            } else if (element2 instanceof MixinEditGameRulesScreen_RuleCategoryWidget mixinEditGameRulesScreen_RuleCategoryWidget) {
               this.body.add(MenuScreenLayout.Entry4.getEntry4(mixinEditGameRulesScreen_RuleCategoryWidget.name().getString()));
            } else if (element2 instanceof ParentElement parentElement && !parentElement.children().isEmpty()) {
               if (element2 instanceof MixinControlsListWidget_KeyBindingEntry mixinControlsListWidget_KeyBindingEntry) {
                  this.body.add(MenuScreenLayout.Entry4.getEntry4(mixinControlsListWidget_KeyBindingEntry.bindingName().getString()));
               }

               if (element2 instanceof MixinEditGameRulesScreen_NamedRuleWidget mixinEditGameRulesScreen_NamedRuleWidget) {
                  StringBuilder sb = new StringBuilder();

                  for (OrderedText orderedText : mixinEditGameRulesScreen_NamedRuleWidget.name()) {
                     orderedText.accept((n, style, n2) -> {
                        sb.appendCodePoint(n2);
                        return true;
                     });
                  }

                  this.body.add(MenuScreenLayout.Entry4.getEntry4(sb.toString()));
               }

               for (Element element3 : parentElement.children()) {
                  this.performAction(element3, collectItems(list3, parentElement), false, flag2);
               }
            } else {
               this.body.add(new MenuScreenLayout.Entry4(null, entryListWidget, element2, ""));
            }
         }
      } else if (element instanceof AbstractTextWidget abstractTextWidget) {
         if (!abstractTextWidget.getMessage().getString().equals(this.screen.getTitle().getString())) {
            this.body.add(MenuScreenLayout.Entry4.getEntry4(abstractTextWidget.getMessage().getString()));
         }
      } else if (element instanceof ClickableWidget widget) {
         if (!widget.visible) {
            return;
         }

         MenuScreenLayout.Entry3 entry3 = new MenuScreenLayout.Entry3(widget, list);
         MenuScreenLayout.Entry2 entry2 = this.originalPositions
            .computeIfAbsent(widget, widget2 -> new MenuScreenLayout.Entry2(widget2.getY(), widget2.getY() >= this.screen.height - 56));
         if (flag && !flag2 && widget instanceof PressableWidget && entry2.footer) {
            this.footer.add(entry3);
         } else {
            this.body.add(new MenuScreenLayout.Entry4(entry3, null, null, ""));
         }
      } else if (element instanceof ParentElement parentElement2) {
         for (Element element4 : parentElement2.children()) {
            this.performAction(element4, collectItems(list, parentElement2), false, flag2);
         }
      }
   }

   private void render(DrawContext context, MenuScreenLayout.Entry3 entry3, int n, int n2) {
      ClickableWidget widget = entry3.widget;
      if (widget != this.captured && widget instanceof MixinSimpleOption_OptionSliderWidgetImpl mixinSimpleOption_OptionSliderWidgetImpl) {
         mixinSimpleOption_OptionSliderWidgetImpl.southside$applyPendingValue();
      }

      this.visibleControls.add(entry3);
      if (widget instanceof TextFieldWidget textField) {
         this.fields.computeIfAbsent(textField, textField2 -> new MenuTextFieldRenderer()).render(context, textField);
      } else if (widget instanceof SliderWidget sliderWidget) {
         MenuOverlayRenderer.render(context, sliderWidget, ((IMenuSliderAccessor)sliderWidget).menuValue());
      } else if (widget instanceof PressableWidget widget2) {
         MenuOverlayRenderer.render(context, widget2, widget.isMouseOver(n, n2));
      } else {
         MenuOverlayRenderer.render(context, widget.getMessage().getString(), widget.getX(), widget.getY() + 15, 12.0F, OneConfigColors.WHITE_90);
      }
   }

   public boolean isValid(double d, double d2, int n) {
      for (MenuScreenLayout.Entry3 entry3 : this.visibleControls) {
         ClickableWidget widget = entry3.widget;
         boolean flag = d2 >= this.bodyTop && d2 < this.bodyBottom;
         if (widget.active && widget.isMouseOver(d, d2) && (flag || this.footer.contains(entry3))) {
            performAction(entry3.path, widget);
            if (widget instanceof TextFieldWidget textField && n == 0) {
               this.fields.computeIfAbsent(textField, textField2 -> new MenuTextFieldRenderer()).performAction(textField, d, false);
               this.captured = textField;
               return true;
            }

            if (widget.mouseClicked(d, d2, n)) {
               this.captured = widget;
               return true;
            }
         }
      }

      if (!(d2 < this.bodyTop) && !(d2 >= this.bodyBottom)) {
         for (MenuScreenLayout.Entry entry : this.visibleRows) {
            if (!(d < entry.x) && !(d >= entry.x + entry.width) && !(d2 < entry.y) && !(d2 >= entry.y + entry.height)) {
               this.screen.setFocused(entry.item.list);
               if (entry.item.entry instanceof ResourcePackEntry resourcePackEntry && n == 0) {
                  Pack pack = ((MixinPackListWidget_ResourcePackEntry)resourcePackEntry).pack();
                  if (d >= entry.x + entry.width - 32 && d2 < entry.y + entry.height / 2 && pack.canMoveTowardStart()) {
                     pack.moveTowardStart();
                  } else if (d >= entry.x + entry.width - 32 && d2 >= entry.y + entry.height / 2 && pack.canMoveTowardEnd()) {
                     pack.moveTowardEnd();
                  } else {
                     resourcePackEntry.toggle();
                  }

                  return true;
               }

               int n2 = !(entry.item.list instanceof WorldListWidget) && !(entry.item.list instanceof MultiplayerServerListWidget) ? 16 : 40;
               return entry.item
                  .entry
                  .mouseClicked(
                     entry.item.list.getRowLeft() + n2, entry.item.list.getRowTop(entry.item.list.children().indexOf(entry.item.entry)) + 10, n
                  );
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public boolean isValidEx(double d, double d2, int n) {
      if (this.captured == null) {
         return false;
      }

      this.captured.mouseReleased(d, d2, n);
      this.captured = null;

      for (OptionListWidget optionListWidget : this.options) {
         optionListWidget.applyAllPendingValues();
      }

      return true;
   }

   public boolean isHovered(double d, double d2, int n, double d3, double d4) {
      if (this.captured instanceof TextFieldWidget textField) {
         this.fields.get(textField).performAction(textField, d, true);
         return true;
      } else {
         return this.captured != null && this.captured.mouseDragged(d, d2, n, d3, d4);
      }
   }

   public boolean isValid(double d) {
      this.scroll = (float)Math.max(0.0, Math.min(this.maxScroll, this.scroll - d * 40.0));
      return this.maxScroll > 0.0F;
   }

   private static void performAction(List<ParentElement> list, Element element) {
      for (int i = 0; i < list.size(); i++) {
         ((ParentElement)list.get(i)).setFocused(i + 1 < list.size() ? (Element)list.get(i + 1) : element);
      }

      element.setFocused(true);
   }

   private static List<ParentElement> collectItems(List<ParentElement> list, ParentElement parentElement) {
      ArrayList list2 = new ArrayList(list);
      list2.add(parentElement);
      return list2;
   }

   private int computeInt(MenuScreenLayout.Entry4 entry4, boolean flag) {
      if (entry4.entry != null) {
         return 62;
      }

      if (entry4.control != null) {
         if (entry4.control.widget instanceof TextFieldWidget) {
            return 60;
         } else {
            return flag ? 42 : 38;
         }
      } else {
         int n = 0;

         for (String text : entry4.label.split("\n", -1)) {
            int n2 = text.codePoints().map(n3 -> n3 > 255 ? 13 : 7).sum();
            n += Math.max(1, (n2 + this.screen.width - 61) / (this.screen.width - 60));
         }

         return Math.max(30, n * 18 + 8);
      }
   }

   private int computeInt(int n) {
      long l = Util.getMeasuringTimeMs() - this.shownAt;
      float f = Math.max(0.0F, Math.min(1.0F, (float)(l - Math.min(n, 10) * 55) / 320.0F));
      return -Math.round((this.screen.width + 32) * (float)Math.pow(1.0F - f, 4.0));
   }

   private record Entry(MenuScreenLayout.Entry4 item, int x, int y, int width, int height) {

      public MenuScreenLayout.Entry4 getItem() {
         return this.item;
      }

      public int getX() {
         return this.x;
      }

      public int getY() {
         return this.y;
      }

      public int getWidth() {
         return this.width;
      }

      public int getHeight() {
         return this.height;
      }
   }

   private record Entry2(int y, boolean footer) {

      public int getY() {
         return this.y;
      }

      public boolean isFooter() {
         return this.footer;
      }
   }

   private record Entry3(ClickableWidget widget, List<ParentElement> path) {

      public ClickableWidget getWidget() {
         return this.widget;
      }

      public List<ParentElement> getPath() {
         return this.path;
      }
   }

   private record Entry4(MenuScreenLayout.Entry3 control, EntryListWidget<?> list, Element entry, String label) {

      public static MenuScreenLayout.Entry4 getEntry4(String text) {
         return new MenuScreenLayout.Entry4(null, null, null, text);
      }

      public MenuScreenLayout.Entry3 getControl() {
         return this.control;
      }

      public EntryListWidget<?> getList() {
         return this.list;
      }

      public Element getEntry() {
         return this.entry;
      }

      public String getLabel() {
         return this.label;
      }
   }
}
