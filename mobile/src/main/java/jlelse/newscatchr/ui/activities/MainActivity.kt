/*
 * NewsCatchr  Copyright (C) 2016  Jan-Lukas Else
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package jlelse.newscatchr.ui.activities

import android.app.SearchManager
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import co.metalab.asyncawait.async
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.TransactionDetails
import com.bumptech.glide.Glide
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.wearable.Wearable
import com.ncapdevi.fragnav.FragNavController
import jlelse.newscatchr.backend.Feed
import jlelse.newscatchr.backend.apis.SharingApi
import jlelse.newscatchr.backend.apis.askForSharingService
import jlelse.newscatchr.backend.helpers.Preferences
import jlelse.newscatchr.backend.helpers.Tracking
import jlelse.newscatchr.customTabsHelperFragment
import jlelse.newscatchr.extensions.*
import jlelse.newscatchr.lastTab
import jlelse.newscatchr.ui.fragments.*
import jlelse.newscatchr.ui.interfaces.FAB
import jlelse.newscatchr.ui.interfaces.FragmentManipulation
import jlelse.newscatchr.ui.interfaces.FragmentValues
import jlelse.newscatchr.ui.layout.MainActivityUI
import jlelse.newscatchr.ui.views.Toolbar
import jlelse.readit.R
import me.zhanghai.android.customtabshelper.CustomTabsHelperFragment
import org.jetbrains.anko.*

class MainActivity : AppCompatActivity(), BaseFragment.FragmentNavigation {
	private lateinit var fragNavController: FragNavController
	private val toolbar: Toolbar? by lazy { find<Toolbar>(R.id.mainactivity_toolbar) }
	private val appbar: AppBarLayout? by lazy { find<AppBarLayout>(R.id.mainactivity_appbar) }
	private val fab: FloatingActionButton? by lazy { find<FloatingActionButton>(R.id.mainactivity_fab) }
	private val subtitle: TextView? by lazy { find<TextView>(R.id.mainactivity_toolbarsubtitle) }
	private val toolbarBackground: ImageView? by lazy { find<ImageView>(R.id.mainactivity_toolbarbackground) }
	private val bottomNavigationView: BottomNavigationView? by lazy { find<BottomNavigationView>(R.id.mainactivity_navigationview) }
	private var googleApiClient: GoogleApiClient? = null
	private var billingProcessor: BillingProcessor? = null

	var IABReady = false

	private val licenceKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAmnVnCUmnyl0MQek4LpMopUVNb5czY+7RVsCl0vV7LU2nAJppZqTtHoZpeFyD9ae6BtVx/f6WW/xV37KCr4Eo/4Bh796e8AYzxfOm8icw7TPU9M2FNUjJ46qtZZl5I9ItfhOoapu5tGAY8i0Z5142046UpMs0XLGeGhVsr/3RSGWSzbHRhWOKVFJqa1JgWSHUGTUUHvkVai3sWKl1acreIivio3kpNh/jY9T9xwd6pl5Xzg32i00m87BMuJaA+QofQjFWTFmsUDC0tx+nERxnUud6S/A/n2nKKkhQ3c0mz961swxarWpzrP131VbYYPmGZ0WhXt6tMsTnpg/G++2l1wIDAQAB"
	private val PRO_SKU = "prosub"

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(MainActivityUI().createView(AnkoContext.create(this, this)))
		doAsync {
			// Init Tracking
			Tracking.init(this@MainActivity)
			// Init Custom Tabs
			customTabsHelperFragment = CustomTabsHelperFragment.attachTo(this@MainActivity)
			// Init Google Api Client for Android Wear
			googleApiClient = GoogleApiClient.Builder(this@MainActivity)
					.addApiIfAvailable(Wearable.API)
					.addConnectionCallbacks(object : GoogleApiClient.ConnectionCallbacks {
						override fun onConnected(p0: Bundle?) {
						}

						override fun onConnectionSuspended(p0: Int) {
						}
					})
					.addOnConnectionFailedListener {}
					.build()
					.apply { connect() }
			// Check purchases
			if (BillingProcessor.isIabServiceAvailable(this@MainActivity)) {
				billingProcessor = BillingProcessor(this@MainActivity, licenceKey, object : BillingProcessor.IBillingHandler {
					override fun onBillingInitialized() {
						IABReady = true
						billingProcessor?.loadOwnedPurchasesFromGoogle()
						checkProStatus()
					}

					override fun onBillingError(errorCode: Int, error: Throwable?) {
					}

					override fun onProductPurchased(productId: String?, details: TransactionDetails?) = checkProStatus()
					override fun onPurchaseHistoryRestored() = checkProStatus()
				})
			}
		}

		setSupportActionBar(toolbar)

		fragNavController = FragNavController(savedInstanceState, supportFragmentManager, R.id.mainactivity_container, listOf(
				HomeFragment().apply { addTitle(R.string.news.resStr()) },
				BookmarksFragment().apply { addTitle(R.string.bookmarks.resStr()) },
				SettingsFragment().apply { addTitle(R.string.settings.resStr()) }
		), 0)
		fragNavController.setTransactionListener(object : FragNavController.TransactionListener {
			override fun onFragmentTransaction(fragment: Fragment?) = checkFragmentDependingThings()
			override fun onTabTransaction(fragment: Fragment?, index: Int) = checkFragmentDependingThings()
		})

		var firstLaunch = true
		bottomNavigationView?.apply {
			setOnNavigationItemSelectedListener { item ->
				val itemNumber = when (item.itemId) {
					R.id.bb_news -> 0
					R.id.bb_bookmarks -> 1
					R.id.bb_settings -> 2
					else -> 0
				}
				if (itemNumber == lastTab && !firstLaunch) fragNavController.clearStack()
				else fragNavController.switchTab(itemNumber)
				lastTab = itemNumber
				firstLaunch = false
				true
			}
		}
		fragNavController.switchTab(lastTab)
		findViewById(when (lastTab) {
			0 -> R.id.bb_news
			1 -> R.id.bb_bookmarks
			2 -> R.id.bb_settings
			else -> R.id.bb_news
		}).performClick()

		checkFragmentDependingThings()

		// Check Intent
		handleIntent(intent)

	}

	private fun handleIntent(intent: Intent?) {
		if (intent != null) {
			// Shortcut
			intent.getStringExtra("feedid")?.let {
				fragNavController.clearStack()
				val feedTitle = intent.getStringExtra("feedtitle")
				if (it.notNullOrBlank()) pushFragment(FeedFragment().apply {
					addObject("feed", Feed(feedId = it, title = feedTitle))
				}, feedTitle)
			}
			// Browser
			if (intent.scheme == "http" || intent.scheme == "https") {
				intent.dataString?.let {
					searchForFeeds(this, this, it)
				}
			}
			// Google Voice Search
			if (intent.action == "com.google.android.gms.actions.SEARCH_ACTION") {
				intent.getStringExtra(SearchManager.QUERY).let {
					searchForFeeds(this, this, it)
				}
			}
			// Pocket
			val currentFrag = fragNavController.currentFrag
			if (currentFrag is SettingsFragment && intent.scheme == "pocketapp45699") {
				currentFrag.progressDialog?.show()
				currentFrag.pocketAuth?.authenticate()
			}
		}
	}

	fun buildWearNotification(title: String, content: String) = async {
		if (googleApiClient?.isConnected ?: false) Wearable.NodeApi.getConnectedNodes(googleApiClient).await().nodes.forEach {
			Wearable.MessageApi.sendMessage(googleApiClient, it.id, "/newscatchr", (title + "x_x_x" + content).toByteArray()).await()
		}
	}

	fun createHomeScreenShortcut(title: String, feedId: String) {
		Intent().apply {
			putExtra("duplicate", false)
			putExtra(Intent.EXTRA_SHORTCUT_INTENT, intentFor<MainActivity>("feedtitle" to title, "feedid" to feedId).newTask().clearTop())
			putExtra(Intent.EXTRA_SHORTCUT_NAME, title)
			putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(applicationContext, R.drawable.icon))
			action = "com.android.launcher.action.INSTALL_SHORTCUT"
		}.let {
			applicationContext.sendBroadcast(it)
		}
	}

	fun resetToolbarBackground() {
		toolbarBackground?.let {
			Glide.clear(it)
			it.setImageBitmap(null)
		}
	}

	fun loadToolbarBackground(url: String?) = toolbarBackground?.loadImage(url)

	private fun checkFragmentDependingThings() {
		val currentFragment = fragNavController.currentFrag
		// Check Back Arrow
		if (fragNavController.isRootFragment) {
			supportActionBar?.setDisplayHomeAsUpEnabled(false)
			appbar?.setExpanded(if (currentFragment is FragmentManipulation) currentFragment.expanded ?: false else false)
		} else {
			supportActionBar?.setDisplayHomeAsUpEnabled(true)
			appbar?.setExpanded(if (currentFragment is FragmentManipulation) currentFragment.expanded ?: true else true)
		}
		// Check Title
		refreshFragmentDependingTitle(currentFragment)
		// Check Help Menu Item
		invalidateOptionsMenu()
		// Check FAB
		if (currentFragment is FAB) {
			fab?.let {
				if (currentFragment.fabDrawable != null) it.setImageDrawable(currentFragment.fabDrawable?.resDrw(this, Color.WHITE))
				it.onClick { currentFragment.fabClick() }
				it.showView()
				it.show()
			}
		} else {
			fab?.makeInvisible()
		}
	}

	fun refreshFragmentDependingTitle(fragment: Fragment?) = tryOrNull {
		toolbar?.title = R.string.app_name.resStr()
		if (fragment is FragmentValues) subtitle?.text = fragment.getAddedTitle()
	}

	override fun pushFragment(fragment: Fragment, title: String?) {
		val currentFrag = fragNavController.currentFrag
		if (fragment is FragmentValues) fragment.addTitle(title ?: if (currentFrag is FragmentValues) currentFrag.getAddedTitle() ?: "" else "")
		fragNavController.pushFragment(fragment)
	}

	override fun popFragment() = fragNavController.popFragment()

	private fun checkProStatus() {
		Preferences.supportUser = billingProcessor?.isSubscribed(PRO_SKU) == true
		sendBroadcast(Intent("purchaseStatus"))
		checkFragmentDependingThings()
	}

	fun purchaseSupport() = billingProcessor?.subscribe(this, PRO_SKU)

	override fun onNewIntent(intent: Intent?) {
		super.onNewIntent(intent)
		handleIntent(intent)
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		if (!(billingProcessor?.handleActivityResult(requestCode, resultCode, data) ?: false)) super.onActivityResult(requestCode, resultCode, data)
	}

	override fun onDestroy() {
		billingProcessor?.release()
		super.onDestroy()
	}

	override fun onCreateOptionsMenu(menu: Menu?): Boolean {
		super.onCreateOptionsMenu(menu)
		menuInflater.inflate(R.menu.universal, menu)
		return true
	}

	override fun onOptionsItemSelected(item: MenuItem?) = when (item?.itemId) {
		android.R.id.home -> {
			onBackPressed()
			true
		}
		R.id.share_app -> {
			askForSharingService(this, { network ->
				SharingApi(this, network).share("\" ${R.string.share_app.resStr()}\"", R.string.try_nc.resStr()!!)
			})
			true
		}
		else -> super.onOptionsItemSelected(item)
	}

	override fun onBackPressed() = if (fragNavController.currentStack.size > 1) fragNavController.popFragment() else super.onBackPressed()

	override fun onSaveInstanceState(outState: Bundle) {
		fragNavController.onSaveInstanceState(outState)
		super.onSaveInstanceState(outState)
	}
}
