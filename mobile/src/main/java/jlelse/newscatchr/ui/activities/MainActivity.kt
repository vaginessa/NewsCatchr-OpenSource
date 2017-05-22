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

@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package jlelse.newscatchr.ui.activities

import android.app.SearchManager
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.TransactionDetails
import com.bumptech.glide.Glide
import jlelse.newscatchr.backend.Feed
import jlelse.newscatchr.backend.apis.SharingApi
import jlelse.newscatchr.backend.apis.askForSharingService
import jlelse.newscatchr.backend.helpers.Preferences
import jlelse.newscatchr.backend.helpers.Tracking
import jlelse.newscatchr.customTabsHelperFragment
import jlelse.newscatchr.extensions.*
import jlelse.newscatchr.lastTab
import jlelse.newscatchr.ui.fragments.BookmarksView
import jlelse.newscatchr.ui.fragments.FeedView
import jlelse.newscatchr.ui.fragments.HomeView
import jlelse.newscatchr.ui.fragments.SettingsView
import jlelse.newscatchr.ui.interfaces.FAB
import jlelse.newscatchr.ui.interfaces.FragmentManipulation
import jlelse.newscatchr.ui.layout.MainActivityUI
import jlelse.readit.R
import jlelse.viewmanager.ViewManagerActivity
import jlelse.viewmanager.ViewManagerView
import me.zhanghai.android.customtabshelper.CustomTabsHelperFragment
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.find
import java.util.*

class MainActivity : ViewManagerActivity() {
	private val toolbar: Toolbar? by lazy { find<Toolbar>(R.id.mainactivity_toolbar) }
	private val appbar: AppBarLayout? by lazy { find<AppBarLayout>(R.id.mainactivity_appbar) }
	private val fab: FloatingActionButton? by lazy { find<FloatingActionButton>(R.id.mainactivity_fab) }
	private val subtitle: TextView? by lazy { find<TextView>(R.id.mainactivity_toolbarsubtitle) }
	private val toolbarBackground: ImageView? by lazy { find<ImageView>(R.id.mainactivity_toolbarbackground) }
	private val bottomNavigationView: BottomNavigationView? by lazy { find<BottomNavigationView>(R.id.mainactivity_navigationview) }
	private var billingProcessor: BillingProcessor? = null

	var IABReady = false

	private val licenceKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAmnVnCUmnyl0MQek4LpMopUVNb5czY+7RVsCl0vV7LU2nAJppZqTtHoZpeFyD9ae6BtVx/f6WW/xV37KCr4Eo/4Bh796e8AYzxfOm8icw7TPU9M2FNUjJ46qtZZl5I9ItfhOoapu5tGAY8i0Z5142046UpMs0XLGeGhVsr/3RSGWSzbHRhWOKVFJqa1JgWSHUGTUUHvkVai3sWKl1acreIivio3kpNh/jY9T9xwd6pl5Xzg32i00m87BMuJaA+QofQjFWTFmsUDC0tx+nERxnUud6S/A/n2nKKkhQ3c0mz961swxarWpzrP131VbYYPmGZ0WhXt6tMsTnpg/G++2l1wIDAQAB"
	private val PRO_SKU = "prosub"

	override val initViewStacks: List<Stack<ViewManagerView>>
		get() = listOf(
				Stack<ViewManagerView>().apply { add(HomeView().apply { title = R.string.news.resStr() }) },
				Stack<ViewManagerView>().apply { add(BookmarksView().apply { title = R.string.bookmarks.resStr() }) },
				Stack<ViewManagerView>().apply { add(SettingsView().apply { title = R.string.settings.resStr() }) }
		)
	override val containerView: FrameLayout
		get() = find(R.id.mainactivity_container)

	override fun onCreate(savedInstanceState: Bundle?) {
		setContentView(MainActivityUI().createView(AnkoContext.create(this, this)))
		super.onCreate(savedInstanceState)
		doAsync {
			// Init Tracking
			Tracking.init(this@MainActivity)
			// Init Custom Tabs
			customTabsHelperFragment = CustomTabsHelperFragment.attachTo(this@MainActivity)
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
		bottomNavigationView?.apply {
			selectedItemId = when (lastTab) {
				1 -> R.id.bb_bookmarks
				2 -> R.id.bb_settings
				else -> R.id.bb_news
			}
			setOnNavigationItemSelectedListener { item ->
				val itemNumber = when (item.itemId) {
					R.id.bb_news -> 0
					R.id.bb_bookmarks -> 1
					R.id.bb_settings -> 2
					else -> 0
				}
				if (itemNumber == lastTab) resetStack()
				else switchStack(itemNumber)
				lastTab = itemNumber
				true
			}
		}
		checkFragmentDependingThings()
		handleIntent(intent)
		switchStack(currentStack())
	}

	override fun onSwitchView() {
		super.onSwitchView()
		checkFragmentDependingThings()
	}

	private fun handleIntent(intent: Intent?) {
		if (intent != null) {
			// Shortcut
			intent.getStringExtra("feedid")?.let {
				resetStack()
				val feedTitle = intent.getStringExtra("feedtitle")
				if (it.notNullOrBlank()) openView(FeedView(feed = Feed(feedId = it, title = feedTitle)).apply { title = feedTitle })
			}
			// Browser
			if (intent.scheme == "http" || intent.scheme == "https") {
				intent.dataString?.let {
					searchForFeeds(this, currentView(), it)
				}
			}
			// Google Voice Search
			if (intent.action == "com.google.android.gms.actions.SEARCH_ACTION") {
				intent.getStringExtra(SearchManager.QUERY).let {
					searchForFeeds(this, currentView(), it)
				}
			}
			// Pocket
			val currentFrag = currentView()
			if (currentFrag is SettingsView && intent.scheme == "pocketapp45699") currentFrag.finishPocketAuth()
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
		val currentFragment = currentView()
		// Check Back Arrow
		if (isRootView()) {
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
				it.setOnClickListener { currentFragment.fabClick() }
				it.showView()
				it.show()
			}
		} else {
			fab?.makeInvisible()
		}
	}

	fun refreshFragmentDependingTitle(fragment: ViewManagerView?) = tryOrNull {
		toolbar?.title = R.string.app_name.resStr()
		subtitle?.text = fragment?.title
	}

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

	override fun createMenu(menu: Menu?) {
		super.createMenu(menu)
		menuInflater.inflate(R.menu.universal, menu)
	}

	override fun onOptionsItemSelected(item: MenuItem?): Boolean {
		super.onOptionsItemSelected(item)
		when (item?.itemId) {
			android.R.id.home -> onBackPressed()
			R.id.share_app -> askForSharingService(this, { network ->
				SharingApi(this, network).share("\" ${R.string.share_app.resStr()}\"", R.string.try_nc.resStr()!!)
			})
		}
		return true
	}

	override fun onBackPressed() = if (isRootView()) super.onBackPressed() else closeView()

}
