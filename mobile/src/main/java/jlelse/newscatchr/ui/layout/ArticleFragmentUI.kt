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

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.util.TypedValue
import android.view.View
import com.google.android.flexbox.FlexboxLayout
import jlelse.newscatchr.extensions.*
import jlelse.newscatchr.ui.fragments.ArticleFragment
import jlelse.readit.R
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.nestedScrollView

class ArticleFragmentUI : AnkoComponent<ArticleFragment> {
	@SuppressLint("PrivateResource")
	override fun createView(ui: AnkoContext<ArticleFragment>): View = with(ui) {
		swipeRefreshLayout {
			id = R.id.articlefragment_refresh
			nestedScrollView {
				id = R.id.articlefragment_scrollview
				verticalLayout {
					lparams(width = matchParent, height = wrapContent)
					view().lparams(width = matchParent, height = 32.dpToPx())
					textView {
						lparams(width = matchParent, height = wrapContent) {
							bottomMargin = 8.dpToPx()
							rightMargin = 16.dpToPx()
							leftMargin = 16.dpToPx()
						}
						id = R.id.articlefragment_title
						setTextStyle(context, R.style.TextAppearance_AppCompat_Headline)
						setTypeface(typeface, Typeface.BOLD)
					}
					imageView {
						lparams(width = matchParent, height = wrapContent) {
							bottomMargin = 8.dpToPx()
							rightMargin = 16.dpToPx()
							leftMargin = 16.dpToPx()
						}
						id = R.id.articlefragment_visual
						adjustViewBounds = true
					}
					textView {
						lparams(width = matchParent, height = wrapContent) {
							bottomMargin = 8.dpToPx()
							rightMargin = 16.dpToPx()
							leftMargin = 16.dpToPx()
						}
						id = R.id.articlefragment_details
						setTextStyle(context, R.style.TextAppearance_AppCompat_Caption)
					}
					zoomTextView {
						lparams(width = matchParent, height = wrapContent) {
							bottomMargin = 8.dpToPx()
							rightMargin = 16.dpToPx()
							leftMargin = 16.dpToPx()
						}
						id = R.id.articlefragment_content
						setTextStyle(context, R.style.TextAppearance_AppCompat_Body1)
						setTextIsSelectable(true)
						setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
					}
					flexboxLayout {
						lparams(width = matchParent, height = wrapContent) {
							bottomMargin = 16.dpToPx()
							setPadding(12.dpToPx(), 0, 12.dpToPx(), 0)
							visibility = View.GONE
						}
						id = R.id.articlefragment_tagsbox
						flexWrap = FlexboxLayout.FLEX_WRAP_WRAP
						justifyContent = FlexboxLayout.JUSTIFY_CONTENT_FLEX_START
					}
				}
			}
		}
	}
}
