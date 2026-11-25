package cc.polyfrost.oneconfig.config;

import cc.polyfrost.oneconfig.config.core.ConfigUtils;
import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.config.data.OptionType;
import cc.polyfrost.oneconfig.config.data.PageLocation;
import cc.polyfrost.oneconfig.config.elements.BasicOption;
import cc.polyfrost.oneconfig.hud.BasicHud;
import cc.polyfrost.oneconfig.hud.Hud;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.annotations.SerializedName;
import dev.southside.Southside;
import dev.southside.module.Module;
import dev.southside.module.values.Value;
import dev.southside.utils.ConfigCore;
import dev.southside.utils.ConfigField;
import dev.southside.utils.ConfigHeaderOption;
import dev.southside.utils.ConfigKeyBindOption;
import dev.southside.utils.ConfigPageOption;
import dev.southside.utils.GuiPage;
import dev.southside.utils.GuiUtils;
import dev.southside.utils.HudCore;
import dev.southside.utils.HudPosition;
import dev.southside.utils.HudValue;
import dev.southside.utils.KeyBindHandler;
import dev.southside.utils.ModConfigPage;
import dev.southside.utils.ModData;
import dev.southside.utils.OneConfigGui;
import dev.southside.utils.OneKeyBind;
import dev.southside.utils.OptionPage;
import dev.southside.utils.OptionSubcategory;
import dev.southside.utils.PageValue;
import dev.southside.utils.WindowMetrics;
import dev.southside.utils.HudPosition.Mode;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.ApiStatus.Experimental;

public class Config {
   public final transient HashMap<String, BasicOption> optionElements = new HashMap<>();
   public final transient Gson GSON = this.getGsonBuilder(new GsonBuilder()).create();
   public final transient List<Consumer<Boolean>> toggleListener = new ArrayList<>();
   public final transient ModData modData;
   @SerializedName("enabled")
   public ConfigField<Boolean> enabled = new ConfigField<Boolean>("Enabled", false) {
      public void setValue(Boolean val2) {
         super.setValue(val2);
         Config.this.toggleListener.forEach(consumer -> consumer.accept(val2));
      }
   };
   @SerializedName("canToggle")
   public final boolean canToggle;
   public final transient Logger LOGGER;
   private static final long i = 823716917112995968L;

   public Config(ModData modData2, boolean flag, boolean flag2) {
      this.modData = modData2;
      this.enabled.setValue(flag);
      this.canToggle = flag2;
      this.LOGGER = LogManager.getLogger(this.getClass());
   }

   public Config(ModData modData2, boolean flag) {
      this(modData2, flag, true);
   }

   public Config(ModData modData2) {
      this(modData2, false);
   }

   public void initialize() {
      ConfigCore.modDatas.add(this.modData);
   }

   public void setConfigEnabled(boolean flag) {
      this.enabled.setValue(flag);
   }

   public boolean isEnabled() {
      return this.enabled.getValue();
   }

   public void addToggleListener(Consumer<Boolean> consumer) {
      this.toggleListener.add(consumer);
   }

   public void reInitialize() {
      this.LOGGER.trace("Reinitializing config for {}...", this.modData.name);
      if (this instanceof Module module) {
         try {
            Southside.INSTANCE.getModuleManager().performAction(List.of(module));
         } catch (IOException e) {
            e.printStackTrace();
         }
      } else {
         this.initialize();
      }
   }

   public void resetToDefaults() {
      try {
         performAction(null, this);
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   public JsonObject saveToJson() {
      this.LOGGER.trace("Saving config for {}...", this.modData.name);
      JsonObject json = new JsonObject();

      try {
         toJson(json, this);
      } catch (Exception e) {
         e.printStackTrace();
      }

      return json;
   }

   public void loadFromJson(JsonObject json) {
      this.LOGGER.trace("Loading config for {}...", this.modData.name);

      try {
         JsonObject asJsonObject = json.getAsJsonObject(this.modData.name);
         performAction(asJsonObject, this);
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   private static List<Field> register(Class<?> clazz) {
      LinkedHashSet linkedHashSet = new LinkedHashSet<>(Arrays.asList(clazz.getFields()));
      linkedHashSet.addAll(Arrays.asList(clazz.getDeclaredFields()));
      return new ArrayList<>(linkedHashSet);
   }

   public static void performAction(JsonObject json, Object obj) throws Exception {
      for (Field field : register(obj.getClass())) {
         boolean flag = false;
         Object obj2;
         if (!(obj instanceof ConfigField) && !(obj instanceof Value)) {
            boolean isStatic = Modifier.isStatic(field.getModifiers());
            Object obj3 = isStatic ? null : obj;
            if (!field.canAccess(obj3) || Modifier.isTransient(field.getModifiers())) {
               field.setAccessible(true);
            }

            obj2 = field.get(obj3);
         } else {
            obj2 = obj;
            flag = true;
         }

         if (obj2 instanceof ConfigField configField) {
            String label = configField.getLabel();
            Class clz = configField.getClz();
            if (json == null) {
               if (label.equals("Enabled") && obj instanceof Module module) {
                  module.setEnabled((Boolean)configField.getDefaultValue());
                  continue;
               }

               configField.performAction();
               if (flag) {
                  break;
               }
               continue;
            }

            JsonElement element = json.get(label);
            if (element != null) {
               if (clz.isAssignableFrom(Boolean.class)) {
                  if (label.equals("Enabled") && obj instanceof Module module2) {
                     module2.setEnabled(element.getAsBoolean());
                  } else {
                     Boolean asBoolean = element.getAsBoolean();
                     configField.setValue(asBoolean);
                  }
               } else if (clz.isAssignableFrom(Character.class)) {
                  Character asCharacter = element.getAsCharacter();
                  configField.setValue(asCharacter);
               } else if (clz.isAssignableFrom(Short.class)) {
                  Short asShort = element.getAsShort();
                  configField.setValue(asShort);
               } else if (clz.isAssignableFrom(Byte.class)) {
                  Byte asByte = element.getAsByte();
                  configField.setValue(asByte);
               } else if (clz.isAssignableFrom(Integer.class)) {
                  Integer asInt = element.getAsInt();
                  configField.setValue(asInt);
               } else if (clz.isAssignableFrom(Float.class)) {
                  Float asFloat = element.getAsFloat();
                  configField.setValue(asFloat);
               } else if (clz.isAssignableFrom(Long.class)) {
                  Long asLong = element.getAsLong();
                  configField.setValue(asLong);
               } else if (clz.isAssignableFrom(Double.class)) {
                  Double asDouble = element.getAsDouble();
                  configField.setValue(asDouble);
               } else if (clz.isAssignableFrom(String.class)) {
                  configField.setValue(element.getAsString());
               } else if (clz.isAssignableFrom(HudPosition.class)) {
                  JsonObject asJsonObject = element.getAsJsonObject();
                  HudPosition hudPosition = (HudPosition)configField.getValue();
                  float asFloat2 = asJsonObject.get("x").getAsFloat();
                  float asFloat3 = asJsonObject.get("y").getAsFloat();
                  hudPosition = new HudPosition(hudPosition.getHud(), asFloat2, asFloat3, WindowMetrics.getScaledWidth(), WindowMetrics.getScaledHeight());
                  int n = Integer.parseInt(asJsonObject.get("anchor").getAsString());
                  hudPosition.anchor = Mode.values()[n];
                  configField.setValue(hudPosition);
               }
            }
         } else if (obj2 instanceof Value value) {
            String label2 = value.getLabel();
            if (json == null) {
               if (obj2 instanceof HudValue) {
                  performAction(null, value.getValue());
               } else if (obj2 instanceof PageValue) {
                  Object obj4 = value.getValue();
                  if (!(obj4 instanceof GuiPage)) {
                     performAction(null, value.getValue());
                  }
               } else {
                  value.performAction();
               }

               if (flag) {
                  break;
               }
               continue;
            }

            JsonElement element2 = json.get(label2);
            if (element2 != null) {
               if (obj2 instanceof HudValue) {
                  performAction(element2.getAsJsonObject(), value.getValue());
               } else if (obj2 instanceof PageValue) {
                  Object obj5 = value.getValue();
                  if (!(obj5 instanceof GuiPage)) {
                     performAction(element2.getAsJsonObject(), value.getValue());
                  }
               } else {
                  switch (value.getOptionType()) {
                     case SWITCH:
                     case CHECKBOX:
                     case DUAL_OPTION:
                        value.performAction(element2.getAsBoolean());
                        break;
                     case TEXT:
                        value.performAction(element2.getAsString());
                        break;
                     case SLIDER:
                     case NUMBER:
                        value.performAction(element2.getAsNumber());
                        break;
                     case COLOR:
                        JsonObject asJsonObject2 = element2.getAsJsonObject();
                        JsonArray asJsonArray = asJsonObject2.get("hsba").getAsJsonArray();
                        JsonPrimitive asJsonPrimitive = asJsonObject2.get("dataBit").getAsJsonPrimitive();
                        OneColor color = new OneColor(
                           asJsonArray.get(0).getAsNumber().intValue(),
                           asJsonArray.get(1).getAsNumber().intValue(),
                           asJsonArray.get(2).getAsNumber().intValue(),
                           asJsonArray.get(3).getAsNumber().intValue(),
                           asJsonPrimitive.getAsInt()
                        );
                        value.performAction(color);
                        break;
                     case DROPDOWN:
                        value.performAction(element2.getAsNumber().intValue());
                        break;
                     case KEYBIND:
                        Value value2 = value;
                        ((OneKeyBind)value.value).keyBinds.clear();
                        JsonObject asJsonObject3 = element2.getAsJsonObject();
                        JsonArray asJsonArray2 = asJsonObject3.get("keyBinds").getAsJsonArray();
                        int[] values = new int[asJsonArray2.size()];

                        for (int j = 0; j < asJsonArray2.size(); j++) {
                           values[j] = asJsonArray2.get(j).getAsNumber().intValue();
                        }

                        for (int n2 : values) {
                           ((OneKeyBind)value2.value).keyBinds.add(n2);
                        }

                        if (value2.basicOption != null) {
                           value2.basicOption.performAction();
                        }
                  }
               }
            }
         }

         if (flag) {
            break;
         }
      }
   }

   public static JsonObject toJson(JsonObject json, Object obj) throws Exception {
      for (Field field : register(obj.getClass())) {
         boolean flag = false;
         Object obj2;
         if (!(obj instanceof ConfigField) && !(obj instanceof Value)) {
            boolean isStatic = Modifier.isStatic(field.getModifiers());
            if (Modifier.isTransient(field.getModifiers())) {
               continue;
            }

            Object obj3 = isStatic ? null : obj;
            if (!field.canAccess(obj3)) {
               field.setAccessible(true);
            }

            obj2 = field.get(obj3);
         } else {
            obj2 = obj;
            flag = true;
         }

         if (obj2 instanceof ConfigField configField) {
            String label = configField.getLabel();
            Class clz = configField.getClz();
            Object obj4 = configField.getValue();
            if (obj4 != null) {
               if (clz.isAssignableFrom(Boolean.class)) {
                  json.addProperty(label, (Boolean)obj4);
               } else if (clz.isAssignableFrom(Character.class)) {
                  json.addProperty(label, (Character)obj4);
               } else if (clz.isAssignableFrom(Short.class)) {
                  json.addProperty(label, (Short)obj4);
               } else if (clz.isAssignableFrom(Byte.class)) {
                  json.addProperty(label, (Byte)obj4);
               } else if (clz.isAssignableFrom(Integer.class)) {
                  json.addProperty(label, (Integer)obj4);
               } else if (clz.isAssignableFrom(Float.class)) {
                  json.addProperty(label, (Float)obj4);
               } else if (clz.isAssignableFrom(Long.class)) {
                  json.addProperty(label, (Long)obj4);
               } else if (clz.isAssignableFrom(Double.class)) {
                  json.addProperty(label, (Double)obj4);
               } else if (clz.isAssignableFrom(String.class)) {
                  json.addProperty(label, (String)obj4);
               } else if (clz.isAssignableFrom(HudPosition.class)) {
                  HudPosition hudPosition = (HudPosition)obj4;
                  JsonObject json2 = new JsonObject();
                  json2.addProperty("anchor", String.valueOf(hudPosition.anchor.ordinal()));
                  json2.addProperty("x", hudPosition.x);
                  json2.addProperty("y", hudPosition.y);
                  json.add(label, json2);
               }
            }
         } else if (obj2 instanceof Value value) {
            String label2 = value.getLabel();
            Object obj5 = value.getValue();
            if (obj5 != null) {
               if (obj2 instanceof HudValue) {
                  json.add(label2, toJson(new JsonObject(), obj5));
               } else if (obj2 instanceof PageValue) {
                  Object obj6 = value.getValue();
                  if (!(obj6 instanceof GuiPage)) {
                     json.add(label2, toJson(new JsonObject(), obj5));
                  }
               } else {
                  switch (value.getOptionType()) {
                     case SWITCH:
                     case CHECKBOX:
                     case DUAL_OPTION:
                        json.addProperty(label2, (Boolean)obj5);
                        break;
                     case TEXT:
                        json.addProperty(label2, (String)obj5);
                        break;
                     case SLIDER:
                     case NUMBER:
                        json.addProperty(label2, (Number)obj5);
                        break;
                     case COLOR:
                        OneColor color = (OneColor)obj5;
                        JsonObject json3 = new JsonObject();
                        JsonArray array = new JsonArray();

                        for (short s : color.getHsba()) {
                           array.add(s);
                        }

                        json3.add("hsba", array);
                        json3.addProperty("dataBit", color.dataBit);
                        json.add(label2, json3);
                        break;
                     case DROPDOWN:
                        json.addProperty(label2, (Integer)obj5);
                        break;
                     case KEYBIND:
                        OneKeyBind oneKeyBind = (OneKeyBind)obj5;
                        JsonObject json4 = new JsonObject();
                        JsonArray array2 = new JsonArray();

                        for (Integer integer : oneKeyBind.keyBinds) {
                           array2.add(integer);
                        }

                        json4.add("keyBinds", array2);
                        json.add(label2, json4);
                  }
               }
            }
         }

         if (flag) {
            break;
         }
      }

      return json;
   }

   public final ArrayList<BasicOption> addDependency(Object obj, OptionPage optionPage, ModData modData2) {
      return this.collectItems(new ArrayList<>(), obj, obj.getClass(), optionPage, modData2);
   }

   public final ArrayList<BasicOption> collectItems(ArrayList<BasicOption> list, Object obj, Class<?> clazz, OptionPage optionPage, ModData modData2, Object... objArray) {
      this.LOGGER.trace("Generating option list for {}... (targetting={})", modData2.name, clazz.getName());
      Class superclass = clazz.getSuperclass();
      if (superclass != Object.class) {
         list.addAll(this.collectItems(new ArrayList<>(), obj, superclass, optionPage, modData2, objArray));
      }

      for (Field field : clazz.getDeclaredFields()) {
         boolean isStatic = Modifier.isStatic(field.getModifiers());
         Object obj2 = isStatic ? null : obj;
         if (!field.canAccess(obj2)) {
            field.setAccessible(true);
         }

         Object obj3;
         try {
            obj3 = field.get(obj2);
         } catch (IllegalAccessException e) {
            e.printStackTrace();
            continue;
         }

         if (obj3 instanceof Value value) {
            if (obj3 instanceof HudValue hudValue) {
               ((Hud)hudValue.getValue()).position.getValue().setHud((Hud)hudValue.getValue());
               ((Hud)hudValue.getValue()).setConfig(this);
               HudCore.hudMap.put(hudValue, (Hud)hudValue.getValue());
               ArrayList list2 = new ArrayList();
               list2.add(new ConfigHeaderOption(null, null, hudValue.getLabel(), hudValue.getA(), hudValue.getDisplayText(), 2));
               list2 = this.collectItems(list2, hudValue.getValue(), ((Hud)hudValue.getValue()).getClass(), optionPage, modData2, hudValue.getA(), hudValue.getDisplayText());
               HudCore.basicOptions.addAll(list2);
               if (hudValue.getValue() instanceof BasicHud basicHud) {
                  try {
                     if (((Number)basicHud.paddingX.getValue()).floatValue() > 10.0F) {
                        basicHud.paddingX.performAction(5.0F);
                     }

                     if (((Number)basicHud.paddingY.getValue()).floatValue() > 10.0F) {
                        basicHud.paddingY.performAction(5.0F);
                     }
                  } catch (Exception e2) {
                     e2.printStackTrace();
                  }
               }

               ConfigUtils.getNameUtil(optionPage, value.getA(), value.getDisplayText()).options.addAll(list2);
            } else if (obj3 instanceof PageValue pageValue) {
               OptionSubcategory nameUtil = ConfigUtils.getNameUtil(optionPage, pageValue.getA(), pageValue.getDisplayText());
               Object obj4 = pageValue.getValue();
               if (obj4 != null) {
                  ConfigPageOption configPageOption;
                  if (obj4 instanceof GuiPage guiPage) {
                     configPageOption = new ConfigPageOption(field, obj, pageValue.getLabel(), pageValue.getDescription(), pageValue.getA(), pageValue.getDisplayText(), guiPage);
                  } else {
                     OptionPage optionPage2 = new OptionPage(pageValue.getLabel(), modData2);
                     this.addDependency(obj4, optionPage2, modData2);
                     configPageOption = new ConfigPageOption(field, obj, pageValue.getLabel(), pageValue.getDescription(), pageValue.getA(), pageValue.getDisplayText(), optionPage2);
                  }

                  if (pageValue.getLocation() == PageLocation.TOP) {
                     nameUtil.topButtons.add(configPageOption);
                  } else {
                     nameUtil.bottomButtons.add(configPageOption);
                  }
               }
            } else {
               BasicOption option;
               if (objArray.length == 2) {
                  option = ConfigUtils.getBasicOption(value, (String)objArray[0], (String)objArray[1]);
               } else {
                  option = ConfigUtils.getBasicOption(optionPage, value);
               }

               this.optionElements.put(value.getLabel(), option);
               list.add(option);
            }
         }
      }

      this.LOGGER.trace("Finished generating option list for {} (targetting={})", modData2.name, clazz.getName());
      return list;
   }

   public GsonBuilder getGsonBuilder(GsonBuilder gsonBuilder) {
      return gsonBuilder.excludeFieldsWithModifiers(new int[]{(int)i}).setPrettyPrinting();
   }

   public void openGui() {
      if (this.modData != null) {
         GuiUtils.openScreen(new OneConfigGui(new ModConfigPage(this.modData.defaultPage)));
      }
   }

   public final void addDependency(String text, String text2, Supplier<Boolean> supplier) {
      if (this.optionElements.containsKey(text)) {
         BasicOption option = this.optionElements.get(text);
         option.applyText(text2, supplier);
      }
   }

   public final void addDependency(String text, Supplier<Boolean> supplier) {
      if (this.optionElements.containsKey(text)) {
         this.optionElements.get(text).performAction(supplier);
      }
   }

   public final void addDependency(String text, String text2) {
      if (this.optionElements.containsKey(text) && this.optionElements.containsKey(text2)) {
         BasicOption option = this.optionElements.get(text2);
         this.optionElements.get(text).applyText(option.name, () -> {
            try {
               return (Boolean)option.resolveValue();
            } catch (IllegalAccessException e) {
               return true;
            }
         });
      }
   }

   @Deprecated
   public final void addDependency(String text, boolean flag) {
      if (this.optionElements.containsKey(text)) {
         this.optionElements.get(text).performAction(() -> flag);
      }
   }

   public final void hideIf(String text, Supplier<Boolean> supplier) {
      if (this.optionElements.containsKey(text)) {
         this.optionElements.get(text).performActionEx(supplier);
      }
   }

   public final void hideIf(String text, String text2) {
      if (this.optionElements.containsKey(text) && this.optionElements.containsKey(text2)) {
         this.optionElements.get(text).performActionEx(() -> {
            try {
               return (Boolean)this.optionElements.get(text2).resolveValue();
            } catch (IllegalAccessException e) {
               return true;
            }
         });
      }
   }

   @Deprecated
   public final void hideIf(String text, boolean flag) {
      if (this.optionElements.containsKey(text)) {
         this.optionElements.get(text).performActionEx(() -> flag);
      }
   }

   public final void addListener(String text, Runnable action) {
      if (this.optionElements.containsKey(text)) {
         this.optionElements.get(text).runTask(action);
      }
   }

   public final void registerKeyBind(OneKeyBind oneKeyBind, Runnable action) {
      Field field = null;
      Object obj = null;

      for (BasicOption option : this.optionElements.values()) {
         if (option instanceof ConfigKeyBindOption) {
            try {
               Field field2 = option.getField();
               OneKeyBind oneKeyBind2 = (OneKeyBind)option.resolveValue();
               if (oneKeyBind2 == oneKeyBind) {
                  field = field2;
                  obj = option.getParent();
                  break;
               }
            } catch (IllegalAccessException e) {
            }
         }
      }

      oneKeyBind.setRunnable(action);
      KeyBindHandler.INSTANCE.performAction(field, obj, oneKeyBind);
   }

   public boolean isValid() {
      return true;
   }

   @Experimental
   @Contract("null -> null")
   public static ModData register(ModData modData2) {
      if (modData2 == null) {
         return null;
      }

      if (ConfigCore.modDatas.contains(modData2)) {
         return modData2;
      }

      ConfigCore.modDatas.add(modData2);
      ConfigCore.performActionImpl();
      return null;
   }

   public final void performActionEx() {
   }

}
