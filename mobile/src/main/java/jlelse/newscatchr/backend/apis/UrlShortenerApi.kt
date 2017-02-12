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

import android.support.annotation.Keep
import com.afollestad.bridge.Bridge
import com.afollestad.bridge.annotations.Body
import jlelse.newscatchr.extensions.notNullOrBlank

class UrlShortenerApi {

	fun getShortUrl(url: String?): String? {
		if (url.notNullOrBlank()) {
			Bridge.post("https://www.googleapis.com/urlshortener/v1/url?fields=id&key=$GoogleApiKey")
					.body("{\"longUrl\":\"$url\"}")
					.header("Content-Type", "application/json")
					.asClass(Response::class.java)
					?.shortUrl
					?.let {
						if (it.notNullOrBlank()) return it
					}
		}
		return url
	}

	@Keep
	private class Response {
		@Body(name = "id")
		var shortUrl: String? = null
	}

}
