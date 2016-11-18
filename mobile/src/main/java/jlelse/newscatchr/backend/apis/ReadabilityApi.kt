/*
 * NewsCatchr  Copyright (C) 2016  Jan-Lukas Else
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package jlelse.newscatchr.backend.apis

import android.net.Uri
import android.support.annotation.Keep
import com.afollestad.bridge.Bridge
import com.afollestad.bridge.annotations.Body
import com.afollestad.bridge.annotations.ContentType
import jlelse.newscatchr.backend.Article
import jlelse.newscatchr.extensions.notNullOrBlank
import jlelse.newscatchr.extensions.tryOrNull

class ReadabilityApi {

	fun reparse(article: Article?): Pair<Article?, Boolean> {
		val wordpress = wordpress(article?.url)?.filter { it.component2().notNullOrBlank() }
		val mercury = mercury(article?.url)?.filter { it.component2().notNullOrBlank() }
		val good = (wordpress?.get("title") ?: mercury?.get("title")).notNullOrBlank() && (wordpress?.get("content") ?: mercury?.get("content")).notNullOrBlank()
		return Pair(article?.apply {
			if (good) {
				title = wordpress?.get("title") ?: mercury?.get("title") ?: title
				content = wordpress?.get("content") ?: mercury?.get("content") ?: content
				if ((wordpress?.get("title") ?: mercury?.get("title")).notNullOrBlank()) {
					enclosure = null
					visualUrl = wordpress?.get("image") ?: mercury?.get("image") ?: visualUrl
				}
				process(true)
			}
		}, good)
	}

	fun mercury(url: String?): Map<String, String?>? = tryOrNull(url.notNullOrBlank()) {
		Bridge.get("https://mercury.postlight.com/parser?url=$url")
				.header("Content-Type", "application/json")
				.header("x-api-key", ReadabilityApiKey)
				.asClass(MercuryR::class.java)
				?.let { mapOf("title" to it.title, "content" to it.content, "image" to it.lead_image_url) }
	}

	fun wordpress(url: String?): Map<String, String?>? = tryOrNull(url.notNullOrBlank()) {
		Bridge.get("https://public-api.wordpress.com/rest/v1.1/sites/${Uri.parse(url).host.replace("www.", "")}/posts/slug:${Uri.parse(url).lastPathSegment}?fields=title,content,featured_image")
				.asClass(WordpressR::class.java)
				?.let { mapOf("title" to it.title, "content" to it.content, "image" to it.featured_image) }
	}

	@Keep @ContentType("application/json") private open class Response {
		@Body var title: String? = ""
		@Body var content: String? = ""
	}

	@Keep @ContentType("application/json") private class MercuryR : Response() {
		@Body var lead_image_url: String? = ""
	}

	@Keep @ContentType("application/json") private class WordpressR : Response() {
		@Body var featured_image: String? = null
	}

}
