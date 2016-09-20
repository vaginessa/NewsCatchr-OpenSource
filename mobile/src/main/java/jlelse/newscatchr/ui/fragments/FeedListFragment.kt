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
import android.support.v4.widget.NestedScrollView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.flexbox.FlexboxLayout
import com.mcxiaoke.koi.ext.find
import com.mikepenz.fastadapter.adapters.FastItemAdapter
import jlelse.newscatchr.backend.Feed
import jlelse.newscatchr.extensions.getAddedObject
import jlelse.newscatchr.extensions.hideView
import jlelse.newscatchr.extensions.notNullAndEmpty
import jlelse.newscatchr.extensions.showView
import jlelse.newscatchr.ui.recycleritems.FeedListRecyclerItem
import jlelse.newscatchr.ui.views.addTagView
import jlelse.readit.R

class FeedListFragment() : BaseFragment() {
	private var feeds: Array<Feed>? = null
	private var tags: Array<String>? = null
	private var fastAdapter: FastItemAdapter<FeedListRecyclerItem>? = null
	private var scrollView: NestedScrollView? = null

	override val saveStateScrollViews: Array<NestedScrollView?>?
		get() = arrayOf(scrollView)

	override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		super.onCreateView(inflater, container, savedInstanceState)
		val view = inflater?.inflate(R.layout.basicrecycler, container, false)
		val recyclerOne = view?.find<RecyclerView>(R.id.recyclerOne)?.apply {
			isNestedScrollingEnabled = false
			layoutManager = LinearLayoutManager(context)
		}
		scrollView = view?.find<NestedScrollView>(R.id.scrollView)
		val tagsBox = view?.find<FlexboxLayout>(R.id.tagsBox)
		feeds = getAddedObject<Array<Feed>>("feeds")
		if (feeds.notNullAndEmpty()) {
			fastAdapter = FastItemAdapter<FeedListRecyclerItem>()
			recyclerOne?.adapter = fastAdapter
			fastAdapter?.setNewList(mutableListOf<FeedListRecyclerItem>())
			feeds?.forEachIndexed { i, feed ->
				fastAdapter?.add(FeedListRecyclerItem().withFeed(feed).withIsLast(i == feeds?.lastIndex).withFragment(this@FeedListFragment).withAdapter(fastAdapter!!))
			}
			fastAdapter?.withSavedInstanceState(savedInstanceState)
		}
		tags = getAddedObject<Array<String>>("tags")
		if (tags.notNullAndEmpty()) {
			tagsBox?.showView()
			tagsBox?.removeAllViews()
			tags?.forEach {
				tagsBox?.addTagView(this, it)
			}
		} else {
			tagsBox?.hideView()
		}
		return view
	}

	override fun onSaveInstanceState(outState: Bundle?) {
		super.onSaveInstanceState(outState)
		fastAdapter?.saveInstanceState(outState)
	}
}
