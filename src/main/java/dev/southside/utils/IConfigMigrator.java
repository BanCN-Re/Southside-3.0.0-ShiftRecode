package dev.southside.utils;

import java.lang.reflect.Field;
import org.jetbrains.annotations.Nullable;

public interface IConfigMigrator {
   @Nullable
   Object getValue(Field field, String text, String text2, String text3);
}
