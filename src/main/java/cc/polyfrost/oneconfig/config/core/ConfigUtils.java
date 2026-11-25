package cc.polyfrost.oneconfig.config.core;

import cc.polyfrost.oneconfig.config.data.OptionType;
import cc.polyfrost.oneconfig.config.elements.BasicOption;
import cc.polyfrost.oneconfig.internal.config.profiles.Profiles;
import com.google.gson.FieldAttributes;
import dev.southside.module.values.BooleanValue;
import dev.southside.module.values.Value;
import dev.southside.utils.ConfigButtonOption;
import dev.southside.utils.ConfigKeyBindOption;
import dev.southside.utils.ConfigCheckboxOption;
import dev.southside.utils.ConfigSwitchOption;
import dev.southside.utils.ConfigReflectionException;
import dev.southside.utils.ConfigHeaderOption;
import dev.southside.utils.KeyBindValue;
import dev.southside.utils.ColorValue;
import dev.southside.utils.CheckboxValue;
import dev.southside.utils.ConfigColorOption;
import dev.southside.utils.ConfigNumberOption;
import dev.southside.utils.ConfigSliderOption;
import dev.southside.utils.ConfigDualOption;
import dev.southside.utils.DualBooleanValue;
import dev.southside.utils.NumberValue;
import dev.southside.utils.IntegerValue;
import dev.southside.utils.OptionSubcategory;
import dev.southside.utils.OptionPage;
import dev.southside.utils.ModeValue;
import dev.southside.utils.ConfigDropdownOption;
import dev.southside.utils.StringValue;
import dev.southside.utils.ConfigTextOption;
import dev.southside.utils.HeaderValue;
import dev.southside.utils.OptionCategory;
import dev.southside.utils.ButtonValue;
import dev.southside.utils.ConfigInfoOption;
import dev.southside.utils.InfoValue;
import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;

public class ConfigUtils {
   private static final Field field = getField();

   private static Field getField() {
      try {
         return Value.class.getField("value");
      } catch (NoSuchFieldException e) {
         throw new ExceptionInInitializerError(e);
      }
   }

   public static File getFile(String text) {
      return Profiles.getFile2(text);
   }

   public static BasicOption getBasicOption(OptionPage optionPage, Value<?> value) {
      BasicOption basicOption = getBasicOption(value);
      if (basicOption == null) {
         return null;
      }

      getNameUtil(optionPage, basicOption.category, basicOption.subcategory).options.add(basicOption);
      return basicOption;
   }

   public static ArrayList<Field> collectItems(Class<?> clazz) {
      ArrayList list = new ArrayList<>(Arrays.asList(clazz.getDeclaredFields()));

      for (Class i = clazz.getSuperclass(); i != null; i = i.getSuperclass()) {
         list.addAll(Arrays.asList(i.getDeclaredFields()));
      }

      return list;
   }

   public static <T extends Annotation> T getAnnotation(FieldAttributes fieldAttributes, Class<T> clazz) {
      Annotation annotation = fieldAttributes.getAnnotation(clazz);
      if (annotation != null) {
         return (T)annotation;
      }

      for (Annotation annotation2 : fieldAttributes.getAnnotations()) {
         if (annotation2.annotationType().isAnnotationPresent(clazz)) {
            return annotation2.annotationType().getAnnotation(clazz);
         }
      }

      return null;
   }

   public static <T extends Annotation> T getAnnotation(Method method, Class<T> clazz) {
      if (method.isAnnotationPresent(clazz)) {
         return method.getAnnotation(clazz);
      }

      for (Annotation annotation : method.getDeclaredAnnotations()) {
         if (annotation.annotationType().isAnnotationPresent(clazz)) {
            return annotation.annotationType().getAnnotation(clazz);
         }
      }

      return null;
   }

   public static BasicOption getBasicOption(Value<?> value) {
      return getBasicOption(value, value.getA(), value.getDisplayText());
   }

   public static void performAction(Field field2, Object obj, Object obj2) {
      try {
         field2.setAccessible(true);
         field2.set(obj2, obj);
      } catch (Exception e) {
      }
   }

   public static void applyText(String text, Field field2, Class<?>... clazzArray) {
      for (Class clazz : clazzArray) {
         if (clazz.isAssignableFrom(field2.getType())) {
            return;
         }
      }

      String name = field2.getName();
      String name2 = field2.getDeclaringClass().getName();
      String text2 = Arrays.toString(clazzArray);
      String text3 = String.valueOf(field2.getType());
      String text4 = text3;
      String text5 = text2;
      String text6 = text;
      String text7 = name2;
      String text8 = name;
      throw new ConfigReflectionException(
         "Field " + text8 + " in config " + text7 + " is annotated as a " + text6 + ", but is not of valid type, expected " + text5 + " (found " + text4 + ")"
      );
   }

   public static Object resolveValue(Field field2, Object obj) {
      try {
         field2.setAccessible(true);
         return field2.get(obj);
      } catch (Exception e) {
         return null;
      }
   }

   public static ArrayList<BasicOption> collectItems(Object obj) {
      ArrayList list = new ArrayList();

      for (Field field2 : collectItems(obj.getClass())) {
         Object obj2 = Modifier.isStatic(field2.getModifiers()) ? null : obj;
         if (field2.canAccess(obj2)) {
            try {
               if (field2.get(obj2) instanceof Value value) {
                  list.add(getBasicOption(value));
               }
            } catch (IllegalAccessException e) {
            }
         }
      }

      return list;
   }

   public static BasicOption getBasicOption(Value<?> value, String text, String text2) {
      Field field2 = field;
      switch (value.getOptionType()) {
         case SWITCH:
            BooleanValue booleanValue = (BooleanValue)value;
            return new ConfigSwitchOption(field2, value, booleanValue.getLabel(), booleanValue.getDescription(), text, text2, booleanValue.getSize());
         case CHECKBOX:
            CheckboxValue checkboxValue = (CheckboxValue)value;
            return new ConfigCheckboxOption(field2, value, checkboxValue.getLabel(), checkboxValue.getDescription(), text, text2, checkboxValue.getSize());
         case INFO:
            InfoValue infoValue = (InfoValue)value;
            return new ConfigInfoOption(field2, value, (String)infoValue.getDefaultValue(), text, text2, infoValue.getSize(), infoValue.getType());
         case HEADER:
            HeaderValue headerValue = (HeaderValue)value;
            return new ConfigHeaderOption(field2, value, (String)headerValue.getValue(), text, text2, headerValue.getSize());
         case COLOR:
            ColorValue colorValue = (ColorValue)value;
            return new ConfigColorOption(field2, value, colorValue.getLabel(), colorValue.getDescription(), text, text2, colorValue.getSize(), colorValue.isAllowAlpha());
         case DROPDOWN:
            ModeValue modeValue = (ModeValue)value;
            return new ConfigDropdownOption(field2, value, modeValue.getLabel(), modeValue.getDescription(), text, text2, modeValue.getSize(), modeValue.getOptions());
         case TEXT:
            StringValue stringValue = (StringValue)value;
            String label = stringValue.getLabel();
            String description = stringValue.getDescription();
            int n = !stringValue.isSecure() && !stringValue.isMultiline() ? stringValue.getSize() : 2;
            String placeholder = stringValue.getPlaceholder();
            boolean isMultiline = stringValue.isMultiline();
            boolean isSecure = stringValue.isSecure();
            return new ConfigTextOption(field2, value, label, description, text, text2, n, placeholder, isSecure, isMultiline);
         case BUTTON:
            ButtonValue buttonValue = (ButtonValue)value;
            return new ConfigButtonOption((Runnable)buttonValue.getValue(), value, buttonValue.getLabel(), buttonValue.getDescription(), text, text2, buttonValue.getSize(), buttonValue.getTextEx());
         case SLIDER:
            NumberValue numberValue = (NumberValue)value;
            return new ConfigSliderOption(
               field2,
               value,
               numberValue.getLabel(),
               numberValue.getDescription(),
               text,
               text2,
               numberValue.getMin(),
               numberValue.getMax(),
               numberValue.getStep(),
               numberValue.isInstant(),
               numberValue.getIncrement()
            );
         case NUMBER:
            IntegerValue integerValue = (IntegerValue)value;
            return new ConfigNumberOption(
               field2, value, integerValue.getLabel(), integerValue.getDescription(), text, text2, integerValue.getMin(), integerValue.getMax(), integerValue.getStep(), integerValue.getSize()
            );
         case KEYBIND:
            KeyBindValue keyBindValue = (KeyBindValue)value;
            return new ConfigKeyBindOption(field2, value, keyBindValue.getLabel(), keyBindValue.getDescription(), text, text2, keyBindValue.getSize());
         case DUAL_OPTION:
            DualBooleanValue dualBooleanValue = (DualBooleanValue)value;
            return new ConfigDualOption(field2, value, dualBooleanValue.getLabel(), dualBooleanValue.getDescription(), text, text2, dualBooleanValue.getSize(), dualBooleanValue.getLeft(), dualBooleanValue.getRight());
         default:
            return null;
      }
   }

   public static <T extends Annotation> T getAnnotation(Class<?> clazz, Class<T> clazz2) {
      if (clazz.isAnnotationPresent(clazz2)) {
         return clazz.getAnnotation(clazz2);
      }

      for (Annotation annotation : clazz.getDeclaredAnnotations()) {
         if (annotation.annotationType().isAnnotationPresent(clazz2)) {
            return annotation.annotationType().getAnnotation(clazz2);
         }
      }

      return null;
   }

   public static File getFile() {
      return Profiles.getFile();
   }

   public static OptionSubcategory getNameUtil(OptionPage optionPage, String text, String text2) {
      if (!optionPage.categories.containsKey(text)) {
         optionPage.categories.put(text, new OptionCategory());
      }

      OptionCategory optionCategory = optionPage.categories.get(text);
      OptionSubcategory optionSubcategory = optionCategory.subcategories.stream().filter(optionSubcategory2 -> optionSubcategory2.getName().equals(text2)).findFirst().orElse(null);
      if (optionCategory.subcategories.isEmpty() || optionSubcategory == null) {
         optionSubcategory = new OptionSubcategory(text2, text);
         optionCategory.subcategories.add(optionSubcategory);
      }

      return optionSubcategory;
   }

   public static <T extends Annotation> T getAnnotation(Field field2, Class<T> clazz) {
      if (field2.isAnnotationPresent(clazz)) {
         return field2.getAnnotation(clazz);
      }

      for (Annotation annotation : field2.getDeclaredAnnotations()) {
         if (annotation.annotationType().isAnnotationPresent(clazz)) {
            return annotation.annotationType().getAnnotation(clazz);
         }
      }

      return null;
   }

   public static String getDisplayText() {
      return Profiles.getDisplayText();
   }
}
