/*
 * Copyright 2015 Devmil Solutions
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.devmil.paperlaunch

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.widget.Toolbar
import de.devmil.paperlaunch.service.LauncherOverlayService
import de.devmil.paperlaunch.view.fragments.EditFolderFragment

class MainActivity : Activity() {

    private var mToolbar: Toolbar? = null
    private var mFragment: EditFolderFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LauncherOverlayService.launch(this)
        setContentView(R.layout.activity_main)

        mToolbar = findViewById(R.id.activity_main_toolbar) as Toolbar
        mFragment = fragmentManager.findFragmentById(R.id.activity_main_editfolder_fragment) as EditFolderFragment

        setActionBar(mToolbar)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val result = super.onCreateOptionsMenu(menu)

        val itemSettings = menu.add(R.string.title_activity_settings)
        itemSettings.setOnMenuItemClickListener {
            val settingsIntent = Intent(this@MainActivity, SettingsActivity::class.java)
            startActivity(settingsIntent)
            true
        }

        val itemAbout = menu.add(R.string.title_activity_about)
        itemAbout.setOnMenuItemClickListener {
            val settingsIntent = Intent(this@MainActivity, AboutActivity::class.java)
            startActivity(settingsIntent)
            true
        }

        return result
    }
}
