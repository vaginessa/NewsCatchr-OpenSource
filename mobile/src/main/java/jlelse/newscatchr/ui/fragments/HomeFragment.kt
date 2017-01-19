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
import jlelse.newscatchr.ui.recycleritems.MoreAdapter
import jlelse.newscatchr.ui.recycleritems.MoreRecyclerItem
import jlelse.newscatchr.ui.views.SwipeRefreshLayout
import jlelse.newscatchr.ui.views.addTagView
import jlelse.readit.R
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.find
import org.jetbrains.anko.uiThread
import java.util.*

class HomeFragment : BaseFragment(), FAB, FragmentManipulation {
	private var fastAdapterOne: FastItemAdapter<FeedListRecyclerItem>? = null
	private var fastAdapterTwo: FastItemAdapter<FeedListRecyclerItem>? = null
	private var fastAdapterThree: FastItemAdapter<FeedListRecyclerItem>? = null
	private var recFeeds: Array<Feed>? = null
	private var recRelated: Array<String>? = null
	private var fragmentView: View? = null
	private val recyclerOne: RecyclerView? by lazy { fragmentView?.find<RecyclerView>(R.id.homefragment_recyclerone) }
	private val recyclerTwo: RecyclerView? by lazy { fragmentView?.find<RecyclerView>(R.id.homefragment_recyclertwo) }
	private val recyclerThree: RecyclerView? by lazy { fragmentView?.find<RecyclerView>(R.id.homefragment_recyclerthree) }
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

	override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		super.onCreateView(inflater, container, savedInstanceState)
		setHasOptionsMenu(true)
		fragmentView = HomeFragmentUI().createView(AnkoContext.create(context, this))
		tagsTitle?.apply {
			hideView()
			text = R.string.rec_topics.resStr()
		}
		refresh?.apply {
			setOnRefreshListener {
				loadRecommendedFeeds(false)
			}
		}
		loadRecommendedFeeds(true)
		loadLastFeeds()
		loadFavoriteFeeds()
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
						loadRecommendedFeeds(false)
					}
					.negativeText(android.R.string.cancel)
					.show()
			true
		}
		else -> super.onOptionsItemSelected(item)
	}

	private fun loadLastFeeds() {
		doAsync {
			val lastFeeds = Database.allLastFeeds.toTypedArray().takeLast(5).reversed()
			uiThread {
				if (lastFeeds.notNullAndEmpty()) {
					recyclerOne?.showView()
					fastAdapterOne = FastItemAdapter<FeedListRecyclerItem>()
					val headerAdapter = HeaderAdapter<HeaderRecyclerItem>()
					val moreAdapter = MoreAdapter<MoreRecyclerItem>()
					recyclerOne?.adapter = headerAdapter.wrap(moreAdapter.wrap(fastAdapterOne))
					fastAdapterOne?.setNewList(mutableListOf<FeedListRecyclerItem>())
					headerAdapter.add(HeaderRecyclerItem().withTitle(R.string.last_feeds.resStr()!!))
					lastFeeds.forEachIndexed { i, feed ->
						fastAdapterOne?.add(FeedListRecyclerItem().withFeed(feed).withFragment(this@HomeFragment).withIsLast(i == lastFeeds.lastIndex))
					}
					moreAdapter.add(MoreRecyclerItem().withCallback {
						fragmentNavigation.pushFragment(FeedListFragment().addObject(Database.allLastFeeds.reversed().toTypedArray(), "feeds"), R.string.last_feeds.resStr())
					})
				} else {
					recyclerOne?.hideView()
				}
				restoreScrollState()
			}
		}
	}

	private fun loadFavoriteFeeds() {
		doAsync {
			val favoriteFeeds = Database.allFavorites.take(5)
			uiThread {
				if (favoriteFeeds.notNullAndEmpty()) {
					recyclerTwo?.showView()
					fastAdapterTwo = FastItemAdapter<FeedListRecyclerItem>()
					val headerAdapter = HeaderAdapter<HeaderRecyclerItem>()
					val moreAdapter = MoreAdapter<MoreRecyclerItem>()
					recyclerTwo?.adapter = headerAdapter.wrap(moreAdapter.wrap(fastAdapterTwo))
					fastAdapterTwo?.setNewList(mutableListOf<FeedListRecyclerItem>())
					headerAdapter.add(HeaderRecyclerItem().withTitle(R.string.favorites.resStr()!!))
					favoriteFeeds.forEachIndexed { i, feed ->
						fastAdapterTwo?.add(FeedListRecyclerItem().withFeed(feed).withFragment(this@HomeFragment).withIsLast(i == favoriteFeeds.lastIndex))
					}
					moreAdapter.add(MoreRecyclerItem().withCallback {
						fragmentNavigation.pushFragment(FavoritesFragment(), R.string.favorites.resStr())
					})
				} else {
					recyclerTwo?.hideView()
				}
				restoreScrollState()
			}
		}
	}

	private fun loadRecommendedFeeds(cache: Boolean) {
		refresh?.showIndicator()
		doAsync {
			if (recFeeds == null || !cache) Feedly().recommendedFeeds(Preferences.recommendationsLanguage, cache) { feeds, related ->
				recFeeds = feeds
				recRelated = related
			}
			uiThread {
				if (recFeeds.notNullAndEmpty()) {
					recyclerThree?.showView()
					fastAdapterThree = FastItemAdapter<FeedListRecyclerItem>()
					val headerAdapter = HeaderAdapter<HeaderRecyclerItem>()
					val moreAdapter = MoreAdapter<MoreRecyclerItem>()
					recyclerThree?.adapter = headerAdapter.wrap(moreAdapter.wrap(fastAdapterThree))
					fastAdapterThree?.setNewList(mutableListOf<FeedListRecyclerItem>())
					headerAdapter.add(HeaderRecyclerItem().withTitle(R.string.recommendations.resStr()!!))
					recFeeds?.take(15)?.forEachIndexed { i, feed ->
						fastAdapterThree?.add(FeedListRecyclerItem().withFeed(feed).withFragment(this@HomeFragment).withIsLast(i == recFeeds?.take(15)?.lastIndex))
					}
					moreAdapter.add(MoreRecyclerItem().withCallback {
						fragmentNavigation.pushFragment(FeedListFragment().addObject(recFeeds, "feeds").addObject(recRelated, "tags"), R.string.recommendations.resStr())
					})
				} else {
					recyclerThree?.hideView()
				}
				if (recRelated.notNullAndEmpty()) {
					tagsTitle?.showView()
					tagsBox?.showView()
					tagsBox?.removeAllViews()
					recRelated?.forEach {
						tagsBox?.addTagView(this@HomeFragment, it)
					}
				} else {
					tagsTitle?.hideView()
					tagsBox?.hideView()
				}
				refresh?.hideIndicator()
				restoreScrollState()
			}
		}
	}

	private fun restoreScrollState() {
		scrollView?.restorePosition(this)
	}

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