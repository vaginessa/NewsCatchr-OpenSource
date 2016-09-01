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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mcxiaoke.koi.ext.find
import com.mikepenz.fastadapter.adapters.FastItemAdapter
import jlelse.newscatchr.backend.Article
import jlelse.newscatchr.extensions.getAddedObject
import jlelse.newscatchr.extensions.notNullAndEmpty
import jlelse.newscatchr.ui.recycleritems.ArticleListRecyclerItem
import jlelse.readit.R
import java.util.*

class ArticleSearchResultFragment() : BaseFragment() {
    private var recyclerOne: RecyclerView? = null
    private var fastAdapter: FastItemAdapter<ArticleListRecyclerItem>? = null
    private var articles: List<Article>? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater?.inflate(R.layout.basicrecycler, container, false)
        setHasOptionsMenu(true)
        recyclerOne = view?.find<RecyclerView>(R.id.recyclerOne)?.apply {
            isNestedScrollingEnabled = false
            layoutManager = LinearLayoutManager(context)
        }
        articles = getAddedObject<List<Article>>("articles")
        if (articles.notNullAndEmpty()) {
            fastAdapter = FastItemAdapter<ArticleListRecyclerItem>()
            recyclerOne?.adapter = fastAdapter
            fastAdapter?.setNewList(ArrayList<ArticleListRecyclerItem>())
            articles?.forEach {
                fastAdapter?.add(ArticleListRecyclerItem().withArticle(it).withFragment(this@ArticleSearchResultFragment))
            }
            fastAdapter?.withSavedInstanceState(savedInstanceState)
        }
        return view
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        fastAdapter?.saveInstanceState(outState)
    }
}
