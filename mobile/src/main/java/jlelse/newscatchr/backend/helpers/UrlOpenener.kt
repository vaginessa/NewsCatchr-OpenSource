package jlelse.newscatchr.backend.helpers

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.support.customtabs.CustomTabsIntent
import com.mcxiaoke.koi.async.asyncUnsafe
import com.mcxiaoke.koi.async.mainThread
import jlelse.newscatchr.backend.apis.AmpApi
import jlelse.newscatchr.customTabsHelperFragment
import jlelse.newscatchr.extensions.resClr
import jlelse.readit.R
import me.zhanghai.android.customtabshelper.CustomTabsHelperFragment


class UrlOpenener {

	fun mayOpenUrl(url: String) = asyncUnsafe {
		val finalUrl = if (Preferences.amp) AmpApi().getAmpUrl(url) ?: url else url
		if (Preferences.customTabs) customTabsHelperFragment?.mayLaunchUrl(Uri.parse(finalUrl), null, null)
	}

	fun openUrl(url: String, activity: Activity) = asyncUnsafe {
		val finalUrl = if (Preferences.amp) AmpApi().getAmpUrl(url) ?: url else url
		mainThread {
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