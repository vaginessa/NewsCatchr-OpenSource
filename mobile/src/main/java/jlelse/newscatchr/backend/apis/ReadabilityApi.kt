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
import jlelse.newscatchr.extensions.notNullOrEmpty
import jlelse.newscatchr.extensions.tryOrNull

class ReadabilityApi {

	fun reparse(article: Article?): Pair<Article?, Boolean> {
		val wordpressResult = WordpressApi().reparse(article)
		return if (wordpressResult.second) wordpressResult
		else tryOrNull(article.notNullOrEmpty()) {
			Bridge.get("https://readability.com/api/content/v1/parser?token=$ReadabilityApiKey&url=${article?.url}")
					.asClass(Response::class.java)
					?.let {
						val good = it.title.notNullOrBlank() && it.content.notNullOrBlank()
						Pair(article?.apply {
							if (good) {
								title = it.title
								content = it.content
								if (it.lead_image_url.notNullOrBlank()) {
									enclosure = null
									visualUrl = it.lead_image_url
								}
								process(true)
							}
						}, good)
					}
		} ?: Pair(article, false)
	}

	@Keep
	@ContentType("application/json")
	private class Response {
		@Body
		var content: String? = ""
		@Body
		var title: String? = ""
		@Body
		var lead_image_url: String? = ""
	}

}
