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
import android.util.AttributeSet

class Toolbar : android.support.v7.widget.Toolbar {
	//private var offset: Int? = null

	constructor(context: Context) : super(context)
	constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
	constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

	/*override fun offsetTopAndBottom(offset: Int) {
		super.offsetTopAndBottom(offset)
		if (this.offset == null) this.offset = offset
	}

	override fun getTitleMarginTop(): Int {
		return if (offset == null) super.getTitleMarginTop() else -top + offset!! + super.getTitleMarginTop()
	}

	override fun getTitleMarginBottom(): Int {
		return if (offset == null) super.getTitleMarginBottom() else top - offset!! + super.getTitleMarginBottom()
	}*/

}
