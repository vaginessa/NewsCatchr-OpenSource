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

import android.graphics.Color
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CollapsingToolbarLayout
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import jlelse.newscatchr.extensions.actionBarSize
import jlelse.newscatchr.ui.activities.MainActivity
import jlelse.readit.R
import org.jetbrains.anko.*
import org.jetbrains.anko.appcompat.v7.toolbar
import org.jetbrains.anko.design.*


class NewMainActivityUI : AnkoComponent<MainActivity> {
	override fun createView(ui: AnkoContext<MainActivity>): View = with(ui) {
		coordinatorLayout {
			lparams(width = matchParent, height = matchParent)
			fitsSystemWindows = true
			themedAppBarLayout(R.style.ThemeOverlay_AppCompat_Dark_ActionBar) {
				lparams(width = matchParent, height = wrapContent)
				id = R.id.mainactivity_appbar
				fitsSystemWindows = true
				collapsingToolbarLayout {
					id = R.id.mainactivity_collapsingtoolbar
					fitsSystemWindows = true
					setContentScrimResource(R.color.colorPrimary)
					expandedTitleMarginBottom = dip(64)
					expandedTitleMarginStart = dip(16)
					imageView {
						(layoutParams as CollapsingToolbarLayout.LayoutParams?)?.apply {
							collapseMode = CollapsingToolbarLayout.LayoutParams.COLLAPSE_MODE_PARALLAX
						}
						id = R.id.mainactivity_toolbarbackground
						fitsSystemWindows = true
						scaleType = ImageView.ScaleType.CENTER_CROP
						setColorFilter(Color.parseColor("#33000000"))
					}.lparams(width = matchParent, height = matchParent)
					textView {
						id = R.id.mainactivity_toolbarsubtitle
						ellipsize = TextUtils.TruncateAt.END
						maxLines = 2
						textColor = Color.parseColor("#B3FFFFFF")
						textSize = 14f
					}.lparams(width = matchParent, height = dip(128)) {
						horizontalPadding = dip(16)
						topPadding = dip(72)
						topMargin = actionBarSize()
					}
					toolbar {
						(layoutParams as CollapsingToolbarLayout.LayoutParams?)?.apply {
							collapseMode = CollapsingToolbarLayout.LayoutParams.COLLAPSE_MODE_PIN
						}
						id = R.id.mainactivity_toolbar
						popupTheme = R.style.ToolbarPopupTheme
					}.lparams(width = matchParent, height = actionBarSize())
				}.lparams(width = matchParent, height = wrapContent) {
					scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED or AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP
				}
			}
			frameLayout {
				id = R.id.mainactivity_container
			}.lparams(width = matchParent, height = matchParent) {
				bottomMargin = dip(56)
				behavior = AppBarLayout.ScrollingViewBehavior()
			}
			bottomNavigationView {
				id = R.id.mainactivity_navigationview
				inflateMenu(R.menu.navigationview)
			}.lparams(width = matchParent, height = wrapContent) {
				gravity = Gravity.BOTTOM
				backgroundResource = R.color.bb_background
			}
			floatingActionButton {
				id = R.id.mainactivity_fab
			}.lparams(width = wrapContent, height = wrapContent) {
				margin = dip(16)
				visibility = View.GONE
				anchorId = R.id.mainactivity_appbar
				anchorGravity = Gravity.BOTTOM or Gravity.END
			}
		}
	}
}
