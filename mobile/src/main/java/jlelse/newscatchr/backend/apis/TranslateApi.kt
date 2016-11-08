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
import jlelse.newscatchr.extensions.forEach
import jlelse.newscatchr.extensions.jsonArray
import jlelse.newscatchr.extensions.notNullOrBlank
import jlelse.newscatchr.extensions.string

class TranslateApi {

	fun translate(targetLanguage: String?, query: String?): String? {
		if (targetLanguage.notNullOrBlank() && query.notNullOrBlank()) {
			var realresponse = ""
			Bridge.get("https://translate.googleapis.com/translate_a/single?client=gtx&sl=auto&tl=%s&dt=t&q=%s", targetLanguage, query).asString()
					?.replace(",,", ",") // Fix broken JSON
					?.jsonArray()?.jsonArray(0) // Get Array with all the translations
					?.forEach { array, index ->
						if (index > 0) realresponse += "<br>" // Add line break
						realresponse += array.jsonArray(index)?.string(0) // Add string
					}
			return realresponse
		}
		return null
	}

}