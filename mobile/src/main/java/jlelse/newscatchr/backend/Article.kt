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
import com.afollestad.ason.AsonName
import com.afollestad.bridge.annotations.ContentType
import jlelse.newscatchr.backend.apis.SharingApi
import jlelse.newscatchr.backend.apis.UrlShortenerApi
import jlelse.newscatchr.backend.apis.askForSharingService
import jlelse.newscatchr.backend.helpers.Preferences
import jlelse.newscatchr.extensions.*
import jlelse.readit.R
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

@ContentType("application/json")
class Article(
		@AsonName(name = "id")
		var originalId: String? = null,
		@AsonName(name = "published")
		var published: Long = 0,
		@AsonName(name = "author")
		var author: String? = null,
		@AsonName(name = "title")
		var title: String? = null,
		@AsonName(name = "cannonical.$0.href")
		var canonical: String? = null,
		@AsonName(name = "alternate.$0.href")
		var alternateHref: String? = null,
		@AsonName(name = "enclosure.$0.href")
		var enclosureHref: String? = null,
		@AsonName(name = "keywords")
		var keywords: Array<String>? = null,
		@AsonName(name = "visual.url")
		var visualUrl: String? = null,
		@AsonName(name = "origin.title")
		var originTitle: String? = null,
		@AsonName(name = "summary.content")
		var content: String? = null,
		@AsonName(name = "content.content")
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
			if (canonical.notNullOrBlank()) url = canonical
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
}