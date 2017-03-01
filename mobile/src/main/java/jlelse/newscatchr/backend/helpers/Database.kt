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

import jlelse.newscatchr.backend.Article
import jlelse.newscatchr.backend.Feed
import jlelse.newscatchr.backend.apis.Pocket
import jlelse.newscatchr.extensions.*
import org.jetbrains.anko.doAsync

/**
 * Database
 */
object Database {

	private val FAVORITES = "feeds_database"
	private val favoritesStore = KeyObjectStore(FAVORITES)
	private val BOOKMARKS = "bookmarks_database"
	private val bookmarksStore = KeyObjectStore(BOOKMARKS)
	private val READ_URLS = "urls_database"
	private val readUrlsStore = KeyObjectStore(READ_URLS)
	private val LAST_FEEDS = "last_feeds"
	private val lastFeedsStore = KeyObjectStore(LAST_FEEDS)

	var allFavorites: Array<Feed>
		get() = favoritesStore.read(FAVORITES, Array<Feed>::class.java) ?: arrayOf<Feed>()
		set(value) {
			tryOrNull { favoritesStore.write<Array<Feed>>(FAVORITES, value.onlySaved()) }
		}

	val allFavoritesUrls = allFavorites.map(Feed::url)

	fun addFavorite(feed: Feed?) {
		if (feed.notNullOrEmpty() && !isSavedFavorite(feed?.url())) allFavorites += feed!!
	}

	fun deleteFavorite(url: String?) {
		if (url.notNullOrBlank()) allFavorites = allFavorites.filterNot { it.url() == url }.toTypedArray()
	}

	fun updateFavoriteTitle(feedUrl: String?, newTitle: String?) {
		if (feedUrl.notNullOrBlank() && newTitle.notNullOrBlank()) {
			allFavorites = allFavorites.apply {
				forEach {
					if (it.url() == feedUrl) it.title = newTitle
				}
			}
		}
	}

	var allBookmarks: Array<Article>
		get() = bookmarksStore.read(BOOKMARKS, Array<Article>::class.java) ?: arrayOf<Article>()
		set(value) {
			tryOrNull { bookmarksStore.write<Array<Article>>(BOOKMARKS, value.removeEmptyArticles()) }
		}

	val allBookmarkUrls = allBookmarks.map { it.url }

	private fun addBookmarks(vararg articles: Article?) {
		articles.removeEmptyArticles().filter { !isSavedBookmark(it.url) }.let {
			allBookmarks += it
		}
	}

	fun addBookmark(article: Article?) {
		tryOrNull(execute = article != null) {
			if (Preferences.pocketSync && Preferences.pocketUserName.notNullOrBlank() && Preferences.pocketAccessToken.notNullOrBlank()) {
				doAsync {
					article!!.pocketId = PocketHandler().addToPocket(article)
					article.fromPocket = true
					addBookmarks(article)
				}
			} else {
				addBookmarks(article)
			}
		}
	}

	fun deleteBookmark(url: String?) {
		tryOrNull(execute = url.notNullOrBlank()) {
			allBookmarks.filter { it.url == url }.forEach {
				val pocket = Preferences.pocketSync && Preferences.pocketUserName.notNullOrBlank() && Preferences.pocketAccessToken.notNullOrBlank()
				if (pocket && it.fromPocket) doAsync {
					PocketHandler().archiveOnPocket(it)
				}
			}
			allBookmarks = allBookmarks.filterNot { it.url == url }.toTypedArray()
		}
	}

	var allReadUrls: Array<String>
		get() = readUrlsStore.read(READ_URLS, Array<String>::class.java) ?: arrayOf<String>()
		set(value) {
			tryOrNull { readUrlsStore.write<Array<String>>(READ_URLS, value.cleanNullable()) }
		}

	fun addReadUrl(url: String?) {
		if (url.notNullOrBlank() && !isSavedReadUrl(url)) allReadUrls += url!!
	}

	var allLastFeeds: Array<Feed>
		get() = lastFeedsStore.read(LAST_FEEDS, Array<Feed>::class.java) ?: arrayOf<Feed>()
		set(value) {
			tryOrNull { lastFeedsStore.write<Array<Feed>>(LAST_FEEDS, value.removeEmptyFeeds()) }
		}

	val allLastFeedUrls = allLastFeeds.map(Feed::url)

	fun addLastFeed(feed: Feed?) {
		if (feed?.url().notNullOrBlank() && !isLastFeed(feed?.url())) allLastFeeds += feed!!
	}

	fun isSavedFavorite(url: String?) = url.notNullOrBlank() && allFavoritesUrls.contains(url)

	fun isSavedBookmark(url: String?) = url.notNullOrBlank() && allBookmarkUrls.contains(url)

	fun isSavedReadUrl(url: String?) = url.notNullOrBlank() && allReadUrls.contains(url)

	fun isLastFeed(url: String?) = url.notNullOrBlank() && allLastFeedUrls.contains(url)

	// Helpers

	class PocketHandler {

		fun addToPocket(item: Article) = tryOrNull { Pocket().add(item.url!!) }

		fun archiveOnPocket(item: Article) = tryOrNull { Pocket().archive(item.pocketId!!) }

	}

}
