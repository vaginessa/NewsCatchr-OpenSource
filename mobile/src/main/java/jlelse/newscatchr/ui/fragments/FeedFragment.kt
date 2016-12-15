/*
 * NewsCatchr  Copyright (C) 2016  Jan-Lukas Else
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package jlelse.newscatchr.ui.fragments

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import com.afollestad.materialdialogs.MaterialDialog
import com.mcxiaoke.koi.ext.find
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter
import com.mikepenz.fastadapter_extensions.scroll.EndlessRecyclerOnScrollListener
import jlelse.newscatchr.backend.Article
import jlelse.newscatchr.backend.Feed
import jlelse.newscatchr.backend.helpers.Database
import jlelse.newscatchr.backend.helpers.Tracking
import jlelse.newscatchr.backend.loaders.FeedlyLoader
import jlelse.newscatchr.extensions.*
import jlelse.newscatchr.ui.activities.MainActivity
import jlelse.newscatchr.ui.recycleritems.ArticleListRecyclerItem
import jlelse.newscatchr.ui.views.ProgressDialog
import jlelse.newscatchr.ui.views.SwipeRefreshLayout
import jlelse.readit.R
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class FeedFragment() : BaseFragment() {
	private var recyclerOne: RecyclerView? = null
	private var refreshOne: SwipeRefreshLayout? = null
	private var fastAdapter: FastItemAdapter<ArticleListRecyclerItem>? = null
	private var articles: MutableList<Article>? = null
	private var savedInstanceState: Bundle? = null
	private var feed: Feed? = null
	private var favorite = false
	private var feedlyLoader: FeedlyLoader? = null
	private var editMenuItem: MenuItem? = null

	override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		super.onCreateView(inflater, container, savedInstanceState)
		this.savedInstanceState = savedInstanceState
		val view = inflater?.inflate(R.layout.refreshrecycler, container, false)
		setHasOptionsMenu(true)
		recyclerOne = view?.find<RecyclerView>(R.id.recyclerOne)?.apply {
			layoutManager = LinearLayoutManager(context)
		}
		refreshOne = view?.find<SwipeRefreshLayout>(R.id.refreshOne)?.apply {
			setOnRefreshListener {
				loadArticles(false)
			}
		}
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
		return view
	}

	private fun loadArticles(cache: Boolean) {
		refreshOne?.showIndicator()
		doAsync {
			if (articles == null || !cache) {
				articles = feedlyLoader?.items(cache)?.toMutableList()
				addString(feedlyLoader?.continuation, "continuation")
			}
			uiThread {
				if (articles.notNullAndEmpty()) {
					recyclerOne?.clearOnScrollListeners()
					fastAdapter = FastItemAdapter<ArticleListRecyclerItem>()
					recyclerOne?.adapter = fastAdapter
					fastAdapter?.setNewList(mutableListOf<ArticleListRecyclerItem>())
					articles?.forEach {
						fastAdapter?.add(ArticleListRecyclerItem().withArticle(it).withFragment(this@FeedFragment))
					}
					fastAdapter?.withSavedInstanceState(savedInstanceState)
					recyclerOne?.addOnScrollListener(object : EndlessRecyclerOnScrollListener() {
						override fun onLoadMore(currentPage: Int) {
							doAsync {
								val newArticles = feedlyLoader?.moreItems()
								addString(feedlyLoader?.continuation, "continuation")
								if (newArticles != null) articles?.addAll(newArticles)
								uiThread {
									newArticles?.forEach {
										fastAdapter?.add(ArticleListRecyclerItem().withArticle(it).withFragment(this@FeedFragment))
									}
								}
							}
						}
					})
				} else {
					context.nothingFound()
					fragmentNavigation.popFragment()
				}
				refreshOne?.hideIndicator()
			}
		}
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
				if (favorite) {
					Database.addFavorite(feed)
				} else {
					Database.deleteFavorite(feed?.url())
				}
				item?.icon = (if (favorite) R.drawable.ic_favorite_universal else R.drawable.ic_favorite_border_universal).resDrw(context, Color.WHITE)
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
						}) { dialog, itemView, which, text ->
							feedlyLoader?.apply {
								continuation = ""
								ranked = when (which) {
									1 -> FeedlyLoader.Ranked.OLDEST
									else -> FeedlyLoader.Ranked.NEWEST
								}
							}
							articles = null
							loadArticles(true)
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
						.input(null, null, { materialDialog, query ->
							progressDialog.show()
							doAsync {
								val foundArticles = FeedlyLoader().apply {
									type = FeedlyLoader.FeedTypes.SEARCH
									feedUrl = "feed/" + feed?.url()
									this.query = query.toString()
								}.items(false)
								uiThread {
									progressDialog.dismiss()
									if (foundArticles.notNullAndEmpty()) {
										fragmentNavigation.pushFragment(ArticleSearchResultFragment().addObject(foundArticles?.toList(), "articles"), "Results for " + query.toString())
									} else {
										context.nothingFound()
									}
								}
							}
						})
						.negativeText(android.R.string.cancel)
						.positiveText(android.R.string.search_go)
						.show()
				true
			}
			R.id.refresh -> {
				loadArticles(false)
				true
			}
			R.id.edit_title -> {
				MaterialDialog.Builder(context)
						.title(R.string.edit_feed_title)
						.input(null, feed?.title, { materialDialog, input ->
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
				Snackbar.make(activity.findViewById(R.id.container), R.string.shortcut_created, Snackbar.LENGTH_SHORT).show()
				true
			}
			else -> {
				super.onOptionsItemSelected(item)
			}
		}
	}

	override fun onSaveInstanceState(outState: Bundle?) {
		fastAdapter?.saveInstanceState(outState)
		super.onSaveInstanceState(outState)
	}
}
