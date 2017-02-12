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

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * Everything Google Analytics related
 */
object Tracking {
	private var analyticsInstance: FirebaseAnalytics? = null

	fun init(context: Context) {
		analyticsInstance = FirebaseAnalytics.getInstance(context)
	}

	fun track(
			type: TYPE,
			url: String? = null,
			query: String? = null
	) {
		when (type) {
			TYPE.FEED -> {
				analyticsInstance?.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, Bundle().apply {
					putString(FirebaseAnalytics.Param.CONTENT_TYPE, "feed")
					putString(FirebaseAnalytics.Param.ITEM_ID, url)
				})
			}
			TYPE.MIX -> {
				analyticsInstance?.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, Bundle().apply {
					putString(FirebaseAnalytics.Param.CONTENT_TYPE, "mix")
					putString(FirebaseAnalytics.Param.ITEM_ID, url)
				})
			}
			TYPE.ARTICLE -> {
				analyticsInstance?.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, Bundle().apply {
					putString(FirebaseAnalytics.Param.CONTENT_TYPE, "article")
					putString(FirebaseAnalytics.Param.ITEM_ID, url)
				})
			}
			TYPE.FEED_SEARCH -> {
				analyticsInstance?.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, Bundle().apply {
					putString(FirebaseAnalytics.Param.SEARCH_TERM, query)
					putString("search_type", "feed")
				})
			}
			TYPE.ARTICLE_SEARCH -> {
				analyticsInstance?.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, Bundle().apply {
					putString(FirebaseAnalytics.Param.SEARCH_TERM, query)
					putString("search_type", "article")
				})
			}
		}
	}

	enum class TYPE {
		FEED,
		MIX,
		ARTICLE,
		FEED_SEARCH,
		ARTICLE_SEARCH
	}
}
