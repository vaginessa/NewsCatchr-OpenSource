package jlelse.newscatchr.backend.helpers

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.support.customtabs.CustomTabsIntent
import com.mcxiaoke.koi.async.asyncSafe
import com.mcxiaoke.koi.async.mainThreadSafe
import jlelse.newscatchr.backend.apis.AmpApi
import jlelse.newscatchr.extensions.resClr
import jlelse.newscatchr.ui.customtabsutils.CustomTabActivityHelper
import jlelse.newscatchr.ui.customtabsutils.Fallback
import jlelse.readit.R


class UrlOpenener {

	fun openUrl(url: String, activity: Activity) {
		asyncSafe {
			val finalUrl = if (Preferences.amp) AmpApi().getAmpUrl(url) ?: url else url
			mainThreadSafe {
				val alternateIntent = Intent(Intent.ACTION_VIEW, Uri.parse(finalUrl))
				if (Preferences.customTabs) {
					try {
						val customTabsIntent = CustomTabsIntent.Builder()
								.setToolbarColor(R.color.colorPrimary.resClr(activity)!!)
								.setShowTitle(true)
								.addDefaultShareMenuItem()
								.enableUrlBarHiding()
								.build()
						CustomTabActivityHelper.openCustomTab(activity, customTabsIntent, Uri.parse(finalUrl), Fallback())
					} catch (e: Exception) {
						e.printStackTrace()
						activity.startActivity(alternateIntent)
					}
				} else {
					activity.startActivity(alternateIntent)
				}
			}
		}
	}

}