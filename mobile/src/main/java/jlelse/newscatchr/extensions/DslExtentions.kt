package jlelse.newscatchr.extensions

import android.support.design.widget.BottomNavigationView
import android.view.ViewManager
import com.google.android.flexbox.FlexboxLayout
import jlelse.newscatchr.ui.views.SwipeRefreshLayout
import jlelse.newscatchr.ui.views.ZoomTextView
import org.jetbrains.anko.custom.ankoView

inline fun ViewManager.flexboxLayout(theme: Int = 0, init: FlexboxLayout.() -> Unit) = ankoView(::FlexboxLayout, theme, init)
inline fun ViewManager.swipeRefreshLayout(theme: Int = 0, init: SwipeRefreshLayout.() -> Unit) = ankoView(::SwipeRefreshLayout, theme, init)
inline fun ViewManager.zoomTextView(theme: Int = 0, init: ZoomTextView.() -> Unit) = ankoView(::ZoomTextView, theme, init)
inline fun ViewManager.bottomNavigationView(theme: Int = 0, init: BottomNavigationView.() -> Unit) = ankoView(::BottomNavigationView, theme, init)
