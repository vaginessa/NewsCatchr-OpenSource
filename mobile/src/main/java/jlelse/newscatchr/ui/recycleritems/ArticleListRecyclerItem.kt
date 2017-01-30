/*
 * NewsCatchr  Copyright (C) 2016  Jan-Lukas Else
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package jlelse.newscatchr.ui.recycleritems

import android.graphics.Typeface
import android.support.annotation.Keep
import android.support.v7.widget.RecyclerView
import android.text.format.DateUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.android.flexbox.FlexboxLayout
import com.mcxiaoke.koi.ext.find
import com.mcxiaoke.koi.ext.onClick
import com.mikepenz.fastadapter.items.AbstractItem
import com.mikepenz.fastadapter.utils.ViewHolderFactory
import jlelse.newscatchr.backend.Article
import jlelse.newscatchr.backend.helpers.Database
import jlelse.newscatchr.extensions.*
import jlelse.newscatchr.ui.fragments.ArticleFragment
import jlelse.newscatchr.ui.fragments.BaseFragment
import jlelse.newscatchr.ui.views.addTagView
import jlelse.readit.R

@Keep
class ArticleListRecyclerItem : AbstractItem<ArticleListRecyclerItem, ArticleListRecyclerItem.ViewHolder>() {
	private val FACTORY = ItemFactory()

	private var article: Article? = null
	private var fragment: BaseFragment? = null

	fun withArticle(article: Article): ArticleListRecyclerItem {
		this.article = article
		return this
	}

	fun withFragment(fragment: BaseFragment): ArticleListRecyclerItem {
		this.fragment = fragment
		return this
	}

	override fun getType(): Int {
		return R.id.articlelist_item_id
	}

	override fun getLayoutRes(): Int {
		return R.layout.articlelistrecycleritem
	}



	override fun bindView(viewHolder: ViewHolder, payloads: MutableList<Any?>?) {
		super.bindView(viewHolder, payloads)
		val context = viewHolder.itemView.context
		if (article?.title.notNullOrBlank()) {
			viewHolder.title.showView()
			viewHolder.title.text = article?.title
			viewHolder.title.setTypeface(null, if (Database.isSavedReadUrl(article?.url)) Typeface.BOLD_ITALIC else Typeface.BOLD)
		} else {
			viewHolder.title.hideView()
		}
		if ((article?.published?.toInt() ?: 0) != 0) {
			viewHolder.details.showView()
			val detailText = DateUtils.getRelativeTimeSpanString(article!!.published)
			viewHolder.details.text = detailText
		} else {
			viewHolder.details.hideView()
		}
		if (article?.content.notNullOrBlank()) {
			viewHolder.content.showView()
			viewHolder.content.text = article?.excerpt
		} else {
			viewHolder.content.hideView()
		}
		if (article?.keywords.notNullAndEmpty()) {
			viewHolder.tagsBox.showView()
			viewHolder.tagsBox.removeAllViews()
			article?.keywords?.take(3)?.forEach {
				viewHolder.tagsBox.addTagView(fragment!!, it)
			}
		} else {
			//viewHolder.tagsBox.hideView()
		}
		if (article?.visualUrl.notNullOrBlank()) {
			viewHolder.visual.showView()
			viewHolder.visual.loadImage(article?.visualUrl)
		} else {
			viewHolder.visual.hideView()
		}
		viewHolder.itemView.onClick {
			if (article != null) fragment?.fragmentNavigation?.pushFragment(ArticleFragment().addObject(article, "article"), article?.originTitle)
		}
		viewHolder.bookmark.setImageDrawable((if (Database.isSavedBookmark(article?.url)) R.drawable.ic_bookmark_universal else R.drawable.ic_bookmark_border_universal).resDrw(context, context.getPrimaryTextColor()))
		viewHolder.bookmark.onClick {
			if (article != null) {
				if (Database.isSavedBookmark(article?.url)) {
					Database.deleteBookmark(article?.url)
					viewHolder.bookmark.setImageDrawable(R.drawable.ic_bookmark_border_universal.resDrw(context, context.getPrimaryTextColor()))
				} else {
					Database.addBookmark(article)
					viewHolder.bookmark.setImageDrawable(R.drawable.ic_bookmark_universal.resDrw(context, context.getPrimaryTextColor()))
				}
			}
		}
		viewHolder.share.setImageDrawable(R.drawable.ic_share_universal.resDrw(context, context.getPrimaryTextColor()))
		viewHolder.share.onClick {
			if (fragment != null) article?.share(fragment!!.activity)
		}
	}

	override fun getFactory(): ViewHolderFactory<out ViewHolder> = FACTORY

	class ItemFactory : ViewHolderFactory<ViewHolder> {
		override fun create(v: View): ViewHolder {
			return ViewHolder(v)
		}
	}

	class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
		var bookmark: ImageView
		var share: ImageView
		var title: TextView
		var details: TextView
		var content: TextView
		var visual: ImageView
		var tagsBox: FlexboxLayout

		init {
			this.bookmark = view.find<ImageView>(R.id.bookmark)
			this.share = view.find<ImageView>(R.id.share)
			this.title = view.find<TextView>(R.id.title)
			this.details = view.find<TextView>(R.id.details)
			this.content = view.find<TextView>(R.id.content)
			this.visual = view.find<ImageView>(R.id.visual)
			this.tagsBox = view.find<FlexboxLayout>(R.id.tagsBox)
		}
	}
}
