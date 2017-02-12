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

package jlelse.newscatchr.backend.loaders

import jlelse.newscatchr.backend.Article
import jlelse.newscatchr.backend.apis.Feedly
import jlelse.newscatchr.backend.apis.Ids
import jlelse.newscatchr.backend.helpers.ArticleCache
import jlelse.newscatchr.backend.helpers.readFromCache
import jlelse.newscatchr.backend.helpers.saveToCache
import jlelse.newscatchr.extensions.notNullAndEmpty
import jlelse.newscatchr.extensions.removeBlankStrings
import jlelse.newscatchr.extensions.removeEmptyArticles

class FeedlyLoader {
	var type: FeedTypes? = null
	var count = 20
	var query: String? = null
	var feedUrl: String? = null
	var ranked: Ranked = Ranked.NEWEST
	var continuation: String? = null

	private val articleCache by lazy { ArticleCache() }

	fun items(cache: Boolean): Array<Article>? = when (type) {
		FeedTypes.MIX -> {
			var ids: Ids? = if (cache) readFromCache("MixIds$feedUrl" + when (ranked) {
				Ranked.OLDEST -> "oldest"
				else -> ""
			}) else null
			if (ids == null) {
				ids = Feedly().mixIds(feedUrl, count).apply {
					saveToCache("MixIds$feedUrl" + when (ranked) {
						Ranked.OLDEST -> "oldest"
						else -> ""
					})
				}
			}
			itemsByIds(ids?.ids, cache)
		}
		FeedTypes.FEED -> {
			var ids: Ids? = if (cache) readFromCache("StreamIds$feedUrl" + when (ranked) {
				Ranked.OLDEST -> "oldest"
				else -> ""
			}) else null
			if (ids == null) {
				ids = Feedly().streamIds(feedUrl, count, null, when (ranked) {
					Ranked.NEWEST -> "newest"
					Ranked.OLDEST -> "oldest"
				}).apply {
					saveToCache("StreamIds$feedUrl" + when (ranked) {
						Ranked.OLDEST -> "oldest"
						else -> ""
					})
				}
			}
			continuation = ids?.continuation
			itemsByIds(ids?.ids, cache)
		}
		FeedTypes.SEARCH -> {
			Feedly().articleSearch(feedUrl, query)?.items
		}
		else -> null
	}?.apply {
		forEach {
			ArticleCache().save(it.process())
		}
	}?.removeEmptyArticles()

	fun moreItems(): Array<Article>? = itemsByIds(
			Feedly().streamIds(feedUrl, count, continuation, when (ranked) {
				Ranked.NEWEST -> "newest"
				Ranked.OLDEST -> "oldest"
			})?.apply {
				this@FeedlyLoader.continuation = continuation
			}?.ids, true
	)?.apply {
		forEach {
			ArticleCache().save(it.process())
		}
	}

	private fun itemsByIds(ids: Array<String>?, cache: Boolean): Array<Article>? = if (ids.notNullAndEmpty()) {
		ids!!.removeBlankStrings().filter { if (cache) !articleCache.isCached(it) else true }.let {
			if (it.notNullAndEmpty()) Feedly().entries(it.toTypedArray())?.forEach {
				articleCache.save(it)
			}
		}
		mutableListOf<Article>().apply {
			ids.removeBlankStrings().forEach {
				articleCache.getById(it)?.let { add(it) }
			}
		}.toTypedArray()
	} else null

	enum class FeedTypes {
		FEED,
		SEARCH,
		MIX
	}

	enum class Ranked {
		NEWEST,
		OLDEST
	}

}
