/*
 * NewsCatchr  Copyright (C) 2016  Jan-Lukas Else
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package jlelse.newscatchr.backend.helpers

import android.content.Context
import com.google.android.gms.analytics.GoogleAnalytics
import com.google.android.gms.analytics.HitBuilders
import com.google.android.gms.analytics.Tracker
import jlelse.newscatchr.extensions.resBool
import jlelse.readit.R
import java.util.*

/**
 * Everything Google Analytics related
 */
class Tracking {

    object GATracker {

        fun track(url: String?, type: TYPE) {
            if (R.bool.debug_mode.resBool() == false) {
                val t = AnalyticsTrackers.instance?.get(AnalyticsTrackers.Target.APP)
                t?.send(HitBuilders.EventBuilder(when (type) {
                    TYPE.FEED -> "feed"
                    TYPE.MIX -> "mix"
                    TYPE.ARTICLE -> "article"
                    TYPE.FEED_SEARCH -> "feed_search"
                    TYPE.ARTICLE_SEARCH -> "article_search"
                }, url).build())
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

    class AnalyticsTrackers private constructor(context: Context) {
        private val mTrackers = HashMap<Target, Tracker>()
        private val mContext: Context

        init {
            mContext = context.applicationContext
        }

        @Synchronized operator fun get(target: Target): Tracker? {
            if (!mTrackers.containsKey(target)) {
                val tracker: Tracker
                when (target) {
                    Target.APP -> tracker = GoogleAnalytics.getInstance(mContext).newTracker(R.xml.app_tracker)
                    else -> throw IllegalArgumentException("Unhandled analytics target " + target)
                }
                mTrackers.put(target, tracker)
            }
            return mTrackers[target]
        }

        enum class Target {
            APP
        }

        companion object {

            private var sInstance: AnalyticsTrackers? = null

            @Synchronized fun initialize(context: Context) {
                if (sInstance != null) {
                    throw IllegalStateException("Extra call to initialize analytics trackers")
                }
                sInstance = AnalyticsTrackers(context)
            }

            val instance: AnalyticsTrackers?
                @Synchronized get() {
                    if (sInstance == null) {
                        throw IllegalStateException("Call initialize() before getInstance()")
                    }
                    return sInstance
                }
        }
    }

}
