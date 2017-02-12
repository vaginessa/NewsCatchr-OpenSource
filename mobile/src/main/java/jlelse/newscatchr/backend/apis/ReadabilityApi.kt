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

import android.support.annotation.Keep
import com.afollestad.bridge.Bridge
import com.afollestad.bridge.annotations.Body
import com.afollestad.bridge.annotations.ContentType
import jlelse.newscatchr.backend.Article
import jlelse.newscatchr.extensions.notNullOrBlank
import jlelse.newscatchr.extensions.tryOrNull

class ReadabilityApi {

	fun reparse(article: Article?): Pair<Article?, Boolean> {
		val mercury = mercury(article?.url)?.filter { it.component2().notNullOrBlank() }
		val good = mercury?.get("title").notNullOrBlank() && mercury?.get("content").notNullOrBlank()
		return Pair(article?.apply {
			if (good) {
				title = if (mercury?.get("title").notNullOrBlank()) mercury?.get("title") else title
				content = if (mercury?.get("content").notNullOrBlank()) mercury?.get("content") else title
				if ((mercury?.get("image")).notNullOrBlank()) {
					enclosureHref = null
					visualUrl = mercury?.get("image") ?: visualUrl
				}
				process(true)
			}
		}, good)
	}

	fun mercury(url: String?): Map<String, String?>? = tryOrNull(execute = url.notNullOrBlank()) {
		Bridge.get("https://mercury.postlight.com/parser?url=$url")
				.header("Content-Type", "application/json")
				.header("x-api-key", ReadabilityApiKey)
				.asClass(Response::class.java)
				?.let { mapOf("title" to it.title, "content" to it.content, "image" to it.lead_image_url) }
	}

	@Keep @ContentType("application/json") private open class Response {
		@Body var title: String? = ""
		@Body var content: String? = ""
		@Body var lead_image_url: String? = ""
	}

}
