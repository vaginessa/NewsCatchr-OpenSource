/*
 * NewsCatchr  Copyright (C) 2016  Jan-Lukas Else
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package jlelse.newscatchr.ui.customtabsutils

import android.app.Activity
import android.net.Uri
import android.support.customtabs.CustomTabsIntent

object CustomTabActivityHelper {

    fun openCustomTab(activity: Activity, customTabsIntent: CustomTabsIntent, uri: Uri, fallback: CustomTabFallback?) {
        val packageName = CustomTabsHelper.getPackageNameToUse(activity)
        if (packageName == null) {
            fallback?.openUri(activity, uri)
        } else {
            customTabsIntent.intent.`package` = packageName
            customTabsIntent.launchUrl(activity, uri)
        }
    }

    interface CustomTabFallback {
        fun openUri(activity: Activity, uri: Uri)
    }

}
