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

package jlelse.newscatchr.extensions

import android.content.Context
import com.afollestad.materialdialogs.MaterialDialog
import jlelse.newscatchr.backend.Article
import jlelse.newscatchr.backend.Feed
import jlelse.newscatchr.backend.apis.AutoCompleteAdapter
import jlelse.newscatchr.backend.apis.Feedly
import jlelse.newscatchr.ui.fragments.BaseFragment
import jlelse.newscatchr.ui.fragments.FeedListFragment
import jlelse.newscatchr.ui.views.ProgressDialog
import jlelse.newscatchr.ui.views.SearchDialogView
import jlelse.readit.R
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

fun List<Article?>.removeEmptyArticles(): List<Article> {
	return mutableListOf<Article>().apply {
		this@removeEmptyArticles.forEach { if (it.notNullOrEmpty()) add(it!!) }
	}
}

fun Array<out Article?>.removeEmptyArticles() = this.toList().removeEmptyArticles().toTypedArray()

fun List<Feed?>.removeEmptyFeeds(): List<Feed> {
	return mutableListOf<Feed>().apply {
		this@removeEmptyFeeds.forEach { if (it.notNullOrEmpty()) add(it!!) }
	}
}

fun Array<out Feed?>.removeEmptyFeeds() = this.toList().removeEmptyFeeds().toTypedArray()

fun Array<Feed>.onlySaved(): Array<Feed> = toMutableList().filter(Feed::saved).toTypedArray()

fun Feed?.notNullOrEmpty(): Boolean = this?.url().notNullOrBlank()

fun Article?.notNullOrEmpty(): Boolean = this?.process()?.url.notNullOrBlank()

fun searchForFeeds(context: Context, fragmentNavigation: BaseFragment.FragmentNavigation, query: String? = null) {
	val progressDialog = ProgressDialog(context)
	val load = { finalQuery: String ->
		progressDialog.show()
		context.doAsync {
			var foundFeeds: Array<Feed>? = null
			var foundRelated: Array<String>? = null
			Feedly().feedSearch(finalQuery, 100, null, null) { feeds, related ->
				foundFeeds = feeds
				foundRelated = related
			}
			uiThread {
				progressDialog.dismiss()
				if (foundFeeds.notNullAndEmpty()) {
					fragmentNavigation.pushFragment(FeedListFragment().apply {
						addObject("feeds", foundFeeds)
						addObject("tags", foundRelated)
					}, "${R.string.search_results_for.resStr()} $finalQuery")
				} else context.nothingFound()
			}
		}
	}
	if (query.isNullOrBlank()) {
		val textView = SearchDialogView(context)
		textView.setAdapter(AutoCompleteAdapter(context))
		MaterialDialog.Builder(context)
				.title(android.R.string.search_go)
				.customView(textView, true)
				.onPositive { _, _ ->
					load(textView.text.toString())
				}
				.negativeText(android.R.string.cancel)
				.positiveText(android.R.string.search_go)
				.show()
	} else {
		load(query!!)
	}
}