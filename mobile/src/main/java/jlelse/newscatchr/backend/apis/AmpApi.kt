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
import com.afollestad.bridge.annotations.ContentType
import jlelse.newscatchr.extensions.notNullOrBlank

class AmpApi {

	private val cachedUrls = mutableMapOf<String, String>()

	fun getAmpUrl(url: String?): String? {
		if (cachedUrls.contains(url)) return cachedUrls[url]
		if (url.notNullOrBlank()) {
			Bridge.post("https://acceleratedmobilepageurl.googleapis.com/v1/ampUrls:batchGet?fields=ampUrls%2FcdnAmpUrl&key=$GoogleApiKey")
					.body("{\"urls\":[\"$url\"]}")
					.connectTimeout(2500)
					.readTimeout(2500)
					.asClass(Response::class.java)
					?.ampUrls?.firstOrNull()?.cdnAmpUrl
					?.let {
						if (it.notNullOrBlank()) return cacheAndReturn(url, it)
					}
			return cacheAndReturn(url, "https://googleweblight.com/?lite_url=$url")
		}
		return null
	}

	private fun cacheAndReturn(url: String?, response: String?): String? {
		if (url.notNullOrBlank() && response.notNullOrBlank()) cachedUrls.put(url!!, response!!)
		return response
	}

	@Keep
	@ContentType("application/json")
	private class Response {
		@Body
		var ampUrls: Array<AmpUrlObject>? = null
	}

	@Keep
	@ContentType("application/json")
	private class AmpUrlObject {
		@Body
		var cdnAmpUrl: String? = null
	}

}
