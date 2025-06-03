package com.jaime.ascend.utils

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jaime.ascend.data.models.Moment
import kotlinx.coroutines.flow.first


private val Context.dataStore by preferencesDataStore(name = "shop_cache")

/**
 * Local cache for moments.
 * @param context The context.
 */
class ShopLocalCache(private val context: Context) {
    private val gson = Gson()
    private val momentsType = object : TypeToken<List<Moment>>() {}.type

    /**
     * Companion object containing keys for data store preferences.
     */
    companion object {
        val CACHED_MOMENTS = stringPreferencesKey("cached_moments")
        val LAST_REROLL_DATE = stringPreferencesKey("last_reroll_date")
    }

    /**
     * Caches a list of moments.
     * @param moments The list of moments to cache.
     */
    suspend fun cacheMoments(moments: List<Moment>) {
        context.dataStore.edit { preferences ->
            preferences[CACHED_MOMENTS] = gson.toJson(moments)
        }
    }

    /**
     * Retrieves a list of cached moments.
     * @return The list of cached moments.
     */
    suspend fun getCachedMoments(): List<Moment> {
        val jsonString = context.dataStore.data.first()[CACHED_MOMENTS] ?: return emptyList()
        return try {
            gson.fromJson(jsonString, momentsType) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Caches the last re-roll date.
     * @param date The re-roll date.
     */
    suspend fun cacheLastReRollDate(date: String) {
        context.dataStore.edit { preferences ->
            preferences[LAST_REROLL_DATE] = date
        }
    }

    /**
     * Retrieves the last re-roll date.
     * @return The last re-roll date.
     */
    suspend fun getLastReRollDate(): String? {
        return context.dataStore.data.first()[LAST_REROLL_DATE]
    }
}