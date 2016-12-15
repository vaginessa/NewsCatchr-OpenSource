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

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import com.mcxiaoke.koi.ext.find
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter
import jlelse.newscatchr.backend.helpers.Database
import jlelse.newscatchr.backend.helpers.Preferences
import jlelse.newscatchr.backend.loaders.PocketLoader
import jlelse.newscatchr.extensions.notNullAndEmpty
import jlelse.newscatchr.extensions.notNullOrBlank
import jlelse.newscatchr.ui.recycleritems.ArticleListRecyclerItem
import jlelse.newscatchr.ui.views.SwipeRefreshLayout
import jlelse.readit.R
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class BookmarksFragment : BaseFragment() {
	private var recyclerOne: RecyclerView? = null
	private var refreshOne: SwipeRefreshLayout? = null
	private var fastAdapter: FastItemAdapter<ArticleListRecyclerItem>? = null
	private var savedInstanceState: Bundle? = null

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
		loadArticles(true)
		return view
	}

	fun loadArticles(cache: Boolean) {
		refreshOne?.showIndicator()
		doAsync {
			if (!cache && (Preferences.pocketSync && Preferences.pocketUserName.notNullOrBlank() && Preferences.pocketAccessToken.notNullOrBlank())) {
				try {
					val pocketItems = PocketLoader().items()
					Database.allBookmarks = pocketItems
				} catch (e: Exception) {
					e.printStackTrace()
				}
			}
			val articles = Database.allBookmarks
			uiThread {
				if (articles.notNullAndEmpty()) {
					fastAdapter = FastItemAdapter<ArticleListRecyclerItem>()
					recyclerOne?.adapter = fastAdapter
					fastAdapter?.setNewList(mutableListOf<ArticleListRecyclerItem>())
					articles.forEach {
						fastAdapter?.add(ArticleListRecyclerItem().withArticle(it).withFragment(this@BookmarksFragment))
					}
					fastAdapter?.withSavedInstanceState(savedInstanceState)
				} else {
					fastAdapter?.clear()
				}
				refreshOne?.hideIndicator()
			}
		}
	}

	override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
		super.onCreateOptionsMenu(menu, inflater)
		inflater?.inflate(R.menu.bookmarksfragment, menu)
	}

	override fun onOptionsItemSelected(item: MenuItem?): Boolean {
		when (item?.itemId) {
			R.id.refresh -> {
				loadArticles(false)
				return true
			}
			else -> {
				return super.onOptionsItemSelected(item)
			}
		}
	}

	override fun onSaveInstanceState(outState: Bundle?) {
		super.onSaveInstanceState(outState)
		fastAdapter?.saveInstanceState(outState)
	}
}
