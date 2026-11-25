package dev.southside.manager;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import dev.southside.Southside;
import dev.southside.module.Module;
import dev.southside.nel.open_nel.GameCharacter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ConfigManager {
   private static final String DEFAULT_TEXT = ".json";
   public static final File file = Southside.FOLDER.toPath().resolve("configs").toFile();
   @SerializedName("configs")
   public final List<GameCharacter> configs = new ArrayList<>();

   public ConfigManager() {
      if (!file.exists()) {
         file.mkdirs();
      }
   }

   public void applyText(String text) throws IOException {
      File file2 = new File(file, text + ".json");
      if (!file2.exists() || !file2.isFile()) {
         file2.createNewFile();
      }

      JsonObject json = new JsonObject();

      for (Module module : Southside.INSTANCE.getModuleManager().getModules()) {
         json.add(module.modData.name, module.toJson());
      }

      try (PrintWriter printWriter = new PrintWriter(new FileWriter(file2))) {
         printWriter.println(Southside.GSON.toJson(json));
      }

      this.performAction();
   }

   public void applyTextEx(String text) {
      File file2 = new File(file, text + ".json");
      if (file2.exists() && file2.isFile() && file2.delete()) {
         this.configs.removeIf(gameCharacter -> gameCharacter.file.getName().equals(text + ".json"));
      }
   }

   public void performAction() {
      this.configs.clear();

      try (Stream<Path> stream = Files.list(file.toPath())) {
         stream.forEach(this::performAction);
      } catch (IOException e) {
         Southside.LOGGER.error("Unexpected error in ConfigManager", e);
      }
   }

   private void performAction(Path path) {
      File file2 = path.toFile();
      String name = file2.getName();
      if (file2.isFile() && name.endsWith(".json")) {
         String text = name.substring(0, name.length() - ".json".length());
         this.configs.add(new GameCharacter(text, file2));
      }
   }

   public void applyTextAlt(String text) throws IOException {
      for (GameCharacter gameCharacter : this.configs) {
         if (gameCharacter.name.equals(text)) {
            gameCharacter.performAction();
            return;
         }
      }
   }

   public boolean matches(String text) {
      File file2 = new File(file, text + ".json");
      return file2.exists() && file2.isFile();
   }
}
