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

import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import com.google.android.flexbox.FlexboxLayout
import jlelse.newscatchr.extensions.dpToPx
import jlelse.newscatchr.extensions.flexboxLayout
import jlelse.newscatchr.extensions.getPrimaryTextColor
import jlelse.newscatchr.extensions.setTextStyle
import jlelse.newscatchr.ui.recycleritems.ArticleRecyclerItem
import jlelse.readit.R
import org.jetbrains.anko.*

class ArticleRecyclerItemUI : AnkoComponent<ArticleRecyclerItem> {
	override fun createView(ui: AnkoContext<ArticleRecyclerItem>): View = with(ui) {
		verticalLayout {
			lparams(width = matchParent, height = wrapContent) {
				setPadding(16.dpToPx(), 16.dpToPx(), 16.dpToPx(), 0)
			}
			context.obtainStyledAttributes(intArrayOf(R.attr.selectableItemBackground)).apply {
				backgroundResource = getResourceId(0, 0)
			}.recycle()
			verticalLayout {
				lparams(width = matchParent, height = wrapContent) {
					bottomPadding = 8.dpToPx()
				}
				orientation = LinearLayout.HORIZONTAL
				gravity = Gravity.TOP
				textView {
					lparams(width = 0.dpToPx(), height = wrapContent, weight = 1f) {
						rightMargin = 16.dpToPx()
					}
					id = R.id.articlerecycleritem_title
					setTextStyle(context, R.style.TextAppearance_AppCompat_Medium)
					textColor = context.getPrimaryTextColor()
					typeface = Typeface.DEFAULT_BOLD
				}
				imageView {
					lparams(width = 36.dpToPx(), height = 36.dpToPx()) {
						padding = 6.dpToPx()
					}
					id = R.id.articlerecycleritem_bookmark
					context.obtainStyledAttributes(intArrayOf(R.attr.selectableItemBackgroundBorderless)).apply {
						backgroundResource = getResourceId(0, 0)
					}.recycle()
				}
				imageView {
					lparams(width = 36.dpToPx(), height = 36.dpToPx()) {
						padding = 6.dpToPx()
					}
					id = R.id.articlerecycleritem_share
					context.obtainStyledAttributes(intArrayOf(R.attr.selectableItemBackgroundBorderless)).apply {
						backgroundResource = getResourceId(0, 0)
					}.recycle()
				}
			}
			imageView {
				lparams(width = matchParent, height = wrapContent) {
					bottomMargin = 8.dpToPx()
				}
				adjustViewBounds = true
				id = R.id.articlerecycleritem_visual
			}
			textView {
				lparams(width = matchParent, height = wrapContent) {
					bottomMargin = 8.dpToPx()
				}
				id = R.id.articlerecycleritem_details
				setTextStyle(context, R.style.TextAppearance_AppCompat_Caption)
			}
			textView {
				lparams(width = matchParent, height = wrapContent) {
					bottomMargin = 8.dpToPx()
				}
				id = R.id.articlerecycleritem_content
				setTextStyle(context, R.style.TextAppearance_AppCompat_Body1)
			}
			flexboxLayout {
				lparams(width = matchParent, height = wrapContent) {
					bottomMargin = 16.dpToPx()
				}
				id = R.id.articlerecycleritem_tagsbox
				flexWrap = FlexboxLayout.FLEX_WRAP_WRAP
				justifyContent = FlexboxLayout.JUSTIFY_CONTENT_FLEX_START
			}
			include<View>(R.layout.divider)
		}
	}
}