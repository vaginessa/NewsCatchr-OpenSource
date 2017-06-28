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

package jlelse.newscatchr.ui.layout

import android.view.View
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.JustifyContent
import jlelse.newscatchr.extensions.flexboxLayout
import jlelse.newscatchr.ui.recycleritems.TagsRecyclerItem
import jlelse.readit.R
import org.jetbrains.anko.*

class TagsRecyclerItemUI : AnkoComponent<TagsRecyclerItem> {
	override fun createView(ui: AnkoContext<TagsRecyclerItem>): View = with(ui) {
		verticalLayout {
			flexboxLayout {
				lparams(width = matchParent, height = wrapContent) {
					padding = dip(12)
				}
				id = R.id.tagsrecycleritem_box
				flexWrap = FlexWrap.WRAP
				justifyContent = JustifyContent.FLEX_START
			}
		}
	}
}