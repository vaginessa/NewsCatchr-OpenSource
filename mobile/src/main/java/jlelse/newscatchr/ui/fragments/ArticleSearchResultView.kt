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

package jlelse.newscatchr.ui.fragments

import android.support.v7.widget.RecyclerView
import android.view.View
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter
import jlelse.newscatchr.backend.Article
import jlelse.newscatchr.extensions.notNullAndEmpty
import jlelse.newscatchr.ui.layout.BasicRecyclerUI
import jlelse.newscatchr.ui.recycleritems.ArticleRecyclerItem
import jlelse.readit.R
import jlelse.viewmanager.ViewManagerView
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.find

class ArticleSearchResultView(val articles: List<Article>) : ViewManagerView() {
	private var fragmentView: View? = null
	private val recyclerOne: RecyclerView? by lazy { fragmentView?.find<RecyclerView>(R.id.basicrecyclerview_recycler) }
	private var fastAdapter = FastItemAdapter<ArticleRecyclerItem>()

	override fun onCreateView(): View? {
		super.onCreateView()
		fragmentView = BasicRecyclerUI().createView(AnkoContext.create(context, this))
		if (recyclerOne?.adapter == null) recyclerOne?.adapter = fastAdapter
		if (articles.notNullAndEmpty()) fastAdapter.setNewList(articles.map { ArticleRecyclerItem(it, this@ArticleSearchResultView) })
		return fragmentView
	}
}