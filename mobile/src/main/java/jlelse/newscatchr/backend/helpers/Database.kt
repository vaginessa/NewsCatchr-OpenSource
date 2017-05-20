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
import jlelse.newscatchr.extensions.notNullOrBlank
import jlelse.newscatchr.extensions.tryOrNull
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

	private fun Feed?.safeFavorite() = this != null && !this.url().isNullOrBlank()

	var allFavorites: Array<Feed>
		get() = favoritesStore.read(FAVORITES, Array<Feed>::class.java) ?: arrayOf<Feed>()
		set(value) {
			tryOrNull { favoritesStore.write<Array<Feed>>(FAVORITES, value.filter { it.safeFavorite() }.distinctBy { it.url() }.toTypedArray()) }
		}

	val allFavoritesUrls = allFavorites.map { it.url() }

	fun addFavorites(vararg feeds: Feed?) {
		allFavorites += feeds.filterNotNull().filter { !isSavedFavorite(it.url()) }
	}

	fun deleteFavorite(url: String?) {
		allFavorites = allFavorites.filter { it.url() != url }.toTypedArray()
	}

	fun updateFavoriteTitle(feedUrl: String?, newTitle: String?) {
		if (!feedUrl.isNullOrBlank() && !newTitle.isNullOrBlank()) {
			allFavorites = allFavorites.toMutableList().onEach {
				if (it.url() == feedUrl) it.title = newTitle
			}.toTypedArray()
		}
	}

	private fun Article?.safeBookmark() = this != null && !this.url.isNullOrBlank()

	var allBookmarks: Array<Article>
		get() = bookmarksStore.read(BOOKMARKS, Array<Article>::class.java) ?: arrayOf<Article>()
		set(value) {
			tryOrNull { bookmarksStore.write<Array<Article>>(BOOKMARKS, value.filter { it.safeBookmark() }.distinctBy { it.url }.toTypedArray()) }
		}

	val allBookmarkUrls = allBookmarks.map { it.url }

	private fun addBookmarks(vararg articles: Article?) {
		allBookmarks += articles.filterNotNull().filter { !isSavedBookmark(it.url) }
	}

	fun addBookmark(article: Article?) {
		tryOrNull(execute = article.safeBookmark()) {
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
			if (Preferences.pocketSync && !Preferences.pocketUserName.isNullOrBlank() && !Preferences.pocketAccessToken.isNullOrBlank())
				allBookmarks.filter { it.url == url }.forEach {
					if (it.fromPocket) doAsync {
						PocketHandler().archiveOnPocket(it)
					}
				}
			allBookmarks = allBookmarks.filter { it.url != url }.toTypedArray()
		}
	}

	var allReadUrls: Array<String>
		get() = readUrlsStore.read(READ_URLS, Array<String>::class.java) ?: arrayOf<String>()
		set(value) {
			tryOrNull { readUrlsStore.write<Array<String>>(READ_URLS, value.filterNotNull().distinct().toTypedArray()) }
		}

	fun addReadUrl(url: String?) {
		if (url != null) allReadUrls += url
	}

	private fun Feed?.safeLastFeed() = this != null && !this.url().isNullOrBlank()

	var allLastFeeds: Array<Feed>
		get() = lastFeedsStore.read(LAST_FEEDS, Array<Feed>::class.java) ?: arrayOf<Feed>()
		set(value) {
			tryOrNull { lastFeedsStore.write<Array<Feed>>(LAST_FEEDS, value.filterNotNull().filter { it.safeLastFeed() }.toTypedArray()) }
		}

	fun addLastFeed(feed: Feed?) {
		if (feed != null) {
			allLastFeeds = allLastFeeds.filter { it.url() != feed.url() }.toTypedArray()
			allLastFeeds += feed
		}
	}

	fun isSavedFavorite(url: String?) = allFavoritesUrls.contains(url)

	fun isSavedBookmark(url: String?) = allBookmarkUrls.contains(url)

	fun isSavedReadUrl(url: String?) = allReadUrls.contains(url)

	class PocketHandler {

		fun addToPocket(item: Article) = tryOrNull { Pocket().add(item.url!!) }

		fun archiveOnPocket(item: Article) = tryOrNull { Pocket().archive(item.pocketId!!) }

	}

}
