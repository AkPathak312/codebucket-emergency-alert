package com.example.emergencyalert.data.repository;

import android.content.Context;
import android.preference.PreferenceManager;

public class UtilSharedPreferences {

    static  final String LOGGED_IN="logged_in";
    static  final String AUTH_TOKEN="auth_token";

    public static android.content.SharedPreferences getSharedPreferences(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    //Set and Get User Logged in Status
    public static void setLoggedIn(Context ctx, boolean status) {
        android.content.SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putBoolean(LOGGED_IN, status);
        editor.apply();
    }

    public static boolean getLoggedIn(Context ctx) {
        return getSharedPreferences(ctx).getBoolean(LOGGED_IN, false);
    }

    //Set and get Token
    public static void setAuthToken(Context ctx, String name) {
        android.content.SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(AUTH_TOKEN, name);
        editor.apply();
    }
    public static String getAuthToken(Context ctx) {
        return getSharedPreferences(ctx).getString(AUTH_TOKEN, "default");
    }
}
