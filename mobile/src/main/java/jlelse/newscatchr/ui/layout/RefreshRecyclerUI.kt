package jlelse.newscatchr.ui.layout

import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.google.android.flexbox.FlexboxLayout
import com.mcxiaoke.koi.ext.dpToPx
import jlelse.newscatchr.extensions.flexboxLayout
import jlelse.newscatchr.extensions.statefulRecyclerView
import jlelse.newscatchr.extensions.swipeRefreshLayout
import jlelse.newscatchr.ui.fragments.BaseFragment
import jlelse.readit.R
import org.jetbrains.anko.*
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.support.v4.nestedScrollView

class RefreshRecyclerUI : AnkoComponent<BaseFragment> {
	override fun createView(ui: AnkoContext<BaseFragment>): View = with(ui) {
		swipeRefreshLayout {
			id = R.id.refreshrecyclerview_refresh
			statefulRecyclerView {
				id = R.id.refreshrecyclerview_recycler
				layoutManager = LinearLayoutManager(context)
			}
		}
	}
}