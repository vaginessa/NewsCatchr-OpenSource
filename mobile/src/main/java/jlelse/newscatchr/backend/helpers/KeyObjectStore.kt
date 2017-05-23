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

package jlelse.newscatchr.backend.helpers

import com.afollestad.ason.Ason
import com.afollestad.ason.AsonArray
import jlelse.newscatchr.appContext
import jlelse.newscatchr.extensions.tryOrNull
import java.io.File

class KeyObjectStore(name: String = "default", cache: Boolean = false) {
	private var folder: File = File(if (cache) appContext?.cacheDir else appContext?.filesDir, "NCStore$name")

	init {
		if (!folder.exists()) tryOrNull { folder.mkdir() }
	}

	fun <T> write(key: String?, item: Any?): KeyObjectStore {
		val file = File(folder, key + ".nc")
		if (item == null) tryOrNull { file.delete() }
		else if (key != null && key.isNotBlank()) {
			if (item.javaClass.isArray) tryOrNull { saveAsonArray(file, Ason.serializeArray<T>(item)) }
			else tryOrNull { saveAson(file, Ason.serialize(item)) }
		}
		return this
	}

	fun delete(key: String?): KeyObjectStore {
		if (key != null && key.isNotBlank()) {
			tryOrNull { File(folder, key + ".nc").delete() }
		}
		return this
	}

	fun <T> read(key: String?, type: Class<T>, defaultValue: T? = null): T? = if (key != null) tryOrNull {
		val file = File(folder, key + ".nc")
		if (file.exists()) {
			if (type.isArray) Ason.deserialize(AsonArray<T>(file.readText()), type)
			else Ason.deserialize(Ason(file.readText()), type)
		} else null
	} ?: defaultValue else defaultValue

	fun destroy() {
		tryOrNull { folder.deleteRecursively() }
	}

	fun exists(key: String?): Boolean = if (key != null && key.isNotBlank()) File(folder, key + ".nc").exists() else false

	private fun saveAson(file: File, ason: Ason) {
		if (!file.exists()) tryOrNull(print = true) { file.createNewFile() }
		tryOrNull { file.writeText(ason.toString()) }
	}

	private fun <T> saveAsonArray(file: File, ason: AsonArray<T>) {
		if (!file.exists()) tryOrNull { file.createNewFile() }
		tryOrNull { file.writeText(ason.toString()) }
	}

}