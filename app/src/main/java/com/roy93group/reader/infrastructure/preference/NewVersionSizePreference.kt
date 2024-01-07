package com.roy93group.reader.infrastructure.preference

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.roy93group.reader.ui.ext.DataStoreKeys
import com.roy93group.reader.ui.ext.dataStore
import com.roy93group.reader.ui.ext.put

object NewVersionSizePreference {

    const val default = ""

    fun Int.formatSize(): String =
        (this / 1024f / 1024f)
            .takeIf { it > 0f }
            ?.run { " ${String.format("%.2f", this)} MB" } ?: ""

    fun put(context: Context, scope: CoroutineScope, value: String) {
        scope.launch(Dispatchers.IO) {
            context.dataStore.put(DataStoreKeys.NewVersionSize, value)
        }
    }

    fun fromPreferences(preferences: Preferences) =
        preferences[DataStoreKeys.NewVersionSize.key] ?: default
}