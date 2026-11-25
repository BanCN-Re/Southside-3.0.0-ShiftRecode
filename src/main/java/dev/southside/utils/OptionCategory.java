package dev.southside.utils;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;

public class OptionCategory {
   @SerializedName("subcategories")
   public final ArrayList<OptionSubcategory> subcategories = new ArrayList<>();
}
