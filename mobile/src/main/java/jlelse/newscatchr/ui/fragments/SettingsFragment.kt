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

package jlelse.newscatchr.ui.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceFragmentCompat
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.afollestad.materialdialogs.MaterialDialog
import jlelse.newscatchr.backend.Feed
import jlelse.newscatchr.backend.apis.PocketAuth
import jlelse.newscatchr.backend.apis.backupRestore
import jlelse.newscatchr.backend.helpers.*
import jlelse.newscatchr.extensions.*
import jlelse.newscatchr.ui.activities.MainActivity
import jlelse.newscatchr.ui.interfaces.FragmentValues
import jlelse.newscatchr.ui.objects.Library
import jlelse.newscatchr.ui.views.LinkTextView
import jlelse.newscatchr.ui.views.ProgressDialog
import jlelse.readit.R
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.support.v4.onUiThread
import org.jetbrains.anko.uiThread
import java.util.*

class SettingsFragment : PreferenceFragmentCompat(), Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener, FragmentValues {
	override val valueMap = mutableMapOf<String, Any?>()

	private var purchaseReceiver: PurchaseStatusUpdateReceiver? = null
	private var purchaseReceiverRegistered = false
	private var syncReceiver: SyncStatusUpdateReceiver? = null
	private var syncReceiverRegistered = false

	private val clearCachePref: Preference? by lazy { findPreference(R.string.prefs_key_clear_cache.resStr()) }
	private val clearHistoryPref: Preference? by lazy { findPreference(R.string.prefs_key_clear_history.resStr()) }
	private val viewLibsPref: Preference? by lazy { findPreference(R.string.prefs_key_view_libs.resStr()) }
	private val viewApisPref: Preference? by lazy { findPreference(R.string.prefs_key_view_apis.resStr()) }
	private val aboutPref: Preference? by lazy { findPreference(R.string.prefs_key_about_nc.resStr()) }
	private val backupPref: Preference? by lazy { findPreference(R.string.prefs_key_backup.resStr()) }
	private val syncNowPref: Preference? by lazy { findPreference(R.string.prefs_key_sync_now.resStr()) }
	private val supportPref: Preference? by lazy { findPreference(R.string.prefs_key_support_pref.resStr()) }
	private val donateCategory: Preference? by lazy { findPreference(R.string.prefs_key_pro_category.resStr()) }
	private val syncPref: Preference? by lazy { findPreference(R.string.prefs_key_sync.resStr()) }
	private val syncIntervalPref: Preference? by lazy { findPreference(R.string.prefs_key_sync_interval.resStr()) }
	private val nightModePref: Preference? by lazy { findPreference(R.string.prefs_key_night_mode.resStr()) }
	private val pocketLoginPref: Preference? by lazy { findPreference(R.string.prefs_key_pocket_login.resStr()) }
	private val pocketSyncPref: Preference? by lazy { findPreference(R.string.prefs_key_pocket_sync.resStr()) }
	private val languagePref: Preference? by lazy { findPreference(R.string.prefs_key_language.resStr()) }
	private val issuePref: Preference? by lazy { findPreference(R.string.prefs_key_issue.resStr()) }

	// Pocket stuff
	val progressDialog: ProgressDialog? by lazy { ProgressDialog(context) }
	var pocketAuth: PocketAuth? = null

	override fun onCreatePreferences(p0: Bundle?, p1: String?) {
		addPreferencesFromResource(R.xml.preferences)
	}

	override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		restoreValues(savedInstanceState)
		if (activity is MainActivity) (activity as MainActivity).resetToolbarBackground()
		val view = super.onCreateView(inflater, container, savedInstanceState)
		if (!purchaseReceiverRegistered) {
			purchaseReceiver = PurchaseStatusUpdateReceiver(this)
			activity.registerReceiver(purchaseReceiver, IntentFilter("purchaseStatus"))
			purchaseReceiverRegistered = true
		}
		if (!syncReceiverRegistered) {
			syncReceiver = SyncStatusUpdateReceiver(this)
			activity.registerReceiver(syncReceiver, IntentFilter("syncStatus"))
			syncReceiverRegistered = true
		}
		return view
	}

	override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		// Add ClickListeners
		clearCachePref?.onPreferenceClickListener = this
		clearHistoryPref?.onPreferenceClickListener = this
		supportPref?.onPreferenceClickListener = this
		viewLibsPref?.onPreferenceClickListener = this
		viewApisPref?.onPreferenceClickListener = this
		aboutPref?.onPreferenceClickListener = this
		backupPref?.onPreferenceClickListener = this
		syncIntervalPref?.onPreferenceClickListener = this
		syncNowPref?.onPreferenceClickListener = this
		nightModePref?.onPreferenceClickListener = this
		pocketLoginPref?.onPreferenceClickListener = this
		languagePref?.onPreferenceClickListener = this
		issuePref?.onPreferenceClickListener = this

		// Add ChangeListeners
		syncPref?.onPreferenceChangeListener = this

		refreshLastSyncTime()
		refreshNightModeDesc()
		refreshSyncIntervalDesc()
		refreshPocket()

		handlePurchases()
	}

	override fun onPreferenceClick(preference: Preference?): Boolean {
		when (preference) {
			clearCachePref -> {
				context.clearCache {
					Snackbar.make(activity.findViewById(R.id.mainactivity_container), R.string.cleared_cache, Snackbar.LENGTH_SHORT).show()
				}
			}
			clearHistoryPref -> {
				doAsync {
					Database.allLastFeeds = arrayOf<Feed>()
					uiThread {
						context.sendBroadcast(Intent("last_feed_updated"))
						Snackbar.make(activity.findViewById(R.id.mainactivity_container), R.string.cleared_history, Snackbar.LENGTH_SHORT).show()
					}
				}
			}
			supportPref -> if (activity is MainActivity) (activity as MainActivity).purchaseSupport()
			viewLibsPref -> {
				var html = ""
				arrayOf(
						Library("Material Dialogs", "A beautiful, easy-to-use, and customizable dialogs API, enabling you to use Material designed dialogs down to API 8.", "https://github.com/afollestad/material-dialogs"),
						Library("FastAdapter", "The bullet proof, fast and easy to use adapter library, which minimizes developing time to a fraction...", "https://github.com/mikepenz/FastAdapter/"),
						Library("jsoup", "Java HTML Parser, with best of DOM, CSS, and jquery", "https://github.com/jhy/jsoup"),
						Library("Bridge", "A simple but powerful HTTP networking library for Android. It features a Fluent chainable API, powered by Java/Android's URLConnection classes for maximum compatibility and speed.", "https://github.com/afollestad/bridge"),
						Library("Glide", "An image loading and caching library for Android focused on smooth scrolling", "https://github.com/bumptech/glide"),
						Library("Paper", "Fast and simple data storage library for Android", "https://github.com/pilgr/Paper"),
						Library("google-gson", "A Java serialization/deserialization library that can convert Java Objects into JSON and back.", "https://github.com/google/gson"),
						Library("CloudRail", "Integrate Multiple Services With Just One API", "https://github.com/CloudRail/cloudrail-si-android-sdk"),
						Library("Android In-App Billing v3 Library", "A lightweight implementation of Android In-app Billing Version 3", "https://github.com/anjlab/android-inapp-billing-v3"),
						Library("FlexboxLayout", "FlexboxLayout is a library project which brings the similar capabilities of CSS Flexible Box Layout Module to Android.", "https://github.com/google/flexbox-layout"),
						Library("Android-Job", "Android library to handle jobs in the background.", "https://github.com/evernote/android-job"),
						Library("android-issue-reporter", "A powerful and simple library to open issues on GitHub directly from your app.", "https://github.com/HeinrichReimer/android-issue-reporter"),
						Library("CustomTabsHelper", "Custom tabs, made easy.", "https://github.com/DreaminginCodeZH/CustomTabsHelper")
				).forEach {
					html += "<b><a href=\"${it.link}\">${it.name}</a></b> ${it.description}<br><br>"
				}
				if (html.length > 8) html.removeRange(html.lastIndex - 8, html.lastIndex) // Remove last useless linebreaks
				MaterialDialog.Builder(context)
						.title(R.string.used_libraries)
						.content(html.toHtml())
						.positiveText(android.R.string.ok)
						.build()
						.apply {
							LinkTextView().apply(contentView, activity)
							show()
						}
			}
			viewApisPref -> {
				var html = ""
				arrayOf(
						Library("feedly Cloud API", "", "https://developer.feedly.com"),
						Library("Pocket API", "", "https://getpocket.com/developer/"),
						Library("Google URL Shortener", "", "https://developers.google.com/url-shortener/"),
						Library("Google AMP Cache", "", "https://developers.google.com/amp/cache/"),
						Library("Mercury Web Parser", "", "https://mercury.postlight.com/web-parser/")
				).forEach {
					html += "<b><a href=\"${it.link}\">${it.name}</a></b><br><br>"
				}
				if (html.length > 8) html.removeRange(html.lastIndex - 8, html.lastIndex)
				MaterialDialog.Builder(context)
						.title(R.string.used_libraries)
						.content(html.toHtml())
						.positiveText(android.R.string.ok)
						.build()
						.apply {
							LinkTextView().apply(contentView, activity)
							show()
						}
			}
			aboutPref -> {
				val description: String = "<b>The best newsreader for Android<br><i>It's the way of reading news in 2020</i></b><br><br>Developer: <a href=\"https://plus.google.com/+JanLkElse\">Jan-Lukas Else</a><br>Icon designer: <a href=\"https://plus.google.com/+KevinAguilarC\">Kevin Aguilar</a><br>Banner designer: <a href=\"https://plus.google.com/+%C5%BDan%C4%8Cerne\">&#381;an &#268;erne</a><br><br><a href=\"https://newscatchr.jlelse.eu\">NewsCatchr Website</a><br><a href=\"https://github.com/jlelse/NewsCatchr-OpenSource\">Source code on GitHub</a><br><br>"
				val statsDesc = "You already opened ${Database.allReadUrls.size} articles. Thanks for that!"
				MaterialDialog.Builder(context)
						.title(R.string.app_name)
						.content("$description$statsDesc".toHtml())
						.positiveText(android.R.string.ok)
						.build()
						.apply {
							LinkTextView().apply(contentView, activity)
							show()
						}
			}
			nightModePref -> {
				val oldValue = Preferences.nightMode
				MaterialDialog.Builder(context)
						.title(R.string.night_mode)
						.items(R.array.night_mode_pref_titles)
						.itemsCallbackSingleChoice(oldValue) { _, _, which, _ ->
							val oldPrefValue = Preferences.nightMode
							Preferences.nightMode = which
							preference?.summary = R.array.night_mode_pref_titles.resStrArr()!![Preferences.nightMode]
							if (which != oldPrefValue) {
								setNightMode()
								activity.recreate()
							}
							true
						}
						.show()
			}
			backupPref -> {
				backupRestore(activity as MainActivity, {})
			}
			syncIntervalPref -> {
				MaterialDialog.Builder(context)
						.title(R.string.sync_interval)
						.items(R.array.sync_interval_titles)
						.itemsCallbackSingleChoice(resources.getIntArray(R.array.sync_interval_values).indexOf(Preferences.syncInterval)) { _, _, which, _ ->
							Preferences.syncInterval = resources.getIntArray(R.array.sync_interval_values)[which]
							if (Preferences.syncEnabled) scheduleSync(Preferences.syncInterval) else cancelSync()
							refreshSyncIntervalDesc()
							true
						}
						.show()
			}
			syncNowPref -> {
				doAsync { sync(context) }
			}
			pocketLoginPref -> {
				val loggedIn = Preferences.pocketUserName.notNullOrBlank() && Preferences.pocketAccessToken.notNullOrBlank()
				if (loggedIn) {
					Preferences.pocketAccessToken = ""
					Preferences.pocketUserName = ""
					refreshPocket()
				} else {
					progressDialog?.show()
					pocketAuth = PocketAuth("pocketapp45699:authorizationFinished", object : PocketAuth.PocketAuthCallback {

						override fun authorize(url: String) {
							progressDialog?.dismiss()
							startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
						}

						override fun authenticated(accessToken: String, userName: String) {
							onUiThread {
								doAsync {
									val database = Database
									database.allBookmarks.apply {
										forEach {
											database.deleteBookmark(it.url ?: "")
										}
										Preferences.pocketAccessToken = accessToken
										Preferences.pocketUserName = userName
										forEach {
											database.addBookmark(it)
										}
									}
									uiThread {
										refreshPocket()
										progressDialog?.dismiss()
									}
								}
							}
						}

						override fun failed() {
							progressDialog?.dismiss()
						}

					}).apply { startAuth() }
				}
			}
			languagePref -> {
				val availableLocales = mutableSetOf<Locale>().apply {
					arrayOf("en", "de", "fa", "fr", "hr", "zh").forEach {
						add(Locale(it))
					}
				}
				MaterialDialog.Builder(context)
						.items(mutableSetOf<String>().apply {
							availableLocales.forEach { add(it.displayName) }
						})
						.itemsCallback { _, _, i, _ ->
							Preferences.language = availableLocales.toTypedArray()[i].language
							activity.setLocale()
						}
						.negativeText(android.R.string.cancel)
						.show()
			}
			issuePref -> UrlOpenener().openUrl("https://github.com/jlelse/NewsCatchr-OpenSource/issues", activity)
		}
		return true
	}

	override fun onPreferenceChange(preference: Preference?, value: Any?): Boolean {
		when (preference) {
			syncNowPref -> if (Preferences.syncEnabled) scheduleSync(Preferences.syncInterval) else cancelSync()
		}
		return true
	}

	private fun handlePurchases() {
		if (!(activity is MainActivity && (activity as MainActivity).IABReady && !Preferences.supportUser)) hideAllProPrefs()
	}

	private fun hideAllProPrefs() {
		supportPref?.isVisible = false
		donateCategory?.isVisible = false
	}

	private fun refreshLastSyncTime() {
		syncNowPref?.summary = "${R.string.last_suc_sync.resStr()}: " + when (Preferences.lastSync) {
			0.toLong() -> R.string.never.resStr()
			else -> DateUtils.getRelativeTimeSpanString(Preferences.lastSync)
		}
	}

	private fun refreshSyncIntervalDesc() {
		syncIntervalPref?.summary = R.array.sync_interval_titles.resStrArr()!![R.array.sync_interval_values.resIntArr()!!.indexOf(Preferences.syncInterval)]
	}

	private fun refreshNightModeDesc() {
		nightModePref?.summary = R.array.night_mode_pref_titles.resStrArr()!![Preferences.nightMode]
	}

	private fun refreshPocket() {
		val loggedIn = Preferences.pocketUserName.notNullOrBlank() && Preferences.pocketAccessToken.notNullOrBlank()
		pocketSyncPref?.isVisible = loggedIn
		pocketLoginPref?.let {
			it.title = (if (loggedIn) R.string.pocket_logout else R.string.pocket_login).resStr()
			it.summary = if (loggedIn) "${R.string.logged_in_as.resStr()} ${Preferences.pocketUserName}" else null
		}
	}

	override fun onSaveInstanceState(outState: Bundle?) {
		super.onSaveInstanceState(outState)
		saveValues(outState)
	}

	override fun onDestroy() {
		tryOrNull { activity.unregisterReceiver(purchaseReceiver) }
		tryOrNull { activity.unregisterReceiver(syncReceiver) }
		super.onDestroy()
	}

	private class PurchaseStatusUpdateReceiver(val fragment: SettingsFragment) : BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			fragment.handlePurchases()
		}
	}

	private class SyncStatusUpdateReceiver(val fragment: SettingsFragment) : BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			fragment.refreshLastSyncTime()
		}
	}
}
