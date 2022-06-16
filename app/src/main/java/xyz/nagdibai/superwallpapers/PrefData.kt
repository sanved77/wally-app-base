package xyz.nagdibai.superwallpapers

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.dataStore by preferencesDataStore("user_preferences_hNG2rf3gY5hK")

class PrefData(context: Context) {
    object Keys {
        val FIRST_RUN_REVIEW_DONE = booleanPreferencesKey("first_run_review_done")
        val TEST = intPreferencesKey("dhuski")
    }
    val dataStore = context.dataStore
}