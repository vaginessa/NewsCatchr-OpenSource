/*
 * NewsCatchr
 * Copyright © 2017 Jan-Lukas Else
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package jlelse.newscatchr.backend.helpers

import android.content.SharedPreferences
import jlelse.newscatchr.extensions.resStr
import jlelse.newscatchr.extensions.sharedPref
import jlelse.readit.BuildConfig
import jlelse.readit.R
import java.util.*

/**
 * Preferences
 * For all configurations
 */
object Preferences {

	private fun write(): SharedPreferences.Editor = sharedPref().edit()

	private fun read(): SharedPreferences = sharedPref()

	val customTabs: Boolean
		get() = read().getBoolean(R.string.prefs_key_custom_tabs.resStr(), true)

	var amp: Boolean
		get() = read().getBoolean(R.string.prefs_key_amp.resStr(), false)
		set(value) = write().putBoolean(R.string.prefs_key_amp.resStr(), value).apply()

	var urlShortener: Boolean
		get() = read().getBoolean(R.string.prefs_key_url_shortener.resStr(), false)
		set(value) = write().putBoolean(R.string.prefs_key_url_shortener.resStr(), value).apply()

	var pocketUserName: String
		get() = read().getString(R.string.prefs_key_user_name.resStr(), "")
		set(value) = write().putString(R.string.prefs_key_user_name.resStr(), value).apply()

	var pocketAccessToken: String
		get() = read().getString(R.string.prefs_key_access_token.resStr(), "")
		set(value) = write().putString(R.string.prefs_key_access_token.resStr(), value).apply()

	var pocketSync: Boolean
		get() = read().getBoolean(R.string.prefs_key_pocket_sync.resStr(), true)
		set(value) = write().putBoolean(R.string.prefs_key_pocket_sync.resStr(), value).apply()

	var recommendationsLanguage: String
		get() = read().getString("recLanguage", Locale.getDefault().language)
		set(value) = write().putString("recLanguage", value).apply()

	var language: String
		get() = read().getString(R.string.prefs_key_language.resStr(), Locale.getDefault().language)
		set(value) = write().putString(R.string.prefs_key_language.resStr(), value).apply()

	var textScaleFactor: Float
		get() = read().getFloat("textScaleFactor", 1.0f)
		set(value) = write().putFloat("textScaleFactor", value).apply()

	var nightMode: Int
		get() = read().getInt(R.string.prefs_key_night_mode.resStr(), 0)
		set(value) = write().putInt(R.string.prefs_key_night_mode.resStr(), value).apply()

	var syncEnabled: Boolean
		get() = read().getBoolean(R.string.prefs_key_sync.resStr(), false)
		set(value) = write().putBoolean(R.string.prefs_key_sync.resStr(), value).apply()

	var syncInterval: Int
		get() = read().getInt(R.string.prefs_key_sync_interval.resStr(), 30)
		set(value) = write().putInt(R.string.prefs_key_sync_interval.resStr(), value).apply()

	var lastSync: Long
		get() = read().getLong("lastSync", 0.toLong())
		set(value) = write().putLong("lastSync", value).apply()

	var supportUser: Boolean
		get() = read().getBoolean("supportUser", false) || BuildConfig.DEBUG
		set(value) = write().putBoolean("supportUser", value).apply()

	var hostsVersion: Int
		get() = read().getInt("hostsVersion", 0)
		set(value) = write().putInt("hostsVersion", value).apply()

}
