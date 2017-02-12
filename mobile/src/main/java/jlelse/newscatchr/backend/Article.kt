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

package jlelse.newscatchr.backend

import android.app.Activity
import android.support.annotation.Keep
import com.afollestad.bridge.annotations.Body
import com.afollestad.bridge.annotations.ContentType
import com.afollestad.json.Ason
import com.afollestad.json.AsonIgnore
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
		@AsonIgnore
		var canonical: Array<Alternate>? = null,
		var canonicalHref: String? = null,
		@Body
		@AsonIgnore
		var alternate: Array<Alternate>? = null,
		var alternateHref: String? = null,
		@Body
		@AsonIgnore
		var enclosure: Array<Alternate>? = null,
		var enclosureHref: String? = null,
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
) : Jsonizable<Article> {
	fun fix() {
		canonicalHref = canonical?.firstOrNull()?.href
		canonical = null
		alternateHref = alternate?.firstOrNull()?.href
		alternate = null
		enclosureHref = enclosure?.firstOrNull()?.href
		enclosure = null
	}

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
			if (canonicalHref.notNullOrBlank()) url = canonicalHref
			else if (alternateHref.notNullOrBlank()) url = alternateHref
			checkedUrl = true
		}
		if (!checkedImageUrl) {
			if (enclosureHref.notNullOrBlank()) visualUrl = enclosureHref
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

	override fun toAson() = Ason().apply {
		put("originalId", originalId)
		put("published", published)
		put("author", author)
		put("title", title)
		put("canonicalHref", canonicalHref)
		put("alternateHref", alternateHref)
		put("enclosureHref", enclosureHref)
		put("keywords", keywords?.joinToString(separator = arraySeparator))
		put("visualUrl", visualUrl)
		put("originTitle", originTitle)
		put("content", content)
		put("contentB", contentB)
		put("excerpt", excerpt)
		put("url", url)
		put("pocketId", pocketId)
		put("fromPocket", fromPocket)
		put("cleanedContent", cleanedContent)
		put("checkedUrl", checkedUrl)
		put("checkedImageUrl", checkedImageUrl)
	}

	override fun fromJson(json: String?): Article {
		tryOrNull { Ason(json) }?.apply {
			originalId = get("originalId", originalId)
			published = get("published", published)
			author = get("author", author)
			title = get("title", title)
			canonicalHref = get("canonicalHref", canonicalHref)
			alternateHref = get("alternateHref", alternateHref)
			enclosureHref = get("enclosureHref", enclosureHref)
			keywords = tryOrNull { getString("keywords").split(arraySeparator).toTypedArray() } ?: keywords
			visualUrl = get("visualUrl", visualUrl)
			originTitle = get("originTitle", originTitle)
			content = get("content", content)
			contentB = get("contentB", contentB)
			excerpt = get("excerpt", excerpt)
			url = get("url", url)
			pocketId = get("pocketId", pocketId)
			fromPocket = get("fromPocket", fromPocket)
			cleanedContent = get("cleanedContent", cleanedContent)
			checkedUrl = get("checkedUrl", checkedUrl)
			checkedImageUrl = get("checkedImageUrl", checkedImageUrl)
		}
		return this
	}
}

@Keep
@ContentType("application/json")
class Alternate(
		@Body var href: String? = null
)