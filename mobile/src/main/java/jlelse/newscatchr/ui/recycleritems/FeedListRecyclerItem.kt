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

package jlelse.newscatchr.ui.recycleritems

import android.content.Intent
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter
import com.mikepenz.fastadapter.items.AbstractItem
import com.mikepenz.fastadapter.utils.ViewHolderFactory
import jlelse.newscatchr.backend.Feed
import jlelse.newscatchr.backend.helpers.Database
import jlelse.newscatchr.extensions.getPrimaryTextColor
import jlelse.newscatchr.extensions.hideView
import jlelse.newscatchr.extensions.resDrw
import jlelse.newscatchr.extensions.showView
import jlelse.newscatchr.ui.fragments.BaseFragment
import jlelse.newscatchr.ui.fragments.FeedFragment
import jlelse.readit.R
import org.jetbrains.anko.find
import org.jetbrains.anko.onClick

class FeedListRecyclerItem : AbstractItem<FeedListRecyclerItem, FeedListRecyclerItem.ViewHolder>() {
	private val FACTORY = ItemFactory()

	private var feed: Feed? = null
	private var isLast = false
	private var fragment: BaseFragment? = null
	private var adapter: FastItemAdapter<FeedListRecyclerItem>? = null

	fun withFeed(feed: Feed): FeedListRecyclerItem {
		this.feed = feed
		return this
	}

	fun withFragment(fragment: BaseFragment): FeedListRecyclerItem {
		this.fragment = fragment
		return this
	}

	fun withAdapter(adapter: FastItemAdapter<FeedListRecyclerItem>): FeedListRecyclerItem {
		this.adapter = adapter
		return this
	}

	fun withIsLast(isLast: Boolean): FeedListRecyclerItem {
		this.isLast = isLast
		return this
	}

	override fun getType(): Int {
		return R.id.feedlist_item_id
	}

	override fun getLayoutRes(): Int {
		return R.layout.feedlistrecycleritem
	}

	override fun bindView(viewHolder: ViewHolder, payloads: MutableList<Any?>?) {
		super.bindView(viewHolder, payloads)
		val context = viewHolder.itemView.context
		if (feed != null) {
			setTitleText(feed?.title, viewHolder.title)
			viewHolder.website.text = Uri.parse(feed?.website ?: feed?.url()).host
			viewHolder.itemView.onClick {
				fragment?.fragmentNavigation?.pushFragment(FeedFragment().apply {
					addObject("feed", feed)
				}, feed?.title)
			}
			viewHolder.favorite.setImageDrawable((if (Database.isSavedFavorite(feed?.url())) R.drawable.ic_favorite_universal else R.drawable.ic_favorite_border_universal).resDrw(context, context.getPrimaryTextColor()))
			viewHolder.favorite.onClick {
				if (Database.isSavedFavorite(feed?.url())) {
					feed?.saved = false
					Database.deleteFavorite(feed?.url())
					viewHolder.favorite.setImageDrawable(R.drawable.ic_favorite_border_universal.resDrw(context, context.getPrimaryTextColor()))
				} else {
					feed?.saved = true
					Database.addFavorite(feed)
					viewHolder.favorite.setImageDrawable(R.drawable.ic_favorite_universal.resDrw(context, context.getPrimaryTextColor()))
				}
				context.sendBroadcast(Intent("favorites_updated"))
			}
		}
		if (isLast) viewHolder.divider.hideView() else viewHolder.divider.showView()
	}

	private fun setTitleText(title: String?, textView: TextView) {
		textView.text = "$title"
	}

	override fun getFactory(): ViewHolderFactory<out ViewHolder> = FACTORY

	class ItemFactory : ViewHolderFactory<ViewHolder> {
		override fun create(v: View): ViewHolder {
			return ViewHolder(v)
		}
	}

	class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
		var title: TextView
		var website: TextView
		var favorite: ImageView
		var divider: View

		init {
			this.title = view.find<TextView>(R.id.title)
			this.website = view.find<TextView>(R.id.website)
			this.favorite = view.find<ImageView>(R.id.favorite)
			this.divider = view.find<View>(R.id.divider)
		}
	}
}