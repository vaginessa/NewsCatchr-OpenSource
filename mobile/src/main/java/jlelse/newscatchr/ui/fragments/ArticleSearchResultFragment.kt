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
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter
import jlelse.newscatchr.backend.Article
import jlelse.newscatchr.extensions.getAddedObject
import jlelse.newscatchr.extensions.notNullAndEmpty
import jlelse.newscatchr.extensions.restorePosition
import jlelse.newscatchr.ui.layout.BasicRecyclerUI
import jlelse.newscatchr.ui.recycleritems.ArticleListRecyclerItem
import jlelse.readit.R
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.find
import java.util.*

class ArticleSearchResultFragment : BaseFragment() {
	private var fragmentView: View? = null
	private val recyclerOne: RecyclerView? by lazy { fragmentView?.find<RecyclerView>(R.id.basicrecyclerview_recycler) }
	private var fastAdapter = FastItemAdapter<ArticleListRecyclerItem>()
	private val scrollView: NestedScrollView? by lazy { fragmentView?.find<NestedScrollView>(R.id.basicrecyclerview_scrollview) }
	private var articles: List<Article>? = null

	override val saveStateScrollViews: Array<NestedScrollView?>?
		get() = arrayOf(scrollView)

	override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		super.onCreateView(inflater, container, savedInstanceState)
		fragmentView = fragmentView ?: BasicRecyclerUI().createView(AnkoContext.create(context, this))
		setHasOptionsMenu(true)
		articles = getAddedObject<List<Article>>("articles")
		if (articles.notNullAndEmpty()) {
			recyclerOne?.adapter = null
			recyclerOne?.adapter = fastAdapter
			fastAdapter.withSavedInstanceState(savedInstanceState)
			fastAdapter.setNewList(ArrayList<ArticleListRecyclerItem>())
			articles?.forEach {
				fastAdapter.add(ArticleListRecyclerItem().withArticle(it).withFragment(this@ArticleSearchResultFragment))
			}
			scrollView?.restorePosition(this)
		}
		return fragmentView
	}

	override fun onSaveInstanceState(outState: Bundle?) {
		super.onSaveInstanceState(outState)
		fastAdapter.saveInstanceState(outState)
	}
}
