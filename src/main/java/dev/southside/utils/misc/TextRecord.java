package dev.southside.utils.misc;

public record TextRecord(String text, int caret, int selectionStart, int selectionEnd) {
   public TextRecord(String text, int caret, int selectionStart, int selectionEnd) {
      caret = computeInt(text, caret);
      selectionStart = computeInt(text, selectionStart);
      selectionEnd = Math.max(selectionStart, computeInt(text, selectionEnd));
      this.text = text;
      this.caret = caret;
      this.selectionStart = selectionStart;
      this.selectionEnd = selectionEnd;
   }

   private static int computeInt(String text2, int n) {
      int n2 = Math.max(0, Math.min(text2.length(), n));
      if (n2 > 0 && n2 < text2.length() && Character.isLowSurrogate(text2.charAt(n2)) && Character.isHighSurrogate(text2.charAt(n2 - 1))) {
         n2--;
      }

      return n2;
   }

   public String getText() {
      return this.text;
   }

   public int getCaret() {
      return this.caret;
   }

   public int getSelectionStart() {
      return this.selectionStart;
   }

   public int getSelectionEnd() {
      return this.selectionEnd;
   }
}
