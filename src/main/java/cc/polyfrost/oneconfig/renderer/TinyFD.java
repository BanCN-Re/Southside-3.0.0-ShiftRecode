package cc.polyfrost.oneconfig.renderer;

import dev.southside.utils.LwjglManager;
import java.io.File;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface TinyFD {
   String DEFAULT_TEXT = "question";
   String DEFAULT_TEXT_VALUE = "error";
   String DEFAULT_TEXT_DATA = "warning";
   String DEFAULT_TEXT_REF = "info";
   String DEFAULT_TEXT_PRIMARY = "ok";
   String DEFAULT_TEXT_SECONDARY = "okcancel";
   String DEFAULT_TEXT_TERTIARY = "yesno";
   String DEFAULT_TEXT_EXTRA = "yesnocancel";
   TinyFD INSTANCE = LwjglManager.INSTANCE.getTinyFD();

   File getFile2(@Nullable String text, @Nullable String text2, @Nullable String[] textArray, @Nullable String text3);

   @Nullable
   File getFile(@Nullable String text, @Nullable String text2, @Nullable String[] textArray, @Nullable String text3);

   File[] getFileArray(@Nullable String text, @Nullable String text2, @Nullable String[] textArray, @Nullable String text3);

   File openFolderSelector(@Nullable String text, @Nullable String text2);

   boolean showMessageBox(String text, String text2, @NotNull String text3, String text4, boolean flag);

   int showNotification(String text, String text2, String text3);
}
