package jlelse.newscatchr.ui.layout

import android.support.v7.widget.LinearLayoutManager
import android.view.View
import jlelse.newscatchr.extensions.statefulRecyclerView
import jlelse.newscatchr.ui.fragments.BaseFragment
import jlelse.readit.R
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext

class BasicRecyclerUI : AnkoComponent<BaseFragment> {
	override fun createView(ui: AnkoContext<BaseFragment>): View = with(ui) {
		statefulRecyclerView {
			id = R.id.basicrecyclerview_recycler
			layoutManager = LinearLayoutManager(context)
		}
	}
}