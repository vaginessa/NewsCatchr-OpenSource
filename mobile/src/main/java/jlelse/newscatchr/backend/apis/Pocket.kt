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
import com.afollestad.json.Ason
import jlelse.newscatchr.backend.helpers.Preferences
import jlelse.newscatchr.extensions.jsonObject
import jlelse.newscatchr.extensions.tryOrNull
import org.jetbrains.anko.doAsync
import org.json.JSONObject

/**
 * Everything has to be called from async thread
 */
class Pocket {

	fun add(url: String): String? = tryOrNull {
		Bridge.post("https://getpocket.com/v3/add")
				.header("Host", "getpocket.com")
				.header("Content-Type", "application/json; charset=UTF-8")
				.header("X-Accept", "application/json")
				.body(Ason().put("url", url).put("consumer_key", PocketApiKey).put("access_token", Preferences.pocketAccessToken).toStockJson())
				.asJsonObject()?.optString("item_id")
	}

	fun archive(itemId: String) {
		tryOrNull {
			Bridge.post("https://getpocket.com/v3/send")
					.header("Host", "getpocket.com")
					.header("Content-Type", "application/json; charset=UTF-8")
					.header("X-Accept", "application/json")
					.body(ArchiveItem().apply {
						consumer_key = PocketApiKey
						access_token = Preferences.pocketAccessToken
						actions = arrayOf(ArchiveActionItem().apply {
							item_id = itemId
						})
					})
					.response()
		}
	}

	fun get(): Array<GetResponseItemNew>? = tryOrNull {
		mutableListOf<GetResponseItemNew>().apply {
			Bridge.post("https://getpocket.com/v3/get")
					.header("Host", "getpocket.com")
					.header("Content-Type", "application/json; charset=UTF-8")
					.header("X-Accept", "application/json")
					.body(GetRequest().apply {
						consumer_key = PocketApiKey
						access_token = Preferences.pocketAccessToken
					})
					.response()
					?.asJsonObject()
					?.optJSONObject("list")
					?.let {
						for (i in 0..(it.names()?.length() ?: 1) - 1) {
							(it.get(it.names().getString(i))?.toString() ?: "").let {
								add(GetResponseItemNew(it).apply {
									author = it.jsonObject()?.optJSONObject("authors")?.names()?.getString(0)
								})
							}
						}
					}
		}.toTypedArray()
	}
}

@Keep
@ContentType("application/json")
class ArchiveItem {
	@Body
	var actions: Array<ArchiveActionItem>? = null
	@Body
	var consumer_key: String? = null
	@Body
	var access_token: String? = null
}

@Keep
@ContentType("application/json")
class ArchiveActionItem {
	@Body
	val action = "archive"
	@Body
	var item_id: String? = null
}

@Keep
@ContentType("application/json")
class GetRequest {
	@Body
	var detailType = "complete"
	@Body
	var consumer_key: String? = null
	@Body
	var access_token: String? = null
}

@Keep
class GetResponseItemNew(val json: String?) {
	var item_id: String? = null
	var given_url: String? = null
	var resolved_title: String? = null
	var excerpt: String? = null
	var images: GetResponseItemImagesNew? = null
	var author: String? = null

	init {
		json?.jsonObject()?.let {
			item_id = it.optString("item_id")
			given_url = it.optString("given_url")
			resolved_title = it.optString("resolved_title")
			excerpt = it.optString("excerpt")
			images = GetResponseItemImagesNew(it.optJSONObject("images"))
			author = it.optString("author")
		}
	}
}

@Keep
class GetResponseItemImagesNew(jsonObject: JSONObject?) {
	var one: GetResponseItemImageNew? = null

	init {
		one = GetResponseItemImageNew(jsonObject?.optJSONObject("1"))
	}
}

@Keep
class GetResponseItemImageNew(jsonObject: JSONObject?) {
	var src: String? = null

	init {
		src = jsonObject?.optString("src")
	}
}

/**
 * Everything can be called from UI thread
 */
class PocketAuth(val pocketRedirectUri: String, val pocketCallback: PocketAuthCallback) {
	private var pocketCode: String? = null
	private var accessToken: String? = null
	private var userName: String? = null

	fun startAuth() {
		doAsync {
			try {
				Bridge.post("https://getpocket.com/v3/oauth/request")
						.header("Host", "getpocket.com")
						.header("Content-Type", "application/json; charset=UTF-8")
						.header("X-Accept", "application/json")
						.body(FirstRequest().apply {
							consumer_key = PocketApiKey
							redirect_uri = pocketRedirectUri
						})
						.response()
						?.let {
							when (it.header("X-Error-Code")) {
								"138", "140", "152", "199" -> pocketCallback.failed()
								else -> authorize(it.asClass(FirstResponse::class.java)?.code ?: "")
							}
						}
			} catch(exception: Exception) {
				pocketCallback.failed()
			}
		}
	}

	private fun authorize(code: String) {
		pocketCode = code.apply {
			if (isNullOrBlank()) pocketCallback.failed()
		}
		pocketCallback.authorize("https://getpocket.com/auth/authorize?request_token=$code&redirect_uri=$pocketRedirectUri")
	}

	fun authenticate() {
		if (pocketCode.isNullOrBlank()) pocketCallback.failed()
		else {
			doAsync {
				try {
					Bridge.post("https://getpocket.com/v3/oauth/authorize")
							.header("Host", "getpocket.com")
							.header("Content-Type", "application/json; charset=UTF-8")
							.header("X-Accept", "application/json")
							.body(SecondRequest().apply {
								consumer_key = PocketApiKey
								code = pocketCode
							})
							.response()
							?.let {
								when (it.header("X-Error-Code")) {
									"138", "152", "181", "182", "185", "158", "159", "199" -> pocketCallback.failed()
									else -> {
										it.asClass(SecondResponse::class.java)?.let {
											authenticated(it.access_token ?: "", it.username ?: "")
										}
									}
								}
							}
				} catch(exception: Exception) {
					pocketCallback.failed()
				}
			}
		}
	}

	private fun authenticated(accessToken: String, userName: String) {
		this.accessToken = accessToken
		this.userName = userName
		if (accessToken.isNullOrBlank() || userName.isNullOrBlank()) pocketCallback.failed()
		else pocketCallback.authenticated(accessToken, userName)
	}

	interface PocketAuthCallback {
		fun failed()
		fun authorize(url: String)
		fun authenticated(accessToken: String, userName: String)
	}

	@Keep
	@ContentType("application/json")
	class FirstRequest {
		@Body
		var consumer_key: String? = null
		@Body
		var redirect_uri: String? = null
	}

	@Keep
	@ContentType("application/json")
	class FirstResponse {
		@Body
		var code: String? = null
	}

	@Keep
	@ContentType("application/json")
	class SecondRequest {
		@Body
		var consumer_key: String? = null
		@Body
		var code: String? = null
	}

	@Keep
	@ContentType("application/json")
	class SecondResponse {
		@Body
		var access_token: String? = null
		@Body
		var username: String? = null
	}

}