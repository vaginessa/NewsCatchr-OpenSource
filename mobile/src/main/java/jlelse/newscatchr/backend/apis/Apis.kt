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

package jlelse.newscatchr.backend.apis

import android.content.Context
import android.content.Intent
import com.afollestad.ason.Ason
import com.afollestad.bridge.Bridge
import jlelse.newscatchr.backend.Article
import jlelse.newscatchr.extensions.blankNull
import jlelse.newscatchr.extensions.resStr
import jlelse.readit.R

// Share
fun Context.share(title: String, text: String) {
	startActivity(Intent.createChooser(Intent().apply {
		action = Intent.ACTION_SEND
		type = "text/plain"
		putExtra(Intent.EXTRA_SUBJECT, title)
		putExtra(Intent.EXTRA_TEXT, text)
	}, "${R.string.share.resStr()} $title"))
}

// Short Url
fun String.shortUrl(): String {
	if (isNotBlank()) {
		Bridge.post("https://www.googleapis.com/urlshortener/v1/url?fields=id&key=$GoogleApiKey")
				.body(Ason().put("longUrl", this))
				.contentType("application/json")
				.asAsonObject()
				.let {
					return it?.getString("id").blankNull() ?: this
				}
	} else return this
}

// Hastebin
fun String.uploadHaste(): String? {
	if (isNotBlank()) {
		Bridge.post("https://hastebin.com/documents")
				.body(this)
				.contentType("plain/text")
				.asAsonObject()
				.let {
					return it?.getString("key").blankNull()
				}
	} else return null
}

fun String.downloadHaste(): String? {
	if (isNotBlank()) {
		Bridge.get("https://hastebin.com/documents/$this")
				.asAsonObject()
				.let {
					return it?.getString("data").blankNull()
				}
	} else return null
}

// Readability
fun String.fetchArticle(oldArticle: Article? = null): Article? {
	Bridge.get("https://mercury.postlight.com/parser?url=$this")
			.contentType("application/json")
			.header("x-api-key", ReadabilityApiKey)
			.asAsonObject()
			?.let {
				return (oldArticle ?: Article()).apply {
					url = it.getString("url").blankNull() ?: oldArticle?.url ?: this@fetchArticle
					canonicalHref = null
					alternateHref = null
					title = it.getString("title").blankNull() ?: oldArticle?.title
					content = it.getString("content").blankNull() ?: oldArticle?.title
					summaryContent = null
					visualUrl = it.getString("lead_image_url").blankNull() ?: oldArticle?.visualUrl
					enclosureHref = null
					process(force = true)
				}
			}
	return null
}

// AMP
fun String.ampUrl() = "https://mercury.postlight.com/amp?url=$this"