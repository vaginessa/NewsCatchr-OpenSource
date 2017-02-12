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

package jlelse.newscatchr

import android.app.Application
import android.content.Context
import android.support.v7.app.AppCompatDelegate
import com.cloudrail.si.CloudRail
import com.evernote.android.job.JobManager
import io.paperdb.Paper
import jlelse.newscatchr.backend.apis.CloudRailApiKey
import jlelse.newscatchr.backend.helpers.Preferences
import jlelse.newscatchr.backend.helpers.SyncJob
import jlelse.newscatchr.backend.helpers.cancelSync
import jlelse.newscatchr.backend.helpers.scheduleSync
import jlelse.newscatchr.extensions.setLocale
import jlelse.newscatchr.extensions.setNightMode
import me.zhanghai.android.customtabshelper.CustomTabsHelperFragment

/**
 * Application class
 */
class NewsCatchr : Application() {
	override fun onCreate() {
		super.onCreate()
		appContext = applicationContext
		setLocale()
		Paper.init(this)
		AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
		setNightMode()
		JobManager.create(this@NewsCatchr).addJobCreator { tag ->
			when (tag) {
				SyncJob.TAG -> SyncJob()
				else -> null
			}
		}
		if (Preferences.syncEnabled) scheduleSync(Preferences.syncInterval) else cancelSync()
		Paper.book("hosts").destroy()
		CloudRail.setAppKey(CloudRailApiKey)
		// CloudRail.setAdvancedAuthenticationMode(true)
	}
}

var appContext: Context? = null
var customTabsHelperFragment: CustomTabsHelperFragment? = null
var lastTab = 0