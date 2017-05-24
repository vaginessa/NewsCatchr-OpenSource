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
import android.support.v4.widget.NestedScrollView
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
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
import jlelse.newscatchr.ui.recycleritems.*
import jlelse.newscatchr.ui.views.SwipeRefreshLayout
import jlelse.readit.R
import jlelse.viewmanager.ViewManagerView
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.find
import java.util.*

class HomeView : ViewManagerView(), FAB, FragmentManipulation {
	private var recFeeds: Array<Feed>? = null
	private var recRelated: Array<String>? = null
	private var fragmentView: View? = null
	private val recyclerOne: RecyclerView? by lazy { fragmentView?.find<RecyclerView>(R.id.homefragment_recyclerone) }
	private val recyclerTwo: RecyclerView? by lazy { fragmentView?.find<RecyclerView>(R.id.homefragment_recyclertwo) }
	private val recyclerThree: RecyclerView? by lazy { fragmentView?.find<RecyclerView>(R.id.homefragment_recyclerthree) }
	private val fastAdapterOne = FastItemAdapter<FeedRecyclerItem>()
	private val headerAdapterOne = HeaderAdapter<HeaderRecyclerItem>()
	private val moreAdapterOne = NCAdapter<MoreRecyclerItem>(order = 1000)
	private val fastAdapterTwo = FastItemAdapter<FeedRecyclerItem>()
	private val headerAdapterTwo = HeaderAdapter<HeaderRecyclerItem>()
	private val moreAdapterTwo = NCAdapter<MoreRecyclerItem>(order = 1000)
	private val fastAdapterThree = FastItemAdapter<FeedRecyclerItem>()
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
		searchForFeeds(context)
	}

	override val expanded = true

	override fun onCreateView(): View? {
		super.onCreateView()
		if (!lastFeedReceiverRegistered) {
			lastFeedReceiver = LastFeedUpdateReceiver(this)
			context.registerReceiver(lastFeedReceiver, IntentFilter("last_feed_updated"))
			lastFeedReceiverRegistered = true
		}
		if (!favoritesReceiverRegistered) {
			favoritesReceiver = LastFeedUpdateReceiver(this)
			context.registerReceiver(favoritesReceiver, IntentFilter("favorites_updated"))
			favoritesReceiverRegistered = true
		}
		fragmentView = HomeFragmentUI().createView(AnkoContext.create(context, this))
		tagsTitle?.apply {
			hideView()
			text = R.string.rec_topics.resStr()
		}
		refresh?.setOnRefreshListener {
			loadLastFeeds()
			loadFavoriteFeeds()
			loadRecommendedFeeds()
		}
		loadLastFeeds()
		loadFavoriteFeeds()
		loadRecommendedFeeds(true)
		return fragmentView
	}

	override fun inflateMenu(inflater: MenuInflater, menu: Menu?) {
		super.inflateMenu(inflater, menu)
		inflater.inflate(R.menu.homefragment, menu)
		menu?.findItem(R.id.favorites)?.icon = R.drawable.ic_favorite_universal.resDrw(context, Color.WHITE)
	}

	override fun onOptionsItemSelected(item: MenuItem?) {
		super.onOptionsItemSelected(item)
		when (item?.itemId) {
			R.id.favorites -> openView(FavoritesView().withTitle(R.string.favorites.resStr()))
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
			}
		}
	}

	private fun loadLastFeeds() = async {
		if (recyclerOne?.adapter == null) {
			moreAdapterOne.wrap(fastAdapterOne)
			headerAdapterOne.wrap(moreAdapterOne)
			headerAdapterOne.add(HeaderRecyclerItem(title = R.string.last_feeds.resStr()!!, ctx = context))
			recyclerOne?.adapter = headerAdapterOne
		}
		val lastFeeds = await { Database.allLastFeeds.takeLast(5).reversed() }
		fastAdapterOne.setNewList(lastFeeds.mapIndexed { i, feed -> FeedRecyclerItem(context, feed = feed, fragment = this@HomeView, isLast = i == lastFeeds.lastIndex) })
		moreAdapterOne.setNewList(listOf(MoreRecyclerItem(context) {
			openView(FeedListView(feeds = Database.allLastFeeds.reversed().toTypedArray()).withTitle(R.string.last_feeds.resStr()))
		}))
		if (lastFeeds.notNullAndEmpty()) recyclerOne?.showView() else recyclerOne?.hideView()
	}

	private fun loadFavoriteFeeds() = async {
		if (recyclerTwo?.adapter == null) {
			moreAdapterTwo.wrap(fastAdapterTwo)
			headerAdapterTwo.wrap(moreAdapterTwo)
			headerAdapterTwo.add(HeaderRecyclerItem(title = R.string.favorites.resStr()!!, ctx = context))
			recyclerTwo?.adapter = headerAdapterTwo
		}
		val favoriteFeeds = await { Database.allFavorites.take(5) }
		fastAdapterTwo.setNewList(favoriteFeeds.mapIndexed { i, feed -> FeedRecyclerItem(context, feed = feed, fragment = this@HomeView, isLast = i == favoriteFeeds.lastIndex) })
		moreAdapterTwo.setNewList(listOf(MoreRecyclerItem(context) {
			openView(FavoritesView().withTitle(R.string.favorites.resStr()))
		}))
		if (favoriteFeeds.notNullAndEmpty()) recyclerOne?.showView() else recyclerTwo?.hideView()
	}

	private fun loadRecommendedFeeds(cache: Boolean = false) = async {
		if (recyclerThree?.adapter == null) {
			moreAdapterThree.wrap(fastAdapterThree)
			headerAdapterThree.wrap(moreAdapterThree)
			headerAdapterThree.add(HeaderRecyclerItem(title = R.string.recommendations.resStr()!!, ctx = context))
			recyclerThree?.adapter = headerAdapterThree
		}
		refresh?.showIndicator()
		await {
			if (recFeeds == null || !cache) Feedly().recommendedFeeds(Preferences.recommendationsLanguage, cache) { feeds, related ->
				recFeeds = feeds
				recRelated = related
			}
		}
		val tempRecFeeds = recFeeds?.take(15)
		if (tempRecFeeds != null) {
			fastAdapterThree.setNewList(tempRecFeeds.mapIndexed { i, feed -> FeedRecyclerItem(context, feed = feed, fragment = this@HomeView, isLast = i == tempRecFeeds.lastIndex) })
			moreAdapterThree.setNewList(listOf(MoreRecyclerItem(context) {
				openView(FeedListView(feeds = recFeeds, tags = recRelated).withTitle(R.string.recommendations.resStr()))
			}))
		}
		if (recFeeds.notNullAndEmpty()) recyclerThree?.showView() else recyclerThree?.hideView()
		if (recRelated.notNullAndEmpty()) {
			tagsTitle?.showView()
			tagsBox?.showView()
			tagsBox?.removeAllViews()
			recRelated?.forEach { tagsBox?.addTagView(this@HomeView, it) }
		} else {
			tagsTitle?.hideView()
			tagsBox?.hideView()
		}
		refresh?.hideIndicator()
	}

	override fun onDestroy() {
		tryOrNull { context.unregisterReceiver(lastFeedReceiver) }
		tryOrNull { context.unregisterReceiver(favoritesReceiver) }
		super.onDestroy()
	}

	private class LastFeedUpdateReceiver(val fragment: HomeView) : BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			fragment.loadLastFeeds()
		}
	}

	private class FavoritesUpdateReceiver(val fragment: HomeView) : BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			fragment.loadFavoriteFeeds()
		}
	}
}