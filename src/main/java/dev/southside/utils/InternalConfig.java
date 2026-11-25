package dev.southside.utils;

import cc.polyfrost.oneconfig.config.Config;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;
import dev.southside.Southside;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class InternalConfig extends Config {
   protected final transient Gson GSON = this.getGsonBuilder(new GsonBuilder()).create();
   @SerializedName("configFile")
   protected final File configFile;

   public InternalConfig(String text, String text2) {
      super(new ModData(text, null));
      this.configFile = Southside.FOLDER.toPath().resolve(text2).toFile();
   }

   @Override
   public void initialize() {
      if (this.configFile.exists()) {
         this.loadFromJson(null);
      } else {
         this.saveToJson();
      }

      this.addDependency(this, this.modData.defaultPage, this.modData);
      this.modData.config = this;
   }

   @Override
   public JsonObject saveToJson() {
      try {
         if (!this.configFile.exists()) {
            this.configFile.createNewFile();
         }
      } catch (IOException e) {
      }

      try (BufferedWriter bufferedWriter = Files.newBufferedWriter(this.configFile.toPath(), StandardCharsets.UTF_8)) {
         bufferedWriter.write(this.GSON.toJson(Config.toJson(new JsonObject(), this)));
      } catch (Exception e2) {
         e2.printStackTrace();
      }

      return new JsonObject();
   }

   @Override
   public void resetToDefaults() {
   }

   @Override
   public void loadFromJson(JsonObject json) {
      Path path = this.configFile.toPath();

      try (BufferedReader bufferedReader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
         JsonObject asJsonObject = JsonParser.parseReader(bufferedReader).getAsJsonObject();
         Config.performAction(asJsonObject, this);
      } catch (Exception e) {
         e.printStackTrace();
         File file = path.toFile();
         file.renameTo(new File(file.getParentFile(), file.getName() + ".corrupted"));
      }
   }
}
