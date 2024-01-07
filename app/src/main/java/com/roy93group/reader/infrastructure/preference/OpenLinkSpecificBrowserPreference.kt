package com.roy93group.reader.infrastructure.preference

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.Keep
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import com.roy93group.reader.R
import com.roy93group.reader.ui.ext.DataStoreKeys
import com.roy93group.reader.ui.ext.dataStore
import com.roy93group.reader.ui.ext.put

@Keep
data class OpenLinkSpecificBrowserPreference(
    val packageName: String?
    ) : Preference() {

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(
                DataStoreKeys.OpenLinkAppSpecificBrowser,
                packageName.orEmpty()
            )
        }
    }

    fun toDesc(context: Context): String {
        val pm = context.packageManager
        return runCatching {
            pm.run {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    getApplicationInfo(
                        this@OpenLinkSpecificBrowserPreference.packageName!!,
                        PackageManager.ApplicationInfoFlags.of(0)
                    )
                } else {
                    getApplicationInfo(this@OpenLinkSpecificBrowserPreference.packageName!!, 0)
                }
            }
        }.map {
            it.loadLabel(pm)
        }.getOrDefault(context.getString(R.string.open_link_specific_browser_not_selected)).let {
            context.getString(R.string.specific_browser_name, it)
        }
    }

    companion object {
        val default = OpenLinkSpecificBrowserPreference(null)
        fun fromPreferences(preferences: Preferences): OpenLinkSpecificBrowserPreference {
            val packageName = preferences[DataStoreKeys.OpenLinkAppSpecificBrowser.key]
            return OpenLinkSpecificBrowserPreference(packageName)
        }
    }
}
