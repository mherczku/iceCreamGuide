package hu.hm.icguide.ui.settings

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceFragmentCompat
import hu.hm.icguide.R


class SettingsPreference : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if(key == "darkTheme"){
            val darkMode = sharedPreferences?.getBoolean(key, false)
            if(darkMode == true) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
}