package cc.polyfrost.oneconfig.config.migration;

import cc.polyfrost.oneconfig.config.core.OneColor;
import com.google.gson.annotations.SerializedName;
import dev.southside.utils.VigilanceProperty;
import dev.southside.utils.IConfigMigrator;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;

public class VigilanceMigrator implements IConfigMigrator {
   private static final Pattern categoryPattern = Pattern.compile("\\[\"?(?<category>[^.\\[\\]\"]+)\"?(\\.\"?(?<subcategory>[^.\\[\\]\"]+)\"?)?]");
   private static final Pattern booleanPattern = Pattern.compile("\"?(?<name>[^\\s\"]+)\"? = (?<value>true|false)");
   private static final Pattern numberPattern = Pattern.compile("\"?(?<name>[^\\s\"]+)\"? = (?<value>[\\d.]+)");
   private static final Pattern stringPattern = Pattern.compile("\"?(?<name>[^\\s\"]+)\"? = \"(?<value>.+)\"");
   private static final Pattern colorPattern = Pattern.compile("\"?(?<name>[^\\s\"]+)\"? = \"(?<value>(\\d{1,3},){3}\\d{1,3})\"");
   @SerializedName("filePath")
   protected final String filePath;
   @SerializedName("values")
   protected HashMap<String, HashMap<String, HashMap<String, Object>>> values = null;
   @SerializedName("fileExists")
   protected final boolean fileExists;

   public VigilanceMigrator(String text) {
      this.filePath = text;
      this.fileExists = new File(text).exists();
   }

   @NotNull
   protected String parse(@NotNull String text) {
      return text.toLowerCase().replace(" ", "_");
   }

   protected void generateValues() {
      if (this.values == null) {
         this.values = new HashMap<>();
      }

      try (BufferedReader bufferedReader = new BufferedReader(new FileReader(this.filePath))) {
         String text = null;
         String text2 = null;

         String text3;
         while ((text3 = bufferedReader.readLine()) != null) {
            Matcher matcher = categoryPattern.matcher(text3);
            if (matcher.find()) {
               text = matcher.group("category");
               text2 = matcher.group("subcategory");
               if (!this.values.containsKey(text)) {
                  this.values.put(text, new HashMap<>());
               }

               if (!this.values.get(text).containsKey(text2)) {
                  this.values.get(text).put(text2, new HashMap<>());
               }
            } else if (text != null) {
               HashMap map = this.values.get(text).get(text2);
               Matcher matcher2 = booleanPattern.matcher(text3);
               if (matcher2.find()) {
                  map.put(matcher2.group("name"), Boolean.parseBoolean(matcher2.group("value")));
               } else {
                  Matcher matcher3 = numberPattern.matcher(text3);
                  if (matcher3.find()) {
                     String text4 = matcher3.group("value");
                     if (text4.contains(".")) {
                        map.put(matcher3.group("name"), Float.parseFloat(text4));
                     } else {
                        map.put(matcher3.group("name"), Integer.parseInt(text4));
                     }
                  } else {
                     Matcher matcher4 = colorPattern.matcher(text3);
                     if (matcher4.find()) {
                        String[] textArray = matcher4.group("value").split(",");
                        int[] values2 = new int[4];

                        for (int i = 0; i < 4; i++) {
                           values2[i] = Integer.parseInt(textArray[i]);
                        }

                        map.put(matcher4.group("name"), new OneColor(values2[0], values2[1], values2[2], values2[3]));
                     }

                     Matcher matcher5 = stringPattern.matcher(text3);
                     if (matcher5.find()) {
                        map.put(matcher5.group("name"), matcher5.group("value"));
                     }
                  }
               }
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Override
   public Object getValue(Field field, @NotNull String text, @NotNull String text2, String text3) {
      if (!this.fileExists) {
         return null;
      }

      if (this.values == null) {
         this.generateValues();
      }

      if (field.isAnnotationPresent(VigilanceProperty.class)) {
         VigilanceProperty annotation = field.getAnnotation(VigilanceProperty.class);
         text = annotation.name();
         text2 = annotation.category();
         text3 = annotation.subcategory();
      }

      text = this.parse(text);
      text2 = this.parse(text2);
      text3 = this.parse(text3);
      return this.values.getOrDefault(text2, new HashMap<>()).getOrDefault(text3, new HashMap<>()).getOrDefault(text, null);
   }
}
