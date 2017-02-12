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