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

import android.graphics.Color
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CollapsingToolbarLayout
import android.support.design.widget.CoordinatorLayout
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import com.mcxiaoke.koi.ext.dpToPx
import jlelse.newscatchr.extensions.bottomNavigationView
import jlelse.newscatchr.extensions.resDim
import jlelse.newscatchr.ui.activities.MainActivity
import jlelse.readit.R
import org.jetbrains.anko.*
import org.jetbrains.anko.appcompat.v7.toolbar
import org.jetbrains.anko.design.appBarLayout
import org.jetbrains.anko.design.collapsingToolbarLayout
import org.jetbrains.anko.design.coordinatorLayout
import org.jetbrains.anko.design.floatingActionButton


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
						id = R.id.mainactivity_toolbarbackground
						fitsSystemWindows = true
						scaleType = ImageView.ScaleType.CENTER_CROP
						setColorFilter(Color.parseColor("#33000000"))
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
						id = R.id.mainactivity_toolbarsubtitle
						leftPadding = 16.dpToPx()
						rightPadding = 16.dpToPx()
						topPadding = 72.dpToPx()
						ellipsize = TextUtils.TruncateAt.END
						maxLines = 2
						textColor = Color.parseColor("#B3FFFFFF")
						setTextSize(TypedValue.COMPLEX_UNIT_SP, 14.toFloat())
					}
					toolbar {
						context.theme.obtainStyledAttributes(intArrayOf(android.R.attr.actionBarSize)).run {
							lparams(width = matchParent, height = getDimension(0, 0f).toInt())
							recycle()
						}
						layoutParams = (layoutParams as CollapsingToolbarLayout.LayoutParams).apply {
							collapseMode = CollapsingToolbarLayout.LayoutParams.COLLAPSE_MODE_PIN
						}
						id = R.id.mainactivity_toolbar
						popupTheme = R.style.ToolbarPopupTheme
					}
				}
			}
			frameLayout {
				lparams(width = matchParent, height = matchParent)
				layoutParams = (layoutParams as CoordinatorLayout.LayoutParams).apply {
					behavior = AppBarLayout.ScrollingViewBehavior()
				}
				id = R.id.mainactivity_container
			}
			bottomNavigationView {
				lparams(width = matchParent, height = wrapContent) {
					gravity = Gravity.BOTTOM
				}
				id = R.id.mainactivity_navigationview
				setBackgroundResource(R.color.bb_background)
				inflateMenu(R.menu.navigationview)
			}
			floatingActionButton {
				lparams(width = wrapContent, height = wrapContent) {
					margin = R.dimen.fab_margin.resDim()!!.toInt()
				}
				layoutParams = (layoutParams as CoordinatorLayout.LayoutParams).apply {
					anchorGravity = Gravity.BOTTOM or Gravity.END or Gravity.RIGHT
					anchorId = R.id.mainactivity_appbar
				}
				id = R.id.mainactivity_fab
				visibility = View.GONE
			}
		}
	}
}
