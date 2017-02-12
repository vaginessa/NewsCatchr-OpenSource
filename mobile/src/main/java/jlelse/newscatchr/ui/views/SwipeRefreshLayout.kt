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

package jlelse.newscatchr.ui.views

import android.content.Context
import android.support.v4.widget.SwipeRefreshLayout
import android.util.AttributeSet
import jlelse.readit.R


class SwipeRefreshLayout : SwipeRefreshLayout {

	constructor(context: Context?) : super(context) {
		setColorSchemeResources(R.color.colorAccent)
	}

	constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
		setColorSchemeResources(R.color.colorAccent)
	}

	fun showIndicator() {
		setColorSchemeResources(R.color.colorAccent)
		if (isRefreshing == false) isRefreshing = true
	}

	fun hideIndicator() {
		if (isRefreshing == true) isRefreshing = false
	}

}