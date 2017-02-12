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
import android.os.Bundle
import android.os.Parcelable
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet

class StatefulRecyclerView : RecyclerView {
	private var mLayoutManagerSavedState: Parcelable? = null
	private val SAVED_SUPER_STATE = "super-state"
	private val SAVED_LAYOUT_MANAGER = "layout-manager-state"

	constructor(context: Context) : super(context)

	constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

	constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

	override fun onSaveInstanceState(): Parcelable = Bundle().apply {
		putParcelable(SAVED_SUPER_STATE, super.onSaveInstanceState())
		putParcelable(SAVED_LAYOUT_MANAGER, this@StatefulRecyclerView.layoutManager.onSaveInstanceState())
	}

	override fun onRestoreInstanceState(stateInput: Parcelable) {
		var state = stateInput
		if (state is Bundle) {
			mLayoutManagerSavedState = state.getParcelable<Parcelable>(SAVED_LAYOUT_MANAGER)
			state = state.getParcelable<Parcelable>(SAVED_SUPER_STATE)
		}
		super.onRestoreInstanceState(state)
	}

	fun restorePosition() {
		if (mLayoutManagerSavedState != null) {
			this.layoutManager.onRestoreInstanceState(mLayoutManagerSavedState)
			mLayoutManagerSavedState = null
		}
	}

	override fun setAdapter(adapter: RecyclerView.Adapter<*>) {
		super.setAdapter(adapter)
		restorePosition()
	}
}