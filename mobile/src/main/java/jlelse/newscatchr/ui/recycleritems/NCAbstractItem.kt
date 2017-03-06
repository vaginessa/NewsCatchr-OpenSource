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

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.mikepenz.fastadapter.IClickable
import com.mikepenz.fastadapter.IItem
import com.mikepenz.fastadapter.items.AbstractItem
import java.util.*

abstract class NCAbstractItem<Item, VH : RecyclerView.ViewHolder> : AbstractItem<Item, VH>() where Item : IItem<*, *>, Item : IClickable<*> {

	override fun generateView(ctx: Context?): View {
		val viewHolder = getViewHolder(null)
		bindView(viewHolder, Collections.EMPTY_LIST)
		return viewHolder.itemView
	}

	override fun generateView(ctx: Context?, parent: ViewGroup) = generateView(ctx)

	override fun getViewHolder(parent: ViewGroup?): VH = getViewHolder(getItemView())

	override fun getLayoutRes() = 0

	abstract fun getItemView(): View

}