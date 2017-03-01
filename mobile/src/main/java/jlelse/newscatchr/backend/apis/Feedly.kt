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

package jlelse.newscatchr.backend.apis

import com.afollestad.ason.Ason
import com.afollestad.bridge.Bridge
import jlelse.newscatchr.backend.Article
import jlelse.newscatchr.backend.Feed
import jlelse.newscatchr.backend.helpers.readFromCache
import jlelse.newscatchr.backend.helpers.saveToCache
import jlelse.newscatchr.extensions.notNullOrBlank
import jlelse.newscatchr.extensions.tryOrNull

class Feedly {

	private val BASE_URL = "https://cloud.feedly.com/v3"
	private val STREAM_ID = "streamId="
	private val CONTINUATION = "continuation="
	private val COUNT = "count="
	private val RANKED = "ranked="
	private val QUERY = "query="

	fun streamIds(id: String?, count: Int?, continuation: String?, ranked: String?): Ids? = tryOrNull {
		var url = "$BASE_URL/streams/ids?$STREAM_ID%s"
		if (count != null) url += "&$COUNT$count"
		if (continuation.notNullOrBlank()) url += "&$CONTINUATION$continuation"
		if (ranked.notNullOrBlank()) url += "&$RANKED$ranked"
		Bridge.get(url, id).asClass(Ids::class.java)
	}

	fun mixIds(id: String?, count: Int?): Ids? = tryOrNull {
		var url = "$BASE_URL/mixes/ids?$STREAM_ID%s"
		if (count != null) url += "&$COUNT$count"
		Bridge.get(url, id).asClass(Ids::class.java)
	}

	fun entries(ids: Array<String>): Array<Article>? = tryOrNull {
		if (ids.isNotEmpty()) {
			Bridge.post("$BASE_URL/entries/.mget").body(Ason.serializeArray<String>(ids)).asClassList(Article::class.java)?.toTypedArray()
		} else null
	}

	fun feedSearch(query: String?, count: Int?, locale: String?, promoted: Boolean?, callback: (feeds: Array<Feed>?, related: Array<String>?) -> Unit) {
		var feeds: Array<Feed>? = null
		var related: Array<String>? = null
		tryOrNull {
			var url = "$BASE_URL/search/feeds?$QUERY%s"
			if (count != null) url += "&$COUNT$count"
			if (locale.notNullOrBlank()) url += "&locale=$locale"
			if (promoted != null) url += "&promoted=$promoted"
			val search = Bridge.get(url, query).asClass(FeedSearch::class.java)
			feeds = search?.results
			related = search?.related
		}
		callback(feeds, related)
	}

	fun recommendedFeeds(locale: String?, cache: Boolean, callback: (feeds: Array<Feed>?, related: Array<String>?) -> Unit) {
		var feeds: Array<Feed>? = if (cache) readFromCache("recFeeds$locale", Array<Feed>::class.java) else null
		var related: Array<String>? = if (cache) readFromCache("recFeedsRelated$locale", Array<String>::class.java) else null
		if (!cache || feeds == null) {
			tryOrNull {
				feedSearch("news", 30, locale, true) { feedsTemp, relatedTemp ->
					feeds = feedsTemp?.take(30)?.toTypedArray().apply { saveToCache("recFeeds$locale") }
					related = relatedTemp?.apply { saveToCache("recFeedsRelated$locale") }
				}
			}
		}
		callback(feeds, related)
	}

	fun articleSearch(id: String?, query: String?): ArticleSearch? = tryOrNull {
		val url = "$BASE_URL/search/contents?$STREAM_ID%s&$QUERY%s&ct=feedly.desktop"
		Bridge.get(url, id, query).asClass(ArticleSearch::class.java)
	}

}

class Ids(
		var ids: Array<String>? = null,
		var continuation: String? = null
)

class FeedSearch(
		var results: Array<Feed>? = null,
		var related: Array<String>? = null
)

class ArticleSearch(
		var id: String? = null,
		var title: String? = null,
		var items: Array<Article>? = null
)
