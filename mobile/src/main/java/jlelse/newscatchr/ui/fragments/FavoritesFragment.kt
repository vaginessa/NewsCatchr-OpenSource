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
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.*
import com.afollestad.materialdialogs.MaterialDialog
import com.mcxiaoke.koi.ext.find
import com.mcxiaoke.koi.ext.readString
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter
import com.mikepenz.fastadapter_extensions.drag.ItemTouchCallback
import com.mikepenz.fastadapter_extensions.drag.SimpleDragCallback
import jlelse.newscatchr.backend.Feed
import jlelse.newscatchr.backend.apis.backupRestore
import jlelse.newscatchr.backend.helpers.Database
import jlelse.newscatchr.extensions.convertOpmlToFeeds
import jlelse.newscatchr.extensions.notNullAndEmpty
import jlelse.newscatchr.extensions.notNullOrBlank
import jlelse.newscatchr.extensions.sendBroadcast
import jlelse.newscatchr.ui.activities.MainActivity
import jlelse.newscatchr.ui.recycleritems.FeedListRecyclerItem
import jlelse.newscatchr.ui.views.SwipeRefreshLayout
import jlelse.readit.R
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.support.v4.onUiThread
import org.jetbrains.anko.uiThread
import java.util.*

class FavoritesFragment : BaseFragment(), ItemTouchCallback {
	private var fastAdapter: FastItemAdapter<FeedListRecyclerItem>? = null
	private var feeds: MutableList<Feed>? = null
	private var savedInstanceState: Bundle? = null
	private var recyclerOne: RecyclerView? = null
	private var refreshOne: SwipeRefreshLayout? = null

	override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		super.onCreateView(inflater, container, savedInstanceState)
		this.savedInstanceState = savedInstanceState
		val view = inflater?.inflate(R.layout.refreshrecycler, container, false)
		setHasOptionsMenu(true)
		recyclerOne = view?.find<RecyclerView>(R.id.recyclerOne)?.apply {
			layoutManager = LinearLayoutManager(context)
			ItemTouchHelper(SimpleDragCallback(this@FavoritesFragment)).attachToRecyclerView(this)
		}
		refreshOne = view?.find<SwipeRefreshLayout>(R.id.refreshOne)?.apply {
			setOnRefreshListener {
				load()
			}
		}
		load()
		return view
	}

	private fun load() {
		onUiThread {
			refreshOne?.isRefreshing = true
			feeds = Database.allFavorites.toMutableList()
			if (feeds.notNullAndEmpty()) {
				fastAdapter = FastItemAdapter<FeedListRecyclerItem>()
				recyclerOne?.adapter = fastAdapter
				fastAdapter?.setNewList(mutableListOf<FeedListRecyclerItem>())
				feeds?.forEach {
					fastAdapter?.add(FeedListRecyclerItem().withFeed(it).withFragment(this@FavoritesFragment).withAdapter(fastAdapter!!))
				}
				fastAdapter?.withSavedInstanceState(savedInstanceState)
			}
			refreshOne?.isRefreshing = false
		}
	}

	override fun itemTouchOnMove(oldPosition: Int, newPosition: Int): Boolean {
		Collections.swap(fastAdapter?.adapterItems, oldPosition, newPosition)
		fastAdapter?.notifyAdapterItemMoved(oldPosition, newPosition)
		Collections.swap(feeds, oldPosition, newPosition)
		if (feeds != null) Database.allFavorites = feeds!!.toTypedArray()
		sendBroadcast(Intent("favorites_updated"))
		return true
	}

	override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
		super.onCreateOptionsMenu(menu, inflater)
		inflater?.inflate(R.menu.backup, menu)
		inflater?.inflate(R.menu.favoritesfragment, menu)
	}

	override fun onOptionsItemSelected(item: MenuItem?): Boolean {
		when (item?.itemId) {
			R.id.backup -> {
				backupRestore(activity as MainActivity, {
					load()
				})
				return true
			}
			R.id.opml -> {
				importFromFile()
				return true
			}
			else -> {
				return super.onOptionsItemSelected(item)
			}
		}
	}

	private fun importFromFile() {
		val intent = Intent(Intent.ACTION_GET_CONTENT)
		intent.type = "*/*"
		intent.addCategory(Intent.CATEGORY_OPENABLE)
		startActivityForResult(intent, 555)
	}

	private fun importOpml(opml: String?) {
		onUiThread {
			doAsync {
				var imported = 0
				if (opml.notNullOrBlank()) {
					val feeds = opml?.convertOpmlToFeeds()
					Database.addFavorites(feeds)
					imported = feeds?.size ?: 0
				}
				uiThread {
					sendBroadcast(Intent("favorites_updated"))
					MaterialDialog.Builder(context)
							.title(R.string.import_opml)
							.content(if (imported != 0) R.string.suc_import else R.string.import_failed)
							.positiveText(android.R.string.ok)
							.show()
					load()
				}
			}
		}
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		if (resultCode == AppCompatActivity.RESULT_OK && requestCode == 555) {
			doAsync {
				var opml: String? = null
				if (data != null && data.data != null) opml = activity.contentResolver.openInputStream(data.data).readString()
				importOpml(opml)
			}
		}
	}

	override fun onSaveInstanceState(outState: Bundle?) {
		fastAdapter?.saveInstanceState(outState)
		super.onSaveInstanceState(outState)
	}
}