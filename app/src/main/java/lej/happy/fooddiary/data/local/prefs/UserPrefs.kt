package lej.happy.fooddiary.data.local.prefs

import android.content.Context
import android.content.SharedPreferences

class UserPrefs(context: Context) {

    private val PREF_USER_NAME = "prefs_name"
    private val PREF_KEY_USER_IMG_BITMAP = "user_img"
    private val PREF_KEY_USER_NAME = "user_name"

    private val pref: SharedPreferences = context.getSharedPreferences(PREF_USER_NAME, 0)

    var userImg: String?
        get() {
            return try {
                pref.getString(PREF_KEY_USER_IMG_BITMAP, null)
            } catch (e: Exception) {
                null
            }
        }
        set(value) {
            try {
                pref.edit().putString(PREF_KEY_USER_IMG_BITMAP, value).commit()
            } catch (e: Exception) {
                pref.edit().putString(PREF_KEY_USER_IMG_BITMAP, value).commit()
            }
        }

    var userName: String
        get() {
            return try {
                pref.getString(PREF_KEY_USER_NAME, "Name") ?: "Name"
            } catch (e: Exception) {
                "Name"
            }
        }
        set(value) {
            try {
                pref.edit().putString(PREF_KEY_USER_NAME, value).commit()
            } catch (e: Exception) {
                pref.edit().putString(PREF_KEY_USER_NAME, value).commit()
            }
        }
}