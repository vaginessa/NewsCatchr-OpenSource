/*
 * NewsCatchr  Copyright (C) 2016  Jan-Lukas Else
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package jlelse.newscatchr.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.text.format.DateUtils
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import com.google.android.flexbox.FlexboxLayout
import com.mcxiaoke.koi.async.asyncSafe
import com.mcxiaoke.koi.async.mainThread
import com.mcxiaoke.koi.ext.find
import jlelse.newscatchr.backend.Article
import jlelse.newscatchr.backend.apis.Feedly
import jlelse.newscatchr.backend.apis.ReadabilityApi
import jlelse.newscatchr.backend.helpers.Database
import jlelse.newscatchr.backend.helpers.Tracking
import jlelse.newscatchr.backend.helpers.UrlOpenener
import jlelse.newscatchr.extensions.*
import jlelse.newscatchr.ui.activities.MainActivity
import jlelse.newscatchr.ui.interfaces.FAB
import jlelse.newscatchr.ui.views.LinkTextView
import jlelse.newscatchr.ui.views.SwipeRefreshLayout
import jlelse.newscatchr.ui.views.ZoomTextView
import jlelse.newscatchr.ui.views.addTagView
import jlelse.readit.R

class ArticleFragment() : BaseFragment(), FAB {
	private var titleView: TextView? = null
	private var visualView: ImageView? = null
	private var detailsView: TextView? = null
	private var tagsBox: FlexboxLayout? = null
	private var contentView: ZoomTextView? = null
	private var refreshOne: SwipeRefreshLayout? = null
	private var bookmark = false

	private var article: Article? = null

	private fun replaceArticle(newArticle: Article?) {
		if (newArticle?.notNullOrEmpty() == true) {
			article = newArticle?.process(true)
			mainThread {
				updateArticleContent()
				showWearNotification()
			}
		}
		mainThread {
			refreshOne?.hideIndicator()
		}
	}

	override val fabDrawable = R.drawable.ic_share

	override val fabClick = {
		shareArticle()
	}

	override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		super.onCreateView(inflater, container, savedInstanceState)
		setHasOptionsMenu(true)
		val fragmentView = inflater?.inflate(R.layout.articlefragment, container, false)

		titleView = fragmentView?.find<TextView>(R.id.title)
		visualView = fragmentView?.find<ImageView>(R.id.visual)
		detailsView = fragmentView?.find<TextView>(R.id.details)
		tagsBox = fragmentView?.find<FlexboxLayout>(R.id.tagsBox)
		contentView = fragmentView?.find<ZoomTextView>(R.id.content)
		refreshOne = fragmentView?.find<SwipeRefreshLayout>(R.id.refreshOne)

		refreshOne?.setOnRefreshListener {
			try {
				asyncSafe {
					replaceArticle(Feedly().entries(arrayOf(article?.originalId ?: ""))?.firstOrNull())
				}
			} catch (e: Exception) {
				e.printStackTrace()
				refreshOne?.hideIndicator()
			}
		}

		article = getAddedObject("article") ?: savedInstanceState?.getObject("article")
		bookmark = Database().isSavedBookmark(article?.url)

		showArticle()
		showWearNotification()
		Database().addReadUrl(article?.url)
		Tracking.GATracker.track(article?.url, Tracking.GATracker.TYPE.ARTICLE)

		return fragmentView
	}

	private fun showArticle() {
		updateArticleContent()
		LinkTextView().apply(contentView, activity)
		// Setup zoom feature
		contentView?.setOnTouchListener { view, motionEvent ->
			view.performClick()
			if (motionEvent.pointerCount >= 2) {
				when (motionEvent.action) {
					MotionEvent.ACTION_DOWN -> {
						view.parent.parent.requestDisallowInterceptTouchEvent(true)
						contentView?.scaleDetector?.onTouchEvent(motionEvent)
					}
					MotionEvent.ACTION_MOVE -> {
						view.parent.parent.requestDisallowInterceptTouchEvent(true)
						contentView?.scaleDetector?.onTouchEvent(motionEvent)
					}
					MotionEvent.ACTION_UP -> {
						view.parent.parent.requestDisallowInterceptTouchEvent(false)
					}
				}
			} else {
				view.parent.parent.requestDisallowInterceptTouchEvent(false)
				view.onTouchEvent(motionEvent)
			}
			true
		}
	}

	private fun updateArticleContent() {
		if (article?.title.notNullOrBlank()) {
			titleView?.showView()
			titleView?.text = article?.title
		} else {
			titleView?.hideView()
		}
		var details: String? = ""
		if (article?.author.notNullOrBlank()) {
			details += article?.author
		}
		if (article?.originTitle.notNullOrBlank()) {
			if (details.notNullOrBlank()) details += " - "
			details += article?.originTitle
		}
		if ((article?.published?.toInt() ?: 0) != 0) {
			if (details.notNullOrBlank()) details += "\n"
			details += DateUtils.getRelativeTimeSpanString(article!!.published)
		}
		if (details.notNullOrBlank()) {
			detailsView?.showView()
			detailsView?.text = details
		} else {
			detailsView?.hideView()
		}
		if (article?.keywords.notNullAndEmpty()) {
			tagsBox?.removeAllViews()
			article?.keywords?.forEach {
				tagsBox?.addTagView(this, it)
			}
		} else {
			tagsBox?.removeAllViews()
		}
		if (article?.visualUrl.notNullOrBlank()) {
			visualView?.showView()
			visualView?.loadImage(article?.visualUrl)
			(activity as MainActivity).loadToolbarBackground(article?.visualUrl)
		} else {
			visualView?.hideView()
		}
		if (article?.content.notNullOrBlank()) {
			contentView?.showView()
			contentView?.text = article?.content?.toHtml()
		} else {
			contentView?.hideView()
		}
	}

	private fun showWearNotification() {
		(activity as MainActivity).buildWearNotification(article?.title ?: "", (article?.content ?: "").toHtml().toString())
	}

	private fun shareArticle() = article?.share(activity)

	override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
		super.onCreateOptionsMenu(menu, inflater)
		inflater?.inflate(R.menu.articlefragment, menu)
		menu?.findItem(R.id.bookmark)?.icon = (if (bookmark) R.drawable.ic_bookmark_universal else R.drawable.ic_bookmark_border_universal).resDrw(context, Color.WHITE)
	}

	override fun onOptionsItemSelected(item: MenuItem?) = when (item?.itemId) {
		R.id.bookmark -> {
			bookmark = !bookmark
			if (bookmark) Database().addBookmark(article)
			else Database().deleteBookmark(article?.url)
			item?.icon = (if (bookmark) R.drawable.ic_bookmark_universal else R.drawable.ic_bookmark_border_universal).resDrw(context, Color.WHITE)
			true
		}
		R.id.share -> {
			shareArticle()
			true
		}
		R.id.browser -> {
			UrlOpenener().openUrl(article?.url ?: "", activity)
			true
		}
		R.id.readability -> {
			refreshOne?.showIndicator()
			asyncSafe {
				val result = ReadabilityApi().reparse(article)
				if (result.second) replaceArticle(result.first)
				refreshOne?.hideIndicator()
			}
			true
		}
		else -> {
			super.onOptionsItemSelected(item)
		}
	}

	override fun onSaveInstanceState(outState: Bundle?) {
		outState?.addObject(article, "article")
		super.onSaveInstanceState(outState)
	}
}
