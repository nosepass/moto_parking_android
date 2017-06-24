package com.github.nosepass.motoparking

import android.arch.lifecycle.ViewModel
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_main.*

/**
 * biz logic for MainActivity
 */
class MainVM : ViewModel() {
    val progress = PublishSubject.create<Float>()

    init {

    }

    override fun onCleared() {

    }
}
