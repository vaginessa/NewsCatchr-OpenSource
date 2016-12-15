/*
 * NewsCatchr  Copyright (C) 2016  Jan-Lukas Else
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package jlelse.newscatchr.backend

import android.app.Activity
import android.support.annotation.Keep
import com.afollestad.bridge.annotations.Body
import com.afollestad.bridge.annotations.ContentType
import jlelse.newscatchr.backend.apis.SharingApi
import jlelse.newscatchr.backend.apis.UrlShortenerApi
import jlelse.newscatchr.backend.apis.askForSharingService
import jlelse.newscatchr.backend.helpers.Preferences
import jlelse.newscatchr.extensions.*
import jlelse.readit.R
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

@Keep
@ContentType("application/json")
class Article(
		@Body(name = "id")
		var originalId: String? = null,
		@Body
		var published: Long = 0,
		@Body
		var author: String? = null,
		@Body
		var title: String? = null,
		@Body
		var canonical: Array<Alternate>? = null,
		@Body
		var alternate: Array<Alternate>? = null,
		@Body
		var enclosure: Array<Alternate>? = null,
		@Body
		var keywords: Array<String>? = null,
		@Body(name = "visual.url")
		var visualUrl: String? = null,
		@Body(name = "origin.title")
		var originTitle: String? = null,
		@Body(name = "summary.content")
		var content: String? = null,
		@Body(name = "content.content")
		var contentB: String? = null,
		var excerpt: String? = null,
		var url: String? = null,
		var pocketId: String? = null,
		var fromPocket: Boolean = false,
		var cleanedContent: Boolean = false,
		var checkedUrl: Boolean = false,
		var checkedImageUrl: Boolean = false
) {
	fun process(force: Boolean = false): Article {
		if (force) {
			cleanedContent = false
			checkedUrl = false
			checkedImageUrl = false
		}
		if (!cleanedContent) {
			content = (if (contentB.notNullOrBlank()) contentB else content)?.cleanHtml()
			excerpt = content?.toHtml().toString().buildExcerpt(30)
			cleanedContent = true
		}
		if (!checkedUrl) {
			if (canonical != null && canonical?.firstOrNull()?.href.notNullOrBlank()) url = canonical?.firstOrNull()?.href
			else if (alternate != null && alternate?.firstOrNull()?.href.notNullOrBlank()) url = alternate?.firstOrNull()?.href
			checkedUrl = true
		}
		if (!checkedImageUrl) {
			if (enclosure.notNullAndEmpty() && enclosure?.firstOrNull()?.href.notNullOrBlank()) visualUrl = enclosure?.firstOrNull()?.href
			checkedImageUrl = true
		}
		return this
	}

	fun share(context: Activity) {
		askForSharingService(context, { network ->
			doAsync {
				val newUrl = if (Preferences.urlShortener) UrlShortenerApi().getShortUrl(url) ?: url else url
				uiThread {
					SharingApi(context, network).share("\"$title\"", when (network) {
						SharingApi.SocialNetwork.Twitter -> "${title?.take(136 - (newUrl?.length ?: 0))}... $newUrl"
						SharingApi.SocialNetwork.Native, SharingApi.SocialNetwork.Facebook -> "$title - $newUrl\n\n${R.string.shared_with_nc.resStr()}"
					})
				}
			}
		})
	}
}

@Keep
@ContentType("application/json")
class Alternate(
		@Body var href: String? = null
)