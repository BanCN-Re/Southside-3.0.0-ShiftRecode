package dev.southside.command.args;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.util.InputUtil;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class KeybindingArgumentType implements ArgumentType<KeybindingArgumentType.Entry> {
   public static final DynamicCommandExceptionType INVALID_KEY;
   public static final String[] DEFAULT_NAMES;
   public static final Collection<String> EXAMPLES;

   public Collection<String> getExamples() {
      return EXAMPLES;
   }

   public KeybindingArgumentType.Entry parse(StringReader reader) throws CommandSyntaxException {
      String text = reader.canRead() ? reader.readString() : "";
      if (text.equalsIgnoreCase("none")) {
         return new KeybindingArgumentType.Entry(-1, "none");
      }

      try {
         MethodHandle methodHandle = MethodHandles.publicLookup().findStaticGetter(GLFW.class, "GLFW_KEY_" + text.toUpperCase(Locale.ENGLISH), int.class);
         int n = (int)methodHandle.invoke();
         return new KeybindingArgumentType.Entry(n, InputUtil.fromKeyCode(n, 0).getLocalizedText().getString());
      } catch (Throwable e) {
         throw INVALID_KEY.createWithContext(reader, text);
      }
   }

   public static KeybindingArgumentType.Entry getKeybinding(CommandContext<?> ctx, String text) {
      return (KeybindingArgumentType.Entry)ctx.getArgument(text, KeybindingArgumentType.Entry.class);
   }

   public static Message getMessage(Object obj) {
      return Text.literal("Invalid key: " + obj);
   }

   public static KeybindingArgumentType keybinding() {
      return new KeybindingArgumentType();
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> ctx, SuggestionsBuilder builder) {
      return CommandSource.suggestMatching(DEFAULT_NAMES, builder);
   }

   static {
      INVALID_KEY = new DynamicCommandExceptionType(KeybindingArgumentType::getMessage);
      String[] textArray = new String[122];
      textArray[0] = "space";
      textArray[1] = "apostrophe";
      textArray[2] = "comma";
      textArray[3] = "minus";
      textArray[4] = "period";
      textArray[5] = "slash";
      textArray[6] = "0";
      textArray[7] = "1";
      textArray[8] = "2";
      textArray[9] = "3";
      textArray[10] = "4";
      textArray[11] = "5";
      textArray[12] = "6";
      textArray[13] = "7";
      textArray[14] = "8";
      textArray[15] = "9";
      textArray[16] = "semicolon";
      textArray[17] = "equal";
      textArray[18] = "a";
      textArray[19] = "b";
      textArray[20] = "c";
      textArray[21] = "d";
      textArray[22] = "e";
      textArray[23] = "f";
      textArray[24] = "g";
      textArray[25] = "h";
      textArray[26] = "i";
      textArray[27] = "j";
      textArray[28] = "k";
      textArray[29] = "l";
      textArray[30] = "m";
      textArray[31] = "n";
      textArray[32] = "o";
      textArray[33] = "p";
      textArray[34] = "q";
      textArray[35] = "r";
      textArray[36] = "s";
      textArray[37] = "t";
      textArray[38] = "u";
      textArray[39] = "v";
      textArray[40] = "w";
      textArray[41] = "x";
      textArray[42] = "y";
      textArray[43] = "z";
      textArray[44] = "left_bracket";
      textArray[45] = "backslash";
      textArray[46] = "right_bracket";
      textArray[47] = "grave_accent";
      textArray[48] = "world_1";
      textArray[49] = "world_2";
      textArray[50] = "escape";
      textArray[51] = "enter";
      textArray[52] = "tab";
      textArray[53] = "backspace";
      textArray[54] = "insert";
      textArray[55] = "delete";
      textArray[56] = "right";
      textArray[57] = "left";
      textArray[58] = "down";
      textArray[59] = "up";
      textArray[60] = "page_up";
      textArray[61] = "page_down";
      textArray[62] = "home";
      textArray[63] = "end";
      textArray[64] = "caps_lock";
      textArray[65] = "scroll_lock";
      textArray[66] = "num_lock";
      textArray[67] = "print_screen";
      textArray[68] = "pause";
      textArray[69] = "f1";
      textArray[70] = "f2";
      textArray[71] = "f3";
      textArray[72] = "f4";
      textArray[73] = "f5";
      textArray[74] = "f6";
      textArray[75] = "f7";
      textArray[76] = "f8";
      textArray[77] = "f9";
      textArray[78] = "f10";
      textArray[79] = "f11";
      textArray[80] = "f12";
      textArray[81] = "f13";
      textArray[82] = "f14";
      textArray[83] = "f15";
      textArray[84] = "f16";
      textArray[85] = "f17";
      textArray[86] = "f18";
      textArray[87] = "f19";
      textArray[88] = "f20";
      textArray[89] = "f21";
      textArray[90] = "f22";
      textArray[91] = "f23";
      textArray[92] = "f24";
      textArray[93] = "f25";
      textArray[94] = "kp_0";
      textArray[95] = "kp_1";
      textArray[96] = "kp_2";
      textArray[97] = "kp_3";
      textArray[98] = "kp_4";
      textArray[99] = "kp_5";
      textArray[100] = "kp_6";
      textArray[101] = "kp_7";
      textArray[102] = "kp_8";
      textArray[103] = "kp_9";
      textArray[104] = "kp_decimal";
      textArray[105] = "kp_divide";
      textArray[106] = "kp_multiply";
      textArray[107] = "kp_subtract";
      textArray[108] = "kp_add";
      textArray[109] = "kp_enter";
      textArray[110] = "kp_equal";
      textArray[111] = "left_shift";
      textArray[112] = "left_control";
      textArray[113] = "left_alt";
      textArray[114] = "left_super";
      textArray[115] = "right_shift";
      textArray[116] = "right_control";
      textArray[117] = "right_alt";
      textArray[118] = "right_super";
      textArray[119] = "menu";
      textArray[120] = "last";
      textArray[121] = "none";
      DEFAULT_NAMES = textArray;
      String[] textArray2 = new String[6];
      textArray2[0] = "space";
      textArray2[1] = "right_shift";
      textArray2[2] = "a";
      textArray2[3] = "b";
      textArray2[4] = "1";
      textArray2[5] = "2";
      EXAMPLES = Arrays.asList(textArray2);
   }


   public record Entry(int keyCode, String keyName) {
      public int getKeyCode() {
         return this.keyCode;
      }

      public String getKeyName() {
         return this.keyName;
      }
   }
}
