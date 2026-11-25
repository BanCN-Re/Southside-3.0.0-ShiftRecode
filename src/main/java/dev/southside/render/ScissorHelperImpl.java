package dev.southside.render;

import com.google.gson.annotations.SerializedName;
import dev.southside.utils.ScissorHelper;
import dev.southside.utils.ScissorRect;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.nanovg.NanoVG;

public class ScissorHelperImpl implements ScissorHelper {
   @SerializedName("previousScissors")
   private final List<List<ScissorRect>> previousScissors = new ArrayList<>();
   @SerializedName("scissors")
   private List<ScissorRect> scissors = new ArrayList<>();

   @Override
   public ScissorRect scissor(long vg, float x, float y, float width, float height) {
      ScissorRect clip = new ScissorRect(x, y, width, height);
      if (this.scissors.contains(clip)) {
         return clip;
      }

      this.scissors.add(clip);
      this.scissor(vg);
      return clip;
   }

   @Override
   public void resetScissor(long l, ScissorRect clip) {
      if (this.scissors.contains(clip)) {
         this.scissors.remove(clip);
         this.scissor(l);
      }
   }

   @Override
   public void clearScissors(long l) {
      this.scissors.clear();
      NanoVG.nvgResetScissor(l);
   }

   @Override
   public void save() {
      this.previousScissors.add(new ArrayList<>(this.scissors));
   }

   @Override
   public void restore(long l) {
      this.scissors = this.previousScissors.remove(0);
      this.scissor(l);
   }

   private void scissor(long l) {
      NanoVG.nvgResetScissor(l);
      if (this.scissors.size() != 0) {
         ScissorRect scissorRect = this.getScissorRect(this.scissors);
         NanoVG.nvgScissor(l, scissorRect.x, scissorRect.y, scissorRect.width, scissorRect.height);
      }
   }

   private ScissorRect getScissorRect(List<ScissorRect> list) {
      ScissorRect clip = new ScissorRect((ScissorRect)list.get(0));

      for (int i = 1; i < list.size(); i++) {
         ScissorRect clip2 = (ScissorRect)list.get(i);
         float f = Math.min(clip2.x + clip2.width, clip.x + clip.width);
         float f2 = Math.min(clip2.y + clip2.height, clip.y + clip.height);
         clip.x = Math.max(clip.x, clip2.x);
         clip.y = Math.max(clip.y, clip2.y);
         clip.width = f - clip.x;
         clip.height = f2 - clip.y;
      }

      return clip;
   }
}
