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

import com.mcxiaoke.koi.async.asyncUnsafe
import io.paperdb.Paper
import jlelse.newscatchr.backend.Article
import jlelse.newscatchr.backend.Feed
import jlelse.newscatchr.backend.apis.Pocket
import jlelse.newscatchr.extensions.*

/**
 * Database
 */
class Database {

	private val FAVORITES = "feeds_database"
	private val BOOKMARKS = "bookmarks_database"
	private val READ_URLS = "urls_database"
	private val LAST_FEEDS = "last_feeds"

	var allFavorites: Array<Feed>
		get() = try {
			Paper.book(FAVORITES).read<Array<Feed>>(FAVORITES)
		} catch (e: Exception) {
			e.printStackTrace()
			arrayOf<Feed>()
		}
		set(value) {
			try {
				Paper.book(FAVORITES).write(FAVORITES, value.onlySaved())
			} catch (e: Exception) {
				e.printStackTrace()
			}
		}

	val allFavoritesUrls = mutableListOf<String>().apply { allFavorites.forEach { add(it.url()!!) } }.toTypedArray()

	fun addFavorite(feed: Feed?) = addFavorites(arrayOf(feed))

	fun addFavorites(feeds: Array<out Feed?>?) {
		allFavorites = allFavorites.toMutableList().apply { feeds?.removeEmptyFeeds()?.forEach { if (!isSavedFavorite(it.url())) add(it) } }.toTypedArray()
	}

	fun deleteFavorite(url: String?) {
		if (url.notNullOrBlank()) allFavorites = allFavorites.toMutableList().filterNot { it.url() == url }.toTypedArray()
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
		get() = try {
			Paper.book(BOOKMARKS).read<Array<Article>>(BOOKMARKS)
		} catch (e: Exception) {
			e.printStackTrace()
			arrayOf<Article>()
		}
		set(value) {
			try {
				Paper.book(BOOKMARKS).write<Array<Article>>(BOOKMARKS, value.removeEmptyArticles())
			} catch (e: Exception) {
				e.printStackTrace()
			}
		}

	val allBookmarkUrls = mutableListOf<String>().apply { allBookmarks.forEach { add(it.url!!) } }.toTypedArray()

	private fun addBookmarks(vararg articles: Article?) {
		allBookmarks = allBookmarks.toMutableList().apply {
			articles.removeEmptyArticles().forEach { if (!isSavedBookmark(it.url)) add(0, it) }
		}.toTypedArray()
	}

	fun addBookmark(article: Article?) {
		if (article != null) {
			try {
				if (Preferences.pocketSync && Preferences.pocketUserName.notNullOrBlank() && Preferences.pocketAccessToken.notNullOrBlank()) {
					asyncUnsafe {
						article.pocketId = PocketHandler().addToPocket(article)
						article.fromPocket = true
						addBookmarks(article)
					}
				} else {
					addBookmarks(article)
				}
			} catch (e: Exception) {
				e.printStackTrace()
			}
		}
	}

	fun deleteBookmark(url: String?) {
		if (url.notNullOrBlank()) {
			try {
				allBookmarks.toMutableList().filter { it.url == url }.forEach {
					val pocket = Preferences.pocketSync && Preferences.pocketUserName.notNullOrBlank() && Preferences.pocketAccessToken.notNullOrBlank()
					if (pocket && it.fromPocket) {
						asyncUnsafe {
							PocketHandler().archiveOnPocket(it)
						}
					}
				}
				allBookmarks = allBookmarks.toMutableList().filterNot { it.url == url }.toTypedArray()
			} catch(e: Exception) {
				e.printStackTrace()
			}
		}
	}

	var allReadUrls: Array<String>
		get() = try {
			Paper.book(READ_URLS).read<Array<String>>(READ_URLS)
		} catch (e: Exception) {
			e.printStackTrace()
			arrayOf<String>()
		}
		set(value) {
			try {
				Paper.book(READ_URLS).write(READ_URLS, value.removeBlankStrings())
			} catch (e: Exception) {
				e.printStackTrace()
			}
		}

	fun addReadUrl(url: String?) {
		allReadUrls = allReadUrls.toMutableList().apply { if (url.notNullOrBlank() == true) add(url!!) }.toTypedArray()
	}

	var allLastFeeds: Array<Feed>
		get() = try {
			Paper.book(LAST_FEEDS).read<Array<Feed>>(LAST_FEEDS)
		} catch (e: Exception) {
			e.printStackTrace()
			arrayOf<Feed>()
		}
		set(value) {
			try {
				Paper.book(LAST_FEEDS).write(LAST_FEEDS, value.removeEmptyFeeds().takeLast(50).toTypedArray())
			} catch (e: Exception) {
				e.printStackTrace()
			}
		}

	fun addLastFeed(feed: Feed?) {
		allLastFeeds = allLastFeeds.toMutableList().apply { if (feed != null && tryOrNull { allLastFeeds.last().url() } != feed.url()) add(feed) }.toTypedArray()
	}

	fun isSavedFavorite(url: String?) = url.notNullOrBlank() && allFavoritesUrls.contains(url)

	fun isSavedBookmark(url: String?) = url.notNullOrBlank() && allBookmarkUrls.contains(url)

	fun isSavedReadUrl(url: String?) = url.notNullOrBlank() && allReadUrls.contains(url)

	// Helpers

	class PocketHandler {

		fun addToPocket(item: Article) = tryOrNull { Pocket().add(item.url!!) }

		fun archiveOnPocket(item: Article) = tryOrNull { Pocket().archive(item.pocketId!!) }

	}

}
