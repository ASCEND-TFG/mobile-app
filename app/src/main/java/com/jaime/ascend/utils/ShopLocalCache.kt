package com.jaime.ascend.utils

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jaime.ascend.data.models.Moment
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

private val Context.dataStore by preferencesDataStore(name = "shop_cache")

class ShopLocalCache(private val context: Context) {
    private val gson = Gson()
    private val momentsType = object : TypeToken<List<Moment>>() {}.type

    companion object {
        val CACHED_MOMENTS = stringPreferencesKey("cached_moments")
        val LAST_REROLL_DATE = stringPreferencesKey("last_reroll_date")
    }

    suspend fun cacheMoments(moments: List<Moment>) {
        context.dataStore.edit { preferences ->
            preferences[CACHED_MOMENTS] = gson.toJson(moments)
        }
    }

    suspend fun getCachedMoments(): List<Moment> {
        val jsonString = context.dataStore.data.first()[CACHED_MOMENTS] ?: return emptyList()
        return try {
            gson.fromJson(jsonString, momentsType) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun cacheLastReRollDate(date: String) {
        context.dataStore.edit { preferences ->
            preferences[LAST_REROLL_DATE] = date
        }
    }

    suspend fun getLastReRollDate(): String? {
        return context.dataStore.data.first()[LAST_REROLL_DATE]
    }
}