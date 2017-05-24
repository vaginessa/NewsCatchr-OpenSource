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
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import jlelse.newscatchr.extensions.dpToPx
import jlelse.newscatchr.extensions.resDrw
import jlelse.readit.R
import org.jetbrains.anko.*

class TagUI : AnkoComponent<View> {
	override fun createView(ui: AnkoContext<View>): View = with(ui) {
		frameLayout {
			lparams(width = wrapContent, height = wrapContent) {
				padding = 4.dpToPx()
			}
			verticalLayout {
				textView {
					lparams(width = wrapContent, height = 32.dpToPx()) {
						leftPadding = 12.dpToPx()
						rightPadding = 12.dpToPx()
					}
					gravity = Gravity.CENTER
					backgroundDrawable = R.drawable.chip.resDrw(context)
					textColor = Color.WHITE
					setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
				}
			}
		}
	}
}