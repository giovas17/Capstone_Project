package com.softwaremobility.preferences;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class MoninPreferences {

    public enum SHAREDPREFERENCE_KEY {
        KEY_TOKEN_EXPIRY(0L),
        KEY_TOKEN(""),
        KEY_TOKEN_TIME(0L),
        KEY_PROVIDER_LOGIN(""),
        KEY_PRIVACY_POLICY(false),
        KEY_GHOST_HOME(true),
        KEY_GHOST_DETAIL(true),
        KEY_GHOST_RECIPE(true),
        KEY_GHOST_NEW_SLIDE_SHOW(true),
        KEY_GHOST_COMMUNITY(true),
        KEY_INITIAL_WALKTHROUGH(true),
        KEY_CURRENT_PAGE_MONIN(1),
        KEY_CURRENT_PAGE_USER_RECIPES(1),
        KEY_IS_ALCOHOLIC(false),
        KEY_IS_COFFEE(false),
        KEY_IS_IMPERIAL(true),
        KEY_IS_NON_ALCOHOLIC(false),
        KEY_FILTER_BY_REGION(true),
        KEY_GPS_ALLOWED(false),
        KEY_USER_NAME(""),
        KEY_GENERIC_APP_SECRET(""),
        KEY_IS_NO_LOGIN(false),
        KEY_IS_PUBLIC_PROFILE(true),
        KEY_CURRENT_PAGE_COMMUNITY(1),
        KEY_TYPE_BUILD(false),
        KEY_USER_ID("");

        SHAREDPREFERENCE_KEY(final Object defaultValue) {
            mDefaultValue = defaultValue;
        }

        public Object getDefault() {
            return mDefaultValue;
        }

        private final Object mDefaultValue;
    }

    private MoninPreferences() {
    }

    private static SharedPreferences getSharedPreferences(final Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences;
    }

    private static SharedPreferences.Editor getEditor(final Context context) {
        SharedPreferences preferences = getSharedPreferences(context);
        return preferences.edit();
    }


    public static void setBoolean(final Context context, final SHAREDPREFERENCE_KEY key, final boolean value) {
        final Object defaultValue = key.getDefault();
        if (defaultValue instanceof Boolean) {
            getEditor(context).putBoolean(key.name(), value).commit();
        } else {
            throw new IllegalArgumentException("Can not store boolean value in " + key.name());
        }
    }


    public static Boolean getBoolean(final Context context, final SHAREDPREFERENCE_KEY key) {
        final Object defaultValue = key.getDefault();
        if (defaultValue instanceof Boolean) {
            return getSharedPreferences(context).getBoolean(key.name(), (Boolean) defaultValue);
        } else {
            throw new IllegalArgumentException("Boolean value does not exist for " + key.name());
        }
    }


    public static void setInteger(final Context context, final SHAREDPREFERENCE_KEY key, final int value) {
        final Object defaultValue = key.getDefault();
        if (defaultValue instanceof Integer) {
            getEditor(context).putInt(key.name(), value).commit();
        } else {
            throw new IllegalArgumentException("Can not store Integer value in " + key.name());
        }
    }


    public static int getInteger(final Context context, final SHAREDPREFERENCE_KEY key) {
        final Object defaultValue = key.getDefault();

        if (defaultValue instanceof Integer) {
            return getSharedPreferences(context).getInt(key.name(), (Integer) defaultValue);
        } else {
            throw new IllegalArgumentException("Integer value does not exist for " + key.name());
        }
    }


    public static void setString(final Context context, final SHAREDPREFERENCE_KEY key, final String value) {
        final Object defaultValue = key.getDefault();
        if ((defaultValue == null) || (defaultValue instanceof String)) {
            getEditor(context).putString(key.name(), value).commit();
        } else {
            throw new IllegalArgumentException("Can not store String value in " + key.name());
        }
    }


    public static String getString(final Context context, final SHAREDPREFERENCE_KEY key) {
        final Object defaultValue = key.getDefault();

        if ((defaultValue == null) || (defaultValue instanceof String)) {
            return getSharedPreferences(context).getString(key.name(), (String) defaultValue);
        } else {
            throw new IllegalArgumentException("String value does not exist for " + key.name());
        }
    }


    public static void setLong(final Context context, final SHAREDPREFERENCE_KEY key, final long value) {
        final Object defaultValue = key.getDefault();
        if ((defaultValue == null) || (defaultValue instanceof Long)) {
            getEditor(context).putLong(key.name(), value).commit();
        } else {
            throw new IllegalArgumentException("Can not store String value in " + key.name());
        }
    }


    public static long getLong(final Context context, final SHAREDPREFERENCE_KEY key) {
        final Object defaultValue = key.getDefault();

        if ((defaultValue == null) || (defaultValue instanceof Long)) {
            return getSharedPreferences(context).getLong(key.name(), (Long) defaultValue);
        } else {
            throw new IllegalArgumentException("String value does not exist for " + key.name());
        }
    }
}
