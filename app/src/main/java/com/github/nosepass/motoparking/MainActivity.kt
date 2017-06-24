package com.github.nosepass.motoparking

import android.arch.lifecycle.LifecycleActivity
import android.os.Bundle
import android.support.design.widget.NavigationView.OnNavigationItemSelectedListener
import android.support.design.widget.Snackbar
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.view.MenuItem
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import android.arch.lifecycle.ViewModelProviders



/**
 * Landing screen with side nav but mainly a map.
 */
class MainActivity : LifecycleActivity(), OnNavigationItemSelectedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ViewModelProviders.of(this).get(MainVM::class.java)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        val toggle = ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        nav.setNavigationItemSelectedListener(this)
    }

    override fun onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        val id = item.itemId

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.settings) {

        }

        val drawer = findViewById<View>(R.id.drawer) as DrawerLayout
        drawer.closeDrawer(GravityCompat.START)
        return true
    }
}
