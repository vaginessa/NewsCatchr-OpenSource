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
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.mcxiaoke.koi.ext.dpToPx
import jlelse.newscatchr.ui.activities.MainActivity
import jlelse.readit.R
import org.jetbrains.anko.*


class MainActivityUI : AnkoComponent<MainActivity> {
	override fun createView(ui: AnkoContext<MainActivity>): View = with(ui) {
		include<CoordinatorLayout>(R.layout.mainactivity) {
			find<AppBarLayout>(R.id.mainactivity_appbar).apply {
				fitsSystemWindows = true
			}
			find<CollapsingToolbarLayout>(R.id.mainactivity_collapsingtoolbar).apply {
				fitsSystemWindows = true
				setContentScrimResource(R.color.colorPrimary)
				expandedTitleMarginBottom = 64.dpToPx()
				expandedTitleMarginStart = 16.dpToPx()
				setExpandedTitleTextAppearance(android.R.attr.textAppearanceLarge)
			}
			find<ImageView>(R.id.mainactivity_toolbarbackground).apply {
				fitsSystemWindows = true
				scaleType = ImageView.ScaleType.CENTER_CROP
				setColorFilter(Color.parseColor("#33000000"))
			}
			find<TextView>(R.id.mainactivity_toolbarsubtitle).apply {
				ellipsize = TextUtils.TruncateAt.END
				maxLines = 2
				textColor = Color.parseColor("#B3FFFFFF")
				setTextSize(TypedValue.COMPLEX_UNIT_SP, 14.toFloat())
			}
		}
	}
}
