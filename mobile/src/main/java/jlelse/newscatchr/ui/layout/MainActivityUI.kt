/*
 * NewsCatchr  Copyright (C) 2016  Jan-Lukas Else
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package jlelse.newscatchr.ui.layout

import android.support.design.widget.AppBarLayout
import android.support.design.widget.CollapsingToolbarLayout
import android.view.View
import android.widget.ImageView
import com.mcxiaoke.koi.ext.dpToPx
import jlelse.newscatchr.extensions.bottomNavigationView
import jlelse.newscatchr.extensions.floatingActionButton
import jlelse.newscatchr.ui.activities.MainActivity
import jlelse.readit.R
import org.jetbrains.anko.*
import org.jetbrains.anko.appcompat.v7.toolbar
import org.jetbrains.anko.design.appBarLayout
import org.jetbrains.anko.design.collapsingToolbarLayout
import org.jetbrains.anko.design.coordinatorLayout
import android.content.res.TypedArray


class MainActivityUI : AnkoComponent<MainActivity> {
	override fun createView(ui: AnkoContext<MainActivity>): View = with(ui) {
		coordinatorLayout {
			lparams(width = matchParent, height = matchParent)
			fitsSystemWindows = true
			appBarLayout(R.style.ThemeOverlay_AppCompat_Dark_ActionBar) {
				lparams(width = matchParent, height = wrapContent)
				id = R.id.mainactivity_appbar
				fitsSystemWindows = true
				collapsingToolbarLayout {
					lparams(width = matchParent, height = wrapContent)
					layoutParams = (layoutParams as AppBarLayout.LayoutParams).apply {
						scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED or AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP or AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
					}
					id = R.id.mainactivity_collapsingtoolbar
					fitsSystemWindows = true
					setContentScrimResource(R.color.colorPrimary)
					expandedTitleMarginBottom = 64.dpToPx()
					expandedTitleMarginStart = 16.dpToPx()
					setExpandedTitleTextAppearance(android.R.attr.textAppearanceLarge)
					imageView {
						lparams(width = matchParent, height = matchParent)
						layoutParams = (layoutParams as CollapsingToolbarLayout.LayoutParams).apply {
							collapseMode = CollapsingToolbarLayout.LayoutParams.COLLAPSE_MODE_PARALLAX
						}
						fitsSystemWindows = true
						scaleType = ImageView.ScaleType.CENTER_CROP
					}
					textView {
						lparams(width = matchParent, height = 128.dpToPx()) {
							context.theme.obtainStyledAttributes(intArrayOf(android.R.attr.actionBarSize)).run {
								topMargin = getDimension(0, 0f).toInt()
								recycle()
							}
						}
						layoutParams = (layoutParams as CollapsingToolbarLayout.LayoutParams).apply {
							collapseMode = CollapsingToolbarLayout.LayoutParams.COLLAPSE_MODE_PARALLAX
						}
					}
					toolbar {
						popupTheme = R.style.ToolbarPopupTheme
					}
				}
			}
			frameLayout {
			}
			bottomNavigationView {

			}
			floatingActionButton {

			}
		}
	}
}
