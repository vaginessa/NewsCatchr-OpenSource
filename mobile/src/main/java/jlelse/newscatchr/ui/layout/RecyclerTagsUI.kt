package jlelse.newscatchr.ui.layout

import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.google.android.flexbox.FlexboxLayout
import com.mcxiaoke.koi.ext.dpToPx
import jlelse.newscatchr.extensions.flexboxLayout
import jlelse.newscatchr.ui.fragments.BaseFragment
import jlelse.readit.R
import org.jetbrains.anko.*
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.support.v4.nestedScrollView

class RecyclerTagsUI : AnkoComponent<BaseFragment> {
	override fun createView(ui: AnkoContext<BaseFragment>): View = with(ui) {
		nestedScrollView {
			lparams(width = matchParent, height = wrapContent)
			id = R.id.basicrecyclerview_scrollview
			verticalLayout {
				lparams(width = matchParent, height = wrapContent)
				flexboxLayout {
					lparams(width = matchParent, height = wrapContent) {
						bottomMargin = 16.dpToPx()
						topMargin = 16.dpToPx()
						setPadding(12.dpToPx(), 0, 12.dpToPx(), 0)
					}
					id = R.id.basicrecyclerview_tags
					flexWrap = FlexboxLayout.FLEX_WRAP_WRAP
					visibility = View.GONE
					justifyContent = FlexboxLayout.JUSTIFY_CONTENT_FLEX_START
				}
				recyclerView {
					lparams(width = matchParent, height = wrapContent)
					id = R.id.basicrecyclerview_recycler
					isNestedScrollingEnabled = false
					layoutManager = LinearLayoutManager(context)
				}
			}
		}
	}
}