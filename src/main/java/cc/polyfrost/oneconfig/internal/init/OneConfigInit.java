package cc.polyfrost.oneconfig.internal.init;

import java.io.File;

public class OneConfigInit {
   public static void performAction() {
      File file = new File("./OneConfig");
      file.mkdirs();
      new File(file, "configs").mkdirs();
   }
}
