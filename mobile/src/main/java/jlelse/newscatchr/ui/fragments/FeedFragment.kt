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

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.widget.NestedScrollView
import android.support.v7.widget.RecyclerView
import android.view.*
import co.metalab.asyncawait.async
import com.afollestad.materialdialogs.MaterialDialog
import com.mikepenz.fastadapter.adapters.FooterAdapter
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter
import com.mikepenz.fastadapter_extensions.items.ProgressItem
import com.mikepenz.fastadapter_extensions.scroll.EndlessRecyclerOnScrollListener
import jlelse.newscatchr.backend.Article
import jlelse.newscatchr.backend.Feed
import jlelse.newscatchr.backend.helpers.Database
import jlelse.newscatchr.backend.helpers.Tracking
import jlelse.newscatchr.backend.loaders.FeedlyLoader
import jlelse.newscatchr.extensions.*
import jlelse.newscatchr.ui.activities.MainActivity
import jlelse.newscatchr.ui.layout.RefreshRecyclerUI
import jlelse.newscatchr.ui.recycleritems.ArticleListRecyclerItem
import jlelse.newscatchr.ui.views.ProgressDialog
import jlelse.newscatchr.ui.views.SwipeRefreshLayout
import jlelse.readit.R
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.find
import org.jetbrains.anko.support.v4.onUiThread

class FeedFragment : BaseFragment() {
	private var fragmentView: View? = null
	private val recyclerOne: RecyclerView? by lazy { fragmentView?.find<RecyclerView>(R.id.refreshrecyclerview_recycler) }
	private val fastAdapter = FastItemAdapter<ArticleListRecyclerItem>()
	private val footerAdapter = FooterAdapter<ProgressItem>()
	private val scrollView: NestedScrollView? by lazy { fragmentView?.find<NestedScrollView>(R.id.refreshrecyclerview_scrollview) }
	private val refreshOne: SwipeRefreshLayout? by lazy { fragmentView?.find<SwipeRefreshLayout>(R.id.refreshrecyclerview_refresh) }
	private var articles = mutableListOf<Article>()
	private var feed: Feed? = null
	private var favorite = false
	private var feedlyLoader: FeedlyLoader? = null
	private var editMenuItem: MenuItem? = null

	override val saveStateScrollViews: Array<NestedScrollView?>?
		get() = arrayOf(scrollView)

	override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		super.onCreateView(inflater, container, savedInstanceState)
		fragmentView = fragmentView ?: RefreshRecyclerUI().createView(AnkoContext.create(context, this))
		setHasOptionsMenu(true)
		refreshOne?.setOnRefreshListener {
			loadArticles()
		}
		recyclerOne?.adapter = footerAdapter.wrap(fastAdapter)
		fastAdapter.withSavedInstanceState(savedInstanceState)
		feed = getAddedObject<Feed>("feed")
		favorite = Database.isSavedFavorite(feed?.url())
		feedlyLoader = FeedlyLoader().apply {
			type = FeedlyLoader.FeedTypes.FEED
			feedUrl = "feed/" + feed?.url()
			continuation = getAddedString("continuation")
			ranked = when (getAddedString("ranked")) {
				"oldest" -> FeedlyLoader.Ranked.OLDEST
				else -> FeedlyLoader.Ranked.NEWEST
			}
		}
		loadArticles(true)
		Tracking.track(type = Tracking.TYPE.FEED, url = feed?.url())
		Database.addLastFeed(feed)
		sendBroadcast(Intent("last_feed_updated"))
		return fragmentView
	}

	private fun loadArticles(cache: Boolean = false) = async {
		refreshOne?.showIndicator()
		if (articles.isEmpty() || !cache) await {
			feedlyLoader?.items(cache)?.let {
				articles.clear()
				articles.addAll(it)
			}
			addString(feedlyLoader?.continuation, "continuation")
		}
		if (articles.notNullAndEmpty()) {
			recyclerOne?.clearOnScrollListeners()
			fastAdapter.clear()
			fastAdapter.add(mutableListOf<ArticleListRecyclerItem>().apply {
				articles.forEach { add(ArticleListRecyclerItem().withArticle(it).withFragment(this@FeedFragment)) }
			})
			recyclerOne?.addOnScrollListener(object : EndlessRecyclerOnScrollListener(footerAdapter) {
				override fun onLoadMore(currentPage: Int) {
					doAsync {
						val newArticles = feedlyLoader?.moreItems()
						addString(feedlyLoader?.continuation, "continuation")
						if (newArticles != null) articles.addAll(newArticles)
						onUiThread {
							newArticles?.forEach {
								fastAdapter.add(ArticleListRecyclerItem().withArticle(it).withFragment(this@FeedFragment))
							}
						}
					}
				}
			})
			if (cache) scrollView?.restorePosition(this@FeedFragment)
		} else {
			context.nothingFound()
			fragmentNavigation.popFragment()
		}
		refreshOne?.hideIndicator()
	}

	override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
		super.onCreateOptionsMenu(menu, inflater)
		inflater?.inflate(R.menu.feedfragment, menu)
		menu?.findItem(R.id.favorite)?.icon = (if (favorite) R.drawable.ic_favorite_universal else R.drawable.ic_favorite_border_universal).resDrw(context, Color.WHITE)
		editMenuItem = menu?.findItem(R.id.edit_title)
		editMenuItem?.isVisible = favorite
	}

	override fun onOptionsItemSelected(item: MenuItem?): Boolean {
		return when (item?.itemId) {
			R.id.favorite -> {
				favorite = !favorite
				feed?.saved = favorite
				if (favorite) Database.addFavorite(feed)
				else Database.deleteFavorite(feed?.url())
				item.icon = (if (favorite) R.drawable.ic_favorite_universal else R.drawable.ic_favorite_border_universal).resDrw(context, Color.WHITE)
				editMenuItem?.isVisible = favorite
				true
			}
			R.id.sort -> {
				MaterialDialog.Builder(context)
						.title(R.string.sort)
						.items(R.string.newest_first.resStr(), R.string.oldest_first.resStr())
						.itemsCallbackSingleChoice(when (getAddedString("ranked")) {
							"oldest" -> 1
							else -> 0
						}) { _, _, which, _ ->
							feedlyLoader?.apply {
								continuation = ""
								ranked = when (which) {
									1 -> FeedlyLoader.Ranked.OLDEST
									else -> FeedlyLoader.Ranked.NEWEST
								}
							}
							articles.clear()
							loadArticles()
							addString(when (feedlyLoader?.ranked) {
								FeedlyLoader.Ranked.OLDEST -> "oldest"
								else -> "newest"
							}, "ranked")
							true
						}
						.positiveText(R.string.set)
						.negativeText(android.R.string.cancel)
						.show()
				true
			}
			R.id.search -> {
				val progressDialog = ProgressDialog(context)
				MaterialDialog.Builder(context)
						.title(android.R.string.search_go)
						.input(null, null, { _, query ->
							async {
								progressDialog.show()
								val foundArticles = await {
									FeedlyLoader().apply {
										type = FeedlyLoader.FeedTypes.SEARCH
										feedUrl = "feed/" + feed?.url()
										this.query = query.toString()
									}.items(false)
								}
								progressDialog.dismiss()
								if (foundArticles.notNullAndEmpty()) fragmentNavigation.pushFragment(ArticleSearchResultFragment().addObject(foundArticles?.toList(), "articles"), "Results for " + query.toString())
								else context.nothingFound()
							}
						})
						.negativeText(android.R.string.cancel)
						.positiveText(android.R.string.search_go)
						.show()
				true
			}
			R.id.refresh -> {
				loadArticles()
				true
			}
			R.id.edit_title -> {
				MaterialDialog.Builder(context)
						.title(R.string.edit_feed_title)
						.input(null, feed?.title, { _, input ->
							if (input.toString().notNullOrBlank()) {
								Database.updateFavoriteTitle(feed?.url(), input.toString())
								feed?.title = input.toString()
								if (feed != null) addObject(feed!!, "feed")
								addTitle(feed?.title)
								val curActivity = activity
								if (curActivity is MainActivity) curActivity.refreshFragmentDependingTitle(this)
							}
						})
						.negativeText(android.R.string.cancel)
						.positiveText(android.R.string.ok)
						.show()
				true
			}
			R.id.create_shortcut -> {
				if (activity is MainActivity) (activity as MainActivity).createHomeScreenShortcut(getAddedTitle() ?: R.string.app_name.resStr()!!, feed?.url() ?: "")
				Snackbar.make(activity.findViewById(R.id.mainactivity_container), R.string.shortcut_created, Snackbar.LENGTH_SHORT).show()
				true
			}
			else -> {
				super.onOptionsItemSelected(item)
			}
		}
	}

	override fun onSaveInstanceState(outState: Bundle?) {
		fastAdapter.saveInstanceState(outState)
		super.onSaveInstanceState(outState)
	}
}
