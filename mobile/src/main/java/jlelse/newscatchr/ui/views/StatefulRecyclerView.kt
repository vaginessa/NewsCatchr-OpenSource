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