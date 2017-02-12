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

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.design.widget.Snackbar
import com.afollestad.json.Ason
import com.afollestad.materialdialogs.MaterialDialog
import com.cloudrail.si.CloudRail
import com.cloudrail.si.interfaces.CloudStorage
import com.cloudrail.si.services.Dropbox
import com.cloudrail.si.services.GoogleDrive
import com.cloudrail.si.services.OneDrive
import jlelse.newscatchr.backend.Article
import jlelse.newscatchr.backend.Feed
import jlelse.newscatchr.backend.helpers.Database
import jlelse.newscatchr.extensions.notNullAndEmpty
import jlelse.newscatchr.extensions.notNullOrBlank
import jlelse.newscatchr.extensions.readString
import jlelse.newscatchr.extensions.resStr
import jlelse.newscatchr.ui.activities.MainActivity
import jlelse.newscatchr.ui.views.ProgressDialog
import jlelse.readit.R
import org.jetbrains.anko.doAsync
import java.io.File
import java.io.InputStream

class CloudBackupApi(val context: Activity, storage: Storage, val finished: () -> Unit) {
	private var cloudStorage: CloudStorage
	private var progressDialog: ProgressDialog = ProgressDialog(context).apply { show() }

	private val favoritesFile = "feeds.nc"
	private val bookmarksFile = "bookmarks.nc"
	private val readUrlsFile = "readurls.nc"
	private val folder = "NewsCatchr"

	init {
		cloudStorage = when (storage) {
			Storage.OneDrive -> OneDrive(context, OneDriveClientID, OneDriveClientSecret)
			Storage.GoogleDrive -> GoogleDrive(context, GoogleDriveClientID, GoogleDriveClientSecret)
			Storage.DropBox -> Dropbox(context, DropboxClientID, DropboxClientSecret)
		}
	}

	fun backup(): CloudBackupApi {
		context.doAsync {
			val success = try {
				backupBookmarks() && backupFavorites() && backupReadUrls()
			} catch (e: Exception) {
				e.printStackTrace()
				false
			}
			context.runOnUiThread {
				progressDialog.dismiss()
				Snackbar.make(context.findViewById(R.id.mainactivity_container), if (success) R.string.suc_backup else R.string.backup_failed, Snackbar.LENGTH_SHORT).show()
				finished()
			}
		}
		return this
	}

	private fun backupBookmarks(): Boolean {
		val file = File("${context.filesDir.path}/$bookmarksFile").apply {
			delete()
			createNewFile()
			writeText(Ason.serialize(Database.allBookmarks).toString())
		}
		return if (file.exists()) uploadFile(bookmarksFile, file.inputStream(), file.length()) else false
	}

	private fun backupFavorites(): Boolean {
		val file = File("${context.filesDir.path}/$favoritesFile").apply {
			delete()
			createNewFile()
			writeText(Ason.serialize(Database.allFavorites).toString())
		}
		return if (file.exists()) uploadFile(favoritesFile, file.inputStream(), file.length()) else false
	}

	private fun backupReadUrls(): Boolean {
		val file = File("${context.filesDir.path}/$readUrlsFile").apply {
			delete()
			createNewFile()
			writeText(Ason.serialize(Database.allReadUrls).toString())
		}
		return if (file.exists()) uploadFile(readUrlsFile, file.inputStream(), file.length()) else false
	}

	fun restore(): CloudBackupApi {
		context.doAsync {
			val success = try {
				restoreBookmarks() && restoreFavorites() && restoreReadUrls()
			} catch (e: Exception) {
				e.printStackTrace()
				false
			}
			context.runOnUiThread {
				context.sendBroadcast(Intent("favorites_updated"))
				progressDialog.dismiss()
				Snackbar.make(context.findViewById(R.id.mainactivity_container), if (success) R.string.suc_restore else R.string.restore_failed, Snackbar.LENGTH_SHORT).show()
				finished()
			}
		}
		return this
	}

	private fun restoreBookmarks(): Boolean {
		val file = File("${context.filesDir.path}/$bookmarksFile")
		return if (downloadFile(bookmarksFile, file)) {
			try {
				Ason.deserialize(Ason(file.readText()), Array<Article>::class.java)?.let {
					if (it.notNullAndEmpty()) Database.allBookmarks = it
				}
				true
			} catch (e: Exception) {
				e.printStackTrace()
				false
			}
		} else false
	}

	private fun restoreFavorites(): Boolean {
		val file = File("${context.filesDir.path}/$favoritesFile")
		return if (downloadFile(favoritesFile, file)) {
			try {
				Ason.deserialize(Ason(file.readText()), Array<Feed>::class.java)?.let {
					if (it.notNullAndEmpty()) Database.allFavorites = it
				}
				true
			} catch (e: Exception) {
				e.printStackTrace()
				false
			}
		} else false
	}

	private fun restoreReadUrls(): Boolean {
		val file = File("${context.filesDir.path}/$readUrlsFile")
		return if (downloadFile(readUrlsFile, file)) {
			try {
				Ason.deserialize(Ason(file.readText()), Array<String>::class.java)?.let {
					if (it.notNullAndEmpty()) Database.allReadUrls = it.toSet()
				}
				true
			} catch (e: Exception) {
				e.printStackTrace()
				false
			}
		} else false
	}

	private fun uploadFile(name: String, inputStream: InputStream, size: Long): Boolean {
		try {
			cloudStorage.createFolder("/$folder")
		} catch (e: Exception) {
			e.printStackTrace()
		}
		return try {
			cloudStorage.upload("/$folder/$name", inputStream, size, true)
			true
		} catch (e: Exception) {
			e.printStackTrace()
			false
		} finally {
			inputStream.close()
		}
	}

	private fun downloadFile(name: String, destination: File): Boolean {
		return try {
			var success = false
			cloudStorage.download("/$folder/$name")?.readString()?.let {
				if (it.notNullOrBlank()) {
					destination.delete()
					destination.createNewFile()
					destination.writeText(it)
					success = true
				}
			}
			success
		} catch (e: Exception) {
			e.printStackTrace()
			false
		}
	}

	enum class Storage {
		OneDrive, GoogleDrive, DropBox
	}

}

fun backupRestore(context: MainActivity, callback: () -> Unit) {
	MaterialDialog.Builder(context)
			.items(R.string.backup.resStr(), R.string.restore.resStr())
			.itemsCallback { _, _, which, _ ->
				askForCloudBackupService(context, { storage ->
					CloudBackupApi(context, storage, { callback() }).let {
						when (which) {
							0 -> it.backup()
							else -> it.restore()
						}
					}
				})
			}
			.negativeText(android.R.string.cancel)
			.show()
}

private fun askForCloudBackupService(context: Context, storage: (CloudBackupApi.Storage) -> Unit) {
	MaterialDialog.Builder(context)
			.items(R.string.dropbox.resStr(), R.string.google_drive.resStr(), R.string.onedrive.resStr())
			.itemsCallback { _, _, which, _ ->
				when (which) {
					0 -> storage(CloudBackupApi.Storage.DropBox)
					1 -> storage(CloudBackupApi.Storage.GoogleDrive)
					2 -> storage(CloudBackupApi.Storage.OneDrive)
				}
			}
			.negativeText(android.R.string.cancel)
			.show()
}