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

@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package jlelse.newscatchr.backend.helpers

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.support.customtabs.CustomTabsIntent
import co.metalab.asyncawait.async
import jlelse.newscatchr.backend.apis.AmpApi
import jlelse.newscatchr.extensions.resClr
import jlelse.readit.R
import me.zhanghai.android.customtabshelper.CustomTabsHelperFragment

class UrlOpenener {

	fun openUrl(url: String, activity: Activity) = async {
		val finalUrl = await { if (Preferences.amp) AmpApi().getAmpUrl(url) ?: url else url }
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