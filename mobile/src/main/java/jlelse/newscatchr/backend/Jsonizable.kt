package jlelse.newscatchr.backend

import com.afollestad.json.Ason

interface Jsonizable<out T> {

	val arraySeparator: String
		get() = "xNCxNCx"

	fun toAson(): Ason

	fun toJson(): String = toAson().toString()

	fun fromJson(json: String?): T

}