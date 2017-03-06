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

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.*
import co.metalab.asyncawait.async
import com.afollestad.materialdialogs.MaterialDialog
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter
import com.mikepenz.fastadapter_extensions.drag.ItemTouchCallback
import com.mikepenz.fastadapter_extensions.drag.SimpleDragCallback
import jlelse.newscatchr.backend.Feed
import jlelse.newscatchr.backend.apis.backupRestore
import jlelse.newscatchr.backend.helpers.Database
import jlelse.newscatchr.extensions.*
import jlelse.newscatchr.ui.activities.MainActivity
import jlelse.newscatchr.ui.layout.RefreshRecyclerUI
import jlelse.newscatchr.ui.recycleritems.FeedListRecyclerItem
import jlelse.newscatchr.ui.views.StatefulRecyclerView
import jlelse.newscatchr.ui.views.SwipeRefreshLayout
import jlelse.readit.R
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.find
import org.jetbrains.anko.support.v4.onUiThread
import java.util.*

class FavoritesFragment : BaseFragment(), ItemTouchCallback {
	private var fragmentView: View? = null
	private val recyclerOne: StatefulRecyclerView? by lazy { fragmentView?.find<StatefulRecyclerView>(R.id.refreshrecyclerview_recycler) }
	private var fastAdapter = FastItemAdapter<FeedListRecyclerItem>()
	private val refreshOne: SwipeRefreshLayout? by lazy { fragmentView?.find<SwipeRefreshLayout>(R.id.refreshrecyclerview_refresh) }
	private var feeds: MutableList<Feed>? = null

	override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		super.onCreateView(inflater, container, savedInstanceState)
		fragmentView = fragmentView ?: RefreshRecyclerUI().createView(AnkoContext.create(context, this))
		setHasOptionsMenu(true)
		ItemTouchHelper(SimpleDragCallback(this)).attachToRecyclerView(recyclerOne)
		if (recyclerOne?.adapter == null) recyclerOne?.adapter = fastAdapter
		refreshOne?.setOnRefreshListener {
			load()
		}
		load(true)
		return fragmentView
	}

	private fun load(first: Boolean = false) {
		feeds = Database.allFavorites.toMutableList()
		if (feeds.notNullAndEmpty()) {
			fastAdapter.clear()
			feeds?.forEach {
				fastAdapter.add(FeedListRecyclerItem(feed = it, fragment = this@FavoritesFragment, adapter = fastAdapter))
			}
			if (first) recyclerOne?.restorePosition()
		} else {
			fastAdapter.clear()
		}
		refreshOne?.hideIndicator()
	}

	override fun itemTouchOnMove(oldPosition: Int, newPosition: Int): Boolean {
		Collections.swap(fastAdapter.adapterItems, oldPosition, newPosition)
		fastAdapter.notifyAdapterItemMoved(oldPosition, newPosition)
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
					onUiThread { load() }
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

	private fun importOpml(opml: String?) = async {
		var imported = 0
		var feeds: Array<Feed>?
		if (opml.notNullOrBlank()) await {
			feeds = opml?.convertOpmlToFeeds()
			feeds?.forEach { Database.addFavorite(it) }
			imported = feeds?.size ?: 0
		}
		sendBroadcast(Intent("favorites_updated"))
		MaterialDialog.Builder(context)
				.title(R.string.import_opml)
				.content(if (imported != 0) R.string.suc_import else R.string.import_failed)
				.positiveText(android.R.string.ok)
				.show()
		load()
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		if (resultCode == AppCompatActivity.RESULT_OK && requestCode == 555) async {
			var opml: String? = null
			if (data != null && data.data != null) opml = await { activity.contentResolver.openInputStream(data.data).readString() }
			importOpml(opml)
		}
	}
}