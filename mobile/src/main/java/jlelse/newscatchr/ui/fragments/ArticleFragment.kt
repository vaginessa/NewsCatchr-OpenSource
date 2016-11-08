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
import com.mcxiaoke.koi.async.mainThreadSafe
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

	private var article: Article? = null
	private var bookmark = false
	private var zoomInit = false

	override val fabDrawable = R.drawable.ic_share
	override val fabClick = { shareArticle() }

	override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		super.onCreateView(inflater, container, savedInstanceState)
		setHasOptionsMenu(true)
		val fragmentView = inflater?.inflate(R.layout.articlefragment, container, false)

		titleView = fragmentView?.find<TextView>(R.id.title)
		visualView = fragmentView?.find<ImageView>(R.id.visual)
		detailsView = fragmentView?.find<TextView>(R.id.details)
		tagsBox = fragmentView?.find<FlexboxLayout>(R.id.tagsBox)
		contentView = fragmentView?.find<ZoomTextView>(R.id.content)
		refreshOne = fragmentView?.find<SwipeRefreshLayout>(R.id.refreshOne)?.apply {
			setOnRefreshListener {
				try {
					asyncSafe { showArticle(Feedly().entries(arrayOf(article?.originalId ?: ""))?.firstOrNull()) }
				} catch (e: Exception) {
					e.printStackTrace()
					refreshOne?.hideIndicator()
				}
			}
		}

		article = getAddedObject("article") ?: savedInstanceState?.getObject("article")
		bookmark = Database.isSavedBookmark(article?.url)

		initZoom()
		showArticle(article)
		Database.addReadUrl(article?.url)
		Tracking.track(type = Tracking.TYPE.ARTICLE, url = article?.url)

		return fragmentView
	}

	private fun showArticle(article: Article?) {
		refreshOne?.showIndicator()
		asyncSafe {
			article?.process(true)
			mainThreadSafe {
				if (article.notNullOrEmpty()) {
					this@ArticleFragment.article = article
					image(article?.visualUrl)
					title(article?.title)
					details(article?.author, article?.originTitle, article?.published)
					content(article?.content)
					keywords(article?.keywords)
					showWearNotification()
				}
				refreshOne?.hideIndicator()
			}
		}
	}

	private fun image(visualUrl: String? = "") {
		if (visualUrl.notNullOrBlank()) visualView?.apply {
			showView()
			loadImage(visualUrl)
			tryOrNull(activity != null) { (activity as MainActivity).loadToolbarBackground(visualUrl) }
		} else visualView?.hideView()
	}

	private fun title(title: String? = "") {
		if (title.notNullOrBlank()) titleView?.apply {
			showView()
			text = title
		} else titleView?.hideView()
	}

	private fun details(author: String? = null, originTitle: String? = null, published: Long? = 0) {
		var details: String? = ""
		if (author.notNullOrBlank()) details += author
		if (originTitle.notNullOrBlank()) {
			if (details.notNullOrBlank()) details += " - "
			details += originTitle
		}
		if ((published?.toInt() ?: 0) != 0) {
			if (details.notNullOrBlank()) details += "\n"
			details += DateUtils.getRelativeTimeSpanString(published!!)
		}
		if (details.notNullOrBlank()) detailsView?.apply {
			showView()
			text = details
		} else detailsView?.hideView()
	}

	private fun content(content: String? = null) {
		if (content.notNullOrBlank()) contentView?.apply {
			showView()
			text = content?.toHtml()
		} else contentView?.hideView()
	}

	private fun keywords(keywords: Array<String>? = null) {
		if (keywords.notNullAndEmpty()) tagsBox?.apply {
			removeAllViews()
			keywords?.forEach { addTagView(this@ArticleFragment, it) }
		} else tagsBox?.removeAllViews()
	}

	private fun initZoom() {
		if (!zoomInit) {
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
						MotionEvent.ACTION_UP -> view.parent.parent.requestDisallowInterceptTouchEvent(false)
					}
				} else {
					view.parent.parent.requestDisallowInterceptTouchEvent(false)
					view.onTouchEvent(motionEvent)
				}
				true
			}
			zoomInit = true
		}
	}


	private fun showWearNotification() = tryOrNull(activity != null) { (activity as MainActivity).buildWearNotification(article?.title ?: "", (article?.content ?: "").toHtml().toString()) }

	private fun shareArticle() = article?.share(activity)

	override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
		super.onCreateOptionsMenu(menu, inflater)
		inflater?.inflate(R.menu.articlefragment, menu)
		menu?.findItem(R.id.bookmark)?.icon = (if (bookmark) R.drawable.ic_bookmark_universal else R.drawable.ic_bookmark_border_universal).resDrw(context, Color.WHITE)
	}

	override fun onOptionsItemSelected(item: MenuItem?) = when (item?.itemId) {
		R.id.bookmark -> {
			bookmark = !bookmark
			if (bookmark) Database.addBookmark(article)
			else Database.deleteBookmark(article?.url)
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
				if (result.second) showArticle(result.first)
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
