/*
 * NewsCatchr  Copyright (C) 2016  Jan-Lukas Else
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package jlelse.newscatchr.ui.fragments

import android.os.Bundle
import android.support.annotation.IdRes
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import jlelse.newscatchr.extensions.tryOrNull
import org.json.JSONArray
import java.util.*

// Inspired much by https://github.com/ncapdevi/FragNav/blob/master/frag-nav/src/main/java/com/ncapdevi/fragnav/FragNavController.java

class FragNavController(val mNavListener: FragNavController.NavListener?, savedInstanceState: Bundle?, private val mFragmentManager: FragmentManager, @IdRes private val mContainerId: Int, baseFragments: List<Fragment>) {
    private val mFragmentStacks: MutableList<Stack<Fragment>>
    private var mSelectedTabIndex = -1
    private var mTagCount: Int = 0
    private var mCurrentFrag: Fragment? = null

    private val EXTRA_TAG_COUNT = "${FragNavController::class.java.name}:EXTRA_TAG_COUNT"
    private val EXTRA_SELECTED_TAB_INDEX = "${FragNavController::class.java.name}:EXTRA_SELECTED_TAB_INDEX"
    private val EXTRA_CURRENT_FRAGMENT = "${FragNavController::class.java.name}:EXTRA_CURRENT_FRAGMENT"
    private val EXTRA_FRAGMENT_STACK = "${FragNavController::class.java.name}:EXTRA_FRAGMENT_STACK"

    init {
        mFragmentStacks = mutableListOf<Stack<Fragment>>()
        if (savedInstanceState == null) baseFragments.forEach { mFragmentStacks.add(Stack<Fragment>().apply { add(it) }) }
        else onRestoreFromBundle(savedInstanceState, baseFragments)
    }

    fun switchTab(index: Int) {
        if (index >= mFragmentStacks.size) throw IndexOutOfBoundsException("Can't switch to a tab that hasn't been initialized, Index : $index, current stack size : ${mFragmentStacks.size}.")
        else if (mSelectedTabIndex != index) {
            mSelectedTabIndex = index
            val ft = mFragmentManager.beginTransaction()
            detachCurrentFragment(ft)
            var fragment = reattachPreviousFragment(ft)
            if (fragment == null) {
                fragment = mFragmentStacks[mSelectedTabIndex].peek()
                ft.add(mContainerId, fragment, generateTag(fragment))
            }
            ft.commit()
            mCurrentFrag = fragment
            mNavListener?.onTabTransaction(mCurrentFrag, mSelectedTabIndex)
        }
    }

    fun push(fragment: Fragment?) {
        if (fragment != null) {
            mFragmentManager.beginTransaction().apply {
                detachCurrentFragment(this)
                add(mContainerId, fragment, generateTag(fragment))
            }.commit()
            mFragmentManager.executePendingTransactions()
            mFragmentStacks[mSelectedTabIndex].push(fragment)
            mCurrentFrag = fragment
            mNavListener?.onFragmentTransaction(mCurrentFrag)
        }
    }

    fun pop() {
        val poppingFrag = currentFragment
        if (poppingFrag != null) {
            val ft = mFragmentManager.beginTransaction().apply { remove(poppingFrag) }
            val fragmentStack = mFragmentStacks[mSelectedTabIndex]
            if (!fragmentStack.isEmpty()) fragmentStack.pop()
            var fragment = reattachPreviousFragment(ft)
            if (fragment == null && !fragmentStack.isEmpty()) {
                fragment = fragmentStack.peek()
                ft.add(mContainerId, fragment, fragment.tag)
            }
            ft.commit()
            mFragmentManager.executePendingTransactions()
            mCurrentFrag = fragment
            mNavListener?.onFragmentTransaction(mCurrentFrag)
        }
    }

    fun clearStack() {
        val fragmentStack = mFragmentStacks[mSelectedTabIndex]
        if (fragmentStack.size > 1) {
            var fragment: Fragment?
            val ft = mFragmentManager.beginTransaction()
            while (fragmentStack.size > 1) {
                fragment = mFragmentManager.findFragmentByTag(fragmentStack.peek().tag)
                if (fragment != null) {
                    fragmentStack.pop()
                    ft.remove(fragment)
                }
            }
            fragment = reattachPreviousFragment(ft)
            if (fragment != null) ft.commit()
            else {
                if (!fragmentStack.isEmpty()) {
                    fragment = fragmentStack.peek()
                    ft.add(mContainerId, fragment, fragment.tag)
                    ft.commit()
                }
            }
            mFragmentStacks[mSelectedTabIndex] = fragmentStack
            mCurrentFrag = fragment
            mNavListener?.onFragmentTransaction(mCurrentFrag)
        }
    }

    fun onSaveInstanceState(outState: Bundle?) {
        outState?.putInt(EXTRA_TAG_COUNT, mTagCount)
        outState?.putInt(EXTRA_SELECTED_TAB_INDEX, mSelectedTabIndex)
        outState?.putString(EXTRA_CURRENT_FRAGMENT, mCurrentFrag?.tag)
        tryOrNull {
            val stackArrays = JSONArray()
            mFragmentStacks.forEach {
                val stackArray = JSONArray()
                it.forEach {
                    stackArray.put(it.tag)
                }
                stackArrays.put(stackArray)
            }
            outState?.putString(EXTRA_FRAGMENT_STACK, stackArrays.toString())
        }
    }

    private fun reattachPreviousFragment(ft: FragmentTransaction): Fragment? {
        val fragmentStack = mFragmentStacks[mSelectedTabIndex]
        var fragment: Fragment? = null
        if (!fragmentStack.isEmpty()) fragment = mFragmentManager.findFragmentByTag(fragmentStack.peek().tag)?.apply { ft.attach(this) }
        return fragment
    }

    private fun detachCurrentFragment(ft: FragmentTransaction) = currentFragment?.let { ft.detach(it) }

    private fun generateTag(fragment: Fragment) = fragment.javaClass.name + ++mTagCount

    private fun onRestoreFromBundle(savedInstanceState: Bundle, baseFragments: List<Fragment>) {
        mSelectedTabIndex = savedInstanceState.getInt(EXTRA_SELECTED_TAB_INDEX, -1)
        mTagCount = savedInstanceState.getInt(EXTRA_TAG_COUNT, 0)
        mCurrentFrag = mFragmentManager.findFragmentByTag(savedInstanceState.getString(EXTRA_CURRENT_FRAGMENT))
        try {
            val stackArrays = JSONArray(savedInstanceState.getString(EXTRA_FRAGMENT_STACK))
            for (x in 0..stackArrays.length() - 1) {
                val stackArray = stackArrays.getJSONArray(x)
                val stack = Stack<Fragment>()
                if (stackArray.length() == 1) {
                    val tag = stackArray.getString(0)
                    (if (tag == null || "null".equals(tag, ignoreCase = true)) baseFragments[x] else mFragmentManager.findFragmentByTag(tag))?.let { stack.add(it) }
                } else {
                    for (y in 0..stackArray.length() - 1) {
                        val tag = stackArray.getString(y)
                        if (tag != null && !"null".equals(tag, ignoreCase = true)) mFragmentManager.findFragmentByTag(tag)?.let { stack.add(it) }
                    }
                }
                mFragmentStacks.add(stack)
            }
        } catch (t: Throwable) {
            mFragmentStacks.clear()
            baseFragments.forEach {
                mFragmentStacks.add(Stack<Fragment>().apply {
                    add(it)
                })
            }
        }

    }

    val size: Int
        get() = mFragmentStacks.size

    val currentFragment: Fragment?
        get() = if (mCurrentFrag != null) mCurrentFrag
        else {
            val fragmentStack = mFragmentStacks[mSelectedTabIndex]
            if (!fragmentStack.isEmpty()) mFragmentManager.findFragmentByTag(mFragmentStacks[mSelectedTabIndex].peek().tag) else null
        }

    val currentStack: Stack<Fragment>
        get() = mFragmentStacks[mSelectedTabIndex]

    interface NavListener {
        fun onTabTransaction(fragment: Fragment?, index: Int)
        fun onFragmentTransaction(fragment: Fragment?)
    }
}