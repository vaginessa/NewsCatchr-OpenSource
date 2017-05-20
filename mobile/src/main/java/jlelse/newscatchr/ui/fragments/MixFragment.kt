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

@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package jlelse.newscatchr.ui.fragments

import android.os.Bundle
import android.view.*
import co.metalab.asyncawait.async
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter
import jlelse.newscatchr.backend.Article
import jlelse.newscatchr.backend.helpers.Tracking
import jlelse.newscatchr.backend.loaders.FeedlyLoader
import jlelse.newscatchr.extensions.nothingFound
import jlelse.newscatchr.ui.layout.RefreshRecyclerUI
import jlelse.newscatchr.ui.recycleritems.ArticleRecyclerItem
import jlelse.newscatchr.ui.views.StatefulRecyclerView
import jlelse.newscatchr.ui.views.SwipeRefreshLayout
import jlelse.readit.R
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.find

class MixFragment : BaseFragment() {
	private var fragmentView: View? = null
	private val recyclerOne: StatefulRecyclerView? by lazy { fragmentView?.find<StatefulRecyclerView>(R.id.refreshrecyclerview_recycler) }
	private var fastAdapter = FastItemAdapter<ArticleRecyclerItem>()
	private val refreshOne: SwipeRefreshLayout? by lazy { fragmentView?.find<SwipeRefreshLayout>(R.id.refreshrecyclerview_refresh) }
	private val feedId by lazy { getAddedString("feedId") }
	private var articles: List<Article>
		get() = getAddedObject("articles") ?: listOf()
		set(value) = addObject("articles", value)
	private var feedlyLoader: FeedlyLoader? = null

	override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		super.onCreateView(inflater, container, savedInstanceState)
		fragmentView = fragmentView ?: RefreshRecyclerUI().createView(AnkoContext.create(context, this))
		setHasOptionsMenu(true)
		refreshOne?.setOnRefreshListener {
			loadArticles(false)
		}
		feedlyLoader = FeedlyLoader().apply {
			type = FeedlyLoader.FeedTypes.MIX
			feedUrl = feedId
		}
		if (recyclerOne?.adapter == null) recyclerOne?.adapter = fastAdapter
		loadArticles(true)
		Tracking.track(type = Tracking.TYPE.MIX, url = feedId)
		return fragmentView
	}

	private fun loadArticles(cache: Boolean = false) = async {
		refreshOne?.showIndicator()
		if (!cache) await { feedlyLoader?.items(cache)?.let { articles = it } }
		if (!articles.isEmpty()) {
			fastAdapter.setNewList(articles.map { ArticleRecyclerItem(ctx = context, article = it, fragment = this@MixFragment) })
			if (cache) recyclerOne?.restorePosition()
		} else context.nothingFound {
			fragmentNavigation.popFragment()
		}
		refreshOne?.hideIndicator()
	}

	override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
		super.onCreateOptionsMenu(menu, inflater)
		inflater?.inflate(R.menu.mixfragment, menu)
	}

	override fun onOptionsItemSelected(item: MenuItem?): Boolean {
		return when (item?.itemId) {
			R.id.refresh -> {
				loadArticles(false)
				true
			}
			else -> super.onOptionsItemSelected(item)
		}
	}
}
