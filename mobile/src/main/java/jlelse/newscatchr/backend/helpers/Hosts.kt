package jlelse.newscatchr.backend.helpers

import android.content.Context
import android.net.Uri
import com.mcxiaoke.koi.async.asyncUnsafe
import com.mcxiaoke.koi.ext.closeQuietly
import io.paperdb.Paper
import jlelse.newscatchr.extensions.notNullOrBlank
import java.io.BufferedReader
import java.util.*

class Hosts(val context: Context) {
	private val hostsVersion = 1
	private val HOSTS = "hosts"
	private val book = Paper.book(HOSTS)
	private var cached = HashMap<String, Boolean>()

	init {
		if (hostsVersion > Preferences.hostsVersion) context.asyncUnsafe { reload() }
	}

	fun checkUrl(url: String?): String? {
		return if (url.isNullOrBlank() || !isBlocked(url)) url
		else "https://pixabay.com/static/uploads/photo/2016/08/13/23/13/news-1591767_640.jpg"
	}

	private fun isBlocked(url: String?): Boolean {
		return if (url.notNullOrBlank()) {
			if (cached.contains(url)) cached[url] ?: false
			else {
				val blocked = isAdHost(Uri.parse(url).host)
				cached.put(url!!, blocked)
				blocked
			}
		} else false
	}

	private fun isAdHost(host: String): Boolean {
		if (host.isNullOrBlank()) return false
		val index = host.indexOf(".")
		return index >= 0 && (contains(host) || index + 1 < host.length && isAdHost(host.substring(index + 1)))
	}

	private fun contains(host: String?) = book.exist(host)

	private fun reload() {
		var reader: BufferedReader? = null
		try {
			reader = context.assets?.open("hosts")?.bufferedReader()
			reader?.readLines()?.forEach {
				if (it.length > 0 && !it.startsWith("#") && !contains(it)) book.write(it, 1)
			}
			Preferences.hostsVersion = hostsVersion
		} catch (e: Exception) {
			e.printStackTrace()
		} finally {
			reader.closeQuietly()
		}
	}
}
