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

package jlelse.viewmanager

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.*
import android.widget.FrameLayout
import android.widget.LinearLayout
import java.util.*

abstract class ViewManagerActivity : AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		viewManagerActivity = this
		if (viewStacks.size == 0) viewStacks += initViewStacks
	}

	abstract val initViewStacks: List<Stack<ViewManagerView>>
	abstract val containerView: FrameLayout

	fun currentView(): ViewManagerView = viewStacks[currentStack].peek()
	fun allViews() = viewStacks.flatten()

	open fun onSwitchView() {
	}

	fun switchStack(stack: Int) {
		currentStack = stack
		switchView(viewStacks[stack].peek())
	}

	fun openView(view: ViewManagerView) {
		view.title = view.title ?: currentView().title ?: ""
		viewStacks[currentStack].push(view)
		switchView(view)
	}

	fun closeView() {
		viewStacks[currentStack].apply {
			pop().onDestroy()
			switchView(peek())
		}
	}

	fun resetStack() {
		resetStack(currentStack)
	}

	fun isRootView() = viewStacks[currentStack].size == 1
	fun currentStack() = currentStack

	private fun resetStack(stack: Int) {
		viewStacks[stack].apply { while (size > 1) pop().onDestroy() }
		switchStack(stack)
	}

	private fun switchView(view: ViewManagerView?) {
		if (view != null) containerView.apply {
			removeAllViews()
			view.apply { (parent as ViewGroup?)?.removeView(this) }
			addView(view.apply { onShow() })
			supportInvalidateOptionsMenu()
			onSwitchView()
		}
	}

	override fun onBackPressed() {
		if (viewStacks[currentStack].size > 1) closeView()
		else {
			currentStack = 0
			super.onBackPressed()
		}
	}

	override fun onDestroy() {
		containerView.removeAllViews()
		allViews().forEach { it.onDestroy() }
		super.onDestroy()
	}

	override fun onCreateOptionsMenu(menu: Menu?): Boolean {
		super.onCreateOptionsMenu(menu)
		menu?.clear()
		createMenu(menu)
		currentView().inflateMenu(menuInflater, menu)
		return true
	}

	open fun createMenu(menu: Menu?) {
	}

	override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
		super.onPrepareOptionsMenu(menu)
		menu?.clear()
		createMenu(menu)
		currentView().inflateMenu(menuInflater, menu)
		return true
	}

	override fun onOptionsItemSelected(item: MenuItem?): Boolean {
		super.onOptionsItemSelected(item)
		currentView().onOptionsItemSelected(item)
		return true
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		currentView().onActivityResult(requestCode, resultCode, data)
	}

	override fun recreate() {
		allViews().forEach { it.resetView() }
		super.recreate()
	}
}

private class DummyViewManagerActivity : ViewManagerActivity() {
	override val initViewStacks: List<Stack<ViewManagerView>>
		get() = listOf()
	override val containerView: FrameLayout
		get() = FrameLayout(this)
}

abstract class ViewManagerView : LinearLayout(viewManagerActivity) {
	val contentView by lazy { this }
	val context
		get() = viewManagerActivity
	var title: String? = null

	init {
		orientation = LinearLayout.VERTICAL
	}

	fun withTitle(title: String?): ViewManagerView {
		this.title = title
		return this
	}

	fun onShow() {
		if (contentView.childCount == 0) contentView.addView(onCreateView())
	}

	open fun onCreateView(): View? {
		return null
	}

	open fun onDestroy() {}

	fun openView(view: ViewManagerView) {
		context.openView(view)
	}

	fun closeView() {
		context.closeView()
	}

	fun resetView() {
		contentView.removeAllViews()
	}

	open fun inflateMenu(inflater: MenuInflater, menu: Menu?) {
	}

	open fun onOptionsItemSelected(item: MenuItem?) {
	}

	open fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
	}
}

private val viewStacks = mutableListOf<Stack<ViewManagerView>>()
private var currentStack: Int = 0
private var viewManagerActivity: ViewManagerActivity = DummyViewManagerActivity()