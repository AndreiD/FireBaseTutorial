package me.andrei.firebasetest.utils;

import android.content.Context;
import java.util.UUID;

public class MyUtils {

  public static String generateUniqueUserId(Context ctx) {

    String existing_user = ctx.getSharedPreferences("PREFS", 0).getString("user_id", null);
    if (existing_user != null) {
      return existing_user;
    }
    String uniqueID = UUID.randomUUID().toString();
    ctx.getSharedPreferences("PREFS", 0).edit().putString("user_id", uniqueID).commit();
    return uniqueID;
  }
}
