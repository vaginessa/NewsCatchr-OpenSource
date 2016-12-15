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
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.flexbox.FlexboxLayout
import com.mcxiaoke.koi.ext.find
import jlelse.newscatchr.backend.Article
import jlelse.newscatchr.backend.apis.Feedly
import jlelse.newscatchr.backend.apis.ReadabilityApi
import jlelse.newscatchr.backend.apis.TranslateApi
import jlelse.newscatchr.backend.helpers.Database
import jlelse.newscatchr.backend.helpers.Tracking
import jlelse.newscatchr.backend.helpers.UrlOpenener
import jlelse.newscatchr.extensions.*
import jlelse.newscatchr.ui.activities.MainActivity
import jlelse.newscatchr.ui.interfaces.FAB
import jlelse.newscatchr.ui.layout.ArticleFragmentUI
import jlelse.newscatchr.ui.views.SwipeRefreshLayout
import jlelse.newscatchr.ui.views.ZoomTextView
import jlelse.newscatchr.ui.views.addTagView
import jlelse.readit.R
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.*

class ArticleFragment() : BaseFragment(), FAB {
	private var fragmentView: View? = null
	private val titleView: TextView? by lazy { fragmentView?.find<TextView>(R.id.articlefragment_title) }
	private val visualView: ImageView? by lazy { fragmentView?.find<ImageView>(R.id.articlefragment_visual) }
	private val detailsView: TextView? by lazy { fragmentView?.find<TextView>(R.id.articlefragment_details) }
	private val tagsBox: FlexboxLayout? by lazy { fragmentView?.find<FlexboxLayout>(R.id.articlefragment_tagsbox) }
	private val contentView: ZoomTextView? by lazy { fragmentView?.find<ZoomTextView>(R.id.articlefragment_content) }
	private val refreshOne: SwipeRefreshLayout? by lazy { fragmentView?.find<SwipeRefreshLayout>(R.id.articlefragment_refresh) }

	private var article: Article? = null
	private var bookmark = false
	private var zoomInit = false

	override val fabDrawable = R.drawable.ic_share
	override val fabClick = { shareArticle() }

	override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		super.onCreateView(inflater, container, savedInstanceState)
		setHasOptionsMenu(true)
		fragmentView = ArticleFragmentUI().createView(AnkoContext.create(context, this))
		refreshOne?.apply {
			setOnRefreshListener {
				try {
					doAsync { showArticle(Feedly().entries(arrayOf(article?.originalId ?: ""))?.firstOrNull()) }
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
		doAsync {
			article?.process(true)
			uiThread {
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
			text = title?.toHtml()
		} else titleView?.hideView()
	}

	private fun details(author: String? = "", originTitle: String? = "", published: Long? = 0) {
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

	private fun content(content: String? = "") {
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
			doAsync {
				ReadabilityApi().reparse(article).let { if (it.second) showArticle(it.first) }
			}
			true
		}
		R.id.translate -> {
			val translateApi = TranslateApi()
			MaterialDialog.Builder(context)
					.items(mutableListOf<String>().apply {
						translateApi.languages().forEach { add(Locale(it).displayName) }
					})
					.itemsCallback { dialog, view, i, charSequence ->
						val language = translateApi.languages()[i]
						refreshOne?.showIndicator()
						doAsync {
							article?.apply {
								title = tryOrNull { TranslateApi().translate(language, title) } ?: title
								content = tryOrNull { TranslateApi().translate(language, content?.toHtml().toString()) } ?: content
							}
							showArticle(article)
						}
					}
					.negativeText(android.R.string.cancel)
					.show()
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
