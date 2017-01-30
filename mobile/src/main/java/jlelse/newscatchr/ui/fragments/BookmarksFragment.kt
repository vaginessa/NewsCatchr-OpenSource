/*
 * NewsCatchr  Copyright (C) 2016  Jan-Lukas Else
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package jlelse.newscatchr.ui.fragments

import android.os.Bundle
import android.support.v4.widget.NestedScrollView
import android.support.v7.widget.RecyclerView
import android.view.*
import co.metalab.asyncawait.async
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter
import jlelse.newscatchr.backend.helpers.Database
import jlelse.newscatchr.backend.helpers.Preferences
import jlelse.newscatchr.backend.loaders.PocketLoader
import jlelse.newscatchr.extensions.notNullAndEmpty
import jlelse.newscatchr.extensions.notNullOrBlank
import jlelse.newscatchr.extensions.restorePosition
import jlelse.newscatchr.extensions.tryOrNull
import jlelse.newscatchr.ui.layout.RefreshRecyclerUI
import jlelse.newscatchr.ui.recycleritems.ArticleListRecyclerItem
import jlelse.newscatchr.ui.views.SwipeRefreshLayout
import jlelse.readit.R
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.find

class BookmarksFragment : BaseFragment() {
	private var fragmentView: View? = null
	private val recyclerOne: RecyclerView? by lazy { fragmentView?.find<RecyclerView>(R.id.refreshrecyclerview_recycler) }
	private var fastAdapter = FastItemAdapter<ArticleListRecyclerItem>()
	private val scrollView: NestedScrollView? by lazy { fragmentView?.find<NestedScrollView>(R.id.refreshrecyclerview_scrollview) }
	private val refreshOne: SwipeRefreshLayout? by lazy { fragmentView?.find<SwipeRefreshLayout>(R.id.refreshrecyclerview_refresh) }

	override val saveStateScrollViews: Array<NestedScrollView?>?
		get() = arrayOf(scrollView)

	override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		super.onCreateView(inflater, container, savedInstanceState)
		fragmentView = fragmentView ?: RefreshRecyclerUI().createView(AnkoContext.create(context, this))
		setHasOptionsMenu(true)
		refreshOne?.setOnRefreshListener {
			loadArticles()
		}
		recyclerOne?.adapter = fastAdapter
		fastAdapter.withSavedInstanceState(savedInstanceState)
		loadArticles(true)
		return fragmentView
	}

	fun loadArticles(cache: Boolean = false) = async {
		refreshOne?.showIndicator()
		val articles = await {
			if (!cache && Preferences.pocketSync && Preferences.pocketUserName.notNullOrBlank() && Preferences.pocketAccessToken.notNullOrBlank()) {
				tryOrNull { Database.allBookmarks = PocketLoader().items() }
			}
			Database.allBookmarks
		}
		if (articles.notNullAndEmpty()) {
			fastAdapter.clear()
			articles.forEach {
				fastAdapter.add(ArticleListRecyclerItem().withArticle(it).withFragment(this@BookmarksFragment))
			}
			if (cache) scrollView?.restorePosition(this@BookmarksFragment)
		} else {
			fastAdapter.clear()
		}
		refreshOne?.hideIndicator()
	}

	override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
		super.onCreateOptionsMenu(menu, inflater)
		inflater?.inflate(R.menu.bookmarksfragment, menu)
	}

	override fun onOptionsItemSelected(item: MenuItem?): Boolean {
		when (item?.itemId) {
			R.id.refresh -> {
				loadArticles()
				return true
			}
			else -> {
				return super.onOptionsItemSelected(item)
			}
		}
	}

	override fun onSaveInstanceState(outState: Bundle?) {
		super.onSaveInstanceState(outState)
		fastAdapter.saveInstanceState(outState)
	}
}
