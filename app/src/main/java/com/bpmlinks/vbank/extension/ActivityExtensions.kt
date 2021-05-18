package com.bpmlinks.vbank.extension

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction


inline fun FragmentManager.inTransaction(func: FragmentTransaction.() -> FragmentTransaction) {
    beginTransaction().func().addToBackStack(null).commitAllowingStateLoss()
}

fun AppCompatActivity.addFragment(fragment: Fragment, frameId: Int) {
    supportFragmentManager.inTransaction {
        add(frameId, fragment)
    }
}

fun AppCompatActivity.replaceFragment(fragment: Fragment, frameId: Int) {
    supportFragmentManager.inTransaction { replace(frameId, fragment) }
}

inline fun <T : Fragment> T.withArgs(argsBuilder: Bundle.() -> Unit): T = this.apply { arguments = Bundle().apply(argsBuilder) }

fun AppCompatActivity.findCurrentFragment(containerId: Int): Fragment? {
    return supportFragmentManager.findFragmentById(containerId)
}

fun AppCompatActivity.findCurrentVisibleFragment(): Fragment? {
    val fragmentManager = this.supportFragmentManager
    val fragments = fragmentManager.fragments
    for (fragment in fragments) {
        if (fragment != null && fragment.isVisible)
            return fragment as Fragment
    }
    return null
}