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

import com.afollestad.bridge.Bridge
import jlelse.newscatchr.extensions.jsonArray
import jlelse.newscatchr.extensions.notNullOrBlank

class TranslateApi {

	fun translate(targetLanguage: String?, query: String?): String? {
		if (targetLanguage.notNullOrBlank() && query.notNullOrBlank()) {
			return mutableListOf<String>().apply {
				query!!.split(". ").forEach {
					add(translateShort(targetLanguage!!, it))
				}
			}.joinToString(separator = ". ")
		}
		return null
	}

	private fun translateShort(targetLanguage: String, query: String): String {
		var realresponse = ""
		Bridge.get("https://translate.googleapis.com/translate_a/single?client=gtx&sl=auto&tl=%s&dt=t&q=%s", targetLanguage, query).asString()
				?.replace(",,", ",") // Fix broken JSON
				?.jsonArray()?.optJSONArray(0) // Get Array with all the translations
				?.apply {
					for (i in 0..length() - 1) {
						if (i > 0) realresponse += "<br>" // Add line break
						realresponse += optJSONArray(i)?.optString(0) // Add string
					}
				}
		return realresponse
	}

	fun languages() = arrayOf("af", "am", "ar", "az", "be", "bg", "bn", "bs", "ca", "ceb", "co", "cs", "cy", "da", "de", "el", "en", "eo", "es", "et", "eu", "fa", "fi", "fr", "fy", "ga", "gd", "gl", "gu", "ha", "haw", "hi", "hmn", "hr", "ht", "hu", "hy", "id", "ig", "is", "it", "iw", "ja", "jw", "ka", "kk", "km", "kn", "ko", "ku", "ky", "la", "lb", "lo", "lt", "lv", "mg", "mi", "mk", "ml", "mn", "mr", "ms", "mt", "my", "ne", "nl", "no", "ny", "pa", "pl", "ps", "pt", "ro", "ru", "sd", "si", "sk", "sl", "sm", "sn", "so", "sq", "sr", "st", "su", "sv", "sw", "ta", "te", "tg", "th", "tl", "tr", "uk", "ur", "uz", "vi", "xh", "yi", "yo", "zh", "zh-TW", "zu")

}