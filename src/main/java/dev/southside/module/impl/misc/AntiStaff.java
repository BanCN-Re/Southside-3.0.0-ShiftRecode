package dev.southside.module.impl.misc;

import com.google.gson.annotations.SerializedName;
import dev.southside.event.EventTarget;
import dev.southside.event.events.DirectionEvent;
import dev.southside.event.events.EntityAddEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.values.BooleanValue;
import dev.southside.utils.IText;
import dev.southside.utils.StringValue;
import java.util.Set;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.TeamS2CPacket;
import net.minecraft.text.Text;

public class AntiStaff extends Module {
   private static final Set<String> stringsSet;
   @SerializedName("autoHub")
   public final BooleanValue autoHub = new BooleanValue("Auto Hub", false);
   @SerializedName("hubCommand")
   public final StringValue hubCommand = new StringValue("Hub Command", "返回大厅的命令", false, false, "", 1, "/hub");

   public AntiStaff() {
      super(Category.Misc, "AntiStaff");
   }

   @EventTarget
   public void onEntityEvent3(EntityAddEvent entityAddEvent) {
      Text displayName = entityAddEvent.entity.getDisplayName();
      if (displayName != null && stringsSet.contains(displayName.getString())) {
         this.performActionAlt();
      }
   }

   @EventTarget
   public void onDirection(DirectionEvent directionEvent) {
      if (directionEvent.getPacket() instanceof TeamS2CPacket teamS2CPacket) {
         for (String text : teamS2CPacket.getPlayerNames()) {
            if (text != null && stringsSet.contains(text)) {
               this.performActionAlt();
            }
         }
      }
   }

   private void performActionAlt() {
      for (int i = 0; i < 3; i++) {
         IText.performActionEx(Text.literal("Staff Detected!"));
      }

      if ((Boolean)this.autoHub.getValue() && mc2.player != null) {
         ClientPlayNetworkHandler networkHandler = mc2.getNetworkHandler();
         if (networkHandler != null) {
            String text = ((String)this.hubCommand.getValue()).trim();
            if (!text.isEmpty()) {
               IText.performActionEx(Text.literal("Auto Hub → " + text));
               if (text.startsWith("/")) {
                  networkHandler.sendCommand(text.substring(1));
               } else {
                  networkHandler.sendChatMessage(text);
               }
            }
         }
      }
   }

   // $VF: Irreducible bytecode was duplicated to produce valid code
   static {
      String[] textArray = new String[95];
      textArray[0] = "妖猫";
      textArray[1] = "Andrewkrist";
      textArray[2] = "BACs";
      textArray[3] = "CandyApostle";
      textArray[4] = "chunyi1";
      textArray[5] = "CuteXiaoYY";
      textArray[6] = "Davidsun";
      textArray[7] = "叼口仙气就飘飘";
      textArray[8] = "东海林皇";
      textArray[9] = "抖音_awa马原";
      textArray[10] = "斗战胜佛";
      textArray[11] = "枫萧林然";
      textArray[12] = "Fia9";
      textArray[13] = "Fruit_Candy";
      textArray[14] = "广告君A";
      textArray[15] = "呱太";
      textArray[16] = "Henglie";
      textArray[17] = "LamMolincen_";
      textArray[18] = "Jay__Chou";
      textArray[19] = "Kiana__24";
      textArray[20] = "枕上书、塑望月";
      textArray[21] = "流影只会嘤嘤嘤";
      textArray[22] = "绿豆奶";
      textArray[23] = "Magician";
      textArray[24] = "mc加载";
      textArray[25] = "MnamLeo_";
      textArray[26] = "Kuri_Turgut";
      textArray[27] = "七鹤さん";
      textArray[28] = "Qiiiiiii";
      textArray[29] = "qxtmlc99";
      textArray[30] = "圣上荣耀233";
      textArray[31] = "神坑之逗";
      textArray[32] = "霜月月OO";
      textArray[33] = "Skyfoy";
      textArray[34] = "Stellarcat";
      textArray[35] = "倘若两散不如两难";
      textArray[36] = "TempMono";
      textArray[37] = "无量域雪";
      textArray[38] = "小匪";
      textArray[39] = "小H";
      textArray[40] = "小军君、天使之翼";
      textArray[41] = "xPir4te_";
      textArray[42] = "虚空";
      textArray[43] = "枕上书、傲寒";
      textArray[44] = "枕上书、雪夜";
      textArray[45] = "枕上书、中二少年DL";
      textArray[46] = "叕口仙气就飘飘";
      textArray[47] = "咩太";
      textArray[48] = "枕上书丶塑望月";
      textArray[49] = "圣上荣光233";
      textArray[50] = "小军君丶天使之翼";
      textArray[51] = "枕上书丶傲寒";
      textArray[52] = "枕上书丶雪夜";
      textArray[53] = "枕上书丶中二少年DL";
      textArray[54] = "绿豆乃SAMA";
      textArray[55] = "nightbary";
      textArray[56] = "体贴的炼金术雀";
      textArray[57] = "StarNO1";
      textArray[58] = "小妖猫";
      textArray[59] = "妖猫的PC号";
      textArray[60] = "小H修bug";
      textArray[61] = "xiaotufei";
      textArray[62] = "元宵";
      textArray[63] = "CuteGirlQlQl";
      textArray[64] = "彩笔";
      textArray[65] = "布吉岛打工仔";
      textArray[66] = "元宵的测试号";
      textArray[67] = "抑郁的元宵";
      textArray[68] = "元宵睡不醒";
      textArray[69] = "抖音丶小匪";
      textArray[70] = "练书法的苦力怕";
      textArray[71] = "KiKiAman";
      textArray[72] = "WS故";
      textArray[73] = "彩笔qwq";
      textArray[74] = "管理员-1";
      textArray[75] = "管理员-2";
      textArray[76] = "管理员-3";
      textArray[77] = "管理员-4";
      textArray[78] = "管理员-5";
      textArray[79] = "管理员-6";
      textArray[80] = "管理员-7";
      textArray[81] = "管理员-8";
      textArray[82] = "管理员-9";
      textArray[83] = "管理员-10";
      textArray[84] = "天使";
      textArray[85] = "艾米丽";
      textArray[86] = "可比不来嗯忑";
      textArray[87] = "鸡你太美";
      textArray[88] = "神伦子";
      textArray[89] = "马哥乐";
      textArray[90] = "巧克布丁";
      textArray[91] = "三国杀";
      textArray[92] = "枕上书丶塑塑月";
      textArray[93] = "中二少年DL";
      textArray[94] = "mengchen3884";
      stringsSet = Set.of(textArray);
   }
}
