package dev.southside.nel.open_nel;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;
import dev.southside.Southside;
import dev.southside.module.Module;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class GameCharacter {
   @SerializedName("name")
   public final String name;
   @SerializedName("file")
   public final File file;
   public static String label;

   public GameCharacter(String text, File file2) {
      this.name = text;
      this.file = file2;
   }

   public void performAction() {
      if (this.file.isFile()) {
         try (BufferedReader bufferedReader = new BufferedReader(new FileReader(this.file))) {
            JsonElement element = JsonParser.parseReader(bufferedReader);
            if (!(element instanceof JsonNull)) {
               JsonObject asJsonObject = element.getAsJsonObject();

               for (Module module : Southside.INSTANCE.getModuleManager().modules) {
                  if (asJsonObject.has(module.modData.name)) {
                     module.loadFromJson(asJsonObject);
                  }
               }

               Southside.INSTANCE.getModuleManager().performActionEx();
            }
         } catch (IOException e) {
            Southside.LOGGER.error("Unexpected error in ConfigFile", e);
         }
      }
   }

   public static void setLabel(String text) {
      label = text;
   }
}
