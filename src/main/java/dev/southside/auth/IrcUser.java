package dev.southside.auth;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Objects;

public class IrcUser {
   @SerializedName("username")
   private String username;
   @SerializedName("pvp")
   private boolean pvp;
   @SerializedName("uuids")
   private List<String> uuids;

   @Override
   public boolean equals(Object obj) {
      if (obj != null && this.getClass() == obj.getClass()) {
         IrcUser ircUser = (IrcUser)obj;
         return Objects.equals(this.username, ircUser.username);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(this.username);
   }

   public String getUsername() {
      return this.username;
   }

   public boolean isPvp() {
      return this.pvp;
   }

   public List<String> getUuids() {
      return this.uuids;
   }

   public IrcUser(String text, boolean flag, List<String> list) {
      this.username = text;
      this.pvp = flag;
      this.uuids = list;
   }
}
