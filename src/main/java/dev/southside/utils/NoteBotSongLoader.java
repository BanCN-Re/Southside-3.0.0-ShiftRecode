package dev.southside.utils;

import com.google.gson.annotations.SerializedName;
import dev.southside.utils.misc.NotesRecord;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class NoteBotSongLoader {
   private static final int DEFAULT_INT = 1024;
   @SerializedName("executor")
   private final ExecutorService executor = Executors.newSingleThreadExecutor(action -> {
      Thread thread = new Thread(action, "Southside-NoteBot-Loader");
      thread.setDaemon(true);
      return thread;
   });
   @SerializedName("pending")
   private Future<NoteBotSongLoader.Entry> pending;
   @SerializedName("playlist")
   private Future<List<String>> playlist;
   private static final long DEFAULT_LONG = 1025L;

   public void performAction(Path path) {
      if (this.playlist == null) {
         this.playlist = this.executor.submit(() -> {
            Files.createDirectories(path);
            return collectItems(path).stream().map(path2 -> path2.getFileName().toString()).toList();
         });
      }
   }

   public List<String> collectItems() throws IOException {
      if (this.playlist != null && this.playlist.isDone()) {
         Future<List<String>> playlist2 = this.playlist;
         this.playlist = null;
         return getT(playlist2);
      } else {
         return null;
      }
   }

   public void performAction(Path path, String text, String text2) {
      this.performAction();
      this.pending = this.executor.submit(() -> getEntry(path, text, text2));
   }

   public NoteBotSongLoader.Entry getEntry() throws IOException {
      if (this.pending != null && this.pending.isDone()) {
         Future<NoteBotSongLoader.Entry> pending2 = this.pending;
         this.pending = null;
         return getT(pending2);
      } else {
         return null;
      }
   }

   private static <T> T getT(Future<T> future) throws IOException {
      try {
         return (T)future.get();
      } catch (InterruptedException e) {
         Thread.currentThread().interrupt();
         throw new IOException("NBS loading interrupted", e);
      } catch (ExecutionException e2) {
         if (e2.getCause() instanceof IOException e3) {
            throw e3;
         } else {
            throw new IOException("Failed to load NBS song", e2.getCause());
         }
      }
   }

   public void performAction() {
      if (this.pending != null) {
         this.pending.cancel(true);
      }

      this.pending = null;
   }

   public static String formatText(String text) {
      String text2 = text == null ? "" : text.trim();
      if (!text2.isEmpty() && !text2.toLowerCase(Locale.ROOT).endsWith(".nbs")) {
         text2 = text2 + ".nbs";
      }

      return text2;
   }

   private static NoteBotSongLoader.Entry getEntry(Path path, String text, String text2) throws IOException {
      Files.createDirectories(path);
      List list = collectItems(path);
      if (list.isEmpty()) {
         throw new IOException("no .nbs files in " + path);
      }

      Path path2 = getPath(list, text, text2);
      NotesRecord notesRecord = NbsSongParser.getNotesRecord(path2);
      BitSet bitSet = new BitSet();
      int n = 0;

      for (NotesRecord.Entry entry : notesRecord.getNotes()) {
         if ((n++ & 4095) == 0 && Thread.currentThread().isInterrupted()) {
            throw new InterruptedIOException("NBS loading cancelled");
         }

         bitSet.set(entry.getHeight());
      }

      return new NoteBotSongLoader.Entry(path2.getFileName().toString(), list.size(), notesRecord, bitSet.stream().boxed().toList());
   }

   private static List<Path> collectItems(Path path) throws IOException {
      List<Path> list;
      try (Stream<Path> stream = Files.list(path)) {
         list = stream.filter(path2 -> Files.isRegularFile(path2))
            .filter(path3 -> path3.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".nbs"))
            .limit(DEFAULT_LONG)
            .collect(Collectors.toCollection(ArrayList::new));
      }

      if (list.size() > 1024) {
         throw new IOException("playlist contains more than 1024 files");
      }

      list.sort(
         Comparator.<Path, String>comparing(path4 -> path4.getFileName().toString().toLowerCase(Locale.ROOT))
            .thenComparing(path5 -> path5.getFileName().toString())
      );
      return list;
   }

   private static Path getPath(List<Path> list, String text, String text2) throws IOException {
      if (text2 != null) {
         for (int i = 0; i < list.size(); i++) {
            if (((Path)list.get(i)).getFileName().toString().equals(text2)) {
               return (Path)list.get((i + 1) % list.size());
            }
         }

         return (Path)list.getFirst();
      } else {
         if (text.isEmpty()) {
            return (Path)list.getFirst();
         }

         for (Path path : list) {
            if (path.getFileName().toString().equalsIgnoreCase(text)) {
               return path;
            }
         }

         throw new IOException("song not found: " + text);
      }
   }

   public record Entry(String name, int playlistSize, NotesRecord song, List<Integer> neededKeys) {
      public Entry(String name, int playlistSize, NotesRecord song, List<Integer> neededKeys) {
         neededKeys = List.copyOf(neededKeys);
         this.name = name;
         this.playlistSize = playlistSize;
         this.song = song;
         this.neededKeys = neededKeys;
      }

      public String getName() {
         return this.name;
      }

      public int getPlaylistSize() {
         return this.playlistSize;
      }

      public NotesRecord getSong() {
         return this.song;
      }

      public List<Integer> getNeededKeys() {
         return this.neededKeys;
      }
   }
}
