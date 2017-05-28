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
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import co.metalab.asyncawait.async
import com.afollestad.materialdialogs.MaterialDialog
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
	private val fastAdapterOne = FastItemAdapter<NCAbstractItem<*, *>>()
	private val fastAdapterTwo = CustomOrderAdapter<NCAbstractItem<*, *>>(order = 600)
	private val fastAdapterThree = CustomOrderAdapter<NCAbstractItem<*, *>>(order = 700)
	private val fastAdapterFour = CustomOrderAdapter<NCAbstractItem<*, *>>(order = 800)
	private val refresh: SwipeRefreshLayout? by lazy { fragmentView?.find<SwipeRefreshLayout>(R.id.homefragment_refresh) }
	private var feedStateUpdateReceiver: FeedStateUpdateReceiver? = null
	private var lastFeedReceiverRegistered = false

	override val fabDrawable = R.drawable.ic_search

	override val fabClick = {
		searchForFeeds(context)
	}

	override val expanded = true

	override fun onCreateView(): View? {
		super.onCreateView()
		if (!lastFeedReceiverRegistered) {
			feedStateUpdateReceiver = FeedStateUpdateReceiver(this)
			context.registerReceiver(feedStateUpdateReceiver, IntentFilter("feed_state"))
			lastFeedReceiverRegistered = true
		}
		fragmentView = HomeFragmentUI().createView(AnkoContext.create(context, this))
		refresh?.setOnRefreshListener { loadAll(false) }
		loadAll(true)
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

	private fun loadAll(cache: Boolean = true) {
		if (recyclerOne?.adapter == null) recyclerOne?.adapter = fastAdapterFour.wrap(fastAdapterThree.wrap(fastAdapterTwo.wrap(fastAdapterOne)))
		loadLastFeeds()
		loadFavoriteFeeds()
		loadRecommendedFeeds(cache)
	}

	private fun loadLastFeeds() = async {
		fastAdapterOne.setNewList(listOf())
		val lastFeeds = await { Database.allLastFeeds.takeLast(5).reversed() }
		if (lastFeeds.notNullAndEmpty()) {
			fastAdapterOne.add(HeaderRecyclerItem(title = R.string.last_feeds.resStr()!!))
			fastAdapterOne.add(lastFeeds.mapIndexed { i, feed -> FeedRecyclerItem(feed = feed, fragment = this@HomeView, isLast = i == lastFeeds.lastIndex) })
			fastAdapterOne.add(MoreRecyclerItem { openView(FeedListView(feeds = Database.allLastFeeds.reversed().toTypedArray()).withTitle(R.string.last_feeds.resStr())) })
		}
	}

	private fun loadFavoriteFeeds() = async {
		fastAdapterTwo.setNewList(listOf())
		val favoriteFeeds = await { Database.allFavorites.take(5) }
		if (favoriteFeeds.notNullAndEmpty()) {
			fastAdapterTwo.add(HeaderRecyclerItem(title = R.string.favorites.resStr()!!))
			fastAdapterTwo.add(favoriteFeeds.mapIndexed { i, feed -> FeedRecyclerItem(feed = feed, fragment = this@HomeView, isLast = i == favoriteFeeds.lastIndex) })
			fastAdapterTwo.add(MoreRecyclerItem { openView(FavoritesView().withTitle(R.string.favorites.resStr())) })
		}
	}

	private fun loadRecommendedFeeds(cache: Boolean = false) = async {
		fastAdapterThree.setNewList(listOf())
		fastAdapterFour.setNewList(listOf())
		refresh?.showIndicator()
		await {
			if (recFeeds == null || !cache) Feedly().recommendedFeeds(Preferences.recommendationsLanguage, cache) { feeds, related ->
				recFeeds = feeds
				recRelated = related
			}
		}
		val tempRecFeeds = recFeeds?.take(15)
		if (recFeeds.notNullAndEmpty() && tempRecFeeds != null) {
			fastAdapterThree.add(HeaderRecyclerItem(title = R.string.recommendations.resStr()!!))
			fastAdapterThree.add(tempRecFeeds.mapIndexed { i, feed -> FeedRecyclerItem(feed = feed, fragment = this@HomeView, isLast = i == tempRecFeeds.lastIndex) })
			fastAdapterThree.add(listOf(MoreRecyclerItem {
				openView(FeedListView(feeds = recFeeds, tags = recRelated).withTitle(R.string.recommendations.resStr()))
			}))
		}
		if (recRelated.notNullAndEmpty()) fastAdapterFour.add(TagsRecyclerItem(recRelated, this@HomeView))
		refresh?.hideIndicator()
	}

	override fun onDestroy() {
		tryOrNull { context.unregisterReceiver(feedStateUpdateReceiver) }
		super.onDestroy()
	}

	private class FeedStateUpdateReceiver(val homeView: HomeView) : BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			homeView.loadAll()
		}
	}
}