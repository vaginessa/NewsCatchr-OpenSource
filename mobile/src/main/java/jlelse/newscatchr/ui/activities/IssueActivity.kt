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

package jlelse.newscatchr.ui.activities

import com.heinrichreimersoftware.androidissuereporter.IssueReporterActivity
import com.heinrichreimersoftware.androidissuereporter.model.github.ExtraInfo
import com.heinrichreimersoftware.androidissuereporter.model.github.GithubTarget
import jlelse.newscatchr.backend.helpers.Preferences
import jlelse.readit.BuildConfig

class IssueActivity : IssueReporterActivity() {

	override fun getTarget() = GithubTarget("jlelse", "NewsCatchr-OpenSource")

	override fun onSaveExtraInfo(extraInfo: ExtraInfo?) {
		extraInfo?.put("debug", BuildConfig.DEBUG)
		extraInfo?.put("subscribed", Preferences.supportUser)
	}

}