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
import android.view.MenuItem
import android.widget.Toolbar
import de.devmil.paperlaunch.service.LauncherOverlayService
import de.devmil.paperlaunch.utils.PermissionUtils
import de.devmil.paperlaunch.view.fragments.EditFolderFragment

class MainActivity : Activity() {

    private var toolbar: Toolbar? = null
    private var fragment: EditFolderFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LauncherOverlayService.launch(this)
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.activity_main_toolbar)
        fragment = fragmentManager.findFragmentById(R.id.activity_main_editfolder_fragment) as EditFolderFragment

        setActionBar(toolbar)

        PermissionUtils.checkOverlayPermissionAndRouteToSettingsIfNeeded(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            PermissionUtils.REQUEST_DRAW_OVERLAY_PERMISSION -> {
                LauncherOverlayService.permissionChanged(this)
                finish()
                startActivity(intent)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val result = super.onCreateOptionsMenu(menu)

        val itemSettings = menu.add(R.string.title_activity_settings)
        itemSettings.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        itemSettings.icon = getDrawable(R.mipmap.ic_settings_black_48dp)
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
