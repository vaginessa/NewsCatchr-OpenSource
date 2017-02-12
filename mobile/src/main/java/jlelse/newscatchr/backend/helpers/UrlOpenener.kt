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

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.support.customtabs.CustomTabsIntent
import jlelse.newscatchr.backend.apis.AmpApi
import jlelse.newscatchr.customTabsHelperFragment
import jlelse.newscatchr.extensions.resClr
import jlelse.readit.R
import me.zhanghai.android.customtabshelper.CustomTabsHelperFragment
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread


class UrlOpenener {

	fun mayOpenUrl(url: String) = doAsync {
		val finalUrl = if (Preferences.amp) AmpApi().getAmpUrl(url) ?: url else url
		if (Preferences.customTabs) customTabsHelperFragment?.mayLaunchUrl(Uri.parse(finalUrl), null, null)
	}

	fun openUrl(url: String, activity: Activity) = doAsync {
		val finalUrl = if (Preferences.amp) AmpApi().getAmpUrl(url) ?: url else url
		uiThread {
			val alternateIntent = Intent(Intent.ACTION_VIEW, Uri.parse(finalUrl))
			if (Preferences.customTabs) {
				try {
					val customTabsIntent = CustomTabsIntent.Builder()
							.setToolbarColor(R.color.colorPrimary.resClr(activity)!!)
							.setShowTitle(true)
							.addDefaultShareMenuItem()
							.enableUrlBarHiding()
							.build()
					CustomTabsHelperFragment.open(activity, customTabsIntent, Uri.parse(finalUrl)) { activity, _ ->
						activity.startActivity(alternateIntent)
					}
				} catch (e: Exception) {
					activity.startActivity(alternateIntent)
				}
			} else {
				activity.startActivity(alternateIntent)
			}
		}
	}

}