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

import android.app.Activity
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.view.View
import android.widget.TextView
import jlelse.newscatchr.backend.helpers.UrlOpenener

class LinkTextView {

	fun apply(textView: TextView?, activity: Activity) {
		textView?.movementMethod = LinkMovementMethod.getInstance()
		val text = textView?.text
		if (text is Spannable) {
			textView.text = SpannableStringBuilder(text).apply {
				clearSpans()
				text.getSpans(0, text.length, URLSpan::class.java).forEach {
					setSpan(CustomTextClick(it.url, activity), text.getSpanStart(it), text.getSpanEnd(it), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
				}
			}
		}
	}

	private class CustomTextClick(private val url: String, private val activity: Activity) : ClickableSpan() {
		override fun onClick(view: View?) {
			UrlOpenener().openUrl(url, activity)
		}
	}

}