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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.support.v4.widget.NestedScrollView
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.TextView
import co.metalab.asyncawait.async
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.flexbox.FlexboxLayout
import com.mikepenz.fastadapter.adapters.HeaderAdapter
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter
import jlelse.newscatchr.backend.Feed
import jlelse.newscatchr.backend.apis.Feedly
import jlelse.newscatchr.backend.helpers.Database
import jlelse.newscatchr.backend.helpers.Preferences
import jlelse.newscatchr.extensions.*
import jlelse.newscatchr.ui.interfaces.FAB
import jlelse.newscatchr.ui.interfaces.FragmentManipulation
import jlelse.newscatchr.ui.layout.HomeFragmentUI
import jlelse.newscatchr.ui.recycleritems.FeedListRecyclerItem
import jlelse.newscatchr.ui.recycleritems.HeaderRecyclerItem
import jlelse.newscatchr.ui.recycleritems.MoreRecyclerItem
import jlelse.newscatchr.ui.recycleritems.NCAdapter
import jlelse.newscatchr.ui.views.SwipeRefreshLayout
import jlelse.newscatchr.ui.views.addTagView
import jlelse.readit.R
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.find
import java.util.*

class HomeFragment : BaseFragment(), FAB, FragmentManipulation {
	private var recFeeds: Array<Feed>? = null
	private var recRelated: Array<String>? = null
	private var fragmentView: View? = null
	private val recyclerOne: RecyclerView? by lazy { fragmentView?.find<RecyclerView>(R.id.homefragment_recyclerone) }
	private val recyclerTwo: RecyclerView? by lazy { fragmentView?.find<RecyclerView>(R.id.homefragment_recyclertwo) }
	private val recyclerThree: RecyclerView? by lazy { fragmentView?.find<RecyclerView>(R.id.homefragment_recyclerthree) }
	private val fastAdapterOne = FastItemAdapter<FeedListRecyclerItem>()
	private val headerAdapterOne = HeaderAdapter<HeaderRecyclerItem>()
	private val moreAdapterOne = NCAdapter<MoreRecyclerItem>(order = 1000)
	private val fastAdapterTwo = FastItemAdapter<FeedListRecyclerItem>()
	private val headerAdapterTwo = HeaderAdapter<HeaderRecyclerItem>()
	private val moreAdapterTwo = NCAdapter<MoreRecyclerItem>(order = 1000)
	private val fastAdapterThree = FastItemAdapter<FeedListRecyclerItem>()
	private val headerAdapterThree = HeaderAdapter<HeaderRecyclerItem>()
	private val moreAdapterThree = NCAdapter<MoreRecyclerItem>(order = 1000)
	private val tagsTitle: TextView? by lazy { fragmentView?.find<TextView>(R.id.homefragment_tagstitle) }
	private val tagsBox: FlexboxLayout? by lazy { fragmentView?.find<FlexboxLayout>(R.id.homefragment_tagsbox) }
	private val refresh: SwipeRefreshLayout? by lazy { fragmentView?.find<SwipeRefreshLayout>(R.id.homefragment_refresh) }
	private val scrollView: NestedScrollView? by lazy { fragmentView?.find<NestedScrollView>(R.id.homefragment_scrollview) }
	private var lastFeedReceiver: LastFeedUpdateReceiver? = null
	private var lastFeedReceiverRegistered = false
	private var favoritesReceiver: LastFeedUpdateReceiver? = null
	private var favoritesReceiverRegistered = false

	override val fabDrawable = R.drawable.ic_search

	override val fabClick = {
		searchForFeeds(context, fragmentNavigation)
	}

	override val expanded = true

	override val saveStateScrollViews: Array<NestedScrollView?>?
		get() = arrayOf(scrollView)

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		if (!lastFeedReceiverRegistered) {
			lastFeedReceiver = LastFeedUpdateReceiver(this)
			activity.registerReceiver(lastFeedReceiver, IntentFilter("last_feed_updated"))
			lastFeedReceiverRegistered = true
		}
		if (!favoritesReceiverRegistered) {
			favoritesReceiver = LastFeedUpdateReceiver(this)
			activity.registerReceiver(favoritesReceiver, IntentFilter("favorites_updated"))
			favoritesReceiverRegistered = true
		}
	}

	override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		super.onCreateView(inflater, container, savedInstanceState)
		setHasOptionsMenu(true)
		fragmentView = fragmentView ?: HomeFragmentUI().createView(AnkoContext.create(context, this))
		tagsTitle?.apply {
			hideView()
			text = R.string.rec_topics.resStr()
		}
		refresh?.setOnRefreshListener {
			loadLastFeeds()
			loadFavoriteFeeds()
			loadRecommendedFeeds()
		}
		if (recyclerOne?.adapter == null) {
			moreAdapterOne.wrap(fastAdapterOne)
			headerAdapterOne.wrap(moreAdapterOne)
			headerAdapterOne.add(HeaderRecyclerItem(title = R.string.last_feeds.resStr()!!))
			recyclerOne?.adapter = headerAdapterOne
		}
		if (recyclerTwo?.adapter == null) {
			moreAdapterTwo.wrap(fastAdapterTwo)
			headerAdapterTwo.wrap(moreAdapterTwo)
			headerAdapterTwo.add(HeaderRecyclerItem(title = R.string.favorites.resStr()!!))
			recyclerTwo?.adapter = headerAdapterTwo
		}
		if (recyclerThree?.adapter == null) {
			moreAdapterThree.wrap(fastAdapterThree)
			headerAdapterThree.wrap(moreAdapterThree)
			headerAdapterThree.add(HeaderRecyclerItem(title = R.string.recommendations.resStr()!!))
			recyclerThree?.adapter = headerAdapterThree
		}
		loadLastFeeds(true)
		loadFavoriteFeeds(true)
		loadRecommendedFeeds(true)
		return fragmentView
	}

	override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
		super.onCreateOptionsMenu(menu, inflater)
		inflater?.inflate(R.menu.homefragment, menu)
		menu?.findItem(R.id.favorites)?.icon = R.drawable.ic_favorite_universal.resDrw(context, Color.WHITE)
	}

	override fun onOptionsItemSelected(item: MenuItem?) = when (item?.itemId) {
		R.id.favorites -> {
			fragmentNavigation.pushFragment(FavoritesFragment(), R.string.favorites.resStr())
			true
		}
		R.id.language -> {
			val availableLocales = Locale.getAvailableLocales()
			MaterialDialog.Builder(context)
					.items(mutableListOf<String>().apply {
						availableLocales.forEach { add(it.displayName) }
					})
					.itemsCallback { _, _, i, _ ->
						Preferences.recommendationsLanguage = availableLocales[i].language
						loadRecommendedFeeds()
					}
					.negativeText(android.R.string.cancel)
					.show()
			true
		}
		else -> super.onOptionsItemSelected(item)
	}

	private fun loadLastFeeds(first: Boolean = false) = async {
		val lastFeeds = await { Database.allLastFeeds.takeLast(5).reversed() }
		fastAdapterOne.clear()
		moreAdapterOne.clear()
		if (lastFeeds.notNullAndEmpty()) {
			recyclerOne?.showView()
			lastFeeds.forEachIndexed { i, feed ->
				fastAdapterOne.add(FeedListRecyclerItem(feed = feed, fragment = this@HomeFragment, isLast = i == lastFeeds.lastIndex))
			}
			moreAdapterOne.add(MoreRecyclerItem {
				fragmentNavigation.pushFragment(FeedListFragment().apply {
					addObject("feeds", Database.allLastFeeds.reversed().toTypedArray())
				}, R.string.last_feeds.resStr())
			})
		} else recyclerOne?.hideView()
		if (first) restoreScrollState()
	}

	private fun loadFavoriteFeeds(first: Boolean = false) = async {
		val favoriteFeeds = await { Database.allFavorites.take(5) }
		fastAdapterTwo.clear()
		moreAdapterTwo.clear()
		if (favoriteFeeds.notNullAndEmpty()) {
			recyclerOne?.showView()
			favoriteFeeds.forEachIndexed { i, feed ->
				fastAdapterTwo.add(FeedListRecyclerItem(feed = feed, fragment = this@HomeFragment, isLast = i == favoriteFeeds.lastIndex))
			}
			moreAdapterTwo.add(MoreRecyclerItem {
				fragmentNavigation.pushFragment(FavoritesFragment(), R.string.favorites.resStr())
			})
		} else recyclerTwo?.hideView()
		if (first) restoreScrollState()
	}

	private fun loadRecommendedFeeds(cache: Boolean = false) = async {
		refresh?.showIndicator()
		await {
			if (recFeeds == null || !cache) Feedly().recommendedFeeds(Preferences.recommendationsLanguage, cache) { feeds, related ->
				recFeeds = feeds
				recRelated = related
			}
		}
		fastAdapterThree.clear()
		moreAdapterThree.clear()
		if (recFeeds.notNullAndEmpty()) {
			recyclerThree?.showView()
			recFeeds?.take(15)?.forEachIndexed { i, feed ->
				fastAdapterThree.add(FeedListRecyclerItem(feed = feed, fragment = this@HomeFragment, isLast = i == recFeeds?.take(15)?.lastIndex))
			}
			moreAdapterThree.add(MoreRecyclerItem {
				fragmentNavigation.pushFragment(FeedListFragment().apply {
					addObject("feeds", recFeeds)
					addObject("tags", recRelated)
				}, R.string.recommendations.resStr())
			})
		} else recyclerThree?.hideView()
		if (recRelated.notNullAndEmpty()) {
			tagsTitle?.showView()
			tagsBox?.showView()
			tagsBox?.removeAllViews()
			recRelated?.forEach { tagsBox?.addTagView(this@HomeFragment, it) }
		} else {
			tagsTitle?.hideView()
			tagsBox?.hideView()
		}
		refresh?.hideIndicator()
		if (cache) restoreScrollState()
	}

	private fun restoreScrollState() = scrollView?.restorePosition(this)

	override fun onDestroy() {
		tryOrNull { activity.unregisterReceiver(lastFeedReceiver) }
		tryOrNull { activity.unregisterReceiver(favoritesReceiver) }
		super.onDestroy()
	}

	private class LastFeedUpdateReceiver(val fragment: HomeFragment) : BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			fragment.loadLastFeeds()
		}
	}

	private class FavoritesUpdateReceiver(val fragment: HomeFragment) : BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			fragment.loadFavoriteFeeds()
		}
	}
}