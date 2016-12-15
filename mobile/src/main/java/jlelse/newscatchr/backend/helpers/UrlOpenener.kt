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
					CustomTabsHelperFragment.open(activity, customTabsIntent, Uri.parse(finalUrl)) { activity, uri ->
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