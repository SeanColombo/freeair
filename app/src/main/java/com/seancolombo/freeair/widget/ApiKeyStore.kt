package com.seancolombo.freeair.widget

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

// The PurpleAir API key is app-global, not per-widget -- PurpleAir issues one key per account,
// not per sensor -- so it lives in its own app-level DataStore instance, separate from each
// widget's own per-instance state (see WidgetPreferencesStore).
private val Context.apiKeyDataStore: DataStore<Preferences> by preferencesDataStore(name = "api_key")
private val KEY_API_KEY = stringPreferencesKey("api_key")

/** Null means no key has been saved yet -- widgets render [FreeAirWidgetState.NeedsSetup]. */
suspend fun loadApiKey(context: Context): String? =
    context.apiKeyDataStore.data.first()[KEY_API_KEY]?.takeIf { it.isNotBlank() }

// TODO: once there's a flow to let the user change an already-saved key (README: "entry point
// to let you change the key"), a widget whose session is still alive when the key changes won't
// pick up the new value right away. currentState()/produceState's reactivity (see
// FreeAirWidget.WidgetEntryPoint) is driven by the widget's own per-instance Glance Preferences;
// this store is a separate, plain DataStore that Glance's session mechanism doesn't observe, so
// nothing currently forces an affected widget's session to restart when this changes. The
// existing sensor-ID flow doesn't have this problem because saving a new sensor ID changes
// produceState's own key (sensorConfig), which does force a restart.
suspend fun saveApiKey(context: Context, apiKey: String) {
    context.apiKeyDataStore.edit { it[KEY_API_KEY] = apiKey }
}
