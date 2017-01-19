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