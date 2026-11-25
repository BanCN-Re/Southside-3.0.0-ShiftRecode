package dev.southside.utils.misc;

import java.util.List;

public record NotesRecord(List<NotesRecord.Entry> notes, float tempo) {
   public NotesRecord(List<NotesRecord.Entry> notes, float tempo) {
      notes = List.copyOf(notes);
      this.notes = notes;
      this.tempo = tempo;
   }

   public List<NotesRecord.Entry> getNotes() {
      return this.notes;
   }

   public float getTempo() {
      return this.tempo;
   }

   public record Entry(int tick, int instrument, int pitch) {
      private static long timestamp;
      public int getHeight() {
         return this.instrument << (int)timestamp | this.pitch;
      }

      public int getTick() {
         return this.tick;
      }

      public int getInstrument() {
         return this.instrument;
      }

      public int getPitch() {
         return this.pitch;
      }

      static {
         long l = 5437009523892027400L;
         timestamp = l;
      }
   }
}
