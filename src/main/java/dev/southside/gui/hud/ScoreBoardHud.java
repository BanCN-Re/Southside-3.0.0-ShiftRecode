package dev.southside.gui.hud;

import cc.polyfrost.oneconfig.hud.Hud;
import com.google.gson.annotations.SerializedName;
import dev.southside.Southside;
import dev.southside.mixin.hud.InGameHudAccessor;
import dev.southside.module.ModuleManager;
import dev.southside.module.impl.render.ScoreBoard;
import dev.southside.module.values.BooleanValue;
import dev.southside.render.UIMatrixStack;
import dev.southside.utils.NanoVGHelper;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardEntry;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.scoreboard.number.NumberFormat;
import net.minecraft.scoreboard.number.StyledNumberFormat;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class ScoreBoardHud extends Hud {
   private static final Text COLON_TEXT = Text.literal(": ");
   private static final float DEFAULT_FLOAT = 5.0F;
   private static final float DEFAULT_FLOAT_VALUE = 16.0F;
   private static final float DEFAULT_FLOAT_DATA = 9.0F;
   @SerializedName("noScore")
   public final BooleanValue noScore = new BooleanValue("No Score", true);
   private static final long DEFAULT_LONG = 15L;

   public ScoreBoardHud(float x, float y, float z) {
      super(x, y, 1, z);
   }

   private boolean isValidImpl() {
      ScoreBoard scoreBoard = this.getScoreBoard();
      return scoreBoard != null && scoreBoard.style.matches("Vanilla");
   }

   private boolean isValidDirect() {
      ScoreBoard scoreBoard = this.getScoreBoard();
      return scoreBoard != null && scoreBoard.style.matches("Simple");
   }

   private boolean isValidSafe() {
      return this.noScore == null || (Boolean)this.noScore.getValue();
   }

   private ScoreBoard getScoreBoard() {
      Southside southside = Southside.INSTANCE;
      return southside != null && southside.getModuleManager() != null
         ? southside.getModuleManager()
            .modules
            .stream()
            .filter(module -> ModuleManager.isValid(ScoreBoard.class, module))
            .map(ScoreBoard.class::cast)
            .findFirst()
            .orElse(null)
         : null;
   }

   @Override
   public float computeFloat(float f, boolean flag) {
      ScoreBoardHud.Entry2 entry2 = this.getEntry2(flag);
      return entry2 == null ? 0.0F : (entry2.getWidth() + (this.isValidImpl() ? 8.0F : 10.0F)) * f;
   }

   @Override
   public float setScale(float f, boolean flag) {
      ScoreBoardHud.Entry2 entry2 = this.getEntry2(flag);
      return entry2 == null ? 0.0F : computeFloat(entry2, this.isValidImpl()) * f;
   }

   private static float computeFloat(ScoreBoardHud.Entry2 entry2, boolean flag) {
      if (flag) {
         return Math.max((entry2.getLines().length + 1) * 9.0F, 13.0F);
      }

      float f = entry2.getLines().length > 0 ? entry2.getLines().length * 9.0F + 10.0F : 19.0F;
      return 16.0F + f;
   }

   @Override
   public void draw(DrawContext context, UIMatrixStack matrices, float f, float f2, float f3, boolean flag) {
      ScoreBoardHud.Entry2 entry2 = this.getEntry2(flag);
      if (entry2 != null) {
         boolean isValidImpl = this.isValidImpl();
         float f4 = entry2.getWidth() + (isValidImpl ? 8.0F : 10.0F);
         float f5 = computeFloat(entry2, isValidImpl);
         this.draw(f, f2, f4, f5, f3, isValidImpl);
         this.draw(context, entry2, f, f2, f3, isValidImpl);
      }
   }

   private void draw(float f, float f2, float f3, float f4, float f5, boolean flag) {
      NanoVGHelper nvg = NanoVGHelper.INSTANCE;
      if (flag) {
         nvg.performAction(true, l -> {
            nvg.drawRect(l, f, f2, f3 * f5, f4 * f5, 1342177280);
            nvg.drawRect(l, f, f2, f3 * f5, 9.0F * f5, 1613770800);
         });
      } else if (this.isValidDirect()) {
         nvg.performAction(true, l2 -> {
            nvg.drawRect(l2, f, f2, f3 * f5, f4 * f5, -1275068416);
            nvg.drawRect(l2, f, f2, f3 * f5, 16.0F * f5, 553648127);
         });
      } else {
         int textBackgroundColor = mc.options.getTextBackgroundColor(0.3F);
         int textBackgroundColor2 = mc.options.getTextBackgroundColor(0.4F);
         nvg.performAction(true, l3 -> {
            float f6 = 5.0F * f5;
            nvg.drawRoundedRect(l3, f, f2, f3 * f5, f4 * f5, textBackgroundColor, f6);
            nvg.drawRoundedRectVaried(l3, f, f2, f3 * f5, 16.0F * f5, textBackgroundColor2, f6, f6, 0.0F, 0.0F);
         });
      }
   }

   private void draw(DrawContext context, ScoreBoardHud.Entry2 entry2, float f, float f2, float f3, boolean flag) {
      MatrixStack matrices = context.getMatrices();
      matrices.push();
      matrices.translate(f, f2, 0.0F);
      matrices.scale(f3, f3, 1.0F);
      int n = flag ? 4 : 5;
      int n2 = flag ? (int)((entry2.getWidth() + 8.0F - entry2.getTitleWidth()) / 2.0F) : (int)(n + (entry2.getWidth() - entry2.getTitleWidth()) / 2.0F);
      context.drawText(mc.textRenderer, entry2.getTitle(), n2, flag ? 1 : 5, -1, false);
      float f4 = flag ? 9.0F : 21.0F;
      ScoreBoardHud.Entry[] lines = entry2.getLines();

      for (int i = 0; i < lines.length; i++) {
         ScoreBoardHud.Entry entry = lines[i];
         int n3 = (int)(f4 + i * 9.0F);
         context.drawText(mc.textRenderer, entry.getName(), n, n3, -1, false);
         this.render(context, entry2, entry, n, n3);
      }

      matrices.pop();
   }

   private void render(DrawContext context, ScoreBoardHud.Entry2 entry2, ScoreBoardHud.Entry entry, int n, int n2) {
      if (!this.isValidSafe() && entry.getScoreWidth() > 0) {
         int n3 = n + entry2.getWidth() - entry.getScoreWidth();
         context.drawText(mc.textRenderer, COLON_TEXT, n3 - entry2.getSeparatorWidth(), n2, -1, false);
         context.drawText(mc.textRenderer, entry.getScore(), n3, n2, -1, false);
      }
   }

   private ScoreBoardHud.Entry2 getEntry2(boolean flag) {
      ScoreboardObjective scoreboardObjective = this.getScoreboardObjective();
      InGameHudAccessor inGameHud = (InGameHudAccessor)mc.inGameHud;
      if (inGameHud == null) {
         return null;
      }

      TextRenderer textRenderer = inGameHud.invokeGetTextRenderer();
      if (scoreboardObjective == null) {
         if (!flag) {
            return null;
         }

         ScoreBoardHud.Entry[] entryArray = new ScoreBoardHud.Entry[]{
            getEntry(textRenderer, "Player 1", "12"), getEntry(textRenderer, "Player 2", "7"), getEntry(textRenderer, "Player 3", "3")
         };
         return this.getEntry2(Text.literal("Scoreboard"), entryArray, textRenderer);
      } else {
         return this.getEntry2(scoreboardObjective.getDisplayName(), this.getEntryArray(scoreboardObjective, textRenderer), textRenderer);
      }
   }

   private ScoreboardObjective getScoreboardObjective() {
      if (mc.world == null) {
         return null;
      }

      Scoreboard scoreboard = mc.world.getScoreboard();
      Team team = mc.player == null ? null : scoreboard.getScoreHolderTeam(mc.player.getNameForScoreboard());
      if (team != null) {
         ScoreboardDisplaySlot scoreboardDisplaySlot = ScoreboardDisplaySlot.fromFormatting(team.getColor());
         if (scoreboardDisplaySlot != null) {
            ScoreboardObjective objectiveForSlot = scoreboard.getObjectiveForSlot(scoreboardDisplaySlot);
            if (objectiveForSlot != null) {
               return objectiveForSlot;
            }
         }
      }

      return scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
   }

   private ScoreBoardHud.Entry[] getEntryArray(ScoreboardObjective scoreboardObjective, TextRenderer textRenderer) {
      NumberFormat numberFormatOr = scoreboardObjective.getNumberFormatOr(StyledNumberFormat.RED);
      if (mc.world == null) {
         return new ScoreBoardHud.Entry[0];
      }

      Scoreboard scoreboard = mc.world.getScoreboard();
      return scoreboard.getScoreboardEntries(scoreboardObjective)
         .stream()
         .filter(scoreboardEntry -> !scoreboardEntry.hidden())
         .sorted(InGameHudAccessor.getSCOREBOARD_ENTRY_COMPARATOR())
         .limit(DEFAULT_LONG)
         .map(scoreboardEntry2 -> getEntry(scoreboard, numberFormatOr, textRenderer, scoreboardEntry2))
         .toArray(ScoreBoardHud.Entry[]::new);
   }

   private static ScoreBoardHud.Entry getEntry(Scoreboard scoreboard, NumberFormat numberFormat, TextRenderer textRenderer, ScoreboardEntry scoreboardEntry) {
      Team scoreHolderTeam = scoreboard.getScoreHolderTeam(scoreboardEntry.owner());
      MutableText text = Team.decorateName(scoreHolderTeam, scoreboardEntry.name());
      MutableText text2 = scoreboardEntry.formatted(numberFormat);
      return new ScoreBoardHud.Entry(text, text2, textRenderer.getWidth(text2));
   }

   private static ScoreBoardHud.Entry getEntry(TextRenderer textRenderer, String text, String text2) {
      MutableText text3 = Text.literal(text2);
      return new ScoreBoardHud.Entry(Text.literal(text), text3, textRenderer.getWidth(text3));
   }

   private ScoreBoardHud.Entry2 getEntry2(Text text, ScoreBoardHud.Entry[] entryArray, TextRenderer textRenderer) {
      int width = textRenderer.getWidth(text);
      int width2 = textRenderer.getWidth(COLON_TEXT);
      boolean flag = !this.isValidSafe();
      int n = width;

      for (ScoreBoardHud.Entry entry : entryArray) {
         int width3 = textRenderer.getWidth(entry.getName());
         if (flag && entry.getScoreWidth() > 0) {
            width3 += width2 + entry.getScoreWidth();
         }

         n = Math.max(n, width3);
      }

      return new ScoreBoardHud.Entry2(text, entryArray, n, width, width2);
   }

   private record Entry(Text name, Text score, int scoreWidth) {
      public Text getName() {
         return this.name;
      }

      public Text getScore() {
         return this.score;
      }

      public int getScoreWidth() {
         return this.scoreWidth;
      }
   }

   private record Entry2(Text title, ScoreBoardHud.Entry[] lines, int width, int titleWidth, int separatorWidth) {
      public Text getTitle() {
         return this.title;
      }

      public ScoreBoardHud.Entry[] getLines() {
         return this.lines;
      }

      public int getWidth() {
         return this.width;
      }

      public int getTitleWidth() {
         return this.titleWidth;
      }

      public int getSeparatorWidth() {
         return this.separatorWidth;
      }
   }
}
