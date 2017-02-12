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

package jlelse.newscatchr.ui.interfaces

import jlelse.newscatchr.extensions.tryOrNull

interface FragmentValues {

	val valueMap: MutableMap<String, Any?>?

	fun addObject(key: String, objectToSave: Any?) {
		valueMap?.put(key, objectToSave)
	}

	fun addString(key: String, string: String?) {
		addObject(key, string)
	}

	fun addTitle(title: String?) {
		addString("ncTitle", title)
	}

	@Suppress("UNCHECKED_CAST")
	fun <T> getAddedObject(key: String): T? {
		return tryOrNull { valueMap?.get(key) as T? }
	}

	fun getAddedString(key: String): String? {
		return getAddedObject(key)
	}

	fun getAddedTitle(): String? {
		return getAddedString("ncTitle")
	}

}